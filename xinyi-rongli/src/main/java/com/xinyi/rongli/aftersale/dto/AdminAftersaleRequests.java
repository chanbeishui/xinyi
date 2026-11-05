package com.xinyi.rongli.aftersale.dto;

import com.xinyi.rongli.platform.error.*;
import java.util.*;

/** 审核输入不接受保管方、返还标志、收款部门或退款渠道，均依实际锁定记录推导。 */
public final class AdminAftersaleRequests {
    private AdminAftersaleRequests() {}

    public record Preview(long expectedVersion, long approvedAmount, String decision) {}

    public record Review(
            long expectedVersion,
            long observedOrderVersion,
            String decision,
            long approvedAmount,
            String opinion,
            String publicOpinion) {}

    public static Preview preview(Map<String, Object> body) {
        if (!Set.of("expectedVersion", "approvedAmount", "decision").containsAll(body.keySet()))
            invalid();
        String decision = body.containsKey("decision") ? text(body, "decision", 16) : "APPROVE";
        if (!Set.of("APPROVE", "REJECT").contains(decision)) invalid();
        long amount = integer(body, "approvedAmount");
        if ("REJECT".equals(decision) && amount != 0) invalid();
        return new Preview(integer(body, "expectedVersion"), amount, decision);
    }

    public static Review review(Map<String, Object> body) {
        if (!body.keySet()
                .equals(
                        Set.of(
                                "expectedVersion",
                                "observedOrderVersion",
                                "decision",
                                "approvedAmount",
                                "opinion",
                                "publicOpinion"))) invalid();
        String decision = text(body, "decision", 16);
        if (!Set.of("APPROVE", "REJECT").contains(decision)) invalid();
        long amount = integer(body, "approvedAmount");
        if ("REJECT".equals(decision) && amount != 0) invalid();
        return new Review(
                integer(body, "expectedVersion"),
                integer(body, "observedOrderVersion"),
                decision,
                amount,
                text(body, "opinion", 1000),
                text(body, "publicOpinion", 1000));
    }

    private static long integer(Map<String, Object> body, String key) {
        try {
            if (!(body.get(key) instanceof Number)) invalid();
            long value = Long.parseLong(body.get(key).toString());
            if (value < 0 || value > 9007199254740991L) invalid();
            return value;
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String text(Map<String, Object> body, String key, int max) {
        if (!(body.get(key) instanceof String value) || value.isBlank() || value.length() > max)
            invalid();
        return (String) body.get(key);
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
