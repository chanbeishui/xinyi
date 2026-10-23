package com.xinyi.rongli.controller;

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
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.service.AuthService;
import com.xinyi.rongli.service.BookingService;
import com.xinyi.rongli.service.MediaService;
import com.xinyi.rongli.service.MessageService;
import com.xinyi.system.service.ISysDictTypeService;

/**
 * 小程序通用接口：登录、会话、档案、字典、站点、上传、消息与隐私。
 *
 * <p>除登录、刷新令牌、公开配置外，全部要求 MINI 会话令牌；
 * 这些路径在 Spring Security 中放行，由 MiniAuthInterceptor 独立校验。</p>
 */
@RestController("rongliCommonController")
@RequestMapping("/api/common")
public class CommonController
{
    @Autowired
    private AuthService authService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private com.xinyi.rongli.service.FulfillService fulfillService;

    @Autowired
    private ISysDictTypeService dictTypeService;

    // ------------------------------------------------------------------ 登录与会话

    @PostMapping("/login/wechat")
    public Map<String, Object> login(@RequestBody MiniDtos.LoginReq req)
    {
        return authService.login(req);
    }

    @PostMapping("/token/refresh")
    public Map<String, Object> refresh(@RequestBody Map<String, String> body)
    {
        return authService.refresh(body == null ? null : body.get("refreshToken"));
    }

    @GetMapping("/session")
    public Map<String, Object> session()
    {
        return authService.currentSession();
    }

    @PostMapping("/session/logout")
    public Map<String, Object> logout(@RequestParam(defaultValue = "false") boolean all)
    {
        return authService.logout(all);
    }

    @PostMapping("/session/role")
    public Map<String, Object> switchRole(@RequestBody MiniDtos.RoleSwitchReq req)
    {
        return authService.switchRole(req);
    }

    @PostMapping("/session/station")
    public Map<String, Object> switchStation(@RequestBody MiniDtos.StationSwitchReq req)
    {
        return authService.switchStation(req);
    }

    // ------------------------------------------------------------------ 档案与手机号

    @GetMapping("/profile")
    public Map<String, Object> profile()
    {
        return authService.profileOfCurrent();
    }

    /** 小程序不支持 PATCH，这里同时接受 POST，语义与 PATCH 一致（局部更新） */
    @PatchMapping("/profile")
    @PostMapping("/profile")
    public Map<String, Object> patchProfile(@RequestBody MiniDtos.ProfilePatch req)
    {
        return authService.updateProfile(req);
    }

    @PostMapping("/phone/bind")
    public Map<String, Object> bindPhone(@RequestBody MiniDtos.PhoneReq req)
    {
        return authService.bindPhone(req);
    }

    /** 微信手机号授权：已登录账号补充手机号，并按手机号匹配员工账号 */
    @PostMapping("/phone/wechat")
    public Map<String, Object> bindWechatPhone(@RequestBody MiniDtos.WechatPhoneReq req)
    {
        return authService.bindWechatPhone(req);
    }

    @PostMapping("/phone/sms/send")
    public Map<String, Object> sendSms(@RequestBody(required = false) MiniDtos.SmsSendReq req)
    {
        return authService.sendSmsCode(req);
    }

    @PostMapping("/phone/sms/verify")
    public Map<String, Object> verifySms(@RequestBody MiniDtos.SmsVerifyReq req)
    {
        return authService.verifySmsCode(req);
    }

    // ------------------------------------------------------------------ 员工绑定
    // V6.4 起员工绑定只有一条路径：微信手机号授权（POST /phone/wechat）。
    // 旧的「提交绑定申请 / 查询申请 / 凭证兑换」三个端点已下线，
    // 相关表 vi_employee_binding_request、vi_employee_binding_credential 已删除。

    // ------------------------------------------------------------------ 字典与站点

    @GetMapping("/dict/{dictType}")
    public Map<String, Object> dict(@PathVariable String dictType)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        var data = dictTypeService.selectDictDataByType(dictType);
        if (data != null)
        {
            data.forEach(d ->
            {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", d.getDictLabel());
                item.put("value", d.getDictValue());
                item.put("sort", d.getDictSort());
                item.put("cssClass", d.getListClass());
                items.add(item);
            });
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("dictType", dictType);
        view.put("total", items.size());
        view.put("items", items);
        return view;
    }

    @GetMapping("/stations")
    public List<Map<String, Object>> stations()
    {
        return bookingService.visibleStations();
    }

    // ------------------------------------------------------------------ 上传与文件

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

    /** 文件内容读取：仍需通过 access 的权限判定，直接复用服务层校验 */
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

    // ------------------------------------------------------------------ 消息与隐私

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

    // ------------------------------------------------------------------ 联系通道

    @PostMapping("/orders/{orderNo}/contact-access")
    public Map<String, Object> contactAccess(@PathVariable String orderNo,
            @RequestBody(required = false) DriverDtos.ContactAccessReq req)
    {
        return fulfillService.contactAccess(orderNo, req == null ? null : req.purpose);
    }
}
