-- =====================================================================
-- 荣利 · 事件流水补齐「执行结果」维度（系统动作审计）
-- 用途：区分系统自动动作的成功 / 失败 / 跳过，供后台「系统动作」页按主体与结果筛选。
-- 前置：vi_event_log 已存在（ry_20260417.sql 初始版本即含）；无其它迁移依赖。
-- 执行顺序：20261112 第 1 个迁移，独立执行。
-- 备份要求：仅新增列与索引，不改动历史行数据；执行前仍建议 mysqldump。
-- 幂等：可重复执行（先查 information_schema 再 ALTER）。
-- 注意：actor_realm 已能区分 MINI / ADMIN / SYSTEM，本次不再新增 actor_type，
--       避免同一语义两列并存、后续写入出现不一致。
-- =====================================================================

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_event_log' AND COLUMN_NAME = 'result');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_event_log` ADD COLUMN `result` varchar(16) NOT NULL DEFAULT ''SUCCESS'' COMMENT ''执行结果：SUCCESS/FAILED/SKIPPED'' AFTER `action`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_event_log' AND COLUMN_NAME = 'failure_reason');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_event_log` ADD COLUMN `failure_reason` varchar(500) NULL COMMENT ''失败或跳过原因；result=SUCCESS 时为空'' AFTER `result`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 系统动作页按「主体 + 时间」倒序查，现有 ix_order_time 覆盖不到
SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_event_log' AND INDEX_NAME = 'ix_realm_time');
SET @s := IF(@c = 0, 'ALTER TABLE `vi_event_log` ADD KEY `ix_realm_time` (`actor_realm`, `created_at`)', 'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 执行后校验：历史行应全部落成 SUCCESS（无法回溯真实结果，默认视为成功）
SELECT result, COUNT(*) AS cnt FROM vi_event_log GROUP BY result;
SELECT 'migration_20261112_01_event_result done' AS result;
