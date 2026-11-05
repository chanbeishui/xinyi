-- S02 一次售后审核、公开说明和独立审核贡献者归属；前置20261105_02，依迁移序号执行。
-- 执行前备份vi_aftersale_case、vi_aftersale_note、vi_aftersale_review、sys_menu，暂停售后写入。
-- DDL不可重复执行；先检查新增字段/表/索引不存在，不能跳过异常检测继续执行。
-- 历史内部opinion不复制到public_opinion；旧车主补充者account_id原样保留，user_id为空。
-- 历史同人排除只回填已有可信绑定记录及已记录后台贡献者，不猜测历史手机号归属；运行时另核当前可信关系。
-- 回滚应用保留新增列/表与全部审核资金事实，不能删除补充资料或重新执行已预留决定。
-- 菜单不自动授予角色；需要管理员显式分配功能与对应部门资金范围。执行后重启后端，授权后会话正常失效。
SELECT id FROM vi_aftersale_note WHERE account_id IS NULL;
ALTER TABLE vi_aftersale_case ADD COLUMN public_opinion VARCHAR(1000) NULL COMMENT '对申请人公开的审核说明，不包含内部审核意见' AFTER reason_code,
 ADD KEY ix_case_order_history(order_id,id);
ALTER TABLE vi_aftersale_review ADD COLUMN public_opinion VARCHAR(1000) NULL COMMENT '对申请人公开的决定说明，历史无明确公开依据保持NULL' AFTER opinion;
ALTER TABLE vi_aftersale_note MODIFY COLUMN account_id BIGINT NULL COMMENT '小程序贡献者，与后台贡献者互斥',
 ADD COLUMN user_id BIGINT NULL COMMENT '后台实际补充人，与小程序贡献者互斥' AFTER account_id,
 ADD CONSTRAINT ck_aftersale_note_actor CHECK ((account_id IS NOT NULL AND user_id IS NULL) OR (account_id IS NULL AND user_id IS NOT NULL));
CREATE TABLE `vi_aftersale_participant_user` (
  `case_id` BIGINT NOT NULL COMMENT '售后申请，覆盖申请和全部追加贡献者',
  `user_id` BIGINT NOT NULL COMMENT '贡献时已知的可信员工同人关系，撤销绑定后仍排除自审',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '关系冻结UTC时刻',
  PRIMARY KEY (`case_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='追加式售后独立审核排除关系';

INSERT INTO vi_aftersale_participant_user(case_id,user_id)
 SELECT c.id,b.sys_user_id FROM vi_aftersale_case c JOIN vi_employee_binding b ON c.applicant_type='ACCOUNT' AND b.account_id=c.applicant_id
 UNION SELECT c.id,c.applicant_id FROM vi_aftersale_case c WHERE c.applicant_type='SYS_USER'
 UNION SELECT n.case_id,b.sys_user_id FROM vi_aftersale_note n JOIN vi_employee_binding b ON b.account_id=n.account_id
 UNION SELECT n.case_id,n.user_id FROM vi_aftersale_note n WHERE n.user_id IS NOT NULL;

CREATE TEMPORARY TABLE s02_menu_guard (violations INT NOT NULL CHECK(violations=0));
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970800 AND (parent_id<>0 OR COALESCE(perms,'')<>''));
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970800 AND (parent_id<>0 OR COALESCE(perms,'')<>''));
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970800,'取消与退款',0,80,'aftersale',NULL,1,1,'M','0','0','','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970800);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970801 AND (parent_id<>2970800 OR COALESCE(perms,'')<>'biz:aftersale:list')) OR (perms='biz:aftersale:list' AND menu_id<>2970801);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970801 AND (parent_id<>2970800 OR COALESCE(perms,'')<>'biz:aftersale:list')) OR (perms='biz:aftersale:list' AND menu_id<>2970801);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970801,'售后申请',2970800,80,'cases','rongli/aftersale/index',1,1,'C','0','0','biz:aftersale:list','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970801);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970802 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:query')) OR (perms='biz:aftersale:query' AND menu_id<>2970802);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970802 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:query')) OR (perms='biz:aftersale:query' AND menu_id<>2970802);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970802,'查看售后',2970801,80,'#',NULL,1,1,'F','0','0','biz:aftersale:query','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970802);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970803 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:create')) OR (perms='biz:aftersale:create' AND menu_id<>2970803);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970803 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:create')) OR (perms='biz:aftersale:create' AND menu_id<>2970803);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970803,'后台代办售后',2970801,80,'#',NULL,1,1,'F','0','0','biz:aftersale:create','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970803);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970804 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:note')) OR (perms='biz:aftersale:note' AND menu_id<>2970804);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970804 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:note')) OR (perms='biz:aftersale:note' AND menu_id<>2970804);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970804,'补充售后资料',2970801,80,'#',NULL,1,1,'F','0','0','biz:aftersale:note','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970804);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970805 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:reviewpreview')) OR (perms='biz:aftersale:reviewpreview' AND menu_id<>2970805);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970805 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:reviewpreview')) OR (perms='biz:aftersale:reviewpreview' AND menu_id<>2970805);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970805,'预览审核结果',2970801,80,'#',NULL,1,1,'F','0','0','biz:aftersale:reviewpreview','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970805);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970806 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:review')) OR (perms='biz:aftersale:review' AND menu_id<>2970806);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970806 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:aftersale:review')) OR (perms='biz:aftersale:review' AND menu_id<>2970806);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970806,'统一售后审核',2970801,80,'#',NULL,1,1,'F','0','0','biz:aftersale:review','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970806);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970807 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:refund:query')) OR (perms='biz:refund:query' AND menu_id<>2970807);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970807 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:refund:query')) OR (perms='biz:refund:query' AND menu_id<>2970807);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970807,'查看客户退款资金',2970801,80,'#',NULL,1,1,'F','0','0','biz:refund:query','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970807);
SELECT menu_id,parent_id,perms FROM sys_menu WHERE (menu_id=2970808 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:refund:approve')) OR (perms='biz:refund:approve' AND menu_id<>2970808);
INSERT INTO s02_menu_guard SELECT COUNT(*) FROM sys_menu WHERE (menu_id=2970808 AND (parent_id<>2970801 OR COALESCE(perms,'')<>'biz:refund:approve')) OR (perms='biz:refund:approve' AND menu_id<>2970808);
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,remark) SELECT 2970808,'批准客户退款资金',2970801,80,'#',NULL,1,1,'F','0','0','biz:refund:approve','documentation','migration','S02订单权限与收款主体资金权限分别校验；不自动授权' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2970808);
DROP TEMPORARY TABLE s02_menu_guard;

SELECT n.id FROM vi_aftersale_note n LEFT JOIN vi_aftersale_case c ON c.id=n.case_id WHERE c.id IS NULL OR (n.account_id IS NULL)=(n.user_id IS NULL);
SELECT p.case_id,p.user_id FROM vi_aftersale_participant_user p LEFT JOIN vi_aftersale_case c ON c.id=p.case_id LEFT JOIN sys_user u ON u.user_id=p.user_id WHERE c.id IS NULL OR u.user_id IS NULL;
SELECT menu_id,parent_id,perms FROM sys_menu WHERE menu_id BETWEEN 2970800 AND 2970808 ORDER BY menu_id;
SHOW CREATE TABLE vi_aftersale_case;
SHOW CREATE TABLE vi_aftersale_note;
SHOW CREATE TABLE vi_aftersale_review;
SHOW CREATE TABLE vi_aftersale_participant_user;
