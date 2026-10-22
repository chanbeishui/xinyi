# O02 · 后台任务控制和故障恢复

阶段：M6；责任域：jobs；前置：P03, N01, N03, S03。
范围版本：`V6.1-UNCHANGED`；definitionHash：`9e3b7ed03c4207e24bad8ac63920837c06e760ff3d3451ba70f90a2031f84923`。

## 本任务交付
- 失败任务队列、受控重试、告警、租约恢复

## 必测失败与并发
- 重试仍走统一Service权限
- 不能后台任意改终态或清空审计

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_151_get, API_152_post
画板：无
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/jobs/`
`xinyi-rongli/src/main/resources/mapper/rongli/jobs/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/jobs/`
`xinyi-ui/src/views/rongli/jobs/`
`xinyi-ui/src/api/rongli/jobs.ts`
`xinyi-miniapp/src/features/jobs/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=O02JobsIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
