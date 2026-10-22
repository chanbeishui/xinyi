# D06 · 未接车解除与原订单重派

阶段：M3；责任域：fulfillment；前置：D04, E06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`e8627703e9f2f6619e0db15891d149909a1a59f041f6bcb1b137a7860052cbfe`。

## 本任务交付
- 解除指定承接/释放精确占用/保留支付预约/重派
- 生成取消规则事实

## 必测失败与并发
- 已接车禁止自助丢车
- 原订单不取消
- 不重复记满额报酬

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_060_post
画板：D31, D32, D33, D34, D35
主责任表：无
验收：TC-090, TC-091, TC-092, TC-093, TC-094, TC-095, TC-096, TC-097, TC-098, TC-099, TC-100, TC-101, TC-102, TC-103, TC-104

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/fulfillment/`
`xinyi-rongli/src/main/resources/mapper/rongli/fulfillment/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/fulfillment/`
`xinyi-ui/src/views/rongli/fulfillment/`
`xinyi-ui/src/api/rongli/fulfillment.ts`
`xinyi-miniapp/src/features/fulfillment/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D06FulfillmentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
