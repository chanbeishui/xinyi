# 荣利 V6.1 核对报告复核与整改记录

- **复核日期**：2026-10-21
- **复核对象**：`doc/rongli-资料包V6.1功能核对报告.md`（下称"原报告"）
- **复核方式**：对原报告的每条断言回到代码/数据库/资料包原件验证；新增机器比对脚本 `logs/check_contract.py`（249 条契约 ↔ Spring 路由）
- **结论**：原报告的方向正确，但**接口缺口这一节有 5 处与代码不符**；本轮已修复 1 个真实缺口 + 3 个工程质量项，并给出剩余项的补全方案。

---

## 一、原报告断言的逐条复核结果

### 1.1 接口缺口（原报告第三节）——**6 条中 5 条不成立**

用 `logs/check_contract.py` 直接解析资料包 `04_接口/接口全量清单.csv` 与 11 个 Controller 的注解：

```
契约接口: 249 个；后端已注册路由: 260 条
完全匹配: 249
方法/路径不符: 0
未实现: 0
```

| # | 原报告断言 | 复核结果 | 证据 |
| --- | --- | --- | --- |
| 1 | PATCH `/api/app/owner/profile` 未实现、只做了 `POST /api/common/profile` | **不成立** | `OwnerController` 类级 `@RequestMapping("/api/app/owner")` + `@PatchMapping("/profile")`（第 51 行），契约路径与方法均精确匹配；`/api/common/profile` 的 POST 只是小程序兼容入口 |
| 2 | PATCH `/api/app/driver/listening` 未实现 | **不成立** | `DriverController` 类级 `/api/app/driver` + `@PatchMapping("/listening")`（第 165 行），另有 POST 兼容入口 |
| 3 | PATCH `/api/admin/vehicles/{vehicleId}` 未实现 | **不成立** | `AdminDirectoryController` 第 154 行 `@PatchMapping("/vehicles/{vehicleId}")`，按 `remark`/`verificationStatus` 更新 |
| 4 | PATCH `/api/admin/owners/{ownerId}` 未实现 | **不成立** | 同文件第 47 行，按 `serviceFlag`/`remark` 更新 |
| 5 | PATCH `/api/admin/drivers/{driverId}` 未实现 | **不成立** | 同文件第 94 行，按 `remark`/`newAssignmentEnabled` 更新 |
| 6 | GET `/api/admin/owners/{ownerId}/after-sales` 未实现 | **成立，已修复** | 原仅有 orders/vehicles 子列表 |

> 原报告为何误判：`AdminDirectoryController` 的 PATCH 端点权限标识分别是 `biz:owner:edit` / `biz:driver:edit` / `biz:vehicle:edit`，若只按 `biz:*:update` 检索会漏掉；另外 `OwnerController`/`DriverController` 的路径由类级前缀拼接，按绝对路径搜索也会漏。

### 1.2 机器比对脚本本身的两次误判（已修）

首版脚本报"5 条未实现"，其中 4 条是脚本缺陷，不是代码缺陷：

- `HandoverController` 用**类级** `@RequestMapping("/api/common/orders/{orderNo}/handovers")` 承载路径，方法上是**裸** `@PostMapping` —— 脚本只认带参注解，漏掉；
- `NotifyController` 的方法写了**绝对路径**（`@PostMapping("/api/notify/wechat/pay")`），脚本把同文件里另一处方法级 `@RequestMapping("/api/public/channel-health")` 当成了类前缀。

修正后仅剩 1 条真实缺口。

### 1.3 其余断言的复核

