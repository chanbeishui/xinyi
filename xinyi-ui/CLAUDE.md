# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是 xinyi（若依 RuoYi v3.9.2 衍生）管理系统的前端模块，技术栈为 Vue 3 + TypeScript + Vite 6 + Element Plus + Pinia + Vue Router 4。本目录是 Maven 多模块仓库（仓库根为上一级目录 `xinyi/`）中的前端部分，仓库根目录的 `AGENTS.md` 包含整仓贡献指南（Java 后端模块划分、提交规范等），改动前先阅读它。

## 常用命令

```bash
npm run dev          # 启动 Vite 开发服务（端口 5173，自动打开浏览器）
npm run build:prod   # 生产构建（输出 dist/）
npm run build:stage  # 预发布构建（--mode staging）
npm run preview      # 预览构建产物
```

没有配置 lint 或测试框架；前端改动以 `npm run build:prod` 通过作为验证。Windows 下如果 npm 指向错误的全局目录，先把 `npm_config_prefix` 设置为 Node 安装目录。

## 后端联调

- 开发服务器把 `/dev-api` 代理到 `vite.config.ts` 中硬编码的 `baseUrl`（默认 `http://localhost:8080`，即本地 Spring Boot 后端），并去掉 `/dev-api` 前缀。
- 前端 axios 实例的 baseURL 来自 `.env.*` 的 `VITE_APP_BASE_API`（开发环境为 `/dev-api`）。
- 改后端端口或接口路径时，必须同步检查 `vite.config.ts` 的 proxy 配置。

## 架构要点

**权限与动态路由**（读 `src/permission.ts` 即可理解全貌）：
- 全局前置守卫：有 token 且用户信息未加载时，调 `useUserStore().getInfo()` 拉用户信息，再由 `usePermissionStore().generateRoutes()` 根据后端返回的菜单生成路由并通过 `router.addRoute()` 动态注册，随后重放目标导航。
- 静态公共路由（login、register、404、401 等）在 `src/router/index.ts` 的 `constantRoutes`；业务路由由后端菜单驱动。
- 锁屏功能（`useLockStore`）在守卫中强制跳转 `/lock`。

**请求层**（`src/utils/request.ts`）：axios 实例统一注入 `Authorization: Bearer <token>`（`src/utils/auth.ts` 从 cookie 读取），GET 参数经 `tansParams` 序列化，POST/PUT 默认做 1 秒窗口的防重复提交校验（可用请求头 `repeatSubmit: false` 关闭）；响应按 `code` 走 `src/utils/errorCode.ts` 的错误码表，401 触发重新登录流程。`download()` 处理二进制流下载（blob 校验 + file-saver）。

**自动导入**：`unplugin-auto-import` 自动导入 Vue/Vue Router/Pinia 组合式 API 和 `@vueuse/core`（声明文件为根目录 `auto-imports.d.ts`，改插件配置后需重新生成），SFC 中无需手动 `import { ref } from 'vue'`。SVG 图标经 `vite-plugin-svg-icons` 以 `virtual:svg-icons-register` 注册，用 `<svg-icon icon-class="名字" />` 引用（源文件在 `src/assets/icons/svg`）。

**全局注入**：`main.ts` 把 `useDict`、`parseTime`、`handleTree`、`download` 等工具挂到 `app.config.globalProperties`，并把 `Pagination`、`DictTag`、`RightToolbar`、`Editor`、`FileUpload`/`ImageUpload`/`ImagePreview` 注册为全局组件——列表页直接可用，无需 import。

**状态与目录**：
- `src/store/modules/`：Pinia 模块（user、permission、settings、tagsView、dict、lock 等），用 `useXxxStore()` 调用。
- `src/api/`：按业务域分组的后端接口封装；`src/views/`：页面（对应后端菜单路径）；`src/layout/`：整体布局（侧边栏、Navbar、TagsView）。
- `src/utils/ruoyi.ts`：若依系通用工具（时间格式化、树处理、字典翻译、日期范围拼接等）。
- `src/utils/dict.ts` 的 `useDict()` 是字典数据的标准取法（依赖 dict store 缓存）。
- 权限指令 `v-hasPermi` / `v-hasRole` 在 `src/directive/`，控制按钮级权限。

**设置**：`src/settings.ts` 是布局默认值（导航模式、tagsView、主题），页面标题来自 `VITE_APP_TITLE`。

## 代码规范（来自仓库根 AGENTS.md）

- Vue/TS 文件 2 空格缩进；页面目录小写，组件 PascalCase 或遵循目录已有风格。
- 新增代码注释用中文，且只在逻辑不直观时添加。
- 提交信息用简洁祈使句（如 `Fix Vite dev port`）。
- 不要提交凭据、数据库密码、构建产物。
