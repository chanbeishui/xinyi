-- Q03 公司必修政策、冻结计划、按材料受控学习与一次阅卷；前置20261109_01培训管理。
-- 执行前备份vi_driver_profile、vi_training_progress/playback/attempt、sys_menu及培训相关记录；暂停相关写入。
-- DDL不可重复执行。执行顺序：备份/异常清单→新增权威结构→历史隔离→稳定菜单→执行后校验→重启后端。
-- 历史培训无公司epoch/run/可信阅卷快照，全部保持NULL隔离，不伪造通过、不猜归属、不删除历史。
-- 不设置公司默认必修/次数/间隔，不生成provider PASSED；A04实际公司变更由应用同事务递增epoch。
-- 本脚本不缩窄旧字段、不删除业务历史；回滚应用须保留新增表和学习/考试事实，不回写旧唯一键。
-- 分阶段：停写并独立备份原attempt表/SHOW CREATE TABLE及开放尝试清单，再修改生成表达式和索引；验证后才重启。
-- 此处MODIFY仅从保留的原始ID/状态推导新生成值，不删原列或尝试事实；DDL仍可能重建索引，不可当事务回滚。
-- 回滚优先停用新培训入口并保留新结构。若要恢复旧唯一键，必须先输出同application/training多OPEN清单，
-- 经独立审批处理真实开放尝试后分阶段恢复；不得自动关闭/删除历史来强建旧索引。
SELECT id,application_id,training_id,status
FROM vi_training_attempt
WHERE status='OPEN'
ORDER BY id;

ALTER TABLE vi_driver_profile
    ADD COLUMN training_context_revision BIGINT NOT NULL DEFAULT 0 COMMENT '仅实际公司归属变更递增；备注与行政开关不使培训失效' AFTER owner_dept_id;

ALTER TABLE vi_training_progress
    ADD COLUMN run_id BIGINT NULL COMMENT '真实当前材料运行；历史NULL隔离，不猜回填' AFTER application_id,
    ADD COLUMN verified_duration_millis BIGINT NOT NULL DEFAULT 0 COMMENT '服务端校验后媒体区间并集毫秒' AFTER verified_duration_seconds,
    ADD COLUMN verified_segments JSON NULL COMMENT '已验证不重复计时的媒体区间并集' AFTER verified_duration_millis,
    ADD COLUMN active_playback_id BIGINT NULL COMMENT '当前唯一可报告播放，旧设备会话不能并行加时' AFTER verified_segments,
    DROP INDEX uk_training_progress,
    ADD UNIQUE KEY uk_training_progress_run (run_id,training_id),
    ADD KEY ix_training_progress_application (application_id,training_id),
    ADD CONSTRAINT ck_training_progress_millis CHECK (verified_duration_millis>=0);

ALTER TABLE vi_training_playback
    ADD COLUMN run_id BIGINT NULL COMMENT '绑定材料运行；历史NULL不可报告' AFTER progress_id,
    ADD COLUMN context_version BIGINT NULL COMMENT '绑定MINI真实身份视角版本' AFTER session_id,
    ADD COLUMN last_position_millis BIGINT NULL COMMENT '服务端最近位置基线，首次报告不计时' AFTER last_sequence,
    ADD COLUMN last_playing TINYINT NOT NULL DEFAULT 0 COMMENT '暂停不累计；恢复第一报告重新建基线' AFTER last_position_millis;

ALTER TABLE vi_training_attempt
    ADD COLUMN run_id BIGINT NULL COMMENT '真实材料运行；历史NULL不作为当前考试结果' AFTER application_id,
    ADD COLUMN course_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '跨材料/版本累计次数的稳定课程族' AFTER training_id,
    ADD COLUMN grading_snapshot_cipher MEDIUMBLOB NULL COMMENT '独立AAD绑定本次尝试的标准答案与阅卷参数密文' AFTER question_snapshot,
    ADD COLUMN submission_mac BINARY(32) NULL COMMENT '永久作答摘要，换幂等键也不能重新阅卷' AFTER answer_snapshot,
    DROP INDEX uk_open_training_attempt,
    MODIFY COLUMN active_attempt VARCHAR(128) GENERATED ALWAYS AS (IF(status='OPEN' AND run_id IS NOT NULL,CONCAT(run_id,':',training_id),NULL)) STORED COMMENT '同当前运行同课程只存在一个开放尝试',
    ADD UNIQUE KEY uk_open_training_attempt (active_attempt),
    ADD UNIQUE KEY uk_attempt_course_counter (application_id,course_key,attempt_no),
    ADD KEY ix_training_attempt_counter (application_id,course_key,status,id);

