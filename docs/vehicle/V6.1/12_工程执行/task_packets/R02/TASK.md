# R02 · 个人汇总与明确勾选组单

阶段：M5；主责任域：statement；前置：R01, I06。

## 本任务交付

- 未结清/全部账单/付款三标签，跨页选择，按selectedEntries建草稿

## 必测失败与并发

- 同人主体币种，任一选择冲突整体回滚
- 日期非成员
- 已有单未付仍未结清

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_post_api_admin_driver_statements_preview, V4_post_api_admin_driver_statements, V4_get_api_admin_driver_statements, V4_get_api_admin_driver_statements__statementId, V4_post_api_admin_driver_statements__statementId__void, V42_get_api_admin_drivers__driverId__reconciliation_overview, V42_get_api_admin_drivers__driverId__reconciliation_entries, V42_get_api_app_driver_reconciliation_entries

画板：B37, B42, B42-select, B42-selected, B42-generate, B42-conflict, B42-bills, B42-bills-partial, B42-payments, B42-empty, B42-forbidden

建模对象：vi_driver_statement, vi_driver_statement_line, vi_driver_ledger_allocation

源验收：TC-R42-001, TC-R42-002, TC-R42-003, TC-R42-004, TC-R42-005, TC-R42-006, TC-R42-007, TC-R42-008, TC-R42-009, TC-R42-010, TC-R42-011, TC-R42-012, TC-R42-013, TC-R42-014, TC-R42-015, TC-R42-016, TC-R42-017, TC-R42-018, TC-R42-019, TC-R42-020, TC-R42-023, TC-R42-024, TC-R42-035, TC-R42-039, TC-R42-040, TC-R50-029, TC-R50-030

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/statement/`  
`xinyi-rongli/src/main/resources/mapper/rongli/statement/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/statement/`  
`xinyi-ui/src/views/rongli/statement/`  
`xinyi-ui/src/api/rongli/statement.ts`  
`xinyi-miniapp/src/features/statement/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R02StatementIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
