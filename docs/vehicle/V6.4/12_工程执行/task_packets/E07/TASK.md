# E07 · 测试夹具与控制平面接入

阶段：M0；责任域：testing；前置：E02, E03, E06, E04。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`34ce9a9f3848da5541fde6b2baa090712e4762e1fbedb728c8f185e832ca23fb`。

## 本任务交付
- 只在测试classpath建立fixtures/runtime/provider-event
- 真实会话授权和业务Service不mock
- 分步接入FixturePort：仅创建隔离测试的前置组织/用户/站点/配置；未开发服务可先建立接口和纯基础夹具，禁止生成任意支付/交接完成状态；随对应业务任务扩展，T01使用IDENTITY，T02使用BOOKING。
- 增加自动关联fixture：未登录员工预授权、重号、停用、冲突、解绑阻止、换号竞态
- 身份阶段先跑独立身份测试，不以缺支付商户锁住基础实现
- INTEGRATION_CONFIG夹具及6.4.0运行时版本；真实认证路径签发隔离管理员令牌，禁止伪装超管或预造资金成功

## 必测失败与并发
- 生产jar无__integration
- 控制密钥和DB guard双校验
- 禁止设任意订单目标状态

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/E07/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -DskipTests package
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E07TestingIT verify
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