CREATE TABLE `vi_training_policy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成政策聚合主键',
  `owner_dept_id` BIGINT NOT NULL COMMENT '显式公司部门，旗下站点和该公司代驾共用',
  `draft_revision_id` BIGINT NULL COMMENT '当前未发布修订，发布请求必须显式指定',
  `active_revision_id` BIGINT NULL COMMENT '当前生效政策修订，不覆盖既有申请冻结集合',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '政策聚合CAS，与课程内容版本分离',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_policy_company (owner_dept_id),
  CONSTRAINT ck_training_policy_version CHECK (version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司培训政策聚合';

CREATE TABLE `vi_training_policy_revision` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '不可变政策内容修订',
  `policy_id` BIGINT NOT NULL COMMENT '权威公司政策父聚合',
  `revision_no` INT NOT NULL COMMENT '聚合内单调内容修订号',
  `required_training_ids` JSON NOT NULL COMMENT '确切已发布课程ID数组，同族最多一个版本',
  `max_attempts` INT NULL COMMENT '含首次的累计上限，草稿可缺，发布不得缺省',
  `retry_interval_seconds` INT NULL COMMENT '失败或超时到下一次的间隔秒，发布必须显式配置',
  `grading_mode` VARCHAR(32) NOT NULL COMMENT 'TrainingGradingMode: EQUAL_HALF_UP_EXACT，等分及精确集合匹配',
  `content_hash` BINARY(32) NOT NULL COMMENT '服务端政策有密钥摘要，不接受客户端声明',
  `created_by_user_id` BIGINT NOT NULL COMMENT '后台内容创建人',
  `published_by_user_id` BIGINT NULL COMMENT '后台真实发布人，草稿未发布为空',
  `published_at` DATETIME(3) NULL COMMENT '服务端实际发布时间UTC',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '服务端记录CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_policy_revision (policy_id,revision_no),
  CONSTRAINT ck_training_policy_revision CHECK (revision_no>0 AND version>=0),
  CONSTRAINT ck_training_policy_limits CHECK ((max_attempts IS NULL OR max_attempts BETWEEN 1 AND 100) AND (retry_interval_seconds IS NULL OR retry_interval_seconds BETWEEN 0 AND 2592000)),
  CONSTRAINT ck_training_policy_grading CHECK (`grading_mode` IN ('EQUAL_HALF_UP_EXACT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司培训政策不可变修订';

CREATE TABLE `vi_training_plan_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '首次真实学习冻结的申请课程集合',
  `application_id` BIGINT NOT NULL COMMENT '真实资格申请父记录',
  `owner_dept_id` BIGINT NOT NULL COMMENT '数据库权威公司归属',
  `company_revision` BIGINT NOT NULL COMMENT '档案专用公司归属epoch，A转B再转A不可复活',
  `policy_revision_id` BIGINT NOT NULL COMMENT '首次学习固定的确切已发布政策修订',
  `course_snapshot` JSON NOT NULL COMMENT '不可变确切课程版本/媒体/有密钥摘要及学习考试参数，不含标准答案',
  `max_attempts` INT NOT NULL COMMENT '含首次的同申请同课程累计上限',
  `retry_interval_seconds` INT NOT NULL COMMENT '失败或超时后的最小重考间隔秒',
  `grading_mode` VARCHAR(32) NOT NULL COMMENT 'TrainingGradingMode: EQUAL_HALF_UP_EXACT',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '服务端记录CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_plan_context (application_id,company_revision),
  KEY ix_training_plan_company (owner_dept_id,application_id),
  CONSTRAINT ck_training_plan_context CHECK (company_revision>=0 AND version>=0),
  CONSTRAINT ck_training_plan_limits CHECK (max_attempts BETWEEN 1 AND 100 AND retry_interval_seconds BETWEEN 0 AND 2592000),
  CONSTRAINT ck_training_plan_grading CHECK (`grading_mode` IN ('EQUAL_HALF_UP_EXACT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资格申请冻结必修课表';

CREATE TABLE `vi_training_run` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '冻结集合下每次实际材料版本的学习运行',
  `plan_snapshot_id` BIGINT NOT NULL COMMENT '当前申请和公司epoch的不可变课表',
  `application_id` BIGINT NOT NULL COMMENT '真实资格申请父记录',
  `material_revision` BIGINT NOT NULL COMMENT '当前实际材料修订号',
  `material_hash` BINARY(32) NOT NULL COMMENT '当前材料的有密钥摘要，不接受客户端声明',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'TrainingRunStatus: ACTIVE,PASSED',
  `provider_verification_id` BIGINT NULL COMMENT '只在全部必修真实完成后关联本事务培训汇总证明',
  `completed_at` DATETIME(3) NULL COMMENT '当前材料全部必修实际完成的UTC时刻',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '服务端记录CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_run_material (plan_snapshot_id,material_revision),
  KEY ix_training_run_application (application_id,id),
  UNIQUE KEY uk_training_run_proof (provider_verification_id),
  CONSTRAINT ck_training_run_status CHECK (`status` IN ('ACTIVE','PASSED')),
  CONSTRAINT ck_training_run_version CHECK (material_revision>=0 AND version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='当前材料培训运行与汇总';

CREATE TEMPORARY TABLE q03_menu_guard (
    violations INT NOT NULL CHECK (violations=0)
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE menu_id=2971200 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'');

INSERT INTO q03_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE menu_id=2971200 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'');

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971200,'培训管理',2971000,120,'training-policy',NULL,1,1,
    'M','0','0','','education','migration','Q03公司显式必修集合与考试政策，发布不覆盖既有申请冻结计划'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971200
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971201 AND (parent_id<>2971200 OR COALESCE(perms,'')<>'biz:trainingpolicy:query'))
    OR (perms='biz:trainingpolicy:query' AND menu_id<>2971201);

INSERT INTO q03_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971201 AND (parent_id<>2971200 OR COALESCE(perms,'')<>'biz:trainingpolicy:query'))
    OR (perms='biz:trainingpolicy:query' AND menu_id<>2971201);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971201,'培训必修政策',2971200,120,'policies','rongli/training/policy',1,1,
    'C','0','0','biz:trainingpolicy:query','education','migration','Q03公司显式必修集合与考试政策，发布不覆盖既有申请冻结计划'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971201
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971202 AND (parent_id<>2971201 OR COALESCE(perms,'')<>'biz:trainingpolicy:edit'))
    OR (perms='biz:trainingpolicy:edit' AND menu_id<>2971202);

