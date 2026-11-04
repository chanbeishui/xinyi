-- P02 支付关单意图及查单调度；前置V6.4、20261104_01、20261104_02，按此顺序执行。
-- 先备份vi_payment_order；暂停支付接口及支付扫描任务后执行，再部署并重启后端。
-- DDL不可重复执行，不删除/改写历史资金数据；现有行新增字段NULL，无需猜测回填。
-- 回滚应用时保留新增列；不得清除已记录的关单意图。无需清理缓存。
ALTER TABLE vi_payment_order
 ADD COLUMN close_requested_at DATETIME(3) NULL COMMENT '首次未付关单意图，写入后禁止再次预支付',
 ADD COLUMN next_query_at DATETIME(3) NULL COMMENT '数据库UTC查单/关单租约或重试时点',
 ADD KEY ix_payment_reconcile(status,next_query_at,expires_at);
SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME IN ('close_requested_at','next_query_at');
SELECT id,order_id,status FROM vi_payment_order WHERE close_requested_at IS NOT NULL AND status NOT IN ('PENDING','UNKNOWN','SUCCESS','CLOSED','REVIEW_PENDING');
SHOW INDEX FROM vi_payment_order WHERE Key_name='ix_payment_reconcile';
