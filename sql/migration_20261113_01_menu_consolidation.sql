-- =====================================================================
-- 侧栏导航整理：首页与运营工作台合并、系统监控/系统工具并入系统管理
--
-- 背景：侧栏一级目录过多（首页 / 荣利业务 / 系统管理 / 系统监控 / 系统工具），
--       「运营工作台」与「首页」都在回答「今天要做什么」，两处待办口径不同却都占首屏。
--       本次把两张页面合并为「首页」一个入口，并把系统类目录收进「系统管理」。
--
-- 前置：migration_20261022_01_rongli_menu_groups.sql 已执行（运营工作台 2115 存在，
--       且已挂到业务处理分组下）。若库中尚无 2115，本脚本的第三步不影响其它步骤。
-- 执行：mysql -h127.0.0.1 -P3306 -uroot -p xinyi < migration_20261113_01_menu_consolidation.sql
-- 可重复执行：全部为幂等 UPDATE / INSERT IGNORE，不改 menu_id，不删任何授权关系。
-- 备份：涉及 sys_menu、sys_role_menu 两张基础表，执行前请先备份：
--       mysqldump -uroot -p xinyi sys_menu sys_role_menu > menu_backup_20261113.sql
--
-- 回滚（按需手工执行）：
--   UPDATE sys_menu SET parent_id = 0, path = 'monitor', order_num = 2 WHERE menu_id = 2;
--   UPDATE sys_menu SET parent_id = 0, path = 'tool',    order_num = 3 WHERE menu_id = 3;
--   UPDATE sys_menu SET visible = '0' WHERE menu_id = 2115;
-- =====================================================================

-- ---------------------------------------------------------------- 一、系统监控、系统工具挂到系统管理下
-- 路径写成带前导斜杠的绝对路径：
--   侧栏层级由「系统管理 > 系统监控 > 在线用户」表达，但页面 URL 仍是 /monitor/online。
--   若改成相对路径 monitor，URL 会变成 /system/monitor/online，前端 meta.activeMenu、
--   首页快捷入口、历史收藏与角色授权里的菜单路径会一起失效。
-- 前端 Sidebar/SidebarItem.vue 的 resolvePath 已支持子菜单绝对路径。
UPDATE sys_menu
   SET parent_id = 1,
       path = '/monitor',
       order_num = 10,
       update_by = 'admin',
       update_time = sysdate(),
       remark = '系统监控目录（2026-11-13 起归入系统管理，URL 保持 /monitor）'
 WHERE menu_id = 2;

UPDATE sys_menu
   SET parent_id = 1,
       path = '/tool',
       order_num = 11,
       update_by = 'admin',
       update_time = sysdate(),
       remark = '系统工具目录（2026-11-13 起归入系统管理，URL 保持 /tool）'
 WHERE menu_id = 3;

-- ---------------------------------------------------------------- 二、补授「系统管理」父目录
-- 侧栏按父子树渲染：只授了「系统监控」但没授「系统管理」的角色，改变父级后整棵子树会从侧栏消失。
-- 这里为持有监控/工具目录的角色补齐父目录（已有则跳过，主键 role_id + menu_id 保证幂等）。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT role_id, 1 FROM sys_role_menu WHERE menu_id IN (2, 3);

-- ---------------------------------------------------------------- 三、运营工作台并入首页
-- 页面组件已改为复用 /index（views/rongli/dashboard/index.vue 只剩薄壳），
-- 这里只把菜单隐藏：路由仍注册、授权关系仍在，旧链接 /rongli/business/dashboard 不会 404，
-- 但侧栏不再出现第二个「今天要做什么」的入口。
UPDATE sys_menu
   SET visible = '1',
       update_by = 'admin',
       update_time = sysdate(),
       remark = '已并入首页（/index），仅保留旧地址 /rongli/business/dashboard 兼容'
 WHERE menu_id = 2115;

-- ---------------------------------------------------------------- 四、一级目录排序
-- 业务在前、系统在后：运营每天先看订单，管理类菜单固定压在下面。
UPDATE sys_menu SET order_num = 1, update_by = 'admin', update_time = sysdate() WHERE menu_id = 2100;
UPDATE sys_menu SET order_num = 2, update_by = 'admin', update_time = sysdate() WHERE menu_id = 1;

-- ---------------------------------------------------------------- 五、修正调度菜单图标
-- 2107 原图标名 send 在 xinyi-ui/src/assets/icons/svg 下并不存在，侧栏会渲染成空白，
-- 三层菜单展开后更明显。改用已有的 job 图标（时段调度语义），与同级菜单图标齐全度一致。
UPDATE sys_menu
   SET icon = 'job',
       update_by = 'admin',
       update_time = sysdate()
 WHERE menu_id = 2107 AND icon = 'send';

-- ---------------------------------------------------------------- 六、执行后校验
-- 1) 目录归属与路径；期望：两行 parent_id = 1，path 分别为 /monitor、/tool
SELECT menu_id, menu_name, parent_id, path, order_num, visible
  FROM sys_menu WHERE menu_id IN (2, 3) ORDER BY menu_id;

-- 2) 系统管理下的直接子项，期望顺序为 用户/角色/菜单/部门/岗位/字典/参数/公告/日志/系统监控/系统工具
SELECT menu_id, menu_name, menu_type, order_num, path
  FROM sys_menu WHERE parent_id = 1 AND menu_type IN ('M', 'C') ORDER BY order_num;

-- 3) 运营工作台应为隐藏；期望 visible = '1'
SELECT menu_id, menu_name, parent_id, visible FROM sys_menu WHERE menu_id = 2115;

-- 4) 持有监控/工具目录但缺「系统管理」父目录的角色数；期望 0
SELECT COUNT(DISTINCT rm.role_id) AS role_missing_system_parent
  FROM sys_role_menu rm
 WHERE rm.menu_id IN (2, 3)
   AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 1);

-- 5) 一级可见目录；期望顺序 荣利业务 / 系统管理 / 若依官网（首页由前端静态路由提供，不在本表）
SELECT menu_id, menu_name, order_num FROM sys_menu WHERE parent_id = 0 AND visible = '0' ORDER BY order_num;
