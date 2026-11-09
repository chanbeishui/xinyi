-- Q02 公司培训课程/题库不可变发布与真实私有MP4；前置20261107_03，按顺序执行。
-- 执行前备份vi_training_course、vi_training_question、vi_file、sys_menu，暂停培训与媒体上传写入。
-- DDL不可重复执行。只放宽未发布草稿可空字段；不删除既有课程/题目/文件，不生成培训通过记录。
-- 历史课程公司、创建者、摘要保持NULL并输出核查清单，不按员工/主部门/历史订单猜测。
-- 同族多DRAFT等异常先报表并停止；须人工处理真实版本，不静默选择最新或删除历史。
-- 应用回滚保留新增字段、版本和媒体事实；新增视频仍私有。升级后重启后端，无需以缓存清理完成撤权。
SELECT course_key,COUNT(*) draft_count,GROUP_CONCAT(id ORDER BY id) training_ids
FROM vi_training_course
WHERE status='DRAFT'
GROUP BY course_key
HAVING COUNT(*)>1;

CREATE TEMPORARY TABLE q02_schema_guard (
    violations INT NOT NULL CHECK(violations=0)
);

INSERT INTO q02_schema_guard
SELECT COUNT(*)
FROM (
    SELECT course_key
    FROM vi_training_course
    WHERE status='DRAFT'
    GROUP BY course_key
    HAVING COUNT(*)>1
) repeated_draft;

DROP TEMPORARY TABLE q02_schema_guard;

ALTER TABLE vi_training_course
    ADD COLUMN owner_dept_id BIGINT NULL COMMENT '明确选择的公司部门；历史NULL待人工核查，不从创建者或主部门推断' AFTER id,
    ADD COLUMN source_training_id BIGINT NULL COMMENT '派生所依据的已发布确切课程版本' AFTER owner_dept_id,
    ADD COLUMN inherited_media_file_id BIGINT NULL COMMENT '服务端从来源课程冻结的只读媒体白名单，客户端不可指定' AFTER source_training_id,
    ADD COLUMN created_by_user_id BIGINT NULL COMMENT '后台草稿创建人；历史不猜测回填' AFTER inherited_media_file_id,
    ADD COLUMN updated_by_user_id BIGINT NULL COMMENT '最近后台内容维护人' AFTER created_by_user_id,
    ADD COLUMN content_hash BINARY(32) NULL COMMENT '含题库答案的有密钥内容指纹；不向MINI发布' AFTER updated_by_user_id,
    ADD COLUMN draft_course_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin GENERATED ALWAYS AS (IF(status='DRAFT',course_key,NULL)) STORED COMMENT '单课程族仅一个未发布草稿' AFTER content_hash,
    MODIFY COLUMN media_file_id BIGINT NULL COMMENT '私有培训媒体；草稿未选可为空',
    MODIFY COLUMN required_duration_seconds INT NULL COMMENT '规定学习时长；发布须完整且不超过实际媒体时长',
    MODIFY COLUMN pass_score INT NULL COMMENT '0至100的配置门槛；草稿可暂缺',
    MODIFY COLUMN attempt_time_limit_seconds INT NULL COMMENT '考试时限秒；发布须配置至少60秒',
    ADD UNIQUE KEY uk_training_active_draft (draft_course_key),
    ADD KEY ix_training_company (owner_dept_id,status,id),
    ADD CONSTRAINT ck_training_owner CHECK(owner_dept_id IS NULL OR owner_dept_id>0);

ALTER TABLE vi_file
    ADD COLUMN training_id BIGINT NULL COMMENT '培训媒体权威父课程版本，发布后仅只读继承' AFTER station_id,
    ADD COLUMN media_duration_millis BIGINT NULL COMMENT '服务端解析的实际视频时长毫秒' AFTER training_id,
    ADD COLUMN media_width INT NULL COMMENT '实际视频轨道宽度' AFTER media_duration_millis,
    ADD COLUMN media_height INT NULL COMMENT '实际视频轨道高度' AFTER media_width,
    ADD COLUMN media_video_codec VARCHAR(16) NULL COMMENT '实际视频轨道编码' AFTER media_height,
    ADD COLUMN media_audio_codec VARCHAR(16) NULL COMMENT '可选实际音频轨道编码' AFTER media_video_codec,
    ADD CONSTRAINT ck_training_file_parent CHECK((training_id IS NULL AND purpose<>'TRAINING_MEDIA') OR (training_id IS NOT NULL AND purpose='TRAINING_MEDIA' AND order_id IS NULL AND application_id IS NULL AND station_id IS NULL)),
    ADD KEY ix_training_file (training_id,status,id);

