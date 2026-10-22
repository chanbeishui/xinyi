# R02 · 个人汇总与明确勾选组单

阶段：M5；责任域：statement；前置：R01, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`24cf053f7cda0d3a22c1cddd779496d59a217448b61c05ab16b14fdc2ff938bf`。

## 本任务交付
- 未结清/全部账单/付款三标签，跨页选择，按selectedEntries建草稿

## 必测失败与并发
- 同人主体币种，任一选择冲突整体回滚
- 日期非成员
- 已有单未付仍未结清

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_post_api_admin_driver_statements_preview, V4_post_api_admin_driver_statements, V4_get_api_admin_driver_statements, V4_get_api_admin_driver_statements__statementId, V4_post_api_admin_driver_statements__statementId__void, V42_get_api_admin_drivers__driverId__reconciliation_overview, V42_get_api_admin_drivers__driverId__reconciliation_entries, V42_get_api_app_driver_reconciliation_entries
画板：B37, B42, B42-select, B42-selected, B42-generate, B42-conflict, B42-bills, B42-bills-partial, B42-payments, B42-empty, B42-forbidden
主责任表：vi_driver_statement, vi_driver_statement_line, vi_driver_ledger_allocation
验收：TC-R42-001, TC-R42-002, TC-R42-003, TC-R42-004, TC-R42-005, TC-R42-006, TC-R42-007, TC-R42-008, TC-R42-009, TC-R42-010, TC-R42-011, TC-R42-012, TC-R42-013, TC-R42-014, TC-R42-015, TC-R42-016, TC-R42-017, TC-R42-018, TC-R42-019, TC-R42-020, TC-R42-023, TC-R42-024, TC-R42-035, TC-R42-039, TC-R42-040, TC-R50-029, TC-R50-030

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/statement/`
`xinyi-rongli/src/main/resources/mapper/rongli/statement/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/statement/`
`xinyi-ui/src/views/rongli/statement/`
`xinyi-ui/src/api/rongli/statement.ts`
`xinyi-miniapp/src/features/statement/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R02StatementIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
