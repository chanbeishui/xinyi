# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

`xinyi` 是若依 RuoYi-Vue 3.9.2 衍生的多模块仓库，包含「xyjiance 内部管理系统」与「荣利机动车检测预约」业务。后端 Java 17 + Spring Boot 4.x + MyBatis + MySQL 8 + Redis；前端有两套独立工程：

- `xinyi-ui/`：后台管理端，Vue 3 + TypeScript + Vite 6 + Element Plus + Pinia。
- `xinyi-rongli-wx/`：微信小程序，uni-app + Vue 3 + TypeScript，独立 npm 工程，**不在 Maven modules 中**。

仓库级贡献规范（提交粒度、SQL 同步、部门权限安全）以根目录 `AGENTS.md` 为准；后台前端细节见 `xinyi-ui/CLAUDE.md`。本文件补充上手所需的命令与跨模块架构。

## 仓库布局

| 目录 | 职责 |
|---|---|
| `xinyi-admin/` | 启动模块（`com.xinyi.RuoYiApplication`）、系统/监控 Controller、`application*.yml`、打包入口 |
| `xinyi-framework/` | Spring Security/JWT、`@DataScope` 切面、MyBatis/Redis/Druid/线程池配置 |
| `xinyi-system/` | `sys_*` 用户/角色/部门/菜单/字典，多部门授权、`auth_version`、安全 outbox、`ISuperAdminService` |
| `xinyi-common/` | 通用工具、注解、`BaseEntity`/`AjaxResult`、`SessionInvalidator` 等接口、OSS 上传能力 |
| `xinyi-quartz/` | 定时任务 |
| `xinyi-generator/` | 代码生成器 |
| `xinyi-rongli/` | 荣利业务（订单、派单、履约、售后、支付、站点、司机、车主、媒体、消息、集成配置），提供 `/api/admin`、`/api/app`、`/api/common` |
| `xinyi-ui/` | 后台前端；业务页面主要在 `src/views/rongli/**` |
| `xinyi-rongli-wx/` | 小程序（车主/司机/检测站三种角色） |
| `sql/` | `ry_20260417.sql` 全量初始化 + `migration_*.sql` 增量迁移 |
| `logs/` | 验收与静态校验脚本（仓库的唯一自动化回归入口） |
| `docs/` | 实施台账、验收规格、部署/联调文档 |
| `deploy/` | 生产部署脚本、Nginx 与 systemd 配置 |

## 常用命令

### 后端

```bash
mvn clean package                                  # 全量构建
mvn test                                           # 全部单元测试（目前仅 4 个测试类）
mvn -pl xinyi-framework,xinyi-system -am test      # CI 实际执行的范围
mvn -pl xinyi-framework -am -Dtest=TokenServiceTest -DfailIfNoTests=false test  # 单个测试
mvn -o -pl xinyi-admin -am package -DskipTests     # 后端改动后的打包校验
mvn spring-boot:run -pl xinyi-admin                # 启动，默认 8080
java -jar xinyi-admin/target/xinyi-admin.jar       # 或直接运行打包产物
```

- 编译目标 Java 17；CI 使用 JDK 21。启动类为 `xinyi-admin/.../RuoYiApplication.java`。
- 本机没有独立 `mvn` 时可用 `bash logs/mvn.sh <maven 参数>`（包装 IDEA 自带 Maven）；`bash logs/build_backend.sh` 可只编译 `xinyi-rongli`。
- 测试位于各模块 `src/test/java`，命名 `*Test.java`。全项目仅 4 个可运行测试类，真正的回归靠 `logs/` 下的验收脚本。

### 后台前端（xinyi-ui）

```bash
cd xinyi-ui
npm ci                                        # CI 使用 npm ci；package-lock.json 必须入库
npm run dev                                   # Vite dev，5173；/dev-api 代理到 vite.config.ts 的 baseUrl
npm run build:prod                            # 生产构建，输出 dist/
npm run build:stage                           # 预发布构建
npm run preview                               # 预览构建产物
npx vite build --outDir dist-verify-<ts>      # AGENTS.md 约定的构建校验方式（产物不入库）
```

