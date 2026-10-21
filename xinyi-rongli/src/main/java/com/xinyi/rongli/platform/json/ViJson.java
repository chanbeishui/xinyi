package com.xinyi.rongli.platform.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

/**
 * vi 平台专用 JSON 工具（内部私有 ObjectMapper）。
 *
 * 不依赖、也不注册框架的 ObjectMapper Bean：本 RuoYi 分支 HTTP 序列化用 fastjson2，
 * 上下文无 Jackson ObjectMapper Bean；若在此注册全局 ObjectMapper 会触发 JacksonAutoConfiguration
 * 退避、可能改变框架响应序列化。故 vi 平台自持一个隔离实例，仅供幂等 result_json、outbox payload、
 * 审计 summary 等 vi 内部 JSON 使用，与框架互不干扰。
 */
public final class ViJson {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private ViJson() {
    }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "vi JSON 序列化失败", e);
        }
    }

    public static <T> T read(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "vi JSON 反序列化失败", e);
        }
    }

    /** 解析为树；非法 JSON 抛 VI_DATA_INVALID（用于机密字段名检测等需严格 JSON 的场景）。 */
    public static JsonNode tree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "内容不是合法 JSON，无法审计字段名", e);
        }
    }
}
