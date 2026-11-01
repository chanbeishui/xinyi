package com.xinyi.rongli.address.dto;

/** 本人地址投影（A02，Address）。ID 十进制字符串；version 供 CAS；地址/位置解密后仅本人可见。 */
public record AddressView(
        String addressId,
        String label,
        String address,
        GeoPoint location,
        boolean isDefault,
        long version) {
}