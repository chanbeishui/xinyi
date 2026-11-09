-- Q04 微信SOTER本机生物认证及背景/驾驶证双人人工确认；前置20261109_02受控培训。
-- 先独立备份vi_provider_verification、vi_face_session、sys_menu及SHOW CREATE TABLE；暂停资格核验相关写入。
-- 执行顺序：输出历史异常清单→前向扩展→建立人工事实表→稳定菜单→校验→重启后端并刷新权限菜单。
-- DDL不可重复执行；菜单防重且冲突失败。不得把本脚本作为可回滚事务，分阶段执行并检查错误。
-- 历史REALNAME/FACE、供应商字符串或PASSED均不自动升级；新增来源/归属/会话全部NULL隔离，不猜回填。
-- 保留旧表名、旧字段和旧数据，仅新增检查枚举值；不设置同意文本默认、不新增腾讯配置、不生成通过结论。
-- 回滚应用先停用新入口，保留新增列/表及真实确认事实，禁止删除历史或将DEVICE_AUTH改成实名活体。
-- 普通备注是可选文本；用户明确不要求附件和标准发布。经办/复核两人不同，复核人可参与后续资格审核。
SELECT id,application_id,task_type,status FROM vi_provider_verification WHERE status='PASSED' ORDER BY id;
SELECT id,application_id,status FROM vi_face_session ORDER BY id;

ALTER TABLE vi_provider_verification
    ADD COLUMN `owner_dept_id` BIGINT NULL COMMENT '权威公司；历史NULL不猜归属',
    ADD COLUMN `company_revision` BIGINT NULL COMMENT '专用公司归属epoch，历史NULL不可信',
    ADD COLUMN `source_kind` VARCHAR(32) NULL COMMENT 'DEVICE_SESSION或MANUAL_REVIEW，历史无来源不可信',
    ADD COLUMN `source_record_id` BIGINT NULL COMMENT '可信原始会话或双人人工确认事实',
    ADD COLUMN `canonical_result_cipher` MEDIUMBLOB NULL COMMENT '绑定完整上下文与原始事实的认证加密结果',
    ADD COLUMN `source_session_id` BIGINT NULL COMMENT '设备确认绑定实际MINI登录会话；人工事实为空',
    MODIFY COLUMN task_type VARCHAR(64) COMMENT 'VerificationType: DEVICE_AUTH,BACKGROUND,LICENSE,TRAINING；REALNAME/FACE仅历史',
    DROP CHECK ck_66_2612e28e561f,
    ADD CONSTRAINT ck_66_2612e28e561f CHECK (`task_type` IN ('DEVICE_AUTH','REALNAME','FACE','BACKGROUND','LICENSE','TRAINING')),
    ADD UNIQUE KEY uk_verification_source (source_kind,source_record_id,task_type),
    ADD KEY ix_verification_canonical (application_id,company_revision,material_revision,task_type,id);

