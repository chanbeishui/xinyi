# D05 · 出发、到达及任务视图

阶段：M3；责任域：fulfillment；前置：D04, B04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`4c7baf42f104b49e0454eee51bde20abd4edfffd9835c14f5760d37b4ae216f3`。

## 本任务交付
- 按强制顺序出发/到接车/到站
- 导航打点、联系令牌、服务历史

## 必测失败与并发
- 不传targetStatus
- 上门时间与预计到站不同
- 司机结束后通道失效

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_057_post, API_058_post, API_059_post, v5_post_api_common_orders_orderNo_contact_access
画板：D10, D11, D14, D16, D21
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/fulfillment/`
`xinyi-rongli/src/main/resources/mapper/rongli/fulfillment/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/fulfillment/`
`xinyi-ui/src/views/rongli/fulfillment/`
`xinyi-ui/src/api/rongli/fulfillment.ts`
`xinyi-miniapp/src/features/fulfillment/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D05FulfillmentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
