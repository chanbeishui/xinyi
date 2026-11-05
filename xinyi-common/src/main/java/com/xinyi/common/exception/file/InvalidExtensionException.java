package com.xinyi.common.exception.file;

import java.util.Arrays;

/**
 * 文件上传无效扩展名异常类
 * 
 * @author ruoyi
 */
public class InvalidExtensionException extends FileUploadException
{
    private static final long serialVersionUID = 1L;

    private String[] allowedExtension;
    private String extension;
    private String filename;

    public InvalidExtensionException(String[] allowedExtension, String extension, String filename)
    {
        super("文件[" + filename + "]后缀[" + extension + "]不正确，请上传" + Arrays.toString(allowedExtension) + "格式");
        this.allowedExtension = allowedExtension;
        this.extension = extension;
        this.filename = filename;
    }

    private InvalidExtensionException(String message, String[] allowedExtension, String extension, String filename)
    {
        super(message);
        this.allowedExtension = allowedExtension;
        this.extension = extension;
        this.filename = filename;
    }

    /**
     * 后缀在允许列表里，但文件头是另一种图片。
     *
     * <p>复用「后缀不正确」的文案会把人带偏：前端裁剪器固定输出 PNG 却沿用了 .jpg 文件名时，
     * 用户看到「jpg 后缀不正确」只会以为自己传错了图。这里把真实类型和两种解决办法都写出来。</p>
     */
    public static InvalidExtensionException imageContentMismatch(String[] allowedExtension, String extension,
            String detected, String filename)
    {
        String actual = detected.toUpperCase();
        return new InvalidExtensionException(
                "图片[" + filename + "]的后缀是 ." + extension + "，但文件内容实际是 " + actual
                        + "。请把图片另存为标准 " + extension.toUpperCase() + " 再上传，"
                        + "或把文件名后缀改成 ." + detected + " 后重试",
                allowedExtension, extension, filename);
    }

    public String[] getAllowedExtension()
    {
        return allowedExtension;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getFilename()
    {
        return filename;
    }

    public static class InvalidImageExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidImageExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidFlashExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidFlashExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidMediaExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidMediaExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    public static class InvalidVideoExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        public InvalidVideoExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }
}
