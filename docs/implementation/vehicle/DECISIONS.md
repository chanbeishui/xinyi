# 当前工作区适配裁决

工程路径/基线以workspace.json为输入；不得自行降低安全边界。

## 起点裁决（E00）

- D-00 起点：用户未做 M0，当前为改版若依框架 `rongli-wx-home@18b5ce4`（用户确认与 `rongli-wx` 等同）。业务模块 CREATE_NEW（xinyi-rongli `com.xinyi.rongli` + xinyi-miniapp uni-app）；不重建 PC 框架与 sys_user 体系；无历史业务数据迁移（legacyViMigration=NOT_APPLICABLE）。
- D-00b 差异处理：HEAD 领先资料包观察点一个改名提交、AGENTS.md 因 install 追加受管区段、package.json 因改名变化——均如实记录，不回退、不改 baseline 指纹掩盖。

## 本轮评审残留项裁决（R1–R12，纳入 M0）

### E01 处理（契约/模型一致性，不依赖 Docker）

- R1 未绑定枚举列：输出全部未绑定枚举列实际清单，补齐取值、可空性与 CHECK/字典绑定。已知至少 12 列（含 `vi_inspection_result.conclusion` PASS/FAIL、`vi_dispatch_invite.source`、`vi_event_log.actor_realm`、`vi_aftersale_case.kind/return_state/refund_state`、`vi_aftersale_review.decision`、`vi_driver_fee_ledger.entry_kind`、`vi_session.active_role`、`vi_driver_statement_membership_change.action`、`vi_evidence_group.mileage_source`、`vi_inspection_result.source`）。
- R2 refund_state 语义裁决：判断 `vi_aftersale_case.refund_state` 与 `vi_refund_order.status`（ChannelRefundStatus）是否同义。**同义则统一为单一枚举；不同义则分开命名并写明映射表，不简单取并集**。当前矛盾：字典 RefundStatus 9 值含 CLOSED，列注释 8 值无 CLOSED。
- R3 写白名单分三层：①不可变历史表（event_log/snapshot/membership_change/price_change/aftersale_review 等）= insert-only，updateColumns 置空；②受控生命周期表（**session/token 必须保留角色切换、令牌消费、撤销、轮换等受控更新**，不得一律 insert-only）；③可编辑草稿表（statement/line 等）= 正常 update 白名单。逐表标注层级。
- R4 ViRecord 唯一来源：指定 `14_工程模板/backend/src/main/java/com/xinyi/rongli/core/ViRecord.java`（含 `Long id`）为唯一权威版；`10_技术架构_T3.1/参考代码/ViRecord.java`（缺 id）作废或标注为参考。自然键实体（occupancy/cursor/location/guard）单独映射，不套 ViRecord 的 id。
- R5 service-config 登记：`GET /api/public/service-config` 补录进接口全量清单.md（248→249）与权限与能力注册表.json operations，并登记 PUBLIC 授权（x-authorization PUBLIC_WHITELIST）。
- R6 共享端点四要素：handover/files/uploads/employee-binding 等 common 共享端点，按 MINI / 员工MINI / ADMIN 三类调用方分别明确「能力码 + 功能权限码 + 数据范围 + 状态要求」四要素，**不以任意一个条件满足就放行**；解决注册表 biz:* 与 yaml x-capability 双重登记歧义，写明服务端以何为准。
- R7 术语统一：契约统一用 `x-authorization.scope`，清除认证文档中 `x-data-scope` 漂移表述。
- R8 同步派生物：修改主模型/主契约后，同步更新 task_packets 片段、dto-types、订单四视角样本、枚举清单、contract_tests，不只追加说明文档。

### E03 处理（数据落地，依赖 E02 环境）

- R9 迁移脚本形态：将 `001_vi业务表_初始化_V6.0.sql` 改造为符合根 AGENTS 的 `sql/migration_YYYYMMDD_vi_init.sql`，头部写明用途/前置版本/执行顺序/备份要求/不可重复执行条件/失败恢复；同步把 66 表并入 `sql/ry_20260417.sql`；验证「增量脚本」与「完整初始化」两条路径最终结构一致。001 无 IF NOT EXISTS、DDL 隐式提交，须显式标注不可重复执行、不套 START TRANSACTION。
- R10 种子数据：补 sys_menu 菜单/按钮权限（来源=权限注册表 126 码）、sys_dict（vehicle_type/inspection_type 及各状态枚举）种子；**不硬造已有 sys ID、不默认授予全部员工权限**；确认 ry 中 vehicle_type 字典存在或补种子。
- R11 排序规则：不擅改原 sys 表排序规则；vi 用 utf8mb4_0900_ai_ci，vi↔sys 字符串列 join/比较按实际边界显式处理，防 Illegal mix of collations。
- R12 MySQL 版本与 UTC 探针：连库确认服务端 ≥8.0.22（CHECK 强制、表达式默认值）；真实探针验证 CHECK 拒非法值、生成列写拒绝、自然键唯一、UTC_TIMESTAMP(3) 默认值、乐观锁 version。

