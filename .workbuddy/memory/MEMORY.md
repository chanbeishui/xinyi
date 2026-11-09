# 项目长期记忆

## 技术架构
- Vue 3 + Vite 6 + TS + Element Plus 2.13.1 + Pinia；RuoYi-Vue3（constantRoutes + 数据库动态路由）
- 前端 dev server port 3000；后端 8080；MySQL localhost:3306/xinyi（root 密码 chenbeishui）
- 多部门任职体系：`sys_user_dept` + `sys_user.management_scope/auth_version` + `sys_security_outbox/audit_log`，规范见 AGENTS.md
- Vite 前端构建必须 `npx vite build --outDir dist-verify-<ts>`（沙箱禁止清空 dist）
- Maven 用 `/e/codex/.cprep/tools/apache-maven-3.9.16/bin/mvn`（`logs/mvn.sh` 缺 guice，`build_backend.sh` 只做 javac）
- 起本地 18080 实例必须用后台任务方式，不能 `nohup ... &`（进程会被回收）；探活 `/api/common/session` 期望 401

## 工程硬约束
- Mapper 只能放 `com.xinyi.**.mapper` 包（`@MapperScan` 限定），否则启动报 No qualifying bean
- `sys_config` 无 `config_key` 唯一索引，`ON DUPLICATE KEY UPDATE` 不生效，须先去重 + `WHERE NOT EXISTS`
- 未注册路由返回 **HTTP 200 + `{"msg":"No static resource ..."}`**；未登录是 HTTP 200 + body.code=401
- MyBatis 注解 Mapper 不要声明 `byte[]` 返回值（会被当单字节标量），取二进制列返回 `Map` 再手工归一化
- `docker-compose.yml` 的 `docker-entrypoint-initdb.d` 是手写挂载清单，新增迁移必须同步补
- 资料包 `ck_ver_<表名>` 约束批量生成，不可变历史表无 `version` 列，照抄会让 DDL 中途断掉
- `xinyi-ui/package-lock.json` 必须在版本库（CI 用 `npm ci`）；`yarn.lock` 仍忽略
- CI 在 Linux 区分大小写，改前端后建议跑「逐级比对目录项大小写」扫描
- Git Bash 会把 `rev:path` 的 `/` 当路径转换，查远程文件用 `git ls-tree -r <rev> --name-only | grep` 或 `MSYS_NO_PATHCONV=1`
- GitHub API 匿名额度 60 次/小时；查 Actions 可 grep workflow 页 HTML 或读 badge.svg（passing/failing）
- 详见 `docs/implementation/v6.4-sync-diff.md`、`.workbuddy/memory/2026-10-23.md`

## 历史 Bug
- 路由 name 冲突（2026-07-30）：后端 `getRouteName()` 把 menu path 首字母大写，path=`index` → `Index`，与首页 constantRoute 同名，`addRoute` 会移除已有路由。已删除冗余 constantRoutes 并把首页 name 改为 `Home`

## 荣利项目（2026-10-20 已全量回滚）
- 目标版本 `890cfbbfc5e0db404a932ab5ef8b449d07c7f444`（M0 `95319ad` 的父提交），本地与 `origin/rongli-wx` 一致
- 回滚前 M1 未提交改动已删除且无法找回；目录 `xinyi-rongli/`、`xinyi-rongli-wx/` 已删
- 找回 M0：分支 `backup/pre-m0-revert-20261020` 或标签 `backup-m0-95319ad` → `git reset --hard 95319ad`（远程已强推，需云端备份要单独 push 分支）
- **坑**：本地 `reset --hard` 后再 `git pull` 会与远程旧指针合并出 merge commit，把回滚内容拉回；远程必须同步回退
- DB 已回滚（DROP 13 张 `vi_*`、删 sys_menu 2100-2114 及授权），保留 10 条审计留痕；全库备份 `E:\codex\_backup\xinyi_20261020_pre_m0_revert.sql`
- 重启待办：部门归属、岗位角色划分、M2a 的 6 项业务参数
- V6.4 身份流程（当时已落地，代码已随回滚删除）：仅微信手机号授权可自动关联员工；短信验证码只做兜底核验；跨账号冲突必须拒绝；`updateSessionBinding` 会 `context_version+1`，必须与 `issueTokens` 成对
- V6.4 消息自动下发：`vi_notification_route` → 模板 → `vi_notification_delivery`（`INSERT IGNORE` 去重）；未配置一律 `SKIPPED` + 原因码，绝不退回默认通道；`ACCEPTED ≠ DELIVERED`，异常记 `UNKNOWN` 只查回执不重发

