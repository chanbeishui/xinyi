package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.service.MiniTokenService;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import com.xinyi.rongli.platform.json.ViJson;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import javax.imageio.ImageIO;

/** 仅测试基础夹具，无 @Test：真实 MINI/ADMIN、Q01 申请、Q02 视频上传与发布。 不把服务替换为 mock，也不直接制造已学时长、成绩或 TRAINING 通过记录。 */
abstract class TrainingHttpFixture extends PricingHttpFixture {
    static final String TRAINING = "/api/app/driver/qualification/training";
    static final String COURSES = "/api/admin/training-courses";
    static final String POLICIES = "/api/admin/training-policies";
    static final String QUALIFICATION = "/api/app/driver/qualification";
    static final String CONSENT_VERSION = "q03-isolated-consent";
    static final String CONSENT_TEXT = "隔离培训测试申请同意正文，不是运营培训标准。";
    static final Path PRIVATE_ROOT = privateRoot();

    @Autowired MiniTokenService miniTokens;
    @Autowired ViSessionMapper miniSessions;
    @Autowired PhoneCrypto phoneCrypto;
    @Autowired IntegrationKeyProperties integrationKeys;

    String rootAdmin;
    byte[] mp4;
    byte[] png;

    record Student(String token, long accountId, long driverId, long applicationId) {}

    @DynamicPropertySource
    static void trainingPrivateFiles(DynamicPropertyRegistry properties) {
        properties.add("vi.files.root", () -> PRIVATE_ROOT.toString());
    }