## 环境/工具链裁决

- D-ENV-1 Docker 不可用（exitCode 127）：E02 的 compose 隔离 MySQL8.4+Redis 路径不可用。**用户最终决定（2026-10-21，覆盖此前"本机独立库 xinyi_vi_dev"方案）：vi 表初装到现有 `xinyi` 库（与框架 sys_ 表同库，RuoYi 单数据源），Redis 复用现有 6379，不另建任何实例。** 已删除误建的 `xinyi_vi_dev` 库+`vi_dev` 用户、停止误起的 Redis 36379；用户原库/原 Redis 未受影响。实测 `xinyi` 库排序规则 utf8mb4_0900_ai_ci 与 vi 表一致→R11 collation 混用风险消除。MySQL 服务端 9.6.0（vs 目标 8.4）：满足 ≥8.0.22，M0 开发可用，生产 8.4 须复跑 SQL/CAS（RELEASE 门槛）。E03 在 `xinyi` 库增量建 66 张 vi_ 表（IF NOT EXISTS 防护、绝不动 sys_ 及其他既有表）。
- D-ENV-2 Maven 绑定 JDK25 与运行 JDK21 不一致：E04/E05 构建前统一（JAVA_HOME 指向 21 或配置 maven toolchain release17），避免 release17 编译落在 JDK25 上的未验证组合。
- D-ENV-3 Node v22.16.0 满足 uni-app 模板 engines，无需 nvm 切换、不放宽 engines。

## E04 裁决（工程基座构建引出）

- D-E04-1 微信 wx.request 不支持 PATCH：miniapp http.ts 用 POST + `X-HTTP-Method-Override: PATCH` 表达契约的 PATCH 端点（owner/profile、driver/listening 等）。**后端（xinyi-rongli/admin）须启用方法覆盖过滤器**识别该头还原为 PATCH，否则 @PatchMapping 路由不到；E05/E06 落实，或后续契约把 MINI 的 PATCH 改 PUT/POST（二选一，倾向后端加过滤器，改动小）。
- D-E04-2 backend-build 须激活 vi-it profile：admin 的 spring-boot-starter-test 仅在 vi-it profile，`mvn ... -DskipTests package` 不带 -Pvi-it 会因 viit 测试骨架编译失败。E04 用 `-Pvi-it` 构建成功。建议把 engineer.py 的 backend-build kind 补 -Pvi-it，或将 spring-boot-starter-test 移到 admin 主 test 依赖。
- D-E04-3 @dcloudio/types 对齐 3.4.31（uni-app peer 要求），非 --legacy-peer-deps 掩盖；package-lock.json 来自真实 npm install（8063 行）已入库，后续 npm ci。
- D-E04-4 微信 AppID = wx22f41349e671b362 配入 manifest.json mp-weixin（AppID 为公开标识，可入库）；DCloud uni统计 appid 未配（可选，构建告警可忽略）。
- D-E04-5 待修：ViHttpSuiteIT.java line24 路径 `docs/vehicle/V6.0/...` 应为 V6.1（模板残留），E07 绑真实 IT 前修正。

## E05 裁决（PC 接入）

- D-E05-1 vi PC 客户端 = 独立 axios 实例 `xinyi-ui/src/api/rongli/`（viError 映射 + request 适配器），复用框架 sys_user JWT（ADMIN 域），**不改 utils/request.ts 与 errorCode.ts**；/api 经 VITE_APP_BASE_API 代理只出现一次。
- D-E05-2 前端测试框架：xinyi-ui 无 test 框架，按根 AGENTS「先约定再引入」**未擅自加 vitest**；E05 用 Node22 原生 `--experimental-strip-types` 跑纯逻辑单测（viError 11 断言）。完整组件/E2E 框架选型留待与用户约定后单列任务。
- D-E05-3 既存技术债（非本项目引入）：`vue-tsc --noEmit` 全项目 26 个 `Module 'vue' has no exported member App/createApp` 类错误（main.ts/plugins/directive/vue-cropper），vite build 不受影响。建议单列技术债任务评估，不在机动车任务内顺带改（避免无关改动）。

