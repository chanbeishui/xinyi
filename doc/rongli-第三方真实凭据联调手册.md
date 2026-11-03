# 荣利（车辆检测代驾预约）第三方真实凭据联调手册

> 版本：2026-11-03　适用分支：master　对应代码：`xinyi-rongli`（后端）、`xinyi-rongli-wx`（小程序）、`xinyi-ui`（后台）
>
> 本文只讲一件事：**真实支付、真实短信、真实订阅消息，要你提供什么、填在哪里、怎么验证**。
> 三条链路的代码已经写完并通过编译，缺的是凭据本身、公网回调域名，以及把"联调模式"关掉。

---

## 一、先看结论：还差什么

调用一次体检接口即可拿到当前缺项（只读，不发消息、不下单）：

```bash
# 1) 登录拿 token
curl -s -X POST http://<host>:<port>/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2) 体检（probe=true 会额外探测支付回调地址是否公网可达）
curl -s "http://<host>:<port>/api/admin/config/readiness?probe=true" -H "Authorization: Bearer <token>"
```

当前本机 18080 实例的实测结果（未填任何真实凭据）：

| 项 | 状态 | 缺什么 |
| --- | --- | --- |
| 微信支付 | 未就绪 | 支付通道仍是 MOCK |
| 小程序凭据 | 未就绪 | AppID / AppSecret 未填，且服务端仍是联调模式 |
| 短信通道 | 未就绪 | 「真实下发」开关未启用 |
| 订阅消息 | 未就绪 | 一个场景模板都没配，且服务端仍是联调模式 |
| 支付回调 | 未就绪 | 回调地址为空 |

---

## 二、你需要提供什么（按通道列清单）

### 2.1 微信小程序（登录 / 手机号授权 / 订阅消息共用）

| 凭据 | 形态 | 从哪拿 |
| --- | --- | --- |
| 小程序 AppID | `wx` + 16 位十六进制，共 18 位 | 微信公众平台 → 开发管理 → 开发设置 → 开发者 ID |
| 小程序 AppSecret | 32 位 | 同上，「重置」生成后只显示一次，请立刻保存 |

> 这两个值同时驱动：`wx.login` 换 openId、`getPhoneNumber` 换手机号、`subscribeMessage/send` 下发订阅消息。
> 只要服务端还在联调模式，填了也不会被调用。

### 2.2 微信支付（JSAPI 收款 + 退款）

| 凭据 | 形态 | 从哪拿 |
| --- | --- | --- |
| 商户号 mchId | 8~12 位数字 | 商户平台 → 账户中心 → 商户信息 |
| APIv3 密钥 | 32 位字母数字 | 商户平台 → 账户中心 → API 安全 → APIv3 密钥（设置后不可查看，只能重设） |
| 商户证书序列号 | 40 位十六进制 | 用 `openssl x509 -in apiclient_cert.pem -noout -serial` 取 `serial=` 后半段 |
| 商户私钥 apiclient_key.pem | PEM 全文（含 BEGIN/END） | 商户平台 → API 安全 → 申请 API 证书，下载证书工具生成 |
| 平台证书 | **不需要你下载** | 填好上面四项后，调新接口自动拉取（见 §4.1） |
| 支付回调地址 | `https://你的域名/api/notify/wechat/pay` | 你的服务器，必须是公网 https |
| 退款回调地址 | `https://你的域名/api/notify/wechat/refund` | 留空会自动派生：把支付回调最后一段换成 `/refund` |

前置条件（缺一个都会失败，且报错不在我们的代码里）：

- 小程序与商户号**同主体**且已完成关联（商户平台 → 产品中心 → AppID 账号管理）；
- 已开通 JSAPI 支付；
- 回调域名有 ICP 备案 + 有效 https 证书；
- 服务端安全组放行 443，网关把 `/api/notify/wechat/**` 转发到后端（该路径已 permitAll，不需要登录）。

### 2.3 短信（验证码 + 业务通知）

| 项 | 阿里云 | 腾讯云 |
| --- | --- | --- |
| 供应商 | `ALIYUN` | `TENCENT` |
| 密钥 | AccessKeyId / AccessKeySecret（RAM 子账号，仅授 `dysms:SendSms`） | SecretId / SecretKey（CAM 子账号） |
| 签名 | 短信服务 → 签名管理（已审核通过，2~20 字） | 同左 |
| 模板 | 模板 CODE，形如 `SMS_123456789` | 模板 ID，纯数字 |
| 应用 ID | 不需要 | SdkAppId，短信控制台 → 应用管理 |
| 地域 | 固定 cn-hangzhou | 默认 `ap-guangzhou` |

