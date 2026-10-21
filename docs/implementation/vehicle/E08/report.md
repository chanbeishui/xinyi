# E08 CI、任务台账与证据收集 · 报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。

## 交付（对照 TASK.md：跨平台任务命令、接口页面表用例覆盖、JUnit 报告、失败退出码）

新增仓库侧 CI/任务/证据 harness `docs/implementation/vehicle/E08/ci_run.py`（仅标准库，任意 python3 可跑），与资料包 `engineer.py`（run --kind 的 backend-build/db-test/live-test/integration-* + JUnit 落盘）互补——ci_run 侧重 **tasks.json 台账聚合 + 覆盖对账 + NOT_RUN 诚实分层**：

- **跨平台任务命令**：`ci_run.py run --task <id>` 读 tasks.json 的 commands，`shlex.split` 分词 + `shutil.which` 解析平台可执行（Windows 命中 mvn.cmd/python.exe）、`python`→当前解释器，`subprocess(shell=False)` 执行（不经平台 shell，避免差异/注入）。
- **失败退出码**：任一命令非零→整体非零原样传播；未知任务/无命令→exit 2；运行清单写 `evidence/ci-run-<task>.json`。
- **JUnit 报告**：`collect_junit` 归集 mvn `surefire-reports/*.xml`、`failsafe-reports/*.xml` 与 pytest `--junitxml` 产物（evidence/*-junit.xml）。
- **接口/页面/表/用例覆盖**：`ci_run.py report` 对账四个 implementation_map（api 249 / page 287 / table 66 / case 298），按“实现文件真实存在”计 implemented、按 status 分组，聚合 tasks.json 状态 + verifiedWithEvidence + ci-stage + 分层执行情况。

## 验证（真实执行，全部 exit 0）
| 项 | 命令 | 结果 |
|---|---|---|
| 资料包套件 | `pytest 12_工程执行/tests -q --junitxml=evidence/pkg-suite-junit.xml` | **72 passed**（contract_regression/framework_start/simulator/tools）；JUnit 10178B |
| 工程结构 | `engineer.py check --repo .` | **pass=34 fail=0** |
| harness 受控自测 | `pytest E08/test_ci_run.py -q --junitxml=evidence/e08-selftest-junit.xml` | **6 passed**（tmp 工作区驱动 ci_run） |
| 台账聚合 IT | `mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E08PlatformIT verify` | **E08PlatformIT 1/1**，BUILD SUCCESS |

### test_ci_run.py 6 用例（受控测试工作区，对应 done_when）
1. run 模式通过命令 → exit 0 + 清单 exitCode 0
2. run 模式失败命令（sys.exit(3)）→ **exit 3 原样传播**（失败退出码）
3. run 模式未知任务 → exit 2
4. resolve_argv 跨平台：`python`→sys.executable、不存在可执行不抛异常、shell=False
5. report 模式：覆盖总数对账 + L01/L02/L03=NOT_RUN + ci-stage 透传 + verifiedWithEvidence
6. **NOT_RUN/无实现不算完成**：FAIL_TASK（NOT_ASSESSED）不入 verifiedWithEvidence、无实现文件不计 implemented

### E08PlatformIT（真实工作区聚合）
驱动 `ci_run.py report` 对真实仓库聚合，断言：ci-stage=**FOUNDATION**；verifiedWithEvidence 含 **E00–E07**；api 覆盖 total=249、case total=298；业务 **implemented=0**（M0 只建基座，不虚报完成）；**L01 真实渠道 / L02 真机 / L03 部署 = NOT_RUN**。

### report 模式真实输出（诚实定格）
- ciStage=FOUNDATION；tasks 78（development 75，VERIFIED 8=E00-E07 带证据，NOT_ASSESSED 70）
- coverage：api 249/implemented 0；page 287；table 66（G01 统一对账，E06 平台表已有真实 mapper 见 evidence/E06.json）；case 298/passed 0（全 NOT_RUN）
- layers：BUILD/TOOLS_OR_COMPONENT/MYSQL_PROBES=RUN，LIVE_HTTP_MYSQL=**PARTIAL**（E06 服务层+E07 控制面真实 HTTP/MySQL，业务端点未建），L01/L02/L03=**NOT_RUN**
- junitReports：8 个（E06/E07 的 surefire/failsafe XML + 本任务 2 个 pytest XML）

## 边界与负向（符合 negative_checks）
- **不允许 NOT_RUN/skip 算通过**：未实跑层显式 NOT_RUN；case 298 全 NOT_RUN、passed=0；失败命令 exit 码原样传播不吞零。
- **无测试/无实体行为不得标 DONE**：verifiedWithEvidence 仅纳 status∈{VERIFIED,REVIEWED} 且 evidence 文件真实存在者；implemented 以文件真实存在计数；ci-stage 停 FOUNDATION，不用基座绿色代替全功能。

## 完成判据对照（E08 TASK.md）
- [x] CI/任务/证据工具在受控测试工作区验证（test_ci_run.py tmp 工作区 6 用例），阶段从 FOUNDATION 开始
- [x] 未实跑的数据库/HTTP/真机明确 NOT_RUN（L01/L02/L03=NOT_RUN，LIVE_HTTP_MYSQL=PARTIAL）
- [x] 测试阶段按后续能力升级，不用 FOUNDATION 绿色代替全功能（ci-stage 前移需业务端点+真实集成，由后续任务升级）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）

## 遗留/后续
- ci-stage 前移 FOUNDATION→IDENTITY→BOOKING→FULL 需对应业务端点（A/C 系列）+ 真实集成，由后续任务按实际能力升级。
- 四个 implementation_map 的业务覆盖随切片前移；table map 于 G01 统一对账（沿 D-E06-10）。
- ci_run.py 与 engineer.py 分工：engineer.py 跑预置 kind 命令集（含真实 DB/HTTP/集成），ci_run.py 做台账聚合+覆盖对账+诚实分层；二者证据同落 evidence/。
