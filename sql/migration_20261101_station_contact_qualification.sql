-- =============================================================================
-- migration_20261101_station_contact_qualification.sql
-- 用途：C02 站点契约补齐。04_接口/openapi_V6.4.yaml 的 StationCreate/StationUpdate 要求
--       contactPhone+qualificationFileIds、Station/PublicStationView 要求 phoneMask，
--       但 05_数据设计 vi_station 无对应存储列。经用户确认采用「加列补齐（前向迁移）」，
--       为 vi_station 增加联系电话密文与资质文件ID列表两列，使数据模型与接口主源一致。
-- 适用前置：已执行 migration_20261021_vehicle_initial.sql 与 migration_20261022_01_integration_config.sql
--           （xinyi 库已有 vi_station 表，V6.4 79 表结构）。
-- 执行顺序：单文件两条 ALTER ADD COLUMN；DDL 隐式提交、无全脚本事务回滚。
-- 备份要求：执行前必须 mysqldump 备份 xinyi（沿用 .vi-local/backup/ 既有备份约定）。
-- 可重复性：不可重复执行（ADD COLUMN 无 IF NOT EXISTS，重复将报错，以此暴露误重跑）。
-- 失败恢复：从备份整库恢复；或 ALTER TABLE vi_station DROP COLUMN qualification_file_ids,
--           DROP COLUMN contact_phone_cipher（见文件尾回滚说明）。
-- 数据安全：两列均 NULL 可空、不回填、不猜测历史值；vi_station 当前为新系统配置数据，
--           加列不重写既有行；联系电话以 PhoneCrypto 密文存储，展示仅脱敏 phoneMask，不存明文。
-- 权威来源：接口主源 04_接口/openapi_V6.4.yaml（Station/StationCreate/StationUpdate/PublicStationView）；
--           同步更新 05_数据设计/data_model_V6.4.json 的 vi_station 列与 sql/ry_20260417.sql 全量初始化。
-- =============================================================================

ALTER TABLE `vi_station`
  ADD COLUMN `contact_phone_cipher` VARBINARY(512) NULL COMMENT '检测点已批准公开联系电话密文；PhoneCrypto加密，对外仅脱敏phoneMask' AFTER `enabled`,
  ADD COLUMN `qualification_file_ids` JSON NULL COMMENT '资质文件不透明ID列表（引用vi_file）；不含明文证件原图/URL' AFTER `contact_phone_cipher`;

-- ---------------------------------------------------------------------------
-- 执行后校验：两列存在且类型正确
-- ---------------------------------------------------------------------------
-- SELECT column_name, column_type, is_nullable
--   FROM information_schema.columns
--  WHERE table_schema = DATABASE() AND table_name = 'vi_station'
--    AND column_name IN ('contact_phone_cipher','qualification_file_ids');
-- 期望：contact_phone_cipher varbinary(512) YES；qualification_file_ids json YES。

-- ---------------------------------------------------------------------------
-- 回滚说明（仅在需要撤销本次前向变更时执行；会丢失这两列已写入的联系电话密文与资质ID）
-- ---------------------------------------------------------------------------
-- ALTER TABLE `vi_station` DROP COLUMN `qualification_file_ids`, DROP COLUMN `contact_phone_cipher`;