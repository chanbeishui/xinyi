package com.xinyi.rongli.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.xinyi.rongli.mapper.IntegrationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 短信与订阅模板、场景路由、发送诊断。
 *
 * <p>一条业务事件只允许命中一个主通道：路由表 (组织,环境,场景,接收身份) 唯一，
 * 因此不会因为腾讯云与阿里云都开启而发出两份短信。UNKNOWN 只查回执，不自动换通道重发。</p>
 */
@Service
public class IntegrationMessageService
{
    private static final String TARGET_TEMPLATE = "TEMPLATE";

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private IntegrationConfigService configService;

    // ================================================================ 模板

    public Map<String, Object> listTemplates(String provider, String scene, String ownerDeptId, String env,
            Integer pageNum, Integer pageSize)
    {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 200 ? 20 : pageSize;
        Long deptId = ownerDeptId == null || ownerDeptId.isBlank() ? null : Long.valueOf(ownerDeptId);
        String providerFilter = blank(provider);
        String sceneFilter = blank(scene);
        if (providerFilter != null && !IntegrationRules.PROVIDERS.contains(providerFilter))
        {
            throw ApiException.validation("未知的模板供应商：" + providerFilter);
        }
        if (sceneFilter != null && !IntegrationRules.SCENES.contains(sceneFilter))
        {
            throw ApiException.validation("未知的通知场景：" + sceneFilter);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listTemplates(providerFilter, sceneFilter, deptId, blank(env),
                (page - 1) * size, size))
        {
            rows.add(templateView(Row.of(row), false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", mapper.countTemplates(providerFilter, sceneFilter, deptId, blank(env)));
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    public Map<String, Object> viewTemplate(Long templateId)
    {
        return templateView(loadTemplate(templateId), true);
    }

    @Transactional
    public Map<String, Object> createTemplate(Map<String, Object> body)
    {
        String name = IntegrationRules.str(body, "name");
        String provider = IntegrationRules.str(body, "provider");
        String scene = IntegrationRules.str(body, "scene");
        String audience = IntegrationRules.str(body, "audience");
        Map<String, Object> scope = IntegrationRules.obj(body, "scope");
        String env = IntegrationRules.str(scope, "environment");
        if (name == null || name.isBlank() || name.length() > 100)
        {
            throw ApiException.validation("模板名称不能为空且不超过 100 字");
        }
        if (provider == null || !IntegrationRules.PROVIDERS.contains(provider))
        {
            throw ApiException.validation("请选择模板供应商");
        }
        if (scene == null || !IntegrationRules.SCENES.contains(scene))
        {
            throw ApiException.validation("请选择已实现的业务场景，不要自由拼英文事件名");
        }
        if (audience == null || !IntegrationRules.SCOPES.contains(audience))
        {
            throw ApiException.validation("请选择接收身份");
        }
        if (env == null || !IntegrationRules.ENVIRONMENTS.contains(env))
        {
            throw ApiException.validation("请选择环境");
        }
        Long deptId = IntegrationConfigService.longOrNull(IntegrationRules.str(scope, "ownerDeptId"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("provider", provider);
        row.put("ownerDeptId", deptId == null ? 100L : deptId);
        row.put("environment", env);
        row.put("scene", scene);
        row.put("audience", audience);
        row.put("createdBy", configService.currentUserId());
        mapper.insertTemplate(row);
        return templateView(loadTemplate(Row.of(row).lng("id")), true);
    }

    @Transactional
    public Map<String, Object> saveTemplateDraft(Long templateId, Map<String, Object> body)
    {
        Row template = loadTemplate(templateId);
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != template.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "模板已被他人修改，请重新加载后再保存");
        }
        String provider = template.str("provider");
        String bodyProvider = IntegrationRules.str(body, "provider");
        if (bodyProvider != null && !bodyProvider.equals(provider))
        {
            throw ApiException.validation("模板的供应商不可更改；请新建模板");
        }
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("请填写变更原因");
        }
        Map<String, Object> profileRef = IntegrationRules.obj(body, "profile");
        Long profileId = IntegrationConfigService.longOrNull(IntegrationRules.str(profileRef, "profileId"));
        Long profileRevisionId = IntegrationConfigService.longOrNull(IntegrationRules.str(profileRef, "revisionId"));
        if (profileId == null || profileRevisionId == null)
        {
            throw ApiException.validation("请选择已发布的通道配置（短信通道或小程序凭据）");
        }
        Row profile = configService.loadProfile(profileId);
        if (!profile.str("environment").equals(template.str("environment")))
        {
            throw ApiException.validation("通道配置与模板的环境不一致");
        }
        Map<String, Object> draft = new LinkedHashMap<>(body);
        draft.put("profileId", String.valueOf(profileId));
        draft.put("revisionId", String.valueOf(profileRevisionId));
        List<Map<String, Object>> issues = "WECHAT_SUBSCRIBE".equals(provider)
                ? IntegrationRules.validateSubscribeTemplate(draft, false)
                : IntegrationRules.validateSmsTemplate(provider, draft, false);

        Map<String, Object> options = new LinkedHashMap<>();
        copy(draft, options, "approvedContent", "signName", "pageRoute", "pageParameterKeys",
                "miniProgramState", "lang", "subscriptionType", "longTermQualificationEvidenceId");
        Map<String, Object> stored = new LinkedHashMap<>();
        stored.put("vendorTemplateId", IntegrationRules.str(draft, "vendorTemplateId"));
        stored.put("parameters", IntegrationRules.list(draft, "parameters"));
        stored.put("options", options);
        stored.put("enabled", IntegrationRules.bool(draft, "enabled"));
        String hash = IntegrationRules.contentHash(stored, Map.of("profileId", profileId,
                "profileRevisionId", profileRevisionId));

        mapper.bumpTemplateSeq(templateId);
        long seq = mapper.currentTemplateSeq(templateId) == null ? 1L : mapper.currentTemplateSeq(templateId);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("templateId", templateId);
        row.put("revisionNo", seq);
        row.put("profileId", profileId);
        row.put("profileRevisionId", profileRevisionId);
        row.put("vendorTemplateId", IntegrationRules.str(draft, "vendorTemplateId"));
        row.put("parameterBindings", Json.write(IntegrationRules.list(draft, "parameters")));
        row.put("optionsJson", Json.write(options));
        row.put("enabled", IntegrationRules.bool(draft, "enabled") ? 1 : 0);
        row.put("approvalStatus", IntegrationRules.str(draft, "vendorTemplateId") == null ? "UNKNOWN" : "APPROVED");
        row.put("contentHash", hash);
        row.put("createdBy", configService.currentUserId());
        row.put("reason", reason);
        mapper.insertTemplateRevision(row);
        Long revisionId = Row.of(row).lng("id");
        if (mapper.updateTemplateDraftPointer(templateId, revisionId, expectedVersion) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "保存时模板版本已变化，请重新加载");
        }
        Map<String, Object> out = revisionView(Row.of(mapper.findTemplateRevision(revisionId)), provider);
        out.put("savedAsDraft", true);
        out.put("effective", false);
        out.put("validationIssues", issues);
        return out;
    }

    @Transactional
    public Map<String, Object> checkTemplate(Long templateId, Map<String, Object> body)
    {
        Row template = loadTemplate(templateId);
        Long revisionId = IntegrationConfigService.longOf(body.get("revisionId"), 0L);
        Row revision = loadTemplateRevision(revisionId, templateId);
        String level = IntegrationRules.str(body, "level");
        if (level == null || !List.of("LOCAL", "READ_ONLY", "LIVE_EVIDENCE").contains(level))
        {
            throw ApiException.validation("校验级别只能是 LOCAL / READ_ONLY / LIVE_EVIDENCE");
        }
        Map<String, Object> draft = draftOf(revision);
        List<Map<String, Object>> issues = "WECHAT_SUBSCRIBE".equals(template.str("provider"))
                ? IntegrationRules.validateSubscribeTemplate(draft, true)
                : IntegrationRules.validateSmsTemplate(template.str("provider"), draft, true);
        if (!"LOCAL".equals(level))
        {
            issues.add(IntegrationRules.issue("vendor", "VI_CONFIG_UNVERIFIED",
                    "未执行供应商联调：模板审批状态与真实到达率需在测试白名单验证", "WARNING",
                    "完成 L01 联调后单独记录证据，不把本地校验当成已送达"));
        }
        String status = IntegrationRules.hasError(issues) ? "FAIL" : ("LOCAL".equals(level) ? "PASS" : "PARTIAL");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("targetType", TARGET_TEMPLATE);
        row.put("targetId", templateId);
        row.put("revisionId", revisionId);
        row.put("contentHash", revision.str("contentHash"));
        row.put("checkLevel", level);
        row.put("status", status);
        row.put("issuesJson", Json.write(issues));
        row.put("ttlMinutes", 30L);
        row.put("checkedBy", configService.currentUserId());
        row.put("realPaymentVerified", false);
        mapper.insertCheck(row);
        return configService.checkView(Row.of(mapper.findCheck(Row.of(row).lng("id"))), null, templateId);
    }

    public Map<String, Object> previewTemplate(Long templateId, Map<String, Object> body)
    {
        Row template = loadTemplate(templateId);
        Long revisionId = IntegrationConfigService.longOf(body.get("revisionId"), 0L);
        Row revision = loadTemplateRevision(revisionId, templateId);
        Map<String, Object> sample = IntegrationRules.obj(body, "sampleValues");
        Map<String, Object> draft = draftOf(revision);
        List<Map<String, Object>> bindings = bindingsOf(revision);
        List<Map<String, Object>> mapped = new ArrayList<>();
        String text = IntegrationRules.str(draft, "approvedContent");
        List<Map<String, Object>> issues = new ArrayList<>();
        for (Map<String, Object> binding : bindings)
        {
            String key = binding.get("keyword") != null ? String.valueOf(binding.get("keyword"))
                    : String.valueOf(binding.get("parameterKey"));
            String variable = binding.get("variable") == null ? null : String.valueOf(binding.get("variable"));
            String value = variable == null ? null : IntegrationRules.str(sample, variable);
            Map<String, Object> field = new LinkedHashMap<>();
            field.put("key", key);
            field.put("variable", variable);
            field.put("sampleValue", value);
            field.put("mapped", value != null && !value.isBlank());
            mapped.add(field);
            if (value == null || value.isBlank())
            {
                issues.add(IntegrationRules.issue(key, "VI_CONFIG_INVALID",
                        "字段 " + key + " 缺少来源或示例值，真实发送会因参数不完整被供应商拒绝", "WARNING",
                        "补齐变量映射"));
            }
            if (text != null && key != null)
            {
                text = text.replace("{" + key + "}", value == null ? "" : value);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("renderedText", text == null ? "" : text);
        out.put("mappedFields", mapped);
        out.put("issues", issues);
        out.put("willSend", false);
        out.put("note", "仅本地渲染预览，不发送、不消耗订阅次数；示例值只接受白名单变量");
        return out;
    }

    @Transactional
    public Map<String, Object> publishTemplate(Long templateId, Map<String, Object> body)
    {
        Row template = loadTemplate(templateId);
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != template.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "模板版本已变化，请重新加载");
        }
        Long revisionId = IntegrationConfigService.longOf(body.get("revisionId"), 0L);
        Row revision = loadTemplateRevision(revisionId, templateId);
        String contentHash = IntegrationRules.str(body, "contentHash");
        if (contentHash == null || !contentHash.equals(revision.str("contentHash")))
        {
            throw ApiException.validation("内容摘要与待发布版本不一致，请重新保存并校验");
        }
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("发布必须填写原因");
        }
        Long checkId = IntegrationConfigService.longOf(body.get("checkId"), 0L);
        Row check = Row.of(mapper.findCheck(checkId));
        if (check.isEmpty() || !TARGET_TEMPLATE.equals(check.str("targetType"))
                || !templateId.equals(check.lng("targetId")) || !revisionId.equals(check.lng("revisionId")))
        {
            throw ApiException.unprocessable("VI_CONFIG_CHECK_REQUIRED", "请先校验该模板版本");
        }
        if ("FAIL".equals(check.str("status")))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "校验未通过，不能发布");
        }
        Map<String, Object> draft = draftOf(revision);
        List<Map<String, Object>> issues = "WECHAT_SUBSCRIBE".equals(template.str("provider"))
                ? IntegrationRules.validateSubscribeTemplate(draft, true)
                : IntegrationRules.validateSmsTemplate(template.str("provider"), draft, true);
        if (IntegrationRules.hasError(issues))
        {
            for (Map<String, Object> issue : issues)
            {
                if ("ERROR".equals(issue.get("severity")))
                {
                    throw ApiException.unprocessable("VI_CONFIG_INVALID", String.valueOf(issue.get("message")));
                }
            }
        }
        Row profile = configService.loadProfile(revision.lng("profileId"));
        if (profile.lng("activeRevisionId") == null
                || !profile.lng("activeRevisionId").equals(revision.lng("profileRevisionId")))
        {
            throw ApiException.unprocessable("VI_CONFIG_DEPENDENCY_MISSING",
                    "通道配置版本已变化，请重新保存模板并指向当前生效版本");
        }
        Map<String, Object> pub = new LinkedHashMap<>();
        pub.put("targetType", TARGET_TEMPLATE);
        pub.put("targetId", templateId);
        pub.put("revisionId", revisionId);
        pub.put("previousRevisionId", template.lng("activeRevisionId"));
        pub.put("checkId", checkId);
        pub.put("action", "PUBLISH");
        pub.put("publishedBy", configService.currentUserId());
        pub.put("reason", reason);
        Map<String, Object> dep = new LinkedHashMap<>();
        dep.put("profileId", String.valueOf(revision.lng("profileId")));
        dep.put("revisionId", String.valueOf(revision.lng("profileRevisionId")));
        pub.put("dependencyVersions", Json.write(List.of(dep)));
        pub.put("targetVersion", expectedVersion + 1);
        mapper.insertPublication(pub);
        if (mapper.publishTemplate(templateId, revisionId, expectedVersion) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "发布时模板版本已变化");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("publicationId", String.valueOf(Row.of(pub).lng("id")));
        out.put("templateId", String.valueOf(templateId));
        out.put("activeRevisionId", String.valueOf(revisionId));
        out.put("version", expectedVersion + 1);
        out.put("effectiveAt", ViTime.iso(ViTime.now()));
        return out;
    }

    public Map<String, Object> templatePublications(Long templateId, Integer pageNum, Integer pageSize)
    {
        Row template = loadTemplate(templateId);
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 100 ? 20 : pageSize;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listPublications(TARGET_TEMPLATE, templateId, (page - 1) * size, size))
        {
            rows.add(configService.publicationView(Row.of(row), null, templateId));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", mapper.countPublications(TARGET_TEMPLATE, templateId));
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    /**
     * 真实测试发送：必须显式确认、命中白名单、且模板已发布。
     * 本期未接真实 SDK 时如实返回 NOT_EXECUTED，不伪造 accepted。
     */
    public Map<String, Object> testTemplate(Long templateId, Map<String, Object> body)
    {
        Row template = loadTemplate(templateId);
        if (template.lng("activeRevisionId") == null)
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "模板尚未发布，不能做真实发送测试");
        }
        if (!IntegrationRules.bool(body, "confirmed"))
        {
            throw ApiException.validation("真实测试发送可能产生费用或消耗订阅次数，请显式确认后再提交");
        }
        String recipientId = IntegrationRules.str(body, "recipientId");
        Row recipient = recipientId == null ? Row.empty() : Row.of(mapper.findRecipient(Long.valueOf(recipientId)));
        if (recipient.isEmpty() || !recipient.bool("enabled"))
        {
            throw ApiException.unprocessable("VI_CONFIG_RECIPIENT_NOT_ALLOWED",
                    "真实测试只能发送到已登记且启用的白名单收件人");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("templateId", String.valueOf(templateId));
        out.put("recipientId", recipientId);
        out.put("status", "NOT_EXECUTED");
        out.put("note", "当前环境未配置供应商凭据，测试请求未下发；请完成 L01 联调后重试，本接口不返回假的 accepted");
        return out;
    }

    // ================================================================ 路由

    public Map<String, Object> listRoutes(String ownerDeptId, String env)
    {
        Long deptId = IntegrationConfigService.longOrNull(ownerDeptId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listRoutes(deptId, blank(env)))
        {
            rows.add(routeView(Row.of(row)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("rows", rows);
        out.put("note", "同一(组织,环境,场景,接收身份)只能有一个主通道，避免腾讯云与阿里云重复下发");
        return out;
    }

    @Transactional
    public Map<String, Object> createRoute(Map<String, Object> body)
    {
        Map<String, Object> row = routeRow(body);
        Long deptId = IntegrationConfigService.longOrNull(String.valueOf(row.get("ownerDeptId")));
        String env = String.valueOf(row.get("environment"));
        Row existing = Row.of(mapper.findRouteByKey(deptId, env, String.valueOf(row.get("scene")),
                String.valueOf(row.get("audience"))));
        if (existing.isNotEmpty())
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "该场景已有路由，请直接编辑");
        }
        validateRouteTemplates(row, deptId, env);
        row.put("updatedBy", configService.currentUserId());
        mapper.insertRoute(row);
        return routeView(Row.of(mapper.findRoute(Row.of(row).lng("id"))));
    }

    @Transactional
    public Map<String, Object> updateRoute(Long routeId, Map<String, Object> body)
    {
        Row existing = Row.of(mapper.findRoute(routeId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("路由不存在");
        }
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != existing.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "路由已被他人修改");
        }
        Map<String, Object> row = routeRow(body);
        Long deptId = IntegrationConfigService.longOrNull(String.valueOf(row.get("ownerDeptId")));
        String env = String.valueOf(row.get("environment"));
        validateRouteTemplates(row, deptId, env);
        Long primary = IntegrationConfigService.longOrNull(row.get("primaryTemplateId"));
        Long fallback = IntegrationConfigService.longOrNull(row.get("fallbackTemplateId"));
        String policy = String.valueOf(row.get("fallbackPolicy"));
        boolean enabled = IntegrationRules.bool(row, "enabled");
        String reason = String.valueOf(row.get("reason"));
        int updated = mapper.updateRoute(routeId, primary, fallback, policy, enabled,
                configService.currentUserId(), reason, expectedVersion);
        if (updated == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "保存时路由版本已变化");
        }
        return routeView(Row.of(mapper.findRoute(routeId)));
    }

    private Map<String, Object> routeRow(Map<String, Object> body)
    {
        String scene = IntegrationRules.str(body, "scene");
        String audience = IntegrationRules.str(body, "audience");
        String env = IntegrationRules.str(body, "environment");
        String policy = IntegrationRules.str(body, "fallbackPolicy");
        String reason = IntegrationRules.str(body, "reason");
        if (scene == null || !IntegrationRules.SCENES.contains(scene))
        {
            throw ApiException.validation("请选择有效的业务场景");
        }
        if (audience == null || !IntegrationRules.SCOPES.contains(audience))
        {
            throw ApiException.validation("请选择接收身份");
        }
        if (env == null || !IntegrationRules.ENVIRONMENTS.contains(env))
        {
            throw ApiException.validation("请选择环境");
        }
        if (policy == null || !List.of("NONE", "DEFINITELY_NOT_SENT_ONLY").contains(policy))
        {
            throw ApiException.validation("备用策略只能是 NONE 或 DEFINITELY_NOT_SENT_ONLY");
        }
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("请填写变更原因");
        }
        if (IntegrationRules.str(body, "primaryTemplateId") == null)
        {
            throw ApiException.validation("请选择主通道模板");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("scene", scene);
        row.put("audience", audience);
        row.put("environment", env);
        row.put("ownerDeptId", IntegrationRules.str(body, "ownerDeptId"));
        row.put("primaryTemplateId", IntegrationConfigService.longOrNull(body.get("primaryTemplateId")));
        row.put("fallbackTemplateId", IntegrationConfigService.longOrNull(body.get("fallbackTemplateId")));
        row.put("fallbackPolicy", policy);
        row.put("enabled", IntegrationRules.bool(body, "enabled"));
        row.put("reason", reason);
        return row;
    }

    private void validateRouteTemplates(Map<String, Object> row, Long deptId, String env)
    {
        assertTemplateUsable(IntegrationConfigService.longOrNull(row.get("primaryTemplateId")), row, deptId, env);
        Long fallback = IntegrationConfigService.longOrNull(row.get("fallbackTemplateId"));
        if (fallback != null)
        {
            assertTemplateUsable(fallback, row, deptId, env);
            if ("NONE".equals(row.get("fallbackPolicy")))
            {
                throw ApiException.validation("配置了备用模板就必须选择备用策略，或在 NONE 下移除备用模板");
            }
        }
    }

    private void assertTemplateUsable(Long templateId, Map<String, Object> route, Long deptId, String env)
    {
        if (templateId == null)
        {
            throw ApiException.validation("模板ID不能为空");
        }
        Row template = Row.of(mapper.findTemplate(templateId));
        if (template.isEmpty())
        {
            throw ApiException.validation("模板不存在：" + templateId);
        }
        if (template.lng("activeRevisionId") == null)
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "模板「" + template.str("name") + "」尚未发布");
        }
        if (!String.valueOf(template.str("scene")).equals(String.valueOf(route.get("scene")))
                || !String.valueOf(template.str("audience")).equals(String.valueOf(route.get("audience"))))
        {
            throw ApiException.validation("模板的场景或接收身份与路由不一致");
        }
        if (!String.valueOf(template.str("environment")).equals(env))
        {
            throw ApiException.validation("模板与路由的环境不一致");
        }
        if (deptId != null && !deptId.equals(template.lng("ownerDeptId")))
        {
            throw ApiException.validation("模板不属于该组织，不能用于此路由");
        }
    }

    // ================================================================ 发送诊断

    public Map<String, Object> listDeliveries(String status, String provider, String scene, String ownerDeptId,
            Integer pageNum, Integer pageSize)
    {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 200 ? 20 : pageSize;
        Long deptId = IntegrationConfigService.longOrNull(ownerDeptId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listDeliveries(blank(status), blank(provider), blank(scene), deptId,
                (page - 1) * size, size))
        {
            rows.add(deliveryView(Row.of(row)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", mapper.countDeliveries(blank(status), blank(provider), blank(scene), deptId));
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    /** 查询回执：只读，绝不因为 UNKNOWN 就重发 */
    public Map<String, Object> queryReceipt(Long deliveryId, Map<String, Object> body)
    {
        Row delivery = loadDelivery(deliveryId);
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion >= 0 && expectedVersion != delivery.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "发送记录已变化，请刷新");
        }
        Map<String, Object> out = deliveryView(delivery);
        String status = delivery.str("status");
        if ("UNKNOWN".equals(status))
        {
            out.put("queryResult", "UNRESOLVED");
            out.put("note", "供应商未返回确定结果，保持 UNKNOWN 并转人工；禁止自动换通道重发以避免双发");
        }
        else if ("ACCEPTED".equals(status))
        {
            out.put("queryResult", "ACCEPTED_ONLY");
            out.put("note", "供应商已受理不等于已送达，也未代表用户已读；回执需供应商回调或后续查询确认");
        }
        else
        {
            out.put("queryResult", "NO_ACTION");
            out.put("note", "当前状态无需查询");
        }
        return out;
    }

    @Transactional
    public Map<String, Object> retryDelivery(Long deliveryId, Map<String, Object> body)
    {
        Row delivery = loadDelivery(deliveryId);
        if (!"FAILED".equals(delivery.str("status")))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID",
                    "只有确定失败（FAILED）才允许重试；UNKNOWN/ACCEPTED 重试会造成重复发送");
        }
        long expectedVersion = IntegrationConfigService.longOf(body.get("expectedVersion"), -1);
        if (expectedVersion >= 0 && expectedVersion != delivery.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "发送记录已变化，请刷新");
        }
        if (mapper.retryDelivery(deliveryId, delivery.lng("version", 0L)) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "重试时状态已变化");
        }
        return deliveryView(Row.of(mapper.findDelivery(deliveryId)));
    }

    private Map<String, Object> deliveryView(Row row)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("deliveryId", String.valueOf(row.lng("id")));
        out.put("eventId", row.lng("notificationId") == null ? null : String.valueOf(row.lng("notificationId")));
        out.put("scene", row.str("scene"));
        out.put("provider", row.str("provider"));
        out.put("recipientMask", row.str("recipientKey"));
        out.put("templateId", String.valueOf(row.lng("templateId")));
        out.put("templateRevisionId", String.valueOf(row.lng("templateRevisionId")));
        out.put("profileRevisionId", String.valueOf(row.lng("profileRevisionId")));
        out.put("status", row.str("status"));
        out.put("vendorRequestId", row.str("vendorRequestId"));
        out.put("vendorBizId", row.str("vendorBizId"));
        out.put("safeErrorCode", row.str("safeErrorCode"));
        out.put("safeErrorMessage", row.str("safeErrorMessage"));
        out.put("attemptCount", row.lng("attemptCount", 0L));
        out.put("createdAt", ViTime.iso(row.time("createdAt")));
        out.put("updatedAt", ViTime.iso(row.time("updatedAt")));
        List<String> actions = new ArrayList<>();
        if ("UNKNOWN".equals(row.str("status")) || "ACCEPTED".equals(row.str("status")))
        {
            actions.add("QUERY_RECEIPT");
        }
        if ("FAILED".equals(row.str("status")))
        {
            actions.add("RETRY_DEFINITE_FAILURE");
        }
        out.put("allowedActions", actions);
        out.put("version", row.lng("version", 0L));
        return out;
    }

    // ================================================================ 视图

    private Map<String, Object> templateView(Row template, boolean withSettings)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        Long id = template.lng("id");
        out.put("templateId", String.valueOf(id));
        out.put("name", template.str("name"));
        out.put("provider", template.str("provider"));
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("ownerDeptId", String.valueOf(template.lng("ownerDeptId", 0L)));
        scope.put("environment", template.str("environment"));
        out.put("scope", scope);
        out.put("scene", template.str("scene"));
        out.put("audience", template.str("audience"));
        out.put("version", template.lng("version", 0L));
        out.put("activeRevisionId", template.lng("activeRevisionId") == null ? null
                : String.valueOf(template.lng("activeRevisionId")));
        out.put("draftRevisionId", template.lng("draftRevisionId") == null ? null
                : String.valueOf(template.lng("draftRevisionId")));
        if (withSettings)
        {
            out.put("active", templateSettings(template, template.lng("activeRevisionId")));
            out.put("draft", templateSettings(template, template.lng("draftRevisionId")));
        }
        else
        {
            out.put("active", null);
            out.put("draft", null);
        }
        return out;
    }

    private Object templateSettings(Row template, Long revisionId)
    {
        if (revisionId == null)
        {
            return null;
        }
        Row revision = Row.of(mapper.findTemplateRevision(revisionId));
        if (revision.isEmpty())
        {
            return null;
        }
        return revisionView(revision, template.str("provider"));
    }

    private Map<String, Object> revisionView(Row revision, String provider)
    {
        Map<String, Object> options = Json.read(revision.str("optionsJson"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("revisionId", String.valueOf(revision.lng("id")));
        out.put("revision", revision.lng("revisionNo", 0L));
        out.put("contentHash", revision.str("contentHash"));
        out.put("provider", provider);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("profileId", String.valueOf(revision.lng("profileId")));
        profile.put("revisionId", String.valueOf(revision.lng("profileRevisionId")));
        out.put("profile", profile);
        out.put("vendorTemplateId", revision.str("vendorTemplateId"));
        out.put("approvedContent", IntegrationRules.str(options, "approvedContent"));
        out.put("enabled", revision.bool("enabled"));
        out.put("signName", IntegrationRules.str(options, "signName"));
        out.put("smsParameters", "WECHAT_SUBSCRIBE".equals(provider) ? List.of() : bindingsOf(revision));
        out.put("subscribeParameters", "WECHAT_SUBSCRIBE".equals(provider) ? bindingsOf(revision) : List.of());
        out.put("pageRoute", IntegrationRules.str(options, "pageRoute"));
        out.put("pageParameterKeys", options.get("pageParameterKeys"));
        out.put("miniProgramState", IntegrationRules.str(options, "miniProgramState"));
        out.put("lang", IntegrationRules.str(options, "lang"));
        out.put("subscriptionType", IntegrationRules.str(options, "subscriptionType"));
        out.put("vendorApproval", revision.str("approvalStatus"));
        out.put("createdAt", ViTime.iso(revision.time("createdAt")));
        out.put("createdBy", revision.lng("createdBy") == null ? null : String.valueOf(revision.lng("createdBy")));
        out.put("reason", revision.str("reason"));
        return out;
    }

    private Map<String, Object> routeView(Row row)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("routeId", String.valueOf(row.lng("id")));
        out.put("scene", row.str("scene"));
        out.put("audience", row.str("audience"));
        out.put("ownerDeptId", String.valueOf(row.lng("ownerDeptId", 0L)));
        out.put("environment", row.str("environment"));
        out.put("primaryTemplateId", String.valueOf(row.lng("primaryTemplateId")));
        out.put("fallbackTemplateId", row.lng("fallbackTemplateId") == null ? null
                : String.valueOf(row.lng("fallbackTemplateId")));
        out.put("fallbackPolicy", row.str("fallbackPolicy"));
        out.put("enabled", row.bool("enabled"));
        out.put("version", row.lng("version", 0L));
        out.put("effectiveAt", ViTime.iso(row.time("updatedAt")));
        return out;
    }

    // ================================================================ 内部

    private Row loadTemplate(Long id)
    {
        Row row = Row.of(mapper.findTemplate(id));
        if (row.isEmpty())
        {
            throw ApiException.notFound("模板不存在");
        }
        return row;
    }

    private Row loadTemplateRevision(Long revisionId, Long templateId)
    {
        Row row = Row.of(mapper.findTemplateRevision(revisionId));
        if (row.isEmpty() || !templateId.equals(row.lng("templateId")))
        {
            throw ApiException.notFound("模板版本不存在");
        }
        return row;
    }

    private Row loadDelivery(Long id)
    {
        Row row = Row.of(mapper.findDelivery(id));
        if (row.isEmpty())
        {
            throw ApiException.notFound("发送记录不存在");
        }
        return row;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> bindingsOf(Row revision)
    {
        Object raw = revision.get("parameterBindings");
        if (raw == null)
        {
            return List.of();
        }
        try
        {
            return (List<Map<String, Object>>) (List<?>) JSON.parseArray(String.valueOf(raw), Map.class);
        }
        catch (Exception e)
        {
            return List.of();
        }
    }

    /** 把不可变版本还原成“草稿形态”，供校验与预览复用同一套规则 */
    private Map<String, Object> draftOf(Row revision)
    {
        Map<String, Object> draft = new LinkedHashMap<>();
        Map<String, Object> options = Json.read(revision.str("optionsJson"));
        draft.put("profileId", String.valueOf(revision.lng("profileId")));
        draft.put("revisionId", String.valueOf(revision.lng("profileRevisionId")));
        draft.put("vendorTemplateId", revision.str("vendorTemplateId"));
        draft.put("enabled", revision.bool("enabled"));
        draft.put("parameters", bindingsOf(revision));
        for (String key : options.keySet())
        {
            draft.put(key, options.get(key));
        }
        return draft;
    }

    private static void copy(Map<String, Object> from, Map<String, Object> to, String... keys)
    {
        for (String key : keys)
        {
            if (from.get(key) != null)
            {
                to.put(key, from.get(key));
            }
        }
    }

    private static String blank(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 供路由/诊断判断使用：某场景当前是否启用 */
    public boolean sceneEnabled(Long deptId, String environment, String scene, String audience)
    {
        Row route = Row.of(mapper.findRouteByKey(deptId, environment, scene, audience));
        return route.isNotEmpty() && route.bool("enabled");
    }

    /** 订阅模板公开清单：只暴露已发布且启用的模板 */
    public List<Map<String, Object>> publishedSubscribeTemplates(Long deptId, String environment)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : mapper.listTemplates("WECHAT_SUBSCRIBE", null, deptId, environment, 0, 200))
        {
            Row template = Row.of(row);
            if (template.lng("activeRevisionId") == null)
            {
                continue;
            }
            Row revision = Row.of(mapper.findTemplateRevision(template.lng("activeRevisionId")));
            if (revision.isEmpty() || !revision.bool("enabled"))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("templateRevisionId", String.valueOf(revision.lng("id")));
            item.put("vendorTemplateId", revision.str("vendorTemplateId"));
            item.put("scene", template.str("scene"));
            item.put("title", template.str("name"));
            item.put("subscriptionType", Json.str(Json.read(revision.str("optionsJson")), "subscriptionType"));
            out.add(item);
        }
        return out;
    }

    public Set<String> publishedScenes(Long deptId, String environment)
    {
        Set<String> scenes = new LinkedHashSet<>();
        for (Map<String, Object> item : publishedSubscribeTemplates(deptId, environment))
        {
            scenes.add(String.valueOf(item.get("scene")));
        }
        return scenes;
    }
}