### 2.4 订阅消息模板

在微信公众平台 → 订阅消息 → 公共模板库选用后，得到 4 个模板 ID（长串字母数字），以及每个模板的字段名（`thing1` / `time2` / `character_string3` / `amount4` …）。

系统支持 4 个业务场景，建议一次把 4 个都申请：

| 场景码 | 含义 | 建议模板 |
| --- | --- | --- |
| `DISPATCH_INVITE` | 派单邀约（司机） | 新的待接单提醒 |
| `ASSIGNMENT_ACCEPTED` | 接单成功（车主） | 订单已接单通知 |
| `RESULT_READY` | 检测结果可查看（车主） | 检测结果通知 |
| `REFUND_DONE` | 退款完成（车主） | 退款到账通知 |

还需要确定：点击消息后跳到小程序的哪个页面（如 `pages/order/detail`，**不要**以 `/` 开头），以及字段映射（默认 `{"thing1":"content","time2":"now"}`）。

---

## 三、填在哪里（后台菜单 / 配置键 / 接口 三对照）

后台共两套配置入口，**别填错地方**：

| 目录 | 用途 | 生效方式 |
| --- | --- | --- |
| 车辆检测预约 → 检测与配置 → **系统设置**（支付/短信/订阅三个页） | 订单支付、验证码短信、小程序凭据 | 保存即生效，无需重启 |
| 车辆检测预约 → **第三方服务**（支付方式/微信支付商户/短信通道/短信场景模板/订阅消息模板/小程序公共凭据/发布历史与发送诊断） | V6.4 业务事件通知（支付成功→预约确认、接单→司机已分配、检测完成、车辆归还） | 必须「保存草稿 → 校验 → 发布」才生效 |

**关键区别（务必看清）：**

- 车主点「立即支付」走的是 **系统设置 → 微信支付** 里的 `rongli.pay.*`；
- 车主/员工收到的业务短信、订阅消息走的是 **第三方服务** 里已发布的模板 + 路由；
- 验证码短信走 **系统设置 → 短信通道** 里的 `rongli.sms.*`；
- 小程序 AppID/AppSecret 只认 **系统设置 → 订阅消息** 页（写 `rongli.mini.appId` / `rongli.mini.appSecret`），
  第三方服务里的「小程序公共凭据」不驱动登录与订阅发送。

### 3.1 菜单路径

| 页面 | 前端路由 |
| --- | --- |
| 微信支付 | `/rongli/inspect/config/wechatPay` |
| 短信通道 | `/rongli/inspect/config/sms` |
| 订阅消息 | `/rongli/inspect/config/subscribe` |
| 第三方服务 → 微信支付商户 | `/rongli/integration/wechatPay` |
| 第三方服务 → 短信通道 | `/rongli/integration/smsChannel` |
| 第三方服务 → 短信场景模板 | `/rongli/integration/smsTemplates` |
| 第三方服务 → 订阅消息模板 | `/rongli/integration/subscribeTemplates` |
| 第三方服务 → 小程序公共凭据 | `/rongli/integration/wechatApp` |
| 第三方服务 → 发布历史与发送诊断 | `/rongli/integration/publications` |

### 3.2 配置键（sys_config）

