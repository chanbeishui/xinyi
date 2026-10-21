# 项目长期记忆

## 技术架构
- Vue 3 + Vite 6 + TypeScript + Element Plus 2.13.1 + Pinia
- RuoYi-Vue3 框架：constantRoutes + 数据库驱动的动态路由
- Vite dev server 运行在 port 3000（非默认 5173）
- 后端 port 8080，MySQL localhost:3306/xinyi（2026-09 起从 3307 迁到 3306，brew MySQL 9.6，root 密码 chenbeishui）
- 多部门任职体系已落地：sys_user_dept 关联表 + sys_user.management_scope/auth_version + sys_security_outbox/audit_log（详见 sql/migration_multi_dept_authorization.sql），规范见 AGENTS.md 部门数据权限章节

## 重要 Bug 修复记录

### 路由名称冲突（2026-07-30）
- **问题**：constantRoutes 中的路由 `name` 与数据库动态路由生成的 `name` 冲突，导致 Vue Router 4 `addRoute` 时移除已有同名路由
- **根因**：后端 `SysMenuServiceImpl.getRouteName()` 将 menu path 首字母大写作为路由 name。菜单 path=`index` → name=`Index`，与首页路由 name=`Index` 冲突
- **修复**：1) 删除冗余的 constantRoutes（数据库已有对应菜单）；2) 首页路由 name 从 `Index` 改为 `Home`
- **教训**：在 RuoYi 框架中新增 constantRoutes 时，避免使用与数据库菜单 path 首字母大写后相同的 name

## 荣利机动车检测预约（2026-10-20 已全量回滚）
- 目标版本：**`890cfbbfc5e0db404a932ab5ef8b449d07c7f444`**（荣利预约小程序初始版代码），
  即 M0 commit `95319ad` 的父提交。
- **本地**与**远程 GitHub**（`origin/rongli-wx`）均已回到该版本，两者一致。
- 回滚前 M1 未提交的工作区改动（后端 `xinyi-rongli/src/main/**`、后台 `xinyi-ui/src/views|api/rongli/**`、
  小程序 `xinyi-rongli-wx/src/**`、增量 SQL、ADR/门禁文档）**已于 2026-10-20 11:28 被删除且从未提交，无法找回**。
  目录 `xinyi-rongli/`、`xinyi-rongli-wx/` 已整体删除。
- **找回 M0 的方式**（备份仍在，均在本地）：
  - 分支 `backup/pre-m0-revert-20261020` → `95319ad`
  - 标签 `backup-m0-95319ad` → `95319ad`
  - 命令：`git reset --hard 95319ad`（或 `backup-m0-95319ad`）
  注意：远程已强推，GitHub 上已看不到 95319ad；如需云端备份要 `git push origin backup/pre-m0-revert-20261020`。
- **踩坑（重要）**：本地 `reset --hard` 后若执行 `git pull`，会与远程旧指针自动合并产生 merge commit
  （本次是 `fdfc6ef`），已回退的内容会被重新拉回。**远程必须同步回退，否则 pull 一次就前功尽弃。**
- 数据库已同步回滚：DROP 全部 13 张 `vi_*` 表、删除 `sys_menu` 2100-2114 及超管授权 15 条；
  按规范**保留** `sys_security_audit_log` 中 10 条荣利审计留痕。
  回滚脚本当时按部长指示已删除；全库备份：`E:\codex\_backup\xinyi_20261020_pre_m0_revert.sql`。
- 待办（重启该项目时）：荣利在 `sys_dept` 的部门归属、后台岗位角色划分、M2a 的 6 项业务参数
  （客户计价语义、时段粒度与容量、站点清单、车型与检测项目分类、服务地址规则、取消改约规则）。

## 数据大屏与档案室管理
- 数据大屏菜单：menu_id=2024(path=dataScreen), 2025(管理层BI看板), 2026(档案室管理)
- 档案室页面：`src/views/archiveRoom/`，3 个档案室可切换，SVG 平面图 + fuse.js 搜索
- BI 看板页面：`src/views/dataScreen/`，ECharts 图表
- 两页面均为纯前端静态数据，未接后端 API
