# E06 幂等、审计、outbox 与任务基座 · 报告

状态：VERIFIED（REVIEWED 待独立复核人≠实现者）。执行者：Qoder-Agent。
目标库：现有 `xinyi`（MySQL 9.6.0，用户决定，非资料包默认隔离库）。

## 交付（对照 TASK.md 三项）

### 1. 同事务业务写与 outbox
- `OutboxService.publish(eventKey, aggregateType, aggregateId, eventType, payload)`：在调用方 `@Transactional` 内插入 `vi_outbox`（status=READY，next_attempt_at=now），与业务写**同事务提交/回滚**——业务回滚则事件不发出，杜绝“成功事件先于业务落库”。
- `event_key` 唯一约束防重（自然源事件）：重复发布同 key 捕获 `DuplicateKeyException` 幂等忽略（返回 false）。
- `ViOutboxRelay`：与框架 `SecurityOutboxProcessor` **各自独立 Bean**（ADR-02：不注册第二个 SessionInvalidator、vi_outbox 单独分发）。三段式——①租约恢复（回收 lease 过期的滞留 PROCESSING）②领取（短事务 `FOR UPDATE SKIP LOCKED` 选到期 READY，仅领取“已有分发器”的事件，CAS 置 PROCESSING+租约）③事务外分发 + 短事务结算（成功 DONE；失败 attempts+1 退避，达上限 FAILED）。**时钟来自 DB 到期扫描，不依赖 Redis 过期通知**（ADR-08）。
- `OutboxDispatcher` SPI：E06 只提供基座+中继，不实现任何业务分发；通知/支付查单/补偿等由对应业务任务注册。

### 2. 有序锁 / 唯一约束 / 请求幂等
- `IdempotencyService.execute(subjectKey, actionKey, requestKey, body, cacheResult, resultType, business)`：必须在业务 `@Transactional` 内调用，使“幂等占位 + 业务写 + 审计 + outbox”同事务。
  - `request_mac = HMAC-SHA256(vi.lookup-key, body)`（32 字节，匹配 `BINARY(32)`）；空密钥拒绝计算（`VI_CAPABILITY_NOT_CONFIGURED`），不以空密钥静默生成摘要。
  - 唯一键 `uk_request(subject_key,action_key,request_key)` 冲突时：`MessageDigest.isEqual` 常量时间比较 mac——**不同体 → 409 `VI_IDEMPOTENCY_CONFLICT`**；同体已 DONE → 重放缓存结果（业务不重跑）；同体 PROCESSING → 409 在途可重试。
  - `cacheResult=false`（登录/刷新等含明文令牌动作）：只去重不缓存体，`markDoneNoResult` 保持 `result_json=NULL`。
- `OrderLocks.orderedIds/orderedKeys`：去重+升序给出**规范加锁顺序**（ADR-05/06：多行 FOR UPDATE 必须固定顺序，否则交叉死锁）。
- `DeadlockRetry.execute(maxAttempts, transactionalWork)`：仅对 `PessimisticLockingFailureException`（死锁 1213 / 锁等待超时 1205）有限重试，**重试须重跑完整事务**（模板自身绝不在事务中）；其余异常（唯一键/版本冲突）原样抛出，不把一切 SQL 异常当并发冲突。

### 3. 审计双主体
- `AuditService.recordSuccess/recordFailure`：写不可变 `vi_event_log`。
  - **双主体**：`actor_realm` 区分 MINI/ADMIN/SYSTEM/CALLBACK；MINI 落 `actor_account_id`，ADMIN 落 `actor_user_id`，两者**绝不数值混比**；`Actor.samePrincipal` 仅同域比较，跨域“是否同一人”须经 `vi_employee_binding` 可信绑定（I01/I03），结构上拒绝 accountId==sysUserId 伪等价（ADR-02）。
  - **成功审计同事务**（REQUIRED）：业务回滚则成功流水不留下——落实“失败不写成功日志”。
  - **失败审计独立事务**（REQUIRES_NEW）：即便主事务回滚也持久化拒绝/失败事实。
  - before/after_summary 经 `SecretGuard` 拒绝明文机密。
- `Actor`（不可由 HTTP 反序列化构造）+ `BusinessActorResolver`（唯一读 SecurityContext 的边界：MINI 优先于 ADMIN，都无则 SYSTEM；共用 Service 不直接强转 `SecurityUtils.getLoginUser`——ADR-01）+ `MiniPrincipal` 只读契约（具体认证由 I01/I03 实现）。

