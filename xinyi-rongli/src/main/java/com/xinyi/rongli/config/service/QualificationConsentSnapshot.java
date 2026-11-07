package com.xinyi.rongli.config.service;

import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/** 同意版本与公开原文从同一SQL快照读取；公开正文的SHA256用于绑定用户真正阅读的内容。 */
public record QualificationConsentSnapshot(String version, String text) {
    public static final String VERSION_KEY = "vi.biz.qualification.consent.version";
    public static final String TEXT_KEY = "vi.biz.qualification.consent.text";

    public static QualificationConsentSnapshot read(ConfigMapper configs, boolean lock) {
        String version = null;
        String text = null;
        for (Map<String, Object> row : configs.selectQualificationConsent(lock)) {
            if (VERSION_KEY.equals(row.get("config_key")))
                version = (String) row.get("config_value");
            if (TEXT_KEY.equals(row.get("config_key"))) text = (String) row.get("config_value");
        }
        return new QualificationConsentSnapshot(version, text);
    }

    public boolean published() {
        return version != null && !version.isBlank() && text != null && !text.isBlank();
    }

    public String textHash() {
        return hash(text);
    }

    public static String hash(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            // 这里只摘要已公开的同意原文；身份证等低熵个人资料仍使用Q01带密钥材料指纹。
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR);
        }
    }
}
