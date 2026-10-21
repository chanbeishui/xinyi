package com.xinyi.rongli.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 代驾资格、听单状态、位置打点与实名活体核验。
 *
 * <p>资格与听单是两件事：资格审核通过只代表可以接单，是否真正接收邀约还取决于听单开关、
 * 是否被暂停新承接、是否有在手任务、位置是否新鲜。任何一个不满足都不会被派单。</p>
 */
@Service
public class DriverQualificationService
{
    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    // ------------------------------------------------------------------ 资格草稿

    public Map<String, Object> myQualification()
    {
        Row driver = requireOrCreateDriver();
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("driverId", String.valueOf(driver.lng("id")));
        view.put("qualificationStatus", driver.str("qualification_status"));
        view.put("newAssignmentEnabled", driver.bool("new_assignment_enabled"));
        view.put("listeningEnabled", driver.bool("listening_enabled"));
        view.put("licenseClass", driver.str("license_class"));
        view.put("licenseValidTo", driver.date("license_valid_to") == null ? null
                : driver.date("license_valid_to").toString());
        view.put("reviewDueAt", ViTime.iso(driver.time("review_due_at")));
        if (!application.isEmpty())
        {
            view.put("applicationId", String.valueOf(application.lng("id")));
            view.put("applicationStatus", application.str("status"));
            view.put("applicationStatusLabel", statusLabel(application.str("status")));
            view.put("submittedAt", ViTime.iso(application.time("submitted_at")));
            view.put("reviewedAt", ViTime.iso(application.time("reviewed_at")));
            view.put("reviewOpinion", application.str("review_opinion"));
            view.put("draft", maskedPayload(application));
        }
        view.put("canEdit", application.isEmpty() || "DRAFT".equals(application.str("status"))
                || "REJECTED".equals(application.str("status")));
        view.put("canSubmit", !application.isEmpty() && "DRAFT".equals(application.str("status")));
        return view;
    }

    private Map<String, Object> maskedPayload(Row application)
    {
        String plain = crypto.decrypt((byte[]) application.get("sensitive_payload_cipher"));
        if (plain == null)
        {
            return Map.of();
        }
        JSONObject payload = Json.read(plain);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("realName", payload.getString("realName"));
        view.put("idCardNo", maskIdCard(payload.getString("idCardNo")));
        view.put("licenseClass", payload.getString("licenseClass"));
        view.put("licenseValidTo", payload.getString("licenseValidTo"));
        view.put("licenseLongTerm", payload.getBoolean("licenseLongTerm"));
        view.put("evidenceFileIds", application.json("evidence_file_ids"));
        return view;
    }

    private String maskIdCard(String id)
    {
        if (id == null || id.length() < 8)
        {
            return id == null ? null : "****";
        }
        return id.substring(0, 4) + "**********" + id.substring(id.length() - 4);
    }

    private String statusLabel(String status)
    {
        if (status == null)
        {
            return null;
        }
        return switch (status)
        {
            case "DRAFT" -> "草稿";
            case "SUBMITTED" -> "待审核";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            case "EXPIRED" -> "已过期";
            case "SUSPENDED" -> "已暂停";
            default -> status;
        };
    }

    private Row requireOrCreateDriver()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (!driver.isEmpty())
        {
            return driver;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("accountId", p.accountId);
        row.put("licenseClass", null);
        row.put("licenseValidTo", null);
        row.put("licenseLongTerm", false);
        accountMapper.insertDriverProfile(row);
        Row created = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (created.isEmpty())
        {
            throw ApiException.stateConflict("代驾档案创建失败，请重试");
        }
        return created;
    }

