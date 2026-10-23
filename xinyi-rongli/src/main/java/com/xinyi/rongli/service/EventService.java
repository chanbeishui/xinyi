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

    @Autowired
    private com.xinyi.rongli.support.BizConfig config;

    @Autowired
    private com.xinyi.rongli.integration.NotificationDispatchService notificationDispatch;

    /**
     * 内部场景 → V6.4 模板场景。只登记已确认要自动发送的四条订单主链路。
     *
     * <p>PAYMENT_SUCCEEDED 代表客户完成支付、预约正式成立，因此对应 BOOKING_CONFIRMED；
     * ASSIGNMENT_ACCEPTED 代表司机接单成功，对应 DRIVER_ASSIGNED；
     * VEHICLE_RETURNED 代表车辆已交回车主，对应 RETURN_COMPLETED。
     * INSPECTION_FINISHED 直接作为场景名使用（站点交车即检测完成）。</p>
     */
    private static final Map<String, String> TEMPLATE_SCENES = Map.of(
            "PAYMENT_SUCCEEDED", "BOOKING_CONFIRMED",
            "ASSIGNMENT_ACCEPTED", "DRIVER_ASSIGNED",
            "INSPECTION_FINISHED", "INSPECTION_FINISHED",
            "VEHICLE_RETURNED", "RETURN_COMPLETED");

    /** 记录一条业务事件（不可变） */
    public void record(String aggregateType, Long aggregateId, String action, Long orderId,
            Object before, Object after, String reason)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", orderId);
        row.put("aggregateType", aggregateType);
        // 全局事件（如配置变更）没有具体聚合主键，表列 NOT NULL，统一落 0 表示"无单体"
        row.put("aggregateId", aggregateId == null ? 0L : aggregateId);
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

    /** 站内消息（MINI 角色维度）；若该场景配置了订阅模板，同一事务内追加 outbox 推送 */
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
        queueSubscribePush(row, accountId, orderId, scene, content);
        dispatchIntegration(row, accountId, role, orderId, scene);
    }

    /**
     * V6.4 第三方通道下发：把内部站内信场景映射到模板场景，
     * 交给路由决定发不发、走哪条通道。
     *
     * <p>只接了订单主链路四个场景（已与业务确认）。映射不到的场景一律不动，
     * 避免后台配了模板就把历史所有事件都发出去。</p>
     */
    private void dispatchIntegration(Map<String, Object> notification, Long accountId, String role,
            Long orderId, String scene)
    {
        String templateScene = TEMPLATE_SCENES.get(scene);
        if (templateScene == null)
        {
            return;
        }
        Long notificationId = notification.get("id") instanceof Number n ? n.longValue() : null;
        notificationDispatch.dispatch(templateScene, role == null ? Codes.Role.OWNER : role, accountId,
                orderId == null ? notificationId : orderId, null, Map.of());
    }

    /**
     * 为已落库的站内信追加一条订阅推送事件。
     *
     * <p>只在场景配置了模板时才入队，避免无意义地放大 outbox；
     * 推送失败由 {@link OutboxConsumer} 退避重试，站内信不受影响。</p>
     */
    private void queueSubscribePush(Map<String, Object> notification, Long accountId, Long orderId,
            String scene, String content)
    {
        if (scene == null || config.subscribeTemplate(scene) == null)
        {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notificationId", notification.get("id") == null ? null : String.valueOf(notification.get("id")));
        payload.put("accountId", accountId);
        payload.put("orderId", orderId);
        payload.put("scene", scene);
        payload.put("content", content);
        publish("NOTIFICATION", notification.get("id") instanceof Number n ? n.longValue() : null,
                "SUBSCRIBE_PUSH", payload);
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
