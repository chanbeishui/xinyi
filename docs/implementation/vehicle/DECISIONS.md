# 当前工作区适配裁决

工程路径/基线以workspace.json为输入；不得自行降低安全边界。

## 起点裁决（E00）

- D-00 起点：用户未做 M0，当前为改版若依框架 `rongli-wx-home@18b5ce4`（用户确认与 `rongli-wx` 等同）。业务模块 CREATE_NEW（xinyi-rongli `com.xinyi.rongli` + xinyi-miniapp uni-app）；不重建 PC 框架与 sys_user 体系；无历史业务数据迁移（legacyViMigration=NOT_APPLICABLE）。
- D-00b 差异处理：HEAD 领先资料包观察点一个改名提交、AGENTS.md 因 install 追加受管区段、package.json 因改名变化——均如实记录，不回退、不改 baseline 指纹掩盖。

## 本轮评审残留项裁决（R1–R12，纳入 M0）

### E01 处理（契约/模型一致性，不依赖 Docker）

- R1 未绑定枚举列：输出全部未绑定枚举列实际清单，补齐取值、可空性与 CHECK/字典绑定。已知至少 12 列（含 `vi_inspection_result.conclusion` PASS/FAIL、`vi_dispatch_invite.source`、`vi_event_log.actor_realm`、`vi_aftersale_case.kind/return_state/refund_state`、`vi_aftersale_review.decision`、`vi_driver_fee_ledger.entry_kind`、`vi_session.active_role`、`vi_driver_statement_membership_change.action`、`vi_evidence_group.mileage_source`、`vi_inspection_result.source`）。
- R2 refund_state 语义裁决：判断 `vi_aftersale_case.refund_state` 与 `vi_refund_order.status`（ChannelRefundStatus）是否同义。**同义则统一为单一枚举；不同义则分开命名并写明映射表，不简单取并集**。当前矛盾：字典 RefundStatus 9 值含 CLOSED，列注释 8 值无 CLOSED。
- R3 写白名单分三层：①不可变历史表（event_log/snapshot/membership_change/price_change/aftersale_review 等）= insert-only，updateColumns 置空；②受控生命周期表（**session/token 必须保留角色切换、令牌消费、撤销、轮换等受控更新**，不得一律 insert-only）；③可编辑草稿表（statement/line 等）= 正常 update 白名单。逐表标注层级。
- R4 ViRecord 唯一来源：指定 `14_工程模板/backend/src/main/java/com/xinyi/rongli/core/ViRecord.java`（含 `Long id`）为唯一权威版；`10_技术架构_T3.1/参考代码/ViRecord.java`（缺 id）作废或标注为参考。自然键实体（occupancy/cursor/location/guard）单独映射，不套 ViRecord 的 id。
- R5 service-config 登记：`GET /api/public/service-config` 补录进接口全量清单.md（248→249）与权限与能力注册表.json operations，并登记 PUBLIC 授权（x-authorization PUBLIC_WHITELIST）。
- R6 共享端点四要素：handover/files/uploads/employee-binding 等 common 共享端点，按 MINI / 员工MINI / ADMIN 三类调用方分别明确「能力码 + 功能权限码 + 数据范围 + 状态要求」四要素，**不以任意一个条件满足就放行**；解决注册表 biz:* 与 yaml x-capability 双重登记歧义，写明服务端以何为准。
- R7 术语统一：契约统一用 `x-authorization.scope`，清除认证文档中 `x-data-scope` 漂移表述。
- R8 同步派生物：修改主模型/主契约后，同步更新 task_packets 片段、dto-types、订单四视角样本、枚举清单、contract_tests，不只追加说明文档。

### E03 处理（数据落地，依赖 E02 环境）

