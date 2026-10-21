package com.xinyi.rongli.platform;

import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.ActorRealm;
import com.xinyi.rongli.platform.service.DeadlockRetry;
import com.xinyi.rongli.platform.service.OrderLocks;
import org.junit.jupiter.api.Test;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台原语单元证据：有序锁固定顺序、死锁有限重试、双主体 Actor 不跨域数值混比。纯逻辑，无 DB。
 */
class PlatformPrimitivesTest {

    @Test
    void 有序锁去重升序() {
        assertEquals(List.of(1L, 2L, 3L), OrderLocks.orderedIds(List.of(3L, 1L, 2L, 1L)));
        assertEquals(List.of(), OrderLocks.orderedIds(List.of()));
        assertEquals(List.of(5L), OrderLocks.orderedIds(java.util.Arrays.asList(null, 5L, null)));
        assertEquals(List.of("a", "b", "c"), OrderLocks.orderedKeys(List.of("c", "a", "b", "a")));
    }

    @Test
    void 死锁有限重试后成功() {
        AtomicInteger calls = new AtomicInteger();
        String out = DeadlockRetry.execute(3, () -> {
            if (calls.incrementAndGet() < 3) {
                throw new PessimisticLockingFailureException("deadlock");
            }
            return "ok";
        });
        assertEquals("ok", out);
        assertEquals(3, calls.get());
    }

    @Test
    void 死锁超过上限抛出() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(PessimisticLockingFailureException.class,
                () -> DeadlockRetry.execute(2, () -> {
                    calls.incrementAndGet();
                    throw new PessimisticLockingFailureException("deadlock");
                }));
        assertEquals(2, calls.get());
    }

    @Test
    void 非锁异常不重试() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalStateException.class,
                () -> DeadlockRetry.execute(3, () -> {
                    calls.incrementAndGet();
                    throw new IllegalStateException("unique-violation-not-deadlock");
                }));
        assertEquals(1, calls.get());
    }

    @Test
    void 双主体Actor按域构造() {
        Actor mini = Actor.mini(1001L, "OWNER", null, 9L, 3L, null);
        assertEquals(ActorRealm.MINI, mini.realm());
        assertEquals(1001L, mini.accountId());
        assertTrue(mini.isMini());

        Actor admin = Actor.admin(2002L, "ADMIN");
        assertEquals(ActorRealm.ADMIN, admin.realm());
        assertEquals(2002L, admin.sysUserId());
        assertTrue(admin.isAdmin());

        // 构造期校验：MINI 必须有 accountId，ADMIN 必须有 sysUserId，realm 必填
        assertThrows(IllegalArgumentException.class,
                () -> new Actor(ActorRealm.MINI, null, null, "OWNER", null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Actor(ActorRealm.ADMIN, null, null, "ADMIN", null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Actor(null, 1L, 1L, "X", null, null, null));
    }

    @Test
    void 同域比较成立_跨域数值相等也不混比() {
        Actor miniA = Actor.mini(7L, "OWNER", null, null, null, null);
        Actor miniB = Actor.mini(7L, "DRIVER", null, null, null, null);
        assertTrue(miniA.samePrincipal(miniB)); // 同 MINI 域、同 accountId

        Actor admin7 = Actor.admin(7L, "ADMIN");
        // accountId==sysUserId==7 也不得判为同人（ADR-02：须经可信绑定，不数值混比）
        assertFalse(miniA.samePrincipal(admin7));
        assertFalse(admin7.samePrincipal(miniA));
        assertFalse(Actor.system().samePrincipal(Actor.system()));
    }
}
