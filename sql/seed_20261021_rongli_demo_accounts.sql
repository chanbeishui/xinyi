-- =====================================================================
-- 荣利机动车检测预约 · 联调/演示账号种子数据
--
-- 用途：
--   1) 本地联调与冒烟测试所需的稳定账号（车主 / 代驾 / 检测点）。
--   2) 小程序端在 mock 登录模式下，用 code 直接换取这些账号的会话。
--
-- 说明：
--   * 脚本可重复执行（INSERT ... ON DUPLICATE KEY UPDATE），不会产生重复账号。
--   * 仅在 rongli.wechat.mock=true（联调模式）下有效；生产环境请关闭 mock 并走真实
--     微信登录 + 员工绑定流程，不要执行本脚本。
--   * 检测点（STATION）视角依赖「员工绑定 + 站点授权」，无法纯 SQL 造出：
--     需先用 dev_station_001 登录小程序，在「设置 → 员工绑定」用后台发放的口令绑定
--     sys_user，再由后台在「站点授权」中为其授权检测点。
--
-- 执行：mysql -h127.0.0.1 -P3307 -uroot -p xinyi < seed_20261021_rongli_demo_accounts.sql
-- =====================================================================

SET @app_id := 'wxdev000000000000';

-- ---------------------------------------------------------------- 车主
INSERT INTO vi_account (app_id, open_id, union_id, nickname, status, created_at, updated_at, version)
VALUES (@app_id, 'dev_owner_001', NULL, '演示车主', 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = 'ACTIVE', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @owner_account := (SELECT id FROM vi_account WHERE app_id = @app_id AND open_id = 'dev_owner_001');

INSERT INTO vi_account_security (account_id, session_epoch, permission_version, security_status, changed_at, created_at, version)
SELECT @owner_account, 0, 0, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_account_security WHERE account_id = @owner_account);

INSERT INTO vi_owner_profile (account_id, service_name, service_flag, created_at, updated_at, version)
SELECT @owner_account, '演示车主', 'NORMAL', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_owner_profile WHERE account_id = @owner_account);

-- ---------------------------------------------------------------- 代驾 1
INSERT INTO vi_account (app_id, open_id, union_id, nickname, status, created_at, updated_at, version)
VALUES (@app_id, 'dev_driver_001', NULL, '演示代驾·陈师傅', 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = 'ACTIVE', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @driver_account := (SELECT id FROM vi_account WHERE app_id = @app_id AND open_id = 'dev_driver_001');

INSERT INTO vi_account_security (account_id, session_epoch, permission_version, security_status, changed_at, created_at, version)
SELECT @driver_account, 0, 0, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_account_security WHERE account_id = @driver_account);

INSERT INTO vi_owner_profile (account_id, service_name, service_flag, created_at, updated_at, version)
SELECT @driver_account, '演示代驾·陈师傅', 'NORMAL', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_owner_profile WHERE account_id = @driver_account);

-- 资格已通过审核 + 开启接单与听单，可被自动派单命中
INSERT INTO vi_driver_profile (account_id, qualification_status, new_assignment_enabled, listening_enabled,
       license_class, license_valid_to, license_long_term, created_at, updated_at, version)
VALUES (@driver_account, 'APPROVED', 1, 1, 'C1', DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 5 YEAR), 0,
        UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE qualification_status = 'APPROVED', new_assignment_enabled = 1, listening_enabled = 1,
       license_class = VALUES(license_class), license_valid_to = VALUES(license_valid_to),
       updated_at = UTC_TIMESTAMP(3), version = version + 1;

-- ---------------------------------------------------------------- 代驾 2（轮转备用）
INSERT INTO vi_account (app_id, open_id, union_id, nickname, status, created_at, updated_at, version)
VALUES (@app_id, 'dev_driver_002', NULL, '演示代驾·李师傅', 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = 'ACTIVE', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @driver2_account := (SELECT id FROM vi_account WHERE app_id = @app_id AND open_id = 'dev_driver_002');

INSERT INTO vi_account_security (account_id, session_epoch, permission_version, security_status, changed_at, created_at, version)
SELECT @driver2_account, 0, 0, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_account_security WHERE account_id = @driver2_account);

INSERT INTO vi_driver_profile (account_id, qualification_status, new_assignment_enabled, listening_enabled,
       license_class, license_valid_to, license_long_term, created_at, updated_at, version)
VALUES (@driver2_account, 'APPROVED', 1, 1, 'C1', DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 5 YEAR), 0,
        UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE qualification_status = 'APPROVED', new_assignment_enabled = 1, listening_enabled = 1,
       updated_at = UTC_TIMESTAMP(3), version = version + 1;

-- ---------------------------------------------------------------- 检测点作业员（需后续员工绑定）
INSERT INTO vi_account (app_id, open_id, union_id, nickname, status, created_at, updated_at, version)
VALUES (@app_id, 'dev_station_001', NULL, '演示检测点·张工', 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = 'ACTIVE', updated_at = UTC_TIMESTAMP(3), version = version + 1;

SET @station_account := (SELECT id FROM vi_account WHERE app_id = @app_id AND open_id = 'dev_station_001');

INSERT INTO vi_account_security (account_id, session_epoch, permission_version, security_status, changed_at, created_at, version)
SELECT @station_account, 0, 0, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_account_security WHERE account_id = @station_account);

INSERT INTO vi_owner_profile (account_id, service_name, service_flag, created_at, updated_at, version)
SELECT @station_account, '演示检测点·张工', 'NORMAL', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM vi_owner_profile WHERE account_id = @station_account);

-- 站点视角需要「员工绑定 + 站点授权」两件套。演示环境把该账号绑到超管系统账号，
-- 并授权 1 号检测点；生产环境必须走小程序「员工绑定」+ 后台「站点授权」正式流程。
SET @demo_sys_user := (SELECT user_id FROM sys_user WHERE user_name = 'admin' AND del_flag = '0' LIMIT 1);

INSERT INTO vi_employee_binding (account_id, sys_user_id, bound_by, bound_at, created_at, updated_at, version)
SELECT @station_account, @demo_sys_user, @demo_sys_user, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0
FROM DUAL
WHERE @demo_sys_user IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM vi_employee_binding WHERE account_id = @station_account AND revoked_at IS NULL);

INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason, created_at, updated_at, version)
SELECT @demo_sys_user, s.id, 1, @demo_sys_user, '演示数据：站点作业权限', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0
FROM vi_station s
WHERE @demo_sys_user IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM vi_station_grant g WHERE g.sys_user_id = @demo_sys_user AND g.station_id = s.id);

-- ---------------------------------------------------------------- 校验输出
SELECT 'account' AS kind, id, open_id, nickname FROM vi_account WHERE app_id = @app_id ORDER BY id;
SELECT 'driver' AS kind, d.id AS driver_id, a.open_id, d.qualification_status, d.listening_enabled, d.new_assignment_enabled
FROM vi_driver_profile d JOIN vi_account a ON a.id = d.account_id WHERE a.app_id = @app_id;