| 键 | 说明 | 加密 |
| --- | --- | --- |
| `rongli.payment.channel` | `MOCK` / `WECHAT`，决定是否走真实支付 | 否 |
| `rongli.pay.mode` | `NORMAL` / `SANDBOX`（SANDBOX 只验签不能收款） | 否 |
| `rongli.pay.appId` | 小程序 AppID，**必须与 `rongli.mini.appId` 一致** | 否 |
| `rongli.pay.mchId` | 商户号 | 否 |
| `rongli.pay.serialNo` | 商户证书序列号 | 否 |
| `rongli.pay.apiV3Key` | APIv3 密钥 | **ENC:** |
| `rongli.pay.privateKey` | 商户私钥 PEM | **ENC:** |
| `rongli.pay.platformCert` | 平台证书 PEM | **ENC:** |
| `rongli.pay.platformSerialNo` | 平台证书序列号 | 否 |
| `rongli.pay.notifyUrl` | 支付回调 | 否 |
| `rongli.pay.refundNotifyUrl` | 退款回调（可空） | 否 |
| `rongli.pay.description` | 支付商品描述，1~127 字 | 否 |
| `rongli.pay.verifyNotify` | 回调是否验签，默认 true | 否 |
| `rongli.sms.enabled` | 短信真实下发开关 | 否 |
| `rongli.sms.provider` | `ALIYUN` / `TENCENT` | 否 |
| `rongli.sms.signName` / `templateCode` | 签名 / 模板 CODE | 否 |
| `rongli.sms.accessKeyId` / `accessKeySecret` | 阿里云密钥 | 均 **ENC:** |
| `rongli.sms.secretId` / `secretKey` | 腾讯云密钥 | 均 **ENC:** |
| `rongli.sms.sdkAppId` / `region` | 腾讯云应用 ID / 地域 | 否 |
| `rongli.sms.codeExpireSeconds` / `resendSeconds` / `dailyLimit` | 验证码有效期 / 重发间隔 / 单号日上限 | 否 |
| `rongli.subscribe.enabled` | 订阅消息总开关（旧通道） | 否 |
| `rongli.subscribe.template.dispatchInvite` 等四项 | 4 个场景模板 ID（旧通道） | 否 |
| `rongli.subscribe.page` / `miniprogramState` / `fields` | 跳转页 / `formal` / 字段映射 | 否 |
| `rongli.mini.appId` / `rongli.mini.appSecret` | 小程序凭据 | secret 为 **ENC:** |
| `rongli.mini.mock` | 服务端联调模式，**存在时覆盖** yml 的 `rongli.wechat.mock` | 否 |

> 带 **ENC:** 的项由 `ConfigStore.putSecret` 用 AES-GCM 加密后入库，页面上只回显"已配置"，
> 不回显明文；因此这类字段保存时留空表示"不修改"。

---

## 四、分步联调

### 4.1 微信支付（建议按这个顺序，共 6 步）

**第 1 步：填四项基础凭据**
后台 → 系统设置 → 微信支付：填商户号、商户证书序列号、粘贴商户私钥全文、填 APIv3 密钥 → 保存。

**第 2 步：自动拉取平台证书**（新能力，取代手工粘贴）

```bash
curl -s -X POST "http://<host>/api/admin/config/payment/platform-cert/refresh" \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"persist":true}'
```

期望返回：`fetched ≥ 1`、`selectedSerialNo` 有值、`persisted=true`。
- `persist=true` 会把最新证书写入 `rongli.pay.platformCert` + `rongli.pay.platformSerialNo`，**无需重启**；
- 不带 `persist` 或走 `GET` 只返回清单，用于确认"商户号 + 私钥 + APIv3 密钥三者是否配套"；
- 证书轮换（微信每年一次）后重跑这一条即可，不会因为旧证书过期导致全部回调被拒。

失败对照：
- `拉取平台证书前请先补全：…` → 还有四项没填或格式不符（先在页面上点保存，让格式校验跑一遍）；
- HTTP 401 `签名错误` → 私钥与证书序列号不配对，重新下载证书工具生成；
- HTTP 403 → 商户号未开通 APIv3 或 IP 白名单限制。

**第 3 步：填通道与回调**
AppID（= 小程序 AppID）、支付回调 `https://域名/api/notify/wechat/pay`、商品描述（如"机动车检测代驾预约"）、
通道切 `WECHAT`、模式 `NORMAL`、保持「回调验签」开启 → 保存。

**第 4 步：本地自检**
页面「自检」或 `POST /api/admin/config/payment/self-check`，期望：

| 字段 | 期望 |
| --- | --- |
| `privateKeyParsed` | true |
| `signatureProduced` | true |
| `platformCertParsed` | true |
| `ready` | true |
| `missing` | `[]` |

**第 5 步：真实小额下单**
小程序用测试号登录 → 选择订单 → 立即支付。此时下单返回 `mock:false` 且带 `paySign`。
若前端仍显示"模拟支付成功"，说明 `rongli.payment.channel` 没切过来。

**第 6 步：验回调与退款**
支付完成后微信会 POST 回调，检查：
- `vi_payment` 对应记录 `status=PAID`；
- 后台订单状态进入"待派单/待安排"；
- 同一回调重放一次不会重复推进（event_key 去重）。

