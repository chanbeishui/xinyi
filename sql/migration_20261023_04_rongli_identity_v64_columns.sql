-- =============================================================================
-- V6.4 资料包对齐（第四批）：身份域列的完整同步
-- 资料包：机动车检测预约_完整开发资料包_V6.4_第三方接入配置版
--        05_数据设计/001_vi业务表_初始化_V6.4.sql
--
-- 本脚本补齐上一批（02）明确留下的 13 个字段：
--   vi_account                     +3 列 + 1 CHECK
--   vi_employee_binding            +9 列 + 索引调整 + 4 个 CHECK
--   vi_driver_statement_membership_change  +1 CHECK
--
-- 历史行处理（已与业务确认）：
--   现存唯一 1 行绑定是旧流程人工签发凭证建立的，没有手机号 HMAC、
--   没有框架授权版本快照。这类行标记 source='MANUAL_LEGACY'，
--   由条件化 CHECK 豁免新字段非空要求；source='WECHAT_PHONE_MATCH'
--   的新行必须满足 V6.4 全部非空约束。
--   因此 V6.4 中 phone_lookup / last_phone_verified_at /
--   employee_auth_version 的 NOT NULL 在本库改为「按 source 条件非空」，
--   这是为保留历史行做的唯一放宽，已在文件末尾记录。
--
-- 幂等：全部使用 information_schema 判存在后再 ALTER
-- 执行：mysql --default-character-set=utf8mb4 <本文件>
-- =============================================================================

