package com.xinyi.web.viit;

import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.domain.OutboxStatus;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.mapper.ViOutboxMapper;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxDispatcher;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.platform.service.ViOutboxRelay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E06 平台基座真实集成测试：真实 MySQL(xinyi) + 真实 MyBatis + 真实 Service，仅外部分发器为测试替身。
 * 覆盖三大交付（同事务 outbox / 有序锁·唯一约束·请求幂等 / 双主体审计）与三条边界
 * （登录刷新不缓存明文 / 重复键不同体 409 / 失败不写成功日志）。
 *
 * 测试数据用 __E06_IT__ / E06_IT: / aggregate_type=E06_IT 标记，@BeforeEach/@AfterEach 经 JdbcTemplate 清理；
 * vi_event_log 业务上 append-only（不提供 update/delete mapper），此处 DELETE 仅为测试卫生，不代表业务可删。
 * 后台调度经 vi.outbox-relay-enabled=false 关闭，中继由测试显式 runOnce() 驱动，避免后台竞态。
 */
@SpringBootTest(classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "vi.outbox-relay-enabled=false",
                "vi.lookup-key=E06-IT-lookup-key-0123456789abcdef",
                "vi.idempotency-ttl-hours=24",
                "vi.outbox-max-attempts=3",
                "vi.outbox-lease-seconds=30"
        })
@ActiveProfiles("druid")
@Import(E06PlatformIT.TestDispatchers.class)
class E06PlatformIT {

    private static final String SUBJ = "__E06_IT__MINI:9001";
    private static final String ACT = "POST /api/__e06_it/orders";

