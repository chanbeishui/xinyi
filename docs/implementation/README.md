# 实施任务台账

本目录记录荣利机动车检测预约二期的实施任务、状态与**可复核证据**。
原则：每条任务只认"能重跑的命令 + 能看到的输出"，不写"已完成（主观判断）"。

## 台账字段

| 字段 | 含义 |
|---|---|
| 编号 | 任务编号，`E**` 为工程类、`D**/Q**/N**/B**` 为业务类 |
| 任务 | 一句话说明交付内容 |
| 状态 | `DONE` / `DOING` / `TODO` / `BLOCKED` |
| 证据 | 能复现结论的命令（优先仓库内脚本） |
| 结论 | 该命令的输出要点 |

## 证据来源（全部在仓库内）

| 脚本 | 覆盖范围 | 依赖 |
|---|---|---|
| `logs/ci_sql_check.sh` | compose 挂载完整性 + 基础脚本与全部迁移按序执行、**幂等复跑**、80 张 `vi_*` 表 / 4 分组 / 20 可见页断言 | MySQL |
| `logs/check_api_routes.py` | 小程序 end + 后台 end 的接口路径与请求方法是否与后端路由一致 | 无 |
| `logs/check_mapper_schema.py` | 13 个 Mapper 的 SQL 与真实表结构是否一致 | MySQL |
| `logs/check_wxml.py` | 小程序 WXML 语法与节点闭合 | 构建产物 |
| `logs/verify_groups_stats.py` | 侧栏 4 分组、业务统计 B23、支付渠道核对 B31 | 后端 18080 |
| `logs/check_admin_pages.py` | 后台补齐页面接口连通性 | 后端 8080 |
| `logs/smoke_regression_rongli.py` | 存量后台接口冒烟（回归用） | 后端 18080 |
| `logs/verify_remaining.py` | D02 多轮扩圈、Q03 播放心跳、N01 订阅消息、部门数据权限 | 后端 18080 + MySQL |
| `logs/verify_token_refresh.py` | 小程序登录令牌续期：切换角色/站点轮换、并发续期单次消费、迟到 401 快照重放 | 后端 18080 |
| `logs/verify_phone_login.py` | 手机号授权登录、身份边界（车主/代驾/检测员工）、后台新增站点绑定、绑定迁移、短信兜底 | 后端 18080 + MySQL |
| `logs/verify_driver_flow.py` | 履约全链路 40 步：资格→听单→派单→接单→行程→四类交接（站内交接含**双方确认**）→检测登记→完成 | 后端 18080 + MySQL |
| `logs/verify_aftersale_flow.py` | 售后/申诉/对账三条链路端到端：取消退款→审核→渠道退款→订单取消；罚款→申诉→撤销；对账异常→重查→结论 | 后端 18080 + MySQL |
| `logs/verify_config_pages.py` | 系统设置三项配置页：必填清单、格式校验、失败不落库、保存即生效（含备份还原） | 后端 18080 |
| `logs/verify_time_display.py` | 时间口径：接口只允许 ISO8601 UTC、字段驼峰、时段文案与原始值一致、单号内嵌时间与 created_at 对得上 | 后端 18080 |
| `logs/audit_mini_flows.py` | 主链路实测探针（打印每步真实状态，不做断言，用于定位断点） | 后端 18080 |
| `logs/verify_integration_config.py` | V6.4 第三方配置：表/列/菜单/参数、35 个端点注册、草稿→校验→发布生命周期、双关与缺凭据红线、退役端点已下线 | 后端 18080 + MySQL |
| `logs/verify_notification_dispatch.py` | V6.4 消息自动下发：业务事件→路由→模板→投递、未配置时的跳过原因码、`uk_delivery_event` 去重、通知失败不影响业务 | 后端 18080 + MySQL |
| `logs/run_acceptance.py` | **汇总以上全部断言，产出 `logs/evidence.json` 与 `evidence.md`** | 视套件而定 |

统一入口：

```bash
python logs/run_acceptance.py            # 全量
python logs/run_acceptance.py --static-only   # 只跑静态校验（不需要起服务）
```

配套说明文档：

