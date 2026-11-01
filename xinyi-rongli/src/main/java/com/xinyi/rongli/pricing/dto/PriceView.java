package com.xinyi.rongli.pricing.dto;

public record PriceView(String ruleId, String ownerDeptId, String companyName, String environment,
        String ruleKey, int revision, String status, PriceContent config, long version,
        String sourceRuleId, String createdBy, String submittedBy, String approvedBy,
        String contentHash, String approvedHash, String publishedAt) { }
