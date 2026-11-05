-- ================================================================
-- 修复：后台菜单路由名冲突导致页面 404
--
-- 现象：访问 /rongli/inspect/config/wechatPay（系统设置-微信支付）返回 404，
--       菜单能点，点进去是「找不到网页」。
--
-- 根因：SysMenuServiceImpl.getRouteName() 在 route_name 为空时，
--       用 path 首字母大写作为路由名（getRouteName(menu.getRouteName(), menu.getPath())）。
--       path 只在同一父级下唯一，跨层级很容易撞名：
--         2113 rongli/inspect/config      -> Config    与 106 system/config -> Config 撞
--         2141 rongli/inspect/config/wechatPay -> WechatPay
--                                         与 2242 rongli/integration/wechatPay -> WechatPay 撞
--       Vue Router 4 的 router.addRoute() 遇到同名路由会先移除已注册的同名记录，
--       所以后注册的 /rongli/integration/wechatPay 把 /rongli/inspect/config/wechatPay 挤掉了，
--       路由表里没有这条记录 -> 落到 404 通配路由。
--
-- 修法：给冲突菜单显式指定唯一 route_name。只改路由名，不改 path，URL 全部保持不变。
-- ================================================================

UPDATE `sys_menu` SET `route_name` = 'RongliInspectConfig' WHERE `menu_id` = 2113;
UPDATE `sys_menu` SET `route_name` = 'RongliConfigWechatPay' WHERE `menu_id` = 2141;
UPDATE `sys_menu` SET `route_name` = 'RongliIntegrationWechatPay' WHERE `menu_id` = 2242;

-- 自检：仍存在生效路由名重复的菜单。应返回 0 行。
-- 生效名 = route_name 非空时取 route_name，否则取 path 首字母大写（与后端 getRouteName 一致）。
SELECT `menu_id`, `path`, `route_name`, `eff_name`
  FROM (
        SELECT `menu_id`, `path`, `route_name`,
               CASE WHEN `route_name` IS NULL OR `route_name` = ''
                    THEN CONCAT(UPPER(LEFT(`path`, 1)), SUBSTRING(`path`, 2))
                    ELSE `route_name` END AS `eff_name`
          FROM `sys_menu`
         WHERE `menu_type` IN ('M', 'C')
           AND `status` = '0'
       ) t
 WHERE `eff_name` IN (
        SELECT `eff_name` FROM (
          SELECT CASE WHEN `route_name` IS NULL OR `route_name` = ''
                      THEN CONCAT(UPPER(LEFT(`path`, 1)), SUBSTRING(`path`, 2))
                      ELSE `route_name` END AS `eff_name`
            FROM `sys_menu`
           WHERE `menu_type` IN ('M', 'C')
             AND `status` = '0'
        ) x GROUP BY `eff_name` HAVING COUNT(*) > 1
       )
 ORDER BY `eff_name`, `menu_id`;

SELECT 'migration_20261103_01 done' AS result;
