package com.xinyi.rongli.address.service;

import com.xinyi.rongli.identity.config.ViIdentityProperties;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 地址/位置字段加密（A02，ADR-04）。AES-GCM(data-key)，IV||密文；不做手机号规范化（保留地址原文/坐标 JSON）。
 * 密钥来自部署环境/KMS（vi.crypto.data-key），缺失 fail-closed；明文不落日志/审计。
 */
@Service
public class AddressCrypto {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final ViIdentityProperties identity;

    public AddressCrypto(ViIdentityProperties identity) {
        this.identity = identity;
    }

    public byte[] encrypt(String plain) {
        byte[] key = aesKey();
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "地址加密失败", e);
        }
    }

    public String decrypt(byte[] cipherText) {
        if (cipherText == null || cipherText.length <= IV_LEN) {
            return null;
        }
        byte[] key = aesKey();
        try {
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(cipherText, 0, iv, 0, IV_LEN);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] pt = cipher.doFinal(cipherText, IV_LEN, cipherText.length - IV_LEN);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "地址解密失败", e);
        }
    }

    private byte[] aesKey() {
        String dataKey = identity.getDataKey();
        if (dataKey == null || dataKey.isBlank()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "未配置 vi.crypto.data-key");
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(dataKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "派生加密密钥失败", e);
        }
    }
}