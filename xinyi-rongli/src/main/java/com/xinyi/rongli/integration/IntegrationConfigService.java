package com.xinyi.rongli.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.mapper.IntegrationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.DeptScope;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;
import com.xinyi.system.service.ISysConfigService;

/**
 * 第三方配置档案：草稿 → 校验 → 发布 → 回滚。
 *
 * <p>三条硬规则：</p>
 * <ol>
 *   <li>保存草稿不改生效指针，线上读数永远来自 {@code active_revision_id}；</li>
 *   <li>校验结果绑定“版本 + 内容摘要”，改一个字符旧结果就不能用来发布；</li>
 *   <li>凭据只写不读，接口只返回已配置/指纹/写入时间，明文不出服务端。</li>
 * </ol>
 */
@Service
public class IntegrationConfigService
{
    private static final Logger log = LoggerFactory.getLogger(IntegrationConfigService.class);

    private static final String TARGET_PROFILE = "PROFILE";

    private static final String TARGET_TEMPLATE = "TEMPLATE";

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private Environment environment;

    @Autowired
    private ISysConfigService configService;

    // ================================================================ 列表与详情

    public Map<String, Object> list(String kind, String ownerDeptId, String env, Integer pageNum, Integer pageSize)
    {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 200 ? 20 : pageSize;
        Long deptId = scopedDept(ownerDeptId);
        String kindFilter = blankToNull(kind);
        if (kindFilter != null && !IntegrationRules.PROFILE_KINDS.contains(kindFilter))
        {
            throw ApiException.validation("未知的配置类型：" + kindFilter);
        }
        long total = mapper.countProfiles(kindFilter, deptId, blankToNull(env));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listProfiles(kindFilter, deptId, blankToNull(env), (page - 1) * size, size))
        {
            rows.add(profileView(Row.of(row), false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    public Map<String, Object> view(Long profileId)
    {
        Row profile = loadProfile(profileId);
        assertScope(profile);
        return profileView(profile, true);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body)
    {
        String kind = IntegrationRules.str(body, "kind");
        String name = IntegrationRules.str(body, "name");
        if (kind == null || !IntegrationRules.PROFILE_KINDS.contains(kind))
        {
            throw ApiException.validation("请选择有效的配置类型");
        }
        if (name == null || name.isBlank() || name.length() > 100)
        {
            throw ApiException.validation("配置名称不能为空且不超过 100 字");
        }
        Map<String, Object> scope = IntegrationRules.obj(body, "scope");
        String env = IntegrationRules.str(scope, "environment");
        if (env == null || !IntegrationRules.ENVIRONMENTS.contains(env))
        {
            throw ApiException.validation("请选择环境（TEST 或 LIVE）");
        }
        Long deptId = resolveDeptForWrite(IntegrationRules.str(scope, "ownerDeptId"));
        String configKey = "PAYMENT_POLICY".equals(kind) || "WECHAT_APP".equals(kind) ? "DEFAULT"
                : kind.toLowerCase() + "-" + crypto.randomToken().substring(0, 8);
        if (mapper.findProfileByScope(kind, deptId, env, configKey) != null)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "同一主体、环境与该类型下已存在配置，请直接编辑现有配置");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("kind", kind);
        row.put("ownerDeptId", deptId);
        row.put("environment", env);
        row.put("configKey", configKey);
        row.put("displayName", name);
        row.put("createdBy", currentUserId());
        mapper.insertProfile(row);
        Long id = Row.of(row).lng("id");
        return profileView(loadProfile(id), true);
    }

    // ================================================================ 草稿

    @Transactional
    public Map<String, Object> saveDraft(Long profileId, Map<String, Object> body)
    {
        Row profile = lockProfile(profileId);
        assertScope(profile);
        long expectedVersion = longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != profile.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT",
                    "配置已被他人修改（当前版本 " + profile.lng("version", 0L) + "），请重新加载后再保存");
        }
        String kind = profile.str("kind");
        String bodyKind = IntegrationRules.str(body, "kind");
        if (bodyKind != null && !bodyKind.equals(kind))
        {
            throw ApiException.validation("提交的配置类型与档案不一致");
        }
        if (!(body.get("config") instanceof Map))
        {
            throw ApiException.validation("配置内容不能为空");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) body.get("config");
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("请填写变更原因（至少 2 字），发布与停用都会留审计");
        }

        Set<String> slotsBefore = activeSlots(profileId);
        Set<String> slotsAfter = new LinkedHashSet<>(slotsBefore);
        applyCredentials(profile, body.get("credentials"), slotsAfter, reason);

        Map<String, Object> secretRefs = new LinkedHashMap<>();
        for (String slot : slotsAfter)
        {
            Row secret = Row.of(mapper.findActiveSecret(profileId, slot));
            if (secret.isNotEmpty())
            {
                secretRefs.put(slot, secret.lng("id", 0L));
            }
        }
        String hash = IntegrationRules.contentHash(config, secretRefs);

