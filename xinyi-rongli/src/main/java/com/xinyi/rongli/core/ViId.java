package com.xinyi.rongli.core;

public final class ViId {
    private ViId() {}

    /** 接口ID用十进制字符串传输以避免前端精度损失；只接受正整数并由Long解析检查溢出。 */
    public static long parse(String value) {
        if (value == null || !value.matches("[1-9][0-9]{0,18}"))
            throw new IllegalArgumentException("INVALID_ID");
        return Long.parseLong(value);
    }

    public static String external(long id) {
        if (id <= 0) throw new IllegalArgumentException("INVALID_ID");
        return Long.toString(id);
    }
}
