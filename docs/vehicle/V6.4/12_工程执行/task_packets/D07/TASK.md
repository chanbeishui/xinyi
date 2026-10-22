# D07 · 处罚及申诉查询审核

阶段：M4；责任域：penalty；前置：D06, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`610d7675a966a7da96faabd068cd640b56b66d4acca4b8a415ae4b92c70396a0`。

## 本任务交付
- 本人处罚列表详情、申诉提交进度、后台审批，公开意见投影

## 必测失败与并发
- 不能任意创建处罚
- 不恢复事故7/30天旧规则
- 不丢penaltyId

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_061_post, API_127_post, v5_get_api_app_driver_penalties, v5_get_api_app_driver_penalties_penaltyId, v5_get_api_app_driver_penalty_appeals, v5_get_api_app_driver_penalty_appeals_appealId, v5_get_api_admin_penalties, v5_get_api_admin_penalties_penaltyId, v5_get_api_admin_penalty_appeals, v5_get_api_admin_penalty_appeals_appealId
画板：D43, D44, D45, B45
主责任表：vi_penalty, vi_penalty_appeal
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/penalty/`
`xinyi-rongli/src/main/resources/mapper/rongli/penalty/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/penalty/`
`xinyi-ui/src/views/rongli/penalty/`
`xinyi-ui/src/api/rongli/penalty.ts`
`xinyi-miniapp/src/features/penalty/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D07PenaltyIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
