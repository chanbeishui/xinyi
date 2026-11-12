package com.xinyi.rongli.dispatch.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.dispatch.dto.InviteRequests;
import com.xinyi.rongli.dispatch.mapper.*;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.jobs.mapper.DispatchDeadlineMapper;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.order.service.OrderReadService;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

/** 目录守卫串行化派单/权限/取消边界，DB订单、司机和承接唯一键继续作硬兜底；所有渠道网络均不在此事务内。 */
@Service
public class DispatchEngineService implements DispatchLifecyclePort {
    public static final String LIST = "biz:dispatch:list",
            CREATE = "biz:dispatch:create",
            REVOKE = "biz:dispatch:revoke";
    public static final Set<String> EVENTS =
            Set.of(
                    "DISPATCH_REQUESTED",
                    "DISPATCH_CONTINUE",
                    "DISPATCH_INVITE_EXPIRES",
                    "DISPATCH_BATCH_DEADLINE");
    private final DispatchInviteMapper mapper;
    private final DispatchPolicyService policies;
    private final DispatchCandidateAuthority candidates;
    private final DispatchAuthority mini;
    private final FileAuthority files;
    private final IntegrationAuthority admin;
    private final IntegrationCrypto crypto;
    private final BusinessActorResolver actors;
    private final EmployeeDirectoryGuardMapper directory;
    private final PaymentMapper payments;
    private final DispatchDeadlineMapper conditions;
    private final DriverMapper drivers;
    private final OrderReadService orderReads;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final Clock clock;
    private final TransactionTemplate tx;

    public DispatchEngineService(
            DispatchInviteMapper mapper,
            DispatchPolicyService policies,
            DispatchCandidateAuthority candidates,
            DispatchAuthority mini,
            FileAuthority files,
            IntegrationAuthority admin,
            IntegrationCrypto crypto,
            BusinessActorResolver actors,
            EmployeeDirectoryGuardMapper directory,
            PaymentMapper payments,
            DispatchDeadlineMapper conditions,
            DriverMapper drivers,
            OrderReadService orderReads,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.policies = policies;
        this.candidates = candidates;
        this.mini = mini;
        this.files = files;
        this.admin = admin;
        this.crypto = crypto;
        this.actors = actors;
        this.directory = directory;
        this.payments = payments;
        this.conditions = conditions;
        this.drivers = drivers;
        this.orderReads = orderReads;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Page(List<?> rows, long total) {}

    public record Receipt(long inviteId) {}

    public Page driverInvites(InviteRequests.Query query) {
        return miniTransaction(
                "DISPATCH_INVITES_READ",
                () -> {
                    DriverProfile profile = mini.lock(mini.actor());
                    List<?> rows =
                            mapper.invites(null, profile.getId(), query, now()).stream()
                                    .map(row -> inviteView(row, mini.actor()))
                                    .toList();
                    return new Page(rows, mapper.inviteCount(null, profile.getId(), query, now()));
                });
    }

    public Map<String, Object> driverInvite(long id) {
        return miniTransaction(
                "DISPATCH_INVITE_READ",
                () -> {
                    DriverProfile profile = mini.lock(mini.actor());
                    DispatchInvite row = mapper.invite(id);
                    requireOwner(row, profile);
                    return inviteView(row, mini.actor());
                });
    }

    public Object currentAssignment() {
        return miniTransaction(
                "CURRENT_ASSIGNMENT_READ",
                () -> {
                    DriverProfile row = mini.lock(mini.actor());
                    Long order = mapper.currentOrder(row.getId());
                    if (order == null) return null;
                    ViOrder actual = payments.lockOrder(order);
                    // OrderReadService读取真实当前MINI身份和本人承接关系；不构造OWNER或伪MINI扩大地址权限。
                    return orderReads.detail("DRIVER", actual.getOrderNo());
                });
    }

    public Map<String, Object> respond(long id, InviteRequests.Response input, String key) {
        return miniTransaction(
                "DISPATCH_INVITE_RESPONSE",
                () -> {
                    Actor actor = mini.actor();
                    // 先校验真实工作视角，不能让他人的来源观察值把OWNER越权误报成context冲突。
                    if (!"DRIVER".equals(actor.actingRole()))
                        throw new ViException(ViErrorCode.VI_FORBIDDEN);
                    DispatchInvite observed = mapper.findInvite(id);
                    if (observed == null) invisible();
                    ViOrder order = payments.lockOrder(observed.getOrderId());
                    payments.lockPayment(order.getId());
                    DispatchBatch batch = mapper.batch(order.getId());
                    DriverProfile profile = mini.lock(actor);
                    requireOwner(observed, profile);
                    // 只有本人可见目标才比较请求观察；他人邀约始终隐藏为404，不泄露目标上下文。
                    if (!Objects.equals(actor.sessionId(), input.expectedSessionId())
                            || !Objects.equals(
                                    actor.contextVersion(), input.expectedContextVersion()))
                        throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
                    Receipt result =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "DISPATCH_INVITE_RESPONSE:" + id,
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () ->
                                            respondLocked(
                                                    order,
                                                    batch,
                                                    mapper.invite(id),
                                                    profile,
                                                    input,
                                                    actor));
                    return inviteView(mapper.invite(result.inviteId()), actor);
                });
    }

