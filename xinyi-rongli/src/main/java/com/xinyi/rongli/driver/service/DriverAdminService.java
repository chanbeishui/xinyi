package com.xinyi.rongli.driver.service;

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
import com.xinyi.rongli.driver.mapper.AdminDriverMapper;

/**
 * 管理端 driver 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class DriverAdminService
{
    @Autowired
    private AdminDriverMapper adminDriverMapper;
    @Autowired
    private DispatchMapper dispatchMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private EventService eventService;
    @Autowired
    private Crypto crypto;
    @Autowired
    private DeptScope deptScope;

    public Map<String, Object> qualifications(String status, int pageNum, int pageSize)
    {
        // 不传 / 传 ALL 表示“全部状态”，不能退回 SUBMITTED——
        // 否则运营选「全部状态」看到的仍是待审核结果（已通过的申请一条都不显示）
        String filter = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status) ? null : status;
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> all = accountMapper.listApplicationsByStatus(filter, 500);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = offset; i < all.size() && rows.size() < limit; i++)
        {
            Row a = Row.of(all.get(i));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("applicationId", String.valueOf(a.lng("id")));
            item.put("driverId", String.valueOf(a.lng("driver_id")));
            item.put("accountId", String.valueOf(a.lng("account_id")));
            item.put("nickname", a.str("nickname"));
            item.put("status", a.str("status"));
            item.put("qualificationStatus", a.str("qualification_status"));
            item.put("licenseClass", a.str("license_class"));
            item.put("licenseValidTo", a.str("license_valid_to"));
            item.put("submittedAt", ViTime.iso(a.time("submitted_at")));
            item.put("reviewedAt", ViTime.iso(a.time("reviewed_at")));
            item.put("reviewOpinion", a.str("review_opinion"));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", all.size());
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> qualificationDetail(Long applicationId)
    {
        Row a = Row.of(accountMapper.findApplication(applicationId));
        if (a.isEmpty())
        {
            throw ApiException.notFound("资格申请不存在");
        }
        Row driver = Row.of(accountMapper.findDriverById(a.lng("driver_id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("applicationId", String.valueOf(applicationId));
        view.put("driverId", String.valueOf(a.lng("driver_id")));
        view.put("status", a.str("status"));
        view.put("submittedAt", ViTime.iso(a.time("submitted_at")));
        view.put("reviewedAt", ViTime.iso(a.time("reviewed_at")));
        view.put("reviewOpinion", a.str("review_opinion"));
        view.put("evidenceFileIds", a.json("evidence_file_ids"));
        view.put("driverName", driver.str("nickname"));
        view.put("licenseClass", driver.str("license_class"));
        view.put("licenseValidTo", driver.str("license_valid_to"));
        view.put("qualificationStatus", driver.str("qualification_status"));
        String plain = crypto.decrypt((byte[]) a.get("sensitive_payload_cipher"));
        if (plain != null)
        {
            JSONObject payload = Json.read(plain);
            Map<String, Object> masked = new LinkedHashMap<>();
            masked.put("realName", mask(payload.getString("realName"), 1, 0));
            masked.put("idCardNo", mask(payload.getString("idCardNo"), 4, 4));
            masked.put("licenseClass", payload.getString("licenseClass"));
            view.put("sensitive", masked);
            view.put("sensitiveHint", "管理端只展示脱敏信息，原件在独立档案中受控访问");
        }
        List<Map<String, Object>> courses = new ArrayList<>();
        for (Map<String, Object> p : adminDriverMapper.listProgressOfApplication(applicationId))
        {
            Row pr = Row.of(p);
            courses.add(Map.of("trainingId", String.valueOf(pr.lng("training_id")),
                    "verifiedSeconds", pr.intg("verified_duration_seconds", 0),
                    "status", pr.str("status")));
        }
        view.put("training", courses);
        return view;
    }
    @Transactional
    public Map<String, Object> auditQualification(Long applicationId, boolean approve, String opinion, Long userId)
    {
        Row a = Row.of(accountMapper.findApplication(applicationId));
        if (a.isEmpty())
        {
            throw ApiException.notFound("资格申请不存在");
        }
        if (!"SUBMITTED".equals(a.str("status")))
        {
            throw ApiException.stateConflict("该申请不在待审核状态");
        }
        if (!approve && (opinion == null || opinion.isBlank()))
        {
            throw ApiException.validation("驳回必须写明理由，代驾会看到该理由");
        }
        String status = approve ? "APPROVED" : "REJECTED";
        if (accountMapper.auditApplication(applicationId, status, userId, opinion) == 0)
        {
            throw ApiException.stateConflict("申请状态已变化，请刷新");
        }
        Long driverId = a.lng("driver_id");
        if (approve)
        {
            // 资格通过：递增资格版本、写入下次复审到期时间（12 个月），
            // 并同时开启听单——审核通过即进入可派单状态，不必等代驾再手动上线
            long revision = Row.of(accountMapper.findDriverById(driverId)).lng("qualification_revision", 0L) + 1;
            accountMapper.approveDriver(driverId, revision, ViTime.now().plusMonths(12));
        }
        else
        {
            accountMapper.updateDriverQualificationStatus(driverId, status);
        }
        Row driver = Row.of(accountMapper.findDriverById(driverId));
        eventService.record("DRIVER", driverId, "QUALIFICATION_" + status, null,
                Map.of("applicationId", String.valueOf(applicationId)),
                approve ? Map.of("status", status, "listeningEnabled", "true",
                        "newAssignmentEnabled", "true") : Map.of("status", status), opinion);
        eventService.notify(driver.lng("account_id"), Codes.Role.DRIVER, null, "QUALIFICATION_" + status,
                approve ? "您的代驾资格已通过审核，已为您开启接单，可在首页随时关闭听单"
                        : "您的代驾资格审核未通过：" + opinion);
        return qualificationDetail(applicationId);
    }
    public Map<String, Object> drivers(String keyword, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        List<Long> deptIds = deptScope.visibleDeptIds();
        view.put("total", vehicleMapper.adminDriverCount(keyword, status, deptIds));
        view.put("rows", vehicleMapper.adminDriverList(keyword, status, deptIds, offset, limit));
        return view;
    }
    public Map<String, Object> driverDetail(Long driverId)
    {
        Row d = Row.of(vehicleMapper.adminDriverDetail(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(d.map());
        // phone_cipher / open_id 不能直接回给前端：前者是密文（byte[] 会被序列化成 base64），
        // 后者是登录标识。手机号只给脱敏值。
        Object cipher = view.remove("phone_cipher");
        view.remove("open_id");
        view.put("phoneMasked", maskPhone(decryptText(cipher)));
        view.put("phoneVerifiedAt", ViTime.iso(d.time("phone_verified_at")));

        Row progress = Row.of(accountMapper.findLatestApplication(driverId));
        view.put("latestApplicationId", progress.isEmpty() ? null : String.valueOf(progress.lng("id")));
        view.put("latestApplicationStatus", progress.isEmpty() ? null : progress.str("status"));
        view.put("latestApplicationStatusLabel", progress.isEmpty() ? null : qualificationLabel(progress.str("status")));
        view.put("latestApplicationSubmittedAt", progress.isEmpty() ? null : ViTime.iso(progress.time("submitted_at")));
        view.put("latestApplicationReviewedAt", progress.isEmpty() ? null : ViTime.iso(progress.time("reviewed_at")));
        view.put("latestApplicationOpinion", progress.isEmpty() ? null : progress.str("review_opinion"));

        // 管理端统一给一份 camelCase 视图，前端不必再猜 snake_case / camelCase
        view.put("driverId", String.valueOf(driverId));
        view.put("accountId", d.get("account_id") == null ? null : String.valueOf(d.lng("account_id")));
        view.put("nickname", d.str("nickname"));
        view.put("accountStatus", d.str("status"));
        view.put("qualificationStatus", d.str("qualification_status"));
        view.put("qualificationLabel", qualificationLabel(d.str("qualification_status")));
        view.put("listeningEnabled", d.bool("listening_enabled"));
        view.put("newAssignmentEnabled", d.bool("new_assignment_enabled"));
        view.put("licenseClass", d.str("license_class"));
        view.put("licenseValidTo", d.date("license_valid_to") == null ? null : d.date("license_valid_to").toString());
        view.put("licenseLongTerm", d.bool("license_long_term"));
        view.put("reviewDueAt", ViTime.iso(d.time("review_due_at")));
        view.put("qualificationRevision", d.lng("qualification_revision", 0L));
        view.put("deptId", d.get("dept_id") == null ? null : String.valueOf(d.lng("dept_id")));
        view.put("deptName", d.str("dept_name"));
        view.put("internalRemark", d.str("internal_remark"));
        view.put("createdAt", ViTime.iso(d.time("created_at")));
        view.put("updatedAt", ViTime.iso(d.time("updated_at")));
        view.put("accountCreatedAt", ViTime.iso(d.time("account_created_at")));
        view.put("lastLocationAt", ViTime.iso(d.time("last_location_at")));
        view.put("lastAssignmentAt", ViTime.iso(d.time("last_assignment_at")));
        view.put("activeAssignments", d.intg("active_assignments", 0));
        view.put("totalAssignments", d.intg("total_assignments", 0));
        // 派单只看这四个条件（不要求定位），详情里直接算给运营看，省得来回猜
        view.put("canReceive", "APPROVED".equals(d.str("qualification_status"))
                && d.bool("listening_enabled") && d.bool("new_assignment_enabled")
                && "ACTIVE".equals(d.str("status")));

        view.put("recentAssignments", dispatchMapper.listAssignmentsOfDriver(driverId, null, 0, 10));
        return view;
    }
    @Transactional
    public Map<String, Object> maintainDriver(Long driverId, String remark, Boolean newAssignmentEnabled,
            Long userId)
    {
        Row d = Row.of(vehicleMapper.adminDriverDetail(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        if (remark != null)
        {
            accountMapper.updateDriverRemark(driverId, remark);
        }
        if (newAssignmentEnabled != null)
        {
            accountMapper.updateNewAssignmentEnabled(driverId, newAssignmentEnabled);
        }
        eventService.record("DRIVER", driverId, "DRIVER_MAINTAINED", null,
                Map.of("newAssignmentEnabled", String.valueOf(d.bool("new_assignment_enabled"))),
                Map.of("newAssignmentEnabled", String.valueOf(newAssignmentEnabled)), remark);
        return driverDetail(driverId);
    }
    @Transactional
    public Map<String, Object> disableNewAssignments(Long driverId, boolean disabled, String reason, Long userId)
    {
        Row d = Row.of(accountMapper.findDriverById(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写原因，该原因会留痕");
        }
        accountMapper.updateNewAssignmentEnabled(driverId, !disabled);
        eventService.record("DRIVER", driverId, disabled ? "NEW_ASSIGNMENT_DISABLED" : "NEW_ASSIGNMENT_ENABLED",
                null, Map.of("enabled", String.valueOf(d.bool("new_assignment_enabled"))),
                Map.of("enabled", String.valueOf(!disabled)), reason);
        Row account = Row.of(accountMapper.findDriverById(driverId));
        eventService.notify(account.lng("account_id"), Codes.Role.DRIVER, null,
                disabled ? "NEW_ASSIGNMENT_DISABLED" : "NEW_ASSIGNMENT_ENABLED",
                disabled ? "运营已暂停您接收新邀约：" + reason : "运营已恢复您接收新邀约");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("driverId", String.valueOf(driverId));
        view.put("newAssignmentEnabled", !disabled);
        view.put("hint", disabled ? "该代驾不会再收到新邀约，在手任务不受影响" : "已恢复派单资格");
        return view;
    }
    public Map<String, Object> driverAssignments(Long driverId, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", dispatchMapper.listAssignmentsOfDriver(driverId, null, offset, limit));
        view.put("total", dispatchMapper.countAssignmentsOfDriver(driverId, null));
        return view;
    }
    private String mask(String text, int keepLeft, int keepRight)
    {
        if (text == null || text.length() <= keepLeft + keepRight)
        {
            return text == null ? null : "***";
        }
        return text.substring(0, keepLeft) + "*".repeat(Math.max(1, text.length() - keepLeft - keepRight))
                + text.substring(text.length() - keepRight);
    }

    /** 申请单状态 → 中文；与小程序、审核列表保持一致 */
    private String qualificationLabel(String status)
    {
        if (status == null)
        {
            return null;
        }
        return switch (status)
        {
            case "DRAFT" -> "资料待完善";
            case "SUBMITTED" -> "待审核";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            case "EXPIRED" -> "已过期";
            default -> status;
        };
    }

    /** 手机号密文解密；密钥不可用或数据损坏时返回 null，不影响详情其它字段 */
    private String decryptText(Object cipher)
    {
        if (!(cipher instanceof byte[] bytes) || bytes.length == 0)
        {
            return null;
        }
        try
        {
            return crypto.decrypt(bytes);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    private String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return "未绑定";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
