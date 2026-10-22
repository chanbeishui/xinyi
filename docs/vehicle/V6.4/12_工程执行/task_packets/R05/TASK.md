# R05 · 已确认账单调差

阶段：M5；责任域：adjustment；前置：R04。
范围版本：`V6.1-UNCHANGED`；definitionHash：`d465d3cafc0141595fc785f3a04095fb17919013af90b2974cd927ffe4307377`。

## 本任务交付
- 原行申请差额、审批后新台账/后续账单，原记录不可覆盖

## 必测失败与并发
- 客户退款不自动改价
- 负净额不自动追收
- 重复确认不重结转

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_driver_fee_adjustments, V4_post_api_admin_driver_fee_adjustments, V4_post_api_admin_driver_fee_adjustments__adjustmentId__audit
画板：B41, B41-create
主责任表：vi_driver_fee_adjustment
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/adjustment/`
`xinyi-rongli/src/main/resources/mapper/rongli/adjustment/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/adjustment/`
`xinyi-ui/src/views/rongli/adjustment/`
`xinyi-ui/src/api/rongli/adjustment.ts`
`xinyi-miniapp/src/features/adjustment/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=R05AdjustmentIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
