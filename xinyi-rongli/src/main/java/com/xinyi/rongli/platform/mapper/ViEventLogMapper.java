package com.xinyi.rongli.platform.mapper;

import com.xinyi.rongli.platform.domain.ViEventLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * vi_event_log 数据访问。append-only：只 INSERT，不 UPDATE 业务字段、不物理删除（ADR-03）。
 * 双主体：actor_realm + actor_account_id(MINI) / actor_user_id(ADMIN)。
 * 成功审计随业务同事务插入；失败审计由 AuditService 以独立事务（REQUIRES_NEW）持久化。
 */
public interface ViEventLogMapper {

    /** 插入一条不可变事实；created_at/updated_at/version 用数据库默认（UTC）。 */
    int insert(ViEventLog record);

    /** 按聚合读取事实流水（验证/对账用），按 (created_at,id) 升序。 */
    List<ViEventLog> selectByAggregate(@Param("aggregateType") String aggregateType,
                                       @Param("aggregateId") long aggregateId);
}
