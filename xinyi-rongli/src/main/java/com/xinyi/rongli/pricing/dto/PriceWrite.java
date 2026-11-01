package com.xinyi.rongli.pricing.dto;

public record PriceWrite(String ownerDeptId, String environment, String sourceRuleId,
                         PriceContent config, Long expectedVersion) { }
