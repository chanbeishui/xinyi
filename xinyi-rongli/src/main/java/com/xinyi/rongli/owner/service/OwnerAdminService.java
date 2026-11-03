package com.xinyi.rongli.owner.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.service.StationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.auth.mapper.AccountMapper;
import com.xinyi.rongli.admin.mapper.AdminMapper;
import com.xinyi.rongli.aftersale.mapper.AfterSaleMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.ledger.mapper.LedgerMapper;
import com.xinyi.rongli.common.mapper.MiscMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.order.mapper.PriceMapper;
import com.xinyi.rongli.station.mapper.StationMapper;
import com.xinyi.rongli.vehicle.mapper.VehicleMapper;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.DeptScope;
import com.xinyi.rongli.common.support.Codes;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * 管理端 owner 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class OwnerAdminService
{
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private AfterSaleMapper afterSaleMapper;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventService eventService;
    @Autowired
    private Crypto crypto;

    public Map<String, Object> owners(String keyword, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = camelRows(vehicleMapper.adminOwnerList(keyword, offset, limit));
        for (Map<String, Object> item : rows)
        {
            Map<String, Object> raw = new LinkedHashMap<>(item);
            Object accountId = raw.get("accountId");
            item.put("phoneMasked", maskedPhone(accountId));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", vehicleMapper.adminOwnerCount(keyword));
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> ownerDetail(Long ownerId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(camelRow(o.map()));
        view.put("vehicles", camelRows(vehicleMapper.vehiclesByOwner(o.lng("account_id"))));
        return view;
    }
    @Transactional
    public Map<String, Object> maintainOwner(Long ownerId, String serviceFlag, String remark, Long userId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        if (serviceFlag != null && !List.of("NORMAL", "VIP", "BLOCKED").contains(serviceFlag.toUpperCase()))
        {
            throw ApiException.validation("客户标记只能是 NORMAL / VIP / BLOCKED");
        }
        accountMapper.updateOwnerMaintenance(o.lng("account_id"),
                serviceFlag == null ? o.str("service_flag") : serviceFlag.toUpperCase(),
                remark == null ? o.str("internal_remark") : remark, userId);
        eventService.record("OWNER", ownerId, "OWNER_MAINTAINED", null,
                Map.of("serviceFlag", String.valueOf(o.str("service_flag"))),
                Map.of("serviceFlag", String.valueOf(serviceFlag)), remark);
        return ownerDetail(ownerId);
    }
    public Map<String, Object> ownerVehicles(Long ownerId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", camelRows(vehicleMapper.vehiclesByOwner(o.lng("account_id"))));
        return view;
    }
    public Map<String, Object> ownerOrders(Long ownerId, int pageNum, int pageSize)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        List<Map<String, Object>> rows = orderMapper.ownerList(o.lng("account_id"), null,
                Math.max(0, (pageNum - 1) * pageSize), Math.min(Math.max(pageSize, 1), 100));
        rows.forEach(this::enrichOrderRow);
        view.put("rows", camelRows(rows));
        view.put("total", orderMapper.ownerCount(o.lng("account_id"), null));
        return view;
    }
    public Map<String, Object> ownerAfterSales(Long ownerId, int pageNum, int pageSize)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = Math.max(0, (Math.max(pageNum, 1) - 1) * limit);
        Long accountId = o.lng("account_id");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", camelRows(afterSaleMapper.listByApplicant(accountId, offset, limit)));
        view.put("total", afterSaleMapper.countByApplicant(accountId));
        return view;
    }
    private String camelKey(String key)
    {
        if (key == null || key.indexOf('_') < 0)
        {
            return key;
        }
        StringBuilder sb = new StringBuilder(key.length());
        boolean upper = false;
        for (char c : key.toCharArray())
        {
            if (c == '_')
            {
                upper = true;
                continue;
            }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }
    private Map<String, Object> camelRow(Map<String, Object> row)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        if (row == null)
        {
            return out;
        }
        for (Map.Entry<String, Object> entry : row.entrySet())
        {
            out.put(camelKey(entry.getKey()), cellValue(entry.getValue()));
        }
        return out;
    }
    private List<Map<String, Object>> camelRows(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        if (rows == null)
        {
            return out;
        }
        for (Map<String, Object> row : rows)
        {
            out.add(camelRow(row));
        }
        return out;
    }
    private Object cellValue(Object value)
    {
        if (value instanceof LocalDateTime time)
        {
            return ViTime.iso(time);
        }
        if (value instanceof java.time.LocalDate date)
        {
            return date.toString();
        }
        return value;
    }
    private void enrichOrderRow(Map<String, Object> row)
    {
        Object starts = row.get("pickup_starts_at");
        Object ends = row.get("pickup_ends_at");
        if (starts instanceof LocalDateTime s && ends instanceof LocalDateTime e)
        {
            row.put("pickup_window_label", ViTime.windowLabel(s, e));
        }
        Object status = row.get("fulfillment_status");
        if (status != null)
        {
            row.put("fulfillment_status_label", bookingService.statusLabel(String.valueOf(status)));
        }
        Object fen = row.get("total_fen");
        if (fen instanceof Number n)
        {
            row.put("total_yuan", BigDecimal.valueOf(n.longValue()).movePointLeft(2)
                    .setScale(2, RoundingMode.HALF_UP));
        }
    }
    private String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    private String maskedPhone(Object accountId)
    {
        if (accountId == null)
        {
            return null;
        }
        try
        {
            byte[] cipher = accountMapper.findPhoneCipher(Long.valueOf(String.valueOf(accountId)));
            if (cipher == null || cipher.length == 0)
            {
                return null;
            }
            return maskPhone(crypto.decrypt(cipher));
        }
        catch (RuntimeException ex)
        {
            return null;
        }
    }
}