## E06 裁决（平台基座：幂等/审计/outbox/有序锁）

- D-E06-1 落位 `xinyi-rongli/src/main/java/com/xinyi/rongli/platform/`（error/actor/mybatis/json/config/domain/mapper/service/web 子包）+ `resources/mapper/rongli/*.xml`。经 `@MapperScan("com.xinyi.**.mapper")` 与 `classpath*:mapper/**/*Mapper.xml` 被框架自动纳入；纯 MyBatis，不引入 MyBatis-Plus（ADR-05）。vi 表 UTC 用专用 `ViUtcInstantTypeHandler` 按列显式引用，不全局改 sys 映射（ADR-03）。
- D-E06-2 修复 D-E04-2：`spring-boot-starter-test` 从 vi-it profile 移入 xinyi-admin **主 test 依赖**，使 viit 测试骨架在普通 `mvn test`（无 profile）下可 testCompile；vi-it profile 仅保留 failsafe（IT 执行）。
- D-E06-3 failsafe 增 `classesDirectory=${project.build.outputDirectory}`：`spring-boot:repackage` 在 package 阶段把 xinyi-admin.jar 变成 BOOT-INF/classes 结构 fat jar，failsafe 默认把项目 artifact 当 classpath 会导致 IT 主类 `RuoYiApplication` TypeNotPresentException；显式用 target/classes 解决。
- D-E06-4 vi 平台自持隔离 JSON 工具 `ViJson`（内部私有 ObjectMapper）：本 RuoYi 分支 HTTP 序列化用 fastjson2、上下文**无 Jackson ObjectMapper Bean**（注入会致 auditService 创建失败）；**不注册全局 ObjectMapper Bean**，避免触发 JacksonAutoConfiguration 退避而改变框架响应序列化。仅供 vi 内部 result_json/payload/summary 使用。
- D-E06-5 双主体审计：`Actor`（不可 HTTP 反序列化构造）+ `BusinessActorResolver`（唯一读 SecurityContext 边界，MINI 优先 ADMIN，都无则 SYSTEM；Service 不强转 SecurityUtils.getLoginUser）+ `MiniPrincipal` **只读契约**。MINI 落 actor_account_id、ADMIN 落 actor_user_id，`samePrincipal` 仅同域比较，跨域同人须经 vi_employee_binding（I01/I03），结构上拒绝 accountId==sysUserId 伪等价（ADR-02）。MiniPrincipal 具体认证实现留 I01/I03，E06 不建会话/令牌持久化（vi_session* 非本任务表）。
- D-E06-6 幂等 MAC=HMAC-SHA256(`vi.lookup-key`, body)，32 字节匹配 BINARY(32)；空密钥拒绝计算（VI_CAPABILITY_NOT_CONFIGURED），不以空密钥静默生成。`vi.lookup-key` 经 `@Value("${vi.lookup-key:${VI_LOOKUP_KEY:}}")` 优先属性、回退环境变量，**无需改框架 application.yml**。重复键不同体→409 VI_IDEMPOTENCY_CONFLICT（常量时间比较）；登录/刷新类 cacheResult=false 只去重不缓存体。
- D-E06-7 outbox 中继 `ViOutboxRelay` 与框架 `SecurityOutboxProcessor` 各自独立 Bean，不注册第二个 SessionInvalidator、不触碰 sys_security_outbox（ADR-02）。时钟=DB 到期扫描+`FOR UPDATE SKIP LOCKED`+租约恢复，不依赖 Redis 过期通知（ADR-08）。三段式：领取(短事务)→分发(事务外)→结算(短事务)；无分发器的事件保持 READY 不计 attempts。`OutboxDispatcher` 为 SPI，具体业务分发器由后续任务注册。
- D-E06-8 落实 D-E04-1：`HttpMethodOverrideFilter` 仅 `/api/*`、仅 POST、仅 {PATCH,PUT,DELETE} 改写方法；`ViWebConfig` 以 FilterRegistrationBean order=-200 注册（早于 Security -100），不影响 /system 框架端点、不放开任意方法。
- D-E06-9 V6 ApiError 信封的 `@RestControllerAdvice` 渲染**留待首个 vi Controller 任务**：E06 无 Controller，服务层直接抛 `ViException`（携 errorCode/HTTP 状态/fieldErrors/conflicts/retryAfter），IT 断言其码与状态；避免在无 Controller 时引入空切面。`ViErrorCode` 枚举为后端单一错误码来源（与 04_接口/错误码表.json 28 码对齐），后续 Controller/Handler 复用。
- D-E06-10 IT 数据卫生：E06PlatformIT 用 `__E06_IT__`/`E06_IT:`/aggregate_type=`E06_IT` 标记，@BeforeEach+@AfterEach 经 JdbcTemplate 清理；真实 xinyi 库零残留（探针确认三表总行数 0）。vi_event_log 业务 append-only（不提供 update/delete mapper），DELETE 仅测试卫生。后台调度经 `vi.outbox-relay-enabled=false` 关闭，中继由测试显式 runOnce() 驱动避免竞态。table_implementation_map.json 沿用 M0 既有模式（全 66 表 NOT_ASSESSED，G01 期统一对账），本任务不逐条改写。

