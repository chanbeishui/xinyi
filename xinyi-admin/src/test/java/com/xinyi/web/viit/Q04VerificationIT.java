package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/** Q04真实HTTP/MySQL与微信SOTER协议回环；人工从真实双人API产生证明，不seed正向PASSED。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=q04-isolated-phone-key",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class Q04VerificationIT extends VerificationHttpFixture {
    static final String LIST = "biz:manualverification:list";
    static final String QUERY = "biz:manualverification:query";
    static final String CREATE = "biz:manualverification:create";
    static final String REVIEW = "biz:manualverification:review";
    @Autowired IntegrationCrypto crypto;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;

    record Subject(Student person, JsonNode profile) {}

    @Test
    void verifiedOwnerCreatesOnlyServerChallengeWithoutExternalIoOrQualificationProof() {
        Subject subject = subject();
        JsonNode created = createDevice(subject.person());
        assertEquals("CREATED", created.path("status").asText());
        assertEquals("WX_SOTER_DEVICE_AUTH", created.path("launchMode").asText());
        assertTrue(created.path("launchUrl").isNull());
        assertTrue(created.path("challenge").asText().matches("[A-Za-z0-9_-]{43}"));
        assertEquals("验证本机生物特征以继续代驾资格申请", created.path("authContent").asText());
        assertFalse(created.path("resultAvailable").asBoolean());
        assertEquals(0, WECHAT.verifyCalls.get());
        assertEquals(0, WECHAT.tokenCalls.get());
        assertEquals(0, externalProofs(subject.person()));
        assertFalse(
                data(miniCall("GET", "/api/common/session", null, subject.person().token()))
                        .path("availableRoles")
                        .toString()
                        .contains("DRIVER"));
        assertEquals(
                created.path("verificationSessionId").asText(),
                qualification(subject.person()).path("verificationSessionId").asText());
    }

    @Test
    void missingPhoneAdminRealmAndForgedClientCannotCreateOrConfirmDeviceChallenge() {
        String unverified = newMini(false);
        Map<String, Object> body =
                Map.of(
                        "applicationId",
                        "1",
                        "expectedApplicationVersion",
                        0,
                        "expectedCompanyRevision",
                        0,
                        "expectedMaterialRevision",
                        0,
                        "consentVersion",
                        FACE_CONSENT_VERSION,
                        "consentTextHash",
                        sha(FACE_CONSENT_TEXT.getBytes(StandardCharsets.UTF_8)),
                        "acceptedConsent",
                        true);
        requireError(miniCall("POST", FACE, body, unverified), 422, "VI_PHONE_PROOF_REQUIRED");
        requireStatus(rawTraining("POST", FACE, body, rootAdmin, key(), "owner"), 401);
        requireStatus(rawTraining("POST", FACE, body, unverified, key(), "station"), 400);
        requireStatus(rawTraining("POST", FACE, body, null, key(), "owner"), 401);
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void unpublishedOrDisabledWechatConfigCannotVerifyDevice() {
        Student person = student(COMPANY_A, true);
        requireStatus(createDevice(person, deviceBody(person), key()), 422);
        JsonNode profile = configureWechat(COMPANY_A);
        publishWechat(profile, APP_ID, APP_SECRET, false);
        requireStatus(createDevice(person, deviceBody(person), key()), 422);
        assertEquals(0, WECHAT.verifyCalls.get());
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_face_session"));
    }

    @Test
    void unassignedOrDisabledCompanyCannotCreateAnAuthenticationChallenge() {
        configureWechat(COMPANY_A);
        Student person = student(COMPANY_A, true);
        jdbc.update(
                "UPDATE vi_driver_profile SET owner_dept_id=NULL WHERE id=?", person.driverId());
        requireError(createDevice(person, deviceBody(person), key()), 422, "VI_DATA_INVALID");
        jdbc.update(
                "UPDATE vi_driver_profile SET owner_dept_id=? WHERE id=?",
                COMPANY_A,
                person.driverId());
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_A);
        requireStatus(createDevice(person, deviceBody(person), key()), 409);
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void explicitConsentRequiresTrueAndExactCurrentTextHashEvenWhenVersionIsUnchanged() {
        Subject subject = subject();
        for (Object accepted : List.of(false, "true", 1)) {
            Map<String, Object> body = new LinkedHashMap<>(deviceBody(subject.person()));
            body.put("acceptedConsent", accepted);
            requireStatus(createDevice(subject.person(), body, key()), 400);
        }
        Map<String, Object> old = deviceBody(subject.person());
        verificationConsent(FACE_CONSENT_VERSION, FACE_CONSENT_TEXT + "另有明确说明");
        requireError(createDevice(subject.person(), old, key()), 409, "VI_CONFIG_CHANGED");
        assertEquals(0, WECHAT.verifyCalls.get());
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_face_session"));
    }

    @Test
    void clientCannotSupplyOpenidChallengeProviderResultOrAnyAuthorityField() {
        Subject subject = subject();
        for (String field :
                List.of(
                        "openid",
                        "openId",
                        "challenge",
                        "passed",
                        "status",
                        "provider",
                        "ownerDeptId",
                        "sourceSessionId",
                        "params",
                        "resultJSON")) {
            Map<String, Object> bad = new LinkedHashMap<>(deviceBody(subject.person()));
            bad.put(field, "forged");
            requireStatus(createDevice(subject.person(), bad, key()), 400);
        }
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_face_session"));
    }

    @Test
    void staleVersionsNoncanonicalIdsInvalidUuidAndUnexpectedQueryAreRejected() {
        Subject subject = subject();
        for (String field :
                List.of(
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision")) {
            Map<String, Object> bad = new LinkedHashMap<>(deviceBody(subject.person()));
            bad.put(field, 999);
            requireStatus(createDevice(subject.person(), bad, key()), 409);
        }
        Map<String, Object> invalid = new LinkedHashMap<>(deviceBody(subject.person()));
        invalid.put("applicationId", "01");
        requireStatus(createDevice(subject.person(), invalid, key()), 400);
        requireStatus(createDevice(subject.person(), deviceBody(subject.person()), "bad-key"), 400);
        requireStatus(
                miniCallKey(
                        "POST",
                        FACE + "?passed=true",
                        deviceBody(subject.person()),
                        subject.person().token(),
                        key()),
                400);
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_face_session"));
    }

    @Test
    void unknownOrOtherAccountSessionsCannotBeReadOrConfirmed() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Student other = student(COMPANY_A, true);
        requireStatus(getDevice(other, session.path("verificationSessionId").asText()), 404);
        requireStatus(
                confirmDevice(other, session, signedDeviceBody(subject.person(), session), key()),
                404);
        requireStatus(getDevice(subject.person(), "999999999999999"), 404);
        requireStatus(
                miniCall(
                        "GET",
                        FACE_RESULTS
                                + "/"
                                + session.path("verificationSessionId").asText()
                                + "?passed=true",
                        null,
                        subject.person().token()),
                400);
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void sameCreateKeyRecoversSameEncryptedChallengeWithoutDuplicatingCurrentSession() {
        Subject subject = subject();
        String key = key();
        Map<String, Object> body = deviceBody(subject.person());
        JsonNode first = data(createDevice(subject.person(), body, key));
        JsonNode same = data(createDevice(subject.person(), body, key));
        assertEquals(
                first.path("verificationSessionId").asText(),
                same.path("verificationSessionId").asText());
        assertEquals(first.path("challenge").asText(), same.path("challenge").asText());
        Map<String, Object> changed = new LinkedHashMap<>(body);
        changed.put("consentVersion", "different-version");
        requireStatus(createDevice(subject.person(), changed, key), 409);
        requireStatus(createDevice(subject.person(), body, key()), 409);
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_face_session"));
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void concurrentCreateKeysCannotCreateTwoActiveChallengesForOneLoginContext() throws Exception {
        Subject subject = subject();
        Map<String, Object> body = deviceBody(subject.person());
        List<ResponseEntity<String>> responses =
                race(
                        () -> createDevice(subject.person(), body, key()),
                        () -> createDevice(subject.person(), body, key()));
        assertEquals(
                List.of(200, 409),
                responses.stream().map(r -> r.getStatusCode().value()).sorted().toList());
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_face_session"));
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void trustedWechatVerificationPublishesExactlyOneDeviceProofAndNeverRealnameOrLiveness() {
        Subject subject = subject();
        JsonNode result = passDevice(subject.person());
        assertTrue(result.path("challenge").isNull());
        assertTrue(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        assertFalse(checkPassed(qualification(subject.person()), "REALNAME"));
        assertFalse(checkPassed(qualification(subject.person()), "FACE"));
        assertEquals(1, externalProofs(subject.person()));
        assertEquals(
                "DEVICE_AUTH",
                jdbc.queryForObject(
                        "SELECT task_type FROM vi_provider_verification WHERE application_id=?",
                        String.class,
                        subject.person().applicationId()));
        assertEquals(
                miniTokens.resolveAccess(subject.person().token()).sessionId(),
                jdbc.queryForObject(
                        "SELECT source_session_id FROM vi_provider_verification WHERE"
                                + " application_id=?",
                        Long.class,
                        subject.person().applicationId()));
        assertEquals(1, WECHAT.verifyCalls.get());
        assertNull(WECHAT.failure.get());
    }

    @Test
    void falseSignatureVerificationIsTerminalFailureAndNeverAClientSuccess() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> body = new LinkedHashMap<>(signedDeviceBody(subject.person(), session));
        body.put("resultJSONSignature", Base64.getEncoder().encodeToString(new byte[256]));
        JsonNode failed = data(confirmDevice(subject.person(), session, body, key()));
        assertEquals("FAILED", failed.path("status").asText());
        assertFalse(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_provider_verification WHERE status='PASSED' AND"
                                + " application_id=?",
                        subject.person().applicationId()));
        assertEquals(1, WECHAT.verifyCalls.get());
    }

    @Test
    void resultRawMustMatchExactServerNonceBeforeWechatIsCalled() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String wrong = WECHAT.resultJson("not-the-current-challenge");
        Map<String, Object> body =
                Map.of(
                        "expectedVersion",
                        session.path("version").asLong(),
                        "resultJSON",
                        wrong,
                        "resultJSONSignature",
                        WECHAT.sign(openId(subject.person()), wrong));
        requireStatus(confirmDevice(subject.person(), session, body, key()), 422);
        assertEquals(0, WECHAT.verifyCalls.get());
        assertEquals(0, externalProofs(subject.person()));
    }

    @Test
    void duplicateRawKeysMalformedJsonAndNonStringRawCannotHideAnotherChallenge() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String challenge = session.path("challenge").asText();
        for (String raw :
                List.of(
                        "{not-json",
                        "{\"raw\":123}",
                        "{\"raw\":\"wrong\",\"raw\":\"" + challenge + "\"}")) {
            Map<String, Object> body =
                    Map.of(
                            "expectedVersion",
                            session.path("version").asLong(),
                            "resultJSON",
                            raw,
                            "resultJSONSignature",
                            WECHAT.sign(openId(subject.person()), raw));
            requireStatus(confirmDevice(subject.person(), session, body, key()), 422);
        }
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void rawWhitespaceAndUnknownOfficialDeviceFieldsStayInTheExactSignedOriginal() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String raw =
                "  { \"raw\":\""
                        + session.path("challenge").asText()
                        + "\", \"counter\":9, \"future_tee_field\":\"kept\" } \n";
        WECHAT.bindOpenId(APP_ID, openId(subject.person()));
        JsonNode passed =
                data(
                        confirmDevice(
                                subject.person(),
                                session,
                                Map.of(
                                        "expectedVersion",
                                        session.path("version").asLong(),
                                        "resultJSON",
                                        raw,
                                        "resultJSONSignature",
                                        WECHAT.sign(openId(subject.person()), raw)),
                                key()));
        assertEquals("PASSED", passed.path("status").asText());
        SoterSignatureLoopback.Call request =
                WECHAT.calls().stream()
                        .filter(call -> call.path().endsWith("verify_signature"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(raw, request.body().path("json_string").asText());
        assertNull(WECHAT.failure.get());
    }

    @Test
    void signatureFromAnotherOpenidCannotAuthenticateTheCurrentMiniAccount() {
        Subject subject = subject();
        Student other = student(COMPANY_A, true);
        JsonNode session = createDevice(subject.person());
        String raw = WECHAT.resultJson(session.path("challenge").asText());
        WECHAT.bindOpenId(APP_ID, openId(subject.person()));
        WECHAT.bindOpenId(APP_ID, openId(other));
        JsonNode result =
                data(
                        confirmDevice(
                                subject.person(),
                                session,
                                Map.of(
                                        "expectedVersion",
                                        session.path("version").asLong(),
                                        "resultJSON",
                                        raw,
                                        "resultJSONSignature",
                                        WECHAT.sign(openId(other), raw)),
                                key()));
        assertEquals("FAILED", result.path("status").asText());
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_provider_verification WHERE application_id=? AND"
                                + " status='PASSED'",
                        subject.person().applicationId()));
    }

    @Test
    void confirmCannotAcceptClientPassedExtraFieldsInvalidUuidOrStaleCas() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> valid = signedDeviceBody(subject.person(), session);
        for (String name :
                List.of(
                        "passed",
                        "is_ok",
                        "openid",
                        "sourceSessionId",
                        "applicationId",
                        "params",
                        "recover")) {
            Map<String, Object> body = new LinkedHashMap<>(valid);
            body.put(name, true);
            requireStatus(confirmDevice(subject.person(), session, body, key()), 400);
        }
        Map<String, Object> stale = new LinkedHashMap<>(valid);
        stale.put("expectedVersion", 999);
        requireStatus(confirmDevice(subject.person(), session, stale, key()), 409);
        requireStatus(confirmDevice(subject.person(), session, valid, "not-uuid"), 400);
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void unknownResponsesNeverBecomePassedAndOnlyFrozenOriginalCanBeRecovered() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> original = signedDeviceBody(subject.person(), session);
        WECHAT.mode = SoterSignatureLoopback.Mode.DROP_RESPONSE;
        JsonNode unknown = data(confirmDevice(subject.person(), session, original, key()));
        assertEquals("UNKNOWN", unknown.path("status").asText());
        assertEquals(1, WECHAT.verifyCalls.get());
        assertEquals(0, externalProofs(subject.person()));
        Map<String, Object> changed = new LinkedHashMap<>(original);
        changed.put("expectedVersion", unknown.path("version").asLong());
        changed.put("resultJSONSignature", Base64.getEncoder().encodeToString(new byte[256]));
        requireStatus(confirmDevice(subject.person(), unknown, changed, key()), 409);
        WECHAT.mode = SoterSignatureLoopback.Mode.VERIFY_SIGNATURE;
        JsonNode recovered =
                data(
                        confirmDevice(
                                subject.person(),
                                unknown,
                                Map.of(
                                        "expectedVersion",
                                        unknown.path("version").asLong(),
                                        "recover",
                                        true),
                                key()));
        assertEquals("PASSED", recovered.path("status").asText());
        assertEquals(2, WECHAT.verifyCalls.get());
        List<String> signedBodies =
                WECHAT.calls().stream()
                        .filter(call -> call.path().endsWith("verify_signature"))
                        .map(call -> call.body().toString())
                        .toList();
        assertEquals(signedBodies.get(0), signedBodies.get(1));
        assertEquals(1, externalProofs(subject.person()));
    }

    @Test
    void officialErrorAmbiguousTrueMissingBooleanAndMalformedBodiesRemainUnknown() {
        configureWechat(COMPANY_A);
        for (SoterSignatureLoopback.Mode mode :
                List.of(
                        SoterSignatureLoopback.Mode.ERROR,
                        SoterSignatureLoopback.Mode.ERROR_WITH_TRUE,
                        SoterSignatureLoopback.Mode.MISSING_RESULT,
                        SoterSignatureLoopback.Mode.STRING_TRUE,
                        SoterSignatureLoopback.Mode.MALFORMED_JSON,
                        SoterSignatureLoopback.Mode.OVERSIZED_RESPONSE,
                        SoterSignatureLoopback.Mode.HTTP_503)) {
            Student person = student(COMPANY_A, true);
            JsonNode session = createDevice(person);
            WECHAT.mode = mode;
            int before = WECHAT.verifyCalls.get();
            JsonNode result =
                    data(confirmDevice(person, session, signedDeviceBody(person, session), key()));
            assertEquals("UNKNOWN", result.path("status").asText(), mode.name());
            assertEquals(before + 1, WECHAT.verifyCalls.get(), mode.name());
            assertEquals(0, externalProofs(person));
        }
    }

    @Test
    void getSessionOnlyReadsMetadataAndNeverRetriesAWechatVerification() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        WECHAT.mode = SoterSignatureLoopback.Mode.HTTP_503;
        JsonNode unknown =
                data(
                        confirmDevice(
                                subject.person(),
                                session,
                                signedDeviceBody(subject.person(), session),
                                key()));
        int calls = WECHAT.verifyCalls.get();
        for (int i = 0; i < 3; i++) {
            JsonNode read =
                    data(
                            getDevice(
                                    subject.person(),
                                    unknown.path("verificationSessionId").asText()));
            assertEquals("UNKNOWN", read.path("status").asText());
        }
        assertEquals(calls, WECHAT.verifyCalls.get());
    }

    @Test
    void expiredUnsubmittedChallengeRejectsNewResultButUnknownOriginalRecoversAfterExpiry() {
        Subject subject = subject();
        JsonNode first = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), first);
        jdbc.update(
                "UPDATE vi_face_session SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND)"
                        + " WHERE id=?",
                first.path("verificationSessionId").asLong());
        requireStatus(confirmDevice(subject.person(), first, signed, key()), 409);
        assertEquals(0, WECHAT.verifyCalls.get());
        Student other = student(COMPANY_A, true);
        JsonNode second = createDevice(other);
        WECHAT.mode = SoterSignatureLoopback.Mode.HTTP_503;
        JsonNode unknown =
                data(confirmDevice(other, second, signedDeviceBody(other, second), key()));
        jdbc.update(
                "UPDATE vi_face_session SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND)"
                        + " WHERE id=?",
                second.path("verificationSessionId").asLong());
        WECHAT.mode = SoterSignatureLoopback.Mode.VERIFY_SIGNATURE;
        JsonNode recovered =
                data(
                        confirmDevice(
                                other,
                                unknown,
                                Map.of(
                                        "expectedVersion",
                                        unknown.path("version").asLong(),
                                        "recover",
                                        true),
                                key()));
        assertEquals("PASSED", recovered.path("status").asText());
    }

    @Test
    void concurrentConfirmIsSingleFlightAndReplaysCannotCreateAnotherCanonicalFact()
            throws Exception {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        List<ResponseEntity<String>> results =
                race(
                        () -> confirmDevice(subject.person(), session, signed, key()),
                        () -> confirmDevice(subject.person(), session, signed, key()));
        assertTrue(results.stream().anyMatch(r -> r.getStatusCode().value() == 200));
        assertTrue(
                results.stream()
                        .allMatch(r -> Set.of(200, 409).contains(r.getStatusCode().value())));
        assertEquals(1, WECHAT.verifyCalls.get());
        assertEquals(1, externalProofs(subject.person()));
        JsonNode done =
                data(getDevice(subject.person(), session.path("verificationSessionId").asText()));
        requireStatus(
                confirmDevice(
                        subject.person(),
                        done,
                        Map.of("expectedVersion", done.path("version").asLong(), "recover", true),
                        key()),
                409);
        assertEquals(1, WECHAT.verifyCalls.get());
    }

    @Test
    void materialChangesInvalidateSessionProofAndOldSignedPayloadCannotDriftToNewMaterials() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> original = signedDeviceBody(subject.person(), session);
        saveMaterials(subject.person(), Map.of("realName", "当前新材料"));
        requireStatus(confirmDevice(subject.person(), session, original, key()), 409);
        requireStatus(
                getDevice(subject.person(), session.path("verificationSessionId").asText()), 409);
        assertFalse(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void companyRoundTripNeverResurrectsDeviceProofButOrdinaryRemarkDoesNotInvalidateIt() {
        Subject subject = subject();
        JsonNode session = passDevice(subject.person());
        JsonNode profile =
                data(
                        call(
                                "GET",
                                "/api/admin/drivers/" + subject.person().driverId(),
                                null,
                                rootAdmin));
        data(
                call(
                        "PATCH",
                        "/api/admin/drivers/" + subject.person().driverId(),
                        Map.of(
                                "expectedVersion",
                                profile.path("version").asLong(),
                                "reason",
                                "普通备注维护",
                                "internalRemark",
                                "不影响公司上下文"),
                        rootAdmin));
        assertTrue(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        assignCompany(subject.person(), COMPANY_B);
        assignCompany(subject.person(), COMPANY_A);
        assertFalse(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        requireStatus(
                getDevice(subject.person(), session.path("verificationSessionId").asText()), 409);
    }

    @Test
    void aNewLoginSessionCannotReuseThePreviousSessionsDeviceProofOrSubmission() {
        Subject subject = subject();
        JsonNode session = passDevice(subject.person());
        Student second =
                new Student(
                        anotherSession(subject.person().accountId()),
                        subject.person().accountId(),
                        subject.person().driverId(),
                        subject.person().applicationId());
        assertFalse(checkPassed(qualification(second), "DEVICE_AUTH"));
        assertTrue(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
        requireStatus(getDevice(second, session.path("verificationSessionId").asText()), 409);
        requireStatus(
                confirmDevice(
                        second,
                        session,
                        Map.of(
                                "expectedVersion",
                                session.path("version").asLong(),
                                "recover",
                                true),
                        key()),
                409);
        JsonNode newProof = passDevice(second);
        assertNotEquals(
                session.path("verificationSessionId").asText(),
                newProof.path("verificationSessionId").asText());
        assertTrue(checkPassed(qualification(second), "DEVICE_AUTH"));
        assertTrue(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
    }

    @Test
    void completedDeviceProofSurvivesRealRoleSwitchWithinTheSameLoginSession() {
        Subject subject = subject();
        Student person = subject.person();
        JsonNode session = passDevice(person);
        long originalSession = miniTokens.resolveAccess(person.token()).sessionId();
        long user = staffId(scoped(COMPANY_A, "biz:order:list"));
        long station = station(SITE_DEPT);
        // I06既有员工关联及站点授权作为身份边界；用真实切换接口证明员工身份也不免资格审核。
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,source,status,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " SELECT"
                    + " id,?,UTC_TIMESTAMP(3),app_id,'WECHAT_PHONE_MATCH','ACTIVE',phone_lookup,phone_verified_at,0"
                    + " FROM vi_account WHERE id=?",
                user,
                person.accountId());
        jdbc.update(
                "INSERT INTO vi_station_grant(sys_user_id,station_id,granted_by,reason,enabled)"
                        + " VALUES (?,?,1,'设备认证身份边界',1)",
                user,
                station);
        JsonNode current = data(miniCall("GET", "/api/common/session", null, person.token()));
        JsonNode switched =
                data(
                        miniCall(
                                "POST",
                                "/api/common/session/role",
                                Map.of(
                                        "role",
                                        "STATION",
                                        "expectedContextVersion",
                                        current.path("contextVersion").asLong()),
                                person.token()));
        String token = switched.path("token").asText();
        assertFalse(token.isBlank());
        assertEquals("STATION", switched.path("activeRole").asText());
        assertEquals(originalSession, miniTokens.resolveAccess(token).sessionId());
        JsonNode qualification =
                data(rawTraining("GET", QUALIFICATION, null, token, key(), "station"));
        assertTrue(checkPassed(qualification, "DEVICE_AUTH"));
        assertFalse(qualification.path("canAcceptOrder").asBoolean());
        assertEquals(
                "PASSED",
                data(rawTraining(
                                "GET",
                                FACE_RESULTS + "/" + session.path("verificationSessionId").asText(),
                                null,
                                token,
                                key(),
                                "station"))
                        .path("status")
                        .asText());
    }

    @Test
    void revokedMiniSessionCannotConfirmOrRecoverEvenWhenRedisEvictionFails() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        doThrow(new IllegalStateException("隔离Redis删除失败")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE account_id=?",
                subject.person().accountId());
        requireStatus(confirmDevice(subject.person(), session, signed, key()), 401);
        requireStatus(
                getDevice(subject.person(), session.path("verificationSessionId").asText()), 401);
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void historicalWechatRevisionRemainsTheAuthorityWhenCurrentCredentialsRotateOrDisable() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String originalRevision =
                jdbc.queryForObject(
                        "SELECT config_revision_id FROM vi_face_session WHERE id=?",
                        String.class,
                        session.path("verificationSessionId").asLong());
        publishWechat(subject.profile(), APP_ID, APP_SECRET + "-rotated", true);
        JsonNode current = data(call("GET", integrationPath(subject.profile()), null, rootAdmin));
        publishWechat(current, APP_ID, APP_SECRET + "-disabled", false);
        JsonNode result =
                data(
                        confirmDevice(
                                subject.person(),
                                session,
                                signedDeviceBody(subject.person(), session),
                                key()));
        assertEquals("PASSED", result.path("status").asText());
        assertEquals(
                originalRevision,
                jdbc.queryForObject(
                        "SELECT config_revision_id FROM vi_face_session WHERE id=?",
                        String.class,
                        session.path("verificationSessionId").asLong()));
        assertEquals(1, WECHAT.verifyCalls.get());
        assertNull(WECHAT.failure.get());
    }

    @Test
    void rawResultSignatureChallengeAndOpenidNeverAppearInIdempotencyOutboxOrAudit() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        JsonNode passed = data(confirmDevice(subject.person(), session, signed, key()));
        assertEquals("PASSED", passed.path("status").asText());
        for (String value :
                List.of(
                        session.path("challenge").asText(),
                        signed.get("resultJSON").toString(),
                        signed.get("resultJSONSignature").toString(),
                        openId(subject.person()),
                        APP_SECRET)) {
            assertArtifactsDoNotContain(value);
        }
        assertFalse(passed.toString().contains("cpu_id"));
        assertFalse(passed.toString().contains("json_signature"));
    }

    @Test
    void sameConfirmKeyReplaysOneDecisionAndDifferentSignedBodyCannotOverwriteIt() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> body = signedDeviceBody(subject.person(), session);
        String key = key();
        JsonNode first = data(confirmDevice(subject.person(), session, body, key));
        JsonNode replay = data(confirmDevice(subject.person(), session, body, key));
        assertEquals(
                first.path("verificationSessionId").asText(),
                replay.path("verificationSessionId").asText());
        assertEquals("PASSED", replay.path("status").asText());
        Map<String, Object> different = new LinkedHashMap<>(body);
        different.put("resultJSONSignature", Base64.getEncoder().encodeToString(new byte[256]));
        requireStatus(confirmDevice(subject.person(), session, different, key), 409);
        assertEquals(1, WECHAT.verifyCalls.get());
        assertEquals(1, externalProofs(subject.person()));
    }

    @Test
    void expiredProcessingLeaseRecoversOnlyTheDurablyFrozenSignedSubmission() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        WECHAT.mode = SoterSignatureLoopback.Mode.HTTP_503;
        JsonNode unknown =
                data(
                        confirmDevice(
                                subject.person(),
                                session,
                                signedDeviceBody(subject.person(), session),
                                key()));
        jdbc.update(
                "UPDATE vi_face_session SET"
                    + " status='PROCESSING',lease_token=?,lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 SECOND) WHERE id=?",
                UUID.randomUUID().toString(),
                session.path("verificationSessionId").asLong());
        WECHAT.mode = SoterSignatureLoopback.Mode.VERIFY_SIGNATURE;
        JsonNode passed =
                data(
                        confirmDevice(
                                subject.person(),
                                unknown,
                                Map.of(
                                        "expectedVersion",
                                        unknown.path("version").asLong(),
                                        "recover",
                                        true),
                                key()));
        assertEquals("PASSED", passed.path("status").asText());
        assertEquals(2, WECHAT.verifyCalls.get());
        assertEquals(1, externalProofs(subject.person()));
    }

    @Test
    void materialChangeDuringExternalVerificationCannotPublishCurrentDeviceProof()
            throws Exception {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        WECHAT.requestArrived = new CountDownLatch(1);
        WECHAT.releaseResponse = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ResponseEntity<String>> result =
                executor.submit(() -> confirmDevice(subject.person(), session, signed, key()));
        try {
            assertTrue(WECHAT.requestArrived.await(10, TimeUnit.SECONDS));
            saveMaterials(subject.person(), Map.of("realName", "外部验签期间的新资料"));
            WECHAT.releaseResponse.countDown();
            requireStatus(result.get(20, TimeUnit.SECONDS), 409);
            assertFalse(checkPassed(qualification(subject.person()), "DEVICE_AUTH"));
            assertEquals(0, externalProofs(subject.person()));
        } finally {
            WECHAT.releaseResponse.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void revokedAccountDuringExternalVerificationCannotPublishProofOrReturnSuccess()
            throws Exception {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        WECHAT.requestArrived = new CountDownLatch(1);
        WECHAT.releaseResponse = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ResponseEntity<String>> result =
                executor.submit(() -> confirmDevice(subject.person(), session, signed, key()));
        try {
            assertTrue(WECHAT.requestArrived.await(10, TimeUnit.SECONDS));
            jdbc.update(
                    "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE"
                            + " account_id=?",
                    subject.person().accountId());
            WECHAT.releaseResponse.countDown();
            requireStatus(result.get(20, TimeUnit.SECONDS), 401);
            assertEquals(0, externalProofs(subject.person()));
        } finally {
            WECHAT.releaseResponse.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void canonicalOutboxFailureRollsBackDeviceProofThenOriginalPayloadCanRecover() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        Map<String, Object> signed = signedDeviceBody(subject.person(), session);
        doThrow(new IllegalStateException("隔离设备证明outbox失败"))
                .when(outbox)
                .publish(
                        startsWith("VERIFICATION_PROOF_PUBLISHED:"),
                        eq("VI_PROVIDER_VERIFICATION"),
                        anyLong(),
                        eq("VERIFICATION_PROOF_PUBLISHED"),
                        any());
        requireStatus(confirmDevice(subject.person(), session, signed, key()), 500);
        assertEquals(0, externalProofs(subject.person()));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_face_session WHERE id=? AND status='PASSED'",
                        session.path("verificationSessionId").asLong()));
        org.mockito.Mockito.reset(outbox);
        jdbc.update(
                "UPDATE vi_face_session SET lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE id=?",
                session.path("verificationSessionId").asLong());
        JsonNode current =
                data(getDevice(subject.person(), session.path("verificationSessionId").asText()));
        JsonNode recovered =
                data(
                        confirmDevice(
                                subject.person(),
                                current,
                                Map.of(
                                        "expectedVersion",
                                        current.path("version").asLong(),
                                        "recover",
                                        true),
                                key()));
        assertEquals("PASSED", recovered.path("status").asText());
        assertEquals(1, externalProofs(subject.person()));
    }

    @Test
    void adminRecoveryUsesOriginalCompanyAndMetadataNeverContainsBiometricPayload() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String id = session.path("verificationSessionId").asText();
        String a =
                scoped(
                        COMPANY_A,
                        "biz:verification:list",
                        "biz:verification:query",
                        "biz:verification:close");
        String b =
                scoped(
                        COMPANY_B,
                        "biz:verification:list",
                        "biz:verification:query",
                        "biz:verification:close");
        JsonNode admin = data(call("GET", "/api/admin/verification-sessions/" + id, null, a));
        assertFalse(admin.toString().contains(session.path("challenge").asText()));
        assertFalse(admin.toString().contains(openId(subject.person())));
        requireStatus(call("GET", "/api/admin/verification-sessions/" + id, null, b), 404);
        requireStatus(
                call(
                        "POST",
                        "/api/admin/verification-sessions/" + id + "/close",
                        Map.of(
                                "expectedVersion",
                                session.path("version").asLong(),
                                "reason",
                                "无法继续本机认证"),
                        b),
                404);
        JsonNode otherList =
                ViJson.tree(call("GET", "/api/admin/verification-sessions", null, b).getBody());
        assertEquals(0, otherList.path("rows").size());
        assertEquals(0, otherList.path("total").asInt());
        // 查询按实际展示状态筛选，但不把纯GET变成到期持久化任务。
        jdbc.update(
                "UPDATE vi_face_session SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND)"
                    + " WHERE id=?",
                Long.parseLong(id));
        JsonNode expired =
                ViJson.tree(
                        call("GET", "/api/admin/verification-sessions?status=EXPIRED", null, a)
                                .getBody());
        assertEquals(1, expired.path("rows").size());
        assertEquals(1, expired.path("total").asInt());
        assertEquals(id, expired.path("rows").get(0).path("verificationSessionId").asText());
        assertEquals("EXPIRED", expired.path("rows").get(0).path("status").asText());
        assertEquals(
                "CREATED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_face_session WHERE id=?",
                        String.class,
                        Long.parseLong(id)));
        assertEquals(
                0,
                ViJson.tree(
                                call(
                                                "GET",
                                                "/api/admin/verification-sessions?status=CREATED",
                                                null,
                                                a)
                                        .getBody())
                        .path("total")
                        .asInt());
        Student another = student(COMPANY_A, true);
        JsonNode pending = createDevice(another);
        WECHAT.mode = SoterSignatureLoopback.Mode.HTTP_503;
        JsonNode unknown =
                data(confirmDevice(another, pending, signedDeviceBody(another, pending), key()));
        assertEquals("UNKNOWN", unknown.path("status").asText());
        // 真实原文已被冻结，仅模拟执行者消失后的过期租约，不seed通过结论。
        jdbc.update(
                "UPDATE vi_face_session SET"
                    + " status='PROCESSING',lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " SECOND) WHERE id=?",
                pending.path("verificationSessionId").asLong());
        int beforeRead = WECHAT.verifyCalls.get();
        JsonNode unresolved =
                ViJson.tree(
                        call("GET", "/api/admin/verification-sessions?status=UNKNOWN", null, a)
                                .getBody());
        assertEquals(1, unresolved.path("rows").size());
        assertEquals(1, unresolved.path("total").asInt());
        assertEquals(
                pending.path("verificationSessionId").asText(),
                unresolved.path("rows").get(0).path("verificationSessionId").asText());
        assertEquals("UNKNOWN", unresolved.path("rows").get(0).path("status").asText());
        assertEquals(
                "PROCESSING",
                jdbc.queryForObject(
                        "SELECT status FROM vi_face_session WHERE id=?",
                        String.class,
                        pending.path("verificationSessionId").asLong()));
        assertEquals(
                0,
                ViJson.tree(
                                call(
                                                "GET",
                                                "/api/admin/verification-sessions?status=PROCESSING",
                                                null,
                                                a)
                                        .getBody())
                        .path("total")
                        .asInt());
        assertEquals(beforeRead, WECHAT.verifyCalls.get());
        assignCompany(subject.person(), COMPANY_B);
        assertEquals(
                id,
                data(call("GET", "/api/admin/verification-sessions/" + id, null, a))
                        .path("verificationSessionId")
                        .asText());
        assertEquals(beforeRead, WECHAT.verifyCalls.get());
    }

    @Test
    void adminCloseNeedsFunctionCasUuidAndPersistsOnlyLocalClosureWithoutAnyProof() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        String path =
                "/api/admin/verification-sessions/"
                        + session.path("verificationSessionId").asText()
                        + "/close";
        String readOnly = scoped(COMPANY_A, "biz:verification:query");
        Map<String, Object> body =
                Map.of("expectedVersion", session.path("version").asLong(), "reason", "用户无法完成本机确认");
        requireStatus(call("POST", path, body, readOnly), 403);
        requireStatus(
                call("POST", path, Map.of("expectedVersion", 999, "reason", "陈旧版本"), rootAdmin),
                409);
        requireStatus(callKey("POST", path, body, rootAdmin, "bad-key"), 400);
        String key = key();
        JsonNode closed = data(callKey("POST", path, body, rootAdmin, key));
        assertEquals("CLOSED", closed.path("status").asText());
        assertEquals(
                "CLOSED",
                data(callKey("POST", path, body, rootAdmin, key)).path("status").asText());
        requireStatus(
                confirmDevice(
                        subject.person(),
                        session,
                        signedDeviceBody(subject.person(), session),
                        key()),
                409);
        assertEquals(0, externalProofs(subject.person()));
        assertEquals(0, WECHAT.verifyCalls.get());
        assertNotEquals(
                session.path("verificationSessionId").asText(),
                createDevice(subject.person()).path("verificationSessionId").asText());
    }

    @Test
    void miniAndMissingCredentialsCannotEnterTheAdminVerificationOrManualRealm() {
        Subject subject = subject();
        JsonNode session = createDevice(subject.person());
        for (String path :
                List.of(
                        MANUAL,
                        MANUAL + "/applications",
                        "/api/admin/verification-sessions",
                        "/api/admin/verification-sessions/"
                                + session.path("verificationSessionId").asText())) {
            requireStatus(
                    rawTraining("GET", path, null, subject.person().token(), key(), "admin"), 401);
            requireStatus(rawTraining("GET", path, null, null, key(), "admin"), 401);
        }
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void manualConfirmationNeedsTwoAuthorizedPeopleAndNoVoucherOrPolicyPublication() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, LIST, QUERY, CREATE);
        String reviewer = scoped(COMPANY_A, LIST, QUERY, REVIEW);
        JsonNode submitted = submitManual(person, "BACKGROUND", "PASSED", "", operator);
        assertEquals(0, externalProofs(person));
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + submitted.path("recordId").asText() + "/review",
                        manualReviewBody(submitted, "CONFIRM", ""),
                        operator),
                403);
        JsonNode confirmed = reviewManual(submitted, "CONFIRM", "", reviewer);
        assertEquals("CONFIRMED", confirmed.path("status").asText());
        assertEquals(1, externalProofs(person));
        assertTrue(checkPassed(qualification(person), "BACKGROUND"));
        assertFalse(checkPassed(qualification(person), "LICENSE"));
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void manualOperatorCannotReviewItsOwnRecordEvenWithEveryFunction() {
        Student person = student(COMPANY_A, true);
        String user = scoped(COMPANY_A, LIST, QUERY, CREATE, REVIEW);
        JsonNode row = submitManual(person, "LICENSE", "PASSED", "", user);
        requireError(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        user),
                403,
                "VI_SELF_REVIEW_DENIED");
        assertEquals(
                "PENDING_REVIEW",
                data(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, user))
                        .path("status")
                        .asText());
        assertEquals(0, externalProofs(person));
    }

    @Test
    void manualFunctionPermissionsCannotBeReplacedByCompanyMembershipOrVisibleLists() {
        Student person = student(COMPANY_A, true);
        String read = scoped(COMPANY_A, LIST, QUERY),
                writer = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, REVIEW);
        requireStatus(
                call("POST", MANUAL, manualBody(person, "BACKGROUND", "PASSED", ""), read), 403);
        requireStatus(
                call(
                        "POST",
                        MANUAL,
                        manualBody(person, "BACKGROUND", "PASSED", ""),
                        scoped(COMPANY_A, CREATE)),
                403);
        JsonNode row = submitManual(person, "BACKGROUND", "PASSED", "", writer);
        requireStatus(call("GET", MANUAL, null, writer), 403);
        requireStatus(
                call("GET", MANUAL + "/" + row.path("recordId").asText(), null, reviewer), 403);
        assertEquals(0, externalProofs(person));
    }

    @Test
    void manualReadsAndWritesUseRealOriginalCompanyAndEmptyScopesFailClosed() {
        Student a = student(COMPANY_A, true), b = student(COMPANY_B, true);
        String all = rootAdmin,
                aStaff = scoped(COMPANY_A, LIST, QUERY, CREATE, REVIEW),
                bStaff = scoped(COMPANY_B, LIST, QUERY, CREATE, REVIEW);
        JsonNode row = submitManual(a, "LICENSE", "PASSED", "", all);
        requireStatus(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, bStaff), 404);
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        bStaff),
                404);
        requireStatus(call("POST", MANUAL, manualBody(b, "BACKGROUND", "PASSED", ""), aStaff), 404);
        JsonNode visible = ViJson.tree(call("GET", MANUAL, null, aStaff).getBody());
        assertEquals(1, visible.path("rows").size());
        assertEquals(1, visible.path("total").asInt());
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", staffId(aStaff));
        JsonNode empty = ViJson.tree(call("GET", MANUAL, null, aStaff).getBody());
        assertEquals(0, empty.path("rows").size());
        assertEquals(0, empty.path("total").asInt());
        requireStatus(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, aStaff), 404);
        assertEquals(0, externalProofs(a));
    }

    @Test
    void manualCandidatesComeFromCurrentVisibleApplicationsAndNeverPretendNicknameIsRealname() {
        Student a = student(COMPANY_A, true);
        student(COMPANY_B, true);
        String user = scoped(COMPANY_A, QUERY);
        ResponseEntity<String> response = call("GET", MANUAL + "/applications", null, user);
        requireStatus(response, 200);
        JsonNode body = ViJson.tree(response.getBody());
        assertEquals(1, body.path("rows").size());
        assertEquals(1, body.path("total").asInt());
        JsonNode row = body.path("rows").get(0);
        assertEquals(Long.toString(a.applicationId()), row.path("applicationId").asText());
        assertEquals("Q03本人", row.path("applicantName").asText());
        assertFalse(row.toString().contains("110105199001010010"));
    }

    @Test
    void manualRequestCannotInjectEvidenceStandardsReviewersOrCanonicalProofFields() {
        Student person = student(COMPANY_A, true);
        for (String field :
                List.of(
                        "evidenceIds",
                        "policyRevisionId",
                        "source",
                        "proofId",
                        "provider",
                        "reviewedByUserId",
                        "status",
                        "ownerDeptId",
                        "params")) {
            Map<String, Object> bad =
                    new LinkedHashMap<>(manualBody(person, "LICENSE", "PASSED", ""));
            bad.put(field, "1");
            requireStatus(call("POST", MANUAL, bad, rootAdmin), 400);
        }
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_manual_verification"));
        assertEquals(0, externalProofs(person));
    }

    @Test
    void manualStaleApplicationMaterialAndCompanyVersionsNeverCreatePartialRecords() {
        Student person = student(COMPANY_A, true);
        for (String field :
                List.of(
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision")) {
            Map<String, Object> bad =
                    new LinkedHashMap<>(manualBody(person, "LICENSE", "PASSED", ""));
            bad.put(field, 999);
            requireStatus(call("POST", MANUAL, bad, rootAdmin), 409);
        }
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_manual_verification"));
    }

    @Test
    void pendingManualRecordCannotBeBypassedByNewUuidOrRereviewedAfterConfirmation() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        Map<String, Object> body = manualBody(person, "LICENSE", "PASSED", "");
        String key = key();
        JsonNode first = data(callKey("POST", MANUAL, body, operator, key));
        JsonNode same = data(callKey("POST", MANUAL, body, operator, key));
        assertEquals(first.path("recordId").asText(), same.path("recordId").asText());
        requireStatus(call("POST", MANUAL, body, operator), 409);
        JsonNode done = reviewManual(first, "CONFIRM", "", reviewer);
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + first.path("recordId").asText() + "/review",
                        manualReviewBody(done, "RETURN", ""),
                        reviewer),
                409);
        assertEquals(1, externalProofs(person));
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_manual_verification"));
    }

    @Test
    void returnedManualRecordKeepsHistoryAndNewSubmissionNeedsAnotherConfirmation() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode original = submitManual(person, "BACKGROUND", "PASSED", "经办记录", operator);
        JsonNode returned = reviewManual(original, "RETURN", "退回核对", reviewer);
        assertEquals("RETURNED", returned.path("status").asText());
        assertEquals(0, externalProofs(person));
        JsonNode again = submitManual(person, "BACKGROUND", "PASSED", "重新确认", operator);
        assertNotEquals(original.path("recordId").asText(), again.path("recordId").asText());
        reviewManual(again, "CONFIRM", "", reviewer);
        assertEquals(1, externalProofs(person));
        assertEquals(2, rows("SELECT COUNT(*) FROM vi_manual_verification"));
    }

    @Test
    void confirmedManualFailureIsNotConvertedIntoQualificationSuccess() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode row = submitManual(person, "BACKGROUND", "FAILED", "人工确认不通过", operator);
        assertEquals(
                "CONFIRMED", reviewManual(row, "CONFIRM", "", reviewer).path("status").asText());
        assertFalse(checkPassed(qualification(person), "BACKGROUND"));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_provider_verification WHERE application_id=? AND"
                                + " task_type='BACKGROUND' AND status='PASSED'",
                        person.applicationId()));
    }

    @Test
    void concurrentManualReviewCommitsExactlyOneDecisionAndOneCanonicalProof() throws Exception {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                one = scoped(COMPANY_A, QUERY, REVIEW),
                two = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode row = submitManual(person, "LICENSE", "PASSED", "", operator);
        List<ResponseEntity<String>> results =
                race(
                        () ->
                                call(
                                        "POST",
                                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                                        manualReviewBody(row, "CONFIRM", "甲"),
                                        one),
                        () ->
                                call(
                                        "POST",
                                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                                        manualReviewBody(row, "CONFIRM", "乙"),
                                        two));
        assertEquals(
                List.of(200, 409),
                results.stream().map(r -> r.getStatusCode().value()).sorted().toList());
        assertEquals(1, externalProofs(person));
        assertEquals(
                1, rows("SELECT COUNT(*) FROM vi_manual_verification WHERE status='CONFIRMED'"));
    }

    @Test
    void materialOrCompanyEpochChangeBeforeManualReviewRejectsOldFactAndKeepsHistory() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode row = submitManual(person, "BACKGROUND", "PASSED", "", operator);
        saveMaterials(person, Map.of("realName", "新材料"));
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        reviewer),
                409);
        assignCompany(person, COMPANY_B);
        assignCompany(person, COMPANY_A);
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        reviewer),
                409);
        assertEquals(
                "PENDING_REVIEW",
                data(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, reviewer))
                        .path("status")
                        .asText());
        assertEquals(0, externalProofs(person));
    }

    @Test
    void oldAdminTokenIsRejectedForManualReviewEvenWhenRedisEvictionFails() {
        Student person = student(COMPANY_A, true);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode row = submitManual(person, "LICENSE", "PASSED", "", operator);
        doThrow(new IllegalStateException("隔离Redis删除故障")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                staffId(reviewer));
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        reviewer),
                401);
        assertEquals(0, externalProofs(person));
    }

    @Test
    void applicantHistoricalBindingCannotOperateOrReviewAfterTheBindingIsRevoked() {
        Student person = student(COMPANY_A, true);
        String self = scoped(COMPANY_A, QUERY, CREATE, REVIEW);
        long user = staffId(self);
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,phone_lookup,last_phone_verified_at,employee_auth_version,status,revoked_at)"
                    + " VALUES"
                    + " (?,?,UTC_TIMESTAMP(3),'wx0000000000000000',?,UTC_TIMESTAMP(3),0,'REVOKED',UTC_TIMESTAMP(3))",
                person.accountId(),
                user,
                new byte[32]);
        requireError(
                call("POST", MANUAL, manualBody(person, "BACKGROUND", "PASSED", ""), self),
                403,
                "VI_SELF_REVIEW_DENIED");
        JsonNode row = submitManual(person, "BACKGROUND", "PASSED", "", rootAdmin);
        assertTrue(
                jdbc.queryForObject(
                                "SELECT applicant_user_ids FROM vi_manual_verification WHERE id=?",
                                String.class,
                                row.path("recordId").asLong())
                        .contains(Long.toString(user)));
        // 只移除隔离关联来证明冻结的参与事实仍有效，不允许换号或解绑抹除本人身份。
        jdbc.update("DELETE FROM vi_employee_binding WHERE account_id=?", person.accountId());
        requireError(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        self),
                403,
                "VI_SELF_REVIEW_DENIED");
        assertEquals(0, externalProofs(person));
    }

    @Test
    void verifiedPhoneLookupWithoutBindingCannotEraseApplicantIdentityByChangingPhone() {
        Student person = student(COMPANY_A, true);
        String self = scoped(COMPANY_A, QUERY, CREATE, REVIEW);
        long user = staffId(self);
        String phone = "13800139041";
        jdbc.update("UPDATE sys_user SET phonenumber=? WHERE user_id=?", phone, user);
        jdbc.update(
                "UPDATE vi_account SET phone_lookup=?,phone_verified_at=UTC_TIMESTAMP(3) WHERE"
                        + " id=?",
                phoneCrypto.lookup(phone),
                person.accountId());
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_employee_binding WHERE account_id=?",
                        person.accountId()));
        requireError(
                call("POST", MANUAL, manualBody(person, "LICENSE", "PASSED", ""), self),
                403,
                "VI_SELF_REVIEW_DENIED");
        JsonNode row = submitManual(person, "LICENSE", "PASSED", "", rootAdmin);
        jdbc.update("UPDATE sys_user SET phonenumber='13800139042' WHERE user_id=?", user);
        requireError(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        self),
                403,
                "VI_SELF_REVIEW_DENIED");
        assertEquals(
                "PENDING_REVIEW",
                data(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, rootAdmin))
                        .path("status")
                        .asText());
        assertEquals(0, externalProofs(person));
    }

    @Test
    void manualCreateOutboxFailureRollsBackRecordNotesAndIdempotencyReceipt() {
        Student person = student(COMPANY_A, true);
        doThrow(new IllegalStateException("隔离人工新增outbox故障"))
                .when(outbox)
                .publish(
                        startsWith("MANUAL_VERIFICATION_SUBMITTED:"),
                        eq("VI_MANUAL_VERIFICATION"),
                        anyLong(),
                        eq("MANUAL_VERIFICATION_SUBMITTED"),
                        any());
        requireStatus(
                call(
                        "POST",
                        MANUAL,
                        manualBody(person, "BACKGROUND", "PASSED", "保密经办备注"),
                        rootAdmin),
                500);
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_manual_verification"));
        assertEquals(0, externalProofs(person));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_idempotency WHERE action_key LIKE"
                                + " 'MANUAL_VERIFICATION_CREATE:%'"));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_outbox WHERE"
                                + " event_type='MANUAL_VERIFICATION_SUBMITTED'"));
    }

    @Test
    void canonicalPublishFailureRollsBackManualReviewAndKeepsPendingOperatorFact() {
        Student person = student(COMPANY_A, true);
        String reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        JsonNode row = submitManual(person, "LICENSE", "PASSED", "原经办事实", rootAdmin);
        doThrow(new IllegalStateException("隔离canonical outbox故障"))
                .when(outbox)
                .publish(
                        startsWith("VERIFICATION_PROOF_PUBLISHED:"),
                        eq("VI_PROVIDER_VERIFICATION"),
                        anyLong(),
                        eq("VERIFICATION_PROOF_PUBLISHED"),
                        any());
        requireStatus(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", "复核事实"),
                        reviewer),
                500);
        JsonNode unchanged =
                data(call("GET", MANUAL + "/" + row.path("recordId").asText(), null, rootAdmin));
        assertEquals("PENDING_REVIEW", unchanged.path("status").asText());
        assertEquals("原经办事实", unchanged.path("note").asText());
        assertTrue(unchanged.path("reviewedByUserId").isNull());
        assertEquals(row.path("version").asLong(), unchanged.path("version").asLong());
        assertEquals(0, externalProofs(person));
    }

    @Test
    void independentLegacyRealnameFaceOrUnlinkedPassedNeverSatisfyDeviceAuthentication() {
        Subject subject = subject();
        Student person = subject.person();
        Map<String, Object> material =
                jdbc.queryForMap(
                        "SELECT material_revision,material_hash FROM vi_driver_application WHERE"
                                + " id=?",
                        person.applicationId());
        for (String type : List.of("REALNAME", "FACE", "DEVICE_AUTH")) {
            jdbc.update(
                    "INSERT INTO"
                        + " vi_provider_verification(application_id,material_revision,material_hash,provider,task_type,provider_task_id,status,verified_at)"
                        + " VALUES (?,?,?,'ARBITRARY_NAME',?,?,'PASSED',UTC_TIMESTAMP(3))",
                    person.applicationId(),
                    material.get("material_revision"),
                    material.get("material_hash"),
                    type,
                    "legacy-" + key());
        }
        assertFalse(checkPassed(qualification(person), "DEVICE_AUTH"));
        passDevice(person);
        assertTrue(checkPassed(qualification(person), "DEVICE_AUTH"));
        jdbc.update(
                "UPDATE vi_provider_verification SET canonical_result_cipher=? WHERE"
                        + " application_id=? AND task_type='DEVICE_AUTH' AND"
                        + " source_kind='DEVICE_SESSION'",
                new byte[] {1, 2, 3},
                person.applicationId());
        // 认证密文损坏与根密钥不可用统一失败关闭；不能伪装成可继续提交的正常GET。
        requireError(
                miniCall("GET", QUALIFICATION, null, person.token()), 503, "VI_SECRET_UNAVAILABLE");
    }

    @Test
    void realWechatTwoManualConfirmationsAndActualTrainingEnableOnlyQualificationSubmission()
            throws Exception {
        Subject subject = subject();
        Student person = subject.person();
        passDevice(person);
        String operator = scoped(COMPANY_A, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        confirmManual(person, "BACKGROUND", operator, reviewer);
        confirmManual(person, "LICENSE", operator, reviewer);
        JsonNode course = publishCourse(COMPANY_A, 2, 1, 80);
        publishPolicy(COMPANY_A, List.of(course), 3, 0);
        passCourse(person, course);
        assertTrue(qualification(person).path("submitBlockers").isEmpty());
        JsonNode submitted =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION + "/submit",
                                qualificationSubmitBody(person),
                                person.token()));
        assertEquals("SUBMITTED", submitted.path("status").asText());
        assertFalse(
                data(miniCall("GET", "/api/common/session", null, person.token()))
                        .path("availableRoles")
                        .toString()
                        .contains("DRIVER"));
        JsonNode ids =
                ViJson.tree(
                        jdbc.queryForObject(
                                "SELECT provider_result_ids FROM vi_driver_application WHERE id=?",
                                String.class,
                                person.applicationId()));
        assertEquals(4, ids.size());
        Set<String> types = new HashSet<>();
        for (JsonNode id : ids)
            types.add(
                    jdbc.queryForObject(
                            "SELECT task_type FROM vi_provider_verification WHERE id=?",
                            String.class,
                            id.asLong()));
        assertEquals(Set.of("DEVICE_AUTH", "BACKGROUND", "LICENSE", "TRAINING"), types);
    }

    @Test
    void manualNotesAreEncryptedBoundToTheRecordAndAbsentFromAuditOutboxAndIdempotency() {
        Student person = student(COMPANY_A, true);
        String reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        String operatorNote = "仅授权详情读取的经办隐私内容";
        String reviewNote = "仅授权详情读取的复核隐私内容";
        JsonNode row = submitManual(person, "LICENSE", "PASSED", operatorNote, rootAdmin);
        JsonNode done = reviewManual(row, "CONFIRM", reviewNote, reviewer);
        assertEquals(operatorNote, done.path("note").asText());
        assertEquals(reviewNote, done.path("reviewNote").asText());
        byte[] original =
                jdbc.queryForObject(
                        "SELECT note_cipher FROM vi_manual_verification WHERE id=?",
                        byte[].class,
                        row.path("recordId").asLong());
        String aad =
                "Q04:MANUAL:"
                        + row.path("recordId").asText()
                        + ":"
                        + person.applicationId()
                        + ":"
                        + COMPANY_A
                        + ":"
                        + companyEpoch(person)
                        + ":"
                        + row.path("materialRevision").asLong()
                        + ":LICENSE:"
                        + staffId(rootAdmin)
                        + ":SUBMIT";
        assertEquals(operatorNote, ViJson.read(crypto.unenvelope(original, aad), String.class));
        assertThrows(
                RuntimeException.class, () -> crypto.unenvelope(original, aad + ":OTHER_RECORD"));
        assertArtifactsDoNotContain(operatorNote);
        assertArtifactsDoNotContain(reviewNote);
        JsonNode list = ViJson.tree(call("GET", MANUAL, null, rootAdmin).getBody());
        assertTrue(list.path("rows").get(0).path("note").isNull());
    }

    @Test
    void elevenActualDeviceAndManualResponsesMatchMainAndTaskContractsIncludingPublicNullConsent()
            throws Exception {
        Subject subject = subject();
        Student person = subject.person();
        Map<String, ResponseEntity<String>> responses = new LinkedHashMap<>();
        responses.put("face-create", createDevice(person, deviceBody(person), key()));
        JsonNode session = data(responses.get("face-create"));
        responses.put(
                "face-confirm",
                confirmDevice(person, session, signedDeviceBody(person, session), key()));
        responses.put(
                "face-get", getDevice(person, session.path("verificationSessionId").asText()));
        String operator = scoped(COMPANY_A, LIST, QUERY, CREATE),
                reviewer = scoped(COMPANY_A, QUERY, REVIEW);
        responses.put("manual-candidates", call("GET", MANUAL + "/applications", null, operator));
        responses.put(
                "manual-create",
                call("POST", MANUAL, manualBody(person, "LICENSE", "PASSED", ""), operator));
        JsonNode row = data(responses.get("manual-create"));
        responses.put("manual-list", call("GET", MANUAL, null, operator));
        responses.put(
                "manual-detail",
                call("GET", MANUAL + "/" + row.path("recordId").asText(), null, operator));
        responses.put(
                "manual-review",
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, "CONFIRM", ""),
                        reviewer));
        Student second = student(COMPANY_A, true);
        JsonNode unresolved = createDevice(second);
        String id = unresolved.path("verificationSessionId").asText();
        responses.put(
                "admin-list", call("GET", "/api/admin/verification-sessions", null, rootAdmin));
        responses.put(
                "admin-detail",
                call("GET", "/api/admin/verification-sessions/" + id, null, rootAdmin));
        responses.put(
                "admin-close",
                call(
                        "POST",
                        "/api/admin/verification-sessions/" + id + "/close",
                        Map.of(
                                "expectedVersion",
                                unresolved.path("version").asLong(),
                                "reason",
                                "契约验证本地关闭"),
                        rootAdmin));
        responses.put("public-config", miniCall("GET", "/api/public/service-config", null, null));
        jdbc.update(
                "DELETE FROM sys_config WHERE config_key IN"
                    + " ('vi.biz.verification.consent.version','vi.biz.verification.consent.text')");
        responses.put(
                "public-config-null", miniCall("GET", "/api/public/service-config", null, null));
        JsonNode nullConfig = data(responses.get("public-config-null"));
        for (String field :
                List.of(
                        "verificationConsentVersion",
                        "verificationConsentText",
                        "verificationConsentHash")) {
            assertTrue(nullConfig.has(field));
            assertTrue(nullConfig.path(field).isNull());
        }
        Path directory = Files.createTempDirectory("q04-contract-");
        for (var entry : responses.entrySet()) {
            requireStatus(entry.getValue(), 200);
            if (!entry.getKey().startsWith("public-config"))
                assertTrue(entry.getValue().getHeaders().getCacheControl().contains("no-store"));
            Files.writeString(
                    directory.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        Path repo = repository();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/q04_contract.py")
                                        .toString(),
                                repo.toString(),
                                directory.toString())
                        .redirectErrorStream(true)
                        .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(20, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    private Subject subject() {
        return new Subject(student(COMPANY_A, true), configureWechat(COMPANY_A));
    }

    private int externalProofs(Student person) {
        return rows(
                "SELECT COUNT(*) FROM vi_provider_verification WHERE application_id=? AND"
                        + " source_kind IN ('DEVICE_SESSION','MANUAL_REVIEW')",
                person.applicationId());
    }

    private boolean checkPassed(JsonNode app, String type) {
        for (JsonNode check : app.path("checks"))
            if (type.equals(check.path("type").asText()))
                return "PASSED".equals(check.path("status").asText());
        return false;
    }

    private Set<String> fieldNames(JsonNode object) {
        Set<String> keys = new HashSet<>();
        object.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    private long staffId(String token) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId();
    }

    private Map<String, Object> qualificationSubmitBody(Student person) {
        JsonNode app = qualification(person);
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "expectedVersion",
                app.path("version").asLong(),
                "expectedProfileVersion",
                profileVersion(person),
                "reason",
                "完整真实核验和培训后提交审核");
    }

    private List<ResponseEntity<String>> race(
            Callable<ResponseEntity<String>> first, Callable<ResponseEntity<String>> second)
            throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(3, TimeUnit.SECONDS));
                                return first.call();
                            });
            var b =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(3, TimeUnit.SECONDS));
                                return second.call();
                            });
            start.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private void assertArtifactsDoNotContain(String secret) {
        for (var row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log"))
            for (Object value : row.values())
                assertFalse(Objects.toString(value, "").contains(secret));
        for (String value : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class))
            assertFalse(value.contains(secret));
        for (String value :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) assertFalse(value.contains(secret));
    }

    private Path repository() {
        Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!Files.exists(root.resolve("docs/vehicle"))) root = root.getParent();
        return root;
    }
}
