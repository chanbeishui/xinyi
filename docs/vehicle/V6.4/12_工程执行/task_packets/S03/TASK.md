# S03 · 返还后退款执行与差错

阶段：M4；责任域：refund；前置：P04, F06。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`53dcffc4f612a14b6451f00ef72015d920a9eec26e45d6261a50f96c42cb7076`。

## 本任务交付
- 无需第二次审核，返还后按批准决定退
- 查单、重试和异常台账
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- UNKNOWN保留预留
- 相同退款号重试
- 不超实付余额、不自动扣司机费用

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/S03/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S03RefundIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
