# N03 · 导出、敏感访问和治理

阶段：M4；责任域：governance；前置：I06, F01, E06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`421db6f465c2360566ab72faf45dd27848b9efa4a713f43963132e3f9f4831a4`。

## 本任务交付
- 异步导出申请审批下载、证据访问授权、治理工单字段规则

## 必测失败与并发
- 导出时与下载时双鉴权
- CSV公式注入防护
- 无权限不泄数量

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_144_post, API_145_post, API_146_get
画板：无
主责任表：vi_governance_case
验收：TC-155, TC-156, TC-157, TC-158, TC-159, TC-160, TC-161, TC-190, TC-191, TC-192, TC-195

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/governance/`
`xinyi-rongli/src/main/resources/mapper/rongli/governance/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/governance/`
`xinyi-ui/src/views/rongli/governance/`
`xinyi-ui/src/api/rongli/governance.ts`
`xinyi-miniapp/src/features/governance/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=N03GovernanceIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
