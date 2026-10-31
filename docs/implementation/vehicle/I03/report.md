# I03 · 刷新、退出和多设备会话 — 实现报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。范围版本：V6.4-IDENTITY-CONSISTENCY-20261022。
接口：V41_post_api_common_token_refresh（PUBLIC，realm REFRESH）、V41_get_api_common_session（MINI，SESSION_READ）、V41_post_api_common_session_logout（MINI，SESSION_LOGOUT）。主责任表：vi_session、vi_session_token（并读 vi_account/vi_account_security，写 vi_event_log/vi_outbox）。

## 交付（对照 TASK.md / V6.4 ADR-01）
- **单次刷新轮换**：`MiniSessionService.refresh` 按 REFRESH 摘要匹配（不接受后台 JWT/ACCESS），`SELECT ... FOR UPDATE` 锁定令牌行→原子消费（CAS `consumed_at IS NULL AND revoked_at IS NULL`）→`rotateSession` 签发新一代 ACCESS+REFRESH（generation 递增、parent_token_id 链接父令牌）。刷新令牌绝对截止随会话（不无限滑动）。
- **重用撤会话族（事务正确性）**：检测到已消费刷新令牌再次出现→撤销会话全部令牌（`revokeBySession`）+ 撤销会话（`revokeSession`）+ 独立事务拒绝审计（`recordFailure` REQUIRES_NEW）+ outbox `SESSION_FAMILY_REVOKED`，**事务正常提交后**再向客户端抛 `VI_REFRESH_REUSED`。修复了"事务内抛异常会回滚撤销事实"的缺陷：以 `RefreshResult(reused,response)` 结果模式分离撤销提交与对外报错。
- **绝对到期/撤销/代际校验**：会话非 ACTIVE/已撤销/绝对截止已过→`VI_AUTH_REQUIRED`（重新微信登录）；`session_epoch` 与账号安全代际不符（全局退出/封禁）→拒绝；账号非 ACTIVE→`VI_FORBIDDEN`。权限版本变化不直接拒绝基础 REFRESH（仍核账号/会话 epoch 与撤销）；旧 STATION 失权时返回视角回退 OWNER（activeRole 持久化切换属 I04）。
- **当前会话只读视图**：`GET /api/common/session` 返回 `SessionView`（无令牌），availableRoles/资格/员工关联由 `IdentityContextService.compute` 按权威状态实时重算；员工工作权失效仍可返回基础状态与可切换视角。
- **退出当前/全部设备**：`logout(allDevices=false)` 撤当前会话+令牌（scope CURRENT）；`allDevices=true` 递增 `vi_account_security.session_epoch`（scope ALL），所有旧会话因代际不符即时失效。**只清会话，不取消订单、不解除持车任务、不撤销资格**（TC-R63-008）。退出需 Idempotency-Key（≥16，业务动作幂等，不缓存凭据）；严格拒绝未知字段。
- **数据库权威撤权（不依赖 Redis）**：MINI 放行仅读 vi_session_token/vi_session/vi_account_security（`resolveAccess` 无任何 Redis 调用），撤销即时生效、无缓存宽限；Redis 故障不可能导致越权放行。
- **安全链**：`MiniSecurityConfig` 精确放行 `/api/common/login/wechat` 与 `/api/common/token/refresh`（PUBLIC），其余 /api/** 一律 authenticated；refresh 严格只接受 {refreshToken}（≥32），混入其他字段 400。
- **前端**：小程序 features/identity/api.ts 补 refreshSession/getSession/logout（http.ts 已内置 401 自动刷新）；SessionView 类型对齐 V6.4（employeeLink/accountVersion）；PC identity.ts 补 LogoutResult。本任务无新页面（pages.json 空），两端构建通过。

## 真实集成验证（I03IdentityIT 10/10，真实 HTTP+Security(MINI链)+MyBatis+隔离库 MySQL+provider 模拟器）
命令：`mvn -pl xinyi-admin -am -Pvi-it -Dit.test=I03IdentityIT verify` → exit 0，10 用例 0 失败 0 错误 0 跳过。
1. 刷新轮换成功、新访问令牌可用、generation=1 链接父令牌、旧刷新已原子消费
2. 重用已消费刷新 → 401 VI_REFRESH_REUSED，会话族撤销**持久化**（会话 REVOKED、全部令牌 revoked_at 非空、新令牌随族失效）
3. 并发刷新仅一条成功（200/401），重用触发族撤销、旧刷新仅消费一次
4. 会话绝对到期 → 刷新 401 VI_AUTH_REQUIRED、不轮换、截止不滑动
5. 当前会话视图只读无令牌、实时重算角色（批准资格后无需重登即出现 DRIVER）
6. **TC-R63-008**：退出当前会话→旧令牌 401、会话 REVOKED；在手承接 ended_at 仍 NULL、资格仍 APPROVED；重登同一账号恢复 DRIVER 与在手承接
7. 全部设备退出递增 session_epoch、两会话令牌均失效、重登以最新 epoch 恢复
8. 退出缺/过短 Idempotency-Key 及未知字段 → 400，被拒请求不撤销会话
9. MINI 鉴权数据库权威撤销即时拒绝（直接库内撤销→同令牌 401，无 Redis 缓存宽限）
10. 刷新拒绝未知字段/过短令牌/未知随机令牌/ACCESS 冒充 REFRESH（域分离）

全量回归（同次）：E06 10/10、E07 5/5、E08 1/1、I01 9/9、I02 9/9、I03 10/10、平台单元 16/16，BUILD SUCCESS，无回归。
构建：`npm --prefix xinyi-ui run build:prod` exit 0；`npm --prefix xinyi-miniapp run build:mp-weixin` exit 0（DONE Build complete）。

## 负向场景对照（TASK.md 必测）
- **重用刷新撤对应会话族**：用例 2/3 真实验证撤销持久化与并发单次消费。
- **丢失刷新响应重新登录**：刷新响应丢失时旧刷新已被消费，客户端再次刷新触发重用→族撤销→须重新微信登录（用例 2 的族撤销 + 用例 6/7 的重登恢复共同覆盖该恢复路径）；http.ts 在刷新失败时 saveSession(null) 引导重登。
- **Redis 故障不放行**：MINI 鉴权零 Redis 依赖，用例 9 以数据库权威撤销即时拒绝验证该保证（不声称做了真实 Redis 宕机演练；设计层面 Redis 状态不影响 MINI 放行/拒绝）。

## Findings / 遗留
- **事务回滚缺陷已修复**：初版 doRefresh 在重用路径"先撤销再抛异常"，TransactionTemplate 会回滚撤销；改为结果模式（事务内撤销并提交、事务外抛 VI_REFRESH_REUSED），用例 2 以 DB 断言确认持久化。
- **STATION 失权回退 OWNER 为视角校正**：refresh 返回的 activeRole 按当前 availableRoles 校正（失权回 OWNER），activeRole 的持久化切换属 I04（角色/站点切换）；逐操作鉴权仍以实时资格/站点/部门交集为权威。
- **refresh 限流（x-authorization rateLimit:true）**：契约标注限流，属平台横切能力（X 系列/平台限流任务），I03 未单独实现端点限流；当前以单次消费+族撤销遏制刷新滥用。
- **真实微信渠道属 L01 门禁**：IT 微信换取为 SIMULATOR 替身，未声称真实渠道通过。

## 完成判据对照
- [x] 源接口/表/用例有明确实现文件与关联测试（非以目录存在当完成；本任务无页面）
- [x] 真实集成与负向场景实际通过并保存原始报告（I03IdentityIT 10/10 真实 HTTP+MySQL+模拟器）
- [x] 标注测试第三方替身（SIMULATOR provider）与延后项，不声称真实微信渠道通过（L01 另验）
- [ ] 独立复核（REVIEWED 需复核人≠实现人）