| 原报告断言 | 复核结果 | 说明 |
| --- | --- | --- |
| 66 张 vi_* 表全部建齐 | 成立 | 数据库 `information_schema` 实测 66 张，与资料包名称一致 |
| `sql/ry_20260417.sql` 未同步 vi 表 | 成立，**已修复** | 修复前该文件 0 处 `vi_`；现已内联 66 表 + 菜单 2100-2120 + 字典 |
| 后台无部门数据权限 | **成立，已部分修复** | 全模块 0 处 `@DataScope`；现已对后台订单列表/导出按部门范围过滤 |
| 外部付款查询返回密文 | 成立，**已修复** | `LedgerMapper` 两处 `SELECT p.*` 改为显式列，排除 `external_reference_cipher` |
| Q05 无资格到期复审 | 成立 | `vi_driver_profile` 与资格表**均无到期字段**，不只是缺接口，是缺数据模型 |
| D02 无多轮扩圈 | 成立 | `DispatchService` 仅用单一 `dispatchRadiusMeter()` 过滤；`round_key` 只是时间戳，未承载轮次语义 |
| N01 无订阅消息推送 | 成立 | `NotifyService` 无 subscribe 通道，仅站内消息 + outbox |
| Q03 播放为登记占位 | 成立 | 无播放器心跳，学时按前端上报累计 |
| 无 CI / 无任务台账 / 无隔离环境 | 成立 | 无 `.github/workflows`、无 `docs/implementation`、复用本机 3307/6379 |
| `.env` 已入库 | 成立，**已修复** | 已移出版本库，保留 `.env.example` |
| 微信登录/支付/短信/活体/OCR 全 mock | 成立 | `wechat.mock=true` 时 code 直接作 openId；非 mock 直接 503 |

---

## 二、本轮已完成的整改

### 2.1 补齐唯一真实缺失接口

`GET /api/admin/owners/{ownerId}/after-sales`（权限 `biz:aftersale:list`）

- `AfterSaleMapper.countByApplicant`（新增）+ 复用 `listByApplicant`
- `AdminService.ownerAfterSales`：先校验车主存在，再按申请人账号分页
- `AdminDirectoryController` 新增端点

验证：`GET /api/admin/owners/1/after-sales?pageNum=1&pageSize=5` → `200`，`total=3`。契约 **249/249 全匹配**。

### 2.2 修掉外部付款密文外泄

`LedgerMapper.listPayments` / `listPaymentsOfDriver` 原为 `SELECT p.*`，会把 `external_reference_cipher`（VARBINARY 密文）随响应返回；后者还是**小程序司机端**可调接口。

现改为显式列名：司机侧仅返回 `id/statement_id/amount_fen/paid_at/evidence_ids/status/verified_at/reason/reversal_of/created_at/statement_no`，后台侧另加 `payor_entity_id/registered_by/verified_by/statement_revision/version/driver_name`，两处都不含密文列。

### 2.3 后台部门数据权限（订单域）

- 新增 `support/DeptScope`：超管（`SecurityUtils.isAdmin()`）返回 `null` 表示不限制；其余账号取本部门，**未分配部门直接 403**，避免退化为全量可见
- `OrderMapper.adminList/adminCount` 增加 `deptId` 条件：
  `AND o.dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors))`
- 已接入：后台订单列表（`StationService.listOrders`）、脱敏导出（`AdminService` 导出任务生成）

验证：超管 `admin` 查询订单仍为 `total=18`（不受影响），说明超管旁路生效。

**尚未覆盖**：售后列表、统计概览、司机/车辆列表仍无部门过滤（这些表多数没有 `dept_id`，需按订单或站点反查，见第四节）。

### 2.4 初始化脚本同步（E03）

- 新增 `logs/build_init_sql.py`：以增量脚本为唯一事实来源，把 66 张表 + 菜单 + 字典内联进 `sql/ry_20260417.sql`（标记块 `@rongli-vi-begin/end`，可重复生成）
- **实测验证**：新建空库 `xinyi_init_check` 导入该脚本 → 66 张 vi 表、89 张总表全部建成；逐表比对列签名（名称/类型/可空/默认值/extra）与现网库**完全一致**（`diff` 无差异）

### 2.5 `.env` 移出版本库

- `.gitignore` 增加 `xinyi-rongli-wx/.env`，`git rm --cached` 停止跟踪
- 保留 `xinyi-rongli-wx/.env.example` 供新环境复制

---

## 三、我确实遗漏的既定流程（自我检讨）

资料包要求的 78 项任务里，我实际只完成了"写业务代码"这一条主线，**以下流程步骤被整体跳过**，这是原报告最有价值的部分：

1. **E00/E08 任务台账与证据链**：没有先产出 `docs/implementation/vehicle` 下的逐任务台账，也没有"完成=代码+真实测试+独立复核"的证据归档。交付说明是事后自述，不是过程证据。
2. **E02 隔离运行环境**：直接复用本机 MySQL(3307)/Redis，没有隔离实例与模拟供应商环境，导致"新人一条命令起环境"不可复现。
3. **T01-T08 真实集成测试**：资料包 24 个场景一个没跑，只写了自建 `logs/smoke_rongli.py`（覆盖主链路，但不含并发、负向授权、重启补偿、升级一致性）。
4. **298 项验收用例**：0 项按资料包口径执行，`06_验收/全量验收用例_V6.0.csv` 全为 `NOT_RUN`。
5. **L01-L03 供应商联调 / 真机审核 / 部署演练**：完全未做，微信登录目前一关 mock 就 503。
6. **UI 画板级还原**：287 个画板只合并成 26 个小程序页 + 20 个后台页，属"流程可用、视觉未还原"。