CREATE TEMPORARY TABLE q02_menu_guard (
    violations INT NOT NULL CHECK(violations=0)
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971100 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:trainingcourse:list'))
    OR (perms='biz:trainingcourse:list' AND menu_id<>2971100);

INSERT INTO q02_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971100 AND (parent_id<>2971000 OR COALESCE(perms,'')<>'biz:trainingcourse:list'))
    OR (perms='biz:trainingcourse:list' AND menu_id<>2971100);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971100,'培训课程与题库',2971000,110,'training','rongli/training/index',1,1,
    'C','0','0','biz:trainingcourse:list','education','migration','Q02各公司独立培训；只发布不可变版本，不自动生成资格或授予角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971100
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971101 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:query'))
    OR (perms='biz:trainingcourse:query' AND menu_id<>2971101);

INSERT INTO q02_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971101 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:query'))
    OR (perms='biz:trainingcourse:query' AND menu_id<>2971101);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971101,'培训课程详情',2971100,110,'#',NULL,1,1,
    'F','0','0','biz:trainingcourse:query','education','migration','Q02各公司独立培训；只发布不可变版本，不自动生成资格或授予角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971101
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971102 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:create'))
    OR (perms='biz:trainingcourse:create' AND menu_id<>2971102);

INSERT INTO q02_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971102 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:create'))
    OR (perms='biz:trainingcourse:create' AND menu_id<>2971102);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971102,'创建培训草稿',2971100,110,'#',NULL,1,1,
    'F','0','0','biz:trainingcourse:create','education','migration','Q02各公司独立培训；只发布不可变版本，不自动生成资格或授予角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971102
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971103 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:edit'))
    OR (perms='biz:trainingcourse:edit' AND menu_id<>2971103);

INSERT INTO q02_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971103 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:edit'))
    OR (perms='biz:trainingcourse:edit' AND menu_id<>2971103);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971103,'编辑培训草稿',2971100,110,'#',NULL,1,1,
    'F','0','0','biz:trainingcourse:edit','education','migration','Q02各公司独立培训；只发布不可变版本，不自动生成资格或授予角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971103
);

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE (menu_id=2971104 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:publish'))
    OR (perms='biz:trainingcourse:publish' AND menu_id<>2971104);

INSERT INTO q02_menu_guard
SELECT COUNT(*)
FROM sys_menu
WHERE (menu_id=2971104 AND (parent_id<>2971100 OR COALESCE(perms,'')<>'biz:trainingcourse:publish'))
    OR (perms='biz:trainingcourse:publish' AND menu_id<>2971104);

INSERT INTO sys_menu(
    menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
    menu_type,visible,status,perms,icon,create_by,remark
)
SELECT
    2971104,'发布培训课程',2971100,110,'#',NULL,1,1,
    'F','0','0','biz:trainingcourse:publish','education','migration','Q02各公司独立培训；只发布不可变版本，不自动生成资格或授予角色'
WHERE NOT EXISTS(
    SELECT 1
    FROM sys_menu
    WHERE menu_id=2971104
);

DROP TEMPORARY TABLE q02_menu_guard;

-- 无可信历史归属可回填；以下清单需人工核查，不能直接发布为某公司正式培训。
SELECT id,course_key,course_version,status,owner_dept_id,created_by_user_id
FROM vi_training_course
WHERE owner_dept_id IS NULL OR content_hash IS NULL OR created_by_user_id IS NULL
ORDER BY id;

SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,GENERATION_EXPRESSION
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('vi_training_course','vi_file')
ORDER BY TABLE_NAME,ORDINAL_POSITION;

SELECT menu_id,parent_id,perms
FROM sys_menu
WHERE menu_id BETWEEN 2971100 AND 2971104
ORDER BY menu_id;
