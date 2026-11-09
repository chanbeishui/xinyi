# Q04 · 本机生物认证、双人人工核验与可信结果

阶段：M1；责任域：verification；前置：Q01。
范围版本：`V6.4-SOTER-MANUAL-20261109`；definitionHash：`096e6f3e9ff5e015a39ef9fda2c94998c71836033fd7f739b98f14f9fec09ac4`。

## 本任务交付
- 微信SOTER本机随机挑战、原签名微信服务端验真、同实际MINI登录会话可信结果；不接腾讯慧眼，不要求实名活体
- 背景/驾驶证双人人工确认，不要求凭证上传或标准发布；同公司权限、申请人防自审、终态不可改
- 11接口/主契约/页面/模型/增量与完整SQL/任务切片同源，Q01当前四项可信提交

## 必测失败与并发
- 客户端passed、伪造raw/签名/OpenID、重复JSON键、跨实际session或旧材料/公司epoch拒绝
- 微信未知仅恢复首次冻结原文，GET零网络；验签IO期间撤会话/改材料不得生成可信证明
- 两位ADMIN经办复核分离，申请人不能自办自审；复核人可参与最终资格审核
- 空公司范围失败关闭，原公司读归属、当前材料写复核，UUID/CAS/审计outbox失败整体回滚
- 真机SOTER与真实微信API另在L02/L01验证，不用local替身声称完成

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_045_post, q04_get_api_admin_manual_verifications, q04_get_api_admin_manual_verifications_applications, q04_get_api_admin_manual_verifications_id, q04_get_api_admin_verification_sessions, q04_get_api_admin_verification_sessions_id, q04_post_api_admin_manual_verifications, q04_post_api_admin_manual_verifications_id_review, q04_post_api_admin_verification_sessions_id_close, q04_post_api_app_driver_qualification_face_sessions_verificationSessionId_confirm, v5_get_api_app_driver_qualification_face_sessions_verificationSessionId
画板：D04, D46
主责任表：vi_provider_verification, vi_face_session, vi_manual_verification
验收：TC-R50-021

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/verification`
`xinyi-rongli/src/main/resources/mapper/rongli/verification`
`xinyi-miniapp/src/pages/verification/index.vue`
`xinyi-miniapp/src/features/verification`
`xinyi-ui/src/views/rongli/verification`
`xinyi-ui/src/api/rongli/verification.ts`

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
