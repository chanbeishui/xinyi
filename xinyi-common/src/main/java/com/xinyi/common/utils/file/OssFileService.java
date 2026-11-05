package com.xinyi.common.utils.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.common.exception.file.FileSizeLimitExceededException;
import com.xinyi.common.exception.file.InvalidExtensionException;
import com.xinyi.common.utils.DateUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.uuid.IdUtils;

/**
 * 对象存储统一门面。
 *
 * <p>全项目只有一套存储：阿里云 OSS。服务器本地磁盘不再保存任何用户文件，
 * 也就不存在「迁服务器要先搬 upload 目录」这类问题。</p>
 *
 * <p>为什么是 Spring Bean 而不是纯静态工具：RuoYi 里 {@link FileUploadUtils}、
 * {@link FileUtils}、{@code ExcelUtil} 都是静态调用，拿不到注入的
 * {@link FileStorageService}；那些静态方法内部通过 {@code SpringUtils} 取本 Bean，
 * 从而让所有历史调用点零改动地落到 OSS 上。</p>
 *
 * <p><b>ACL 分两类，别混用：</b></p>
 * <ul>
 *   <li>通用上传（头像、富文本图片、Excel 导入提取的图片）：对象设为
 *       {@code public-read}，返回不带签名的直链。这类地址要长期存在于数据库、直接塞进
 *       {@code <img src>}，签名一旦过期头像就裂，所以只能公共读。</li>
 *   <li>荣利业务影像（证件、检测照片）：私有对象 + 后端签发短链，见
 *       {@code MediaService}，不走本类。</li>
 * </ul>
 */
@Component
public class OssFileService
{
    private static final Logger log = LoggerFactory.getLogger(OssFileService.class);

    /** 通用上传目录（调用方没声明业务时兜底），历史数据也都在这一层 */
    public static final String DIR_UPLOAD = "upload";

    /** 富文本编辑器插入的图片 */
    public static final String DIR_RICH_TEXT = "upload/rich-text";

    /** 表单里的图片（ImageUpload 组件） */
    public static final String DIR_IMAGE = "upload/image";

    /** 附件（FileUpload 组件上传的文档、压缩包等） */
    public static final String DIR_FILE = "upload/file";

    /** 头像目录 */
    public static final String DIR_AVATAR = "avatar";

    /** Excel 导入时从单元格里抽出来的图片 */
    public static final String DIR_IMPORT = "import";

    /** 导出文件（Excel 等），下载后可删 */
    public static final String DIR_DOWNLOAD = "download";

    /**
     * 前端声明上传用途的参数名。
     *
     * <p>三个上传组件都打同一个 {@code /common/upload}，服务端无法从请求里分辨是编辑器插图
     * 还是表单附件，所以由调用方带一个查询参数说明用途，再映射到不同前缀——
     * 这样不同用途可以分别设生命周期、分别统计容量，也不会全挤在 {@code upload/} 里。</p>
     */
    public static final String BIZ_PARAM = "biz";

    /**
     * 把前端声明的用途映射成对象前缀。未知取值一律落到 {@link #DIR_UPLOAD}，
     * 不因为前端漏传或传错而让上传失败。
     */
    public static String dirForBiz(String biz)
    {
        if (StringUtils.isEmpty(biz))
        {
            return DIR_UPLOAD;
        }
        switch (biz.trim().toLowerCase())
        {
            case "rich-text":
            case "richtext":
            case "editor":
                return DIR_RICH_TEXT;
            case "image":
            case "img":
            case "photo":
                return DIR_IMAGE;
            case "file":
            case "attachment":
            case "doc":
                return DIR_FILE;
            default:
                return DIR_UPLOAD;
        }
    }

    @Autowired(required = false)
    private FileStorageService fileStorageService;

    @Autowired(required = false)
    private FileStorageProperties properties;

    /**
     * 平台标识必须显式带上：下载/删除是 {@code fileInfo.getPlatform()} 直查平台，
     * 不会回退到 default-platform，不设就报「没有找到对应的存储平台！platform:null」。
     */
    @Value("${dromara.x-file-storage.default-platform:aliyun-oss-1}")
    private String platform;

    /** 通用上传对象的 ACL；留空表示跟随 Bucket（私有），此时图片直链会 403 */
    @Value("${xinyi.oss.public-acl:public-read}")
    private String publicAcl;

    /**
     * 上传一个表单文件。
     *
     * @param file            上传的文件
     * @param dir             逻辑目录，如 {@link #DIR_UPLOAD}
     * @param allowedExtension 允许的扩展名，null 表示不限制
     * @param uuidFilename     true 用 UUID 命名（头像这类不需要原名的），false 保留原名 + 序列号
     * @return 可直接访问的完整 URL
     */
    public String upload(MultipartFile file, String dir, String[] allowedExtension, boolean uuidFilename)
            throws FileSizeLimitExceededException, InvalidExtensionException
    {
        requireStorage();
        FileUploadUtils.assertAllowed(file, allowedExtension);

        String name = uuidFilename ? FileUploadUtils.uuidFilename(file) : FileUploadUtils.extractFilename(file);
        String key = join(dir, name);

        FileInfo info = baseUpload(fileStorageService.of(file), key).upload();
        return requireUrl(info, key);
    }