    private Receipt respondLocked(
            ViOrder order,
            DispatchBatch batch,
            DispatchInvite invite,
            DriverProfile profile,
            InviteRequests.Response input,
            Actor actor) {
        // invite→assignment是永久领域幂等，不依赖24小时请求缓存；双设备不同UUID仍返回同一真实承接。
        if ("ACCEPTED".equals(invite.getStatus()) && "ACCEPT".equals(input.decision())) {
            if (mapper.assignment(invite.getId()) == null) conflict("原邀约承接关系异常，禁止再次占用");
            return new Receipt(invite.getId());
        }
        if (!"WAIT_RESPONSE".equals(invite.getStatus())) conflict("邀约已结束，不能改变原决定");
        if (invite.getVersion() != input.expectedVersion()
                || order.getVersion() != input.expectedOrderVersion()) version();
        if (!invite.getExpiresAt().isAfter(now())) conflict("邀约已到期，请刷新");
        if ("REJECT".equals(input.decision())) {
            finish(invite, "REJECTED", actor, input.reason());
            mapper.release(invite.getDriverId(), order.getId(), invite.getId());
            continueEvent(order, invite);
            return new Receipt(invite.getId());
        }
        String block = operational(order);
        if (block != null) conflict("原订单暂不可承接：" + block);
        if (batch == null
                || !Objects.equals(invite.getBatchId(), batch.getId())
                || !batch.getDeadlineAt().isAfter(now())
                || !Objects.equals(
                        invite.getCompanyRevision(), profile.getTrainingContextRevision()))
            conflict("邀约批次或公司归属已变化");
        Map<String, Object> policy = policies.frozen(order);
        if (policy == null) conflict("原订单派单策略缺失或不可信");
        var candidate =
                candidates.eligible(
                        order,
                        policy,
                        profile.getId(),
                        radius(policy, batch.getRadiusStage()),
                        invite.getId());
        if (candidate == null) conflict("资格、听单、设备来源、位置、车型或唯一占用已变化");
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("orderId", order.getId());
        assignment.put("driverId", profile.getId());
        assignment.put("inviteId", invite.getId());
        assignment.put("acceptedAt", now());
        mapper.insertAssignment(assignment);
        long assignmentId = ((Number) assignment.get("id")).longValue();
        if (mapper.promote(profile.getId(), order.getId(), invite.getId(), assignmentId) != 1)
            conflict("原邀约占用不一致");
        finish(invite, "ACCEPTED", actor, input.reason());
        transition(order, "ACCEPTED", assignmentId);
        batch(batch, "ASSIGNED", null);
        outbox.publish(
                "DRIVER_ASSIGNMENT_CREATED:" + assignmentId,
                "VI_DRIVER_ASSIGNMENT",
                assignmentId,
                "DRIVER_ASSIGNMENT_CREATED",
                Map.of(
                        "assignmentId",
                        Long.toString(assignmentId),
                        "orderId",
                        order.getId().toString(),
                        "driverId",
                        profile.getId().toString()));
        return new Receipt(invite.getId());
    }

    public Page queue(InviteRequests.Query query) {
        return adminTransaction(
                "DISPATCH_QUEUE_READ",
                () -> {
                    Set<Long> scope = admin.scope(LIST);
                    List<?> rows =
                            mapper.queue(scope, query).stream().map(this::queueView).toList();
                    return new Page(rows, mapper.queueCount(scope, query));
                });
    }

    public Page candidateList(InviteRequests.Query query) {
        return adminTransaction(
                "DISPATCH_CANDIDATES_READ",
                () -> {
                    ViOrder order = adminOrder(query.orderNo(), LIST);
                    Map<String, Object> policy = policies.frozen(order);
                    if (policy == null || operational(order) != null) return new Page(List.of(), 0);
                    DispatchBatch batch = mapper.batch(order.getId());
                    boolean activeBatch = activeBatch(batch);
                    if (activeBatch && !batch.getDeadlineAt().isAfter(now()))
                        return new Page(List.of(), 0);
                    int stage = activeBatch ? batch.getRadiusStage() : 0;
                    List<DispatchCandidateAuthority.Candidate> actual = new ArrayList<>();
                    for (long driver : mapper.drivers(order.getServiceCompanyId())) {
                        if (activeBatch && mapper.alreadyInvited(batch.getId(), driver) > 0)
                            continue;
                        var candidate =
                                candidates.eligible(
                                        order, policy, driver, radius(policy, stage), null);
                        if (candidate != null) {
                            // 当前批次截止是已知上界；候选展示不得比服务端仍允许发邀约的窗口活得更久。
                            if (activeBatch)
                                candidate =
                                        new DispatchCandidateAuthority.Candidate(
                                                candidate.driver(),
                                                candidate.location(),
                                                candidate.presence(),
                                                candidate.distanceMeter(),
                                                min(candidate.expiresAt(), batch.getDeadlineAt()));
                            actual.add(candidate);
                        }
                    }
                    if ("distanceAsc".equals(query.sort()))
                        actual.sort(
                                Comparator.comparingInt(
                                                DispatchCandidateAuthority.Candidate::distanceMeter)
                                        .thenComparing(c -> c.driver().getId()));
                    else cursorSort(actual, order.getServiceCompanyId());
                    int from = Math.min(query.offset(), actual.size()),
                            to = Math.min(from + query.pageSize(), actual.size());
                    boolean create = can(order.getDeptId(), CREATE);
                    List<?> rows =
                            actual.subList(from, to).stream()
                                    .map(
                                            c -> {
                                                Map<String, Object> view = candidates.view(c);
                                                if (!create) view.put("allowedActions", List.of());
                                                return view;
                                            })
                                    .toList();
                    return new Page(rows, actual.size());
                });
    }

