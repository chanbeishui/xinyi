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

## 工程硬约束（V6.4 第三方配置落地时确认，2026-10-23）
- Mapper 只能放 `com.xinyi.**.mapper` 包：`@MapperScan` 限定了包名，放其它包会启动即 "No qualifying bean"
- `sys_config` 没有 `config_key` 唯一索引：`ON DUPLICATE KEY UPDATE` 不生效，必须先去重 + `WHERE NOT EXISTS`
- 未注册的路由返回 **HTTP 200 + `{"msg":"No static resource ..."}`**，不是 404；RuoYi 未登录是 HTTP 200 + body.code=401
- 打包用 `/e/codex/.cprep/tools/apache-maven-3.9.16/bin/mvn`（`logs/mvn.sh` 缺 guice 会崩，`build_backend.sh` 只做 javac）
- 前端构建必须 `npx vite build --outDir dist-verify-<ts>`，沙箱禁止清空 `dist`（>50 文件触发批量删除保护）
- **MyBatis 注解 Mapper 不要声明 `byte[]` 返回值**：会被解析成单字节标量类型处理器，
  binary/varbinary 列直接抛 `Value '...' is outside of valid range for type java.lang.Byte`。
  取二进制列要返回 `Map` 再手工归一化（参考 `AccountMapper.findPhoneCipher` 的 default 方法）
- **`docker-compose.yml` 的 `docker-entrypoint-initdb.d` 是手写挂载清单**，新增迁移后必须同步补，
  否则 `docker compose up` 起的全新库会缺表（CI 走 glob 完全看不出来）。`ci_sql_check.sh` 第 0 步已机器断言
- **资料包 `ck_ver_<表名>` 约束是批量生成的**：只追加的不可变历史表（如
  `vi_driver_statement_membership_change`）没有 `version` 列，照抄 `CHECK (version >= 0)` 会
  `ERROR 3820 ... non-existing column`（ALTER 已有表时才是 1054），且会让整份 DDL 中途断掉
  （实测 79 张表只建成 56 张）。正确修法是改表达式落到真实列（`revision_before`/`revision_after`），
  **不是给表加 version 列**
- **资料包 V6.4 原始 zip 已就地修正**：`C:/Users/Administrator/Downloads/机动车检测预约_完整开发资料包_V6.4_第三方接入配置版.zip`
  的 `05_数据设计/001_vi业务表_初始化_V6.4.sql` 第 1270 行；原包备份 `*.zip.bak-20261023`。
  修正后的 SQL 副本在 `E:/codex/_backup/001_vi业务表_初始化_V6.4_patched.sql`
- **Git Bash 会把 `rev:path` 里的 `/` 当路径转换**：`git cat-file -e origin/x:.github/a.yml`
  会被改写成 `origin\x;.github\a.yml` 而报 `Not a valid object name`，看起来像"远程缺这个文件"。
  查远程文件用 `git ls-tree -r <rev> --name-only | grep`，或先 `export MSYS_NO_PATHCONV=1`
- **`xinyi-ui/package-lock.json` 必须在版本库**：`xinyi-ui/.gitignore` 沿用了 RuoYi 上游的
  `package-lock.json` 忽略规则，导致 CI（`npm ci` + setup-node `cache: npm`）必挂。
  已移除该忽略并入库（lockfileVersion 3，620 条依赖）；`yarn.lock` 仍忽略，避免两套锁文件并存。
  `frontend.yml` 已有断言步，锁文件缺失直接给 `::error file=xinyi-ui/.gitignore::` 注解
- **CI 在 Linux 上区分大小写**：本地（Windows）import 路径大小写写错永远不报。
  改前端后可跑一遍「逐级比对目录项大小写」的扫描（1297 个相对导入，脚本思路：对每段路径
  用 `os.listdir` 精确比对，命中则输出近似项）
- **GitHub API 匿名额度只有 60 次/小时**，用完返回 `API rate limit exceeded`。
  查 Actions 结果不必等额度：`https://github.com/OWNER/REPO/actions/workflows/<wf>.yml?query=branch%3A<branch>`
  的 HTML 里能直接 grep 到 `completed successfully: Run N of <wf>` / `failed: Run N of <wf>`，
  或读 `.../actions/workflows/<wf>.yml/badge.svg?branch=<branch>`（输出 `passing`/`failing`）
