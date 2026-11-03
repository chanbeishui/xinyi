package com.xinyi.rongli.dispatch.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.auth.mapper.AccountMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.payment.mapper.PayMapper;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.driver.service.DriverQualificationService;
import com.xinyi.rongli.fulfill.service.FulfillService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.driver.service.PenaltyService;
import com.xinyi.rongli.driver.service.TrainingService;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.MiniContext;
import com.xinyi.rongli.common.support.MiniPrincipal;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * dispatch 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/app/driver")
public class DispatchAppController
{
    @Autowired
    private DispatchMapper dispatchMapper;
    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private FulfillService fulfillService;

    @GetMapping("/invites")
    public Map<String, Object> invites()
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> i : dispatchMapper.listActiveInvites(driver.lng("id")))
        {
            Row inv = Row.of(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("inviteId", String.valueOf(inv.lng("id")));
            item.put("orderNo", inv.str("order_no"));
            item.put("pickupStartsAt", ViTime.iso(inv.time("pickup_starts_at")));
            item.put("pickupWindow", ViTime.shanghaiText(inv.time("pickup_starts_at")) + " - "
                    + ViTime.shanghaiText(inv.time("pickup_ends_at")).substring(11));
            item.put("stationName", inv.str("station_name"));
            item.put("expiresAt", ViTime.iso(inv.time("expires_at")));
            item.put("secondsLeft", Math.max(0,
                    java.time.Duration.between(ViTime.now(), inv.time("expires_at")).getSeconds()));
            item.put("estimatedFen", inv.lng("total_fen", 0L));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    @GetMapping("/invites/{inviteId}")
    public Map<String, Object> invite(@PathVariable Long inviteId)
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        Row inv = Row.of(dispatchMapper.findInviteForDriver(inviteId, driver.lng("id")));
        if (inv.isEmpty())
        {
            throw ApiException.notFound("邀约不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("inviteId", String.valueOf(inviteId));
        view.put("orderNo", inv.str("order_no"));
        view.put("status", inv.str("status"));
        view.put("pickupStartsAt", ViTime.iso(inv.time("pickup_starts_at")));
        view.put("stationName", inv.str("station_name"));
        view.put("expiresAt", ViTime.iso(inv.time("expires_at")));
        view.put("vehicle", Json.read(inv.json("vehicle_snapshot")));
        view.put("hint", "接单后请按约定时间上门，交接时需要 4 位交接码");
        return view;
    }
    @PostMapping("/invites/{inviteId}/response")
    public Map<String, Object> respondInvite(@PathVariable Long inviteId,
            @RequestBody DriverDtos.InviteResponseReq req)
    {
        boolean accept = req != null && Boolean.TRUE.equals(req.accept);
        return dispatchService.respondInvite(inviteId, accept, req == null ? null : req.reason);
    }
    @GetMapping("/assignments/current")
    public Map<String, Object> currentAssignment()
    {
        return fulfillService.currentAssignment();
    }
}
