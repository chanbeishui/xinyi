# P03 · 计划派单和时间驱动任务

阶段：M2；责任域：jobs；前置：P02, C03, E06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`76bee9ed8e68821b5572e1aeee89fc61ed243df5e96c6cf34fc195e79f39918f`。

## 本任务交付
- 到dispatchAt才派，deadline任务扫描领取租约和补偿

## 必测失败与并发
- 重启不丢任务
- 多实例不重派
- Clock可注入，不能sleep24h测试

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/P03/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=P03JobsIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
