package com.xinyi.rongli.capacity.dto;

public record CapacityQuery(int pageNum, int pageSize, Long stationId, String quotaType, Long slotId, Integer proposedTotal) {
}
