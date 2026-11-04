package com.xinyi.rongli.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.domain.*;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.error.*;
import com.wechat.pay.java.core.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** 新单读取当前已发布策略；历史交易保留原身份，允许同身份凭据按发布记录轮换。 */
@Service
public class PaymentConfiguration {
    private final PaymentMapper payments;
    private final IntegrationMapper mapper;
    private final IntegrationSecrets secrets;
    private final IntegrationValidation validation;
    private final IntegrationAuthority authority;

    public PaymentConfiguration(
            PaymentMapper payments,
            IntegrationMapper mapper,
            IntegrationSecrets secrets,
            IntegrationValidation validation,
            IntegrationAuthority authority) {
        this.payments = payments;
        this.mapper = mapper;
        this.secrets = secrets;
        this.validation = validation;
        this.authority = authority;
    }

    public record Policy(
            IntegrationProfile profile, IntegrationRevision revision, JsonNode config) {}

    public Policy policy(PaymentTransaction payment) {
        var p = payments.policyProfile(payment.getPolicyRevisionId());
        active(p, payment.getReceiverDeptId(), "PAYMENT_POLICY");
        var r = mapper.revision(p.getId(), p.getActiveRevisionId());
        return new Policy(p, r, ViJson.tree(r.getConfigJson()));
    }

    /** 线下方式已锁定时展示当时确认的收款信息，防止后台变更覆盖原订单约定。 */
    public Policy originalPolicy(PaymentTransaction payment) {
        var p = payments.policyProfile(payment.getPolicyRevisionId());
        if (p == null
                || !"PAYMENT_POLICY".equals(p.getKind())
                || !Objects.equals(p.getOwnerDeptId(), payment.getReceiverDeptId())
                || !authority.environment.equals(p.getEnvironment())
                || mapper.wasPublished(p.getId(), payment.getPolicyRevisionId()) == 0) invalid();
        var r = mapper.revision(p.getId(), payment.getPolicyRevisionId());
        return new Policy(p, r, ViJson.tree(r.getConfigJson()));
    }

    public void requireOffline(Policy policy) {
        var c = policy.config();
        if (!c.path("offlineEnabled").asBoolean())
            throw new ViException(ViErrorCode.VI_PAYMENT_METHOD_DISABLED);
        if (!"PREPAID_MANUAL_VERIFIED".equals(c.path("offlineCollectionMode").asText())
                || c.path("offlineMethods").isEmpty()) invalid();
        for (String key :
                List.of(
                        "offlineReceiverName",
                        "offlineInstructions",
                        "offlineContactName",
                        "offlineContactPhone")) if (c.path(key).asText().isBlank()) invalid();
        for (var method : c.path("offlineMethods")) {
            if ("BANK_TRANSFER".equals(method.asText())) {
                if (c.path("offlineBankName").asText().isBlank()
                        || c.path("offlineAccountName").asText().isBlank()) invalid();
                byte[] account =
                        secrets.read(policy.profile(), policy.revision(), "offlineBankAccount");
                try {
                    if (account.length == 0) invalid();
                } finally {
                    Arrays.fill(account, (byte) 0);
                }
            } else if (!"CASH".equals(method.asText())) invalid();
        }
    }

    private void active(IntegrationProfile p, long owner, String kind) {
        if (p == null
                || !kind.equals(p.getKind())
                || p.getOwnerDeptId() != owner
                || !authority.environment.equals(p.getEnvironment())
                || !"ACTIVE".equals(p.getLifecycle())
                || p.getActiveRevisionId() == null
                || mapper.wasPublished(p.getId(), p.getActiveRevisionId()) == 0) invalid();
    }

    public IntegrationProfile newMerchant(Policy policy, String accountApp) {
        if (!policy.config().path("wechatEnabled").asBoolean())
            throw new ViException(ViErrorCode.VI_PAYMENT_METHOD_DISABLED);
        if (!validation.validate(policy.profile(), policy.revision()).passed()) invalid();
        var ref = policy.config().path("wechatMerchant");
        var p = mapper.profile(ref.path("profileId").asLong());
        active(p, policy.profile().getOwnerDeptId(), "WECHAT_PAY");
        if (p.getActiveRevisionId() != ref.path("revisionId").asLong()) invalid();
        var c = ViJson.tree(mapper.revision(p.getId(), p.getActiveRevisionId()).getConfigJson());
        var appRef = c.path("appProfile");
        var app = mapper.profile(appRef.path("profileId").asLong());
        active(app, p.getOwnerDeptId(), "WECHAT_APP");
        var a =
                ViJson.tree(
                        mapper.revision(app.getId(), app.getActiveRevisionId()).getConfigJson());
        if (!accountApp.equals(a.path("appId").asText()) || !a.path("enabled").asBoolean())
            invalid();
        return p;
    }