| 文档 | 内容 |
|---|---|
| `g01-g06-delivery.md` | G01~G06 完成情况、三项配置的必填字段与校验规则、交接双方确认流程、证据命令 |
| `time-display.md` | 时间口径：三种时间形状的成因、统一方案、新增代码约定与验证方法 |
| `token-refresh-diagnosis.md` | 小程序令牌频繁失效的定位过程与结论 |
| `mini-auth-flow-review.md` | 手机号授权登录与身份边界整改 |
| `v6.4-sync-diff.md` | **V6.4 资料包同步：逐项差异清单、修掉的缺陷、未同步项与需确认事项** |

## 当前台账

| 编号 | 任务 | 状态 | 证据 | 结论 |
|---|---|---|---|---|
| E00 | 任务台账与证据规范 | DONE | 本文件 | 台账字段与证据脚本清单已固定 |
| E01 | 后端服务层补齐（售后/管理端/媒体/消息/处罚培训/微信通知） | DONE | `logs/check_mapper_schema.py` | 13 个 Mapper SQL 与表结构一致 |
| E02 | Controller 层与接口清单对齐 | DONE | `logs/check_api_routes.py` | 后端 272 条路由，两端路径与方法完全一致 |
| E03 | 定时任务与启动配置 | DONE | `logs/verify_remaining.py`（outbox 消费项） | 订阅事件入队 → 消费 DONE，无死信 |
| E04 | 小程序端页面（含司机处罚申诉/培训考试/收支流水） | DONE | `logs/check_wxml.py` | 27 个 WXML，问题数 0 |
| E05 | 后台管理前端页面 | DONE | `logs/check_admin_pages.py` | 20 条页面接口连通 |
| E06 | 存量接口回归 | DONE | `logs/smoke_regression_rongli.py` | 28 个接口全部 200 |
| E07 | 侧栏 4 分组 / 21 可见页 | DONE | `logs/ci_sql_check.sh`、`logs/verify_groups_stats.py` | 分组=4，可见页=21，接口 24 项通过 |
| E08 | 隔离环境与 CI | DONE | `logs/ci_sql_check.sh`、`docker-compose.yml`、`.github/workflows/` | 18 个迁移首轮 + 复跑全通过；compose 挂载与 `sql/migration_*.sql` 全量一致（已由脚本第 0 步断言，不再靠人工维护清单） |
| E09 | 验收执行器与证据台账 | DONE | `python logs/run_acceptance.py` | 15 个套件、327 条断言全通过（100%，0 跳过），输出 evidence.json/md |
| E10 | 资料包 298 条验收规格入台账 | DONE | `docs/acceptance/test_cases_V6.0.json` + `logs/evidence.md` 第二节 | 298 条（P0 231 / P1 67）按模块汇总，状态以规格文件为准 |
| E11 | 小程序登录令牌频繁失效修复 | DONE | `python logs/verify_token_refresh.py` | 14 项通过：切换角色/站点令牌落盘、并发续期单次消费、迟到 401 快照重放；详见 `token-refresh-diagnosis.md` |
| E12 | 小程序手机号授权登录与身份规则整改 | DONE | `python logs/verify_phone_login.py` + `python logs/verify_driver_flow.py` | 29 项 + 40 步全通过：授权换号、手机号自动绑定员工、首次仅车主、代驾需审核、检测员工需后台配站点；详见 `mini-auth-flow-review.md` |
| E13 | 真实微信登录通道（jscode2session） | DONE（待配置） | `WechatMiniClient.resolveOpenId` | 联调走稳定标识、真实模式调官方接口；需配 `rongli.wechat.mock=false` + `appId/appSecret` |
| E14 | 真实微信支付 | DONE（见 G01） | `logs/verify_config_pages.py`、`logs/verify_aftersale_flow.py` | APIv3 下单/签名/回调验签解密/退款/查退款全链路已实现；真实扣款需商户凭据 |
| G01 | 微信支付 JSAPI 下单、调起参数签名、回调验签解密、退款与查退款 + 独立配置页 | DONE（待配置） | `python logs/verify_config_pages.py`、`WechatPayClient`、`PayConfigService` | 27 项配置校验通过；联调通道可下单支付，切 `WECHAT` 后走真实 APIv3；当前缺商户凭据 |
| G02 | 真实短信通道（阿里云/腾讯云签名、频控、发送留痕）+ 独立配置页 | DONE（待配置） | `python logs/verify_config_pages.py`、`SmsClient`、`SmsService` | 频控三项区间校验生效；`vi_sms_send_log` 留痕；当前缺供应商凭据 |
| G03 | 订阅消息模板/字段映射/运行环境配置 + 独立配置页 | DONE（待配置） | `python logs/verify_config_pages.py`、`logs/verify_remaining.py`（N01 段） | 4 个场景模板可配、字段映射 JSON 校验、缺失只提示不锁死；当前缺模板 ID 与小程序凭据 |
| G04 | 站内交接（`DRIVER_TO_STATION`）需代驾司机侧也确认 | DONE | `python logs/verify_driver_flow.py` | 核验方输码后 `PARTIAL`，司机端确认后 `CONFIRMED` 才推进订单；断言 `PARTIAL → CONFIRMED → COMPLETED` |
| G05 | `sys_user.phonenumber` 唯一校验 | DONE | `sql/migration_20261022_06_...sql`、`UserAuthorizationMutationServiceImpl` | 生成列 `phone_unique` + 唯一索引 `uk_sys_user_phone`（仅未删除且非空参与）；冲突翻译为业务提示 |
| G06 | 售后/申诉/对账端到端回归 | DONE | `python logs/verify_aftersale_flow.py` | 39 项通过：售后结案+订单取消+退款成功且金额一致；申诉撤销处罚；对账登记结论关闭 |
| D02 | 派单多轮扩圈 | DONE | `logs/verify_remaining.py`（D02 段） | 13 项通过：阶梯可配、逐轮留痕、耗尽转人工、人工邀约继承轮次 |
| Q03 | 播放心跳与学时可信度 | DONE | `logs/verify_remaining.py`（Q03 段） | 9 项通过：会话归属、幂等、跳变拦截、间隔封顶、过期拒绝 |
| N01 | 微信订阅消息 | DONE | `logs/verify_remaining.py`（N01 段） | 8 项通过：模板可配、入队、消费、channel=SUBSCRIBE、总开关 |
| P01 | 部门数据权限（订单/统计/司机/车辆/售后） | DONE | `logs/verify_remaining.py`（权限段） | 7 项通过：订单与统计按部门收敛，司机/车辆共享池（dept_id 为空）不收敛 |
| B23 | 业务统计页面 | DONE | `logs/verify_groups_stats.py` | 统计概览、KPI、站点概况接口通过 |
| B31 | 支付渠道核对页面 | DONE | `logs/verify_groups_stats.py` | 渠道核对页面权限与数据接口通过 |
| V64-1 | 第三方配置数据层（12 表 / 支付 9 列 / 退款 2 列 / 19 菜单 / 5 参数） | DONE | `sql/migration_20261023_01_...sql` 连续两次 + `verify_integration_config.py` S 段 | `tables=12 pay_cols=9 refund_cols=2 menus=19 configs=5=distinct`，两次一致 |
| V64-2 | 身份/授权域纯增量对齐（补 3 张 V6.4 空表） | DONE | `sql/migration_20261023_02_...sql` 连续两次 | `v64_tables_present=3 vi_total=82`（当时）；资料包 79 张表全部存在。V64-9 删掉 2 张退役表后为 80 张 |
| V64-3 | 第三方配置后端 35 端点 | DONE（未真实联调） | `mvn package` + 启动 + `verify_integration_config.py` R 段 | 打包 exit=0；35 个端点全部注册（不是静态资源兜底）；未插入真实凭据 |
| V64-4 | 后台 B46–B53 八页 + 两组件 | DONE（未浏览器实测） | `npx vite build` | `✓ 2782 modules transformed`，exit=0；页面交互未在浏览器点过 |
| V64-5 | 小程序支付方式选择与订阅授权 | DONE（未真机实测） | 代码审查 + 构建 | 支付方式按服务端开关渲染；订阅失败不阻断业务 |
| V64-6 | 配置生命周期红线 | DONE | `python logs/verify_integration_config.py` | 28 项：通过 27 / 失败 0 / 跳过 1（跳过的线下资金链路需人工） |
| V64-7 | 同步差异清单文档 | DONE | `docs/implementation/v6.4-sync-diff.md` | 逐项位置+原因；第一轮 5 项待确认已全部处置（见下 5 节） |
| V64-8 | 身份域 13 列补齐 + 历史行标记 | DONE | `sql/migration_20261023_04_...sql` 连续两次 | `account_new_cols=3 binding_new_cols=10 binding_checks=5 legacy_rows_marked=1`；历史绑定为 `MANUAL_LEGACY`，不编造 `phone_lookup` |
| V64-9 | 退役「员工申请 + 后台签发凭证」整条流程 | DONE | `sql/migration_20261023_05_...sql` + `verify_integration_config.py` R4/R5 + `check_admin_pages.py` | 2 张表 DROP、6 个端点下线、10 个 Mapper 方法与 3 个页面入口删除；`retired_binding_tables=0 vi_table_total=80` |
| V64-10 | 孤儿支付单清理 + 支付身份约束 | DONE | `sql/migration_20261023_06_...sql` 连续两次 | `orphan_payments=0 payment_total=20 payment_checks=3`；备份 `E:/codex/_backup/xinyi_payment_orphans_20261023.sql` |
| V64-11 | 业务事件自动下发（短信/订阅） | DONE（未真实送达） | `python logs/verify_notification_dispatch.py` | 10 项通过：4 个订单主链路事件触发投递、未配置时 `SKIPPED` 带原因码、去重生效、通知失败不影响业务；真实送达需凭据 |
| V64-12 | 首轮遗留缺陷修复 | DONE | `python logs/run_acceptance.py` | 修掉 6 个实测缺陷：所有短信被误判无手机号、密文查询抛 `java.lang.Byte` 越界、绑定手机号即 401、投递永远停在 `PENDING`、并发刷新失败方提示随事务时序抖动、无候选订单被反复扩圈；详见差异文档第 4 节 |
| V64-13 | 资料包 `ck_ver_*` 约束笔误修正 | DONE | `migration_20261023_04` 第三节 + `verify_integration_config.py` S6c | 上游写成 `CHECK (version >= 0)` 而该表无 `version` 列（只追加的不可变历史表）。不迁就上游加列，改为 `CHECK (revision_before >= 0 AND revision_after >= 0)`，约束名沿用；负数实测 `ERROR 3819` 被拒 |

