-- 数据大屏菜单（管理层BI看板、档案室管理）
-- 用途：重建数据库大屏相关菜单（此前为手动插入，未随仓库脚本迁移，重新初始化数据库后丢失）。
-- 适用：已执行 ry_20260417.sql 初始化的数据库；前置无需其他迁移，执行顺序不限。
-- 可重复执行：是（按 menu_id 与 parent+path 双重判存，重复执行不会产生重复数据）。
-- 执行前请备份 sys_menu / sys_role_menu；执行后需退出并重新登录以重新加载动态菜单。

START TRANSACTION;

-- ========== 1. 一级目录：数据大屏 ==========
SET @data_screen_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0 AND path = 'dataScreen'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  2024, '数据大屏', 0, 4, 'dataScreen', NULL, '', 'DataScreen',
  1, 0, 'M', '0', '0', '', 'monitor',
  'admin', SYSDATE(), '', NULL, '数据大屏目录'
WHERE @data_screen_parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2024);

SET @data_screen_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0 AND path = 'dataScreen'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '数据大屏', order_num = 4, route_name = 'DataScreen',
    is_frame = 1, is_cache = 0, menu_type = 'M', visible = '0', status = '0',
    perms = '', icon = 'monitor', update_by = 'admin', update_time = SYSDATE(),
    remark = '数据大屏目录'
WHERE menu_id = @data_screen_parent_id;

-- ========== 2. 管理层BI看板 ==========
SET @bi_dashboard_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @data_screen_parent_id AND path = 'biDashboard'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  2025, '管理层BI看板', @data_screen_parent_id, 1, 'biDashboard',
  'dataScreen/index', '', 'BiDashboard',
  1, 0, 'C', '0', '0', '', 'chart',
  'admin', SYSDATE(), '', NULL, '管理层BI看板（ECharts 数据可视化）'
WHERE @data_screen_parent_id IS NOT NULL
  AND @bi_dashboard_menu_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2025);

SET @bi_dashboard_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @data_screen_parent_id AND path = 'biDashboard'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '管理层BI看板', order_num = 1,
    component = 'dataScreen/index', query = '',
    route_name = 'BiDashboard', is_frame = 1, is_cache = 0,
    menu_type = 'C', visible = '0', status = '0', perms = '',
    icon = 'chart', update_by = 'admin', update_time = SYSDATE(),
    remark = '管理层BI看板（ECharts 数据可视化）'
WHERE menu_id = @bi_dashboard_menu_id;

-- ========== 3. 档案室管理 ==========
SET @archive_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @data_screen_parent_id AND path = 'archiveRoom'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  2026, '档案室管理', @data_screen_parent_id, 2, 'archiveRoom',
  'archiveRoom/index', '', 'ArchiveRoomManage',
  1, 0, 'C', '0', '0', '', 'documentation',
  'admin', SYSDATE(), '', NULL, '档案室平面图管理与检索'
WHERE @data_screen_parent_id IS NOT NULL
  AND @archive_room_menu_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2026);

SET @archive_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @data_screen_parent_id AND path = 'archiveRoom'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '档案室管理', order_num = 2,
    component = 'archiveRoom/index', query = '',
    route_name = 'ArchiveRoomManage', is_frame = 1, is_cache = 0,
    menu_type = 'C', visible = '0', status = '0', perms = '',
    icon = 'documentation', update_by = 'admin', update_time = SYSDATE(),
    remark = '档案室平面图管理与检索'
WHERE menu_id = @archive_room_menu_id;

-- ========== 4. 授权默认普通角色（role_id = 2） ==========
-- 管理员角色（role_id = 1）由若依内置逻辑自动拥有全部菜单。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id
FROM sys_menu menu
WHERE menu.menu_id IN (@data_screen_parent_id, @bi_dashboard_menu_id, @archive_room_menu_id)
  AND EXISTS (SELECT 1 FROM sys_role role WHERE role.role_id = 2)
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu role_menu
    WHERE role_menu.role_id = 2 AND role_menu.menu_id = menu.menu_id
  );

COMMIT;

-- ========== 5. 执行后校验 ==========
-- 校验1：三个菜单必须齐全（预期 3 行）
SELECT menu_id, menu_name, parent_id, path, component, menu_type
FROM sys_menu
WHERE menu_id IN (2024, 2025, 2026)
ORDER BY menu_id;

-- 校验2：普通角色授权必须齐全（预期 3 行）
SELECT COUNT(*) AS role2_grant_count
FROM sys_role_menu
WHERE role_id = 2 AND menu_id IN (2024, 2025, 2026);

-- 执行后请退出并重新登录，使当前用户重新加载动态菜单。
