# E07 测试夹具与控制平面接入 · 报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。
隔离测试库：**xinyi_vi_it_e07**（用户批准，在现有 MySQL 3306 服务端新建，一次性可弃，**绝不触碰业务库 xinyi**）。

## 交付（对照 TASK.md 三项）

### 1. 只在测试 classpath 建立 fixtures/runtime/provider-event
- 控制面端点（`/__integration/runtime|fixtures|clock/advance|provider-events|jobs/run-due`）由 `ViIntegrationTestConfiguration.ControlApi` 承载，`@Profile("vi-it")` + 位于 `xinyi-admin/src/test`，**不被 main 组件扫描、不进生产包**。
- `FixtureProvisioner`（新增，src/test）实现 `FixturePort`，经 `@Bean` 装配进 vi-it 上下文（缺此 Bean 则 ControlApi 无法构造、上下文启动失败——不允许默默跳过）。

### 2. 真实会话授权和业务 Service 不 mock
- `FixtureProvisioner` 调用**真实框架 Service**：`ISysDictTypeService.selectDictDataByType` + `ISysDictDataService.insertDictData`（幂等确保 TEST_CAR/TEST_INSPECTION 测试字典）、`ISysUserService.selectUserById`（取 admin 建夹具上下文）。不 mock、不提供返回固定成功的默认实现。
- 夹具管理以框架 admin 身份执行（doc：admin 只用于 fixture 管理，不作默认测试身份），经真实 SecurityContext 装载 `LoginUser`，`create_by`/审计照常。

### 3. 分步接入 FixturePort（先 IDENTITY 与守卫）
- **IDENTITY 已实现**：run 登记表 `test_fixture_run` 上按 `(providerRunId, IDENTITY)` 幂等复用；返回**严格契约响应**（仅 `providerRunId/runId/vehicleTypeCode=TEST_CAR/vehiclePlate/privacyVersion=TEST_PRIVACY_V1`，符合 fixtures-contract.json 的 `additionalProperties:false`）。
- **BOOKING/advanceClock/runDue/applyProviderEvent 诚实延后**：对应业务服务（C02/C03/C04 站点·价格·时段、I01/I03 会话、支付查单、任务到期）尚未实现，一律抛 `UnsupportedOperationException` 并说明延后，**绝不伪造成功、绝不直接造 PAID/RETURNED/CONFIRMED 终态或任意订单目标状态**。

## 隔离测试库引导（可复现）
- `bootstrap_it_db.sh`（新增）：在现有 MySQL 服务端 `DROP/CREATE xinyi_vi_it_e07` → `mysqldump` 克隆 xinyi 完整结构+数据（含视图）→ 清空 66 张 vi_ 表 → 建 `test_environment_guard`（kind=XINYI_VEHICLE_ISOLATED，key_hash=SHA256(VI_TEST_CONTROL_KEY)）→ 建 `test_fixture_run`。带库名安全闸（拒绝非 `xinyi_vi_it_*`、拒绝目标=源）。密码/密钥经环境注入，不写仓库。
- **为何克隆而非纯 init SQL**：仓库 `ry_20260417.sql` 未含 `sys_user_dept_integrity_issue` **视图**（被 `UserDeptIntegrityPatrol` 每日巡检依赖），纯脚本初始化会与实况不一致、有启动/巡检隐患；克隆已验证可启动的 xinyi 结构最可靠。**已记为仓库 init 脚本缺口 finding（见下），非 E07 修复范围。**

## 验证（真实执行，两条命令均 BUILD SUCCESS）
| 项 | 命令 | 结果 |
|---|---|---|
| 生产打包 | `mvn -pl xinyi-admin -am -DskipTests package` | exit 0；fat jar 90MB；`unzip -l` 确认无 `com/xinyi/web/viit/`、无 `__integration` |
| 真实集成 | `mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E07TestingIT verify` | exit 0；**E07TestingIT 5 用例 0失败0错误0跳过**（真实 HTTP→Security→ControlApi→FixtureProvisioner→真实 Service→MyBatis→隔离库 MySQL） |
| 隔离探针 | mysql 探针 | IT 库有夹具产物；业务库 xinyi 零触碰（详见下） |

