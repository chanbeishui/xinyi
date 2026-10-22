# P01 · 支付适配器与预支付

阶段：M2；责任域：payment；前置：B03, E07, X01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`671dab6604be5bce96eb2ca5f200d2965a084ff729488d3acbca8c5de46bc305`。

## 本任务交付
- 微信APIv3 SDK适配+测试网关，支付单商户快照和预支付状态
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 本地Mock不能上生产
- 客户端成功不修改paid
- 超期不重置截止

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_029_post
画板：O10, O11, O12, O13, O13-closed
主责任表：vi_payment_order
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/payment/`
`xinyi-rongli/src/main/resources/mapper/rongli/payment/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/payment/`
`xinyi-ui/src/views/rongli/payment/`
`xinyi-ui/src/api/rongli/payment.ts`
`xinyi-miniapp/src/features/payment/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=P01PaymentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
