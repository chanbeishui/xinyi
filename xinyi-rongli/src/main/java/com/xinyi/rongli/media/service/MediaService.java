package com.xinyi.rongli.media.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.xinyi.rongli.media.mapper.MediaMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.Codes;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.MiniContext;
import com.xinyi.rongli.common.support.MiniPrincipal;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * 限定用途上传。
 *
 * <p>不允许"随手传一张图"：每份文件必须声明用途，上传到对象存储后由服务端复核大小与摘要，
 * 只有核验通过才进入 READY；访问一律走短期签名链接，且按用途校验调用方与订单的关系。</p>
 *
 * <p>存储只走 aliyun-oss（x-file-storage），不提供本地磁盘实现：服务器磁盘上的影像无法随实例迁移，
 * 也无法在磁盘损坏后恢复，因此 {@code vi_file.object_key} 始终指向 OSS 对象，
 * 本地磁盘方案已在本次改造中整体移除。</p>
 */
@Service
public class MediaService
{
    private static final Set<String> PURPOSES = Set.of(
            "EVIDENCE_PHOTO", "LICENSE_PHOTO", "DRIVER_QUALIFICATION", "AVATAR", "AFTERSALE_EVIDENCE", "TRAINING_MEDIA");

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    /** 对象 key 里的日期目录格式，与框架层的 upload/avatar/download 保持同一套 yyyy/MM/dd */
    private static final DateTimeFormatter KEY_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** key 里订单段的最大长度，防止异常订单号把 key 撑过头 */
    private static final int MAX_ORDER_SEGMENT = 40;

    @Autowired
    private MediaMapper mediaMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 平台标识必须显式带上。
     *
     * <p>x-file-storage 的下载/删除路径是 `fileInfo.getPlatform()` 直查平台，**不会回退到
     * default-platform**；不设就报「没有找到对应的存储平台！platform:null」。
     * default-platform 只在上传时的 {@code of()} 里生效，读写两条路的口径不一致，这里统一补上。</p>
     */
    @Value("${dromara.x-file-storage.default-platform:aliyun-oss-1}")
    private String storagePlatform;