    public String mchid(IntegrationProfile p) {
        return ViJson.tree(mapper.revision(p.getId(), p.getActiveRevisionId()).getConfigJson())
                .path("mchid")
                .asText();
    }

    /** 使用原收款公司、商户号和 AppID 校验当前凭据；微信新单开关不阻断历史查单与关单。 */
    public Config sdk(PaymentTransaction pay) {
        var p = mapper.profile(pay.getMerchantProfileId());
        active(p, pay.getReceiverDeptId(), "WECHAT_PAY");
        var r = mapper.revision(p.getId(), p.getActiveRevisionId());
        var c = ViJson.tree(r.getConfigJson());
        var original = mapper.revision(p.getId(), pay.getMerchantRevisionId());
        if (original == null
                || !pay.getMchid().equals(c.path("mchid").asText())
                || !ViJson.tree(original.getConfigJson())
                        .path("appProfile")
                        .path("profileId")
                        .equals(c.path("appProfile").path("profileId"))) invalid();
        var app = mapper.profile(c.path("appProfile").path("profileId").asLong());
        active(app, pay.getReceiverDeptId(), "WECHAT_APP");
        if (!pay.getAppId()
                .equals(
                        ViJson.tree(
                                        mapper.revision(app.getId(), app.getActiveRevisionId())
                                                .getConfigJson())
                                .path("appId")
                                .asText())) invalid();
        byte[] key = secrets.read(p, r, "merchantPrivateKey"), api = null;
        try {
            api = secrets.read(p, r, "apiV3Key");
            String pem = new String(key, StandardCharsets.UTF_8);
            if ("WECHATPAY_PUBLIC_KEY".equals(c.path("verificationMode").asText()))
                return new RSAPublicKeyConfig.Builder()
                        .merchantId(pay.getMchid())
                        .merchantSerialNumber(c.path("merchantCertificateSerial").asText())
                        .privateKey(pem)
                        .apiV3Key(new String(api, StandardCharsets.UTF_8))
                        .publicKeyId(c.path("wechatPayPublicKeyId").asText())
                        .publicKey(c.path("wechatPayPublicKeyPem").asText())
                        .build();
            if (!"PLATFORM_CERT".equals(c.path("verificationMode").asText())) invalid();
            var certificates = new ArrayList<String>();
            for (var cert : c.path("platformCertificates"))
                certificates.add(cert.path("pem").asText());
            if (certificates.isEmpty()) invalid();
            return new RSAConfig.Builder()
                    .merchantId(pay.getMchid())
                    .merchantSerialNumber(c.path("merchantCertificateSerial").asText())
                    .privateKey(pem)
                    .wechatPayCertificates(certificates.toArray(String[]::new))
                    .build();
        } catch (ViException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "支付签名及验签配置无效");
        } finally {
            Arrays.fill(key, (byte) 0);
            if (api != null) Arrays.fill(api, (byte) 0);
        }
    }

    public Map<String, Object> offline(Policy policy, boolean allowed, boolean revealAccount) {
        var c = policy.config();
        var m = new LinkedHashMap<String, Object>();
        m.put("method", "OFFLINE");
        m.put("label", "企业线下支付");
        m.put("available", allowed && c.path("offlineEnabled").asBoolean());
        for (String key :
                List.of(
                        "offlineInstructions",
                        "offlineReceiverName",
                        "offlineBankName",
                        "offlineAccountName",
                        "offlineContactName",
                        "offlineContactPhone")) {
            String target =
                    switch (key) {
                        case "offlineInstructions" -> "instructions";
                        case "offlineReceiverName" -> "receiverName";
                        case "offlineBankName" -> "bankName";
                        case "offlineAccountName" -> "bankAccountName";
                        case "offlineContactName" -> "contactName";
                        default -> "contactPhone";
                    };
            if (c.hasNonNull(key)) m.put(target, c.path(key).asText());
        }
        if (c.has("offlineMethods"))
            m.put("offlineMethods", ViJson.read(c.path("offlineMethods").toString(), List.class));
        if (revealAccount
                && allowed
                && c.path("offlineMethods").toString().contains("BANK_TRANSFER")) {
            byte[] account =
                    secrets.read(policy.profile(), policy.revision(), "offlineBankAccount");
            try {
                m.put("receiverAccountDisplay", new String(account, StandardCharsets.UTF_8));
            } finally {
                Arrays.fill(account, (byte) 0);
            }
        }
        return m;
    }

    private com.wechat.pay.java.core.cipher.Verifier notificationVerifier(IntegrationRevision r) {
        var c = ViJson.tree(r.getConfigJson());
        if ("WECHATPAY_PUBLIC_KEY".equals(c.path("verificationMode").asText()))
            return new com.wechat.pay.java.core.cipher.RSAVerifier(
                    com.wechat.pay.java.core.util.PemUtil.loadPublicKeyFromString(
                            c.path("wechatPayPublicKeyPem").asText()),
                    c.path("wechatPayPublicKeyId").asText());
        var certificates = new ArrayList<java.security.cert.X509Certificate>();
        for (var cert : c.path("platformCertificates")) {
            var parsed =
                    com.wechat.pay.java.core.util.PemUtil.loadX509FromString(
                            cert.path("pem").asText());
            try {
                parsed.checkValidity();
            } catch (java.security.cert.CertificateException e) {
                invalid();
            }
            certificates.add(parsed);
        }
        return new com.wechat.pay.java.core.cipher.RSAVerifier(
                new com.wechat.pay.java.core.certificate.InMemoryCertificateProvider(certificates));
    }

    public com.wechat.pay.java.service.payments.model.Transaction parseNotification(
            PaymentTransaction pay, com.wechat.pay.java.core.notification.RequestParam request) {
        return parseNotification(
                pay, request, com.wechat.pay.java.service.payments.model.Transaction.class);
    }

    /** 依次使用同一交易身份已发布的验签/解密材料，兼容轮换窗口内的在途通知。 */
    public <T> T parseNotification(
            PaymentTransaction pay,
            com.wechat.pay.java.core.notification.RequestParam request,
            Class<T> type) {
        var p = mapper.profile(pay.getMerchantProfileId());
        active(p, pay.getReceiverDeptId(), "WECHAT_PAY");
        var revisions = new ArrayList<IntegrationRevision>();
        revisions.add(mapper.revision(p.getId(), p.getActiveRevisionId()));
        for (var r : mapper.publishedRevisions(p.getId()))
            if (!Objects.equals(r.getId(), p.getActiveRevisionId())) revisions.add(r);
        for (var r : revisions) {
            var c = ViJson.tree(r.getConfigJson());
            if (!pay.getMchid().equals(c.path("mchid").asText())) continue;
            var app = mapper.profile(c.path("appProfile").path("profileId").asLong());
            if (app == null || !Objects.equals(app.getOwnerDeptId(), pay.getReceiverDeptId()))
                continue;
            var appRevision =
                    mapper.revision(app.getId(), c.path("appProfile").path("revisionId").asLong());
            if (appRevision == null
                    || !pay.getAppId()
                            .equals(
                                    ViJson.tree(appRevision.getConfigJson())
                                            .path("appId")
                                            .asText())) continue;
            byte[] key = null;
            try {
                key = secrets.read(p, r, "apiV3Key");
                var parser =
                        new com.wechat.pay.java.core.notification.NotificationParser(
                                Map.of("WECHATPAY2-SHA256-RSA2048", notificationVerifier(r)),
                                Map.of(
                                        "AEAD_AES_256_GCM",
                                        new com.wechat.pay.java.core.cipher.AeadAesCipher(key)));
                return parser.parse(request, type);
            } catch (RuntimeException ignored) {
                /* 仅尝试仍可解密的已发布同资金身份材料，不复活撤销凭据。 */
            } finally {
                if (key != null) Arrays.fill(key, (byte) 0);
            }
        }
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "通知签名或加密材料不匹配");
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "收款配置与订单主体或应用不一致");
    }
}
