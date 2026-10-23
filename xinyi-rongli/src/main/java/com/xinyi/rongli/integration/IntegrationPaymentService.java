package com.xinyi.rongli.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.mapper.IntegrationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.DeptScope;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 支付方式选择与客户线下收退款核实（PRD 23.2 / 23.4）。
 *
 * <p>本版线下支付的定义是「先由客户选择线下方式，后台登记真实收款，另一名有权人员核实全额到账后才算已支付」，
 * 不是服务后赊账，也不提供“我已付款即成功”的按钮。两条红线：</p>
 * <ul>
 *   <li>核实人不能是登记人（数据库 CHECK 与服务端双重校验）；</li>
 *   <li>线下退款只登记人工流水，绝不调用微信退款接口。</li>
 * </ul>
 */
@Service
public class IntegrationPaymentService
{
    private static final List<String> DIRECTIONS = List.of("COLLECTION", "REFUND");

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private IntegrationConfigService configService;

    @Autowired
    private Crypto crypto;

    // ================================================================ 小程序：支付方式

    public Map<String, Object> paymentOptions(String orderNo)
    {
        Row order = loadOwnOrder(orderNo);
        Row payment = Row.of(mapper.findPaymentByOrderId(order.lng("id")));
        if (payment.isEmpty())
        {
            throw ApiException.notFound("该订单还没有支付单");
        }
        Long deptId = payment.lng("receiverDeptId") != null ? payment.lng("receiverDeptId") : order.lng("deptId");
        Row policyProfile = configService.activeProfile("PAYMENT_POLICY", deptId);
        Row policyRevision = configService.activeRevision(policyProfile);
        Map<String, Object> policy = configService.configOf(policyRevision);

        boolean offlineEnabled = IntegrationRules.bool(policy, "offlineEnabled");
        boolean wechatEnabled = IntegrationRules.bool(policy, "wechatEnabled");
        String locked = payment.str("paymentMethod");
        boolean lockedOffline = "OFFLINE".equals(locked);
        boolean lockedWechat = "WECHAT".equals(locked);
        LocalDateTime expiresAt = payment.time("expiresAt");
        boolean expired = expiresAt != null && expiresAt.isBefore(ViTime.now());
        boolean paid = "SUCCESS".equals(payment.str("status"));

        List<Map<String, Object>> options = new ArrayList<>();
        Map<String, Object> wechat = new LinkedHashMap<>();
        wechat.put("method", "WECHAT");
        boolean wechatReady = wechatEnabled && wechatReady(deptId);
        boolean wechatAvailable = !paid && !expired && (lockedWechat || (!lockedOffline && wechatReady));
        wechat.put("available", wechatAvailable);
        wechat.put("reasonCode", !wechatEnabled ? "POLICY_DISABLED" : (lockedOffline ? "METHOD_LOCKED"
                : (expired ? "ORDER_EXPIRED" : (paid ? "ALREADY_PAID" : (wechatReady ? null : "MERCHANT_NOT_CONFIGURED")))));
        wechat.put("label", "微信支付");
        options.add(wechat);

        Map<String, Object> offline = new LinkedHashMap<>();
        offline.put("method", "OFFLINE");
        boolean offlineAvailable = !paid && !expired && (lockedOffline || (!lockedWechat && offlineEnabled));
        offline.put("available", offlineAvailable);
        offline.put("reasonCode", !offlineEnabled ? "POLICY_DISABLED" : (lockedWechat ? "METHOD_LOCKED"
                : (expired ? "ORDER_EXPIRED" : (paid ? "ALREADY_PAID" : null))));
        offline.put("label", "线下付款");
        offline.put("instructions", IntegrationRules.str(policy, "offlineInstructions"));
        offline.put("receiverName", IntegrationRules.str(policy, "offlineReceiverName"));
        offline.put("bankName", IntegrationRules.str(policy, "offlineBankName"));
        offline.put("bankAccountName", IntegrationRules.str(policy, "offlineAccountName"));
        offline.put("offlineMethods", IntegrationRules.list(policy, "offlineMethods"));
        offline.put("contactName", IntegrationRules.str(policy, "offlineContactName"));
        offline.put("contactPhone", IntegrationRules.str(policy, "offlineContactPhone"));
        offline.put("receiverAccountDisplay", offlineAccountDisplay(policyProfile));
        options.add(offline);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderNo", order.str("orderNo"));
        out.put("paymentVersion", payment.lng("version", 0L));
        out.put("policyRevisionId", policyRevision.isEmpty() ? null : String.valueOf(policyRevision.lng("id")));
        out.put("options", options);
        out.put("lockedMethod", locked);
        out.put("expiresAt", ViTime.iso(expiresAt));
        out.put("newPaymentAllowed", !paid && !expired && (offlineEnabled || wechatEnabled));
        if (policyRevision.isEmpty())
        {
            out.put("note", "当前主体尚未发布支付方式配置，按不收款处理：不展示可提交的支付方式");
        }
        return out;
    }

