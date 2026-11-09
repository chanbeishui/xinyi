package com.xinyi.rongli.verification.service;

import com.xinyi.rongli.config.service.QualificationConsentSnapshot;
import com.xinyi.rongli.verification.mapper.VerificationMapper;

import java.util.Map;

/** 设备本机认证独立的公开同意文本；双键一次读取且写事务按key顺序冻结，不冒用一般资格同意。 */
public record VerificationConsentSnapshot(String version, String text) {
    public static final String VERSION_KEY = "vi.biz.verification.consent.version";
    public static final String TEXT_KEY = "vi.biz.verification.consent.text";

    public static VerificationConsentSnapshot read(VerificationMapper mapper, boolean lock) {
        String version = null, text = null;
        for (Map<String, Object> row : mapper.consent(lock)) {
            if (VERSION_KEY.equals(row.get("config_key")))
                version = (String) row.get("config_value");
            if (TEXT_KEY.equals(row.get("config_key"))) text = (String) row.get("config_value");
        }
        return new VerificationConsentSnapshot(version, text);
    }

    public boolean published() {
        return version != null && !version.isBlank() && text != null && !text.isBlank();
    }

    public String textHash() {
        return QualificationConsentSnapshot.hash(text);
    }
}
