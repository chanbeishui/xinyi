package com.xinyi.rongli.booking.dto;

import java.util.List;

/** 正式报价的公开快照；金额单位为分，报价ID和截止时间用于后续建单复核。 */
public record QuoteView(
        String quoteId,
        String expiresAt,
        String stationId,
        String stationName,
        String slotId,
        long routeDistanceMeter,
        String routeBasis,
        List<FeeItem> items,
        long totalAmount,
        int priceRuleVersion,
        String termsVersion,
        long quotaPlanVersion) {
    public record FeeItem(String feeCode, String name, long amount, String calculation) {}
}
