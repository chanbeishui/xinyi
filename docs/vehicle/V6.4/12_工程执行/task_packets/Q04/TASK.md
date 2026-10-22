# Q04 · 实名活体与核验适配

阶段：M1；责任域：verification；前置：Q01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`224a140022a98539496da67c116a8888fd84cab386176a6d7c7c1e65e3870f81`。

## 本任务交付
- 业务face session、白名单启动参数、可信结果查询、供应商适配

## 必测失败与并发
- 客户端passed拒绝
- 模拟结果仅测试环境
- 未开通不阻其他代码开发

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_045_post, v5_get_api_app_driver_qualification_face_sessions_verificationSessionId
画板：D04, D46
主责任表：vi_provider_verification, vi_face_session
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/verification/`
`xinyi-rongli/src/main/resources/mapper/rongli/verification/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/verification/`
`xinyi-ui/src/views/rongli/verification/`
`xinyi-ui/src/api/rongli/verification.ts`
`xinyi-miniapp/src/features/verification/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=Q04VerificationIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