    public Page adminInvites(InviteRequests.Query query) {
        return adminTransaction(
                "DISPATCH_INVITES_READ",
                () -> {
                    Set<Long> scope = admin.scope(LIST);
                    List<?> rows =
                            mapper.invites(scope, null, query, now()).stream()
                                    .map(i -> inviteView(i, admin.actor()))
                                    .toList();
                    return new Page(rows, mapper.inviteCount(scope, null, query, now()));
                });
    }

    public Map<String, Object> manual(InviteRequests.Manual input, String key) {
        return adminTransaction(
                "DISPATCH_MANUAL_INVITE",
                () -> {
                    ViOrder order = adminOrder(input.orderNo(), LIST);
                    department(order, CREATE);
                    Receipt result =
                            idem.execute(
                                    IdempotencyService.subjectKey(admin.actor()),
                                    "DISPATCH_MANUAL_INVITE:" + order.getId(),
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () -> manualLocked(order, input));
                    return inviteView(mapper.invite(result.inviteId()), admin.actor());
                });
    }

    private Receipt manualLocked(ViOrder order, InviteRequests.Manual input) {
        if (order.getVersion() != input.expectedOrderVersion()) version();
        String reason = operational(order);
        if (reason != null) conflict("原订单不满足邀约条件：" + reason);
        Map<String, Object> policy = policies.frozen(order);
        if (policy == null) conflict("原单缺少冻结公司派单政策，不能猜补");
        if (mapper.activeInvite(order.getId()) != null) conflict("原单已有待响应邀约");
        DispatchBatch batch = mapper.batch(order.getId());
        if (batch == null || Set.of("CLOSED", "MANUAL", "ASSIGNED").contains(batch.getStatus()))
            batch = newBatch(order, policy, "MANUAL");
        if (!batch.getDeadlineAt().isAfter(now())) conflict("原派单总截止已到，请先协调预约");
        if (mapper.alreadyInvited(batch.getId(), input.driverId()) > 0) conflict("本批次已经邀约该代驾");
        DriverProfile target = drivers.find(input.driverId());
        if (target == null || !Objects.equals(target.getOwnerDeptId(), order.getServiceCompanyId()))
            invisible();
        var candidate =
                candidates.eligible(
                        order,
                        policy,
                        input.driverId(),
                        radius(policy, batch.getRadiusStage()),
                        null);
        if (candidate == null) conflict("指定代驾当前不符合原订单候选条件");
        if (candidate.driver().getVersion() != input.expectedDriverVersion()) version();
        DispatchInvite invite =
                issue(order, batch, policy, candidate, "MANUAL", admin.actor(), input.reason());
        return new Receipt(invite.getId());
    }

    public Map<String, Object> revoke(long id, InviteRequests.Revoke input, String key) {
        return adminTransaction(
                "DISPATCH_INVITE_REVOKE",
                () -> {
                    DispatchInvite observed = mapper.findInvite(id);
                    if (observed == null) invisible();
                    ViOrder order = payments.lockOrder(observed.getOrderId());
                    department(order, LIST);
                    department(order, REVOKE);
                    payments.lockPayment(order.getId());
                    DispatchBatch batch = mapper.batch(order.getId());
                    Receipt result =
                            idem.execute(
                                    IdempotencyService.subjectKey(admin.actor()),
                                    "DISPATCH_INVITE_REVOKE:" + id,
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () -> revokeLocked(order, batch, mapper.invite(id), input));
                    return inviteView(mapper.invite(result.inviteId()), admin.actor());
                });
    }

    private Receipt revokeLocked(
            ViOrder order,
            DispatchBatch batch,
            DispatchInvite invite,
            InviteRequests.Revoke input) {
        if (invite.getVersion() != input.expectedVersion()
                || order.getVersion() != input.expectedOrderVersion()) version();
        if (!"WAIT_RESPONSE".equals(invite.getStatus()) || !invite.getExpiresAt().isAfter(now()))
            conflict("仅能撤回尚未到期的待响应邀约");
        finish(invite, "REVOKED", admin.actor(), input.reason());
        mapper.release(invite.getDriverId(), order.getId(), invite.getId());
        if (batch != null) batch(batch, "MANUAL", "ADMIN_REVOKED");
        if (order.getActiveAssignmentId() == null) transition(order, "MANUAL_DISPATCH", null);
        signal(order, "ADMIN_REVOKED");
        return new Receipt(invite.getId());
    }

    /** DB扫描是outbox的丢事件修补，不依赖Redis；总截止、已邀集合和cursor都保存在库中。 */
    public int runOnce() {
        int count = 0;
        for (Long id : mapper.dueInvites(now())) {
            expire(id);
            count++;
        }
        LinkedHashSet<Long> orders = new LinkedHashSet<>(mapper.pendingOrders());
        orders.addAll(mapper.activeBatches());
        for (long id : orders) {
            dispatch(id);
            count++;
        }
        return count;
    }

