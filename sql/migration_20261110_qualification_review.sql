-- Q05 资格审批、不可变生效版本与到期复审；前置20261109_03本机认证/双人人工核验。
-- 先独立备份vi_driver_profile、vi_driver_application_submission、sys_menu及SHOW CREATE TABLE，暂停资格提交/审核写入。
-- 执行顺序：输出历史未可信快照→前向加列/索引→两张不可变事实表→稳定菜单→核验→重启后端。
-- DDL不可重复执行，含隐式提交；菜单冲突失败，不能忽略错误继续发布。新环境使用最新完整初始化，不叠加本增量。
-- 历史提交缺epoch/session/申请人身份/认证封装保持NULL，不猜回填为可信；不得从旧PASSED或APPROVED伪造新审核/生效快照。
-- 历史资格和在手承接、占用、车辆及资金不改不删；旧生效快照指针NULL保留历史入口，不凭此新授资格。
-- 回滚先停用新审批/到期入口，保留真实审核、快照和新列；不可删除历史以恢复旧流程。无列缩窄/大批量重写。
-- 审核员每次明确填写复审截止，不根据样例自动一年或提前30天；到期实时准入不依赖定时任务准时。
SELECT id,application_id,submission_revision,owner_dept_id FROM vi_driver_application_submission ORDER BY id;
SELECT id,qualification_status,qualification_revision FROM vi_driver_profile WHERE qualification_status IN ('APPROVED','EXPIRED','SUSPENDED') ORDER BY id;

ALTER TABLE vi_driver_profile
    ADD COLUMN `effective_snapshot_id` BIGINT NULL COMMENT '当前生效资格不可变快照；历史NULL不猜回填',
    MODIFY COLUMN review_due_at DATETIME(3) COMMENT '审核员明确的UTC复审截止；不默认年度周期',
    ADD KEY ix_driver_qualification_expiry (qualification_status,effective_snapshot_id);

ALTER TABLE vi_driver_application_submission
    ADD COLUMN `company_revision` BIGINT NULL COMMENT '提交时专用公司归属epoch；历史NULL不可信',
    ADD COLUMN `source_session_id` BIGINT NULL COMMENT '提交时实际MINI登录会话；审核不要求其仍在线',
    ADD COLUMN `applicant_user_ids` JSON NULL COMMENT '服务端冻结已知申请人后台身份；换号不抹去自审约束',
    ADD COLUMN `submission_seal` MEDIUMBLOB NULL COMMENT '取得数据库ID后同事务一次初始化的提交认证封装；历史NULL不可批准';

