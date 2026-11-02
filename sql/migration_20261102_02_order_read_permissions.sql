-- B04 订单读取菜单与字段权限。前置：V6.4 基线及 migration_20261102_01_order_snapshot.sql。
-- 执行顺序：先备份 sys_menu/sys_role_menu，再执行本脚本，部署后端及前端，重新登录刷新菜单。
-- 可重复执行：只补缺失的同主键/权限菜单，不授予任何角色；冲突先报错，不覆盖现有配置。
-- 本次无业务数据回填，不改变已有角色授权或 auth_version。权限分配仍须统一授权服务。
-- 回滚：停止新版入口，依据备份由统一授权服务撤销新增权限；不删除订单、审计或基础表。
CREATE TEMPORARY TABLE b04_menu_guard (ok INT CHECK(ok=0));
SELECT menu_id,menu_name,perms FROM sys_menu WHERE (menu_id=2970400 AND (COALESCE(perms,'')<>'biz:order:list' OR parent_id<>0)) OR (perms='biz:order:list' AND menu_id<>2970400);
INSERT INTO b04_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970400 AND (COALESCE(perms,'')<>'biz:order:list' OR parent_id<>0)) OR (perms='biz:order:list' AND menu_id<>2970400);
SELECT menu_id,menu_name,perms FROM sys_menu WHERE (menu_id=2970401 AND (COALESCE(perms,'')<>'biz:order:query' OR parent_id<>2970400)) OR (perms='biz:order:query' AND menu_id<>2970401);
INSERT INTO b04_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970401 AND (COALESCE(perms,'')<>'biz:order:query' OR parent_id<>2970400)) OR (perms='biz:order:query' AND menu_id<>2970401);
SELECT menu_id,menu_name,perms FROM sys_menu WHERE (menu_id=2970402 AND (COALESCE(perms,'')<>'biz:order:finance' OR parent_id<>2970400)) OR (perms='biz:order:finance' AND menu_id<>2970402);
INSERT INTO b04_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970402 AND (COALESCE(perms,'')<>'biz:order:finance' OR parent_id<>2970400)) OR (perms='biz:order:finance' AND menu_id<>2970402);
SELECT menu_id,menu_name,perms FROM sys_menu WHERE (menu_id=2970403 AND (COALESCE(perms,'')<>'biz:order:address' OR parent_id<>2970400)) OR (perms='biz:order:address' AND menu_id<>2970403);
INSERT INTO b04_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970403 AND (COALESCE(perms,'')<>'biz:order:address' OR parent_id<>2970400)) OR (perms='biz:order:address' AND menu_id<>2970403);
DROP TEMPORARY TABLE b04_menu_guard;
START TRANSACTION;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970400,'客户订单',0,40,'rongli-orders','rongli/order/order',1,1,'C','0','0','biz:order:list','list','migration','B04四端订单权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970400);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970401,'订单详情',2970400,40,'#',NULL,1,1,'F','0','0','biz:order:query','list','migration','B04四端订单权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970401);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970402,'订单客户金额',2970400,40,'#',NULL,1,1,'F','0','0','biz:order:finance','list','migration','B04四端订单权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970402);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970403,'订单精确地址',2970400,40,'#',NULL,1,1,'F','0','0','biz:order:address','list','migration','B04四端订单权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970403);
COMMIT;
-- 执行后必须为4行，且每个权限仅1行。无默认角色授权。
SELECT menu_id,menu_name,parent_id,component,perms FROM sys_menu WHERE menu_id BETWEEN 2970400 AND 2970403 ORDER BY menu_id;
SELECT perms,COUNT(*) AS copies FROM sys_menu WHERE perms IN ('biz:order:list','biz:order:query','biz:order:finance','biz:order:address') GROUP BY perms;
