# A04 · 代驾员档案与接单控制

阶段：M1；责任域：driver；前置：I06, E05。
范围版本：`V6.1-UNCHANGED`；definitionHash：`a1db049bdd104f1f054627a4b5863095502d59ce3dd417e42079247ae6830a77`。

## 本任务交付
- 代驾列表/资格/承接记录/备注/暂停新邀约
- 个人对账入口

## 必测失败与并发
- 关闭新增承接不解除持车责任
- 自身费用不可自审

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_drivers, API_126_post, V4_get_api_admin_drivers__driverId, V4_patch_api_admin_drivers__driverId, V4_get_api_admin_drivers__driverId__assignments
画板：D27, S15, B28, B28-detail, B28-edit, B28-empty, B28-error, B28-forbidden
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/driver/`
`xinyi-rongli/src/main/resources/mapper/rongli/driver/`
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/driver/`
`xinyi-ui/src/views/rongli/driver/`
`xinyi-ui/src/api/rongli/driver.ts`
`xinyi-miniapp/src/features/driver/`

## 验证命令
```bash
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A04DriverIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

## 完成标准
- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。