CREATE TABLE `vi_qualification_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成不可变审核事实主键',
  `application_id` BIGINT NOT NULL COMMENT '真实资格申请',
  `driver_id` BIGINT NOT NULL COMMENT '真实父代驾档案',
  `owner_dept_id` BIGINT NOT NULL COMMENT '审核原公司归属；历史修复继承显式父档案公司',
  `company_revision` BIGINT NOT NULL COMMENT '审核对应的专用公司归属epoch',
  `submission_id` BIGINT NULL COMMENT '可信提交快照；仅历史驳回结构修复可空',
  `submission_revision` BIGINT NOT NULL COMMENT '本次明确提交修订，历史修复可为0',
  `kind` VARCHAR(32) NOT NULL COMMENT 'QualificationReviewKind: DECISION,REPAIR_REJECTION',
  `decision` VARCHAR(32) NOT NULL COMMENT 'QualificationReviewDecision: APPROVE,REJECT,REPAIR_REJECTION',
  `reviewer_user_id` BIGINT NOT NULL COMMENT '真实ADMIN审核人，禁止已知申请人本人',
  `reviewed_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '服务器审核时刻UTC',
  `internal_opinion_cipher` MEDIUMBLOB NULL COMMENT '取得ID后一次AAD加密内部意见；不返回GET',
  `public_opinion` VARCHAR(1000) NULL COMMENT '可向本人公开的审核说明；与内部意见分离',
  `reject_fields` JSON NOT NULL COMMENT '明确补正的材料字段；批准必须空集合',
  `application_version` BIGINT NOT NULL COMMENT '审核前明确申请CAS',
  `profile_version` BIGINT NOT NULL COMMENT '审核前明确档案CAS',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_qualification_review_target (application_id,submission_revision,kind),
  KEY ix_qualification_review_company (owner_dept_id,application_id,id),
  CONSTRAINT ck_qualification_review_kind CHECK (`kind` IN ('DECISION','REPAIR_REJECTION')),
  CONSTRAINT ck_qualification_review_decision CHECK (`decision` IN ('APPROVE','REJECT','REPAIR_REJECTION')),
  CONSTRAINT ck_qualification_review_versions CHECK (submission_revision>=0 AND company_revision>=0 AND application_version>=0 AND profile_version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格不可变审核历史；内部意见不对外返回';

CREATE TABLE `vi_qualification_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成生效资格快照主键',
  `driver_id` BIGINT NOT NULL COMMENT '真实父代驾档案',
  `account_id` BIGINT NOT NULL COMMENT '真实MINI申请人账号',
  `application_id` BIGINT NOT NULL COMMENT '实际批准的申请',
  `submission_id` BIGINT NOT NULL COMMENT '实际批准的可信提交快照',
  `review_id` BIGINT NOT NULL COMMENT '唯一实际批准审核事实',
  `qualification_revision` BIGINT NOT NULL COMMENT '司机专用生效资格单调版本',
  `owner_dept_id` BIGINT NOT NULL COMMENT '批准时真实公司',
  `company_revision` BIGINT NOT NULL COMMENT '批准时专用公司归属epoch',
  `material_revision` BIGINT NOT NULL COMMENT '批准的实际材料修订',
  `material_hash` BINARY(32) NOT NULL COMMENT '批准材料有密钥摘要',
  `license_class` VARCHAR(64) NOT NULL COMMENT '冻结实际准驾类别，不以字母排序判断车型',
  `license_valid_to` DATETIME(3) NULL COMMENT '上海证件到期日00:00转换UTC；到期当日仍有效',
  `license_long_term` TINYINT NOT NULL COMMENT '是否明确长期有效；不得把NULL期限当永久',
  `review_due_at` DATETIME(3) NOT NULL COMMENT '审核员本次明确的未来UTC复审截止，不默认一年',
  `expires_at` DATETIME(3) NOT NULL COMMENT '实际期限取复审截止与证件到期日次日00:00较早值',
  `approved_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '真实批准UTC时刻',
  `snapshot_seal` MEDIUMBLOB NULL COMMENT '取得ID后同事务一次初始化的生效事实认证封装',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_qualification_snapshot_revision (driver_id,qualification_revision),
  UNIQUE KEY uk_qualification_snapshot_review (review_id),
  UNIQUE KEY uk_qualification_snapshot_submission (submission_id),
  KEY ix_qualification_snapshot_due (expires_at,driver_id),
  CONSTRAINT ck_qualification_snapshot_revision CHECK (qualification_revision>0 AND company_revision>=0 AND material_revision>=0),
  CONSTRAINT ck_qualification_snapshot_license CHECK (license_long_term IN (0,1) AND ((license_long_term=1 AND license_valid_to IS NULL) OR (license_long_term=0 AND license_valid_to IS NOT NULL))),
  CONSTRAINT ck_qualification_snapshot_due CHECK (expires_at<=review_due_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实际批准的不可变生效资格快照；到期不删除历史';

CREATE TEMPORARY TABLE q05_menu_guard (ok TINYINT NOT NULL CHECK(ok=1));
SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971400 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:driverqualification:list')) OR (perms='biz:driverqualification:list' AND menu_id<>2971400);
INSERT INTO q05_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971400 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:driverqualification:list')) OR (perms='biz:driverqualification:list' AND menu_id<>2971400) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971400,'资格审核',2971000,140,'qualification','rongli/qualification/index',1,1,'C','0','0','biz:driverqualification:list','checked','system',SYSDATE(),'Q05；仅建菜单，不默认授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971400);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971401 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:query')) OR (perms='biz:driverqualification:query' AND menu_id<>2971401);
INSERT INTO q05_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971401 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:query')) OR (perms='biz:driverqualification:query' AND menu_id<>2971401) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971401,'查看资格申请',2971400,140,'#',NULL,1,1,'F','0','0','biz:driverqualification:query','checked','system',SYSDATE(),'Q05；仅建菜单，不默认授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971401);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971402 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:audit')) OR (perms='biz:driverqualification:audit' AND menu_id<>2971402);
INSERT INTO q05_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971402 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:audit')) OR (perms='biz:driverqualification:audit' AND menu_id<>2971402) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971402,'审核与补正资格',2971400,140,'#',NULL,1,1,'F','0','0','biz:driverqualification:audit','checked','system',SYSDATE(),'Q05；仅建菜单，不默认授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971402);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971403 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:sensitive')) OR (perms='biz:driverqualification:sensitive' AND menu_id<>2971403);
INSERT INTO q05_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971403 AND (parent_id<>2971400 OR COALESCE(perms,'')<>'biz:driverqualification:sensitive')) OR (perms='biz:driverqualification:sensitive' AND menu_id<>2971403) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971403,'查看提交证件原图',2971400,140,'#',NULL,1,1,'F','0','0','biz:driverqualification:sensitive','checked','system',SYSDATE(),'Q05；仅建菜单，不默认授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971403);

DROP TEMPORARY TABLE q05_menu_guard;

-- 后验：NULL为历史隔离，不用UPDATE伪造；新封装或当前生效指针异常必须独立输出并阻断新审批。
SELECT COUNT(*) AS legacy_untrusted_submissions FROM vi_driver_application_submission WHERE submission_seal IS NULL;
SELECT p.id,p.effective_snapshot_id FROM vi_driver_profile p
LEFT JOIN vi_qualification_snapshot s ON s.id=p.effective_snapshot_id
WHERE p.effective_snapshot_id IS NOT NULL AND (s.id IS NULL OR s.driver_id<>p.id OR s.qualification_revision<>p.qualification_revision);
SELECT menu_id,perms FROM sys_menu WHERE menu_id BETWEEN 2971400 AND 2971403 ORDER BY menu_id;
