# I01 · 统一微信登录与默认车主

阶段：M1；责任域：identity；前置：E06, E07。
范围版本：`V6.4-IDENTITY-CONSISTENCY-20261022`；definitionHash：`8f5384482995ab7da94bc62c4dc6dfac6e494cd7a0bd435c82fc8da8db8ef2f5`。

## 本任务交付
- 可信AppID/OpenID创建或恢复唯一账号；新会话OWNER
- 不接受客户端角色；恢复老账号真实资格，不创建重复sys_user
- 返回accountVersion、资格、公开员工关联状态

## 必测失败与并发
- 同openid并发只一账号
- 不按role授高权
- accountId=1不成为超管

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_001_post, API_005_get
画板：O29, D01, S01, B01, O29-staff, O29-driver, O29-unconfigured, O29-duplicate, O29-conflict, O29-denied, O29-processing, O29-error, S01-ready, S01-unconfigured, D01-draft, D01-pending, D01-rejected, D01-approved
主责任表：vi_account, vi_account_security
验收：TC-001, TC-002, TC-003, TC-004, TC-005, TC-006, TC-007, TC-008, TC-009, TC-010, TC-011, TC-012, TC-013, TC-014, TC-015, TC-016, TC-017, TC-018, TC-R50-007, TC-R50-008, TC-R50-012, TC-R50-032, TC-R62-001, TC-R62-002, TC-R62-018, TC-R62-035, TC-R63-014

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/identity/`
`xinyi-rongli/src/main/resources/mapper/rongli/identity/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/identity/`
`xinyi-ui/src/views/rongli/identity/`
`xinyi-ui/src/api/rongli/identity.ts`
`xinyi-miniapp/src/features/identity/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I01IdentityIT verify
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
