# 小程序授权登录与主流程检查·整改说明

> 结论先行：**"手机号授权登录"此前根本没有实现**，不是配置问题、也不是微信侧限制。
> 前端没有手机号授权入口，后端没有换取手机号的接口，真实微信登录本身也未接通（只有联调模式）。
> 本轮补齐了「手机号授权登录 → 自动识别员工 → 后台配置检测点」这条链路，并修掉两处会让流程直接报错的缺陷。
> 但**真实微信支付仍未实现**，这是上线前的硬性缺口。

核查时间：2026-10-22　核查方式：读代码 + 真实接口实测（`logs/audit_mini_flows.py` 等）

---

## 一、任务总览

### 1.1 之前列出的待办（上一阶段）

| 项目 | 状态 | 说明 |
|---|---|---|
| D02 多轮扩圈 | DONE | 阶梯半径可配置，逐轮留痕，耗尽转人工 |
| Q03 播放心跳 | DONE | 会话校验、序号幂等、跳变拦截 |
| N01 订阅消息 | DONE | 站内信入队 + outbox 消费（模板未配置时只走站内信） |
| 部门数据权限 | DONE | 订单/统计/工作台/司机/车辆/售后 |
| CI 与隔离环境 | DONE | `logs/ci_sql_check.sh` 与 workflow 同源，全新库幂等通过 |
| 298 条验收用例台账 | DONE（台账） | 用例状态仍为 `NOT_RUN`，未代替人工判定 |

### 1.2 本轮新增（本次交付）

| 编号 | 任务 | 状态 |
|---|---|---|
| A01 | 手机号授权登录（`getPhoneNumber` → 服务端换号） | DONE |
| A02 | 补齐真实微信登录 `jscode2session`（此前只能 mock） | DONE |
| A03 | 手机号命中员工账号 → 自动绑定 openid | DONE |
| A04 | 首次登录仅车主身份；代驾需审核；检测员工需后台配站点 | DONE（原逻辑正确，本轮补验证与提示） |
| A05 | 员工/站点关系全部收归后台，小程序端不再申请 | DONE |
| A06 | 后台「新增站点-员工绑定」（此前只能开关已有行） | DONE |
| A07 | 修复小程序站点工单列表 500 | DONE |
| A08 | 真实微信支付 | **TODO（未实现）** |

---

## 二、"手机号授权登录"为什么走不通

不是单点故障，是**四层同时缺失**，任何一层都足以让链路中断。

### 2.1 前端没有授权入口（最直接原因）

`xinyi-rongli-wx/src/pages/login/index.vue` 只有一个普通按钮，点击后调 `uni.login()` 取 `code`：

```ts
uni.login({ provider: 'weixin', success: (res) => resolve(res.code) })
const view = await commonApi.login({ code, deviceLabel: 'mini-program' })
```

全仓库检索 `getPhoneNumber` / `open-type`：**0 处命中**。
微信的手机号敏感数据必须由 `<button open-type="getPhoneNumber">` 触发，普通按钮拿不到。
所以用户在小程序里**从来没有看到过"授权手机号"这个动作**。

### 2.2 后端没有换号接口

- `MiniDtos.LoginReq` 字段只有 `code / appId / nickname / deviceLabel`，没有 `phoneCode`；
- 全后端检索 `getPhoneNumber` / `getuserphonenumber` / `phoneCode`：**0 处命中**；
- 唯一的手机号入口是 `POST /api/common/phone/bind`，走**短信验证码**，且只做三件事：校验格式、校验短信码、写 `vi_account.phone_cipher/phone_lookup`。

也就是说：即便前端拿到 `phoneCode`，后端也无处可传、无人解析。

### 2.3 真实微信登录本身没接通

`AuthService.resolveOpenId()` 原实现：

```java
if (wechatMock) { return code.startsWith("dev_") ? code : "dev_" + code; }
throw new ApiException(SERVICE_UNAVAILABLE, CHANNEL_UNAVAILABLE, "微信登录通道未配置…");
```

