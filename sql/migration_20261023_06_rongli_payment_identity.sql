-- =============================================================================
-- V6.4 资料包对齐（第六批）：清理孤儿支付单并收紧支付身份约束
--
-- 背景：vi_payment_order 中有 31 条 SUCCESS 记录，order_id 在 vi_order 中不存在。
--   经核对：全部 transaction_id 以 MOCK 开头（模拟支付，无真实资金），
--   全部 total_fen = paid_fen = 33000，创建于 2026-10-22 ~ 10-23 的验收脚本运行期，
--   是「脚本建单→支付→删单」残留。这些行让报表「已收金额」虚增 10230 元，
--   也是 ck_pay_method_identity 一直无法建立的原因。
--
-- 备份：E:/codex/_backup/xinyi_payment_orphans_20261023.sql（31 条 INSERT，可回滚）
--
-- 删除条件同时要求「订单不存在」+「MOCK 交易」，避免误删真实收款订单。
-- 退款单引用检查结果：0 条引用这些支付单。
--
-- 幂等：删除按条件执行（重复执行为 0 行）；约束判存在后添加
-- 执行：mysql --default-character-set=utf8mb4 <本文件>
-- =============================================================================

-- ---------------------------------------------------------------- 1. 清理孤儿单
DELETE p FROM `vi_payment_order` p
  LEFT JOIN `vi_order` o ON o.id = p.order_id
 WHERE o.id IS NULL
   AND p.transaction_id LIKE 'MOCK%';

-- ------------------------------------------------- 2. 支付方式枚举与身份一致性
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_payment_method_enum');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD CONSTRAINT `ck_payment_method_enum` CHECK (`payment_method` IN (''UNSELECTED'',''WECHAT'',''OFFLINE''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 未选择 / 线下收款的支付单不得携带任何线上商户身份：
-- 一旦携带，退款与对账会把它当线上单处理，钱走错通道。
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_pay_method_identity');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD CONSTRAINT `ck_pay_method_identity` CHECK ((`payment_method` = ''WECHAT'') OR (`merchant_profile_id` IS NULL AND `merchant_revision_id` IS NULL AND `app_id` IS NULL AND `mchid` IS NULL))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 退款单只允许线上微信或线下现金/转账两种来源
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME='vi_refund_order'
             AND CONSTRAINT_NAME='ck_refund_method_enum');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_refund_order` ADD CONSTRAINT `ck_refund_method_enum` CHECK (`payment_method` IN (''WECHAT'',''OFFLINE''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ==================================================== 3. 验收断言
SELECT 'orphan_payments' `check`, COUNT(*) `value`, 0 `expect`
  FROM vi_payment_order p LEFT JOIN vi_order o ON o.id = p.order_id WHERE o.id IS NULL
UNION ALL
SELECT 'payment_total', COUNT(*), 20 FROM vi_payment_order
UNION ALL
SELECT 'payment_checks', COUNT(*), 3 FROM information_schema.TABLE_CONSTRAINTS
 WHERE CONSTRAINT_SCHEMA = DATABASE()
   AND CONSTRAINT_NAME IN ('ck_payment_method_enum','ck_pay_method_identity','ck_refund_method_enum');

-- =============================================================================
-- 与 V6.4 的差异（有意保留，勿当遗漏）
-- ck_pay_method_identity 目前**不强制** WECHAT 单必须有 app_id / mchid。
-- 原因：清理孤儿单后仍有 13 条历史 WECHAT 单没有商户身份，
-- 它们产生于商户身份字段加入之前（旧 rongli.pay.* 配置项至今为空），
-- 数据库无法推出当时的 app_id / mchid，编造等于伪造资金归属。
-- 新单一律由 BookingService.prepay() 在锁定支付方式时写入身份，
-- 缺身份会被 IntegrationConfigService 的发布前置校验拦住。
-- 待这批历史单自然归档后，可单独迁移把该分支收紧为
-- CHECK (payment_method <> 'WECHAT' OR (app_id IS NOT NULL AND mchid IS NOT NULL))。
-- =============================================================================
