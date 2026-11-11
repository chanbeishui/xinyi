-- =====================================================================
-- 迁移：报价支持「自由上门时间」，不再必须绑定班次
--
-- 背景：车主在小程序里可以自选上门时间（例如 2026-12-01 10:35），只受检测点
--       营业时间约束，不再从平台放出的 PICKUP 班次里挑。订单表本来就有
--       pickup_starts_at / pickup_ends_at 存实际上门时间，所以只需要让报价也能
--       记录这个时间，并允许 pickup_slot_id 为空。
--
-- 前置版本：2026-11-11 及以后
-- 执行顺序：无依赖，可单独执行
-- 可重复执行：是。MODIFY 本身幂等；ADD COLUMN 先查 information_schema 再决定。
-- 破坏性：无。pickup_slot_id 由 NOT NULL 放宽为 NULL，历史数据不受影响。
-- =====================================================================

-- 1) 允许空班次：自由上门时间不占班次容量
ALTER TABLE vi_quote
    MODIFY COLUMN pickup_slot_id bigint NULL COMMENT '上门班次；自由上门时间时为空';

-- 2) 新增自由上门时间（UTC 存储，与其它时间列一致）
SET @has_pickup_at := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_quote' AND COLUMN_NAME = 'pickup_at'
);
SET @ddl := IF(@has_pickup_at = 0,
    'ALTER TABLE vi_quote ADD COLUMN pickup_at datetime(3) NULL COMMENT ''自由上门时间（UTC）；为空表示按班次'' AFTER pickup_slot_id',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 执行后校验：应看到 pickup_at 存在且 pickup_slot_id 允许 NULL
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_quote'
  AND COLUMN_NAME IN ('pickup_slot_id', 'pickup_at');