- **起本地 18080 实例必须用任务方式（run_in_background），不能 `nohup ... &`**：`nohup` 起的进程
  会随该任务结束被回收，日志停在启动中途（实测停在 55 行、无 Started 行、端口不监听）。
  正确做法：`java -jar xinyi-admin/target/xinyi-admin.jar --server.port=18080` 直接后台任务，
  约 8 秒就绪，探活用 `/api/common/session` 期望 **401**
- 详见 `docs/implementation/v6.4-sync-diff.md`、`.workbuddy/memory/2026-10-23.md`

## 荣利身份流程（V6.4，2026-10-23 已落地）
- **唯一合法自动关联路径**：微信手机号授权 → 手机号唯一匹配员工 → 自动建 `vi_employee_binding`。
  短信验证码是"兜底核验"，**不能**作为员工关联依据（V6.4 §2.5）
- 自动关联的行必须 `source='WECHAT_PHONE_MATCH'` + `bound_by IS NULL` + `phone_lookup`/
  `last_phone_verified_at`/`employee_auth_version` 非空（`ck_employee_link_identity` 只约束这一类行，
  `MANUAL_LEGACY` 历史行豁免）
- **跨账号身份冲突必须拒绝**：同一员工已被账号 A 关联，账号 B 再授权同一手机号 → 记 `EMPLOYEE_LINK_CONFLICT`
  并返回，不得静默把绑定迁到 B（V6.4 §4）
- 「员工自助申请 + 后台签发一次性凭证 + 本人兑换」整条流程已退役并删干净
  （2 张表 + 6 个端点 + 10 个 Mapper 方法 + 后台 Tab + 小程序 3 个 API）
- **改会话必须与换令牌成对**：`updateSessionBinding` 等语句会 `context_version + 1`，
  若只改会话不 `issueTokens`，用户手里的令牌立刻失效又拿不到新的，表现为"操作成功后被踢下线"
- `vi_*` 现为 **80 张**（V6.4 的 79 张 + 本库保留的 `vi_sms_send_log`）

## 消息自动下发（V6.4 第六章，2026-10-23 接通）
- 链路：业务事件 → `vi_notification_route`（按 `owner_dept_id` 精确优先、全局兜底）→ 已发布模板 →
  `vi_notification_delivery`（`INSERT IGNORE` + `uk_delivery_event` 去重）→ 短信/订阅
- 接的 4 个事件：`PAYMENT_SUCCEEDED→BOOKING_CONFIRMED`、`ASSIGNMENT_ACCEPTED→DRIVER_ASSIGNED`、
  `INSPECTION_FINISHED→INSPECTION_FINISHED`、`VEHICLE_RETURNED→RETURN_COMPLETED`
- 未配置一律 `SKIPPED` + 原因码（`VI_NOTIF_NO_PHONE`/`NO_PROFILE`/`CHANNEL_DISABLED`/`NO_VENDOR_TEMPLATE`/
  `USER_UNSUBSCRIBED`），**绝不退回默认通道**；环境不一致的模板直接不用（TEST 模板不能打到 LIVE 用户）
- `ACCEPTED ≠ DELIVERED`；网络异常记 `UNKNOWN` 只查回执，**不重发**
- 异常绝不影响业务事务，但投递行不能停在 `PENDING` 无原因
- 手机号密文用 `AccountMapper.findPhoneCipher` 单独取，通用 `findAccount` 不带敏感字段

## 数据大屏与档案室管理
- 数据大屏菜单：menu_id=2024(path=dataScreen), 2025(管理层BI看板), 2026(档案室管理)
- 档案室页面：`src/views/archiveRoom/`，3 个档案室可切换，SVG 平面图 + fuse.js 搜索
- BI 看板页面：`src/views/dataScreen/`，ECharts 图表
- 两页面均为纯前端静态数据，未接后端 API

## 生产部署（阿里云轻量 2C4G + 1Panel，2026-11-04 定案）
- 目标环境：Ubuntu 22.04 + 1Panel 托管 MySQL8/Redis7（容器），后端 jar 用 systemd 常驻，
  前端静态站 + OpenResty 反代 `/prod-api/` → 127.0.0.1:8080
- 产物：`docs/deploy/1panel-2c4g-deployment.md`、`deploy/prod/{application-prod.yml,xinyi.env.example,xinyi-admin.service,nginx-xinyi.conf,backup-xinyi.sh}`
- **启动 profile 必须 `druid,prod`**：druid 配置在 `application-druid.yml`（开发库 localhost:3307），
  顺序反了会被覆盖回开发库；后加载的 profile 优先
