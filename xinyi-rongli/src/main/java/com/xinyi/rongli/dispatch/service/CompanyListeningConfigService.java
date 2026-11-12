package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.dispatch.dto.DispatchRequests;
import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.integration.service.*;
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
import java.util.function.Supplier;

/** 公司统一配置保存即生效；缺参数不补生产默认，放宽只影响后来真实动作，收紧必须永久写回原期限。 */
@Service
public class CompanyListeningConfigService {
    private static final String QUERY = "biz:dispatchlistening:query",
            EDIT = "biz:dispatchlistening:edit";
    private final IntegrationAuthority authority;
    private final IntegrationCrypto crypto;
    private final DispatchListeningMapper mapper;
    private final DriverMapper drivers;
    private final ListeningDeadlineService deadlines;
    private final DriverLocationProtection protection;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final Clock clock;
    private final TransactionTemplate tx;

    public CompanyListeningConfigService(
            IntegrationAuthority authority,
            IntegrationCrypto crypto,
            DispatchListeningMapper mapper,
            DriverMapper drivers,
            ListeningDeadlineService deadlines,
            DriverLocationProtection protection,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.authority = authority;
        this.crypto = crypto;
        this.mapper = mapper;
        this.drivers = drivers;
        this.deadlines = deadlines;
        this.protection = protection;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long version) {}

    public Map<String, Object> get(long owner) {
        return transaction(
                "COMPANY_LISTENING_CONFIG_QUERY",
                () -> {
                    company(owner, QUERY);
                    return view(owner, mapper.config(owner));
                });
    }

    public Map<String, Object> put(long owner, DispatchRequests.Config request, String key) {
        return transaction(
                "COMPANY_LISTENING_CONFIG_CHANGED",
                () -> {
                    company(owner, QUERY);
                    company(owner, EDIT);
                    idem.execute(
                            IdempotencyService.subjectKey(authority.actor()),
                            "COMPANY_LISTENING_CONFIG:" + owner,
                            key,
                            ViJson.write(request),
                            true,
                            Receipt.class,
                            () -> save(owner, request));
                    return view(owner, mapper.config(owner));
                });
    }

    private Receipt save(long owner, DispatchRequests.Config request) {
        ListeningCompanyConfig before = mapper.config(owner);
        long version = before == null ? 0 : before.getVersion();
        if (version != request.expectedVersion()) version();
        ListeningCompanyConfig row = new ListeningCompanyConfig();
        row.setOwnerDeptId(owner);
        row.setClosingTime(request.closingTime());
        row.setLocationMaxAgeSeconds(request.locationMaxAgeSeconds());
        row.setReachabilityMaxAgeSeconds(request.reachabilityMaxAgeSeconds());
        row.setMaxAccuracyMeter(request.maxAccuracyMeter());
        row.setLocationReportIntervalSeconds(request.locationReportIntervalSeconds());
        row.setUpdatedByUserId(authority.actor().sysUserId());
        row.setVersion(version + 1);
        if (before == null) mapper.insertConfig(row);
        else if (mapper.updateConfig(row, version) != 1) version();
        // 同一目录守卫内穷尽本公司真实档案。不能只收紧一批后让随后调宽配置复活遗漏的旧窗口。
        for (Long id : mapper.companyDrivers(owner)) tighten(owner, id, row);
        byte[] reason =
                crypto.envelope(
                        request.reason(),
                        "D01:COMPANY:"
                                + owner
                                + ":CONFIG:"
                                + row.getVersion()
                                + ":ADMIN:"
                                + authority.actor().sysUserId());
        Map<String, Object> summary =
                Map.of(
                        "ownerDeptId",
                        Long.toString(owner),
                        "version",
                        row.getVersion(),
                        "reasonEnvelope",
                        Base64.getEncoder().encodeToString(reason));
        audit.recordSuccess(
                authority.actor(),
                "VI_DRIVER_LISTENING_CONFIG",
                owner,
                "COMPANY_LISTENING_CONFIG_CHANGED",
                null,
                summary,
                "变更原因已加密留存");
        outbox.publish(
                "COMPANY_LISTENING_CONFIG_CHANGED:" + owner + ":" + row.getVersion(),
                "VI_DRIVER_LISTENING_CONFIG",
                owner,
                "COMPANY_LISTENING_CONFIG_CHANGED",
                Map.of("ownerDeptId", Long.toString(owner), "version", row.getVersion()));
        return new Receipt(row.getVersion());
    }

    private void tighten(long owner, long driver, ListeningCompanyConfig config) {
        DriverProfile profile = deadlines.lock(driver);
        if (profile == null || !Objects.equals(profile.getOwnerDeptId(), owner)) return;
        if (profile.getListeningEnabled()) {
            if (profile.getListeningUntil() == null
                    || !Objects.equals(
                            profile.getListeningCompanyRevision(),
                            profile.getTrainingContextRevision())) deadlines.closeIfDue(profile);
            else {
                Instant until =
                        ListeningDayPolicy.tightenedDeadline(
                                profile.getListeningUntil(),
                                ListeningDayPolicy.closingTime(config.getClosingTime()));
                if (until.isBefore(profile.getListeningUntil())) {
                    boolean enabled = until.isAfter(now());
                    if (mapper.saveListening(
                                    profile,
                                    enabled,
                                    until,
                                    profile.getListeningCompanyRevision(),
                                    config.getVersion())
                            != 1) version();
                    profile.setListeningEnabled(enabled);
                    profile.setListeningUntil(until);
                    profile.setListeningConfigVersion(config.getVersion());
                    profile.setListeningRevision(profile.getListeningRevision() + 1);
                    profile.setVersion(profile.getVersion() + 1);
                    record(
                            "VI_DRIVER_PROFILE",
                            driver,
                            "DRIVER_LISTENING_TIGHTENED",
                            profile.getListeningRevision());
                    if (enabled) deadlines.register(profile);
                } else deadlines.closeIfDue(profile);
            }
        }
        tightenLocation(profile, config);
        tightenPresence(profile, config);
    }

