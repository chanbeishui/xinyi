package com.xinyi.rongli.platform.service;

import org.springframework.dao.PessimisticLockingFailureException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 死锁/锁等待有限重试（ADR-05：遇死锁有限重试同幂等键）。
 *
 * 关键约束：被重试的 Supplier 必须自行开启“完整事务”（TransactionTemplate 回调或经代理的
 * @Transactional 方法）。死锁发生时数据库已回滚当前事务，重试必须从新事务开始，
 * 因此本模板自身绝不处于事务中，也绝不在事务内捕获后继续。
 *
 * 只重试悲观锁失败（死锁 1213 / 锁等待超时 1205）；其余异常（含唯一键冲突、版本冲突）原样抛出，
 * 由服务层映射为具体错误，不把一切 SQL 异常当并发冲突（ADR-05 失败处理）。
 * 幂等键在重试间保持不变，配合 IdempotencyService 保证“重试同键”语义。
 */
public final class DeadlockRetry {

    private DeadlockRetry() {
    }

    public static <T> T execute(int maxAttempts, Supplier<T> transactionalWork) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts 必须 >= 1");
        }
        int attempt = 0;
        while (true) {
            try {
                return transactionalWork.get();
            } catch (PessimisticLockingFailureException e) {
                // 覆盖死锁(1213 DeadlockLoserDataAccessException)与锁等待超时(1205 CannotAcquireLockException)，
                // 二者均为 PessimisticLockingFailureException 子类；其余异常原样抛出。
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                sleepBackoff(attempt);
            }
        }
    }

    public static void run(int maxAttempts, Runnable transactionalWork) {
        execute(maxAttempts, () -> {
            transactionalWork.run();
            return null;
        });
    }

    /** 小幅随机退避，降低重试再次撞锁概率。 */
    private static void sleepBackoff(int attempt) {
        long base = Math.min(200L, 10L * attempt);
        long jitter = ThreadLocalRandom.current().nextLong(0, base + 1);
        try {
            Thread.sleep(base + jitter);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("死锁重试等待被中断", ie);
        }
    }
}
