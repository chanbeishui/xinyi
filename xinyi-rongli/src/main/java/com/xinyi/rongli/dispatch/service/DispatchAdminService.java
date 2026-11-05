package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.service.StationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.auth.mapper.AccountMapper;

import com.xinyi.rongli.aftersale.mapper.AfterSaleMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.ledger.mapper.LedgerMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.order.mapper.PriceMapper;
import com.xinyi.rongli.station.mapper.StationMapper;
import com.xinyi.rongli.vehicle.mapper.VehicleMapper;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.DeptScope;
import com.xinyi.rongli.common.support.Codes;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.SubscribeSettings;
import com.xinyi.rongli.common.support.ViTime;
import com.xinyi.rongli.admin.mapper.AdminOpsMapper;

/**
 * 管理端 dispatch 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class DispatchAdminService
{
    @Autowired
    private AdminOpsMapper adminOpsMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DispatchMapper dispatchMapper;
    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private EventService eventService;
    @Autowired
    private BizConfig config;
    @Autowired
    private Crypto crypto;
    @Autowired
    private com.xinyi.system.service.ISysConfigService sysConfigService;

    public Map<String, Object> dispatchQueue()
    {
        List<Map<String, Object>> rows = dispatchService.queue();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> dispatchCandidates(String keyword)
    {
        List<Map<String, Object>> rows = dispatchService.candidates(keyword);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> manualInvite(String orderNo, Long driverId, String reason)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return dispatchService.manualInvite(order.lng("id"), driverId, reason);
    }
    @Transactional
    public Map<String, Object> dispatchNow(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status)
                && !Codes.Fulfill.MANUAL_DISPATCH.equals(status))
        {
            throw ApiException.stateConflict("订单当前状态不可派单");
        }
        if (!Codes.Cancel.NONE.equals(order.str("cancel_status", Codes.Cancel.NONE)))
        {
            throw ApiException.stateConflict("订单处于取消流程中，不可派单");
        }
        Long orderId = order.lng("id");
        orderMapper.updateDispatchAt(orderId, ViTime.now(), order.lng("dispatch_config_version", 0L));
        eventService.record("ORDER", orderId, "DISPATCH_TRIGGERED_NOW", orderId, null,
                Map.of("dispatchAt", ViTime.iso(ViTime.now())), "后台立即派单");
        dispatchService.dispatchOrder(orderId);

        Row after = Row.of(orderMapper.findDetailByNo(orderNo));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("fulfillmentStatus", after.str("fulfillment_status"));
        view.put("dispatchAt", ViTime.iso(after.time("dispatch_at")));
        view.put("hint", Codes.Fulfill.MANUAL_DISPATCH.equals(after.str("fulfillment_status"))
                ? "当前无可用代驾，订单已转待人工安排"
                : "已发起一轮自动邀约");
        return view;
    }
    public Map<String, Object> dispatchInvites(Long orderId, Long driverId, String status, int pageNum, int pageSize)
    {
        return dispatchInvites(orderId, driverId, status, null, pageNum, pageSize);
    }
    public Map<String, Object> dispatchInvites(Long orderId, Long driverId, String status, String keyword,
            int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> i : dispatchMapper.adminListInvites(orderId, driverId, status, keyword, offset, limit))
        {
            Row inv = Row.of(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("inviteId", String.valueOf(inv.lng("id")));
            item.put("orderNo", inv.str("order_no"));
            item.put("plateNo", inv.str("plate_no"));
            item.put("driverId", String.valueOf(inv.lng("driver_id")));
            item.put("driverName", inv.str("driver_name"));
            item.put("source", inv.str("source"));
            item.put("sourceLabel", "MANUAL".equals(inv.str("source")) ? "人工邀约" : "自动派单");
            item.put("roundKey", inv.str("round_key"));
            item.put("roundNo", inv.intg("round_no", 1));
            item.put("status", inv.str("status"));
            item.put("statusLabel", inviteLabel(inv.str("status")));
            item.put("issuedAt", ViTime.iso(inv.time("issued_at")));
            item.put("expiresAt", ViTime.iso(inv.time("expires_at")));
            item.put("respondedAt", ViTime.iso(inv.time("responded_at")));
            item.put("createdAt", ViTime.iso(inv.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", rows);
        view.put("total", dispatchMapper.adminCountInvites(orderId, driverId, status, keyword));
        return view;
    }
    public Map<String, Object> dispatchConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>(dispatchService.dispatchConfig());
        view.put("termsVersion", config.termsVersion());
        view.put("paymentChannel", config.mockPayment() ? "MOCK" : "WECHAT");
        view.put("locationFreshSeconds", config.locationFreshSeconds());
        return view;
    }
    @Transactional
    public Map<String, Object> saveDispatchConfig(Map<String, Object> req)
    {
        if (req == null || req.isEmpty())
        {
            throw ApiException.validation("没有需要保存的配置项");
        }
        Map<String, String> allowed = new LinkedHashMap<>();
        allowed.put("leadMinutes", "rongli.dispatch.leadMinutes");
        allowed.put("radiusMeter", "rongli.dispatch.radiusMeter");
        allowed.put("radiusSteps", "rongli.dispatch.radiusSteps");
        allowed.put("totalMinutes", "rongli.dispatch.totalMinutes");
        allowed.put("inviteExpireSeconds", "rongli.invite.expireSeconds");
        allowed.put("payExpireMinutes", "rongli.pay.expireMinutes");
        allowed.put("autoConfirmHours", "rongli.confirm.autoHours");
        allowed.put("locationFreshSeconds", "rongli.driver.locationFreshSeconds");
        allowed.put("termsVersion", "rongli.service.termsVersion");
        allowed.put("heartbeatMaxGapSeconds", "rongli.training.heartbeatMaxGapSeconds");
        allowed.put("subscribeEnabled", "rongli.subscribe.enabled");
        allowed.put("subscribeTemplateDispatchInvite", "rongli.subscribe.template.dispatchInvite");
        allowed.put("subscribeTemplateAssignmentAccepted", "rongli.subscribe.template.assignmentAccepted");
        allowed.put("subscribeTemplateResultReady", "rongli.subscribe.template.resultReady");
        allowed.put("subscribeTemplateRefundDone", "rongli.subscribe.template.refundDone");
        // 订阅消息跳转与字段映射：模板 ID 在「第三方服务」维护，跳转行为属于业务口径
        allowed.put("subscribePage", "rongli.subscribe.page");
        allowed.put("subscribeMiniprogramState", "rongli.subscribe.miniprogramState");
        allowed.put("subscribeFields", "rongli.subscribe.fields");
        // 短信频控：通道凭据在「第三方服务」维护，发送节流是业务口径
        allowed.put("smsCodeExpireSeconds", "rongli.sms.codeExpireSeconds");
        allowed.put("smsResendSeconds", "rongli.sms.resendSeconds");
        allowed.put("smsDailyLimit", "rongli.sms.dailyLimit");
        allowed.put("contentIntro", "rongli.content.intro");
        allowed.put("contentSupport", "rongli.content.support");
        for (Map.Entry<String, String> e : allowed.entrySet())
        {
            Object v = req.get(e.getKey());
            if (v == null)
            {
                continue;
            }
            String text = String.valueOf(v).trim();
            boolean blankAllowed = e.getValue().startsWith("rongli.subscribe.template.")
                    || "rongli.dispatch.radiusSteps".equals(e.getValue());
            if (text.isEmpty() && !blankAllowed)
            {
                throw ApiException.validation(e.getKey() + " 不能为空");
            }
            if ("rongli.dispatch.radiusSteps".equals(e.getValue()))
            {
                String[] parts = text.isEmpty() ? new String[0] : text.split("[,;\\s]+");
                if (parts.length > 6)
                {
                    throw ApiException.validation("扩圈阶梯最多 6 档");
                }
                for (String part : parts)
                {
                    int n;
                    try
                    {
                        n = Integer.parseInt(part);
                    }
                    catch (NumberFormatException ex)
                    {
                        throw ApiException.validation("扩圈阶梯必须是逗号分隔的正整数（米）");
                    }
                    if (n <= 0 || n > 200000)
                    {
                        throw ApiException.validation("扩圈阶梯半径需在 1~200000 米之间");
                    }
                }
            }
            else if ("rongli.service.termsVersion".equals(e.getValue())
                    || e.getValue().startsWith("rongli.subscribe.template.")
                    || e.getValue().startsWith("rongli.content.")
                    || "rongli.subscribe.enabled".equals(e.getValue()))
            {
                if ("rongli.subscribe.enabled".equals(e.getValue())
                        && !"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text))
                {
                    throw ApiException.validation("订阅消息开关只能是 true 或 false");
                }
            }
            else if ("rongli.subscribe.page".equals(e.getValue()))
            {
                // 订阅消息点开后的落地页：必须是小程序内绝对路径，不能带前导斜杠或域名
                if (!text.matches("^pages/[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)+$"))
                {
                    throw ApiException.validation("订阅消息跳转页需形如 pages/index/index（小程序内路径，不带前导斜杠）");
                }
            }
            else if ("rongli.subscribe.miniprogramState".equals(e.getValue()))
            {
                if (!SubscribeSettings.STATES.contains(text))
                {
                    throw ApiException.validation("订阅消息跳转版本只能是 "
                            + String.join(" / ", SubscribeSettings.STATES));
                }
            }
            else if ("rongli.subscribe.fields".equals(e.getValue()))
            {
                validateSubscribeFields(text);
            }
            else if ("rongli.sms.codeExpireSeconds".equals(e.getValue()))
            {
                requireIntInRange(e.getKey(), text, 60, 1800, "验证码有效期需在 60~1800 秒之间");
            }
            else if ("rongli.sms.resendSeconds".equals(e.getValue()))
            {
                requireIntInRange(e.getKey(), text, 10, 600, "同号重发间隔需在 10~600 秒之间");
            }
            else if ("rongli.sms.dailyLimit".equals(e.getValue()))
            {
                requireIntInRange(e.getKey(), text, 1, 50, "单号每日上限需在 1~50 次之间");
            }
            else
            {
                int n;
                try
                {
                    n = Integer.parseInt(text);
                }
                catch (NumberFormatException ex)
                {
                    throw ApiException.validation(e.getKey() + " 必须是整数");
                }
                if (n < 0 || n > 100000)
                {
                    throw ApiException.validation(e.getKey() + " 超出允许范围");
                }
            }
            Map<String, Object> existing = adminOpsMapper.findConfigByKey(e.getValue());
            if (existing == null)
            {
                com.xinyi.system.domain.SysConfig entity = new com.xinyi.system.domain.SysConfig();
                entity.setConfigKey(e.getValue());
                entity.setConfigName(e.getKey());
                entity.setConfigValue(text);
                entity.setConfigType("N");
                sysConfigService.insertConfig(entity);
            }
            else
            {
                adminOpsMapper.updateConfigValue(e.getValue(), text);
            }
            eventService.record("CONFIG", null, "CONFIG_CHANGED", null,
                    Map.of("key", e.getValue()), Map.of("value", text), "管理端保存业务配置");
        }
        // 配置走的是直接 SQL 更新，必须显式失效框架缓存，否则 BizConfig 仍读到旧值。
        // 先同步刷新一次，保证本次响应回显的是新值；事务结束后再刷一次，
        // 覆盖提交/回滚两种情况，避免缓存与库长期不一致。
        sysConfigService.resetConfigCache();
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive())
        {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization()
                    {
                        @Override
                        public void afterCompletion(int status)
                        {
                            sysConfigService.resetConfigCache();
                        }
                    });
        }
        return dispatchConfig();
    }
    /**
     * 短信频控整数项校验：超范围直接拒绝，而不是像运行时那样静默 clamp——
     * 否则管理员看到的保存值与实际生效值不一致，排查时会被误导。
     */
    private void requireIntInRange(String key, String text, int min, int max, String message)
    {
        int n;
        try
        {
            n = Integer.parseInt(text);
        }
        catch (NumberFormatException ex)
        {
            throw ApiException.validation(key + " 必须是整数");
        }
        if (n < min || n > max)
        {
            throw ApiException.validation(message);
        }
    }

    /**
     * 订阅消息字段映射校验：模板占位符 → 内部取值来源。
     *
     * <p>微信要求 data 里的键与模板定义一致，值有长度限制；这里只校验键名形态与
     * 取值来源是否受支持，避免运营手写 JSON 时把发送链路写坏。</p>
     */
    private void validateSubscribeFields(String text)
    {
        JSONObject json;
        try
        {
            json = JSON.parseObject(text);
        }
        catch (RuntimeException ex)
        {
            throw ApiException.validation("字段映射必须是 JSON 对象，例如 {\"thing1\":\"content\"}");
        }
        if (json == null || json.isEmpty())
        {
            throw ApiException.validation("字段映射不能为空");
        }
        if (json.size() > 10)
        {
            throw ApiException.validation("字段映射最多 10 项（微信单个模板的字段数上限）");
        }
        for (Map.Entry<String, Object> item : json.entrySet())
        {
            if (!SubscribeSettings.FIELD_KEY.matcher(item.getKey()).matches())
            {
                throw ApiException.validation("模板字段名不合法：" + item.getKey() + "（应形如 thing1 / time2）");
            }
            String source = String.valueOf(item.getValue());
            if (!SubscribeSettings.FIELD_SOURCES.contains(source))
            {
                throw ApiException.validation("字段 " + item.getKey() + " 的取值来源只能是 "
                        + String.join(" / ", SubscribeSettings.FIELD_SOURCES));
            }
        }
    }
    public Map<String, Object> previewDispatchPlan(Integer leadMinutes)
    {
        int lead = leadMinutes == null ? config.dispatchLeadMinutes() : Math.max(0, leadMinutes);
        List<Map<String, Object>> affected = new ArrayList<>();
        for (Map<String, Object> r : orderMapper.listPlanCandidates(500))
        {
            Row o = Row.of(r);
            String status = o.str("fulfillment_status");
            if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status))
            {
                continue;
            }
            java.time.LocalDateTime pickup = o.time("pickup_starts_at");
            java.time.LocalDateTime next = pickup == null ? o.time("dispatch_at")
                    : pickup.minusMinutes(lead);
            if (next == null || Objects.equals(next, o.time("dispatch_at")))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", status);
            item.put("currentDispatchAt", ViTime.iso(o.time("dispatch_at")));
            item.put("nextDispatchAt", ViTime.iso(next));
            affected.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("planId", "PLAN" + crypto.randomDigits(8));
        view.put("leadMinutes", lead);
        view.put("affectedCount", affected.size());
        view.put("rows", affected);
        view.put("hint", "确认后仅调整未派单订单的派单时间，已承接订单不受影响");
        return view;
    }
    @Transactional
    public Map<String, Object> applyDispatchPlan(Integer leadMinutes)
    {
        int lead = leadMinutes == null ? config.dispatchLeadMinutes() : Math.max(0, leadMinutes);
        if (lead > 24 * 60)
        {
            throw ApiException.validation("提前量不能超过 1440 分钟");
        }
        int changed = 0;
        List<String> skipped = new ArrayList<>();
        List<Map<String, Object>> rows = orderMapper.listPlanCandidates(500);
        for (Map<String, Object> r : rows)
        {
            Row o = Row.of(r);
            String status = o.str("fulfillment_status");
            if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status))
            {
                skipped.add(o.str("order_no"));
                continue;
            }
            java.time.LocalDateTime pickup = o.time("pickup_starts_at");
            if (pickup == null)
            {
                skipped.add(o.str("order_no"));
                continue;
            }
            java.time.LocalDateTime next = pickup.minusMinutes(lead);
            if (orderMapper.updateDispatchAt(o.lng("id"), next, o.lng("dispatch_config_version", 0L)) > 0)
            {
                changed++;
                eventService.record("ORDER", o.lng("id"), "DISPATCH_PLAN_UPDATED", o.lng("id"),
                        Map.of("dispatchAt", ViTime.iso(o.time("dispatch_at"))),
                        Map.of("dispatchAt", ViTime.iso(next)), "计划变更确认");
            }
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("leadMinutes", lead);
        view.put("changedCount", changed);
        view.put("skippedCount", skipped.size());
        view.put("skippedOrders", skipped);
        return view;
    }
    private String inviteLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.Invite.WAIT_RESPONSE -> "等待响应";
            case Codes.Invite.ACCEPTED -> "已接单";
            case Codes.Invite.REJECTED -> "已拒绝";
            case Codes.Invite.EXPIRED -> "已过期";
            case Codes.Invite.REVOKED -> "已撤销";
            default -> status;
        };
    }
}