        Row existing = Row.of(mapper.findRevisionByHash(profileId, hash));
        Long revisionId;
        if (existing.isNotEmpty())
        {
            revisionId = existing.lng("id");
        }
        else
        {
            mapper.bumpRevisionSeq(profileId);
            long seq = mapper.currentRevisionSeq(profileId) == null ? 1L : mapper.currentRevisionSeq(profileId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("profileId", profileId);
            row.put("revisionNo", seq);
            row.put("configJson", Json.write(config));
            row.put("secretRefsJson", Json.write(secretRefs));
            row.put("contentHash", hash);
            row.put("createdBy", currentUserId());
            row.put("reason", reason);
            mapper.insertRevision(row);
            revisionId = Row.of(row).lng("id");
        }
        int updated = mapper.updateDraftPointer(profileId, revisionId, expectedVersion);
        if (updated == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "保存时配置版本已变化，请重新加载");
        }
        Map<String, Object> out = revisionView(loadRevision(revisionId), kind);
        out.put("savedAsDraft", true);
        out.put("effective", false);
        out.put("validationIssues", IntegrationRules.validate(kind, config, slotsAfter, false));
        return out;
    }

    // ================================================================ 校验

    @Transactional
    public Map<String, Object> check(Long profileId, Map<String, Object> body)
    {
        Row profile = loadProfile(profileId);
        assertScope(profile);
        Long revisionId = longOf(body.get("revisionId"), 0L);
        Row revision = loadRevision(revisionId);
        if (revision.lng("profileId", 0L) != profileId.longValue())
        {
            throw ApiException.validation("该版本不属于当前配置档案");
        }
        String level = IntegrationRules.str(body, "level");
        if (level == null || !List.of("LOCAL", "READ_ONLY", "LIVE_EVIDENCE").contains(level))
        {
            throw ApiException.validation("校验级别只能是 LOCAL / READ_ONLY / LIVE_EVIDENCE");
        }
        String kind = profile.str("kind");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = revision.str("configJson") == null ? Map.of()
                : JSON.parseObject(revision.str("configJson"), Map.class);
        Set<String> slots = new LinkedHashSet<>();
        JSONObject refs = Json.read(revision.str("secretRefsJson"));
        for (String key : refs.keySet())
        {
            slots.add(key);
        }
        List<Map<String, Object>> issues = IntegrationRules.validate(kind, config, slots, true);
        if (!"ENV".equals(masterKeySource()))
        {
            issues.add(IntegrationRules.issue("credentials", "VI_CONFIG_KEY_SOURCE", 
                    "当前使用的根密钥来自部署数据密钥而非 VI_INTEGRATION_MASTER_KEY，生产环境必须先配置独立根密钥",
                    "WARNING", "由运维设置 VI_INTEGRATION_MASTER_KEY 后重新发布"));
        }
        if (!"LOCAL".equals(level))
        {
            issues.add(IntegrationRules.issue("connection", "VI_CONFIG_UNVERIFIED",
                    "只读连接检测未执行：未配置供应商凭据或未开启真实联调。本地通过不代表商户可收款、短信可送达",
                    "WARNING", "在隔离环境完成 L01 实际联调后再标记已连接"));
        }
        String status = IntegrationRules.hasError(issues) ? "FAIL" : ("LOCAL".equals(level) ? "PASS" : "PARTIAL");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("targetType", TARGET_PROFILE);
        row.put("targetId", profileId);
        row.put("revisionId", revisionId);
        row.put("contentHash", revision.str("contentHash"));
        row.put("checkLevel", level);
        row.put("status", status);
        row.put("issuesJson", Json.write(issues));
        row.put("ttlMinutes", checkTtlMinutes());
        row.put("checkedBy", currentUserId());
        row.put("realPaymentVerified", false);
        mapper.insertCheck(row);
        return checkView(Row.of(mapper.findCheck(Row.of(row).lng("id"))), profileId, null);
    }

    // ================================================================ 发布

    @Transactional
    public Map<String, Object> publish(Long profileId, Map<String, Object> body)
    {
        Row profile = lockProfile(profileId);
        assertScope(profile);
        long expectedVersion = longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != profile.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "配置版本已变化，请重新加载后再次发布");
        }
        Long revisionId = longOf(body.get("revisionId"), 0L);
        Row revision = loadRevision(revisionId);
        if (revision.lng("profileId", 0L) != profileId.longValue())
        {
            throw ApiException.validation("该版本不属于当前配置档案");
        }
        String contentHash = IntegrationRules.str(body, "contentHash");
        if (contentHash == null || !contentHash.equals(revision.str("contentHash")))
        {
            throw ApiException.validation("内容摘要与待发布版本不一致，请重新保存草稿并校验");
        }
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("发布必须填写原因（至少 2 字）");
        }
        Row check = requireFreshCheck(TARGET_PROFILE, profileId, revisionId, contentHash,
                longOf(body.get("checkId"), 0L));

        String kind = profile.str("kind");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = JSON.parseObject(revision.str("configJson"), Map.class);
        Set<String> slots = newLinkedSet(Json.read(revision.str("secretRefsJson")).keySet());

        // 服务端重新校验，不信任客户端保存的 check 结果
        List<Map<String, Object>> issues = IntegrationRules.validate(kind, config, slots, true);
        if (IntegrationRules.hasError(issues))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID",
                    firstErrorMessage(issues, "配置仍不满足发布条件"));
        }
        if ("PAYMENT_POLICY".equals(kind) && !IntegrationRules.bool(config, "offlineEnabled")
                && !IntegrationRules.bool(config, "wechatEnabled")
                && !IntegrationRules.bool(body, "acknowledgeNoPaymentMethods"))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID",
                    "两种支付方式都关闭会暂停新付费预约，请勾选确认后再发布");
        }

        List<Map<String, Object>> dependencies = new ArrayList<>();
        if ("PAYMENT_POLICY".equals(kind) && IntegrationRules.bool(config, "wechatEnabled"))
        {
            dependencies.add(assertPublishedDependency("WECHAT_PAY", profile, IntegrationRules.obj(config, "wechatMerchant")));
        }
        if ("WECHAT_PAY".equals(kind))
        {
            dependencies.add(assertPublishedDependency("WECHAT_APP", profile, IntegrationRules.obj(config, "appProfile")));
        }

        String previous = profile.lng("activeRevisionId") == null ? null : String.valueOf(profile.lng("activeRevisionId"));
        Map<String, Object> pub = new LinkedHashMap<>();
        pub.put("targetType", TARGET_PROFILE);
        pub.put("targetId", profileId);
        pub.put("revisionId", revisionId);
        pub.put("previousRevisionId", previous == null ? null : Long.valueOf(previous));
        pub.put("checkId", check.lng("id"));
        pub.put("action", "PUBLISH");
        pub.put("publishedBy", currentUserId());
        pub.put("reason", reason);
        pub.put("dependencyVersions", Json.write(dependencies));
        pub.put("targetVersion", expectedVersion + 1);
        mapper.insertPublication(pub);
        int updated = mapper.publishProfile(profileId, revisionId, currentUserId(), expectedVersion);
        if (updated == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "发布时配置版本已变化，请重新加载");
        }
        log.info("third-party config published profile={} kind={} revision={} by={}", profileId, kind, revisionId,
                currentUserId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("publicationId", String.valueOf(Row.of(pub).lng("id")));
        out.put("profileId", String.valueOf(profileId));
        out.put("activeRevisionId", String.valueOf(revisionId));
        out.put("version", expectedVersion + 1);
        out.put("effectiveFor", "NEW_OPERATIONS");
        out.put("effectiveAt", ViTime.iso(ViTime.now()));
        out.put("note", "已生效，无需改源码或重启；历史交易仍按原身份处理");
        return out;
    }

    @Transactional
    public Map<String, Object> rollback(Long profileId, Map<String, Object> body)
    {
        Row profile = lockProfile(profileId);
        assertScope(profile);
        long expectedVersion = longOf(body.get("expectedVersion"), -1);
        if (expectedVersion != profile.lng("version", 0L))
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "配置版本已变化，请重新加载");
        }
        Long targetRevisionId = longOf(body.get("targetRevisionId"), 0L);
        Row revision = loadRevision(targetRevisionId);
        if (revision.lng("profileId", 0L) != profileId.longValue())
        {
            throw ApiException.validation("回滚目标不属于当前配置");
        }
        String reason = IntegrationRules.str(body, "reason");
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("回滚必须填写原因");
        }
        Row check = requireFreshCheck(TARGET_PROFILE, profileId, targetRevisionId, revision.str("contentHash"),
                longOf(body.get("checkId"), 0L));
        Map<String, Object> pub = new LinkedHashMap<>();
        pub.put("targetType", TARGET_PROFILE);
        pub.put("targetId", profileId);
        pub.put("revisionId", targetRevisionId);
        pub.put("previousRevisionId", profile.lng("activeRevisionId"));
        pub.put("checkId", check.lng("id"));
        pub.put("action", "ROLLBACK");
        pub.put("publishedBy", currentUserId());
        pub.put("reason", reason);
        pub.put("dependencyVersions", "[]");
        pub.put("targetVersion", expectedVersion + 1);
        mapper.insertPublication(pub);
        if (mapper.publishProfile(profileId, targetRevisionId, currentUserId(), expectedVersion) == 0)
        {
            throw ApiException.conflict("VI_CONFIG_VERSION_CONFLICT", "回滚时版本已变化，请重新加载");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("publicationId", String.valueOf(Row.of(pub).lng("id")));
        out.put("activeRevisionId", String.valueOf(targetRevisionId));
        out.put("version", expectedVersion + 1);
        out.put("action", "ROLLBACK");
        out.put("note", "回滚同样是一次新发布，已重新通过当前依赖校验；已泄露凭据不会被复活");
        return out;
    }

    // ================================================================ 版本与审计

    public Map<String, Object> revisions(Long profileId, Integer pageNum, Integer pageSize)
    {
        Row profile = loadProfile(profileId);
        assertScope(profile);
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 100 ? 20 : pageSize;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listRevisions(profileId, (page - 1) * size, size))
        {
            rows.add(revisionView(Row.of(row), profile.str("kind")));
        }
        return page(mapper.countRevisions(profileId), page, size, rows);
    }

    public Map<String, Object> publications(Long profileId, Integer pageNum, Integer pageSize)
    {
        Row profile = loadProfile(profileId);
        assertScope(profile);
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 || pageSize > 100 ? 20 : pageSize;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listPublications(TARGET_PROFILE, profileId, (page - 1) * size, size))
        {
            rows.add(publicationView(Row.of(row), profileId, null));
        }
        return page(mapper.countPublications(TARGET_PROFILE, profileId), page, size, rows);
    }

    // ================================================================ 测试收件人

    public Map<String, Object> testRecipients(String ownerDeptId, String env)
    {
        Long deptId = scopedDept(ownerDeptId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : mapper.listRecipients(deptId, blankToNull(env)))
        {
            rows.add(recipientView(Row.of(row)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("rows", rows);
        out.put("note", "真实测试发送只允许命中白名单；未登记号码一律拒绝，避免误发真实客户");
        return out;
    }

    @Transactional
    public Map<String, Object> addTestRecipient(Map<String, Object> body)
    {
        String name = IntegrationRules.str(body, "name");
        String kind = IntegrationRules.str(body, "kind");
        String env = IntegrationRules.str(body, "environment");
        String reason = IntegrationRules.str(body, "reason");
        if (name == null || name.isBlank() || name.length() > 80)
        {
            throw ApiException.validation("白名单名称不能为空且不超过 80 字");
        }
        if (kind == null || !List.of("PHONE", "MINI_ACCOUNT").contains(kind))
        {
            throw ApiException.validation("白名单类型只能是 PHONE 或 MINI_ACCOUNT");
        }
        if (env == null || !IntegrationRules.ENVIRONMENTS.contains(env))
        {
            throw ApiException.validation("请选择环境");
        }
        if (reason == null || reason.length() < 2)
        {
            throw ApiException.validation("请填写登记原因");
        }
        Long deptId = resolveDeptForWrite(IntegrationRules.str(body, "ownerDeptId"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("kind", kind);
        row.put("ownerDeptId", deptId);
        row.put("environment", env);
        row.put("createdBy", currentUserId());
        row.put("reason", reason);
        if ("PHONE".equals(kind))
        {
            String phone = IntegrationRules.str(body, "phone");
            if (phone == null || !phone.matches("^1[3-9]\\d{9}$"))
            {
                throw ApiException.validation("请填写有效的 11 位手机号");
            }
            row.put("phoneCipher", crypto.encrypt(phone));
            row.put("accountId", null);
            row.put("appProfileId", null);
        }
        else
        {
            Long accountId = longOrNull(body.get("accountId"));
            Long appProfileId = longOrNull(body.get("appProfileId"));
            if (accountId == null || appProfileId == null)
            {
                throw ApiException.validation("按小程序账号登记时必须选择账号与小程序凭据");
            }
            row.put("phoneCipher", null);
            row.put("accountId", accountId);
            row.put("appProfileId", appProfileId);
        }
        mapper.insertRecipient(row);
        return recipientView(Row.of(mapper.findRecipient(Row.of(row).lng("id"))));
    }

    @Transactional
    public Map<String, Object> revokeTestRecipient(Long recipientId)
    {
        Row row = Row.of(mapper.findRecipient(recipientId));
        if (row.isEmpty())
        {
            throw ApiException.notFound("白名单记录不存在");
        }
        if (mapper.revokeRecipient(recipientId) == 0)
        {
            throw ApiException.stateConflict("该白名单已停用");
        }
        return recipientView(Row.of(mapper.findRecipient(recipientId)));
    }

    // ================================================================ 运行时解析（供支付/通知使用）

    /** 读取某主体下已生效的配置；没有则返回空 */
    public Row activeProfile(String kind, Long deptId)
    {
        return Row.of(mapper.findActiveProfile(kind, deptId, runtimeEnvironment()));
    }

    /**
     * 解封当前生效版本的凭据，仅供服务端内部发送使用。
     *
     * <p>绝不通过任何控制器返回：{@code profileView} 只给指纹与写入时间。
     * 单个槽解封失败只跳过该槽并记告警，不影响其它槽（例如只换了 APIv3 密钥
     * 而商户私钥仍可解密时，不应整体失效）。</p>
     */
    public Map<String, String> runtimeSecrets(Row profile)
    {
        Map<String, String> out = new LinkedHashMap<>();
        Row revision = activeRevision(profile);
        if (revision.isEmpty())
        {
            return out;
        }
        for (String slot : Json.read(revision.str("secretRefsJson")).keySet())
        {
            Row secret = Row.of(mapper.findActiveSecret(profile.lng("id"), slot));
            if (secret.isEmpty() || secret.get("ciphertext") == null)
            {
                continue;
            }
            try
            {
                out.put(slot, crypto.decrypt((byte[]) secret.get("ciphertext")));
            }
            catch (Exception e)
            {
                log.warn("凭据 {} 解封失败（profile={}）：{}", slot, profile.lng("id"), e.getMessage());
            }
        }
        return out;
    }

    public Row activeRevision(Row profile)
    {
        if (profile == null || profile.isEmpty() || profile.lng("activeRevisionId") == null)
        {
            return Row.empty();
        }
        return Row.of(mapper.findRevision(profile.lng("activeRevisionId")));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> configOf(Row revision)
    {
        if (revision == null || revision.isEmpty() || revision.str("configJson") == null)
        {
            return Map.of();
        }
        return JSON.parseObject(revision.str("configJson"), Map.class);
    }

    public String runtimeEnvironment()
    {
        String env = configService.selectConfigByKey("rongli.integration.environment");
        return env == null || env.isBlank() ? "TEST" : env.trim();
    }

    public String callbackBaseUrl()
    {
        String base = configService.selectConfigByKey("rongli.integration.callbackBaseUrl");
        return base == null ? "" : base.trim();
    }

    public String masterKeySource()
    {
        String explicit = environment.getProperty("VI_INTEGRATION_MASTER_KEY");
        if (explicit == null || explicit.isBlank())
        {
            explicit = environment.getProperty("rongli.integration.master-key");
        }
        return explicit == null || explicit.isBlank() ? "DEPLOYMENT_DATA_KEY" : "ENV";
    }

    public String masterKeyVersion()
    {
        String v = configService.selectConfigByKey("rongli.integration.masterKeyVersion");
        return v == null || v.isBlank() ? "k1" : v.trim();
    }

    // ================================================================ 凭据

    private void applyCredentials(Row profile, Object rawCredentials, Set<String> slots, String reason)
    {
        if (rawCredentials == null)
        {
            return;
        }
        if (!(rawCredentials instanceof Map<?, ?> map))
        {
            throw ApiException.validation("credentials 必须是对象");
        }
        Set<String> allowed = IntegrationRules.slotsOf(profile.str("kind"));
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            String slot = String.valueOf(entry.getKey());
            if (!allowed.contains(slot))
            {
                throw ApiException.validation("不支持的凭据槽：" + slot);
            }
            if (!(entry.getValue() instanceof Map<?, ?> mutation))
            {
                throw ApiException.validation("凭据变更必须是 {action,value} 结构");
            }
            String action = mutation.get("action") == null ? "KEEP" : String.valueOf(mutation.get("action"));
            switch (action)
            {
                case "KEEP" -> {
                    // 不传与 KEEP 等效：保留现有密文，不产生新版本
                }
                case "REPLACE" -> {
                    Object valueObj = mutation.get("value");
                    String value = valueObj == null ? null : String.valueOf(valueObj);
                    if (value == null || value.isBlank())
                    {
                        throw ApiException.validation("替换凭据时内容不能为空；保留原值请用 KEEP 或不传该字段");
                    }
                    if (IntegrationRules.maskedValue(value))
                    {
                        throw ApiException.validation("不能把掩码（******）当作新凭据保存，请重新输入真实内容");
                    }
                    Long profileId = profile.lng("id");
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("profileId", profileId);
                    row.put("slotName", slot);
                    row.put("credentialKind", IntegrationRules.credentialKindOf(slot));
                    byte[] sealed = crypto.encrypt(value);
                    row.put("ciphertext", sealed);
                    byte[] nonce = new byte[12];
                    System.arraycopy(sealed, 0, nonce, 0, 12);
                    row.put("nonce", nonce);
                    row.put("keyVersion", masterKeyVersion());
                    row.put("aadContext", "profile:" + profileId + "|slot:" + slot + "|env:" + profile.str("environment"));
                    row.put("fingerprint", Crypto.hex(Crypto.sha256(Crypto.hex(Crypto.sha256(value)).getBytes())).substring(0, 16));
                    row.put("expiresAt", null);
                    row.put("createdBy", currentUserId());
                    mapper.insertSecret(row);
                    mapper.retireSecrets(profileId, slot, "RETIRED", currentUserId(), "已替换为新版本：" + reason);
                    slots.add(slot);
                }
                case "CLEAR" -> {
                    Long profileId = profile.lng("id");
                    if (referencedByActive(profile, slot))
                    {
                        throw ApiException.conflict("VI_CONFIG_IN_USE",
                                "该凭据仍被当前生效版本引用，不能直接清除；请先发布不依赖它的新版本");
                    }
                    mapper.retireSecrets(profileId, slot, "REVOKED", currentUserId(), "显式清除：" + reason);
                    slots.remove(slot);
                }
                default -> throw ApiException.validation("凭据变更方式只能是 KEEP / REPLACE / CLEAR");
            }
        }
    }

    private boolean referencedByActive(Row profile, String slot)
    {
        if (profile.lng("activeRevisionId") == null)
        {
            return false;
        }
        Row active = Row.of(mapper.findRevision(profile.lng("activeRevisionId")));
        return active.isNotEmpty() && Json.read(active.str("secretRefsJson")).containsKey(slot);
    }

    private Set<String> activeSlots(Long profileId)
    {
        Set<String> slots = new LinkedHashSet<>();
        for (String slot : IntegrationRules.slotsOf(loadProfile(profileId).str("kind")))
        {
            if (mapper.findActiveSecret(profileId, slot) != null)
            {
                slots.add(slot);
            }
        }
        return slots;
    }

    // ================================================================ 视图

    public Map<String, Object> profileView(Row profile, boolean withSettings)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        Long id = profile.lng("id");
        out.put("profileId", String.valueOf(id));
        out.put("kind", profile.str("kind"));
        out.put("name", profile.str("displayName"));
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("ownerDeptId", String.valueOf(profile.lng("ownerDeptId", 0L)));
        scope.put("environment", profile.str("environment"));
        out.put("scope", scope);
        out.put("configKey", profile.str("configKey"));
        out.put("lifecycle", profile.str("lifecycle"));
        out.put("version", profile.lng("version", 0L));
        out.put("activeRevisionId", nullableId(profile.lng("activeRevisionId")));
        out.put("draftRevisionId", nullableId(profile.lng("draftRevisionId")));
        String kind = profile.str("kind");
        if (withSettings)
        {
            out.put("active", settingsOf(profile, profile.lng("activeRevisionId"), kind));
            out.put("draft", settingsOf(profile, profile.lng("draftRevisionId"), kind));
            Set<String> slots = activeSlots(id);
            @SuppressWarnings("unchecked")
            Map<String, Object> draftConfig = (Map<String, Object>) (out.get("draft") == null ? Map.of()
                    : ((Map<String, Object>) out.get("draft")).get("config"));
            out.put("validationIssues", IntegrationRules.validate(kind, draftConfig == null ? Map.of() : draftConfig,
                    slots, true));
        }
        else
        {
            out.put("active", null);
            out.put("draft", null);
            out.put("validationIssues", List.of());
        }
        if ("WECHAT_PAY".equals(kind))
        {
            String base = callbackBaseUrl();
            out.put("generatedPayNotifyUrl", base.isBlank() ? "" : base + "/api/public/pay/notify");
            out.put("generatedRefundNotifyUrl", base.isBlank() ? "" : base + "/api/public/pay/refund-notify");
        }
        out.put("newTransactionsEnabled", profile.lng("activeRevisionId") != null
                && "ACTIVE".equals(profile.str("lifecycle")));
        out.put("historicalMaintenanceRequired", profile.lng("activeRevisionId") != null);
        out.put("createdAt", ViTime.iso(profile.time("createdAt")));
        out.put("updatedAt", ViTime.iso(profile.time("updatedAt")));
        return out;
    }

    private Object settingsOf(Row profile, Long revisionId, String kind)
    {
        if (revisionId == null)
        {
            return null;
        }
        Row revision = Row.of(mapper.findRevision(revisionId));
        if (revision.isEmpty())
        {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", kind);
        out.put("revisionId", String.valueOf(revisionId));
        out.put("revision", revision.lng("revisionNo", 0L));
        out.put("contentHash", revision.str("contentHash"));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = revision.str("configJson") == null ? Map.of()
                : JSON.parseObject(revision.str("configJson"), Map.class);
        out.put("config", config);
        out.put("credentialStates", credentialStates(profile.lng("id"), kind));
        return out;
    }

    private Map<String, Object> credentialStates(Long profileId, String kind)
    {
        Map<String, Object> states = new LinkedHashMap<>();
        for (String slot : IntegrationRules.slotsOf(kind))
        {
            Row secret = Row.of(mapper.findActiveSecret(profileId, slot));
            Map<String, Object> state = new LinkedHashMap<>();
            if (secret.isEmpty())
            {
                state.put("configured", false);
                state.put("fingerprint", null);
                state.put("writtenAt", null);
                state.put("version", null);
            }
            else
            {
                state.put("configured", true);
                state.put("fingerprint", secret.str("fingerprint"));
                state.put("writtenAt", ViTime.iso(secret.time("createdAt")));
                state.put("version", secret.str("keyVersion"));
                state.put("credentialKind", secret.str("credentialKind"));
                state.put("expiresAt", secret.time("expiresAt") == null ? null : ViTime.iso(secret.time("expiresAt")));
            }
            states.put(slot, state);
        }
        return states;
    }

    public Map<String, Object> revisionView(Row revision, String kind)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("revisionId", String.valueOf(revision.lng("id")));
        out.put("profileId", nullableId(revision.lng("profileId")));
        out.put("revision", revision.lng("revisionNo", 0L));
        out.put("contentHash", revision.str("contentHash"));
        out.put("createdAt", ViTime.iso(revision.time("createdAt")));
        out.put("createdBy", nullableId(revision.lng("createdBy")));
        out.put("reason", revision.str("reason"));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = revision.str("configJson") == null ? Map.of()
                : JSON.parseObject(revision.str("configJson"), Map.class);
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("kind", kind);
        settings.put("config", config);
        if (revision.lng("profileId") != null)
        {
            settings.put("credentialStates", credentialStates(revision.lng("profileId"), kind));
        }
        out.put("settings", settings);
        return out;
    }

    public Map<String, Object> checkView(Row check, Long profileId, Long templateId)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("checkId", String.valueOf(check.lng("id")));
        if (profileId != null)
        {
            out.put("profileId", String.valueOf(profileId));
        }
        if (templateId != null)
        {
            out.put("templateId", String.valueOf(templateId));
        }
        out.put("revisionId", nullableId(check.lng("revisionId")));
        out.put("contentHash", check.str("contentHash"));
        out.put("level", check.str("checkLevel"));
        out.put("status", check.str("status"));
        out.put("issues", parseIssues(check.str("issuesJson")));
        out.put("checkedAt", ViTime.iso(check.time("checkedAt")));
        out.put("expiresAt", ViTime.iso(check.time("expiresAt")));
        out.put("realPaymentVerified", check.bool("realPaymentVerified"));
        return out;
    }

    public Map<String, Object> publicationView(Row row, Long profileId, Long templateId)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("publicationId", String.valueOf(row.lng("id")));
        if (profileId != null)
        {
            out.put("profileId", String.valueOf(profileId));
        }
        if (templateId != null)
        {
            out.put("templateId", String.valueOf(templateId));
        }
        out.put("revisionId", nullableId(row.lng("revisionId")));
        out.put("previousRevisionId", nullableId(row.lng("previousRevisionId")));
        out.put("version", row.lng("targetVersion", 0L));
        out.put("action", row.str("action"));
        out.put("reason", row.str("reason"));
        out.put("publishedAt", ViTime.iso(row.time("publishedAt")));
        out.put("publishedBy", nullableId(row.lng("publishedBy")));
        out.put("effectiveFor", "NEW_OPERATIONS");
        out.put("historicalHandling", "历史交易沿用原商户身份与原版本凭据，不追改");
        return out;
    }

    private Map<String, Object> recipientView(Row row)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recipientId", String.valueOf(row.lng("id")));
        out.put("name", row.str("name"));
        out.put("kind", row.str("kind"));
        out.put("phoneMask", row.str("phoneCipher") == null ? null
                : IntegrationRules.maskPhone(crypto.decrypt((byte[]) row.get("phoneCipher"))));
        out.put("accountId", nullableId(row.lng("accountId")));
        out.put("appProfileId", nullableId(row.lng("appProfileId")));
        out.put("ownerDeptId", nullableId(row.lng("ownerDeptId")));
        out.put("environment", row.str("environment"));
        out.put("version", row.lng("version", 0L));
        out.put("enabled", row.bool("enabled"));
        out.put("createdAt", ViTime.iso(row.time("createdAt")));
        return out;
    }

    // ================================================================ 内部工具

    public Row loadProfile(Long id)
    {
        Row row = Row.of(mapper.findProfile(id));
        if (row.isEmpty())
        {
            throw ApiException.notFound("配置不存在或已删除");
        }
        return row;
    }

    private Row lockProfile(Long id)
    {
        Row row = Row.of(mapper.lockProfile(id));
        if (row.isEmpty())
        {
            throw ApiException.notFound("配置不存在或已删除");
        }
        return row;
    }

    public Row loadRevision(Long id)
    {
        Row row = Row.of(mapper.findRevision(id));
        if (row.isEmpty())
        {
            throw ApiException.notFound("配置版本不存在");
        }
        return row;
    }

    private Row requireFreshCheck(String targetType, Long targetId, Long revisionId, String contentHash, Long checkId)
    {
        Row check = Row.of(mapper.findCheck(checkId));
        if (check.isEmpty())
        {
            throw ApiException.unprocessable("VI_CONFIG_CHECK_REQUIRED", "发布前必须先执行校验");
        }
        if (!targetType.equals(check.str("targetType")) || !targetId.equals(check.lng("targetId"))
                || !revisionId.equals(check.lng("revisionId")))
        {
            throw ApiException.unprocessable("VI_CONFIG_CHECK_REQUIRED", "校验结果与待发布版本不匹配，请重新校验");
        }
        if (!String.valueOf(contentHash).equals(check.str("contentHash")))
        {
            throw ApiException.unprocessable("VI_CONFIG_CHECK_STALE",
                    "当前草稿已修改，原校验失效，请重新校验后再发布");
        }
        if ("FAIL".equals(check.str("status")))
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "校验未通过，不能发布");
        }
        if (check.time("expiresAt") != null && check.time("expiresAt").isBefore(ViTime.now()))
        {
            throw ApiException.unprocessable("VI_CONFIG_CHECK_STALE", "校验结果已过期，请重新校验");
        }
        return check;
    }

    private Map<String, Object> assertPublishedDependency(String kind, Row profile, Map<String, Object> reference)
    {
        String profileId = IntegrationRules.str(reference, "profileId");
        String revisionId = IntegrationRules.str(reference, "revisionId");
        if (profileId == null || revisionId == null)
        {
            throw ApiException.unprocessable("VI_CONFIG_DEPENDENCY_MISSING",
                    "请先选择已发布的" + ("WECHAT_PAY".equals(kind) ? "微信支付商户" : "小程序公共凭据") + "配置");
        }
        Row dependency = Row.of(mapper.findProfile(Long.valueOf(profileId)));
        if (dependency.isEmpty() || !kind.equals(dependency.str("kind")))
        {
            throw ApiException.unprocessable("VI_CONFIG_DEPENDENCY_MISSING", "引用的配置不存在或类型不符");
        }
        if (dependency.lng("ownerDeptId", 0L) != profile.lng("ownerDeptId", 0L)
                || !dependency.str("environment").equals(profile.str("environment")))
        {
            throw ApiException.unprocessable("VI_CONFIG_DEPENDENCY_MISSING", "引用的配置必须属于同一收款主体与同一环境");
        }
        if (dependency.lng("activeRevisionId") == null
                || !Long.valueOf(revisionId).equals(dependency.lng("activeRevisionId")))
        {
            throw ApiException.unprocessable("VI_CONFIG_DEPENDENCY_MISSING", "引用的配置版本尚未发布，请先发布该配置");
        }
        Map<String, Object> dep = new LinkedHashMap<>();
        dep.put("profileId", profileId);
        dep.put("revisionId", revisionId);
        return dep;
    }

    private long checkTtlMinutes()
    {
        String v = configService.selectConfigByKey("rongli.integration.checkTtlMinutes");
        try
        {
            return v == null || v.isBlank() ? 30L : Long.parseLong(v.trim());
        }
        catch (NumberFormatException e)
        {
            return 30L;
        }
    }

    private Long scopedDept(String requested)
    {
        Long deptId = longOrNull(requested);
        Long scope = DeptScope.scopedDeptId();
        if (scope != null)
        {
            if (deptId != null && !deptId.equals(scope))
            {
                throw ApiException.forbidden("只能查看本部门的第三方配置");
            }
            return scope;
        }
        return deptId;
    }

    private Long resolveDeptForWrite(String requested)
    {
        Long requestedId = longOrNull(requested);
        Long scope = DeptScope.scopedDeptId();
        if (scope != null)
        {
            if (requestedId != null && !requestedId.equals(scope))
            {
                throw ApiException.forbidden("只能维护本部门的第三方配置");
            }
            return scope;
        }
        if (requestedId == null)
        {
            Long current = null;
            try
            {
                current = SecurityUtils.getDeptId();
            }
            catch (Exception ignored)
            {
                current = null;
            }
            if (current == null)
            {
                throw ApiException.validation("请指定归属主体部门");
            }
            return current;
        }
        return requestedId;
    }

    public void assertScope(Row profile)
    {
        Long scope = DeptScope.scopedDeptId();
        if (scope != null && !scope.equals(profile.lng("ownerDeptId")))
        {
            throw ApiException.forbidden("无权访问其它主体/部门的第三方配置");
        }
    }

    public Long currentUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            throw ApiException.unauthorized("登录状态已失效");
        }
    }

    private static String firstErrorMessage(List<Map<String, Object>> issues, String fallback)
    {
        for (Map<String, Object> issue : issues)
        {
            if ("ERROR".equals(issue.get("severity")))
            {
                return String.valueOf(issue.get("message"));
            }
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parseIssues(String json)
    {
        if (json == null || json.isBlank())
        {
            return List.of();
        }
        try
        {
            return (List<Map<String, Object>>) (List<?>) JSON.parseArray(json, Map.class);
        }
        catch (Exception e)
        {
            return List.of();
        }
    }

    private static Set<String> newLinkedSet(Set<String> source)
    {
        return new LinkedHashSet<>(source == null ? Set.of() : source);
    }

    private static String nullableId(Long id)
    {
        return id == null ? null : String.valueOf(id);
    }

    private static String blankToNull(String v)
    {
        return v == null || v.isBlank() ? null : v.trim();
    }

    public static long longOf(Object value, long fallback)
    {
        if (value == null)
        {
            return fallback;
        }
        try
        {
            return Long.parseLong(String.valueOf(value).trim());
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    public static Long longOrNull(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text))
        {
            return null;
        }
        try
        {
            return Long.valueOf(text);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static Map<String, Object> page(long total, int page, int size, List<Map<String, Object>> rows)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("pageNum", page);
        out.put("pageSize", size);
        out.put("rows", rows);
        return out;
    }

    /** 供模板服务复用的时间格式 */
    public static String iso(LocalDateTime time)
    {
        return ViTime.iso(time);
    }
}
