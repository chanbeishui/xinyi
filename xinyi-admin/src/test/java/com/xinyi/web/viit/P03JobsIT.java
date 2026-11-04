package com.xinyi.web.viit;

import com.xinyi.rongli.jobs.service.*;
import com.xinyi.rongli.platform.service.ViOutboxRelay;
import com.xinyi.rongli.platform.mapper.ViOutboxMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** 时间推进只替换Clock；HTTP安全链、支付服务、数据库事务、行锁和outbox租约均真实执行。 */
@TestPropertySource(
        properties = {"vi.jobs.dispatch-enabled=true", "vi.jobs.dispatch-delay-ms=3600000"})
class P03JobsIT extends PaymentHttpFixture {
    @MockitoBean(name = "viDeadlineClock")
    Clock clock;

    @Autowired DispatchDeadlineService deadlines;
    @Autowired DispatchDeadlineJob job;
    @Autowired ViOutboxRelay relay;
    @Autowired ViOutboxMapper events;
    Instant pickup;
    Instant due;
    long stationId;

    @BeforeEach
    void scheduledOrder() {
        pickup = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        due = pickup.minusSeconds(1800);
        time(due.minusSeconds(1));
        queryTrade = state("out_trade_no");
        stationId =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        long quoteId =
                jdbc.queryForObject(
                        "SELECT quote_id FROM vi_order WHERE id=?", Long.class, orderId);
        jdbc.update(
                "UPDATE vi_order SET pickup_starts_at=?, dispatch_at=?, dispatch_config_version=7"
                    + " WHERE id=?",
                utc(pickup),
                utc(due),
                orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_quote(id,owner_account_id,station_id,vehicle_snapshot,address_snapshot_cipher,pickup_slot_id,rule_id,route_snapshot,amount_fen,items_json,expires_at)"
                    + " VALUES(?,?,?,'{}',?,1,1,'{}',12345,'{\"policy\":{\"lead\":0}}',DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 3 DAY))",
                quoteId,
                account,
                stationId,
                new byte[] {1});
        for (String type : List.of("PICKUP", "STATION_RECEIVE")) {
            long slotId = SEQ.incrementAndGet();
            jdbc.update(
                    "INSERT INTO"
                        + " vi_quota_slot(id,quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota)"
                        + " VALUES(?,?,?, ?,?,?,5,1)",
                    slotId,
                    type,
                    "station:" + stationId,
                    stationId,
                    utc(pickup),
                    utc(pickup.plusSeconds(3600)));
            jdbc.update(
                    "INSERT INTO vi_quota_reservation(order_id,slot_id,status,quantity,expires_at)"
                        + " SELECT ?,?,'HELD',1,expires_at FROM vi_payment_order WHERE order_id=?",
                    orderId,
                    slotId,
                    orderId);
        }
    }

