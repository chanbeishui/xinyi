package com.xinyi.rongli.admin.service;

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
import com.xinyi.rongli.admin.service.OpsAdminService;
import com.xinyi.rongli.order.service.OrderAdminService;
import com.xinyi.rongli.fulfill.service.FulfillAdminService;
import com.xinyi.rongli.dispatch.service.DispatchAdminService;
import com.xinyi.rongli.station.service.StationAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;
import com.xinyi.rongli.aftersale.service.AfterSaleAdminService;
import com.xinyi.rongli.driver.service.DriverAdminService;
import com.xinyi.rongli.vehicle.service.VehicleAdminService;
import com.xinyi.rongli.owner.service.OwnerAdminService;

/**
 * 管理端聚合门面。
 *
 * 原先承载全部管理端逻辑，按功能域拆分后只做转发，保留是为让控制器在拆分期间无需同步改动；
 * 控制器改为直接注入各域服务后可整体删除。
 */
@Service
public class AdminService
{
    @Autowired
    private OpsAdminService opsAdminService;

    @Autowired
    private OrderAdminService orderAdminService;

    @Autowired
    private FulfillAdminService fulfillAdminService;

    @Autowired
    private DispatchAdminService dispatchAdminService;

    @Autowired
    private StationAdminService stationAdminService;

    @Autowired
    private LedgerAdminService ledgerAdminService;

    @Autowired
    private AfterSaleAdminService afterSaleAdminService;

    @Autowired
    private DriverAdminService driverAdminService;

    @Autowired
    private VehicleAdminService vehicleAdminService;

    @Autowired
    private OwnerAdminService ownerAdminService;


    public Map<String, Object> dashboard()
    {
        return opsAdminService.dashboard();
    }

