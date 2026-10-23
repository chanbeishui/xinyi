-- =====================================================================
-- 荣利机动车检测预约 · 后台侧栏分组与补齐页面
--
-- 依据：资料包 02_UI/全量页面图 后台画板（B02 运营工作台等）左侧导航，
--       四组 21 项 + 底部「组织、人员与菜单权限 / 复用现有框架系统管理」。
--
--   业务处理      运营工作台 / 预约订单 / 调度与派单 / 取消与退款 / 异常与争议
--   资料与人员    车辆列表 / 车主列表 / 代驾员列表 / 代驾对账 / 培训课程 / 限制申诉
--   检测与配置    收车与核验 / 检测结果 / 检测点与容量 / 计价与车型 / 员工与站点权限 / 系统配置
--   运行与审计    消息与内容 / 业务统计 / 支付渠道核对 / 审计与访问
--
-- 设计上图未单独列出的页面（派单记录 B06、行驶证核验 B08、检测结果登记 B11、
-- 外部付款核对 B40、对账调整记录 B41、个人对账汇总 B42、车型字典 B33、
-- 治理与导出）作为隐藏路由挂在对应父页面下，由父页面按钮进入，不进侧栏。
--
-- 前置：migration_20261021_01/02/03 已执行。
-- 可重复执行：全部使用 INSERT ... ON DUPLICATE KEY UPDATE，不改动既有 menu_id，
--             因此其它角色已授的菜单关系不会丢失。
-- 注意：分组会改变页面 URL（如 /rongli/order → /rongli/business/order），
--       前端硬编码跳转已同步修改，见 logs/check_routes.py 校验。
-- 执行：mysql -h127.0.0.1 -P3307 -uroot -p xinyi < migration_20261022_01_rongli_menu_groups.sql
-- =====================================================================

