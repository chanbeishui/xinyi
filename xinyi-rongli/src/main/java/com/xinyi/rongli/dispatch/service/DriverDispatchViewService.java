package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.driver.service.DriverNewAssignmentPolicy;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.qualification.service.QualificationSnapshotAuthority;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** D01只给基础候选状态，订单半径/车型/实际邀约接受仍由D02重新核验；近期交互不是设备持续在线承诺。 */
@Service
public class DriverDispatchViewService {
    private final DispatchListeningMapper mapper;
    private final DriverMapper drivers;
    private final DriverNewAssignmentPolicy qualifications;
    private final QualificationSnapshotAuthority snapshots;
    private final DriverLocationProtection locations;
    private final Clock clock;

    public DriverDispatchViewService(
            DispatchListeningMapper mapper,
            DriverMapper drivers,
            DriverNewAssignmentPolicy qualifications,
            QualificationSnapshotAuthority snapshots,
            DriverLocationProtection locations,
            @Qualifier("viDeadlineClock") Clock clock) {
        this.mapper = mapper;
        this.drivers = drivers;
        this.qualifications = qualifications;
        this.snapshots = snapshots;
        this.locations = locations;
        this.clock = clock;
    }

    public String qualificationReason(DriverProfile row) {
        String reason = qualifications.qualificationBlock(row, now());
        if (reason == null
                && row.getEffectiveSnapshotId() != null
                && snapshots.authenticated(row.getEffectiveSnapshotId()) == null)
            return "QUALIFICATION_SNAPSHOT_UNTRUSTED";
        return reason;
    }

    public boolean companyActive(DriverProfile row) {
        if (row.getOwnerDeptId() == null) return false;
        var company = drivers.company(row.getOwnerDeptId());
        return company != null
                && "0".equals(company.get("status"))
                && "0".equals(company.get("del_flag"));
    }

