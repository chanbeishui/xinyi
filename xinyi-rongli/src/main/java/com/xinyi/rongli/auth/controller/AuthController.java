package com.xinyi.rongli.auth.controller;

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
 * auth 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/common")
public class AuthController
{
    @Autowired
    private AuthService authService;

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
    @GetMapping("/profile")
    public Map<String, Object> profile()
    {
        return authService.profileOfCurrent();
    }
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
}
