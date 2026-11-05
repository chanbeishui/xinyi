-- F01 私有文件上传者双域、对象归属及上传租约；前置20261104_03，完成既有迁移后执行。
-- 执行前备份vi_file并暂停文件写入口。DDL不可重复执行；不删除历史文件或猜测历史归属。
-- 旧记录保留uploader_account_id；新增声明/租约字段NULL，禁止为旧UPLOADING记录伪造新签名。
-- 回滚应用时保留新增列；后台新文件的uploader_account_id可能NULL，旧代码不能重新启用文件写入口。
-- 部署后配置私有目录、签名密钥/可信访问域名并重启后端，无需清缓存。
SELECT id FROM vi_file WHERE uploader_account_id IS NULL;
ALTER TABLE vi_file
 MODIFY COLUMN uploader_account_id BIGINT NULL COMMENT '小程序上传者；与后台上传者互斥',
 ADD COLUMN `uploader_user_id` BIGINT NULL COMMENT '后台上传者；与小程序上传者互斥' AFTER `uploader_account_id`,
 ADD COLUMN `station_id` BIGINT NULL COMMENT '站点资质等文件的权威站点归属' AFTER `uploader_user_id`,
 ADD COLUMN `declared_size` BIGINT NULL COMMENT '客户端声明大小，只用于与实际字节核对' AFTER `station_id`,
 ADD COLUMN `declared_mime` VARCHAR(64) NULL COMMENT '客户端声明类型，READY以服务端识别为准' AFTER `declared_size`,
 ADD COLUMN `upload_expires_at` DATETIME(3) NULL COMMENT '原上传授权截止UTC' AFTER `declared_mime`,
 ADD COLUMN `upload_lease_token` VARCHAR(36) NULL COMMENT '上传租约标识，禁止向客户端或日志输出' AFTER `upload_expires_at`,
 ADD COLUMN `upload_lease_until` DATETIME(3) NULL COMMENT '上传租约截止，文件IO不持数据库事务' AFTER `upload_lease_token`,
 ADD CONSTRAINT ck_file_uploader CHECK ((uploader_account_id IS NOT NULL AND uploader_user_id IS NULL) OR (uploader_account_id IS NULL AND uploader_user_id IS NOT NULL));
-- 回填：既有小程序上传者保持原值，其余新增列保持NULL；旧文件不猜测后台上传者或站点。
SELECT id FROM vi_file WHERE (uploader_account_id IS NULL)=(uploader_user_id IS NULL);
SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_file' ORDER BY ORDINAL_POSITION;
SHOW CREATE TABLE vi_file;

-- F01文件功能权限；仅登记，不自动向既有角色授权。ID/权限冲突必须报告并停止。
CREATE TEMPORARY TABLE f01_menu_guard (bad INT CHECK(bad=0));
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970700 AND (parent_id<>0 OR COALESCE(perms,'')<>''));
INSERT INTO f01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970700 AND (parent_id<>0 OR COALESCE(perms,'')<>''));
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970700,'私有文件权限',0,70,'private-files',NULL,1,1,'M','1','0','','system','migration','F01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970700);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970701 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:uploads:create')) OR (perms='biz:uploads:create' AND menu_id<>2970701);
INSERT INTO f01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970701 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:uploads:create')) OR (perms='biz:uploads:create' AND menu_id<>2970701);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970701,'申请私有上传',2970700,70,'#',NULL,1,1,'F','1','0','biz:uploads:create','system','migration','F01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970701);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970702 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:uploads:complete')) OR (perms='biz:uploads:complete' AND menu_id<>2970702);
INSERT INTO f01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970702 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:uploads:complete')) OR (perms='biz:uploads:complete' AND menu_id<>2970702);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970702,'完成私有上传',2970700,70,'#',NULL,1,1,'F','1','0','biz:uploads:complete','system','migration','F01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970702);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970703 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:files:query')) OR (perms='biz:files:query' AND menu_id<>2970703);
INSERT INTO f01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970703 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:files:query')) OR (perms='biz:files:query' AND menu_id<>2970703);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970703,'查看文件状态',2970700,70,'#',NULL,1,1,'F','1','0','biz:files:query','system','migration','F01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970703);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970704 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:files:access')) OR (perms='biz:files:access' AND menu_id<>2970704);
INSERT INTO f01_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970704 AND (parent_id<>2970700 OR COALESCE(perms,'')<>'biz:files:access')) OR (perms='biz:files:access' AND menu_id<>2970704);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970704,'查看私有凭证',2970700,70,'#',NULL,1,1,'F','1','0','biz:files:access','system','migration','F01服务端权限；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970704);
DROP TEMPORARY TABLE f01_menu_guard;
SELECT menu_id,parent_id,perms FROM sys_menu WHERE menu_id BETWEEN 2970700 AND 2970704 ORDER BY menu_id;
