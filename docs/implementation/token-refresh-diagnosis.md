# 小程序登录令牌频繁失效：定位与修复

> 环境：微信开发者工具 + `xinyi-rongli-wx`（uni-app）+ `xinyi-rongli` 后端
> 结论时间：2026-10-22

## 一、结论

**不是令牌过期时间的问题，而是「服务端轮换了令牌，前端没有落盘」。**

后端在切换角色、切换检测点、员工绑定三个动作里都会撤销整条旧令牌链并签发新令牌；
前端只更新了内存状态（`applySession`），从未把返回的新令牌写进 Storage。
于是之后每个请求都携带**已被服务端撤销**的旧令牌，必然 401；而续期所需的 refresh 令牌
也在同一次撤销里失效，无法自愈，只能被踢回登录页。用户体感就是"令牌频繁失效"。

## 二、现场证据

数据库 `vi_session_token` 的统计直接否定了"令牌过期"的假设：

| 指标 | 实测值 | 说明 |
|---|---|---|
| `consumed_at` 非空的刷新令牌 | **0 / 96** | 续期接口从未成功执行过一次 |
| `revoked_at` 非空的刷新令牌 | 11 | 被"整链撤销"连带作废，从未被消费 |
| 9 分钟内新建的会话 | 7 个（02:51–03:00） | 反复重新登录，而非续期 |
| 每个会话的 `generation` | 恒为 1 | 从未发生过第 2 代令牌 |

`session 85` 留下了最关键的一行证据：`context_version=1` 的令牌对在 `03:00:20.134`
被撤销，`context_version=2` 的新令牌在 `03:00:20.136` 签发 —— 间隔 2 毫秒，正是
`switchRole` 内部的 `revokeTokensBySession` + `issueTokens`。

接口实测（两组均复现）：

```
POST /api/common/session/role   -> 200，返回新 accessToken/refreshToken
GET  /api/common/session  带旧令牌 -> 401 {"code":"UNAUTHORIZED","message":"登录令牌已失效"}
GET  /api/common/session  带新令牌 -> 200
POST /api/common/token/refresh 带旧 refresh -> 401 "刷新令牌已失效，请重新进入小程序"
```

## 三、按你列的方向逐项排查

### 1. 令牌过期时间设置 —— 排除

`ACCESS_TTL_MINUTES=120`、`REFRESH_TTL_DAYS=30`、`SESSION_TTL_DAYS=30`，
写入 `expires_at` 的也是 UTC 分量（`ViTime.now()` 与 SQL 的 `UTC_TIMESTAMP(3)` 基准一致，
实测 `created_at=03:00` → `expires_at=05:00`，正好 +2h）。时间基准没问题。

真实问题是这三个值**硬编码在 Java 常量里**，联调时想缩短有效期复现问题只能改代码重启。
已改为配置项（默认值不变），见第五节。

### 2. 请求拦截与刷新机制 —— 主因所在

`request.ts` 的 401 分支本身写得对（单飞 `refreshing` + 重放 + `retried` 防死循环），
但**它依赖的令牌压根没被更新**。三个调用点全部漏了落盘：

| 位置 | 问题 |
|---|---|
| `pages/role/index.vue:101` | `switchRole` 后只 `applySession(view)` |
| `pages/station/home/index.vue:141` | `switchStation` 后只 `applySession(view)` |
| `pages/settings/index.vue:185` | `bindEmployee` 后**连返回值都丢弃**，只弹"绑定成功" |

此外 `uploadFile` 走的是 `uni.uploadFile`，完全没有 401 续期分支 —— 调试时上传车况照片
会直接提示"上传失败"，同样被感知为令牌问题。

### 3. 本地存储读写 —— 次因

`saveTokens` / `clearSession` 逻辑正确，但有两处隐患：

- `App.vue` 里把存储键硬编码成字符串 `'rongli_access_token'`，而其余位置用 `STORAGE.access`
  常量。一旦常量改动，两处就会漂移，表现为"明明登录了却判定未登录"。
- 开发者工具里切换编译模式、点"清缓存 → 全部清除"会清掉 Storage，`onShow` 的静默校验
  拿不到令牌就会跳登录页。这属于工具行为，不是代码缺陷，但会放大人对"失效"的感知。

### 4. 并发请求下的令牌覆盖 —— 真实存在，已修

后端 `AuthService.refresh()` 里：

```java
if (!"REFRESH".equals(...) || consumed != null || revoked != null) { ... }
...
accountMapper.consumeToken(t.lng("id"));          // 返回值被丢弃
accountMapper.revokeTokensBySession(s.lng("id")); // 撤销该会话下所有未撤销令牌
```

并发两个刷新请求时，两者都能通过 `findToken` 的校验（读时都还是未消费状态），
随后都会执行 `revokeTokensBySession`。后到的那个会把**前一个刚签发的新令牌一起撤销**，
客户端若收到的是先返回的那套，就必然陷入"刷新成功 → 立刻失效 → 再刷新 → 再次失效"。

前端侧还有一处放大：`refreshing` 在 `complete` 里同步清空，某个请求的 401 若晚于
刷新完成才回来，会再触发一次多余轮换，而每次轮换都会撤销刚用上的令牌。

### 5. 调试环境的网络与缓存差异 —— 已排除为根因