## 附带修正
- **D-E04-1（X-HTTP-Method-Override）**：`HttpMethodOverrideFilter` 仅在 `/api/*`、仅对 POST、仅接受 {PATCH,PUT,DELETE} 时改写方法，使微信 wx.request 的 POST+override=PATCH 能被 `@PatchMapping` 匹配；`ViWebConfig` 以 `FilterRegistrationBean` order=-200 注册（早于 Security 的 -100），不影响 `/system` 等框架端点。
- **D-E04-2 收尾**：`spring-boot-starter-test` 从 vi-it profile 移入 xinyi-admin 主 test 依赖，使 viit 测试骨架在普通 `mvn test`（无 profile）下可 testCompile。
- **failsafe classpath**：vi-it profile 的 failsafe 增 `classesDirectory=${project.build.outputDirectory}`，避开 `spring-boot:repackage` 后 fat jar（BOOT-INF/classes 结构）导致 IT 主类 `TypeNotPresentException`。
- **ViJson**：vi 平台自持隔离 ObjectMapper（本 RuoYi 分支 HTTP 用 fastjson2、上下文无 Jackson ObjectMapper Bean）；不注册全局 ObjectMapper Bean，避免触发 JacksonAutoConfiguration 退避而改变框架响应序列化。

## 验证（真实执行）
| 项 | 命令 | 结果 |
|---|---|---|
| 单元 | `mvn -pl xinyi-admin -am test` | exit 0，BUILD SUCCESS；surefire 22 全过=E06 新增 16（SecretGuard 5 + PlatformPrimitives 6 + IdempotencyMac 5）+ 既存 6 |
| 真实集成 | `mvn -pl xinyi-admin -am -Pvi-it -Dit.test=E06PlatformIT verify` | exit 0，BUILD SUCCESS；**E06PlatformIT 10 用例 0 失败 0 错误 0 跳过**（真实 MySQL+MyBatis+Service+事务，webEnvironment=MOCK） |
| 库零污染 | pymysql 探针 | E06_IT 标记残留 0/0/0；三表总行数 0/0/0 |
| 工程结构 | `check_engineering.py` | pass=34 fail=0 |

### E06PlatformIT 10 用例（覆盖三交付+三边界）
1. 同键同体重放_业务不重复执行（幂等重放，runs 计数=1）
2. 重复键不同体_409幂等冲突（边界：VI_IDEMPOTENCY_CONFLICT + httpStatus 409）
3. 业务失败_幂等占位同事务回滚（失败请求零幂等痕迹）
4. 含明文令牌结果_拒绝缓存（边界：SecretGuard → VI_DATA_INVALID，回滚后零落库）
5. outbox与业务同事务_提交可见回滚消失（同事务交付）
6. outbox唯一事件键防重（uk_event 幂等忽略）
7. 中继领取分发并置DONE（relay runOnce + 测试替身分发器）
8. 中继分发失败_退避重试不丢事件（attempts=1，回 READY，写退避时间）
9. 审计双主体_MINI与ADMIN分列不混比（actor_account_id vs actor_user_id，另一列为 NULL）
10. 成功审计同事务回滚_失败审计独立持久化（边界：aggregate_id=200 成功流水缺失、201 失败流水存在）

> 测试第三方替身声明：仅 `RecordingDispatcher`（OutboxDispatcher 测试实现）为替身；数据库、MyBatis、Service、事务、幂等/审计/outbox 逻辑均真实。未声称任何真实供应商/支付/真机渠道通过。

## 边界与负向（符合 negative_checks）
- 登录刷新不缓存明文：`cacheResult=false` + `SecretGuard` 双重保障（见上）。
- 重复键不同体 409：mac 常量时间比较，IT 用例 2 验证。
- 失败不写成功日志：成功审计同事务回滚 / 失败审计 REQUIRES_NEW，IT 用例 10 验证。
- 未改框架既有 `.java`（git diff 仅 `xinyi-admin/pom.xml` 附加修改）；未注册第二个 SessionInvalidator；未触碰 sys_security_outbox。

## 完成判据对照（E06 TASK.md）
- [x] 源接口/表/用例有明确实现文件与关联测试（27 主类+3 XML+4 测试；非以目录存在当完成）
- [x] 真实集成与负向场景实际通过并保存原始报告（E06PlatformIT 10 用例真实 MySQL）
- [x] 标注测试第三方替身（RecordingDispatcher），不声称真实渠道通过
- [ ] 独立复核（REVIEWED 需复核人≠实现者）

## 遗留/后续
- MiniPrincipal 具体认证（令牌/刷新轮换/会话持久化）由 I01/I03 实现并填充本只读契约。
- V6 ApiError 信封的 `@RestControllerAdvice` 渲染留待首个 vi Controller 任务；E06 服务层抛 `ViException`（携带 errorCode/HTTP 状态/fieldErrors/conflicts/retryAfter），IT 直接断言。
- 具体 OutboxDispatcher 由业务任务注册；E06 中继对“无分发器”事件保持 READY、不计 attempts。
- MySQL 9.6.0 与目标 8.4 偏差为 RELEASE 门禁：生产 8.4 须复跑 SQL/CAS（沿 E03 记录）。
