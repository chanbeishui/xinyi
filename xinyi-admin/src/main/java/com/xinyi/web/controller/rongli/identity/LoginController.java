package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.SessionResponse;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.MiniLoginService;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统一微信登录（API_001_post，PUBLIC）。只接受 wx.login 临时 code；服务端换取可信 AppID/OpenID。
 * 严格拒绝未知字段（additionalProperties:false）：请求体含 role/openid/sysUserId 等一律 400，
 * 绝不据客户端字段授予角色或身份（TC-002 / TC-R62-002）。响应 no-store；明文令牌仅此处返回一次。
 */
@RestController
@RequestMapping("/api/common")
public class LoginController {

    private final MiniLoginService loginService;

    public LoginController(MiniLoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login/wechat")
    public ResponseEntity<ViSuccess<SessionResponse>> login(@RequestBody Map<String, Object> body,
                                                            HttpServletRequest request) {
        String code = requireOnlyCode(body);
        SessionResponse session = loginService.login(code, request.getHeader("User-Agent"));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ViSuccess.ok(session));
    }

    private String requireOnlyCode(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!"code".equals(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "登录请求只接受 code 字段",
                        List.of(Map.of("field", key, "reasonCode", "UNKNOWN_FIELD",
                                "message", "不接受角色/OpenID/sysUserId 等身份字段")),
                        null, null);
            }
        }
        Object code = body.get("code");
        if (!(code instanceof String s) || s.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少有效的微信登录 code",
                    List.of(Map.of("field", "code", "reasonCode", "REQUIRED", "message", "code 必填且非空")),
                    null, null);
        }
        return s.trim();
    }
}
