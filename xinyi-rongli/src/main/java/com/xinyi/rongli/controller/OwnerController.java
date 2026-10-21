package com.xinyi.rongli.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.service.AfterSaleService;
import com.xinyi.rongli.service.AuthService;
import com.xinyi.rongli.service.BookingService;
import com.xinyi.rongli.service.FulfillService;
import com.xinyi.rongli.service.OwnerOrderService;
import com.xinyi.rongli.service.VehicleService;

/**
 * 车主视角：档案、车辆、地址、时段、报价下单、支付、订单、售后与评价。
 */
@RestController
@RequestMapping("/api/app/owner")
public class OwnerController
{
    @Autowired
    private AuthService authService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private OwnerOrderService ownerOrderService;

    @Autowired
    private AfterSaleService afterSaleService;

    @Autowired
    private FulfillService fulfillService;

    // ------------------------------------------------------------------ 档案

    @PatchMapping("/profile")
    public Map<String, Object> patchProfile(@RequestBody MiniDtos.ProfilePatch req)
    {
        return authService.updateProfile(req);
    }

    // ------------------------------------------------------------------ 车辆

    @GetMapping("/vehicles")
    public List<Map<String, Object>> vehicles()
    {
        return vehicleService.vehicles();
    }

    @PostMapping("/vehicles")
    public Map<String, Object> createVehicle(@RequestBody MiniDtos.VehicleReq req)
    {
        return vehicleService.createVehicle(req);
    }

    @PutMapping("/vehicles/{id}")
    public Map<String, Object> updateVehicle(@PathVariable Long id, @RequestBody MiniDtos.VehicleReq req)
    {
        return vehicleService.updateVehicle(id, req);
    }

    @DeleteMapping("/vehicles/{id}")
    public Map<String, Object> deleteVehicle(@PathVariable Long id)
    {
        vehicleService.deleteVehicle(id);
        return Map.of("deleted", true, "vehicleId", String.valueOf(id));
    }

    @PostMapping("/vehicles/{id}/default")
    public Map<String, Object> defaultVehicle(@PathVariable Long id)
    {
        return vehicleService.setDefaultVehicle(id);
    }

    // ------------------------------------------------------------------ 地址

    @GetMapping("/addresses")
    public List<Map<String, Object>> addresses()
    {
        return vehicleService.addresses();
    }

    @PostMapping("/addresses")
    public Map<String, Object> createAddress(@RequestBody MiniDtos.AddressReq req)
    {
        return vehicleService.createAddress(req);
    }

    @PutMapping("/addresses/{id}")
    public Map<String, Object> updateAddress(@PathVariable Long id, @RequestBody MiniDtos.AddressReq req)
    {
        return vehicleService.updateAddress(id, req);
    }

    @DeleteMapping("/addresses/{id}")
    public Map<String, Object> deleteAddress(@PathVariable Long id)
    {
        vehicleService.deleteAddress(id);
        return Map.of("deleted", true, "addressId", String.valueOf(id));
    }

    @PostMapping("/addresses/{id}/default")
    public Map<String, Object> defaultAddress(@PathVariable Long id)
    {
        return vehicleService.setDefaultAddress(id);
    }

    // ------------------------------------------------------------------ 时段与下单

    @GetMapping("/schedule/slots")
    public List<Map<String, Object>> slots()
    {
        return bookingService.slots();
    }

