package com.xinyi.rongli.platform.mapper;

import com.xinyi.rongli.platform.domain.ViOutbox;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * vi_outbox 数据访问。唯一键 uk_event(event_key) 防重（自然源事件）。
 * 中继按 DB 到期扫描 + FOR UPDATE SKIP LOCKED 领取 + 租约恢复，Redis 过期通知不是唯一时钟（ADR-08）。
 * 纯 MyBatis；所有状态推进用 version CAS，受影响行数须为 1。
 */
public interface ViOutboxMapper {

    /** 同事务插入 READY 事件；next_attempt_at 必填，created_at/updated_at/version 用数据库默认。 */
    int insert(ViOutbox record);

    ViOutbox selectByEventKey(@Param("eventKey") String eventKey);

    /** 到期可领取：status=READY 且 next_attempt_at<=now，按 (next_attempt_at,id) 稳定排序，行锁跳过已锁。 */
    List<ViOutbox> selectDueForUpdate(@Param("now") Instant now, @Param("limit") int limit);

    /** 领取：CAS READY→PROCESSING 并写租约；version 匹配，返回受影响行数。 */
    int claim(@Param("id") long id,
              @Param("leaseUntil") Instant leaseUntil,
              @Param("expectedVersion") long expectedVersion);

    /** 完成：CAS PROCESSING→DONE；version 匹配。 */
    int markDone(@Param("id") long id, @Param("expectedVersion") long expectedVersion);

    /** 失败退避：attempts+1，写 next_attempt_at；attempts 达上限则置 FAILED，否则回 READY 等待重试。 */
    int markFailed(@Param("id") long id,
                   @Param("attempts") int attempts,
                   @Param("nextAttemptAt") Instant nextAttemptAt,
                   @Param("terminal") boolean terminal,
                   @Param("expectedVersion") long expectedVersion);

    /** 租约恢复：PROCESSING 且 lease_until<now 的滞留事件回收为 READY（不改 attempts）。返回行数。 */
    int recoverExpiredLeases(@Param("now") Instant now);
}
