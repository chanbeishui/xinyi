package com.xinyi.rongli.config.dto;

/** 业务字典项（C01，DictItem）。code/label/enabled 必填；sort/version 可选。 */
public record DictItemView(String code, String label, int sort, boolean enabled, long version) {
}