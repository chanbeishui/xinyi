package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.dispatch.dto.DispatchRequests;
import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

/** 本人意愿、近期交互和最近位置分别写入；任何上报或前后台变化都不能代替本人开启听单。 */
@Service
public class DriverDispatchService {
    private final DispatchAuthority authority;
    private final DispatchListeningMapper mapper;
    private final DriverDispatchViewService views;
    private final DriverLocationProtection protection;
    private final ListeningDeadlineService deadlines;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final Clock clock;
    private final TransactionTemplate tx;

    public DriverDispatchService(
            DispatchAuthority authority,
            DispatchListeningMapper mapper,
            DriverDispatchViewService views,
            DriverLocationProtection protection,
            ListeningDeadlineService deadlines,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.authority = authority;
        this.mapper = mapper;
        this.views = views;
        this.protection = protection;
        this.deadlines = deadlines;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long version) {}

    public Map<String, Object> availability() {
        return transaction(
                "DRIVER_AVAILABILITY_QUERY", actor -> views.view(actor, authority.lock(actor)));
    }

    public Map<String, Object> listening(DispatchRequests.Listening request, String key) {
        return transaction(
                "DRIVER_LISTENING_CHANGED",
                actor -> {
                    DriverProfile row = authority.lock(actor);
                    authority.source(actor, row, request.source());
                    idem.execute(
                            IdempotencyService.subjectKey(actor),
                            "DRIVER_LISTENING:" + row.getId(),
                            key,
                            ViJson.write(DispatchRequests.tree(request)),
                            true,
                            Receipt.class,
                            () -> changeListening(actor, row, request));
                    return views.view(actor, mapper.lockProfile(actor.accountId()));
                });
    }

    private Receipt changeListening(
            Actor actor, DriverProfile row, DispatchRequests.Listening request) {
        if (row.getListeningRevision() != request.expectedVersion()) version();
        Instant now = now();
        if (request.enabled()) {
            if (!now.atZone(ListeningDayPolicy.BUSINESS_ZONE)
                    .toLocalDate()
                    .toString()
                    .equals(request.expectedBusinessDate())) state("开启意图已跨日，请读取今天状态后由本人重新开启");
            ListeningCompanyConfig config = config(row);
            if (!Objects.equals(request.expectedConfigVersion(), config.getVersion())) version();
            if (views.qualificationReason(row) != null || !row.getNewAssignmentEnabled())
                state("资格或行政许可当前不允许开启听单");
            if (ListeningDayPolicy.isListening(
                    row.getListeningEnabled(),
                    row.getListeningUntil(),
                    row.getListeningCompanyRevision(),
                    row.getTrainingContextRevision(),
                    now)) return new Receipt(row.getListeningRevision());
            Instant until =
                    ListeningDayPolicy.deadlineForOpening(
                            now, ListeningDayPolicy.closingTime(config.getClosingTime()));
            saveListening(row, true, until, row.getTrainingContextRevision(), config.getVersion());
            deadlines.register(row);
        } else {
            // 安全关闭不依赖资格、配置或占用。只结束本人意愿，不变更邀约、承接和车辆责任。
            if (!row.getListeningEnabled()) return new Receipt(row.getListeningRevision());
            saveListening(
                    row,
                    false,
                    row.getListeningUntil(),
                    row.getListeningCompanyRevision(),
                    row.getListeningConfigVersion());
        }
        record(
                actor,
                "VI_DRIVER_PROFILE",
                row.getId(),
                "DRIVER_LISTENING_CHANGED",
                row.getListeningRevision());
        return new Receipt(row.getListeningRevision());
    }

    private void saveListening(
            DriverProfile row, boolean enabled, Instant until, Long epoch, Long config) {
        if (mapper.saveListening(row, enabled, until, epoch, config) != 1) version();
        row.setListeningEnabled(enabled);
        row.setListeningUntil(until);
        row.setListeningCompanyRevision(epoch);
        row.setListeningConfigVersion(config);
        row.setListeningRevision(row.getListeningRevision() + 1);
        row.setVersion(row.getVersion() + 1);
    }

