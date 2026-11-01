package com.xinyi.web.controller.rongli.station;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.station.dto.GeoPoint;
import com.xinyi.rongli.station.dto.StationInput;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 站点请求体解析（C02）。白名单字段，拒绝未知；location/数组/版本严格解析；deptId 仅 ADMIN 端接受。 */
final class StationWebSupport {

    private static final Set<String> BASE_FIELDS = Set.of("name", "address", "location", "contactPhone", "enabled",
            "supportedVehicleTypeCodes", "businessHours", "serviceRadiusMeter", "qualificationFileIds");

    private StationWebSupport() {
    }

    static StationInput parse(Map<String, Object> body, boolean isUpdate, boolean allowDeptId) {
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String k : body.keySet()) {
            boolean ok = BASE_FIELDS.contains(k) || (isUpdate && k.equals("expectedVersion")) || (allowDeptId && k.equals("deptId"));
            if (!ok) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "未知字段: " + k);
            }
        }
        GeoPoint location = parseLocation(body.get("location"));
        Long expectedVersion = body.get("expectedVersion") instanceof Number n ? n.longValue() : null;
        Long deptId = allowDeptId && body.get("deptId") instanceof Number dn ? dn.longValue() : null;
        return new StationInput(
                str(body.get("name")), str(body.get("address")), location, str(body.get("contactPhone")),
                body.get("enabled") instanceof Boolean b ? b : null,
                strList(body.get("supportedVehicleTypeCodes")), str(body.get("businessHours")),
                body.get("serviceRadiusMeter") instanceof Number r ? r.intValue() : null,
                body.get("qualificationFileIds") == null ? null : strList(body.get("qualificationFileIds")),
                expectedVersion, deptId);
    }

    private static GeoPoint parseLocation(Object o) {
        if (!(o instanceof Map<?, ?> m)) {
            return null;
        }
        BigDecimal lng = decimal(m.get("lng"));
        BigDecimal lat = decimal(m.get("lat"));
        String cs = m.get("coordinateSystem") == null ? null : String.valueOf(m.get("coordinateSystem"));
        return new GeoPoint(lng, lat, cs);
    }

    private static BigDecimal decimal(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "经纬度须为数值");
        }
    }

    private static List<String> strList(Object o) {
        if (!(o instanceof List<?> raw)) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (Object e : raw) {
            out.add(e == null ? null : String.valueOf(e));
        }
        return out;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}