-- ============================================== 一、vi_account 手机号来源
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_account' AND COLUMN_NAME='phone_verified_at');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_account` ADD COLUMN `phone_verified_at` DATETIME(3) NULL COMMENT ''服务端最后一次可信手机号验证UTC时刻'' AFTER `phone_lookup`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_account' AND COLUMN_NAME='phone_source');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_account` ADD COLUMN `phone_source` VARCHAR(24) NULL COMMENT ''PhoneProofSource: WECHAT,SMS；仅WECHAT可触发新员工关联'' AFTER `phone_verified_at`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_account' AND COLUMN_NAME='employee_auto_link_blocked');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_account` ADD COLUMN `employee_auto_link_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT ''管理员解除后阻止该账号自动换绑；仅后台明确恢复，可不影响车主'' AFTER `phone_source`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_account'
             AND CONSTRAINT_NAME='ck_account_phone_source');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_account` ADD CONSTRAINT `ck_account_phone_source` CHECK (`phone_source` IN (''WECHAT'',''SMS''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ================================== 二、vi_employee_binding 关联来源与生命周期
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='app_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT ''关联应用命名空间'' AFTER `permission_version`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='source');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `source` VARCHAR(32) NULL COMMENT ''EmployeeLinkSource: WECHAT_PHONE_MATCH, MANUAL_LEGACY'' AFTER `app_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='status');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `status` VARCHAR(24) NULL COMMENT ''EmployeeBindingLifecycle: ACTIVE,SUSPENDED,REVOKED'' AFTER `source`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='phone_lookup');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `phone_lookup` BINARY(32) NULL COMMENT ''关联时一致的规范化手机号HMAC；不是可还原裸手机号'' AFTER `status`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='last_phone_verified_at');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `last_phone_verified_at` DATETIME(3) NULL COMMENT ''最近本人微信手机号核验时间'' AFTER `phone_lookup`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='employee_auth_version');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `employee_auth_version` BIGINT NULL COMMENT ''建立关联时的框架授权版本快照；每次工作请求仍检查当前员工状态和权限'' AFTER `last_phone_verified_at`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='auto_rebind_blocked');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `auto_rebind_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT ''管理员撤销后拦截同应用同员工新自动关联；明确allow-rematch才解除'' AFTER `employee_auth_version`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='suspended_reason');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `suspended_reason` VARCHAR(64) NULL COMMENT ''手机号变更、账号停用等失效原因；不泄漏其他账号'' AFTER `auto_rebind_blocked`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='revoked_by');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `revoked_by` BIGINT NULL COMMENT ''后台受控撤销操作者'' AFTER `suspended_reason`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND COLUMN_NAME='revoked_reason');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD COLUMN `revoked_reason` VARCHAR(500) NULL COMMENT ''解除依据'' AFTER `revoked_by`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- bound_by 改为可空：V6.4 规定自动关联时为空，代表操作者是系统而不是某个管理员
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `bound_by` BIGINT NULL COMMENT '自动关联为NULL，操作者为系统；管理员后续操作单独审计，绝不填假管理员';

-- 历史行回填：app_id 取账号所属应用；状态按 revoked_at 推导；来源标记为历史人工绑定
UPDATE `vi_employee_binding` b
  JOIN `vi_account` a ON a.id = b.account_id
   SET b.app_id = COALESCE(b.app_id, a.app_id),
       b.source = COALESCE(b.source, 'MANUAL_LEGACY'),
       b.status = COALESCE(b.status, IF(b.revoked_at IS NULL, 'ACTIVE', 'REVOKED'))
 WHERE b.app_id IS NULL OR b.source IS NULL OR b.status IS NULL;

-- 回填后收紧；phone_lookup 等三列保持可空以容纳 MANUAL_LEGACY 行
ALTER TABLE `vi_employee_binding`
  MODIFY COLUMN `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '关联应用命名空间',
  MODIFY COLUMN `source` VARCHAR(32) NOT NULL DEFAULT 'WECHAT_PHONE_MATCH' COMMENT 'EmployeeLinkSource: WECHAT_PHONE_MATCH, MANUAL_LEGACY',
  MODIFY COLUMN `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'EmployeeBindingLifecycle: ACTIVE,SUSPENDED,REVOKED';

-- 唯一占用的命名空间从「全库唯一」改为「按应用唯一」，与 V6.4 uk_active_user(app_id,active_user) 一致
SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND INDEX_NAME='uk_active_user' AND SEQ_IN_INDEX=2);
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` DROP INDEX `uk_active_user`, ADD UNIQUE KEY `uk_active_user` (`app_id`,`active_user`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND INDEX_NAME='ix_employee_link_phone');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD KEY `ix_employee_link_phone` (`phone_lookup`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND INDEX_NAME='ix_employee_link_block');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD KEY `ix_employee_link_block` (`app_id`,`sys_user_id`,`auto_rebind_blocked`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND CONSTRAINT_NAME='ck_employee_binding_source');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD CONSTRAINT `ck_employee_binding_source` CHECK (`source` IN (''WECHAT_PHONE_MATCH'',''MANUAL_LEGACY''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND CONSTRAINT_NAME='ck_employee_binding_status');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD CONSTRAINT `ck_employee_binding_status` CHECK (`status` IN (''ACTIVE'',''SUSPENDED'',''REVOKED''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND CONSTRAINT_NAME='ck_employee_link_revoke');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD CONSTRAINT `ck_employee_link_revoke` CHECK ((`status`=''REVOKED'' AND `revoked_at` IS NOT NULL) OR (`status` IN (''ACTIVE'',''SUSPENDED'') AND `revoked_at` IS NULL))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 自动关联必须由系统发起：不允许挂管理员身份
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND CONSTRAINT_NAME='ck_employee_link_source_actor');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD CONSTRAINT `ck_employee_link_source_actor` CHECK (`source` <> ''WECHAT_PHONE_MATCH'' OR `bound_by` IS NULL)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 自动关联必须有手机号 HMAC 与核验时刻；历史人工绑定豁免
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
             AND CONSTRAINT_NAME='ck_employee_link_identity');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_employee_binding` ADD CONSTRAINT `ck_employee_link_identity` CHECK (`source` <> ''WECHAT_PHONE_MATCH'' OR (`phone_lookup` IS NOT NULL AND `last_phone_verified_at` IS NOT NULL AND `employee_auth_version` IS NOT NULL))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ============================ 三、vi_driver_statement_membership_change 版本约束
-- 资料包 V6.4 的 001_vi业务表_初始化_V6.4.sql 第 1270 行写的是：
--   CONSTRAINT ck_ver_driver_statement_membership_change CHECK (version >= 0)
-- 该表**没有 version 列**（列清单：id, statement_id, ledger_id, line_id, action,
-- revision_before, revision_after, actor_user_id, reason, snapshot_json, created_at），
-- 直接执行会报 ERROR 3820 Check constraint ... refers to non-existing column 'version'，
-- 并且整个脚本在第 1256 行中断 —— 79 张表只建到 56 张，后面 23 张全缺（实测）。
-- 若改成对已有表 ALTER 则是另一个报法：ERROR 1054 Unknown column 'version'。
-- 这是一张"对账成员变更不可变历史"表：只追加、永不 UPDATE，本来就不做乐观锁，
-- 所以没有 version 列是对的；上游把它当成普通业务表批量生成了 ck_ver_* 约束。
--
-- 修法：把"版本不得为负"这个校验落到本表**真实存在的两个版本列**上，
-- 而不是给一张只追加的表硬加一个永远为 0 的 version 列（那会让乐观锁语义名存实亡）。
-- 约束名沿用上游，避免与资料包逐表比对时出现"少一条约束"的假差异。
-- 资料包侧同步修正：把该行改成 CHECK (revision_before >= 0 AND revision_after >= 0)。
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_driver_statement_membership_change'
             AND CONSTRAINT_NAME='ck_ver_driver_statement_membership_change');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_driver_statement_membership_change` ADD CONSTRAINT `ck_ver_driver_statement_membership_change` CHECK (`revision_before` >= 0 AND `revision_after` >= 0)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ==================================================== 四、验收断言
SELECT 'account_new_cols' `check`, COUNT(*) `value`, 3 `expect` FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_account'
    AND COLUMN_NAME IN ('phone_verified_at','phone_source','employee_auto_link_blocked')
UNION ALL
SELECT 'binding_new_cols', COUNT(*), 10 FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
    AND COLUMN_NAME IN ('app_id','source','status','phone_lookup','last_phone_verified_at',
                        'employee_auth_version','auto_rebind_blocked','suspended_reason',
                        'revoked_by','revoked_reason')
UNION ALL
SELECT 'binding_checks', COUNT(*), 5 FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding' AND CONSTRAINT_TYPE='CHECK'
    AND CONSTRAINT_NAME IN ('ck_employee_binding_source','ck_employee_binding_status',
                            'ck_employee_link_revoke','ck_employee_link_source_actor',
                            'ck_employee_link_identity')
UNION ALL
SELECT 'legacy_rows_marked', COUNT(*), 1 FROM vi_employee_binding WHERE source='MANUAL_LEGACY'
UNION ALL
SELECT 'statement_change_cols', COUNT(*), 11 FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_driver_statement_membership_change'
UNION ALL
SELECT 'statement_change_ver_ck', COUNT(*), 1 FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_driver_statement_membership_change'
    AND CONSTRAINT_TYPE='CHECK' AND CONSTRAINT_NAME='ck_ver_driver_statement_membership_change';

-- =============================================================================
-- 与 V6.4 的差异（有意保留，勿当遗漏）
-- 1) vi_employee_binding.phone_lookup / last_phone_verified_at /
--    employee_auth_version 在 V6.4 是 NOT NULL，本库为可空。
--    原因：现存 1 行历史人工绑定没有手机号 HMAC 与授权版本快照，
--    数据库无法反推出可信值，编造会写成错误的身份锚点。
--    新流程写入的行由 ck_employee_link_identity 强制三列非空，
--    安全性不依赖列级 NOT NULL。
-- 2) source 枚举比 V6.4 多一个 MANUAL_LEGACY，仅用于标识历史行；
--    新代码只写 WECHAT_PHONE_MATCH。
-- 3) ck_ver_driver_statement_membership_change 与 V6.4 同名但表达式不同：
--    V6.4 是 CHECK (version >= 0)，而该表没有 version 列（追加型不可变历史表，
--    不做乐观锁），照抄会直接 ERROR 1054。本库改为
--    CHECK (revision_before >= 0 AND revision_after >= 0)，
--    即把"版本不得为负"校验到该表真实存在的两个版本列上。
--    资料包侧建议同步改成同一表达式，否则每次重跑资料包 SQL 仍会在这里报错。
-- =============================================================================
