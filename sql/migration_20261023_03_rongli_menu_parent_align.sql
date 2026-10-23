-- =============================================================================
-- 侧栏分组归属对齐（V6.4 巡检时发现的漂移）
--
-- 现象：全新库按 sql/migration_20261022_01_rongli_menu_groups.sql 建出来的
--       2122「收车与核验」、2124「检测结果」挂在 2192（巡检组）下；
--       而本机长期使用的库里这两条挂在 2190（业务组）下。
-- 原因：2190–2193 四个分组落库后，这两条菜单被手工移动过，没有任何迁移脚本记录该变更，
--       于是"开发库"与"新装库"分组不一致，ci_sql_check.sh 的可见页断言也对不上。
-- 处理：以迁移脚本为准，把这两条改回 2192；断言同步收敛为 20。
--
-- 幂等：先查当前父级，已经是目标值就不动。
-- =============================================================================

UPDATE `sys_menu` SET `parent_id` = 2192, `order_num` = 1
 WHERE `menu_id` = 2122 AND `parent_id` <> 2192;

UPDATE `sys_menu` SET `parent_id` = 2192, `order_num` = 2
 WHERE `menu_id` = 2124 AND `parent_id` <> 2192;

-- ==================================================== 断言
SELECT 'business_items' AS obj, COUNT(*) AS cnt FROM `sys_menu` WHERE `parent_id` = 2190
UNION ALL SELECT 'people_items',   COUNT(*) FROM `sys_menu` WHERE `parent_id` = 2191
UNION ALL SELECT 'inspect_items',  COUNT(*) FROM `sys_menu` WHERE `parent_id` = 2192
UNION ALL SELECT 'runtime_items',  COUNT(*) FROM `sys_menu` WHERE `parent_id` = 2193
UNION ALL SELECT 'visible_pages',  COUNT(*) FROM `sys_menu`
  WHERE `parent_id` IN (2190,2191,2192,2193) AND `menu_type` = 'C' AND `visible` = '0';
