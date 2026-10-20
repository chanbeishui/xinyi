# E01 进度（契约修复 + 残留项修正）

状态：**VERIFIED**（账本已置；REVIEWED 待独立复核）。R1–R8 全部完成，三道静态检查 + 全套测试全绿（详见 verification.md）。
执行者：Qoder-Agent。venv：`~/.vi-vehicle-venv`（仓库外隔离，未污染系统/仓库）。

## 基线（修正前，全绿）
- check_engineering.py：34/34 PASS
- check_package.py：42/42 PASS（249 操作 / 278 Schema / 66 表）
- test_contract_regression.py：14 passed
- 存档：`baseline-regression.md`

## 已完成

### R1 未绑定枚举列补齐（15 列绑定，权威清单见 R1-unbound-enum-columns.md）
迁移脚本 `apply_r1_r2.py`（可复现），四处同步：
- 枚举字典：enums 67→79（+12），columnBindings 59→74（+15）
- data_model：15 列加 columnRules.enumRef + checks（可空列用 `IS NULL OR IN`）
- openapi：components.schemas 278→290（+12 具名枚举 Schema）
- 001 SQL：15 条 CHECK 约束（修正了首版缺逗号问题，全 66 表逗号结构校验 0 错）
- 绑定列：aftersale_case.kind/return_state/refund_state、aftersale_review.decision、dispatch_invite.source、driver_fee_ledger.entry_kind/currency、driver_fee_rule.currency、driver_statement.currency、statement_membership_change.action、event_log.actor_realm、evidence_group.mileage_source、inspection_result.conclusion/source、session.active_role
- 未硬约束（D 类字典/注册表引用 + E 类误报，共 6 列，有意不 CHECK）：dispute.type、service_exception.type、event_log.action、notification.role（疑为 SessionRole，待确认）、channel_event.event_key、dispute.conclusion

### R2 refund_state 语义裁决（已定）
**裁决：不同义，分开命名 + 映射，不取并集。**
- `vi_aftersale_case.refund_state` = 售后单退款**编排投影**，绑定既有 `RefundStatus`（9 值：NONE/NOT_REQUIRED/WAIT_RETURN/READY/PROCESSING/SUCCESS/UNKNOWN/ABNORMAL/CLOSED）。列注释由 8 值补为 9 值（含 CLOSED），消除该枚举此前的"孤儿"状态。
- `vi_refund_order.status` = 渠道**执行态**，保持 `ChannelRefundStatus`（6 值：CREATED/PROCESSING/SUCCESS/CLOSED/ABNORMAL/UNKNOWN）。
- 映射（编排态 ← 渠道态）：READY→(建退款单)CREATED；PROCESSING↔PROCESSING；SUCCESS←SUCCESS；ABNORMAL←ABNORMAL/UNKNOWN；CLOSED←CLOSED；NONE/NOT_REQUIRED/WAIT_RETURN 为渠道无关的业务编排态（无对应渠道单或尚未发起）。

### R8 枚举派生物同步（本批相关部分）
- `接口枚举清单.json` namedEnums 72→84（补 12 新枚举，已有条目与 openapi 全一致、无多余）
- `dto-types.ts` / `miniapp/src/api/types.ts`：未引用任一新枚举（这些值在 DTO 中目前为内联字符串联合，非 $ref 具名枚举），故无需改动；若后续令 DTO 字段 $ref 具名枚举，再同步生成
- task_packets：视图走 `$ref` 主契约，未因新增枚举失效

### 复验（修正后，全绿）
- check_package.py：42/42 PASS（Schema 290）
- check_engineering.py：34/34 PASS
- test_contract_regression.py：14 passed
- 备份：`backup_pre_r1/`（4 个原始契约文件，可回滚）

## R3–R7 已完成（详见 verification.md）
- R3 写白名单 66 表分三层：IMMUTABLE_HISTORY 7（updateColumns 置空）/ CONTROLLED_LIFECYCLE 51 / EDITABLE_DRAFT 8；session/token/account_security 收窄为受控更新列（角色切换/消费/撤销/轮换/代际），未一律 insert-only。
- R4 唯一权威 ViRecord = `14_工程模板/backend/.../core/ViRecord.java`（含 Long id）；T3.1 参考版已标注非权威；自然键实体单独映射。
- R5 `GET /api/public/service-config` 补录权限注册表（operations 248→249）+ 接口全量清单（计数与表行）。
- R6 认证文档新增『共享 common 端点授权』章节：handover/files/uploads/employee-binding 按调用方认证域选判据 + 能力/功能权限 + 数据范围 + 状态四要素全成立才放行；裁决 biz:* 与 x-capability 为同一能力两域登记、以调用方域为准。
- R7 术语统一 `x-authorization.scope`，x-data-scope 残留 0。
- R8 派生物同步：接口枚举清单 72→84；dto-types/样本/task_packets 核对无需改。

## 完成判据对照（E01 TASK.md）
- [x] 执行本包契约与报价回归，保存实际断言记录（verification.md，72 passed）
- [x] 当前静态契约检查不标真实 API 已实现（STATIC_DOCUMENT_CHECK）
- [x] R1–R8 修正 + 派生物同步 + 复验全绿
- [ ] 独立复核（REVIEWED 需复核人≠实现人）