    @Autowired IdempotencyService idempotency;
    @Autowired OutboxService outbox;
    @Autowired AuditService audit;
    @Autowired ViOutboxRelay relay;
    @Autowired ViOutboxMapper outboxMapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager txManager;
    @Autowired RecordingDispatcher dispatcher;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        jdbc.update("DELETE FROM vi_idempotency WHERE subject_key LIKE '__E06_IT__%'");
        jdbc.update("DELETE FROM vi_outbox WHERE event_key LIKE 'E06_IT:%'");
        jdbc.update("DELETE FROM vi_event_log WHERE aggregate_type = 'E06_IT'");
        dispatcher.invoked.clear();
    }

    // ---------- 请求幂等 ----------

    @Test
    void 同键同体重放_业务不重复执行() {
        AtomicInteger runs = new AtomicInteger();
        String body = "{\"amountFen\":6500}";

        String r1 = tx.execute(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-1", body, true, String.class,
                () -> { runs.incrementAndGet(); return "RESULT-1"; }));
        assertEquals("RESULT-1", r1);
        assertEquals(1, runs.get());

        Integer done = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_idempotency WHERE subject_key=? AND request_key='E06-KEY-1' AND status='DONE'",
                Integer.class, SUBJ);
        assertEquals(1, done);

        String r2 = tx.execute(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-1", body, true, String.class,
                () -> { runs.incrementAndGet(); return "RESULT-2"; }));
        assertEquals("RESULT-1", r2);          // 重放缓存结果
        assertEquals(1, runs.get());           // 业务未再次执行
    }

    @Test
    void 重复键不同体_409幂等冲突() {
        tx.execute(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-2", "{\"a\":1}", true, String.class, () -> "OK"));

        ViException e = assertThrows(ViException.class, () ->
                tx.execute(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-2", "{\"a\":2}", true, String.class, () -> "OK2")));
        assertEquals(ViErrorCode.VI_IDEMPOTENCY_CONFLICT, e.getErrorCode());
        assertEquals(409, e.getHttpStatus());
    }

    @Test
    void 业务失败_幂等占位同事务回滚() {
        assertThrows(RuntimeException.class, () ->
                tx.executeWithoutResult(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-FAIL", "{\"a\":1}", true, String.class,
                        () -> { throw new RuntimeException("boom"); })));

        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_idempotency WHERE subject_key=? AND request_key='E06-KEY-FAIL'",
                Integer.class, SUBJ);
        assertEquals(0, cnt, "失败请求不得留下幂等成功痕迹");
    }

    @Test
    void 含明文令牌结果_拒绝缓存() {
        ViException e = assertThrows(ViException.class, () ->
                tx.execute(s -> idempotency.execute(SUBJ, ACT, "E06-KEY-SECRET", "{\"a\":1}", true, Map.class,
                        () -> Map.of("token", "PLAINTEXT-MUST-NOT-PERSIST"))));
        assertEquals(ViErrorCode.VI_DATA_INVALID, e.getErrorCode());

        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_idempotency WHERE subject_key=? AND request_key='E06-KEY-SECRET'",
                Integer.class, SUBJ);
        assertEquals(0, cnt, "拒绝后不得落库明文");
    }

    // ---------- 同事务 outbox ----------

    @Test
    void outbox与业务同事务_提交可见回滚消失() {
        tx.executeWithoutResult(s -> outbox.publish("E06_IT:evt-ok", "E06_IT", 1L, "E06_TEST", Map.of("k", "v")));
        assertEquals(OutboxStatus.READY, outboxMapper.selectByEventKey("E06_IT:evt-ok").getStatus());

        assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(s -> {
            outbox.publish("E06_IT:evt-rb", "E06_IT", 2L, "E06_TEST", Map.of("k", "v"));
            throw new RuntimeException("boom");
        }));
        assertNull(outboxMapper.selectByEventKey("E06_IT:evt-rb"), "业务回滚则事件不得发出");
    }

    @Test
    void outbox唯一事件键防重() {
        boolean first = tx.execute(s -> outbox.publish("E06_IT:evt-dup", "E06_IT", 3L, "E06_TEST", Map.of()));
        boolean second = tx.execute(s -> outbox.publish("E06_IT:evt-dup", "E06_IT", 3L, "E06_TEST", Map.of()));
        assertTrue(first);
        assertFalse(second);
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE event_key='E06_IT:evt-dup'", Integer.class);
        assertEquals(1, cnt);
    }

    @Test
    void 中继领取分发并置DONE() {
        tx.executeWithoutResult(s -> outbox.publish("E06_IT:evt-done", "E06_IT", 10L, "E06_TEST", Map.of("n", 1)));
        relay.runOnce();
        assertTrue(dispatcher.invoked.contains("E06_IT:evt-done"), "分发器应被调用");
        ViOutbox row = outboxMapper.selectByEventKey("E06_IT:evt-done");
        assertEquals(OutboxStatus.DONE, row.getStatus());
        assertEquals(0, row.getAttempts());
    }

    @Test
    void 中继分发失败_退避重试不丢事件() {
        tx.executeWithoutResult(s -> outbox.publish("E06_IT:evt-fail", "E06_IT", 11L, "E06_FAIL", Map.of()));
        relay.runOnce();
        ViOutbox row = outboxMapper.selectByEventKey("E06_IT:evt-fail");
        assertEquals(1, row.getAttempts());
        assertEquals(OutboxStatus.READY, row.getStatus(), "未达上限应回 READY 等待重试");
        assertTrue(row.getNextAttemptAt().isAfter(Instant.now().minusSeconds(2)), "应写入退避时间");
    }

    // ---------- 双主体审计 ----------

    @Test
    void 审计双主体_MINI与ADMIN分列不混比() {
        Actor mini = Actor.mini(9001L, "OWNER", null, 777L, 5L, null);
        Actor admin = Actor.admin(8001L, "ADMIN");
        tx.executeWithoutResult(s -> {
            audit.recordSuccess(mini, "E06_IT", 100L, "TEST_MINI", null, Map.of("after", 1), "r");
            audit.recordSuccess(admin, "E06_IT", 101L, "TEST_ADMIN", null, Map.of("after", 2), "r");
        });

        Map<String, Object> m = jdbc.queryForMap(
                "SELECT actor_realm,actor_account_id,actor_user_id,session_id,context_version,station_id " +
                "FROM vi_event_log WHERE aggregate_type='E06_IT' AND aggregate_id=100");
        assertEquals("MINI", m.get("actor_realm"));
        assertEquals(9001L, ((Number) m.get("actor_account_id")).longValue());
        assertNull(m.get("actor_user_id"));
        assertEquals(777L, ((Number) m.get("session_id")).longValue());

        Map<String, Object> a = jdbc.queryForMap(
                "SELECT actor_realm,actor_account_id,actor_user_id FROM vi_event_log " +
                "WHERE aggregate_type='E06_IT' AND aggregate_id=101");
        assertEquals("ADMIN", a.get("actor_realm"));
        assertEquals(8001L, ((Number) a.get("actor_user_id")).longValue());
        assertNull(a.get("actor_account_id"));
    }

    @Test
    void 成功审计同事务回滚_失败审计独立持久化() {
        Actor admin = Actor.admin(8002L, "ADMIN");
        assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(s -> {
            audit.recordSuccess(admin, "E06_IT", 200L, "TEST_SUCCESS_RB", null, null, "r");
            audit.recordFailure(admin, "E06_IT", 201L, "TEST_FAILURE_INDEP", null, null, "denied");
            throw new RuntimeException("boom");
        }));

        Integer okCnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='E06_IT' AND aggregate_id=200", Integer.class);
        Integer failCnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='E06_IT' AND aggregate_id=201", Integer.class);
        assertEquals(0, okCnt, "主事务回滚则成功流水不留下（失败不写成功日志）");
        assertEquals(1, failCnt, "失败审计 REQUIRES_NEW 独立持久化");
    }

    // ---------- 测试替身分发器（仅此为替身，其余均真实） ----------

    @TestConfiguration
    static class TestDispatchers {
        @Bean
        RecordingDispatcher e06RecordingDispatcher() {
            return new RecordingDispatcher();
        }
    }

    static class RecordingDispatcher implements OutboxDispatcher {
        final Set<String> invoked = ConcurrentHashMap.newKeySet();

        @Override
        public boolean supports(String eventType) {
            return "E06_TEST".equals(eventType) || "E06_FAIL".equals(eventType);
        }

        @Override
        public void dispatch(ViOutbox event) {
            invoked.add(event.getEventKey());
            if ("E06_FAIL".equals(event.getEventType())) {
                throw new RuntimeException("simulated dispatch failure");
            }
        }
    }
}
