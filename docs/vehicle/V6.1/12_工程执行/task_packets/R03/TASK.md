# R03 · 草稿成员、行内核价与整单复核

阶段：M5；主责任域：statement；前置：R02。

## 本任务交付

- 添加/移出留痕、最终拟价、改价原因、独立复核、撤回

## 必测失败与并发

- 60→70→65非累加
- 集合变化也使旧hash失效
- 同人不能切角色复核自己

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：V41_put_api_admin_driver_statements_statementId_lines_lineId_price, V41_get_api_admin_driver_statements_statementId_lines_lineId_price_history, V41_post_api_admin_driver_statements_statementId_submit_review, V41_post_api_admin_driver_statements_statementId_review, V41_get_api_admin_driver_statements_statementId_reviews, V41_post_api_admin_driver_statements_statementId_withdraw, V42_post_api_admin_driver_statements__statementId__lines_add, V42_post_api_admin_driver_statements__statementId__lines__lineId__remove, V42_get_api_admin_driver_statements__statementId__membership_history

画板：B38, B38-confirmed, B38-difference, B38-empty, B38-generate, B38-sent, B38-unpriced, B38-price, B38-price-empty, B38-price-conflict, B38-review, B38-self-review, B38-review-rejected, B38-history, B38-withdraw, B38-adjust, B38-readonly, B38-forbidden, B38-add, B38-partial, B38-paid, B38-zero

建模对象：vi_driver_price_change, vi_driver_statement_review, vi_driver_statement_membership_change

源验收：TC-R41-001, TC-R41-002, TC-R41-003, TC-R41-004, TC-R41-005, TC-R41-006, TC-R41-007, TC-R41-008, TC-R41-009, TC-R41-010, TC-R41-011, TC-R41-012, TC-R41-013, TC-R41-014, TC-R41-015, TC-R41-016, TC-R41-017, TC-R41-018, TC-R41-019, TC-R41-020, TC-R41-021, TC-R41-022, TC-R41-023, TC-R41-024, TC-R41-025, TC-R41-026, TC-R41-027, TC-R41-028, TC-R41-029, TC-R41-030

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
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R03StatementIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。
