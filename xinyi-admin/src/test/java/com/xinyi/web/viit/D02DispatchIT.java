package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** D02 真实 HTTP/MySQL：公司池轮转、绝对邀约期限、真实双会话接受与精确占用。 时间只控制 viDeadlineClock；业务、余额、鉴权、加密和数据库约束均不 mock。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=d02-isolated-phone-key",
            "vi.payment.public-origin=https://isolated.invalid",
            "vi.outbox-relay-enabled=false",
            "vi.jobs.dispatch-enabled=true",
            "vi.jobs.dispatch-delay-ms=3600000",
            "vi.jobs.invitation-enabled=true",
            "vi.jobs.invitation-delay-ms=3600000",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class D02DispatchIT extends D02HttpFixture {
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;

    @Test
    void missingTokensAndWrongRealmsReturnReal401RatherThanSuccessfulEnvelope() {
        for (String path : List.of(INVITES, CURRENT)) {
            requireStatus(rawTraining("GET", path, null, null, key(), "driver"), 401);
            requireStatus(rawTraining("GET", path, null, rootAdmin, key(), "driver"), 401);
        }
        for (String path : List.of(ADMIN_DISPATCH + "/queue", dispatchPolicyPath(COMPANY_A))) {
            requireStatus(rawTraining("GET", path, null, null, key(), "admin"), 401);
            requireStatus(rawTraining("GET", path, null, driver.token(), key(), "admin"), 401);
        }
    }

    @Test
    void ownerRoleAndFakeClientIdentityCannotReadOrAnswerDriverInvites() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = invite(order);
        String owner = anotherSession(driver.accountId());
        requireStatus(rawTraining("GET", INVITES, null, owner, key(), "driver"), 403);
        requireStatus(
                rawTraining(
                        "POST",
                        INVITES + "/" + invitation.path("inviteId").asText() + "/response",
                        responseBody(ownInvite(invitation), "ACCEPT"),
                        owner,
                        key(),
                        "driver"),
                403);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void anotherDriverCannotEnumerateOrAnswerInvitesByChangingTheId() {
        PaidOrder order = automaticWaiting();
        JsonNode own = ownInvite(invite(order));
        Student other = additionalDriver(COMPANY_A);
        requireStatus(
                rawTraining(
                        "GET",
                        INVITES + "/" + own.path("inviteId").asText(),
                        null,
                        other.token(),
                        key(),
                        "driver"),
                404);
        requireStatus(
                rawTraining(
                        "POST",
                        INVITES + "/" + own.path("inviteId").asText() + "/response",
                        responseBody(own, "ACCEPT"),
                        other.token(),
                        key(),
                        "driver"),
                404);
        JsonNode list = page(rawTraining("GET", INVITES, null, other.token(), key(), "driver"));
        assertEquals(0, list.path("total").asInt());
    }

    @Test
    void missingCompanyPolicyIsReadOnlyAndNeverInventsOperationalDefaults() {
        long audits = countRows("SELECT COUNT(*) FROM vi_event_log");
        JsonNode policy = dispatchPolicy(COMPANY_A);
        assertFalse(policy.path("configured").asBoolean());
        assertEquals(0, policy.path("version").asLong());
        for (String field :
                List.of(
                        "inviteResponseSeconds",
                        "radiusMeter",
                        "expandedRadiusMeter",
                        "overallDeadlineSeconds")) assertTrue(policy.path(field).isNull(), field);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_policy"));
        assertEquals(audits, countRows("SELECT COUNT(*) FROM vi_event_log"));
    }

    @Test
    void policyReadAndWriteNeedBothDedicatedFunctionPermissions() {
        String read = scoped(COMPANY_A, POLICY_QUERY), edit = scoped(COMPANY_A, POLICY_EDIT);
        data(call("GET", dispatchPolicyPath(COMPANY_A), null, read));
        requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), dispatchPolicyBody(0), read), 403);
        requireStatus(call("GET", dispatchPolicyPath(COMPANY_A), null, edit), 403);
        requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), dispatchPolicyBody(0), edit), 403);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_policy"));
    }

    @Test
    void policyDepartmentScopeAndEmptySetFailClosedAndRecordDenials() {
        String actor = scoped(COMPANY_A, POLICY_QUERY, POLICY_EDIT);
        requireStatus(call("GET", dispatchPolicyPath(COMPANY_B), null, actor), 404);
        requireStatus(
                call("PUT", dispatchPolicyPath(COMPANY_B), dispatchPolicyBody(0), actor), 404);
        data(call("PUT", dispatchPolicyPath(COMPANY_A), dispatchPolicyBody(0), actor));
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(actor));
        requireStatus(call("GET", dispatchPolicyPath(COMPANY_A), null, actor), 404);
        requireStatus(
                call("PUT", dispatchPolicyPath(COMPANY_A), dispatchPolicyBody(1), actor), 404);
        assertEquals(1, dispatchPolicy(COMPANY_A).path("version").asLong());
        assertEquals(
                4,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE actor_user_id=? AND action LIKE"
                                + " 'DISPATCH_POLICY%_REJECTED'",
                        administratorId(actor)));
    }

    @Test
    void policyRejectsUnknownFieldsInternalScopeMalformedIdsAndNonIntegerValues() {
        for (String field :
                List.of(
                        "ownerDeptId",
                        "params",
                        "visibleDeptIds",
                        "configHash",
                        "updatedByUserId")) {
            Map<String, Object> body = dispatchPolicyBody(0);
            body.put(field, "1");
            requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 400);
        }
        for (Object invalid : List.of("60", 1.5, true)) {
            Map<String, Object> body = dispatchPolicyBody(0);
            body.put("inviteResponseSeconds", invalid);
            requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 400);
        }
        for (String target : List.of("01", "-1", "9223372036854775808"))
            requireStatus(call("GET", CONFIGS + target + "/dispatch-policy", null, rootAdmin), 400);
    }

    @Test
    void policyRequiresExplicitPositiveRangesExpansionAndKnownEnums() {
        for (Map.Entry<String, Object> invalid :
                List.<Map.Entry<String, Object>>of(
                        Map.entry("inviteResponseSeconds", 0),
                        Map.entry("inviteResponseSeconds", 3601),
                        Map.entry("radiusMeter", 0),
                        Map.entry("expandedRadiusMeter", 4999),
                        Map.entry("overallDeadlineSeconds", 86401))) {
            Map<String, Object> body = dispatchPolicyBody(0);
            body.put(invalid.getKey(), invalid.getValue());
            requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 422);
        }
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_policy"));
        for (Map.Entry<String, Object> invalid :
                List.<Map.Entry<String, Object>>of(
                        Map.entry("roundPolicy", "GLOBAL"),
                        Map.entry("distanceMode", "TENCENT_ROUTE"))) {
            Map<String, Object> body = dispatchPolicyBody(0);
            body.put(invalid.getKey(), invalid.getValue());
            requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 400);
        }
    }

    @Test
    void policyLicenseMatrixRejectsMissingDuplicateUnknownAndForgedMappings() {
        List<Object> invalid =
                List.of(
                        List.of(),
                        List.of(
                                Map.of(
                                        "vehicleTypeCode",
                                        "UNKNOWN",
                                        "licenseClasses",
                                        List.of("C1"))),
                        List.of(Map.of("vehicleTypeCode", "C04_CAR", "licenseClasses", List.of())),
                        List.of(
                                Map.of(
                                        "vehicleTypeCode",
                                        "C04_CAR",
                                        "licenseClasses",
                                        List.of("C1", "C1"))),
                        List.of(
                                Map.of(
                                        "vehicleTypeCode",
                                        "C04_CAR",
                                        "licenseClasses",
                                        List.of("C1")),
                                Map.of(
                                        "vehicleTypeCode",
                                        "C04_CAR",
                                        "licenseClasses",
                                        List.of("C2"))));
        for (Object matrix : invalid) {
            Map<String, Object> body = dispatchPolicyBody(0);
            body.put("licenseCompatibility", matrix);
            requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 422);
        }
        Map<String, Object> body = dispatchPolicyBody(0);
        body.put(
                "licenseCompatibility",
                List.of(
                        Map.of(
                                "vehicleTypeCode",
                                "C04_CAR",
                                "licenseClasses",
                                List.of("C1"),
                                "passed",
                                true)));
        requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin), 400);
    }

    @Test
    void policyCasUuidAndIdempotencyDoNotSilentlyOverwriteAnotherVersion() {
        Map<String, Object> original = dispatchPolicyBody(0);
        requireStatus(
                callKey("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin, null), 400);
        requireStatus(
                callKey("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin, "not-uuid"),
                400);
        String uuid = key();
        JsonNode first =
                data(callKey("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin, uuid));
        assertEquals(
                first,
                data(callKey("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin, uuid)));
        Map<String, Object> changed = new LinkedHashMap<>(original);
        changed.put("radiusMeter", 6000);
        requireStatus(callKey("PUT", dispatchPolicyPath(COMPANY_A), changed, rootAdmin, uuid), 409);
        requireError(
                call("PUT", dispatchPolicyPath(COMPANY_A), changed, rootAdmin),
                409,
                "VI_VERSION_CONFLICT");
        assertEquals(1, dispatchPolicy(COMPANY_A).path("version").asLong());
    }

    @Test
    void policyConcurrentUpdatesCommitOneVersionAndOneSuccessEvent() throws Exception {
        Map<String, Object> original = dispatchPolicyBody(0);
        List<ResponseEntity<String>> replies =
                concurrent(
                        () -> call("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin),
                        () -> call("PUT", dispatchPolicyPath(COMPANY_A), original, rootAdmin));
        assertStatuses(replies, 200, 409);
        assertEquals(1, dispatchPolicy(COMPANY_A).path("version").asLong());
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE"
                                + " action='DISPATCH_POLICY_CHANGED'"));
    }

    @Test
    void realOrderFreezesServiceCompanyMatchingHistoricalPaymentReceiver() {
        JsonNode policy = configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        assertEquals(Long.toString(COMPANY_A), orderField(order, "service_company_id"));
        assertEquals(Long.toString(COMPANY_A), paymentField(order, "receiver_dept_id"));
        assertEquals(policy.path("version").asText(), orderField(order, "dispatch_policy_version"));
        assertNotNull(orderField(order, "dispatch_policy_snapshot"));
        assertEquals(
                2,
                countRows(
                        "SELECT COUNT(*) FROM vi_quota_reservation WHERE order_id=? AND"
                                + " status='CONFIRMED'",
                        order.id()));
        byte[] rotated = new byte[32];
        new java.security.SecureRandom().nextBytes(rotated);
        Map<String, String> retained = new HashMap<>(integrationKeys.getVersions());
        retained.put("d02-rotated", Base64.getEncoder().encodeToString(rotated));
        integrationKeys.setVersions(retained);
        integrationKeys.setCurrentVersion("d02-rotated");
        PaidOrder afterRotation = unpaidOrder(dispatchStation, pickupSlot);
        assertEquals(
                policy.path("version").asText(),
                orderField(afterRotation, "dispatch_policy_version"));
        assertEquals(Long.toString(COMPANY_A), orderField(afterRotation, "service_company_id"));
        // 运营内容认证不能依赖当前加密root版本；真正改库而不重签仍必须拒绝新建单。
        jdbc.update(
                "UPDATE vi_dispatch_policy SET radius_meter=radius_meter+1 WHERE owner_dept_id=?",
                COMPANY_A);
        requireError(
                createOrderAttempt(dispatchStation, pickupSlot, newMini(true)),
                422,
                "VI_CONFIG_INVALID");
        assertEquals(2, countRows("SELECT COUNT(*) FROM vi_order"));
        dispatchNow();
        assertEquals(
                "WAIT_RESPONSE", invite(order).path("status").asText(), "旧单仍按自身携带旧key版本的认证快照执行");
    }

    @Test
    void unconfiguredOrderStillBuildsAndPaysButDispatchRequiresManualCoordination() {
        readyDriver();
        PaidOrder order = paidOrder();
        assertNull(orderField(order, "dispatch_policy_snapshot"));
        dispatchNow();
        assertEquals("MANUAL_DISPATCH", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertEquals("SUCCESS", paymentField(order, "status"));
    }

    @Test
    void missingHistoricalServiceCompanyNeverGuessesCurrentStationOrReceivingCompany() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        jdbc.update("UPDATE vi_order SET service_company_id=NULL WHERE id=?", order.id());
        dispatchNow();
        assertEquals("MANUAL_DISPATCH", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertNull(orderField(order, "service_company_id"));
    }

    @Test
    void policyChangesDoNotRewriteExistingOrderOrUseCurrentRadiusForItsBatch() {
        JsonNode oldPolicy = configureDispatch();
        controlledNow.set(pickupStarts.minusSeconds(1801));
        readyDriver();
        PaidOrder old = paidOrder();
        assertEquals("SCHEDULED", orderField(old, "fulfillment_status"));
        String originalDispatchAt = orderField(old, "dispatch_at");
        String originalConfigVersion = orderField(old, "dispatch_config_version");
        JsonNode global = data(call("GET", "/api/admin/config", null, rootAdmin));
        JsonNode changedGlobal = global;
        for (String lead : List.of("20", "15", "15")) {
            long previous = changedGlobal.path("version").asLong();
            changedGlobal =
                    data(
                            call(
                                    "PUT",
                                    "/api/admin/config",
                                    globalLeadBody(
                                            previous,
                                            List.of(
                                                    Map.of(
                                                            "key",
                                                            "vi.biz.dispatch.lead.minutes",
                                                            "value",
                                                            lead))),
                                    rootAdmin));
            assertEquals(previous + 1, changedGlobal.path("version").asLong());
            assertEquals(
                    1,
                    countRows(
                            "SELECT COUNT(*) FROM sys_config WHERE"
                                + " config_key='vi.biz.dispatch.lead.minutes'"),
                    "两次修改和同值保存都必须更新唯一原行，不能追加同名键");
            assertEquals(
                    lead,
                    jdbc.queryForObject(
                            "SELECT config_value FROM sys_config WHERE"
                                + " config_key='vi.biz.dispatch.lead.minutes'",
                            String.class));
        }
        long stableVersion = changedGlobal.path("version").asLong();
        int auditBefore =
                countRows("SELECT COUNT(*) FROM vi_event_log WHERE action='CONFIG_UPDATE'");
        int outboxBefore =
                countRows("SELECT COUNT(*) FROM vi_outbox WHERE event_type='CONFIG_UPDATED'");
        requireStatus(
                call(
                        "PUT",
                        "/api/admin/config",
                        globalLeadBody(
                                stableVersion,
                                List.of(
                                        Map.of(
                                                "key",
                                                "vi.biz.dispatch.lead.minutes",
                                                "value",
                                                "15"),
                                        Map.of(
                                                "key",
                                                "vi.biz.dispatch.lead.minutes",
                                                "value",
                                                "16"))),
                        rootAdmin),
                400);
        String duplicateName = "__D02_duplicate_lead__" + SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO sys_config(config_name,config_key,config_value,config_type)"
                    + " VALUES(?,'vi.biz.dispatch.lead.minutes','99','N')",
                duplicateName);
        long duplicateId =
                jdbc.queryForObject(
                        "SELECT config_id FROM sys_config WHERE config_name=?",
                        Long.class,
                        duplicateName);
        try {
            requireError(
                    call(
                            "PUT",
                            "/api/admin/config",
                            globalLeadBody(
                                    stableVersion,
                                    List.of(
                                            Map.of(
                                                    "key",
                                                    "vi.biz.dispatch.lead.minutes",
                                                    "value",
                                                    "18"))),
                            rootAdmin),
                    422,
                    "VI_CONFIG_INVALID");
            assertEquals(
                    List.of("15", "99"),
                    jdbc.queryForList(
                            "SELECT config_value FROM sys_config WHERE"
                                + " config_key='vi.biz.dispatch.lead.minutes' ORDER BY config_id",
                            String.class));
            assertEquals(
                    Long.toString(stableVersion),
                    jdbc.queryForObject(
                            "SELECT config_value FROM sys_config WHERE"
                                + " config_key='vi.config.version'",
                            String.class));
            assertEquals(
                    auditBefore,
                    countRows("SELECT COUNT(*) FROM vi_event_log WHERE action='CONFIG_UPDATE'"));
            assertEquals(
                    outboxBefore,
                    countRows("SELECT COUNT(*) FROM vi_outbox WHERE event_type='CONFIG_UPDATED'"));
        } finally {
            // 只移除本用例精确创建的异常行，不猜修任何原参数，也不绕过真实C01保存。
            jdbc.update(
                    "DELETE FROM sys_config WHERE config_id=? AND config_name=?",
                    duplicateId,
                    duplicateName);
        }
        Map<String, Object> changed = dispatchPolicyBody(oldPolicy.path("version").asLong());
        changed.put("radiusMeter", 1);
        changed.put("expandedRadiusMeter", 1);
        changed.put("inviteResponseSeconds", 10);
        JsonNode current = data(call("PUT", dispatchPolicyPath(COMPANY_A), changed, rootAdmin));
        controlledNow.set(pickupStarts.minusSeconds(1800));
        dispatchNow();
        JsonNode invitation = invite(old);
        assertEquals(
                60,
                Duration.between(
                                instant(invitation.path("issuedAt")),
                                instant(invitation.path("expiresAt")))
                        .getSeconds());
        assertEquals(
                oldPolicy.path("version").asText(), orderField(old, "dispatch_policy_version"));
        assertEquals(originalDispatchAt, orderField(old, "dispatch_at"));
        assertEquals(originalConfigVersion, orderField(old, "dispatch_config_version"));
        PaidOrder fresh = unpaidOrder(dispatchStation, pickupSlot);
        assertEquals(
                current.path("version").asText(), orderField(fresh, "dispatch_policy_version"));
        assertEquals(
                changedGlobal.path("version").asText(),
                orderField(fresh, "dispatch_config_version"));
        LocalDateTime oldPlan =
                jdbc.queryForObject(
                        "SELECT dispatch_at FROM vi_order WHERE id=?",
                        LocalDateTime.class,
                        old.id());
        LocalDateTime newPlan =
                jdbc.queryForObject(
                        "SELECT dispatch_at FROM vi_order WHERE id=?",
                        LocalDateTime.class,
                        fresh.id());
        assertEquals(utc(pickupStarts).minusMinutes(30), oldPlan);
        assertEquals(utc(pickupStarts).minusMinutes(15), newPlan);
        assertEquals(oldPlan.plusMinutes(15), newPlan);
        dispatchNow();
        assertEquals(
                1, countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", old.id()));
    }

    @Test
    void stationTransferDoesNotMoveOldOrderIntoAnotherCompanyDriverPool() {
        configureDispatch();
        controlledNow.set(pickupStarts.minusSeconds(1801));
        readyDriver();
        PaidOrder old = paidOrder();
        assertEquals("SCHEDULED", orderField(old, "fulfillment_status"));
        jdbc.update("UPDATE vi_station SET dept_id=? WHERE id=?", COMPANY_B, dispatchStation);
        controlledNow.set(pickupStarts.minusSeconds(1800));
        dispatchNow();
        JsonNode invitation = invite(old);
        assertEquals(Long.toString(COMPANY_A), invitation.path("serviceCompanyId").asText());
        assertEquals(
                driver.driverId(),
                jdbc.queryForObject(
                        "SELECT driver_id FROM vi_dispatch_invite WHERE order_id=?",
                        Long.class,
                        old.id()));
        assertEquals(Long.toString(COMPANY_A), orderField(old, "service_company_id"));
        assertEquals(
                "ACCEPTED", data(respond(ownInvite(invitation), "ACCEPT")).path("status").asText());
        assertEquals(Long.toString(SITE_DEPT), orderField(old, "dept_id"));
    }

    @Test
    void scheduledPaymentBeforeFrozenDispatchAtCannotInviteAndAtDeadlineIssuesOnlyOnce() {
        configureDispatch();
        readyDriver();
        controlledNow.set(pickupStarts.minusSeconds(1801));
        PaidOrder order = paidOrder();
        dispatchNow();
        assertEquals("SCHEDULED", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        controlledNow.set(pickupStarts.minusSeconds(1800));
        dispatchNow();
        dispatchNow();
        assertEquals("WAIT_RESPONSE", invite(order).path("status").asText());
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void paymentAfterFrozenPlanImmediatelyEntersCurrentBatchWithoutNextDayDelay() {
        configureDispatch();
        controlledNow.set(pickupStarts.minusSeconds(1200));
        // 本例验证晚付款而非十分钟前已过期的候选；在实际付款时明确报告真实新鲜点与可达。
        readyDriver();
        PaidOrder order = paidOrder();
        dispatchNow();
        assertEquals("WAIT_RESPONSE", invite(order).path("status").asText());
        assertEquals(now(), instant(invite(order).path("issuedAt")));
    }

    @Test
    void unpaidOrderNeverCreatesBatchInvitationOrDriverOccupancy() {
        configureDispatch();
        readyDriver();
        PaidOrder order = unpaidOrder(dispatchStation, pickupSlot);
        dispatchNow();
        assertEquals("PENDING_PAY", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_batch WHERE order_id=?", order.id()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_occupancy"));
    }

    @Test
    void automaticInviteOccupiesDriverButDoesNotPretendHeAlreadyAccepted() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = invite(order);
        assertEquals("AUTO", invitation.path("source").asText());
        assertEquals("WAIT_RESPONSE", invitation.path("status").asText());
        assertEquals("DISPATCHING", orderField(order, "fulfillment_status"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_type='INVITE' AND holder_id=? AND order_id=?",
                        driver.driverId(),
                        invitation.path("inviteId").asLong(),
                        order.id()));
        assertTrue(data(driverCall("GET", CURRENT, null)).isNull());
    }

    @Test
    void companyCursorSharedAcrossStationsAdvancesOnlyOnCommittedAutomaticInvites() {
        configureDispatch();
        readyDriver();
        Student second = additionalDriver(COMPANY_A);
        readyDriver(second);
        long station2 = supplyStation(SITE_DEPT), slot2 = supplySlots(station2);
        PaidOrder first = paidOrder();
        dispatchNow();
        long chosenFirst =
                jdbc.queryForObject(
                        "SELECT driver_id FROM vi_dispatch_invite WHERE order_id=?",
                        Long.class,
                        first.id());
        PaidOrder anotherStation = paidOrder(station2, slot2);
        dispatchNow();
        long chosenSecond =
                jdbc.queryForObject(
                        "SELECT driver_id FROM vi_dispatch_invite WHERE order_id=?",
                        Long.class,
                        anotherStation.id());
        assertNotEquals(chosenFirst, chosenSecond);
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_cursor WHERE scope_key=?",
                        "company:" + COMPANY_A));
        assertEquals(
                chosenSecond,
                jdbc.queryForObject(
                        "SELECT last_driver_id FROM vi_dispatch_cursor WHERE scope_key=?",
                        Long.class,
                        "company:" + COMPANY_A));
        assertEquals(
                0, countRows("SELECT COUNT(*) FROM vi_dispatch_cursor WHERE scope_key='GLOBAL'"));
    }

    @Test
    void crossCompanyDriverIsExcludedEvenWithFreshPositionAndManualSelection() {
        configureDispatch();
        Student foreign = additionalDriver(COMPANY_B);
        configure(COMPANY_B, "23:59");
        readyDriver(foreign);
        PaidOrder order = paidOrder();
        assertEquals(0, candidates(order).path("total").asInt());
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, foreign),
                        rootAdmin),
                404);
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void freshBackgroundPresenceKeepsListeningAndCanReceiveAnInvite() {
        configureDispatch();
        readyDriver();
        controlledNow.set(now().plusMillis(1));
        contact("BACKGROUND", now());
        PaidOrder order = paidOrder();
        dispatchNow();
        assertEquals("WAIT_RESPONSE", invite(order).path("status").asText());
        assertTrue(availability().path("listeningEnabled").asBoolean());
    }

    @Test
    void stalePositionAndReachabilityNeverBecomeCandidatesJustBecauseListeningIsOn() {
        configureDispatch();
        readyDriver();
        controlledNow.set(now().plusSeconds(121));
        PaidOrder order = paidOrder();
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertTrue(availability().path("listeningEnabled").asBoolean());
    }

    @Test
    void twoDifferentDeviceSessionsCannotContributeHalfOfCandidateEvidence() {
        configureDispatch();
        listening(true);
        locate(now());
        Student second = driverSession(driver.accountId());
        Student saved = driver;
        driver = second;
        try {
            contact("FOREGROUND", now());
        } finally {
            driver = saved;
        }
        PaidOrder order = paidOrder();
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void revokedSourceSessionCannotSupplyCandidatesToAnOtherwiseActiveAccount() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE id=?",
                sessionId(driver));
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void expiredSourceSessionAndChangedContextDoNotSurviveInCandidateCache() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        long session = sessionId(driver);
        jdbc.update("UPDATE vi_session SET absolute_expires_at=? WHERE id=?", utc(now()), session);
        assertEquals(0, candidates(order).path("total").asInt());
        jdbc.update(
                "UPDATE vi_session SET absolute_expires_at=?,context_version=context_version+1"
                        + " WHERE id=?",
                utc(now().plusSeconds(3600)),
                session);
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void accountSecurityEpochChangeInvalidatesSourceFactsWithoutRedisCleanup() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        doThrow(new IllegalStateException("D02隔离Redis故障")).when(redis).deleteObject(anyString());
        try {
            jdbc.update(
                    "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE"
                            + " account_id=?",
                    driver.accountId());
            assertEquals(0, candidates(order).path("total").asInt());
            dispatchNow();
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?",
                            order.id()));
        } finally {
            reset(redis);
        }
    }

    @Test
    void administrativePauseBlocksAutomaticAndManualWithoutReleasingCurrentResponsibility() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        data(
                call(
                        "POST",
                        "/api/admin/drivers/" + driver.driverId() + "/disable-new-assignments",
                        Map.of("expectedVersion", profileVersion(driver), "reason", "D02真实行政暂停"),
                        rootAdmin));
        assertEquals(0, candidates(order).path("total").asInt());
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        rootAdmin),
                409);
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertTrue(availability().path("listeningEnabled").asBoolean());
    }

    @Test
    void qualificationExpiryIsRecheckedByAutomaticAndManualDispatchBeforeExpiryJobRuns() {
        configureDispatch();
        Submitted applicant = submitted(COMPANY_A);
        Map<String, Object> approval = approvalBody(adminDetail(applicant));
        approval.put("reviewDueAt", now().plusSeconds(60).toString());
        data(audit(applicant, approval, rootAdmin));
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + applicant.person().driverId()
                                + "/enable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(applicant.person()),
                                "reason",
                                "明确启用有即将到期资格的真实司机"),
                        rootAdmin));
        Student expiring = switchToDriver(applicant.person());
        extendTestSession(expiring);
        Student saved = driver;
        try {
            driver = expiring;
            readyDriver();
            PaidOrder inHand = paidOrder();
            dispatchNow();
            JsonNode responsibility = data(respond(ownInvite(invite(inHand)), "ACCEPT"));
            controlledNow.set(now().plusSeconds(60));
            PaidOrder order = paidOrder();
            assertEquals(
                    "REVIEW_EXPIRED_OR_UNKNOWN",
                    availability().path("qualificationReason").asText());
            assertEquals(0, candidates(order).path("total").asInt());
            requireStatus(
                    call(
                            "POST",
                            ADMIN_DISPATCH + "/manual-invites",
                            manualBody(order, driver),
                            rootAdmin),
                    409);
            dispatchNow();
            assertEquals(
                    inHand.no(), data(driverCall("GET", CURRENT, null)).path("orderNo").asText());
            assertEquals(
                    1,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                    + " holder_type='ASSIGNMENT' AND holder_id=? AND order_id=?",
                            driver.driverId(),
                            responsibility.path("assignmentId").asLong(),
                            inHand.id()));
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?",
                            order.id()));
        } finally {
            driver = saved;
        }
    }

    @Test
    void activePenaltyAndStoppedListeningAreBothRealCandidateGates() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        // D05 尚后续：仅消费已存在的处罚事实，不声称本例验证取消计次/申诉生成。
        jdbc.update(
                "INSERT INTO"
                    + " vi_penalty(driver_id,assignment_id,business_date,penalty_type,status,reason,rule_version,effective_until)"
                    + " VALUES(?,?,?,'STOP_TODAY','ACTIVE','D02处罚消费者边界','D02_BOUNDARY',?)",
                driver.driverId(),
                SEQ.incrementAndGet(),
                now().atZone(SHANGHAI).toLocalDate(),
                utc(now().plusSeconds(300)));
        assertEquals(0, candidates(order).path("total").asInt());
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        rootAdmin),
                409);
        jdbc.update("UPDATE vi_penalty SET status='REVOKED' WHERE driver_id=?", driver.driverId());
        listening(false);
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void companyEpochRoundTripCannotRevivePreviouslyTrustedPositionOrReachability() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        assignCompany(driver, COMPANY_B);
        assignCompany(driver, COMPANY_A);
        assertFalse(availability().path("listeningEnabled").asBoolean());
        assertEquals(
                "QUALIFICATION_COMPANY_CHANGED",
                availability().path("qualificationReason").asText());
        assertEquals(0, candidates(order).path("total").asInt());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void acceptRechecksSourceDeviceRevocationEvenWhenTheAcceptingDeviceIsStillValid() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = invite(order);
        Student another = driverSession(driver.accountId());
        JsonNode observed =
                data(
                        rawTraining(
                                "GET",
                                INVITES + "/" + invitation.path("inviteId").asText(),
                                null,
                                another.token(),
                                key(),
                                "driver"));
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE id=?",
                sessionId(driver));
        requireStatus(
                rawTraining(
                        "POST",
                        INVITES + "/" + invitation.path("inviteId").asText() + "/response",
                        responseBody(observed, "ACCEPT"),
                        another.token(),
                        key(),
                        "driver"),
                409);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void policyReasonAuditIsAuthenticatedOnceAndNeverLeakedIntoBusinessOutputs() {
        String reason = "D02公司策略专用加密原因";
        Map<String, Object> body = dispatchPolicyBody(0);
        body.put("reason", reason);
        String uuid = key();
        JsonNode policy =
                data(callKey("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin, uuid));
        data(callKey("PUT", dispatchPolicyPath(COMPANY_A), body, rootAdmin, uuid));
        String summary =
                jdbc.queryForObject(
                        "SELECT after_summary FROM vi_event_log WHERE"
                                + " action='DISPATCH_POLICY_CHANGED' AND aggregate_id=?",
                        String.class,
                        COMPANY_A);
        byte[] envelope =
                Base64.getDecoder().decode(ViJson.tree(summary).path("reasonEnvelope").asText());
        String aad = "D02:POLICY:" + COMPANY_A + ":VERSION:1:ADMIN:" + administratorId(rootAdmin);
        assertEquals(reason, boundaryCrypto.unenvelope(envelope, aad));
        assertThrows(
                RuntimeException.class, () -> boundaryCrypto.unenvelope(envelope, aad + ":OTHER"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='DISPATCH_POLICY_CHANGED'"
                                + " AND aggregate_id=?",
                        COMPANY_A));
        assertFalse(policy.toString().contains(reason));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_outbox WHERE payload LIKE ?", "%" + reason + "%"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_idempotency WHERE result_json LIKE ?",
                        "%" + reason + "%"));
        Map<String, Object> stale = dispatchPolicyBody(0);
        requireStatus(call("PUT", dispatchPolicyPath(COMPANY_A), stale, rootAdmin), 409);
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE"
                                + " action='DISPATCH_POLICY_CHANGED_REJECTED' AND actor_user_id=?",
                        administratorId(rootAdmin)));
    }

    @Test
    void policyOutboxFailureRollsBackConfigurationAndAuditBeforeRetry() {
        failOutbox("DISPATCH_POLICY_CHANGED");
        try {
            requireStatus(
                    call("PUT", dispatchPolicyPath(COMPANY_A), dispatchPolicyBody(0), rootAdmin),
                    500);
            assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_policy"));
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                    + " action='DISPATCH_POLICY_CHANGED'"));
        } finally {
            reset(outbox);
        }
        assertEquals(1, configureDispatch().path("version").asLong());
    }

    @Test
    void explicitLicenseMatrixDoesNotTreatC1AsUniversal() {
        JsonNode configured = configureDispatch();
        Map<String, Object> rules = dispatchPolicyBody(configured.path("version").asLong());
        rules.put(
                "licenseCompatibility",
                List.of(Map.of("vehicleTypeCode", "C04_CAR", "licenseClasses", List.of("C2"))));
        data(call("PUT", dispatchPolicyPath(COMPANY_A), rules, rootAdmin));
        readyDriver();
        PaidOrder order = paidOrder();
        assertEquals(0, candidates(order).path("total").asInt());
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        rootAdmin),
                409);
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void inaccurateLocationAndOutsideExpandedRadiusCannotBeManuallyBypassed() {
        Map<String, Object> policy = dispatchPolicyBody(0);
        policy.put("radiusMeter", 1);
        policy.put("expandedRadiusMeter", 1);
        data(call("PUT", dispatchPolicyPath(COMPANY_A), policy, rootAdmin));
        listening(true);
        Map<String, Object> location = locationBody(now());
        location.put("lng", 113.123456);
        location.put("accuracyMeter", 200);
        data(driverCall("POST", LOCATION, location));
        contact("FOREGROUND", now());
        PaidOrder order = paidOrder();
        assertEquals(0, candidates(order).path("total").asInt());
        controlledNow.set(now().plusMillis(1));
        location = locationBody(now());
        location.put("lat", 22.234579);
        data(driverCall("POST", LOCATION, location));
        // 真实直线约1.33米，显示四舍五入为1米仍不应穿过1米业务半径。
        assertEquals(0, candidates(order).path("total").asInt());
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        rootAdmin),
                409);
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void missingInitialRadiusCandidateExpandsOnceAndDoesNotInviteTheSameDriverAgain() {
        configureDispatch();
        listening(true);
        Map<String, Object> location = locationBody(now());
        location.put("lat", 22.29);
        data(driverCall("POST", LOCATION, location));
        contact("FOREGROUND", now());
        PaidOrder order = paidOrder();
        dispatchNow();
        JsonNode invitation = ownInvite(invite(order));
        assertTrue(invitation.path("distanceMeter").asInt() > 5000);
        assertTrue(invitation.path("distanceMeter").asInt() <= 8000);
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT radius_stage FROM vi_dispatch_batch WHERE id=?",
                        Integer.class,
                        invitation.path("batchId").asLong()));
        data(respond(invitation, "REJECT"));
        dispatchNow();
        dispatchNow();
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=? AND driver_id=?",
                        order.id(),
                        driver.driverId()));
    }

    @Test
    void noCandidatesTerminateWithoutAnInvitationStormOrInfiniteRescans() {
        configureDispatch();
        PaidOrder order = paidOrder();
        for (int i = 0; i < 5; i++) dispatchNow();
        assertEquals("MANUAL_DISPATCH", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_batch WHERE order_id=?", order.id()));
    }

    @Test
    void manualInviteNeedsOrderScopeAndDedicatedCreateAndReadPermissions() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        String read = scoped(SITE_DEPT, DISPATCH_LIST), create = scoped(SITE_DEPT, DISPATCH_CREATE);
        requireStatus(
                call("POST", ADMIN_DISPATCH + "/manual-invites", manualBody(order, driver), read),
                403);
        requireStatus(
                call("POST", ADMIN_DISPATCH + "/manual-invites", manualBody(order, driver), create),
                403);
        String foreign = scoped(COMPANY_B, DISPATCH_LIST, DISPATCH_CREATE);
        requireStatus(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        foreign),
                404);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_invite"));
    }

    @Test
    void manualInvitationRemainsPendingAndDoesNotMoveAutomaticFairnessCursor() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        JsonNode invitation = manualInvite(order, driver);
        assertEquals("MANUAL", invitation.path("source").asText());
        assertEquals("WAIT_RESPONSE", invitation.path("status").asText());
        assertEquals("DISPATCHING", orderField(order, "fulfillment_status"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_cursor"));
    }

    @Test
    void manualVersionsAndForgedInternalFieldsCannotChooseOrOverwriteAnotherDriver() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        for (String field :
                List.of(
                        "serviceCompanyId",
                        "deptId",
                        "params",
                        "visibleDeptIds",
                        "assignmentId",
                        "status")) {
            Map<String, Object> body = manualBody(order, driver);
            body.put(field, "1");
            requireStatus(call("POST", ADMIN_DISPATCH + "/manual-invites", body, rootAdmin), 400);
        }
        for (String field : List.of("expectedOrderVersion", "expectedDriverVersion")) {
            Map<String, Object> body = manualBody(order, driver);
            body.put(field, ((Number) body.get(field)).longValue() + 1);
            requireError(
                    call("POST", ADMIN_DISPATCH + "/manual-invites", body, rootAdmin),
                    409,
                    "VI_VERSION_CONFLICT");
        }
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_invite"));
    }

    @Test
    void manualSameKeyReplaysOneInviteAndDifferentBodyCannotExtendItsDeadline() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        Map<String, Object> body = manualBody(order, driver);
        String uuid = key();
        JsonNode first =
                data(callKey("POST", ADMIN_DISPATCH + "/manual-invites", body, rootAdmin, uuid));
        controlledNow.set(now().plusSeconds(20));
        JsonNode again =
                data(callKey("POST", ADMIN_DISPATCH + "/manual-invites", body, rootAdmin, uuid));
        assertEquals(first.path("inviteId"), again.path("inviteId"));
        assertEquals(first.path("expiresAt"), again.path("expiresAt"));
        body.put("reason", "同键换原因仍为不同意图");
        requireStatus(
                callKey("POST", ADMIN_DISPATCH + "/manual-invites", body, rootAdmin, uuid), 409);
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void preacceptInviteProjectionDoesNotLeakPreciseAddressPhoneCoordinatesOrPrivateReason() {
        PaidOrder order = automaticWaiting();
        JsonNode own = ownInvite(invite(order));
        String response = own.toString();
        for (String secret :
                List.of(
                        "D02隔离接车地址",
                        "112.123456",
                        "22.234567",
                        "phoneCipher",
                        "phoneLookup",
                        "reasonCipher",
                        "locationCipher",
                        "openid")) assertFalse(response.contains(secret), secret);
        assertTrue(own.has("pickupArea"));
        assertEquals("GCJ02_GREAT_CIRCLE", own.path("distanceMode").asText());
        assertTrue(own.path("assignmentId").isNull());
    }

    @Test
    void acceptAtomicallyConvertsExactOccupancyAndExposesOnlyTheRealCurrentAssignment() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        JsonNode accepted = data(respond(invitation, "ACCEPT"));
        long assignment = accepted.path("assignmentId").asLong();
        assertTrue(assignment > 0);
        assertEquals("ACCEPTED", accepted.path("status").asText());
        assertEquals("ACCEPTED", orderField(order, "fulfillment_status"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE id=? AND invite_id=? AND"
                                + " order_id=? AND driver_id=? AND ended_at IS NULL",
                        assignment,
                        invitation.path("inviteId").asLong(),
                        order.id(),
                        driver.driverId()));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_type='ASSIGNMENT' AND holder_id=? AND order_id=?",
                        driver.driverId(),
                        assignment,
                        order.id()));
        assertEquals(Long.toString(assignment), orderField(order, "active_assignment_id"));
        JsonNode current = data(driverCall("GET", CURRENT, null));
        assertEquals(order.no(), current.path("orderNo").asText());
        assertEquals("SUCCESS", paymentField(order, "status"));
        assertEquals(
                2,
                countRows(
                        "SELECT COUNT(*) FROM vi_quota_reservation WHERE order_id=? AND"
                                + " status='CONFIRMED'",
                        order.id()));
    }

    @Test
    void responseUnknownKeysBadUuidWrongCasAndClientSessionCannotGrantAssignment() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        for (String field : List.of("driverId", "assignmentId", "deptId", "params", "accepted")) {
            Map<String, Object> body = responseBody(invitation, "ACCEPT");
            body.put(field, "1");
            requireStatus(respond(invitation, body, key()), 400);
        }
        requireStatus(respond(invitation, responseBody(invitation, "ACCEPT"), null), 400);
        requireStatus(respond(invitation, responseBody(invitation, "ACCEPT"), "bad-key"), 400);
        for (String field : List.of("expectedVersion", "expectedOrderVersion")) {
            Map<String, Object> body = responseBody(invitation, "ACCEPT");
            body.put(field, ((Number) body.get(field)).longValue() + 1);
            requireError(respond(invitation, body, key()), 409, "VI_VERSION_CONFLICT");
        }
        Map<String, Object> wrong = responseBody(invitation, "ACCEPT");
        wrong.put("expectedSessionId", Long.toString(sessionId(driver) + 1));
        requireStatus(respond(invitation, wrong, key()), 409);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void trueTwoActiveDriverDevicesAcceptTheSameInviteAndReceiveOnePermanentAssignment()
            throws Exception {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        Student second = driverSession(driver.accountId());
        JsonNode secondView =
                data(
                        rawTraining(
                                "GET",
                                INVITES + "/" + invitation.path("inviteId").asText(),
                                null,
                                second.token(),
                                key(),
                                "driver"));
        assertNotEquals(invitation.path("sessionId"), secondView.path("sessionId"));
        Map<String, Object> body1 = responseBody(invitation, "ACCEPT"),
                body2 = responseBody(secondView, "ACCEPT");
        List<ResponseEntity<String>> replies =
                concurrent(
                        () -> respond(invitation, body1, key()),
                        () ->
                                rawTraining(
                                        "POST",
                                        INVITES
                                                + "/"
                                                + invitation.path("inviteId").asText()
                                                + "/response",
                                        body2,
                                        second.token(),
                                        key(),
                                        "driver"));
        JsonNode one = data(replies.get(0)), two = data(replies.get(1));
        assertEquals(one.path("assignmentId"), two.path("assignmentId"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE order_id=?", order.id()));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='DISPATCH_INVITE_ACCEPTED'"
                                + " AND aggregate_id=?",
                        invitation.path("inviteId").asLong()));
    }

    @Test
    void acceptedReplayCannotOverwriteANewerAssignmentOrReacquireReleasedResponsibility() {
        PaidOrder first = automaticWaiting();
        JsonNode invitation = ownInvite(invite(first));
        Map<String, Object> original = responseBody(invitation, "ACCEPT");
        JsonNode accepted = data(respond(invitation, original, key()));
        long oldAssignment = accepted.path("assignmentId").asLong();
        // 实际车辆归还履约尚后续：这里明确建立结束消费者事实，只验证旧接受回包绝不再次授予新任务。
        jdbc.update(
                "UPDATE vi_driver_assignment SET ended_at=?,end_reason='RETURNED' WHERE id=?",
                utc(now()),
                oldAssignment);
        jdbc.update(
                "DELETE FROM vi_driver_occupancy WHERE driver_id=? AND holder_type='ASSIGNMENT' AND"
                        + " holder_id=?",
                driver.driverId(),
                oldAssignment);
        jdbc.update("UPDATE vi_order SET active_assignment_id=NULL WHERE id=?", first.id());
        PaidOrder next = paidOrder();
        dispatchNow();
        JsonNode nextInvite = ownInvite(invite(next));
        JsonNode newAssignment = data(respond(nextInvite, "ACCEPT"));
        JsonNode replay = data(respond(invitation, original, key()));
        assertEquals(oldAssignment, replay.path("assignmentId").asLong());
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_type='ASSIGNMENT' AND holder_id=? AND order_id=?",
                        driver.driverId(),
                        newAssignment.path("assignmentId").asLong(),
                        next.id()));
        assertEquals(2, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        requireStatus(respond(invitation, responseBody(invitation, "REJECT"), key()), 409);
    }

    @Test
    void acceptedSameKeyDifferentDecisionConflictsAndRevokedTokenCannotReplay() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        String uuid = key();
        Map<String, Object> original = responseBody(invitation, "ACCEPT");
        JsonNode accepted = data(respond(invitation, original, uuid));
        assertEquals(
                accepted.path("assignmentId"),
                data(respond(invitation, original, uuid)).path("assignmentId"));
        requireStatus(respond(invitation, responseBody(invitation, "REJECT"), uuid), 409);
        long session = sessionId(driver);
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE id=?",
                session);
        requireStatus(respond(invitation, original, uuid), 401);
        assertEquals(1, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void acceptRechecksAdministrativePauseRatherThanTrustingIssuedInvite() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        PaidOrder historical = paidOrder();
        long historicalInvite = SEQ.incrementAndGet();
        // 明确的异常历史消费者事实：原INVITE占用不能遮蔽另一个真实未结束承接。
        jdbc.update(
                "INSERT INTO vi_driver_assignment(order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES(?,?,?,?)",
                historical.id(),
                driver.driverId(),
                historicalInvite,
                utc(now()));
        requireStatus(respond(invitation, "ACCEPT"), 409);
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE invite_id=? AND ended_at"
                                + " IS NULL",
                        historicalInvite));
        assertNull(orderField(order, "active_assignment_id"));
        jdbc.update("DELETE FROM vi_driver_assignment WHERE invite_id=?", historicalInvite);
        data(
                call(
                        "POST",
                        "/api/admin/drivers/" + driver.driverId() + "/disable-new-assignments",
                        Map.of("expectedVersion", profileVersion(driver), "reason", "发邀约后管理员暂停新增"),
                        rootAdmin));
        requireStatus(respond(invitation, "ACCEPT"), 409);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        assertEquals("SUCCESS", paymentField(order, "status"));
    }

    @Test
    void acceptingFromASecondDeviceMayUseTheSameAccountsOtherFullyValidSourceDevice() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = invite(order);
        Student second = driverSession(driver.accountId());
        JsonNode secondView =
                data(
                        rawTraining(
                                "GET",
                                INVITES + "/" + invitation.path("inviteId").asText(),
                                null,
                                second.token(),
                                key(),
                                "driver"));
        JsonNode accepted =
                data(
                        rawTraining(
                                "POST",
                                INVITES + "/" + invitation.path("inviteId").asText() + "/response",
                                responseBody(secondView, "ACCEPT"),
                                second.token(),
                                key(),
                                "driver"));
        assertEquals("ACCEPTED", accepted.path("status").asText());
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE order_id=?", order.id()));
    }

    @Test
    void acceptAndRejectRaceCommitOneDecisionWithoutTwoTasksOrDanglingHolder() throws Exception {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        List<ResponseEntity<String>> responses =
                concurrent(
                        () -> respond(invitation, "ACCEPT"), () -> respond(invitation, "REJECT"));
        assertStatuses(responses, 200, 409);
        String status =
                jdbc.queryForObject(
                        "SELECT status FROM vi_dispatch_invite WHERE id=?",
                        String.class,
                        invitation.path("inviteId").asLong());
        if ("ACCEPTED".equals(status)) {
            assertEquals(
                    1,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_assignment WHERE order_id=?",
                            order.id()));
            assertEquals(
                    1,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                    + " holder_type='ASSIGNMENT'",
                            driver.driverId()));
        } else {
            assertEquals("REJECTED", status);
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_assignment WHERE order_id=?",
                            order.id()));
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                            driver.driverId()));
        }
    }

    @Test
    void rejectionReleasesOnlyItsInviteAndContinuesToAnotherEligibleDriver() {
        configureDispatch();
        readyDriver();
        Student other = additionalDriver(COMPANY_A);
        readyDriver(other);
        PaidOrder order = paidOrder();
        dispatchNow();
        JsonNode invitation = invite(order);
        long first =
                jdbc.queryForObject(
                        "SELECT driver_id FROM vi_dispatch_invite WHERE id=?",
                        Long.class,
                        invitation.path("inviteId").asLong());
        Student saved = driver;
        if (first == other.driverId()) driver = other;
        try {
            data(respond(ownInvite(invitation), "REJECT"));
        } finally {
            driver = saved;
        }
        assertEquals(
                0, countRows("SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?", first));
        dispatchNow();
        assertEquals(
                2,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=? AND"
                                + " status='WAIT_RESPONSE' AND driver_id<>?",
                        order.id(),
                        first));
    }

    @Test
    void exactAbsoluteExpiryRejectsAcceptEvenBeforeTheTimerPersistsExpiredStatus()
            throws Exception {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        controlledNow.set(instant(invitation.path("expiresAt")));
        JsonNode projected = ownInvite(invitation);
        assertEquals("EXPIRED", projected.path("status").asText());
        assertEquals("WAIT_RESPONSE", projected.path("storedStatus").asText());
        requireStatus(respond(invitation, "ACCEPT"), 409);
        concurrent(
                () -> {
                    dispatchNow();
                    return true;
                },
                () -> {
                    requireStatus(respond(invitation, "ACCEPT"), 409);
                    return true;
                });
        assertEquals(
                "EXPIRED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_dispatch_invite WHERE id=?",
                        String.class,
                        invitation.path("inviteId").asLong()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void singleInviteIsCappedByCompanyListeningCutoffAndCannotExtendItByReplay() {
        configureDispatch();
        configure(COMPANY_A, "09:31");
        controlledNow.set(pickupStarts.minusSeconds(1740 + 10));
        readyDriver();
        PaidOrder order = paidOrder();
        dispatchNow();
        JsonNode invitation = invite(order);
        Instant cutoff =
                pickupStarts
                        .atZone(SHANGHAI)
                        .toLocalDate()
                        .atTime(9, 31)
                        .atZone(SHANGHAI)
                        .toInstant();
        assertEquals(cutoff, instant(invitation.path("expiresAt")));
        controlledNow.set(cutoff);
        requireStatus(respond(ownInvite(invitation), "ACCEPT"), 409);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
    }

    @Test
    void batchDeadlineWinsOverLongSingleInviteAndDoesNotWaitForAllCandidates() {
        Map<String, Object> policy = dispatchPolicyBody(0);
        policy.put("inviteResponseSeconds", 600);
        policy.put("overallDeadlineSeconds", 30);
        data(call("PUT", dispatchPolicyPath(COMPANY_A), policy, rootAdmin));
        readyDriver();
        Student spare = additionalDriver(COMPANY_A);
        readyDriver(spare);
        PaidOrder order = paidOrder();
        dispatchNow();
        JsonNode invitation = ownInvite(invite(order));
        assertEquals(now().plusSeconds(30), instant(invitation.path("overallDeadlineAt")));
        assertEquals(invitation.path("overallDeadlineAt"), invitation.path("expiresAt"));
        controlledNow.set(now().plusSeconds(30));
        assertEquals(0, candidates(order).path("total").asInt(), "总截止后其它仍新鲜的候选也不能继续发邀约");
        JsonNode queue =
                page(call("GET", ADMIN_DISPATCH + "/queue?orderNo=" + order.no(), null, rootAdmin))
                        .path("rows")
                        .get(0);
        assertFalse(queue.path("allowedActions").toString().contains("CREATE_MANUAL_INVITE"));
        requireStatus(respond(invitation, "ACCEPT"), 409);
        dispatchNow();
        assertEquals("MANUAL_DISPATCH", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        driver.driverId()));
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
    }

    @Test
    void revokedInviteRequiresDedicatedPermissionAndReleasesOnlyUnacceptedHolder() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = invite(order);
        String read = scoped(SITE_DEPT, DISPATCH_LIST), revoke = scoped(SITE_DEPT, DISPATCH_REVOKE);
        requireStatus(call("POST", revokePath(invitation), revokeBody(invitation), read), 403);
        requireStatus(call("POST", revokePath(invitation), revokeBody(invitation), revoke), 403);
        JsonNode result =
                data(call("POST", revokePath(invitation), revokeBody(invitation), rootAdmin));
        assertEquals("REVOKED", result.path("status").asText());
        assertEquals("MANUAL_DISPATCH", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        driver.driverId()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        requireStatus(respond(ownInvite(invitation), "ACCEPT"), 409);
    }

    @Test
    void acceptedInviteCannotBeRevokedAndResponsibilitySurvivesListeningOff() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        JsonNode accepted = data(respond(invitation, "ACCEPT"));
        listening(false);
        JsonNode latest =
                page(call(
                                "GET",
                                ADMIN_DISPATCH + "/invites?orderNo=" + order.no(),
                                null,
                                rootAdmin))
                        .path("rows")
                        .get(0);
        requireStatus(call("POST", revokePath(latest), revokeBody(latest), rootAdmin), 409);
        assertEquals(order.no(), data(driverCall("GET", CURRENT, null)).path("orderNo").asText());
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND holder_id=?"
                                + " AND holder_type='ASSIGNMENT'",
                        driver.driverId(),
                        accepted.path("assignmentId").asLong()));
        PaidOrder next = paidOrder();
        dispatchNow();
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=? AND driver_id=?",
                        next.id(),
                        driver.driverId()));
        assertEquals("ACCEPTED", orderField(order, "fulfillment_status"));
    }

    @Test
    void expiredOldInviteTimerCannotDeleteAnAssignmentForANewerOrder() {
        PaidOrder first = automaticWaiting();
        JsonNode old = ownInvite(invite(first));
        data(respond(old, "REJECT"));
        PaidOrder next = paidOrder();
        dispatchNow();
        JsonNode accepted = data(respond(ownInvite(invite(next)), "ACCEPT"));
        controlledNow.set(instant(old.path("expiresAt")).plusMillis(1));
        dispatchNow();
        dispatchNow();
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_type='ASSIGNMENT' AND holder_id=? AND order_id=?",
                        driver.driverId(),
                        accepted.path("assignmentId").asLong(),
                        next.id()));
        assertEquals("ACCEPTED", orderField(next, "fulfillment_status"));
    }

    @Test
    void simultaneousOrdersCanOnlyOccupyTheSameDriverOnceAndContinueToAnotherCandidate()
            throws Exception {
        configureDispatch();
        readyDriver();
        Student other = additionalDriver(COMPANY_A);
        readyDriver(other);
        PaidOrder first = paidOrder(), second = paidOrder();
        concurrent(
                () -> {
                    dispatchNow();
                    return true;
                },
                () -> {
                    dispatchNow();
                    return true;
                });
        assertEquals(
                2,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE status='WAIT_RESPONSE'"));
        assertEquals(
                2,
                countRows(
                        "SELECT COUNT(DISTINCT driver_id) FROM vi_dispatch_invite WHERE"
                                + " status='WAIT_RESPONSE'"));
        assertEquals(
                2,
                countRows("SELECT COUNT(*) FROM vi_driver_occupancy WHERE holder_type='INVITE'"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=? AND"
                                + " status='WAIT_RESPONSE'",
                        first.id()));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=? AND"
                                + " status='WAIT_RESPONSE'",
                        second.id()));
    }

    @Test
    void repeatedWorkerDeliveryDoesNotCreateAnotherBatchOrDuplicateInvitation() throws Exception {
        PaidOrder order = automaticWaiting();
        concurrent(
                () -> {
                    for (int i = 0; i < 3; i++) dispatchNow();
                    return true;
                },
                () -> {
                    for (int i = 0; i < 3; i++) dispatchNow();
                    return true;
                });
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_batch WHERE order_id=?", order.id()));
        assertEquals(
                1,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM vi_driver_occupancy"));
    }

    @Test
    void queueHistoryAndCandidatesUseRealOrderDepartmentAndRejectForgedScope() {
        PaidOrder order = automaticWaiting();
        String foreign = scoped(COMPANY_B, DISPATCH_LIST);
        assertEquals(
                0,
                page(call("GET", ADMIN_DISPATCH + "/queue", null, foreign)).path("total").asInt());
        assertEquals(
                0,
                page(call("GET", ADMIN_DISPATCH + "/invites", null, foreign))
                        .path("total")
                        .asInt());
        requireStatus(
                call("GET", ADMIN_DISPATCH + "/candidates?orderNo=" + order.no(), null, foreign),
                404);
        String site = scoped(SITE_DEPT, DISPATCH_LIST);
        assertEquals(
                1,
                page(call("GET", ADMIN_DISPATCH + "/queue?orderNo=" + order.no(), null, site))
                        .path("total")
                        .asInt());
        for (String suffix :
                List.of(
                        "&params.dataScope=1=1",
                        "&visibleDeptIds=" + SITE_DEPT,
                        "&searchDeptIds=" + SITE_DEPT))
            requireStatus(
                    call(
                            "GET",
                            ADMIN_DISPATCH + "/queue?orderNo=" + order.no() + suffix,
                            null,
                            foreign),
                    400);
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(site));
        assertEquals(
                0, page(call("GET", ADMIN_DISPATCH + "/queue", null, site)).path("total").asInt());
    }

    @Test
    void oldAdminAuthVersionAndRevokedMiniStayRejectedWhenRedisEvictionFails() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        String admin = scoped(SITE_DEPT, DISPATCH_LIST, DISPATCH_CREATE);
        doThrow(new IllegalStateException("D02隔离Redis失败")).when(redis).deleteObject(anyString());
        try {
            jdbc.update(
                    "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                    administratorId(admin));
            requireStatus(
                    call(
                            "POST",
                            ADMIN_DISPATCH + "/manual-invites",
                            manualBody(order, driver),
                            admin),
                    401);
            jdbc.update(
                    "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE"
                            + " account_id=?",
                    driver.accountId());
            requireStatus(driverCall("GET", INVITES, null), 401);
            assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_invite"));
        } finally {
            reset(redis);
        }
    }

    @Test
    void invitationOutboxFailureRollsBackInviteOccupancyOrderAndCursor() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        long version = orderVersion(order);
        failOutbox("DISPATCH_INVITE");
        try {
            requireStatus(
                    call(
                            "POST",
                            ADMIN_DISPATCH + "/manual-invites",
                            manualBody(order, driver),
                            rootAdmin),
                    500);
            assertEquals(0, countRows("SELECT COUNT(*) FROM vi_dispatch_invite"));
            assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_occupancy"));
            assertEquals(version, orderVersion(order));
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                    + " action='DISPATCH_INVITE_ISSUED'"));
        } finally {
            reset(outbox);
        }
        assertEquals("WAIT_RESPONSE", manualInvite(order, driver).path("status").asText());
    }

    @Test
    void acceptanceOutboxFailureCannotLeaveTaskPointerOrConvertedOccupancy() {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        long version = orderVersion(order);
        failOutbox("DISPATCH_INVITE_ACCEPTED");
        try {
            requireStatus(respond(invitation, "ACCEPT"), 500);
            assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
            assertNull(orderField(order, "active_assignment_id"));
            assertEquals(version, orderVersion(order));
            assertEquals(
                    1,
                    countRows(
                            "SELECT COUNT(*) FROM vi_driver_occupancy WHERE holder_type='INVITE'"
                                    + " AND holder_id=?",
                            invitation.path("inviteId").asLong()));
            assertEquals(
                    "WAIT_RESPONSE",
                    jdbc.queryForObject(
                            "SELECT status FROM vi_dispatch_invite WHERE id=?",
                            String.class,
                            invitation.path("inviteId").asLong()));
            assertEquals(
                    0,
                    countRows(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                    + " action='DISPATCH_INVITE_ACCEPTED'"));
        } finally {
            reset(outbox);
        }
        assertEquals("ACCEPTED", data(respond(invitation, "ACCEPT")).path("status").asText());
    }

    @Test
    void cancellationApplicationPausesNewInvitationsWithoutPretendingItIsFormalCancellation() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        JsonNode claim = applyAftersale(order, "CANCEL_REFUND", 0);
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", order.id()));
        assertEquals("APPLIED", orderField(order, "cancel_status"));
        assertNotEquals("CANCELLED", orderField(order, "fulfillment_status"));
        assertEquals(
                "PENDING_REVIEW",
                jdbc.queryForObject(
                        "SELECT case_status FROM vi_aftersale_case WHERE id=?",
                        String.class,
                        claim.path("caseId").asLong()));
        assertEquals("SUCCESS", paymentField(order, "status"));
    }

    @Test
    void cancellationDuringInvitePreventsAcceptAndApprovedCancellationClosesOnlyOldInvite()
            throws Exception {
        PaidOrder order = automaticWaiting();
        JsonNode invitation = ownInvite(invite(order));
        JsonNode claim = applyAftersale(order, "CANCEL_REFUND", 0);
        concurrent(
                () -> {
                    approveAftersale(order, claim, 0);
                    return true;
                },
                () -> {
                    requireStatus(respond(invitation, "ACCEPT"), 409);
                    return true;
                });
        dispatchNow();
        assertEquals("CANCELLED", orderField(order, "fulfillment_status"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE holder_type='INVITE' AND"
                                + " holder_id=?",
                        invitation.path("inviteId").asLong()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_assignment"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_refund_order r JOIN vi_payment_order p ON"
                                + " p.id=r.payment_id WHERE p.order_id=?",
                        order.id()));
        PaidOrder next = paidOrder();
        dispatchNow();
        JsonNode responsibility = data(respond(ownInvite(invite(next)), "ACCEPT"));
        controlledNow.set(instant(invitation.path("expiresAt")).plusMillis(1));
        dispatchNow();
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_type='ASSIGNMENT' AND holder_id=? AND order_id=?",
                        driver.driverId(),
                        responsibility.path("assignmentId").asLong(),
                        next.id()));
        assertEquals("ACCEPTED", orderField(next, "fulfillment_status"));
    }

    @Test
    void approvedRefundOnlyReservationAllowsServiceWhileUnexplainedBalanceBlocksIt() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        JsonNode claim = applyAftersale(order, "REFUND_ONLY", 1000);
        approveAftersale(order, claim, 1000);
        dispatchNow();
        assertEquals("WAIT_RESPONSE", invite(order).path("status").asText());
        assertEquals("1000", paymentField(order, "refund_reserved_fen"));
        assertEquals("NONE", orderField(order, "cancel_status"));
        data(call("POST", revokePath(invite(order)), revokeBody(invite(order)), rootAdmin));
        PaidOrder other = paidOrder();
        jdbc.update(
                "UPDATE vi_payment_order SET refund_reserved_fen=1 WHERE order_id=?", other.id());
        dispatchNow();
        assertEquals(
                0,
                countRows("SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?", other.id()));
        assertEquals("MANUAL_DISPATCH", orderField(other, "fulfillment_status"));
    }

    @Test
    void allActualHttpResponseShapesConformToMainOpenApiIncludingCurrentAssignment()
            throws Exception {
        Map<String, Object> actual = new LinkedHashMap<>();
        actual.put(
                "policy-get", actual(call("GET", dispatchPolicyPath(COMPANY_A), null, rootAdmin)));
        actual.put(
                "policy-put",
                actual(
                        call(
                                "PUT",
                                dispatchPolicyPath(COMPANY_A),
                                dispatchPolicyBody(0),
                                rootAdmin)));
        readyDriver();
        PaidOrder order = paidOrder();
        actual.put(
                "candidates",
                actual(
                        call(
                                "GET",
                                ADMIN_DISPATCH + "/candidates?orderNo=" + order.no(),
                                null,
                                rootAdmin)));
        ResponseEntity<String> first =
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, driver),
                        rootAdmin);
        actual.put("manual", actual(first));
        JsonNode initial = data(first);
        actual.put("list", actual(driverCall("GET", INVITES, null)));
        actual.put(
                "detail",
                actual(driverCall("GET", INVITES + "/" + initial.path("inviteId").asText(), null)));
        actual.put("history", actual(call("GET", ADMIN_DISPATCH + "/invites", null, rootAdmin)));
        actual.put("queue", actual(call("GET", ADMIN_DISPATCH + "/queue", null, rootAdmin)));
        actual.put(
                "revoked",
                actual(call("POST", revokePath(initial), revokeBody(initial), rootAdmin)));
        JsonNode next = ownInvite(manualInvite(order, driver));
        actual.put("accepted", actual(respond(next, "ACCEPT")));
        actual.put("current", actual(driverCall("GET", CURRENT, null)));
        Path input = Files.createTempFile("d02-actual-contract-", ".json");
        try {
            Files.writeString(input, ViJson.write(actual), StandardCharsets.UTF_8);
            Path script =
                    repositoryRoot()
                            .resolve("xinyi-admin/src/test/resources/integration/d02_contract.py");
            Process process =
                    new ProcessBuilder(
                                    System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                    script.toString(),
                                    repositoryRoot().toString(),
                                    input.toString())
                            .redirectErrorStream(true)
                            .start();
            String output =
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "契约校验必须有限时");
            assertEquals(0, process.exitValue(), output);
        } finally {
            Files.deleteIfExists(input);
        }
    }

    private JsonNode actual(ResponseEntity<String> response) {
        requireStatus(response, 200);
        return ViJson.tree(response.getBody());
    }

    private Map<String, Object> globalLeadBody(long version, List<Map<String, String>> items) {
        return Map.of(
                "items", items, "expectedVersion", version, "reason", "TC053真实维护全局提前量，旧预约保持原冻结计划");
    }

    private PaidOrder automaticWaiting() {
        configureDispatch();
        readyDriver();
        PaidOrder order = paidOrder();
        dispatchNow();
        assertEquals("WAIT_RESPONSE", invite(order).path("status").asText());
        return order;
    }

    private Student driverSession(long account) {
        String token = anotherSession(account);
        Student active =
                switchToDriver(
                        new Student(token, account, driver.driverId(), driver.applicationId()));
        extendTestSession(active);
        return active;
    }

    private JsonNode applyAftersale(PaidOrder order, String kind, int amount) {
        return data(
                ownerOrder(
                        order,
                        "POST",
                        "/after-sales",
                        Map.of(
                                "kind",
                                kind,
                                "reason",
                                "D02真实售后申请",
                                "reasonCode",
                                "PLAN_CHANGED",
                                "requestedAmount",
                                amount,
                                "expectedOrderVersion",
                                orderVersion(order),
                                "voucherFileIds",
                                List.of())));
    }

    private JsonNode approveAftersale(PaidOrder order, JsonNode claim, int amount) {
        String path = "/api/admin/after-sales/" + claim.path("caseId").asText();
        JsonNode current = data(call("GET", path, null, rootAdmin));
        return data(
                call(
                        "POST",
                        path + "/review",
                        Map.of(
                                "expectedVersion",
                                current.path("version").asLong(),
                                "observedOrderVersion",
                                orderVersion(order),
                                "decision",
                                "APPROVE",
                                "approvedAmount",
                                amount,
                                "opinion",
                                "D02资金/取消联动核实",
                                "publicOpinion",
                                "已核实本次申请"),
                        rootAdmin));
    }

    private void failOutbox(String prefix) {
        doAnswer(
                        invocation -> {
                            for (Object argument : invocation.getArguments())
                                if (argument instanceof String value && value.startsWith(prefix))
                                    throw new IllegalStateException("D02隔离outbox故障");
                            return invocation.callRealMethod();
                        })
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), anyString(), any());
    }

    private static void assertStatuses(List<ResponseEntity<String>> responses, int... expected) {
        assertEquals(
                Arrays.stream(expected).sorted().boxed().toList(),
                responses.stream()
                        .map(response -> response.getStatusCode().value())
                        .sorted()
                        .toList(),
                responses.toString());
    }

    @SafeVarargs
    private static <T> List<T> concurrent(Callable<T>... actions) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(actions.length);
        CountDownLatch ready = new CountDownLatch(actions.length), go = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> action : actions)
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    assertTrue(go.await(15, TimeUnit.SECONDS));
                                    return action.call();
                                }));
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            go.countDown();
            List<T> result = new ArrayList<>();
            for (Future<T> future : futures) result.add(future.get(40, TimeUnit.SECONDS));
            return result;
        } finally {
            go.countDown();
            executor.shutdownNow();
        }
    }

    private static Path repositoryRoot() {
        Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (!Files.exists(root.resolve("docs/vehicle/V6.4"))) root = root.getParent();
        return root;
    }
}
