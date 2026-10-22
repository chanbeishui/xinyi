# S04 · 普通异常与争议处置

阶段：M4；责任域：support；前置：F06, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`8a30752a1f0884c9ca778788a682b7f128a0041ad442228d550391046991d9f8`。

## 本任务交付
- 普通异常、服务争议、人工接管与结论、暂停自动确认

## 必测失败与并发
- 不恢复事故理赔系统
- 通知失败不撤销已保存事实
- 保管责任始终明确

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_037_post, API_038_get, API_062_post, API_080_post, API_091_post, API_116_get, API_117_get, API_118_post, API_119_get, API_120_post
画板：O23, O23-progress, D24, S14, B15, B15-detail
主责任表：vi_dispute, vi_service_exception
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/support/`
`xinyi-rongli/src/main/resources/mapper/rongli/support/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/support/`
`xinyi-ui/src/views/rongli/support/`
`xinyi-ui/src/api/rongli/support.ts`
`xinyi-miniapp/src/features/support/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=S04SupportIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
