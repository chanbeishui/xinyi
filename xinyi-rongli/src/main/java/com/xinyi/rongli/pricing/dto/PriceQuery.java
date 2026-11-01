package com.xinyi.rongli.pricing.dto;

public record PriceQuery(int pageNum, int pageSize, Long ownerDeptId, String status,
                         String vehicleTypeCode, String inspectionTypeCode, String environment) { }
