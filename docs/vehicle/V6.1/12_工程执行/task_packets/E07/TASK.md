# E07 · 测试夹具与控制平面接入

阶段：M0；当前用户未实施M0。前置：E02, E03, E06, E04。

## 本任务交付

- 只在测试classpath建立fixtures/runtime/provider-event
- 真实会话授权和业务Service不mock
- 分步接入FixturePort：仅创建隔离测试的前置组织/用户/站点/配置；未开发服务可先建立接口和纯基础夹具，禁止生成任意支付/交接完成状态；随对应业务任务扩展，T01使用IDENTITY，T02使用BOOKING。

## 代码落位

`docs/implementation/vehicle/E07/`  

## 失败与边界

- 生产jar无__integration
- 控制密钥和DB guard双校验
- 禁止设任意订单目标状态

## 验证命令

```text
mvn -pl xinyi-admin -am -DskipTests package
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E07TestingIT verify
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。
