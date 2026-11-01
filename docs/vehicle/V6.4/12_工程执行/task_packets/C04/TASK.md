# C04 · 客户计价规则和试算

阶段：M2；责任域：pricing；前置：C01, C02。
范围版本：`V6.4-COMPANY-PRICING-20261101`；definitionHash：`a7e3fa64ff43f20930b917be6d2068e8e5e3297b9b31f0a35a48fe15f86ef93e`。

## 本任务交付
- 同公司统一、多站点共用的独立TEST/LIVE计价版本与权威部门归属
- 草稿、独立审核、发布、停用、显式原子替换与不可变历史配置
- 保存版本和正式匹配试算共用整数计算器；比例计费HALF_UP至分；拒绝无匹配、多匹配和等待费未批准启用

## 必测失败与并发
- 多匹配不猜最便宜
- 未配置拒绝报价
- test配置与prod隔离
- 提交人/编辑参与人自审、摘要变化后发布拒绝
- 未知旧记录不猜测回填；跨公司/环境写入拒绝
- 同条件并发发布只能一个成功；显式替换失败整体回滚

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_133_get, API_134_post, API_135_put, API_136_post, API_137_post, API_138_post, V64_post_api_admin_prices_ruleId_trial, V64_post_api_admin_prices_trial, V64_post_api_admin_prices_ruleId_disable
画板：B20, B20-edit, B20-review
主责任表：vi_price_rule
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/pricing/`
`xinyi-rongli/src/main/resources/mapper/rongli/pricing/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/pricing/`
`xinyi-ui/src/views/rongli/pricing/`
`xinyi-ui/src/api/rongli/pricing.ts`
小程序报价入口由B02接入；本任务提供共享计价Service及生成契约类型，不伪造独立计价页。

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=C04PricingIT,C04LivePricingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
