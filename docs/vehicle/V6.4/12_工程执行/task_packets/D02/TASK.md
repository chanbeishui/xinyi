# D02 · 自动派单、单人邀约与接受事务

阶段：M3；责任域：dispatch；前置：D01, P03。
范围版本：`V6.4-DISPATCH-INVITES-20261112`；definitionHash：`1ec34daa6ce139e45b068b9dd0c8f129f88b42fb62ecc9609fa8b9c5964694bc`。

## 本任务交付
- 订单独立冻结服务公司与策略；公司共享游标、一次扩圈、单人邀约、绝对截止及人工兜底
- 数据库司机/订单唯一占用与接受原子事务；旧事件精确holder保护
- 公司策略与撤回接口；本人API051–054及D07/D08/D07-list随D04共同交付，后台API098–101/B05/B06为D03已实现子范围；关联切片D03/D04，不整体宣称计划预览应用已完成

## 必测失败与并发
- 司机/订单不能双占
- 候选为空不死循环
- 超时撤旧邀约

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：d02_get_company_dispatch_policy, d02_put_company_dispatch_policy, d02_post_revoke_invite
画板：B24-dispatch-company
主责任表：vi_dispatch_invite, vi_driver_occupancy, vi_dispatch_cursor, vi_dispatch_policy, vi_dispatch_batch
验收：TC-051, TC-052, TC-053, TC-055, TC-056, TC-057, TC-058, TC-059, TC-060, TC-061, TC-062, TC-063, TC-064, TC-066, TC-017, TC-065, TC-D02-001, TC-D02-002, TC-D02-003, TC-D02-004, TC-D02-005

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/dispatch/`
`xinyi-rongli/src/main/resources/mapper/rongli/dispatch/`
`xinyi-ui/src/views/rongli/dispatch/`
`xinyi-miniapp/src/features/dispatch/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=D02DispatchIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
