# A03 · 车主档案管理 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.1-UNCHANGED。
接口（ADMIN）：V4_get_api_admin_owners（列表）、V4_get_api_admin_owners__ownerId（详情）、V4_patch_api_admin_owners__ownerId（维护）、V4_get_api_admin_owners__ownerId__vehicles（关联车辆）、V4_get_api_admin_owners__ownerId__orders（关联订单）。
画板：B27/B27-detail/B27-edit/B27-empty/B27-error/B27-forbidden。主责任表：vi_owner_profile（1:1 扩展 vi_account）。验收用例：无（负向以 IT 覆盖）。

## 交付（对照 TASK.md）
- **车主列表/详情**：`OwnerAdminService`；列表 rows/total 信封（pageNum/pageSize/keyword/status）；一行=车主运营档案（vi_owner_profile JOIN vi_account）；手机号脱敏（PhoneCrypto.mask）、phoneVerified 来自认证服务核验时刻（vi_account.phone_verified_at），不由运营字段覆盖；车辆数/订单数/最近服务时间投影。
- **部门数据范围（跨公司隔离）**：经 `IDeptScopeService.resolve` 解析；可见性与关联订单按订单权威 `vi_order.dept_id` 过滤——**跨公司历史不因一条订单暴露**；全量(null)放行、空集合 fail-closed（无订单车主对受限管理员不可见，统一 404 不泄露存在）。
- **维护（PATCH）**：仅运营字段 serviceName/serviceFlag/internalRemark；**手机号/核验结果/微信标识不可由后台直接改**（未知字段 400 拒绝）；serviceFlag 白名单 NORMAL/VERIFY_AGAIN（**标记不封禁下单**，仅二次提醒）；CAS 版本 409 不静默覆盖；部分更新保留原值；审计+outbox 同事务；强制 Idempotency-Key。
- **关联车辆/订单**：车辆 rows/total（VIN 脱敏）；订单 rows/total 为 AdminOrderSummary 最小投影（view/orderNo/fulfillmentStatus/displayStatus/cancelStatus/custody/vehicle 快照/station/slot/returnedAt/version/allowedActions/paymentStatus），不覆盖历史订单快照。
- **前端**：PC B27 owner.vue（搜索+分页列表+详情弹窗[车辆/订单]+维护弹窗[原因必填]+CAS/幂等）+ owner.ts + 常量路由；构建通过。A03 全为 ADMIN 接口、无 MINI 画板，小程序 features/owner 无对应面（不虚构空模块）。

## 真实集成验证（A03OwnerIT 7/7，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=A03OwnerIT verify` → exit 0，7 用例 0 失败 0 错误 0 跳过。
覆盖：
1. 列表/详情脱敏与档案投影（phoneMask 含 ****、phoneVerified、不泄完整手机号/密文、车辆数/订单数/标记/最近服务、internalRemark、version）；
2. 维护仅运营字段+CAS（改名/标记/备注成功 version+1；phoneVerified/phone 字段 400 拒绝；旧版本 409；核验状态不因维护改变）；
3. **跨公司历史不因一条订单暴露**（自建 sys_dept 990001/990002/990003 + 受限管理员 dataScope=本部门：A 公司只见 A 订单、B 只见 B、C 无订单详情 404、超管全量见两条；订单投影 view/vehicle.plateNo/station/slot Z 结尾）；
4. 无订单车主受限不可见 fail-closed（受限 404+列表不含；超管可见）；
5. 关联车辆投影与 rows/total 分页信封（VIN 脱敏 ****1234）；
6. MINI 令牌不获 ADMIN（框架链拒绝，非成功列表）；
7. 非超管有数据范围但无 biz:owner:list 功能权限 → 403。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10、A02 7、A01 7、C01 6、A03 7 = 111，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-ui run build:prod` exit 0（owner chunk 产出）；`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0。

## Findings / 遗留
- **selectOwnerById 共享片段 deptFilter**：详情/维护/关联订单复用 ownerCols（含按 deptIds 的订单计数子查询），初版未传 deptIds 触发 OGNL 绑定异常 500；已为 selectOwnerById 增加 deptIds 参数并在服务层透传（allData=null，空集合先行 fail-closed 不触达 foreach）。
- **phoneVerified 来源**：vi_account 无 phone_verified 列，核验时刻为 migration_20261022 增列 phone_verified_at；IT 经 PhoneCrypto 加密写密文+核验时刻模拟认证服务结果，运营维护不可覆盖。
- **AdminOrderSummary 投影**：A03 按契约最小投影直读 vi_order/vi_station/vehicle_snapshot/vi_payment_order；vi_station 无 service_phone 列→servicePhone=null，publicAddress=address，location.coordinateSystem=GCJ02。完整订单域读写（状态流转/动作鉴权）属 B01/B02 下单与履约任务。
- **菜单基数据**：非超管需 sys_menu 授予 biz:owner:list/query/edit（服务端强制）；常量路由仅超管可见，菜单/权限基数据属部署/权限任务。

## 完成判据对照
- [x] 源接口/页面/表有明确实现文件与关联测试（5 ADMIN 接口 + B27 六态 + vi_owner_profile；A03 无验收用例，负向以 IT 覆盖）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（A03OwnerIT 7/7 + 全量 111/111）
- [x] 标注测试第三方替身（SIMULATOR），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）