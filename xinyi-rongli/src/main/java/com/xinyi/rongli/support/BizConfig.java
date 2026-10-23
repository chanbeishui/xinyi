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

    /**
     * 逐轮扩圈半径阶梯（米），升序去重。
     *
     * <p>未配置时退化为单一半径，保证旧部署行为不变。</p>
     */
    public java.util.List<Integer> dispatchRadiusSteps()
    {
        String raw = get("rongli.dispatch.radiusSteps", "");
        java.util.List<Integer> steps = new java.util.ArrayList<>();
        if (raw != null && !raw.isBlank())
        {
            for (String part : raw.split("[,;\\s]+"))
            {
                if (part.isBlank())
                {
                    continue;
                }
                try
                {
                    int v = Integer.parseInt(part.trim());
                    if (v > 0 && !steps.contains(v))
                    {
                        steps.add(v);
                    }
                }
                catch (NumberFormatException ignored)
                {
                    // 忽略非法片段，不影响其余阶梯
                }
            }
        }
        if (steps.isEmpty())
        {
            steps.add(dispatchRadiusMeter());
        }
        steps.sort(Integer::compareTo);
        return steps;
    }

    /** 相邻两次学习心跳的最大可信间隔（秒） */
    public int trainingHeartbeatMaxGapSeconds()
    {
        return Math.max(5, getInt("rongli.training.heartbeatMaxGapSeconds", 30));
    }

    public boolean subscribeEnabled()
    {
        return getBool("rongli.subscribe.enabled", true);
    }

    /** 微信订阅消息模板 ID；未配置返回 null，表示该场景只发站内信 */
    public String subscribeTemplate(String scene)
    {
        String key = switch (scene)
        {
            case "DISPATCH_INVITE" -> "rongli.subscribe.template.dispatchInvite";
            case "ASSIGNMENT_ACCEPTED" -> "rongli.subscribe.template.assignmentAccepted";
            case "RESULT_READY" -> "rongli.subscribe.template.resultReady";
            case "REFUND_DONE" -> "rongli.subscribe.template.refundDone";
            default -> null;
        };
        if (key == null)
        {
            return null;
        }
        String v = get(key, "");
        return v.isBlank() ? null : v;
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

    /**
     * 需要双方确认的交接类型集合。
     *
     * <p>默认 {@code DRIVER_TO_STATION}：车交给检测站是"交付 + 接收"两个动作，
     * 只由检测员工核验会缺少司机侧的交付确认。留空表示所有交接都单方确认。</p>
     */
    public java.util.Set<String> handoverMutualConfirmTypes()
    {
        String raw = get("rongli.handover.mutualConfirmTypes", "DRIVER_TO_STATION");
        java.util.Set<String> types = new java.util.LinkedHashSet<>();
        if (raw != null && !raw.isBlank())
        {
            for (String part : raw.split("[,;\\s]+"))
            {
                if (!part.isBlank())
                {
                    types.add(part.trim().toUpperCase());
                }
            }
        }
        return types;
    }

    public boolean handoverMutualConfirm(String handoverType)
    {
        return handoverType != null && handoverMutualConfirmTypes().contains(handoverType.toUpperCase());
    }
}
