-- S01 售后申请原因代码与追加式补充资料；前置20261105_01，按迁移文件序号执行。
-- 执行前备份vi_aftersale_case并暂停售后写入口；DDL不可重复执行，先检查列和表不存在。
-- 旧申请reason及evidence_ids完整保留；reason_code不猜测回填，历史补充资料无可靠来源不伪造。
-- 应用回滚保留新增列和表，不能删除已有申请、补充资料或凭证；数据库前向兼容无需恢复旧结构。
-- 执行后重启后端启用接口，不需清理权限缓存；本迁移不变更资金、审核或权限数据。
SELECT id,case_no FROM vi_aftersale_case WHERE reason IS NULL OR TRIM(reason)='';
ALTER TABLE vi_aftersale_case ADD COLUMN reason_code VARCHAR(64) NULL COMMENT '申请原因代码，原说明独立保留' AFTER reason;
CREATE TABLE `vi_aftersale_note` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '补充记录ID，API十进制字符串',
  `case_id` BIGINT NOT NULL COMMENT '原售后申请；权限以原订单归属为准',
  `account_id` BIGINT NOT NULL COMMENT '实际补充资料的车主账号',
  `note` VARCHAR(1000) NOT NULL COMMENT '补充说明，追加后不可覆盖原申请',
  `evidence_ids` JSON COMMENT '已核验且归属原订单的私有文件ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  PRIMARY KEY (`id`),
  KEY ix_aftersale_note_case (case_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车主售后补充资料追加历史';

-- 执行后校验：新增表应存在，历史申请不得发生资金/状态回填，原因代码允许NULL。
SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('vi_aftersale_case','vi_aftersale_note') ORDER BY TABLE_NAME,ORDINAL_POSITION;
SELECT n.id FROM vi_aftersale_note n LEFT JOIN vi_aftersale_case c ON c.id=n.case_id WHERE c.id IS NULL;
SHOW CREATE TABLE vi_aftersale_case;
SHOW CREATE TABLE vi_aftersale_note;