## E07 裁决（测试夹具与控制平面）

- D-E07-1 隔离测试库 **xinyi_vi_it_e07**（用户批准）在现有 MySQL 3306 服务端新建，一次性可弃，控制面/夹具只在此库运行，**业务库 xinyi 全程零改动**（探针证明 xinyi 无 TEST_CAR、无 test_fixture_run、vi_ 三表仍 0）。库名匹配 TestGuard/test_schema.py 的 `xinyi_vi_(dev|it_[a-z0-9_]+)` 正则。引导脚本 `E07/bootstrap_it_db.sh`（带库名安全闸，密码/密钥经环境注入不入库）。
- D-E07-2 **克隆而非纯 init SQL**：仓库 `ry_20260417.sql`(89表)+quartz.sql(11)+多部门/菜单迁移合并=101 表，但 xinyi 实况 102 对象——缺 `sys_user_dept_integrity_issue` **视图**（`UserDeptIntegrityPatrol` 每日巡检依赖），纯脚本初始化会与实况不一致。故 mysqldump 克隆已验证可启动的 xinyi 完整结构（含视图）最可靠。**记为仓库 init 脚本缺口 finding（违反根 AGENTS.md「完整初始化脚本须一次到最新结构」），建议单列任务补该视图 DDL 入 migration+ry_20260417.sql；E07 未擅改业务库/未补写视图（避免越权）。**
- D-E07-3 控制面只在 `src/test` + `@Profile("vi-it")`：`FixtureProvisioner`(实现 FixturePort，调真实 ISysDictDataService/ISysDictTypeService/ISysUserService，不 mock、不返回固定成功) 经 `@Bean` 装配进 `ViIntegrationTestConfiguration`；生产 fat jar 扫描确认无 `com/xinyi/web/viit/`、无 `__integration`（negative check）。
- D-E07-4 **分步 FixturePort**：仅 IDENTITY 落地（TEST_CAR/TEST_INSPECTION 测试字典经真实框架 Service 幂等确保 + `test_fixture_run` 按 (providerRunId,kind) 幂等 + 严格 5 字段契约响应）；BOOKING/advanceClock/runDue/applyProviderEvent **诚实抛 UnsupportedOperationException 标明延后**（依赖 C02-C04/I01-I03/支付/任务），绝不造 PAID/RETURNED/CONFIRMED 终态或任意订单目标状态（FixturePort 无订单状态方法）。
- D-E07-5 **控制面错误经框架“恒 200+body.code”**：`/__integration/*` 在 RuoYi 应用内，安全拒绝经 `AuthenticationEntryPointImpl`→HTTP200+{code:401}，控制器异常经 `GlobalExceptionHandler`→HTTP200+{code:500,msg}。故 E07TestingIT 对拒绝/延后**断言 body.code 而非 HTTP 状态**（守卫确已拒绝、请求不达 ControlApi；BOOKING 确已诚实失败）。这是测试控制面（非 vi 业务 API）；vi 业务 API 仍按 ADR-04 用真实 HTTP 状态（E06 ViErrorCode/ViException 已备，待首个 vi Controller 的 @RestControllerAdvice 渲染）。
- D-E07-6 E07TestingIT 用 `RestTemplate`（配 no-throw ErrorHandler 断言状态/体），因 Spring Boot 4.0.6 测试模块无 `TestRestTemplate`（org.springframework.boot.test.web.client 包不存在）。数据源经 `@SpringBootTest(properties)` 覆盖 `spring.datasource.druid.master.url` 指向 xinyi_vi_it_e07（复用 root 凭据、不硬编码密码）；控制密钥经 OS 环境 `VI_TEST_CONTROL_KEY` 注入（TestGuard 用 env.getRequiredProperty 解析）。
- D-E07-7 修 D-E04-5：ViHttpSuiteIT 路径 V6.0→V6.1。ViHttpSuiteIT 跑 pytest live（owner/address/quote），依赖未实现业务端点，**非 E07 验证命令目标**（E07 只跑 E07TestingIT），待业务端点就绪由对应任务驱动；ci-stage 仍 FOUNDATION。

