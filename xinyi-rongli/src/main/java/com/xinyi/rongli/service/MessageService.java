package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 站内消息与隐私请求。
 */
@Service
public class MessageService
{
    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private Crypto crypto;

    public Map<String, Object> myMessages(int pageNum, int pageSize, boolean unreadOnly)
    {
        MiniPrincipal p = MiniContext.require();
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> m : miscMapper.listNotifications(p.accountId, p.role, offset, limit))
        {
            Row n = Row.of(m);
            if (unreadOnly && n.time("read_at") != null)
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("messageId", String.valueOf(n.lng("id")));
            item.put("scene", n.str("scene"));
            item.put("role", n.str("role"));
            item.put("orderId", n.lng("order_id") == null ? null : String.valueOf(n.lng("order_id")));
            item.put("content", n.str("content"));
            item.put("sendStatus", n.str("send_status"));
            item.put("read", n.time("read_at") != null);
            item.put("readAt", ViTime.iso(n.time("read_at")));
            item.put("createdAt", ViTime.iso(n.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("unread", miscMapper.countUnread(p.accountId));
        result.put("rows", rows);
        return result;
    }

    @Transactional
    public Map<String, Object> markRead(Long messageId)
    {
        MiniPrincipal p = MiniContext.require();
        int n = miscMapper.markNotificationRead(messageId, p.accountId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messageId", String.valueOf(messageId));
        result.put("read", true);
        result.put("changed", n > 0);
        return result;
    }

    // ------------------------------------------------------------------ 隐私请求

    @Transactional
    public Map<String, Object> createPrivacyCase(String reason, String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请说明隐私请求内容");
        }
        if (reason.length() > 1000)
        {
            throw ApiException.validation("隐私请求说明不能超过 1000 字");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("caseType", "PRIVACY");
        row.put("orderId", null);
        row.put("requesterAccountId", p.accountId);
        row.put("requesterUserId", null);
        row.put("status", "APPLIED");
        row.put("payloadCipher", crypto.encrypt(com.alibaba.fastjson2.JSON.toJSONString(Map.of(
                "reason", reason, "orderNo", orderNo == null ? "" : orderNo,
                "channel", "MINI"))));
        row.put("reason", reason.length() > 500 ? reason.substring(0, 500) : reason);
        row.put("expiresAt", ViTime.now().plusDays(30));
        adminMapper.insertGovernance(row);
        Long caseId = ((Number) row.get("id")).longValue();
        return privacyView(Row.of(adminMapper.findGovernance(caseId)), false);
    }

    public Map<String, Object> myPrivacyCases(int pageNum, int pageSize)
    {
        MiniPrincipal p = MiniContext.require();
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : adminMapper.listGovernanceOfAccount("PRIVACY", p.accountId, offset, limit))
        {
            rows.add(privacyView(Row.of(c), false));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> adminPrivacyCases(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : adminMapper.listGovernance("PRIVACY", status, null, offset, limit))
        {
            rows.add(privacyView(Row.of(c), true));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countGovernance("PRIVACY", status, null));
        result.put("rows", rows);
        return result;
    }

    @Transactional
    public Map<String, Object> resolvePrivacyCase(Long caseId, String status, String conclusion, Long userId)
    {
        Row c = Row.of(adminMapper.findGovernance(caseId));
        if (c.isEmpty() || !"PRIVACY".equals(c.str("case_type")))
        {
            throw ApiException.notFound("隐私请求不存在");
        }
        String target = status == null ? "COMPLETED" : status.toUpperCase();
        if (!"PROCESSING".equals(target) && !"COMPLETED".equals(target) && !"REJECTED".equals(target))
        {
            throw ApiException.validation("处理结论只能是 PROCESSING / COMPLETED / REJECTED");
        }
        adminMapper.updateGovernance(caseId, target, userId, conclusion, null, null);
        return privacyView(Row.of(adminMapper.findGovernance(caseId)), true);
    }

    private Map<String, Object> privacyView(Row c, boolean admin)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("caseId", String.valueOf(c.lng("id")));
        view.put("status", c.str("status"));
        view.put("statusLabel", switch (c.str("status", ""))
        {
            case "APPLIED" -> "已提交";
            case "PROCESSING" -> "处理中";
            case "COMPLETED" -> "已处理";
            case "REJECTED" -> "已驳回";
            default -> c.str("status");
        });
        view.put("reason", c.str("reason"));
        view.put("createdAt", ViTime.iso(c.time("created_at")));
        view.put("expiresAt", ViTime.iso(c.time("expires_at")));
        if (admin)
        {
            view.put("requesterAccountId", c.lng("requester_account_id") == null ? null
                    : String.valueOf(c.lng("requester_account_id")));
            view.put("reviewerId", c.lng("reviewer_id"));
            view.put("orderNo", c.str("order_no"));
        }
        else
        {
            view.put("hint", "我们会在 15 个工作日内响应您的隐私请求");
        }
        return view;
    }

    /** 隐私请求详情（本人或管理端） */
    public Map<String, Object> privacyDetail(Long caseId, boolean admin)
    {
        Row c = Row.of(adminMapper.findGovernance(caseId));
        if (c.isEmpty() || !"PRIVACY".equals(c.str("case_type")))
        {
            throw ApiException.notFound("隐私请求不存在");
        }
        if (!admin)
        {
            MiniPrincipal p = MiniContext.require();
            if (!Objects.equals(p.accountId, c.lng("requester_account_id")))
            {
                throw ApiException.forbidden("无权查看该隐私请求");
            }
        }
        return privacyView(c, admin);
    }
}
