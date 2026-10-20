# R04 · 发布快照与本人确认差异

阶段：M5；主责任域：statement；前置：R03, I04。

## 本任务交付

- 公开快照hash、SENT本人确认、差异说明、版本重发

## 必测失败与并发

- MINI不得内部投影
- 撤回与确认互斥
- 确认后工作行只读

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V4_get_api_app_driver_statements, V4_get_api_app_driver_statements__statementId, V4_post_api_admin_driver_statements__statementId__send, V4_post_api_app_driver_statements__statementId__confirm, V4_post_api_app_driver_statements__statementId__differences, V4_get_api_admin_statement_differences, V4_post_api_admin_statement_differences__differenceId__resolve, V41_get_api_admin_driver_statements_statementId_revisions_revision

画板：D37, D38, D38-confirmed, D38-difference, D37-services, D37-payments, D38-partial

建模对象：vi_driver_statement_difference, vi_driver_statement_snapshot

源验收：TC-178, TC-179, TC-180, TC-181, TC-182

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
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R04StatementIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