- 未配置 lint/测试框架，`build:prod` 通过即视为前端改动可交付。
- 临时指向远程后端：`VITE_PROXY_TARGET=http://<host>:<port>/prod-api npm run dev`（见 `docs/deploy/fast-debug.md`）。

### 小程序（xinyi-rongli-wx）

```bash
cd xinyi-rongli-wx
npm run dev:mp-weixin       # 产物 dist/dev/mp-weixin，用微信开发者工具导入
npm run build:mp-weixin     # 产物 dist/build/mp-weixin
npm run type-check          # vue-tsc --noEmit
npm run release:mp          # 构建 + 调用开发者工具 CLI 上传（需工具已登录并开启服务端口）
```

- 运行时配置来自 `.env`（参考 `.env.example`）：`VITE_API_BASE`、`VITE_LOGIN_MODE`、`VITE_LOGIN_CODE`。`.env` 已被忽略，不要提交。
- 真机必须使用局域网 IP 或已备案 HTTPS 域名，并在微信后台配置合法域名。

### 数据库与验收

```bash
docker compose up -d                    # 隔离环境：MySQL 13307 / Redis 16379
bash logs/ci_sql_check.sh xinyi_ci      # 基础脚本+全部迁移按序执行、幂等复跑、结构断言
python logs/run_acceptance.py           # 全量验收（需要 8080 与 18080 两个后端实例）
python logs/run_acceptance.py --static-only
python logs/check_api_routes.py         # 前端调用路径与后端路由的静态比对
python logs/check_oss.py                # OSS 凭据与公网可达性自检
```

- `run_acceptance.py` 默认 `BACKEND_DEV=http://127.0.0.1:8080`、`BACKEND_VERIFY=http://127.0.0.1:18080`；两个实例必须运行同一份代码，否则 `vi_outbox` 会被旧实例错误消费。结果写入 `logs/evidence.json` / `logs/evidence.md`。
- 本机 `application-druid.yml` 默认 MySQL 3307、`application.yml` 默认 Redis 6379，与 compose 端口不同，按需覆盖；不要把新凭据写进版本库。
- OSS 通过环境变量启用：`RONGLI_OSS_ENABLED`、`RONGLI_OSS_ACCESS_KEY`、`RONGLI_OSS_SECRET_KEY`、`RONGLI_OSS_ENDPOINT`、`RONGLI_OSS_BUCKET`、`RONGLI_OSS_DOMAIN`。

### 部署

```bash
XINYI_HOST=root@<host> ./deploy/prod/deploy.sh all        # 前后端
XINYI_HOST=root@<host> ./deploy/prod/deploy.sh backend    # 仅后端
XINYI_HOST=root@<host> ./deploy/prod/deploy.sh frontend   # 仅前端
./deploy/prod/deploy.sh build                             # 只打包不上传
```

与 `.github/workflows/deploy.yml` 共用 `deploy/prod/remote-deploy.sh`（备份 → 切换 → 重启 → 健康检查 → 失败回滚）。

## 后端架构要点

### 模块与扫描

- `xinyi-admin` 只负责启动和系统/监控接口；荣利业务 Controller 全部在 `xinyi-rongli`。
- `ApplicationConfig` 上 `@MapperScan("com.xinyi.**.mapper")`；MyBatis 配置为 `typeAliasesPackage=com.xinyi.**.domain`、`mapperLocations=classpath*:mapper/**/*Mapper.xml`。XML 只存在于 `xinyi-system`、`xinyi-quartz`、`xinyi-generator` 的 `src/main/resources/mapper`。
- `xinyi-rongli` 按业务域分包：`admin/aftersale/auth/common/dispatch/driver/fulfill/integration/ledger/media/notification/order/owner/payment/station/vehicle`，每个域下通常有 `controller/service/mapper/domain`（部分域有 `dto`）。Mapper 几乎都用 `@Select/@Insert` 注解并返回 `Map<String,Object>`，不要假设存在 XML 或实体绑定。

