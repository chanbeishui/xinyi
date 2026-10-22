# N02 · 隐私、公开内容和设置

阶段：M4；责任域：privacy；前置：C01, I01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`332c5cb7c45953354a4b56cd5edcf0b6ded59288d9594e0d0283b10afe952b03`。

## 本任务交付
- 公开企业条款、版本授权、显示偏好、隐私请求进度

## 必测失败与并发
- 旧保险钱包文案不回流
- 不物理删资金交接证据
- 权限撤回不抹历史

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_153_get, API_154_post, API_163_post, API_164_get
画板：U03, U03-progress, U05, U06, O09, O24, O28, D26, D30, S20, B34
主责任表：无
验收：TC-193, TC-194

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/privacy/`
`xinyi-rongli/src/main/resources/mapper/rongli/privacy/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/privacy/`
`xinyi-ui/src/views/rongli/privacy/`
`xinyi-ui/src/api/rongli/privacy.ts`
`xinyi-miniapp/src/features/privacy/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=N02PrivacyIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
