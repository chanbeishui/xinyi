# I06 · 后台员工站点预配置与组合权限 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-IDENTITY-CONSISTENCY-20261022。
接口：API_094_get（MINI 获授权站点）、API_131_get（ADMIN 列表）、API_132_put（ADMIN 修改跨站授权）、v62_get_station_grant（ADMIN 按 sys_user 查询，可未登录微信）。主责任表：vi_station_grant、vi_station_grant_set。

## 交付（对照 TASK.md / V6.4 §5）
- **按 sys_user 配置，不要求 accountId/openid**：`StationGrantService` 直接以 sys_user 为对象；员工未微信登录也能 GET 空集合（version=0、wechatLinkStatus=NOT_LOGGED_IN_OR_UNLINKED）并 PUT 建立关系，不创建伪造微信身份。
- **grant_set 集合版本含空集合（防 ABA）**：vi_station_grant_set.version 为 CAS 版本；删空不重置（空集合版本持续存在）；旧 expectedVersion 保存 409 不覆盖。
- **原子同事务**：PUT 在 GLOBAL_DIRECTORY 锁内更新集合+子授权增减（enabled 标记保留历史）+框架 auth_version+已关联 MINI permission_version+审计+outbox 同事务；后台新增/撤销刷新生效，旧会话实时拒绝已撤权操作（IdentityContextService 实时读启用授权）。
- **组合权限/数据范围**：员工覆盖全部任职（checkUserFullAccess）；站点 dept 与角色数据范围交集，批量越权/不存在/停用站点整体拒绝（无部分写入）；站点不授予功能码。
- **零站点可保存撤权，不自动回行政站点**：清空后 STATION 失权、车主保留；切已撤权站点 403。
- **前端**：PC B26 stationGrant.vue + scope.ts + 常量路由；小程序 S18 stations.vue + features/scope/api.ts；两端构建通过。

## 真实集成验证（I06ScopeIT 10/10，真实 HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I06ScopeIT verify` → exit 0，10 用例 0 失败 0 错误 0 跳过。ADMIN 令牌为 TokenService 测试夹具。
覆盖：未登录先授权不建微信身份+首登读到(016/R63-006)、先关联后补站点刷新可用(017)、可STATION不可DRIVER(006)、空集合版本/CAS冲突(034/R63-013)、清空失权车主保留(R63-007)、越权站点切换拒绝(023)、配置对象不串员工(R63-002)、不存在/停用站点批量拒绝、MINI 获授权站点投影(API_094)、列表分页+微信状态(API_131)。

全量回归（同次）：E06 10、E07 5、E08 1、I01 9、I02 9、I03 10、I04 10、I05 20、I06 10 = 84，0 失败 0 错误 0 跳过；平台单元 16/16；BUILD SUCCESS。
构建：`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0；`npm --prefix xinyi-ui run build:prod` exit 0。

## Findings / 遗留
- **auth_version 受控写**：I06 集合变更与 sys_user.auth_version 同事务递增（V6.4 §5 控制路径，伴随审计/outbox）；不写 sys_user_dept/sys_user_role。
- **PC B26 常量路由**（rongli scope 视图）；非超管需 sys_menu 授予 biz:stationgrant:* 方可调用 API（服务端强制），菜单基数据登记属部署/权限任务。
- **B25/S18 视觉变体**：B26-revoke/unlinked/conflict 由同一视图按 wechatLinkStatus/空集合/409 渲染；S18 小程序列表。
- **真实微信渠道属 L01**；SIMULATOR 替身不声称真实渠道通过。

## 完成判据对照
- [x] 源接口/页面/表/用例有明确实现文件与关联测试（S18/B26 + 变体；9 验收映射 10 测试）
- [x] 真实集成与负向/并发场景实际通过并保存原始报告（I06ScopeIT 10/10）
- [x] 标注测试第三方替身与延后项，不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现人）