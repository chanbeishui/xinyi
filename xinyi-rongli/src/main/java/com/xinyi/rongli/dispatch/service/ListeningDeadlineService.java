package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** 每次本人开启有独立固定期限，定时器只关闭；重启、配置延长和重复消息都不能重建为次日听单。 */
@Service
public class ListeningDeadlineService {
    public static final String TIMER = "DRIVER_LISTENING_EXPIRES";
    private final DispatchListeningMapper mapper;
    private final DriverMapper drivers;
    private final EmployeeDirectoryGuardMapper directory;
    private final ViAccountMapper accounts;
    private final OrderMapper orders;
    private final OutboxService outbox;
    private final AuditService audit;
    private final Clock clock;
    private final TransactionTemplate tx;

    public ListeningDeadlineService(
            DispatchListeningMapper mapper,
            DriverMapper drivers,
            EmployeeDirectoryGuardMapper directory,
            ViAccountMapper accounts,
            OrderMapper orders,
            OutboxService outbox,
            AuditService audit,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.drivers = drivers;
        this.directory = directory;
        this.accounts = accounts;
        this.orders = orders;
        this.outbox = outbox;
        this.audit = audit;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public void register(DriverProfile row) {
        requireTransaction();
        if (!row.getListeningEnabled() || row.getListeningUntil() == null) return;
        outbox.publishAt(
                "LISTENING_EXPIRY:" + row.getId() + ":" + row.getListeningRevision(),
                "VI_DRIVER_PROFILE",
                row.getId(),
                TIMER,
                Map.of(
                        "driverId",
                        row.getId(),
                        "listeningRevision",
                        row.getListeningRevision(),
                        "until",
                        row.getListeningUntil().toString()),
                row.getListeningUntil());
    }

    public int repairMissing() {
        int count = 0;
        for (Long id : mapper.dueDrivers(now())) {
            Boolean result =
                    tx.execute(
                            status -> {
                                DriverProfile row = lock(id);
                                return row != null && closeIfDue(row);
                            });
            if (Boolean.TRUE.equals(result)) count++;
        }
        for (Long id : mapper.missingTimers()) {
            tx.executeWithoutResult(
                    status -> {
                        DriverProfile row = lock(id);
                        if (row != null && !closeIfDue(row)) register(row);
                    });
        }
        return count;
    }

    public void execute(ViOutbox event) {
        if (!TIMER.equals(event.getEventType())
                || !"VI_DRIVER_PROFILE".equals(event.getAggregateType())) invalid();
        var payload = ViJson.tree(event.getPayload());
        long revision = payload.path("listeningRevision").asLong(-1);
        long driver = payload.path("driverId").asLong(-1);
        Instant until;
        try {
            until = Instant.parse(payload.path("until").asText(""));
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
        if (!payload.isObject()
                || payload.size() != 3
                || !payload.path("driverId").isIntegralNumber()
                || !payload.path("listeningRevision").isIntegralNumber()
                || revision < 0
                || driver != event.getAggregateId()
                || !("LISTENING_EXPIRY:" + driver + ":" + revision).equals(event.getEventKey()))
            invalid();
        tx.executeWithoutResult(
                status -> {
                    DriverProfile row = lock(driver);
                    if (row == null
                            || !row.getListeningEnabled()
                            || row.getListeningRevision() != revision) return;
                    // 旧代次任务只ACK；同代次必须精确匹配原截止，错payload不能关闭另一份真实窗口。
                    if (!until.equals(row.getListeningUntil())) invalid();
                    if (!closeIfDue(row))
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "尚未到本次固定听单截止");
                });
    }

    public DriverProfile lock(long id) {
        directory.ensureGuard();
        directory.lockGuard();
        Long account = mapper.account(id);
        if (account == null) return null;
        if (accounts.selectByIdForUpdate(account) == null || orders.lockSecurity(account) == null)
            throw new ViException(ViErrorCode.VI_AUTHORITY_UNAVAILABLE);
        return drivers.lock(id);
    }

    public boolean closeIfDue(DriverProfile row) {
        requireTransaction();
        if (!row.getListeningEnabled()
                || ListeningDayPolicy.isListening(
                        true,
                        row.getListeningUntil(),
                        row.getListeningCompanyRevision(),
                        row.getTrainingContextRevision(),
                        now())) return false;
        if (mapper.saveListening(
                        row,
                        false,
                        row.getListeningUntil(),
                        row.getListeningCompanyRevision(),
                        row.getListeningConfigVersion())
                != 1) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        long closedRevision = row.getListeningRevision() + 1;
        audit.recordSuccess(
                Actor.system(),
                "VI_DRIVER_PROFILE",
                row.getId(),
                "DRIVER_LISTENING_EXPIRED",
                null,
                Map.of("listeningRevision", closedRevision),
                null);
        outbox.publish(
                "DRIVER_LISTENING_EXPIRED:" + row.getId() + ":" + closedRevision,
                "VI_DRIVER_PROFILE",
                row.getId(),
                "DRIVER_LISTENING_EXPIRED",
                Map.of("driverId", row.getId(), "listeningRevision", closedRevision));
        return true;
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("听单期限必须与本人开关同事务");
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
