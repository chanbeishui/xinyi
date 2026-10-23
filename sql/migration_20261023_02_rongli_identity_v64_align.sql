-- =============================================================================
-- V6.4 资料包对齐（第二批）：身份/授权域的纯增量同步
-- 资料包：机动车检测预约_完整开发资料包_V6.4_第三方接入配置版
--        05_数据设计/001_vi业务表_初始化_V6.4.sql（79 张 vi_ 表）
--
-- 本脚本只做**纯增量**：
--   1) 补齐 V6.4 有、本库无的 3 张空表（无外键、无存量数据，可安全新建）
--   2) 不做列变更、不删表 —— 原因见文件末尾「明确不做的事」，都有阻塞条件
--
-- 幂等：全部使用 IF NOT EXISTS / information_schema 判存在
-- 执行：mysql --default-character-set=utf8mb4 <本文件>
-- =============================================================================

-- ---------------------------------------------------------------- 1. 缺失表
-- 员工手机号匹配与目录变更串行防护（V6.4 新增，当前代码未使用）
CREATE TABLE IF NOT EXISTS `vi_employee_directory_guard` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `guard_key` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '首期唯一GLOBAL_DIRECTORY；同库轻量互斥，不是用户权限',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_directory_guard` (`guard_key`),
  CONSTRAINT `ck_ver_employee_directory_guard` CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工手机号匹配与目录变更串行防护';

-- 手机号授权处理与重试回执（V6.4 新增，当前代码未使用）
CREATE TABLE IF NOT EXISTS `vi_phone_verification_receipt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `request_id` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '客户端一次流程UUID，绑定当前账号',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端应用标识',
  `account_id` BIGINT NOT NULL COMMENT '已认证MINI账号；不从请求接收',
  `session_id` BIGINT NOT NULL COMMENT '发起的基础会话',
  `code_hash` BINARY(32) NOT NULL COMMENT '微信phoneCode摘要，防跨账号重放；绝不保存原始code',
  `status` VARCHAR(24) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PhoneVerificationStatus: PROCESSING,VERIFIED,UNKNOWN,FAILED',
  `phone_lookup` BINARY(32) NULL COMMENT '成功后可信手机号HMAC',
  `phone_cipher` VARBINARY(512) NULL COMMENT '成功结果恢复所需加密号码；敏感回执到期按留存规则清理，审计不记录原文',
  `verified_at` DATETIME(3) NULL COMMENT '微信已成功核验的UTC时间',
  `expires_at` DATETIME(3) NOT NULL COMMENT '关联重试窗口截至时间；默认10分钟是技术值，不是微信code有效期',
  `employee_link_outcome` VARCHAR(40) NOT NULL DEFAULT 'NOT_ATTEMPTED' COMMENT 'EmployeeLinkOutcome；内部原因不直接下发小程序',
  `lease_until` DATETIME(3) NULL COMMENT '处理租约；处理中未知不盲目重复消费供应商凭据',
  `finished_at` DATETIME(3) NULL COMMENT '最终处理时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone_request` (`account_id`,`request_id`),
  UNIQUE KEY `uk_phone_code` (`app_id`,`code_hash`),
  KEY `ix_phone_receipt_status` (`status`,`lease_until`),
  CONSTRAINT `ck_cbadb1ceb7` CHECK (`status` IN ('PROCESSING','VERIFIED','UNKNOWN','FAILED')),
  CONSTRAINT `ck_b584437700` CHECK (`employee_link_outcome` IN ('NOT_ATTEMPTED','NO_MATCH','LINKED','EXISTING_LINK','AMBIGUOUS_PHONE','EMPLOYEE_UNAVAILABLE','USER_ALREADY_LINKED','ACCOUNT_ALREADY_LINKED','REBIND_BLOCKED','PHONE_CHANGED','DIRECTORY_UNAVAILABLE')),
  CONSTRAINT `ck_ver_phone_verification_receipt` CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手机号授权处理与重试回执';

-- 员工站点授权集合版本（V6.4 新增，当前代码未使用）
CREATE TABLE IF NOT EXISTS `vi_station_grant_set` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `sys_user_id` BIGINT NOT NULL COMMENT '目标框架员工；可无微信账号',
  `own_station_id` BIGINT NULL COMMENT '仅行政展示，不自动授权',
  `changed_by` BIGINT NULL COMMENT '后台修改人',
  `reason` VARCHAR(500) NULL COMMENT '本次集合调整原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_station_grant_employee` (`sys_user_id`),
  CONSTRAINT `ck_ver_station_grant_set` CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工站点授权集合版本';

-- ---------------------------------------------------------------- 2. 断言
SELECT 'v64_tables_present' AS obj, COUNT(*) AS cnt FROM information_schema.TABLES
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN
  ('vi_employee_directory_guard','vi_phone_verification_receipt','vi_station_grant_set')
UNION ALL SELECT 'vi_total', COUNT(*) FROM information_schema.TABLES
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME LIKE 'vi\_%';

-- =============================================================================
-- 明确不做的事（都有阻塞条件，需先决策或先改代码）
-- =============================================================================
-- A. vi_account 缺 3 列、vi_employee_binding 缺 10 列（V6.3 身份流程遗留）
--    缺失列：
--      vi_account            : phone_verified_at, phone_source, employee_auto_link_blocked
--      vi_employee_binding   : app_id, source, status, phone_lookup, last_phone_verified_at,
--                              employee_auth_version, auto_rebind_blocked, suspended_reason,
--                              revoked_by, revoked_reason
--    阻塞点：phone_lookup BINARY(32) / last_phone_verified_at / employee_auth_version 均为 NOT NULL，
--    且 phone_lookup 是手机号 HMAC，数据库无法自行推导。现有 1 行 vi_employee_binding 若用隐式默认值
--    回填，会写出一串错误的 HMAC，等于伪造身份锚点。正确做法是由应用层按当时的可信手机号重新计算后回填，
--    再收紧 NOT NULL；在此之前不允许加列。
--
-- B. vi_employee_binding_credential / vi_employee_binding_request 在 V6.4 已退役，但本库仍在用
--    引用位置：AccountMapper、AuthService（约 654 行）、AdminService（约 2375 行）。
--    直接 DROP 会让员工绑定申请与凭据兑换直接报错，必须先改代码并做数据迁移，不能用 DDL 硬删。
--
-- C. vi_sms_send_log 是本仓库自有扩展（V6.4 未定义），用于短信发送留痕，保留不动。
