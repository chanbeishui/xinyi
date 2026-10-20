# E01 验证记录（契约修复 + R1–R8 残留项修正）

执行时间：2026-10-21（UTC）· venv：`~/.vi-vehicle-venv`（Python 3.11.15）
性质：STATIC_DOCUMENT_CHECK / 契约回归。未实现真实 API、未连 MySQL、未构建后端。

## 修正后复验（全绿）

| 检查 | 命令 | 结果 |
|---|---|---|
| 深度静态一致性 | `11_工具/check_package.py` | **42 pass / 0 fail**（Schema 290、表 66、操作 249） |
| 工程一致性 | `12_工程执行/engineer.py check` | **34 / 34 PASS** |
| 契约回归 | `pytest tests/test_contract_regression.py` | **14 passed** |
| 全套工具/契约/框架启动/模拟器 | `pytest tests/ 11_工具/` | **72 passed** |

## R1–R8 落地核对

- R1：15 列绑定枚举（字典 enums 67→79、columnBindings 59→74；data_model +enumRef+CHECK；openapi Schema 278→290；SQL +15 CHECK）。剩 6 列有意不硬约束（D 类字典/注册表引用 + E 类误报）。
- R2：`vi_aftersale_case.refund_state`→`RefundStatus`(9值,含CLOSED) 与 `vi_refund_order.status`→`ChannelRefundStatus`(6值) **分开命名 + 映射，不取并集**；列注释 8→9 值。
- R3：写白名单 66 表分三层 = IMMUTABLE_HISTORY 7（updateColumns 置空）/ CONTROLLED_LIFECYCLE 51 / EDITABLE_DRAFT 8；`vi_session` updateColumns 收窄为 [status, context_version, active_role, station_id, revoked_at, linked_sys_user_id]，`vi_session_token`=[consumed_at, revoked_at, replaced_by_token_id]，`vi_account_security`=[session_epoch, permission_version, security_status, changed_at]。
- R4：`10_技术架构_T3.1/参考代码/ViRecord.java` 顶部标注非权威，唯一权威 = `14_工程模板/backend/.../core/ViRecord.java`（含 Long id）；自然键实体单独映射。
- R5：`GET /api/public/service-config` 补录权限注册表（operations 248→249，authorization realm=PUBLIC/scope=PUBLIC_WHITELIST）+ 接口全量清单（计数 248→249 + 表行）。
- R6：认证文档新增『共享 common 端点授权』章节——handover/files/uploads/employee-binding 按调用方认证域选判据（MINI 用 x-capability、ADMIN 用 biz:*），叠加能力/功能权限 + 数据范围 + 状态 + 认证域**四要素全部成立才放行**，不以任一条件满足放行；明确 biz:* 与 x-capability 是同一能力在两域的登记、以调用方域为准。
- R7：契约术语统一为 `x-authorization.scope`，`x-data-scope` 残留 0。
- R8：派生物同步——接口枚举清单 namedEnums 72→84；dto-types.ts/types.ts 未引用新枚举（值为内联字符串联合，无需改）；订单四视角样本不含受影响字段；task_packets 视图走 $ref 主契约未失效。

## 官方 E01 交付（TASK.md）

- 报价四组合（新旧车 × 新旧址）XOR / 同意协议约束：由 `test_contract_regression.py` 覆盖（QuoteRequest vehicleId|vehicleDraft、addressId|addressDraft 二选一，缺组/双传/null/ID溢出/伪造金额站点拒绝），14 passed。
- 静态契约检查不标真实 API 已实现（check_package kind=STATIC_DOCUMENT_CHECK）。

## 迁移脚本（可复现）

- `apply_r1_r2.py`：R1+R2（枚举字典/data_model/openapi/SQL 四处同步）
- `apply_r3_to_r7.py`：R3–R7
- 备份：`backup_pre_r1/`（4 契约文件原始态）、`backup_pre_r3_r7/`（5 文件 R3 前态）
- 过程中修正了首版 SQL CHECK 缺逗号语法错误（全 66 表逗号结构校验 0 错）。

## 结论

E01（契约修复 + R1–R8）实现完成，三道静态检查 + 全套测试全绿。REVIEWED 需独立复核人（≠实现者），留待用户/独立复核。真实 API/MySQL/构建证据属后续 M1+ 与 E02/E03。
