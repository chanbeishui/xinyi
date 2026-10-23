-- =====================================================================
-- 荣利 · 司机 / 车辆 主数据归属部门（后台数据权限支撑）
-- 设计：dept_id 为空 = 全公司共享（默认），非空 = 仅归属部门及其下级可见。
-- 幂等：可重复执行
-- =====================================================================

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_driver_profile' AND COLUMN_NAME = 'dept_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_driver_profile` ADD COLUMN `dept_id` BIGINT NULL COMMENT ''归属部门；NULL=全公司共享'' AFTER `internal_remark`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_vehicle' AND COLUMN_NAME = 'dept_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_vehicle` ADD COLUMN `dept_id` BIGINT NULL COMMENT ''归属部门；NULL=全公司共享'' AFTER `internal_remark`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_driver_profile' AND INDEX_NAME = 'ix_driver_dept');
SET @s := IF(@c = 0, 'ALTER TABLE `vi_driver_profile` ADD KEY `ix_driver_dept` (`dept_id`)', 'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_vehicle' AND INDEX_NAME = 'ix_vehicle_dept');
SET @s := IF(@c = 0, 'ALTER TABLE `vi_vehicle` ADD KEY `ix_vehicle_dept` (`dept_id`)', 'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'migration_05 done' AS result;
