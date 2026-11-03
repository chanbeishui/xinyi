package com.xinyi.rongli.driver.controller;

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
 * driver 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/app/driver")
public class DriverAppController
{
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private PenaltyService penaltyService;
    @Autowired
    private DriverQualificationService qualificationService;
    @Autowired
    private TrainingService trainingService;

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
