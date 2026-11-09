package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.service.MiniTokenService;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.AfterAll;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

/**
 * Q01 真实 HTTP、MINI 会话、MyBatis、隔离 MySQL 和私有磁盘证件链。 不 mock 身份、资格或文件服务；后台审批和供应商结果仅能作为明确的历史事实夹具，不能据此声称
 * Q05 或真机核验完成。
 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=q01-isolated-test-phone-key",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class Q01QualificationIT extends PricingHttpFixture {
    static final String BASE = "/api/app/driver/qualification";
    static final String CONSENT_VERSION = "q01-consent-test-v1";
    static final String CONSENT_TEXT = "隔离测试：为审核代驾资格而收集本人证件资料。";
    static final String ID_CARD = "110105199001010010";
    static final Path PRIVATE_ROOT = privateRoot();

    @Autowired MiniTokenService miniTokens;
    @Autowired ViSessionMapper sessions;
    @Autowired PhoneCrypto phones;
    @Autowired IntegrationKeyProperties keys;
    @Autowired IntegrationCrypto crypto;
    @Autowired IdempotencyService idempotency;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoSpyBean OutboxService outbox;

    String owner;
    String root;
    long account;
    byte[] png;

    static Path privateRoot() {
        try {
            return Files.createTempDirectory("xinyi-q01-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void privateFiles(DynamicPropertyRegistry properties) {
        properties.add("vi.files.root", () -> PRIVATE_ROOT.toString());
    }

    @BeforeEach
    void prepareQualification() throws Exception {
        // 父夹具先检查测试库名称与保护标记；本套件不得与其他写同一隔离库的测试并行。
        for (String table :
                List.of(
                        "vi_driver_application_submission",
                        "vi_face_session",
                        "vi_provider_verification",
                        "vi_manual_verification",
                        "vi_training_playback",
                        "vi_training_attempt",
                        "vi_training_progress",
                        "vi_training_run",
                        "vi_training_plan_snapshot",
                        "vi_training_policy_revision",
                        "vi_training_policy",
                        "vi_training_question",
                        "vi_training_course",
                        "vi_driver_occupancy",
                        "vi_driver_assignment",
                        "vi_dispatch_invite",
                        "vi_file",
                        "vi_driver_application",
                        "vi_driver_profile")) {
            jdbc.execute("DELETE FROM " + table);
        }
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        keys.setCurrentVersion("q01-test");
        keys.setVersions(
                new HashMap<>(Map.of("q01-test", Base64.getEncoder().encodeToString(key))));
        configureConsent(CONSENT_VERSION, CONSENT_TEXT);
        root = rootToken();
        owner = mini(true);
        account = miniTokens.resolveAccess(owner).accountId();
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, 0x2255AA);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", bytes));
        png = bytes.toByteArray();
    }

    @AfterAll
    static void removeOwnPrivateBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    @Test
    void verifiedOwnerWithoutADriverProfileCanReadAndStartItsOwnApplication() {
        ResponseEntity<String> empty = requestMini("GET", BASE, null, owner);
        status(empty, 200);
        assertTrue(ViJson.tree(empty.getBody()).path("data").isNull());
        JsonNode row = create(owner);
        assertEquals("DRAFT", row.path("status").asText());
        assertEquals("DRAFT", row.path("effectiveQualificationStatus").asText());
        assertTrue(row.path("ownerDeptId").isNull());
        assertFalse(row.path("canAcceptOrder").asBoolean());
        assertEquals(
                0,
                number(
                        "SELECT new_assignment_enabled FROM vi_driver_profile WHERE account_id=?",
                        account));
        assertEquals(
                1, number("SELECT COUNT(*) FROM vi_driver_profile WHERE account_id=?", account));
        JsonNode session = session(owner);
        assertFalse(session.path("availableRoles").toString().contains("DRIVER"));
        assertTrue(session.path("capabilities").toString().contains("DRIVER_QUALIFICATION_CREATE"));
    }

    @Test
    void unverifiedPhoneCannotReadCreateSaveOrSubmitQualification() {
        String unverified = mini(false);
        for (String method : List.of("GET", "POST", "PUT")) {
            Object body =
                    method.equals("GET")
                            ? null
                            : method.equals("POST")
                                    ? createBody()
                                    : Map.of(
                                            "applicationId",
                                            "1",
                                            "expectedVersion",
                                            0,
                                            "realName",
                                            "测试姓名");
            status(requestMini(method, BASE, body, unverified), 422);
        }
        status(
                requestMini(
                        "POST",
                        BASE + "/submit",
                        Map.of(
                                "applicationId",
                                "1",
                                "expectedVersion",
                                0,
                                "expectedProfileVersion",
                                0,
                                "reason",
                                "未核验"),
                        unverified),
                422);
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application"));
    }

    @Test
    void realStationIdentityUsesTheSameDraftRulesAndIsNotGrantedDriver() {
        String employee = stationIdentity();
        JsonNode row =
                data(
                        requestMini(
                                "POST",
                                BASE,
                                createBody(),
                                employee,
                                UUID.randomUUID().toString(),
                                "station"));
        assertEquals("DRAFT", row.path("status").asText());
        JsonNode current =
                data(requestMini("GET", "/api/common/session", null, employee, null, "station"));
        assertTrue(current.path("availableRoles").toString().contains("STATION"));
        assertFalse(current.path("availableRoles").toString().contains("DRIVER"));
        status(
                requestMini(
                        "POST",
                        "/api/common/session/role",
                        Map.of("role", "DRIVER", "expectedContextVersion", 0),
                        employee,
                        null,
                        "station"),
                403);
    }

    @Test
    void adminTokensAndMissingLoginCannotBecomeTheMiniApplicant() {
        for (String token : List.of("", root)) {
            status(requestMini("GET", BASE, null, token), 401);
            status(requestMini("POST", BASE, createBody(), token), 401);
        }
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application"));
    }

    @Test
    void bodyCannotForgeAccountCompanyQualificationReviewOrVerificationResults() {
        for (String field :
                List.of(
                        "accountId",
                        "driverId",
                        "ownerDeptId",
                        "status",
                        "qualificationStatus",
                        "reviewerUserId",
                        "providerPassed",
                        "providerResultIds",
                        "score",
                        "params")) {
            Map<String, Object> body = new LinkedHashMap<>(createBody());
            body.put(field, "APPROVED");
            status(requestMini("POST", BASE, body, owner), 400);
        }
        JsonNode row = create(owner);
        for (String field :
                List.of(
                        "applicationId",
                        "ownerDeptId",
                        "status",
                        "reviewerUserId",
                        "providerPassed",
                        "providerResultIds")) {
            status(save(row, Map.of(field, "APPROVED"), owner), 400);
        }
        assertEquals("DRAFT", current(owner).path("status").asText());
    }

    @Test
    void consentRequiresExplicitAcceptanceAndTheConfiguredCurrentVersion() {
        status(requestMini("POST", BASE, createBody(), owner, null, "owner"), 400);
        status(requestMini("POST", BASE, createBody(), owner, "invalid-key", "owner"), 400);
        status(requestMini("POST", BASE, Map.of("consentVersion", CONSENT_VERSION), owner), 400);
        status(
                requestMini(
                        "POST",
                        BASE,
                        Map.of(
                                "consentVersion",
                                CONSENT_VERSION,
                                "consentTextHash",
                                consentHash(CONSENT_TEXT),
                                "acceptedConsent",
                                false),
                        owner),
                400);
        status(requestMini("POST", BASE, consentBody("unknown-old", CONSENT_TEXT), owner), 409);
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application"));
        status(
                requestMini(
                        "POST",
                        BASE,
                        Map.of("consentVersion", CONSENT_VERSION, "acceptedConsent", true),
                        owner),
                400);
        Map<String, Object> wrong = createBody();
        wrong.put("consentTextHash", "0".repeat(64));
        configChanged(requestMini("POST", BASE, wrong, owner));
        String changedText = "同版本发布了新正文，原有勾选不能替代重新阅读。";
        configureConsent(CONSENT_VERSION, changedText);
        JsonNode published = data(requestMini("GET", "/api/public/service-config", null, ""));
        assertEquals(consentHash(changedText), published.path("qualificationConsentHash").asText());
        configChanged(requestMini("POST", BASE, createBody(), owner));
        JsonNode accepted =
                data(requestMini("POST", BASE, consentBody(CONSENT_VERSION, changedText), owner));
        assertEquals(
                consentHash(changedText), accepted.path("draft").path("consentTextHash").asText());
    }

    @Test
    void missingConsentConfigurationHasNoSilentDefaultOrEmptyApprovalText() throws Exception {
        jdbc.update(
                "DELETE FROM sys_config WHERE config_key LIKE 'vi.biz.qualification.consent.%'");
        ResponseEntity<String> response = requestMini("POST", BASE, createBody(), owner);
        status(response, 422);
        assertEquals(
                "VI_CAPABILITY_NOT_CONFIGURED",
                ViJson.tree(response.getBody()).path("errorCode").asText());
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application"));
        ResponseEntity<String> published =
                requestMini("GET", "/api/public/service-config", null, "");
        JsonNode config = data(published);
        assertTrue(config.has("qualificationConsentHash"));
        assertTrue(config.get("qualificationConsentHash").isNull(), config.toString());
        Path directory = Files.createTempDirectory("q01-public-missing-contract-");
        Files.writeString(directory.resolve("public-missing.json"), published.getBody());
        validateContractFiles(directory, "public-missing");
    }

    @Test
    void acceptedConsentVersionAndTextAreFrozenInTheApplication() {
        JsonNode row = create(owner);
        long id = id(row);
        assertEquals(
                CONSENT_VERSION,
                jdbc.queryForObject(
                        "SELECT consent_version FROM vi_driver_application WHERE id=?",
                        String.class,
                        id));
        assertEquals(
                CONSENT_TEXT,
                jdbc.queryForObject(
                        "SELECT consent_text FROM vi_driver_application WHERE id=?",
                        String.class,
                        id));
        assertNotNull(
                jdbc.queryForMap(
                                "SELECT consent_accepted_at FROM vi_driver_application WHERE id=?",
                                id)
                        .get("consent_accepted_at"));
        configureConsent("q01-consent-test-v2", "更新后的明确说明");
        assertEquals(
                CONSENT_TEXT,
                jdbc.queryForObject(
                        "SELECT consent_text FROM vi_driver_application WHERE id=?",
                        String.class,
                        id));
        status(
                save(
                        row,
                        Map.of("consentVersion", "q01-consent-test-v2", "acceptedConsent", true),
                        owner),
                400);
        Map<String, Object> stale = consentBody("q01-consent-test-v2", CONSENT_TEXT);
        configChanged(save(row, stale, owner));
        JsonNode renewed = data(save(row, consentBody("q01-consent-test-v2", "更新后的明确说明"), owner));
        assertEquals(
                consentHash("更新后的明确说明"), renewed.path("draft").path("consentTextHash").asText());
        assertEquals(row.path("materialRevision"), renewed.path("materialRevision"));
    }

    @Test
    void createIdempotencyReplaysOneApplicationAndDifferentBodyConflicts() {
        String key = UUID.randomUUID().toString();
        JsonNode first = data(requestMini("POST", BASE, createBody(), owner, key, "owner"));
        JsonNode replay = data(requestMini("POST", BASE, createBody(), owner, key, "owner"));
        assertEquals(first.path("applicationId"), replay.path("applicationId"));
        Map<String, Object> altered = new LinkedHashMap<>(createBody());
        altered.put("consentVersion", "changed-body");
        status(requestMini("POST", BASE, altered, owner, key, "owner"), 409);
        assertEquals(1, number("SELECT COUNT(*) FROM vi_driver_application"));
        status(requestMini("POST", BASE, createBody(), owner), 409);
    }

    @Test
    void concurrentCreatesWithDifferentKeysLeaveOneEditableApplication() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var first =
                    pool.submit(
                            () -> {
                                start.await();
                                return requestMini("POST", BASE, createBody(), owner);
                            });
            var second =
                    pool.submit(
                            () -> {
                                start.await();
                                return requestMini("POST", BASE, createBody(), owner);
                            });
            start.countDown();
            List<Integer> states =
                    new ArrayList<>(
                            List.of(
                                    first.get(10, TimeUnit.SECONDS).getStatusCode().value(),
                                    second.get(10, TimeUnit.SECONDS).getStatusCode().value()));
            states.sort(Integer::compareTo);
            assertEquals(List.of(200, 409), states);
            assertEquals(
                    1,
                    number("SELECT COUNT(*) FROM vi_driver_profile WHERE account_id=?", account));
            assertEquals(1, number("SELECT COUNT(*) FROM vi_driver_application"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void partialDraftSavePreservesUnsentFieldsAndMasksTheIdentityNumber() {
        JsonNode row = create(owner);
        row = data(save(row, Map.of("realName", "测试姓名", "idCardNo", ID_CARD), owner));
        assertEquals("测试姓名", row.path("draft").path("realName").asText());
        assertTrue(row.path("draft").path("idCardNoProvided").asBoolean());
        assertTrue(row.path("draft").path("idCardNoMask").asText().contains("*"));
        assertFalse(row.toString().contains(ID_CARD));
        row = data(save(row, Map.of("licenseClass", "C1"), owner));
        assertEquals("测试姓名", row.path("draft").path("realName").asText());
        assertTrue(row.path("draft").path("idCardNoProvided").asBoolean());
        assertEquals("C1", row.path("draft").path("licenseClass").asText());
        assertFalse(row.toString().contains(ID_CARD));
    }

    @Test
    void maskedOrInvalidIdentityNumbersCannotOverwriteTheStoredSecret() {
        JsonNode row = data(save(create(owner), Map.of("idCardNo", ID_CARD), owner));
        byte[] before = payload(id(row));
        for (String wrong :
                List.of("1101**********0010", "110105199001010011", "110105199013010010")) {
            status(save(row, Map.of("idCardNo", wrong), owner), 422);
            assertArrayEquals(before, payload(id(row)));
        }
    }

    @Test
    void draftDtoRejectsUnknownFieldsBadVersionsAndInvalidScalarTypes() {
        JsonNode row = create(owner);
        Map<String, Object> body =
                Map.of(
                        "applicationId",
                        row.path("applicationId").asText(),
                        "expectedVersion",
                        row.path("version").asLong(),
                        "realName",
                        123);
        status(requestMini("PUT", BASE, body, owner), 400);
        for (Object version : List.of(-1, "0", 9007199254740992L)) {
            status(
                    requestMini(
                            "PUT",
                            BASE,
                            Map.of(
                                    "applicationId",
                                    row.path("applicationId").asText(),
                                    "expectedVersion",
                                    version,
                                    "realName",
                                    "测试姓名"),
                            owner),
                    400);
        }
        status(save(row, Map.of("idCardFrontFileId", "9223372036854775808"), owner), 400);
        status(save(row, Map.of("licenseClass", true), owner), 400);
        assertEquals(row.path("version").asLong(), current(owner).path("version").asLong());
    }

    @Test
    void staleDraftVersionCannotOverwriteTheWinningSavedMaterial() {
        JsonNode original = create(owner);
        data(save(original, Map.of("realName", "先保存的资料"), owner));
        status(save(original, Map.of("realName", "旧版本覆盖"), owner), 409);
        assertEquals("先保存的资料", current(owner).path("draft").path("realName").asText());
    }

    @Test
    void saveReplayDoesNotIncreaseMaterialRevisionAgainAndChangedBodyConflicts() {
        JsonNode row = create(owner);
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = draftBody(row, Map.of("realName", "只保存一次的资料"));
        JsonNode first = data(requestMini("PUT", BASE, body, owner, key, "owner"));
        JsonNode replay = data(requestMini("PUT", BASE, body, owner, key, "owner"));
        assertEquals(first.path("version"), replay.path("version"));
        assertEquals(first.path("materialRevision"), replay.path("materialRevision"));
        body.put("realName", "同键另一个资料");
        status(requestMini("PUT", BASE, body, owner, key, "owner"), 409);
        assertTrue(first.path("materialHash").asText().matches("[0-9a-f]{64}"));
    }

    @Test
    void fourRealReadyFilesCanBeBoundOnlyToTheirCorrectDocumentSlots() throws Exception {
        JsonNode row = create(owner);
        Map<String, Object> files = documents(row, owner);
        row = data(save(row, files, owner));
        for (var entry : files.entrySet()) {
            assertEquals(
                    entry.getValue().toString(), row.path("draft").path(entry.getKey()).asText());
        }
        assertEquals(
                4,
                number(
                        "SELECT COUNT(*) FROM vi_file WHERE application_id=? AND status='READY'",
                        id(row)));
    }

    @Test
    void anotherAccountsReadyFileIsHiddenAndMixedReferencesAreAtomic() throws Exception {
        JsonNode mine = create(owner);
        String other = mini(true);
        JsonNode theirs = create(other);
        String foreign = ready(theirs, "ID_CARD", other);
        String own = ready(mine, "ID_CARD", owner);
        byte[] before = payload(id(mine));
        status(
                save(mine, Map.of("idCardFrontFileId", own, "idCardBackFileId", foreign), owner),
                404);
        assertArrayEquals(before, payload(id(mine)));
        assertTrue(current(owner).path("draft").path("idCardFrontFileId").isNull());
        status(requestMini("GET", "/api/common/files/" + foreign, null, owner), 404);
    }

    @Test
    void sameAccountCannotBorrowAFileBoundToItsPreviousApprovedApplication() throws Exception {
        JsonNode previous = create(owner);
        String oldFile = ready(previous, "ID_CARD", owner);
        historyApproved(previous);
        JsonNode renewal = create(owner);
        assertNotEquals(previous.path("applicationId"), renewal.path("applicationId"));
        status(save(previous, Map.of("realName", "旧请求不得漂移到新复审"), owner), 409);
        status(save(renewal, Map.of("realName", "他人不得写本申请"), mini(true)), 404);
        status(save(renewal, Map.of("idCardFrontFileId", oldFile), owner), 404);
        assertEquals(
                id(previous),
                jdbc.queryForObject(
                        "SELECT application_id FROM vi_file WHERE id=?",
                        Long.class,
                        Long.parseLong(oldFile)));
    }

    @Test
    void uploadingVerifyingAndRejectedFilesCannotBecomeAcceptedEvidence() {
        JsonNode row = create(owner);
        JsonNode ticket = uploadTicket(row, "ID_CARD", owner);
        String file = ticket.path("fileId").asText();
        for (String state : List.of("UPLOADING", "VERIFYING", "REJECTED")) {
            jdbc.update("UPDATE vi_file SET status=? WHERE id=?", state, Long.parseLong(file));
            status(save(row, Map.of("idCardFrontFileId", file), owner), 409);
            assertEquals(row.path("version").asLong(), current(owner).path("version").asLong());
        }
    }

    @Test
    void wrongPurposeAndDuplicateDocumentSlotsAreRejectedWithoutPartialSave() throws Exception {
        JsonNode row = create(owner);
        String license = ready(row, "DRIVER_LICENSE", owner);
        status(save(row, Map.of("idCardFrontFileId", license), owner), 422);
        String card = ready(row, "ID_CARD", owner);
        status(save(row, Map.of("idCardFrontFileId", card, "idCardBackFileId", card), owner), 422);
        assertEquals(row.path("version").asLong(), current(owner).path("version").asLong());
    }

    @Test
    void companyAssignmentIsOnlyTheExistingAdminActionNotAMiniDraftField() {
        JsonNode row = create(owner);
        status(save(row, Map.of("ownerDeptId", Long.toString(COMPANY_A)), owner), 400);
        assertTrue(current(owner).path("ownerDeptId").isNull());
        assignCompany(row);
        assertEquals(Long.toString(COMPANY_A), current(owner).path("ownerDeptId").asText());
        assertEquals(
                0,
                number(
                        "SELECT new_assignment_enabled FROM vi_driver_profile WHERE id=?",
                        driverId(row)));
    }

    @Test
    void submittedApplicationDoesNotAllowDraftMutationOrAnotherActiveCreate() {
        JsonNode row = create(owner);
        seedSubmitted(row);
        status(save(row, Map.of("realName", "提交后改资料"), owner), 409);
        status(requestMini("POST", BASE, createBody(), owner), 409);
        assertEquals("SUBMITTED", current(owner).path("status").asText());
        assertFalse(session(owner).path("availableRoles").toString().contains("DRIVER"));
    }

    @Test
    void submittedApplicationStopsTicketCreationRawUploadAndCompletionButKeepsReadyRead()
            throws Exception {
        JsonNode row = create(owner);
        String ready = ready(row, "ID_CARD", owner);
        JsonNode rawTicket = uploadTicket(row, "DRIVER_LICENSE", owner);
        JsonNode completeTicket = uploadTicket(row, "ID_CARD", owner);
        status(upload(completeTicket, owner), 204);
        seedSubmitted(row);
        status(requestMini("POST", "/api/common/uploads", uploadBody(row, "ID_CARD"), owner), 409);
        status(upload(rawTicket, owner), 409);
        status(
                requestMini(
                        "POST",
                        "/api/common/uploads/"
                                + completeTicket.path("uploadId").asText()
                                + "/complete",
                        Map.of(),
                        owner),
                409);
        status(requestMini("GET", "/api/common/files/" + ready, null, owner), 200);
    }

    @Test
    void rejectedDraftCanOnlyCorrectStructuredRejectedFieldsEvenAcrossTwoSaves() throws Exception {
        JsonNode row = create(owner);
        row = data(save(row, Map.of("realName", "原姓名"), owner));
        jdbc.update(
                "UPDATE vi_driver_application SET"
                    + " status='REJECTED',reject_fields=JSON_ARRAY('licenseMainFileId'),public_review_opinion='请更换驾驶证主页',review_opinion='不应对外公开的内部审查记录'"
                    + " WHERE id=?",
                id(row));
        row = current(owner);
        status(save(row, Map.of("realName", "借补正换姓名"), owner), 409);
        row =
                data(
                        save(
                                row,
                                Map.of("licenseMainFileId", ready(row, "DRIVER_LICENSE", owner)),
                                owner));
        assertEquals("REJECTED", row.path("status").asText());
        status(save(row, Map.of("realName", "第二次绕过补正范围"), owner), 409);
        assertEquals("请更换驾驶证主页", row.path("publicReviewOpinion").asText());
        assertFalse(row.toString().contains("不应对外公开的内部审查记录"));
        assertEquals("原姓名", row.path("draft").path("realName").asText());
    }

    @Test
    void legacyRejectionWithoutStructuredFieldsNeverGuessesAnEditableScope() {
        JsonNode row = create(owner);
        jdbc.update(
                "UPDATE vi_driver_application SET"
                    + " status='REJECTED',reject_fields=NULL,review_opinion='历史自由文本' WHERE id=?",
                id(row));
        row = current(owner);
        assertTrue(row.path("rejectFields").isNull());
        status(save(row, Map.of("realName", "不能猜测放行"), owner), 409);
        status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 409);
    }

    @Test
    void renewalDraftDoesNotOverwriteTheEffectiveQualificationOrApprovedHistory() {
        JsonNode approved =
                data(save(create(owner), Map.of("realName", "原生效资料", "idCardNo", ID_CARD), owner));
        historyApproved(approved);
        Map<String, Object> effective = effectiveProfile(driverId(approved));
        byte[] oldPayload = payload(id(approved));
        JsonNode renewal = create(owner);
        renewal = data(save(renewal, Map.of("realName", "尚未审核的新资料"), owner));
        assertEquals("DRAFT", renewal.path("status").asText());
        assertEquals("APPROVED", renewal.path("effectiveQualificationStatus").asText());
        assertEquals(effective, effectiveProfile(driverId(approved)));
        assertArrayEquals(oldPayload, payload(id(approved)));
        assertTrue(session(owner).path("availableRoles").toString().contains("DRIVER"));
        assertFalse(renewal.path("canAcceptOrder").asBoolean());
    }

    @Test
    void expiredAndSuspendedHistoricalApplicationsOfferRevisionWithoutRestoringQualification() {
        for (String state : List.of("EXPIRED", "SUSPENDED")) {
            String applicant = mini(true);
            JsonNode previous = create(applicant);
            historyApproved(previous);
            jdbc.update(
                    "UPDATE vi_driver_application SET status=? WHERE id=?", state, id(previous));
            jdbc.update(
                    "UPDATE vi_driver_profile SET qualification_status=?,new_assignment_enabled=0"
                            + " WHERE id=?",
                    state,
                    driverId(previous));
            JsonNode current = current(applicant);
            assertTrue(
                    current.path("allowedActions")
                            .toString()
                            .contains("CREATE_QUALIFICATION_REVISION"),
                    current.toString());
            JsonNode revision = create(applicant);
            assertNotEquals(previous.path("applicationId"), revision.path("applicationId"));
            assertEquals("DRAFT", revision.path("status").asText());
            assertEquals(state, revision.path("effectiveQualificationStatus").asText());
            assertEquals(
                    0,
                    number(
                            "SELECT new_assignment_enabled FROM vi_driver_profile WHERE id=?",
                            driverId(previous)));
            assertFalse(session(applicant).path("availableRoles").toString().contains("DRIVER"));
        }
    }

    @Test
    void concurrentConsentPublicationCannotMixTheNoticeOrBypassFreshCreateAndSubmitConsent()
            throws Exception {
        ResponseEntity<String> staleCreate =
                whilePublishingConsent(
                        () -> requestMini("POST", BASE, createBody(), owner),
                        "q01-consent-test-v2",
                        "并发发布后的第二版说明");
        configChanged(staleCreate);
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application"));
        JsonNode row =
                data(
                        requestMini(
                                "POST",
                                BASE,
                                consentBody("q01-consent-test-v2", "并发发布后的第二版说明"),
                                owner));
        assertEquals(
                "并发发布后的第二版说明",
                jdbc.queryForObject(
                        "SELECT consent_text FROM vi_driver_application WHERE id=?",
                        String.class,
                        id(row)));
        row = data(save(row, completeValues(row), owner));
        assignCompany(row);
        trustedChecks(row);
        Map<String, Object> submit = submitBody(row);
        ResponseEntity<String> staleSubmit =
                whilePublishingConsent(
                        () -> requestMini("POST", BASE + "/submit", submit, owner),
                        "q01-consent-test-v3",
                        "并发提交前发布的第三版说明");
        configChanged(staleSubmit);
        assertEquals("DRAFT", current(owner).path("status").asText());
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application_submission"));
        assertEquals(
                "并发发布后的第二版说明",
                jdbc.queryForObject(
                        "SELECT consent_text FROM vi_driver_application WHERE id=?",
                        String.class,
                        id(row)));
    }

    @Test
    void savingNewSensitiveMaterialIncrementsItsRevisionAndDoesNotReuseItsHash() {
        JsonNode row = create(owner);
        JsonNode first = data(save(row, Map.of("realName", "初次资料"), owner));
        JsonNode next = data(save(first, Map.of("realName", "变更后的资料"), owner));
        assertTrue(
                next.path("materialRevision").asLong() > first.path("materialRevision").asLong());
        assertNotEquals(first.path("materialHash"), next.path("materialHash"));
    }

    @Test
    void rejectedOrSubmittedFirstApplicationCannotGrantDriverByRefreshingOrSwitching() {
        JsonNode row = create(owner);
        for (String state : List.of("SUBMITTED", "REJECTED")) {
            jdbc.update("UPDATE vi_driver_application SET status=? WHERE id=?", state, id(row));
            jdbc.update(
                    "UPDATE vi_driver_profile SET qualification_status=? WHERE id=?",
                    state,
                    driverId(row));
            assertFalse(session(owner).path("availableRoles").toString().contains("DRIVER"));
            status(
                    requestMini(
                            "POST",
                            "/api/common/session/role",
                            Map.of("role", "DRIVER", "expectedContextVersion", 0),
                            owner),
                    403);
        }
    }

    @Test
    void documentCipherAndPrivateFieldsNeverAppearInAuditOutboxOrIdempotencyResults() {
        JsonNode row =
                data(
                        save(
                                create(owner),
                                Map.of("realName", "测试敏感姓名甲", "idCardNo", ID_CARD),
                                owner));
        assertFalse(
                new String(payload(id(row)), java.nio.charset.StandardCharsets.UTF_8)
                        .contains(ID_CARD));
        assertNoPlainArtifacts(ID_CARD);
        assertNoPlainArtifacts("测试敏感姓名甲");
        String aad = "Q01:ACCOUNT:" + account + ":APPLICATION:" + id(row) + ":PAYLOAD";
        JsonNode decrypted = ViJson.tree(crypto.unenvelope(payload(id(row)), aad));
        assertEquals(ID_CARD, decrypted.path("idCardNo").asText());
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        crypto.unenvelope(
                                payload(id(row)),
                                "Q01:ACCOUNT:"
                                        + (account + 1)
                                        + ":APPLICATION:"
                                        + id(row)
                                        + ":PAYLOAD"));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () -> crypto.unenvelope(payload(id(row)), aad + "0"));
        String response = current(owner).toString();
        assertFalse(response.contains(ID_CARD));
        assertFalse(response.contains("sensitivePayloadCipher"));
        assertFalse(response.contains("providerResultIds"));
    }

    @Test
    void missingEncryptionKeyCannotFallBackToPlaintextOrPartiallySave() {
        JsonNode row = create(owner);
        byte[] before = payload(id(row));
        keys.setVersions(Map.of());
        status(save(row, Map.of("realName", "密钥缺失不落明文", "idCardNo", ID_CARD), owner), 503);
        assertArrayEquals(before, payload(id(row)));
        assertEquals(
                row.path("version").asLong(),
                jdbc.queryForObject(
                        "SELECT version FROM vi_driver_application WHERE id=?",
                        Long.class,
                        id(row)));
    }

    @Test
    void outboxFailureRollsBackDraftCipherVersionAndSuccessfulIntent() {
        JsonNode row = create(owner);
        byte[] before = payload(id(row));
        long version = row.path("version").asLong();
        doThrow(new IllegalStateException("隔离Q01事件故障"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), anyString(), any());
        status(save(row, Map.of("realName", "必须回滚的材料"), owner), 500);
        assertArrayEquals(before, payload(id(row)));
        assertEquals(
                version,
                jdbc.queryForObject(
                        "SELECT version FROM vi_driver_application WHERE id=?",
                        Long.class,
                        id(row)));
    }

    @Test
    void revokedMiniSessionCannotReuseAReadVersionToSaveApplication() {
        JsonNode row = create(owner);
        long session = miniTokens.resolveAccess(owner).sessionId();
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE id=?",
                session);
        status(save(row, Map.of("realName", "撤销后维护"), owner), 401);
        assertEquals(
                row.path("version").asLong(),
                jdbc.queryForObject(
                        "SELECT version FROM vi_driver_application WHERE id=?",
                        Long.class,
                        id(row)));
    }

    @Test
    void responsesAreNoStoreAndIdentifiersCannotBeProvidedThroughQueryOrExtraBody() {
        JsonNode row = create(owner);
        ResponseEntity<String> response = requestMini("GET", BASE, null, owner);
        assertEquals("no-store", response.getHeaders().getCacheControl());
        status(requestMini("GET", BASE + "?accountId=1", null, owner), 400);
        status(requestMini("GET", BASE + "?applicationId=" + id(row), null, owner), 400);
        status(save(row, Map.of("searchDeptIds", List.of("1")), owner), 400);
    }

    @Test
    void submitRequiresCompleteDocumentsAndAnExplicitValidCompany() throws Exception {
        JsonNode incomplete = create(owner);
        status(requestMini("POST", BASE + "/submit", submitBody(incomplete), owner), 422);
        JsonNode ready = data(save(incomplete, completeValues(incomplete), owner));
        trustedChecks(ready);
        assertTrue(current(owner).path("submitBlockers").toString().contains("COMPANY_UNASSIGNED"));
        status(requestMini("POST", BASE + "/submit", submitBody(ready), owner), 422);
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application_submission"));
        assignCompany(ready);
        trustedChecks(ready);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_A);
        assertTrue(
                current(owner).path("submitBlockers").toString().contains("COMPANY_UNAVAILABLE"));
        status(requestMini("POST", BASE + "/submit", submitBody(ready), owner), 422);
        assertEquals("DRAFT", current(owner).path("status").asText());
    }

    @Test
    void trustedChecksMustMatchCurrentMaterialAndIncludeVerifiedTraining() throws Exception {
        JsonNode row = completeApplication();
        assignCompany(row);
        assertTrue(current(owner).path("checks").toString().contains("UNAVAILABLE"));
        status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 422);
        trustedChecks(row);
        jdbc.update(
                "DELETE FROM vi_provider_verification WHERE application_id=? AND"
                        + " task_type='TRAINING'",
                id(row));
        status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 422);
        for (String alteration :
                List.of(
                        "material_revision=material_revision+1",
                        "material_hash=UNHEX(SHA2('wrong-material',256))",
                        "verified_at=NULL",
                        "verified_at=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2 DAY)",
                        "status='FAILED'")) {
            trustedChecks(row);
            // 仅使用源码固定SQL片段构造可信边界异常，不接受客户端字段构造SQL。
            jdbc.update(
                    "UPDATE vi_provider_verification SET " + alteration + " WHERE application_id=?",
                    id(row));
            status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 422);
        }
        trustedChecks(row);
        JsonNode changed = data(save(row, Map.of("realName", "旧核验不能解释新材料"), owner));
        status(requestMini("POST", BASE + "/submit", submitBody(changed), owner), 422);
        assertEquals("DRAFT", current(owner).path("status").asText());
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application_submission"));
    }

    @Test
    void strictSubmitFreezesOneSnapshotAndFourActualResponsesMatchThePrimaryContract()
            throws Exception {
        ResponseEntity<String> created = requestMini("POST", BASE, createBody(), owner);
        JsonNode row = data(created);
        ResponseEntity<String> saved = save(row, completeValues(row), owner);
        row = data(saved);
        assignCompany(row);
        trustedChecks(row);
        ResponseEntity<String> read = requestMini("GET", BASE, null, owner);
        Map<String, Object> body = submitBody(data(read));
        String reason = "Q01提交原因仅审计密文可见";
        body = new LinkedHashMap<>(body);
        body.put("reason", reason);
        ResponseEntity<String> submitted = requestMini("POST", BASE + "/submit", body, owner);
        JsonNode result = data(submitted);
        assertEquals("SUBMITTED", result.path("status").asText());
        assertEquals(1, result.path("submissionRevision").asLong());
        assertFalse(result.path("canAcceptOrder").asBoolean());
        assertFalse(session(owner).path("availableRoles").toString().contains("DRIVER"));
        assertEquals(
                1,
                number(
                        "SELECT COUNT(*) FROM vi_driver_application_submission WHERE"
                                + " application_id=?",
                        id(row)));
        Map<String, Object> frozen =
                jdbc.queryForMap(
                        "SELECT * FROM vi_driver_application_submission WHERE application_id=?",
                        id(row));
        assertEquals(COMPANY_A, ((Number) frozen.get("owner_dept_id")).longValue());
        assertEquals(
                body.get("expectedProfileVersion"),
                ((Number) frozen.get("profile_version")).longValue());
        assertEquals(4, ViJson.tree(frozen.get("provider_result_ids").toString()).size());
        assertArrayEquals(payload(id(row)), (byte[]) frozen.get("sensitive_payload_cipher"));
        String reasonAad = "Q01:ACCOUNT:" + account + ":APPLICATION:" + id(row) + ":SUBMIT:1";
        byte[] encryptedReason = (byte[]) frozen.get("submit_reason_cipher");
        assertEquals(reason, crypto.unenvelope(encryptedReason, reasonAad));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () -> crypto.unenvelope(encryptedReason, reasonAad + "0"));
        assertNoPlainArtifacts(ID_CARD);
        assertNoPlainArtifacts(reason);
        assertFalse(result.toString().contains(reason));
        status(requestMini("POST", "/api/common/uploads", uploadBody(row, "ID_CARD"), owner), 409);

        Path directory = Files.createTempDirectory("q01-contract-");
        Map<String, ResponseEntity<String>> responses =
                Map.of("create", created, "save", saved, "get", read, "submit", submitted);
        for (var entry : responses.entrySet()) {
            status(entry.getValue(), 200);
            Files.writeString(
                    directory.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        validateContractFiles(directory, "qualification");
    }

    @Test
    void concurrentSubmitAndIdempotentReplayCreateOnlyOneFrozenDecisionIntent() throws Exception {
        JsonNode row = completeApplication();
        assignCompany(row);
        trustedChecks(row);
        Map<String, Object> body = submitBody(row);
        String firstKey = UUID.randomUUID().toString(), secondKey = UUID.randomUUID().toString();
        var pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var first =
                    pool.submit(
                            () -> {
                                start.await();
                                return requestMini(
                                        "POST", BASE + "/submit", body, owner, firstKey, "owner");
                            });
            var second =
                    pool.submit(
                            () -> {
                                start.await();
                                return requestMini(
                                        "POST", BASE + "/submit", body, owner, secondKey, "owner");
                            });
            start.countDown();
            ResponseEntity<String> a = first.get(10, TimeUnit.SECONDS),
                    b = second.get(10, TimeUnit.SECONDS);
            List<Integer> statuses =
                    new ArrayList<>(List.of(a.getStatusCode().value(), b.getStatusCode().value()));
            statuses.sort(Integer::compareTo);
            assertEquals(List.of(200, 409), statuses);
            String won = a.getStatusCode().value() == 200 ? firstKey : secondKey;
            data(requestMini("POST", BASE + "/submit", body, owner, won, "owner"));
            Map<String, Object> changed = new LinkedHashMap<>(body);
            changed.put("reason", "同键另一个提交意图");
            status(requestMini("POST", BASE + "/submit", changed, owner, won, "owner"), 409);
            assertEquals(
                    1,
                    number(
                            "SELECT COUNT(*) FROM vi_driver_application_submission WHERE"
                                    + " application_id=?",
                            id(row)));
            assertEquals(
                    1,
                    number(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                + " aggregate_type='VI_DRIVER_APPLICATION' AND aggregate_id=? AND"
                                + " action='QUALIFICATION_SUBMITTED'",
                            id(row)));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void changedProfileVersionOrConsentCannotBeSilentlyAcceptedAtSubmit() throws Exception {
        JsonNode row = completeApplication();
        Map<String, Object> stale = new LinkedHashMap<>(submitBody(row));
        assignCompany(row);
        trustedChecks(row);
        status(requestMini("POST", BASE + "/submit", stale, owner), 409);
        configureConsent("q01-consent-test-v2", "提交前已更新同意正文");
        status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 409);
        assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application_submission"));
    }

    @Test
    void renewalSubmissionPreservesTheEffectiveLicenseAndExistingVehicleResponsibility()
            throws Exception {
        JsonNode approved = create(owner);
        historyApproved(approved);
        Map<String, Object> effective = effectiveProfile(driverId(approved));
        long order = heldVehicle(driverId(approved));
        Map<String, Object> orderBefore =
                jdbc.queryForMap("SELECT * FROM vi_order WHERE id=?", order);
        Map<String, Object> occupation =
                jdbc.queryForMap(
                        "SELECT * FROM vi_driver_occupancy WHERE driver_id=?", driverId(approved));
        JsonNode renewal = create(owner);
        renewal = data(save(renewal, completeValues(renewal), owner));
        trustedChecks(renewal);
        JsonNode submitted =
                data(requestMini("POST", BASE + "/submit", submitBody(renewal), owner));
        assertEquals("SUBMITTED", submitted.path("status").asText());
        assertEquals("APPROVED", submitted.path("effectiveQualificationStatus").asText());
        assertEquals(effective, effectiveProfile(driverId(approved)));
        assertEquals(orderBefore, jdbc.queryForMap("SELECT * FROM vi_order WHERE id=?", order));
        assertEquals(
                occupation,
                jdbc.queryForMap(
                        "SELECT * FROM vi_driver_occupancy WHERE driver_id=?", driverId(approved)));
        assertTrue(session(owner).path("availableRoles").toString().contains("DRIVER"));
    }

    @Test
    void databaseFailureRollsBackTheFrozenSnapshotAndInitialProfileTransition() throws Exception {
        JsonNode row = completeApplication();
        assignCompany(row);
        trustedChecks(row);
        Map<String, Object> before =
                jdbc.queryForMap("SELECT * FROM vi_driver_application WHERE id=?", id(row));
        Map<String, Object> profile = effectiveProfile(driverId(row));
        jdbc.execute(
                "ALTER TABLE vi_driver_application_submission ADD CONSTRAINT ck_q01_test_reject"
                        + " CHECK (submission_revision<0)");
        try {
            status(requestMini("POST", BASE + "/submit", submitBody(row), owner), 500);
            Map<String, Object> after =
                    jdbc.queryForMap("SELECT * FROM vi_driver_application WHERE id=?", id(row));
            assertEquals(before.get("status"), after.get("status"));
            assertEquals(before.get("version"), after.get("version"));
            assertArrayEquals(
                    (byte[]) before.get("sensitive_payload_cipher"),
                    (byte[]) after.get("sensitive_payload_cipher"));
            assertEquals(profile, effectiveProfile(driverId(row)));
            assertEquals(0, number("SELECT COUNT(*) FROM vi_driver_application_submission"));
            assertEquals(
                    0,
                    number(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                + " aggregate_type='VI_DRIVER_APPLICATION' AND aggregate_id=? AND"
                                + " action='QUALIFICATION_SUBMITTED'",
                            id(row)));
        } finally {
            jdbc.execute(
                    "ALTER TABLE vi_driver_application_submission DROP CHECK ck_q01_test_reject");
        }
    }

    private JsonNode completeApplication() throws Exception {
        JsonNode row = create(owner);
        return data(save(row, completeValues(row), owner));
    }

    private void validateContractFiles(Path directory, String mode) throws Exception {
        Path repo = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!Files.exists(repo.resolve("docs/vehicle"))) repo = repo.getParent();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/q01_contract.py")
                                        .toString(),
                                repo.toString(),
                                directory.toString(),
                                mode)
                        .redirectErrorStream(true)
                        .start();
        String output =
                new String(
                        process.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(process.waitFor(15, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    private Map<String, Object> completeValues(JsonNode application) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>(documents(application, owner));
        values.put("realName", "测试申请人");
        values.put("idCardNo", ID_CARD);
        values.put("licenseClass", "C1");
        values.put("firstIssueDate", "2010-01-01");
        values.put("validFrom", "2020-01-01");
        values.put("validTo", "2099-12-31");
        values.put("longTermValid", false);
        return values;
    }

    private void trustedChecks(JsonNode application) {
        // 仅Q04/Q03上游完成事实的消费边界；生产者真实微信设备验签/双人/墙钟链分别在各自IT验证。
        long id = id(application);
        jdbc.update("DELETE FROM vi_provider_verification WHERE application_id=?", id);
        VerificationCompletedBoundaryFixture.seed(
                jdbc, crypto, idempotency, id, miniTokens.resolveAccess(owner).sessionId());
        TrainingCompletedBoundaryFixture.seed(jdbc, crypto, idempotency, id);
    }

    private long heldVehicle(long driver) {
        long order = SEQ.incrementAndGet(),
                assignment = SEQ.incrementAndGet(),
                invite = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(id,order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,active_assignment_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,'{}','PICKED_UP','DRIVER',?,?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'Q01-test')",
                order,
                "Q01-ORDER-" + order,
                account,
                SEQ.incrementAndGet(),
                SITE_DEPT,
                SEQ.incrementAndGet(),
                driver,
                assignment);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                assignment,
                order,
                driver,
                invite);
        jdbc.update(
                "INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES"
                        + " (?,'ASSIGNMENT',?,?)",
                driver,
                assignment,
                order);
        return order;
    }

    private String mini(boolean verified) {
        long suffix = SEQ.incrementAndGet();
        String phone = "139" + String.format("%08d", Math.floorMod(suffix, 100000000));
        String openId = "q01-" + UUID.randomUUID();
        // 与真实微信登录共用AUTO_INCREMENT；手工SEQ可能被其它套件的真实登录追上，不能混用两种主键分配。
        jdbc.update(
                "INSERT INTO"
                    + " vi_account(app_id,open_id,nickname,status,phone_cipher,phone_lookup,phone_verified_at,phone_source)"
                    + " VALUES ('wx0000000000000000',?,'Q01本人','ACTIVE',?,?,?,?)",
                openId,
                verified ? phones.encrypt(phone) : null,
                verified ? phones.lookup(phone) : null,
                verified ? java.sql.Timestamp.from(Instant.now()) : null,
                verified ? "WECHAT" : null);
        long id =
                jdbc.queryForObject(
                        "SELECT id FROM vi_account WHERE app_id='wx0000000000000000' AND open_id=?",
                        Long.class,
                        openId);
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES"
                        + " (?,UTC_TIMESTAMP(3))",
                id);
        return miniSession(id, "OWNER", null);
    }

    private String miniSession(long accountId, String role, Long employee) {
        ViSession session = new ViSession();
        session.setAccountId(accountId);
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole(role);
        session.setLinkedSysUserId(employee);
        session.setAbsoluteExpiresAt(Instant.now().plusSeconds(3600));
        sessions.insert(session);
        return miniTokens.issueForSession(session).accessToken();
    }

    private String stationIdentity() {
        String person = mini(true);
        long accountId = miniTokens.resolveAccess(person).accountId();
        String admin = scopedWithScope(COMPANY_A, "4", "biz:order:list");
        long user = adminUser(admin);
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,source,status,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " SELECT"
                    + " id,?,UTC_TIMESTAMP(3),app_id,'WECHAT_PHONE_MATCH','ACTIVE',phone_lookup,phone_verified_at,0"
                    + " FROM vi_account WHERE id=?",
                user,
                accountId);
        long station = station(SITE_DEPT);
        jdbc.update(
                "INSERT INTO vi_station_grant(sys_user_id,station_id,granted_by,reason,enabled)"
                        + " VALUES (?,?,1,'隔离Q01工作授权',1)",
                user,
                station);
        return miniSession(accountId, "STATION", user);
    }

    private void configureConsent(String version, String text) {
        jdbc.update(
                "DELETE FROM sys_config WHERE config_key LIKE 'vi.biz.qualification.consent.%'");
        jdbc.update(
                "INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES"
                    + " ('Q01测试授权版本','vi.biz.qualification.consent.version',?,'N'),('Q01测试授权正文','vi.biz.qualification.consent.text',?,'N')",
                version,
                text);
    }

    private Map<String, Object> createBody() {
        return consentBody(CONSENT_VERSION, CONSENT_TEXT);
    }

    private Map<String, Object> consentBody(String version, String text) {
        return new LinkedHashMap<>(
                Map.of(
                        "consentVersion",
                        version,
                        "consentTextHash",
                        consentHash(text),
                        "acceptedConsent",
                        true));
    }

    private static String consentHash(String text) {
        try {
            return java.util.HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            text.getBytes(
                                                    java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private ResponseEntity<String> whilePublishingConsent(
            java.util.function.Supplier<ResponseEntity<String>> operation,
            String version,
            String text)
            throws Exception {
        CountDownLatch locked = new CountDownLatch(1), commit = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            // 真实数据库发布事务按同一key顺序锁住两项，先改正文、后改版本，读方不能见混合状态。
            var publisher =
                    pool.submit(
                            () ->
                                    new TransactionTemplate(transactionManager)
                                            .execute(
                                                    status -> {
                                                        jdbc.queryForList(
                                                                "SELECT config_key FROM sys_config"
                                                                    + " WHERE config_key IN (?,?)"
                                                                    + " ORDER BY config_key FOR"
                                                                    + " UPDATE",
                                                                "vi.biz.qualification.consent.text",
                                                                "vi.biz.qualification.consent.version");
                                                        jdbc.update(
                                                                "UPDATE sys_config SET"
                                                                    + " config_value=? WHERE"
                                                                    + " config_key='vi.biz.qualification.consent.text'",
                                                                text);
                                                        locked.countDown();
                                                        try {
                                                            assertTrue(
                                                                    commit.await(
                                                                            5, TimeUnit.SECONDS));
                                                        } catch (InterruptedException error) {
                                                            Thread.currentThread().interrupt();
                                                            throw new IllegalStateException(error);
                                                        }
                                                        return jdbc.update(
                                                                "UPDATE sys_config SET"
                                                                    + " config_value=? WHERE"
                                                                    + " config_key='vi.biz.qualification.consent.version'",
                                                                version);
                                                    }));
            assertTrue(locked.await(3, TimeUnit.SECONDS));
            var pending = pool.submit(operation::get);
            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> pending.get(150, TimeUnit.MILLISECONDS));
            commit.countDown();
            assertEquals(1, publisher.get(5, TimeUnit.SECONDS));
            return pending.get(10, TimeUnit.SECONDS);
        } finally {
            commit.countDown();
            pool.shutdownNow();
        }
    }

    private void configChanged(ResponseEntity<String> response) {
        status(response, 409);
        assertEquals(
                "VI_CONFIG_CHANGED", ViJson.tree(response.getBody()).path("errorCode").asText());
    }

    private JsonNode create(String token) {
        return data(requestMini("POST", BASE, createBody(), token));
    }

    private JsonNode current(String token) {
        return data(requestMini("GET", BASE, null, token));
    }

    private JsonNode session(String token) {
        return data(requestMini("GET", "/api/common/session", null, token));
    }

    private long id(JsonNode row) {
        return Long.parseLong(row.path("applicationId").asText());
    }

    private long driverId(JsonNode row) {
        return Long.parseLong(row.path("driverId").asText());
    }

    private Map<String, Object> draftBody(JsonNode row, Map<String, Object> values) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("applicationId", row.path("applicationId").asText());
        body.put("expectedVersion", row.path("version").asLong());
        body.putAll(values);
        return body;
    }

    private ResponseEntity<String> save(JsonNode row, Map<String, Object> values, String token) {
        return requestMini("PUT", BASE, draftBody(row, values), token);
    }

    private Map<String, Object> submitBody(JsonNode row) {
        return Map.of(
                "applicationId",
                row.path("applicationId").asText(),
                "expectedVersion",
                row.path("version").asLong(),
                "expectedProfileVersion",
                jdbc.queryForObject(
                        "SELECT version FROM vi_driver_profile WHERE id=?",
                        Long.class,
                        driverId(row)),
                "reason",
                "核对本人资料后申请审核");
    }

    private void assignCompany(JsonNode row) {
        data(
                call(
                        "PATCH",
                        "/api/admin/drivers/" + driverId(row),
                        Map.of(
                                "expectedVersion",
                                jdbc.queryForObject(
                                        "SELECT version FROM vi_driver_profile WHERE id=?",
                                        Long.class,
                                        driverId(row)),
                                "ownerDeptId",
                                Long.toString(COMPANY_A),
                                "reason",
                                "明确认领隔离测试申请人公司"),
                        root));
    }

    private Map<String, Object> uploadBody(JsonNode application, String purpose) {
        return Map.of(
                "purpose",
                purpose,
                "fileName",
                "本人证件.png",
                "mimeType",
                "image/png",
                "size",
                png.length,
                "applicationId",
                application.path("applicationId").asText());
    }

    private JsonNode uploadTicket(JsonNode application, String purpose, String token) {
        return data(
                requestMini(
                        "POST", "/api/common/uploads", uploadBody(application, purpose), token));
    }

    private ResponseEntity<String> upload(JsonNode ticket, String token) {
        HttpHeaders headers = headers(token, null, "owner");
        headers.setContentType(MediaType.IMAGE_PNG);
        ticket.path("requiredHeaders")
                .fields()
                .forEachRemaining(entry -> headers.set(entry.getKey(), entry.getValue().asText()));
        return http.exchange(
                "http://127.0.0.1:" + port + ticket.path("uploadUrl").asText(),
                HttpMethod.PUT,
                new HttpEntity<>(png, headers),
                String.class);
    }

    private String ready(JsonNode application, String purpose, String token) throws Exception {
        JsonNode ticket = uploadTicket(application, purpose, token);
        status(upload(ticket, token), 204);
        JsonNode file =
                data(
                        requestMini(
                                "POST",
                                "/api/common/uploads/"
                                        + ticket.path("uploadId").asText()
                                        + "/complete",
                                Map.of(),
                                token));
        assertEquals("READY", file.path("status").asText());
        return ticket.path("fileId").asText();
    }

    private Map<String, Object> documents(JsonNode application, String token) throws Exception {
        return Map.of(
                "idCardFrontFileId",
                ready(application, "ID_CARD", token),
                "idCardBackFileId",
                ready(application, "ID_CARD", token),
                "licenseMainFileId",
                ready(application, "DRIVER_LICENSE", token),
                "licenseSubFileId",
                ready(application, "DRIVER_LICENSE", token));
    }

    private void seedSubmitted(JsonNode row) {
        // 仅建立未来审核前的历史事实，以测试不可变/文件守卫；不冒充本轮真实提交动作已通过。
        jdbc.update(
                "UPDATE vi_driver_application SET"
                        + " status='SUBMITTED',submitted_at=UTC_TIMESTAMP(3),owner_dept_id=? WHERE"
                        + " id=?",
                COMPANY_A,
                id(row));
    }

    private void historyApproved(JsonNode row) {
        // Q05 尚未实现：这里只验证已有生效资格不会被复审草稿覆盖，不声称完成真实审批链。
        jdbc.update(
                "UPDATE vi_driver_application SET"
                        + " status='APPROVED',reviewer_user_id=1,reviewed_at=UTC_TIMESTAMP(3) WHERE"
                        + " id=?",
                id(row));
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                    + " owner_dept_id=?,qualification_status='APPROVED',qualification_revision=?,new_assignment_enabled=1,listening_enabled=1,license_class='C1',license_valid_to=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 YEAR),review_due_at=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 1 YEAR) WHERE"
                    + " id=?",
                COMPANY_A,
                id(row),
                driverId(row));
    }

    private Map<String, Object> effectiveProfile(long driver) {
        return jdbc.queryForMap(
                "SELECT"
                    + " qualification_status,qualification_revision,owner_dept_id,new_assignment_enabled,listening_enabled,license_class,license_valid_to,license_long_term,review_due_at"
                    + " FROM vi_driver_profile WHERE id=?",
                driver);
    }

    private byte[] payload(long application) {
        return jdbc.queryForObject(
                "SELECT sensitive_payload_cipher FROM vi_driver_application WHERE id=?",
                byte[].class,
                application);
    }

    private int number(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private long adminUser(String token) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId();
    }

    private HttpHeaders headers(String token, String key, String client) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) headers.setBearerAuth(token);
        if (key != null) headers.set("Idempotency-Key", key);
        if (client != null) headers.set("X-Client-Type", client);
        return headers;
    }

    private ResponseEntity<String> requestMini(
            String method, String path, Object body, String token) {
        return requestMini(method, path, body, token, UUID.randomUUID().toString(), "owner");
    }

    private ResponseEntity<String> requestMini(
            String method, String path, Object body, String token, String key, String client) {
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers(token, key, client)),
                String.class);
    }

    private void status(ResponseEntity<String> response, int expected) {
        assertEquals(expected, response.getStatusCode().value(), response.getBody());
    }

    private void assertNoPlainArtifacts(String fragment) {
        for (var row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log")) {
            for (Object value : row.values())
                assertFalse(java.util.Objects.toString(value, "").contains(fragment));
        }
        for (String value : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class))
            assertFalse(value.contains(fragment));
        for (String value :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) assertFalse(value.contains(fragment));
    }
}