**判断**：功能主干可用，但按资料包 `completion_gate.py` 口径，本项目当前不具备"完成"资格，只能算"业务代码就绪、验收未开始"。

---

## 四、剩余缺口与补全方案（按优先级）

| 优先级 | 缺口 | 影响 | 补全方案 | 预估 |
| --- | --- | --- | --- | --- |
| P0 | 微信登录 jscode2session 未实现（`AuthService.resolveOpenId` 非 mock 直接 503） | 无法真机正式登录 | 接 `https://api.weixin.qq.com/sns/jscode2session`，加 appId/secret 配置、超时重试、错误码映射；openId 落 `vi_account` 唯一键 | 0.5 天 |
| P0 | 微信支付（商户号/证书/回调验签） | 无法收款 | 实现 prepay + 平台证书验签 + 金额与商户号一致性校验 + 查单补偿 | 3 天 |
| P0 | Q05 资格到期无数据模型 | 过期司机仍可被派单（TC-017 不通过） | 加 `qualification_expires_at` 字段（新迁移脚本）→ 派单候选池加 `expires_at > now` → 每日 `RongliJob` 扫描置 `EXPIRED` 并发站内信 | 1 天 |
| P1 | D02 无多轮扩圈 | 初始半径无人时直接转人工（TC-064 不通过） | `vi_dispatch_invite.round_no` + 配置 `radiusSteps[]`；每轮结束按 `(order, driver, round)` 去重后放大半径，最多 N 轮后转 `MANUAL_DISPATCH` | 1.5 天 |
| P1 | 部门数据权限只覆盖订单 | 售后/统计仍可跨部门查看 | `vi_aftersale_case` 按 `order_id` join `vi_order.dept_id`；统计/司机/车辆同法；详情与导出增加二次授权校验 | 1.5 天 |
| P1 | Q03 播放无心跳 | 学时可信度不足 | 播放会话表加心跳时间戳，服务端按墙钟校验相邻心跳间隔 ≤ 阈值，超时不算学时 | 1 天 |
| P2 | N01 无订阅消息 | 用户收不到微信通知 | 接 `subscribeMessage.send`，模板 ID 配置化，outbox 增加 `SUBSCRIBE` 通道与失败退避 | 1 天 |
| P2 | E02/E08 CI 与隔离环境 | 无法持续验证 | `docker-compose`（MySQL+Redis）+ `.github/workflows/backend.yml`（mvn verify + 前端构建 + 脚本自检）+ `docs/implementation/` 台账模板 | 1 天 |
| P2 | 298 项验收用例 | 无法证明完成 | 按 `06_验收` 的 Step 定义接控制平面，先跑 P0 用例（约 120 条），输出 `evidence.json` | 5 天 |
| P3 | UI 画板级还原 | 视觉与设计稿不一致 | 按 `02_UI` 逐页对齐，优先 D09-D23 履约页与支付 3 态 | 5 天+ |

---

## 五、当前可复现的验证命令

```bash
# 契约完整性（249/249）
python logs/check_contract.py

# Mapper SQL ↔ 表结构
python logs/check_mapper_schema.py

# 小程序接口 ↔ 后端路由
python logs/check_api_routes.py

# WXML 结构（开发者工具编译期才暴露的问题）
python logs/check_wxml.py

# 重新生成完整初始化脚本
python logs/build_init_sql.py

# 主链路冒烟（需后端 8080 + MySQL 3307 + Redis 6379）
python logs/smoke_rongli.py
```

**本轮实测结果**：契约 249/249；Mapper 13/13 一致；初始化脚本在空库导入后 66 张表结构与现网逐列一致；新增车主售后接口返回 `total=3`；导出/订单列表在超管下不受影响。

---

*本复核以静态比对 + 本地运行验证为准；真实供应商联调、并发与故障注入、298 项验收仍未执行，不能据此宣称项目"完成"。*