    public Map<String, Object> view(Actor actor, DriverProfile row) {
        long completed = completedAssignmentCount(actor, row);
        Instant now = now();
        ListeningCompanyConfig config =
                row.getOwnerDeptId() == null ? null : mapper.config(row.getOwnerDeptId());
        boolean configured = config != null && companyActive(row);
        boolean listening =
                ListeningDayPolicy.isListening(
                        row.getListeningEnabled(),
                        row.getListeningUntil(),
                        row.getListeningCompanyRevision(),
                        row.getTrainingContextRevision(),
                        now);
        String qualification = qualificationReason(row);
        var occupation = mapper.occupation(row.getId());
        List<Long> penalties = mapper.penalties(row.getId(), now);
        DriverLocation location = mapper.location(row.getId());
        DriverPresence presence = mapper.presence(row.getId());
        String locationState = locationState(actor, row, location, config, now);
        String reachableState = presenceState(actor, row, presence, now);
        boolean fresh = Set.of("FRESH", "INACCURATE", "UNKNOWN_ACCURACY").contains(locationState);
        boolean accurate =
                fresh
                        && location.getAccuracyMeter() != null
                        && config != null
                        && location.getAccuracyMeter() <= config.getMaxAccuracyMeter();
        boolean reachable = "RECENT_CONTACT".equals(reachableState);
        String reason =
                reason(
                        row,
                        configured,
                        qualification,
                        listening,
                        occupation,
                        penalties,
                        reachableState,
                        locationState);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("driverId", row.getId().toString());
        result.put("completedAssignmentCount", completed);
        result.put("ownerDeptId", external(row.getOwnerDeptId()));
        result.put("companyName", row.getCompanyName());
        result.put("companyRevision", row.getTrainingContextRevision());
        result.put(
                "businessDate",
                now.atZone(ListeningDayPolicy.BUSINESS_ZONE).toLocalDate().toString());
        result.put("sessionId", actor.sessionId().toString());
        result.put("contextVersion", actor.contextVersion());
        result.put("newAssignmentEnabled", row.getNewAssignmentEnabled());
        result.put("listeningIntentEnabled", row.getListeningEnabled());
        result.put("listeningEnabled", listening);
        result.put("version", row.getListeningRevision());
        result.put("listeningVersion", row.getListeningRevision());
        result.put("profileVersion", row.getVersion());
        result.put("listeningUntil", time(row.getListeningUntil()));
        result.put("listeningCompanyRevision", row.getListeningCompanyRevision());
        result.put("listeningConfigVersion", row.getListeningConfigVersion());
        result.put("configVersion", config == null ? null : config.getVersion());
        result.put("configurationReady", configured);
        result.put("closingTime", config == null ? null : config.getClosingTime());
        result.put(
                "locationReportIntervalSeconds",
                config == null ? null : config.getLocationReportIntervalSeconds());
        result.put("qualificationValid", qualification == null);
        result.put("qualificationReason", qualification);
        result.put("occupationType", occupation == null ? "NONE" : occupation.get("holder_type"));
        result.put(
                "holderId",
                occupation == null ? null : Objects.toString(occupation.get("holder_id"), null));
        result.put("occupied", occupation != null);
        result.put("activePenaltyIds", penalties.stream().map(String::valueOf).toList());
        result.put("locationVersion", location == null ? 0L : location.getVersion());
        result.put("locationStatus", locationState);
        result.put("locationFresh", fresh);
        result.put("locationAccuracyValid", accurate);
        result.put("locationCapturedAt", location == null ? null : time(location.getCapturedAt()));
        result.put("locationReceivedAt", location == null ? null : time(location.getReceivedAt()));
        result.put("locationExpiresAt", location == null ? null : time(location.getValidUntil()));
        result.put("accuracyMeter", location == null ? null : location.getAccuracyMeter());
        result.put("presenceVersion", presence == null ? 0L : presence.getVersion());
        result.put("clientState", presence == null ? null : presence.getClientState());
        result.put("reachabilityStatus", reachableState);
        result.put("clientReachable", reachable);
        result.put("lastContactAt", presence == null ? null : time(presence.getObservedAt()));
        result.put(
                "reachabilityExpiresAt", presence == null ? null : time(presence.getValidUntil()));
        result.put("availableForNewOrder", "BASE_READY".equals(reason));
        result.put("eligibilityScope", "BASE_CANDIDATE_ONLY");
        result.put("reason", reason);
        List<String> actions = new ArrayList<>();
        if (row.getListeningEnabled()) actions.add("DISABLE_LISTENING");
        if (!listening
                && configured
                && qualification == null
                && row.getNewAssignmentEnabled()
                && beforeClosing(config, now)) actions.add("ENABLE_LISTENING");
        if (configured) {
            actions.add("REPORT_DRIVER_LOCATION");
            actions.add("REPORT_DRIVER_PRESENCE");
        }
        result.put("allowedActions", actions);
        return result;
    }

    private long completedAssignmentCount(Actor actor, DriverProfile row) {
        if (actor == null
                || !actor.isMini()
                || !"DRIVER".equals(actor.actingRole())
                || row == null
                || !Objects.equals(actor.accountId(), row.getAccountId()))
            throw new com.xinyi.rongli.platform.error.ViException(
                    com.xinyi.rongli.platform.error.ViErrorCode.VI_FORBIDDEN);
        // 本人历史按数据库真实driver绑定统计；允许本人跨原公司查看累计数量，不从客户端部门放宽范围。
        // 复用统一RETURNED且ended_at非空谓词，只返回计数，不泄露原订单或其他司机详情。
        Long completed = drivers.completedCount(row.getId(), null);
        if (completed == null)
            throw new com.xinyi.rongli.platform.error.ViException(
                    com.xinyi.rongli.platform.error.ViErrorCode.VI_AUTHORITY_UNAVAILABLE);
        return completed;
    }

