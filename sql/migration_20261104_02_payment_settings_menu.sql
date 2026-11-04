-- X02 支付设置/微信商户管理菜单；前置V6.4及20261104_01_payment_prepay。
-- 先备份 sys_menu/sys_role_menu，再执行本脚本，再部署并重启后端，重新登录刷新菜单。
-- 可重复执行；只调整既有X01权限的展示入口，不增加角色授权、不改资金数据。
-- 发现主键/权限归属冲突时报告并拒绝。回滚应用时保留权限记录，可将菜单visible改回1隐藏入口。
CREATE TEMPORARY TABLE x02_menu_guard(ok INT CHECK(ok=0));
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970500 AND (menu_type<>'M' OR parent_id<>0 OR COALESCE(perms,'')<>'')) OR (menu_id=2970501 AND (perms<>'biz:integration:query' OR parent_id<>2970500)) OR (perms='biz:integration:query' AND menu_id<>2970501);
INSERT INTO x02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970500 AND (menu_type<>'M' OR parent_id<>0 OR COALESCE(perms,'')<>'')) OR (menu_id=2970501 AND (perms<>'biz:integration:query' OR parent_id<>2970500)) OR (perms='biz:integration:query' AND menu_id<>2970501);
INSERT INTO x02_menu_guard SELECT IF(COUNT(*)=2,0,1) FROM sys_menu WHERE menu_id IN (2970500,2970501);
DROP TEMPORARY TABLE x02_menu_guard;
START TRANSACTION;
UPDATE sys_menu SET visible='0' WHERE menu_id=2970500;
UPDATE sys_menu SET menu_name='支付与微信商户',menu_type='C',path='payment',component='rongli/integrations/payment',visible='0',is_cache=1,remark='X02支付/商户/小程序配置入口，原查询权限，不自动授权' WHERE menu_id=2970501;
COMMIT;
SELECT menu_id,parent_id,menu_name,menu_type,path,component,visible,perms FROM sys_menu WHERE menu_id IN (2970500,2970501);
SELECT perms,COUNT(*) AS copies FROM sys_menu WHERE perms='biz:integration:query' GROUP BY perms;