    /** 申请上传：返回 uploadId 与落盘地址参数，此时文件尚不可用 */
    @Transactional
    public Map<String, Object> requestUpload(String purpose, String mimeType, Long size, String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        String pz = purpose == null ? "" : purpose.trim().toUpperCase();
        if (!PURPOSES.contains(pz))
        {
            throw ApiException.validation("上传用途不合法");
        }
        String mime = mimeType == null ? "" : mimeType.trim().toLowerCase();
        if (!IMAGE_TYPES.contains(mime))
        {
            throw ApiException.validation("仅支持 jpg/png/webp 图片");
        }
        if (size != null && size > config.mediaMaxBytes())
        {
            throw ApiException.validation("单个文件不能超过 " + (config.mediaMaxBytes() / 1024 / 1024) + "MB");
        }
        Long orderId = null;
        if (orderNo != null && !orderNo.isBlank())
        {
            Row order = Row.of(orderMapper.findDetailByNo(orderNo));
            if (order.isEmpty())
            {
                throw ApiException.notFound("订单不存在");
            }
            orderId = order.lng("id");
        }
        String ext = switch (mime)
        {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String objectKey = buildObjectKey(pz, orderNo, orderId, ext);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uploaderAccountId", p.accountId);
        row.put("purpose", pz);
        row.put("orderId", orderId);
        row.put("applicationId", null);
        row.put("objectKey", objectKey);
        row.put("mimeType", mime);
        row.put("retainUntil", ViTime.now().plusDays(90));
        mediaMapper.insertFile(row);
        Long fileId = ((Number) row.get("id")).longValue();

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("uploadId", String.valueOf(fileId));
        view.put("objectKey", objectKey);
        view.put("purpose", pz);
        view.put("mimeType", mime);
        view.put("maxBytes", config.mediaMaxBytes());
        view.put("uploadPath", "/api/common/uploads/" + fileId + "/content");
        view.put("method", "POST");
        view.put("field", "file");
        view.put("hint", "请以 multipart/form-data 上传到 uploadPath，完成后调用 complete 接口");
        return view;
    }

    /** 接收二进制并上传到对象存储，随后立即做服务器端核验 */
    @Transactional
    public Map<String, Object> uploadContent(Long uploadId, MultipartFile file)
    {
        MiniPrincipal p = MiniContext.require();
        Row f = requireFile(uploadId);
        if (!Objects.equals(p.accountId, f.lng("uploader_account_id")))
        {
            throw ApiException.forbidden("只能上传自己申请的文件");
        }
        if (!"UPLOADING".equals(f.str("status")))
        {
            throw ApiException.stateConflict("该上传单已处理，请重新申请");
        }
        if (file == null || file.isEmpty())
        {
            throw ApiException.validation("请选择要上传的文件");
        }
        if (file.getSize() > config.mediaMaxBytes())
        {
            throw ApiException.validation("单个文件不能超过 " + (config.mediaMaxBytes() / 1024 / 1024) + "MB");
        }
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!IMAGE_TYPES.contains(mime))
        {
            throw ApiException.validation("仅支持 jpg/png/webp 图片");
        }
        // 摘要以服务端读取的字节为准，不用客户端声明值；MultipartFile 支持重复读取
        byte[] sha;
        try (InputStream in = file.getInputStream())
        {
            sha = sha256(in);
        }
        catch (IOException e)
        {
            throw ApiException.unprocessable(Codes.Error.INTERNAL, "文件读取失败：" + e.getMessage());
        }
        String objectKey = f.str("object_key");
        FileInfo uploaded;
        try
        {
            uploaded = fileStorageService.of(file)
                    .setPlatform(storagePlatform)
                    .setPath(directoryOf(objectKey))
                    .setSaveFilename(fileNameOf(objectKey))
                    .upload();
        }
        catch (RuntimeException e)
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.unprocessable(Codes.Error.INTERNAL, "影像上传到对象存储失败：" + e.getMessage());
        }
        if (uploaded == null)
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.unprocessable(Codes.Error.INTERNAL,
                    "对象存储未启用，请先配置 OSS 凭据后再上传");
        }
        long serverSize = uploaded.getSize() == null ? file.getSize() : uploaded.getSize();
        mediaMapper.markReady(uploadId, serverSize, sha);
        return fileView(Row.of(mediaMapper.findFile(uploadId)));
    }

    /**
     * 兼容旧的两步式调用。
     *
     * <p>上传路径（{@link #uploadContent}）在写入对象存储时已完成大小与摘要复核并置为 READY，
     * 能走到这里的只有"申请了但没真正上传"的情况。不再像本地磁盘时代那样现算摘要并放行，
     * 否则等于绕开服务端复核。</p>
     */
    @Transactional
    public Map<String, Object> complete(Long uploadId, Long declaredSize, String declaredSha256)
    {
        MiniPrincipal p = MiniContext.require();
        Row f = requireFile(uploadId);
        if (!Objects.equals(p.accountId, f.lng("uploader_account_id")))
        {
            throw ApiException.forbidden("只能核验自己申请的文件");
        }
        if ("READY".equals(f.str("status")))
        {
            return fileView(f);
        }
        mediaMapper.markRejected(uploadId);
        throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "该上传单未完成服务端核验，请重新上传");
    }

    public Map<String, Object> fileStatus(Long fileId)
    {
        MiniPrincipal p = MiniContext.require();
        Row f = requireFile(fileId);
        if (!Objects.equals(p.accountId, f.lng("uploader_account_id")))
        {
            throw ApiException.forbidden("无权查看该文件");
        }
        return fileView(f);
    }

    /** 短期访问授权：用途决定可见范围，图片必须已核验 */
    @Transactional
    public Map<String, Object> access(Long fileId)
    {
        MiniPrincipal p = MiniContext.require();
        Row f = requireFile(fileId);
        if (!"READY".equals(f.str("status")))
        {
            throw ApiException.stateConflict("文件尚未核验通过，不能访问");
        }
        boolean owner = Objects.equals(p.accountId, f.lng("uploader_account_id"));
        boolean sameOrder = false;
        Long orderId = f.lng("order_id");
        if (!owner && orderId != null)
        {
            Row order = Row.of(orderMapper.findDetailById(orderId));
            sameOrder = !order.isEmpty()
                    && (Objects.equals(p.accountId, order.lng("owner_account_id"))
                            || Objects.equals(p.stationId, order.lng("station_id")));
        }
        if (!owner && !sameOrder)
        {
            throw ApiException.forbidden("无权访问该文件");
        }
        long expireMillis = System.currentTimeMillis() + config.mediaAccessMinutes() * 60_000L;
        FileInfo info = fileInfoOf(f);
        // 签名链接由对象存储直接校验，不再依赖后端校验一个从来没验过的 token 参数
        String url = fileStorageService.isSupportPresignedUrl()
                ? fileStorageService.generatePresignedUrl(info, new java.util.Date(expireMillis))
                : "/api/common/files/" + fileId + "/content";
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("fileId", String.valueOf(fileId));
        view.put("url", url);
        view.put("signed", Boolean.TRUE);
        view.put("mimeType", f.str("mime_type"));
        view.put("size", f.lng("server_size", 0L));
        view.put("expiresAt", ViTime.iso(ViTime.now().plusMinutes(config.mediaAccessMinutes())));
        view.put("hint", "链接短期有效，请勿转发");
        return view;
    }

    /** 读取前校验：必须是已核验文件，返回登记行供调用方取 mime 与用途 */
    public Row requireReadyFile(Long fileId)
    {
        Row f = requireFile(fileId);
        if (!"READY".equals(f.str("status")))
        {
            throw ApiException.stateConflict("文件尚未核验通过");
        }
        return f;
    }

    /**
     * 读前探活：对象不存在或存储不可用时，在响应提交前就失败。
     *
     * <p>一旦开始往 {@code OutputStream} 写字节，响应头就发出去了，此时再抛异常只能给用户一个截断的文件；
     * 所以先做一次 HEAD 判定，宁可多一次请求也要保证错误是明确的 JSON。</p>
     */
    public void assertReadable(Row f)
    {
        try
        {
            if (!fileStorageService.exists(fileInfoOf(f)))
            {
                throw ApiException.notFound("文件已过期或被清理");
            }
        }
        catch (ApiException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw ApiException.unprocessable(Codes.Error.INTERNAL,
                    "对象存储不可用（请检查 OSS 凭据与对象是否存在）：" + e.getMessage());
        }
    }

    /** 从对象存储流式写出：不落临时文件，200MB 的培训视频也不会撑爆内存 */
    public void writeContent(Row f, OutputStream out)
    {
        fileStorageService.download(fileInfoOf(f)).outputStream(out);
    }

    public Map<String, Object> myFiles(int pageNum, int pageSize)
    {
        MiniPrincipal p = MiniContext.require();
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Map<String, Object> f : mediaMapper.listOfUploader(p.accountId, offset, limit))
        {
            list.add(fileView(Row.of(f)));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("rows", list);
        return result;
    }

    private Row requireFile(Long fileId)
    {
        Row f = Row.of(mediaMapper.findFile(fileId));
        if (f.isEmpty())
        {
            throw ApiException.notFound("文件不存在");
        }
        return f;
    }

    private Map<String, Object> fileView(Row f)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("fileId", String.valueOf(f.lng("id")));
        view.put("purpose", f.str("purpose"));
        view.put("status", f.str("status"));
        view.put("statusLabel", switch (f.str("status", ""))
        {
            case "UPLOADING" -> "待上传";
            case "VERIFYING" -> "核验中";
            case "READY" -> "可用";
            case "REJECTED" -> "已拒绝";
            default -> f.str("status");
        });
        view.put("mimeType", f.str("mime_type"));
        view.put("size", f.lng("server_size", 0L));
        view.put("orderId", f.lng("order_id") == null ? null : String.valueOf(f.lng("order_id")));
        view.put("createdAt", ViTime.iso(f.time("created_at")));
        return view;
    }

    private byte[] sha256(InputStream in) throws IOException
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0)
            {
                md.update(buf, 0, n);
            }
            return md.digest();
        }
        catch (java.security.NoSuchAlgorithmException e)
        {
            throw new IOException("SHA-256 不可用", e);
        }
    }

    /**
     * 由 object_key 还原对象存储定位信息。
     *
     * <p>{@code base-path} 配置为空，所以 object_key 就是 path + filename；
     * 两者必须拆开是因为 x-file-storage 的定位由 path 与 filename 共同决定，
     * 整串当 filename 会读写到不同对象。</p>
     */
    private FileInfo fileInfoOf(Row f)
    {
        String objectKey = f.str("object_key");
        return new FileInfo()
                .setPlatform(storagePlatform)
                .setBasePath("")
                .setPath(directoryOf(objectKey))
                .setFilename(fileNameOf(objectKey));
    }

    private String directoryOf(String objectKey)
    {
        int i = objectKey == null ? -1 : objectKey.lastIndexOf('/');
        return i < 0 ? "" : objectKey.substring(0, i + 1);
    }

    /**
     * 业务影像的对象 key：{@code 用途/[ord-订单号/]yyyy/MM/dd/用途_随机串.扩展名}。
     *
     * <p>三处刻意的设计：</p>
     * <ul>
     *   <li><b>订单号排在日期之前</b>。对象存储没有重命名，也不能按通配符筛「所有日期下的某订单」；
     *       订单段靠前才能一次前缀删除清掉整单影像（{@code evidence_photo/ord-RL20261105001/}），
     *       放到日期后面就只能一个日期一个日期地删。</li>
     *   <li><b>日期统一 {@code yyyy/MM/dd}</b>。框架层（upload、avatar、download）本来就是这套，
     *       影像原先用 {@code 2026-11-05}，同一个桶里两种层级风格，写清理脚本要兼容两套。</li>
     *   <li><b>文件名带用途前缀 + 12 位随机串</b>。纯随机串在桶里完全看不出是什么，
     *       带前缀便于人工排查；随机部分保证 key 不可枚举（订单号本身是可猜的）。</li>
     * </ul>
     *
     * <p>订单号会被清洗成 {@code [A-Za-z0-9_-]}，清洗后为空退回订单 id，都没有就不带这一段——
     * 头像、培训素材这类本来就不挂订单。</p>
     */
    private String buildObjectKey(String purpose, String orderNo, Long orderId, String ext)
    {
        String pz = purpose.toLowerCase();
        StringBuilder key = new StringBuilder(pz).append('/');
        String segment = orderSegment(orderNo, orderId);
        if (segment != null)
        {
            key.append(segment).append('/');
        }
        key.append(ViTime.shanghaiDate(ViTime.now()).format(KEY_DATE)).append('/');
        key.append(pz).append('_').append(crypto.randomToken(), 0, 12).append(ext);
        return key.toString();
    }

    private String orderSegment(String orderNo, Long orderId)
    {
        // 只留 key 安全的字符；连字符在首尾会拼出 ord--xxx 这种难看的段，一并去掉
        String safe = orderNo == null ? "" : orderNo.replaceAll("[^A-Za-z0-9_-]", "").replaceAll("^-+|-+$", "");
        if (safe.isEmpty() && orderId != null)
        {
            safe = String.valueOf(orderId);
        }
        if (safe.isEmpty())
        {
            return null;
        }
        if (safe.length() > MAX_ORDER_SEGMENT)
        {
            safe = safe.substring(0, MAX_ORDER_SEGMENT);
        }
        return "ord-" + safe;
    }

    private String fileNameOf(String objectKey)
    {
        int i = objectKey == null ? -1 : objectKey.lastIndexOf('/');
        return i < 0 ? objectKey : objectKey.substring(i + 1);
    }

    /** 供证据组关联照片前校验文件归属与用途 */
    public Row requireReadyImage(Long fileId, Long accountId, String expectedPurpose)
    {
        Row f = requireFile(fileId);
        if (!Objects.equals(accountId, f.lng("uploader_account_id")))
        {
            throw ApiException.forbidden("只能关联本人上传的照片");
        }
        if (!"READY".equals(f.str("status")))
        {
            throw ApiException.stateConflict("照片未核验通过，不能关联");
        }
        if (expectedPurpose != null && !expectedPurpose.equals(f.str("purpose")))
        {
            throw ApiException.validation("照片用途不匹配，应为 " + expectedPurpose);
        }
        return f;
    }

    String asJson(Object value)
    {
        return value == null ? null : JSON.toJSONString(value);
    }
}
