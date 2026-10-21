-- 机动车检测预约 V6.0：必要业务字典种子（vehicle_type 计价车型 / inspection_type 检测类型）
-- 用途：为 vi_vehicle.vehicle_type_code、vi_price_rule.vehicle_type_code/inspection_type_code、vi_station.supported_vehicle_codes 提供框架字典类型。
-- 适用前置：已执行 ry_20260417.sql（含 sys_dict_type/sys_dict_data）；在 sql/migration_20261021_vehicle_initial.sql（vi 建表）之后。
-- 执行顺序：vehicle_initial → 本脚本 → （E05）vehicle_menu 菜单/权限种子。
-- 备份要求：执行前备份 sys_dict_type / sys_dict_data。
-- 可重复执行：是（按 dict_type 与 dict_type+dict_value 双重判存，重复不产生重复数据；不硬造 dict_id/dict_code，用自增）。
-- 数据安全：仅 INSERT 字典；不硬造已有 sys ID；不默认授予任何角色权限；样本值为业务起点，非生产默认，业务方按需增删启停。
-- 说明：车型/检测类型清单是运营配置（PRD C04/17.1），此处仅建字典类型与少量示例项；字典新增不自动代表检测点支持该车辆，也不自动产生价格或驾驶资格兼容。

START TRANSACTION;

-- ========== 字典类型 ==========
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '计价车型', 'vehicle_type', '0', 'vehicle-migration', NOW(), '机动车检测预约计价车型字典（业务可增删启停）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'vehicle_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '检测类型', 'inspection_type', '0', 'vehicle-migration', NOW(), '机动车检测类型/套餐字典（业务可增删启停）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'inspection_type');

-- ========== 字典数据：计价车型（示例起点，非生产默认）==========
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 1, '小型轿车', 'SMALL_SEDAN', 'vehicle_type', 'N', '0', 'vehicle-migration', NOW(), '示例，业务方按需调整'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='vehicle_type' AND dict_value='SMALL_SEDAN');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 2, 'SUV', 'SUV', 'vehicle_type', 'N', '0', 'vehicle-migration', NOW(), '示例，业务方按需调整'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='vehicle_type' AND dict_value='SUV');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 3, 'MPV', 'MPV', 'vehicle_type', 'N', '0', 'vehicle-migration', NOW(), '示例，业务方按需调整'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='vehicle_type' AND dict_value='MPV');

-- ========== 字典数据：检测类型（示例起点，非生产默认）==========
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 1, '年检', 'ANNUAL', 'inspection_type', 'N', '0', 'vehicle-migration', NOW(), '示例，业务方按需调整'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='inspection_type' AND dict_value='ANNUAL');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 2, '转籍检测', 'TRANSFER', 'inspection_type', 'N', '0', 'vehicle-migration', NOW(), '示例，业务方按需调整'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='inspection_type' AND dict_value='TRANSFER');

COMMIT;

-- 执行后校验：
--   SELECT dict_type, COUNT(*) FROM sys_dict_data WHERE dict_type IN ('vehicle_type','inspection_type') GROUP BY dict_type;
--   期望 vehicle_type=3, inspection_type=2（示例起点）。
