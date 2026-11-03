package com.xinyi.rongli.media.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.common.dto.MiniDtos;
import com.xinyi.rongli.auth.service.AuthService;
import com.xinyi.rongli.order.service.BookingService;
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
    @GetMapping("/files/{fileId}/content")
    public ResponseEntity<Resource> fileContent(@PathVariable Long fileId,
            @RequestParam(required = false) String token)
    {
        Path path = mediaService.resolveContent(fileId);
        Resource resource = new FileSystemResource(path);
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        try
        {
            String probed = Files.probeContentType(path);
            if (probed != null)
            {
                type = MediaType.parseMediaType(probed);
            }
        }
        catch (Exception ignored)
        {
            // 探测失败按二进制下载，不影响访问
        }
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=60")
                .body(resource);
    }
}
