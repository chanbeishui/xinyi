-- =====================================================================
-- 荣利预约后台：业务统计(B23) / 支付渠道核对(B31) 权限补齐
-- 背景：2130/2131 两个菜单在 migration_20261022_01 中已建，但存在两处硬伤：
--   1) 2131 权限标识写成 biz:reconciliation:list，后端实际校验 biz:paymentreconciliation:list，
--      非超管角色会 403；
--   2) 设计稿要求的「查询原交易」「核对退款」「申请导出」三个操作没有任何按钮权限，
--      角色无法按岗位授予。
-- 本脚本只做修正与补充，不改动既有菜单层级与排序。
-- =====================================================================

USE xinyi;

-- ---------------------------------------------------------------- 一、权限标识修正
UPDATE sys_menu SET perms = 'biz:paymentreconciliation:list', update_time = sysdate()
WHERE menu_id = 2131 AND perms <> 'biz:paymentreconciliation:list';

-- ---------------------------------------------------------------- 二、页面操作按钮
DELETE FROM sys_menu WHERE menu_id BETWEEN 2233 AND 2235;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 (2233, '查询原交易', 2131, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'biz:paymentreconciliation:query',   '#', 'admin', sysdate(), '', NULL, '只重查渠道状态，不改金额与单据状态'),
 (2234, '核对退款',   2131, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'biz:paymentreconciliation:resolve', '#', 'admin', sysdate(), '', NULL, '人工登记核对结论，必须写明依据'),
 (2235, '申请导出',   2130, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'biz:order:export',                   '#', 'admin', sysdate(), '', NULL, '按统计范围提交脱敏导出申请，需他人审批')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
  menu_type = VALUES(menu_type), visible = VALUES(visible), status = VALUES(status),
  perms = VALUES(perms), remark = VALUES(remark), update_time = sysdate();

-- ---------------------------------------------------------------- 三、运营角色授予
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_id BETWEEN 2233 AND 2235;

-- ---------------------------------------------------------------- 四、校验
SELECT 'menu_2131_perms' AS obj, perms AS val FROM sys_menu WHERE menu_id = 2131
UNION ALL SELECT 'btn_2131', CAST(COUNT(*) AS CHAR) FROM sys_menu WHERE parent_id = 2131 AND menu_type = 'F'
UNION ALL SELECT 'btn_2130', CAST(COUNT(*) AS CHAR) FROM sys_menu WHERE parent_id = 2130 AND menu_type = 'F'
UNION ALL SELECT 'role2_btn', CAST(COUNT(*) AS CHAR) FROM sys_role_menu WHERE role_id = 2 AND menu_id BETWEEN 2233 AND 2235;
