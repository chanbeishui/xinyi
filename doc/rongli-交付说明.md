# 荣利机动车检测预约 · 交付说明

本文件说明「机动车检测预约」项目（分支 `rongli-wx`）的实现范围、启动步骤、功能模块清单与验收结果，对应资料包 `机动车检测预约_完整开发资料包_V6.1_框架起点版`。

---

## 一、交付概览

| 项 | 结果 |
| --- | --- |
| 后端模块 | 新增 `xinyi-rongli`（`com.xinyi.rongli`），18 个 Service、9 个 Controller、13 个 Mapper |
| 后端接口 | 266 条路由（去重路径 231 条），小程序端 115 个、管理端 127 个，两端路径与方法一致 |
| 数据库 | 业务表 66 张（`vi_*`），全部为增量脚本创建，未改动框架表结构 |
| 管理端页面 | 20 个页面（21 条菜单含目录），覆盖资料包 B01–B45 验收范围 |
| 小程序页面 | 27 个（车主 11、代驾 8、检测点 2、公共 6），覆盖 U/O/D/S 验收范围 |
| 主链路 | 端到端冒烟通过：报价 → 下单 → 支付 → 派单 → 接单 → 交接 → 留证 → 检测 → 取车 → 归还 → 售后 → 完成，时间线 21 个节点 |
| 一致性校验 | Mapper SQL ↔ 表结构 13/13 一致；后端路由 ↔ 两端前端接口 100% 匹配；20 项后台/小程序接口连通性全部通过 |

技术栈：Spring Boot 4.0.6 / JDK 21 / MyBatis / Druid / Redis / MySQL 8.0；前端 Vue 3.5 + Element Plus；小程序 uni-app（Vue 3 + TS + Vite）。

---

## 二、启动与初始化

### 1. 数据库（MySQL 127.0.0.1:3307，库 `xinyi`）

按顺序执行，脚本均可重复执行：

```bash
mysql -h127.0.0.1 -P3307 -uroot -p --default-character-set=utf8mb4 xinyi \
  < sql/migration_20261021_01_rongli_vi_business_tables.sql   # 66 张业务表
mysql ... xinyi < sql/migration_20261021_02_rongli_menu_dict_seed.sql   # 菜单 2100-2114、字典、检测点与计价种子
mysql ... xinyi < sql/seed_20261021_rongli_demo_accounts.sql            # 演示账号（仅联调 mock 模式）
mysql ... xinyi < sql/migration_20261021_03_rongli_admin_pages.sql      # 菜单 2115-2120、培训演示课程与考题
```

> 务必加 `--default-character-set=utf8mb4`，否则中文菜单名会因客户端字符集被截断报 `Data too long`。
> 顺序不可颠倒：`03` 依赖 `01` 的表与 `seed` 的演示账号。

### 2. 后端

```bash
# 复用现有 application-druid.yml（MySQL 3307 / Redis 6379），无需改连接配置
mvn -DskipTests package
java -jar xinyi-admin/target/xinyi-admin.jar --server.port=8080
```

若本机 shell 注入了非 8080 的端口环境变量，请显式追加 `--server.port=8080`。

### 3. 管理端

```bash
cd xinyi-ui && npm install && npm run dev     # http://localhost:3000
```

登录 `admin / admin123`，左侧「车辆检测预约」目录下为业务菜单。

### 4. 小程序

```bash
cd xinyi-rongli-wx && npm install
npm run dev:mp-weixin        # 开发
npm run build:mp-weixin      # 生产（产物 dist/build/mp-weixin）
```

用微信开发者工具导入 `dist/dev/mp-weixin`。联调模式下登录 `code` 直接使用下表演示账号的 `open_id`。

### 5. 演示账号

| 视角 | 登录 code | 说明 |
| --- | --- | --- |
| 车主 | `dev_owner_001` | 演示车主，可下单、支付、确认服务、申请售后 |
| 代驾 | `dev_driver_001` / `dev_driver_002` | 认证已通过、听单开启；派单游标在两人间轮转 |
| 检测点 | `dev_station_001` | 已绑 `sys_user` 并授权 1 号检测点 |

检测点视角依赖「员工绑定 + 站点授权」，纯 SQL 造不出：先在后台「治理与导出 → 员工账号绑定」签发 8 位凭证，再由该账号在小程序「设置」中输入完成绑定。

---

## 三、后端模块

