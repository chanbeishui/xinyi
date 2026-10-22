# T04 · 对账核价和凭证集成

阶段：M5；责任域：testing；前置：R01, R02, R03, R04, R05, R06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`6953a27ca06056c5bc48dc7ce2cad70e441421ab449e8024e53f7f27e9f68749`。

## 本任务交付
- 跨月明确选择→改价/复核→本人确认→部分/足额凭证核实

## 必测失败与并发
- 重叠集合一胜一409
- 同期间不重叠均成功
- 同人自审和public hash

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T04/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T04TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