    @PostMapping("/orders/quote")
    public Map<String, Object> quote(@RequestBody MiniDtos.QuoteReq req)
    {
        return bookingService.quote(req);
    }

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody MiniDtos.OrderCreateReq req)
    {
        return bookingService.createOrder(req);
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return bookingService.ownerOrderList(status, pageNum, pageSize);
    }

    @GetMapping("/orders/{orderNo}")
    public Map<String, Object> order(@PathVariable String orderNo)
    {
        return bookingService.ownerOrderView(orderNo, com.xinyi.rongli.support.MiniContext.require().accountId);
    }

    @GetMapping("/orders/{orderNo}/timeline")
    public Map<String, Object> timeline(@PathVariable String orderNo)
    {
        return ownerOrderService.timeline(orderNo);
    }

    // ------------------------------------------------------------------ 支付

    @PostMapping("/orders/{orderNo}/prepay")
    public Map<String, Object> prepay(@PathVariable String orderNo)
    {
        return bookingService.prepay(orderNo);
    }

    /** 联调通道模拟支付成功；真实通道下该接口会拒绝 */
    @PostMapping("/orders/{orderNo}/mock-pay")
    public Map<String, Object> mockPay(@PathVariable String orderNo)
    {
        return bookingService.mockPay(orderNo);
    }

    @GetMapping("/orders/{orderNo}/payment")
    public Map<String, Object> payment(@PathVariable String orderNo)
    {
        return ownerOrderService.payment(orderNo);
    }

    @PostMapping("/orders/{orderNo}/close-unpaid")
    public Map<String, Object> closeUnpaid(@PathVariable String orderNo,
            @RequestBody(required = false) MiniDtos.VersionReq req)
    {
        return bookingService.closeUnpaid(orderNo, req);
    }

    // ------------------------------------------------------------------ 服务确认与结果

    @PostMapping("/orders/{orderNo}/service-confirmation")
    public Map<String, Object> confirmService(@PathVariable String orderNo,
            @RequestBody(required = false) MiniDtos.ServiceConfirmReq req)
    {
        return bookingService.confirmService(orderNo, req);
    }

    @GetMapping("/orders/{orderNo}/result")
    public Map<String, Object> result(@PathVariable String orderNo)
    {
        return ownerOrderService.result(orderNo);
    }

    @GetMapping("/orders/{orderNo}/evidence")
    public Map<String, Object> evidence(@PathVariable String orderNo)
    {
        return fulfillService.evidenceList(orderNo);
    }

    @GetMapping("/orders/{orderNo}/service-snapshot")
    public Map<String, Object> serviceSnapshot(@PathVariable String orderNo)
    {
        return ownerOrderService.serviceSnapshot(orderNo);
    }

    // ------------------------------------------------------------------ 争议与评价

    @PostMapping("/orders/{orderNo}/disputes")
    public Map<String, Object> createDispute(@PathVariable String orderNo,
            @RequestBody MiniDtos.DisputeCreateReq req)
    {
        return ownerOrderService.createDispute(orderNo, req);
    }

    @GetMapping("/orders/{orderNo}/disputes")
    public Map<String, Object> disputes(@PathVariable String orderNo)
    {
        return ownerOrderService.listDisputes(orderNo);
    }

    @PostMapping("/orders/{orderNo}/reviews")
    public Map<String, Object> review(@PathVariable String orderNo,
            @RequestBody MiniDtos.ReviewCreateReq req)
    {
        return ownerOrderService.submitReview(orderNo, req);
    }

    // ------------------------------------------------------------------ 售后

    @PostMapping("/orders/{orderNo}/after-sales")
    public Map<String, Object> createAfterSale(@PathVariable String orderNo,
            @RequestBody MiniDtos.AfterSaleApply req)
    {
        return afterSaleService.createByOwner(orderNo, req);
    }

    @GetMapping("/orders/{orderNo}/after-sales")
    public Map<String, Object> afterSalesOfOrder(@PathVariable String orderNo)
    {
        return afterSaleService.listOfOrder(orderNo, false);
    }

    @GetMapping("/after-sales")
    public Map<String, Object> afterSales(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return afterSaleService.myCases(pageNum, pageSize);
    }

    @GetMapping("/after-sales/{caseId}")
    public Map<String, Object> afterSale(@PathVariable Long caseId)
    {
        return afterSaleService.detail(caseId, false);
    }

    @PostMapping("/after-sales/{caseId}/notes")
    public Map<String, Object> afterSaleNote(@PathVariable Long caseId,
            @RequestBody MiniDtos.AfterSaleNote req)
    {
        return afterSaleService.addNote(caseId, req == null ? null : req.content, false);
    }
}
