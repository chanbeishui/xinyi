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

## 数据库变更同步规范

任何数据库结构或基础权限数据变更都必须随代码提供可移植 SQL，确保其他电脑和环境可以仅依靠仓库内容同步数据库。禁止只修改本机数据库而不提交脚本。

- 新建表、修改表、增加或删除字段、索引、约束、视图，以及菜单、字典、配置等基础数据变更，都必须在 `sql/` 下新增独立的增量迁移脚本。
- 增量脚本统一命名为 `migration_YYYYMMDD_<feature>.sql`；同一天存在多个迁移时追加两位序号，例如 `migration_20260727_02_order_audit.sql`。
- 每次数据库变更必须同时更新完整初始化脚本 `sql/ry_20260417.sql`，保证新环境可以一次初始化到最新结构。
- 增量脚本开头必须说明用途、适用的前置版本、执行顺序、备份要求和是否允许重复执行；不能重复执行时必须明确标注。
- 脚本必须包含必要的数据回填和执行后校验 SQL；异常数据应生成查询结果或异常报告，不得静默猜测修复。
- 迁移应优先使用兼容现有数据的前向变更。涉及删除字段、删除表、类型收窄或大批量数据重写时，必须提供独立备份、分阶段发布和回滚说明。
- 菜单、角色权限、字典和配置等依赖数据必须使用稳定主键或唯一键，并防止重复插入。
- 完成开发后必须在测试数据库执行增量脚本，并验证增量升级结果与完整初始化结果的表结构一致。
- PR 和交付说明必须列出本次新增或修改的 SQL 文件、执行顺序、校验结果以及是否需要重启后端或清理缓存。
- SQL 文件必须提交到 Git。仅存在于本机且未提交的脚本不视为完成数据库变更。

## 部门数据权限与安全审计开发规范

本系统支持用户多部门任职。新增或修改业务功能时，必须先明确数据的权威部门归属、读取范围和写入范围；归属不明确时先补充设计，不得默认使用用户主部门、创建人部门或客户端提交部门。

### 查询权限

- `@PreAuthorize` 只校验功能权限，部门数据权限必须在 Service 层通过 `@DataScope` 或专用权限服务执行。
- `@DataScope` 只保护查询，不能替代新增、修改和删除权限。
- 带 `@DataScope` 的方法必须通过 Spring 代理调用，禁止使用 `this.method()` 绕过切面。
- `deptAlias`、`userAlias` 必须指向记录的权威归属表；存在父记录归属时按父记录校验。
- 用户列表采用任职部门相交语义；用户详情和账户写操作采用覆盖全部任职部门语义。
- 权限集合为空时必须 fail-closed，不得省略权限条件。
- 客户端不得提交或覆盖 `params.dataScope`、`searchDeptIds`、`visibleDeptIds` 等内部字段。

### 写权限

- 新增记录必须校验提交的目标部门。
- 修改记录必须先从数据库加载原记录，再校验原部门与新部门并集。
- 删除记录必须校验数据库中的真实部门，不能只使用请求参数。
- 批量操作必须先验证全部目标，任一越权则整体拒绝。
- 前端隐藏按钮、列表查询范围和客户端 `deptId` 都不能作为写权限依据。
- 跨部门共享数据应使用明确的授权关联表，不得使用逗号字符串表达权限。
- 定时任务、导入、消息消费者和内部接口必须调用统一 Service，不得直接写 Mapper 绕过权限校验。

### 用户、角色与会话

- `sys_user.dept_id` 是主部门，完整任职以 `sys_user_dept` 为准；不得只用主部门判断多部门用户权限。
- `PLATFORM` 账号的后台资料、部门、岗位、角色、状态、密码重置和删除仅最高平台管理员可操作。
- 普通管理员不得修改自己的任职部门或角色。
- 用户任职、角色、状态、密码重置和删除必须在同一事务递增 `auth_version` 并写安全 outbox。
- 修改角色权限、角色状态、角色 DataScope、角色菜单或部门状态时，必须递增所有受影响用户的 `auth_version`。
- `sys_user_dept`、`sys_user_role`、`auth_version`、安全 outbox 和审计表只能通过统一授权服务写入。

### DTO、SQL 与审计

- 查询 DTO、写入 DTO、数据库实体和内部权限上下文必须分离，禁止实体批量绑定敏感字段。
- 部门及用户 ID 使用数据库加载的 `Long`；列表 SQL 使用 MyBatis 参数绑定，禁止拼接客户端字符串。
- 部门、角色、状态、密码重置、删除、导入及越权拒绝必须写安全审计。
- 审计不得记录密码、Token 或其他凭据，业务代码不得提供审计删除或清空功能。
- 权限检查、业务写入、`auth_version`、成功审计和 outbox 必须处于同一数据库事务。
- 强退失败可以重试，但权限撤销不能依赖 Redis 删除成功。

### 测试与评审

涉及部门数据或用户权限的 PR 至少覆盖：

- 有权访问和无权访问；
- 伪造部门 ID、直接修改记录 ID；
- 原部门有权而新部门无权，以及相反场景；
- 批量请求混入一个越权对象时整体回滚；
- 多部门列表可见与详情不可见边界；
- 权限变更后旧 Token 失效；
- 并发变更、事务回滚和 Redis 故障；
- 审计成功、失败和拒绝记录。

PR 描述必须说明数据归属、查询权限、写权限、会话影响、审计事件和测试结果。无法回答其中任一项时，不应进入合并。
