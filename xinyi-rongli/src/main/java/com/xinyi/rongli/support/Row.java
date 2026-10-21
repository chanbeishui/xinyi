package com.xinyi.rongli.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据库行读取包装。
 *
 * <p>查询统一返回 snake_case 列名，本类同时兼容 camelCase 与 snake_case 取值，
 * 避免为每张表编写实体类和大量 getter。</p>
 */
public class Row implements Map<String, Object>
{
    private final Map<String, Object> data;

    private Row(Map<String, Object> data)
    {
        this.data = data;
    }

    public static Row of(Map<String, Object> map)
    {
        return new Row(map == null ? new LinkedHashMap<>() : map);
    }

    public static Row empty()
    {
        return new Row(new LinkedHashMap<>());
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    public boolean isNotEmpty()
    {
        return !data.isEmpty();
    }

    private Object raw(String key)
    {
        if (data.containsKey(key))
        {
            return data.get(key);
        }
        StringBuilder sb = new StringBuilder();
        for (char c : key.toCharArray())
        {
            if (Character.isUpperCase(c))
            {
                sb.append('_').append(Character.toLowerCase(c));
            }
            else
            {
                sb.append(c);
            }
        }
        return data.get(sb.toString());
    }

    public String str(String key)
    {
        Object v = raw(key);
        return v == null ? null : String.valueOf(v);
    }

    public String str(String key, String defaultValue)
    {
        String v = str(key);
        return v == null || v.isEmpty() ? defaultValue : v;
    }

    public Long lng(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof Number n)
        {
            return n.longValue();
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : Long.valueOf(s);
    }

    public long lng(String key, long defaultValue)
    {
        Long v = lng(key);
        return v == null ? defaultValue : v;
    }

    public Integer intg(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof Number n)
        {
            return n.intValue();
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    public int intg(String key, int defaultValue)
    {
        Integer v = intg(key);
        return v == null ? defaultValue : v;
    }

    public BigDecimal dec(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof BigDecimal b)
        {
            return b;
        }
        return new BigDecimal(String.valueOf(v));
    }

    public boolean bool(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return false;
        }
        if (v instanceof Boolean b)
        {
            return b;
        }
        if (v instanceof Number n)
        {
            return n.intValue() != 0;
        }
        String s = String.valueOf(v);
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }

    public LocalDateTime time(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof LocalDateTime t)
        {
            return t;
        }
        if (v instanceof java.sql.Timestamp ts)
        {
            return ts.toLocalDateTime();
        }
        if (v instanceof java.util.Date d)
        {
            return LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneOffset.UTC);
        }
        return LocalDateTime.parse(String.valueOf(v).replace(' ', 'T'));
    }

    public LocalDate date(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof LocalDate d)
        {
            return d;
        }
        if (v instanceof java.sql.Date d)
        {
            return d.toLocalDate();
        }
        return LocalDate.parse(String.valueOf(v));
    }

    public String json(String key)
    {
        Object v = raw(key);
        if (v == null)
        {
            return null;
        }
        if (v instanceof byte[] bytes)
        {
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
        return String.valueOf(v);
    }

    public Map<String, Object> map()
    {
        return data;
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return data.get(key);
    }

    @Override
    public Object put(String key, Object value)
    {
        return data.put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m)
    {
        data.putAll(m);
    }

    @Override
    public void clear()
    {
        data.clear();
    }

    @Override
    public java.util.Set<String> keySet()
    {
        return data.keySet();
    }

    @Override
    public java.util.Collection<Object> values()
    {
        return data.values();
    }

    @Override
    public java.util.Set<Entry<String, Object>> entrySet()
    {
        return data.entrySet();
    }
}
