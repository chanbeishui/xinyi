# 时间显示口径与修复说明

> 适用范围：后台管理端（`xinyi-ui`）、微信小程序（`xinyi-rongli-wx`）、荣利业务接口（`xinyi-rongli`）。
> 回归脚本：`python logs/verify_time_display.py`（已接入 `logs/run_acceptance.py`，套件代号 `STM`）

## 一、结论先说

后台页面原来把接口返回的 **UTC 原文**直接渲染出来，所以看到的是
`2026-10-21T05:00:00.000Z`；把它当作北京时间读，等于比真实时间早 8 小时。
库里存的数据本身是对的，坏的只是展示层。

以截图里那条订单为例：

| 位置 | 原样显示 | 实际含义 |
|---|---|---|
| 上门时段 | `2026-10-21T05:00:00.000Z` | 北京时间 **2026-10-21 13:00 - 15:00** |
| 派单时间 | `2026-10-21T04:30:00.000Z` | 北京时间 **2026-10-21 12:30**（上门前 30 分钟） |

## 二、根因：同一批接口里混着三种时间形状

| 形状 | 来源 | 浏览器如何理解 | 后果 |
|---|---|---|---|
| `2026-10-21T05:00:00.000Z` | `ViTime.iso()` 显式格式化 | 按 UTC 解析 | 渲染原文，看起来"怪"；若只做字符串截取则少 8 小时 |
| `2026-10-21T02:18:02.638` | MyBatis 行对象里的 `LocalDateTime` 被 Jackson 直接写出 | **按本地时间**解析（ES 规范：无偏移即本地） | 差 8 小时，且最容易误判（看着像正常 ISO） |
| `2026-10-21 09:13:53` | 若依 `BaseEntity` 的 `java.util.Date` + `@JsonFormat` | 本地时间 | 本身正确，不能再加 8 小时 |

前两种其实是同一个时刻的两种写法（库里统一存 UTC 分量），第三种是另一套体系（系统表存本地时间）。
三种混在同一个页面，任何"一刀切"的转换都会错一半 —— 这就是"数据来源不一致"的实质。

另外还有一层：部分档案类接口把 MyBatis Map **原样**抛出，键是下划线（`pickup_starts_at`），
而页面按驼峰取值（`pickupStartsAt`），于是整列空白 —— 看起来像"时间没取到"。
`/api/admin/owners`、`/api/admin/vehicles`、`/api/admin/owners/{id}/orders`、
`/api/admin/vehicles/{id}/inspection-results` 都属于这一类。

还有两处字段名对不上：
`订单管理`、`车主订单`、`车辆订单` 的"上门时段"列绑定的是 `pickupWindow`，
而接口给的是 `pickupStartsAt`，列永远是空的。

## 三、修正方案

### 1. 展示层：一个函数 + 一次全局归一（`xinyi-ui/src/utils/datetime.ts`）

- `formatDateTime(value, pattern)`：带 `Z`/偏移的按绝对时刻处理，**裸 ISO 按 UTC 处理**，
  已经是 `yyyy-MM-dd HH:mm:ss` 的视为本地文本原样返回（幂等）。
  计算方式是"先平移到 +08，再用 UTC 取值"，因此结果不依赖浏览器所在时区。
- `normalizeTimeStrings(body)`：递归改写响应体里 `yyyy-MM-ddT...` 形状的字符串。
  在 `src/utils/request.ts` 的成功分支统一调用，**所有页面、弹窗、详情一次性生效**，
  不需要逐个视图改模板。

这样：业务时间 → 北京时间文本；若依系统时间（带空格、无 T）→ 原样不动。

### 2. 接口层：只允许一种形状

- 档案类接口统一走 `AdminService.camelRows()/camelRow()`：键转驼峰、`LocalDateTime` 转
  `ViTime.iso()`（带 `Z`），并补 `pickupWindowLabel`、`fulfillmentStatusLabel`、`totalYuan` 等展示字段。
- 新增 `ViTime.windowLabel(start, end)`：同日输出 `2026-10-21 13:00 - 15:00`。
  派单队列、订单列表、车主/车辆订单都带上这个文案，前端不再自己拼时间。
- 保留原始 ISO 字段（`pickupStartsAt`/`dispatchAt` 等）供需要精确时刻的场景使用。

### 3. 顺带修掉的解析缺陷

`xinyi-ui/src/utils/ruoyi.ts` 的 `parseTime()` 原来会把 `-` 换成 `/`、再去掉 `.SSS`，
但 **保留了尾部的 Z**，得到 `2026/10/21 05:00:00Z` —— 各浏览器解释不一致。
现在遇到带偏移的字符串直接交给 `new Date()`，不再做字符串改写。

小程序侧 `toDate()` 本来就补 `Z`，这次补了"纯日期"的边界（`2026-10-21` → `2026-10-21T00:00:00Z`）。

## 四、约定（新增代码必须遵守）

1. 业务时间（`vi_*` 表）**只存 UTC 分量**，接口**只输出带 `Z` 的 ISO8601**，用 `ViTime.iso()`。
2. 需要给人看的时段，用 `*Label` 字段（`ViTime.shanghaiText()` / `ViTime.windowLabel()`），
   前端直接渲染，不再二次格式化。
3. 不要把 `LocalDateTime` 直接放进返回的 Map —— Jackson 会输出没有时区的字符串。
   新增接口若必须返回行对象，套 `camelRows()`/`camelRow()`。
4. 前端不要自己 `new Date()` 拼时间；统一用 `@/utils/datetime` 的 `formatDateTime`。
5. 若依系统表（`sys_*`）走 `java.util.Date`，本来就是本地时间，展示层不会再动它。

## 五、验证

```bash
# 只看时间口径（24 项）
python logs/verify_time_display.py

# 全量回归（13 套件 287 项）
BACKEND_DEV=http://127.0.0.1:18080 BACKEND_VERIFY=http://127.0.0.1:18080 python logs/run_acceptance.py
```

脚本断言的内容：

- 各接口返回的时间串**只有** `yyyy-MM-ddTHH:mm:ss.SSSZ` 一种形状（出现裸 ISO 即失败）；
- 档案类接口字段为驼峰（出现下划线键即失败）；
- `pickupWindowLabel` 的起点必须等于 `pickupStartsAt` 换算成北京时间后的值；
- 用 `order_no` 内嵌的 `yyyyMMddHHmmss` 反查 `created_at`：5 条抽样全部一致，证明 UTC 存储 + 8 小时换算正确；
- 固定样本 `2026-10-21T05:00:00.000Z` → `2026-10-21 13:00:00`，防止有人把 +8 写成 -8。

## 六、给使用者的操作提示

1. 后台前端需要重新构建/刷新（`npm run dev` 会热更新，生产包需 `npm run build:prod`）。
2. 小程序无需改动；如仍是旧的 `dist/dev` 产物，重新 `npm run dev:mp-weixin`。
3. 页面上的时间是**北京时间**；直接查库时 `created_at` 是 UTC，需要 `+8` 才是本地时间
   （或用 `CONVERT_TZ(created_at,'+00:00','+08:00')`）。