`rongli.wechat.mock` 默认 `true`。一旦置为 `false`（上真实环境），**登录直接 503**。
`jscode2session` 一次都没有被调用过。这意味着"真实手机号授权登录"在服务端也没有地基。

### 2.4 员工身份只有"后台发凭证"一条路

原设计里，检测员工想获得工作视角必须：
1. 在小程序设置页提交"绑定申请"，后台人工审核；
2. 后台签发一次性凭证，线下发给员工；
3. 员工在小程序粘贴凭证完成绑定。

用户要求的"手机号与员工账号一致即自动绑定"**完全没有实现** —— `bindPhone` 只写手机号，从不查 `sys_user`。

### 2.5 实测证据（改造前）

`python logs/audit_mini_flows.py` 的输出：

```
[OK  ] 200 登录并携带手机号授权码
        返回是否含手机号字段: phoneBound=False      ← phoneCode 被完全忽略
[OK  ] 200 用验证码绑定手机号
        绑定后 phoneBound=True roles=['OWNER'] employeeBound=False   ← 手机号命中 admin 也不绑定
[FAIL] 403 授权站点   {"message":"尚未绑定员工身份，无法使用检测点工作台"}
```

---

## 三、逐流程核查结论

实测脚本：`logs/audit_mini_flows.py`（主链路）、`logs/verify_driver_flow.py`（履约全链路 37 步）、
`logs/verify_phone_login.py`（29 项）。

| 环节 | 改造前 | 改造后 | 依据 |
|---|---|---|---|
| 冷启动进入 | 可用 | 可用 | `App.vue` onLaunch 恢复展示态，onShow 静默校验会话 |
| 手机号授权 | **未实现** | 可用 | `open-type="getPhoneNumber"` + `/login/wechat` 带 `phoneCode` |
| 微信登录（真实） | **503** | 可用 | 新增 `jscode2session` 调用（需配 appId/appSecret） |
| 登录（联调） | 可用 | 可用 | code 作稳定 openId，加 `dev_` 前缀 |
| 会话/续期 | 可用 | 可用 | 切换角色/站点/绑定均轮换令牌，前端整包覆盖 |
| 视角切换 | 可用 | 可用 | `rolesOf`：OWNER 默认，DRIVER 需审核通过，STATION 需绑定+站点授权 |
| 车主建档（车辆/地址） | 可用 | 可用 | 实测 200 |
| 时段 → 报价 → 下单 | 可用 | 可用 | 实测 200（报价需该车型+检验类型有生效规则） |
| 支付 | **仅 mock** | **仅 mock** | `prepay` 返回 `prepay_id=mock…`，无 JSAPI 下单与回调 |
| 派单 → 邀约 → 接单 | 可用 | 可用 | 订单未到提前派单窗口时为 `SCHEDULED`，到点或后台"立即派单"才发邀约 |
| 代驾资格申请 | 可用 | 可用 | 提交后 `roles` 仍只有 OWNER，审核通过才有 DRIVER |
| 培训与学时 | 可用 | 可用 | Q03 心跳校验（9 项通过） |
| 行程（出发/到达/到站） | 可用 | 可用 | 实测 200 |
| 交接 · 车主↔代驾 | 可用 | 可用 | 车主出码、代驾核验（OWNER_TO_DRIVER）；归还时代驾出码、车主核验 |
| 交接 · 代驾↔站点 | **站点列表 500** | 可用 | 修复 `DeptScope` 在 MINI 上下文抛异常；站内交接由检测员工发起并核验 |
| 检测点工单/证件/结果/通知 | 可用 | 可用 | 实测 200，结果登记前须完成行驶证核验 |
| 还车 → 确认服务 → 完成 | 可用 | 可用 | 实测终态 `COMPLETED` |
| 售后/申诉/对账 | 接口通 | 接口通 | 冒烟覆盖；本次未做端到端深度回归 |
| 消息中心/隐私请求 | 可用 | 可用 | 实测 200 |

