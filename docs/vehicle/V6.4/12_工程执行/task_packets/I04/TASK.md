# I04 · 角色与站点上下文切换

阶段：M1；责任域：identity；前置：I03。
范围版本：`V6.4-IDENTITY-CONSISTENCY-20261022`；definitionHash：`07b22a0fbb43670c5dacbdeb132b2ac45894a394bc592cdaf10f861715f6b372`。

## 本任务交付
- 只切换availableRoles；OWNER可申请代驾，未批准无DRIVER
- STATION需有效员工及站点/功能交集，首次仍OWNER
- 权限刷新不改变司机任务；撤权回安全视角

## 必测失败与并发
- 旧检测权限失效仍可切回OWNER
- 切换不释放assignment

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_006_post, API_007_post
画板：U01, U01-unqualified, S21, S22, U01-driver, U01-staff, U01-all
主责任表：无
验收：TC-R62-024, TC-R63-001, TC-R63-009, TC-R63-010, TC-R63-016

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/identity/`
`xinyi-rongli/src/main/resources/mapper/rongli/identity/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/identity/`
`xinyi-ui/src/views/rongli/identity/`
`xinyi-ui/src/api/rongli/identity.ts`
`xinyi-miniapp/src/features/identity/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I04IdentityIT verify
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
