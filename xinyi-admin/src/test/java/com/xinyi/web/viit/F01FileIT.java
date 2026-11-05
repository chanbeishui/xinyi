package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.service.MiniTokenService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/** 真实 HTTP、双认证域、MySQL、磁盘字节和审计；临时目录只承载本测试生成的图片。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class F01FileIT extends PricingHttpFixture {
    static final Path PRIVATE_ROOT = privateRoot();
    static final String[] FILE_PERMISSIONS = {
        "biz:uploads:create", "biz:uploads:complete", "biz:files:query", "biz:files:access"
    };
    @Autowired MiniTokenService miniTokens;
    @Autowired ViSessionMapper sessions;
    @Autowired com.xinyi.rongli.file.service.FileTickets fileTickets;
    String root;
    byte[] png;

    private static Path privateRoot() {
        try {
            // macOS /var 是符号链接，配置必须使用真实路径，不能误测为目录配置错误。
            return Files.createTempDirectory("xinyi-f01-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void fileProperties(DynamicPropertyRegistry properties) {
        properties.add("vi.files.root", () -> PRIVATE_ROOT.toString());
    }

    @BeforeEach
    void fileFixture() throws Exception {
        // 父夹具已检查数据库名称及保护标记，文件数据只在本隔离库中清理。
        jdbc.execute("DELETE FROM vi_file");
        root = rootToken();
        png = picture("png");
    }

    @AfterAll
    static void cleanupGeneratedBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    byte[] picture(String format) throws Exception {
        var image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x336699);
        var out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, out));
        return out.toByteArray();
    }

    Map<String, Object> avatar(byte[] bytes, String mime) {
        return new LinkedHashMap<>(
                Map.of(
                        "purpose",
                        "AVATAR",
                        "fileName",
                        "测试图片.png",
                        "mimeType",
                        mime,
                        "size",
                        bytes.length));
    }

    JsonNode ticket(byte[] bytes, String mime, String token) {
        return data(call("POST", "/api/common/uploads", avatar(bytes, mime), token));
    }

    String filePath(JsonNode file) {
        return "/api/common/files/" + file.get("fileId").asText();
    }

    ResponseEntity<String> complete(JsonNode file, String token) {
        return call(
                "POST",
                "/api/common/uploads/" + file.get("uploadId").asText() + "/complete",
                null,
                token);
    }

    ResponseEntity<String> transfer(JsonNode ticket, byte[] bytes, String token) {
        return transfer(
                ticket,
                bytes,
                token,
                "admin",
                ticket.get("requiredHeaders").get("X-File-Ticket").asText());
    }

    ResponseEntity<String> transfer(
            JsonNode ticket, byte[] bytes, String token, String client, String signature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", client);
        headers.set("X-File-Ticket", signature);
        headers.setContentType(
                MediaType.parseMediaType(
                        ticket.get("requiredHeaders").get("Content-Type").asText()));
        return http.exchange(
                "http://127.0.0.1:" + port + ticket.get("uploadUrl").asText(),
                HttpMethod.PUT,
                new HttpEntity<>(bytes, headers),
                String.class);
    }

    JsonNode ready(byte[] bytes, String mime, String token) {
        JsonNode file = ticket(bytes, mime, token);
        assertTrue(transfer(file, bytes, token).getStatusCode().is2xxSuccessful());
        assertEquals("READY", data(complete(file, token)).get("status").asText());
        return file;
    }

    JsonNode access(JsonNode file, String token) {
        return data(call("POST", filePath(file) + "/access", Map.of("purpose", "查看本人业务凭证"), token));
    }

    ResponseEntity<byte[]> download(JsonNode grant, String token, String signature) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) headers.setBearerAuth(token);
        headers.set("X-Client-Type", "admin");
        if (signature != null) headers.set("X-File-Ticket", signature);
        return http.exchange(
                "http://127.0.0.1:" + port + grant.get("downloadUrl").asText(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class);
    }

    String signature(JsonNode grant) {
        return grant.get("requiredHeaders").get("X-File-Ticket").asText();
    }

    @Test
    void PNG实际上传核验读取与审计全部接通() throws Exception {
        JsonNode file = ready(png, "image/png", root);
        JsonNode meta = data(call("GET", filePath(file), null, root));
        assertEquals(png.length, meta.get("size").asLong());
        assertEquals(
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(png)),
                meta.get("serverSha256").asText());
        assertFalse(meta.has("objectKey"));
        assertFalse(meta.has("uploadLeaseToken"));
        JsonNode grant = access(file, root);
        assertEquals("APPROVED", grant.get("status").asText());
        assertTrue(
                Instant.parse(grant.get("expiresAt").asText())
                        .isBefore(Instant.now().plusSeconds(61)));
        ResponseEntity<byte[]> response = download(grant, root, signature(grant));
        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(png, response.getBody());
        assertTrue(response.getHeaders().getCacheControl().contains("no-store"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='FILE_DOWNLOAD'",
                        Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='FILE_ACCESS'",
                        Integer.class));
    }

    @Test
    void JPEG按实际内容识别() throws Exception {
        byte[] jpeg = picture("jpg");
        JsonNode file = ready(jpeg, "image/jpeg", root);
        assertEquals(
                "image/jpeg",
                jdbc.queryForObject(
                        "SELECT mime_type FROM vi_file WHERE id=?",
                        String.class,
                        file.get("fileId").asLong()));
    }

    @Test
    void 申请幂等只保存文件编号并拒绝同键不同内容() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = avatar(png, "image/png");
        JsonNode first = data(callKey("POST", "/api/common/uploads", body, root, key));
        JsonNode second = data(callKey("POST", "/api/common/uploads", body, root, key));
        assertEquals(first.get("fileId"), second.get("fileId"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_file", Integer.class));
        body.put("size", png.length + 1);
        assertEquals(
                409,
                callKey("POST", "/api/common/uploads", body, root, key).getStatusCode().value());
        String cached =
                jdbc.queryForObject("SELECT result_json FROM vi_idempotency LIMIT 1", String.class);
        assertFalse(cached.contains("X-File-Ticket"));
        assertFalse(cached.contains("objectKey"));
    }

    @Test
    void 上传声明不能覆盖归属部门路径状态或服务端摘要() {
        for (String field :
                List.of("deptId", "objectKey", "uploaderUserId", "status", "serverSha256")) {
            Map<String, Object> body = avatar(png, "image/png");
            body.put(field, "forged");
            assertEquals(
                    400,
                    call("POST", "/api/common/uploads", body, root).getStatusCode().value(),
                    field);
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_file", Integer.class));
    }

    @Test
    void 大小与类型声明边界在签发前校验() {
        for (Object value : List.of(0, -1, 10485761, "12", 1.5)) {
            var body = avatar(png, "image/png");
            body.put("size", value);
            assertEquals(
                    400,
                    call("POST", "/api/common/uploads", body, root).getStatusCode().value(),
                    value.toString());
        }
        var body = avatar(png, "image/svg+xml");
        assertEquals(400, call("POST", "/api/common/uploads", body, root).getStatusCode().value());
    }

    @Test
    void 用途与唯一父记录必须匹配() {
        var body = avatar(png, "image/png");
        body.put("orderNo", "FORGED");
        assertEquals(400, call("POST", "/api/common/uploads", body, root).getStatusCode().value());
        body.remove("orderNo");
        body.put("purpose", "DISPUTE");
        assertEquals(400, call("POST", "/api/common/uploads", body, root).getStatusCode().value());
        body.put("orderNo", "FORGED");
        assertEquals(404, call("POST", "/api/common/uploads", body, root).getStatusCode().value());
    }

    @Test
    void 未上传不能完成或取得读取许可() {
        JsonNode file = ticket(png, "image/png", root);
        assertEquals(409, complete(file, root).getStatusCode().value());
        assertEquals(
                409,
                call("POST", filePath(file) + "/access", Map.of("purpose", "查看"), root)
                        .getStatusCode()
                        .value());
        assertEquals(
                "UPLOADING", data(call("GET", filePath(file), null, root)).get("status").asText());
    }

    @Test
    void 脚本伪装图片会拒绝且无法读取() {
        byte[] bytes =
                "<script>alert(1)</script>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode file = ticket(bytes, "image/png", root);
        assertTrue(transfer(file, bytes, root).getStatusCode().is2xxSuccessful());
        assertEquals(422, complete(file, root).getStatusCode().value());
        assertEquals(
                "REJECTED", data(call("GET", filePath(file), null, root)).get("status").asText());
        assertEquals(
                409,
                call("POST", filePath(file) + "/access", Map.of("purpose", "查看"), root)
                        .getStatusCode()
                        .value());
    }

    @Test
    void 声明类型与实际图片不匹配会拒绝() {
        JsonNode file = ticket(png, "image/jpeg", root);
        assertTrue(transfer(file, png, root).getStatusCode().is2xxSuccessful());
        assertEquals(422, complete(file, root).getStatusCode().value());
    }

    @Test
    void 实际字节大小错误不能得到READY文件() {
        JsonNode file = ticket(png, "image/png", root);
        assertEquals(
                422,
                transfer(file, Arrays.copyOf(png, png.length - 1), root).getStatusCode().value());
        assertEquals(
                "REJECTED", data(call("GET", filePath(file), null, root)).get("status").asText());
    }

    @Test
    void 完成操作重复和并发都不改变已核验内容() throws Exception {
        JsonNode file = ticket(png, "image/png", root);
        assertTrue(transfer(file, png, root).getStatusCode().is2xxSuccessful());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var start = new CountDownLatch(1);
            Callable<Integer> complete =
                    () -> {
                        start.await();
                        return complete(file, root).getStatusCode().value();
                    };
            Future<Integer> first = pool.submit(complete), second = pool.submit(complete);
            start.countDown();
            assertEquals(200, first.get(20, TimeUnit.SECONDS));
            assertEquals(200, second.get(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        JsonNode initial = data(complete(file, root));
        JsonNode repeated = data(complete(file, root));
        assertEquals(initial, repeated);
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='FILE_COMPLETE'",
                        Integer.class));
    }

    @Test
    void 上传票据不可重用覆盖文件() {
        JsonNode file = ready(png, "image/png", root);
        assertEquals(409, transfer(file, png, root).getStatusCode().value());
        assertEquals("READY", data(call("GET", filePath(file), null, root)).get("status").asText());
    }

    @Test
    void 并发上传仅一个租约成功() throws Exception {
        JsonNode file = ticket(png, "image/png", root);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var start = new CountDownLatch(1);
            Callable<Integer> upload =
                    () -> {
                        start.await();
                        return transfer(file, png, root).getStatusCode().value();
                    };
            Future<Integer> first = pool.submit(upload), second = pool.submit(upload);
            start.countDown();
            assertEquals(
                    Set.of(204, 409),
                    Set.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)));
        } finally {
            pool.shutdownNow();
        }
        assertEquals("READY", data(complete(file, root)).get("status").asText());
    }

    @Test
    void 上传签名不能伪造或跨文件使用() {
        JsonNode first = ticket(png, "image/png", root), second = ticket(png, "image/png", root);
        assertEquals(403, transfer(first, png, root, "admin", "0.forged").getStatusCode().value());
        assertEquals(
                403,
                transfer(second, png, root, "admin", signature(first)).getStatusCode().value());
    }

    @Test
    void 数据库过期上传授权必须拒绝() {
        JsonNode file = ticket(png, "image/png", root);
        jdbc.update(
                "UPDATE vi_file SET upload_expires_at=DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 SECOND)"
                        + " WHERE id=?",
                file.get("fileId").asLong());
        assertEquals(409, transfer(file, png, root).getStatusCode().value());
    }

    @Test
    void 读取必须同时具备登录与正确短期票据() {
        JsonNode file = ready(png, "image/png", root);
        JsonNode grant = access(file, root);
        assertNotEquals(200, download(grant, null, signature(grant)).getStatusCode().value());
        assertEquals(403, download(grant, root, "0.forged").getStatusCode().value());
        assertEquals(403, download(grant, root, null).getStatusCode().value());
        assertFalse(grant.get("downloadUrl").asText().contains("?"));
    }

    @Test
    void 非上传者不能猜测头像编号查看或完成() {
        String other = scoped(COMPANY_A, FILE_PERMISSIONS);
        JsonNode file = ready(png, "image/png", root);
        assertEquals(404, call("GET", filePath(file), null, other).getStatusCode().value());
        assertEquals(404, complete(file, other).getStatusCode().value());
        assertEquals(
                404,
                call("POST", filePath(file) + "/access", Map.of("purpose", "查看"), other)
                        .getStatusCode()
                        .value());
        assertTrue(
                jdbc.queryForObject(
                                "SELECT COUNT(*) FROM vi_event_log WHERE action LIKE"
                                        + " 'FILE%_DENIED'",
                                Integer.class)
                        >= 3);
    }

    @Test
    void 后台功能权限与站点部门权限取交集() {
        long station = station(COMPANY_A);
        var body = avatar(png, "image/png");
        body.put("purpose", "STATION_QUALIFICATION");
        body.put("stationId", Long.toString(station));
        String allowed = scoped(COMPANY_A, FILE_PERMISSIONS);
        String wrongDepartment = scoped(COMPANY_B, FILE_PERMISSIONS);
        String wrongPermission = scoped(COMPANY_A, "biz:files:query");
        assertEquals(
                404,
                call("POST", "/api/common/uploads", body, wrongDepartment).getStatusCode().value());
        assertEquals(
                403,
                call("POST", "/api/common/uploads", body, wrongPermission).getStatusCode().value());
        JsonNode file = data(call("POST", "/api/common/uploads", body, allowed));
        assertTrue(transfer(file, png, allowed).getStatusCode().is2xxSuccessful());
        assertEquals(200, complete(file, allowed).getStatusCode().value());
        assertEquals(
                404, call("GET", filePath(file), null, wrongDepartment).getStatusCode().value());
        assertEquals(200, call("GET", filePath(file), null, allowed).getStatusCode().value());
    }

    @Test
    void 签发后后台撤权旧令牌不能读取() {
        String allowed = scoped(COMPANY_A, FILE_PERMISSIONS);
        JsonNode file = ready(png, "image/png", allowed);
        JsonNode grant = access(file, allowed);
        Long userId =
                jdbc.queryForObject(
                        "SELECT uploader_user_id FROM vi_file WHERE id=?",
                        Long.class,
                        file.get("fileId").asLong());
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?", userId);
        assertNotEquals(200, download(grant, allowed, signature(grant)).getStatusCode().value());
    }

    @Test
    void 小程序真实会话可上传本人头像但不能借客户端标记进入后台域() {
        String mini = mini();
        JsonNode file =
                data(miniCall("POST", "/api/common/uploads", avatar(png, "image/png"), mini));
        assertTrue(
                transfer(file, png, mini, "owner", signature(file))
                        .getStatusCode()
                        .is2xxSuccessful());
        assertEquals(
                "READY",
                data(miniCall(
                                "POST",
                                "/api/common/uploads/"
                                        + file.get("uploadId").asText()
                                        + "/complete",
                                null,
                                mini))
                        .get("status")
                        .asText());
        assertNotEquals(200, call("GET", filePath(file), null, mini).getStatusCode().value());
        assertNotEquals(200, miniCall("GET", filePath(file), null, root).getStatusCode().value());
    }

    @Test
    void 小程序他人头像与撤销会话不可访问() {
        String mini = mini(), other = mini();
        JsonNode file =
                data(miniCall("POST", "/api/common/uploads", avatar(png, "image/png"), mini));
        assertEquals(404, miniCall("GET", filePath(file), null, other).getStatusCode().value());
        long account = miniTokens.resolveAccess(mini).accountId();
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED', revoked_at=UTC_TIMESTAMP(3) WHERE"
                        + " account_id=?",
                account);
        assertEquals(401, miniCall("GET", filePath(file), null, mini).getStatusCode().value());
    }

    @Test
    void 敏感读取请求不能伪造内部权限字段() {
        JsonNode file = ready(png, "image/png", root);
        assertEquals(
                400,
                call(
                                "POST",
                                filePath(file) + "/access",
                                Map.of("purpose", "查看", "deptId", Long.toString(COMPANY_A)),
                                root)
                        .getStatusCode()
                        .value());
        assertEquals(
                400,
                call(
                                "POST",
                                filePath(file) + "/access",
                                Map.of("purpose", "查看", "fields", List.of("objectKey")),
                                root)
                        .getStatusCode()
                        .value());
    }

    @Test
    void 回执丢失后租约到期可核验已有完整对象() {
        JsonNode file = ticket(png, "image/png", root);
        assertTrue(transfer(file, png, root).getStatusCode().is2xxSuccessful());
        // 仅重现本测试文件在上传回执事务提交前进程退出的持久化状态。
        jdbc.update(
                "UPDATE vi_file SET"
                    + " status='UPLOADING',upload_lease_token=?,upload_lease_until=DATE_SUB(UTC_TIMESTAMP(3),"
                    + " INTERVAL 1 SECOND) WHERE id=?",
                UUID.randomUUID().toString(),
                file.get("fileId").asLong());
        assertEquals("READY", data(complete(file, root)).get("status").asText());
        assertEquals(409, transfer(file, png, root).getStatusCode().value());
    }

    @Test
    void 文件路径出现符号链接时拒绝读取() throws Exception {
        JsonNode file = ready(png, "image/png", root);
        JsonNode grant = access(file, root);
        String key =
                jdbc.queryForObject(
                        "SELECT object_key FROM vi_file WHERE id=?",
                        String.class,
                        file.get("fileId").asLong());
        Path original = PRIVATE_ROOT.resolve(key);
        Path alternate = PRIVATE_ROOT.resolve("test-generated-alternate-" + UUID.randomUUID());
        Files.write(alternate, png, StandardOpenOption.CREATE_NEW);
        Files.delete(original);
        Files.createSymbolicLink(original, alternate);
        assertEquals(422, download(grant, root, signature(grant)).getStatusCode().value());
    }

    @Test
    void 非法编号与不存在编号返回稳定拒绝() {
        assertEquals(400, call("GET", "/api/common/files/0", null, root).getStatusCode().value());
        assertEquals(
                400,
                call("GET", "/api/common/files/9223372036854775808", null, root)
                        .getStatusCode()
                        .value());
        assertEquals(
                404,
                call("GET", "/api/common/files/9223372036854775807", null, root)
                        .getStatusCode()
                        .value());
    }

    @Test
    void READY对象字节损坏后拒绝按原图片发送() throws Exception {
        JsonNode file = ready(png, "image/png", root);
        JsonNode grant = access(file, root);
        String key =
                jdbc.queryForObject(
                        "SELECT object_key FROM vi_file WHERE id=?",
                        String.class,
                        file.get("fileId").asLong());
        byte[] corrupted = png.clone();
        corrupted[corrupted.length - 1] ^= 1;
        Files.write(PRIVATE_ROOT.resolve(key), corrupted, StandardOpenOption.TRUNCATE_EXISTING);
        assertEquals(422, download(grant, root, signature(grant)).getStatusCode().value());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='FILE_DOWNLOAD'",
                        Integer.class));
    }

    @Test
    void POSIX私有目录与新文件仅属主可读写() throws Exception {
        JsonNode file = ready(png, "image/png", root);
        String key =
                jdbc.queryForObject(
                        "SELECT object_key FROM vi_file WHERE id=?",
                        String.class,
                        file.get("fileId").asLong());
        if (Files.getFileStore(PRIVATE_ROOT).supportsFileAttributeView("posix")) {
            Path created =
                    new com.xinyi.rongli.file.service.PrivateFileStorage(
                                    PRIVATE_ROOT.resolve("deployment-root").toString())
                            .root();
            assertEquals(
                    java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"),
                    Files.getPosixFilePermissions(created));
            assertEquals(
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"),
                    Files.getPosixFilePermissions(PRIVATE_ROOT.resolve(key)));
        }
    }

    @Test
    void 员工可维护无订单站点资质且撤销站点授权立即失效() {
        long station = station(COMPANY_A);
        String mini = mini();
        var principal = miniTokens.resolveAccess(mini);
        String staffName = "c04_f01_staff_" + SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password)"
                        + " VALUES (?,?,'0','0',0,'x')",
                staffName,
                "F01员工夹具");
        long employee =
                jdbc.queryForObject(
                        "SELECT user_id FROM sys_user WHERE user_name=?", Long.class, staffName);
        long role = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag)"
                    + " VALUES (?,?,'F01_FIXTURE',0,'3','0','0')",
                role,
                "F01文件权限夹具");
        try {
            jdbc.update(
                    "INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,?)", employee, COMPANY_A);
            jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES (?,?)", employee, role);
            for (String permission : FILE_PERMISSIONS) {
                Long menu =
                        jdbc.queryForObject(
                                "SELECT menu_id FROM sys_menu WHERE perms=? LIMIT 1",
                                Long.class,
                                permission);
                jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES (?,?)", role, menu);
            }
            jdbc.update(
                    "INSERT INTO"
                        + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,phone_lookup,last_phone_verified_at,employee_auth_version)"
                        + " VALUES"
                        + " (?,?,UTC_TIMESTAMP(3),'wx0000000000000000',?,UTC_TIMESTAMP(3),0)",
                    principal.accountId(),
                    employee,
                    new byte[32]);
            jdbc.update(
                    "INSERT INTO vi_station_grant(sys_user_id,station_id,enabled,granted_by,reason)"
                            + " VALUES (?,?,1,1,'隔离文件测试授权')",
                    employee,
                    station);
            // 夹具直接建立与已授权员工相符的真实会话；业务请求仍经 MINI 认证链与实时权限查询。
            jdbc.update(
                    "UPDATE vi_session SET active_role='STATION',station_id=?,linked_sys_user_id=?"
                            + " WHERE id=?",
                    station,
                    employee,
                    principal.sessionId());
            var body = avatar(png, "image/png");
            body.put("purpose", "STATION_QUALIFICATION");
            body.put("stationId", Long.toString(station));
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_order WHERE station_id=?",
                            Integer.class,
                            station));
            JsonNode file = data(stationCall("POST", "/api/common/uploads", body, mini));
            assertTrue(
                    transfer(file, png, mini, "station", signature(file))
                            .getStatusCode()
                            .is2xxSuccessful());
            assertEquals(
                    "READY",
                    data(stationCall(
                                    "POST",
                                    "/api/common/uploads/"
                                            + file.get("uploadId").asText()
                                            + "/complete",
                                    null,
                                    mini))
                            .get("status")
                            .asText());
            JsonNode grant =
                    data(
                            stationCall(
                                    "POST",
                                    filePath(file) + "/access",
                                    Map.of("purpose", "核实站点资质"),
                                    mini));
            jdbc.update(
                    "UPDATE vi_station_grant SET enabled=0 WHERE sys_user_id=? AND station_id=?",
                    employee,
                    station);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(mini);
            headers.set("X-Client-Type", "station");
            headers.set("X-File-Ticket", signature(grant));
            assertEquals(
                    404,
                    http.exchange(
                                    "http://127.0.0.1:" + port + grant.get("downloadUrl").asText(),
                                    HttpMethod.GET,
                                    new HttpEntity<>(headers),
                                    String.class)
                            .getStatusCode()
                            .value());
        } finally {
            jdbc.update("DELETE FROM sys_role_menu WHERE role_id=?", role);
            jdbc.update("DELETE FROM sys_user_role WHERE role_id=?", role);
            jdbc.update("DELETE FROM sys_role WHERE role_id=?", role);
            jdbc.update("DELETE FROM vi_station_grant WHERE sys_user_id=?", employee);
            jdbc.update(
                    "DELETE FROM vi_employee_binding WHERE account_id=?", principal.accountId());
        }
    }

    ResponseEntity<String> stationCall(String method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", "station");
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers),
                String.class);
    }

    @Test
    void 正确签名到期和跨动作票据均不能读取() {
        JsonNode file = ready(png, "image/png", root);
        JsonNode grant = access(file, root);
        long id = file.get("fileId").asLong();
        long user =
                jdbc.queryForObject(
                        "SELECT uploader_user_id FROM vi_file WHERE id=?", Long.class, id);
        var actor = com.xinyi.rongli.platform.actor.Actor.admin(user, "ADMIN");
        // 只控制可信签发的时间/动作，不替换 HTTP 身份校验、HMAC 或数据库权限。
        String expired = fileTickets.sign(actor, id, "READ", Instant.now().minusSeconds(1));
        String wrongAction = fileTickets.sign(actor, id, "UPLOAD", Instant.now().plusSeconds(60));
        assertEquals(403, download(grant, root, expired).getStatusCode().value());
        assertEquals(403, download(grant, root, wrongAction).getStatusCode().value());
    }

    @Test
    void 司机仅可操作当前承接订单解除后原票据失效() {
        String mini = mini();
        var principal = miniTokens.resolveAccess(mini);
        long owner = miniTokens.resolveAccess(mini()).accountId();
        long driver = SEQ.incrementAndGet(), assignment = SEQ.incrementAndGet();
        long station = station(COMPANY_A), order = SEQ.incrementAndGet();
        String no = "F01_DRIVER_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.update(
                "INSERT INTO vi_driver_profile(id,account_id,qualification_status) VALUES"
                        + " (?,?,'APPROVED')",
                driver,
                principal.accountId());
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(id,order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version,active_assignment_id)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,'{}','ACCEPTED','OWNER',?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2"
                    + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3"
                    + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'test',?)",
                order,
                no,
                owner,
                station,
                COMPANY_A,
                SEQ.incrementAndGet(),
                owner,
                assignment);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                assignment,
                order,
                driver,
                SEQ.incrementAndGet());
        jdbc.update("UPDATE vi_session SET active_role='DRIVER' WHERE id=?", principal.sessionId());
        try {
            var body = avatar(png, "image/png");
            body.put("purpose", "VEHICLE_PHOTO");
            body.put("orderNo", no);
            JsonNode file = data(roleCall("driver", "POST", "/api/common/uploads", body, mini));
            assertTrue(
                    transfer(file, png, mini, "driver", signature(file))
                            .getStatusCode()
                            .is2xxSuccessful());
            assertEquals(
                    "READY",
                    data(roleCall(
                                    "driver",
                                    "POST",
                                    "/api/common/uploads/"
                                            + file.get("uploadId").asText()
                                            + "/complete",
                                    null,
                                    mini))
                            .get("status")
                            .asText());
            JsonNode grant =
                    data(
                            roleCall(
                                    "driver",
                                    "POST",
                                    filePath(file) + "/access",
                                    Map.of("purpose", "查看在手任务凭证"),
                                    mini));
            jdbc.update(
                    "UPDATE vi_driver_assignment SET"
                            + " ended_at=UTC_TIMESTAMP(3),end_reason='DRIVER_CANCEL' WHERE id=?",
                    assignment);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(mini);
            headers.set("X-Client-Type", "driver");
            headers.set("X-File-Ticket", signature(grant));
            assertEquals(
                    404,
                    http.exchange(
                                    "http://127.0.0.1:" + port + grant.get("downloadUrl").asText(),
                                    HttpMethod.GET,
                                    new HttpEntity<>(headers),
                                    String.class)
                            .getStatusCode()
                            .value());
            assertEquals(
                    404,
                    roleCall("driver", "POST", "/api/common/uploads", body, mini)
                            .getStatusCode()
                            .value());
        } finally {
            jdbc.update("DELETE FROM vi_driver_assignment WHERE id=?", assignment);
            jdbc.update("DELETE FROM vi_order WHERE id=?", order);
            jdbc.update("DELETE FROM vi_driver_profile WHERE id=?", driver);
        }
    }

    ResponseEntity<String> roleCall(
            String client, String method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", client);
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers),
                String.class);
    }

    String mini() {
        String open = "f01-" + UUID.randomUUID();
        jdbc.update(
                "INSERT INTO vi_account(app_id,open_id,status)"
                        + " VALUES('wx0000000000000000',?,'ACTIVE')",
                open);
        long id =
                jdbc.queryForObject("SELECT id FROM vi_account WHERE open_id=?", Long.class, open);
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

    ResponseEntity<String> miniCall(String method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", "owner");
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers),
                String.class);
    }
}
