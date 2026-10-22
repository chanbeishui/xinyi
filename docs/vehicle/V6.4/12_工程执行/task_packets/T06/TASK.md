# T06 · 结构、Mapper生成列和升级一致性

阶段：M0；责任域：testing；前置：E03, E06。
范围版本：`V6.4-IDENTITY-20261022`；definitionHash：`d2f726ba641cefc427525718ce4e9f97460d4fb6ec2752e6d3eedec7ee859220`。

## 本任务交付
- 真实MySQL执行79表、主键/索引/CHECK/UTC/自增/生成列
- 初装与M0适配目标一致

## 必测失败与并发
- H2/SQLite不能替代此门禁
- SQL可解析不等于可执行

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/T06/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T06TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