    public void dispatch(long orderId) {
        tx.executeWithoutResult(
                status -> {
                    lockDirectory();
                    ViOrder order = payments.lockOrder(orderId);
                    if (order != null) advance(order);
                });
    }

    public void execute(ViOutbox event) {
        if (!EVENTS.contains(event.getEventType()))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        var payload = ViJson.tree(event.getPayload());
        if ("DISPATCH_INVITE_EXPIRES".equals(event.getEventType())) {
            if (!"VI_DISPATCH_INVITE".equals(event.getAggregateType())
                    || payload.path("inviteId").asLong(-1) != event.getAggregateId()) invalid();
            DispatchInvite observed = mapper.findInvite(event.getAggregateId());
            if (observed == null) return;
            if (!("DISPATCH_INVITE_EXPIRES:" + observed.getId()).equals(event.getEventKey())
                    || payload.path("orderId").asLong(-1) != observed.getOrderId()
                    || !observed.getExpiresAt()
                            .toString()
                            .equals(payload.path("expiresAt").asText())) invalid();
            expire(event.getAggregateId());
            return;
        }
        if (!"VI_ORDER".equals(event.getAggregateType())
                || payload.path("orderId").asLong(-1) != event.getAggregateId()) invalid();
        tx.executeWithoutResult(
                status -> {
                    lockDirectory();
                    ViOrder order = payments.lockOrder(event.getAggregateId());
                    if (order == null) return;
                    if ("DISPATCH_REQUESTED".equals(event.getEventType())
                            && !("DISPATCH_REQUESTED:" + order.getId()).equals(event.getEventKey()))
                        invalid();
                    if ("DISPATCH_CONTINUE".equals(event.getEventType())) {
                        DispatchInvite source =
                                mapper.findInvite(payload.path("inviteId").asLong(-1));
                        if (source == null
                                || !Objects.equals(source.getOrderId(), order.getId())
                                || !("DISPATCH_CONTINUE:" + source.getId())
                                        .equals(event.getEventKey())) invalid();
                    }
                    if ("DISPATCH_BATCH_DEADLINE".equals(event.getEventType())) {
                        DispatchBatch batch = mapper.batch(order.getId());
                        if (batch == null
                                || !("DISPATCH_BATCH_DEADLINE:" + batch.getId())
                                        .equals(event.getEventKey())
                                || batch.getId() != payload.path("batchId").asLong(-1)
                                || !batch.getDeadlineAt()
                                        .toString()
                                        .equals(payload.path("deadlineAt").asText())) return;
                    }
                    advance(order);
                });
    }

    private void expire(long id) {
        tx.executeWithoutResult(
                status -> {
                    lockDirectory();
                    DispatchInvite observed = mapper.findInvite(id);
                    if (observed == null) return;
                    ViOrder order = payments.lockOrder(observed.getOrderId());
                    payments.lockPayment(order.getId());
                    DispatchBatch batch = mapper.batch(order.getId());
                    DispatchInvite row = mapper.invite(id);
                    if (!"WAIT_RESPONSE".equals(row.getStatus())
                            || row.getExpiresAt().isAfter(now())) return;
                    finish(row, "EXPIRED", Actor.system(), null);
                    mapper.release(row.getDriverId(), order.getId(), row.getId());
                    if (batch != null
                            && !Set.of("ASSIGNED", "CLOSED", "MANUAL").contains(batch.getStatus()))
                        advance(order);
                });
    }

    private void advance(ViOrder order) {
        if (!Set.of("PENDING_DISPATCH", "DISPATCHING").contains(order.getFulfillmentStatus()))
            return;
        payments.lockPayment(order.getId());
        DispatchBatch batch = mapper.batch(order.getId());
        String blocked = operational(order);
        if (blocked != null) {
            if ("CANCEL_ACTIVE".equals(blocked)) {
                if (batch != null) batch(batch, "PAUSED", blocked);
                return;
            }
            manualRequired(order, batch, blocked);
            return;
        }
        Map<String, Object> policy = policies.frozen(order);
        if (policy == null) {
            manualRequired(
                    order,
                    batch,
                    order.getServiceCompanyId() == null
                            ? "SERVICE_COMPANY_UNKNOWN"
                            : "FROZEN_POLICY_MISSING");
            return;
        }
        if (batch == null) batch = newBatch(order, policy, "AUTO");
        if (Set.of("ASSIGNED", "CLOSED", "MANUAL").contains(batch.getStatus())) return;
        if (!batch.getDeadlineAt().isAfter(now())) {
            manualRequired(order, batch, "OVERALL_DEADLINE");
            return;
        }
        DispatchInvite active = mapper.activeInvite(order.getId());
        if (active != null) {
            if (active.getExpiresAt().isAfter(now())) return;
            finish(active, "EXPIRED", Actor.system(), null);
            mapper.release(active.getDriverId(), order.getId(), active.getId());
        }
        String cursorKey = cursorKey(order.getServiceCompanyId());
        mapper.ensureCursor(cursorKey);
        Map<String, Object> cursor = mapper.cursor(cursorKey);
        long last = number(cursor, "last_driver_id", 0);
        List<Long> ids = new ArrayList<>(mapper.drivers(order.getServiceCompanyId()));
        ids.sort(
                Comparator.<Long>comparingInt(id -> id > last ? 0 : 1).thenComparingLong(id -> id));
        for (int stage = batch.getRadiusStage(); stage <= 1; stage++) {
            if (stage != batch.getRadiusStage()) {
                batch.setRadiusStage(stage);
                batch(batch, "RUNNING", null);
            }
            for (long id : ids) {
                if (mapper.alreadyInvited(batch.getId(), id) > 0) continue;
                var candidate = candidates.eligible(order, policy, id, radius(policy, stage), null);
                if (candidate == null) continue;
                issue(order, batch, policy, candidate, "AUTO", Actor.system(), null);
                if (mapper.advanceCursor(cursorKey, id, number(cursor, "version", 0)) != 1)
                    version();
                return;
            }
        }
        manualRequired(order, batch, "NO_ELIGIBLE_DRIVER");
    }

