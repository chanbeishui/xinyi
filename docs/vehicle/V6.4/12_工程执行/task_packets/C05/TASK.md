# C05 · 司机独立费用规则

阶段：M5；责任域：driverfee；前置：C01, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`3f56a5618bd98c541a1804cda6695fc5e08ea3242c24ad2c6524d4d741630ae3`。

## 本任务交付
- 固定/约定路线/取消返还/解除场景，草稿试算独立审核发布

## 必测失败与并发
- 客户收费不等于司机报酬
- 未匹配NULL不当0
- 不改历史

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_driver_fee_rules, V4_post_api_admin_driver_fee_rules, V4_get_api_admin_driver_fee_rules__ruleId, V4_put_api_admin_driver_fee_rules__ruleId, V4_post_api_admin_driver_fee_rules__ruleId__publish, V4_post_api_admin_driver_fee_rules_trial
画板：B39, B39-edit, B39-review
主责任表：vi_driver_fee_rule
验收：TC-169, TC-170

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/driverfee/`
`xinyi-rongli/src/main/resources/mapper/rongli/driverfee/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/driverfee/`
`xinyi-ui/src/views/rongli/driverfee/`
`xinyi-ui/src/api/rongli/driverfee.ts`
`xinyi-miniapp/src/features/driverfee/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C05DriverfeeIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
