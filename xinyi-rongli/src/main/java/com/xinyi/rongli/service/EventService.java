package com.xinyi.rongli.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;

/**
 * 业务事实留痕：事件流水、可靠异步消息(outbox)与站内通知。
 *
 * <p>与业务写入处于同一事务；outbox 由定时任务异步消费，通知失败不会回滚业务。</p>
 */
@Service
public class EventService
{
    @Autowired
    private MiscMapper miscMapper;

    /** 记录一条业务事件（不可变） */
    public void record(String aggregateType, Long aggregateId, String action, Long orderId,
            Object before, Object after, String reason)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", orderId);
        row.put("aggregateType", aggregateType);
        row.put("aggregateId", aggregateId);
        row.put("action", action);
        row.put("before", before == null ? null : Json.write(before));
        row.put("after", after == null ? null : Json.write(after));
        row.put("reason", reason);
        MiniPrincipal p = MiniContext.get();
        if (p != null)
        {
            row.put("actorAccountId", p.accountId);
            row.put("actorUserId", null);
            row.put("actingRole", p.role);
            row.put("stationId", p.stationId);
            row.put("realm", "MINI");
            row.put("sessionId", p.sessionId);
            row.put("contextVersion", p.contextVersion);
        }
        else
        {
            Long userId = currentUserId();
            row.put("actorAccountId", null);
            row.put("actorUserId", userId);
            row.put("actingRole", "ADMIN");
            row.put("stationId", null);
            row.put("realm", userId == null ? "SYSTEM" : "ADMIN");
            row.put("sessionId", null);
            row.put("contextVersion", null);
        }
        miscMapper.insertEvent(row);
    }

    public void record(String aggregateType, Long aggregateId, String action, Long orderId)
    {
        record(aggregateType, aggregateId, action, orderId, null, null, null);
    }

    /** 写 outbox 事件 */
    public void publish(String aggregateType, Long aggregateId, String eventType, Object payload)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("eventKey", eventType + ":" + aggregateType + ":" + aggregateId + ":" + UUID.randomUUID());
        row.put("aggregateType", aggregateType);
        row.put("aggregateId", aggregateId);
        row.put("eventType", eventType);
        row.put("payload", payload == null ? "{}" : Json.write(payload));
        miscMapper.insertOutbox(row);
    }

    /** 站内消息（MINI 角色维度） */
    public void notify(Long accountId, String role, Long orderId, String scene, String content)
    {
        if (accountId == null)
        {
            return;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("accountId", accountId);
        row.put("role", role == null ? Codes.Role.OWNER : role);
        row.put("orderId", orderId);
        row.put("scene", scene);
        row.put("content", content);
        miscMapper.insertNotification(row);
    }

    private Long currentUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
