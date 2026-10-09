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

## 编码行为准则（默认启用，无需提醒）
- 用户级技能 `karpathy-guidelines`（C:\Users\Administrator\.workbuddy\skills\karpathy-guidelines\）：
  编码前思考 / 简洁优先 / 精准修改 / 目标驱动执行，来源 multica-ai/andrej-karpathy-skills
- 本项目所有开发默认遵守，交付前固定输出四条原则自检
- 冲突优先级：本项目 AGENTS.md 硬性规范（SQL 迁移、部门数据权限与审计）> 该技能原则 > 个人偏好

## 数据大屏与档案室管理
- 数据大屏菜单：menu_id=2024(path=dataScreen), 2025(管理层BI看板), 2026(档案室管理)
- 档案室页面：`src/views/archiveRoom/`，3 个档案室可切换，SVG 平面图 + fuse.js 搜索
- BI 看板页面：`src/views/dataScreen/`，ECharts 图表
- 两页面均为纯前端静态数据，未接后端 API

## 电子签对接（调研中）
- 背景：内部系统（检测报告签字盖章）拟对接 CA 电子签；报告对外出具为法律文书需可靠电子签名，且国企数据敏感不愿整份报告出境
- 结论方向：不采用标准公有云 SaaS（文件上传）模式；优先"私有化部署"或"哈希签名 / 原文不出本地"方案
- 哈希签名原理：本地对报告算 SHA-256 摘要 → 只把摘要传 CA 用企业私钥签名 + 时间戳 → 签名值回传本地 → 本地合成签章 PDF（PAdES 标准）；验证时公钥解密签名值得摘要 A，重算原文哈希得摘要 B，A==B 且证书有效即证明未篡改且确为签名者所签
- 待确认：具体供应商选型（e签宝/法大大私有化与哈希签名能力）、是否走 PAdES 标准、私钥本地 HSM 还是 CA 托管
