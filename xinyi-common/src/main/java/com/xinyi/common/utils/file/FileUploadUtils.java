package com.xinyi.common.utils.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.common.exception.UtilException;
import com.xinyi.common.exception.file.FileNameLengthLimitExceededException;
import com.xinyi.common.exception.file.FileSizeLimitExceededException;
import com.xinyi.common.exception.file.InvalidExtensionException;
import com.xinyi.common.utils.DateUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.spring.SpringUtils;
import com.xinyi.common.utils.uuid.IdUtils;
import com.xinyi.common.utils.uuid.Seq;

/**
 * 文件上传工具类
 *
 * <p>只做「校验 + 命名 + 转发」，落地一律交给 {@link OssFileService}。
 * 服务器本地磁盘不保存任何用户文件，所以这里不再有 baseDir 目录的概念，
 * 参数 {@code dir} 表示对象存储里的逻辑前缀（upload / avatar / import）。</p>
 *
 * @author ruoyi
 */
public class FileUploadUtils
{
    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024L;

    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /** 能靠文件头识别真伪的图片扩展名；不在此列的扩展名不做内容校验 */
    private static final Set<String> IMAGE_SIGNATURE_EXTENSIONS = Set.of("bmp", "gif", "jpg", "jpeg", "png");

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件的可访问地址
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException
    {
        try
        {
            return upload(OssFileService.DIR_UPLOAD, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 上传到指定逻辑目录
     *
     * @param dir  逻辑目录（upload / avatar / import）
     * @param file 上传的文件
     * @return 文件的可访问地址
     * @throws IOException
     */
    public static final String upload(String dir, MultipartFile file) throws IOException
    {
        try
        {
            return upload(dir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param dir 逻辑目录
     * @param file 上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功后的访问地址
     */
    public static final String upload(String dir, MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        return upload(dir, file, allowedExtension, false);
    }

    /**
     * 文件上传
     *
     * @param dir 逻辑目录
     * @param file 上传的文件
     * @param useCustomNaming 系统自定义文件名
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功后的访问地址
     */
    public static final String upload(String dir, MultipartFile file, String[] allowedExtension, boolean useCustomNaming)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException
    {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }
        return SpringUtils.getBean(OssFileService.class)
                .upload(file, logicalDir(dir), allowedExtension, useCustomNaming);
    }

    /**
     * 规范化逻辑目录。
     *
     * <p>改造前调用点传的是 {@code D:/ruoyi/uploadPath/upload} 这种本地路径，需要剥掉磁盘部分；
     * 现在调用点传的是 {@code upload/rich-text} 这类多级前缀，必须原样保留——
     * 早先的实现只取最后一段，会把它压成 {@code rich-text}，业务分层就失效了。</p>
     */
    private static String logicalDir(String dir)
    {
        if (StringUtils.isEmpty(dir))
        {
            return OssFileService.DIR_UPLOAD;
        }
        String d = dir.trim().replace('\\', '/');
        // 兼容改造前传进来的绝对路径：D:/ruoyi/uploadPath/upload
        int idx = d.indexOf("uploadPath/");
        if (idx >= 0)
        {
            d = d.substring(idx + "uploadPath/".length());
        }
        // 剥盘符
        if (d.length() > 1 && d.charAt(1) == ':')
        {
            d = d.substring(2);
        }
        d = d.replaceAll("/{2,}", "/").replaceAll("^/+", "").replaceAll("/+$", "");
        if (StringUtils.isEmpty(d) || d.contains("..") || d.contains(":"))
        {
            return OssFileService.DIR_UPLOAD;
        }
        return d;
    }

    /**
     * 编码文件名(日期格式目录 + 原文件名 + 序列值 + 后缀)
     */
    public static final String extractFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}_{}.{}", DateUtils.datePath(), FilenameUtils.getBaseName(file.getOriginalFilename()), Seq.getId(Seq.uploadSeqType), getExtension(file));
    }

    /**
     * 编编码文件名(日期格式目录 + UUID + 后缀)
     */
    public static final String uuidFilename(MultipartFile file)
    {
        return StringUtils.format("{}/{}.{}", DateUtils.datePath(), IdUtils.fastSimpleUUID(), getExtension(file));
    }

    /**
     * 文件大小校验
     *
     * @param file 上传的文件
     * @throws FileSizeLimitExceededException 超出最大大小
     * @throws InvalidExtensionException 类型不允许
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException
    {
        long size = file.getSize();
        if (size > DEFAULT_MAX_SIZE)
        {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension))
        {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        fileName);
            }
            else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION)
            {
                throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
                        fileName);
            }
            else
            {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }

        assertImageSignature(file, extension);
    }

    /**
     * 图片类扩展名必须与真实文件头一致。
     *
     * <p>只看扩展名挡不住「把 exe 改名成 png」：文件会原样存进对象存储，一旦被当成静态资源
     * 直出，等于提供了一个可执行文件托管点。这里按魔数核对，认不出任何已知图片签名就拒绝。
     * 非图片扩展名（文档、压缩包等）不做此校验，避免误伤。</p>
     */
    private static void assertImageSignature(MultipartFile file, String extension) throws InvalidExtensionException
    {
        if (StringUtils.isEmpty(extension))
        {
            return;
        }
        String ext = extension.toLowerCase();
        if (!IMAGE_SIGNATURE_EXTENSIONS.contains(ext))
        {
            return;
        }
        if (matchesImageSignature(readHead(file, 12), ext))
        {
            return;
        }
        throw new InvalidExtensionException.InvalidImageExtensionException(MimeTypeUtils.IMAGE_EXTENSION, extension,
                file.getOriginalFilename());
    }

    /** 读文件头若干字节；读不满（极小文件）时按实际长度返回 */
    private static byte[] readHead(MultipartFile file, int length)
    {
        try (InputStream in = file.getInputStream())
        {
            byte[] buf = new byte[length];
            int off = 0;
            while (off < length)
            {
                int n = in.read(buf, off, length - off);
                if (n < 0)
                {
                    break;
                }
                off += n;
            }
            return off == 0 ? null : Arrays.copyOf(buf, off);
        }
        catch (IOException e)
        {
            throw new UtilException("读取上传文件失败：" + e.getMessage());
        }
    }

    private static boolean matchesImageSignature(byte[] head, String ext)
    {
        if (head == null || head.length < 2)
        {
            return false;
        }
        switch (ext)
        {
            case "jpg":
            case "jpeg":
                return head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8
                        && (head[2] & 0xFF) == 0xFF;
            case "png":
                return head.length >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G'
                        && (head[4] & 0xFF) == 0x0D && (head[5] & 0xFF) == 0x0A && (head[6] & 0xFF) == 0x1A
                        && (head[7] & 0xFF) == 0x0A;
            case "gif":
                return head.length >= 4 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8';
            case "bmp":
                return head[0] == 'B' && head[1] == 'M';
            case "webp":
                return head.length >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                        && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            default:
                return true;
        }
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension)
    {
        for (String str : allowedExtension)
        {
            if (str.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件名的后缀
     */
    public static final String getExtension(MultipartFile file)
    {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension))
        {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        return extension;
    }
}