    private DispatchBatch newBatch(ViOrder order, Map<String, Object> policy, String source) {
        DispatchBatch batch = new DispatchBatch();
        batch.setOrderId(order.getId());
        batch.setRunNo(mapper.nextRun(order.getId()));
        batch.setServiceCompanyId(order.getServiceCompanyId());
        batch.setSource(source);
        batch.setStatus("RUNNING");
        batch.setPolicyVersion(order.getDispatchPolicyVersion());
        batch.setPolicySnapshot(order.getDispatchPolicySnapshot());
        batch.setStartedAt(now());
        Instant deadline =
                min(
                        now().plusSeconds(number(policy, "overallDeadlineSeconds", 0)),
                        order.getPickupStartsAt());
        if (!deadline.isAfter(now())) conflict("接车时段已到，不能建立新邀约");
        batch.setDeadlineAt(deadline);
        mapper.insertBatch(batch);
        outbox.publishAt(
                "DISPATCH_BATCH_DEADLINE:" + batch.getId(),
                "VI_ORDER",
                order.getId(),
                "DISPATCH_BATCH_DEADLINE",
                Map.of(
                        "orderId",
                        order.getId(),
                        "batchId",
                        batch.getId(),
                        "deadlineAt",
                        deadline.toString()),
                deadline);
        return batch;
    }

    private DispatchInvite issue(
            ViOrder order,
            DispatchBatch batch,
            Map<String, Object> policy,
            DispatchCandidateAuthority.Candidate candidate,
            String source,
            Actor actor,
            String reason) {
        DispatchInvite invite = new DispatchInvite();
        DriverProfile driver = candidate.driver();
        invite.setOrderId(order.getId());
        invite.setDriverId(driver.getId());
        invite.setSource(source);
        invite.setStatus("WAIT_RESPONSE");
        invite.setRoundKey("BATCH:" + batch.getId());
        invite.setBatchId(batch.getId());
        invite.setServiceCompanyId(order.getServiceCompanyId());
        invite.setCompanyRevision(driver.getTrainingContextRevision());
        invite.setDistanceMeter(candidate.distanceMeter());
        invite.setDriverSessionId(candidate.location().getSessionId());
        invite.setDriverContextVersion(candidate.location().getContextVersion());
        invite.setDriverLocationVersion(candidate.location().getVersion());
        invite.setDriverPresenceVersion(candidate.presence().getVersion());
        invite.setIssuedByUserId(actor.isAdmin() ? actor.sysUserId() : null);
        invite.setIssuedAt(now());
        invite.setExpiresAt(
                min(
                        now().plusSeconds(number(policy, "inviteResponseSeconds", 0)),
                        batch.getDeadlineAt(),
                        driver.getListeningUntil()));
        if (!invite.getExpiresAt().isAfter(now())) conflict("候选听单期限已到");
        mapper.insertInvite(invite);
        if (reason != null) {
            byte[] sealed = reasonEnvelope(invite.getId(), "DISPATCH_INVITE_ISSUED", actor, reason);
            if (mapper.issuedReason(invite.getId(), sealed) != 1) version();
            invite.setReasonCipher(sealed);
        }
        mapper.occupy(invite);
        batch(batch, "WAIT_RESPONSE", null);
        if (!"DISPATCHING".equals(order.getFulfillmentStatus()))
            transition(order, "DISPATCHING", null);
        recordInvite(invite, "DISPATCH_INVITE_ISSUED", actor, reason);
        outbox.publishAt(
                "DISPATCH_INVITE_EXPIRES:" + invite.getId(),
                "VI_DISPATCH_INVITE",
                invite.getId(),
                "DISPATCH_INVITE_EXPIRES",
                Map.of(
                        "inviteId",
                        invite.getId(),
                        "orderId",
                        order.getId(),
                        "expiresAt",
                        invite.getExpiresAt().toString()),
                invite.getExpiresAt());
        return invite;
    }

    private void manualRequired(ViOrder order, DispatchBatch batch, String reason) {
        DispatchInvite pending = mapper.activeInvite(order.getId());
        if (pending != null) {
            finish(pending, "REVOKED", Actor.system(), null);
            mapper.release(pending.getDriverId(), order.getId(), pending.getId());
        }
        if (batch != null && !Set.of("CLOSED", "ASSIGNED").contains(batch.getStatus()))
            batch(batch, "MANUAL", reason);
        if (order.getActiveAssignmentId() == null
                && !"MANUAL_DISPATCH".equals(order.getFulfillmentStatus()))
            transition(order, "MANUAL_DISPATCH", null);
        signal(order, reason);
    }

