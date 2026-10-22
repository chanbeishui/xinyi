# F04 · 行驶证OCR和人工复核

阶段：M3；责任域：inspection；前置：F01, F03。
范围版本：`V6.1-UNCHANGED`；definitionHash：`fc521d74706ff596d9c96dafb1b50bb50a52c2f75b45796a94a7617df9e07014`。

## 本任务交付
- 行驶证文件、字段置信度、人工复核
- 后台与小程序共用入口

## 必测失败与并发
- 低置信度标示
- 原图需单独授权
- 通知/OCR失败不伪造已核验

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_074_get, API_075_put, API_085_get, API_086_put
画板：S08, S08-manual, B07, B07-confirm, B08
主责任表：vi_vehicle_license
验收：TC-145, TC-146, TC-147

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/inspection/`
`xinyi-rongli/src/main/resources/mapper/rongli/inspection/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/inspection/`
`xinyi-ui/src/views/rongli/inspection/`
`xinyi-ui/src/api/rongli/inspection.ts`
`xinyi-miniapp/src/features/inspection/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F04InspectionIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