**改造后一次完整履约实测：37 步全部 200，终态 `COMPLETED`。**

```
PICKED_UP → ARRIVED_STATION → INSPECTING → INSPECTED → CAR_TAKEN → COMPLETED
```

最终全量回归：`python logs/run_acceptance.py` → **192 条，通过 192，失败 0**（10 个套件，通过率 100%）。
资料包 298 条验收规格仍为 `NOT_RUN`，台账只登记证据，不代替人工判定。

---

## 四、整改内容

### 4.1 手机号授权登录

**新增 `support/WechatMiniClient.java`**

| 方法 | 作用 |
|---|---|
| `resolveOpenId(appId, code)` | 联调：`dev_`+code 作稳定标识；真实：调 `sns/jscode2session` |
| `resolvePhone(appId, phoneCode)` | 联调：`phoneCode` 直接当 11 位手机号；真实：调 `wxa/business/getuserphonenumber` |

真实通道带令牌缓存（提前 5 分钟过期）、`40001/42001` 清缓存重试一次；
未配置 `appSecret` 时明确返回 `CHANNEL_UNAVAILABLE`，不静默降级。

**`AuthService.login`** 支持一次完成两件事：

```
wx.login → code ─┐
                 ├→ 登录（openId 建档/取档）
getPhoneNumber → phoneCode ─┘→ 换手机号 → 写档 → 按手机号匹配员工 → 建会话
```

**新增 `POST /api/common/phone/wechat`**：已登录后补授权。绑定关系发生变化时撤销旧令牌并换发新令牌，
前端用返回的会话视图整包覆盖（避免上一轮"令牌未落盘"的问题复现）。

### 4.2 身份规则（按要求实现）

| 场景 | 结果 |
|---|---|
| 首次微信登录 | `roles = ["OWNER"]`，只有车主视角 |
| 提交代驾申请 | 仍只有 OWNER；审核通过后才出现 DRIVER |
| 手机号命中员工账号 | 自动写 `vi_employee_binding`（`bound_by=0` 表示系统绑定），`employeeBindSource=PHONE` |
| 命中多个员工账号 | **不绑定**，写 `EMPLOYEE_BIND_AMBIGUOUS` 事件，交后台人工处理 |
| 该员工已被别的微信绑定 | 撤销旧绑定并迁移到新账号，写 `EMPLOYEE_BINDING_MIGRATED`，保留旧账号的车主数据 |
| 员工未配检测点 | 有员工身份、无 STATION 视角，返回 `employeeStationCount=0` 供前端提示 |
| 后台配好检测点 | 重新打开小程序 / 刷新会话即出现 STATION 视角 |

### 4.3 员工与站点关系收归后台

- 小程序设置页：**删除**"申请凭证""粘贴绑定凭证"输入，改为只读状态 + 指引文案；
- 小程序角色页：检测视角未开通时，不再引导"去绑定"，而是说明"用公司登记手机号授权 / 联系管理员配站点"；
- 后台「站点授权」页：新增"新增绑定"对话框，支持按手机号/姓名/账号搜索员工、选检测点、填原因；
- 新增 `GET /api/admin/employee-candidates?keyword=`：返回脱敏手机号 + 已授权站点数。

> 凭证绑定接口在后端保留（`/employee-binding*`），作为异常情况的兜底手段，但小程序端不再暴露入口。

### 4.4 顺带修掉的缺陷

| 问题 | 影响 | 修复 |
|---|---|---|
| `StationService.listOrders` 在 MINI 上下文调 `DeptScope.scopedDeptId()` | 小程序站点工单列表 **500**（`获取用户ID异常`） | 小程序按已授权站点限定，不再套部门范围；后台入口仍按部门过滤 |
| `LoginReq` 无 unionId | 多端身份无法归一 | `jscode2session` 返回的 `unionid` 落库 |
| 短信绑定不触发员工匹配 | 两条手机号入口行为不一致 | `bindPhone` 同样走自动匹配 |

