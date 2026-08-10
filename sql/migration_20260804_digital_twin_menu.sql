-- 数字孪生菜单与角色授权
-- 适用于已有数据库，可重复执行。
-- 管理员角色（role_id = 1）由若依内置逻辑自动拥有全部菜单；
-- 本脚本同时授权默认普通角色（role_id = 2），其他自定义角色请在角色管理中按需授权。

START TRANSACTION;

SET @digital_twin_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0 AND path = 'digital-twin'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  9000, '数字孪生', 0, 5, 'digital-twin', NULL, '', 'DigitalTwin',
  1, 0, 'M', '0', '0', '', 'monitor',
  'admin', SYSDATE(), '', NULL, '数字孪生目录'
WHERE @digital_twin_parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9000);

SET @digital_twin_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0 AND path = 'digital-twin'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '数字孪生', order_num = 5, route_name = 'DigitalTwin',
    is_frame = 1, is_cache = 0, menu_type = 'M', visible = '0', status = '0',
    perms = '', icon = 'monitor', update_by = 'admin', update_time = SYSDATE(),
    remark = '数字孪生目录'
WHERE menu_id = @digital_twin_parent_id;

SET @archive_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'archive-room'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  9001, '数字档案室', @digital_twin_parent_id, 1, 'archive-room',
  'digital-twin/archive-room/index', '', 'ArchiveRoomDigitalTwin',
  1, 0, 'C', '0', '0', '', 'documentation',
  'admin', SYSDATE(), '', NULL, '数字档案室三维看板'
WHERE @digital_twin_parent_id IS NOT NULL
  AND @archive_room_menu_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9001);

SET @archive_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'archive-room'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '数字档案室', order_num = 1,
    component = 'digital-twin/archive-room/index', query = '',
    route_name = 'ArchiveRoomDigitalTwin', is_frame = 1, is_cache = 0,
    menu_type = 'C', visible = '0', status = '0', perms = '',
    icon = 'documentation', update_by = 'admin', update_time = SYSDATE(),
    remark = '数字档案室三维看板'
WHERE menu_id = @archive_room_menu_id;

SET @instrument_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'instrument-room'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  9002, '智慧仪器室', @digital_twin_parent_id, 2, 'instrument-room',
  'digital-twin/instrument-room/index', '', 'InstrumentRoomDigitalTwin',
  1, 0, 'C', '0', '0', '', 'dashboard',
  'admin', SYSDATE(), '', NULL, '智慧仪器室三维看板'
WHERE @digital_twin_parent_id IS NOT NULL
  AND @instrument_room_menu_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9002);

SET @instrument_room_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'instrument-room'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '智慧仪器室', order_num = 2,
    component = 'digital-twin/instrument-room/index', query = '',
    route_name = 'InstrumentRoomDigitalTwin', is_frame = 1, is_cache = 0,
    menu_type = 'C', visible = '0', status = '0', perms = '',
    icon = 'dashboard', update_by = 'admin', update_time = SYSDATE(),
    remark = '智慧仪器室三维看板'
WHERE menu_id = @instrument_room_menu_id;

SET @business_map_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'business-map'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  9003, '业务点分布图', @digital_twin_parent_id, 3, 'business-map',
  'bizMap/index', '', 'BusinessPointMap',
  1, 0, 'C', '0', '0', '', 'chart',
  'admin', SYSDATE(), '', NULL, '区域业务点分布与自动巡检看板'
WHERE @digital_twin_parent_id IS NOT NULL
  AND @business_map_menu_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9003);

SET @business_map_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @digital_twin_parent_id AND path = 'business-map'
  ORDER BY menu_id
  LIMIT 1
);

UPDATE sys_menu
SET menu_name = '业务点分布图', order_num = 3,
    component = 'bizMap/index', query = '',
    route_name = 'BusinessPointMap', is_frame = 1, is_cache = 0,
    menu_type = 'C', visible = '0', status = '0', perms = '',
    icon = 'chart', update_by = 'admin', update_time = SYSDATE(),
    remark = '区域业务点分布与自动巡检看板'
WHERE menu_id = @business_map_menu_id;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id
FROM sys_menu menu
WHERE menu.menu_id IN (
  @digital_twin_parent_id,
  @archive_room_menu_id,
  @instrument_room_menu_id,
  @business_map_menu_id
)
  AND EXISTS (SELECT 1 FROM sys_role role WHERE role.role_id = 2)
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu role_menu
    WHERE role_menu.role_id = 2 AND role_menu.menu_id = menu.menu_id
  );

COMMIT;

-- 执行后请退出并重新登录，使当前用户重新加载动态菜单。
