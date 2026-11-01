package com.xinyi.rongli.station.dto;

import java.math.BigDecimal;

/** 坐标点（GeoPoint）；固定 GCJ02。 */
public record GeoPoint(BigDecimal lng, BigDecimal lat, String coordinateSystem) {
    public static GeoPoint gcj02(BigDecimal lng, BigDecimal lat) {
        return new GeoPoint(lng, lat, "GCJ02");
    }
}