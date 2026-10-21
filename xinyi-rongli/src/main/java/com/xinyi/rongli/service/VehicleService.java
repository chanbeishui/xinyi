package com.xinyi.rongli.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.mapper.VehicleMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 车主车辆与常用地址：地址与坐标以密文入库，列表只回传业务必要字段。
 */
@Service
public class VehicleService
{
    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    public List<Map<String, Object>> vehicles()
    {
        MiniPrincipal p = MiniContext.require();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : vehicleMapper.listByOwner(p.accountId))
        {
            Row v = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("vehicleId", String.valueOf(v.lng("id")));
            item.put("plateNo", v.str("plate_no"));
            item.put("vehicleTypeCode", v.str("vehicle_type_code"));
            item.put("vehicleTypeName", v.str("vehicle_type_name", v.str("vehicle_type_code")));
            item.put("brandModel", v.str("brand_model"));
            item.put("vinLast4", v.str("vin_last4"));
            item.put("registeredOn", v.date("registered_on") == null ? null : v.date("registered_on").toString());
            item.put("useType", v.str("use_type"));
            item.put("verificationStatus", v.str("verification_status"));
            item.put("isDefault", v.bool("is_default"));
            item.put("version", v.lng("version"));
            out.add(item);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> createVehicle(MiniDtos.VehicleReq req)
    {
        MiniPrincipal p = MiniContext.require();
        String plate = normalizePlate(req == null ? null : req.plateNo);
        if (req == null || req.vehicleTypeCode == null || req.vehicleTypeCode.isBlank())
        {
            throw ApiException.validation("请选择车型");
        }
        requireVehicleType(req.vehicleTypeCode);
        if (vehicleMapper.countByPlate(p.accountId, plate) > 0)
        {
            throw ApiException.stateConflict("该车牌已存在于本人车辆档案");
        }
        boolean first = vehicleMapper.listByOwner(p.accountId).isEmpty();
        boolean isDefault = req.isDefault != null ? req.isDefault : first;
        if (isDefault)
        {
            vehicleMapper.clearDefault(p.accountId);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ownerAccountId", p.accountId);
        row.put("plateNo", plate);
        row.put("vehicleTypeCode", req.vehicleTypeCode);
        row.put("brandModel", req.brandModel);
        row.put("vinLast4", req.vinLast4);
        row.put("registeredOn", parseDate(req.registeredOn));
        row.put("useType", req.useType);
        row.put("isDefault", isDefault);
        vehicleMapper.insert(row);
        eventService.record("VEHICLE", ((Number) row.get("id")).longValue(), "VEHICLE_CREATED", null);
        return vehicleView(((Number) row.get("id")).longValue());
    }

    @Transactional
    public Map<String, Object> updateVehicle(Long vehicleId, MiniDtos.VehicleReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row existing = Row.of(vehicleMapper.findOwned(vehicleId, p.accountId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("车辆不存在");
        }
        String plate = req.plateNo == null || req.plateNo.isBlank() ? existing.str("plate_no") : normalizePlate(req.plateNo);
        if (req.vehicleTypeCode != null && !req.vehicleTypeCode.isBlank())
        {
            requireVehicleType(req.vehicleTypeCode);
        }
        if (!plate.equals(existing.str("plate_no")) && vehicleMapper.countByPlate(p.accountId, plate) > 0)
        {
            throw ApiException.stateConflict("该车牌已存在于本人车辆档案");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", vehicleId);
        row.put("ownerAccountId", p.accountId);
        row.put("plateNo", plate);
        row.put("vehicleTypeCode", req.vehicleTypeCode == null ? existing.str("vehicle_type_code") : req.vehicleTypeCode);
        row.put("brandModel", req.brandModel == null ? existing.str("brand_model") : req.brandModel);
        row.put("vinLast4", req.vinLast4 == null ? existing.str("vin_last4") : req.vinLast4);
        row.put("registeredOn", req.registeredOn == null ? existing.date("registered_on") : parseDate(req.registeredOn));
        row.put("useType", req.useType == null ? existing.str("use_type") : req.useType);
        vehicleMapper.update(row);
        eventService.record("VEHICLE", vehicleId, "VEHICLE_UPDATED", null);
        return vehicleView(vehicleId);
    }

    @Transactional
    public void deleteVehicle(Long vehicleId)
    {
        MiniPrincipal p = MiniContext.require();
        if (vehicleMapper.softDelete(vehicleId, p.accountId) == 0)
        {
            throw ApiException.notFound("车辆不存在或已删除");
        }
        eventService.record("VEHICLE", vehicleId, "VEHICLE_DELETED", null);
    }

    @Transactional
    public Map<String, Object> setDefaultVehicle(Long vehicleId)
    {
        MiniPrincipal p = MiniContext.require();
        if (vehicleMapper.findOwned(vehicleId, p.accountId) == null)
        {
            throw ApiException.notFound("车辆不存在");
        }
        vehicleMapper.clearDefault(p.accountId);
        vehicleMapper.markDefault(vehicleId, p.accountId);
        return vehicleView(vehicleId);
    }

    private Map<String, Object> vehicleView(Long vehicleId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("vehicleId", String.valueOf(v.lng("id")));
        item.put("plateNo", v.str("plate_no"));
        item.put("vehicleTypeCode", v.str("vehicle_type_code"));
        item.put("brandModel", v.str("brand_model"));
        item.put("vinLast4", v.str("vin_last4"));
        item.put("verificationStatus", v.str("verification_status"));
        item.put("isDefault", v.bool("is_default"));
        item.put("version", v.lng("version"));
        return item;
    }

    // ------------------------------------------------------------------ 地址

    public List<Map<String, Object>> addresses()
    {
        MiniPrincipal p = MiniContext.require();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : vehicleMapper.listAddresses(p.accountId))
        {
            out.add(addressView(Row.of(r)));
        }
        return out;
    }

    private Map<String, Object> addressView(Row row)
    {
        JSONObject plain = Json.read(crypto.decrypt((byte[]) row.get("address_cipher")));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("addressId", String.valueOf(row.lng("id")));
        item.put("label", row.str("label"));
        item.put("address", plain.getString("address"));
        item.put("lng", plain.get("lng"));
        item.put("lat", plain.get("lat"));
        item.put("isDefault", row.bool("is_default"));
        item.put("version", row.lng("version"));
        return item;
    }

    @Transactional
    public Map<String, Object> createAddress(MiniDtos.AddressReq req)
    {
        MiniPrincipal p = MiniContext.require();
        validateAddress(req);
        boolean first = vehicleMapper.listAddresses(p.accountId).isEmpty();
        boolean isDefault = req.isDefault != null ? req.isDefault : first;
        if (isDefault)
        {
            vehicleMapper.clearAddressDefault(p.accountId);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ownerAccountId", p.accountId);
        row.put("label", req.label == null ? "常用地址" : req.label);
        row.put("addressCipher", crypto.encrypt(addressJson(req)));
        row.put("locationCipher", crypto.encrypt(locationJson(req)));
        row.put("isDefault", isDefault);
        vehicleMapper.insertAddress(row);
        return addressView(Row.of(vehicleMapper.findAddress(((Number) row.get("id")).longValue(), p.accountId)));
    }

    @Transactional
    public Map<String, Object> updateAddress(Long addressId, MiniDtos.AddressReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row existing = Row.of(vehicleMapper.findAddress(addressId, p.accountId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("地址不存在");
        }
        validateAddress(req);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", addressId);
        row.put("ownerAccountId", p.accountId);
        row.put("label", req.label == null ? existing.str("label") : req.label);
        row.put("addressCipher", crypto.encrypt(addressJson(req)));
        row.put("locationCipher", crypto.encrypt(locationJson(req)));
        vehicleMapper.updateAddress(row);
        return addressView(Row.of(vehicleMapper.findAddress(addressId, p.accountId)));
    }

    @Transactional
    public void deleteAddress(Long addressId)
    {
        MiniPrincipal p = MiniContext.require();
        if (vehicleMapper.softDeleteAddress(addressId, p.accountId) == 0)
        {
            throw ApiException.notFound("地址不存在或已删除");
        }
    }

    @Transactional
    public Map<String, Object> setDefaultAddress(Long addressId)
    {
        MiniPrincipal p = MiniContext.require();
        if (vehicleMapper.findAddress(addressId, p.accountId) == null)
        {
            throw ApiException.notFound("地址不存在");
        }
        vehicleMapper.clearAddressDefault(p.accountId);
        vehicleMapper.markAddressDefault(addressId, p.accountId);
        return addressView(Row.of(vehicleMapper.findAddress(addressId, p.accountId)));
    }

    private void validateAddress(MiniDtos.AddressReq req)
    {
        if (req == null || req.address == null || req.address.isBlank())
        {
            throw ApiException.validation("请填写详细地址");
        }
        if (req.lng == null || req.lat == null)
        {
            throw ApiException.validation("请在地图上选择准确位置");
        }
        if (req.address.length() > 200)
        {
            throw ApiException.validation("地址过长");
        }
    }

    private String addressJson(MiniDtos.AddressReq req)
    {
        JSONObject obj = new JSONObject();
        obj.put("address", req.address.trim());
        obj.put("lng", req.lng);
        obj.put("lat", req.lat);
        obj.put("label", req.label);
        return obj.toJSONString();
    }

    private String locationJson(MiniDtos.AddressReq req)
    {
        JSONObject obj = new JSONObject();
        obj.put("lng", req.lng);
        obj.put("lat", req.lat);
        return obj.toJSONString();
    }

    /** 车型必须来自 vehicle_type 字典，避免脏车型码进入档案后再到报价环节才暴露。 */
    private void requireVehicleType(String vehicleTypeCode)
    {
        if (vehicleMapper.countDictValue("vehicle_type", vehicleTypeCode) == 0)
        {
            throw ApiException.validation("车型不在可选范围内，请重新选择");
        }
    }

    private String normalizePlate(String plateNo)
    {
        if (plateNo == null || plateNo.isBlank())
        {
            throw ApiException.validation("请填写车牌号");
        }
        String plate = plateNo.trim().replace(" ", "").toUpperCase();
        if (!plate.matches("^[\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5,6}$"))
        {
            throw ApiException.validation("车牌号格式不正确，例如：粤K12345");
        }
        return plate;
    }

    private LocalDate parseDate(String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        try
        {
            return LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
        }
        catch (Exception e)
        {
            throw ApiException.validation("日期格式应为 yyyy-MM-dd");
        }
    }

    public BigDecimal distanceKm(double lng1, double lat1, double lng2, double lat2)
    {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return BigDecimal.valueOf(2 * r * Math.asin(Math.min(1, Math.sqrt(a))) / 1000).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public String nowText()
    {
        return ViTime.iso(ViTime.now());
    }
}
