package com.xinyi.common.utils.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.lang.reflect.Method;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.xinyi.common.exception.file.InvalidExtensionException;

/**
 * 上传校验与目录前缀的约束。
 *
 * <p>这两条都是「改错了不会报错、只会在线上慢慢显形」的规则：
 * 目录层级被压平会让生命周期规则失效，扩展名校验放松会让对象存储变成可执行文件托管点。</p>
 */
class FileUploadUtilsTest
{
    /** 1x1 的真 PNG */
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    @Test
    void pngWithCorrectHeaderShouldPass() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", PNG);
        // 不抛异常即为通过
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.IMAGE_EXTENSION);
    }

    @Test
    void executableRenamedToPngShouldBeRejected()
    {
        byte[] exe = new byte[] { 'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00 };
        MockMultipartFile file = new MockMultipartFile("file", "evil.png", "image/png", exe);
        assertThatThrownBy(() -> FileUploadUtils.assertAllowed(file, MimeTypeUtils.IMAGE_EXTENSION))
                .isInstanceOf(InvalidExtensionException.class);
    }

    @Test
    void nonImageExtensionShouldNotBeCheckedBySignature() throws Exception
    {
        // 文档类扩展名不做魔数校验，避免误伤正常附件
        MockMultipartFile file = new MockMultipartFile("file", "a.docx", "application/octet-stream",
                new byte[] { 0x50, 0x4B, 0x03, 0x04 });
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    @Test
    void multiLevelLogicalDirShouldBePreserved() throws Exception
    {
        // 业务分层前缀不能被压成最后一段
        assertThat(logicalDir("upload/rich-text")).isEqualTo("upload/rich-text");
        assertThat(logicalDir("upload/image")).isEqualTo("upload/image");
        // 改造前的本地绝对路径要收敛成逻辑前缀
        assertThat(logicalDir("D:/ruoyi/uploadPath/upload")).isEqualTo("upload");
        assertThat(logicalDir("D:\\ruoyi\\uploadPath\\avatar")).isEqualTo("avatar");
        // 越界路径直接退回兜底目录
        assertThat(logicalDir("../../etc")).isEqualTo(OssFileService.DIR_UPLOAD);
    }

    @Test
    void bizShouldMapToDistinctPrefix()
    {
        assertThat(OssFileService.dirForBiz("rich-text")).isEqualTo(OssFileService.DIR_RICH_TEXT);
        assertThat(OssFileService.dirForBiz("image")).isEqualTo(OssFileService.DIR_IMAGE);
        assertThat(OssFileService.dirForBiz("file")).isEqualTo(OssFileService.DIR_FILE);
        // 未知/缺省不能失败，落到通用的 upload/
        assertThat(OssFileService.dirForBiz(null)).isEqualTo(OssFileService.DIR_UPLOAD);
        assertThat(OssFileService.dirForBiz("whatever")).isEqualTo(OssFileService.DIR_UPLOAD);
    }

    private static String logicalDir(String dir) throws Exception
    {
        Method method = FileUploadUtils.class.getDeclaredMethod("logicalDir", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, dir);
    }
}