    private String operational(ViOrder order) {
        if (!"NONE".equals(order.getCancelStatus())) return "CANCEL_ACTIVE";
        if (order.getActiveAssignmentId() != null) return "ASSIGNMENT_EXISTS";
        if (!Set.of("PENDING_DISPATCH", "DISPATCHING", "MANUAL_DISPATCH")
                .contains(order.getFulfillmentStatus())) return "ORDER_NOT_DISPATCHABLE";
        if (!order.getPickupStartsAt().isAfter(now())) return "PICKUP_TIME_REACHED";
        if (order.getServiceCompanyId() == null) return "SERVICE_COMPANY_UNKNOWN";
        Map<String, Object> company = drivers.company(order.getServiceCompanyId());
        if (company == null
                || !"0".equals(company.get("status"))
                || !"0".equals(company.get("del_flag"))) return "SERVICE_COMPANY_UNAVAILABLE";
        var payment = payments.lockPayment(order.getId());
        if (payment == null
                || !"SUCCESS".equals(payment.getStatus())
                || payment.getPaidFen() != order.getTotalFen()
                || "MANUAL_CONFLICT".equals(payment.getCapacityResolution())
                || "RELEASED".equals(payment.getCapacityResolution())
                || conditions.pendingRefund(order.getId()) != 0) return "FUNDING_NOT_READY";
        if (conditions.stationOperational(order.getStationId()) != 1
                || conditions.confirmedReservations(order.getId()) != 2)
            return "CAPACITY_OR_STATION_UNAVAILABLE";
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void closeForCancellation(long orderId) {
        // 调用者已经目录→原订单→payment→售后锁；不重新创建权限主体，且不释放任何ASSIGNMENT占用。
        DispatchBatch batch = mapper.batch(orderId);
        DispatchInvite row = mapper.activeInvite(orderId);
        if (row != null) {
            finish(row, "REVOKED", actors.currentActor(), null);
            mapper.release(row.getDriverId(), orderId, row.getId());
        }
        if (batch != null && !"CLOSED".equals(batch.getStatus()))
            batch(batch, "CLOSED", "ORDER_CANCELLED");
    }

    private void finish(DispatchInvite row, String status, Actor actor, String reason) {
        long version = row.getVersion();
        row.setStatus(status);
        row.setRespondedAt(now());
        String action = "DISPATCH_INVITE_" + status;
        if (reason != null) row.setReasonCipher(reasonEnvelope(row.getId(), action, actor, reason));
        if (mapper.finishInvite(row, version) != 1) version();
        row.setVersion(version + 1);
        recordInvite(row, action, actor, reason);
    }

    private void recordInvite(DispatchInvite row, String action, Actor actor, String reason) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("inviteId", row.getId().toString());
        summary.put("orderId", row.getOrderId().toString());
        summary.put("driverId", row.getDriverId().toString());
        summary.put("status", row.getStatus());
        if (reason != null)
            summary.put(
                    "reasonEnvelope",
                    Base64.getEncoder()
                            .encodeToString(reasonEnvelope(row.getId(), action, actor, reason)));
        audit.recordSuccess(
                actor,
                "VI_DISPATCH_INVITE",
                row.getId(),
                action,
                null,
                summary,
                reason == null ? null : "操作原因已加密留存",
                row.getOrderId());
        outbox.publish(
                action + ":" + row.getId(),
                "VI_DISPATCH_INVITE",
                row.getId(),
                action,
                Map.of(
                        "inviteId",
                        row.getId().toString(),
                        "orderId",
                        row.getOrderId().toString(),
                        "driverId",
                        row.getDriverId().toString()));
    }

    private byte[] reasonEnvelope(long id, String action, Actor actor, String reason) {
        Long principal =
                actor.isAdmin() ? actor.sysUserId() : actor.isMini() ? actor.accountId() : null;
        return crypto.envelope(
                reason,
                "D02:INVITE:"
                        + id
                        + ":"
                        + action
                        + ":"
                        + actor.realm()
                        + ":"
                        + (principal == null ? "SYSTEM" : principal));
    }

    private void continueEvent(ViOrder order, DispatchInvite row) {
        outbox.publish(
                "DISPATCH_CONTINUE:" + row.getId(),
                "VI_ORDER",
                order.getId(),
                "DISPATCH_CONTINUE",
                Map.of("orderId", order.getId(), "inviteId", row.getId()));
        // 同事务立即推进，不依赖后续消费者调度延迟；事件仍保留，重复执行按当前真实状态幂等。
        advance(order);
    }

    private void signal(ViOrder order, String reason) {
        String event = "DISPATCH_MANUAL_REQUIRED:" + order.getId() + ":" + order.getVersion();
        if (outbox.publish(
                event,
                "VI_ORDER",
                order.getId(),
                "DISPATCH_MANUAL_REQUIRED",
                Map.of("orderId", order.getId(), "reasonCode", reason)))
            audit.recordSuccess(
                    Actor.system(),
                    "VI_ORDER",
                    order.getId(),
                    "DISPATCH_MANUAL_REQUIRED",
                    null,
                    Map.of("reasonCode", reason),
                    "派单条件不满足，保留原订单等待协调",
                    order.getId());
    }