ALTER TABLE vi_face_session
    ADD COLUMN `owner_dept_id` BIGINT NULL COMMENT '原始公司归属；历史NULL不可信',
    ADD COLUMN `company_revision` BIGINT NULL COMMENT '专用公司归属epoch',
    ADD COLUMN `session_id` BIGINT NULL COMMENT '真实MINI登录会话；新登录必须重做',
    ADD COLUMN `context_version` BIGINT NULL COMMENT '创建和未完成确认绑定原始身份视角',
    ADD COLUMN `app_id` VARCHAR(64) NULL COMMENT '可信登录小程序AppID',
    ADD COLUMN `open_id_mac` BINARY(32) NULL COMMENT '服务端OpenID上下文摘要，不接收客户端身份',
    ADD COLUMN `request_no` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '创建UUID，绑定原始请求永久恢复',
    ADD COLUMN `request_mac` BINARY(32) NULL COMMENT '原始目标和同意正文有密钥摘要',
    ADD COLUMN `config_profile_id` BIGINT NULL COMMENT '实际使用的小程序公共凭据聚合',
    ADD COLUMN `config_revision_id` BIGINT NULL COMMENT '签发时冻结的已发布凭据修订',
    ADD COLUMN `provider` VARCHAR(64) NULL COMMENT 'WECHAT_SOTER，仅设备已录入生物特征确认',
    ADD COLUMN `product` VARCHAR(64) NULL COMMENT 'WX_SOTER_DEVICE_AUTH',
    ADD COLUMN `provider_identity_digest` VARCHAR(64) NULL COMMENT '非秘密渠道身份摘要',
    ADD COLUMN `config_snapshot` JSON NULL COMMENT '非秘密配置快照，不保存AppSecret',
    ADD COLUMN `challenge_cipher` BLOB NULL COMMENT '一次性随机挑战认证密文',
    ADD COLUMN `challenge_mac` BINARY(32) NULL COMMENT '挑战匹配摘要',
    ADD COLUMN `submission_cipher` MEDIUMBLOB NULL COMMENT '首次接受的原始resultJSON及签名认证密文',
    ADD COLUMN `submission_mac` BINARY(32) NULL COMMENT '首次提交原文不可替换摘要',
    ADD COLUMN `submitted_at` DATETIME(3) NULL COMMENT '首次签名提交必须在挑战有效期内',
    ADD COLUMN `consent_text_hash` CHAR(64) NULL COMMENT '明确同意正文SHA256',
    ADD COLUMN `consent_text` TEXT NULL COMMENT '创建时同意正文快照',
    ADD COLUMN `consent_accepted_at` DATETIME(3) NULL COMMENT '服务端确认明确同意时刻UTC',
    ADD COLUMN `execution_started_at` DATETIME(3) NULL COMMENT '服务端验签意图开始时刻UTC',
    ADD COLUMN `lease_token` CHAR(36) NULL COMMENT '锁外微信验签租约防旧结果覆盖',
    ADD COLUMN `lease_until` DATETIME(3) NULL COMMENT '验签租约截止UTC',
    ADD COLUMN `verified_at` DATETIME(3) NULL COMMENT '微信可信验签终态确认时刻UTC',
    ADD COLUMN `result_digest` VARCHAR(64) NULL COMMENT '安全结果摘要，不含设备标识或生物数据',
    ADD COLUMN `active_device_context` VARCHAR(200) GENERATED ALWAYS AS (IF(request_no IS NOT NULL AND status IN ('CREATED','PROCESSING','UNKNOWN'),CONCAT(application_id,':',session_id,':',context_version),NULL)) STORED COMMENT '同一申请实际会话及视角只允许一个未决挑战；历史隔离',
    MODIFY COLUMN status VARCHAR(24) NOT NULL DEFAULT 'CREATED' COMMENT 'FaceSessionStatus: CREATED,PROCESSING,UNKNOWN,PASSED,FAILED,EXPIRED,CLOSED',
    MODIFY COLUMN launch_mode VARCHAR(24) NOT NULL DEFAULT 'WEBVIEW_URL' COMMENT 'FaceLaunchMode: WX_SOTER_DEVICE_AUTH；WEBVIEW_URL仅历史',
    MODIFY COLUMN expires_at DATETIME(3) NOT NULL COMMENT '本机随机挑战创建后300秒；已接受原文未知结果可恢复',
    DROP CHECK ck_66_b67f9761e849,
    DROP CHECK ck_66_a153de145e1b,
    ADD CONSTRAINT ck_66_b67f9761e849 CHECK (`status` IN ('CREATED','PROCESSING','UNKNOWN','PASSED','FAILED','EXPIRED','CLOSED')),
    ADD CONSTRAINT ck_66_a153de145e1b CHECK (`launch_mode` IN ('WEBVIEW_URL','WX_SOTER_DEVICE_AUTH')),
    ADD UNIQUE KEY uk_device_request (request_no),
    ADD UNIQUE KEY uk_device_active (active_device_context),
    ADD KEY ix_device_owner_status (owner_dept_id,status,id),
    COMMENT='本机生物认证挑战与微信验签会话；物理旧表名保留';