### 4.5 涉及文件

```
新增  xinyi-rongli/.../support/WechatMiniClient.java
改动  xinyi-rongli/.../service/AuthService.java       登录/换号/自动绑定/绑定来源
改动  xinyi-rongli/.../service/StationService.java    站点工单部门范围修复
改动  xinyi-rongli/.../service/AdminService.java      员工候选检索
改动  xinyi-rongli/.../dto/MiniDtos.java              phoneCode / WechatPhoneReq
改动  xinyi-rongli/.../mapper/AccountMapper.java      手机号查员工、绑定迁移、候选搜索
改动  xinyi-rongli/.../controller/CommonController.java       /phone/wechat
改动  xinyi-rongli/.../controller/admin/AdminDirectoryController.java  /employee-candidates
改动  xinyi-rongli-wx/src/pages/login/index.vue       手机号授权登录
改动  xinyi-rongli-wx/src/pages/settings/index.vue    授权手机号 + 员工身份只读说明
改动  xinyi-rongli-wx/src/pages/role/index.vue        未开通视角的引导文案
改动  xinyi-rongli-wx/src/api/index.ts                bindWechatPhone
改动  xinyi-ui/src/views/rongli/stationGrant/index.vue 新增绑定对话框
改动  xinyi-ui/src/api/rongli/index.ts                 listEmployeeCandidates
新增  logs/verify_phone_login.py   29 项
新增  logs/verify_driver_flow.py   37 步
新增  logs/audit_mini_flows.py     主链路实测探针
```

---

## 五、仍未完成 / 需要决策

| 编号 | 事项 | 现状 | 建议 |
|---|---|---|---|
| G01 | **真实微信支付** | `prepay` 只返回 `prepay_id=mock…`，仅 `mock-pay` 能改状态 | 上线前必须接 JSAPI 下单 + 支付回调验签 + 对账；当前只能联调演示 |
| G02 | 真实短信通道 | `devCode` 回显验证码 | 接供应商；同时给发送加频控 |
| G03 | 订阅消息模板 | `rongli.subscribe.template.*` 为空 → 只走站内信 | 在公众平台申请模板后填入 4 个场景的模板 ID |
| G04 | 站内交接的"双方确认" | 收车交接由检测员工发起并核验，司机侧无确认动作 | 若要求司机也确认，需把核验方从"具体账号"改为"角色+站点"，属安全模型调整，需产品确认 |
| G05 | `sys_user.phonenumber` 不唯一 | 命中多条时跳过自动绑定 | 建议在员工管理加手机号唯一校验，或在绑定页展示候选让人工选择 |
| G06 | 售后/申诉/对账端到端 | 接口冒烟通过，未做全链路回归 | 建议补 3 条端到端脚本 |

---

## 六、复跑方式

```bash
# 1) 后端（18080）需运行最新代码
python logs/verify_phone_login.py     # 29 项：授权登录、身份边界、后台配站点、绑定迁移
python logs/verify_driver_flow.py     # 37 步：资格→听单→派单→接单→行程→交接→检测→完成
python logs/audit_mini_flows.py       # 主链路探针（打印每步状态，不做断言）

# 2) 全量回归（含以上两项）
python logs/run_acceptance.py         # 输出 logs/evidence.json / evidence.md
```

### 真机联调前置

1. `.env` 里 `VITE_LOGIN_MODE=wechat`、`VITE_API_BASE` 指向可访问的后端；
2. 后端 `rongli.wechat.mock=false`，并配置 `appId` / `appSecret`；
3. 小程序后台把请求域名加入白名单，或开发者工具勾选"不校验合法域名"；
4. 手机号授权要求小程序已完成「获取手机号」能力开通（企业主体、已认证）。