    private String locationState(
            Actor actor,
            DriverProfile row,
            DriverLocation location,
            ListeningCompanyConfig config,
            Instant now) {
        if (location == null) return "MISSING";
        if (location.getOwnerDeptId() == null
                || location.getCompanyRevision() == null
                || location.getSessionId() == null
                || location.getContextVersion() == null) return "UNTRUSTED";
        if (!same(
                actor,
                row,
                location.getOwnerDeptId(),
                location.getCompanyRevision(),
                location.getSessionId(),
                location.getContextVersion())) return "CONTEXT_CHANGED";
        if (!locations.authenticated(location)) return "UNTRUSTED";
        if (!location.getValidUntil().isAfter(now)
                || location.getCapturedAt().isAfter(now.plusSeconds(5))) return "STALE";
        if (location.getAccuracyMeter() == null || config == null) return "UNKNOWN_ACCURACY";
        return location.getAccuracyMeter() > config.getMaxAccuracyMeter() ? "INACCURATE" : "FRESH";
    }

    private String presenceState(
            Actor actor, DriverProfile row, DriverPresence presence, Instant now) {
        if (presence == null) return "UNKNOWN";
        if (!same(
                actor,
                row,
                presence.getOwnerDeptId(),
                presence.getCompanyRevision(),
                presence.getSessionId(),
                presence.getContextVersion())) return "CONTEXT_CHANGED";
        return presence.getValidUntil().isAfter(now)
                        && !presence.getObservedAt().isAfter(now.plusSeconds(5))
                ? "RECENT_CONTACT"
                : "STALE";
    }

    private boolean same(
            Actor actor, DriverProfile row, long owner, long epoch, long session, long context) {
        if (!Objects.equals(row.getOwnerDeptId(), owner)
                || row.getTrainingContextRevision() != epoch
                || !Objects.equals(actor.sessionId(), session)
                || !Objects.equals(actor.contextVersion(), context)) return false;
        var actual = mapper.liveSession(session);
        return actual != null
                && number(actual, "account_id") == row.getAccountId()
                && "DRIVER".equals(actual.get("active_role"))
                && "ACTIVE".equals(actual.get("status"))
                && actual.get("revoked_at") == null
                && number(actual, "context_version") == context
                && number(actual, "session_epoch") == number(actual, "current_epoch")
                && "ACTIVE".equals(actual.get("security_status"))
                && "ACTIVE".equals(actual.get("account_status"));
    }

    private String reason(
            DriverProfile row,
            boolean config,
            String qualification,
            boolean listening,
            Map<String, Object> occupation,
            List<Long> penalties,
            String presence,
            String location) {
        if (row.getOwnerDeptId() == null) return "DRIVER_COMPANY_UNASSIGNED";
        if (!companyActive(row)) return "DRIVER_COMPANY_UNAVAILABLE";
        if (!config) return "LISTENING_CONFIG_NOT_READY";
        if (qualification != null) return qualification;
        if (!row.getNewAssignmentEnabled()) return "NEW_ASSIGNMENTS_DISABLED";
        if (!listening)
            return row.getListeningEnabled()
                    ? "LISTENING_EXPIRED_OR_CONTEXT_CHANGED"
                    : "LISTENING_DISABLED";
        if (occupation != null) return "OCCUPIED";
        if (!penalties.isEmpty()) return "ACTIVE_PENALTY";
        if (!"RECENT_CONTACT".equals(presence)) return "CLIENT_" + presence;
        if (!"FRESH".equals(location)) return "LOCATION_" + location;
        return "BASE_READY";
    }

    private static boolean beforeClosing(ListeningCompanyConfig config, Instant now) {
        try {
            ListeningDayPolicy.deadlineForOpening(
                    now, ListeningDayPolicy.closingTime(config.getClosingTime()));
            return true;
        } catch (com.xinyi.rongli.platform.error.ViException failure) {
            return false;
        }
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    private static long number(Map<String, Object> row, String key) {
        return row.get(key) instanceof Number value ? value.longValue() : Long.MIN_VALUE;
    }

    private static String external(Long value) {
        return value == null ? null : value.toString();
    }

    private static String time(Instant value) {
        return value == null ? null : value.toString();
    }
}
