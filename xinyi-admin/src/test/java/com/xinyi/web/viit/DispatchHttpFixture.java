package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.*;

/**
 * D01无@Test夹具：Q03/Q04是明确的正式来源消费者边界，Q01提交、Q05批准、行政启用与切身份均走真实API。
 * 不把历史APPROVED或直接写入定位当作本轮D01正向，也不替换业务Service或鉴权。
 */
abstract class DispatchHttpFixture extends QualificationAuditHttpFixture {
    static final String AVAILABILITY = "/api/app/driver/availability";
    static final String LISTENING = "/api/app/driver/listening";
    static final String LOCATION = "/api/app/driver/location";
    static final String PRESENCE = "/api/app/driver/presence";
    static final String CONFIGS = "/api/admin/dispatch/companies/";
    static final String CONFIG_QUERY = "biz:dispatchlistening:query";
    static final String CONFIG_EDIT = "biz:dispatchlistening:edit";
    static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    Submitted qualified;
    Student driver;
    String ownerSession;

    @BeforeEach
    void dispatchBase() {
        // 公司每日时间由各测试明确配置；09:00只是受控业务时钟，不是任何生产运营默认。
        controlledNow.set(LocalDate.now(SHANGHAI).atTime(9, 0).atZone(SHANGHAI).toInstant());
        jdbc.execute("DELETE FROM vi_driver_location");
        jdbc.execute("DELETE FROM vi_driver_presence");
        jdbc.execute("DELETE FROM vi_driver_listening_config");
        jdbc.execute("DELETE FROM vi_penalty");
        qualified = submitted();
        data(audit(qualified, approvalBody(adminDetail(qualified)), rootAdmin));
        ownerSession = qualified.person().token();
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + qualified.person().driverId()
                                + "/enable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(qualified.person()),
                                "reason",
                                "D01隔离测试明确允许新增承接"),
                        rootAdmin));
        driver = switchToDriver(qualified.person());
    }

    Student switchToDriver(Student person) {
        JsonNode current = data(miniCall("GET", "/api/common/session", null, person.token()));
        assertEquals("OWNER", current.path("activeRole").asText());
        JsonNode switched =
                data(
                        miniCall(
                                "POST",
                                "/api/common/session/role",
                                Map.of(
                                        "role",
                                        "DRIVER",
                                        "expectedContextVersion",
                                        current.path("contextVersion").asLong()),
                                person.token()));
        assertEquals("DRIVER", switched.path("activeRole").asText());
        String token = switched.path("token").asText();
        assertFalse(token.isBlank());
        return new Student(token, person.accountId(), person.driverId(), person.applicationId());
    }

    ResponseEntity<String> driverCall(String method, String path, Object body) {
        return driverCall(method, path, body, key());
    }

    ResponseEntity<String> driverCall(String method, String path, Object body, String key) {
        return rawTraining(method, path, body, driver.token(), key, "driver");
    }

    JsonNode availability() {
        return data(driverCall("GET", AVAILABILITY, null));
    }

    String configPath(long company) {
        return CONFIGS + company + "/listening-config";
    }

    Map<String, Object> configBody(long version, String closing) {
        return new LinkedHashMap<>(
                Map.of(
                        "expectedVersion", version,
                        "closingTime", closing,
                        "locationMaxAgeSeconds", 120,
                        "reachabilityMaxAgeSeconds", 90,
                        "maxAccuracyMeter", 100,
                        "locationReportIntervalSeconds", 30,
                        "reason", "D01隔离测试明确公司当日规则"));
    }

    JsonNode configure(long company, String closing) {
        JsonNode old = data(call("GET", configPath(company), null, rootAdmin));
        return data(
                call(
                        "PUT",
                        configPath(company),
                        configBody(old.path("version").asLong(), closing),
                        rootAdmin));
    }

    JsonNode configured() {
        return configure(COMPANY_A, "18:00");
    }

    Map<String, Object> observationTarget(JsonNode current, String versionField) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expectedVersion", current.path(versionField).asLong());
        body.put("expectedCompanyRevision", current.path("companyRevision").asLong());
        body.put("expectedSessionId", current.path("sessionId").asText());
        body.put("expectedContextVersion", current.path("contextVersion").asLong());
        return body;
    }

    Map<String, Object> listeningBody(boolean enabled) {
        JsonNode current = availability();
        Map<String, Object> body = observationTarget(current, "listeningVersion");
        body.put("enabled", enabled);
        body.put("expectedBusinessDate", current.path("businessDate").asText());
        body.put(
                "expectedConfigVersion",
                current.path("configVersion").isNull()
                        ? null
                        : current.path("configVersion").asLong());
        return body;
    }

    JsonNode listening(boolean enabled) {
        return data(driverCall("PATCH", LISTENING, listeningBody(enabled)));
    }

    Map<String, Object> locationBody(Instant captured) {
        Map<String, Object> body = observationTarget(availability(), "locationVersion");
        body.put("lng", 112.123456);
        body.put("lat", 22.234567);
        body.put("accuracyMeter", 30);
        body.put("capturedAt", captured.toString());
        body.put("coordinateSystem", "GCJ02");
        return body;
    }

    JsonNode locate(Instant captured) {
        return data(driverCall("POST", LOCATION, locationBody(captured)));
    }

    Map<String, Object> presenceBody(String state, Instant observed) {
        Map<String, Object> body = observationTarget(availability(), "presenceVersion");
        body.put("clientState", state);
        body.put("observedAt", observed.toString());
        return body;
    }

    JsonNode contact(String state, Instant observed) {
        return data(driverCall("POST", PRESENCE, presenceBody(state, observed)));
    }

    JsonNode readyCandidate() {
        configured();
        listening(true);
        locate(now());
        contact("FOREGROUND", now());
        return availability();
    }

    Instant businessTime(int hour, int minute) {
        return now().atZone(SHANGHAI)
                .toLocalDate()
                .atTime(hour, minute)
                .atZone(SHANGHAI)
                .toInstant();
    }

    int countRows(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    long driverVersion() {
        return profileVersion(driver);
    }

    Instant now() {
        return controlledNow.get();
    }

    Map<String, Object> persistedProfile() {
        return jdbc.queryForMap("SELECT * FROM vi_driver_profile WHERE id=?", driver.driverId());
    }
}
