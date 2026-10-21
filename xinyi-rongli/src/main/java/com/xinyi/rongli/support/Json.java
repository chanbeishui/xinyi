package com.xinyi.rongli.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * JSON 读写辅助：数据库 JSON 列以字符串读写，避免各服务重复处理异常。
 */
public final class Json
{
    private Json()
    {
    }

    public static String write(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof String s)
        {
            return s;
        }
        return JSON.toJSONString(value);
    }

    public static JSONObject read(String text)
    {
        if (text == null || text.isBlank())
        {
            return new JSONObject();
        }
        try
        {
            return JSON.parseObject(text);
        }
        catch (Exception e)
        {
            return new JSONObject();
        }
    }

    public static String str(JSONObject obj, String key)
    {
        Object v = obj == null ? null : obj.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public static long lng(JSONObject obj, String key, long defaultValue)
    {
        Object v = obj == null ? null : obj.get(key);
        if (v == null)
        {
            return defaultValue;
        }
        if (v instanceof Number n)
        {
            return n.longValue();
        }
        try
        {
            return Long.parseLong(String.valueOf(v));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
}
