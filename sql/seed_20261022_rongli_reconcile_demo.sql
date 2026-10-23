-- =====================================================================
-- 荣利预约后台：支付渠道核对(B31) 演示数据
-- 目的：B31 页面需要「待核实记录」才能演示查询原交易 / 核对退款两条操作链路。
-- 数据来源真实异常，不是凭空编造：
--   vi_order.fulfillment_status = 'CLOSED' 且 vi_payment_order.status = 'SUCCESS'
--   即「订单已关闭但渠道支付成功」的关单与支付通知竞态，必须人工核对。
-- 幂等：同一订单已存在 RECONCILE 案件时不重复插入。
-- =====================================================================

USE xinyi;

INSERT INTO vi_governance_case
  (case_type, order_id, requester_account_id, requester_user_id, status, payload_cipher, reason,
   expires_at, created_at, updated_at, version)
SELECT 'RECONCILE', o.id, NULL, NULL, 'OPEN', NULL, '关单与支付通知竞态',
       NULL, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0
FROM vi_order o
JOIN vi_payment_order p ON p.order_id = o.id
WHERE o.fulfillment_status = 'CLOSED'
  AND p.status = 'SUCCESS'
  AND NOT EXISTS (SELECT 1 FROM vi_governance_case g
                  WHERE g.case_type = 'RECONCILE' AND g.order_id = o.id)
ORDER BY o.id
LIMIT 2;

SELECT 'reconcile_cases' AS obj, CAST(COUNT(*) AS CHAR) AS val
FROM vi_governance_case WHERE case_type = 'RECONCILE';
