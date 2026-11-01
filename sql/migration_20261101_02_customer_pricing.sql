-- 用途：C04 公司统一客户计价的权威归属、运行环境及审核内容绑定。
-- 前置版本：V6.4 79表，已完成 migration_20261101_station_contact_qualification.sql。
-- 顺序：备份 vi_price_rule → 执行本文件一次 → 查看下方异常清单 → 部署后端/PC。
-- 备份：执行前单独导出 vi_price_rule（结构与数据），保留原始导出及执行日志。
-- 可重复性：不可重复执行；重复ADD COLUMN会报错，不用IF NOT EXISTS掩盖结构差异。
-- 回填：不猜测旧规则公司、环境、创建/审核人；旧行保持NULL并由异常查询报告，
--       缺少权威归属或审核哈希的旧行不参与新报价。需人工核实后新建并独立审核发布。
-- 兼容性：仅前向新增可空列、索引与兼容NULL的CHECK；不删除或重写已有数据。
-- 回滚：优先回退应用并保留新增列及审计；保留备份，禁止直接删列丢失新发布证据。
--       若必须物理恢复，先停止写入、独立归档新增配置/审计/outbox，再按审批恢复备份。
-- 生产与现有业务库执行需另行授权；本轮仅在专属测试库/随机临时库验证。
SET NAMES utf8mb4;

ALTER TABLE `vi_price_rule`
  ADD COLUMN `owner_dept_id` BIGINT NULL COMMENT '明确选择的公司权威部门，旧未知归属保持NULL' AFTER `id`,
  ADD COLUMN `environment` VARCHAR(12) NULL COMMENT 'TEST/LIVE；旧未知环境保持NULL' AFTER `owner_dept_id`,
  ADD COLUMN `created_by` BIGINT NULL COMMENT '可信ADMIN创建人；不接收客户端提交' AFTER `approved_by`,
  ADD COLUMN `config_hash` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '规范化公司环境及计价内容SHA256' AFTER `created_by`,
  ADD COLUMN `approved_hash` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '独立审核通过时绑定的内容SHA256' AFTER `config_hash`,
  ADD COLUMN `published_at` DATETIME(3) NULL COMMENT '实际发布时间UTC；不改历史配置' AFTER `approved_hash`,
  ADD COLUMN `source_rule_id` BIGINT NULL COMMENT '显式派生来源规则ID，服务端验证实际归属' AFTER `published_at`,
  ADD KEY `ix_price_owner_lookup` (`owner_dept_id`,`environment`,`vehicle_type_code`,`inspection_type_code`,`status`,`effective_from`,`effective_to`),
  ADD CONSTRAINT `ck_price_owner` CHECK (`owner_dept_id` IS NULL OR `owner_dept_id` > 0),
  ADD CONSTRAINT `ck_price_environment` CHECK (`environment` IS NULL OR `environment` IN ('TEST','LIVE')),
  ADD CONSTRAINT `ck_price_hash` CHECK (`config_hash` IS NULL OR LENGTH(`config_hash`) = 64),
  ADD CONSTRAINT `ck_price_approved_hash` CHECK (`approved_hash` IS NULL OR LENGTH(`approved_hash`) = 64);

-- 异常报告：必须保留查询结果，不得自动用主部门/创建人/测试环境填充。
SELECT id, rule_key, revision, status, owner_dept_id, environment,
       CASE WHEN owner_dept_id IS NULL OR environment IS NULL THEN '归属或环境待核实'
            WHEN config_hash IS NULL THEN '内容摘要待核实'
            WHEN status IN ('APPROVED','PUBLISHED') AND approved_hash IS NULL THEN '审核绑定待核实'
            ELSE '其他' END AS issue
FROM vi_price_rule
WHERE owner_dept_id IS NULL OR environment IS NULL OR config_hash IS NULL
   OR (status IN ('APPROVED','PUBLISHED') AND approved_hash IS NULL);

-- 执行后校验：应返回7列；核对类型、可空性以及新增索引/CHECK。
SELECT column_name, column_type, is_nullable FROM information_schema.columns
WHERE table_schema=DATABASE() AND table_name='vi_price_rule'
  AND column_name IN ('owner_dept_id','environment','created_by','config_hash','approved_hash','published_at','source_rule_id');
SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_order
FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='vi_price_rule'
GROUP BY index_name;
SELECT constraint_name FROM information_schema.table_constraints
WHERE table_schema=DATABASE() AND table_name='vi_price_rule' AND constraint_type='CHECK';