| Service | 职责 |
| --- | --- |
| `AuthService` | 小程序登录、会话与角色切换、刷新令牌、手机号绑定、员工绑定 |
| `BookingService` | 车型/地址/时段、报价（按检测点距离分段计费）、下单与额度占用 |
| `OwnerOrderService` | 车主侧订单视图、时间线、服务确认、评价、争议 |
| `DispatchService` | 自动派单游标轮转、单邀约、超时过期、无候选转人工、立即派单 |
| `FulfillService` | 履约状态机、六类留证与里程、站点收车、检测结果、取车与归还交接 |
| `HandoverService`*（在 `FulfillService` 内） | 4 位交接码 + HMAC + AES-GCM、连错锁定、交付方/接收方不同人 |
| `StationService` | 检测点视图、行驶证登记核验、结果登记与更正、容量 |
| `MediaService` | 限定用途上传、服务端摘要核验、短期授权访问 |
| `AfterSaleService` | 取消/退款一次性审核、退款执行与重试、售后留言 |
| `LedgerService` | 代驾费用台账、对账单生成与版本、差异处理、外部付款登记与核实 |
| `PenaltyService` | 当日停单/跳过本轮限制、申诉提交与审核 |
| `TrainingService` | 培训课程版本、有效学时核算、考试与阅卷 |
| `AdminService` | 后台查询、配置、统计、治理（隐私/导出）、通知模板、outbox 重试 |
| `NotifyService` / `MessageService` | 站内消息、通知模板渲染、隐私请求 |
| `OutboxConsumer` / `EventService` | outbox 可靠投递、不可变事件流水 |
| `VehicleService` / `DriverQualificationService` | 车辆档案（含车型字典校验）、代驾资格与人脸会话 |

关键实现约定：

- 金额一律为整数「分」；时间以 `DATETIME(3)` 存 UTC，营业日按 Asia/Shanghai，接口输出 `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`。
- 状态迁移一律 CAS（`WHERE status = #{from}`），司机占用走唯一约束表 `vi_driver_occupancy`。
- 交接码只存 SHA-256 与密文，明文不下发；连续输错 3 次锁定。
- 留证必须「六类齐全」（车头/车尾/左侧/右侧/仪表盘/内饰）+ 仪表里程，缺项按中文列出。
- 派单时点 `dispatch_at = pickup_starts_at - 提前量(默认 30 分钟)`；后台支持「立即派单」跳过等待。

---

## 四、管理端功能（20 个页面）

| 页面 | 菜单路径 | 对应验收 | 主要能力 |
| --- | --- | --- | --- |
| 运营工作台 | `/rongli/dashboard` | B02 / B23 | 关键指标、8 项待办直达、履约分布、下单与金额趋势（ECharts）、逐日明细与统计口径说明 |
| 预约订单 | `/rongli/order` | B03/B04/B07/B08/B10/B11 | 全生命周期查看与操作、行驶证核验、检测结果登记、异常上报、时间线 |
| 派单调度 | `/rongli/dispatch` | B05/B06 | 派单队列、候选检索、人工邀约、**立即派单**、派单参数与邀约记录 |
| 车辆档案 | `/rongli/vehicle` | B09/B33 | 车辆列表与详情、车型字典校验、订单与检测结果回看 |
| 车主档案 | `/rongli/owner` | B27 | 车主详情、车辆与订单 |
| 代驾员 | `/rongli/driver` | B28 | 资格状态、听单/接单开关、承接记录、费用台账 |
| 检测点 | `/rongli/station` | B17/B19 | 检测点维护、营业时间与时段容量 |
| 计价规则 | `/rongli/price` | B20 | 版本化规则、提交审核、发布 |
| 代驾费用规则 | `/rongli/feeRule` | B39 | 报酬规则版本、试算、发布 |
| 取消与退款 | `/rongli/aftersale` | B13/B14 | 一次性审核、退款查询与重试、留言 |
| 代驾对账 | `/rongli/statement` | B37/B38/B40/B41/B42 | 对账单生成与版本、行内计价、差异处理、外部付款登记与核实 |
| 资格审核 | `/rongli/qualification` | B16 | 代驾资格申请审核 |
| 员工站点授权 | `/rongli/stationGrant` | B25/B26 | 员工跨站操作范围 |
| 业务配置 | `/rongli/config` | B24 | 派单、支付、确认、定位、条款参数 |
| 审计事件 | `/rongli/audit` | B22 | 不可变业务流水检索 |
| 异常与争议 | `/rongli/exception` | B15/B30/B31 | 服务争议、服务异常、结果更正审核、支付对账异常（含通道查询） |
| 处罚与申诉 | `/rongli/penalty` | B45 | 接单限制记录、申诉审核（通过即撤销限制） |
| 培训课程 | `/rongli/training` | B43/B44 | 课程版本、学时与及格分、题库编辑、发布 |
| 通知与任务 | `/rongli/notification` | B21/B35 | 7 个通知模板编辑、outbox 投递记录与手工重试 |
| 治理与导出 | `/rongli/governance` | B32/B34/B36 | 隐私请求处理、脱敏导出申请/审批/预览、员工绑定凭证签发与撤销 |

