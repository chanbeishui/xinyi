package com.xinyi.rongli.pricing.dto;

/** 已校验的计价内容；金额为分、距离为米，时间使用规范化UTC文本以绑定审核摘要。 */
public record PriceContent(String name, String vehicleTypeCode, String inspectionTypeCode,
        long inspectionFee, long driverBaseFee, long includedDistanceMeter, long distanceUnitMeter,
        long distanceUnitFee, String distanceRounding, String routeBasis, boolean waitingFeeEnabled,
        long waitingFreeSeconds, long waitingUnitSeconds, long waitingUnitFee,
        String effectiveFrom, String effectiveTo) { }
