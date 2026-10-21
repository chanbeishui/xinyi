package com.xinyi.rongli.platform.service;

import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.domain.ViEventLog;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.mapper.ViEventLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 双主体审计（ADR-02）。写入不可变 vi_event_log：
 * - actor_realm 区分 MINI/ADMIN/SYSTEM/CALLBACK；MINI 落 actor_account_id，ADMIN 落 actor_user_id，
 *   两者绝不数值混比；同人比较须经可信绑定（I01/I03），不在此处比较两类 ID。
 * - before/after_summary 为脱敏 JSON，经 SecretGuard 拒绝明文 token/credential/launchUrl。
 * - 成功审计随业务同事务（REQUIRED）：业务回滚则成功流水不留下，落实“失败不写成功日志”。
 * - 失败审计独立事务（REQUIRES_NEW）：即便主事务回滚也持久化拒绝/失败事实。
 */
@Service
public class AuditService {

    private final ViEventLogMapper mapper;
    private final SecretGuard secretGuard;

    public AuditService(ViEventLogMapper mapper, SecretGuard secretGuard) {
        this.mapper = mapper;
        this.secretGuard = secretGuard;
    }

    /** 成功事实：与业务写同事务提交。 */
    @Transactional
    public void recordSuccess(Actor actor, String aggregateType, long aggregateId, String action,
                              Object before, Object after, String reason) {
        insert(build(actor, aggregateType, aggregateId, action, before, after, reason, null));
    }

    /** 成功事实（带订单关联）。 */
    @Transactional
    public void recordSuccess(Actor actor, String aggregateType, long aggregateId, String action,
                              Object before, Object after, String reason, Long orderId) {
        insert(build(actor, aggregateType, aggregateId, action, before, after, reason, orderId));
    }

    /** 失败/拒绝事实：独立事务持久化，主事务回滚不丢失。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Actor actor, String aggregateType, long aggregateId, String action,
                              Object before, Object after, String reason) {
        insert(build(actor, aggregateType, aggregateId, action, before, after, reason, null));
    }

    private void insert(ViEventLog record) {
        int affected = mapper.insert(record);
        if (affected != 1) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "审计写入失败（受影响行数=" + affected + "）");
        }
    }

    private ViEventLog build(Actor actor, String aggregateType, long aggregateId, String action,
                             Object before, Object after, String reason, Long orderId) {
        if (actor == null) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "审计缺少操作人 Actor");
        }
        ViEventLog log = new ViEventLog();
        log.setOrderId(orderId);
        log.setAggregateType(aggregateType);
        log.setAggregateId(aggregateId);
        log.setAction(action);
        log.setActorRealm(actor.realm().name());
        // 双主体：按域分别落对应 ID，不交叉、不数值混比。
        if (actor.isMini()) {
            log.setActorAccountId(actor.accountId());
        } else if (actor.isAdmin()) {
            log.setActorUserId(actor.sysUserId());
        }
        log.setActingRole(actor.actingRole());
        log.setStationId(actor.stationId());
        log.setSessionId(actor.sessionId());
        log.setContextVersion(actor.contextVersion());
        log.setBeforeSummary(toSummaryJson(before));
        log.setAfterSummary(toSummaryJson(after));
        log.setReason(reason);
        return log;
    }

    private String toSummaryJson(Object value) {
        if (value == null) {
            return null;
        }
        String json = ViJson.write(value);
        secretGuard.assertNoSecret(json);
        return json;
    }
}
