-- =============================================
-- 增量迁移：数据大屏菜单
-- 用途：在侧边栏添加"数据大屏"目录及"检测BI看板"菜单
-- 前置版本：ry_20260417.sql
-- 执行顺序：在基础初始化脚本之后执行
-- 备份要求：无需特殊备份（仅新增数据，不修改已有数据）
-- 是否允许重复执行：否（重复执行会报主键冲突）
-- =============================================

-- 一级目录：数据大屏
INSERT INTO sys_menu VALUES (2024, '数据大屏', 0, 5, 'dataScreen', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', NULL, '数据大屏目录');

-- 二级菜单：检测BI看板
INSERT INTO sys_menu VALUES (2025, '检测BI看板', 2024, 1, 'index', 'dataScreen/index', '', '', 1, 0, 'C', '0', '0', 'dataScreen:index:list', 'chart', 'admin', sysdate(), '', NULL, '检测行业BI看板数据大屏');

-- 给管理员角色分配权限（role_id=1 为超级管理员）
INSERT INTO sys_role_menu VALUES (1, 2024);
INSERT INTO sys_role_menu VALUES (1, 2025);

-- 校验
SELECT menu_id, menu_name, parent_id, path, component, menu_type, icon
  FROM sys_menu WHERE menu_id IN (2024, 2025);
