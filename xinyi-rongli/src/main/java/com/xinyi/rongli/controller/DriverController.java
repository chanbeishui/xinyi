package com.xinyi.rongli.controller;

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
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.PayMapper;
import com.xinyi.rongli.service.DispatchService;
import com.xinyi.rongli.service.DriverQualificationService;
import com.xinyi.rongli.service.FulfillService;
import com.xinyi.rongli.service.LedgerService;
import com.xinyi.rongli.service.PenaltyService;
import com.xinyi.rongli.service.TrainingService;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 代驾视角：资格与培训、听单与位置、邀约响应、任务执行、证据、对账与申诉。
 */
@RestController
@RequestMapping("/api/app/driver")
public class DriverController
{
    @Autowired
    private DriverQualificationService qualificationService;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private FulfillService fulfillService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private PenaltyService penaltyService;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PayMapper payMapper;

    // ------------------------------------------------------------------ 资格

    @GetMapping("/qualification")
    public Map<String, Object> qualification()
    {
        return qualificationService.myQualification();
    }

    @PostMapping("/qualification")
    public Map<String, Object> createQualification()
    {
        return qualificationService.createDraft();
    }

    @PutMapping("/qualification")
    public Map<String, Object> saveQualification(@RequestBody DriverDtos.QualificationDraft req)
    {
        return qualificationService.saveDraft(req);
    }

    @PostMapping("/qualification/submit")
    public Map<String, Object> submitQualification()
    {
        return qualificationService.submit();
    }

    @PostMapping("/qualification/face-session")
    public Map<String, Object> createFaceSession(@RequestBody(required = false) Map<String, String> body)
    {
        return qualificationService.createFaceSession(body == null ? null : body.get("consentVersion"));
    }

    @PostMapping("/qualification/face-sessions/{verificationSessionId}/mock-pass")
    public Map<String, Object> mockFacePass(@PathVariable String verificationSessionId)
    {
        return qualificationService.completeFaceSessionMock(verificationSessionId);
    }

    @GetMapping("/qualification/face-sessions/{verificationSessionId}")
    public Map<String, Object> faceSession(@PathVariable String verificationSessionId)
    {
        return qualificationService.faceSession(verificationSessionId);
    }

    // ------------------------------------------------------------------ 培训与考试

    @GetMapping("/qualification/training")
    public Map<String, Object> training()
    {
        return trainingService.myTraining();
    }

    @GetMapping("/qualification/training/{trainingId}")
    public Map<String, Object> trainingCourse(@PathVariable Long trainingId)
    {
        return trainingService.courseDetail(trainingId);
    }

    @PostMapping("/qualification/training/{trainingId}/playback")
    public Map<String, Object> startPlayback(@PathVariable Long trainingId)
    {
        return trainingService.startPlayback(trainingId);
    }

    @PostMapping("/qualification/training/{trainingId}/progress")
    public Map<String, Object> reportProgress(@PathVariable Long trainingId,
            @RequestBody DriverDtos.ProgressReq req)
    {
        return trainingService.reportProgress(trainingId, req);
    }

    @PostMapping("/qualification/training/{trainingId}/attempts")
    public Map<String, Object> createAttempt(@PathVariable Long trainingId)
    {
        return trainingService.createAttempt(trainingId);
    }

    @GetMapping("/qualification/training/attempts/{attemptId}")
    public Map<String, Object> attempt(@PathVariable Long attemptId)
    {
        return trainingService.attemptDetail(attemptId);
    }

    @PostMapping("/qualification/training/submit")
    public Map<String, Object> submitAttempt(@RequestBody DriverDtos.AttemptSubmitReq req)
    {
        return trainingService.submitAttempt(req);
    }

    // ------------------------------------------------------------------ 听单与位置

    @GetMapping("/availability")
    public Map<String, Object> availability()
    {
        return qualificationService.availability();
    }

    @PatchMapping("/listening")
    public Map<String, Object> listening(@RequestBody DriverDtos.ListeningReq req)
    {
        return qualificationService.setListening(req == null ? null : req.enabled);
    }

