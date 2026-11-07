-- Q01 本人代驾资格草稿、材料版本和不可变提交快照；前置20261107_02，按序执行。
-- 执行前备份vi_driver_profile、vi_driver_application及核验/培训五表；暂停资格、证件上传、核验与培训写入。
-- 本脚本DDL不可重复执行，不自动授予资格、不猜测公司、同意或驳回字段，不调用任何供应商。
-- 历史多活动申请先输出异常并停止；需人工核查真实记录，禁止静默任选最新或删除旧数据。
-- 历史材料/核验绑定保持NULL不自动认定通过；回滚应用保留新增事实及审计，不删除提交快照。
-- 升级与完整初始化必须隔离比对；应用重启后生效，不依赖缓存删除完成授权撤销。
SELECT driver_id,COUNT(*) active_count,GROUP_CONCAT(id ORDER BY id) application_ids
FROM vi_driver_application
WHERE status IN ('DRAFT','SUBMITTED','REJECTED')
GROUP BY driver_id
HAVING COUNT(*)>1;

CREATE TEMPORARY TABLE q01_active_guard (
    violations INT NOT NULL CHECK(violations=0)
);

INSERT INTO q01_active_guard
SELECT COUNT(*)
FROM (
    SELECT driver_id
    FROM vi_driver_application
    WHERE status IN ('DRAFT','SUBMITTED','REJECTED')
    GROUP BY driver_id
    HAVING COUNT(*)>1
) invalid_active;

DROP TEMPORARY TABLE q01_active_guard;

ALTER TABLE vi_driver_application
    ADD COLUMN previous_application_id BIGINT NULL COMMENT '上次已批准资格申请；复审另建申请不覆盖生效档案' AFTER driver_id,
    ADD COLUMN owner_dept_id BIGINT NULL COMMENT '提交时从司机档案冻结的真实公司；草稿不由客户端指定' AFTER previous_application_id,
    ADD COLUMN material_revision BIGINT NOT NULL DEFAULT 0 COMMENT '材料实际变更版本；纯同意不递增' AFTER owner_dept_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '当前规范化材料的有密钥摘要；不使用身份证裸哈希' AFTER material_revision,
    ADD COLUMN submission_revision BIGINT NOT NULL DEFAULT 0 COMMENT '提交修订号；对应不可变提交快照' AFTER material_hash,
    ADD COLUMN consent_version VARCHAR(64) NULL COMMENT '服务端核对的资格申请同意版本' AFTER submission_revision,
    ADD COLUMN consent_text TEXT NULL COMMENT '已公开资格申请同意正文快照；不含个人资料' AFTER consent_version,
    ADD COLUMN consent_accepted_at DATETIME(3) NULL COMMENT '本人明确接受当前同意版本的UTC时刻' AFTER consent_text,
    ADD COLUMN reject_fields JSON NULL COMMENT '可补正的精确材料字段；历史NULL不得猜测为全部可编辑' AFTER consent_accepted_at,
    ADD COLUMN public_review_opinion VARCHAR(1000) NULL COMMENT '向本人公开的审核意见；不返回内部review_opinion' AFTER reject_fields,
    ADD COLUMN active_driver BIGINT GENERATED ALWAYS AS (IF(status IN ('DRAFT','SUBMITTED','REJECTED'),driver_id,NULL)) STORED COMMENT '同司机最多一个活动申请' AFTER public_review_opinion,
    ADD UNIQUE KEY uk_driver_active_application (active_driver),
    ADD KEY ix_application_company (owner_dept_id,status,id),
    ADD CONSTRAINT ck_qualification_material_revision CHECK(material_revision>=0 AND submission_revision>=0),
    ADD CONSTRAINT ck_qualification_application_owner CHECK(owner_dept_id IS NULL OR owner_dept_id>0);