### E07TestingIT 5 用例
1. `runtime_正确密钥返回守卫信息`：正确控制密钥+隔离库+integration+SIMULATOR+loopback → 200，返回 environment=integration、databaseGuard=XINYI_VEHICLE_ISOLATED、providerMode=SIMULATOR、contractVersion=6.0.0（**反证数据源已切到 xinyi_vi_it_e07**，否则 DB guard 不符会拒）。
2. `runtime_错误密钥被拒`：错误密钥 → 安全层拒绝（请求不达 ControlApi）。**本框架 AuthenticationEntryPoint 以 HTTP200+{code:401} 表达拒绝**（RuoYi“恒 200+body.code”约定），故断言 body.code∈{401,403} 且不含 databaseGuard。
3. `fixtures_IDENTITY返回严格契约且幂等`：POST /fixtures IDENTITY → 201，响应字段恰为契约 5 项；同 providerRunId 二次调用复用同一 runId（幂等）。
4. `fixtures_BOOKING诚实延后不造终态`：BOOKING → `UnsupportedOperationException` 经 GlobalExceptionHandler 包成 {code:500,msg:延后…}；断言不含 runId/vehicleTypeCode、非固定成功。
5. `生产jar不含测试控制面类`：扫描 `target/xinyi-admin.jar` entries，断言无 `com/xinyi/web/viit/`、无 `__integration`。

### 隔离性探针（关键安全属性）
- IT 库 `xinyi_vi_it_e07`：TEST_CAR 字典=1、TEST_INSPECTION=1、test_fixture_run=2、**vi_account=0**（未造业务账号）、**vi_idempotency=0**（未造终态）。
- 业务库 `xinyi`：**TEST_CAR=0**（夹具零触碰）、无 test_fixture_run 表、vi_ 三平台表仍 **0/0/0**。

> 测试第三方替身声明：本任务未涉及外部供应商；仅“未实现业务分发/BOOKING 构造”为诚实延后（抛异常，非替身成功）。未声称任何真实微信/支付/地图/OCR/活体渠道通过，未声称业务端点已实现。

## 附带修正
- **D-E04-5**：`ViHttpSuiteIT` line24 路径 `docs/vehicle/V6.0/...` → `V6.1`（模板残留）。注：ViHttpSuiteIT 跑 pytest live 身份/报价用例，依赖尚未实现的 owner/address/quote 业务端点，**非 E07 验证命令目标**（E07 只跑 E07TestingIT），待业务端点就绪由对应任务驱动。

## 边界与负向（符合 negative_checks）
- 生产 jar 无 __integration：控制面 @Profile(vi-it)+src/test，jar 扫描断言通过。
- 控制密钥和 DB guard 双校验：TestGuard 同时校验密钥(常量时间)+loopback+environment+SIMULATOR+库名正则+guard 表 hash；正确钥放行、错误钥拒绝（body.code 401）。
- 禁止设任意订单目标状态：FixturePort 无订单状态方法；IDENTITY 不造业务/终态；BOOKING 诚实延后。

## 完成判据对照（E07 TASK.md）
- [x] 源接口/用例有明确实现文件与关联测试（FixtureProvisioner/E07TestingIT；非以目录存在当完成）
- [x] 真实集成与负向场景实际通过并保存原始报告（E07TestingIT 5 用例真实 HTTP+MySQL）
- [x] 标注测试第三方替身/延后，不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）

## Findings / 遗留
- **仓库 init 脚本缺口（非 E07 引入，建议单列）**：`sys_user_dept_integrity_issue` 是 xinyi 实况中的 **VIEW**（`UserDeptIntegrityPatrol` 每日 2:30 巡检依赖），但 `ry_20260417.sql`/quartz.sql/各 migration 均无其 DDL。违反根 AGENTS.md「数据库变更必须随代码提供可移植 SQL、完整初始化脚本须能一次初始化到最新结构」。建议补一条 migration 收录该视图 DDL 并同步 ry_20260417.sql；E07 用克隆法规避，未擅改业务库或补写该视图（避免越权改框架数据）。
- FixturePort 的 BOOKING/clock/runDue/provider-event 待 C02/C03/C04、I01/I03、支付/任务业务落地后扩展。
- ci-stage.json 仍 FOUNDATION；未进入 IDENTITY/BOOKING 业务验收（需业务端点）。
- 隔离库 xinyi_vi_it_e07 含夹具残留（TEST_CAR 字典+2 run 登记），一次性可弃，可经 bootstrap_it_db.sh 重建。