    /**
     * 上传一段字节，文件名由本方法生成（Excel 导入里从单元格抽出来的图片走这里）。
     *
     * @param data      文件内容
     * @param dir       逻辑目录
     * @param extension 扩展名，不含点
     * @return 可直接访问的完整 URL
     */
    public String uploadBytes(byte[] data, String dir, String extension)
    {
        String ext = StringUtils.isEmpty(extension) ? "jpg" : extension;
        return uploadBytesNamed(data, dir, DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + ext);
    }

    /**
     * 上传一段字节，使用指定文件名（导出文件这类需要保留可读名的场景）。
     *
     * @param data     文件内容
     * @param dir      逻辑目录
     * @param filename 相对文件名，含扩展名，允许带日期子目录
     * @return 可直接访问的完整 URL
     */
    public String uploadBytesNamed(byte[] data, String dir, String filename)
    {
        requireStorage();
        if (data == null || data.length == 0)
        {
            throw new IllegalArgumentException("文件内容为空");
        }
        String key = join(dir, filename);
        FileInfo info = baseUpload(fileStorageService.of(new ByteArrayInputStream(data)), key)
                .setOriginalFilename(fileNameOf(key))
                .upload();
        return requireUrl(info, key);
    }

    /**
     * 把对象内容写到响应流（替代原来的本地文件读取）。
     *
     * <p>先探存在再写：响应一旦开始输出，就没法再返回 JSON 错误。</p>
     */
    public void writeTo(String urlOrKey, OutputStream out) throws IOException
    {
        requireStorage();
        String key = keyOf(urlOrKey);
        FileInfo info = fileInfoOf(key);
        if (!fileStorageService.exists(info))
        {
            throw new IOException("文件不存在：" + key);
        }
        fileStorageService.download(info).outputStream(out);
    }

    /**
     * 对象是否存在。
     */
    public boolean exists(String urlOrKey)
    {
        String key = keyOf(urlOrKey);
        if (fileStorageService == null || StringUtils.isEmpty(key))
        {
            return false;
        }
        try
        {
            return fileStorageService.exists(fileInfoOf(key));
        }
        catch (RuntimeException e)
        {
            log.warn("探测对象是否存在失败：{}", key, e);
            return false;
        }
    }

    /**
     * 删除对象。传入完整 URL、{@code /profile/xxx} 旧路径或裸 key 都能识别。
     *
     * <p>删失败只记日志不抛：换头像、删附件这类操作不该因为对象已不存在而整体失败。</p>
     */
    public boolean delete(String urlOrKey)
    {
        String key = keyOf(urlOrKey);
        if (fileStorageService == null || StringUtils.isEmpty(key))
        {
            return false;
        }
        try
        {
            return fileStorageService.delete(fileInfoOf(key));
        }
        catch (RuntimeException e)
        {
            log.warn("删除对象失败：{}", key, e);
            return false;
        }
    }

    /**
     * 生成短链（私有对象用），{@code minutes} 分钟后失效。
     */
    public String presignedUrl(String urlOrKey, int minutes)
    {
        requireStorage();
        FileInfo info = fileInfoOf(keyOf(urlOrKey));
        return fileStorageService.generatePresignedUrl(info, new Date(System.currentTimeMillis() + minutes * 60_000L));
    }

    /**
     * 从完整 URL / 旧 {@code /profile/...} 路径反解出对象 key。
     *
     * <p>数据库里可能同时存在三种历史形态，都要能认：
     * {@code https://bucket.oss.../avatar/x.png}、{@code /profile/avatar/x.png}、
     * {@code avatar/x.png}。</p>
     */
    public String keyOf(String urlOrKey)
    {
        if (StringUtils.isEmpty(urlOrKey))
        {
            return null;
        }
        String s = urlOrKey.trim();
        int query = s.indexOf('?');
        if (query >= 0)
        {
            s = s.substring(0, query);
        }
        if (s.startsWith("http://") || s.startsWith("https://"))
        {
            int scheme = s.indexOf("://") + 3;
            int slash = s.indexOf('/', scheme);
            s = slash < 0 ? "" : s.substring(slash + 1);
        }
        // 旧数据的 /profile/upload/... 、以及 RuoYi 的 /profile 前缀
        while (s.startsWith("/"))
        {
            s = s.substring(1);
        }
        if (s.startsWith("profile/"))
        {
            s = s.substring("profile/".length());
        }
        return s;
    }

