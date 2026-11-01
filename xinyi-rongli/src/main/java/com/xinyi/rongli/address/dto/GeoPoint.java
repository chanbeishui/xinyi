package com.xinyi.rongli.address.dto;

/** 服务坐标（GCJ02）。报价前必须可解析到服务坐标。 */
public record GeoPoint(double lng, double lat, String coordinateSystem) {
}