    private void tightenLocation(DriverProfile profile, ListeningCompanyConfig config) {
        DriverLocation before = mapper.location(profile.getId());
        if (before == null
                || !Objects.equals(before.getOwnerDeptId(), profile.getOwnerDeptId())
                || !Objects.equals(
                        before.getCompanyRevision(), profile.getTrainingContextRevision())
                || !protection.authenticated(before)) return;
        Instant until =
                earlier(before.getCapturedAt(), before.getReceivedAt())
                        .plusSeconds(config.getLocationMaxAgeSeconds());
        if (!until.isBefore(before.getValidUntil())) return;
        DriverLocation row = new DriverLocation();
        row.setDriverId(before.getDriverId());
        row.setOwnerDeptId(before.getOwnerDeptId());
        row.setCompanyRevision(before.getCompanyRevision());
        row.setSessionId(before.getSessionId());
        row.setContextVersion(before.getContextVersion());
        row.setCapturedAt(before.getCapturedAt());
        row.setReceivedAt(before.getReceivedAt());
        row.setAccuracyMeter(before.getAccuracyMeter());
        row.setValidUntil(until);
        row.setConfigVersion(config.getVersion());
        row.setVersion(before.getVersion() + 1);
        row.setLocationCipher(protection.rebind(before, row));
        if (mapper.updateLocation(row, before.getVersion()) != 1) version();
        record(
                "VI_DRIVER_LOCATION",
                profile.getId(),
                "DRIVER_LOCATION_VALIDITY_TIGHTENED",
                row.getVersion());
    }

    private void tightenPresence(DriverProfile profile, ListeningCompanyConfig config) {
        DriverPresence row = mapper.presence(profile.getId());
        if (row == null
                || row.getOwnerDeptId() != profile.getOwnerDeptId()
                || row.getCompanyRevision() != profile.getTrainingContextRevision()) return;
        Instant until =
                earlier(row.getObservedAt(), row.getReceivedAt())
                        .plusSeconds(config.getReachabilityMaxAgeSeconds());
        if (!until.isBefore(row.getValidUntil())) return;
        long version = row.getVersion();
        row.setValidUntil(until);
        row.setConfigVersion(config.getVersion());
        row.setVersion(version + 1);
        if (mapper.updatePresence(row, version) != 1) version();
        record(
                "VI_DRIVER_PRESENCE",
                profile.getId(),
                "DRIVER_PRESENCE_VALIDITY_TIGHTENED",
                row.getVersion());
    }

    private void company(long owner, String permission) {
        Set<Long> scope = authority.scope(permission);
        if (scope != null && !scope.contains(owner))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        var company = drivers.company(owner);
        if (company == null
                || !"0".equals(company.get("status"))
                || !"0".equals(company.get("del_flag")))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请选择真实有效的公司部门");
    }

    private Map<String, Object> view(long owner, ListeningCompanyConfig row) {
        var company = drivers.company(owner);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ownerDeptId", Long.toString(owner));
        result.put("companyName", Objects.toString(company.get("dept_name"), ""));
        result.put("timeZone", "Asia/Shanghai");
        result.put("configured", row != null);
        result.put("version", row == null ? 0L : row.getVersion());
        result.put("closingTime", row == null ? null : row.getClosingTime());
        result.put("locationMaxAgeSeconds", row == null ? null : row.getLocationMaxAgeSeconds());
        result.put(
                "reachabilityMaxAgeSeconds",
                row == null ? null : row.getReachabilityMaxAgeSeconds());
        result.put("maxAccuracyMeter", row == null ? null : row.getMaxAccuracyMeter());
        result.put(
                "locationReportIntervalSeconds",
                row == null ? null : row.getLocationReportIntervalSeconds());
        boolean edit;
        try {
            company(owner, EDIT);
            edit = true;
        } catch (ViException denied) {
            edit = false;
        }
        result.put("allowedActions", edit ? List.of("EDIT_LISTENING_CONFIG") : List.of());
        return result;
    }

    private void record(String aggregate, long driver, String action, long version) {
        var summary = Map.of("driverId", Long.toString(driver), "version", version);
        audit.recordSuccess(authority.actor(), aggregate, driver, action, null, summary, null);
        outbox.publish(action + ":" + driver + ":" + version, aggregate, driver, action, summary);
    }

    private <T> T transaction(String action, Supplier<T> work) {
        Actor actor = authority.actor();
        try {
            return tx.execute(
                    status -> {
                        authority.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException failure) {
            audit.recordFailure(
                    actor,
                    "VI_DRIVER_LISTENING_CONFIG",
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

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    private static Instant earlier(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }

    private static void version() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }
}
