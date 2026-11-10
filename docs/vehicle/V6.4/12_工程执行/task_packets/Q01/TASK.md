# Q01 · 代驾资格草稿与提交

阶段：M1；责任域：qualification；前置：I02, C01, F01。
范围版本：`V6.4-QUALIFICATION-DRAFT-20261107`；definitionHash：`efc3037c98c6f5c386374b2b6ee41e73ea5f1578570ee8b3df919412faf9c3c7`。

## 本任务交付
- 本人认证档案、证件归属、提交校验、驳回只补相关项
- 已验证OWNER可以申请，STATION不自动授DRIVER；拒绝/审核中不进入接单工作台
- 用户确认完整资料、真实核验和培训全部通过后才可提交；材料HMAC版本与证明绑定、不可变提交快照
- 本人证件真实私有上传及字段补正；复审草稿不覆盖已生效资格，公司由A04显式认领，不从客户端猜归属
- F49资格申请/补正/复审与员工F04分离；严格提交门槛与状态页面同源

## 必测失败与并发
- 员工和普通人同审核
- 申请接口不要求先具备正式代驾资格

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_042_get, API_043_put, v5_post_api_app_driver_qualification, API_044_post
画板：D02, D03, D05, D05-passed, D05-rejected, D28, D28-expired
主责任表：vi_driver_profile, vi_driver_application, vi_driver_application_submission
验收：TC-R62-003, TC-R62-004

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/qualification/`
`xinyi-rongli/src/main/resources/mapper/rongli/qualification/`
`xinyi-miniapp/src/features/qualification/`
`xinyi-rongli/src/main/java/com/xinyi/rongli/qualification/controller/`
`xinyi-miniapp/src/pages/qualification/index.vue`
`xinyi-admin/src/test/java/com/xinyi/web/viit/Q01QualificationIT.java`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q01QualificationIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
