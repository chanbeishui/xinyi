# R04 · 发布快照与本人确认差异

阶段：M5；责任域：statement；前置：R03, I04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`86b3eddf7f5646828f630dbd3af8bc64ca780e6b736d1b6ef3098af69c09a9ba`。

## 本任务交付
- 公开快照hash、SENT本人确认、差异说明、版本重发

## 必测失败与并发
- MINI不得内部投影
- 撤回与确认互斥
- 确认后工作行只读

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_app_driver_statements, V4_get_api_app_driver_statements__statementId, V4_post_api_admin_driver_statements__statementId__send, V4_post_api_app_driver_statements__statementId__confirm, V4_post_api_app_driver_statements__statementId__differences, V4_get_api_admin_statement_differences, V4_post_api_admin_statement_differences__differenceId__resolve, V41_get_api_admin_driver_statements_statementId_revisions_revision
画板：D37, D38, D38-confirmed, D38-difference, D37-services, D37-payments, D38-partial
主责任表：vi_driver_statement_difference, vi_driver_statement_snapshot
验收：TC-178, TC-179, TC-180, TC-181, TC-182

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/statement/`
`xinyi-rongli/src/main/resources/mapper/rongli/statement/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/statement/`
`xinyi-ui/src/views/rongli/statement/`
`xinyi-ui/src/api/rongli/statement.ts`
`xinyi-miniapp/src/features/statement/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R04StatementIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
