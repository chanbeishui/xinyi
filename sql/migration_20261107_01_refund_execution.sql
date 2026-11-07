-- P04 退款租约/可信核验与永久对账异常；前置20261105_03，按日期和序号顺序执行。
-- 执行前备份vi_refund_order及sys_menu，暂停退款入口、通知与退款任务，确认新增列/表不存在。
-- DDL不可重复执行。旧CREATED未发起记录只保留原号；旧PROCESSING/UNKNOWN/ABNORMAL因可能已请求不允许再次POST。
-- 回滚应用保留全部新增列/表、资金事实及对账历史；不得删除异常或恢复已消费预留。
-- 自动执行默认关闭，迁移后重启后端；无生产启用或真实退款授权，不自动授予角色或清缓存。
ALTER TABLE vi_refund_order
    ADD COLUMN `execution_started_at` DATETIME(3) NULL COMMENT '首次退款发起意图UTC，一旦存在仅查原号' AFTER `updated_at`,
    ADD COLUMN `execution_lease_token` VARCHAR(36) NULL COMMENT '执行租约标识，不向客户端或日志输出' AFTER `execution_started_at`,
    ADD COLUMN `execution_lease_until` DATETIME(3) NULL COMMENT '执行租约截止UTC' AFTER `execution_lease_token`,
    ADD COLUMN `execution_attempts` INT NOT NULL DEFAULT 0 COMMENT '提交与查询尝试计数' AFTER `execution_lease_until`,
    ADD COLUMN `last_error_code` VARCHAR(64) NULL COMMENT '脱敏稳定错误码，不含SDK异常或凭据' AFTER `execution_attempts`,
    ADD COLUMN `last_verified_status` VARCHAR(24) NULL COMMENT '最近可信渠道状态，不采信客户端' AFTER `last_error_code`,
    ADD COLUMN `last_verified_at` DATETIME(3) NULL COMMENT '最近可信渠道核对UTC时刻' AFTER `last_verified_status`,
    ADD CONSTRAINT ck_refund_execution_attempts CHECK(execution_attempts>=0);
