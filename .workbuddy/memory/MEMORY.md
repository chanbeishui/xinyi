# 项目长期记忆

## 技术架构
- Vue 3 + Vite 6 + TypeScript + Element Plus 2.13.1 + Pinia
- RuoYi-Vue3 框架：constantRoutes + 数据库驱动的动态路由
- Vite dev server 运行在 port 3000（非默认 5173）
- 后端 port 8080，MySQL localhost:3307/xinyi

## 重要 Bug 修复记录

### 路由名称冲突（2026-07-30）
- **问题**：constantRoutes 中的路由 `name` 与数据库动态路由生成的 `name` 冲突，导致 Vue Router 4 `addRoute` 时移除已有同名路由
- **根因**：后端 `SysMenuServiceImpl.getRouteName()` 将 menu path 首字母大写作为路由 name。菜单 path=`index` → name=`Index`，与首页路由 name=`Index` 冲突
- **修复**：1) 删除冗余的 constantRoutes（数据库已有对应菜单）；2) 首页路由 name 从 `Index` 改为 `Home`
- **教训**：在 RuoYi 框架中新增 constantRoutes 时，避免使用与数据库菜单 path 首字母大写后相同的 name

## 数据大屏与档案室管理
- 数据大屏菜单：menu_id=2024(path=dataScreen), 2025(管理层BI看板), 2026(档案室管理)
- 档案室页面：`src/views/archiveRoom/`，3 个档案室可切换，SVG 平面图 + fuse.js 搜索
- BI 看板页面：`src/views/dataScreen/`，ECharts 图表
- 两页面均为纯前端静态数据，未接后端 API