    // vi DATETIME约定保存UTC；不能让JdbcTemplate按本机Asia/Shanghai转换Timestamp。
    LocalDateTime utc(Instant value) {
        return LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    void time(Instant now) {
        when(clock.instant()).thenReturn(now);
    }

    String fulfillment() {
        return jdbc.queryForObject(
                "SELECT fulfillment_status FROM vi_order WHERE id=?", String.class, orderId);
    }

    int eventCount(String type) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_outbox WHERE aggregate_id=? AND aggregate_type='VI_ORDER'"
                    + " AND event_type=?",
                Integer.class,
                orderId,
                type);
    }

    void paySuccess() {
        data(pay(intent(), UUID.randomUUID().toString()));
        queryState = "SUCCESS";
        assertEquals(
                "SUCCESS",
                data(ownerCall("GET", "/payment", null, owner, null)).path("payStatus").asText());
    }

    void dueScan() {
        time(due);
        job.runOnce();
    }

    @Test
    void beforeDueDoesNotDispatchAtDueExactlyOnce() {
        paySuccess();
        assertEquals("SCHEDULED", fulfillment());
        job.runOnce();
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
        dueScan();
        job.runOnce();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(1, eventCount("DISPATCH_REQUESTED"));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE order_id=?",
                        Integer.class,
                        orderId));
    }

    @Test
    void paymentAfterDueImmediatelyRequestsDispatch() {
        time(due.plusSeconds(600));
        paySuccess();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(1, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void latePaymentWithInsufficientLeadRequiresCoordination() {
        jdbc.update(
                "UPDATE vi_quote SET items_json='{\"policy\":{\"lead\":1500}}' WHERE id=(SELECT"
                    + " quote_id FROM vi_order WHERE id=?)",
                orderId);
        time(due.plusSeconds(600));
        paySuccess();
        assertEquals("MANUAL_DISPATCH", fulfillment());
        assertEquals("SUCCESS", state("status"));
    }

    @Test
    void unpaidOrderHasNoTimerAndCannotDispatch() {
        dueScan();
        assertEquals("PENDING_PAY", fulfillment());
        assertEquals(0, eventCount(DispatchDeadlineService.TIMER));
    }

    @Test
    void cancelledOrderIsNotRevived() {
        paySuccess();
        jdbc.update("UPDATE vi_order SET fulfillment_status='CANCELLED' WHERE id=?", orderId);
        dueScan();
        assertEquals("CANCELLED", fulfillment());
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void expiredPickupRequiresCoordinationWithoutReleasingMoneyOrQuota() {
        paySuccess();
        time(pickup);
        job.runOnce();
        assertEquals("MANUAL_DISPATCH", fulfillment());
        assertEquals("SUCCESS", state("status"));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_quota_reservation WHERE order_id=? AND"
                            + " status='CONFIRMED'",
                        Integer.class,
                        orderId));
    }

    @Test
    void disabledStationCannotAutomaticallyDispatch() {
        paySuccess();
        jdbc.update("UPDATE vi_station SET enabled=0 WHERE id=?", stationId);
        dueScan();
        assertEquals("MANUAL_DISPATCH", fulfillment());
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void refundReservationPreventsAutomaticDispatch() {
        paySuccess();
        jdbc.update("UPDATE vi_payment_order SET refund_reserved_fen=1 WHERE order_id=?", orderId);
        dueScan();
        assertEquals("MANUAL_DISPATCH", fulfillment());
    }

    @Test
    void legacyMissingPlanIsReportedWithoutGuessing() {
        jdbc.update(
                "UPDATE vi_order SET dispatch_at=NULL,dispatch_config_version=NULL WHERE id=?",
                orderId);
        paySuccess();
        job.runOnce();
        job.runOnce();
        assertEquals("SCHEDULED", fulfillment());
        assertEquals(1, eventCount("DISPATCH_PLAN_MISSING"));
        assertEquals(0, eventCount(DispatchDeadlineService.TIMER));
    }

    @Test
    void missingTimerIsReconstructedFromFrozenOrder() {
        paySuccess();
        jdbc.update("DELETE FROM vi_outbox WHERE event_key=?", "DISPATCH_TIMER:" + orderId);
        time(due);
        new DispatchDeadlineJob(deadlines, relay, true).runOnce();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(1, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void expiredWorkerLeaseIsRecoveredAfterWorkerReconstruction() {
        paySuccess();
        jdbc.update(
                "UPDATE vi_outbox SET status='PROCESSING',lease_until=?,version=version+1 WHERE"
                    + " event_key=?",
                utc(due.plusSeconds(30)),
                "DISPATCH_TIMER:" + orderId);
        time(due);
        new DispatchDeadlineJob(deadlines, relay, true).runOnce();
        assertEquals("SCHEDULED", fulfillment());
        time(due.plusSeconds(31));
        new DispatchDeadlineJob(deadlines, relay, true).runOnce();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(1, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void concurrentWorkersAndRepeatedDeliveryDoNotDoubleDispatch() throws Exception {
        paySuccess();
        time(due);
        var event = events.selectByEventKey("DISPATCH_TIMER:" + orderId);
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var first =
                    pool.submit(
                            () -> {
                                start.await();
                                return new DispatchDeadlineJob(deadlines, relay, true).runOnce();
                            });
            var second =
                    pool.submit(
                            () -> {
                                start.await();
                                return new DispatchDeadlineJob(deadlines, relay, true).runOnce();
                            });
            start.countDown();
            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
            deadlines.execute(event);
            deadlines.execute(event);
            assertEquals(1, eventCount("DISPATCH_REQUESTED"));
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_event_log WHERE order_id=? AND"
                                + " action='DISPATCH_REQUESTED'",
                            Integer.class,
                            orderId));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void unknownEventBacklogCannotStarveDeadline() {
        paySuccess();
        for (int i = 0; i < 110; i++)
            jdbc.update(
                    "INSERT INTO"
                        + " vi_outbox(event_key,aggregate_type,aggregate_id,event_type,payload,status,next_attempt_at)"
                        + " VALUES(?,'VI_ORDER',?,'P03_UNHANDLED','{}','READY',?)",
                    UUID.randomUUID().toString(),
                    orderId,
                    utc(due.minusSeconds(10)));
        dueScan();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(110, eventCount("P03_UNHANDLED"));
    }

    @Test
    void stalePlanVersionCannotDispatch() {
        paySuccess();
        jdbc.update("UPDATE vi_order SET dispatch_config_version=8 WHERE id=?", orderId);
        dueScan();
        assertEquals("SCHEDULED", fulfillment());
        assertEquals(1, eventCount("DISPATCH_PLAN_CONFLICT"));
    }

    @Test
    void outboxFailureRollsBackStateAndRetrySucceeds() {
        paySuccess();
        String trigger = "p03_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute(
                "CREATE TRIGGER "
                        + trigger
                        + " BEFORE INSERT ON vi_outbox FOR EACH ROW BEGIN IF"
                        + " NEW.event_type='DISPATCH_REQUESTED' THEN SIGNAL SQLSTATE '45000' SET"
                        + " MESSAGE_TEXT='P03_ROLLBACK'; END IF; END");
        try {
            dueScan();
            assertEquals("SCHEDULED", fulfillment());
            assertEquals(0, eventCount("DISPATCH_REQUESTED"));
        } finally {
            jdbc.execute("DROP TRIGGER " + trigger);
        }
        time(due.plusSeconds(3));
        job.runOnce();
        assertEquals("PENDING_DISPATCH", fulfillment());
        assertEquals(1, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void cancellationReviewAndExistingAssignmentBlockNewDispatch() {
        paySuccess();
        jdbc.update("UPDATE vi_order SET cancel_status='APPLIED' WHERE id=?", orderId);
        dueScan();
        assertEquals("SCHEDULED", fulfillment());
        assertEquals(1, eventCount("DISPATCH_BLOCKED"));
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
        // 即便取消已退出，有效承接也禁止复用旧任务再派司机。
        jdbc.update(
                "UPDATE vi_order SET cancel_status='NONE',active_assignment_id=991234 WHERE id=?",
                orderId);
        deadlines.execute(events.selectByEventKey("DISPATCH_TIMER:" + orderId));
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void wrongAuthoritativeDepartmentAndMissingQuotaRequireCoordination() {
        paySuccess();
        jdbc.update("UPDATE vi_order SET dept_id=100 WHERE id=?", orderId);
        jdbc.update("UPDATE vi_quota_reservation SET status='RELEASED' WHERE order_id=?", orderId);
        dueScan();
        assertEquals("MANUAL_DISPATCH", fulfillment());
        assertEquals(0, eventCount("DISPATCH_REQUESTED"));
    }

    @Test
    void configRejectsInvalidLeadThroughRealAdminHttp() {
        long version = data(call("GET", "/api/admin/config", null, root)).path("version").asLong();
        for (String value : List.of("", "-1", "1.5", "10081", "abc")) {
            var body =
                    java.util.Map.of(
                            "items",
                            List.of(java.util.Map.of("key", DispatchPlan.LEAD_KEY, "value", value)),
                            "expectedVersion",
                            version,
                            "reason",
                            "验证非法派单提前量");
            assertEquals(409, call("PUT", "/api/admin/config", body, root).getStatusCode().value());
        }
        assertEquals(
                version,
                data(call("GET", "/api/admin/config", null, root)).path("version").asLong());
    }
}
