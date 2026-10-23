package com.xinyi.rongli.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.mapper.SmsMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.SmsClient;
import com.xinyi.rongli.support.SmsSettings;
import com.xinyi.rongli.support.ViTime;

/**
 * 短信发送与频控（G02）。
 *
 * <p>所有发送都先过频控再调供应商，成功与失败都落 {@code vi_sms_send_log}：
 * 失败也计数，避免用"发送失败立即重试"绕过间隔限制。
 * 频控按手机号摘要统计，同一号码换账号也无法绕过。</p>
 *
 * <p>未启用真实通道时（{@code rongli.sms.enabled=false}）不调用供应商，
 * 验证码由调用方回显，仅用于联调。</p>
 */
@Service
public class SmsService
{
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private SmsMapper smsMapper;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    /** 发送场景：登录/绑定手机号 */
    public static final String SCENE_LOGIN = "LOGIN";

    /** 发送场景：管理端连通性测试 */
    public static final String SCENE_TEST = "TEST";

    /**
     * 发送验证码。真实通道失败时抛 502，联调模式返回 delivered=false 由调用方回显。
     *
     * @return delivered=是否真正经供应商下发；provider=mock/ALIYUN/TENCENT
     */
    @Transactional
    public Map<String, Object> sendCode(Long accountId, String phone, String code, String scene)
    {
        SmsSettings settings = SmsSettings.load(config, crypto);
        byte[] lookup = Crypto.sha256("phone:" + phone);
        String masked = maskPhone(phone);
        LocalDateTime since = ViTime.now().minusHours(24);

        if (settings.enabled())
        {
            checkThrottle(settings, accountId, lookup, phone, masked, scene);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (!settings.enabled())
        {
            // 联调模式：不调用供应商，仅登记一条 MOCK 记录便于排查频控口径
            logRow(accountId, lookup, masked, scene, "MOCK", settings.templateCode(), "OK", null, null);
            result.put("delivered", false);
            result.put("provider", "MOCK");
            result.put("reason", "短信通道未启用（联调模式）");
            return result;
        }

        SmsClient.Result sent;
        try
        {
            sent = smsClient.send(settings, phone, code);
        }
        catch (RuntimeException ex)
        {
            logRow(accountId, lookup, masked, scene, settings.provider(), settings.templateCode(), "FAIL",
                    "NETWORK", truncate(ex.getMessage()));
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "短信发送失败，请稍后重试或改用微信手机号授权");
        }
        logRow(accountId, lookup, masked, scene, settings.provider(), settings.templateCode(),
                sent.ok() ? "OK" : "FAIL", sent.errorCode(), truncate(sent.errorMessage()));
        if (!sent.ok())
        {
            log.warn("短信发送被供应商拒绝 provider={} code={} message={}", sent.provider(), sent.errorCode(),
                    sent.errorMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "短信发送失败：" + readableProviderError(sent.errorCode(), sent.errorMessage()));
        }
        result.put("delivered", true);
        result.put("provider", settings.provider());
        result.put("expiresInSeconds", settings.effectiveExpire());
        return result;
    }

    /**
     * 频控：重发间隔 + 手机号日上限 + 账号日上限。
     *
     * <p>拦截时写一条 BLOCKED 记录，便于运营区分"用户没收到"与"被限流"。</p>
     */
    private void checkThrottle(SmsSettings settings, Long accountId, byte[] lookup, String phone, String masked,
            String scene)
    {
        LocalDateTime last = smsMapper.lastSentAt(lookup);
        if (last != null)
        {
            long elapsed = Duration.between(last, ViTime.now()).getSeconds();
            if (elapsed < settings.effectiveResend())
            {
                long wait = settings.effectiveResend() - elapsed;
                logRow(accountId, lookup, masked, scene, settings.provider(), settings.templateCode(), "BLOCKED",
                        "RESEND_TOO_SOON", "距上次发送 " + elapsed + " 秒");
                throw ApiException.validation("发送过于频繁，请 " + wait + " 秒后再试");
            }
        }
        int phoneCount = smsMapper.countByPhoneSince(lookup, ViTime.now().minusHours(24));
        if (phoneCount >= settings.effectiveDailyLimit())
        {
            logRow(accountId, lookup, masked, scene, settings.provider(), settings.templateCode(), "BLOCKED",
                    "DAILY_LIMIT", "24 小时内已发送 " + phoneCount + " 条");
            throw ApiException.validation("该手机号今日验证码发送次数已达上限，请明天再试或联系客服");
        }
        if (accountId != null)
        {
            int accountCount = smsMapper.countByAccountSince(accountId, ViTime.now().minusHours(24));
            if (accountCount >= settings.effectiveDailyLimit() * 2)
            {
                logRow(accountId, lookup, masked, scene, settings.provider(), settings.templateCode(), "BLOCKED",
                        "ACCOUNT_LIMIT", "账号 24 小时内已发送 " + accountCount + " 条");
                throw ApiException.validation("当前账号验证码请求过多，请明天再试");
            }
        }
    }

    /** 供应商错误码翻译成用户能看懂的话，原始码仍写日志 */
    private String readableProviderError(String code, String message)
    {
        if (code == null)
        {
            return message == null ? "未知错误" : message;
        }
        return switch (code)
        {
            case "isv.MOBILE_NUMBER_ILLEGAL", "InvalidParameterValue.PhoneNumber" -> "手机号格式不正确";
            case "isv.SMS_SIGNATURE_ILLEGAL", "FailedOperation.SignatureIncorrectOrUnapproved" -> "短信签名未审核通过";
            case "isv.SMS_TEMPLATE_ILLEGAL", "FailedOperation.TemplateIncorrectOrUnapproved" -> "短信模板未审核通过";
            case "isv.AMOUNT_NOT_ENOUGH", "FailedOperation.InsufficientBalanceInSmsPackage" -> "短信余额不足";
            case "isv.BUSINESS_LIMIT_CONTROL", "LimitExceeded.PhoneNumberDailyLimit" -> "供应商侧触发频率限制";
            case "AuthFailure", "AuthFailure.SignatureFailure" -> "供应商凭据无效，请检查密钥";
            default -> (message == null || message.isBlank()) ? code : message;
        };
    }

    private void logRow(Long accountId, byte[] lookup, String masked, String scene, String provider,
            String templateCode, String status, String errorCode, String errorMessage)
    {
        try
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountId", accountId);
            row.put("phoneLookup", lookup);
            row.put("phoneMasked", masked);
            row.put("scene", scene);
            row.put("provider", provider == null ? "UNKNOWN" : provider);
            row.put("templateCode", templateCode);
            row.put("status", status);
            row.put("errorCode", errorCode);
            row.put("errorMessage", errorMessage);
            smsMapper.insertLog(row);
        }
        catch (Exception e)
        {
            // 日志失败不能影响主流程，但必须可见
            log.warn("写入短信日志失败：{}", e.getMessage());
        }
    }

    /** 138****8000 */
    public static String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String truncate(String text)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() > 480 ? text.substring(0, 480) : text;
    }
}