    /** 用户选择线下：锁定方式，订单仍为待支付，不产生“已付款”事实 */
    @Transactional
    public Map<String, Object> chooseOffline(String orderNo, Map<String, Object> body)
    {
        Row order = loadOwnOrder(orderNo);
        Row payment = Row.of(mapper.lockPaymentByOrderId(order.lng("id")));
        if (payment.isEmpty())
        {
            throw ApiException.notFound("该订单没有支付单");
        }
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != payment.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "支付状态已变化，请刷新后重试");
        }
        if ("SUCCESS".equals(payment.str("status")))
        {
            throw ApiException.stateConflict("订单已支付，不能再改为线下收款");
        }
        if (payment.time("expiresAt") != null && payment.time("expiresAt").isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("支付已超过30分钟，请重新预约");
        }
        Long deptId = payment.lng("receiverDeptId") != null ? payment.lng("receiverDeptId") : order.lng("deptId");
        Row policyProfile = configService.activeProfile("PAYMENT_POLICY", deptId);
        Row policyRevision = configService.activeRevision(policyProfile);
        if (policyRevision.isEmpty())
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "当前主体尚未开启线下支付");
        }
        String expectedPolicy = IntegrationRules.str(body, "expectedPolicyRevisionId");
        if (expectedPolicy != null && !expectedPolicy.equals(String.valueOf(policyRevision.lng("id"))))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "支付方式配置已更新，请重新进入支付页确认");
        }
        Map<String, Object> policy = configService.configOf(policyRevision);
        if (!IntegrationRules.bool(policy, "offlineEnabled"))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "线下支付当前未开启");
        }
        String current = payment.str("paymentMethod");
        if ("WECHAT".equals(current))
        {
            throw ApiException.stateConflict("该订单已锁定微信支付，如需改线下请先关闭原交易后重新预约");
        }
        if ("UNSELECTED".equals(current)
                && mapper.lockOfflineIntent(payment.lng("id"), policyRevision.lng("id"),
                        payment.lng("version", 0L)) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "支付方式已被其他操作锁定，请刷新");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderNo", order.str("orderNo"));
        out.put("paymentVersion", payment.lng("version", 0L) + 1);
        out.put("lockedMethod", "OFFLINE");
        out.put("paymentStatus", "PENDING");
        out.put("amountFen", payment.lng("totalFen", 0L));
        out.put("instructions", IntegrationRules.str(policy, "offlineInstructions"));
        out.put("offlineMethods", IntegrationRules.list(policy, "offlineMethods"));
        out.put("contactName", IntegrationRules.str(policy, "offlineContactName"));
        out.put("contactPhone", IntegrationRules.str(policy, "offlineContactPhone"));
        out.put("note", "已记录你的线下付款意向，订单仍为待支付；后台登记并核实到账后才会进入派单");
        return out;
    }

    /** 微信首次预支付时锁定渠道与商户身份快照（由支付服务调用） */
    @Transactional
    public void lockWechat(String orderNo, Long merchantProfileId, Long merchantRevisionId, String appId,
            String mchid)
    {
        Row order = Row.of(mapper.findOrderByNo(orderNo));
        if (order.isEmpty())
        {
            return;
        }
        Row payment = Row.of(mapper.findPaymentByOrderId(order.lng("id")));
        if (payment.isEmpty() || !"UNSELECTED".equals(payment.str("paymentMethod")))
        {
            return;
        }
        Long deptId = payment.lng("receiverDeptId") != null ? payment.lng("receiverDeptId") : order.lng("deptId");
        Row policy = configService.activeRevision(configService.activeProfile("PAYMENT_POLICY", deptId));
        mapper.lockWechatIntent(payment.lng("id"), policy.isEmpty() ? null : policy.lng("id"),
                merchantProfileId, merchantRevisionId, appId, mchid, payment.lng("version", 0L));
    }

    // ================================================================ 后台：线下凭证

    public Map<String, Object> listProofs(String ownerDeptId, String status, String direction, Integer pageNum,
            Integer pageSize)
    {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 200 ? 20 : pageSize;
        Long deptId = IntegrationConfigService.longOrNull(ownerDeptId);
        Long scope = DeptScope.scopedDeptId();
        if (scope != null)
        {
            if (deptId != null && !deptId.equals(scope))
            {
                throw ApiException.forbidden("只能查看本主体的线下资金凭证");
            }
            deptId = scope;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listOfflineRecords(deptId, blank(status), blank(direction),
                (page - 1) * size, size))
        {
            rows.add(proofView(Row.of(row), false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", mapper.countOfflineRecords(deptId, blank(status), blank(direction)));
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    public Map<String, Object> proofDetail(Long proofId)
    {
        Row proof = loadProof(proofId);
        return proofView(proof, true);
    }

    @Transactional
    public Map<String, Object> registerProof(Map<String, Object> body)
    {
        String direction = IntegrationRules.str(body, "direction");
        if (direction == null || !DIRECTIONS.contains(direction))
        {
            throw ApiException.validation("direction 只能是 COLLECTION 或 REFUND");
        }
        String method = IntegrationRules.str(body, "method");
        if (method == null || !List.of("CASH", "BANK_TRANSFER").contains(method))
        {
            throw ApiException.validation("收款方式只能是 CASH 或 BANK_TRANSFER");
        }
        String orderNo = IntegrationRules.str(body, "orderNo");
        if (orderNo == null)
        {
            throw ApiException.validation("请指定订单号");
        }
        Row order = Row.of(mapper.findOrderByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Row payment = Row.of(mapper.lockPaymentByOrderId(order.lng("id")));
        if (payment.isEmpty())
        {
            throw ApiException.notFound("该订单没有支付单");
        }
        long amountFen = IntegrationConfigService.longOf(body.get("actualAmount"), 0L);
        if (amountFen <= 0)
        {
            throw ApiException.validation("实际金额必须大于 0（单位：分）");
        }
        String note = IntegrationRules.str(body, "note");
        if (note == null || note.length() < 2)
        {
            throw ApiException.validation("请填写登记说明");
        }
        String externalReference = IntegrationRules.str(body, "externalReference");
        if (externalReference == null || externalReference.length() > 128)
        {
            throw ApiException.validation("请填写主体内唯一的凭证号/银行流水（不超过 128 字）");
        }
        Long deptId = payment.lng("receiverDeptId") != null ? payment.lng("receiverDeptId") : order.lng("deptId");
        String referenceHmac = Crypto.hex(crypto.hmac(deptId + "|" + direction + "|" + method + "|" + externalReference));
        if (mapper.findOfflineByReference(deptId, direction, method, referenceHmac) != null)
        {
            throw ApiException.conflict("VI_CONFIG_DUPLICATE_REFERENCE", "该流水号已登记过，不能重复入账");
        }

        Long refundId = null;
        if ("COLLECTION".equals(direction))
        {
            if (!"OFFLINE".equals(payment.str("paymentMethod")))
            {
                throw ApiException.unprocessable("VI_CONFIG_INVALID",
                        "该订单未选择线下付款方式，不能登记线下收款；请先在支付页选择线下");
            }
            if (amountFen != payment.lng("totalFen", 0L))
            {
                throw ApiException.unprocessable("VI_CONFIG_INVALID",
                        "本期只接受一次全额收款（应付 " + payment.lng("totalFen", 0L) + " 分），少款/多款走人工差错处理");
            }
        }
        else
        {
            String refundNo = IntegrationRules.str(body, "refundNo");
            Row refund = refundNo == null ? Row.empty() : Row.of(mapper.findRefundByNo(refundNo));
            if (refund.isEmpty())
            {
                throw ApiException.validation("请指定已由统一售后批准的退款单号");
            }
            if (!"OFFLINE".equals(payment.str("paymentMethod")))
            {
                throw ApiException.unprocessable("VI_CONFIG_INVALID",
                        "原微信收款订单必须按原商户走微信退款，不能登记为线下退款");
            }
            if (amountFen != refund.lng("amountFen", 0L))
            {
                throw ApiException.unprocessable("VI_CONFIG_INVALID", "线下退款金额必须与已批准的退款金额一致");
            }
            refundId = refund.lng("id");
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("paymentId", payment.lng("id"));
        row.put("refundId", refundId);
        row.put("direction", direction);
        row.put("ownerDeptId", deptId);
        row.put("amountFen", amountFen);
        row.put("paidAt", parsePaidAt(body.get("paidAt")));
        row.put("method", method);
        row.put("referenceCipher", crypto.encrypt(externalReference));
        row.put("referenceHmac", referenceHmac);
        row.put("voucherFileIds", com.xinyi.rongli.support.Json.write(IntegrationRules.list(body, "voucherFileIds")));
        row.put("registeredBy", configService.currentUserId());
        row.put("note", note);
        mapper.insertOfflineRecord(row);
        if ("COLLECTION".equals(direction))
        {
            mapper.markReviewPending(payment.lng("id"), payment.lng("version", 0L));
        }
        return proofView(Row.of(mapper.findOfflineRecord(Row.of(row).lng("id"))), true);
    }

    @Transactional
    public Map<String, Object> verifyProof(Long proofId, Map<String, Object> body)
    {
        Row proof = Row.of(mapper.lockOfflineRecord(proofId));
        if (proof.isEmpty())
        {
            throw ApiException.notFound("凭证不存在");
        }
        Long scope = DeptScope.scopedDeptId();
        if (scope != null && !scope.equals(proof.lng("ownerDeptId")))
        {
            throw ApiException.forbidden("无权核实其它主体的线下资金");
        }
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != proof.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "凭证已被他人处理，请刷新");
        }
        String decision = IntegrationRules.str(body, "decision");
        if (!List.of("VERIFY", "REJECT").contains(decision))
        {
            throw ApiException.validation("decision 只能是 VERIFY 或 REJECT");
        }
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("请填写核实/驳回原因");
        }
        Long userId = configService.currentUserId();
        if (userId.equals(proof.lng("registeredBy")))
        {
            throw ApiException.forbidden("登记人不能核实自己登记的收款，请由另一位有权人员处理");
        }

        String status = "VERIFY".equals(decision) ? "VERIFIED" : "REJECTED";
        if (mapper.verifyOfflineRecord(proofId, status, userId, reason, expectedVersion) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "凭证状态已变化，请刷新");
        }
        String capacity = null;
        String note = null;
        if ("VERIFIED".equals(status))
        {
            Row payment = Row.of(mapper.findPaymentById(proof.lng("paymentId")));
            if ("COLLECTION".equals(proof.str("direction")))
            {
                Row order = Row.of(mapper.findOrderByNo(orderNoOf(payment)));
                boolean stillValid = order.isNotEmpty() && "PENDING_PAY".equals(order.str("fulfillmentStatus"))
                        && order.time("payExpiresAt") != null && order.time("payExpiresAt").isAfter(ViTime.now());
                capacity = stillValid ? "HELD" : "MANUAL_CONFLICT";
                mapper.markOfflinePaid(payment.lng("id"), proof.lng("amountFen", 0L), proof.time("paidAt"),
                        capacity, payment.lng("version", 0L));
                if (stillValid)
                {
                    mapper.releaseToScheduledAfterOfflinePaid(order.lng("id"));
                    note = "已核实全额到账，订单进入待派单";
                }
                else
                {
                    note = "已核实到账，但原预约已过期或容量不可用：资金事实成立，订单保持不可派单，转人工处理";
                }
            }
            else
            {
                mapper.markOfflineRefundDone(proof.lng("refundId"));
                mapper.addRefundedFen(payment.lng("id"), proof.lng("amountFen", 0L));
                capacity = "RELEASED";
                note = "线下退款已核实完成，未调用微信退款接口";
            }
        }
        else
        {
            Row payment = Row.of(mapper.findPaymentById(proof.lng("paymentId")));
            if ("COLLECTION".equals(proof.str("direction")) && "REVIEW_PENDING".equals(payment.str("status")))
            {
                mapper.revertToPending(payment.lng("id"), payment.lng("version", 0L));
            }
            note = "已驳回该凭证，收款状态回到待支付；客户可补充材料后重新登记";
        }
        Map<String, Object> out = proofView(Row.of(mapper.findOfflineRecord(proofId)), true);
        out.put("capacityResolution", capacity == null ? out.get("capacityResolution") : capacity);
        out.put("result", note);
        return out;
    }

    // ================================================================ 视图

    private Map<String, Object> proofView(Row row, boolean withDetail)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("proofId", String.valueOf(row.lng("id")));
        out.put("direction", row.str("direction"));
        out.put("ownerDeptId", String.valueOf(row.lng("ownerDeptId", 0L)));
        out.put("amount", row.lng("amountFen", 0L));
        out.put("paidAt", ViTime.iso(row.time("paidAt")));
        out.put("method", row.str("method"));
        out.put("status", row.str("status"));
        out.put("version", row.lng("version", 0L));
        out.put("registeredBy", String.valueOf(row.lng("registeredBy", 0L)));
        out.put("verifiedBy", row.lng("verifiedBy") == null ? null : String.valueOf(row.lng("verifiedBy")));
        out.put("registeredAt", ViTime.iso(row.time("registeredAt")));
        out.put("verifiedAt", ViTime.iso(row.time("verifiedAt")));
        out.put("verifyReason", row.str("verifyReason"));
        out.put("registerNote", row.str("registerNote"));
        out.put("capacityResolution", capacityOf(row));
        String reference = row.get("externalReferenceCipher") == null ? null
                : crypto.decrypt((byte[]) row.get("externalReferenceCipher"));
        out.put("externalReferenceMask", IntegrationRules.maskReference(reference));
        Row payment = Row.of(mapper.findPaymentById(row.lng("paymentId")));
        if (payment.isNotEmpty())
        {
            out.put("orderNo", orderNoOf(payment));
        }
        if (row.lng("refundId") != null)
        {
            Row refund = Row.of(mapper.findRefundById(row.lng("refundId")));
            out.put("refundNo", refund.isNotEmpty() ? refund.str("outRefundNo") : null);
        }
        if (withDetail)
        {
            out.put("voucherFiles", voucherFiles(row));
        }
        else
        {
            out.put("voucherFiles", List.of());
        }
        return out;
    }

    private List<Object> voucherFiles(Row row)
    {
        String json = row.str("voucherFileIds");
        if (json == null || json.isBlank())
        {
            return List.of();
        }
        try
        {
            return com.alibaba.fastjson2.JSON.parseArray(json, Object.class);
        }
        catch (Exception e)
        {
            return List.of();
        }
    }

    private String capacityOf(Row row)
    {
        Row payment = Row.of(mapper.findPaymentById(row.lng("paymentId")));
        return payment.isEmpty() ? null : payment.str("capacityResolution");
    }

    private String orderNoOf(Row payment)
    {
        if (payment == null || payment.isEmpty())
        {
            return null;
        }
        Long orderId = payment.lng("orderId");
        if (orderId == null)
        {
            return null;
        }
        // 支付单只存 order_id，这里用最小查询换回单号
        return mapper.findOrderNoById(orderId);
    }

    private String offlineAccountDisplay(Row policyProfile)
    {
        if (policyProfile == null || policyProfile.isEmpty())
        {
            return null;
        }
        Row secret = Row.of(mapper.findActiveSecret(policyProfile.lng("id"), "offlineBankAccount"));
        if (secret.isEmpty())
        {
            return null;
        }
        String account = crypto.decrypt((byte[]) secret.get("ciphertext"));
        return IntegrationRules.maskReference(account);
    }

    private boolean wechatReady(Long deptId)
    {
        Row merchant = configService.activeProfile("WECHAT_PAY", deptId);
        Row revision = configService.activeRevision(merchant);
        if (revision.isEmpty())
        {
            return false;
        }
        Map<String, Object> config = configService.configOf(revision);
        @SuppressWarnings("unchecked")
        Map<String, Object> refs = revision.str("secretRefsJson") == null ? Map.of()
                : com.alibaba.fastjson2.JSON.parseObject(revision.str("secretRefsJson"), Map.class);
        return !IntegrationRules.hasError(IntegrationRules.validate("WECHAT_PAY", config, refs.keySet(), true));
    }

    // ================================================================ 内部

    private Row loadOwnOrder(String orderNo)
    {
        Row order = Row.of(mapper.findOrderByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Long accountId = MiniContext.require().accountId;
        if (!accountId.equals(order.lng("ownerAccountId")))
        {
            throw ApiException.forbidden("只能查看自己的订单");
        }
        return order;
    }

    private Row loadProof(Long proofId)
    {
        Row row = Row.of(mapper.findOfflineRecord(proofId));
        if (row.isEmpty())
        {
            throw ApiException.notFound("凭证不存在");
        }
        Long scope = DeptScope.scopedDeptId();
        if (scope != null && !scope.equals(row.lng("ownerDeptId")))
        {
            throw ApiException.forbidden("无权查看其它主体的线下资金凭证");
        }
        return row;
    }

    private LocalDateTime parsePaidAt(Object value)
    {
        if (value == null)
        {
            return ViTime.now();
        }
        LocalDateTime parsed = ViTime.parseFlexible(String.valueOf(value));
        if (parsed == null)
        {
            throw ApiException.validation("实际收款时间格式不正确");
        }
        return parsed;
    }

    private static String blank(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
