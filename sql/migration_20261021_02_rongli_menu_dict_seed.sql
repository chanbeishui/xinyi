-- =============================================================================
-- 荣利机动车检测预约：菜单、字典、业务配置与演示基础数据
-- 用途：为后台管理端注册菜单与权限标识，注册车型/检测类型字典，写入派单、支付、
--       服务确认等业务参数，并准备 1 个可用检测点与已发布的演示计价/代驾费用规则。
-- 前置版本：必须先执行 migration_20261021_01_rongli_vi_business_tables.sql（依赖 vi_station 等表）。
-- 执行顺序：02（本脚本）。
-- 备份要求：执行前备份目标库；脚本会写入 sys_menu / sys_dict_type / sys_dict_data / sys_config。
-- 可重复执行：是（全部使用固定主键 + ON DUPLICATE KEY UPDATE / NOT EXISTS 保护，重复执行不产生重复行）。
-- 说明：演示计价数值只用于联调，不是真实费率；上线前必须由运营在后台重新配置并发布。
-- =============================================================================
SET NAMES utf8mb4;

-- ---------------------------------------------------------------- 字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES (300, '机动车车型', 'vehicle_type', '0', 'admin', sysdate(), '荣利预约：车型字典，后台可新增与启停'),
       (301, '检测业务类型', 'inspection_type', '0', 'admin', sysdate(), '荣利预约：检测业务类型'),
       (302, '检测结论', 'rongli_conclusion', '0', 'admin', sysdate(), '荣利预约：结构化检测结论'),
       (303, '订单履约状态', 'rongli_order_status', '0', 'admin', sysdate(), '荣利预约：主履约状态'),
       (304, '取消状态', 'rongli_cancel_status', '0', 'admin', sysdate(), '荣利预约：取消申请状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), remark = VALUES(remark);

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
 (3001, 1, '轿车',   'sedan',  'vehicle_type', '', 'default', 'Y', '0', 'admin', sysdate(), '5座及以下轿车'),
 (3002, 2, 'SUV',    'suv',    'vehicle_type', '', 'default', 'N', '0', 'admin', sysdate(), '运动型多用途车'),
 (3003, 3, '商务车', 'mpv',    'vehicle_type', '', 'default', 'N', '0', 'admin', sysdate(), '7座商务车'),
 (3004, 4, '皮卡',   'pickup', 'vehicle_type', '', 'default', 'N', '0', 'admin', sysdate(), '轻型多用途货车'),
 (3005, 5, '面包车', 'van',    'vehicle_type', '', 'default', 'N', '0', 'admin', sysdate(), '微型客车'),
 (3011, 1, '定期检验', 'ANNUAL',    'inspection_type', '', 'default', 'Y', '0', 'admin', sysdate(), '在用机动车定期安全技术检验'),
 (3012, 2, '营运检验', 'OPERATION', 'inspection_type', '', 'default', 'N', '0', 'admin', sysdate(), '营运车辆检验'),
 (3021, 1, '合格',   'PASS', 'rongli_conclusion', '', 'success', 'N', '0', 'admin', sysdate(), '检测结论合格'),
 (3022, 2, '不合格', 'FAIL', 'rongli_conclusion', '', 'danger',  'N', '0', 'admin', sysdate(), '检测结论不合格'),
 (3031, 1, '待支付',        'PENDING_PAY',       'rongli_order_status', '', 'warning',   'N', '0', 'admin', sysdate(), ''),
 (3032, 2, '预约成功待安排', 'SCHEDULED',         'rongli_order_status', '', 'info',      'N', '0', 'admin', sysdate(), ''),
 (3033, 3, '待派单',        'PENDING_DISPATCH',  'rongli_order_status', '', 'info',      'N', '0', 'admin', sysdate(), ''),
 (3034, 4, '派单中',        'DISPATCHING',       'rongli_order_status', '', 'info',      'N', '0', 'admin', sysdate(), ''),
 (3035, 5, '待人工安排',     'MANUAL_DISPATCH',   'rongli_order_status', '', 'warning',   'N', '0', 'admin', sysdate(), ''),
 (3036, 6, '代驾已接单',     'ACCEPTED',          'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3037, 7, '代驾前往接车',   'GOING_PICKUP',      'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3038, 8, '代驾已到达',     'ARRIVED_PICKUP',    'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3039, 9, '已接车',        'PICKED_UP',         'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3040, 10, '已到检测点',    'ARRIVED_STATION',   'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3041, 11, '检测中',        'INSPECTING',        'rongli_order_status', '', 'success',   'N', '0', 'admin', sysdate(), ''),
 (3042, 12, '检测完成待取车', 'INSPECTED',         'rongli_order_status', '', 'success',   'N', '0', 'admin', sysdate(), ''),
 (3043, 13, '返还中',        'CAR_TAKEN',         'rongli_order_status', '', 'primary',   'N', '0', 'admin', sysdate(), ''),
 (3044, 14, '已归还待确认',   'WAIT_CONFIRM',      'rongli_order_status', '', 'warning',   'N', '0', 'admin', sysdate(), ''),
 (3045, 15, '已完成',        'COMPLETED',         'rongli_order_status', '', 'success',   'N', '0', 'admin', sysdate(), ''),
 (3046, 16, '已取消',        'CANCELLED',         'rongli_order_status', '', 'danger',    'N', '0', 'admin', sysdate(), ''),
 (3047, 17, '未支付已关闭',   'CLOSED',            'rongli_order_status', '', 'info',      'N', '0', 'admin', sysdate(), ''),
 (3051, 1, '无取消申请', 'NONE',                'rongli_cancel_status', '', 'info',    'N', '0', 'admin', sysdate(), ''),
 (3052, 2, '取消待审核', 'APPLIED',             'rongli_cancel_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (3053, 3, '审核驳回',   'REJECTED',            'rongli_cancel_status', '', 'danger',  'N', '0', 'admin', sysdate(), ''),
 (3054, 4, '待归还车辆', 'APPROVED_WAIT_RETURN', 'rongli_cancel_status', '', 'danger',  'N', '0', 'admin', sysdate(), ''),
 (3055, 5, '取消已办结', 'COMPLETED',           'rongli_cancel_status', '', 'success', 'N', '0', 'admin', sysdate(), '')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), dict_value = VALUES(dict_value), remark = VALUES(remark);

-- ---------------------------------------------------------------- 业务配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES
 (300, '荣利-派单提前分钟', 'rongli.dispatch.leadMinutes', '30', 'N', 'admin', sysdate(), '预约上门时段开始前 N 分钟启动派单'),
 (301, '荣利-支付超时分钟', 'rongli.pay.expireMinutes', '30', 'N', 'admin', sysdate(), '待支付订单创建后 N 分钟关闭'),
 (302, '荣利-自动完成小时', 'rongli.confirm.autoHours', '24', 'N', 'admin', sysdate(), '实际归还后 N 小时无争议自动完成'),
 (303, '荣利-邀约有效期秒', 'rongli.invite.expireSeconds', '60', 'N', 'admin', sysdate(), '单人邀约默认 60 秒'),
 (304, '荣利-派单半径米', 'rongli.dispatch.radiusMeter', '8000', 'N', 'admin', sysdate(), '候选司机派单半径'),
 (305, '荣利-自动派单总截止分钟', 'rongli.dispatch.totalMinutes', '5', 'N', 'admin', sysdate(), '超过后转待人工安排'),
 (306, '荣利-服务协议版本', 'rongli.service.termsVersion', 'v1', 'N', 'admin', sysdate(), '下单时写入订单快照'),
 (307, '荣利-支付通道', 'rongli.payment.channel', 'MOCK', 'N', 'admin', sysdate(), 'MOCK=联调模拟通道；WECHAT=真实微信支付（需 L01 凭据）'),
 (308, '荣利-位置新鲜秒', 'rongli.driver.locationFreshSeconds', '600', 'N', 'admin', sysdate(), '司机位置超过该时长视为不新鲜')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), remark = VALUES(remark);

-- ---------------------------------------------------------------- 菜单
-- 仅重建本脚本负责的菜单区间；2115+ 由后续增量脚本（03）维护，避免重跑时误删
DELETE FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2114;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 (2100, '车辆检测预约', 0, 6, 'rongli', NULL, '', '', 1, 0, 'M', '0', '0', '', 'car', 'admin', sysdate(), '', NULL, '荣利机动车检测预约目录'),
 (2101, '预约订单', 2100, 1, 'order', 'rongli/order/index', '', '', 1, 0, 'C', '0', '0', 'biz:order:list', 'form', 'admin', sysdate(), '', NULL, '订单列表与全生命周期操作'),
 (2102, '车辆档案', 2100, 2, 'vehicle', 'rongli/vehicle/index', '', '', 1, 0, 'C', '0', '0', 'biz:vehicle:list', 'guide', 'admin', sysdate(), '', NULL, '车辆独立档案'),
 (2103, '车主档案', 2100, 3, 'owner', 'rongli/owner/index', '', '', 1, 0, 'C', '0', '0', 'biz:owner:list', 'peoples', 'admin', sysdate(), '', NULL, '车主独立档案'),
 (2104, '代驾员', 2100, 4, 'driver', 'rongli/driver/index', '', '', 1, 0, 'C', '0', '0', 'biz:driver:list', 'user', 'admin', sysdate(), '', NULL, '代驾资格、听单与在手承接'),
 (2105, '检测点', 2100, 5, 'station', 'rongli/station/index', '', '', 1, 0, 'C', '0', '0', 'biz:station:list', 'tree', 'admin', sysdate(), '', NULL, '检测点与容量'),
 (2106, '计价规则', 2100, 6, 'price', 'rongli/price/index', '', '', 1, 0, 'C', '0', '0', 'biz:price:list', 'money', 'admin', sysdate(), '', NULL, '车主侧计价版本'),
 (2107, '派单调度', 2100, 7, 'dispatch', 'rongli/dispatch/index', '', '', 1, 0, 'C', '0', '0', 'biz:dispatch:list', 'send', 'admin', sysdate(), '', NULL, '自动/人工派单与派单参数'),
 (2108, '取消与退款', 2100, 8, 'aftersale', 'rongli/aftersale/index', '', '', 1, 0, 'C', '0', '0', 'biz:aftersale:list', 'money', 'admin', sysdate(), '', NULL, '一次审核的取消退款售后'),
 (2109, '代驾对账', 2100, 9, 'statement', 'rongli/statement/index', '', '', 1, 0, 'C', '0', '0', 'biz:driverstatement:list', 'documentation', 'admin', sysdate(), '', NULL, '对账单、行内核价与付款核实'),
 (2110, '代驾费用规则', 2100, 10, 'feeRule', 'rongli/feeRule/index', '', '', 1, 0, 'C', '0', '0', 'biz:driverfeerule:list', 'edit', 'admin', sysdate(), '', NULL, '代驾报酬规则版本'),
 (2111, '资格审核', 2100, 11, 'qualification', 'rongli/qualification/index', '', '', 1, 0, 'C', '0', '0', 'biz:driverqualification:list', 'checkbox', 'admin', sysdate(), '', NULL, '代驾资格申请审核'),
 (2112, '员工站点授权', 2100, 12, 'stationGrant', 'rongli/stationGrant/index', '', '', 1, 0, 'C', '0', '0', 'biz:stationgrant:list', 'lock', 'admin', sysdate(), '', NULL, '小程序员工跨站操作范围'),
 (2113, '业务配置', 2100, 13, 'config', 'rongli/config/index', '', '', 1, 0, 'C', '0', '0', 'biz:config:query', 'tool', 'admin', sysdate(), '', NULL, '派单、支付与通知参数'),
 (2114, '审计事件', 2100, 14, 'audit', 'rongli/audit/index', '', '', 1, 0, 'C', '0', '0', 'biz:audit:list', 'monitor', 'admin', sysdate(), '', NULL, '不可变业务事实流水');

-- 普通角色（role_id=2）仅授予只读查询，写权限由管理员按岗位另授
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2100 AND 2199 AND (menu_id = 2100 OR perms LIKE '%:list');

-- ---------------------------------------------------------------- 演示基础数据
-- 检测点：dept_id 指向框架部门 100（若依研发部）
INSERT INTO vi_station (id, name, dept_id, address, lng, lat, service_radius_meter, supported_vehicle_codes, business_hours, enabled)
VALUES (1, '茂名集信检测站', 100, '广东省茂名市茂南区站前路 1 号', 110.925000, 21.663000, 30000,
        JSON_ARRAY('sedan','suv','mpv','pickup','van'),
        JSON_OBJECT('weekdays', JSON_ARRAY(1,2,3,4,5,6), 'startHour', 8, 'endHour', 18), 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), enabled = VALUES(enabled);

-- 车主侧计价：每个车型一条已发布版本（演示数值，单位为分）
INSERT INTO vi_price_rule (rule_key, revision, vehicle_type_code, inspection_type_code, status, config_json, effective_from, submitted_by, approved_by)
VALUES
 ('STD_ANNUAL_SEDAN',  1, 'sedan',  'ANNUAL', 'PUBLISHED', JSON_OBJECT('inspectionFeeFen', 30000, 'baseFeeFen', 3000, 'includedMeters', 5000, 'unitMeters', 1000, 'unitPriceFen', 300, 'rounding', 'CEIL'), UTC_TIMESTAMP(3), 1, 1),
 ('STD_ANNUAL_SUV',    1, 'suv',    'ANNUAL', 'PUBLISHED', JSON_OBJECT('inspectionFeeFen', 30000, 'baseFeeFen', 3500, 'includedMeters', 5000, 'unitMeters', 1000, 'unitPriceFen', 350, 'rounding', 'CEIL'), UTC_TIMESTAMP(3), 1, 1),
 ('STD_ANNUAL_MPV',    1, 'mpv',    'ANNUAL', 'PUBLISHED', JSON_OBJECT('inspectionFeeFen', 30000, 'baseFeeFen', 4000, 'includedMeters', 5000, 'unitMeters', 1000, 'unitPriceFen', 400, 'rounding', 'CEIL'), UTC_TIMESTAMP(3), 1, 1),
 ('STD_ANNUAL_PICKUP', 1, 'pickup', 'ANNUAL', 'PUBLISHED', JSON_OBJECT('inspectionFeeFen', 35000, 'baseFeeFen', 4000, 'includedMeters', 5000, 'unitMeters', 1000, 'unitPriceFen', 450, 'rounding', 'CEIL'), UTC_TIMESTAMP(3), 1, 1),
 ('STD_ANNUAL_VAN',    1, 'van',    'ANNUAL', 'PUBLISHED', JSON_OBJECT('inspectionFeeFen', 32000, 'baseFeeFen', 3800, 'includedMeters', 5000, 'unitMeters', 1000, 'unitPriceFen', 380, 'rounding', 'CEIL'), UTC_TIMESTAMP(3), 1, 1)
ON DUPLICATE KEY UPDATE status = VALUES(status), config_json = VALUES(config_json);

-- 代驾费用规则：演示固定费 + 里程（单位分/米），PAYOR 主体 1
INSERT INTO vi_driver_fee_rule (id, payor_entity_id, rule_code, revision, currency, scenario, scope_json, pricing_json, status, effective_from, created_by, approved_by, approved_at)
VALUES (1, 1, 'DEFAULT_NORMAL', 1, 'CNY', 'NORMAL_RETURN',
        JSON_OBJECT('vehicleTypes', JSON_ARRAY('sedan','suv','mpv','pickup','van')),
        JSON_OBJECT('baseFen', 4000, 'includedMeters', 8000, 'unitMeters', 1000, 'unitPriceFen', 500, 'rounding', 'CEIL'),
        'PUBLISHED', UTC_TIMESTAMP(3), 1, 1, UTC_TIMESTAMP(3)),
       (2, 1, 'DEFAULT_CANCEL', 1, 'CNY', 'CANCEL_RETURN',
        JSON_OBJECT('vehicleTypes', JSON_ARRAY('sedan','suv','mpv','pickup','van')),
        JSON_OBJECT('baseFen', 2000, 'includedMeters', 0, 'unitMeters', 1000, 'unitPriceFen', 0, 'rounding', 'CEIL'),
        'PUBLISHED', UTC_TIMESTAMP(3), 1, 1, UTC_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE status = VALUES(status), pricing_json = VALUES(pricing_json);

-- 执行后校验：应输出 1 个可用检测点、5 条已发布计价、2 条已发布代驾费用规则、15 条菜单
SELECT 'station' AS item, COUNT(*) AS cnt FROM vi_station WHERE enabled = 1
UNION ALL SELECT 'price_rule_published', COUNT(*) FROM vi_price_rule WHERE status = 'PUBLISHED'
UNION ALL SELECT 'driver_fee_rule_published', COUNT(*) FROM vi_driver_fee_rule WHERE status = 'PUBLISHED'
UNION ALL SELECT 'menu', COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2199;