### 双认证域（关键）

- **ADMIN 域**：若依 JWT（`Authorization: Bearer <token>`），覆盖 `/system/**`、`/monitor/**` 与荣利后台 `/api/admin/**`。功能权限用 `@PreAuthorize("@ss.hasPermi('biz:...')")`。
- **MINI 域**：`/api/app/**`、`/api/common/**` 以及 `/api/notify/**`、`/api/public/**` 在 `SecurityConfig` 中 `permitAll`，由 `MiniAuthInterceptor` 独立校验。令牌从 `X-Mini-Token`（兼容 `Authorization: Bearer`）读取，只在 `vi_session_token` 保存 SHA-256 摘要；每次请求校验令牌类型/状态/过期、`vi_session` 状态、账户安全状态与 `context_version`，然后写入 `MiniContext`。登录、刷新、支付回调等路径在 `RongliWebConfig` 中排除。
- 两个身份域不能混用：MINI 接口不得依赖 `SecurityUtils` 或 ADMIN JWT；ADMIN 接口不得依赖 `MiniContext`。
- 小程序 access/refresh 令牌均由服务端签发，refresh 单次消费并整链轮换；前端 `src/utils/request.ts` 负责并发 401 合并刷新与迟到 401 快照重放。
- 鉴权失败/权限拒绝的提示必须直说原因（缺令牌、会话结束、角色/站点已切换等），不要统一兜成「请重新登录」。

### 授权与数据权限

- `@PreAuthorize` 只判断功能权限点；**部门数据范围必须在 Service 层**通过 `@DataScope`（框架查询）或 `xinyi-rongli` 的 `DeptScope.visibleDeptIds()` 显式过滤。客户端提交的 `params.dataScope`、`searchDeptIds`、`visibleDeptIds` 一律不可信。
- 权限集合为空时 fail-closed（生成 `1 = 0` 或直接拒绝），不得省略条件。带 `@DataScope` 的方法必须经 Spring 代理调用，禁止 `this.method()` 绕过切面。
- 最高管理员统一通过 `ISuperAdminService` 判定（`user_id = 1` **或** 持有 `role_id = 1`），不要各自硬编码 `SecurityUtils.isAdmin()`。
- `auth_version` 只在影响授权、凭据或账号可用性的变更时递增：角色、部门、管理范围、角色权限/状态/数据范围/菜单、密码（含代重置）、状态、删除、导入覆盖。昵称、邮箱、手机号、性别、备注、岗位、头像不递增，只刷新在线会话的展示快照。
- 凭据类/可用性类变更（改密、代重置、停用、删除、导入覆盖）必须淘汰所有会话；授权类变更保留操作者当前会话，其余设备立即失效。
- 权限校验、业务写入、`auth_version`、成功审计与 `sys_security_outbox` 必须在同一数据库事务；Redis 清理只是加速手段，权限撤销以数据库为准。

### 荣利业务约定

- 状态流转用 CAS：`WHERE id = ? AND fulfillment_status = ? AND version = ?`，禁止无条件 UPDATE；写操作先读数据库中的权威记录，再校验归属。
- 业务事件、站内通知与可靠异步消息统一走 `EventService`：与业务写入同事务，outbox 由定时任务消费，通知失败不得回滚业务。
- 时间口径：`vi_*` 表只存 UTC 分量；接口只输出 `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`（`ViTime.iso()`），需要展示的时段另给 `*Label` 字段；返回行对象时用 `camelRow(s)` 转驼峰，不要直接把 `LocalDateTime` 放进 Map。后台前端统一用 `@/utils/datetime` 的 `formatDateTime`；`sys_*` 表走本地时间，展示层不要二次转换。
- 外部服务（高德、OSS、短信、微信）的额度/权限类错误必须把原因写进用户提示，例如「高德地图基础 LBS 服务每月使用额度已达上限」，不要兜成「服务暂时不可用」。高德错误码映射在 `xinyi-ui/src/utils/amap.ts`。
- 图片上传按文件头魔数校验，扩展名必须与真实内容一致；浏览器转码/裁剪后上传要按 Blob 的真实 MIME 重命名。用户文件只存 OSS；需要 canvas 读像素时走同源 `/profile/<key>`，不要用 OSS 直链。

