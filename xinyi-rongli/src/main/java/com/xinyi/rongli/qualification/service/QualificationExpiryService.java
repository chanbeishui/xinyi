package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.qualification.domain.QualificationSnapshot;
import com.xinyi.rongli.qualification.mapper.QualificationExpiryMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** 资格到期是版本事实；实时新单门禁不依赖任务准时，定时处理不得抛弃已有车辆责任。 */
@Service
public class QualificationExpiryService implements QualificationLifecyclePort {
    public static final String TIMER = "DRIVER_QUALIFICATION_EXPIRES";
    private final QualificationExpiryMapper mapper;
    private final DriverMapper drivers;
    private final ViAccountMapper accounts;
    private final ViAccountSecurityMapper security;
    private final OrderMapper orders;
    private final EmployeeDirectoryGuardMapper directory;
    private final QualificationSnapshotAuthority snapshots;
    private final OutboxService outbox;
    private final AuditService audit;
    private final BusinessActorResolver actors;
    private final Clock clock;
    private final TransactionTemplate tx;

    public QualificationExpiryService(
            QualificationExpiryMapper mapper,
            DriverMapper drivers,
            ViAccountMapper accounts,
            ViAccountSecurityMapper security,
            OrderMapper orders,
            EmployeeDirectoryGuardMapper directory,
            QualificationSnapshotAuthority snapshots,
            OutboxService outbox,
            AuditService audit,
            BusinessActorResolver actors,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.drivers = drivers;
        this.accounts = accounts;
        this.security = security;
        this.orders = orders;
        this.directory = directory;
        this.snapshots = snapshots;
        this.outbox = outbox;
        this.audit = audit;
        this.actors = actors;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Override
    public void decisionRecorded(
            long accountId,
            long driverId,
            long reviewId,
            Long snapshotId,
            long qualificationRevision,
            Instant expiresAt) {
        requireTransaction();
        Actor actor = actors.requireAdmin();
        var review = mapper.review(reviewId);
        if (review == null
                || number(review.get("driver_id")) != driverId
                || !Objects.equals(mapper.account(driverId), accountId)) invalid();
        if (snapshotId != null) {
            QualificationSnapshot snapshot = snapshots.authenticated(snapshotId);
            if (snapshot == null
                    || snapshot.getReviewId() != reviewId
                    || snapshot.getDriverId() != driverId
                    || snapshot.getAccountId() != accountId
                    || snapshot.getQualificationRevision() != qualificationRevision
                    || !Objects.equals(snapshot.getExpiresAt(), expiresAt)
                    || !"APPROVE".equals(review.get("decision"))) invalid();
            register(snapshot);
        } else if ("APPROVE".equals(review.get("decision"))) invalid();
        // 永久审核源事件去重：未知响应重放也不能重复递增能力版本。
        boolean changed =
                outbox.publish(
                        "QUALIFICATION_DECISION:" + reviewId,
                        "VI_DRIVER_PROFILE",
                        driverId,
                        "DRIVER_QUALIFICATION_CHANGED",
                        Map.of("reviewId", reviewId, "accountId", accountId));
        if (!changed) return;
        if (security.bumpPermissionVersion(accountId, clock.instant()) != 1)
            throw new ViException(ViErrorCode.VI_AUTHORITY_UNAVAILABLE);
        audit.recordSuccess(
                actor,
                "VI_DRIVER_PROFILE",
                driverId,
                "QUALIFICATION_PERMISSION_VERSION_CHANGED",
                null,
                Map.of("reviewId", reviewId),
                null);
    }

    /** 只补当前可信版本缺少的永久任务；既有DONE/FAILED任务不会被重建成另一份计划。 */
    public int repairMissing() {
        int count = 0;
        for (Long id : mapper.missingTimers()) {
            Boolean registered =
                    tx.execute(
                            status -> {
                                DriverProfile profile = lockProfile(id);
                                if (profile == null
                                        || !"APPROVED".equals(profile.getQualificationStatus())
                                        || profile.getEffectiveSnapshotId() == null) return false;
                                QualificationSnapshot snapshot =
                                        snapshots.authenticated(profile.getEffectiveSnapshotId());
                                if (!current(profile, snapshot)) return false;
                                return register(snapshot);
                            });
            if (Boolean.TRUE.equals(registered)) count++;
        }
        return count;
    }

    public void execute(ViOutbox event) {
        if (!TIMER.equals(event.getEventType())
                || !"VI_DRIVER_PROFILE".equals(event.getAggregateType())) invalid();
        var payload = ViJson.tree(event.getPayload());
        long snapshotId = payload.path("snapshotId").asLong(-1);
        long revision = payload.path("qualificationRevision").asLong(-1);
        if (snapshotId < 1 || revision < 1) invalid();
        tx.executeWithoutResult(status -> expire(event.getAggregateId(), snapshotId, revision));
    }

    private void expire(long driverId, long snapshotId, long revision) {
        DriverProfile profile = lockProfile(driverId);
        if (profile == null) return;
        QualificationSnapshot snapshot = snapshots.authenticated(snapshotId);
        if (snapshot == null
                || snapshot.getDriverId() != driverId
                || snapshot.getAccountId() != profile.getAccountId()
                || snapshot.getQualificationRevision() != revision) invalid();
        // 原版本任务允许最终ACK，但不能让旧期限使新生效资格过期。
        if (!current(profile, snapshot) || !"APPROVED".equals(profile.getQualificationStatus()))
            return;
        if (clock.instant().isBefore(snapshot.getExpiresAt()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "尚未到资格截止时刻");
        if (mapper.expire(driverId, snapshotId, revision, profile.getVersion()) != 1)
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        if (security.bumpPermissionVersion(profile.getAccountId(), clock.instant()) != 1)
            throw new ViException(ViErrorCode.VI_AUTHORITY_UNAVAILABLE);
        var summary = Map.of("snapshotId", snapshotId, "qualificationRevision", revision);
        audit.recordSuccess(
                Actor.system(),
                "VI_DRIVER_PROFILE",
                driverId,
                "DRIVER_QUALIFICATION_EXPIRED",
                null,
                summary,
                null);
        outbox.publish(
                "QUALIFICATION_EXPIRED:" + snapshotId,
                "VI_DRIVER_PROFILE",
                driverId,
                "DRIVER_QUALIFICATION_EXPIRED",
                summary);
    }

    private DriverProfile lockProfile(long driverId) {
        // 与资格审核、A04及MINI写入共用锁序，不能先持档案再反向等账号/目录锁。
        directory.ensureGuard();
        directory.lockGuard();
        Long accountId = mapper.account(driverId);
        if (accountId == null) return null;
        if (accounts.selectByIdForUpdate(accountId) == null
                || orders.lockSecurity(accountId) == null)
            throw new ViException(ViErrorCode.VI_AUTHORITY_UNAVAILABLE);
        return drivers.lock(driverId);
    }

    private boolean register(QualificationSnapshot snapshot) {
        return outbox.publishAt(
                "QUALIFICATION_EXPIRY:" + snapshot.getId(),
                "VI_DRIVER_PROFILE",
                snapshot.getDriverId(),
                TIMER,
                Map.of(
                        "snapshotId",
                        snapshot.getId(),
                        "qualificationRevision",
                        snapshot.getQualificationRevision()),
                snapshot.getExpiresAt());
    }

    private static boolean current(DriverProfile profile, QualificationSnapshot snapshot) {
        return snapshot != null
                && snapshot.getDriverId() == profile.getId()
                && Objects.equals(profile.getEffectiveSnapshotId(), snapshot.getId())
                && Objects.equals(
                        profile.getQualificationRevision(), snapshot.getQualificationRevision());
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : Long.MIN_VALUE;
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("资格权限版本与任务必须随审核事务提交");
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
