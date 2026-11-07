# P02 · 支付可信通知、查单与关单

阶段：M2；责任域：payment；前置：P01, E06, X02。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`e30d70d555020b44ea14a8e7b1bd4f5c52dcf3b586c6bb9cf434f37154ab8cff`。

## 本任务交付
- 原始body验签/解密/金额商户一致、查单补偿、30分钟可信关单
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 回调与关单竞争
- 未知不误报未扣款
- 回调防重放与永久去重

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_030_get, API_031_post, API_165_post, API_166_post
画板：无
主责任表：vi_channel_event
验收：TC-037, TC-038, TC-039, TC-040, TC-041, TC-042, TC-043, TC-044, TC-045, TC-046, TC-047, TC-048, TC-049, TC-050

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/payment/`
`xinyi-rongli/src/main/resources/mapper/rongli/payment/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/payment/controller/`
`xinyi-miniapp/src/features/payment/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/P02PaymentIT.java`
`sql/migration_20261104_03_payment_reconciliation.sql`
`xinyi-miniapp/src/pages/payment/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/PaymentHttpFixture.java`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=P02PaymentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
