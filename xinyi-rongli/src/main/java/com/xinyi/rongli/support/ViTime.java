package com.xinyi.rongli.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间工具：数据库统一存放 UTC 分量，对外输出 ISO8601 UTC 字符串；营业日界线按 Asia/Shanghai。
 */
public final class ViTime
{
    public static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private ViTime()
    {
    }

    /** 当前 UTC 时刻，精确到毫秒 */
    public static LocalDateTime now()
    {
        return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
    }

    public static LocalDateTime plusMinutes(LocalDateTime base, long minutes)
    {
        return base.plusMinutes(minutes);
    }

    /** 输出 ISO8601 UTC 字符串 */
    public static String iso(LocalDateTime utc)
    {
        if (utc == null)
        {
            return null;
        }
        return utc.atOffset(ZoneOffset.UTC).format(ISO);
    }

    /** 解析 ISO8601（含 Z 或偏移）为 UTC 本地时刻 */
    public static LocalDateTime parse(String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        return java.time.OffsetDateTime.parse(text).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /** 字符串时刻转 UTC 时刻，兼容 "yyyy-MM-dd HH:mm:ss" */
    public static LocalDateTime parseFlexible(String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        String v = text.trim();
        if (v.endsWith("Z") || v.contains("+"))
        {
            return parse(v);
        }
        v = v.replace(' ', 'T');
        if (v.length() == 10)
        {
            v = v + "T00:00:00";
        }
        return LocalDateTime.parse(v).atZone(SHANGHAI).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /** 北京时间当天 00:00 对应的 UTC 时刻 */
    public static LocalDateTime startOfShanghaiDay(LocalDate shanghaiDate)
    {
        return shanghaiDate.atStartOfDay(SHANGHAI).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /** 北京时间自然日 */
    public static LocalDate shanghaiDate(LocalDateTime utc)
    {
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(SHANGHAI).toLocalDate();
    }

    /** 把 UTC 时刻格式化为北京时间展示串 */
    public static String shanghaiText(LocalDateTime utc)
    {
        if (utc == null)
        {
            return null;
        }
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(SHANGHAI)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /** 北京时间本地时刻转 UTC */
    public static LocalDateTime fromShanghai(LocalDateTime shanghaiLocal)
    {
        return shanghaiLocal.atZone(SHANGHAI).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static boolean isBefore(LocalDateTime a, LocalDateTime b)
    {
        return a != null && b != null && a.isBefore(b);
    }
}
