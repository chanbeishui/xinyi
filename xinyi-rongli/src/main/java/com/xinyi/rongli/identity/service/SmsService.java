package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

/**
 * 短信兜底通道（API_003_post 发送 / API_004_post 核验，V6.4 主PRD23 + X04/X05）。
 * 仅已有微信 MINI 会话的普通手机号兜底；**短信核验绝不触发员工自动关联**（须本人微信手机号核验）。
 * 按当前已发布 LOGIN_OTP/PHONE_VERIFY 路由调用腾讯云/阿里云；**未配置则拒绝并提示，禁止固定成功**。
 *
 * I02 范围：第三方消息路由/模板/供应商发送由 X01–X07 落地；当前无已发布路由，故 fail-closed 拒绝
 * （VI_CAPABILITY_NOT_CONFIGURED），不伪造发送成功、不建立员工关联。验证码限频/TTL/一次消费、明文不落日志
 * 在路由接入时实现。改变已验证号码须暂停旧员工工作能力（由手机号绑定/换号流程处理）。
 */
@Service
public class SmsService {

    /** 短信兜底发送。无已发布路由 → 拒绝（不假成功）。 */
    public void send(long accountId, String phone, String scene) {
        if (phone == null || phone.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少手机号");
        }
        if (!"BIND_PHONE".equals(scene)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "不支持的短信场景");
        }
        throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED,
                "短信兜底通道尚未配置（无已发布 LOGIN_OTP/PHONE_VERIFY 路由）；请使用微信手机号授权");
    }

    /** 短信兜底核验。无已发布路由 → 拒绝；即便核验成功也不建立员工关联。 */
    public void verify(long accountId, String challengeId, String code) {
        if (challengeId == null || challengeId.isBlank() || code == null || code.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少验证码或挑战编号");
        }
        throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED,
                "短信兜底通道尚未配置（无已发布 LOGIN_OTP/PHONE_VERIFY 路由）；请使用微信手机号授权");
    }
}
