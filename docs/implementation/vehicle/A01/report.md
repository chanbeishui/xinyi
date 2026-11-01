# A01 · 车辆档案与PC车辆列表 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.1-UNCHANGED。
接口：MINI API_013_get/014_post/015_put/016_delete/017_post；ADMIN V4_get_api_admin_vehicles、V4_get_..._vehicleId、V4_patch_...、V4_get_..._orders、V4_get_..._inspection_results。主责任表：vi_vehicle。

## 交付（对照 TASK.md）
- **本人车辆 CRUD/常用/软删**：`VehicleService`；归属本人（伪造 owner/猜车牌越权 404 不泄露）；同车主同车牌活跃唯一（冲突 409，不泄露其他车主）；软删释放车牌可重建；生成列 active_plate 不入实体/DTO/不写；CAS；历史订单快照不追改。
- **PC 详情/维护/关联记录**：`VehicleAdminService` 列表/详情（VIN/手机号脱敏、ownerRelation 投影、allowedActions 服务端计算）/维护 PATCH（部分更新保留原值不置空 NOT NULL、CAS 409 不静默覆盖、仅改本行不追改快照）/关联订单/关联检测结果（只读）。
- **并发设常用单默认**：账号行锁+清旧默认（排除目标）。
- **前端**：小程序 features/vehicle/api.ts + pages/vehicle/list.vue（O04/O05/O27）；PC B09 vehicle.vue + vehicle.ts + 常量路由。两端构建通过。

## 真实集成验证（A01VehicleIT 7/7，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A01VehicleIT verify` → exit 0，7 用例 0 失败 0 错误 0 跳过。
覆盖：CRUD+版本+生成列不入DTO、同车主车牌冲突409+软删重建、伪造owner/猜车牌越权404+不同车主同车牌允许、设常用并发单默认、CAS409、ADMIN列表/详情脱敏/维护CAS/关联订单/结果、MINI不获ADMIN。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10、A02 7、A01 7 = 98，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0；`npm --prefix xinyi-ui run build:prod` exit 0。

## Findings / 遗留
- **维护 PATCH 部分更新**：未提供字段保留原值（修复初版置空 NOT NULL 列导致 500）；null DATE 参数补 jdbcType=DATE。
- **车型标签 vehicleTypeLabel**：暂 null，字典富化属 C01。
- **PC B09 常量路由**；非超管需 sys_menu 授予 biz:vehicle:*/biz:result:list（服务端强制），菜单基数据属部署/权限任务。

## 完成判据对照
- [x] 源接口/页面/表有明确实现文件与关联测试（O04/O05/O27/B09 变体；A01 无验收用例，负向以 IT 覆盖）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（A01VehicleIT 7/7）
- [x] 标注测试第三方替身（SIMULATOR），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）