---

## 五、小程序功能（27 个页面）

**公共**：登录（微信 mock / 正式）、视角切换、消息中心、设置与隐私（手机号绑定、隐私请求、员工绑定、退出）。

**车主端（11）**：预约首页 → 选车 → 选时段 → 接车地址 → 确认报价下单 → 支付（MOCK/微信）→ 订单详情（时间线、留证、结果、交接码）→ 预约记录 → 售后与退款（列表/详情）→ 我的车辆与地址 → 我的。

**代驾端（8）**：听单工作台（听单开关、定位上报、在手任务）→ 派单邀约（接受/拒绝，超时失效）→ 接送任务（出发、到达、留证、交接、到站、取车、归还）→ 我的对账（对账单确认/差异）→ **收支流水**（对账明细与收款记录）→ **服务培训**（学时累计、考试、成绩）→ **处罚与申诉**（限制记录、提交申诉、进度）→ 认证中心与资格资料 → 我的。

**检测点端（2）**：检测工作台（订单列表、时段容量）→ 订单处理（收车、行驶证核对与**正副页拍照留档**、检测结果登记、放车交接、异常上报）。

本轮补齐的小程序能力：

1. `utils/request.ts` 现在能识别「HTTP 200 + `{code≥400,message}`」的业务错误（此前会被当成功），并兼容 `{msg}` 写法；文件上传完成参数改为后端要求的 `{size, sha256}`，已核验文件不再重复请求。
2. 新增处罚申诉、服务培训、收支流水三个页面并挂到「我的」；培训入口显示通过进度。
3. 检测点行驶证核对支持正页/副页拍照上传（`LICENSE_PHOTO`），保存时随核验结果一并提交。
4. 消息中心补上底部导航；司机「我的」处罚字段改为后端真实字段（`penaltyTypeLabel` / `statusLabel`）。

---

## 六、验收与自检

| 自检项 | 脚本 | 结果 |
| --- | --- | --- |
| 主链路端到端 | `logs/smoke_rongli.py` | 通过，21 个时间线节点，终态「已完成」，代驾费用台账写入 4000 分 |
| Mapper ↔ 表结构 | `logs/check_mapper_schema.py` | 13/13 一致 |
| 后端路由 ↔ 前端接口 | `logs/check_api_routes.py` | 266 / 115 / 127，完全一致 |
| 后台与小程序接口连通 | `logs/check_admin_pages.py` | 20/20 通过（统计、导出、课程题库、处罚申诉、通知、任务、隐私、绑定、四类异常、小程序培训/处罚/流水） |
| 构建 | `vite build` / `uni build -p mp-weixin` | 均通过 |

自检时若需免验证码调用管理端接口，临时执行 `UPDATE sys_config SET config_value='false' WHERE config_key='sys.account.captchaEnabled'` 并删除 Redis 键 `sys_config:sys.account.captchaEnabled`，验证后务必改回 `true`（本次已恢复）。

---

## 七、已知限制与后续建议

1. **支付通道**为 `MOCK`。切换真实微信支付需配置商户号、证书与回调地址（`rongli.payment.channel=WECHAT`），代码分支已预留。
2. **人脸核验**为 mock 会话（`mock-pass`），真实环境需接入第三方活体服务。
3. **培训视频**仅登记媒体占位，学时按「前台停留 + 服务端可核算时长」累计；接入真实播放器后建议加播放心跳。
4. **外部付款登记**接口 `SELECT p.*` 会把 `external_reference_cipher` 密文一并返回；建议后续显式列出列名，避免密文外泄到前端。
5. 对账单行内计价、差异处理等操作目前以表单 + 接口方式提供，未做批量勾选，数据量大时可加批量操作。
6. 后台 `dashboard` 趋势图依赖 `echarts`（已在依赖中），如后续做代码分割可改为按需引入。
