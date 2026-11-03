package com.xinyi.rongli.fulfill.controller;

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
 * fulfill 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/app/driver")
public class FulfillAppController
{
    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private FulfillService fulfillService;
    @Autowired
    private com.xinyi.rongli.station.service.StationService stationService;

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return fulfillService.driverOrders(status, pageNum, pageSize);
    }
    @GetMapping("/orders/{orderNo}")
    public Map<String, Object> order(@PathVariable String orderNo)
    {
        return fulfillService.driverOrder(orderNo);
    }
    @PostMapping("/orders/{orderNo}/depart")
    public Map<String, Object> depart(@PathVariable String orderNo)
    {
        return fulfillService.depart(orderNo);
    }
    @PostMapping("/orders/{orderNo}/arrive-pickup")
    public Map<String, Object> arrivePickup(@PathVariable String orderNo)
    {
        return fulfillService.arrivePickup(orderNo);
    }
    @PostMapping("/orders/{orderNo}/arrive-station")
    public Map<String, Object> arriveStation(@PathVariable String orderNo)
    {
        return fulfillService.arriveStation(orderNo);
    }
    @PostMapping("/orders/{orderNo}/assignment-cancellation")
    public Map<String, Object> cancelAssignment(@PathVariable String orderNo,
            @RequestBody(required = false) DriverDtos.DriverCancelReq req)
    {
        return dispatchService.cancelAssignment(orderNo, req == null ? null : req.reason);
    }
    @PostMapping("/orders/{orderNo}/exceptions")
    public Map<String, Object> exception(@PathVariable String orderNo,
            @RequestBody DriverDtos.ExceptionReq req)
    {
        return stationService.reportException(orderNo, req);
    }
    @PostMapping("/orders/{orderNo}/evidence")
    public Map<String, Object> createEvidence(@PathVariable String orderNo,
            @RequestBody(required = false) DriverDtos.EvidenceCreateReq req)
    {
        return fulfillService.createEvidence(orderNo, req == null ? null : req.nodeNo,
                req == null ? null : req.mileageKm == null ? null : req.mileageKm.intValue());
    }
    @GetMapping("/orders/{orderNo}/evidence")
    public Map<String, Object> evidence(@PathVariable String orderNo)
    {
        return fulfillService.evidenceList(orderNo);
    }
    @PostMapping("/orders/{orderNo}/evidence/{groupId}/photos")
    public Map<String, Object> addPhoto(@PathVariable String orderNo, @PathVariable Long groupId,
            @RequestBody DriverDtos.PhotoReq req)
    {
        return fulfillService.addPhoto(orderNo, groupId, req);
    }
    @PutMapping("/orders/{orderNo}/evidence/{groupId}/mileage")
    public Map<String, Object> mileage(@PathVariable String orderNo, @PathVariable Long groupId,
            @RequestBody DriverDtos.MileageReq req)
    {
        return fulfillService.updateMileage(orderNo, groupId, req);
    }
    @PostMapping("/orders/{orderNo}/evidence/{groupId}/ready")
    public Map<String, Object> evidenceReady(@PathVariable String orderNo, @PathVariable Long groupId,
            @RequestBody(required = false) DriverDtos.EvidenceReadyReq req)
    {
        return fulfillService.markEvidenceReady(orderNo, groupId);
    }
}
