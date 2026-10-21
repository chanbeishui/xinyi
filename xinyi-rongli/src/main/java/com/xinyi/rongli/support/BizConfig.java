package com.xinyi.rongli.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xinyi.system.service.ISysConfigService;

/**
 * 业务参数读取：复用框架 sys_config，参数变更只影响新订单（订单内保存派单参数版本快照）。
 */
@Component
public class BizConfig
{
    @Autowired
    private ISysConfigService configService;

    public String get(String key, String defaultValue)
    {
        try
        {
            String v = configService.selectConfigByKey(key);
            return v == null || v.isBlank() ? defaultValue : v.trim();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue)
    {
        try
        {
            return Long.parseLong(get(key, String.valueOf(defaultValue)));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public boolean getBool(String key, boolean defaultValue)
    {
        String v = get(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    public int dispatchLeadMinutes()
    {
        return getInt("rongli.dispatch.leadMinutes", 30);
    }

    public int payExpireMinutes()
    {
        return getInt("rongli.pay.expireMinutes", 30);
    }

    public int autoConfirmHours()
    {
        return getInt("rongli.confirm.autoHours", 24);
    }

    public int inviteExpireSeconds()
    {
        return getInt("rongli.invite.expireSeconds", 60);
    }

    public int dispatchRadiusMeter()
    {
        return getInt("rongli.dispatch.radiusMeter", 8000);
    }

    public int dispatchTotalMinutes()
    {
        return getInt("rongli.dispatch.totalMinutes", 5);
    }

    public String termsVersion()
    {
        return get("rongli.service.termsVersion", "v1");
    }

    public boolean mockPayment()
    {
        return "MOCK".equalsIgnoreCase(get("rongli.payment.channel", "MOCK"));
    }

    public int locationFreshSeconds()
    {
        return getInt("rongli.driver.locationFreshSeconds", 600);
    }

    /** 限定用途上传的落盘根目录；生产环境应替换为对象存储 */
    public String mediaRoot()
    {
        return get("rongli.media.root", System.getProperty("user.home") + "/rongli-uploads");
    }

    /** 单文件上限（字节） */
    public long mediaMaxBytes()
    {
        return getLong("rongli.media.maxBytes", 10L * 1024 * 1024);
    }

    /** 文件访问短链有效期（分钟） */
    public int mediaAccessMinutes()
    {
        return getInt("rongli.media.accessMinutes", 10);
    }
}
