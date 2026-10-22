# F03 · 四次交接的主体核验

阶段：M3；责任域：handover；前置：F02, I06。
范围版本：`V6.1-UNCHANGED`；definitionHash：`99322aae001bfc7df5cbbf83fc3ae7793fa3b51e25186324efb9f6d91edee630`。

## 本任务交付
- 类型/保管方/持码方/核验方/验证码/错误计数/一次性交接

## 必测失败与并发
- 自己拿码自己验证拒绝
- 错码计数即使业务回滚也持久
- 同码不可跨单

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_068_post, API_069_get, API_070_get, API_071_post
画板：O35, O35-receipt, O35-return, D13, D13-code-error, D13-locked, D20, D20-cancel, D23, D23-cancel, S07, S07-code-error, S13, S13-cancel, S13-released
主责任表：vi_handover, vi_handover_attempt_guard
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/handover/`
`xinyi-rongli/src/main/resources/mapper/rongli/handover/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/handover/`
`xinyi-ui/src/views/rongli/handover/`
`xinyi-ui/src/api/rongli/handover.ts`
`xinyi-miniapp/src/features/handover/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=F03HandoverIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