INSERT INTO q03_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971202 AND (parent_id<>2971201 OR COALESCE(perms,'')<>'biz:trainingpolicy:edit'))
    OR (perms='biz:trainingpolicy:edit' AND menu_id<>2971202);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971202,'维护培训政策',2971201,120,'#',NULL,1,1,
    'F','0','0','biz:trainingpolicy:edit','education','migration','Q03公司显式必修集合与考试政策，发布不覆盖既有申请冻结计划'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971202
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971203 AND (parent_id<>2971201 OR COALESCE(perms,'')<>'biz:trainingpolicy:publish'))
    OR (perms='biz:trainingpolicy:publish' AND menu_id<>2971203);

INSERT INTO q03_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971203 AND (parent_id<>2971201 OR COALESCE(perms,'')<>'biz:trainingpolicy:publish'))
    OR (perms='biz:trainingpolicy:publish' AND menu_id<>2971203);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971203,'发布培训政策',2971201,120,'#',NULL,1,1,
    'F','0','0','biz:trainingpolicy:publish','education','migration','Q03公司显式必修集合与考试政策，发布不覆盖既有申请冻结计划'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971203
);

DROP TEMPORARY TABLE q03_menu_guard;

-- 历史隔离清单，不能将下列行解释为当前申请正式通过。
SELECT id,application_id,training_id,status
FROM vi_training_progress
WHERE run_id IS NULL
ORDER BY id;

SELECT id,application_id,training_id,status
FROM vi_training_attempt
WHERE run_id IS NULL
ORDER BY id;

SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,GENERATION_EXPRESSION
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE()
    AND TABLE_NAME IN ('vi_training_policy','vi_training_policy_revision','vi_training_plan_snapshot',
        'vi_training_run','vi_training_progress','vi_training_playback','vi_training_attempt','vi_driver_profile')
ORDER BY TABLE_NAME,ORDINAL_POSITION;

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE menu_id BETWEEN 2971200 AND 2971203
ORDER BY menu_id;
