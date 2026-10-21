package com.xinyi.rongli.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.service.StationService;

/**
 * 检测员工视角：工作订单、行驶证复核、结果登记、容量维护、取车通知与拒收异常。
 */
@RestController
@RequestMapping("/api/app/station")
public class StationAppController
{
    @Autowired
    private StationService stationService;

    @GetMapping("/authorized-stations")
    public List<Map<String, Object>> authorizedStations()
    {
        return stationService.authorizedStations();
    }

    @GetMapping("/info")
    public Map<String, Object> info()
    {
        return stationService.stationInfo();
    }

    @PutMapping("/info")
    public Map<String, Object> updateInfo(@RequestBody DriverDtos.StationUpdateReq req)
    {
        return stationService.updateStationInfo(req);
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return stationService.orders(status, pageNum, pageSize);
    }

    @GetMapping("/orders/{orderNo}")
    public Map<String, Object> order(@PathVariable String orderNo)
    {
        return stationService.orderDetail(orderNo);
    }

    @GetMapping("/orders/{orderNo}/license")
    public Map<String, Object> license(@PathVariable String orderNo)
    {
        return stationService.license(orderNo);
    }

    @PutMapping("/orders/{orderNo}/license")
    public Map<String, Object> saveLicense(@PathVariable String orderNo,
            @RequestBody DriverDtos.LicenseReviewReq req)
    {
        return stationService.saveLicense(orderNo, req);
    }

    @GetMapping("/orders/{orderNo}/results")
    public Map<String, Object> results(@PathVariable String orderNo)
    {
        return stationService.results(orderNo);
    }

    @PostMapping("/orders/{orderNo}/results")
    public Map<String, Object> submitResult(@PathVariable String orderNo,
            @RequestBody DriverDtos.ResultReq req)
    {
        return stationService.submitResult(orderNo, req, true);
    }

    @PostMapping("/orders/{orderNo}/result-corrections")
    public Map<String, Object> correction(@PathVariable String orderNo,
            @RequestBody DriverDtos.CorrectionReq req)
    {
        return stationService.applyCorrection(orderNo, req, true);
    }

    @PostMapping("/orders/{orderNo}/pickup-notification")
    public Map<String, Object> pickupNotification(@PathVariable String orderNo)
    {
        return stationService.resendPickupNotification(orderNo);
    }

    @PostMapping("/orders/{orderNo}/exceptions")
    public Map<String, Object> exception(@PathVariable String orderNo,
            @RequestBody DriverDtos.ExceptionReq req)
    {
        return stationService.reportException(orderNo, req);
    }

    @GetMapping("/capacity")
    public Map<String, Object> capacity()
    {
        return stationService.capacity();
    }

    @PutMapping("/capacity/{slotId}")
    public Map<String, Object> updateCapacity(@PathVariable Long slotId,
            @RequestBody DriverDtos.CapacityChangeReq req)
    {
        return stationService.updateCapacity(slotId, req);
    }
}
