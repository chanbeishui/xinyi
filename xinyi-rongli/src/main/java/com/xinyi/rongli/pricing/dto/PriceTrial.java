package com.xinyi.rongli.pricing.dto;

/** 试算不创建报价快照、订单或容量占用。 */
public record PriceTrial(String ruleId, int revision, long version, String ownerDeptId, String environment,
        boolean matchedPublished, String routeBasis, String distanceRounding, long oneWayDistanceMeter,
        long billableDistanceMeter, long includedDistanceMeter, long excessDistanceMeter, String chargedUnits,
        long inspectionFee, long driverBaseFee, long distanceFee, long totalFen, String contentHash) { }
