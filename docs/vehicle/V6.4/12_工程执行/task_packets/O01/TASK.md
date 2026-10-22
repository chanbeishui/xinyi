# O01 · 工作台、统计与审计

阶段：M6；责任域：operations；前置：B04, N01, R06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`c09c5799fb525bc891bc0dfed89495af044c5ab5f2284ae981ea4bfbe4549be7`。

## 本任务交付
- 授权汇总、待处理列表、时延与金额口径、事件审计检索

## 必测失败与并发
- 先聚合后join防SUM重复
- 内部审计按主体且不记录敏感凭据

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_097_get, API_147_get, API_148_get
画板：B02, B22, B32, B32-request
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/operations/`
`xinyi-rongli/src/main/resources/mapper/rongli/operations/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/operations/`
`xinyi-ui/src/views/rongli/operations/`
`xinyi-ui/src/api/rongli/operations.ts`
`xinyi-miniapp/src/features/operations/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=O01OperationsIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
