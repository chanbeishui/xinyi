# S01 · 统一售后申请和进度

阶段：M2；责任域：aftersale；前置：B04, P02。
范围版本：`V6.1-UNCHANGED`；definitionHash：`1239370e0a0a0e8c0ffaf87b6c8751a9987be3f9110470dec1f746eacfda9ebd`。

## 本任务交付
- 车主取消并退款/仅退款一次申请、草稿理由金额凭证、重复入口

## 必测失败与并发
- 已支付未接车也人工审核
- 退款申请不直接REFUNDING

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_post_api_app_owner_orders__orderNo__after_sales, V4_get_api_app_owner_orders__orderNo__after_sales, V4_get_api_app_owner_after_sales, V4_get_api_app_owner_after_sales__caseId, V4_post_api_app_owner_after_sales__caseId__notes
画板：O19, O19-duplicate, O19-picked, O19-refund, O21, O21-abnormal, O21-return, O21-success
主责任表：vi_aftersale_case
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/aftersale/`
`xinyi-rongli/src/main/resources/mapper/rongli/aftersale/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/aftersale/`
`xinyi-ui/src/views/rongli/aftersale/`
`xinyi-ui/src/api/rongli/aftersale.ts`
`xinyi-miniapp/src/features/aftersale/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S01AftersaleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