- `.env` 的 `VITE_API_BASE=http://localhost:8080`：开发者工具可用，但必须勾选
  **详情 → 本地设置 → 不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**，
  否则请求被工具拦截，表现为"网络异常"而非 401。
- 工具的 GET 缓存、HMR 重编译会重置模块级的 `refreshing` 变量，让在飞的刷新 Promise 变孤儿；
  这类问题只在热更新期间出现，冷启动不复现。
- 一个实例层面的坑（本项目特有）：**8080 与 18080 连同一个库**，`vi_outbox` 是竞争消费，
  两个实例必须是同一份代码，否则会出现"事件被旧实例标 DONE 却不投递"。

## 四、修复内容

### 前端 `xinyi-rongli-wx`

**`src/store/session.ts`** —— 根因修复，一处覆盖三个调用点：

```ts
export function applySession(view: any) {
  if (!view) return
  // 切换角色/站点、员工绑定都会撤销旧令牌链并签发新令牌，带了就必须落盘
  if (view.accessToken) {
    saveTokens(view.accessToken, view.refreshToken)
  }
  session.loggedIn = true
  ...
}
```

`session()` 接口返回的视图不含 `accessToken`（`sessionView(sessionId, null)`），
因此这段不会误覆盖已有令牌。

**`src/utils/request.ts`**

1. `RequestOptions` 新增 `token` 快照字段；401 时先比对快照与当前存储：
   - 不一致 → 令牌已在别处续期，**直接用新令牌重放，不再刷新**（避免多余轮换）
   - 一致 → 走 `refreshToken()`，失败才跳登录
2. `refreshing` 改为在刷新结果落地后才释放，不再是 `complete` 里同步清空。
3. `toLogin()` 增加 `redirecting` 闸门，并发 401 只 `reLaunch` 一次。
4. `uploadFile` 补 401 续期后重试一次（`retried` 参数）。

**`src/App.vue`** —— 存储键改用 `STORAGE.access` 常量，消除键名漂移。

### 后端 `xinyi-rongli`

**`AuthService.refresh()`** —— 让"单次消费"真正生效：

```java
if (accountMapper.consumeToken(t.lng("id")) == 0) {
    throw ApiException.unauthorized("刷新令牌已被使用，请重新进入小程序");
}
```

`consumeToken` 的 SQL 本身带 `WHERE consumed_at IS NULL AND revoked_at IS NULL`，
返回 0 说明已被别的请求抢先消费。此时直接失败，**不再执行整链撤销**，
从而不会误伤并发中标胜者的新令牌。

**TTL 改为可配置**（默认值与原来完全一致，行为不变）：

```java
@Value("${rongli.auth.access-ttl-minutes:120}") private long accessTtlMinutes;
@Value("${rongli.auth.refresh-ttl-days:30}")    private long refreshTtlDays;
@Value("${rongli.auth.session-ttl-days:30}")    private long sessionTtlDays;
```

## 五、下次排查同类问题的思路

按"先证伪，再定位"的顺序，四步即可收敛：

1. **区分"过期"与"被撤销"**：查 `vi_session_token` 的 `consumed_at` / `revoked_at` /
   `generation`。如果 `generation` 恒为 1 且 `consumed` 为 0，说明续期从没跑通，
   方向应转向"前端有没有发续期请求"，而不是查 TTL。
2. **看会话数量增长**：短时间内大量新建 session = 反复重新登录，不是续期失败。
3. **抓一次写操作前后的令牌**：凡是接口返回体里带 `accessToken` 的（登录、切换角色、
   切换站点、员工绑定），前端必须落盘；用 `curl` 复现一次即可确认服务端是否已撤销旧令牌。
4. **并发验证**：用同一个 refresh 令牌并发打两次续期，断言"恰好一个 200、一个 401"，
   且 200 那方的新令牌仍可用 —— 这是并发误伤的唯一可靠判据。

调试期可把 `rongli.auth.access-ttl-minutes` 调成 1，几十秒就能走完整个过期→续期路径。

## 六、验证结果

新增 `logs/verify_token_refresh.py`（真实 HTTP，14 条断言，全部通过）：

- 切换角色后旧访问令牌 401、新访问令牌 200、旧刷新令牌已撤销
- 并发续期恰好 1 个 200、1 个 401，提示"刷新令牌已被使用"
- **并发中成功签发的令牌未被对方撤销**（修复前必然失败）
- 迟到 401 用当前令牌重放即可成功，且未发生多余轮换

回归：`logs/run_acceptance.py` **112/112 通过**（含侧栏统计 24、后台页面 20、存量冒烟 28、
本轮专项 37）；小程序 `build:mp-weixin` 构建通过；后端 60 个源文件编译通过。

## 七、仍需注意的边界

1. **切换角色/站点会强制换令牌**是既有的安全设计（`context_version` 校验），本次没有改动语义，
   只是让前端跟上。若后续希望"切角色不打断在途请求"，需要把 `revokeTokensBySession`
   收敛为"仅撤销更低 generation 的令牌"，这属于策略调整，需单独评审。
2. **员工绑定后**（`consumeBinding`）同样会轮换令牌，现在由 `applySession` 统一落盘；
   但 `settings/index.vue` 绑定成功后只调了 `loadProfile()`，若后续有人在绑定流程里
   另加分支，请沿用 `applySession(view)` 而不要直接 `uni.setStorageSync`。
3. 开发者工具清缓存会丢 Storage，这不是 bug；真机复测时请以真机为准。
