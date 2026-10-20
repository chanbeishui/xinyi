# E00 框架基线核对报告（只读实勘）

任务：E00 · 框架基线核对：M0尚未开始
执行时间：2026-10-20（UTC）
实现者：Qoder-Agent（本轮）
性质：只读实勘。未写业务代码、未执行 DDL、未升级依赖、未部署、未 commit/checkout/reset/clean。

## 1. 用户确认的起点

- 用户明确：**没有做 M0**，当前仅有若依前后端分离改版后的 xinyi 框架。
- 用户确认：分支 `rongli-wx-home` 与资料包观察的 `rongli-wx` 等同，无区别，按当前分支推进。
- 用户决定：直接开始 E00，本轮评审残留项纳入 M0 执行，按 tasks.json 依赖推进，不停在评估报告。

## 2. 真实工作区状态（git）

| 项 | 实际值 | 资料包 baseline(890cfbb) | 说明 |
|---|---|---|---|
| 当前分支 | `rongli-wx-home` | `rongli-wx` | 用户确认等同 |
| HEAD | `18b5ce4e424211dc9711dffcf428fd4125f341cf`（"mac电脑demo", 2026-10-19） | `890cfbb…` | HEAD 领先一个提交 |
| 工作树 dirty | clean（install 前）；install 后 `M AGENTS.md` + `?? docs/` | — | install 仅新增文档/账本+AGENTS受管区段 |
| headMatchesObservation | **false** | — | 如实记录，不回退、不改指纹掩盖 |

`18b5ce4` 仅改动 `xinyi-ui/package.json`（name `ruoyi`→`rongli`、author `若依`→`荣利`、repository url 清空），即品牌化改名提交，不含任何机动车业务代码。

## 3. 源码指纹核对（5 个 baseline 文件，git blob SHA-1）

| 文件 | baseline(890cfbb) | 当前工作区 | 结论 |
|---|---|---|---|
| pom.xml | b0746875… | b0746875… | 一致 |
| xinyi-framework/.../SecurityConfig.java | cbb66984… | cbb66984… | 一致 |
| xinyi-framework/.../JwtAuthenticationTokenFilter.java | 8c66b190… | 8c66b190… | 一致 |
| AGENTS.md | 7b1a3864… | 549e9249… | **变化**：install 追加 `VEHICLE_V6_EXECUTION` 受管区段（原文全保留） |
| xinyi-ui/package.json | 1335f0a3… | 2a05e5a5… | **变化**：18b5ce4 品牌化改名 |

两处变化均为预期、可解释、非掩盖性差异。

## 4. 生效的 AGENTS / 规范

- 根 `AGENTS.md`：保留原有全部内容（项目结构、构建测试、代码风格、数据库变更同步规范、部门数据权限与安全审计开发规范），末尾新增受管区段（E00 入口指向 `docs/vehicle/V6.1/START-CODEX.md`，状态在 `docs/implementation/vehicle/`）。
- 资料包规范 `docs/vehicle/V6.1/AGENTS.md`：V6 工程规范，声明不覆盖目标仓库根/作用域 AGENTS，严禁 reset/clean/DROP、默认 permitAll、伪造 LoginUser、测试替身入生产。
- 两者为"合并"关系，非覆盖；原仓库安全/多部门/迁移规范继续生效。

## 5. 模块清单与新建责任

- 现有 Maven 子模块（6）：xinyi-admin、xinyi-framework、xinyi-system、xinyi-quartz、xinyi-generator、xinyi-common；另有前端 xinyi-ui。
- **无** xinyi-rongli / xinyi-vehicle / xinyi-miniapp（doctor: `miniappExists=false`、`moduleConflict=false`、`suggestedBusinessModule=xinyi-rongli`）。
- sql/ 现有 5 份框架脚本（ry_20260417.sql、quartz.sql、migration_multi_dept_authorization.sql、migration_20260804_digital_twin_menu.sql、migration_20260915_data_screen_menu.sql），**无任何 vi_ 表/会话迁移脚本**。
- 结论：业务模块动作 = **CREATE_NEW**（M0 新建 xinyi-rongli `com.xinyi.rongli` + xinyi-miniapp uni-app）；不重建 PC 框架与 sys_user 体系；无历史业务数据迁移（legacyViMigration=NOT_APPLICABLE）。

## 6. 环境工具链实勘

