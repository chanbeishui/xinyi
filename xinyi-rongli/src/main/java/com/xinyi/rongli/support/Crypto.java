package com.xinyi.rongli.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 业务数据加解密与摘要工具。
 *
 * <p>地址、坐标、证件等敏感字段以 AES-GCM 密文入库；摘要统一 SHA-256/HMAC-SHA256。
 * 生产环境必须通过 rongli.security.data-key 覆盖默认开发密钥。</p>
 */
@Component
public class Crypto
{
    private final byte[] key;

    private final SecureRandom random = new SecureRandom();

    public Crypto(@Value("${rongli.security.data-key:rongli-dev-only-key-please-change}") String rawKey)
    {
        this.key = sha256(rawKey.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] sha256(byte[] input)
    {
        try
        {
            return MessageDigest.getInstance("SHA-256").digest(input);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    public static byte[] sha256(String text)
    {
        return sha256(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String hex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public byte[] hmac(String context)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(context.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("HMAC 不可用", e);
        }
    }

    /** AES-GCM 加密，输出 IV(12) + 密文 + Tag */
    public byte[] encrypt(String plain)
    {
        if (plain == null)
        {
            return null;
        }
        try
        {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return out;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("加密失败", e);
        }
    }

    public String decrypt(byte[] payload)
    {
        if (payload == null || payload.length <= 12)
        {
            return null;
        }
        try
        {
            byte[] iv = new byte[12];
            System.arraycopy(payload, 0, iv, 0, 12);
            byte[] cipherText = new byte[payload.length - 12];
            System.arraycopy(payload, 12, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // 历史数据或密钥轮换后无法解密时不抛出，交由调用方按空值处理
            return null;
        }
    }

    /** 生成安全随机令牌（URL 安全 Base64） */
    public String randomToken()
    {
        byte[] buf = new byte[32];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** 生成 4 位交接码 */
    public String randomHandoverCode()
    {
        return String.format("%04d", random.nextInt(10000));
    }

    public String randomDigits(int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
        {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static byte[] fromBase64(String text)
    {
        return Base64.getDecoder().decode(text);
    }

    public static String toBase64(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