    @BeforeEach
    void trainingBase() throws Exception {
        // 父夹具先验证隔离库名称和保护标记；主执行者必须串行，不与其它清理同库的套件并行。
        for (String table :
                List.of(
                        "vi_training_playback",
                        "vi_training_attempt",
                        "vi_training_progress",
                        "vi_training_run",
                        "vi_training_plan_snapshot",
                        "vi_training_policy_revision",
                        "vi_training_policy",
                        "vi_training_question",
                        "vi_training_course",
                        "vi_file",
                        "vi_driver_application_submission",
                        "vi_face_session",
                        "vi_provider_verification",
                        "vi_driver_occupancy",
                        "vi_driver_assignment",
                        "vi_dispatch_invite",
                        "vi_driver_application",
                        "vi_driver_profile")) {
            jdbc.execute("DELETE FROM " + table);
        }
        byte[] rootKey = new byte[32];
        new java.security.SecureRandom().nextBytes(rootKey);
        integrationKeys.setCurrentVersion("q03-isolated");
        integrationKeys.setVersions(
                new HashMap<>(Map.of("q03-isolated", Base64.getEncoder().encodeToString(rootKey))));
        rootAdmin = rootToken();
        config("vi.biz.qualification.consent.version", CONSENT_VERSION);
        config("vi.biz.qualification.consent.text", CONSENT_TEXT);
        try (var bytes = getClass().getResourceAsStream("/integration/q02/flower.mp4")) {
            assertNotNull(bytes, "真实 MP4 样本必须存在，不能伪造 READY 或元数据替代");
            mp4 = bytes.readAllBytes();
        }
        assertEquals("0cd83d944a6ca7822b4a8306cecc60a36e859b041f6702c6a1ad9ead78924451", sha(mp4));
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, 0x336699);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        png = output.toByteArray();
    }

    @AfterAll
    static void removeTrainingPrivateBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    Student student(long company, boolean completeMaterials) {
        String token = newMini(true);
        long account = miniTokens.resolveAccess(token).accountId();
        JsonNode application =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION,
                                Map.of(
                                        "consentVersion",
                                        CONSENT_VERSION,
                                        "consentTextHash",
                                        sha(CONSENT_TEXT.getBytes(StandardCharsets.UTF_8)),
                                        "acceptedConsent",
                                        true),
                                token));
        Student person =
                new Student(
                        token,
                        account,
                        application.path("driverId").asLong(),
                        application.path("applicationId").asLong());
        assignCompany(person, company);
        if (completeMaterials) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("realName", "培训隔离申请人");
            values.put("idCardNo", "110105199001010010");
            values.put("licenseClass", "C1");
            values.put("firstIssueDate", "2010-01-01");
            values.put("validFrom", "2020-01-01");
            values.put("validTo", "2099-12-31");
            values.put("longTermValid", false);
            for (String slot :
                    List.of(
                            "idCardFrontFileId",
                            "idCardBackFileId",
                            "licenseMainFileId",
                            "licenseSubFileId")) {
                String purpose = slot.startsWith("idCard") ? "ID_CARD" : "DRIVER_LICENSE";
                values.put(
                        slot,
                        Long.toString(
                                uploadPrivate(
                                        Map.of(
                                                "purpose",
                                                purpose,
                                                "applicationId",
                                                Long.toString(person.applicationId())),
                                        png,
                                        "image/png",
                                        "证件.png",
                                        token,
                                        "owner")));
            }
            saveMaterials(person, values);
        }
        return person;
    }

    JsonNode qualification(Student person) {
        return data(miniCall("GET", QUALIFICATION, null, person.token()));
    }

    JsonNode saveMaterials(Student person, Map<String, Object> values) {
        JsonNode row = qualification(person);
        Map<String, Object> body = new LinkedHashMap<>(values);
        body.put("applicationId", Long.toString(person.applicationId()));
        body.put("expectedVersion", row.path("version").asLong());
        return data(miniCall("PUT", QUALIFICATION, body, person.token()));
    }

    JsonNode assignCompany(Student person, long company) {
        return data(
                call(
                        "PATCH",
                        "/api/admin/drivers/" + person.driverId(),
                        Map.of(
                                "expectedVersion",
                                profileVersion(person),
                                "ownerDeptId",
                                Long.toString(company),
                                "reason",
                                "隔离测试明确公司归属"),
                        rootAdmin));
    }

    JsonNode trainingView(Student person) {
        return data(miniCall("GET", TRAINING, null, person.token()));
    }

    JsonNode trainingUnit(Student person, JsonNode course) {
        return data(
                miniCall(
                        "GET",
                        TRAINING + "/" + course.path("trainingId").asText(),
                        null,
                        person.token()));
    }

    JsonNode policy(long company) {
        return data(call("GET", POLICIES + "/" + company, null, rootAdmin));
    }

    Map<String, Object> policyDraftBody(
            long company, List<String> ids, Integer maximum, Integer interval) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expectedVersion", policy(company).path("version").asLong());
        body.put("requiredTrainingIds", ids);
        body.put("maxAttempts", maximum);
        body.put("retryIntervalSeconds", interval);
        body.put("reason", "明确配置隔离公司必修课程与考试政策");
        return body;
    }

    JsonNode publishPolicy(long company, List<JsonNode> courses, int maximum, int interval) {
        List<String> ids =
                courses.stream().map(course -> course.path("trainingId").asText()).toList();
        JsonNode saved =
                data(
                        call(
                                "PUT",
                                POLICIES + "/" + company,
                                policyDraftBody(company, ids, maximum, interval),
                                rootAdmin));
        return data(
                call(
                        "POST",
                        POLICIES + "/" + company + "/publish",
                        policyPublishBody(saved),
                        rootAdmin));
    }

    Map<String, Object> policyPublishBody(JsonNode value) {
        return Map.of(
                "expectedVersion",
                value.path("version").asLong(),
                "draftRevisionId",
                value.path("draftRevision").path("revisionId").asText(),
                "reason",
                "发布隔离必修政策不可变修订");
    }

    Map<String, Object> startBody(Student person, JsonNode course) {
        JsonNode overview = trainingView(person);
        JsonNode unit = trainingUnit(person, course);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("applicationId", Long.toString(person.applicationId()));
        body.put("expectedApplicationVersion", overview.path("version").asLong());
        body.put("expectedCompanyRevision", overview.path("companyRevision").asLong());
        body.put("expectedMaterialRevision", overview.path("materialRevision").asLong());
        body.put("expectedPolicyRevisionId", overview.path("policyRevisionId").asText());
        body.put("planId", optionalText(overview, "planId"));
        body.put("runId", optionalText(overview, "runId"));
        body.put("courseVersion", course.path("courseVersion").asInt());
        body.put("expectedVersion", unit.path("version").asLong());
        return body;
    }

    JsonNode startPlayback(Student person, JsonNode course) {
        return data(
                miniCall(
                        "POST",
                        TRAINING + "/" + course.path("trainingId").asText() + "/playback",
                        startBody(person, course),
                        person.token()));
    }

    Map<String, Object> progressBody(
            Student person,
            JsonNode course,
            JsonNode playback,
            int sequence,
            long positionMillis,
            boolean playing) {
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "runId",
                playback.path("runId").asText(),
                "courseVersion",
                course.path("courseVersion").asInt(),
                "expectedVersion",
                trainingUnit(person, course).path("version").asLong(),
                "playbackSessionId",
                playback.path("playbackSessionId").asText(),
                "sequence",
                sequence,
                "positionMillis",
                positionMillis,
                "playing",
                playing);
    }

    JsonNode heartbeat(
            Student person,
            JsonNode course,
            JsonNode playback,
            int sequence,
            long positionMillis,
            boolean playing) {
        return data(
                miniCall(
                        "POST",
                        TRAINING + "/" + course.path("trainingId").asText() + "/progress",
                        progressBody(person, course, playback, sequence, positionMillis, playing),
                        person.token()));
    }

    JsonNode watchCourse(Student person, JsonNode course) throws Exception {
        JsonNode playback = startPlayback(person, course);
        JsonNode baseline = heartbeat(person, course, playback, 1, 0, true);
        assertEquals(0, baseline.path("verifiedDurationMillis").asLong());
        int required = course.path("requiredDurationSeconds").asInt();
        assertTrue(required >= 1 && required <= 4, "正向夹具以真实短视频和真实墙钟完成，不修改服务器时间或已学时长");
        Thread.sleep(required * 1000L + 150);
        JsonNode watched = heartbeat(person, course, playback, 2, required * 1000L, true);
        assertTrue(watched.path("verifiedDurationMillis").asLong() >= required * 1000L);
        assertEquals("READY_FOR_EXAM", watched.path("status").asText());
        return watched;
    }

    Map<String, Object> attemptBody(Student person, JsonNode course) {
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "runId",
                trainingView(person).path("runId").asText(),
                "courseVersion",
                course.path("courseVersion").asInt(),
                "expectedVersion",
                trainingUnit(person, course).path("version").asLong());
    }

    JsonNode beginAttempt(Student person, JsonNode course) {
        return data(
                miniCall(
                        "POST",
                        TRAINING + "/" + course.path("trainingId").asText() + "/attempts",
                        attemptBody(person, course),
                        person.token()));
    }

    Map<String, Object> answerBody(Student person, JsonNode attempt, int correct) {
        List<Map<String, Object>> answers = new ArrayList<>();
        for (JsonNode question : attempt.path("questions")) {
            answers.add(
                    Map.of(
                            "questionId",
                            question.path("questionId").asText(),
                            "answerCodes",
                            List.of(answers.size() < correct ? "A" : "B")));
        }
        return submissionBody(person, attempt, answers);
    }

    Map<String, Object> submissionBody(
            Student person, JsonNode attempt, List<Map<String, Object>> answers) {
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "runId",
                attempt.path("runId").asText(),
                "attemptId",
                attempt.path("attemptId").asText(),
                "expectedVersion",
                attempt.path("version").asLong(),
                "answers",
                answers);
    }

    JsonNode submitAnswers(Student person, JsonNode attempt, int correct) {
        return data(
                miniCall(
                        "POST",
                        TRAINING + "/submit",
                        answerBody(person, attempt, correct),
                        person.token()));
    }

    JsonNode passCourse(Student person, JsonNode course) throws Exception {
        watchCourse(person, course);
        JsonNode attempt = beginAttempt(person, course);
        JsonNode result = submitAnswers(person, attempt, attempt.path("questions").size());
        assertEquals("PASSED", result.path("status").asText());
        assertEquals(100, result.path("score").asInt());
        return result;
    }

    void trustedOtherVerifications(Student person) {
        // 仅Q04四种外部核验的明确边界事实；TRAINING正向绝不在此插入或设置PASSED。
        Map<String, Object> material =
                jdbc.queryForMap(
                        "SELECT material_revision,material_hash FROM vi_driver_application WHERE"
                                + " id=?",
                        person.applicationId());
        for (String type : List.of("REALNAME", "FACE", "BACKGROUND", "LICENSE")) {
            jdbc.update(
                    "INSERT INTO"
                        + " vi_provider_verification(application_id,material_revision,material_hash,provider,task_type,provider_task_id,status,result_digest,verified_at)"
                        + " VALUES"
                        + " (?,?,?,'Q03_Q04_BOUNDARY_FACT',?,?,'PASSED','explicit-upstream-boundary',UTC_TIMESTAMP(3))",
                    person.applicationId(),
                    material.get("material_revision"),
                    material.get("material_hash"),
                    type,
                    "q03-upstream-" + UUID.randomUUID());
        }
    }

    long profileVersion(Student person) {
        return jdbc.queryForObject(
                "SELECT version FROM vi_driver_profile WHERE id=?", Long.class, person.driverId());
    }

    long companyEpoch(Student person) {
        return jdbc.queryForObject(
                "SELECT training_context_revision FROM vi_driver_profile WHERE id=?",
                Long.class,
                person.driverId());
    }

    JsonNode publishCourse(long company, int questions, int requiredSeconds, int passScore) {
        return publishCourse(company, questionBank(questions), requiredSeconds, passScore);
    }

    JsonNode publishCourse(
            long company, List<Map<String, Object>> questions, int requiredSeconds, int passScore) {
        JsonNode draft =
                data(
                        call(
                                "POST",
                                COURSES,
                                Map.of("ownerDeptId", Long.toString(company), "title", "Q03真实培训课程"),
                                rootAdmin));
        long course = draft.path("trainingId").asLong();
        long media =
                uploadPrivate(
                        Map.of("purpose", "TRAINING_MEDIA", "trainingId", Long.toString(course)),
                        mp4,
                        "video/mp4",
                        "flower.mp4",
                        rootAdmin,
                        "admin");
        JsonNode saved =
                data(
                        call(
                                "PUT",
                                COURSES + "/" + course,
                                courseBody(draft, media, questions, requiredSeconds, passScore),
                                rootAdmin));
        return data(
                call(
                        "POST",
                        COURSES + "/" + course + "/publish",
                        Map.of(
                                "expectedVersion",
                                saved.path("version").asLong(),
                                "reason",
                                "发布隔离课程版本"),
                        rootAdmin));
    }

    Map<String, Object> courseBody(
            JsonNode draft,
            long media,
            List<Map<String, Object>> questions,
            int requiredSeconds,
            int passScore) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", draft.path("title").asText());
        body.put("description", "真实学习与阅卷的测试教材，不是正式运营标准");
        body.put("mediaFileId", Long.toString(media));
        body.put("requiredDurationSeconds", requiredSeconds);
        body.put("passScore", passScore);
        body.put("attemptTimeLimitSeconds", 60);
        body.put("questions", questions);
        body.put("expectedVersion", draft.path("version").asLong());
        body.put("reason", "配置隔离课程与正式格式题库");
        return body;
    }

    List<Map<String, Object>> questionBank(int amount) {
        List<Map<String, Object>> questions = new ArrayList<>();
        for (int index = 1; index <= amount; index++) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("questionCode", "Q" + index);
            question.put("type", "SINGLE");
            question.put("prompt", "隔离题干" + index + "：请核对交接记录");
            question.put(
                    "options",
                    List.of(
                            Map.of("code", "A", "text", "正确交接记录"),
                            Map.of("code", "B", "text", "错误交接记录"),
                            Map.of("code", "C", "text", "其它记录")));
            question.put("correctOptionCodes", List.of("A"));
            questions.add(question);
        }
        return questions;
    }

    JsonNode reviseCourse(JsonNode source, String correctCode) {
        JsonNode draft =
                data(
                        call(
                                "POST",
                                COURSES,
                                Map.of(
                                        "sourceTrainingId",
                                        source.path("trainingId").asText(),
                                        "expectedSourceVersion",
                                        source.path("version").asLong()),
                                rootAdmin));
        List<Map<String, Object>> questions = questionBank(source.path("questionCount").asInt());
        for (Map<String, Object> question : questions)
            question.put("correctOptionCodes", List.of(correctCode));
        JsonNode saved =
                data(
                        call(
                                "PUT",
                                COURSES + "/" + draft.path("trainingId").asText(),
                                courseBody(
                                        draft,
                                        draft.path("mediaFileId").asLong(),
                                        questions,
                                        source.path("requiredDurationSeconds").asInt(),
                                        source.path("passScore").asInt()),
                                rootAdmin));
        return data(
                call(
                        "POST",
                        COURSES + "/" + draft.path("trainingId").asText() + "/publish",
                        Map.of(
                                "expectedVersion",
                                saved.path("version").asLong(),
                                "reason",
                                "发布新的真实题目版本"),
                        rootAdmin));
    }

    long uploadPrivate(
            Map<String, Object> target,
            byte[] bytes,
            String mime,
            String name,
            String token,
            String client) {
        Map<String, Object> body = new LinkedHashMap<>(target);
        body.put("fileName", name);
        body.put("mimeType", mime);
        body.put("size", bytes.length);
        JsonNode ticket =
                data(rawTraining("POST", "/api/common/uploads", body, token, key(), client));
        HttpHeaders headers = trainingHeaders(token, null, client);
        headers.set("X-File-Ticket", ticket.path("requiredHeaders").path("X-File-Ticket").asText());
        headers.setContentType(MediaType.parseMediaType(mime));
        ResponseEntity<String> response =
                http.exchange(
                        "http://127.0.0.1:" + port + ticket.path("uploadUrl").asText(),
                        HttpMethod.PUT,
                        new HttpEntity<>(bytes, headers),
                        String.class);
        requireStatus(response, 204);
        JsonNode file =
                data(
                        rawTraining(
                                "POST",
                                "/api/common/uploads/"
                                        + ticket.path("uploadId").asText()
                                        + "/complete",
                                Map.of(),
                                token,
                                key(),
                                client));
        assertEquals("READY", file.path("status").asText());
        return ticket.path("fileId").asLong();
    }

    String newMini(boolean verified) {
        long suffix = SEQ.incrementAndGet();
        String phone = "139" + String.format("%08d", Math.floorMod(suffix, 100000000));
        String openId = "q03-" + UUID.randomUUID();
        // 账号主键交给数据库，与同库其它真实登录保持同一分配机制，不能手工SEQ撞上AUTO_INCREMENT。
        jdbc.update(
                "INSERT INTO"
                    + " vi_account(app_id,open_id,nickname,status,phone_cipher,phone_lookup,phone_verified_at,phone_source)"
                    + " VALUES ('wx0000000000000000',?,'Q03本人','ACTIVE',?,?,?,?)",
                openId,
                verified ? phoneCrypto.encrypt(phone) : null,
                verified ? phoneCrypto.lookup(phone) : null,
                verified ? java.sql.Timestamp.from(Instant.now()) : null,
                verified ? "WECHAT" : null);
        long account =
                jdbc.queryForObject(
                        "SELECT id FROM vi_account WHERE app_id='wx0000000000000000' AND open_id=?",
                        Long.class,
                        openId);
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES"
                        + " (?,UTC_TIMESTAMP(3))",
                account);
        return anotherSession(account);
    }

    String anotherSession(long account) {
        ViSession session = new ViSession();
        session.setAccountId(account);
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole("OWNER");
        session.setAbsoluteExpiresAt(Instant.now().plusSeconds(3600));
        miniSessions.insert(session);
        return miniTokens.issueForSession(session).accessToken();
    }

    ResponseEntity<String> miniCall(String method, String path, Object body, String token) {
        return rawTraining(method, path, body, token, key(), "owner");
    }

    ResponseEntity<String> miniCallKey(
            String method, String path, Object body, String token, String key) {
        return rawTraining(method, path, body, token, key, "owner");
    }

    ResponseEntity<String> rawTraining(
            String method, String path, Object body, String token, String key, String client) {
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, trainingHeaders(token, key, client)),
                String.class);
    }

    HttpHeaders trainingHeaders(String token, String key, String client) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) headers.setBearerAuth(token);
        if (key != null) headers.set("Idempotency-Key", key);
        if (client != null) headers.set("X-Client-Type", client);
        return headers;
    }

    void requireStatus(ResponseEntity<?> response, int expected) {
        assertEquals(
                expected, response.getStatusCode().value(), Objects.toString(response.getBody()));
    }

    void requireError(ResponseEntity<String> response, int status, String code) {
        requireStatus(response, status);
        assertEquals(code, ViJson.tree(response.getBody()).path("errorCode").asText());
    }

    JsonNode responseTree(ResponseEntity<String> response) {
        requireStatus(response, 200);
        return ViJson.tree(response.getBody());
    }

    int rows(String sql, Object... arguments) {
        return jdbc.queryForObject(sql, Integer.class, arguments);
    }

    private void config(String name, String value) {
        // 框架config_key没有唯一键，ON DUPLICATE KEY不会覆盖同名键；隔离夹具必须明确建立唯一配置事实。
        jdbc.update("DELETE FROM sys_config WHERE config_key=?", name);
        jdbc.update(
                "INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES"
                        + " (?,?,?,'N')",
                name,
                name,
                value);
        assertEquals(1, rows("SELECT COUNT(*) FROM sys_config WHERE config_key=?", name));
    }

    static String key() {
        return UUID.randomUUID().toString();
    }

    String optionalText(JsonNode value, String field) {
        return value.path(field).isNull() || value.path(field).isMissingNode()
                ? null
                : value.path(field).asText();
    }

    static String sha(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static Path privateRoot() {
        try {
            return Files.createTempDirectory("xinyi-q03-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }
}
