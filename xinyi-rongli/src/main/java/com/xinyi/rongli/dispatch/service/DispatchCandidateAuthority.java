package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.address.service.AddressCrypto;
import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.dispatch.mapper.*;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.platform.json.ViJson;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

/** 候选是数据库真实来源事实，不伪装MINI操作人。点与近期交互必须属于同一仍有效DRIVER设备上下文。 */
@Service
public class DispatchCandidateAuthority {
    private final DriverMapper drivers;
    private final DispatchInviteMapper dispatch;
    private final DispatchListeningMapper listening;
    private final DriverDispatchViewService base;
    private final DriverLocationProtection protection;
    private final OrderMapper orders;
    private final AddressCrypto addresses;
    private final PhoneCrypto phones;
    private final Clock clock;

    public DispatchCandidateAuthority(
            DriverMapper drivers,
            DispatchInviteMapper dispatch,
            DispatchListeningMapper listening,
            DriverDispatchViewService base,
            DriverLocationProtection protection,
            OrderMapper orders,
            AddressCrypto addresses,
            PhoneCrypto phones,
            @Qualifier("viDeadlineClock") Clock clock) {
        this.drivers = drivers;
        this.dispatch = dispatch;
        this.listening = listening;
        this.base = base;
        this.protection = protection;
        this.orders = orders;
        this.addresses = addresses;
        this.phones = phones;
        this.clock = clock;
    }

    public record Candidate(
            DriverProfile driver,
            DriverLocation location,
            DriverPresence presence,
            int distanceMeter,
            Instant expiresAt) {}

    public Candidate eligible(
            ViOrder order, Map<String, Object> policy, long driverId, int radius, Long ownInvite) {
        DriverProfile row = drivers.lock(driverId);
        Instant now = clock.instant();
        if (row == null
                || order.getServiceCompanyId() == null
                || !Objects.equals(row.getOwnerDeptId(), order.getServiceCompanyId())
                || !base.companyActive(row)
                || base.qualificationReason(row) != null
                || !row.getNewAssignmentEnabled()
                || !ListeningDayPolicy.isListening(
                        row.getListeningEnabled(),
                        row.getListeningUntil(),
                        row.getListeningCompanyRevision(),
                        row.getTrainingContextRevision(),
                        now)) return null;
        ListeningCompanyConfig config = listening.config(row.getOwnerDeptId());
        if (config == null
                || !compatible(policy, order, row)
                || !listening.penalties(driverId, now).isEmpty()) return null;
        // 普通候选已由occupation的三事实UNION保护。接受本邀约时额外独立检查，避免排除ownInvite遮住异常并存责任。
        if (ownInvite != null && dispatch.otherResponsibilities(driverId, ownInvite) != 0)
            return null;
        Map<String, Object> occupation = listening.occupation(driverId);
        if (occupation != null
                && !(ownInvite != null
                        && "INVITE".equals(occupation.get("holder_type"))
                        && number(occupation, "holder_id") == ownInvite)) return null;
        DriverLocation location = listening.location(driverId);
        DriverPresence presence = listening.presence(driverId);
        if (location == null
                || presence == null
                || !Objects.equals(location.getOwnerDeptId(), row.getOwnerDeptId())
                || !Objects.equals(presence.getOwnerDeptId(), row.getOwnerDeptId())
                || !Objects.equals(location.getCompanyRevision(), row.getTrainingContextRevision())
                || !Objects.equals(presence.getCompanyRevision(), row.getTrainingContextRevision())
                || location.getSessionId() == null
                || !Objects.equals(location.getSessionId(), presence.getSessionId())
                || location.getContextVersion() == null
                || !Objects.equals(location.getContextVersion(), presence.getContextVersion())
                || location.getValidUntil() == null
                || presence.getValidUntil() == null
                || !location.getValidUntil().isAfter(now)
                || !presence.getValidUntil().isAfter(now)
                || location.getCapturedAt().isAfter(now.plusSeconds(5))
                || presence.getObservedAt().isAfter(now.plusSeconds(5))
                || location.getAccuracyMeter() == null
                || location.getAccuracyMeter() > config.getMaxAccuracyMeter()
                || !protection.authenticated(location)) return null;
        Map<String, Object> session = dispatch.lockLiveSession(location.getSessionId());
        if (session == null
                || number(session, "account_id") != row.getAccountId()
                || !"ACTIVE".equals(session.get("status"))
                || session.get("revoked_at") != null
                || !"DRIVER".equals(session.get("active_role"))
                || number(session, "context_version") != location.getContextVersion()
                || number(session, "session_epoch") != number(session, "current_epoch")
                || !"ACTIVE".equals(session.get("account_status"))
                || !"ACTIVE".equals(session.get("security_status"))) return null;
        Instant sessionUntil = instant(session.get("absolute_expires_at"));
        if (sessionUntil == null || !sessionUntil.isAfter(now)) return null;
        Map<String, Object> point = protection.coordinates(location);
        var snapshot = orders.snapshot(order.getId());
        if (snapshot == null || snapshot.getAddressCipher() == null) return null;
        var address = ViJson.tree(addresses.decrypt(snapshot.getAddressCipher())).path("location");
        if (!"GCJ02".equals(address.path("coordinateSystem").asText())
                || !address.path("lng").isNumber()
                || !address.path("lat").isNumber()) return null;
        double actualDistance =
                distance(
                        ((Number) point.get("lng")).doubleValue(),
                        ((Number) point.get("lat")).doubleValue(),
                        address.path("lng").asDouble(),
                        address.path("lat").asDouble());
        // 先用未取整距离判圈，不能让圈外不足半米的点因展示四舍五入而进入候选。
        if (!Double.isFinite(actualDistance) || actualDistance > radius) return null;
        int distance = (int) Math.round(actualDistance);
        Instant expires =
                min(
                        row.getReviewDueAt(),
                        row.getListeningUntil(),
                        location.getValidUntil(),
                        presence.getValidUntil(),
                        sessionUntil,
                        order.getPickupStartsAt());
        if (!row.getLicenseLongTerm())
            expires =
                    min(
                            expires,
                            row.getLicenseValidTo()
                                    .atZone(ListeningDayPolicy.BUSINESS_ZONE)
                                    .toLocalDate()
                                    .plusDays(1)
                                    .atStartOfDay(ListeningDayPolicy.BUSINESS_ZONE)
                                    .toInstant());
        return expires.isAfter(now)
                ? new Candidate(row, location, presence, distance, expires)
                : null;
    }

