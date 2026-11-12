package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.domain.DriverLocation;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/** 最近点只加密保存；来源、时间及有效期认证绑定，不能通过跨会话复制密文或改到期列制造新鲜位置。 */
@Service
public class DriverLocationProtection {
    private final IntegrationCrypto crypto;

    public DriverLocationProtection(IntegrationCrypto crypto) {
        this.crypto = crypto;
    }

    public byte[] protect(DriverLocation row, BigDecimal lng, BigDecimal lat) {
        return crypto.envelope(
                ViJson.write(Map.of("lng", lng, "lat", lat, "coordinateSystem", "GCJ02")),
                aad(row));
    }

    public boolean authenticated(DriverLocation row) {
        if (!bound(row)) return false;
        try {
            return coordinates(row) != null;
        } catch (ViException failure) {
            if (failure.getErrorCode() == ViErrorCode.VI_SECRET_UNAVAILABLE) throw failure;
            return false;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    /** 配置收紧必须重新认证封装，但保留采集/接收时刻；后续再调宽也不能复活旧点有效期。 */
    public byte[] rebind(DriverLocation before, DriverLocation tightened) {
        Map<String, Object> point = coordinates(before);
        return protect(
                tightened,
                new BigDecimal(point.get("lng").toString()),
                new BigDecimal(point.get("lat").toString()));
    }

    private Map<String, Object> coordinates(DriverLocation row) {
        Map<String, Object> point =
                ViJson.read(crypto.unenvelope(row.getLocationCipher(), aad(row)), Map.class);
        if (!point.keySet().equals(Set.of("lng", "lat", "coordinateSystem"))
                || !"GCJ02".equals(point.get("coordinateSystem"))) invalid();
        for (String field : List.of("lng", "lat")) {
            Object value = point.get(field);
            if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue()))
                invalid();
            int max = "lng".equals(field) ? 180 : 90;
            if (new BigDecimal(value.toString()).abs().compareTo(BigDecimal.valueOf(max)) > 0)
                invalid();
        }
        return point;
    }

    private static boolean bound(DriverLocation row) {
        return row != null
                && row.getLocationCipher() != null
                && row.getOwnerDeptId() != null
                && row.getCompanyRevision() != null
                && row.getSessionId() != null
                && row.getContextVersion() != null
                && row.getConfigVersion() != null
                && row.getCapturedAt() != null
                && row.getReceivedAt() != null
                && row.getValidUntil() != null;
    }

    private static String aad(DriverLocation row) {
        return "D01:LOCATION:"
                + row.getDriverId()
                + ":SESSION:"
                + row.getSessionId()
                + ":CONTEXT:"
                + row.getContextVersion()
                + ":COMPANY:"
                + row.getOwnerDeptId()
                + ":EPOCH:"
                + row.getCompanyRevision()
                + ":VERSION:"
                + row.getVersion()
                + ":CONFIG:"
                + row.getConfigVersion()
                + ":CAPTURED:"
                + row.getCapturedAt()
                + ":RECEIVED:"
                + row.getReceivedAt()
                + ":UNTIL:"
                + row.getValidUntil()
                + ":ACCURACY:"
                + row.getAccuracyMeter();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "最近点认证内容不正确");
    }
}
