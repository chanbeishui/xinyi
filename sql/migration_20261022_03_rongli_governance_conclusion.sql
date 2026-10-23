-- =====================================================================
-- 荣利预约后台：治理台账增加「结论」独立字段
-- 背景：支付渠道核对(B31)需要同时保留「问题类型」与「核对结论」。
--   原实现把结论写回 reason（问题类型），登记一次结论就把问题类型覆盖掉，
--   列表里「问题类型」列会变成结论文本，审计也无法还原原始异常类型。
-- 处理：新增 conclusion 列，登记结论只写 conclusion，reason 保持为问题类型。
-- 幂等：列已存在时跳过。
-- =====================================================================

USE xinyi;

SET @has_col := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'xinyi' AND TABLE_NAME = 'vi_governance_case' AND COLUMN_NAME = 'conclusion'
);
SET @ddl := IF(@has_col = 0,
  'ALTER TABLE vi_governance_case ADD COLUMN conclusion VARCHAR(1000) NULL COMMENT ''人工处置结论，与 reason（问题类型/申请依据）分开保存'' AFTER reason',
  'SELECT ''conclusion 列已存在，跳过'' AS msg');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修正被历史结论覆盖的问题类型（仅限对账案件，且 reason 明显是结论文本）
UPDATE vi_governance_case
SET reason = '关单与支付通知竞态', updated_at = UTC_TIMESTAMP(3), version = version + 1
WHERE case_type = 'RECONCILE'
  AND (reason IS NULL OR reason NOT LIKE '%竞态%')
  AND conclusion IS NULL;

SELECT 'has_conclusion_col' AS obj, CAST(COUNT(*) AS CHAR) AS val FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'xinyi' AND TABLE_NAME = 'vi_governance_case' AND COLUMN_NAME = 'conclusion'
UNION ALL SELECT 'reconcile_cases', CAST(COUNT(*) AS CHAR) FROM vi_governance_case WHERE case_type = 'RECONCILE';
