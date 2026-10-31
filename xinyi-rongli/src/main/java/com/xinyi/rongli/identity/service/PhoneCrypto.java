package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.config.ViIdentityProperties;
import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 手机号密码学处理（ADR-04 / V6.4 §2）。phone_lookup=HMAC-SHA256(lookup-key, 规范化号码) 用于匹配（非裸哈希）；
 * phone_cipher=AES-GCM(data-key) 密文（IV||密文），仅用于本人脱敏展示恢复。明文号码绝不落日志/审计。
 * 密钥来自部署环境/KMS（vi.lookup-key / vi.crypto.data-key），缺失则 fail-closed，不用空密钥静默生成。
 */
@Service
public class PhoneCrypto {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;

    private final ViPlatformProperties platform;
    private final ViIdentityProperties identity;

    public PhoneCrypto(ViPlatformProperties platform, ViIdentityProperties identity) {
        this.platform = platform;
        this.identity = identity;
    }

    /** 规范化：去空格/连字符/括号，保留前导 + 与数字，作为 HMAC 与匹配的一致键。 */
    public static String normalize(String phone) {
        if (phone == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (c == '+' && sb.length() == 0) {
                sb.append(c);
            } else if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 带密钥摘要（32 字节），用于 vi_account.phone_lookup / vi_employee_binding.phone_lookup 匹配。 */
    public byte[] lookup(String phone) {
        if (!platform.hasLookupKey()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "未配置 vi.lookup-key，无法计算手机号摘要");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(platform.getLookupKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(normalize(phone).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "手机号摘要计算失败", e);
        }
    }

    /** AES-GCM 加密，返回 IV(12B)||密文（含 tag）。 */
    public byte[] encrypt(String phone) {
        byte[] key = aesKey();
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(normalize(phone).getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "手机号加密失败", e);
        }
    }

    /** 解密恢复明文号码（仅服务端本人脱敏用，绝不外泄完整号码）。 */
    public String decrypt(byte[] cipherText) {
        if (cipherText == null || cipherText.length <= GCM_IV_LEN) {
            return null;
        }
        byte[] key = aesKey();
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            System.arraycopy(cipherText, 0, iv, 0, GCM_IV_LEN);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(cipherText, GCM_IV_LEN, cipherText.length - GCM_IV_LEN);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "手机号解密失败", e);
        }
    }

    /** 脱敏展示：保留前3后4，中间 ****（不足则全掩）。 */
    public static String mask(String phone) {
        String n = normalize(phone);
        if (n.length() < 7) {
            return n.isEmpty() ? null : "****";
        }
        return n.substring(0, 3) + "****" + n.substring(n.length() - 4);
    }

    private byte[] aesKey() {
        String dataKey = identity.getDataKey();
        if (dataKey == null || dataKey.isBlank()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "未配置 vi.crypto.data-key，无法加密手机号");
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(dataKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "派生加密密钥失败", e);
        }
    }
}
