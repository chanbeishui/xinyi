# N01 · 站内消息、订阅与重试

阶段：M4；责任域：notification；前置：E06, I04。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`ed37d5efa627684fb26c769a044f7c807b8b93f793c5011e741496d8a12fcc65`。

## 本任务交付
- 按角色/站点的待办和未读、订阅适配、通知模板与任务退避
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 用户拒绝订阅不阻业务
- 推送提示只做恢复查询不当事实

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_149_get, API_150_put, API_161_get, API_162_post
画板：U04, S19, B23, B35
主责任表：vi_notification
验收：TC-127, TC-128, TC-129, TC-130, TC-131, TC-132, TC-133, TC-134, TC-135, TC-136, TC-137, TC-138, TC-139, TC-140, TC-141, TC-142

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/notification/`
`xinyi-rongli/src/main/resources/mapper/rongli/notification/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/notification/`
`xinyi-ui/src/views/rongli/notification/`
`xinyi-ui/src/api/rongli/notification.ts`
`xinyi-miniapp/src/features/notification/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=N01NotificationIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
