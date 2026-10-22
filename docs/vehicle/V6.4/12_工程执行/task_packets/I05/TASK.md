# I05 · 手机号自动关联员工与后台异常管理

阶段：M1；责任域：identity；前置：I02, I03。
范围版本：`V6.4-IDENTITY-CONSISTENCY-20261022`；definitionHash：`dc343dd4c0ce51809df061b045e2372ee95de2d15b65e3ad3abff68e7ea7e8cf`。

## 本任务交付
- 唯一有效sys_user手机号自动关联；无申请/凭证
- 框架目录保护行与人员创建/导入/手机号/状态变更钩子
- 后台查询、暂停、解除、允许本人重新核验；不直接覆盖不同关联
- 解绑阻止自动复绑、换号安全失效、审计与权限版本同事务

## 必测失败与并发
- 重号按distinct人员计数，无名单泄露
- 并发手机号变更/绑定唯一性保护
- 未验证号/SMS/假openid不绑定
- 既有关系不可抢占；主动解绑下一次不恢复
- 后台账号匹配不签ADMIN Token

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：v62_get_employee_link, v62_post_employee_link_reconcile, v62_get_employee_links, v62_get_employee_link_detail, v62_suspend_employee_link, v62_revoke_employee_link, v62_allow_employee_rematch
画板：U02, B36, U02-revoked, U02-unconfigured, U02-conflict, U02-linked, B36-suspend, B36-revoke, B36-rematch, U02-suspended, U02-verifying, U02-unknown, B36-unlinked
主责任表：vi_employee_binding, vi_employee_directory_guard
验收：TC-R50-016, TC-R62-007, TC-R62-009, TC-R62-010, TC-R62-011, TC-R62-012, TC-R62-013, TC-R62-015, TC-R62-019, TC-R62-020, TC-R62-022, TC-R62-026, TC-R62-027, TC-R62-028, TC-R62-032, TC-R62-033, TC-R62-036, TC-R63-003, TC-R63-004, TC-R63-005, TC-R63-011, TC-R63-015

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/identity/`
`xinyi-rongli/src/main/resources/mapper/rongli/identity/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/identity/`
`xinyi-ui/src/views/rongli/identity/`
`xinyi-ui/src/api/rongli/identity.ts`
`xinyi-miniapp/src/features/identity/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I05IdentityIT verify
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
