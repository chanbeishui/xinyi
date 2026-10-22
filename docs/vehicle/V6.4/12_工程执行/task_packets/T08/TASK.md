# T08 · 全量前端实现与可用性验收

阶段：M6；责任域：testing；前置：T01, T02, T03, T04, N02, O01, E04, E05。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`ad14800db6de06f35ca43cb43a11d47b5a5f07348cb10c37ddc1c587952aeb8d`。

## 本任务交付
- 按355画板/有效主视图逐项验收路由、字段、状态、错误恢复、留存和键盘
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 不是每个画板单独路由
- H5不得假称微信真机
- 不得localStorage存订单当后端

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T08/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T08TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
