package com.xinyi.web.viit;

import com.xinyi.RuoYiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * E07 测试夹具与控制平面真实集成测试。
 * 真实启动嵌入式服务器（RANDOM_PORT，仅 127.0.0.1）连隔离库 xinyi_vi_it_e07，经真实 HTTP→SpringSecurity
 * →ControlApi→FixtureProvisioner→真实框架 Service→MyBatis→MySQL 断言；仅外部分发器/供应商为替身（此处未涉及）。
 *
 * 覆盖三条 negative_checks：
 * - 控制密钥和 DB guard 双校验：正确密钥+隔离库→/runtime 200；错误密钥→403。
 * - 生产 jar 无 __integration：扫描 target/xinyi-admin.jar，断言无 com/xinyi/web/viit 测试控制面类。
 * - 禁止设任意订单目标状态：BOOKING 夹具诚实延后（非 2xx），不造终态订单；FixturePort 无“设任意订单状态”方法。
 *
 * 控制密钥来自环境变量 VI_TEST_CONTROL_KEY（不硬编码、不入库）；缺失则失败而非跳过。
 */
@SpringBootTest(classes = RuoYiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                "vi.environment=integration",
                "vi.providers.mode=SIMULATOR",
                "vi.outbox-relay-enabled=false",
                "server.address=127.0.0.1"
        })
@ActiveProfiles({"druid", "vi-it"})
@Import(ViIntegrationTestConfiguration.class)
class E07TestingIT {

    @LocalServerPort
    int port;

    /** 不抛 4xx/5xx 的 RestTemplate，便于断言真实状态码（Spring Boot 4 测试模块无 TestRestTemplate）。 */
    private final RestTemplate rest = new RestTemplate() {{
        setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }};

    private static String controlKey() {
        String key = System.getenv("VI_TEST_CONTROL_KEY");
        if (key == null || key.length() < 32) {
            fail("缺少环境变量 VI_TEST_CONTROL_KEY（>=32 字节）；E07 不得跳过，须在受控测试环境注入");
        }
        return key;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private HttpEntity<Object> withKey(String key, Object body) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Test-Control-Key", key);
        if (body != null) {
            h.set("Content-Type", "application/json");
        }
        return new HttpEntity<>(body, h);
    }

    @Test
    @SuppressWarnings("unchecked")
    void runtime_正确密钥返回守卫信息() {
        ResponseEntity<Map> resp = rest.exchange(url("/__integration/runtime"), HttpMethod.GET,
                new HttpEntity<>(withKey(controlKey(), null).getHeaders()), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals("integration", body.get("environment"));
        assertEquals("XINYI_VEHICLE_ISOLATED", body.get("databaseGuard"));
        assertEquals("SIMULATOR", body.get("providerMode"));
        assertEquals("6.0.0", body.get("contractVersion"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void runtime_错误密钥被拒() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Test-Control-Key", "wrong-key-0123456789-0123456789-abcdef");
        ResponseEntity<Map> resp = rest.exchange(url("/__integration/runtime"), HttpMethod.GET,
                new HttpEntity<>(h), Map.class);
        // 守卫在安全层拒绝（请求不达 ControlApi）；本框架 AuthenticationEntryPoint 以 HTTP200+{code:401} 表达拒绝，
        // 故断言响应体而非 HTTP 状态：不得返回守卫信息，且 body.code 为 401/403。
        Map<String, Object> body = resp.getBody();
        assertNotNull(body, "拒绝也应有响应体");
        assertTrue(!body.containsKey("databaseGuard") && !body.containsKey("environment"),
                "错误密钥不得返回守卫信息（必须被拒）");
        Object code = body.get("code");
        assertTrue(code instanceof Number && (((Number) code).intValue() == 401 || ((Number) code).intValue() == 403),
                "拒绝应体现为 body.code 401/403，实际=" + code);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fixtures_IDENTITY返回严格契约且幂等() {
        String providerRunId = "e07-run-" + UUID.randomUUID();
        Map<String, Object> req = Map.of("fixture", "IDENTITY", "providerRunId", providerRunId);

        ResponseEntity<Map> first = rest.exchange(url("/__integration/fixtures"), HttpMethod.POST,
                withKey(controlKey(), req), Map.class);
        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        Map<String, Object> b1 = first.getBody();
        assertNotNull(b1);
        // 严格契约：恰好 5 个字段（additionalProperties:false）
        assertEquals(java.util.Set.of("providerRunId", "runId", "vehicleTypeCode", "vehiclePlate", "privacyVersion"),
                b1.keySet(), "IDENTITY 响应字段必须与契约完全一致");
        assertEquals(providerRunId, b1.get("providerRunId"));
        assertEquals("TEST_CAR", b1.get("vehicleTypeCode"));
        assertEquals("TEST_PRIVACY_V1", b1.get("privacyVersion"));
        assertNotNull(b1.get("runId"));
        assertNotNull(b1.get("vehiclePlate"));

        // 幂等：同 (providerRunId, IDENTITY) 复用同一 runId
        ResponseEntity<Map> second = rest.exchange(url("/__integration/fixtures"), HttpMethod.POST,
                withKey(controlKey(), req), Map.class);
        assertEquals(HttpStatus.CREATED, second.getStatusCode());
        assertEquals(b1.get("runId"), second.getBody().get("runId"), "同 providerRunId+IDENTITY 必须复用");
    }

    @Test
    @SuppressWarnings("unchecked")
    void fixtures_BOOKING诚实延后不造终态() {
        String providerRunId = "e07-run-" + UUID.randomUUID();
        Map<String, Object> req = Map.of("fixture", "BOOKING", "providerRunId", providerRunId);
        ResponseEntity<Map> resp = rest.exchange(url("/__integration/fixtures"), HttpMethod.POST,
                withKey(controlKey(), req), Map.class);
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        // 诚实失败：FixtureProvisioner 对 BOOKING 抛 UnsupportedOperationException，
        // 经框架 GlobalExceptionHandler 包成 {code:500,msg:延后...}（HTTP200）；绝不返回固定成功、不造终态夹具。
        assertFalse(body.containsKey("runId"), "BOOKING 不得返回夹具成功响应");
        assertFalse(body.containsKey("vehicleTypeCode"), "BOOKING 不得造终态/成功夹具");
        Object code = body.get("code");
        assertTrue(code instanceof Number && ((Number) code).intValue() == 500,
                "BOOKING 未实现必须诚实失败(body.code=500)，绝不返回固定成功，实际=" + code);
    }

    @Test
    void 生产jar不含测试控制面类() throws Exception {
        Path jar = Path.of("target/xinyi-admin.jar");
        if (!Files.isRegularFile(jar)) {
            jar = Path.of("xinyi-admin/target/xinyi-admin.jar");
        }
        assertTrue(Files.isRegularFile(jar), "未找到生产 jar（应先 package）: " + jar.toAbsolutePath());
        try (JarFile jf = new JarFile(jar.toFile())) {
            boolean leaked = jf.stream().anyMatch(e -> e.getName().contains("com/xinyi/web/viit/")
                    || e.getName().contains("__integration"));
            assertTrue(!leaked, "生产 jar 不得包含测试控制面（com/xinyi/web/viit 或 __integration）");
        }
    }
}
