# P04 · 未接车退款执行与渠道基础

阶段：M2；责任域：refund；前置：S02, P02。
范围版本：`V6.4-REFUND-EXECUTION-20261107`；definitionHash：`a9cb334b417b9fec3bf1cec70155bdfd533dbfe22ccacf1758a68bf8c66994a5`。

## 本任务交付
- 审核批准后退款单与渠道调用，复用原退款号查询及重试
- 为真实付款准备取消出口
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路
- 真实官方SDK签验、404未受理证据校验、租约恢复与原号受控重试；共用资金归并不释放车辆承接
- 持久支付/退款对账及可信终态办结；线下本地查询不冒充已退，默认关闭自动执行

## 必测失败与并发
- 未知结果不重复扣款
- 允许退额=实付减已退减预留，拒绝超额

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_113_get, API_114_post, API_115_post, API_141_get, API_142_post, API_143_post
画板：B31, B31-query
主责任表：vi_refund_order, vi_payment_reconciliation
验收：TC-118, TC-119, TC-120, TC-121, TC-123, TC-124, TC-128, TC-153, TC-154

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/refund/`
`xinyi-rongli/src/main/resources/mapper/rongli/refund/`
`xinyi-ui/src/views/rongli/refund/`
`xinyi-ui/src/api/rongli/refund.ts`
`xinyi-miniapp/src/features/refund/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/refund/controller/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/P04RefundIT.java`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=P04RefundIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