再发起一笔退款，观察退款回调是否把状态推到"已退款"。

> 小程序侧注意：切真实通道后 `VITE_LOGIN_MODE` 必须为 `wechat`，否则拿不到真实 openId，
> 微信会报 `openid 与 appid 不匹配`。

### 4.2 短信（4 步）

1. 后台 → 系统设置 → 短信通道：选供应商 → 填签名、模板 CODE、AK/SK（或 SecretId/Key + SdkAppId）→
   打开「启用真实下发」→ 保存（保存时会校验格式）。
2. 点「发送测试短信」，填自己的手机号。**测试短信同样计入频控与日志**，不是免费通道。
3. 期望：页面提示已下发，`vi_sms_send_log` 新增一条 `status=OK`（含供应商 RequestId）。
4. 小程序侧验证：登录 → 绑定手机号 → 点获取验证码，手机应真实收到。

失败对照：

| 返回码 | 含义 | 处理 |
| --- | --- | --- |
| 阿里云 `SignatureDoesNotMatch` | AK/SK 错，或系统时间偏差 >15 分钟 | 核对密钥；校准服务器时间 |
| 阿里云 `isv.SMS_SIGNATURE_ILLEGAL` | 签名未审核/与账号不匹配 | 短信控制台确认签名状态 |
| 阿里云 `isv.SMS_TEMPLATE_ILLEGAL` | 模板 CODE 错或未通过审核 | 用控制台里的 CODE 原文 |
| 腾讯云 `InvalidParameterValue.PhoneNumber` | 号码格式 | 代码已自动补 `+86`，检查是否非大陆号 |
| 腾讯云 `FailedOperation.SignatureIncorrectOrUnapproved` | 签名问题 | 同上 |
| 腾讯云 `AuthFailure.SecretIdNotFound` | SecretId 错 | 核对 CAM 子账号密钥 |

> 2026-11-03 修掉一个必现 bug：阿里云分支声明 `SignatureMethod=HMAC-SHA1` 却用 HMAC-SHA256 计算签名，
> 真实密钥下 100% 返回 `SignatureDoesNotMatch`。已改为按声明算法计算。

### 4.3 订阅消息（5 步）

**先做一个选择**（两条通道都实现了，但同场景不要都配，否则用户会收到两条）：

- **方案 A（推荐）**：模板配在「第三方服务 → 订阅消息模板」，发布 + 配路由。
  业务事件（支付成功/接单/检测完成/归还）由 V6.4 投递链路统一处理，带发送记录、回执查询、失败重试。
- **方案 B（快速验证）**：模板只配在「系统设置 → 订阅消息」的 4 个场景框里，不建 V6.4 模板。
  适合先用一个测试号打通"能收到"。

无论哪个方案，小程序凭据都必须在「系统设置 → 订阅消息」页填。

1. 填 AppID + AppSecret → 保存；
2. 关闭服务端联调模式（见 §5），否则下发只写日志；
3. 点「连通性自检」，期望 `passed=true`（会真实换一次 access_token，能换到就说明 AppID 与 Secret 配对）；
4. 配模板（方案 A 或 B）+ 跳转页 + 字段映射；
5. 真机走一遍：登录 → 下单 → 支付 → 派单接单，收到订阅消息；点开应跳到订单详情。
   后台「第三方服务 → 发布历史与发送诊断」能看到 `ACCEPTED`/`SKIPPED` 及原因码。

失败对照：

| errcode | 含义 | 处理 |
| --- | --- | --- |
| 40001 | access_token 失效 | 代码已自动清缓存重取一次，连续出现说明 AppSecret 错 |
| 40013 | invalid appid | AppID 与 Secret 不同主体 |
| 43101 | 用户未订阅/已取消 | 终态，记 `SKIPPED` 不重试；需用户重新授权 |
| 47003 | 模板参数为空或不合法 | 字段映射（`thing1` 等）与模板不一致，或值超长（thing 类限 20 字） |
| 41030 | 页面不存在 | `page` 写错，注意不要以 `/` 开头 |

> 从联调模式切到真实模式后，**旧账号的 open_id 是 `dev_` 开头的假值**，必须重新登录生成真实 open_id，
> 否则订阅下发一律被微信拒绝。

---

