# I02 · 手机号验证回执与自动关联触发

阶段：M1；责任域：identity；前置：I01。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`c59fe293c8b449f69baaecbddbd3d5b2a8c9cdd4c5361d902577548a2de06207`。

## 本任务交付
- 分开wx.login code与phoneCode
- 服务端手机号验证、专用verificationRequestId回执和结果查询
- SMS不新增员工关联；换号暂停旧工作能力
- 验证成功与员工匹配结果分开；不因重号取消OWNER
- V6.4同步范围：第三方配置/支付method/外部路由/消息模板以主PRD23和X01–X07为准；旧静态配置不能形成旁路

## 必测失败与并发
- 同code跨账号/同requestId异体拒绝；处理中不重复消费
- 外部未知查回执或重新授权，不假成功
- 预约联系人电话不成为身份依据

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_002_post, API_003_post, API_004_post, API_012_patch, v62_get_phone_verification
画板：O02, O25, O25-pending, O25-approved
主责任表：vi_phone_verification_receipt
验收：TC-R62-008, TC-R62-014, TC-R62-021, TC-R62-025, TC-R62-029, TC-R62-030, TC-R62-031, TC-R63-012

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/identity/`
`xinyi-rongli/src/main/resources/mapper/rongli/identity/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/identity/`
`xinyi-ui/src/views/rongli/identity/`
`xinyi-ui/src/api/rongli/identity.ts`
`xinyi-miniapp/src/features/identity/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I02IdentityIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过
- 本版主PRD/逐页规格/当前UI实图一致；不得复用已退役员工申请页面

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