    public Map<String, Object> exports(String status, int pageNum, int pageSize)
    {
        return opsAdminService.exports(status, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> requestExport(Map<String, Object> req, Long userId)
    {
        return opsAdminService.requestExport(req, userId);
    }

@Transactional
        public Map<String, Object> auditExport(Long caseId, boolean approve, String opinion, Long userId)
    {
        return opsAdminService.auditExport(caseId, approve, opinion, userId);
    }

    public Map<String, Object> exportTask(Long caseId)
    {
        return opsAdminService.exportTask(caseId);
    }

    public Map<String, Object> auditEvents(Long orderId, String aggregateType, String action, int pageNum, int pageSize)
    {
        return opsAdminService.auditEvents(orderId, aggregateType, action, pageNum, pageSize);
    }

    public Map<String, Object> statistics(int days)
    {
        return opsAdminService.statistics(days);
    }

    public Map<String, Object> statistics(int days, String range)
    {
        return opsAdminService.statistics(days, range);
    }

    public Map<String, Object> jobs(int pageNum, int pageSize)
    {
        return opsAdminService.jobs(pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> retryJob(Long taskId)
    {
        return opsAdminService.retryJob(taskId);
    }

    public Map<String, Object> businessConfig()
    {
        return opsAdminService.businessConfig();
    }

    public Map<String, Object> notificationTemplates()
    {
        return opsAdminService.notificationTemplates();
    }

@Transactional
        public Map<String, Object> saveNotificationTemplate(String templateId, String content, Long userId)
    {
        return opsAdminService.saveNotificationTemplate(templateId, content, userId);
    }

    public Map<String, Object> orders(String status, String group, String keyword, Long stationId, LocalDateTime from, LocalDateTime to, int pageNum, int pageSize)
    {
        return orderAdminService.orders(status, group, keyword, stationId, from, to, pageNum, pageSize);
    }

    public Map<String, Object> orderDetail(String orderNo)
    {
        return orderAdminService.orderDetail(orderNo);
    }

    public Map<String, Object> license(String orderNo)
    {
        return fulfillAdminService.license(orderNo);
    }

@Transactional
        public Map<String, Object> saveLicense(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.LicenseReviewReq req, Long userId)
    {
        return fulfillAdminService.saveLicense(orderNo, req, userId);
    }

    public Map<String, Object> results(String orderNo)
    {
        return fulfillAdminService.results(orderNo);
    }

@Transactional
        public Map<String, Object> submitResult(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.ResultReq req)
    {
        return fulfillAdminService.submitResult(orderNo, req);
    }

@Transactional
        public Map<String, Object> applyCorrection(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.CorrectionReq req)
    {
        return fulfillAdminService.applyCorrection(orderNo, req);
    }

@Transactional
        public Map<String, Object> resendPickupNotification(String orderNo)
    {
        return fulfillAdminService.resendPickupNotification(orderNo);
    }

@Transactional
        public Map<String, Object> reportException(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.ExceptionReq req)
    {
        return fulfillAdminService.reportException(orderNo, req);
    }

    public Map<String, Object> receivingBoard(Long stationId, String keyword, int pageNum, int pageSize)
    {
        return fulfillAdminService.receivingBoard(stationId, keyword, pageNum, pageSize);
    }

    public Map<String, Object> inspectionTodoBoard(Long stationId, String keyword, String bucket, int pageNum, int pageSize)
    {
        return fulfillAdminService.inspectionTodoBoard(stationId, keyword, bucket, pageNum, pageSize);
    }

    public Map<String, Object> corrections(String status, int pageNum, int pageSize)
    {
        return fulfillAdminService.corrections(status, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> auditCorrection(Long correctionId, boolean approve, String opinion, Long userId)
    {
        return fulfillAdminService.auditCorrection(correctionId, approve, opinion, userId);
    }

    public Map<String, Object> dispatchQueue()
    {
        return dispatchAdminService.dispatchQueue();
    }

    public Map<String, Object> dispatchCandidates(String keyword)
    {
        return dispatchAdminService.dispatchCandidates(keyword);
    }

@Transactional
        public Map<String, Object> manualInvite(String orderNo, Long driverId, String reason)
    {
        return dispatchAdminService.manualInvite(orderNo, driverId, reason);
    }

@Transactional
        public Map<String, Object> dispatchNow(String orderNo)
    {
        return dispatchAdminService.dispatchNow(orderNo);
    }

    public Map<String, Object> dispatchInvites(Long orderId, Long driverId, String status, int pageNum, int pageSize)
    {
        return dispatchAdminService.dispatchInvites(orderId, driverId, status, pageNum, pageSize);
    }

    public Map<String, Object> dispatchInvites(Long orderId, Long driverId, String status, String keyword, int pageNum, int pageSize)
    {
        return dispatchAdminService.dispatchInvites(orderId, driverId, status, keyword, pageNum, pageSize);
    }

    public Map<String, Object> dispatchConfig()
    {
        return dispatchAdminService.dispatchConfig();
    }

@Transactional
        public Map<String, Object> saveDispatchConfig(Map<String, Object> req)
    {
        return dispatchAdminService.saveDispatchConfig(req);
    }

    public Map<String, Object> previewDispatchPlan(Integer leadMinutes)
    {
        return dispatchAdminService.previewDispatchPlan(leadMinutes);
    }

@Transactional
        public Map<String, Object> applyDispatchPlan(Integer leadMinutes)
    {
        return dispatchAdminService.applyDispatchPlan(leadMinutes);
    }

    public Map<String, Object> capacity(Long stationId)
    {
        return stationAdminService.capacity(stationId);
    }

@Transactional
        public Map<String, Object> updateCapacity(Long slotId, com.xinyi.rongli.driver.dto.DriverDtos.CapacityChangeReq req)
    {
        return stationAdminService.updateCapacity(slotId, req);
    }

    public Map<String, Object> stations(int pageNum, int pageSize)
    {
        return stationAdminService.stations(pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> createStation(com.xinyi.rongli.driver.dto.DriverDtos.StationUpdateReq req)
    {
        return stationAdminService.createStation(req);
    }

@Transactional
        public Map<String, Object> updateStation(Long stationId, com.xinyi.rongli.driver.dto.DriverDtos.StationUpdateReq req)
    {
        return stationAdminService.updateStation(stationId, req);
    }

    public Map<String, Object> stationInfo(Long stationId)
    {
        return stationAdminService.stationInfo(stationId);
    }

    public Map<String, Object> stationGrants(int pageNum, int pageSize)
    {
        return stationAdminService.stationGrants(pageNum, pageSize);
    }

    public List<Map<String, Object>> employeeCandidates(String keyword)
    {
        return stationAdminService.employeeCandidates(keyword);
    }

@Transactional
        public Map<String, Object> updateStationGrant(Long employeeUserId, Long stationId, boolean enabled, String reason, Long grantedBy)
    {
        return stationAdminService.updateStationGrant(employeeUserId, stationId, enabled, reason, grantedBy);
    }

    public Map<String, Object> prices(String status, String vehicleTypeCode, int pageNum, int pageSize)
    {
        return ledgerAdminService.prices(status, vehicleTypeCode, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> createPrice(Map<String, Object> req, Long userId)
    {
        return ledgerAdminService.createPrice(req, userId);
    }

@Transactional
        public Map<String, Object> updatePrice(Long ruleId, Map<String, Object> req)
    {
        return ledgerAdminService.updatePrice(ruleId, req);
    }

@Transactional
        public Map<String, Object> submitPrice(Long ruleId, Long userId)
    {
        return ledgerAdminService.submitPrice(ruleId, userId);
    }

@Transactional
        public Map<String, Object> auditPrice(Long ruleId, boolean approve, String opinion, Long userId)
    {
        return ledgerAdminService.auditPrice(ruleId, approve, opinion, userId);
    }

@Transactional
        public Map<String, Object> publishPrice(Long ruleId, Long userId)
    {
        return ledgerAdminService.publishPrice(ruleId, userId);
    }

    public Map<String, Object> reconciliation(String status, int pageNum, int pageSize)
    {
        return ledgerAdminService.reconciliation(status, pageNum, pageSize);
    }

    public Map<String, Object> reconciliation(String status, String keyword, int pageNum, int pageSize)
    {
        return ledgerAdminService.reconciliation(status, keyword, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> queryReconciliation(Long caseId)
    {
        return ledgerAdminService.queryReconciliation(caseId);
    }

@Transactional
        public Map<String, Object> resolveReconciliation(Long caseId, String conclusion, Long userId)
    {
        return ledgerAdminService.resolveReconciliation(caseId, conclusion, userId);
    }

    public Map<String, Object> driverReconciliationOverview(Long driverId)
    {
        return ledgerAdminService.driverReconciliationOverview(driverId);
    }

    public Map<String, Object> driverReconciliationEntries(Long driverId, String from, String to, String keyword, boolean onlySelectable)
    {
        return ledgerAdminService.driverReconciliationEntries(driverId, from, to, keyword, onlySelectable);
    }

    public Map<String, Object> driverFeeLedger(Long driverId, String status)
    {
        return ledgerAdminService.driverFeeLedger(driverId, status);
    }

    public Map<String, Object> statements(Long driverId, String status, int pageNum, int pageSize)
    {
        return ledgerAdminService.statements(driverId, status, pageNum, pageSize);
    }

    public Map<String, Object> statementRevision(Long statementId, int revision)
    {
        return ledgerAdminService.statementRevision(statementId, revision);
    }

    public Map<String, Object> paymentRecords(String status, Long statementId, int pageNum, int pageSize)
    {
        return ledgerAdminService.paymentRecords(status, statementId, pageNum, pageSize);
    }

    public Map<String, Object> paymentRecordDetail(Long recordId)
    {
        return ledgerAdminService.paymentRecordDetail(recordId);
    }

    public Map<String, Object> feeAdjustments(String status, int pageNum, int pageSize)
    {
        return ledgerAdminService.feeAdjustments(status, pageNum, pageSize);
    }

    public Map<String, Object> statementDifferences(String status, int pageNum, int pageSize)
    {
        return ledgerAdminService.statementDifferences(status, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> resolveDifference(Long differenceId, String resolution, boolean resend, Long userId)
    {
        return ledgerAdminService.resolveDifference(differenceId, resolution, resend, userId);
    }

    public Map<String, Object> disputes(String status, int pageNum, int pageSize)
    {
        return afterSaleAdminService.disputes(status, pageNum, pageSize);
    }

    public Map<String, Object> disputeDetail(Long disputeId)
    {
        return afterSaleAdminService.disputeDetail(disputeId);
    }

@Transactional
        public Map<String, Object> resolveDispute(Long disputeId, String conclusion, Long userId)
    {
        return afterSaleAdminService.resolveDispute(disputeId, conclusion, userId);
    }

    public Map<String, Object> exceptions(String status, int pageNum, int pageSize)
    {
        return afterSaleAdminService.exceptions(status, pageNum, pageSize);
    }

@Transactional
        public Map<String, Object> resolveException(Long exceptionId, String resolution, Long userId)
    {
        return afterSaleAdminService.resolveException(exceptionId, resolution, userId);
    }

    public Map<String, Object> qualifications(String status, int pageNum, int pageSize)
    {
        return driverAdminService.qualifications(status, pageNum, pageSize);
    }

    public Map<String, Object> qualificationDetail(Long applicationId)
    {
        return driverAdminService.qualificationDetail(applicationId);
    }

@Transactional
        public Map<String, Object> auditQualification(Long applicationId, boolean approve, String opinion, Long userId)
    {
        return driverAdminService.auditQualification(applicationId, approve, opinion, userId);
    }

    public Map<String, Object> drivers(String keyword, String status, int pageNum, int pageSize)
    {
        return driverAdminService.drivers(keyword, status, pageNum, pageSize);
    }

    public Map<String, Object> driverDetail(Long driverId)
    {
        return driverAdminService.driverDetail(driverId);
    }

@Transactional
        public Map<String, Object> maintainDriver(Long driverId, String remark, Boolean newAssignmentEnabled, Long userId)
    {
        return driverAdminService.maintainDriver(driverId, remark, newAssignmentEnabled, userId);
    }

@Transactional
        public Map<String, Object> disableNewAssignments(Long driverId, boolean disabled, String reason, Long userId)
    {
        return driverAdminService.disableNewAssignments(driverId, disabled, reason, userId);
    }

    public Map<String, Object> driverAssignments(Long driverId, int pageNum, int pageSize)
    {
        return driverAdminService.driverAssignments(driverId, pageNum, pageSize);
    }

    public Map<String, Object> vehicles(String keyword, String vehicleTypeCode, String verificationStatus, int pageNum, int pageSize)
    {
        return vehicleAdminService.vehicles(keyword, vehicleTypeCode, verificationStatus, pageNum, pageSize);
    }

    public Map<String, Object> vehicleDetail(Long vehicleId)
    {
        return vehicleAdminService.vehicleDetail(vehicleId);
    }

@Transactional
        public Map<String, Object> maintainVehicle(Long vehicleId, String remark, String verificationStatus, Long userId)
    {
        return vehicleAdminService.maintainVehicle(vehicleId, remark, verificationStatus, userId);
    }

    public Map<String, Object> vehicleOrders(Long vehicleId)
    {
        return vehicleAdminService.vehicleOrders(vehicleId);
    }

    public Map<String, Object> vehicleResults(Long vehicleId)
    {
        return vehicleAdminService.vehicleResults(vehicleId);
    }

    public Map<String, Object> owners(String keyword, int pageNum, int pageSize)
    {
        return ownerAdminService.owners(keyword, pageNum, pageSize);
    }

    public Map<String, Object> ownerDetail(Long ownerId)
    {
        return ownerAdminService.ownerDetail(ownerId);
    }

@Transactional
        public Map<String, Object> maintainOwner(Long ownerId, String serviceFlag, String remark, Long userId)
    {
        return ownerAdminService.maintainOwner(ownerId, serviceFlag, remark, userId);
    }

    public Map<String, Object> ownerVehicles(Long ownerId)
    {
        return ownerAdminService.ownerVehicles(ownerId);
    }

    public Map<String, Object> ownerOrders(Long ownerId, int pageNum, int pageSize)
    {
        return ownerAdminService.ownerOrders(ownerId, pageNum, pageSize);
    }

    public Map<String, Object> ownerAfterSales(Long ownerId, int pageNum, int pageSize)
    {
        return ownerAdminService.ownerAfterSales(ownerId, pageNum, pageSize);
    }
}