    private Row requireDriver()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("请先创建代驾资格申请");
        }
        return driver;
    }

    @Transactional
    public Map<String, Object> createDraft()
    {
        Row driver = requireOrCreateDriver();
        Row existing = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        if (!existing.isEmpty() && !"DRAFT".equals(existing.str("status")))
        {
            throw ApiException.stateConflict("已有提交中的申请，请等待审核结果");
        }
        if (!existing.isEmpty())
        {
            return myQualification();
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("driverId", driver.lng("id"));
        row.put("status", "DRAFT");
        row.put("payload", crypto.encrypt("{}"));
        row.put("evidence", null);
        row.put("submittedAt", null);
        accountMapper.insertApplication(row);
        eventService.record("DRIVER", driver.lng("id"), "QUALIFICATION_DRAFT_CREATED", null);
        return myQualification();
    }

    @Transactional
    public Map<String, Object> saveDraft(DriverDtos.QualificationDraft req)
    {
        if (req == null || req.realName == null || req.realName.isBlank())
        {
            throw ApiException.validation("请填写真实姓名");
        }
        if (req.idCardNo == null || !req.idCardNo.matches("\\d{17}[\\dXx]"))
        {
            throw ApiException.validation("身份证号格式不正确");
        }
        if (req.licenseClass == null || req.licenseClass.isBlank())
        {
            throw ApiException.validation("请填写准驾车型");
        }
        Row driver = requireOrCreateDriver();
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        if (application.isEmpty())
        {
            createDraft();
            application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        }
        if (!"DRAFT".equals(application.str("status")))
        {
            throw ApiException.stateConflict("当前申请不可编辑，请等待审核结果");
        }
        JSONObject payload = new JSONObject();
        payload.put("realName", req.realName.trim());
        payload.put("idCardNo", req.idCardNo.trim().toUpperCase());
        payload.put("licenseClass", req.licenseClass.trim().toUpperCase());
        payload.put("licenseValidTo", req.licenseValidTo);
        payload.put("licenseLongTerm", Boolean.TRUE.equals(req.licenseLongTerm));
        accountMapper.updateApplicationDraft(application.lng("id"), crypto.encrypt(payload.toJSONString()),
                req.evidenceFileIds);
        if (req.licenseValidTo != null && !Boolean.TRUE.equals(req.licenseLongTerm))
        {
            LocalDate validTo;
            try
            {
                validTo = LocalDate.parse(req.licenseValidTo.substring(0, 10));
            }
            catch (Exception e)
            {
                throw ApiException.validation("驾驶证有效期格式应为 yyyy-MM-dd");
            }
            if (validTo.isBefore(ViTime.shanghaiDate(ViTime.now())))
            {
                throw ApiException.validation("驾驶证已过期，无法提交审核");
            }
        }
        return myQualification();
    }

    @Transactional
    public Map<String, Object> submit()
    {
        Row driver = requireOrCreateDriver();
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        if (application.isEmpty())
        {
            throw ApiException.stateConflict("请先填写资格信息");
        }
        if (!"DRAFT".equals(application.str("status")))
        {
            throw ApiException.stateConflict("该申请已提交，请勿重复提交");
        }
        String plain = crypto.decrypt((byte[]) application.get("sensitive_payload_cipher"));
        JSONObject payload = Json.read(plain);
        if (payload.getString("realName") == null || payload.getString("idCardNo") == null)
        {
            throw ApiException.validation("请先补全姓名与身份证号");
        }
        if (application.json("evidence_file_ids") == null || application.json("evidence_file_ids").isBlank())
        {
            throw ApiException.validation("请上传驾驶证与身份证照片后再提交");
        }
        if (accountMapper.submitApplication(application.lng("id")) == 0)
        {
            throw ApiException.stateConflict("提交失败，请刷新后重试");
        }
        accountMapper.updateDriverQualificationStatus(driver.lng("id"), "SUBMITTED");
        eventService.record("DRIVER", driver.lng("id"), "QUALIFICATION_SUBMITTED", null,
                Map.of("applicationId", String.valueOf(application.lng("id"))), null, null);
        eventService.publish("DRIVER", driver.lng("id"), "QUALIFICATION_SUBMITTED",
                Map.of("applicationId", String.valueOf(application.lng("id"))));
        return myQualification();
    }

    // ------------------------------------------------------------------ 实名活体

    @Transactional
    public Map<String, Object> createFaceSession(String consentVersion)
    {
        Row driver = requireDriver();
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        if (application.isEmpty())
        {
            throw ApiException.stateConflict("请先建立资格申请");
        }
        MiniPrincipal p = MiniContext.require();
        String verificationId = "FS" + crypto.randomDigits(12);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("applicationId", application.lng("id"));
        row.put("verificationId", verificationId);
        row.put("accountId", p.accountId);
        row.put("launchMode", "WEB_H5");
        row.put("launchPayloadCipher", crypto.encrypt(JSONObject.of("verificationId", verificationId).toJSONString()));
        row.put("consentVersion", consentVersion == null ? "v1" : consentVersion);
        row.put("expiresAt", ViTime.now().plusMinutes(15));
        adminMapper.insertFaceSession(row);
        eventService.record("DRIVER", driver.lng("id"), "FACE_SESSION_CREATED", null,
                Map.of("verificationId", verificationId), null, "创建实名活体核验会话");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("verificationSessionId", verificationId);
        view.put("launchMode", "WEB_H5");
        view.put("expiresAt", ViTime.iso((java.time.LocalDateTime) row.get("expiresAt")));
        view.put("consentVersion", row.get("consentVersion"));
        view.put("mock", true);
        view.put("hint", "当前为联调环境：请在小程序内点击“模拟完成核验”，生产环境将跳转实名供应商页面");
        return view;
    }

    /** 联调环境模拟核验通过；真实环境应由服务端回调供应商结果 */
    @Transactional
    public Map<String, Object> completeFaceSessionMock(String verificationId)
    {
        Row session = Row.of(adminMapper.findFaceSession(verificationId));
        if (session.isEmpty())
        {
            throw ApiException.notFound("核验会话不存在");
        }
        MiniPrincipal p = MiniContext.require();
        if (!Objects.equals(p.accountId, session.lng("account_id")))
        {
            throw ApiException.forbidden("无权操作该核验会话");
        }
        if (session.time("expires_at") != null && session.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("核验会话已过期，请重新发起");
        }
        adminMapper.updateFaceResult(session.lng("id"), true, "PASSED", null);
        Row driver = requireDriver();
        eventService.record("DRIVER", driver.lng("id"), "FACE_VERIFIED", null,
                Map.of("verificationId", verificationId), null, "联调环境模拟核验通过");
        return faceSessionView(Row.of(adminMapper.findFaceSession(verificationId)));
    }

    public Map<String, Object> faceSession(String verificationId)
    {
        Row session = Row.of(adminMapper.findFaceSession(verificationId));
        if (session.isEmpty())
        {
            throw ApiException.notFound("核验会话不存在");
        }
        MiniPrincipal p = MiniContext.require();
        if (!Objects.equals(p.accountId, session.lng("account_id")))
        {
            throw ApiException.forbidden("无权查看该核验会话");
        }
        return faceSessionView(session);
    }

    private Map<String, Object> faceSessionView(Row session)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("verificationSessionId", session.str("verification_id"));
        view.put("status", session.str("status"));
        view.put("launchMode", session.str("launch_mode"));
        view.put("resultAvailable", session.bool("result_available"));
        view.put("failureCode", session.str("failure_code"));
        view.put("expiresAt", ViTime.iso(session.time("expires_at")));
        view.put("hint", session.bool("result_available") && session.str("failure_code") == null
                ? "实名核验已通过" : "核验结果以服务端查询为准，客户端回调不作为凭据");
        return view;
    }

    // ------------------------------------------------------------------ 听单与位置

    public Map<String, Object> availability()
    {
        Row driver = requireDriver();
        Row occupancy = Row.of(dispatchMapper.findOccupancy(driver.lng("id")));
        Row location = Row.of(accountMapper.findLocation(driver.lng("id")));
        boolean locationFresh = !location.isEmpty() && location.time("captured_at") != null
                && !location.time("captured_at").isBefore(ViTime.now().minusSeconds(config.locationFreshSeconds()));
        boolean qualified = "APPROVED".equals(driver.str("qualification_status"));
        boolean busy = !occupancy.isEmpty();
        boolean canReceive = qualified && driver.bool("listening_enabled")
                && driver.bool("new_assignment_enabled") && !busy;

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("qualificationStatus", driver.str("qualification_status"));
        view.put("listeningEnabled", driver.bool("listening_enabled"));
        view.put("newAssignmentEnabled", driver.bool("new_assignment_enabled"));
        view.put("onTask", busy);
        view.put("locationFresh", locationFresh);
        view.put("locationUpdatedAt", ViTime.iso(location.time("captured_at")));
        view.put("canReceiveInvite", canReceive);
        List<String> blockers = new java.util.ArrayList<>();
        if (!qualified)
        {
            blockers.add("资格未通过审核");
        }
        if (!driver.bool("listening_enabled"))
        {
            blockers.add("听单已关闭");
        }
        if (!driver.bool("new_assignment_enabled"))
        {
            blockers.add("运营已暂停接收新邀约");
        }
        if (busy)
        {
            blockers.add("当前有在手任务，归还后自动释放");
        }
        if (!locationFresh)
        {
            blockers.add("位置已过期，请重新授权定位");
        }
        view.put("blockers", blockers);
        view.put("locationFreshSeconds", config.locationFreshSeconds());
        return view;
    }

    @Transactional
    public Map<String, Object> setListening(Boolean enabled)
    {
        Row driver = requireDriver();
        if (enabled == null)
        {
            throw ApiException.validation("请指定听单开关");
        }
        if (enabled && !"APPROVED".equals(driver.str("qualification_status")))
        {
            throw ApiException.stateConflict("资格未通过审核，暂时不能开启听单");
        }
        accountMapper.updateListening(driver.lng("id"), enabled);
        eventService.record("DRIVER", driver.lng("id"), enabled ? "LISTENING_ON" : "LISTENING_OFF", null,
                Map.of("listening", String.valueOf(driver.bool("listening_enabled"))),
                Map.of("listening", String.valueOf(enabled)), null);
        return availability();
    }

    @Transactional
    public Map<String, Object> reportLocation(DriverDtos.LocationReq req)
    {
        Row driver = requireDriver();
        if (req == null || req.lng == null || req.lat == null)
        {
            throw ApiException.validation("请提供经纬度");
        }
        if (req.lng < -180 || req.lng > 180 || req.lat < -90 || req.lat > 90)
        {
            throw ApiException.validation("经纬度超出有效范围");
        }
        java.time.LocalDateTime capturedAt = req.capturedAt == null ? ViTime.now()
                : ViTime.parseFlexible(req.capturedAt);
        if (capturedAt.isAfter(ViTime.now().plusMinutes(2)))
        {
            capturedAt = ViTime.now();
        }
        JSONObject location = new JSONObject();
        location.put("lng", req.lng);
        location.put("lat", req.lat);
        location.put("capturedAt", ViTime.iso(capturedAt));
        accountMapper.upsertLocation(driver.lng("id"), crypto.encrypt(location.toJSONString()), capturedAt);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("accepted", true);
        view.put("capturedAt", ViTime.iso(capturedAt));
        view.put("freshSeconds", config.locationFreshSeconds());
        return view;
    }
}