    private void transition(ViOrder order, String state, Long assignment) {
        if (mapper.transition(order, state, assignment) != 1) version();
        order.setFulfillmentStatus(state);
        order.setActiveAssignmentId(assignment);
        order.setVersion(order.getVersion() + 1);
    }

    private void batch(DispatchBatch row, String state, String reason) {
        if (Objects.equals(row.getStatus(), state) && Objects.equals(row.getReasonCode(), reason))
            return;
        long version = row.getVersion();
        row.setStatus(state);
        row.setReasonCode(reason);
        if (mapper.updateBatch(row, version) != 1) version();
        row.setVersion(version + 1);
    }

    private Map<String, Object> inviteView(DispatchInvite row, Actor actor) {
        ViOrder order = payments.lockOrder(row.getOrderId());
        DispatchBatch batch = row.getBatchId() == null ? null : mapper.batchById(row.getBatchId());
        Map<String, Object> meta = mapper.metadata(order.getId());
        DriverProfile driver = drivers.find(row.getDriverId());
        var vehicle = ViJson.tree(order.getVehicleSnapshot());
        String status =
                "WAIT_RESPONSE".equals(row.getStatus()) && !row.getExpiresAt().isAfter(now())
                        ? "EXPIRED"
                        : row.getStatus();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inviteId", row.getId().toString());
        result.put("orderNo", order.getOrderNo());
        result.put("driverId", row.getDriverId().toString());
        result.put(
                "driverName", driver == null ? "" : Objects.toString(driver.getDisplayName(), ""));
        result.put("assignmentId", external(mapper.assignment(row.getId())));
        result.put("batchId", external(row.getBatchId()));
        result.put("source", row.getSource());
        result.put("status", status);
        result.put("storedStatus", row.getStatus());
        result.put("version", row.getVersion());
        result.put("orderVersion", order.getVersion());
        result.put("serviceCompanyId", external(order.getServiceCompanyId()));
        result.put("serviceCompanyName", meta == null ? null : meta.get("service_company_name"));
        result.put("stationId", order.getStationId().toString());
        result.put(
                "stationName", meta == null ? "" : Objects.toString(meta.get("station_name"), ""));
        result.put("pickupArea", "上门接车");
        result.put("pickupSlotStartAt", time(order.getPickupStartsAt()));
        result.put("pickupSlotEndAt", time(order.getPickupEndsAt()));
        result.put("vehicleTypeCode", vehicle.path("vehicleTypeCode").asText());
        result.put("vehicleTypeName", vehicle.path("vehicleTypeLabel").asText());
        result.put("vehiclePlateMasked", null);
        result.put("distanceMeter", row.getDistanceMeter());
        result.put("distanceMode", "GCJ02_GREAT_CIRCLE");
        result.put("issuedAt", time(row.getIssuedAt()));
        result.put("expiresAt", time(row.getExpiresAt()));
        result.put("overallDeadlineAt", batch == null ? null : time(batch.getDeadlineAt()));
        result.put("respondedAt", time(row.getRespondedAt()));
        result.put("sessionId", actor.isMini() ? external(actor.sessionId()) : null);
        result.put("contextVersion", actor.isMini() ? actor.contextVersion() : null);
        List<String> actions = new ArrayList<>();
        String block = null;
        if ("WAIT_RESPONSE".equals(status)) {
            if (actor.isMini()) {
                block = acceptBlock(order, batch, row);
                if (block == null) actions.add("ACCEPT_INVITE");
                actions.add("REJECT_INVITE");
            } else if (actor.isAdmin() && can(order.getDeptId(), REVOKE))
                actions.add("REVOKE_INVITE");
        }
        if (actor.isMini() && "ACCEPTED".equals(status) && mapper.assignment(row.getId()) != null)
            actions.add("VIEW_ASSIGNMENT");
        result.put("acceptBlockReason", block);
        result.put("allowedActions", actions);
        return result;
    }

    private String acceptBlock(ViOrder order, DispatchBatch batch, DispatchInvite invite) {
        String reason = operational(order);
        if (reason != null) return reason;
        if (batch == null || !batch.getDeadlineAt().isAfter(now()))
            return "BATCH_EXPIRED_OR_UNKNOWN";
        Map<String, Object> policy = policies.frozen(order);
        if (policy == null) return "FROZEN_POLICY_MISSING";
        var candidate =
                candidates.eligible(
                        order,
                        policy,
                        invite.getDriverId(),
                        radius(policy, batch.getRadiusStage()),
                        invite.getId());
        return candidate == null
                        || !Objects.equals(
                                invite.getCompanyRevision(),
                                candidate.driver().getTrainingContextRevision())
                ? "DRIVER_ELIGIBILITY_CHANGED"
                : null;
    }

