# T02 · 预约支付售后真实集成

阶段：M2；责任域：testing；前置：B01, B02, B03, B04, P01, P02, P04, S01, S02。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`591fca91d56c0aabb3151990825695d3da85a8f281718279995bee0b76ef6bb5`。

## 本任务交付
- 四组合首单报价→占额建单→测试渠道回调→查询→未接车审核取消退款
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 同键异体、支付关单竞争、通知晚到、未配置费用错误

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T02/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T02TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