    /** 小程序端 uni.request 不支持 PATCH，提供等价的 POST 入口 */
    @PostMapping("/listening")
    public Map<String, Object> listeningByPost(@RequestBody DriverDtos.ListeningReq req)
    {
        return qualificationService.setListening(req == null ? null : req.enabled);
    }

    @PostMapping("/location")
    public Map<String, Object> location(@RequestBody DriverDtos.LocationReq req)
    {
        return qualificationService.reportLocation(req);
    }

    // ------------------------------------------------------------------ 邀约

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

    // ------------------------------------------------------------------ 任务

    @GetMapping("/assignments/current")
    public Map<String, Object> currentAssignment()
    {
        return fulfillService.currentAssignment();
    }

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

    @Autowired
    private com.xinyi.rongli.service.StationService stationService;

    // ------------------------------------------------------------------ 证据

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

    // ------------------------------------------------------------------ 对账

    @GetMapping("/statements")
    public Map<String, Object> statements()
    {
        return ledgerService.driverStatements();
    }

    @GetMapping("/statements/{statementId}")
    public Map<String, Object> statement(@PathVariable Long statementId)
    {
        return ledgerService.myStatementDetail(statementId);
    }

    @Autowired
    private com.xinyi.rongli.mapper.LedgerMapper ledgerMapperRef;

    @PostMapping("/statements/{statementId}/confirm")
    public Map<String, Object> confirmStatement(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        Integer revision = body == null || body.get("revision") == null ? null
                : Integer.valueOf(String.valueOf(body.get("revision")));
        String hash = body == null || body.get("snapshotHash") == null ? null
                : String.valueOf(body.get("snapshotHash"));
        if (revision == null)
        {
            throw ApiException.validation("请提供账单版本号");
        }
        return ledgerService.confirmStatement(statementId, revision, hash);
    }

    @PostMapping("/statements/{statementId}/differences")
    public Map<String, Object> submitDifference(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        String description = body == null || body.get("description") == null ? null
                : String.valueOf(body.get("description"));
        String entryIds = body == null || body.get("entryIds") == null ? null
                : String.valueOf(body.get("entryIds"));
        return ledgerService.submitDifference(statementId, description, entryIds);
    }

    @GetMapping("/reconciliation-entries")
    public Map<String, Object> reconciliationEntries(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String keyword)
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        List<Map<String, Object>> rows = ledgerService.entries(driver.lng("id"), from, to, keyword, true);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }

    @GetMapping("/payment-records")
    public Map<String, Object> paymentRecords()
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        List<Map<String, Object>> rows = ledgerMapperRef.listPaymentsOfDriver(driver.lng("id"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }

    // ------------------------------------------------------------------ 处罚与申诉

    @GetMapping("/penalties")
    public Map<String, Object> penalties(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return penaltyService.myPenalties(pageNum, pageSize);
    }

    @GetMapping("/penalties/{penaltyId}")
    public Map<String, Object> penalty(@PathVariable Long penaltyId)
    {
        return penaltyService.myPenalty(penaltyId);
    }

    @PostMapping("/penalties/{penaltyId}/appeals")
    public Map<String, Object> appeal(@PathVariable Long penaltyId,
            @RequestBody DriverDtos.AppealReq req)
    {
        return penaltyService.submitAppeal(penaltyId, req == null ? null : req.reason,
                req == null ? null : req.evidenceFileIds);
    }

    @GetMapping("/penalty-appeals")
    public Map<String, Object> appeals(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return penaltyService.myAppeals(pageNum, pageSize);
    }

    @GetMapping("/penalty-appeals/{appealId}")
    public Map<String, Object> appeal(@PathVariable String appealId)
    {
        return penaltyService.myAppeal(Long.valueOf(appealId));
    }

    // ------------------------------------------------------------------ 我的账户

    @GetMapping("/profile")
    public Map<String, Object> profile()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("accountId", String.valueOf(p.accountId));
        view.put("driverId", driver.isEmpty() ? null : String.valueOf(driver.lng("id")));
        view.put("nickname", Row.of(accountMapper.findAccount(p.accountId)).str("nickname"));
        view.put("qualificationStatus", driver.str("qualification_status"));
        view.put("listeningEnabled", driver.bool("listening_enabled"));
        view.put("newAssignmentEnabled", driver.bool("new_assignment_enabled"));
        return view;
    }
}