## E08 裁决（CI/任务台账/证据）

- D-E08-1 新增仓库侧 CI harness `docs/implementation/vehicle/E08/ci_run.py`（仅标准库），与资料包 `engineer.py` **互补不重复**：engineer.py 跑预置 kind 命令集（backend-build/db-test/live-test/integration-*，JUnit 落 evidence/），ci_run.py 侧重 **tasks.json 台账聚合 + 四图覆盖对账（api249/page287/table66/case298）+ 分层 NOT_RUN 诚实**。
- D-E08-2 **跨平台命令执行**：`shlex.split`（posix 按平台）+ `shutil.which` 解析可执行（Windows 命中 mvn.cmd/python.exe）、`python`→`sys.executable`、`subprocess(shell=False)`——不经平台 shell，避免差异与命令注入。**失败退出码原样传播**（失败命令 exit3→整体 exit3；未知任务/无命令→exit2）。
- D-E08-3 **诚实分层，不用 FOUNDATION 绿色代替全功能**：report 将 L01_REAL_CHANNEL/L02_DEVICE/L03_DEPLOY 明确 NOT_RUN、LIVE_HTTP_MYSQL=PARTIAL（E06 服务层+E07 控制面真实 HTTP/MySQL，业务端点未建）；verifiedWithEvidence 仅纳 status∈{VERIFIED,REVIEWED} 且 evidence 文件真实存在者（当前 E00-E07）；api implemented 以 controllerFile/serviceFile 真实存在计数（当前 0）；case passed 以 status=PASS+testMethod 真实计数（当前 0）。ci-stage 停 FOUNDATION，前移需业务端点+真实集成由后续任务升级。
- D-E08-4 验证分两层：`test_ci_run.py`（pytest，tmp **受控测试工作区**驱动 ci_run，6 用例：退出码传播/跨平台/覆盖/NOT_RUN 不算完成）+ `E08PlatformIT`（轻量 JUnit IT，**不启 Spring**，shell 到 ci_run.py report 对真实仓库聚合断言）。E08PlatformIT 经 `mvn -Pvi-it -Dit.test=E08PlatformIT verify` 跑（1/1）。资料包套件 72 + engineer.py check 34/0 为 E08 两条列名命令。
- D-E08-5 JUnit 报告统一归集 evidence/：mvn surefire/failsafe XML + pytest --junitxml（pkg-suite-junit.xml、e08-selftest-junit.xml）；ci_run.py collect_junit 汇总，report 列 junitReports（当前 8 个）。


## D-C03-20261101 · 每站独立双额度（用户本轮明确）

用户选择「每个站点独立维护接车和接收额度」。PICKUP/STATION_RECEIVE 均归真实站点，部门归属通过 vi_station.dept_id 解析；scope_key 服务端生成 station:<id>。不新建共享区域额度，不默认采用操作者主部门。未填写地址时只展示候选时间，不预占或承诺具体站点。既有无归属额度只报告，不猜测回填。

C03增加后台创建未来时段动作，原4接口保留；GET的slotId+proposedTotal为只读影响预览。总额度、停约状态可受控调整，预占/正式/已消耗计数不接受客户端写入。当前API总数288，任务仍85项。

## D-C04-20261101 · 公司共享计价与分级舍入（用户本轮明确）

用户选择「同公司统一维护，多个站点共用」及「计算后四舍五入到分」。owner_dept_id为明确选择的公司权威部门，站点通过自身dept_id及部门树关联，不推断为操作者主部门。环境由部署确定为TEST/LIVE。往返先乘二再扣包含米，EXACT_PRORATA使用精确整数比例HALF_UP到分一次；等待费仍受P02约束。

C04补充保存版本试算、生效匹配试算、停用3接口，保留原6接口；总数291。显式替换使用旧规则ID+版本，审核后配置冻结、独立审核哈希绑定，原子发布不覆盖历史。未知旧记录只报告不猜测回填。实现与验证见C04/report.md。

## D-B02-20261102 · 车主选站、腾讯路线

用户明确选择「让车主从可用站点中选择」和「腾讯位置服务」。新增只读候选站点查询，正式报价必填stationId并重新校验；不默认取第一站，不将司机轮转策略套用到站点。P04交接/缓冲/提前量/有效期由现有C01业务配置维护，缺值不可报价，未将测试值发布到生产。详见B02/design.md和B02/report.md。
