# E01 · R1 未绑定枚举列权威清单与处置

来源：程序化解析 `05_数据设计/001_vi业务表_初始化_V6.0.sql`（66 表）× `枚举字典_V6.0.json`（59 绑定 / 67 枚举）。
方法：枚举样式列 = VARCHAR/CHAR 且注释含 `A/B` 取值或列名含 status/state/type/kind/realm/role/decision/action/source/conclusion/scope/currency；排除已有 `CHECK(... IN ...)` 或已在 columnBindings 的列。
结果：枚举样式列 53，已 CHECK/已绑定 32，**未绑定且无 CHECK = 21 列**。

## A 类：真·闭合枚举 → 必须补 CHECK + 字典绑定（11 列）

| 表.列 | 取值 | 可空性 | 处置 |
|---|---|---|---|
| vi_aftersale_case.kind | CANCEL_REFUND/REFUND_ONLY | NOT NULL | 绑定 AfterSaleKind + CHECK |
| vi_aftersale_case.return_state | UNDETERMINED/NOT_REQUIRED/WAIT_RETURN/RETURNED | NOT NULL | 绑定 ReturnState + CHECK |
| vi_aftersale_review.decision | APPROVE/REJECT | NOT NULL | 绑定 ReviewDecision + CHECK |
| vi_dispatch_invite.source | AUTO/MANUAL | NOT NULL | 绑定 InviteSource + CHECK |
| vi_driver_fee_ledger.entry_kind | NORMAL_RETURN/CANCEL_RETURN/CANCEL_BEFORE_PICKUP/ADJUSTMENT/INITIAL_VALUATION/PRICE_DELTA | NOT NULL | 绑定 FeeEntryKind + CHECK |
| vi_driver_statement_membership_change.action | CREATE/ADD/REMOVE/REACTIVATE/VOID | NOT NULL | 绑定 MembershipAction + CHECK |
| vi_event_log.actor_realm | MINI/ADMIN/SYSTEM/CALLBACK | NOT NULL | 绑定 ActorRealm + CHECK |
| vi_evidence_group.mileage_source | MANUAL/OCR_REVIEWED | 可空 | 绑定 MileageSource + CHECK(允许NULL) |
| vi_inspection_result.conclusion | PASS/FAIL | NOT NULL | 绑定 InspectionConclusion + CHECK（核心结果字段，最高优先） |
| vi_inspection_result.source | STATION/ADMIN | NOT NULL | 绑定 ResultSource + CHECK |
| vi_session.active_role | OWNER/DRIVER/STATION | NOT NULL | 绑定 SessionRole + CHECK（注：非授权来源，仅视角） |

## B 类：币种 CHAR(3) → 绑定 Currency 枚举或 CHECK 白名单（3 列）

| 表.列 | 处置 |
|---|---|
| vi_driver_fee_ledger.currency | 首期固定 CNY；绑定 Currency 枚举(CNY) + CHECK，预留扩展 |
| vi_driver_fee_rule.currency | 同上 |
| vi_driver_statement.currency | 同上 |

## C 类：R2 语义裁决后再绑定（1 列）

| 表.列 | 问题 | 处置 |
|---|---|---|
| vi_aftersale_case.refund_state | 注释 8 值(NONE/NOT_REQUIRED/WAIT_RETURN/READY/PROCESSING/SUCCESS/UNKNOWN/ABNORMAL)，字典 RefundStatus 9 值含 CLOSED；与 vi_refund_order.status(ChannelRefundStatus) 概念重叠 | **R2 裁决**：判定 refund_state(售后单上的退款投影) 与 vi_refund_order.status(渠道退款执行状态) 是否同义。倾向【不同义】：refund_state 是售后视角的退款编排状态(含 WAIT_RETURN/NOT_REQUIRED 等业务态)，ChannelRefundStatus 是渠道执行态(CREATED/PROCESSING/SUCCESS/CLOSED/ABNORMAL/UNKNOWN)。若不同义→分开命名(refund_state 用 AfterSaleRefundState 枚举)并写明 refund_state↔channel status 映射表，不取并集；CLOSED 归渠道态，不进 refund_state。 |

## D 类：半开放/字典引用 → 不用 CHECK，文档化为字典或注册表引用（4 列）

| 表.列 | 性质 | 处置 |
|---|---|---|
| vi_dispute.type | 普通服务争议类型（业务可扩展） | sys_dict 引用，不 CHECK |
| vi_service_exception.type | 失联/拒收/资料/上传等（可扩展） | sys_dict 引用，不 CHECK |
| vi_event_log.action | 事件/动作名（随业务增长） | 事件注册表引用，不 CHECK |
| vi_notification.role | 业务视角（疑为 OWNER/DRIVER/STATION，注释未明示） | 澄清：若确为三视角→归 A 类绑定 SessionRole；若为通知目标角色集→文档化。默认按 OWNER/DRIVER/STATION 绑定，待确认 |

## E 类：误报（非枚举，剔除）（2 列）

| 表.列 | 说明 |
|---|---|
| vi_channel_event.event_key | 渠道事件唯一标识（自由文本 ID），非枚举。唯一性已由 uk_event 保证 |
| vi_dispute.conclusion | VARCHAR(2000) 处理结论自由文本，非枚举 |

## R1 执行结论

- 需新增 CHECK + 字典绑定：A 类 11 + B 类 3 + C 类 1（R2 裁决后）+ D 类 vi_notification.role 1 = **最多 16 列**。
- 需文档化为字典/注册表引用（不 CHECK）：D 类 3 列。
- 剔除误报：E 类 2 列。
- 落地：① `枚举字典_V6.0.json` 补 AfterSaleKind/ReturnState/ReviewDecision/InviteSource/FeeEntryKind/MembershipAction/ActorRealm/MileageSource/InspectionConclusion/ResultSource/SessionRole/Currency 等枚举定义与 columnBindings；② `001_..._V6.0.sql` 对应表补 `CONSTRAINT ck_xx CHECK (col IN (...))`；③ `data_model_V6.0.json` 同步；④ 可空列 CHECK 用 `(col IS NULL OR col IN (...))`；⑤ 改完复跑 `engineer.py check` + `test_contract_regression.py` + `verify_mysql.py`（E03 真实库）。
- 与 R2 联动：refund_state 先裁决再绑定。
- 与 R8 联动：以上改动须同步 task_packets 片段/类型/样本/测试，不只追加说明。
