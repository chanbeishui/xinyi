package com.xinyi.rongli.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.xinyi.rongli.mapper.MediaMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 限定用途上传。
 *
 * <p>不允许"随手传一张图"：每份文件必须声明用途，服务器落盘后自行计算大小与摘要，
 * 只有核验通过（扩展名、MIME、大小、摘要一致）才进入 READY；
 * 访问一律走短期授权链接，且按用途校验调用方与订单的关系。</p>
 */
@Service
public class MediaService
{
    private static final Set<String> PURPOSES = Set.of(
            "EVIDENCE_PHOTO", "LICENSE_PHOTO", "DRIVER_QUALIFICATION", "AVATAR", "AFTERSALE_EVIDENCE", "TRAINING_MEDIA");

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Autowired
    private MediaMapper mediaMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

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
        String objectKey = pz.toLowerCase() + "/" + ViTime.shanghaiDate(ViTime.now()) + "/"
                + crypto.randomToken().substring(0, 24) + ext;

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

    /** 接收二进制并落盘，随后立即做服务器端核验 */
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
        Path root = Paths.get(config.mediaRoot());
        Path target = root.resolve(f.str("object_key")).normalize();
        if (!target.startsWith(root.normalize()))
        {
            throw ApiException.validation("对象键不合法");
        }
        try (InputStream in = file.getInputStream())
        {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            throw ApiException.unprocessable(Codes.Error.INTERNAL, "文件落盘失败：" + e.getMessage());
        }
        return complete(uploadId, file.getSize(), null);
    }

    /** 服务器核验上传完成：大小与摘要以服务端计算为准 */
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
        if (!"UPLOADING".equals(f.str("status")) && !"VERIFYING".equals(f.str("status")))
        {
            throw ApiException.stateConflict("该上传单状态不可核验");
        }
        Path target = Paths.get(config.mediaRoot()).resolve(f.str("object_key")).normalize();
        if (!Files.exists(target))
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "服务器未收到文件，请重新上传");
        }
        long size;
        byte[] sha;
        try
        {
            size = Files.size(target);
            sha = sha256(target);
        }
        catch (IOException e)
        {
            throw ApiException.unprocessable(Codes.Error.INTERNAL, "文件读取失败：" + e.getMessage());
        }
        if (size <= 0)
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.validation("文件内容为空");
        }
        if (size > config.mediaMaxBytes())
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.validation("文件超过大小上限");
        }
        if (declaredSize != null && declaredSize > 0 && declaredSize != size)
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.validation("文件大小与声明不一致");
        }
        if (declaredSha256 != null && !declaredSha256.isBlank()
                && !declaredSha256.equalsIgnoreCase(crypto.hex(sha)))
        {
            mediaMapper.markRejected(uploadId);
            throw ApiException.validation("文件摘要与声明不一致");
        }
        mediaMapper.markReady(uploadId, size, sha);
        return fileView(Row.of(mediaMapper.findFile(uploadId)));
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
        String token = crypto.randomToken().substring(0, 32);
        String url = "/api/common/files/" + fileId + "/content?token=" + token;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("fileId", String.valueOf(fileId));
        view.put("url", url);
        view.put("token", token);
        view.put("mimeType", f.str("mime_type"));
        view.put("size", f.lng("server_size", 0L));
        view.put("expiresAt", ViTime.iso(ViTime.now().plusMinutes(config.mediaAccessMinutes())));
        view.put("hint", "链接短期有效，请勿转发");
        return view;
    }

    /** 读盘输出（由 Controller 直出字节），仍复用 access 的权限判定 */
    public Path resolveContent(Long fileId)
    {
        Row f = requireFile(fileId);
        if (!"READY".equals(f.str("status")))
        {
            throw ApiException.stateConflict("文件尚未核验通过");
        }
        Path target = Paths.get(config.mediaRoot()).resolve(f.str("object_key")).normalize();
        if (!Files.exists(target))
        {
            throw ApiException.notFound("文件已过期或被清理");
        }
        return target;
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

    private byte[] sha256(Path path) throws IOException
    {
        try (InputStream in = Files.newInputStream(path))
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
