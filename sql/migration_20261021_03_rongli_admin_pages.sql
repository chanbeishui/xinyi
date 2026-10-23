-- =====================================================================
-- 荣利机动车检测预约 · 后台补齐页面菜单 + 培训演示课程
--
-- 用途：
--   1) 为运营工作台、治理与导出、异常与争议、处罚与申诉、培训课程、通知与任务
--      六个后台页面注册菜单（menu_id 2115-2120）。
--   2) 插入一门已发布的演示培训课程与 3 道考题，便于小程序端「服务培训」
--      页面与后台题库编辑联调。
--
-- 说明：
--   * 脚本可重复执行：菜单先删后插，课程按 (course_key, course_version) 幂等。
--   * 演示课程学时设为 180 秒，是为了让联调可快速走完「学时达标 → 考试」；
--     生产课程请在后台按真实时长新建版本。
--   * 执行：mysql -h127.0.0.1 -P3306 -uroot -p xinyi < migration_20261021_03_rongli_admin_pages.sql
-- =====================================================================

-- ---------------------------------------------------------------- 菜单
DELETE FROM sys_menu WHERE menu_id BETWEEN 2115 AND 2120;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 (2115, '运营工作台', 2100, 0, 'dashboard', 'rongli/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'biz:statistics:list', 'dashboard', 'admin', sysdate(), '', NULL, '统计概览、待办与趋势'),
 (2116, '异常与争议', 2100, 15, 'exception', 'rongli/exception/index', '', '', 1, 0, 'C', '0', '0', 'biz:dispute:list', 'bug', 'admin', sysdate(), '', NULL, '争议、异常、结果更正与支付对账异常'),
 (2117, '处罚与申诉', 2100, 16, 'penalty', 'rongli/penalty/index', '', '', 1, 0, 'C', '0', '0', 'biz:penalty:list', 'warning', 'admin', sysdate(), '', NULL, '接单限制记录与代驾申诉审核'),
 (2118, '培训课程', 2100, 17, 'training', 'rongli/training/index', '', '', 1, 0, 'C', '0', '0', 'biz:trainingcourse:list', 'education', 'admin', sysdate(), '', NULL, '课程版本、学时与题库'),
 (2119, '通知与任务', 2100, 18, 'notification', 'rongli/notification/index', '', '', 1, 0, 'C', '0', '0', 'biz:notificationtemplate:list', 'message', 'admin', sysdate(), '', NULL, '通知模板与 outbox 重试'),
 (2120, '治理与导出', 2100, 19, 'governance', 'rongli/governance/index', '', '', 1, 0, 'C', '0', '0', 'biz:privacycase:list', 'lock', 'admin', sysdate(), '', NULL, '隐私请求、脱敏导出审批与员工绑定凭证');

-- 普通角色（role_id=2）授予只读查询，写权限由管理员按岗位另授
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2115 AND 2120 AND perms LIKE '%:list';

-- ---------------------------------------------------------------- 培训演示课程
SET @app_id := 'wxdev000000000000';

-- 联调种子账号：演示课程视频需要一个上传者账号。真实环境由微信登录自动创建，
-- 全新库（CI / docker compose）里没有任何账号，缺这一行会导致后续 @seed_account 为
-- NULL，vi_file.uploader_account_id 插入直接报 1048。
INSERT INTO vi_account (app_id, open_id, status, created_at, updated_at, version)
VALUES (@app_id, 'dev_owner_001', 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE status = 'ACTIVE', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @seed_account := (SELECT id FROM vi_account WHERE app_id = @app_id AND open_id = 'dev_owner_001' LIMIT 1);
SET @seed_account := IFNULL(@seed_account, (SELECT id FROM vi_account ORDER BY id LIMIT 1));

-- 课程视频占位文件（服务端已登记为可用；仅用于课程详情展示，不影响学时核算）
INSERT INTO vi_file (uploader_account_id, purpose, order_id, application_id, object_key, status,
       mime_type, server_size, server_sha256, retain_until, legal_hold, created_at, updated_at, version)
VALUES (@seed_account, 'TRAINING_MEDIA', NULL, NULL, 'training/demo/service-basic.mp4', 'READY',
       'video/mp4', 1024, UNHEX(REPEAT('00', 32)), DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 365 DAY), 0,
       UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE status = 'READY', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @demo_media := (SELECT id FROM vi_file WHERE object_key = 'training/demo/service-basic.mp4');

INSERT INTO vi_training_course (course_key, course_version, title, description, media_file_id,
       required_duration_seconds, pass_score, attempt_time_limit_seconds, status, published_at,
       published_by_user_id, created_at, updated_at, version)
VALUES ('SERVICE_BASIC', 1, '服务流程与车辆交接基础',
        '了解上门接车、检测点交接、返程归还的标准动作与留证要求；学完并通过测验后才能开始接单。',
        @demo_media, 180, 80, 1800, 'PUBLISHED', UTC_TIMESTAMP(3), 1,
        UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description),
       media_file_id = VALUES(media_file_id), required_duration_seconds = VALUES(required_duration_seconds),
       pass_score = VALUES(pass_score), attempt_time_limit_seconds = VALUES(attempt_time_limit_seconds),
       status = 'PUBLISHED', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @demo_course := (SELECT id FROM vi_training_course WHERE course_key = 'SERVICE_BASIC' AND course_version = 1);

DELETE FROM vi_training_question WHERE training_id = @demo_course;

INSERT INTO vi_training_question (training_id, question_code, question_type, prompt, options_json,
       correct_option_codes, sort_no, created_at, updated_at, version)
VALUES
 (@demo_course, 'Q1', 'SINGLE', '到车主处接车时，留证照片至少需要覆盖哪几类？',
  JSON_ARRAY(JSON_OBJECT('code', 'A', 'text', '只拍车头一张即可'),
             JSON_OBJECT('code', 'B', 'text', '车头、车尾、左右两侧、仪表盘、内饰共六类'),
             JSON_OBJECT('code', 'C', 'text', '由车主自行拍摄，代驾无需留证')),
  JSON_ARRAY('B'), 1, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0),
 (@demo_course, 'Q2', 'SINGLE', '交接码连续输错几次会被锁定？',
  JSON_ARRAY(JSON_OBJECT('code', 'A', 'text', '3 次'),
             JSON_OBJECT('code', 'B', 'text', '5 次'),
             JSON_OBJECT('code', 'C', 'text', '不限制')),
  JSON_ARRAY('A'), 2, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0),
 (@demo_course, 'Q3', 'SINGLE', '车辆在检测点完成检测后，谁负责把车交回代驾？',
  JSON_ARRAY(JSON_OBJECT('code', 'A', 'text', '车主本人到检测点取车'),
             JSON_OBJECT('code', 'B', 'text', '检测点员工与代驾完成放车交接'),
             JSON_OBJECT('code', 'C', 'text', '无需交接，代驾直接开走')),
  JSON_ARRAY('B'), 3, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0);

-- ---------------------------------------------------------------- 校验
SELECT 'menu' AS obj, COUNT(*) AS cnt FROM sys_menu WHERE menu_id BETWEEN 2115 AND 2120
UNION ALL SELECT 'training_course', COUNT(*) FROM vi_training_course WHERE course_key = 'SERVICE_BASIC'
UNION ALL SELECT 'training_question', COUNT(*) FROM vi_training_question WHERE training_id = @demo_course;
