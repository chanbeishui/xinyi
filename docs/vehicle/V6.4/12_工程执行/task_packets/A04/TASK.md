# A04 · 代驾员档案与接单控制

阶段：M1；责任域：driver；前置：I06, E05。
范围版本：`V6.4-DRIVER-COMPANY-20261107`；definitionHash：`b7ea4b6d6b46503ec44ba0baa8d7d57207618614771f56883b0f9dbd344cdff4`。

## 本任务交付
- 代驾列表/资格/承接记录/备注/暂停新邀约
- 本人工作页和服务记录入口；费用对账导航须R任务接通后启用，不虚构账单
- 同公司共用代驾池；显式归属及空闲迁移，存量不猜测公司；原新权限并集与迁移后禁新
- 暂停/恢复行政新承接开关，保留听单偏好、资格与在手责任；D01可达/定位最终判定及对账费用另待对应任务

## 必测失败与并发
- 关闭新增承接不解除持车责任
- 自身费用不可自审

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：V4_get_api_admin_drivers, API_126_post, V4_get_api_admin_drivers__driverId, V4_patch_api_admin_drivers__driverId, V4_get_api_admin_drivers__driverId__assignments, V64_post_api_admin_drivers__driverId__enable_new_assignments
画板：D27, S15, B28, B28-detail, B28-edit, B28-empty, B28-error, B28-forbidden
主责任表：无
验收：无

## 代码落位
`xinyi-rongli/src/main/java/com/xinyi/rongli/driver/`
`xinyi-rongli/src/main/resources/mapper/rongli/driver/`
`xinyi-ui/src/views/rongli/driver/`
`xinyi-ui/src/api/rongli/driver.ts`
`xinyi-rongli/src/main/java/com/xinyi/rongli/driver/controller/`
`xinyi-admin/src/test/java/com/xinyi/web/viit/A04DriverIT.java`
`xinyi-miniapp/src/pages/work/`

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
