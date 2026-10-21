package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 取消处罚与停单申诉。
 *
 * <p>处罚由系统按"跳轮/停单"规则自动产生；申诉是代驾唯一的人工救济通道，
 * 处理结论必须公示（public_opinion），且只有申诉通过才会撤销处罚。</p>
 */
@Service
public class PenaltyService
{
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private EventService eventService;

    // ------------------------------------------------------------------ 代驾侧

    public Map<String, Object> myPenalties(int pageNum, int pageSize)
    {
        Row driver = requireDriver();
        int limit = Math.min(Math.max(pageSize, 1), 50);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> p : adminMapper.listPenalties(driver.lng("id"), null, offset, limit))
        {
            rows.add(penaltyView(Row.of(p), false));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countPenalties(driver.lng("id"), null));
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> myPenalty(Long penaltyId)
    {
        Row driver = requireDriver();
        Row p = Row.of(miscMapper.findPenalty(penaltyId));
        if (p.isEmpty() || !Objects.equals(driver.lng("id"), p.lng("driver_id")))
        {
            throw ApiException.notFound("处罚记录不存在");
        }
        Map<String, Object> view = penaltyView(p, false);
        List<Map<String, Object>> appeals = new ArrayList<>();
        for (Map<String, Object> a : adminMapper.listAppeals(driver.lng("id"), null, 0, 50))
        {
            Row ap = Row.of(a);
            if (Objects.equals(ap.lng("penalty_id"), penaltyId))
            {
                appeals.add(appealView(ap, false));
            }
        }
        view.put("appeals", appeals);
        view.put("canAppeal", "ACTIVE".equals(p.str("status")) && appeals.stream()
                .noneMatch(a -> "SUBMITTED".equals(a.get("status"))));
        return view;
    }

