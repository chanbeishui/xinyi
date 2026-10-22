# I06 · 后台员工站点预配置与组合权限

阶段：M1；责任域：scope；前置：I04, I05。
范围版本：`V6.4-IDENTITY-CONSISTENCY-20261022`；definitionHash：`28b5bc61b5922e1ec6507eb96588f6f3b4ae96ddab1ecb67e8a25d892cc2485c`。

## 本任务交付
- 直接按sys_user配置站点，员工未微信登录也能保存
- grant_set集合版本含空集合；原角色功能与组织/部门/站点交集
- 后台新增/撤销授权刷新生效，旧会话实时拒绝已撤权操作

## 必测失败与并发
- 零站点可保存并撤权，不自动回行政站点
- 批量越权全拒绝；旧集合version冲突
- 未登录员工配置不要求accountId/openid

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：API_094_get, API_131_get, API_132_put, v62_get_station_grant
画板：S18, B25, B26, B26-revoke, B26-unlinked, B26-conflict
主责任表：vi_station_grant, vi_station_grant_set
验收：TC-R62-006, TC-R62-016, TC-R62-017, TC-R62-023, TC-R62-034, TC-R63-002, TC-R63-006, TC-R63-007, TC-R63-013

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/scope/`
`xinyi-rongli/src/main/resources/mapper/rongli/scope/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/scope/`
`xinyi-ui/src/views/rongli/scope/`
`xinyi-ui/src/api/rongli/scope.ts`
`xinyi-miniapp/src/features/scope/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I06ScopeIT verify
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
