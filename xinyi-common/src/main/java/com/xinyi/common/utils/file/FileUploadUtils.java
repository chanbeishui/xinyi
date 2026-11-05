package com.xinyi.common.utils.file;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
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
     * 把历史调用方传进来的绝对路径收敛成逻辑目录。
     *
     * <p>改造前调用点传的是 {@code D:/ruoyi/uploadPath/upload} 这种本地路径，
     * 现在只取最后一段作为 OSS 前缀，避免把整串磁盘路径写进对象 key。</p>
     */
    private static String logicalDir(String dir)
    {
        if (StringUtils.isEmpty(dir))
        {
            return OssFileService.DIR_UPLOAD;
        }
        String d = dir.replace('\\', '/').replaceAll("/+$", "");
        int i = d.lastIndexOf('/');
        String last = i < 0 ? d : d.substring(i + 1);
        if (StringUtils.isEmpty(last) || last.contains(":"))
        {
            return OssFileService.DIR_UPLOAD;
        }
        return last;
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
