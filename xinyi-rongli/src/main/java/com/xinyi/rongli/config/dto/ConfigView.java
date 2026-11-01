package com.xinyi.rongli.config.dto;

import java.util.List;

/** 业务配置视图（C01，ConfigPatchView）。items+version（CAS）；固定安全项不可由普通配置绕过。 */
public record ConfigView(List<Item> items, long version) {
    public record Item(String key, String value) {
    }
}