## 数据大屏与档案室
- 菜单 2024(dataScreen) / 2025(管理层BI看板) / 2026(档案室管理)
- 页面 `src/views/archiveRoom/`（SVG 平面图 + fuse.js）、`src/views/dataScreen/`（ECharts），均为纯前端静态数据

## 生产部署（阿里云轻量 2C4G + 1panel，2026-11-04）
- Ubuntu 22.04 + 1panel 托管 MySQL8/Redis7，jar 用 systemd，OpenResty 反代 `/prod-api/` → 127.0.0.1:8080
- 产物：`docs/deploy/1panel-2c4g-deployment.md`、`deploy/prod/*`
- **启动 profile 必须 `druid,prod`**（后加载优先，顺序反了会覆盖回开发库）
- `sql/quartz.sql` 必须单独导入（`ry_20260417.sql` 不含 QRTZ_* 表，docker-compose 也没挂载）
- 生产不要导 `seed_20261021_*.sql`、`seed_20261022_*.sql`（演示数据）；MySQL 必须 8.0，容器设 `default-time-zone=+08:00`
- Docker 发布端口绕过 ufw，端口管控靠阿里云控制台防火墙

## 影像存储（x-file-storage 2.3.0 + 阿里云 OSS，2026-11-05）
- **服务器不保存任何用户文件**，全部走 OSS。统一门面 `com.xinyi.common.utils.file.OssFileService`；
  `ruoyi.profile`、`rongli.media.root`、`FileStorage`/`MediaStorage` 自建门面全部作废；凭据走 `RONGLI_OSS_*` 环境变量
- `@EnableFileStorage` 写在启动类 `com.xinyi.RuoYiApplication`；依赖放 **xinyi-common**，版本在根 pom dependencyManagement
- 静态工具类（`FileUploadUtils`/`FileUtils`/`ImageUtils`/`ExcelUtil`）通过 `SpringUtils.getBean(OssFileService.class)` 转发
- **ACL 分两类**：通用上传（头像等）`public-read`（地址长期存库、直接喂 `<img src>`，签名会过期）；业务影像私有 + 短链。Bucket 整体私有但**必须关闭「阻止公共访问」**
- **RAM 策略必须含 `oss:PutObjectAcl`**（x-file-storage 走 SetObjectACL 接口），缺了普通上传正常、传头像立刻 AccessDenied
- **endpoint 必须公网节点**：`-internal` 会让手机端打不开图片（上传成功但图片全白）
- `/profile/**` 是读 OSS 的兼容层 `ProfileResourceController`，不是本地目录映射；`ResourcesConfig` 的 `file:` 映射已删
- `/common/download` 的 `checkAllowDownload` 必须传 `FileUtils.getName(url)`（完整 URL 带 query 会被判非法）
- **`FileInfo` 必须显式 `setPlatform(默认平台)`**：download/exists/delete 不回退 default-platform，不设报 `platform:null`
- 读取前先 `exists`/HEAD 再写响应字节；响应开始输出后没法返回 JSON 错误
- 目录规范：`avatar/`、`upload/rich-text|image|file/`、`upload/`（兼容旧数据）、`import/`、`download/yyyy/MM/dd/`、
  业务影像 `<用途>/[ord-订单号/]yyyy/MM/dd/`，日期统一 `yyyy/MM/dd`
- 通用上传用途由 `/common/upload?biz=` 决定，映射 `OssFileService.dirForBiz`，未知落 `upload/`
- `FileUploadUtils.logicalDir` 必须保留多级前缀（旧实现压成最后一段会让分层失效）
- 业务影像订单段排在日期之前（前缀删除是唯一批量手段）；影像文件名 `用途_12位随机串`
- 图片上传按**魔数**校验（jpg/png/gif/bmp），文档类扩展名不校验
- 生命周期脚本 `logs/set_oss_lifecycle.py` 默认 dry-run，`PutBucketLifecycle` 是整份替换必须先 GET 再合并；
  需 Bucket 级动作 `oss:GetBucketLifecycle`/`PutBucketLifecycle`（Resource 写 `acs:oss:*:*:<bucket>`，不带 `/*`）
- OSS V1 签名 CanonicalizedResource 是 `/<bucket>/<object>?<子资源>`，三级域名不含 bucket 段，从 URL 截会 SignatureDoesNotMatch

## 版本管理约定（用户明确要求）
- 每完成一次大修改立刻提交，不要攒着；提交描述**一律中文**
- 一次提交只做一件事，多批不相干改动按主题分批 `git add` 分别提交
- 格式 `type(scope): 中文描述`，正文写「为什么改」
- 提交前扫明文凭据（AK/SK、密码、真实 IP），部署脚本里只能是 `CHANGE_ME` 或示例地址