    // ------------------------------------------------------------------ 内部

    private org.dromara.x.file.storage.core.UploadPretreatment baseUpload(
            org.dromara.x.file.storage.core.UploadPretreatment pre, String key)
    {
        pre.setPlatform(platform)
                .setPath(directoryOf(key))
                .setSaveFilename(fileNameOf(key));
        if (StringUtils.isNotEmpty(publicAcl))
        {
            pre.setAcl(publicAcl);
        }
        return pre;
    }

    /**
     * 由 key 还原对象定位信息。basePath 配置为空，所以 key 就是 path + filename；
     * 必须拆开传，整串当 filename 会读写到不同对象。
     */
    private FileInfo fileInfoOf(String key)
    {
        return new FileInfo()
                .setPlatform(platform)
                .setBasePath("")
                .setPath(directoryOf(key))
                .setFilename(fileNameOf(key));
    }

    /**
     * 取可访问地址。库里的 domain 配好时 getUrl 直接可用；没配则按 bucket + endpoint 拼，
     * 避免把 null 或相对路径写进数据库（头像、富文本图片都依赖这个地址长期有效）。
     */
    private String requireUrl(FileInfo info, String key)
    {
        if (info == null)
        {
            throw new IllegalStateException("对象存储未启用，请先配置 OSS 凭据（RONGLI_OSS_*）");
        }
        String url = info.getUrl();
        if (StringUtils.isNotEmpty(url) && url.startsWith("http"))
        {
            return url;
        }
        String base = configuredBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            throw new IllegalStateException("对象存储地址无法拼接：请配置 RONGLI_OSS_DOMAIN（以 / 结尾）");
        }
        return base + key;
    }

    private String configuredBaseUrl()
    {
        if (properties == null || properties.getAliyunOss() == null || properties.getAliyunOss().isEmpty())
        {
            return null;
        }
        List<? extends FileStorageProperties.AliyunOssConfig> list = properties.getAliyunOss();
        FileStorageProperties.AliyunOssConfig cfg = null;
        for (FileStorageProperties.AliyunOssConfig item : list)
        {
            if (cfg == null)
            {
                cfg = item;
            }
            if (Objects.equals(platform, item.getPlatform()))
            {
                cfg = item;
                break;
            }
        }
        if (cfg == null)
        {
            return null;
        }
        String domain = cfg.getDomain();
        if (StringUtils.isNotEmpty(domain))
        {
            return domain.endsWith("/") ? domain : domain + "/";
        }
        String bucket = cfg.getBucketName();
        String endpoint = cfg.getEndPoint();
        if (StringUtils.isEmpty(bucket) || StringUtils.isEmpty(endpoint))
        {
            return null;
        }
        for (String scheme : new String[] { "https://", "http://" })
        {
            if (endpoint.startsWith(scheme))
            {
                endpoint = endpoint.substring(scheme.length());
            }
        }
        return "https://" + bucket + "." + endpoint.replaceAll("/+$", "") + "/";
    }

    private void requireStorage()
    {
        if (fileStorageService == null)
        {
            throw new IllegalStateException("对象存储未启用，请先配置 OSS 凭据（RONGLI_OSS_*）");
        }
    }

    private String join(String dir, String name)
    {
        if (StringUtils.isEmpty(dir))
        {
            return name;
        }
        return dir.replaceAll("^/+", "").replaceAll("/+$", "") + "/" + name;
    }

    private String directoryOf(String objectKey)
    {
        int i = objectKey == null ? -1 : objectKey.lastIndexOf('/');
        return i < 0 ? "" : objectKey.substring(0, i + 1);
    }

    private String fileNameOf(String objectKey)
    {
        int i = objectKey == null ? -1 : objectKey.lastIndexOf('/');
        return i < 0 ? objectKey : objectKey.substring(i + 1);
    }

    /**
     * 推断扩展名，供 {@link #uploadBytes} 调用方使用。
     */
    public static String extensionOf(String filename)
    {
        return StringUtils.isEmpty(filename) ? null : FilenameUtils.getExtension(filename);
    }

    /**
     * 供静态工具类判断是否已装配（未配置 OSS 时给出可读报错，而不是 NPE）。
     */
    public boolean isReady()
    {
        return fileStorageService != null;
    }

    /**
     * 读取对象为可重复读取的流，调用方负责关闭。
     */
    public InputStream openStream(String urlOrKey) throws IOException
    {
        byte[] bytes = readAllBytes(urlOrKey);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 对象全部字节。仅用于小文件（Excel 导入的单元格图片），大文件请走 {@link #writeTo}。
     */
    public byte[] readAllBytes(String urlOrKey) throws IOException
    {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        writeTo(urlOrKey, buffer);
        return buffer.toByteArray();
    }
}
