package com.xinyi.web.controller.common;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.file.OssFileService;

/**
 * 历史图片地址兼容层。
 *
 * <p>改造前 RuoYi 把用户文件写在服务器 {@code ruoyi.profile} 目录，数据库里存的是
 * {@code /profile/upload/2026/01/01/a.png} 这种相对地址，由静态资源映射直接出图。
 * 现在本地磁盘不再保存任何用户文件，这个前缀如果直接删掉，历史头像和富文本图片会整片 404。</p>
 *
 * <p>所以保留同一路径，但内容改为从对象存储取：{@code /profile/x} 对应对象 key
 * {@code x}。新上传的文件返回的是 OSS 直链，不经过这里。</p>
 *
 * <p>返回 404/400 一律用 {@code setStatus}，不要用 {@code sendError}：
 * 后者会转派到 {@code /error}，而 {@code /error} 需要认证，最终会变成
 * 「HTTP 200 + body.code=401」这种看不懂的响应。</p>
 */
@RestController
public class ProfileResourceController
{
    private static final Logger log = LoggerFactory.getLogger(ProfileResourceController.class);

    private static final String PREFIX = "/profile/";

    @Autowired
    private OssFileService ossFileService;

    @GetMapping("/profile/**")
    public void read(HttpServletRequest request, HttpServletResponse response)
    {
        String uri = request.getRequestURI();
        int idx = uri.indexOf(PREFIX);
        if (idx < 0)
        {
            response.setStatus(404);
            return;
        }
        String key = uri.substring(idx + PREFIX.length());
        // 防目录穿越；对象 key 里也不该出现 ..
        if (key.isEmpty() || key.contains(".."))
        {
            response.setStatus(400);
            return;
        }

        if (!ossFileService.exists(key))
        {
            response.setStatus(404);
            return;
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM);
        response.setContentType(mediaType.toString());
        // 头像/富文本图是低频变更的静态资源，让浏览器缓存一天，避免每次都打到后端
        response.setHeader("Cache-Control", "public, max-age=86400");

        try
        {
            ossFileService.writeTo(key, response.getOutputStream());
        }
        catch (IOException e)
        {
            // 响应已开始输出时无法再改状态码，只能记日志
            log.warn("读取历史图片失败：{}", key, e);
        }
    }
}