- **`sql/quartz.sql` 必须单独导入**：`ry_20260417.sql` 不含 `QRTZ_*` 表，根 docker-compose
  也没挂载它，漏导则定时任务模块报错
- 生产**不要**导 `seed_20261021_rongli_demo_accounts.sql`、`seed_20261022_rongli_reconcile_demo.sql`（演示数据）
- MySQL 必须 8.0（`utf8mb4_0900_ai_ci`），容器需设 `default-time-zone=+08:00`
- Docker 发布端口绕过 ufw，端口管控只能靠阿里云控制台防火墙

## 影像存储（x-file-storage 2.3.0 + 阿里云 OSS，2026-11-04）
- **只有对象存储，没有本地磁盘兜底**：`rongli.media.root`、`~/rongli-uploads`、`FileStorage`/`MediaStorage`
  那套自建门面全部作废。上传/下载一律 `FileStorageService`，凭据走 `RONGLI_OSS_*` 环境变量
- **`@EnableFileStorage` 必须按文档写在启动类** `com.xinyi.RuoYiApplication` 上。
  不要再在业务模块建只含注解的空 `@Configuration`——技术上等价的实现细节，不足以成为偏离文档的理由
- 依赖放 **`xinyi-common`**（与 `FileUploadUtils`/`FileUtils` 同层），版本在根 pom 的 `dependencyManagement`。
  **不要放业务模块**：`generator`、`quartz` 只依赖 common，放 rongli 会让脚手架反向依赖业务模块（分层倒置）。
  「放 common 会把云 SDK 扩散到 codegen/quartz 撑大包体」是伪论据——单体部署所有模块进同一个 fat jar，
  实测挪位置前后 jar 只差 232 字节；且 `x-file-storage-spring` 只带自己的 core，其他厂商 SDK 都是 optional
- **`FileInfo` 必须显式 `setPlatform(默认平台)`**：download/exists/delete 直查平台、不回退
  `default-platform`，不设就报 `没有找到对应的存储平台！platform:null`（HTTP 422）。
  `default-platform` 只在上传的 `of()` 生效——读写两条路口径不一致，这是最容易踩的坑
- 读取前先 `exists`/HEAD 再写响应字节：响应一旦开始输出，就没法再返回 JSON 错误

## 全量去本地化（2026-11-05，commit 3f83050）
- **服务器不再保存任何用户文件**。通用上传（头像、富文本图、Excel 导入提取图、导出文件）也走 OSS，
  统一门面 `com.xinyi.common.utils.file.OssFileService`；`ruoyi.profile` 已从配置和代码中删除
- 静态工具类（`FileUploadUtils`/`FileUtils`/`ImageUtils`/`ExcelUtil`）通过
  `SpringUtils.getBean(OssFileService.class)` 转发，不是自己持有 bean
- **ACL 必须分两类**：通用上传 `public-read`（地址长期存库、直接喂 `<img src>`，签名会过期→头像天天裂）；
  业务影像私有 + 短链。Bucket 整体保持私有，但**必须关闭「阻止公共访问」**，否则 public-read 上传报 AccessDenied
- `/profile/**` 保留为**读 OSS 的兼容层**（`ProfileResourceController`），历史相对地址仍能显示；
  它不是本地目录映射，`ResourcesConfig` 里那条 `file:` 映射已删
- `/common/download` 的 `checkAllowDownload` 必须传 `FileUtils.getName(url)`：完整 URL 带 query 会被判非法
- **endpoint 必须公网节点**：短链由该节点签出，`-internal` 会让手机端打不开图片（现象：上传成功、图片全白）

## 版本管理约定（2026-11-05 用户明确要求）
- **每完成一次大修改就立刻提交**，不要攒着；提交描述**一律用中文**
- 一次提交只做一件事：工作区同时积压多批不相干改动时，按主题分批 `git add` 再分别提交，
  不要为了省事合成一个 commit
- 格式沿用历史风格：`type(scope): 中文描述`，正文写清「为什么改」而不只是「改了什么」
- 提交前扫一遍是否带入明文凭据（AK/SK、密码、真实 IP）；部署脚本里只能有 `CHANGE_ME` 或示例地址