CREATE TABLE `vi_manual_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成单次经办复核事实主键',
  `application_id` BIGINT NOT NULL COMMENT '权威资格申请父记录',
  `owner_dept_id` BIGINT NOT NULL COMMENT '提交时数据库权威公司',
  `company_revision` BIGINT NOT NULL COMMENT '提交时专用公司归属epoch',
  `material_revision` BIGINT NOT NULL COMMENT '绑定当前材料修订',
  `material_hash` BINARY(32) NOT NULL COMMENT '服务端材料有密钥摘要',
  `verification_type` VARCHAR(24) NOT NULL COMMENT 'ManualVerificationType: BACKGROUND,LICENSE',
  `conclusion` VARCHAR(16) NOT NULL COMMENT '经办结论PASSED或FAILED，不是实名官方事实',
  `status` VARCHAR(24) NOT NULL COMMENT 'ManualVerificationStatus: PENDING_REVIEW,CONFIRMED,RETURNED',
  `submitted_by_user_id` BIGINT NOT NULL COMMENT '真实经办ADMIN用户',
  `reviewed_by_user_id` BIGINT NULL COMMENT '不同经办的真实复核ADMIN用户',
  `submitted_at` DATETIME(3) NOT NULL COMMENT '服务端经办UTC时刻',
  `reviewed_at` DATETIME(3) NULL COMMENT '服务端复核UTC时刻',
  `note_cipher` BLOB NULL COMMENT '绑定事实ID的普通经办备注认证密文',
  `review_note_cipher` BLOB NULL COMMENT '绑定事实ID及复核人的备注认证密文',
  `proof_id` BIGINT NULL COMMENT '同事务发布并链接的可信结论',
  `applicant_user_ids` JSON NOT NULL COMMENT '仅服务器冻结已知申请人后台用户ID；换号不解除自审限制',
  `active_context` VARCHAR(200) GENERATED ALWAYS AS (IF(status='PENDING_REVIEW',CONCAT(application_id,':',company_revision,':',material_revision,':',verification_type),NULL)) STORED COMMENT '同申请材料公司epoch核验类型仅一个待复核',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新必须显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '经办复核CAS',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_manual_active (active_context),
  UNIQUE KEY uk_manual_proof (proof_id),
  KEY ix_manual_owner (owner_dept_id,id),
  KEY ix_manual_application (application_id,verification_type,id),
  CONSTRAINT ck_manual_type CHECK (`verification_type` IN ('BACKGROUND','LICENSE')),
  CONSTRAINT ck_manual_conclusion CHECK (`conclusion` IN ('PASSED','FAILED')),
  CONSTRAINT ck_manual_status CHECK (`status` IN ('PENDING_REVIEW','CONFIRMED','RETURNED')),
  CONSTRAINT ck_manual_distinct CHECK (reviewed_by_user_id IS NULL OR reviewed_by_user_id<>submitted_by_user_id),
  CONSTRAINT ck_manual_version CHECK (version>=0 AND company_revision>=0 AND material_revision>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='背景和驾驶证双人人工确认，不要求上传凭证';

CREATE TEMPORARY TABLE q04_menu_guard (ok TINYINT NOT NULL CHECK(ok=1));
SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971300 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:manualverification:list')) OR (perms='biz:manualverification:list' AND menu_id<>2971300);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971300 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:manualverification:list')) OR (perms='biz:manualverification:list' AND menu_id<>2971300) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971300,'资格核验',2971000,130,'verification','rongli/verification/index',1,1,'C','0','0','biz:manualverification:list','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971300);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971301 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:query')) OR (perms='biz:manualverification:query' AND menu_id<>2971301);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971301 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:query')) OR (perms='biz:manualverification:query' AND menu_id<>2971301) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971301,'查看核验详情',2971300,130,'#',NULL,1,1,'F','0','0','biz:manualverification:query','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971301);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971302 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:create')) OR (perms='biz:manualverification:create' AND menu_id<>2971302);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971302 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:create')) OR (perms='biz:manualverification:create' AND menu_id<>2971302) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971302,'登记人工确认',2971300,130,'#',NULL,1,1,'F','0','0','biz:manualverification:create','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971302);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971303 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:review')) OR (perms='biz:manualverification:review' AND menu_id<>2971303);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971303 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:manualverification:review')) OR (perms='biz:manualverification:review' AND menu_id<>2971303) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971303,'复核人工确认',2971300,130,'#',NULL,1,1,'F','0','0','biz:manualverification:review','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971303);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971304 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:list')) OR (perms='biz:verification:list' AND menu_id<>2971304);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971304 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:list')) OR (perms='biz:verification:list' AND menu_id<>2971304) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971304,'查看本机认证会话',2971300,130,'#',NULL,1,1,'F','0','0','biz:verification:list','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971304);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971305 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:query')) OR (perms='biz:verification:query' AND menu_id<>2971305);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971305 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:query')) OR (perms='biz:verification:query' AND menu_id<>2971305) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971305,'查看本机认证详情',2971300,130,'#',NULL,1,1,'F','0','0','biz:verification:query','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971305);

SELECT menu_id,perms FROM sys_menu WHERE (menu_id=2971306 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:close')) OR (perms='biz:verification:close' AND menu_id<>2971306);
INSERT INTO q04_menu_guard(ok) SELECT 0 FROM sys_menu WHERE (menu_id=2971306 AND (parent_id<>2971300 OR COALESCE(perms,'')<>'biz:verification:close')) OR (perms='biz:verification:close' AND menu_id<>2971306) LIMIT 1;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971306,'封存异常认证会话',2971300,130,'#',NULL,1,1,'F','0','0','biz:verification:close','verified','system',SYSDATE(),'Q04；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971306);

DROP TEMPORARY TABLE q04_menu_guard;

-- 执行后：历史为空是预期隔离；新任务必须有完整可信来源，不得用更新语句伪造修复。
SELECT COUNT(*) AS legacy_untrusted_proofs FROM vi_provider_verification WHERE source_kind IS NULL;
SELECT id FROM vi_face_session WHERE request_no IS NOT NULL AND (owner_dept_id IS NULL OR session_id IS NULL OR challenge_mac IS NULL);
SELECT id FROM vi_manual_verification WHERE reviewed_by_user_id=submitted_by_user_id;
SELECT menu_id,perms FROM sys_menu WHERE menu_id BETWEEN 2971300 AND 2971306 ORDER BY menu_id;
