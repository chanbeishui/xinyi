-- X01 校验根版本绑定与第三方服务权限。
-- 前置：V6.4 基线、20261102_01 与 20261102_02；先执行旧迁移，再本脚本，再部署新版后端。
-- 备份：vi_integration_check、sys_menu、sys_role_menu。仅增加可空列，不删除/猜测回填历史数据。
-- 不允许重复执行 ALTER；菜单部分可重复执行，主键/权限冲突时报错。
-- 旧校验 target_version=NULL：历史发布继续可追溯；新发布必须重新校验。
-- 回退：回退应用并保留新增列及历史记录；角色授权由统一授权服务处理，不直接删除审计。
ALTER TABLE vi_integration_check ADD COLUMN `target_version` BIGINT NULL COMMENT '校验时根版本；旧记录NULL仅保留历史证据不可授权新发布' AFTER revision_id;
-- X01 第三方服务权限；无默认角色授权，页面由后续任务接入。
CREATE TEMPORARY TABLE x01_menu_guard(ok INT CHECK(ok=0));
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970500 AND (COALESCE(perms,'')<>'' OR parent_id<>0));
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970500 AND (COALESCE(perms,'')<>'' OR parent_id<>0));
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970501 AND (COALESCE(perms,'')<>'biz:integration:query' OR parent_id<>2970500)) OR (perms='biz:integration:query' AND menu_id<>2970501);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970501 AND (COALESCE(perms,'')<>'biz:integration:query' OR parent_id<>2970500)) OR (perms='biz:integration:query' AND menu_id<>2970501);
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970502 AND (COALESCE(perms,'')<>'biz:integration:create' OR parent_id<>2970500)) OR (perms='biz:integration:create' AND menu_id<>2970502);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970502 AND (COALESCE(perms,'')<>'biz:integration:create' OR parent_id<>2970500)) OR (perms='biz:integration:create' AND menu_id<>2970502);
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970503 AND (COALESCE(perms,'')<>'biz:integration:edit' OR parent_id<>2970500)) OR (perms='biz:integration:edit' AND menu_id<>2970503);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970503 AND (COALESCE(perms,'')<>'biz:integration:edit' OR parent_id<>2970500)) OR (perms='biz:integration:edit' AND menu_id<>2970503);
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970504 AND (COALESCE(perms,'')<>'biz:integration:check' OR parent_id<>2970500)) OR (perms='biz:integration:check' AND menu_id<>2970504);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970504 AND (COALESCE(perms,'')<>'biz:integration:check' OR parent_id<>2970500)) OR (perms='biz:integration:check' AND menu_id<>2970504);
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970505 AND (COALESCE(perms,'')<>'biz:integration:publish' OR parent_id<>2970500)) OR (perms='biz:integration:publish' AND menu_id<>2970505);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970505 AND (COALESCE(perms,'')<>'biz:integration:publish' OR parent_id<>2970500)) OR (perms='biz:integration:publish' AND menu_id<>2970505);
SELECT menu_id,perms,parent_id FROM sys_menu WHERE (menu_id=2970506 AND (COALESCE(perms,'')<>'biz:integration:testrecipient' OR parent_id<>2970500)) OR (perms='biz:integration:testrecipient' AND menu_id<>2970506);
INSERT INTO x01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970506 AND (COALESCE(perms,'')<>'biz:integration:testrecipient' OR parent_id<>2970500)) OR (perms='biz:integration:testrecipient' AND menu_id<>2970506);
DROP TEMPORARY TABLE x01_menu_guard;
START TRANSACTION;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970500,'第三方服务',0,50,'integrations',NULL,1,1,'M','1','0','','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970500);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970501,'配置查询',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:query','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970501);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970502,'创建配置',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:create','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970502);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970503,'保存草稿',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:edit','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970503);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970504,'配置校验',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:check','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970504);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970505,'配置发布回滚',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:publish','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970505);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970506,'测试白名单维护',2970500,50,'#',NULL,1,1,'F','1','0','biz:integration:testrecipient','system','migration','X01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970506);
COMMIT;
SELECT menu_id,parent_id,perms FROM sys_menu WHERE menu_id BETWEEN 2970500 AND 2970506 ORDER BY menu_id;
SELECT id,target_id,revision_id,target_version FROM vi_integration_check WHERE target_version IS NULL;
SELECT column_name,column_type,is_nullable FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='vi_integration_check' AND column_name='target_version';