-- ---------------------------------------------------------------- 一、四个分组
DELETE FROM sys_menu WHERE menu_id BETWEEN 2190 AND 2193;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 (2190, '业务处理',   2100, 1, 'business', NULL, '', '', 1, 0, 'M', '0', '0', '', 'form',   'admin', sysdate(), '', NULL, '订单、调度、售后与异常受理'),
 (2191, '资料与人员', 2100, 2, 'people',   NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples','admin', sysdate(), '', NULL, '车辆、车主、代驾档案与代驾对账'),
 (2192, '检测与配置', 2100, 3, 'inspect',  NULL, '', '', 1, 0, 'M', '0', '0', '', 'tree',   'admin', sysdate(), '', NULL, '收车、检测结果、检测点与计价配置'),
 (2193, '运行与审计', 2100, 4, 'runtime',  NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor','admin', sysdate(), '', NULL, '消息、统计、支付核对与审计');

-- ---------------------------------------------------------------- 二、页面（含隐藏路由）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 -- 业务处理
 (2115, '运营工作台',     2190, 1, 'dashboard',      'rongli/dashboard/index',      '', '', 1, 0, 'C', '0', '0', 'biz:statistics:list',          'dashboard',   'admin', sysdate(), '', NULL, 'B02 待办与当日预约'),
 (2101, '预约订单',       2190, 2, 'order',          'rongli/order/index',          '', '', 1, 0, 'C', '0', '0', 'biz:order:list',               'form',        'admin', sysdate(), '', NULL, 'B03/B04 订单列表与详情'),
 (2107, '调度与派单',     2190, 3, 'dispatch',       'rongli/dispatch/index',       '', '', 1, 0, 'C', '0', '0', 'biz:dispatch:list',            'send',        'admin', sysdate(), '', NULL, 'B05 调度台与派单参数'),
 (2108, '取消与退款',     2190, 4, 'aftersale',      'rongli/aftersale/index',      '', '', 1, 0, 'C', '0', '0', 'biz:aftersale:list',           'money',       'admin', sysdate(), '', NULL, 'B13/B14 取消退款与统一审核'),
 (2116, '异常与争议',     2190, 5, 'exception',      'rongli/exception/index',      '', '', 1, 0, 'C', '0', '0', 'biz:dispute:list',             'bug',         'admin', sysdate(), '', NULL, 'B15/B30 争议、异常与结果更正'),
 (2121, '派单记录',       2190, 6, 'dispatchRecord', 'rongli/dispatchRecord/index', '', '', 1, 0, 'C', '1', '0', 'biz:dispatch:list',            'list',        'admin', sysdate(), '', NULL, 'B06 隐藏路由，由调度与派单进入'),

 -- 资料与人员
 (2102, '车辆列表',       2191, 1, 'vehicle',          'rongli/vehicle/index',          '', '', 1, 0, 'C', '0', '0', 'biz:vehicle:list',       'guide',         'admin', sysdate(), '', NULL, 'B09 车辆档案'),
 (2103, '车主列表',       2191, 2, 'owner',            'rongli/owner/index',            '', '', 1, 0, 'C', '0', '0', 'biz:owner:list',         'peoples',       'admin', sysdate(), '', NULL, 'B27 车主档案'),
 (2104, '代驾员列表',     2191, 3, 'driver',           'rongli/driver/index',           '', '', 1, 0, 'C', '0', '0', 'biz:driver:list',        'user',          'admin', sysdate(), '', NULL, 'B28 代驾资格与在手承接'),
 (2109, '代驾对账',       2191, 4, 'statement',        'rongli/statement/index',        '', '', 1, 0, 'C', '0', '0', 'biz:driverstatement:list','documentation', 'admin', sysdate(), '', NULL, 'B37/B38 对账单与行内核价'),
 (2118, '培训课程',       2191, 5, 'training',         'rongli/training/index',         '', '', 1, 0, 'C', '0', '0', 'biz:trainingcourse:list','education',     'admin', sysdate(), '', NULL, 'B43/B44 课程版本与题库'),
 (2117, '限制申诉',       2191, 6, 'penalty',          'rongli/penalty/index',          '', '', 1, 0, 'C', '0', '0', 'biz:penalty:list',       'warning',       'admin', sysdate(), '', NULL, 'B45 接单限制与申诉审核'),
 (2126, '外部付款核对',   2191, 7, 'paymentVerify',    'rongli/paymentVerify/index',    '', '', 1, 0, 'C', '1', '0', 'biz:driverpayment:list', 'money',         'admin', sysdate(), '', NULL, 'B40 隐藏路由，由代驾对账进入'),
 (2127, '对账调整记录',   2191, 8, 'adjustment',       'rongli/adjustment/index',       '', '', 1, 0, 'C', '1', '0', 'biz:driveradjust:list',  'edit',          'admin', sysdate(), '', NULL, 'B41 隐藏路由，由代驾对账进入'),
 (2128, '个人对账汇总',   2191, 9, 'reconcileSummary', 'rongli/reconcileSummary/index', '', '', 1, 0, 'C', '1', '0', 'biz:driverstatement:list','chart',         'admin', sysdate(), '', NULL, 'B42 隐藏路由，由代驾对账进入'),
 (2110, '代驾费用规则',   2191, 10,'feeRule',          'rongli/feeRule/index',          '', '', 1, 0, 'C', '1', '0', 'biz:driverfeerule:list', 'edit',          'admin', sysdate(), '', NULL, 'B39 隐藏路由，由代驾对账进入'),
 (2111, '代驾认证审核',   2191, 11,'qualification',    'rongli/qualification/index',    '', '', 1, 0, 'C', '1', '0', 'biz:driverqualification:list','checkbox', 'admin', sysdate(), '', NULL, 'B16 隐藏路由，由代驾员列表进入'),

 -- 检测与配置
 (2122, '收车与核验',     2192, 1, 'receivePicked',  'rongli/receivePicked/index',  '', '', 1, 0, 'C', '0', '0', 'biz:receive:list',  'checkbox',      'admin', sysdate(), '', NULL, 'B07 站点收车登记'),
 (2124, '检测结果',       2192, 2, 'inspectionTodo', 'rongli/inspectionTodo/index', '', '', 1, 0, 'C', '0', '0', 'biz:result:list',   'time-range',    'admin', sysdate(), '', NULL, 'B10 检测结果待办'),
 (2105, '检测点与容量',   2192, 3, 'station',        'rongli/station/index',        '', '', 1, 0, 'C', '0', '0', 'biz:station:list',  'tree',          'admin', sysdate(), '', NULL, 'B17/B19 检测点与排期容量'),
 (2106, '计价与车型',     2192, 4, 'price',          'rongli/price/index',          '', '', 1, 0, 'C', '0', '0', 'biz:price:list',    'money',         'admin', sysdate(), '', NULL, 'B20 计价规则，含车型字典入口'),
 (2112, '员工与站点权限', 2192, 5, 'stationGrant',   'rongli/stationGrant/index',   '', '', 1, 0, 'C', '0', '0', 'biz:stationgrant:list','lock',       'admin', sysdate(), '', NULL, 'B25/B26 员工站点权限与跨站授权'),
 (2113, '系统配置',       2192, 6, 'config',         'rongli/config/index',         '', '', 1, 0, 'C', '0', '0', 'biz:config:query',  'tool',          'admin', sysdate(), '', NULL, 'B24 派单、支付与通知参数'),
 (2123, '行驶证核验',     2192, 7, 'license',        'rongli/license/index',        '', '', 1, 0, 'C', '1', '0', 'biz:license:list',  'documentation', 'admin', sysdate(), '', NULL, 'B08 隐藏路由，由收车与核验进入'),
 (2125, '检测结果登记',   2192, 8, 'resultRegister', 'rongli/resultRegister/index', '', '', 1, 0, 'C', '1', '0', 'biz:result:list',   'edit',          'admin', sysdate(), '', NULL, 'B11 隐藏路由，由检测结果进入'),
 (2129, '车型字典',       2192, 9, 'vehicleType',    'rongli/vehicleType/index',    '', '', 1, 0, 'C', '1', '0', '',                  'education',     'admin', sysdate(), '', NULL, 'B33 隐藏路由，由计价与车型进入'),

 -- 运行与审计
 (2119, '消息与内容',     2193, 1, 'notification',    'rongli/notification/index',    '', '', 1, 0, 'C', '0', '0', 'biz:notificationtemplate:list','message', 'admin', sysdate(), '', NULL, 'B21/B35 模板与发送记录'),
 (2130, '业务统计',       2193, 2, 'statistics',      'rongli/statistics/index',      '', '', 1, 0, 'C', '0', '0', 'biz:statistics:list',          'chart',   'admin', sysdate(), '', NULL, 'B23 预约、履约与资金核对统计'),
 (2131, '支付渠道核对',   2193, 3, 'paymentReconcile','rongli/paymentReconcile/index', '', '', 1, 0, 'C', '0', '0', 'biz:reconciliation:list',     'eye-open','admin', sysdate(), '', NULL, 'B31 支付退款对账异常'),
 (2114, '审计与访问',     2193, 4, 'audit',           'rongli/audit/index',           '', '', 1, 0, 'C', '0', '0', 'biz:audit:list',              'monitor', 'admin', sysdate(), '', NULL, 'B22 安全审计事件'),
 (2120, '治理与导出',     2193, 5, 'governance',      'rongli/governance/index',      '', '', 1, 0, 'C', '1', '0', 'biz:privacycase:list',        'lock',    'admin', sysdate(), '', NULL, 'B32/B34/B36 隐藏路由，由审计与访问进入')
AS new
ON DUPLICATE KEY UPDATE
  menu_name = new.menu_name, parent_id = new.parent_id, order_num = new.order_num,
  path = new.path, component = new.component, route_name = new.route_name,
  is_frame = new.is_frame, is_cache = new.is_cache, menu_type = new.menu_type,
  visible = new.visible, status = new.status, perms = new.perms, icon = new.icon,
  remark = new.remark;

-- 页面级写权限（F 型按钮，供角色按岗位授予）
DELETE FROM sys_menu WHERE menu_id BETWEEN 2231 AND 2232;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
 (2231, '收车确认',     2122, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'biz:receive:edit',      '#', 'admin', sysdate(), '', NULL, '必须校验 4 位交接码，规则与代驾端一致'),
 (2232, '证件原图授权', 2123, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'biz:sensitive:access',  '#', 'admin', sysdate(), '', NULL, '敏感影像单独授权，每次查看逐次留痕');

-- ---------------------------------------------------------------- 三、授权
-- 普通运营角色（role_id=2）只给页面查询，写权限由管理员按岗位另授
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_id BETWEEN 2190 AND 2193;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2101 AND 2131 AND menu_type = 'C';

-- 目录 2100 自身保留，顺序置为 1
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2100;

-- ---------------------------------------------------------------- 四、校验
SELECT 'groups' AS obj, COUNT(*) AS cnt FROM sys_menu WHERE menu_id BETWEEN 2190 AND 2193
UNION ALL SELECT 'business_items', COUNT(*) FROM sys_menu WHERE parent_id = 2190
UNION ALL SELECT 'people_items',   COUNT(*) FROM sys_menu WHERE parent_id = 2191
UNION ALL SELECT 'inspect_items',  COUNT(*) FROM sys_menu WHERE parent_id = 2192
UNION ALL SELECT 'runtime_items',  COUNT(*) FROM sys_menu WHERE parent_id = 2193
UNION ALL SELECT 'visible_items',  COUNT(*) FROM sys_menu WHERE parent_id IN (2190,2191,2192,2193) AND menu_type='C' AND visible='0';