- R9 迁移脚本形态：将 `001_vi业务表_初始化_V6.0.sql` 改造为符合根 AGENTS 的 `sql/migration_YYYYMMDD_vi_init.sql`，头部写明用途/前置版本/执行顺序/备份要求/不可重复执行条件/失败恢复；同步把 66 表并入 `sql/ry_20260417.sql`；验证「增量脚本」与「完整初始化」两条路径最终结构一致。001 无 IF NOT EXISTS、DDL 隐式提交，须显式标注不可重复执行、不套 START TRANSACTION。
- R10 种子数据：补 sys_menu 菜单/按钮权限（来源=权限注册表 126 码）、sys_dict（vehicle_type/inspection_type 及各状态枚举）种子；**不硬造已有 sys ID、不默认授予全部员工权限**；确认 ry 中 vehicle_type 字典存在或补种子。
- R11 排序规则：不擅改原 sys 表排序规则；vi 用 utf8mb4_0900_ai_ci，vi↔sys 字符串列 join/比较按实际边界显式处理，防 Illegal mix of collations。
- R12 MySQL 版本与 UTC 探针：连库确认服务端 ≥8.0.22（CHECK 强制、表达式默认值）；真实探针验证 CHECK 拒非法值、生成列写拒绝、自然键唯一、UTC_TIMESTAMP(3) 默认值、乐观锁 version。

## 环境/工具链裁决

- D-ENV-1 Docker 不可用（exitCode 127）：E02 的 compose 隔离 MySQL8.4+Redis 路径不可用。**用户最终决定（2026-10-21，覆盖此前"本机独立库 xinyi_vi_dev"方案）：vi 表初装到现有 `xinyi` 库（与框架 sys_ 表同库，RuoYi 单数据源），Redis 复用现有 6379，不另建任何实例。** 已删除误建的 `xinyi_vi_dev` 库+`vi_dev` 用户、停止误起的 Redis 36379；用户原库/原 Redis 未受影响。实测 `xinyi` 库排序规则 utf8mb4_0900_ai_ci 与 vi 表一致→R11 collation 混用风险消除。MySQL 服务端 9.6.0（vs 目标 8.4）：满足 ≥8.0.22，M0 开发可用，生产 8.4 须复跑 SQL/CAS（RELEASE 门槛）。E03 在 `xinyi` 库增量建 66 张 vi_ 表（IF NOT EXISTS 防护、绝不动 sys_ 及其他既有表）。
- D-ENV-2 Maven 绑定 JDK25 与运行 JDK21 不一致：E04/E05 构建前统一（JAVA_HOME 指向 21 或配置 maven toolchain release17），避免 release17 编译落在 JDK25 上的未验证组合。
- D-ENV-3 Node v22.16.0 满足 uni-app 模板 engines，无需 nvm 切换、不放宽 engines。

## E04 裁决（工程基座构建引出）

- D-E04-1 微信 wx.request 不支持 PATCH：miniapp http.ts 用 POST + `X-HTTP-Method-Override: PATCH` 表达契约的 PATCH 端点（owner/profile、driver/listening 等）。**后端（xinyi-rongli/admin）须启用方法覆盖过滤器**识别该头还原为 PATCH，否则 @PatchMapping 路由不到；E05/E06 落实，或后续契约把 MINI 的 PATCH 改 PUT/POST（二选一，倾向后端加过滤器，改动小）。
- D-E04-2 backend-build 须激活 vi-it profile：admin 的 spring-boot-starter-test 仅在 vi-it profile，`mvn ... -DskipTests package` 不带 -Pvi-it 会因 viit 测试骨架编译失败。E04 用 `-Pvi-it` 构建成功。建议把 engineer.py 的 backend-build kind 补 -Pvi-it，或将 spring-boot-starter-test 移到 admin 主 test 依赖。
- D-E04-3 @dcloudio/types 对齐 3.4.31（uni-app peer 要求），非 --legacy-peer-deps 掩盖；package-lock.json 来自真实 npm install（8063 行）已入库，后续 npm ci。
- D-E04-4 微信 AppID = wx22f41349e671b362 配入 manifest.json mp-weixin（AppID 为公开标识，可入库）；DCloud uni统计 appid 未配（可选，构建告警可忽略）。
- D-E04-5 待修：ViHttpSuiteIT.java line24 路径 `docs/vehicle/V6.0/...` 应为 V6.1（模板残留），E07 绑真实 IT 前修正。
