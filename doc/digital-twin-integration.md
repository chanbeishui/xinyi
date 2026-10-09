# 数字孪生接入说明

## 本次变更

- 新增“数字孪生”一级菜单，以及“数字档案室”“智慧仪器室”“业务点分布图”三个子菜单。
- 新增两个数字孪生前端生产构建，并通过同源 `iframe` 嵌入若依内容区。
- 将业务点分布图作为若依原生 Vue 页面接入数字孪生菜单，支持粤桂总览和茂名市级巡检。
- 提供可独立导入、可重复执行的完整菜单 SQL 数据包。
- 保留数字孪生子项目的独立资源目录，后续可分别替换构建产物，不影响若依其他业务页面。

本次提交不包含本机数据库账号密码、开发端口、运行日志、IDE 配置、`node_modules` 或 `dist` 等环境相关文件。

## 菜单结构

- 数字孪生
- 数字档案室
- 智慧仪器室
- 业务点分布图

菜单完全由数据库 `sys_menu` 生成，前端路由中不写死菜单项。

## 已接入文件

- `xinyi-ui/public/digital-twin/archive-room/`：数字档案室生产构建
- `xinyi-ui/public/digital-twin/instrument-room/`：智慧仪器室生产构建
- `xinyi-ui/src/components/DigitalTwinFrame/index.vue`：若依内嵌容器
- `xinyi-ui/src/views/digital-twin/archive-room/index.vue`：数字档案室菜单页面
- `xinyi-ui/src/views/digital-twin/instrument-room/index.vue`：智慧仪器室菜单页面
- `xinyi-ui/src/views/bizMap/`：业务点分布图及地图交互组件
- `sql/migration_20260804_digital_twin_menu.sql`：数字孪生完整菜单与默认角色授权数据包

## 数据库初始化

已有数据库执行：

```sql
source sql/migration_20260804_digital_twin_menu.sql;
```

脚本可重复执行，不会重复创建菜单或默认角色授权。脚本末尾会返回菜单和默认角色授权校验结果，正常情况下两组结果均为 4 条。执行后退出并重新登录。

新数据库先导入 `sql/ry_20260417.sql`，再执行同一份菜单数据包；已有数据库直接执行菜单数据包。两种安装方式最终得到一致的菜单和默认角色授权。

管理员角色由若依内置逻辑自动拥有全部菜单；迁移脚本同时授权默认普通角色 `role_id = 2`。其他自定义角色请在“系统管理 / 角色管理”中按需勾选“数字孪生”。

## 更新子应用

数字档案室在源项目执行 `npm run build` 后，将 `dist/client/` 内容覆盖到：

```text
xinyi-ui/public/digital-twin/archive-room/
```

智慧仪器室在源项目执行 `npm run build` 后，将 `dist/` 内容覆盖到：

```text
xinyi-ui/public/digital-twin/instrument-room/
```

最后在 `xinyi-ui` 执行：

```bash
npm run build:prod
```
