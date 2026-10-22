# T01 · 身份与资料真实集成切片

阶段：M1；责任域：testing；前置：I01, I02, I03, I04, I05, I06, A01, A02, A03, A04, E04, E05。
范围版本：`V6.4-IDENTITY-20261022`；definitionHash：`111ab15e01259157ce47633ac8823c567d88c6e16fe7ebbb7c8b607df46cb64b`。

## 本任务交付
- 真实HTTP→Service→MyBatis→MySQL，退出重登/重启后持久化，PC受限查看

## 必测失败与并发
- 不能用MockMvc代替HTTP全链，不能只测原型
- 两个用户交叉越权

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T01/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T01TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