    public Map<String, Object> location(DispatchRequests.Position request, String key) {
        return transaction(
                "DRIVER_LOCATION_REPORTED",
                actor -> {
                    DriverProfile profile = authority.lock(actor);
                    authority.source(actor, profile, request.source());
                    ListeningCompanyConfig config = config(profile);
                    Receipt receipt =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "DRIVER_LOCATION:" + profile.getId(),
                                    key,
                                    ViJson.write(DispatchRequests.tree(request)),
                                    true,
                                    Receipt.class,
                                    () -> saveLocation(actor, profile, config, request));
                    return ack(key, receipt.version());
                });
    }

    private Receipt saveLocation(
            Actor actor,
            DriverProfile profile,
            ListeningCompanyConfig config,
            DispatchRequests.Position request) {
        Instant now = now();
        validateCaptured(request.capturedAt(), now);
        DriverLocation previous = mapper.location(profile.getId());
        long oldVersion = previous == null ? 0 : previous.getVersion();
        if (oldVersion != request.expectedVersion()) version();
        if (previous != null && !request.capturedAt().isAfter(previous.getCapturedAt()))
            state("定位时刻已过期或重复，不能倒退最近点");
        DriverLocation row = new DriverLocation();
        row.setDriverId(profile.getId());
        row.setOwnerDeptId(profile.getOwnerDeptId());
        row.setCompanyRevision(profile.getTrainingContextRevision());
        row.setSessionId(actor.sessionId());
        row.setContextVersion(actor.contextVersion());
        row.setCapturedAt(request.capturedAt());
        row.setReceivedAt(now);
        row.setAccuracyMeter(request.accuracyMeter());
        row.setConfigVersion(config.getVersion());
        row.setValidUntil(
                earlier(request.capturedAt(), now).plusSeconds(config.getLocationMaxAgeSeconds()));
        row.setVersion(oldVersion + 1);
        row.setLocationCipher(protection.protect(row, request.lng(), request.lat()));
        if (previous == null) mapper.insertLocation(row);
        else if (mapper.updateLocation(row, oldVersion) != 1) version();
        record(
                actor,
                "VI_DRIVER_LOCATION",
                profile.getId(),
                "DRIVER_LOCATION_REPORTED",
                row.getVersion());
        return new Receipt(row.getVersion());
    }

    public Map<String, Object> presence(DispatchRequests.Presence request, String key) {
        return transaction(
                "DRIVER_PRESENCE_REPORTED",
                actor -> {
                    DriverProfile profile = authority.lock(actor);
                    authority.source(actor, profile, request.source());
                    ListeningCompanyConfig config = config(profile);
                    Receipt receipt =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "DRIVER_PRESENCE:" + profile.getId(),
                                    key,
                                    ViJson.write(DispatchRequests.tree(request)),
                                    true,
                                    Receipt.class,
                                    () -> savePresence(actor, profile, config, request));
                    return ack(key, receipt.version());
                });
    }

    private Receipt savePresence(
            Actor actor,
            DriverProfile profile,
            ListeningCompanyConfig config,
            DispatchRequests.Presence request) {
        Instant now = now();
        validateCaptured(request.observedAt(), now);
        DriverPresence previous = mapper.presence(profile.getId());
        long oldVersion = previous == null ? 0 : previous.getVersion();
        if (oldVersion != request.expectedVersion()) version();
        if (previous != null && !request.observedAt().isAfter(previous.getObservedAt()))
            state("交互时刻已过期或重复，不能延长近期交互期限");
        DriverPresence row = new DriverPresence();
        row.setDriverId(profile.getId());
        row.setOwnerDeptId(profile.getOwnerDeptId());
        row.setCompanyRevision(profile.getTrainingContextRevision());
        row.setSessionId(actor.sessionId());
        row.setContextVersion(actor.contextVersion());
        row.setClientState(request.clientState());
        row.setObservedAt(request.observedAt());
        row.setReceivedAt(now);
        row.setValidUntil(
                earlier(request.observedAt(), now)
                        .plusSeconds(config.getReachabilityMaxAgeSeconds()));
        row.setConfigVersion(config.getVersion());
        row.setVersion(oldVersion + 1);
        if (previous == null) mapper.insertPresence(row);
        else if (mapper.updatePresence(row, oldVersion) != 1) version();
        // BACKGROUND只记录最后交互，不关闭意愿；租约超时才独立变为未知可达。
        record(
                actor,
                "VI_DRIVER_PRESENCE",
                profile.getId(),
                "DRIVER_PRESENCE_REPORTED",
                row.getVersion());
        return new Receipt(row.getVersion());
    }

    private ListeningCompanyConfig config(DriverProfile row) {
        if (row.getOwnerDeptId() == null || !views.companyActive(row))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请先明确有效归属公司");
        ListeningCompanyConfig config = mapper.config(row.getOwnerDeptId());
        if (config == null)
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "公司尚未配置下班时间及位置/交互有效期");
        return config;
    }

    private <T> T transaction(String action, Function<Actor, T> work) {
        Actor actor = authority.actor();
        try {
            return tx.execute(status -> work.apply(actor));
        } catch (RuntimeException failure) {
            audit.recordFailure(
                    actor,
                    "VI_DRIVER_PROFILE",
                    0,
                    action + "_DENIED",
                    null,
                    null,
                    failure instanceof ViException error
                            ? error.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw failure;
        }
    }

    private void record(Actor actor, String aggregate, long id, String action, long version) {
        Map<String, Object> summary = Map.of("driverId", Long.toString(id), "version", version);
        // 坐标、位置密文和设备详细输入不进入审计/outbox/幂等结果。
        audit.recordSuccess(actor, aggregate, id, action, null, summary, null);
        outbox.publish(action + ":" + id + ":" + version, aggregate, id, action, summary);
    }

    private static Map<String, Object> ack(String key, long version) {
        return Map.of("accepted", true, "requestId", key, "version", version);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    private static Instant earlier(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }

    private static void validateCaptured(Instant captured, Instant now) {
        if (captured.isAfter(now.plusSeconds(5)) || captured.isBefore(now.minusSeconds(86400)))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "设备时刻偏差超出允许技术窗口");
    }

    private static void version() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void state(String text) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, text);
    }
}
