package com.xinyi.rongli.notification.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
 * notification 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/common")
public class MessageController
{
    @Autowired
    private MessageService messageService;

    @GetMapping("/messages")
    public Map<String, Object> messages(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "false") boolean unreadOnly)
    {
        return messageService.myMessages(pageNum, pageSize, unreadOnly);
    }
    @PostMapping("/messages/{messageId}/read")
    public Map<String, Object> readMessage(@PathVariable Long messageId)
    {
        return messageService.markRead(messageId);
    }
    @PostMapping("/privacy-cases")
    public Map<String, Object> createPrivacy(@RequestBody MiniDtos.PrivacyReq req)
    {
        return messageService.createPrivacyCase(req == null ? null : req.reason,
                req == null ? null : req.orderNo);
    }
    @GetMapping("/privacy-cases")
    public Map<String, Object> privacyCases(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return messageService.myPrivacyCases(pageNum, pageSize);
    }
}
