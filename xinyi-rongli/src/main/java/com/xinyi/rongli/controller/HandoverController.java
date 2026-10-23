package com.xinyi.rongli.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.service.FulfillService;

/**
 * 实物交接：车主、代驾、检测员工三方共用的交接接口。
 *
 * <p>交接码只在持码方读取时返回一次明文；核验失败计次，连续 3 次锁定。</p>
 */
@RestController
@RequestMapping("/api/common/orders/{orderNo}/handovers")
public class HandoverController
{
    @Autowired
    private FulfillService fulfillService;

    @PostMapping
    public Map<String, Object> create(@PathVariable String orderNo,
            @RequestBody(required = false) DriverDtos.HandoverCreateReq req)
    {
        if (req == null || req.handoverType == null || req.handoverType.isBlank())
        {
            throw com.xinyi.rongli.support.ApiException.validation("请指定交接类型");
        }
        return fulfillService.createHandover(orderNo, req.handoverType.toUpperCase());
    }

    @GetMapping("/{handoverId}")
    public Map<String, Object> detail(@PathVariable String orderNo, @PathVariable Long handoverId)
    {
        return fulfillService.handoverDetail(orderNo, handoverId);
    }

    @GetMapping("/{handoverId}/secret")
    public Map<String, Object> secret(@PathVariable String orderNo, @PathVariable Long handoverId)
    {
        return fulfillService.handoverSecret(orderNo, handoverId);
    }

    @PostMapping("/{handoverId}/confirm")
    public Map<String, Object> confirm(@PathVariable String orderNo, @PathVariable Long handoverId,
            @RequestBody DriverDtos.HandoverConfirmReq req)
    {
        return fulfillService.confirmHandover(orderNo, handoverId, req);
    }

    /**
     * 持码方确认已交付（G04）。
     *
     * <p>站内交接需要司机与检测员工双方确认：检测员工核验交接码后，司机还要在自己的端内
     * 确认车辆已交出。本接口不需要再输码——调用方本身就是出码方。</p>
     */
    @PostMapping("/{handoverId}/holder-confirm")
    public Map<String, Object> holderConfirm(@PathVariable String orderNo, @PathVariable Long handoverId)
    {
        return fulfillService.holderConfirmHandover(orderNo, handoverId);
    }
}
