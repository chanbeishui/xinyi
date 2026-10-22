# R06 · 付款凭证登记与独立核实

阶段：M5；责任域：externalpayment；前置：R04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`acfe7d38b25e4740fb9d3f3c2128d88950502cd8a8bf310d87a497a585961f60`。

## 本任务交付
- 账单确认后关联真实付款凭证、流水、部分付款和0元无需付款

## 必测失败与并发
- 待核实暂占余额但不计已付
- 同主体流水唯一
- 不能按行随意分摊

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_driver_payment_records, V4_post_api_admin_driver_payment_records, V4_post_api_admin_driver_payment_records__recordId__verify, V42_get_api_admin_driver_payment_records__recordId, V42_get_api_app_driver_payment_records
画板：B40, B40-record, B40-verify, B40-detail
主责任表：vi_driver_external_payment
验收：TC-183, TC-184, TC-185, TC-186, TC-187, TC-188, TC-189, TC-R42-021, TC-R42-022, TC-R42-025, TC-R42-026, TC-R42-027, TC-R42-028, TC-R42-029, TC-R42-030, TC-R42-031, TC-R42-032, TC-R42-033, TC-R42-034, TC-R42-036, TC-R42-037, TC-R42-038

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/externalpayment/`
`xinyi-rongli/src/main/resources/mapper/rongli/externalpayment/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/externalpayment/`
`xinyi-ui/src/views/rongli/externalpayment/`
`xinyi-ui/src/api/rongli/externalpayment.ts`
`xinyi-miniapp/src/features/externalpayment/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R06ExternalpaymentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