    @Transactional
    public Map<String, Object> submitAppeal(Long penaltyId, String reason, String evidenceFileIds)
    {
        Row driver = requireDriver();
        Row p = Row.of(miscMapper.findPenalty(penaltyId));
        if (p.isEmpty() || !Objects.equals(driver.lng("id"), p.lng("driver_id")))
        {
            throw ApiException.notFound("处罚记录不存在");
        }
        if (!"ACTIVE".equals(p.str("status")))
        {
            throw ApiException.stateConflict("该处罚已失效，无需申诉");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写申诉理由");
        }
        if (reason.length() > 1000)
        {
            throw ApiException.validation("申诉理由不能超过 1000 字");
        }
        for (Map<String, Object> a : adminMapper.listAppeals(driver.lng("id"), "SUBMITTED", 0, 50))
        {
            if (Objects.equals(Row.of(a).lng("penalty_id"), penaltyId))
            {
                throw ApiException.stateConflict("该处罚已有处理中的申诉");
            }
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("penaltyId", penaltyId);
        row.put("driverId", driver.lng("id"));
        row.put("reason", reason);
        row.put("evidenceFileIds", evidenceFileIds);
        miscMapper.insertAppeal(row);
        Long appealId = ((Number) row.get("id")).longValue();
        eventService.record("PENALTY", penaltyId, "APPEAL_SUBMITTED", null, null,
                Map.of("appealId", String.valueOf(appealId)), reason);
        return appealView(Row.of(adminMapper.findAppealDetail(appealId)), false);
    }

    public Map<String, Object> myAppeals(int pageNum, int pageSize)
    {
        Row driver = requireDriver();
        int limit = Math.min(Math.max(pageSize, 1), 50);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> a : adminMapper.listAppeals(driver.lng("id"), null, offset, limit))
        {
            rows.add(appealView(Row.of(a), false));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countAppeals(driver.lng("id"), null));
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> myAppeal(Long appealId)
    {
        Row driver = requireDriver();
        Row ap = Row.of(adminMapper.findAppealDetail(appealId));
        if (ap.isEmpty() || !Objects.equals(driver.lng("id"), ap.lng("driver_id")))
        {
            throw ApiException.notFound("申诉记录不存在");
        }
        return appealView(ap, false);
    }

    // ------------------------------------------------------------------ 管理端

    public Map<String, Object> adminPenalties(Long driverId, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> p : adminMapper.listPenalties(driverId, status, offset, limit))
        {
            rows.add(penaltyView(Row.of(p), true));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countPenalties(driverId, status));
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> adminPenalty(Long penaltyId)
    {
        Row p = Row.of(miscMapper.findPenalty(penaltyId));
        if (p.isEmpty())
        {
            throw ApiException.notFound("处罚记录不存在");
        }
        Map<String, Object> view = penaltyView(p, true);
        List<Map<String, Object>> appeals = new ArrayList<>();
        for (Map<String, Object> a : adminMapper.listAppeals(p.lng("driver_id"), null, 0, 50))
        {
            Row ap = Row.of(a);
            if (Objects.equals(ap.lng("penalty_id"), penaltyId))
            {
                appeals.add(appealView(ap, true));
            }
        }
        view.put("appeals", appeals);
        return view;
    }

    public Map<String, Object> adminAppeals(Long driverId, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> a : adminMapper.listAppeals(driverId, status, offset, limit))
        {
            rows.add(appealView(Row.of(a), true));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countAppeals(driverId, status));
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> adminAppeal(Long appealId)
    {
        Row ap = Row.of(adminMapper.findAppealDetail(appealId));
        if (ap.isEmpty())
        {
            throw ApiException.notFound("申诉记录不存在");
        }
        return appealView(ap, true);
    }

    /**
     * 申诉审核：通过即撤销处罚，驳回必须给出公示结论。
     */
    @Transactional
    public Map<String, Object> auditAppeal(Long appealId, boolean approve, String opinion, Long reviewerUserId)
    {
        Row ap = Row.of(adminMapper.findAppealDetail(appealId));
        if (ap.isEmpty())
        {
            throw ApiException.notFound("申诉记录不存在");
        }
        if (!"SUBMITTED".equals(ap.str("status")))
        {
            throw ApiException.stateConflict("该申诉已处理");
        }
        if (opinion == null || opinion.isBlank())
        {
            throw ApiException.validation("请填写处理意见，该意见会公示给代驾");
        }
        String status = approve ? "APPROVED" : "REJECTED";
        if (miscMapper.auditAppeal(appealId, status, reviewerUserId, opinion) == 0)
        {
            throw ApiException.stateConflict("申诉状态已变化，请刷新");
        }
        if (approve)
        {
            miscMapper.revokePenalty(ap.lng("penalty_id"));
            eventService.record("PENALTY", ap.lng("penalty_id"), "PENALTY_REVOKED_BY_APPEAL", null, null,
                    Map.of("appealId", String.valueOf(appealId)), opinion);
        }
        eventService.notify(Row.of(accountMapper.findDriverById(ap.lng("driver_id"))).lng("account_id"),
                Codes.Role.DRIVER, null, "APPEAL_" + status,
                "您的停单申诉" + (approve ? "已通过，处罚已撤销" : "未通过") + "：" + opinion);
        return appealView(Row.of(adminMapper.findAppealDetail(appealId)), true);
    }

    // ------------------------------------------------------------------ 视图与工具

    private Row requireDriver()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("当前账号还不是代驾员");
        }
        return driver;
    }

    private Map<String, Object> penaltyView(Row p, boolean admin)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("penaltyId", String.valueOf(p.lng("id")));
        view.put("driverId", String.valueOf(p.lng("driver_id")));
        view.put("penaltyType", p.str("penalty_type"));
        view.put("penaltyTypeLabel", "STOP_TODAY".equals(p.str("penalty_type")) ? "当日停单" : "跳过本轮");
        view.put("status", p.str("status"));
        view.put("statusLabel", switch (p.str("status", ""))
        {
            case "ACTIVE" -> "生效中";
            case "EXPIRED" -> "已过期";
            case "REVOKED" -> "已撤销";
            default -> p.str("status");
        });
        view.put("reason", p.str("reason"));
        view.put("businessDate", p.date("business_date") == null ? null : p.date("business_date").toString());
        view.put("effectiveUntil", ViTime.iso(p.time("effective_until")));
        view.put("createdAt", ViTime.iso(p.time("created_at")));
        if (admin)
        {
            view.put("driverName", p.str("driver_name"));
            view.put("orderNo", p.str("order_no"));
            view.put("ruleVersion", p.str("rule_version"));
            view.put("roundKey", p.str("round_key"));
        }
        return view;
    }

    private Map<String, Object> appealView(Row ap, boolean admin)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("appealId", String.valueOf(ap.lng("id")));
        view.put("penaltyId", String.valueOf(ap.lng("penalty_id")));
        view.put("status", ap.str("status"));
        view.put("statusLabel", switch (ap.str("status", ""))
        {
            case "SUBMITTED" -> "待处理";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "未通过";
            default -> ap.str("status");
        });
        view.put("reason", ap.str("reason"));
        view.put("penaltyReason", ap.str("penalty_reason"));
        view.put("penaltyType", ap.str("penalty_type"));
        view.put("submittedAt", ViTime.iso(ap.time("submitted_at")));
        view.put("reviewedAt", ViTime.iso(ap.time("reviewed_at")));
        view.put("publicOpinion", ap.str("public_opinion"));
        view.put("evidenceFileIds", ap.json("evidence_file_ids"));
        if (admin)
        {
            view.put("driverId", String.valueOf(ap.lng("driver_id")));
            view.put("driverName", ap.str("driver_name"));
            view.put("reviewerUserId", ap.lng("reviewer_user_id"));
        }
        return view;
    }
}