## 前端架构要点

### 后台管理端（xinyi-ui）

- 动态路由：`src/permission.ts` 全局守卫在登录后调 `getInfo()` → `generateRoutes()`，按后端菜单 `router.addRoute()` 注册，再重放目标导航。业务路由由菜单驱动，不要在前端写死。
- 请求层 `src/utils/request.ts`：注入 Bearer token；GET 参数经 `tansParams`；POST/PUT 默认 1 秒防重复提交（请求头 `repeatSubmit: false` 关闭）；401 触发重新登录；响应统一经过 `normalizeTimeStrings` 处理时间串。
- 自动导入 Vue/Router/Pinia 组合式 API 与 `@vueuse/core`；`Pagination`、`DictTag`、`RightToolbar`、`Editor`、`FileUpload`/`ImageUpload` 等已全局注册，列表页可直接使用。
- 荣利接口封装在 `src/api/rongli/index.ts`，返回业务视图对象（不是框架 `AjaxResult`），错误体为 `{code, message}`。
- 更细的目录与设置说明见 `xinyi-ui/CLAUDE.md`。

### 小程序（xinyi-rongli-wx）

- 页面按角色分组：`pages/owner/**`（预约、订单、支付、售后）、`pages/driver/**`（听单、任务、认证、培训、流水、处罚）、`pages/station/**`（站点接车、检测）；公共页面为 `pages/login|role|messages|settings`。
- 接口集中在 `src/api/index.ts`（`commonApi` / `ownerApi` / `driverApi` / `stationApi` / `handoverApi`）；运行时配置在 `src/utils/config.ts`；请求与令牌续期在 `src/utils/request.ts`。
- 页面路由与窗口样式在 `src/pages.json`；appid、编译选项在 `src/manifest.json`。

## 数据库迁移与提交

- 任何表结构、菜单、字典、配置或基础权限数据变更，都必须在 `sql/` 新增 `migration_YYYYMMDD_<feature>.sql`（同日多份加两位序号），并同步更新全量初始化脚本 `sql/ry_20260417.sql`。
- 新增迁移必须挂进 `docker-compose.yml` 的 `/docker-entrypoint-initdb.d`；`logs/ci_sql_check.sh` 会断言没有漏挂。
- 迁移必须可重复执行（CI 会把全部迁移跑两遍）；脚本开头说明用途、前置版本、执行顺序、备份要求，包含数据回填与执行后校验 SQL。禁止只改本机数据库。
- 提交粒度：一个功能点或一个 bug 修复单独一次提交，不要把「功能 + 重构 + 格式化」混在一起。修复类提交写清复现条件、根因、修法和验证方式；type/scope 规范见 `AGENTS.md`。
- 不提交构建产物、运行日志、`.env`、凭据或 IDE 元数据；不改写 hooks 绕过检查。
- 改动后至少运行：后端 `mvn -o -pl xinyi-admin -am package -DskipTests`；后台前端 `npx vite build --outDir dist-verify-<ts>`；数据层或业务逻辑改动 `python logs/run_acceptance.py`。

## 参考文档

- `AGENTS.md`：完整贡献指南、多部门权限与安全审计开发规范。
- `xinyi-ui/CLAUDE.md`：后台前端模块细节。
- `docs/implementation/README.md`：实施任务台账、验收脚本清单与已知环境约束。
- `docs/implementation/time-display.md`：时间存储与展示口径。
- `docs/deploy/fast-debug.md`、`docs/deploy/ci-cd.md`：本地联调与部署流程。
- `doc/security/multi-dept-authorization.md`：多部门任职、授权与审计设计基线。
