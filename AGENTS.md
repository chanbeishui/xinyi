# 仓库贡献指南

## 项目结构与模块划分

本仓库包含 Java 17 Spring Boot 后端和 Vue 3 TypeScript 前端。

- `xinyi-admin/`：后端启动模块，包含控制器和运行配置。
- `xinyi-framework/`：安全、配置、数据源、Web 基础能力。
- `xinyi-system/`、`xinyi-quartz/`、`xinyi-generator/`、`xinyi-common/`：系统业务、定时任务、代码生成和通用工具。
- `xinyi-ui/`：Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus 前端。
- `sql/`：数据库初始化脚本。
- `doc/`：项目文档和使用手册。
- `bin/`：打包、运行等辅助脚本。

Java 代码位于各模块的 `src/main/java`，资源和 MyBatis XML 位于 `src/main/resources`。前端页面在 `xinyi-ui/src/views`，接口封装在 `xinyi-ui/src/api`，组件在 `xinyi-ui/src/components`，静态资源在 `xinyi-ui/src/assets`。

## 构建、测试与本地开发

后端常用命令：

- `mvn clean package`：构建 Maven 多模块项目。
- `mvn test`：运行后端测试。
- `mvn spring-boot:run -pl xinyi-admin`：启动后端服务。

前端常用命令：

- `cd xinyi-ui && npm run dev`：启动 Vite 开发服务。
- `cd xinyi-ui && npm run build:prod`：构建生产环境前端资源。
- `cd xinyi-ui && npm run build:stage`：构建预发布环境前端资源。
- `cd xinyi-ui && npm run preview`：预览构建结果。

Windows 下如果 npm 指向错误的全局目录，先把 `npm_config_prefix` 设置为 Node 安装目录。

## 代码风格与命名规范

Java 包名统一使用 `com.xinyi`。控制器、服务、Mapper、实体类应保持在现有模块边界内，命名沿用 `SysUserController`、`SysUserServiceImpl`、`SysUserMapper` 等模式。

前端使用 Vue 单文件组件和 TypeScript。页面目录按功能使用小写命名，组件文件优先使用 PascalCase 或遵循所在目录已有风格，接口模块按业务域分组。Vue/TS 文件使用 2 空格缩进。以后新增代码注释使用中文，且只在逻辑不直观时添加。

## 测试规范

后端测试放在对应模块的 `src/test/java`，测试类命名为 `*Test.java`。当前前端未配置测试框架，如需引入，应先约定工具并在 `package.json` 中补充命令。后端改动前运行 `mvn test`，前端发布前运行 `npm run build:prod`。

## 提交与 Pull Request 规范

当前历史只有 `Initial commit`，后续提交建议使用简洁的祈使句，例如 `Add user import validation` 或 `Fix Vite dev port`。PR 应说明改动摘要、影响模块、测试或构建结果、关联 issue；涉及界面变化时附截图。

## 安全与配置提示

不要提交凭据、本地数据库密码、构建产物或 IDE 元数据。环境相关后端配置放在对应的 `application*.yml` 中；前端代理或接口路径变更需同步检查 `xinyi-ui/vite.config.ts`。