ALTER TABLE vi_provider_verification
    ADD COLUMN material_revision BIGINT NULL COMMENT '绑定核验或培训对应的材料版本；历史NULL不可视为当前通过' AFTER application_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '绑定申请当前材料有密钥摘要；不接受客户端声明' AFTER material_revision;

ALTER TABLE vi_face_session
    ADD COLUMN material_revision BIGINT NULL COMMENT '绑定核验或培训对应的材料版本；历史NULL不可视为当前通过' AFTER application_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '绑定申请当前材料有密钥摘要；不接受客户端声明' AFTER material_revision;

ALTER TABLE vi_training_progress
    ADD COLUMN material_revision BIGINT NULL COMMENT '绑定核验或培训对应的材料版本；历史NULL不可视为当前通过' AFTER application_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '绑定申请当前材料有密钥摘要；不接受客户端声明' AFTER material_revision;

ALTER TABLE vi_training_playback
    ADD COLUMN material_revision BIGINT NULL COMMENT '绑定核验或培训对应的材料版本；历史NULL不可视为当前通过' AFTER progress_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '绑定申请当前材料有密钥摘要；不接受客户端声明' AFTER material_revision;

ALTER TABLE vi_training_attempt
    ADD COLUMN material_revision BIGINT NULL COMMENT '绑定核验或培训对应的材料版本；历史NULL不可视为当前通过' AFTER application_id,
    ADD COLUMN material_hash BINARY(32) NULL COMMENT '绑定申请当前材料有密钥摘要；不接受客户端声明' AFTER material_revision;

CREATE TABLE `vi_driver_application_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '不可变提交记录主键',
  `application_id` BIGINT NOT NULL COMMENT '本人资格申请',
  `submission_revision` BIGINT NOT NULL COMMENT '同申请提交修订号',
  `owner_dept_id` BIGINT NOT NULL COMMENT '提交时真实归属公司快照',
  `profile_version` BIGINT NOT NULL COMMENT '提交校验的司机档案版本',
  `material_revision` BIGINT NOT NULL COMMENT '冻结材料版本',
  `material_hash` BINARY(32) NOT NULL COMMENT '冻结材料的有密钥摘要',
  `sensitive_payload_cipher` LONGBLOB NOT NULL COMMENT '实名及证件加密快照；AAD仍绑定账号及申请',
  `evidence_file_ids` JSON NOT NULL COMMENT '四证件槽位到私有文件ID的冻结关联',
  `provider_result_ids` JSON NOT NULL COMMENT '实际可信核验和培训依据ID',
  `consent_version` VARCHAR(64) NOT NULL COMMENT '已接受的资格申请同意版本',
  `consent_text` TEXT NOT NULL COMMENT '已公开同意正文快照',
  `consent_accepted_at` DATETIME(3) NOT NULL COMMENT '本人接受同意UTC时刻',
  `submit_reason_cipher` LONGBLOB NOT NULL COMMENT '提交原因加密；不进入响应或消息',
  `submitted_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '提交时刻UTC',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_application_submission (application_id,submission_revision),
  KEY ix_qualification_submission_company (owner_dept_id,application_id,id),
  CONSTRAINT ck_qualification_submission_revision CHECK (submission_revision>0 AND material_revision>=0 AND profile_version>=0),
  CONSTRAINT ck_qualification_submission_owner CHECK (owner_dept_id>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格申请不可变提交快照；无更新删除业务入口';

-- 无可证明的历史材料/同意与驳回结构，输出人工核查清单，不静默回填。
SELECT id,driver_id,status,material_revision,consent_version
FROM vi_driver_application
WHERE material_hash IS NULL
    OR consent_version IS NULL
    OR (status='REJECTED' AND reject_fields IS NULL)
ORDER BY id;

SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,GENERATION_EXPRESSION
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE()
    AND TABLE_NAME IN ('vi_driver_application','vi_driver_application_submission','vi_provider_verification','vi_face_session','vi_training_progress','vi_training_playback','vi_training_attempt')
ORDER BY TABLE_NAME,ORDINAL_POSITION;