    private boolean compatible(Map<String, Object> policy, ViOrder order, DriverProfile row) {
        String code = ViJson.tree(order.getVehicleSnapshot()).path("vehicleTypeCode").asText();
        for (Object item : (List<?>) policy.get("licenseCompatibility")) {
            Map<?, ?> entry = (Map<?, ?>) item;
            if (code.equals(entry.get("vehicleTypeCode"))
                    && ((List<?>) entry.get("licenseClasses")).contains(row.getLicenseClass()))
                return true;
        }
        return false;
    }

    public Map<String, Object> view(Candidate candidate) {
        DriverProfile row = candidate.driver();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("driverId", row.getId().toString());
        result.put("driverName", Objects.toString(row.getDisplayName(), ""));
        String phone = row.getPhoneCipher() == null ? null : phones.decrypt(row.getPhoneCipher());
        result.put(
                "phoneMask",
                phone == null
                        ? null
                        : phone.length() < 7
                                ? "***"
                                : phone.substring(0, 3)
                                        + "****"
                                        + phone.substring(phone.length() - 4));
        result.put("driverVersion", row.getVersion());
        result.put("serviceCompanyId", row.getOwnerDeptId().toString());
        result.put("licenseClass", row.getLicenseClass());
        result.put("distanceMeter", candidate.distanceMeter());
        result.put("distanceMode", "GCJ02_GREAT_CIRCLE");
        result.put("eligibilityExpiresAt", candidate.expiresAt().toString());
        result.put("locationCapturedAt", candidate.location().getCapturedAt().toString());
        result.put("locationExpiresAt", candidate.location().getValidUntil().toString());
        result.put("reachabilityExpiresAt", candidate.presence().getValidUntil().toString());
        result.put("listeningUntil", row.getListeningUntil().toString());
        result.put("reviewDueAt", row.getReviewDueAt().toString());
        result.put("allowedActions", List.of("CREATE_MANUAL_INVITE"));
        return result;
    }

    private static double distance(double lng1, double lat1, double lng2, double lat2) {
        double p1 = Math.toRadians(lat1),
                p2 = Math.toRadians(lat2),
                dp = p2 - p1,
                dl = Math.toRadians(lng2 - lng1);
        double a =
                Math.pow(Math.sin(dp / 2), 2)
                        + Math.cos(p1) * Math.cos(p2) * Math.pow(Math.sin(dl / 2), 2);
        return 6371008.8 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(Math.max(0, 1 - a)));
    }

    private static long number(Map<String, Object> row, String key) {
        return row.get(key) instanceof Number n ? n.longValue() : Long.MIN_VALUE;
    }

    private static Instant instant(Object o) {
        if (o instanceof java.sql.Timestamp value) return value.toInstant();
        if (o instanceof LocalDateTime value) return value.toInstant(ZoneOffset.UTC);
        return o instanceof Instant value ? value : null;
    }

    private static Instant min(Instant... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElseThrow();
    }
}
