package com.xinyi.rongli.pricing.dto;

public record PriceAction(long expectedVersion, String reason, String decision,
                          String replacesRuleId, Long replacesExpectedVersion) { }
