-- P01：预支付加密快照和请求租约。前置：20261102_03_integration_control，之后执行本脚本。
-- 执行前备份 vi_payment_order；维护窗口暂停建单/支付。DDL不可重复执行；回填可重复。
-- 兼容前向增加字段，不删除历史交易。回退应用前保留本表和密钥，已锁定交易不得解锁。
ALTER TABLE vi_payment_order
  ADD COLUMN `prepay_state` VARCHAR(16) NOT NULL DEFAULT 'NONE' COMMENT '预支付 NONE/REQUESTING/READY/UNKNOWN，不代表付款成功',
  ADD COLUMN `prepay_request_cipher` MEDIUMBLOB NULL COMMENT '不可变预下单请求，加密绑定支付单ID',
  ADD COLUMN `prepay_result_cipher` MEDIUMBLOB NULL COMMENT '预支付ID加密存储，不存客户端签名',
  ADD COLUMN `prepay_lease_token` VARCHAR(36) NULL COMMENT '外部请求租约令牌',
  ADD COLUMN `prepay_lease_until` DATETIME(3) NULL COMMENT '外部请求租约截止UTC';
-- 只修正从未锁定方式或发起交易的长编号；其余异常仅报告，人工核查。
UPDATE vi_payment_order p LEFT JOIN vi_payment_order collision ON collision.out_trade_no=CONCAT('P',LPAD(CAST(p.id AS CHAR),20,'0')) AND collision.id<>p.id
SET p.out_trade_no=CONCAT('P',LPAD(CAST(p.id AS CHAR),20,'0')),p.version=p.version+1
WHERE CHAR_LENGTH(p.out_trade_no)>32 AND p.payment_method='UNSELECTED' AND p.status='PENDING' AND p.transaction_id IS NULL AND p.merchant_profile_id IS NULL AND p.method_locked_at IS NULL AND p.paid_fen=0 AND collision.id IS NULL;
SELECT id,payment_method,status,CHAR_LENGTH(out_trade_no) AS invalid_length FROM vi_payment_order WHERE CHAR_LENGTH(out_trade_no)>32;
SELECT column_name,column_type FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='vi_payment_order' AND column_name LIKE 'prepay_%';