## 五、联调模式开关（每次真实联调都要做的动作）

```bash
# 关闭联调（会先校验 AppID/AppSecret，缺则拒绝，避免登录直接 503）
curl -s -X PUT "http://<host>/api/admin/config/readiness/mock" \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"mock":false}'

# 需要回到演示/无凭据状态时再打开
curl -s -X PUT ".../readiness/mock" -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" -d '{"mock":true}'
```

写入 `rongli.mini.mock`，保存即生效，**不需要重启**（此前只能改 `application.yml` 的 `rongli.wechat.mock`
并重启，导致"凭据填了也验证不了"）。体检接口的 `mockSource` 会告诉你当前生效值来自 sys_config 还是 yml。

---

## 六、本次代码改动（已完成部分）

| 文件 | 改动 | 原因 |
| --- | --- | --- |
| `support/SmsClient.java` | 阿里云签名改用 HMAC-SHA1，HMAC 工具按算法参数化 | 原先算法与声明不一致，真实 AK 必挂 |
| `support/WechatMiniClient.java` | `isMock()` 改为 sys_config 优先、yml 兜底；新增 `ymlMock()` | 联调模式原先只能改 yml 重启 |
| `support/WechatPayClient.java` | 新增 `fetchPlatformCertificates()`、`credentialMissing()` | 平台证书自动拉取，支持轮换 |
| `service/PayCertService.java`（新） | 拉取 + 解密 + 落库 platformCert/serialNo | 取代手工粘贴证书 |
| `controller/admin/AdminPayCertController.java`（新） | `GET/POST /api/admin/config/payment/platform-cert(/refresh)` | 上述能力的入口 |
| `service/ChannelReadinessService.java`（新） | 三通道 + 小程序 + 回调五块体检，含回调可达性探测 | 一屏看清"还差什么" |
| `controller/admin/AdminChannelReadinessController.java`（新） | `GET /readiness`、`PUT /readiness/mock` | 体检与联调开关入口 |
| `service/AuthService.java` | 登录 appId 改为「配置页 → yml → 开发号」；删掉未生效的 `wechatMock` 字段 | 真实模式下登录仍用开发号会报 `invalid appid` |
| `integration/NotificationDispatchService.java` | 订阅跳转页兼容 `pageRoute` 与 `page` 两个键 | 模板发布写的是 pageRoute，发送端只读 page，导致点开消息无落地页 |

已验证：`mvn compile` 通过；18080 实例启动正常；体检接口、`PUT /readiness/mock`、
平台证书接口（缺凭据时返回可读的缺项清单）均按预期响应。

---

## 七、上线前检查清单

- [ ] `rongli.mini.mock=false`（或 yml `rongli.wechat.mock=false`），体检 `mock=false`
- [ ] 小程序凭据自检 `passed=true`
- [ ] 支付通道 `WECHAT`、模式 `NORMAL`、`ready=true`、平台证书已落库且未过期
- [ ] 回调地址为公网 https，`probe=true` 返回「可达」
- [ ] 回调验签保持开启（`verifyNotify=true`），且平台证书序列号与微信侧一致
- [ ] 短信 `enabled=true`，测试短信到达
- [ ] 订阅消息运行环境 `formal`（`developer`/`trial` 只对开发/体验成员生效）
- [ ] 同场景只保留一条通道（V6.4 模板 或 `rongli.subscribe.*`），避免双推
- [ ] 支付/短信/订阅各做一次真实全链路，并保留截图或投递记录作为证据

---

## 八、仍需你确认或暂不支持

1. **公网域名与备案**：回调地址必须由你提供，本机只能验证签名与报文格式。
2. **商户号开通状态**：JSAPI 支付、AppID 关联、APIv3 密钥、API 证书四项需在商户平台完成，代码无法代劳。
3. **短信签名/模板审核**：审核期间下发会被拒，属于供应商侧流程。
4. **订阅消息模板申请**：一次性模板需用户每次授权，长期模板需要类目资质；选哪套取决于你们的类目。
5. **实名活体 / 行驶证 OCR**：仍为 mock 或缺失，不在本次三通道范围内，需单独确认供应商（腾讯云人脸核身 / 阿里云实人认证）。
6. **微信支付「公钥模式」**：当前只实现平台证书验签；如果你们商户号启用了"微信支付公钥"，需要再补一套公钥验签分支。
