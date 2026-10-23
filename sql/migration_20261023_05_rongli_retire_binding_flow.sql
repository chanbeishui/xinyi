-- =============================================================================
-- V6.4 资料包对齐（第五批）：退役员工绑定申请与一次性凭证兑换流程
-- 资料包：01_PRD/身份流程_员工自动关联_V6.4.md 第 7 节「页面和接口退役」
--
-- 背景：V6.4 用「微信手机号授权 → 唯一匹配自动关联」取代了
--       「员工自助提交申请 → 管理员签发一次性凭证 → 员工输入凭证兑换」。
--       两张表现存数据均为 0 行，删除无数据损失。
--       相关代码（4 个接口、10 个 Mapper 方法、3 个服务方法、2 个 DTO、
--       治理页「员工账号绑定」页签）已同步删除。
--
-- 保留：vi_employee_binding（关联本身仍需要），仅退役 request/credential 两张。
-- 保留：sys_security_audit_log 中历史 BINDING_* 事件（审计不可删）。
--
-- 幂等：drop 前判存在
-- 执行：mysql --default-character-set=utf8mb4 <本文件>
-- =============================================================================

SET @c := (SELECT COUNT(1) FROM information_schema.TABLES
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding_credential');
SET @s := IF(@c > 0, 'DROP TABLE `vi_employee_binding_credential`', 'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLES
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding_request');
SET @s := IF(@c > 0, 'DROP TABLE `vi_employee_binding_request`', 'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ==================================================== 验收断言
SELECT 'retired_binding_tables' `check`, COUNT(*) `value`, 0 `expect`
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME IN ('vi_employee_binding_request','vi_employee_binding_credential')
UNION ALL
SELECT 'binding_table_kept', COUNT(*), 1 FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_employee_binding'
UNION ALL
SELECT 'vi_table_total', COUNT(*), 80 FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'vi\_%';

-- 说明：资料包 V6.4 是 79 张 vi_ 表，本库为 80 张。
-- 多出的一张是 vi_sms_send_log：它承担短信验证码（LOGIN_OTP / PHONE_VERIFY）
-- 的发送留痕与按账号/手机号/场景的频控计数，这部分用例 V6.4 的
-- vi_notification_delivery 没有覆盖（后者只承载模板化业务通知）。
-- 属于「V6.4 未覆盖的既有用例」，保留并在交付文档登记。
