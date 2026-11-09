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
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Q02 真实后台 HTTP、部门权限、MySQL 事务及私有 MP4 传输；不替换课程、文件或鉴权 Service。 历史考试行仅证明发布新版本不覆盖既有引用，不冒充尚未实现的 Q03
 * 学习、阅卷或正式培训通过。
 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=q02-isolated-test-phone-key",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class Q02TrainingIT extends PricingHttpFixture {
    static final String BASE = "/api/admin/training-courses";
    static final String[] TRAINING_PERMISSIONS = {
        "biz:trainingcourse:list", "biz:trainingcourse:create", "biz:trainingcourse:query",
        "biz:trainingcourse:edit", "biz:trainingcourse:publish", "biz:uploads:create",
        "biz:uploads:complete", "biz:files:query", "biz:files:access"
    };
    static final String SECRET_ANSWER = "Q02_仅题库管理员可见的标准选项";
    static final Path PRIVATE_ROOT = privateRoot();

    @Autowired MiniTokenService miniTokens;
    @Autowired ViSessionMapper sessions;
    @Autowired IntegrationKeyProperties keys;
    @Autowired IntegrationCrypto auditCrypto;
    @Autowired PlatformTransactionManager transactions;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;
    @MockitoSpyBean AuditService audits;

    String root;
    String companyA;
    String companyB;
    byte[] video;
    int trainingProofCount;

    @DynamicPropertySource
    static void privateFiles(DynamicPropertyRegistry properties) {
        properties.add("vi.files.root", () -> PRIVATE_ROOT.toString());
    }

    @BeforeEach
    void prepareTraining() throws Exception {
        // 父夹具已经验证数据库名称及保护标记；所有套件由主执行者串行，不能并发清理共享隔离库。
        for (String table :
                List.of(
                        "vi_training_playback",
                        "vi_training_attempt",
                        "vi_training_progress",
                        "vi_training_question",
                        "vi_training_course",
                        "vi_file")) {
            jdbc.execute("DELETE FROM " + table);
        }
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        keys.setCurrentVersion("q02-test");
        keys.setVersions(
                new HashMap<>(Map.of("q02-test", Base64.getEncoder().encodeToString(key))));
        root = rootToken();
        companyA = scoped(COMPANY_A, TRAINING_PERMISSIONS);
        companyB = scoped(COMPANY_B, TRAINING_PERMISSIONS);
        trainingProofCount =
                count("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'");
        try (var input = getClass().getResourceAsStream("/integration/q02/flower.mp4")) {
            assertNotNull(input, "需要仓库内真实 MP4 样本，不允许将 READY 文件直接写库代替上传");
            video = input.readAllBytes();
        }
        assertEquals(
                "0cd83d944a6ca7822b4a8306cecc60a36e859b041f6702c6a1ad9ead78924451", sha(video));
    }

    @AfterAll
    static void removeOwnPrivateBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    @Test
    void explicitlyOwnedMinimalDraftDoesNotInventMediaQuestionsOrTrainingPass() {
        JsonNode row = draft(COMPANY_A, companyA);
        assertEquals(Long.toString(COMPANY_A), row.path("ownerDeptId").asText());
        assertEquals("DRAFT", row.path("status").asText());
        assertEquals(1, row.path("courseVersion").asInt());
        assertEquals(0, row.path("questions").size());
        for (String field :
                List.of(
                        "mediaFileId",
                        "requiredDurationSeconds",
                        "passScore",
                        "attemptTimeLimitSeconds",
                        "publishedAt",
                        "sourceTrainingId")) {
            assertTrue(row.path(field).isNull(), field);
        }
        assertTrue(row.path("publishBlockers").size() > 0);
        assertEquals(
                trainingProofCount,
                count("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
    }

    @Test
    void fullReplacementCanPersistIncompleteDraftAndExplicitlyClearOptionalValues() {
        JsonNode row = draft(COMPANY_A, companyA);
        Map<String, Object> body = replacement(row);
        body.put("description", "尚待运营完善");
        body.put("passScore", 0);
        JsonNode saved = save(id(row), body, companyA);
        assertEquals("尚待运营完善", saved.path("description").asText());
        assertEquals(0, saved.path("passScore").asInt());
        body = replacement(saved);
        body.put("description", null);
        body.put("passScore", null);
        JsonNode cleared = save(id(row), body, companyA);
        assertTrue(cleared.path("description").isNull());
        assertTrue(cleared.path("passScore").isNull());
        assertEquals("DRAFT", cleared.path("status").asText());
    }

    @Test
    void realUploadedMediaAndQuestionBankCanBePublishedWithImmutableContentHash() {
        JsonNode ready = completeDraft(COMPANY_A, companyA);
        JsonNode published = publishTraining(ready, companyA);
        assertEquals("PUBLISHED", published.path("status").asText());
        assertTrue(published.path("publishedAt").asText().endsWith("Z"));
        assertTrue(published.path("contentHash").asText().matches("[0-9a-f]{64}"));
        assertEquals(0, published.path("publishBlockers").size());
        assertEquals(3, published.path("questions").size());
        assertTrue(published.path("media").path("durationMillis").asLong() >= 5000);
        assertEquals(960, published.path("media").path("width").asInt());
        assertEquals(540, published.path("media").path("height").asInt());
    }

    @Test
    void fiveActualHttpResponsesConformToPrimaryAndTaskOpenApi() throws Exception {
        Path samples = Files.createTempDirectory("q02-contract-");
        Map<String, ResponseEntity<String>> responses = new LinkedHashMap<>();
        ResponseEntity<String> created = call("POST", BASE, createBody(COMPANY_A), companyA);
        responses.put("create", created);
        JsonNode row = data(created);
        responses.put("list", call("GET", BASE, null, companyA));
        responses.put("detail", get(id(row), companyA));
        long file = upload(id(row), companyA);
        responses.put("save", call("PUT", BASE + "/" + id(row), completeBody(row, file), companyA));
        responses.put(
                "publish",
                call(
                        "POST",
                        BASE + "/" + id(row) + "/publish",
                        action(data(responses.get("save"))),
                        companyA));
        for (var entry : responses.entrySet()) {
            status(entry.getValue(), 200);
            Files.writeString(
                    samples.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        Path repo = repository();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/q02_contract.py")
                                        .toString(),
                                repo.toString(),
                                samples.toString())
                        .redirectErrorStream(true)
                        .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(20, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    @Test
    void missingMiniOrForgedClientRealmCannotEnterAnyAdminTrainingEndpoint() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (String token : List.of("", mini())) {
            for (String client : List.of("admin", "owner", "station", "driver")) {
                status(raw("GET", BASE, null, token, null, client), 401);
                status(raw("GET", BASE + "/" + id(row), null, token, null, client), 401);
                status(
                        raw(
                                "POST",
                                BASE,
                                createBody(COMPANY_A),
                                token,
                                UUID.randomUUID().toString(),
                                client),
                        401);
                status(
                        raw(
                                "PUT",
                                BASE + "/" + id(row),
                                replacement(row),
                                token,
                                UUID.randomUUID().toString(),
                                client),
                        401);
                status(
                        raw(
                                "POST",
                                BASE + "/" + id(row) + "/publish",
                                action(row),
                                token,
                                UUID.randomUUID().toString(),
                                client),
                        401);
            }
        }
    }

    @Test
    void eachOfFiveFunctionalPermissionsIsRequiredIndependently() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (String missing : List.of("list", "query", "create", "edit", "publish")) {
            String[] permissions =
                    Arrays.stream(TRAINING_PERMISSIONS)
                            .filter(value -> !value.equals("biz:trainingcourse:" + missing))
                            .toArray(String[]::new);
            String token = scoped(COMPANY_A, permissions);
            ResponseEntity<String> response =
                    switch (missing) {
                        case "list" -> call("GET", BASE, null, token);
                        case "query" -> get(id(row), token);
                        case "create" -> call("POST", BASE, createBody(COMPANY_A), token);
                        case "edit" -> call("PUT", BASE + "/" + id(row), replacement(row), token);
                        default ->
                                call("POST", BASE + "/" + id(row) + "/publish", action(row), token);
                    };
            error(response, 403, "VI_FORBIDDEN");
        }
    }

    @Test
    void listRowsAndTotalUseRealCompanyScopeAndDetailHidesAnotherCompany() {
        JsonNode a = draft(COMPANY_A, companyA);
        JsonNode b = draft(COMPANY_B, companyB);
        JsonNode list = tree(call("GET", BASE + "?pageNum=1&pageSize=1", null, companyA));
        assertEquals(1, list.path("total").asInt());
        assertEquals(Long.toString(id(a)), list.path("rows").get(0).path("trainingId").asText());
        error(get(id(b), companyA), 404, "VI_RESOURCE_NOT_VISIBLE");
        error(
                call("PUT", BASE + "/" + id(b), replacement(b), companyA),
                404,
                "VI_RESOURCE_NOT_VISIBLE");
        error(
                call("POST", BASE + "/" + id(b) + "/publish", action(b), companyA),
                404,
                "VI_RESOURCE_NOT_VISIBLE");
        assertEquals(0, field(id(b), "version", Long.class));
    }

    @Test
    void emptyCompanyDataScopeFailsClosedForReadsAndWrites() {
        JsonNode row = draft(COMPANY_A, companyA);
        String empty = scopedWithScope(COMPANY_A, "2", TRAINING_PERMISSIONS);
        JsonNode list = tree(call("GET", BASE, null, empty));
        assertEquals(0, list.path("total").asInt());
        assertEquals(0, list.path("rows").size());
        status(get(id(row), empty), 404);
        status(call("POST", BASE, createBody(COMPANY_A), empty), 403);
        status(call("PUT", BASE + "/" + id(row), replacement(row), empty), 404);
    }

    @Test
    void ownerMustBeExplicitValidAndAuthorizedRatherThanInferredFromPrimaryDepartment() {
        status(call("POST", BASE, Map.of("title", "不得猜归属"), companyA), 400);
        status(call("POST", BASE, createBody(COMPANY_B), companyA), 403);
        error(call("POST", BASE, createBody(Long.MAX_VALUE), root), 403, "VI_FORBIDDEN");
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_B);
        error(call("POST", BASE, createBody(COMPANY_B), root), 403, "VI_FORBIDDEN");
        jdbc.update("UPDATE sys_dept SET status='0',del_flag='2' WHERE dept_id=?", COMPANY_B);
        error(call("POST", BASE, createBody(COMPANY_B), root), 403, "VI_FORBIDDEN");
        // 跨部门管理员明确选择公司乙，不能按其主部门或创建人偷偷写成公司甲。
        jdbc.update("UPDATE sys_dept SET del_flag='0' WHERE dept_id=?", COMPANY_B);
        JsonNode row = draft(COMPANY_B, root);
        assertEquals(Long.toString(COMPANY_B), row.path("ownerDeptId").asText());
    }

    @Test
    void bodyCannotForgeIdsVersionsPublicationOwnershipOrInternalDataScope() {
        for (String field :
                List.of(
                        "trainingId",
                        "courseKey",
                        "courseVersion",
                        "status",
                        "publishedAt",
                        "publishedByUserId",
                        "version",
                        "contentHash",
                        "params",
                        "searchDeptIds",
                        "visibleDeptIds")) {
            Map<String, Object> body = new LinkedHashMap<>(createBody(COMPANY_A));
            body.put(field, "PUBLISHED");
            status(call("POST", BASE, body, companyA), 400);
        }
        JsonNode row = draft(COMPANY_A, companyA);
        Map<String, Object> body = replacement(row);
        body.put("ownerDeptId", Long.toString(COMPANY_B));
        status(call("PUT", BASE + "/" + id(row), body, companyA), 400);
    }

    @Test
    void malformedIdentifiersPaginationSortAndInternalQueryFieldsAreRejected() {
        for (String suffix :
                List.of(
                        "?pageNum=0",
                        "?pageSize=101",
                        "?pageSize=1.5",
                        "?sort=id%20desc;drop",
                        "?ownerDeptId=01",
                        "?params.dataScope=1",
                        "?visibleDeptIds=1",
                        "?unexpected=1",
                        "?status=APPROVED")) {
            status(call("GET", BASE + suffix, null, companyA), 400);
        }
        for (String id : List.of("0", "01", "-1", "9223372036854775808", "1.5")) {
            status(getPath(id, companyA), 400);
        }
    }

    @Test
    void fullReplacementAndUuidAndExpectedVersionAreMandatory() {
        JsonNode row = draft(COMPANY_A, companyA);
        Map<String, Object> complete = replacement(row);
        for (String field : new ArrayList<>(complete.keySet())) {
            Map<String, Object> incomplete = new LinkedHashMap<>(complete);
            incomplete.remove(field);
            status(call("PUT", BASE + "/" + id(row), incomplete, companyA), 400);
        }
        for (String field : List.of("title", "questions", "expectedVersion", "reason")) {
            Map<String, Object> explicitNull = new LinkedHashMap<>(complete);
            explicitNull.put(field, null);
            error(
                    call("PUT", BASE + "/" + id(row), explicitNull, companyA),
                    400,
                    "VI_REQUEST_INVALID");
        }
        status(callKey("PUT", BASE + "/" + id(row), complete, companyA, null), 400);
        status(callKey("PUT", BASE + "/" + id(row), complete, companyA, "not-uuid"), 400);
        Map<String, Object> forged = new LinkedHashMap<>(complete);
        forged.put("expectedVersion", "0");
        status(call("PUT", BASE + "/" + id(row), forged, companyA), 400);
        JsonNode saved = save(id(row), complete, companyA);
        error(call("PUT", BASE + "/" + id(row), complete, companyA), 409, "VI_VERSION_CONFLICT");
        assertEquals(saved.path("version").asLong(), field(id(row), "version", Long.class));
    }

    @Test
    void boundedTitlesThresholdsAndReasonsAreValidatedWithoutReplacingFormalDefaults() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (var entry :
                Map.<String, Object>of(
                                "title",
                                " ",
                                "description",
                                "字".repeat(5001),
                                "requiredDurationSeconds",
                                0,
                                "passScore",
                                101,
                                "attemptTimeLimitSeconds",
                                59,
                                "reason",
                                "字".repeat(501))
                        .entrySet()) {
            Map<String, Object> body = replacement(row);
            body.put(entry.getKey(), entry.getValue());
            boolean formatError = Set.of("title", "description", "reason").contains(entry.getKey());
            error(
                    call("PUT", BASE + "/" + id(row), body, companyA),
                    formatError ? 400 : 422,
                    formatError ? "VI_REQUEST_INVALID" : "VI_DATA_INVALID");
        }
        assertEquals(0, field(id(row), "version", Long.class));
    }

    @Test
    void singleMultipleAndTrueFalseQuestionsPersistTheirExactConfiguredAnswers() {
        JsonNode row = draft(COMPANY_A, companyA);
        Map<String, Object> body = replacement(row);
        body.put("questions", questions());
        JsonNode saved = save(id(row), body, companyA);
        assertEquals(3, saved.path("questions").size());
        assertEquals("SINGLE", saved.path("questions").get(0).path("type").asText());
        assertEquals("MULTIPLE", saved.path("questions").get(1).path("type").asText());
        assertEquals("TRUE_FALSE", saved.path("questions").get(2).path("type").asText());
        assertEquals(
                "A", saved.path("questions").get(0).path("correctOptionCodes").get(0).asText());
        assertEquals(
                3, count("SELECT COUNT(*) FROM vi_training_question WHERE training_id=?", id(row)));
    }

    @Test
    void duplicateOrMalformedQuestionCodesAreRejectedAtomically() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (String code : List.of("Q1", "bad code", "字")) {
            List<Map<String, Object>> questions = questions();
            questions.get(1).put("questionCode", code);
            assertInvalidQuestions(row, questions);
        }
        List<Map<String, Object>> overlong = questions();
        overlong.get(1).put("questionCode", "x".repeat(65));
        assertInvalidQuestions(row, overlong, 400);
        assertEquals(
                0, count("SELECT COUNT(*) FROM vi_training_question WHERE training_id=?", id(row)));
    }

    @Test
    void questionOptionsRequireDistinctCodesAndNonBlankBoundedContent() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (List<Map<String, String>> options :
                List.of(
                        List.of(Map.of("code", "A", "text", "仅一项")),
                        List.of(Map.of("code", "A", "text", "甲"), Map.of("code", "A", "text", "乙")),
                        List.of(
                                Map.of("code", "bad code", "text", "甲"),
                                Map.of("code", "B", "text", "乙")))) {
            List<Map<String, Object>> questions = questions();
            questions.get(0).put("options", options);
            assertInvalidQuestions(row, questions);
        }
        List<Map<String, Object>> blank = questions();
        blank.get(0)
                .put(
                        "options",
                        List.of(
                                Map.of("code", "A", "text", " "),
                                Map.of("code", "B", "text", "乙")));
        assertInvalidQuestions(row, blank, 400);
    }

    @Test
    void answersMustBeUniqueOptionSubsetsAndRespectEachQuestionType() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (List<String> answers :
                List.of(List.<String>of(), List.of("Z"), List.of("A", "A"), List.of("A", "B"))) {
            List<Map<String, Object>> questions = questions();
            questions.get(0).put("correctOptionCodes", answers);
            assertInvalidQuestions(row, questions);
        }
        List<Map<String, Object>> falseQuestion = questions();
        falseQuestion
                .get(2)
                .put(
                        "options",
                        List.of(
                                Map.of("code", "A", "text", "对"),
                                Map.of("code", "B", "text", "错"),
                                Map.of("code", "C", "text", "也许")));
        assertInvalidQuestions(row, falseQuestion);
    }

    @Test
    void publicationRejectsIncompleteDraftAndKeepsEveryOriginalFact() {
        JsonNode row = draft(COMPANY_A, companyA);
        error(
                call("POST", BASE + "/" + id(row) + "/publish", action(row), companyA),
                422,
                "VI_DATA_INVALID");
        assertEquals("DRAFT", field(id(row), "status", String.class));
        assertEquals(0, field(id(row), "version", Long.class));
        assertNull(field(id(row), "published_at", java.sql.Timestamp.class));
    }

    @Test
    void publishedCourseAndQuestionRowsCannotBeEditedOrRepublished() {
        JsonNode published = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        String original = questionFacts(id(published));
        String hash = published.path("contentHash").asText();
        error(
                call("PUT", BASE + "/" + id(published), replacement(published), companyA),
                409,
                "VI_STATE_CONFLICT");
        error(
                call("POST", BASE + "/" + id(published) + "/publish", action(published), companyA),
                409,
                "VI_STATE_CONFLICT");
        assertEquals(original, questionFacts(id(published)));
        assertEquals(hash, detail(id(published), companyA).path("contentHash").asText());
    }

    @Test
    void privateMp4LifecycleVerifiesActualBytesHashMetadataAndAuthorizedDownload()
            throws Exception {
        JsonNode row = draft(COMPANY_A, companyA);
        long file = uploadWithObservedLease(id(row), companyA);
        JsonNode meta = data(call("GET", "/api/common/files/" + file, null, companyA));
        assertEquals("READY", meta.path("status").asText());
        assertTrue(meta.path("durationMillis").asLong() >= 5000);
        assertEquals(
                sha(video),
                jdbc.queryForObject(
                        "SELECT LOWER(HEX(server_sha256)) FROM vi_file WHERE id=?",
                        String.class,
                        file));
        assertArrayEquals(video, download(file, companyA));
        status(call("GET", "/api/common/files/" + file, null, companyB), 404);
        status(
                call(
                        "POST",
                        "/api/common/files/" + file + "/access",
                        Map.of("purpose", "跨公司读取"),
                        companyB),
                404);
        for (boolean reverse : List.of(false, true)) {
            String split =
                    perFunctionScope(
                            Map.of(
                                    "biz:files:query",
                                    reverse ? COMPANY_B : COMPANY_A,
                                    "biz:trainingcourse:query",
                                    reverse ? COMPANY_A : COMPANY_B));
            status(call("GET", "/api/common/files/" + file, null, split), 404);
            String accessSplit =
                    perFunctionScope(
                            Map.of(
                                    "biz:files:access",
                                    reverse ? COMPANY_B : COMPANY_A,
                                    "biz:trainingcourse:query",
                                    reverse ? COMPANY_A : COMPANY_B));
            status(
                    call(
                            "POST",
                            "/api/common/files/" + file + "/access",
                            Map.of("purpose", "逐功能范围交集"),
                            accessSplit),
                    404);
        }
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                            + " action='FILE_MEDIA_DOWNLOAD_STARTED'",
                        file));
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                            + " action='FILE_MEDIA_DOWNLOAD'",
                        file));
        // 首字节之前的开始审计必须成功；不能用不相关outbox故障替代，也不能先送出视频再补审计。
        doThrow(new IllegalStateException("隔离Q02媒体开始审计故障"))
                .when(audits)
                .recordSuccess(
                        any(),
                        eq("FILE"),
                        eq(file),
                        eq("FILE_MEDIA_DOWNLOAD_STARTED"),
                        any(),
                        any(),
                        nullable(String.class));
        JsonNode access =
                data(
                        call(
                                "POST",
                                "/api/common/files/" + file + "/access",
                                Map.of("purpose", "验证开始审计失败不输出原件"),
                                companyA));
        HttpHeaders readHeaders = headers(companyA, null, "admin");
        readHeaders.set(
                "X-File-Ticket", access.path("requiredHeaders").path("X-File-Ticket").asText());
        ResponseEntity<byte[]> failed =
                http.exchange(
                        "http://127.0.0.1:" + port + access.path("downloadUrl").asText(),
                        HttpMethod.GET,
                        new HttpEntity<>(readHeaders),
                        byte[].class);
        assertEquals(500, failed.getStatusCode().value());
        assertFalse(Arrays.equals(video, failed.getBody()));
        assertEquals(
                "application/json",
                Objects.requireNonNull(failed.getHeaders().getContentType()).getType()
                        + "/"
                        + failed.getHeaders().getContentType().getSubtype());
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                            + " action='FILE_MEDIA_DOWNLOAD_STARTED'",
                        file));
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                            + " action='FILE_MEDIA_DOWNLOAD'",
                        file));
    }

    @Test
    void qualificationOrAvatarImagesCannotBeUsedAsTrainingMedia() {
        JsonNode row = draft(COMPANY_A, companyA);
        // 真正上传本人头像 PNG，证明 READY 本身不等于允许当课程视频使用。
        String mini = mini();
        byte[] png =
                Base64.getDecoder()
                        .decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        Map<String, Object> body =
                Map.of(
                        "purpose",
                        "AVATAR",
                        "fileName",
                        "avatar.png",
                        "mimeType",
                        "image/png",
                        "size",
                        png.length);
        JsonNode ticket =
                data(
                        raw(
                                "POST",
                                "/api/common/uploads",
                                body,
                                mini,
                                UUID.randomUUID().toString(),
                                "owner"));
        transfer(ticket, png, mini, "owner");
        status(
                raw(
                        "POST",
                        "/api/common/uploads/" + ticket.path("uploadId").asText() + "/complete",
                        Map.of(),
                        mini,
                        UUID.randomUUID().toString(),
                        "owner"),
                200);
        Map<String, Object> replacement = completeBody(row, ticket.path("fileId").asLong());
        status(call("PUT", BASE + "/" + id(row), replacement, companyA), 404);
        assertTrue(detail(id(row), companyA).path("mediaFileId").isNull());
    }

    @Test
    void mediaFromAnotherCourseOrCompanyCannotBeAttachedByGuessingItsId() {
        JsonNode a = draft(COMPANY_A, companyA);
        JsonNode otherA = draft(COMPANY_A, companyA);
        JsonNode b = draft(COMPANY_B, companyB);
        long otherFile = upload(id(otherA), companyA);
        long foreignFile = upload(id(b), companyB);
        status(call("PUT", BASE + "/" + id(a), completeBody(a, otherFile), companyA), 404);
        status(call("PUT", BASE + "/" + id(a), completeBody(a, foreignFile), companyA), 404);
        assertTrue(detail(id(a), companyA).path("mediaFileId").isNull());
    }

    @Test
    void untransferredOrUncompletedMediaCannotBecomePublishedCourseEvidence() {
        JsonNode row = draft(COMPANY_A, companyA);
        JsonNode ticket = ticket(id(row), companyA, video.length, "video/mp4");
        status(
                call(
                        "PUT",
                        BASE + "/" + id(row),
                        completeBody(row, ticket.path("fileId").asLong()),
                        companyA),
                409);
        status(
                call(
                        "POST",
                        "/api/common/uploads/" + ticket.path("uploadId").asText() + "/complete",
                        Map.of(),
                        companyA),
                409);
        assertEquals(
                "UPLOADING",
                jdbc.queryForObject(
                        "SELECT status FROM vi_file WHERE id=?",
                        String.class,
                        ticket.path("fileId").asLong()));
        transfer(ticket, video, companyA, "admin");
        status(
                call(
                        "PUT",
                        BASE + "/" + id(row),
                        completeBody(row, ticket.path("fileId").asLong()),
                        companyA),
                409);
        assertEquals("DRAFT", field(id(row), "status", String.class));
    }

    @Test
    void fakeOrTruncatedMp4ContentIsRejectedRatherThanTrustedFromMimeOrExtension() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (byte[] invalid :
                List.of("not an mp4".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(video, 64))) {
            JsonNode ticket = ticket(id(row), companyA, invalid.length, "video/mp4");
            transfer(ticket, invalid, companyA, "admin");
            status(
                    call(
                            "POST",
                            "/api/common/uploads/" + ticket.path("uploadId").asText() + "/complete",
                            Map.of(),
                            companyA),
                    422);
            assertEquals(
                    "REJECTED",
                    jdbc.queryForObject(
                            "SELECT status FROM vi_file WHERE id=?",
                            String.class,
                            ticket.path("fileId").asLong()));
        }
    }

    @Test
    void mediaLimitsAndTrainingParentMustBeCheckedBeforeAnyTicketIsIssued() {
        JsonNode row = draft(COMPANY_A, companyA);
        for (long size : List.of(0L, 100L * 1024 * 1024 + 1)) {
            status(
                    call(
                            "POST",
                            "/api/common/uploads",
                            ticketBody(id(row), size, "video/mp4"),
                            companyA),
                    400);
        }
        status(
                call(
                        "POST",
                        "/api/common/uploads",
                        ticketBody(id(row), video.length, "image/png"),
                        companyA),
                400);
        Map<String, Object> body = ticketBody(id(row), video.length, "video/mp4");
        body.put("stationId", "1");
        status(call("POST", "/api/common/uploads", body, companyA), 400);
        String editOnly = scoped(COMPANY_A, "biz:trainingcourse:edit");
        status(
                call(
                        "POST",
                        "/api/common/uploads",
                        ticketBody(id(row), video.length, "video/mp4"),
                        editOnly),
                403);
        String uploadOnly = scoped(COMPANY_A, "biz:uploads:create", "biz:uploads:complete");
        status(
                call(
                        "POST",
                        "/api/common/uploads",
                        ticketBody(id(row), video.length, "video/mp4"),
                        uploadOnly),
                403);
        for (boolean reverse : List.of(false, true)) {
            String split =
                    perFunctionScope(
                            Map.of(
                                    "biz:uploads:create",
                                    reverse ? COMPANY_B : COMPANY_A,
                                    "biz:trainingcourse:edit",
                                    reverse ? COMPANY_A : COMPANY_B));
            status(
                    call(
                            "POST",
                            "/api/common/uploads",
                            ticketBody(id(row), video.length, "video/mp4"),
                            split),
                    404);
        }
    }

    @Test
    void requiredStudyDurationCannotExceedVerifiedVideoDuration() {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        Map<String, Object> body = replacement(row);
        body.put("requiredDurationSeconds", 6);
        JsonNode saved = save(id(row), body, companyA);
        status(call("POST", BASE + "/" + id(row) + "/publish", action(saved), companyA), 422);
        assertEquals("DRAFT", field(id(row), "status", String.class));
    }

    @Test
    void publicationFreezesNewUploadRawUploadAndCompleteButKeepsReadyMediaReadable() {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        JsonNode pending = ticket(id(row), companyA, video.length, "video/mp4");
        JsonNode published = publishTraining(row, companyA);
        status(
                call(
                        "POST",
                        "/api/common/uploads",
                        ticketBody(id(row), video.length, "video/mp4"),
                        companyA),
                409);
        status(transferResponse(pending, video, companyA, "admin"), 409);
        status(
                call(
                        "POST",
                        "/api/common/uploads/" + pending.path("uploadId").asText() + "/complete",
                        Map.of(),
                        companyA),
                409);
        assertArrayEquals(video, download(published.path("mediaFileId").asLong(), companyA));
    }

    @Test
    void derivingNewDraftPreservesSeriesAndCopiesQuestionsWithoutMutatingPublishedVersion() {
        JsonNode published = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        // 真实持久版本超过 Long 缓存区间仍须按数值比较，不能让包装对象引用决定能否派生。
        jdbc.update("UPDATE vi_training_course SET version=128 WHERE id=?", id(published));
        published = detail(id(published), companyA);
        String oldFacts = questionFacts(id(published));
        JsonNode revision = derive(published, companyA);
        assertEquals(published.path("courseKey").asText(), revision.path("courseKey").asText());
        assertEquals(
                published.path("courseVersion").asInt() + 1,
                revision.path("courseVersion").asInt());
        assertEquals(Long.toString(id(published)), revision.path("sourceTrainingId").asText());
        assertEquals("DRAFT", revision.path("status").asText());
        assertEquals(published.path("mediaFileId").asText(), revision.path("mediaFileId").asText());
        assertTrue(revision.path("media").path("inherited").asBoolean());
        Map<String, Object> changed = replacement(revision);
        changed.put("title", "新内容版本");
        List<Map<String, Object>> questions = questions();
        questions.get(0).put("prompt", "修订后的题干");
        changed.put("questions", questions);
        JsonNode v2 = publishTraining(save(id(revision), changed, companyA), companyA);
        assertNotEquals(published.path("contentHash").asText(), v2.path("contentHash").asText());
        assertEquals(oldFacts, questionFacts(id(published)));
        assertEquals(
                published.path("contentHash").asText(),
                detail(id(published), companyA).path("contentHash").asText());
        assertArrayEquals(video, download(v2.path("mediaFileId").asLong(), companyA));
    }

    @Test
    void derivationRejectsDraftSourceForeignCompanyStaleVersionAndForgedOwner() {
        JsonNode draft = draft(COMPANY_A, companyA);
        status(call("POST", BASE, deriveBody(draft), companyA), 409);
        JsonNode published = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        status(call("POST", BASE, deriveBody(published), companyB), 404);
        Map<String, Object> stale = deriveBody(published);
        stale.put("expectedSourceVersion", 0);
        status(call("POST", BASE, stale, companyA), 409);
        Map<String, Object> forged = deriveBody(published);
        forged.put("ownerDeptId", Long.toString(COMPANY_B));
        status(call("POST", BASE, forged, companyA), 400);
        String createOnly = scoped(COMPANY_A, "biz:trainingcourse:create", "biz:files:query");
        status(call("POST", BASE, deriveBody(published), createOnly), 403);
    }

    @Test
    void onlyOneDraftCanExistPerCourseSeriesWithoutReusingOldCourseVersionNumbers() {
        JsonNode v1 = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        JsonNode draft = derive(v1, companyA);
        status(call("POST", BASE, deriveBody(v1), companyA), 409);
        JsonNode v2 = publishTraining(draft, companyA);
        JsonNode next = derive(v2, companyA);
        assertEquals(3, next.path("courseVersion").asInt());
        assertEquals(
                3,
                count(
                        "SELECT COUNT(*) FROM vi_training_course WHERE course_key=?",
                        v1.path("courseKey").asText()));
    }

    @Test
    void oldAttemptStillReferencesItsExactPublishedCourseAndQuestionSnapshot() {
        JsonNode v1 = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        long attempt = historicalAttempt(v1);
        String snapshot =
                jdbc.queryForObject(
                        "SELECT question_snapshot FROM vi_training_attempt WHERE id=?",
                        String.class,
                        attempt);
        String facts = questionFacts(id(v1));
        JsonNode revision = derive(v1, companyA);
        Map<String, Object> body = replacement(revision);
        body.put("title", "V2教材");
        List<Map<String, Object>> questions = questions();
        questions.get(0).put("correctOptionCodes", List.of("B"));
        body.put("questions", questions);
        publishTraining(save(id(revision), body, companyA), companyA);
        assertEquals(
                id(v1),
                jdbc.queryForObject(
                        "SELECT training_id FROM vi_training_attempt WHERE id=?",
                        Long.class,
                        attempt));
        assertEquals(
                snapshot,
                jdbc.queryForObject(
                        "SELECT question_snapshot FROM vi_training_attempt WHERE id=?",
                        String.class,
                        attempt));
        assertEquals(facts, questionFacts(id(v1)));
        assertNull(
                jdbc.queryForObject(
                        "SELECT score FROM vi_training_attempt WHERE id=?",
                        Integer.class,
                        attempt));
    }

    @Test
    void concurrentSameVersionSavesHaveExactlyOneWinnerAndNoMixedQuestionSet() throws Exception {
        JsonNode row = draft(COMPANY_A, companyA);
        Map<String, Object> first = replacement(row);
        first.put("title", "并发甲");
        first.put("questions", questions());
        Map<String, Object> second = replacement(row);
        second.put("title", "并发乙");
        List<ResponseEntity<String>> results =
                race(
                        () -> call("PUT", BASE + "/" + id(row), first, companyA),
                        () -> call("PUT", BASE + "/" + id(row), second, companyA));
        assertOneWinner(results);
        JsonNode saved = detail(id(row), companyA);
        assertEquals(
                saved.path("title").asText().equals("并发甲") ? 3 : 0, saved.path("questions").size());
        assertEquals(1, saved.path("version").asLong());
    }

    @Test
    void concurrentSaveAndPublishFreezeOneConsistentAuthorizedVersion() throws Exception {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        Map<String, Object> update = replacement(row);
        update.put("title", "并发修改的教材");
        List<ResponseEntity<String>> results =
                race(
                        () -> call("PUT", BASE + "/" + id(row), update, companyA),
                        () ->
                                call(
                                        "POST",
                                        BASE + "/" + id(row) + "/publish",
                                        action(row),
                                        companyA));
        assertOneWinner(results);
        JsonNode after = detail(id(row), companyA);
        if (after.path("status").asText().equals("PUBLISHED")) {
            assertEquals(row.path("title").asText(), after.path("title").asText());
        } else {
            assertEquals("并发修改的教材", after.path("title").asText());
        }
        assertEquals(row.path("version").asLong() + 1, after.path("version").asLong());
    }

    @Test
    void concurrentDerivationCreatesExactlyOneDraftForTheSeries() throws Exception {
        JsonNode v1 = publishTraining(completeDraft(COMPANY_A, companyA), companyA);
        List<ResponseEntity<String>> results =
                race(
                        () -> call("POST", BASE, deriveBody(v1), companyA),
                        () -> call("POST", BASE, deriveBody(v1), companyA));
        assertOneWinner(results);
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_training_course WHERE course_key=? AND"
                                + " status='DRAFT'",
                        v1.path("courseKey").asText()));
        assertEquals(
                2,
                count(
                        "SELECT COUNT(*) FROM vi_training_course WHERE course_key=?",
                        v1.path("courseKey").asText()));
    }

    @Test
    void idempotentCreateReplaysOneRecordAndRejectsSameKeyWithAnotherBody() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = createBody(COMPANY_A);
        JsonNode first = data(callKey("POST", BASE, body, companyA, key));
        JsonNode second = data(callKey("POST", BASE, body, companyA, key));
        assertEquals(id(first), id(second));
        assertEquals(1, count("SELECT COUNT(*) FROM vi_training_course"));
        body.put("title", "另一个内容");
        error(callKey("POST", BASE, body, companyA, key), 409, "VI_IDEMPOTENCY_CONFLICT");
    }

    @Test
    void idempotentPublishCreatesOneAuditAndOutboxAndDoesNotLeakTheAnswers() {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = action(row);
        String reason = "Q02发布私密操作原因_仅绑定审计可解密";
        body.put("reason", reason);
        JsonNode first =
                data(callKey("POST", BASE + "/" + id(row) + "/publish", body, companyA, key));
        JsonNode replay =
                data(callKey("POST", BASE + "/" + id(row) + "/publish", body, companyA, key));
        assertEquals(first.path("version").asLong(), replay.path("version").asLong());
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND"
                                + " action='TRAINING_COURSE_PUBLISHED'",
                        id(row)));
        assertEquals(
                1,
                count(
                        "SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=? AND"
                                + " event_type='TRAINING_COURSE_PUBLISHED'",
                        id(row)));
        JsonNode summary =
                ViJson.tree(
                        jdbc.queryForObject(
                                "SELECT after_summary FROM vi_event_log WHERE aggregate_id=? AND"
                                        + " action='TRAINING_COURSE_PUBLISHED'",
                                String.class,
                                id(row)));
        byte[] envelope = Base64.getDecoder().decode(summary.path("reasonEnvelope").asText());
        String aad =
                "Q02:ADMIN:"
                        + adminUser(companyA)
                        + ":TRAINING:"
                        + id(row)
                        + ":TRAINING_COURSE_PUBLISHED";
        assertEquals(reason, auditCrypto.unenvelope(envelope, aad));
        assertThrows(RuntimeException.class, () -> auditCrypto.unenvelope(envelope, aad + "0"));
        assertThrows(
                RuntimeException.class,
                () ->
                        auditCrypto.unenvelope(
                                envelope,
                                "Q02:ADMIN:"
                                        + adminUser(companyA)
                                        + ":TRAINING:"
                                        + id(row)
                                        + ":TRAINING_COURSE_UPDATED"));
        assertFalse(replay.toString().contains(reason));
        assertNoPlainArtifacts(reason);
        assertNoAnswerArtifacts();
    }

    @Test
    void outboxFailureRollsBackCourseQuestionsVersionAuditAndIdempotency() {
        JsonNode row = draft(COMPANY_A, companyA);
        int beforeIdem = count("SELECT COUNT(*) FROM vi_idempotency");
        int beforeEvents = count("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=?", id(row));
        doThrow(new IllegalStateException("隔离Q02事务outbox故障"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), anyString(), any());
        Map<String, Object> body = replacement(row);
        body.put("questions", questions());
        body.put("title", "必须整体回滚");
        status(call("PUT", BASE + "/" + id(row), body, companyA), 500);
        assertEquals(row.path("title").asText(), field(id(row), "title", String.class));
        assertEquals(0, field(id(row), "version", Long.class));
        assertEquals(
                0, count("SELECT COUNT(*) FROM vi_training_question WHERE training_id=?", id(row)));
        assertEquals(beforeIdem, count("SELECT COUNT(*) FROM vi_idempotency"));
        assertEquals(
                beforeEvents,
                count("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=?", id(row)));
    }

    @Test
    void databaseFailureDoesNotLeaveHalfReplacedQuestionBankOrSuccessfulEvents() {
        JsonNode row = draft(COMPANY_A, companyA);
        int idem = count("SELECT COUNT(*) FROM vi_idempotency");
        jdbc.execute(
                "ALTER TABLE vi_training_question ADD CONSTRAINT ck_q02_test_prompt CHECK (prompt"
                        + " <> 'Q02数据库故障')");
        try {
            Map<String, Object> body = replacement(row);
            List<Map<String, Object>> questions = questions();
            questions.get(1).put("prompt", "Q02数据库故障");
            body.put("questions", questions);
            status(call("PUT", BASE + "/" + id(row), body, companyA), 500);
            assertEquals(0, field(id(row), "version", Long.class));
            assertEquals(
                    0,
                    count(
                            "SELECT COUNT(*) FROM vi_training_question WHERE training_id=?",
                            id(row)));
            assertEquals(idem, count("SELECT COUNT(*) FROM vi_idempotency"));
        } finally {
            jdbc.execute("ALTER TABLE vi_training_question DROP CHECK ck_q02_test_prompt");
        }
    }

    @Test
    void revokedMembershipCannotReuseEarlierDetailOrIdempotencyResult() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> create = createBody(COMPANY_A);
        JsonNode row = data(callKey("POST", BASE, create, companyA, key));
        detail(id(row), companyA);
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", adminUser(companyA));
        status(get(id(row), companyA), 404);
        status(callKey("POST", BASE, create, companyA, key), 403);
        status(call("PUT", BASE + "/" + id(row), replacement(row), companyA), 404);
        assertEquals(0, field(id(row), "version", Long.class));
    }

    @Test
    void oldTokenIsRejectedEvenWhenRedisEvictionFailsAndCannotReplaySuccessfulWrite() {
        JsonNode row = draft(COMPANY_A, companyA);
        doThrow(new IllegalStateException("隔离Q02 Redis删除失败")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                adminUser(companyA));
        status(get(id(row), companyA), 401);
        status(call("PUT", BASE + "/" + id(row), replacement(row), companyA), 401);
        assertEquals(0, field(id(row), "version", Long.class));
    }

    @Test
    void authorityRevocationAndPendingPublicationSerializeThroughTheDirectoryGuard()
            throws Exception {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        jdbc.update(
                "INSERT INTO vi_employee_directory_guard(guard_key) VALUES ('GLOBAL_DIRECTORY') ON"
                        + " DUPLICATE KEY UPDATE guard_key=guard_key");
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var revoked =
                    pool.submit(
                            () ->
                                    new TransactionTemplate(transactions)
                                            .execute(
                                                    status -> {
                                                        jdbc.queryForObject(
                                                                "SELECT id FROM"
                                                                    + " vi_employee_directory_guard"
                                                                    + " WHERE"
                                                                    + " guard_key='GLOBAL_DIRECTORY'"
                                                                    + " FOR UPDATE",
                                                                Long.class);
                                                        held.countDown();
                                                        await(release);
                                                        return jdbc.update(
                                                                "UPDATE sys_user SET"
                                                                    + " auth_version=auth_version+1"
                                                                    + " WHERE user_id=?",
                                                                adminUser(companyA));
                                                    }));
            assertTrue(held.await(3, TimeUnit.SECONDS));
            var publish =
                    pool.submit(
                            () ->
                                    call(
                                            "POST",
                                            BASE + "/" + id(row) + "/publish",
                                            action(row),
                                            companyA));
            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> publish.get(150, TimeUnit.MILLISECONDS));
            release.countDown();
            assertEquals(1, revoked.get(5, TimeUnit.SECONDS));
            status(publish.get(5, TimeUnit.SECONDS), 401);
            assertEquals("DRAFT", field(id(row), "status", String.class));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void listAndMutationWithoutQueryPermissionDoNotExposeQuestionAnswers() {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        String listOnly = scoped(COMPANY_A, "biz:trainingcourse:list");
        JsonNode list = tree(call("GET", BASE, null, listOnly));
        assertEquals(1, list.path("total").asInt());
        assertFalse(list.toString().contains("correctOptionCodes"));
        assertFalse(list.path("rows").get(0).has("questions"));
        status(get(id(row), listOnly), 403);
        String editOnly = scoped(COMPANY_A, "biz:trainingcourse:edit", "biz:files:query");
        JsonNode updated = data(call("PUT", BASE + "/" + id(row), replacement(row), editOnly));
        assertFalse(updated.has("questions"));
        assertFalse(updated.toString().contains("correctOptionCodes"));
        assertFalse(updated.toString().contains(SECRET_ANSWER));
    }

    @Test
    void successfulAndDeniedAuditsArePresentWithoutPlainQuestionOrAnswerPayloads() {
        JsonNode row = completeDraft(COMPANY_A, companyA);
        status(get(id(row), companyB), 404);
        assertTrue(
                count(
                                "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND action"
                                    + " IN ('TRAINING_COURSE_CREATED','TRAINING_COURSE_UPDATED')",
                                id(row))
                        >= 2);
        assertTrue(
                count(
                                "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_id=? AND action"
                                        + " LIKE 'TRAINING%_DENIED'",
                                id(row))
                        >= 1);
        assertNoAnswerArtifacts();
    }

    @Test
    void fileTicketsCannotBypassCourseScopeOrRevokedTrainingPermission() {
        JsonNode row = draft(COMPANY_A, companyA);
        JsonNode ticket = ticket(id(row), companyA, video.length, "video/mp4");
        status(transferResponse(ticket, video, companyB, "admin"), 404);
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                adminUser(companyA));
        status(transferResponse(ticket, video, companyA, "admin"), 401);
        assertEquals(
                "UPLOADING",
                jdbc.queryForObject(
                        "SELECT status FROM vi_file WHERE id=?",
                        String.class,
                        ticket.path("fileId").asLong()));
    }

    private JsonNode draft(long company, String token) {
        return data(call("POST", BASE, createBody(company), token));
    }

    private Map<String, Object> createBody(long company) {
        return new LinkedHashMap<>(
                Map.of("ownerDeptId", Long.toString(company), "title", "Q02正式配置测试课程"));
    }

    private JsonNode completeDraft(long company, String token) {
        JsonNode row = draft(company, token);
        return save(id(row), completeBody(row, upload(id(row), token)), token);
    }

    private JsonNode detail(long id, String token) {
        return data(get(id, token));
    }

    private ResponseEntity<String> get(long id, String token) {
        return getPath(Long.toString(id), token);
    }

    private ResponseEntity<String> getPath(String id, String token) {
        return call("GET", BASE + "/" + id, null, token);
    }

    private Map<String, Object> replacement(JsonNode row) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", row.path("title").asText());
        body.put("description", nullableText(row, "description"));
        body.put("mediaFileId", nullableText(row, "mediaFileId"));
        for (String field :
                List.of("requiredDurationSeconds", "passScore", "attemptTimeLimitSeconds")) {
            body.put(
                    field,
                    row.path(field).isNull() || row.path(field).isMissingNode()
                            ? null
                            : row.path(field).asInt());
        }
        body.put("questions", ViJson.read(row.path("questions").toString(), List.class));
        body.put("expectedVersion", row.path("version").asLong());
        body.put("reason", "Q02管理员配置课程与题库");
        return body;
    }

    private Map<String, Object> completeBody(JsonNode row, long file) {
        Map<String, Object> body = replacement(row);
        body.put("description", "用于真实配置与版本验证，非上线培训教材");
        body.put("mediaFileId", Long.toString(file));
        body.put("requiredDurationSeconds", 1);
        body.put("passScore", 80);
        body.put("attemptTimeLimitSeconds", 600);
        body.put("questions", questions());
        return body;
    }

    private List<Map<String, Object>> questions() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String type : List.of("SINGLE", "MULTIPLE", "TRUE_FALSE")) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("questionCode", "Q" + (result.size() + 1));
            question.put("type", type);
            question.put("prompt", "Q02题干：安全交接应如何记录？");
            question.put(
                    "options",
                    List.of(
                            Map.of("code", "A", "text", SECRET_ANSWER),
                            Map.of("code", "B", "text", "另一选项")));
            question.put(
                    "correctOptionCodes",
                    type.equals("MULTIPLE") ? List.of("A", "B") : List.of("A"));
            result.add(question);
        }
        return result;
    }

    private JsonNode save(long id, Map<String, Object> body, String token) {
        return data(call("PUT", BASE + "/" + id, body, token));
    }

    private JsonNode publishTraining(JsonNode row, String token) {
        return data(call("POST", BASE + "/" + id(row) + "/publish", action(row), token));
    }

    private Map<String, Object> deriveBody(JsonNode source) {
        return new LinkedHashMap<>(
                Map.of(
                        "sourceTrainingId",
                        Long.toString(id(source)),
                        "expectedSourceVersion",
                        source.path("version").asLong()));
    }

    private JsonNode derive(JsonNode source, String token) {
        return data(call("POST", BASE, deriveBody(source), token));
    }

    private Map<String, Object> ticketBody(long training, long size, String mime) {
        return new LinkedHashMap<>(
                Map.of(
                        "purpose",
                        "TRAINING_MEDIA",
                        "trainingId",
                        Long.toString(training),
                        "fileName",
                        "flower.mp4",
                        "mimeType",
                        mime,
                        "size",
                        size));
    }

    private JsonNode ticket(long training, String token, long size, String mime) {
        return data(call("POST", "/api/common/uploads", ticketBody(training, size, mime), token));
    }

    private long upload(long training, String token) {
        JsonNode ticket = ticket(training, token, video.length, "video/mp4");
        transfer(ticket, video, token, "admin");
        JsonNode ready =
                data(
                        call(
                                "POST",
                                "/api/common/uploads/"
                                        + ticket.path("uploadId").asText()
                                        + "/complete",
                                Map.of(),
                                token));
        assertEquals("READY", ready.path("status").asText());
        return ticket.path("fileId").asLong();
    }

    private long uploadWithObservedLease(long training, String token) throws Exception {
        JsonNode ticket = ticket(training, token, video.length, "video/mp4");
        long id = ticket.path("fileId").asLong();
        assertMediaExpiry(ticket);
        // 只发送首字节，服务端真实取得上传租约后会阻塞在剩余请求体；此时才能观察租约而非伪造数据库状态。
        try (var socket = new java.net.Socket("127.0.0.1", port)) {
            socket.setSoTimeout(10000);
            String request =
                    "PUT "
                            + ticket.path("uploadUrl").asText()
                            + " HTTP/1.1\r\n"
                            + "Host: 127.0.0.1:"
                            + port
                            + "\r\n"
                            + "Authorization: Bearer "
                            + token
                            + "\r\n"
                            + "X-Client-Type: admin\r\n"
                            + "X-File-Ticket: "
                            + ticket.path("requiredHeaders").path("X-File-Ticket").asText()
                            + "\r\n"
                            + "User-Agent: c04-it\r\nContent-Type: video/mp4\r\n"
                            + "Content-Length: "
                            + video.length
                            + "\r\nConnection: close\r\n\r\n";
            var output = socket.getOutputStream();
            output.write(request.getBytes(StandardCharsets.US_ASCII));
            output.write(video, 0, 1);
            output.flush();
            Integer seconds = null;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (seconds == null && System.nanoTime() < deadline) {
                seconds =
                        jdbc.queryForObject(
                                "SELECT TIMESTAMPDIFF(SECOND,UTC_TIMESTAMP(3),upload_lease_until)"
                                    + " FROM vi_file WHERE id=?",
                                Integer.class,
                                id);
                if (seconds == null) Thread.sleep(10);
            }
            assertNotNull(seconds, "真实HTTP上传应先取得持久化租约");
            assertTrue(seconds >= 294 && seconds <= 300, "媒体租约须与300秒传输时限一致：" + seconds);
            error(
                    call("POST", "/api/common/uploads/" + id + "/complete", Map.of(), token),
                    409,
                    "VI_STATE_CONFLICT");
            output.write(video, 1, video.length - 1);
            output.flush();
            String response =
                    new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
            assertTrue(response.startsWith("HTTP/1.1 204"), response.split("\r\n", 2)[0]);
        }
        JsonNode ready =
                data(call("POST", "/api/common/uploads/" + id + "/complete", Map.of(), token));
        assertEquals("READY", ready.path("status").asText());
        assertNull(
                jdbc.queryForObject(
                        "SELECT upload_lease_until FROM vi_file WHERE id=?",
                        java.sql.Timestamp.class,
                        id));
        return id;
    }

    private void assertMediaExpiry(JsonNode ticket) {
        long remaining =
                java.time.Duration.between(
                                Instant.now(), Instant.parse(ticket.path("expiresAt").asText()))
                        .getSeconds();
        assertTrue(remaining >= 294 && remaining <= 300, "媒体票据须覆盖300秒传输时限：" + remaining);
    }

    private ResponseEntity<String> transferResponse(
            JsonNode ticket, byte[] bytes, String token, String client) {
        HttpHeaders headers = headers(token, null, client);
        headers.set("X-File-Ticket", ticket.path("requiredHeaders").path("X-File-Ticket").asText());
        headers.setContentType(
                MediaType.parseMediaType(
                        ticket.path("requiredHeaders").path("Content-Type").asText()));
        return http.exchange(
                "http://127.0.0.1:" + port + ticket.path("uploadUrl").asText(),
                HttpMethod.PUT,
                new HttpEntity<>(bytes, headers),
                String.class);
    }

    private void transfer(JsonNode ticket, byte[] bytes, String token, String client) {
        status(transferResponse(ticket, bytes, token, client), 204);
    }

    private byte[] download(long file, String token) {
        JsonNode access =
                data(
                        call(
                                "POST",
                                "/api/common/files/" + file + "/access",
                                Map.of("purpose", "核对私有培训教材"),
                                token));
        assertMediaExpiry(access);
        HttpHeaders headers = headers(token, null, "admin");
        headers.set("X-File-Ticket", access.path("requiredHeaders").path("X-File-Ticket").asText());
        ResponseEntity<byte[]> response =
                http.exchange(
                        "http://127.0.0.1:" + port + access.path("downloadUrl").asText(),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        byte[].class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "video/mp4",
                Objects.requireNonNull(response.getHeaders().getContentType()).toString());
        assertTrue(
                Objects.requireNonNull(response.getHeaders().getCacheControl())
                        .contains("no-store"));
        return response.getBody();
    }

    private void assertInvalidQuestions(JsonNode row, List<Map<String, Object>> questions) {
        assertInvalidQuestions(row, questions, 422);
    }

    private void assertInvalidQuestions(
            JsonNode row, List<Map<String, Object>> questions, int status) {
        Map<String, Object> body = replacement(row);
        body.put("questions", questions);
        error(
                call("PUT", BASE + "/" + id(row), body, companyA),
                status,
                status == 400 ? "VI_REQUEST_INVALID" : "VI_DATA_INVALID");
    }

    private String questionFacts(long training) {
        return ViJson.write(
                jdbc.queryForList(
                        "SELECT"
                            + " id,training_id,question_code,question_type,prompt,options_json,correct_option_codes,sort_no,version"
                            + " FROM vi_training_question WHERE training_id=? ORDER BY sort_no,id",
                        training));
    }

    private long historicalAttempt(JsonNode course) {
        String token = mini();
        long account = miniTokens.resolveAccess(token).accountId();
        long driver = SEQ.incrementAndGet();
        long application = SEQ.incrementAndGet();
        long attempt = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO vi_driver_profile(id,account_id,owner_dept_id,qualification_status)"
                        + " VALUES (?,?,?,'DRAFT')",
                driver,
                account,
                COMPANY_A);
        jdbc.update(
                "INSERT INTO vi_driver_application(id,driver_id,owner_dept_id,status) VALUES"
                        + " (?,?,?,'DRAFT')",
                application,
                driver,
                COMPANY_A);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_attempt(id,application_id,driver_id,training_id,attempt_no,status,question_snapshot,started_at,expires_at)"
                    + " VALUES"
                    + " (?,?,?,?,1,'OPEN',?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 10"
                    + " MINUTE))",
                attempt,
                application,
                driver,
                id(course),
                ViJson.write(questions()));
        return attempt;
    }

    private String mini() {
        long id = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO vi_account(id,app_id,open_id,nickname,status) VALUES"
                        + " (?,'wx0000000000000000',?,'Q02测试本人','ACTIVE')",
                id,
                "q02-" + UUID.randomUUID());
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES"
                        + " (?,UTC_TIMESTAMP(3))",
                id);
        ViSession session = new ViSession();
        session.setAccountId(id);
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole("OWNER");
        session.setAbsoluteExpiresAt(Instant.now().plusSeconds(3600));
        sessions.insert(session);
        return miniTokens.issueForSession(session).accessToken();
    }

    private void assertNoAnswerArtifacts() {
        assertNoPlainArtifacts(SECRET_ANSWER);
        assertNoPlainArtifacts("correctOptionCodes");
    }

    private void assertNoPlainArtifacts(String fragment) {
        for (var row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log")) {
            for (Object value : row.values()) {
                assertFalse(Objects.toString(value, "").contains(fragment));
            }
        }
        for (String value : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class)) {
            assertFalse(value.contains(fragment));
        }
        for (String value :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) {
            assertFalse(value.contains(fragment));
        }
    }

    private List<ResponseEntity<String>> race(
            java.util.concurrent.Callable<ResponseEntity<String>> first,
            java.util.concurrent.Callable<ResponseEntity<String>> second)
            throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a =
                    pool.submit(
                            () -> {
                                await(start);
                                return first.call();
                            });
            var b =
                    pool.submit(
                            () -> {
                                await(start);
                                return second.call();
                            });
            start.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private void assertOneWinner(List<ResponseEntity<String>> responses) {
        assertEquals(
                List.of(200, 409),
                responses.stream().map(value -> value.getStatusCode().value()).sorted().toList(),
                responses.toString());
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
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
        // 文件权限与课程权限各自计算真实公司范围，任何一方越权均不能借另一方的角色扩大范围。
        for (var entry : departments.entrySet()) {
            SysRole role = new SysRole();
            role.setRoleId(SEQ.incrementAndGet());
            role.setRoleKey("Q02_TEST");
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
        request.addHeader("User-Agent", "q02-it");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {
            return tokens.createToken(login);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    private HttpHeaders headers(String token, String key, String client) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) headers.setBearerAuth(token);
        if (key != null) headers.set("Idempotency-Key", key);
        if (client != null) headers.set("X-Client-Type", client);
        return headers;
    }

    private ResponseEntity<String> raw(
            String method, String path, Object body, String token, String key, String client) {
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers(token, key, client)),
                String.class);
    }

    private JsonNode tree(ResponseEntity<String> response) {
        status(response, 200);
        return ViJson.tree(response.getBody());
    }

    private void status(ResponseEntity<String> response, int expected) {
        assertEquals(expected, response.getStatusCode().value(), response.getBody());
    }

    private void error(ResponseEntity<String> response, int status, String code) {
        status(response, status);
        assertEquals(code, ViJson.tree(response.getBody()).path("errorCode").asText());
    }

    private int count(String sql, Object... arguments) {
        return jdbc.queryForObject(sql, Integer.class, arguments);
    }

    private <T> T field(long id, String column, Class<T> type) {
        // 列名全部是本测试固定字面量，不接收任何请求参数。
        return jdbc.queryForObject(
                "SELECT " + column + " FROM vi_training_course WHERE id=?", type, id);
    }

    private long id(JsonNode row) {
        return Long.parseLong(row.path("trainingId").asText());
    }

    private String nullableText(JsonNode row, String field) {
        return row.path(field).isNull() || row.path(field).isMissingNode()
                ? null
                : row.path(field).asText();
    }

    private static String sha(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static Path privateRoot() {
        try {
            return Files.createTempDirectory("xinyi-q02-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    private Path repository() {
        Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!Files.exists(root.resolve("docs/vehicle"))) root = root.getParent();
        return root;
    }
}
