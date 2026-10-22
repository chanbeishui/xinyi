# E06 · 幂等、审计、outbox与任务基座

阶段：M0；责任域：platform；前置：E03, E04。
范围版本：`V6.4-IDENTITY-20261022`；definitionHash：`4121f3c5b3314d9c1b90d6b0b1073e0797109f3e9d542c24a5955b4ca74b8089`。

## 本任务交付
- 同事务业务写与outbox
- 有序锁/唯一约束/请求幂等
- 审计双主体

## 必测失败与并发
- 登录刷新不缓存明文
- 重复键不同体409
- 失败不写成功日志

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：vi_event_log, vi_outbox, vi_idempotency
验收：无

## 代码落位
`docs/implementation/vehicle/E06/`

## 验证命令
```bash
mvn -pl xinyi-admin -am test
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E06PlatformIT verify
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
