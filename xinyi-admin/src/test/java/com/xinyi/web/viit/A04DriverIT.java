package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.service.MiniTokenService;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A04 真实 HTTP、双身份安全链、MyBatis 和隔离 MySQL 集成测试。 人员、公司、资格及履约事实为受保护测试夹具；不替换业务服务、权限或余额。
 * 本任务没有派单接受和实物交接接口，暂停后的安全约束直接核对持久事实，不冒充 H/D 全链通过。
 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=a04-isolated-phone-key",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class A04DriverIT extends PricingHttpFixture {
    private static final String BASE = "/api/admin/drivers";
    private static final String LIST = "biz:driver:list";
    private static final String QUERY = "biz:driver:query";
    private static final String EDIT = "biz:driver:edit";
    private static final String ASSIGN_COMPANY = "biz:driver:assigncompany";
    private static final String DISABLE = "biz:driver:disablenewassignments";
    private static final String ENABLE = "biz:driver:enablenewassignments";
    private static final String HISTORY = "biz:assignment:list";
    private static final String[] ALL = {
        LIST, QUERY, EDIT, ASSIGN_COMPANY, DISABLE, ENABLE, HISTORY
    };

    @Autowired MiniTokenService miniTokens;
    @Autowired ViSessionMapper sessions;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired IntegrationKeyProperties keys;
    @Autowired IntegrationCrypto auditCrypto;
    @Autowired PhoneCrypto phoneCrypto;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;

    String root;
    String companyA;
    String companyB;
    long driver;
    long account;

    @BeforeEach
    void prepareDriverPool() {
        // 父夹具先检查数据库名称与保护标记；各套件串行运行，不在业务数据库清理事实。
        for (String table :
                List.of(
                        "vi_driver_occupancy",
                        "vi_driver_assignment",
                        "vi_dispatch_invite",
                        "vi_driver_application",
                        "vi_driver_profile")) {
            jdbc.execute("DELETE FROM " + table);
        }
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        keys.setVersions(
                new HashMap<>(Map.of("a04-test", Base64.getEncoder().encodeToString(key))));
        keys.setCurrentVersion("a04-test");
        root = rootToken();
        companyA = scopedWithScope(COMPANY_A, "4", ALL);
        companyB = scopedWithScope(COMPANY_B, "4", ALL);
        driver = driver(COMPANY_A, "APPROVED", true);
        account = driverAccount(driver);
    }

    @Test
    void companyPoolUsesExplicitCompanyInsteadOfTheAdministratorsStationDepartment() {
        long other = driver(COMPANY_B, "APPROVED", true);
        JsonNode list = list(companyA, "");
        assertEquals(1, list.path("total").asInt());
        assertEquals(Long.toString(driver), list.path("rows").get(0).path("driverId").asText());
        assertFalse(list.toString().contains(Long.toString(other)));
        assertEquals(
                Long.toString(COMPANY_A), detail(driver, companyA).path("ownerDeptId").asText());
        // 管理范围仍取公司权威归属；站点主部门不是公司数据范围的替代。
        String siteOnly = scoped(SITE_DEPT, ALL);
        assertEquals(0, list(siteOnly, "").path("total").asInt());
        status(get(driver, siteOnly), 404);
    }

    @Test
    void rowAndTotalUseTheSameScopeAcrossSearchAndPagination() {
        driver(COMPANY_A, "SUBMITTED", false);
        driver(COMPANY_B, "APPROVED", true);
        JsonNode list = list(companyA, "?pageNum=1&pageSize=1");
        assertEquals(2, list.path("total").asInt());
        assertEquals(1, list.path("rows").size());
        JsonNode next = list(companyA, "?pageNum=2&pageSize=1");
        assertEquals(2, next.path("total").asInt());
        assertEquals(1, next.path("rows").size());
        assertNotEquals(
                list.path("rows").get(0).path("driverId"),
                next.path("rows").get(0).path("driverId"));
    }

    @Test
    void displayNameIsExplicitlyANicknameAndNoInventedScoreIsReturned() {
        JsonNode row = detail(driver, companyA);
        assertEquals("测试昵称非实名", row.path("name").asText());
        assertTrue(row.has("score"));
        assertTrue(row.get("score").isNull(), row.toString());
        assertFalse(row.toString().contains("sensitivePayload"));
        assertFalse(row.toString().contains("evidenceFileIds"));
    }

    @Test
    void unverifiedPhoneCipherIsNeverDisplayedAndVerifiedPhoneIsOnlyMasked() {
        String phone = "13800138000";
        jdbc.update(
                "UPDATE vi_account SET phone_cipher=?,phone_lookup=?,phone_verified_at=NULL WHERE"
                        + " id=?",
                phoneCrypto.encrypt(phone),
                phoneCrypto.lookup(phone),
                account);
        assertTrue(detail(driver, companyA).path("phoneMask").isNull());
        assertTrue(list(companyA, "").path("rows").get(0).path("phoneMask").isNull());
        assertEquals(0, list(companyA, "?keyword=" + phone).path("total").asInt());
        jdbc.update("UPDATE vi_account SET phone_verified_at=UTC_TIMESTAMP(3) WHERE id=?", account);
        JsonNode row = detail(driver, companyA);
        assertEquals("138****8000", row.path("phoneMask").asText());
        assertFalse(row.toString().contains(phone));
        assertEquals(1, list(companyA, "?keyword=" + phone).path("total").asInt());
    }

    @Test
    void unownedLegacyRowsAreVisibleOnlyWithExplicitGlobalAuthority() {
        long unowned = driver(null, "APPROVED", true);
        assertEquals(1, list(companyA, "").path("total").asInt());
        status(get(unowned, companyA), 404);
        assertTrue(detail(unowned, root).path("ownerDeptId").isNull());
    }

    @Test
    void companyScopeCannotClaimAnUnownedLegacyDriver() {
        long unowned = driver(null, "APPROVED", true);
        status(patch(unowned, Map.of("ownerDeptId", Long.toString(COMPANY_A)), companyA), 404);
        assertNull(field(unowned, "owner_dept_id", Long.class));
    }

    @Test
    void globalAuthorityClaimsLegacyRowWithoutGuessingEmployeeDepartment() {
        long unowned = driver(null, "APPROVED", true);
        JsonNode claimed =
                data(patch(unowned, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root));
        assertEquals(Long.toString(COMPANY_B), claimed.path("ownerDeptId").asText());
        assertEquals(0, field(unowned, "new_assignment_enabled", Integer.class));
        assertEquals(1, field(unowned, "listening_enabled", Integer.class));
        assertEquals(1, field(unowned, "version", Long.class));
    }

    @Test
    void ownershipRequiresSeparatePermissionEvenWhenEditingRemarksIsAllowed() {
        String editor = scopedWithScope(COMPANY_A, "4", EDIT, QUERY);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), editor), 403);
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
        data(patch(driver, Map.of("internalRemark", "只维护运营备注"), editor));
        assertEquals("只维护运营备注", field(driver, "internal_remark", String.class));
    }

    @Test
    void transferChecksTheUnionOfOldAndNewCompanyScopes() {
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), companyA), 403);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), companyB), 404);
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
        assertEquals(0, field(driver, "version", Long.class));
    }

    @Test
    void multiDepartmentAuthorityCanTransferAndTransferAlwaysDisablesNewWork() {
        String both = scoped(COMPANY_A, ALL);
        jdbc.update(
                "INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,?)",
                adminUser(both),
                COMPANY_B);
        JsonNode moved = data(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), both));
        assertEquals(Long.toString(COMPANY_B), moved.path("ownerDeptId").asText());
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
        assertEquals(1, field(driver, "listening_enabled", Integer.class));
        status(get(driver, companyA), 404);
        assertEquals(Long.toString(driver), detail(driver, companyB).path("driverId").asText());
    }

    @Test
    void transferRejectsMissingInactiveOrDeletedTargetDepartments() {
        status(patch(driver, Map.of("ownerDeptId", "9223372036854775807"), root), 422);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_B);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root), 422);
        jdbc.update("UPDATE sys_dept SET status='0',del_flag='2' WHERE dept_id=?", COMPANY_B);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root), 422);
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
    }

    @Test
    void ownershipCannotBeClearedOrForgedWithInternalScopeFields() {
        Map<String, Object> clear = new LinkedHashMap<>();
        clear.put("ownerDeptId", null);
        status(patch(driver, clear, root), 400);
        for (String key :
                List.of(
                        "accountId",
                        "params",
                        "visibleDeptIds",
                        "searchDeptIds",
                        "qualificationStatus")) {
            status(patch(driver, Map.of(key, "APPROVED"), root), 400);
        }
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
    }

    @Test
    void waitingInvitationBlocksTransferEvenWithoutAnOccupancyRow() {
        invitation(driver, false);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root), 409);
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
    }

    @Test
    void occupancyBlocksTransferEvenWithoutAnActiveAssignmentRow() {
        invitation(driver, true);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root), 409);
        assertEquals(
                1, count("SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?", driver));
    }

    @Test
    void activeAssignmentBlocksTransferEvenIfOccupancyWasLost() {
        assignment(driver, COMPANY_A, false, false);
        status(patch(driver, Map.of("ownerDeptId", Long.toString(COMPANY_B)), root), 409);
        assertEquals(COMPANY_A, field(driver, "owner_dept_id", Long.class));
    }

    @Test
    void remarksCannotChangeQualificationCredentialsListeningOrNewWorkSwitch() {
        for (String field :
                List.of(
                        "qualificationStatus",
                        "qualificationRevision",
                        "licenseValidTo",
                        "licenseLongTerm",
                        "licenseClass",
                        "reviewDueAt",
                        "listening",
                        "newAssignmentEnabled")) {
            status(patch(driver, Map.of(field, "APPROVED"), companyA), 400);
        }
        assertEquals("APPROVED", field(driver, "qualification_status", String.class));
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
        assertEquals(0, field(driver, "version", Long.class));
    }

    @Test
    void remarkUpdateIsVersionedAndDoesNotMutateAccountOrQualification() {
        JsonNode updated = data(patch(driver, Map.of("internalRemark", "需提前确认到站时间"), companyA));
        assertEquals("需提前确认到站时间", updated.path("internalRemark").asText());
        assertEquals(1, updated.path("version").asLong());
        assertEquals(
                "测试昵称非实名",
                jdbc.queryForObject(
                        "SELECT nickname FROM vi_account WHERE id=?", String.class, account));
        assertEquals("APPROVED", field(driver, "qualification_status", String.class));
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
    }

    @Test
    void staleVersionDoesNotOverwriteTheFirstOperatorsRemark() {
        Map<String, Object> first = maintenance(driver, Map.of("internalRemark", "第一条真实备注"));
        data(call("PATCH", BASE + "/" + driver, first, companyA));
        Map<String, Object> stale = new LinkedHashMap<>(first);
        stale.put("internalRemark", "过期覆盖");
        status(call("PATCH", BASE + "/" + driver, stale, companyA), 409);
        assertEquals("第一条真实备注", field(driver, "internal_remark", String.class));
    }

    @Test
    void concurrentSameVersionEditsProduceOneWinnerAndOneConflict() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Map<String, Object> a = maintenance(driver, Map.of("internalRemark", "并发甲"));
        Map<String, Object> b = maintenance(driver, Map.of("internalRemark", "并发乙"));
        try {
            var first =
                    pool.submit(
                            () -> {
                                start.await();
                                return call("PATCH", BASE + "/" + driver, a, companyA);
                            });
            var second =
                    pool.submit(
                            () -> {
                                start.await();
                                return call("PATCH", BASE + "/" + driver, b, companyA);
                            });
            start.countDown();
            List<Integer> results =
                    new ArrayList<>(
                            List.of(
                                    first.get(10, TimeUnit.SECONDS).getStatusCode().value(),
                                    second.get(10, TimeUnit.SECONDS).getStatusCode().value()));
            results.sort(Integer::compareTo);
            assertEquals(List.of(200, 409), results);
            assertEquals(1, field(driver, "version", Long.class));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void sameIdempotencyKeyReplaysOnceAndDifferentBodyConflicts() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = maintenance(driver, Map.of("internalRemark", "幂等备注"));
        String reason = "A04仅授权审计可读的维护说明";
        body.put("reason", reason);
        data(callKey("PATCH", BASE + "/" + driver, body, companyA, key));
        data(callKey("PATCH", BASE + "/" + driver, body, companyA, key));
        body.put("internalRemark", "同键不同意图");
        status(callKey("PATCH", BASE + "/" + driver, body, companyA, key), 409);
        assertEquals(1, field(driver, "version", Long.class));
        assertEquals(1, count("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=?", driver));
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                                + " action='DRIVER_UPDATED'",
                        driver));
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                                + " action='DRIVER_EDIT_DENIED'",
                        driver));
        byte[] envelope = auditEnvelope("DRIVER_UPDATED", "changeEnvelope");
        String aad = "A04:ADMIN:" + adminUser(companyA) + ":DRIVER_EDIT:" + driver;
        JsonNode changes = ViJson.tree(auditCrypto.unenvelope(envelope, aad));
        assertEquals(reason, changes.path("reason").asText());
        assertTrue(changes.path("beforeRemark").isNull());
        assertEquals("幂等备注", changes.path("afterRemark").asText());
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () -> auditCrypto.unenvelope(envelope, aad + "0"));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        auditCrypto.unenvelope(
                                envelope,
                                "A04:ADMIN:"
                                        + adminUser(companyA)
                                        + ":DRIVER_NEW_ASSIGNMENTS_ENABLED:"
                                        + driver));
        assertNoPlainAuditOrMessages(reason);
        assertNoPlainAuditOrMessages("幂等备注");
        JsonNode current = detail(driver, companyA);
        assertFalse(current.toString().contains(reason));
        assertFalse(current.has("changeEnvelope"));
        assertEquals("幂等备注", current.path("internalRemark").asText());
    }

    @Test
    void missingInvalidKeyVersionAndOversizedFieldsAreRejectedBeforeWriting() {
        Map<String, Object> body = maintenance(driver, Map.of("internalRemark", "合法"));
        status(callKey("PATCH", BASE + "/" + driver, body, companyA, null), 400);
        status(callKey("PATCH", BASE + "/" + driver, body, companyA, "not-a-uuid"), 400);
        for (Object invalid : List.of(-1, "0", 9007199254740992L)) {
            body.put("expectedVersion", invalid);
            status(call("PATCH", BASE + "/" + driver, body, companyA), 400);
        }
        body = maintenance(driver, Map.of("internalRemark", "长".repeat(1001)));
        status(call("PATCH", BASE + "/" + driver, body, companyA), 400);
        body = maintenance(driver, Map.of("internalRemark", "合法"));
        body.put("reason", "原".repeat(501));
        status(call("PATCH", BASE + "/" + driver, body, companyA), 400);
        assertEquals(0, field(driver, "version", Long.class));
    }

    @Test
    void disablePreservesPickedUpVehicleAssignmentOccupancyAndMoneyFacts() {
        long assignment = assignment(driver, SITE_DEPT, true, true);
        long order = orderOf(assignment);
        long slot = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_payment_order(order_id,out_trade_no,status,total_fen,paid_fen,refund_reserved_fen,refunded_fen,expires_at,payment_method,receiver_dept_id,capacity_resolution)"
                    + " VALUES"
                    + " (?,?,'SUCCESS',12345,12345,1000,500,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30"
                    + " MINUTE),'OFFLINE',?,'HELD')",
                order,
                "A04-PAY-" + order,
                COMPANY_A);
        jdbc.update(
                "INSERT INTO"
                    + " vi_quota_slot(id,quota_type,scope_key,starts_at,ends_at,total_quota,consumed_quota)"
                    + " VALUES (?,'PICKUP',?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " HOUR),2,1)",
                slot,
                "A04-" + slot);
        jdbc.update(
                "INSERT INTO vi_quota_reservation(order_id,slot_id,status) VALUES (?,?,'CONSUMED')",
                order,
                slot);
        Map<String, Object> before = jdbc.queryForMap("SELECT * FROM vi_order WHERE id=?", order);
        Map<String, Object> funds =
                jdbc.queryForMap("SELECT * FROM vi_payment_order WHERE order_id=?", order);
        Map<String, Object> quota =
                jdbc.queryForMap("SELECT * FROM vi_quota_slot WHERE id=?", slot);
        data(toggle(driver, false, companyA));
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
        assertEquals(1, field(driver, "listening_enabled", Integer.class));
        assertEquals(before, jdbc.queryForMap("SELECT * FROM vi_order WHERE id=?", order));
        assertEquals(
                funds, jdbc.queryForMap("SELECT * FROM vi_payment_order WHERE order_id=?", order));
        assertEquals(quota, jdbc.queryForMap("SELECT * FROM vi_quota_slot WHERE id=?", slot));
        assertEquals(
                "CONSUMED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_quota_reservation WHERE order_id=? AND slot_id=?",
                        String.class,
                        order,
                        slot));
        assertNull(
                jdbc.queryForObject(
                        "SELECT ended_at FROM vi_driver_assignment WHERE id=?",
                        Instant.class,
                        assignment));
        assertEquals(
                assignment,
                jdbc.queryForObject(
                        "SELECT holder_id FROM vi_driver_occupancy WHERE driver_id=?",
                        Long.class,
                        driver));
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type IN"
                                + " ('ASSIGNMENT_ENDED','REFUND_READY')"));
    }

    @Test
    void disablePreservesWaitingInvitationAndItsExactOccupancy() {
        long invite = invitation(driver, true);
        data(toggle(driver, false, companyA));
        assertEquals(
                "WAIT_RESPONSE",
                jdbc.queryForObject(
                        "SELECT status FROM vi_dispatch_invite WHERE id=?", String.class, invite));
        assertEquals(
                invite,
                jdbc.queryForObject(
                        "SELECT holder_id FROM vi_driver_occupancy WHERE driver_id=?",
                        Long.class,
                        driver));
        assertEquals(
                "INVITE",
                jdbc.queryForObject(
                        "SELECT holder_type FROM vi_driver_occupancy WHERE driver_id=?",
                        String.class,
                        driver));
    }

    @Test
    void disableDoesNotReleaseANewerAssignmentAfterHistoricalReturn() {
        long old = assignment(driver, SITE_DEPT, true, true);
        jdbc.update(
                "UPDATE vi_driver_assignment SET ended_at=UTC_TIMESTAMP(3),end_reason='RETURNED'"
                        + " WHERE id=?",
                old);
        jdbc.update("DELETE FROM vi_driver_occupancy WHERE driver_id=?", driver);
        long current = assignment(driver, SITE_DEPT, true, true);
        data(toggle(driver, false, companyA));
        assertEquals(
                current,
                jdbc.queryForObject(
                        "SELECT holder_id FROM vi_driver_occupancy WHERE driver_id=?",
                        Long.class,
                        driver));
        assertNull(
                jdbc.queryForObject(
                        "SELECT ended_at FROM vi_driver_assignment WHERE id=?",
                        Instant.class,
                        current));
    }

    @Test
    void disableAndFreshMiniSessionsKeepTheQualifiedRoleAndExistingResponsibility() {
        assignment(driver, SITE_DEPT, true, true);
        String before = mini(account);
        data(toggle(driver, false, companyA));
        JsonNode live = miniData("GET", "/api/common/session", null, before);
        assertTrue(live.path("availableRoles").toString().contains("DRIVER"), live.toString());
        JsonNode newSession = miniData("GET", "/api/common/session", null, mini(account));
        assertTrue(newSession.path("availableRoles").toString().contains("DRIVER"));
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
        assertEquals(
                1, count("SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?", driver));
    }

    @Test
    void enableRequiresItsOwnFunctionalPermission() {
        jdbc.update("UPDATE vi_driver_profile SET new_assignment_enabled=0 WHERE id=?", driver);
        String onlyDisable = scopedWithScope(COMPANY_A, "4", DISABLE, QUERY);
        status(toggle(driver, true, onlyDisable), 403);
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
        String enableReason = "A04已核实资格并恢复的私有说明";
        Map<String, Object> enableBody = maintenance(driver, Map.of());
        enableBody.put("reason", enableReason);
        String enableKey = UUID.randomUUID().toString();
        data(
                callKey(
                        "POST",
                        BASE + "/" + driver + "/enable-new-assignments",
                        enableBody,
                        companyA,
                        enableKey));
        data(
                callKey(
                        "POST",
                        BASE + "/" + driver + "/enable-new-assignments",
                        enableBody,
                        companyA,
                        enableKey));
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
        assertSwitchReason("DRIVER_NEW_ASSIGNMENTS_ENABLED", enableReason);
        String disableReason = "A04暂停新邀约但保留责任的私有说明";
        Map<String, Object> disableBody = maintenance(driver, Map.of());
        disableBody.put("reason", disableReason);
        String disableKey = UUID.randomUUID().toString();
        data(
                callKey(
                        "POST",
                        BASE + "/" + driver + "/disable-new-assignments",
                        disableBody,
                        companyA,
                        disableKey));
        data(
                callKey(
                        "POST",
                        BASE + "/" + driver + "/disable-new-assignments",
                        disableBody,
                        companyA,
                        disableKey));
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
        assertSwitchReason("DRIVER_NEW_ASSIGNMENTS_DISABLED", disableReason);
    }

    @Test
    void enableRequiresApprovedQualificationInsteadOfAnEmployeeRole() {
        for (String qualification :
                List.of("DRAFT", "SUBMITTED", "REJECTED", "EXPIRED", "SUSPENDED")) {
            jdbc.update(
                    "UPDATE vi_driver_profile SET qualification_status=?,new_assignment_enabled=0"
                            + " WHERE id=?",
                    qualification,
                    driver);
            stateConflict(toggle(driver, true, companyA));
            assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
            JsonNode session = miniData("GET", "/api/common/session", null, mini(account));
            assertFalse(
                    session.path("availableRoles").toString().contains("DRIVER"),
                    session.toString());
        }
    }

    @Test
    void enableRejectsMissingOrExpiredLicenseAndExpiredReview() {
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                    + " new_assignment_enabled=0,license_valid_to=NULL,license_long_term=0 WHERE"
                    + " id=?",
                driver);
        stateConflict(toggle(driver, true, companyA));
        jdbc.update(
                "UPDATE vi_driver_profile SET license_valid_to=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " DAY) WHERE id=?",
                driver);
        stateConflict(toggle(driver, true, companyA));
        jdbc.update(
                "UPDATE vi_driver_profile SET license_valid_to=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " YEAR),review_due_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 DAY) WHERE id=?",
                driver);
        stateConflict(toggle(driver, true, companyA));
        assertEquals(0, field(driver, "new_assignment_enabled", Integer.class));
    }

    @Test
    void enableAllowsAnExplicitLongTermLicenseAndDoesNotChangeListening() {
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                    + " new_assignment_enabled=0,listening_enabled=0,license_valid_to=NULL,license_long_term=1"
                    + " WHERE id=?",
                driver);
        data(toggle(driver, true, companyA));
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
        assertEquals(0, field(driver, "listening_enabled", Integer.class));
        assertEquals("APPROVED", field(driver, "qualification_status", String.class));
    }

    @Test
    void enableRejectsUnownedOrInactiveCompanyWithoutRemovingTheRoleOrTask() {
        long unowned = driver(null, "APPROVED", false);
        status(toggle(unowned, true, root), 422);
        jdbc.update("UPDATE vi_driver_profile SET new_assignment_enabled=0 WHERE id=?", driver);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_A);
        status(toggle(driver, true, root), 422);
        assertEquals("APPROVED", field(driver, "qualification_status", String.class));
    }

    @Test
    void assignmentsFilterEachOriginalOrderAndTotalRatherThanExposeAllDriverHistory() {
        long visible = assignment(driver, SITE_DEPT, false, false);
        end(visible, "RETURNED");
        long hidden = assignment(driver, COMPANY_B, false, false);
        end(hidden, "RETURNED");
        JsonNode rows =
                ViJson.tree(
                        call("GET", BASE + "/" + driver + "/assignments?pageSize=1", null, companyA)
                                .getBody());
        assertEquals(1, rows.path("total").asInt());
        assertEquals(
                Long.toString(visible), rows.path("rows").get(0).path("assignmentId").asText());
        assertFalse(rows.toString().contains(Long.toString(hidden)));
        status(call("GET", BASE + "/" + driver + "/assignments", null, companyB), 404);
    }

    @Test
    void completedCountUsesActualReturnAndRespectsHistoryScope() {
        long returned = assignment(driver, SITE_DEPT, false, false);
        end(returned, "RETURNED");
        long cancelled = assignment(driver, SITE_DEPT, false, false);
        end(cancelled, "DRIVER_CANCEL");
        long hidden = assignment(driver, COMPANY_B, false, false);
        end(hidden, "RETURNED");
        assignment(driver, SITE_DEPT, false, false);
        assertEquals(1, detail(driver, companyA).path("completedAssignmentCount").asInt());
    }

    @Test
    void historyRequiresAssignmentPermissionSeparatelyFromDriverDetails() {
        String onlyDetail = scopedWithScope(COMPANY_A, "4", QUERY);
        detail(driver, onlyDetail);
        status(call("GET", BASE + "/" + driver + "/assignments", null, onlyDetail), 403);
    }

    @Test
    void independentCompanyQueryAndSingleStationHistoryScopesAreBothHonored() {
        long otherSite = 994004L;
        jdbc.update(
                "INSERT INTO sys_dept(dept_id,parent_id,ancestors,dept_name,status,del_flag) VALUES"
                        + " (?,?,'0,994001','A04同公司另一站','0','0') ON DUPLICATE KEY UPDATE"
                        + " parent_id=VALUES(parent_id),status='0',del_flag='0'",
                otherSite,
                COMPANY_A);
        String token = perFunctionScope(Map.of(QUERY, COMPANY_A, HISTORY, SITE_DEPT));
        long visible = assignment(driver, SITE_DEPT, false, false);
        end(visible, "RETURNED");
        long sameCompanyHidden = assignment(driver, otherSite, false, false);
        end(sameCompanyHidden, "RETURNED");
        long otherCompanyHidden = assignment(driver, COMPANY_B, false, false);
        end(otherCompanyHidden, "RETURNED");
        long hiddenActive = assignment(driver, otherSite, true, true);
        JsonNode row = detail(driver, token);
        assertEquals(1, row.path("completedAssignmentCount").asInt());
        assertTrue(row.path("assignmentId").isNull());
        assertTrue(row.path("occupied").asBoolean());
        assertTrue(row.path("availability").path("holderId").isNull());
        ResponseEntity<String> response =
                call("GET", BASE + "/" + driver + "/assignments?pageSize=1", null, token);
        status(response, 200);
        JsonNode result = ViJson.tree(response.getBody());
        assertEquals(1, result.path("total").asInt());
        assertEquals(1, result.path("rows").size());
        assertEquals(
                Long.toString(visible), result.path("rows").get(0).path("assignmentId").asText());
        for (long hidden : List.of(sameCompanyHidden, otherCompanyHidden, hiddenActive)) {
            assertFalse(result.toString().contains(Long.toString(hidden)));
        }
    }

    @Test
    void emptyDepartmentScopeFailsClosedAndCannotUseAClientScopeOverride() {
        String empty = scopedWithScope(COMPANY_A, "5", ALL);
        assertEquals(0, list(empty, "").path("total").asInt());
        status(get(driver, empty), 404);
        status(patch(driver, Map.of("internalRemark", "越权"), empty), 404);
        for (String query :
                List.of("?params.dataScope=1=1", "?visibleDeptIds=1", "?searchDeptIds=1")) {
            status(call("GET", BASE + query, null, companyA), 400);
        }
    }

    @Test
    void missingFunctionsAreNotGrantedByCompanyScopeOrClientView() {
        String noPermission = scopedWithScope(COMPANY_A, "4", "biz:order:query");
        status(call("GET", BASE, null, noPermission), 403);
        status(get(driver, noPermission), 403);
        status(patch(driver, Map.of("internalRemark", "越权"), noPermission), 403);
        status(toggle(driver, false, noPermission), 403);
    }

    @Test
    void adminEndpointsRejectMissingLoginMiniTokensAndForgedClientRealm() {
        String mini = mini(account);
        for (String token : List.of("", mini)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Client-Type", "admin");
            if (!token.isEmpty()) headers.setBearerAuth(token);
            status(
                    http.exchange(
                            "http://127.0.0.1:" + port + BASE,
                            HttpMethod.GET,
                            new HttpEntity<>(null, headers),
                            String.class),
                    401);
            headers.set("Idempotency-Key", UUID.randomUUID().toString());
            status(
                    http.exchange(
                            "http://127.0.0.1:" + port + BASE + "/" + driver,
                            HttpMethod.PATCH,
                            new HttpEntity<>(
                                    maintenance(driver, Map.of("internalRemark", "伪造")), headers),
                            String.class),
                    401);
        }
        assertEquals(0, field(driver, "version", Long.class));
    }

    @Test
    void oldAuthVersionIsRejectedEvenWhenRedisEvictionFails() {
        doThrow(new IllegalStateException("隔离Redis删除故障")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                adminUser(companyA));
        status(get(driver, companyA), 401);
        status(toggle(driver, false, companyA), 401);
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
    }

    @Test
    void revokedCompanyMembershipCannotReuseAnEarlierAuthorizedDetail() {
        detail(driver, companyA);
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", adminUser(companyA));
        status(patch(driver, Map.of("internalRemark", "失权后保存"), companyA), 404);
        assertEquals(0, field(driver, "version", Long.class));
    }

    @Test
    void authorityRevocationAndPendingMaintenanceAreSerializedByTheDirectoryGuard()
            throws Exception {
        jdbc.update(
                "INSERT INTO vi_employee_directory_guard(guard_key) VALUES ('GLOBAL_DIRECTORY')"
                        + " ON DUPLICATE KEY UPDATE guard_key=guard_key");
        long user = adminUser(companyA);
        CountDownLatch authorityHeld = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            // 模拟统一授权事务先持目录守卫：在撤权提交后排队的维护不得沿用先前令牌授权。
            var revoked =
                    pool.submit(
                            () ->
                                    new TransactionTemplate(transactionManager)
                                            .execute(
                                                    status -> {
                                                        jdbc.queryForObject(
                                                                "SELECT id FROM"
                                                                    + " vi_employee_directory_guard"
                                                                    + " WHERE"
                                                                    + " guard_key='GLOBAL_DIRECTORY'"
                                                                    + " FOR UPDATE",
                                                                Long.class);
                                                        authorityHeld.countDown();
                                                        try {
                                                            assertTrue(
                                                                    release.await(
                                                                            5, TimeUnit.SECONDS));
                                                        } catch (InterruptedException error) {
                                                            Thread.currentThread().interrupt();
                                                            throw new IllegalStateException(error);
                                                        }
                                                        return jdbc.update(
                                                                "UPDATE sys_user SET"
                                                                    + " auth_version=auth_version+1"
                                                                    + " WHERE user_id=?",
                                                                user);
                                                    }));
            assertTrue(authorityHeld.await(3, TimeUnit.SECONDS));
            var write =
                    pool.submit(() -> patch(driver, Map.of("internalRemark", "撤权并发维护"), companyA));
            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> write.get(150, TimeUnit.MILLISECONDS));
            release.countDown();
            assertEquals(1, revoked.get(5, TimeUnit.SECONDS));
            status(write.get(5, TimeUnit.SECONDS), 401);
            assertEquals(0, field(driver, "version", Long.class));
            assertNull(field(driver, "internal_remark", String.class));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void outboxFailureRollsBackRemarkVersionAuditAndIdempotency() {
        doThrow(new IllegalStateException("隔离A04事件故障"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), anyString(), any());
        status(patch(driver, Map.of("internalRemark", "必须回滚"), companyA), 500);
        assertNull(field(driver, "internal_remark", String.class));
        assertEquals(0, field(driver, "version", Long.class));
        assertEquals(0, count("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=?", driver));
        assertEquals(0, count("SELECT COUNT(*) FROM vi_idempotency"));
    }

    @Test
    void databaseFailureRollsBackTheWholeMaintenanceIntent() {
        jdbc.execute(
                "ALTER TABLE vi_driver_profile ADD CONSTRAINT ck_a04_test_remark CHECK"
                        + " (internal_remark IS NULL)");
        try {
            status(patch(driver, Map.of("internalRemark", "触发隔离数据库约束"), companyA), 500);
            assertNull(field(driver, "internal_remark", String.class));
            assertEquals(0, field(driver, "version", Long.class));
            assertEquals(0, count("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=?", driver));
            assertEquals(0, count("SELECT COUNT(*) FROM vi_idempotency"));
        } finally {
            jdbc.execute("ALTER TABLE vi_driver_profile DROP CHECK ck_a04_test_remark");
        }
    }

    @Test
    void responseIsNoStoreAndMalformedIdentifiersSortAndPaginationAreRejected() {
        assertEquals("no-store", get(driver, companyA).getHeaders().getCacheControl());
        for (String suffix : List.of("/0", "/-1", "/9223372036854775808", "/1.5")) {
            status(call("GET", BASE + suffix, null, companyA), 400);
        }
        for (String query :
                List.of(
                        "?pageNum=0",
                        "?pageSize=101",
                        "?sort=owner_dept_id%3BDROP",
                        "?status=FAKE")) {
            status(call("GET", BASE + query, null, companyA), 400);
        }
    }

    @Test
    void enableRequiresAnExplicitLicenseClassAndFutureReviewButIncludesTodaysLicense() {
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                    + " new_assignment_enabled=0,license_class='',review_due_at=NULL WHERE id=?",
                driver);
        stateConflict(toggle(driver, true, companyA));
        jdbc.update("UPDATE vi_driver_profile SET license_class='C1' WHERE id=?", driver);
        stateConflict(toggle(driver, true, companyA));
        // 有效期按上海日历日判断，当日仍有效；复审截止是精确 UTC 时刻，不能混用日期截断。
        jdbc.update(
                "UPDATE vi_driver_profile SET review_due_at=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " DAY), license_valid_to=TIMESTAMP(DATE(DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 8"
                    + " HOUR)))-INTERVAL 8 HOUR WHERE id=?",
                driver);
        data(toggle(driver, true, companyA));
        assertEquals(1, field(driver, "new_assignment_enabled", Integer.class));
    }

    @Test
    void sixActualHttpResponsesConformToThePrimaryOpenApi() throws Exception {
        // 公司选择沿用仓库真实框架目录，不能让前端 API 夹具掩盖并不存在的树形路由。
        ResponseEntity<String> directory = call("GET", "/system/dept/list", null, root);
        status(directory, 200);
        JsonNode directoryBody = ViJson.tree(directory.getBody());
        assertEquals(200, directoryBody.path("code").asInt());
        Set<String> companyIds = new HashSet<>();
        for (JsonNode department : directoryBody.path("data")) {
            assertTrue(department.has("deptId"));
            assertTrue(department.has("parentId"));
            assertTrue(department.has("deptName"));
            assertTrue(department.has("status"));
            companyIds.add(department.path("deptId").asText());
        }
        assertTrue(companyIds.contains(Long.toString(COMPANY_A)));
        assertTrue(companyIds.contains(Long.toString(COMPANY_B)));
        ResponseEntity<String> deniedDirectory = call("GET", "/system/dept/list", null, companyA);
        // 旧框架目录沿用 HTTP 200/code 403；六个 A04 业务接口仍严格验证真实 HTTP 状态。
        status(deniedDirectory, 200);
        JsonNode deniedBody = ViJson.tree(deniedDirectory.getBody());
        assertEquals(403, deniedBody.path("code").asInt());
        assertFalse(deniedBody.has("data"));
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a04-contract-");
        Map<String, ResponseEntity<String>> responses = new LinkedHashMap<>();
        responses.put("list", call("GET", BASE, null, companyA));
        responses.put("detail", get(driver, companyA));
        responses.put("patch", patch(driver, Map.of("internalRemark", "契约采集备注"), companyA));
        responses.put("disable", toggle(driver, false, companyA));
        responses.put("enable", toggle(driver, true, companyA));
        long completed = assignment(driver, SITE_DEPT, false, false);
        end(completed, "RETURNED");
        responses.put(
                "assignments", call("GET", BASE + "/" + driver + "/assignments", null, companyA));
        for (var entry : responses.entrySet()) {
            status(entry.getValue(), 200);
            java.nio.file.Files.writeString(
                    dir.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        java.nio.file.Path repo =
                java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!java.nio.file.Files.exists(repo.resolve("docs/vehicle"))) repo = repo.getParent();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/a04_contract.py")
                                        .toString(),
                                repo.toString(),
                                dir.toString())
                        .redirectErrorStream(true)
                        .start();
        String output =
                new String(
                        process.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(process.waitFor(15, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    private long driver(Long company, String qualification, boolean enabled) {
        long accountId = SEQ.incrementAndGet();
        long id = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO vi_account(id,app_id,open_id,nickname,status) VALUES"
                        + " (?,'wx0000000000000000',?,'测试昵称非实名','ACTIVE')",
                accountId,
                "a04-" + UUID.randomUUID());
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES"
                        + " (?,UTC_TIMESTAMP(3))",
                accountId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_driver_profile(id,account_id,owner_dept_id,qualification_status,new_assignment_enabled,listening_enabled,license_class,license_valid_to,review_due_at)"
                    + " VALUES (?,?,?,?,?,1,'C1',DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " YEAR),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1 YEAR))",
                id,
                accountId,
                company,
                qualification,
                enabled ? 1 : 0);
        return id;
    }

    private long driverAccount(long id) {
        return field(id, "account_id", Long.class);
    }

    private byte[] auditEnvelope(String action, String field) {
        String summary =
                jdbc.queryForObject(
                        "SELECT after_summary FROM vi_event_log WHERE aggregate_id=? AND action=?",
                        String.class,
                        driver,
                        action);
        return Base64.getDecoder().decode(ViJson.tree(summary).path(field).asText());
    }

    private void assertSwitchReason(String action, String reason) {
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND action=?",
                        driver,
                        action));
        byte[] envelope = auditEnvelope(action, "reasonEnvelope");
        String aad = "A04:ADMIN:" + adminUser(companyA) + ":" + action + ":" + driver;
        assertEquals(reason, auditCrypto.unenvelope(envelope, aad));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () -> auditCrypto.unenvelope(envelope, aad + "0"));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        auditCrypto.unenvelope(
                                envelope,
                                "A04:ADMIN:" + adminUser(companyA) + ":DRIVER_EDIT:" + driver));
        assertNoPlainAuditOrMessages(reason);
        assertFalse(detail(driver, companyA).toString().contains(reason));
        assertFalse(detail(driver, companyA).has("reasonEnvelope"));
    }

    private void assertNoPlainAuditOrMessages(String fragment) {
        for (var row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log")) {
            for (Object value : row.values()) {
                assertFalse(java.util.Objects.toString(value, "").contains(fragment));
            }
        }
        for (String payload : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class)) {
            assertFalse(payload.contains(fragment));
        }
        for (String cached :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) {
            assertFalse(cached.contains(fragment));
        }
    }

    private <T> T field(long id, String name, Class<T> type) {
        // 列名仅取测试源码中的固定字面量，不能从接口输入进入 SQL。
        return jdbc.queryForObject(
                "SELECT " + name + " FROM vi_driver_profile WHERE id=?", type, id);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private long adminUser(String token) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId();
    }

    private String perFunctionScope(Map<String, Long> departments) {
        String initial = scoped(COMPANY_A, departments.keySet().toArray(String[]::new));
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + initial);
        var login = tokens.getLoginUser(request);
        List<SysRole> roles = new ArrayList<>();
        // 不同功能各有自定义角色范围；不能用一个公司树角色替代本站承接权限。
        for (var entry : departments.entrySet()) {
            SysRole role = new SysRole();
            role.setRoleId(SEQ.incrementAndGet());
            role.setRoleKey("A04_TEST");
            role.setStatus("0");
            role.setDataScope("2");
            role.setPermissions(Set.of(entry.getKey()));
            roles.add(role);
            jdbc.update(
                    "INSERT INTO sys_role_dept(role_id,dept_id) VALUES (?,?)",
                    role.getRoleId(),
                    entry.getValue());
        }
        login.getUser().setRoles(roles);
        login.setPermissions(new HashSet<>(departments.keySet()));
        request.addHeader("User-Agent", "a04-it");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {
            return tokens.createToken(login);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    private ResponseEntity<String> get(long id, String token) {
        return call("GET", BASE + "/" + id, null, token);
    }

    private JsonNode detail(long id, String token) {
        return data(get(id, token));
    }

    private JsonNode list(String token, String query) {
        ResponseEntity<String> response = call("GET", BASE + query, null, token);
        status(response, 200);
        return ViJson.tree(response.getBody());
    }

    private Map<String, Object> maintenance(long id, Map<String, Object> values) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expectedVersion", field(id, "version", Long.class));
        result.put("reason", "隔离测试：核实公司归属与运营维护");
        result.putAll(values);
        return result;
    }

    private ResponseEntity<String> patch(long id, Map<String, Object> values, String token) {
        return call("PATCH", BASE + "/" + id, maintenance(id, values), token);
    }

    private ResponseEntity<String> toggle(long id, boolean enable, String token) {
        return call(
                "POST",
                BASE + "/" + id + (enable ? "/enable-new-assignments" : "/disable-new-assignments"),
                maintenance(id, Map.of()),
                token);
    }

    private void status(ResponseEntity<String> response, int expected) {
        assertEquals(expected, response.getStatusCode().value(), response.getBody());
    }

    private void stateConflict(ResponseEntity<String> response) {
        status(response, 409);
        assertEquals(
                "VI_STATE_CONFLICT", ViJson.tree(response.getBody()).path("errorCode").asText());
    }

    private long order(long dept) {
        long id = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(id,order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,'{}','ACCEPTED','OWNER',?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'A04-test')",
                id,
                "A04-ORDER-" + id,
                account,
                SEQ.incrementAndGet(),
                dept,
                SEQ.incrementAndGet(),
                account);
        return id;
    }

    private long invitation(long id, boolean occupy) {
        long order = order(SITE_DEPT);
        long invite = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_dispatch_invite(id,order_id,driver_id,status,source,issued_at,expires_at)"
                    + " VALUES"
                    + " (?,?,?,'WAIT_RESPONSE','AUTO',UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 5 MINUTE))",
                invite,
                order,
                id);
        if (occupy) {
            jdbc.update(
                    "INSERT INTO"
                        + " vi_driver_occupancy(driver_id,holder_type,holder_id,order_id,expires_at)"
                        + " VALUES (?,'INVITE',?,?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 5 MINUTE))",
                    id,
                    invite,
                    order);
        }
        return invite;
    }

    private long assignment(long id, long dept, boolean pickedUp, boolean occupy) {
        long order = order(dept);
        long invite = SEQ.incrementAndGet();
        long assignment = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_dispatch_invite(id,order_id,driver_id,status,source,issued_at,expires_at,responded_at)"
                    + " VALUES"
                    + " (?,?,?,'ACCEPTED','AUTO',UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 5 MINUTE),UTC_TIMESTAMP(3))",
                invite,
                order,
                id);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                assignment,
                order,
                id,
                invite);
        jdbc.update(
                "UPDATE vi_order SET"
                    + " active_assignment_id=?,fulfillment_status=?,custody_type=?,custody_ref_id=?"
                    + " WHERE id=?",
                assignment,
                pickedUp ? "PICKED_UP" : "ACCEPTED",
                pickedUp ? "DRIVER" : "OWNER",
                pickedUp ? id : account,
                order);
        if (occupy) {
            jdbc.update(
                    "INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id)"
                            + " VALUES (?,'ASSIGNMENT',?,?)",
                    id,
                    assignment,
                    order);
        }
        return assignment;
    }

    private long orderOf(long assignment) {
        return jdbc.queryForObject(
                "SELECT order_id FROM vi_driver_assignment WHERE id=?", Long.class, assignment);
    }

    private void end(long assignment, String reason) {
        jdbc.update(
                "UPDATE vi_driver_assignment SET ended_at=UTC_TIMESTAMP(3),end_reason=? WHERE id=?",
                reason,
                assignment);
    }

    private String mini(long accountId) {
        ViSession session = new ViSession();
        session.setAccountId(accountId);
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole("OWNER");
        session.setAbsoluteExpiresAt(Instant.now().plusSeconds(3600));
        sessions.insert(session);
        return miniTokens.issueForSession(session).accessToken();
    }

    private JsonNode miniData(String method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", "owner");
        return data(
                http.exchange(
                        "http://127.0.0.1:" + port + path,
                        HttpMethod.valueOf(method),
                        new HttpEntity<>(body, headers),
                        String.class));
    }
}