| 工具 | 实际版本 | 计划要求 | 结论 |
|---|---|---|---|
| Java（运行时） | 21.0.8 LTS | 运行 JDK21 | ✅ 符合 |
| Maven | 3.9.14 | — | ✅ 可用 |
| Maven 绑定的 JDK | **Homebrew openjdk 25.0.2** | 编译目标 release17 | ⚠️ 构建期 JDK 与"运行21/编译17"不一致，E04/E05 构建前须统一（配置 toolchain 或 JAVA_HOME） |
| Node | **v22.16.0** | uni-app 模板 `>=22 <23` | ✅ 符合（上轮基于环境摘要的 v24 担忧不成立，已纠正） |
| npm | 10.9.2 | — | ✅ |
| Python | 3.11.15 | 工具/pytest | ✅ |
| MySQL | 本机 **mysqld 运行中**（监听 3306，PID 1374，Homebrew）；客户端 9.6.0 | 隔离库 MySQL8.4（容器） | ⚠️ 服务端版本待 E02/E03 连库确认（需 ≥8.0.22 支持 CHECK）；Docker 不可用→改用本机独立库 xinyi_vi_dev |
| Docker / compose | **不可用**（exitCode 127, FileNotFoundError） | E02 compose 起隔离 MySQL8.4+Redis | ❌ ENVIRONMENT 阻塞：compose 路径不可用，按用户指示 II.2 适配为本机独立数据库 + 本机 Redis |
| Redis | 待 E02 探测 | 本地独立实例 | 未探测 |

## 7. 本轮评审残留项登记（纳入 M0，映射到任务）

详见 `docs/implementation/vehicle/DECISIONS.md` 的 R1–R12。摘要与映射：

- **E01 处理**（契约/模型一致性，不依赖 Docker）：
  - R1 12 个未绑定枚举列补取值/可空性/CHECK（含 `vi_inspection_result.conclusion` PASS/FAIL）
  - R2 `vi_aftersale_case.refund_state` 与 `vi_refund_order.status` 语义裁决（同义统一/异义分开命名+映射，不取并集）
  - R3 写白名单分三层：不可变历史(insert-only)、受控生命周期(session/token 保留角色切换/消费/撤销等受控更新)、可编辑草稿；不一律 insert-only
  - R4 指定唯一 ViRecord 权威来源（以 14_工程模板/backend 版含 id 为准），自然键实体单独映射
  - R5 `GET /api/public/service-config` 补录接口全量清单 + 权限注册表 PUBLIC 授权登记
  - R6 共享端点（handover/files/uploads/employee-binding）按 MINI/员工MINI/ADMIN 明确能力+功能权限+数据范围+状态四要素，不以任一条件满足放行
  - R7 统一契约术语为 `x-authorization.scope`（清除 x-data-scope 漂移）
  - R8 改主模型/主契约后同步任务片段、类型、样本、测试（不只追加说明文档）
- **E03 处理**（数据落地，依赖 E02 环境）：
  - R9 001 初始化脚本改造为符合根 AGENTS 的 `migration_YYYYMMDD_vi_init.sql`（前置/备份/顺序/不可重复执行/失败恢复），同步并入 `ry_20260417.sql`，验证两条初始化路径结构一致
  - R10 补菜单/权限/必要字典种子（不硬造已有 sys ID、不默认授予全部员工权限）；确认 vehicle_type 字典存在或补种子
  - R11 不擅改原 sys 表排序规则；vi 用 utf8mb4_0900_ai_ci，按实际字符串比较边界处理 vi↔sys join
  - R12 MySQL 服务端版本确认 ≥8.0.22（CHECK/表达式默认值），UTC 默认值真实探针
- **环境/工具链**（E02/E04 前）：
  - Docker 不可用 → E02 适配本机独立库；Maven JDK25 与运行 JDK21 不一致 → E04/E05 构建前统一
  - 追加表白名单 updateColumns 与"不可变"矛盾 → 并入 R3
  - 002 结构验收不校验表数/唯一键 → E03 配合 verify_mysql.py 闭环

## 8. 完成判据核对（对照 task_packets/E00/TASK.md）

- [x] 记录当前工作区 HEAD/branch/dirty、生效 AGENTS、源码指纹（§2–§4）
- [x] 确认框架模块并记录将新建 xinyi-rongli/xinyi-miniapp；不从旧日志/目录/SQL 推断完成（§5）
- [x] 保留 sys 与其他框架业务；未 checkout/reset/clean，未重启或改写原数据库（全程只读 + install 仅新增文档）
- [x] 真实命令、HEAD 与差异；无密钥、无个人数据（§2/§6，未读取 application*.yml 秘密）
- [x] 只完成 E00 基线，未预标 M0 整体/构建/业务测试通过
- [ ] 独立复核观察证据（REVIEWED 需复核人≠实现人，留待用户/独立复核者；本 Agent 不自审）

## 9. 结论与下一步

- E00 基线核对完成：起点 = 改版若依框架（rongli-wx-home@18b5ce4），M0 未开始，业务模块 CREATE_NEW，无历史迁移。
- 环境就绪度：Java21/Node22/Maven/Python/本机MySQL 可用；**Docker 不可用**为主要环境约束，E02 按用户指示适配本机独立库。
- 下一可执行任务：**E01（契约修复+残留项 R1–R8）** 与 **E02（隔离环境，需处理 Docker 缺失适配）** 均已随 E00 完成而解锁；E03 依赖 E02。
- 按用户决定，继续 E01，不停在本报告。
