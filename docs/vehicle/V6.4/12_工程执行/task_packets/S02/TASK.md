# S02 · 一次审核及退款预留

阶段：M2；责任域：aftersale；前置：S01, I06。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`6a697f99a82cb1afde32222416c500d90f89fda9a1879f761ac78f920b91728e`。

## 本任务交付
- 当前保管事实+可退额预览、批准/驳回
- 已接车WAIT_RETURN
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 取消批准不释放持车司机
- 仅退款不改履约
- 审核并发只一次

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_post_api_admin_orders__orderNo__after_sales, V4_get_api_admin_orders__orderNo__after_sales, V4_get_api_admin_after_sales, V4_get_api_admin_after_sales__caseId, V4_post_api_admin_after_sales__caseId__notes, V4_post_api_admin_after_sales__caseId__review_preview, V4_post_api_admin_after_sales__caseId__review, V4_get_api_admin_owners__ownerId__after_sales
画板：B13, B13-return, B14, B14-before, B14-complete, B14-conflict, B14-processing, B14-refund, B14-rejected, B14-return, B14-unknown
主责任表：vi_aftersale_review
验收：TC-163, TC-164, TC-165, TC-166, TC-167, TC-168, TC-171, TC-172, TC-173, TC-174, TC-175, TC-176, TC-177

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/aftersale/`
`xinyi-rongli/src/main/resources/mapper/rongli/aftersale/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/aftersale/`
`xinyi-ui/src/views/rongli/aftersale/`
`xinyi-ui/src/api/rongli/aftersale.ts`
`xinyi-miniapp/src/features/aftersale/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S02AftersaleIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
