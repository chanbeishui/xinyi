package com.xinyi.web.viit;

import com.xinyi.common.core.domain.entity.SysDictData;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.system.service.ISysDictDataService;
import com.xinyi.system.service.ISysDictTypeService;
import com.xinyi.system.service.ISysUserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * E07 测试夹具供给（仅测试 classpath，@Profile("vi-it")，绝不进生产包）。
 * 实现 FixturePort，调用**真实框架 Service**（ISysDictDataService/ISysDictTypeService/ISysUserService），
 * 不 mock、不提供返回固定成功的默认实现。
 *
 * 分步落地（ADR：E07 先 IDENTITY 与守卫）：
 * - IDENTITY：以 admin 上下文经真实字典 Service 幂等确保 TEST_CAR/TEST_INSPECTION 测试字典，
 *   在测试专用 run 登记表 test_fixture_run 上按 (providerRunId,fixtureKind) 幂等复用，返回严格契约响应
 *   （仅 providerRunId/runId/vehicleTypeCode=TEST_CAR/vehiclePlate/privacyVersion=TEST_PRIVACY_V1）。
 * - BOOKING / advanceClock / runDue / applyProviderEvent：对应业务服务（C02/C03/C04 站点·价格·时段、
 *   I01/I03 会话、支付查单、任务到期）尚未实现，**诚实抛 UnsupportedOperationException 说明延后**，
 *   绝不伪造成功、绝不直接造 PAID/RETURNED/CONFIRMED 终态或任意订单目标状态。
 */
public class FixtureProvisioner implements FixturePort {

    private static final long ADMIN_USER_ID = 1L;
    private static final String VEHICLE_TYPE_DICT = "vehicle_type";
    private static final String INSPECTION_TYPE_DICT = "inspection_type";

    private final ISysDictDataService dictDataService;
    private final ISysDictTypeService dictTypeService;
    private final ISysUserService userService;
    private final JdbcTemplate jdbc;

    public FixtureProvisioner(ISysDictDataService dictDataService, ISysDictTypeService dictTypeService,
                              ISysUserService userService, JdbcTemplate jdbc) {
        this.dictDataService = dictDataService;
        this.dictTypeService = dictTypeService;
        this.userService = userService;
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, Object> create(String fixtureKind, String providerRunId) {
        if (fixtureKind == null || providerRunId == null || providerRunId.isBlank()) {
            throw new IllegalArgumentException("fixture/providerRunId 不可为空");
        }
        if ("BOOKING".equals(fixtureKind)) {
            throw new UnsupportedOperationException(
                    "BOOKING 夹具延后至 C02/C03/C04（站点/价格/时段）；E07 只落 IDENTITY 与守卫，不造终态订单");
        }
        if (!"IDENTITY".equals(fixtureKind)) {
            throw new IllegalArgumentException("未知夹具类型: " + fixtureKind);
        }
        return createIdentity(providerRunId);
    }

    private Map<String, Object> createIdentity(String providerRunId) {
        // 幂等：同一 (providerRunId, IDENTITY) 复用既有 run
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT response_json FROM test_fixture_run WHERE provider_run_id=? AND fixture_kind='IDENTITY'",
                providerRunId);
        if (!existing.isEmpty()) {
            Object json = existing.get(0).get("response_json");
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = ViJson.read(String.valueOf(json), Map.class);
            return cached;
        }

        Authentication saved = SecurityContextHolder.getContext().getAuthentication();
        try {
            runAsAdmin(() -> {
                ensureDict(VEHICLE_TYPE_DICT, "TEST_CAR", "测试车型", 90L);
                ensureDict(INSPECTION_TYPE_DICT, "TEST_INSPECTION", "测试检测类型", 90L);
            });
        } finally {
            SecurityContextHolder.getContext().setAuthentication(saved);
        }

        String runId = "e07id-" + UUID.randomUUID();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("providerRunId", providerRunId);
        response.put("runId", runId);
        response.put("vehicleTypeCode", "TEST_CAR");
        response.put("vehiclePlate", "粤KTEST01");
        response.put("privacyVersion", "TEST_PRIVACY_V1");

        try {
            jdbc.update("INSERT INTO test_fixture_run (provider_run_id, fixture_kind, run_id, response_json) " +
                            "VALUES (?, 'IDENTITY', ?, ?)",
                    providerRunId, runId, ViJson.write(response));
        } catch (DuplicateKeyException race) {
            // 并发同 run：回读既有登记，保证幂等
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT response_json FROM test_fixture_run WHERE provider_run_id=? AND fixture_kind='IDENTITY'",
                    providerRunId);
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = ViJson.read(String.valueOf(row.get("response_json")), Map.class);
            return cached;
        }
        return response;
    }

    /** 幂等确保测试字典项存在；经真实框架 ISysDictDataService 写入（create_by/审计照常）。 */
    private void ensureDict(String dictType, String dictValue, String dictLabel, Long dictSort) {
        List<SysDictData> list = dictTypeService.selectDictDataByType(dictType);
        if (list != null) {
            for (SysDictData d : list) {
                if (dictValue.equals(d.getDictValue())) {
                    return; // 已存在，幂等跳过
                }
            }
        }
        SysDictData data = new SysDictData();
        data.setDictType(dictType);
        data.setDictValue(dictValue);
        data.setDictLabel(dictLabel);
        data.setDictSort(dictSort);
        data.setStatus("0");
        data.setIsDefault("N");
        dictDataService.insertDictData(data);
    }

    /** 以框架 admin 身份执行夹具管理（doc：admin 只用于 fixture 管理，不作默认测试身份）。 */
    private void runAsAdmin(Runnable work) {
        SysUser admin = userService.selectUserById(ADMIN_USER_ID);
        if (admin == null) {
            throw new IllegalStateException("隔离库缺少 admin(user_id=1)，无法以框架 Service 建夹具");
        }
        LoginUser loginUser = new LoginUser(admin, Set.of("*:*:*"));
        loginUser.setUserId(ADMIN_USER_ID);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            work.run();
        } finally {
            // 由调用方 finally 恢复原上下文
        }
    }

    @Override
    public Map<String, Object> advanceClock(String runId, long advanceSeconds) {
        throw new UnsupportedOperationException(
                "advanceClock 依赖注入 Clock 与真实任务到期扫描（业务任务落地后接入）；E07 不直接改 order.status");
    }

    @Override
    public Map<String, Object> runDue(String runId, String jobKind) {
        throw new UnsupportedOperationException(
                "runDue 依赖生产 TaskProcessor 到期任务逻辑（业务任务落地后接入）；E07 不做任意 SQL");
    }

    @Override
    public Map<String, Object> applyProviderEvent(String runId, String rawBody, String signature) {
        throw new UnsupportedOperationException(
                "applyProviderEvent 依赖可信事件应用服务与测试 ProviderVerifier（支付/回调任务落地后接入）；" +
                "E07 不把 HMAC 模拟签名当微信 APIv3 验签");
    }
}
