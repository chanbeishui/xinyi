package com.xinyi.rongli.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.domain.*;
import com.xinyi.rongli.integration.dto.IntegrationRequests.Query;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import static com.xinyi.rongli.integration.service.IntegrationViews.map;

/** 父记录及目录锁下原子发布；配置读取以主库指针为准。 */
@Service
public class IntegrationService {
    private final IntegrationMapper mapper;
    private final IntegrationAuthority auth;
    private final IntegrationInput input;
    private final IntegrationSecrets secrets;
    private final IntegrationCrypto crypto;
    private final IntegrationValidation validation;
    private final IntegrationViews views;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public IntegrationService(
            IntegrationMapper mapper,
            IntegrationAuthority auth,
            IntegrationInput input,
            IntegrationSecrets secrets,
            IntegrationCrypto crypto,
            IntegrationValidation validation,
            IntegrationViews views,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.auth = auth;
        this.input = input;
        this.secrets = secrets;
        this.crypto = crypto;
        this.validation = validation;
        this.views = views;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Page(int code, String msg, List<?> rows, long total) {}

    public Page list(Query q) {
        if (q.environment() != null) auth.environment(q.environment());
        var scope = auth.scope("biz:integration:query");
        return new Page(
                200,
                "操作成功",
                mapper.profiles(q, auth.environment, scope).stream()
                        .map(p -> views.profile(p, false))
                        .toList(),
                mapper.countProfiles(q, auth.environment, scope));
    }

    public Map<String, Object> detail(long id) {
        return views.profile(auth.profile(id, "biz:integration:query", false), true);
    }

    public Page history(long id, int offset, int size, boolean publications) {
        var p = auth.profile(id, "biz:integration:query", false);
        return publications
                ? new Page(
                        200,
                        "操作成功",
                        mapper.publications(id, offset, size).stream()
                                .map(views::publication)
                                .toList(),
                        mapper.countPublications(id))
                : new Page(
                        200,
                        "操作成功",
                        mapper.revisions(id, offset, size).stream()
                                .map(r -> views.revision(p, r))
                                .toList(),
                        mapper.countRevisions(id));
    }

    public Map<String, Object> create(Object body, String key) {
        var n = input.validate("IntegrationProfileCreate", body);
        return write(
                "create",
                0,
                key,
                n,
                () -> {
                    auth.environment(n.path("scope").path("environment").asText());
                    long dept =
                            auth.department(
                                    id(n.path("scope").path("ownerDeptId")),
                                    auth.scope("biz:integration:create"));
                    return replay(
                            "create",
                            0,
                            key,
                            n,
                            () -> {
                                var p = new IntegrationProfile();
                                p.setKind(n.path("kind").asText());
                                p.setDisplayName(text(n, "name"));
                                p.setOwnerDeptId(dept);
                                p.setEnvironment(auth.environment);
                                p.setCreatedBy(auth.actor().sysUserId());
                                p.setConfigKey(
                                        Set.of("PAYMENT_POLICY", "WECHAT_APP").contains(p.getKind())
                                                ? "DEFAULT"
                                                : UUID.randomUUID().toString());
                                try {
                                    mapper.insertProfile(p);
                                } catch (org.springframework.dao.DuplicateKeyException e) {
                                    throw new ViException(
                                            ViErrorCode.VI_STATE_CONFLICT, "该公司环境的单例配置已存在");
                                }
                                success(p.getId(), "INTEGRATION_CREATE", 0);
                                return views.profile(mapper.profile(p.getId()), true);
                            });
                });
    }

    /** 保存新的配置修订和凭据引用只移动草稿指针，运行时仍使用已发布版本。 */
    public Map<String, Object> draft(long id, Object body, String key) {
        var d = input.draft(body);
        var n = ViJson.tree(ViJson.write(body));
        return write(
                "draft",
                id,
                key,
                n,
                () -> {
                    var p = auth.profile(id, "biz:integration:edit", true);
                    auth.department(p.getOwnerDeptId(), auth.scope("biz:integration:edit"));
                    return replay(
                            "draft",
                            id,
                            key,
                            n,
                            () -> {
                                version(p, d.expectedVersion());
                                if (!p.getKind().equals(d.kind())) invalid("配置类型不可变更");
                                text(n, "reason");
                                var previous =
                                        p.getDraftRevisionId() == null
                                                ? null
                                                : mapper.revision(id, p.getDraftRevisionId());
                                var refs =
                                        secrets.mutate(
                                                p,
                                                previous,
                                                d.credentials(),
                                                auth.actor().sysUserId());
                                var held = new IntegrationRevision();
                                held.setSecretRefsJson(ViJson.write(refs));
                                for (String slot : refs.keySet()) {
                                    byte[] plain = secrets.read(p, held, slot);
                                    Arrays.fill(plain, (byte) 0);
                                }
                                String config = IntegrationInput.canonical(d.publicConfig());
                                // 避免误粘贴秘密至公开字段；合法的证书和公钥仍允许保存。
                                if (config.contains("PRIVATE KEY") || config.contains("BEGIN RSA"))
                                    invalid("秘密材料只能填写在专用凭据字段");
                                for (var mutation : d.credentials().values())
                                    if ("REPLACE".equals(mutation.action())
                                            && (config.contains(mutation.value())
                                                    || d.reason().contains(mutation.value())))
                                        invalid("凭据不得重复填写到公开字段或备注");
                                var r = new IntegrationRevision();
                                r.setProfileId(id);
                                r.setRevisionNo(p.getRevisionSeq() + 1);
                                r.setConfigJson(config);
                                r.setSecretRefsJson(ViJson.write(refs));
                                r.setContentHash(
                                        hash(
                                                config
                                                        + "\n"
                                                        + IntegrationInput.canonical(
                                                                ViJson.tree(
                                                                        r.getSecretRefsJson()))));
                                r.setCreatedBy(auth.actor().sysUserId());
                                r.setReason(d.reason());
                                var existing = mapper.revisionByHash(id, r.getContentHash());
                                if (existing == null) mapper.insertRevision(r);
                                else r = existing;
                                one(
                                        mapper.pointDraft(
                                                id, r.getId(), r.getRevisionNo(), p.getVersion()));
                                success(id, "INTEGRATION_DRAFT", p.getVersion() + 1);
                                return views.profile(mapper.profile(id), true);
                            });
                });
    }

    /** 校验结果绑定修订内容和目标版本，并设置有效期；本地通过不代表真实渠道接入成功。 */
    public Map<String, Object> check(long id, Object body, String key) {
        var n = input.validate("IntegrationCheckRequest", body);
        return write(
                "check",
                id,
                key,
                n,
                () -> {
                    var p = auth.profile(id, "biz:integration:check", true);
                    auth.department(p.getOwnerDeptId(), auth.scope("biz:integration:check"));
                    return replay(
                            "check",
                            id,
                            key,
                            n,
                            () -> {
                                version(p, n.path("expectedVersion").asLong());
                                var r = revision(p, id(n.path("revisionId")));
                                var result = validation.validate(p, r);
                                var c = new IntegrationCheck();
                                c.setTargetId(id);
                                c.setRevisionId(r.getId());
                                c.setTargetVersion(p.getVersion());
                                c.setContentHash(r.getContentHash());
                                c.setCheckLevel(n.path("level").asText());
                                c.setStatus(
                                        !result.passed()
                                                ? "FAIL"
                                                : "LOCAL".equals(c.getCheckLevel())
                                                        ? "PASS"
                                                        : "PARTIAL");
                                c.setIssuesJson(ViJson.write(result.issues()));
                                c.setCheckedAt(Instant.now());
                                c.setExpiresAt(c.getCheckedAt().plusSeconds(900));
                                c.setCheckedBy(auth.actor().sysUserId());
                                mapper.insertCheck(c);
                                audit.recordSuccess(
                                        auth.actor(),
                                        "VI_INTEGRATION_PROFILE",
                                        id,
                                        "INTEGRATION_CHECK",
                                        null,
                                        map("checkId", c.getId(), "status", c.getStatus()),
                                        "配置校验");
                                return views.check(c);
                            });
                });
    }

    /** 在统一写事务中重新核验版本、校验票据和依赖，再原子切换发布指针并保存审计。 */
    public Map<String, Object> publish(long id, Object body, String key, boolean rollback) {
        String action = rollback ? "rollback" : "publish";
        var n =
                input.validate(
                        rollback ? "IntegrationRollbackRequest" : "IntegrationPublishRequest",
                        body);
        return write(
                action,
                id,
                key,
                n,
                () -> {
                    var p = auth.profile(id, "biz:integration:publish", true);
                    auth.department(p.getOwnerDeptId(), auth.scope("biz:integration:publish"));
                    auth.recentAuthentication();
                    return replay(
                            action,
                            id,
                            key,
                            n,
                            () -> {
                                version(p, n.path("expectedVersion").asLong());
                                text(n, "reason");
                                var r =
                                        revision(
                                                p,
                                                id(
                                                        n.path(
                                                                rollback
                                                                        ? "targetRevisionId"
                                                                        : "revisionId")));
                                if (rollback
                                        ? mapper.wasPublished(id, r.getId()) == 0
                                        : !Objects.equals(p.getDraftRevisionId(), r.getId()))
                                    conflict("发布目标不是当前草稿或可回滚的历史版本");
                                if (!rollback
                                        && !r.getContentHash()
                                                .equals(n.path("contentHash").asText()))
                                    conflict("内容摘要已变化，请重新校验");
                                var c = mapper.check(id, id(n.path("checkId")));
                                if (c == null
                                        || !Objects.equals(c.getRevisionId(), r.getId())
                                        || !r.getContentHash().equals(c.getContentHash())
                                        || !Objects.equals(c.getTargetVersion(), p.getVersion())
                                        || !"PASS".equals(c.getStatus())
                                        || c.getExpiresAt() == null
                                        || !c.getExpiresAt().isAfter(Instant.now()))
                                    invalid("校验不通过、已过期或不对应当前版本，请重新校验");
                                var result = validation.validate(p, r);
                                if (!result.passed()) invalid("配置或依赖已失效，请重新校验");
                                if (n.has("expectedDependencyVersions")) {
                                    var expected = new TreeSet<String>();
                                    for (var dep : n.get("expectedDependencyVersions"))
                                        expected.add(IntegrationInput.canonical(dep));
                                    var actual = new TreeSet<String>();
                                    result.dependencies()
                                            .forEach(
                                                    dep ->
                                                            actual.add(
                                                                    IntegrationInput.canonical(
                                                                            ViJson.tree(
                                                                                    ViJson.write(
                                                                                            dep)))));
                                    if (!expected.equals(actual)) conflict("依赖版本已变化");
                                }
                                JsonNode config = ViJson.tree(r.getConfigJson());
                                if (!rollback
                                        && "PAYMENT_POLICY".equals(p.getKind())
                                        && !config.path("offlineEnabled").asBoolean()
                                        && !config.path("wechatEnabled").asBoolean()
                                        && !n.path("acknowledgeNoPaymentMethods").asBoolean())
                                    invalid("关闭所有支付方式须显式确认，将阻止新建订单");
                                identityFrozen(p, r);
                                one(
                                        mapper.pointActive(
                                                id,
                                                r.getId(),
                                                auth.actor().sysUserId(),
                                                p.getVersion()));
                                var pub = new IntegrationPublication();
                                pub.setTargetId(id);
                                pub.setRevisionId(r.getId());
                                pub.setPreviousRevisionId(p.getActiveRevisionId());
                                pub.setCheckId(c.getId());
                                pub.setAction(rollback ? "ROLLBACK" : "PUBLISH");
                                pub.setPublishedBy(auth.actor().sysUserId());
                                pub.setPublishedAt(Instant.now());
                                pub.setReason(n.path("reason").asText());
                                pub.setDependencyVersions(ViJson.write(result.dependencies()));
                                pub.setTargetVersion(p.getVersion() + 1);
                                mapper.insertPublication(pub);
                                success(id, "INTEGRATION_" + pub.getAction(), p.getVersion() + 1);
                                return views.publication(pub);
                            });
                });
    }

    /** 已被账号或支付使用的身份不能原地替换；轮换凭据不能顺带改变 AppID 或商户。 */
    private void identityFrozen(IntegrationProfile p, IntegrationRevision next) {
        if (p.getActiveRevisionId() == null) return;
        JsonNode old = ViJson.tree(revision(p, p.getActiveRevisionId()).getConfigJson()),
                now = ViJson.tree(next.getConfigJson());
        if ("WECHAT_APP".equals(p.getKind())
                && !old.path("appId").equals(now.path("appId"))
                && mapper.accountCount(old.path("appId").asText()) > 0)
            conflict("已有账号使用该小程序，不能原地替换AppID");
        if ("WECHAT_PAY".equals(p.getKind())
                && mapper.paymentCount(p.getId()) > 0
                && (!old.path("mchid").equals(now.path("mchid"))
                        || !old.path("appProfile")
                                .path("profileId")
                                .equals(now.path("appProfile").path("profileId"))))
            conflict("已有支付订单的商户身份不能原地替换");
    }

    public Page recipients(int offset, int size) {
        var scope = auth.scope("biz:integration:query");
        return new Page(
                200,
                "操作成功",
                mapper.recipients(auth.environment, scope, offset, size).stream()
                        .map(views::recipient)
                        .toList(),
                mapper.countRecipients(auth.environment, scope));
    }

    public Map<String, Object> recipientCreate(Object body, String key) {
        var n = input.validate("IntegrationTestRecipientInput", body);
        return write(
                "recipientCreate",
                0,
                key,
                n,
                () -> {
                    auth.environment(n.path("environment").asText());
                    long dept =
                            auth.department(
                                    id(n.path("ownerDeptId")),
                                    auth.scope("biz:integration:testrecipient"));
                    return replay(
                            "recipientCreate",
                            0,
                            key,
                            n,
                            () -> {
                                text(n, "reason");
                                var r = new IntegrationRecipient();
                                r.setName(text(n, "name"));
                                r.setKind(n.path("kind").asText());
                                r.setOwnerDeptId(dept);
                                r.setEnvironment(auth.environment);
                                r.setCreatedBy(auth.actor().sysUserId());
                                r.setReason(n.path("reason").asText());
                                if ("PHONE".equals(r.getKind())) {
                                    if (!n.path("phone").asText().matches("1[3-9][0-9]{9}"))
                                        invalid("测试手机号格式无效");
                                    r.setPhoneCipher(new byte[] {0});
                                } else {
                                    long appId = id(n.path("appProfileId"));
                                    var app =
                                            auth.profile(
                                                    appId, "biz:integration:testrecipient", true);
                                    if (!deptEquals(app, dept)
                                            || !"WECHAT_APP".equals(app.getKind())
                                            || app.getActiveRevisionId() == null)
                                        invalid("白名单账号须引用同公司已发布小程序");
                                    var cfg =
                                            ViJson.tree(
                                                    revision(app, app.getActiveRevisionId())
                                                            .getConfigJson());
                                    r.setAccountId(id(n.path("accountId")));
                                    if (!cfg.path("enabled").asBoolean()
                                            || !cfg.path("appId")
                                                    .asText()
                                                    .equals(mapper.accountApp(r.getAccountId())))
                                        invalid("账号与小程序不匹配或已停用");
                                    r.setAppProfileId(appId);
                                }
                                mapper.insertRecipient(r);
                                if ("PHONE".equals(r.getKind()))
                                    one(
                                            mapper.setRecipientCipher(
                                                    r.getId(),
                                                    crypto.envelope(
                                                            n.path("phone").asText(),
                                                            IntegrationViews.recipientAad(r))));
                                success(r.getId(), "INTEGRATION_RECIPIENT_CREATE", 0);
                                return views.recipient(mapper.recipient(r.getId()));
                            });
                });
    }

    public Map<String, Object> recipientRevoke(long id, Object body, String key) {
        var n = input.validate("TestRecipientRevoke", body);
        return write(
                "recipientRevoke",
                id,
                key,
                n,
                () -> {
                    var scope = auth.scope("biz:integration:testrecipient");
                    var r = mapper.lockRecipient(id);
                    if (r == null
                            || !r.getEnvironment().equals(auth.environment)
                            || scope != null && !scope.contains(r.getOwnerDeptId()))
                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    auth.department(r.getOwnerDeptId(), scope);
                    return replay(
                            "recipientRevoke",
                            id,
                            key,
                            n,
                            () -> {
                                text(n, "reason");
                                one(mapper.revokeRecipient(id, n.path("expectedVersion").asLong()));
                                success(
                                        id,
                                        "INTEGRATION_RECIPIENT_REVOKE",
                                        r.getVersion() + 1,
                                        n.path("reason").asText());
                                return views.recipient(mapper.recipient(id));
                            });
                });
    }

    private boolean deptEquals(IntegrationProfile p, long dept) {
        return p.getOwnerDeptId() == dept;
    }

    private IntegrationRevision revision(IntegrationProfile p, long id) {
        var r = mapper.revision(p.getId(), id);
        if (r == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return r;
    }

    /** 目录守卫与账号版本复核覆盖全部配置写入；失败审计在业务回滚后另行保留。 */
    private Map<String, Object> write(
            String action, long id, String key, JsonNode n, Supplier<Map<String, Object>> work) {
        var actor = auth.actor();
        try {
            return tx.execute(
                    status -> {
                        auth.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException e) {
            audit.recordFailure(
                    actor,
                    "VI_INTEGRATION",
                    id,
                    "INTEGRATION_REJECTED",
                    null,
                    map(
                            "errorCode",
                            e instanceof ViException v
                                    ? v.getErrorCode().name()
                                    : "VI_INTERNAL_ERROR"),
                    "配置操作未成功");
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    /** 幂等按操作者、动作、配置和请求内容隔离；重复点击不能产生第二次发布。 */
    private Map<String, Object> replay(
            String action, long id, String key, JsonNode n, Supplier<Map<String, Object>> work) {
        return idem.execute(
                IdempotencyService.subjectKey(auth.actor()),
                "INTEGRATION " + action + "/" + id,
                key,
                IntegrationInput.canonical(n),
                true,
                Map.class,
                () -> work.get());
    }

    private void success(long id, String action, long version) {
        success(id, action, version, "配置事务完成");
    }

    private void success(long id, String action, long version, String reason) {
        var summary = map("resourceId", Long.toString(id), "version", version);
        audit.recordSuccess(auth.actor(), "VI_INTEGRATION", id, action, null, summary, reason);
        outbox.publish(
                "integration:" + id + ":" + action + ":" + version,
                "VI_INTEGRATION",
                id,
                action,
                summary);
    }

    private static long id(JsonNode n) {
        try {
            return com.xinyi.rongli.core.ViId.parse(n.asText());
        } catch (RuntimeException e) {
            invalid("ID格式无效");
            return 0;
        }
    }

    private static String text(JsonNode n, String field) {
        String value = n.path(field).asText();
        if (value.isBlank()) invalid("必填字段不能仅含空白");
        return value;
    }

    private static void version(IntegrationProfile p, long expected) {
        if (p.getVersion() != expected) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void one(int n) {
        if (n != 1) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void invalid(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }

    private static void conflict(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }

    private static String hash(String s) {
        try {
            return HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