CREATE TABLE `vi_payment_reconciliation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '独立对账异常ID，对外caseId十进制字符串',
  `kind` VARCHAR(16) NOT NULL COMMENT 'PAYMENT或REFUND',
  `subject_id` BIGINT NOT NULL COMMENT '对应支付记录或退款记录ID，按kind解释',
  `order_id` BIGINT NOT NULL COMMENT '原订单',
  `payment_id` BIGINT NOT NULL COMMENT '原支付记录，权威收款主体从此加载',
  `status` VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN,CHECKING,RESOLVED',
  `local_status` VARCHAR(24) NOT NULL COMMENT '最近本地状态快照',
  `channel_status` VARCHAR(24) NULL COMMENT '最近可信渠道状态，未知为NULL',
  `reason_code` VARCHAR(64) NOT NULL COMMENT '受控异常原因，不存自由文本或SDK报文',
  `verified_at` DATETIME(3) NULL COMMENT '最近可信渠道核对UTC',
  `resolved_by` BIGINT NULL COMMENT '已核实一致后登记结案的后台用户',
  `resolved_at` DATETIME(3) NULL COMMENT '结案UTC，异常重开时清空',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_reconciliation_subject (`kind`,`subject_id`),
  KEY ix_reconciliation_status (`status`,`updated_at`,`id`),
  CONSTRAINT ck_reconciliation_kind CHECK (`kind` IN ('PAYMENT','REFUND')),
  CONSTRAINT ck_reconciliation_status CHECK (`status` IN ('OPEN','CHECKING','RESOLVED')),
  CONSTRAINT ck_reconciliation_version CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='原支付主体下永久对账异常，不删除历史，原case可重开';
-- 已有渠道状态说明曾执行过，保守回填首次发起时刻为已有创建时间；不猜测渠道结果。
UPDATE vi_refund_order
SET execution_started_at=created_at
WHERE status IN ('PROCESSING','UNKNOWN','ABNORMAL','SUCCESS','CLOSED')
    AND execution_started_at IS NULL;
INSERT INTO vi_payment_reconciliation(
    kind,subject_id,order_id,payment_id,local_status,reason_code
)
SELECT 'PAYMENT',p.id,p.order_id,p.id,p.status,
    CASE
        WHEN p.capacity_resolution='MANUAL_CONFLICT' THEN 'CAPACITY_CONFLICT'
        ELSE 'PAYMENT_UNKNOWN'
    END
FROM vi_payment_order p
WHERE p.status='UNKNOWN'
    OR p.capacity_resolution='MANUAL_CONFLICT';
INSERT INTO vi_payment_reconciliation(
    kind,subject_id,order_id,payment_id,local_status,reason_code
)
SELECT 'REFUND',r.id,p.order_id,p.id,r.status,CONCAT('REFUND_',r.status)
FROM vi_refund_order r
JOIN vi_payment_order p ON p.id=r.payment_id
WHERE r.status IN ('UNKNOWN','ABNORMAL','CLOSED');
CREATE TEMPORARY TABLE p04_menu_guard (
    violations INT NOT NULL CHECK(violations=0)
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970900
        AND (parent_id<>0
            OR COALESCE(perms,'')<>''));

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970900
        AND (parent_id<>0
            OR COALESCE(perms,'')<>''));

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970900,'客户支付与退款',0,90,'payment-finance',
    NULL,1,1,'M','0',
    '0','','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970900
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970901
        AND (parent_id<>2970900
            OR COALESCE(perms,'')<>''));

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970901
        AND (parent_id<>2970900
            OR COALESCE(perms,'')<>''));

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970901,'渠道退款',2970900,90,'refunds',
    'rongli/refund/index',1,1,'C','0',
    '0','','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970901
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970902
        AND (parent_id<>2970900
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:list'))
    OR (perms='biz:paymentreconciliation:list'
        AND menu_id<>2970902);

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970902
        AND (parent_id<>2970900
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:list'))
    OR (perms='biz:paymentreconciliation:list'
        AND menu_id<>2970902);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970902,'支付对账异常',2970900,90,'reconciliation',
    'rongli/reconciliation/index',1,1,'C','0',
    '0','biz:paymentreconciliation:list','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970902
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970903
        AND (parent_id<>2970901
            OR COALESCE(perms,'')<>'biz:refund:retry'))
    OR (perms='biz:refund:retry'
        AND menu_id<>2970903);

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970903
        AND (parent_id<>2970901
            OR COALESCE(perms,'')<>'biz:refund:retry'))
    OR (perms='biz:refund:retry'
        AND menu_id<>2970903);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970903,'安全重试退款',2970901,90,'#',
    NULL,1,1,'F','0',
    '0','biz:refund:retry','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970903
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970904
        AND (parent_id<>2970902
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:query'))
    OR (perms='biz:paymentreconciliation:query'
        AND menu_id<>2970904);

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970904
        AND (parent_id<>2970902
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:query'))
    OR (perms='biz:paymentreconciliation:query'
        AND menu_id<>2970904);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970904,'重查对账渠道',2970902,90,'#',
    NULL,1,1,'F','0',
    '0','biz:paymentreconciliation:query','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970904
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2970905
        AND (parent_id<>2970902
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:resolve'))
    OR (perms='biz:paymentreconciliation:resolve'
        AND menu_id<>2970905);

INSERT INTO p04_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2970905
        AND (parent_id<>2970902
            OR COALESCE(perms,'')<>'biz:paymentreconciliation:resolve'))
    OR (perms='biz:paymentreconciliation:resolve'
        AND menu_id<>2970905);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,
    component,is_frame,is_cache,menu_type,visible,
    status,perms,icon,create_by,remark
)
SELECT
    2970905,'核实对账结案',2970902,90,'#',
    NULL,1,1,'F','0',
    '0','biz:paymentreconciliation:resolve','money','migration','P04资金数据归属原收款主体；不自动授权'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2970905
);

DROP TEMPORARY TABLE p04_menu_guard;
SELECT r.id
FROM vi_refund_order r
LEFT JOIN vi_payment_order p ON p.id=r.payment_id
LEFT JOIN vi_aftersale_case c ON c.id=r.case_id
WHERE p.id IS NULL
    OR c.id IS NULL
    OR c.payment_id<>r.payment_id;
SELECT c.id
FROM vi_payment_reconciliation c
LEFT JOIN vi_payment_order p ON p.id=c.payment_id
WHERE p.id IS NULL
    OR p.order_id<>c.order_id;
SHOW CREATE TABLE vi_refund_order;
SHOW CREATE TABLE vi_payment_reconciliation;
SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE menu_id BETWEEN 2970900 AND 2970905
ORDER BY menu_id;