## 已知环境约束

1. **本机没有 Docker**，`docker-compose.yml` 与 workflow 未在本机执行；等价校验用本机 MySQL 8.0 跑 `logs/ci_sql_check.sh` 完成（同一份脚本、同一组断言）。
2. **同库多实例**：本机 8080（IDEA devtools）与 18080（验收实例）连接同一个库，`vi_outbox` 是竞争消费。
   验收期间须保证两个实例运行**同一版本的代码**，否则旧实例会把新事件类型直接标 DONE 而不投递。
3. **迁移脚本必须幂等**：基础脚本 `ry_20260417.sql` 已包含 `vi_*` 主体结构（66 张），
   其余由迁移补齐，最终为 **80 张**（V6.4 的 79 张 + 本库保留的 `vi_sms_send_log`），
   因此迁移脚本在全新库上是"空跑"。任何不带 `IF NOT EXISTS` / 判存在逻辑的 DDL 都会让 CI 失败。

## 资料包 298 条验收规格的执行口径

`docs/acceptance/test_cases_V6.0.json` 是资料包 `06_验收/test_cases_V6.0.json` 的原样副本
（从提交 `44e1432` 取回，298 条；P0 231 / P1 67）。执行口径：

- **执行器不改写其 `status`**。该字段当前全部为 `NOT_RUN`，只有真正按 given/when/then 跑过才能改。
- 自动化断言（327 条）验证的是**实现不变量**（派单轮次、心跳、订阅、数据权限、接口契约、表结构、第三方配置与消息下发），
  与规格用例不是 1:1。少数用例存在**局部**自动化证据，例如：
  - `TC-R50-018`（培训进度不能快进伪造）→ `logs/verify_remaining.py` 的"播放位置跳变被拒""重复序号幂等""超时按阈值封顶"；
  - `TC-R50-012`（中文自由权限被拒绝）→ 同脚本的权限段（无权限账号访问 403）。
  即便如此，也不能据此把整条用例标为通过，仍需按 `then` 的完整表述人工复核。
- 结论：**298 条中当前 0 条完成人工执行**，台账只提供按模块的分布与自动化基线，
  不宣称任何一条业务用例通过。