    private Map<String, Object> queueView(ViOrder order) {
        Map<String, Object> meta = mapper.metadata(order.getId());
        DispatchBatch batch = mapper.batch(order.getId());
        DispatchInvite active = mapper.activeInvite(order.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("orderVersion", order.getVersion());
        result.put("serviceCompanyId", external(order.getServiceCompanyId()));
        result.put("serviceCompanyName", meta == null ? null : meta.get("service_company_name"));
        result.put("stationId", order.getStationId().toString());
        result.put(
                "stationName", meta == null ? "" : Objects.toString(meta.get("station_name"), ""));
        result.put("fulfillmentStatus", order.getFulfillmentStatus());
        result.put("cancelStatus", order.getCancelStatus());
        result.put("pickupSlotStartAt", time(order.getPickupStartsAt()));
        result.put("pickupSlotEndAt", time(order.getPickupEndsAt()));
        result.put("pickupArea", "上门接车");
        result.put("batchId", batch == null ? null : external(batch.getId()));
        result.put("batchStatus", batch == null ? null : batch.getStatus());
        result.put("batchVersion", batch == null ? null : batch.getVersion());
        result.put("policyVersion", order.getDispatchPolicyVersion());
        result.put("overallDeadlineAt", batch == null ? null : time(batch.getDeadlineAt()));
        result.put("radiusStage", batch == null ? null : batch.getRadiusStage());
        result.put("activeInviteId", active == null ? null : external(active.getId()));
        result.put(
                "reasonCode",
                batch == null
                        ? (order.getServiceCompanyId() == null
                                ? "SERVICE_COMPANY_UNKNOWN"
                                : order.getDispatchPolicySnapshot() == null
                                        ? "FROZEN_POLICY_MISSING"
                                        : null)
                        : batch.getReasonCode());
        List<String> actions = new ArrayList<>(List.of("VIEW_DISPATCH_INVITES"));
        if (policies.frozen(order) != null
                && operational(order) == null
                && active == null
                && (!activeBatch(batch) || batch.getDeadlineAt().isAfter(now()))) {
            actions.add("VIEW_DISPATCH_CANDIDATES");
            if (can(order.getDeptId(), CREATE)) actions.add("CREATE_MANUAL_INVITE");
        }
        result.put("allowedActions", actions);
        return result;
    }

    private void cursorSort(List<DispatchCandidateAuthority.Candidate> rows, long company) {
        String key = cursorKey(company);
        Map<String, Object> cursor = mapper.cursor(key);
        long last = cursor == null ? 0 : number(cursor, "last_driver_id", 0);
        rows.sort(
                Comparator.<DispatchCandidateAuthority.Candidate>comparingInt(
                                c -> c.driver().getId() > last ? 0 : 1)
                        .thenComparing(c -> c.driver().getId()));
    }

    private ViOrder adminOrder(String no, String permission) {
        Long id = mapper.orderId(no);
        if (id == null) invisible();
        ViOrder order = payments.lockOrder(id);
        department(order, permission);
        return order;
    }

    private void department(ViOrder order, String permission) {
        Set<Long> scope = admin.scope(permission);
        if (order == null || scope != null && !scope.contains(order.getDeptId())) invisible();
    }

    private boolean can(long dept, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        Set<Long> scope = admin.scope(permission);
        return scope == null || scope.contains(dept);
    }

    private static void requireOwner(DispatchInvite invite, DriverProfile driver) {
        if (invite == null || !Objects.equals(invite.getDriverId(), driver.getId())) invisible();
    }

    private <T> T miniTransaction(String action, Supplier<T> work) {
        Actor actor = mini.actor();
        return guarded(
                actor,
                action,
                () -> {
                    files.guard(actor, "biz:driver:query");
                    return work.get();
                });
    }

    private <T> T adminTransaction(String action, Supplier<T> work) {
        Actor actor = admin.actor();
        return guarded(
                actor,
                action,
                () -> {
                    admin.lockAuthority();
                    return work.get();
                });
    }

    private <T> T guarded(Actor actor, String action, Supplier<T> work) {
        try {
            return tx.execute(
                    status -> {
                        lockDirectory();
                        return work.get();
                    });
        } catch (RuntimeException failure) {
            audit.recordFailure(
                    actor,
                    "VI_DISPATCH_INVITE",
                    0,
                    action + "_REJECTED",
                    null,
                    Map.of(
                            "code",
                            failure instanceof ViException v
                                    ? v.getErrorCode().name()
                                    : "VI_INTERNAL_ERROR"),
                    "调度访问或状态迁移未完成");
            throw failure;
        }
    }

    private void lockDirectory() {
        directory.ensureGuard();
        directory.lockGuard();
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    private static boolean activeBatch(DispatchBatch batch) {
        return batch != null
                && Set.of("RUNNING", "WAIT_RESPONSE", "PAUSED").contains(batch.getStatus());
    }

    private static int radius(Map<String, Object> policy, int stage) {
        return ((Number) policy.get(stage == 0 ? "radiusMeter" : "expandedRadiusMeter")).intValue();
    }

    private static String cursorKey(long company) {
        return "company:" + company;
    }

    private static long number(Map<String, Object> row, String key, long fallback) {
        return row.get(key) instanceof Number n ? n.longValue() : fallback;
    }

    private static Instant min(Instant... values) {
        return Arrays.stream(values).min(Comparator.naturalOrder()).orElseThrow();
    }

    private static String external(Long value) {
        return value == null ? null : value.toString();
    }

    private static String time(Instant value) {
        return value == null ? null : value.toString();
    }

    private static void version() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void conflict(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
