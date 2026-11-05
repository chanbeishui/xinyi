package com.xinyi.rongli.media.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.common.dto.MiniDtos;
import com.xinyi.rongli.auth.service.AuthService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.media.service.MediaService;
import com.xinyi.rongli.notification.service.MessageService;
import com.xinyi.system.service.ISysDictTypeService;
import com.xinyi.rongli.common.support.MiniAuthInterceptor;

/**
 * media 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/common")
public class MediaController
{
    @Autowired
    private MediaService mediaService;

    @PostMapping("/uploads")
    public Map<String, Object> requestUpload(@RequestBody MiniDtos.UploadReq req)
    {
        return mediaService.requestUpload(req == null ? null : req.purpose, req == null ? null : req.mimeType,
                req == null ? null : req.size, req == null ? null : req.orderNo);
    }
    @PostMapping("/uploads/{uploadId}/content")
    public Map<String, Object> uploadContent(@PathVariable Long uploadId,
            @RequestPart(value = "file", required = false) MultipartFile file)
    {
        return mediaService.uploadContent(uploadId, file);
    }
    @PostMapping("/uploads/{uploadId}/complete")
    public Map<String, Object> completeUpload(@PathVariable Long uploadId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        Long size = body == null || body.get("size") == null ? null
                : Long.valueOf(String.valueOf(body.get("size")));
        String sha = body == null || body.get("sha256") == null ? null : String.valueOf(body.get("sha256"));
        return mediaService.complete(uploadId, size, sha);
    }
    @GetMapping("/files/{fileId}")
    public Map<String, Object> fileStatus(@PathVariable Long fileId)
    {
        return mediaService.fileStatus(fileId);
    }
    @PostMapping("/files/{fileId}/access")
    public Map<String, Object> fileAccess(@PathVariable Long fileId)
    {
        return mediaService.access(fileId);
    }
    /**
     * 直出文件内容。
     *
     * <p>正常路径是 {@code access} 返回的 OSS 预签名链接由客户端直连；
     * 这个端点保留给不支持直连的场景（如内部预览），由后端从对象存储流式转发。</p>
     */
    @GetMapping("/files/{fileId}/content")
    public void fileContent(@PathVariable Long fileId, HttpServletResponse response) throws IOException
    {
        Row f = mediaService.requireReadyFile(fileId);
        mediaService.assertReadable(f);
        response.setContentType(f.str("mime_type", "application/octet-stream"));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=60");
        mediaService.writeContent(f, response.getOutputStream());
    }
}
