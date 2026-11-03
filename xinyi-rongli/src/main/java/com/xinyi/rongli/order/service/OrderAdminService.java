package com.xinyi.rongli.order.service;

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

import com.xinyi.rongli.aftersale.mapper.AfterSaleMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.ledger.mapper.LedgerMapper;
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
import com.xinyi.rongli.aftersale.mapper.AdminAfterSaleMapper;
import com.xinyi.rongli.fulfill.mapper.AdminFulfillMapper;

/**
 * 管理端 order 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class OrderAdminService
{
    @Autowired
    private AdminAfterSaleMapper adminAfterSaleMapper;

    @Autowired
    private AdminFulfillMapper adminFulfillMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StationService stationService;

    public Map<String, Object> orders(String status, String group, String keyword, Long stationId,
            LocalDateTime from, LocalDateTime to, int pageNum, int pageSize)
    {
        return stationService.adminOrders(status, group, keyword, stationId, from, to, pageNum, pageSize);
    }
    public Map<String, Object> orderDetail(String orderNo)
    {
        Map<String, Object> view = new LinkedHashMap<>(stationService.adminOrderDetail(orderNo));
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        Long orderId = order.lng("id");
        view.put("ownerAccountId", order.lng("owner_account_id") == null ? null
                : String.valueOf(order.lng("owner_account_id")));
        view.put("stationId", order.lng("station_id") == null ? null : String.valueOf(order.lng("station_id")));
        view.put("totalFen", order.lng("total_fen", 0L));
        view.put("custodyRefId", order.lng("custody_ref_id") == null ? null
                : String.valueOf(order.lng("custody_ref_id")));
        view.put("returnedAt", ViTime.iso(order.time("returned_at")));
        view.put("autoConfirmAt", ViTime.iso(order.time("auto_confirm_at")));
        view.put("disputes", adminAfterSaleMapper.listDisputesOfOrder(orderId).size());
        view.put("corrections", adminFulfillMapper.listCorrectionsOfOrder(orderId).size());
        return view;
    }
}
