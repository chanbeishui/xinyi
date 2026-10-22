# M0 → V6.4 schema 前向适配报告（E03 V6.4 范围）

执行者：Qoder-Agent。目标库：现有 `xinyi`（用户决定，非隔离库）。权威 DDL 来源：V6.4 `05_数据设计/001_vi业务表_初始化_V6.4.sql`。

## 背景
用户提供 V6.4「第三方接入配置版」(T3.4)。复核发现 vi_ schema 从 V6.1 的 66 表变为 V6.4 的 79 表，且 4 表加列、多处约束变化、2 表退役。按 V6.4「已有工作区前向适配」规则（独立增量 migration、逐对象断言、禁 DROP、同步完整初始化、比较结构一致）执行。

## 差异全景（V6.4 001 vs E03 的 V6.1 66 表）
- **新增 15 表**：12 第三方配置（vi_integration_profile/revision/secret/check/publication、vi_message_template(+revision)、vi_notification_route/delivery、vi_subscription_prompt、vi_offline_payment_record、vi_integration_test_recipient）+ 3 身份安全表（vi_employee_directory_guard 单例目录锁、vi_phone_verification_receipt 手机号证明回执、vi_station_grant_set 站点授权集合版本）。
- **4 表加列**（前向新增，无删改）：vi_account +3、vi_employee_binding +10、vi_payment_order +9、vi_refund_order +2。
- **约束变化**：vi_payment_order.status 扩 `REVIEW_PENDING`（先 DROP 旧 CHECK 再 ADD 新，符合"新增 PaymentStatus 值的 CHECK 先修改"）；vi_account.status CHECK 统一到 V6.4 命名；vi_employee_binding +4 CHECK +2 索引。
- **退役 2 表**：vi_employee_binding_request/credential（员工绑定申请/凭证链）——xinyi **保留不 DROP**、加表注释标注停用、应用层停写旧接口；完整初始化 ry **不再创建**（新安装不建）。

## 执行（均先 mysqldump 备份 xinyi → .vi-local/backup/xinyi_pre_v64_*.sql）
1. `migration_20261022_01_integration_config.sql`：新增 15 表（001 原样）+ 4 表 ADD COLUMN/约束/索引。应用到 xinyi，exit 0。
2. `migration_20261022_02_align_v64_constraints.sql`：用户选定"对齐纯 V6.4 001"——DROP 15 个 E01 R1 给 V6.1 补、V6.4 001 未采用的语义名枚举 CHECK（actor_realm/active_role/currency/aftersale·inspection 枚举等；这些枚举改由应用层 E06 ViErrorCode + 业务 Service + 枚举字典 + COMMENT 约束，actor_realm 等按 ADR-02 服务端受控写入）。应用到 xinyi，exit 0。
3. `ry_20260417.sql` 同步：sys_/gen 段（23 表 + 267 种子 INSERT）原样保留，vi_ 段替换为 V6.4 的 79 表（由 `v64_adapt/sync_ry.py` 从 001 生成）。

## 一致性校验（根 AGENTS 要求：增量结果 == 完整初始化结果）
`v64_adapt/verify_schema.py`（比对字段/CHECK/主键/生成列 vs V6.4 001）：
- **xinyi（增量）**：79 表 **0 mismatch**；退役 2 表保留。→ `STRUCTURE_CONSISTENT`
- **ry 完整初始化**（scratch 库 xinyi_vi_it_rycheck，用 ry 全量建后校验，已清理）：ry 干净应用 exit 0，79 vi 表 **0 mismatch**，不含退役表（完整初始化正确排除）。→ `STRUCTURE_CONSISTENT`
- 两者唯一差异 = xinyi 保留 2 退役表（既有库前向适配规则），符合预期。

## ⚠ Finding：V6.4 001 自身缺陷（非本项目引入）
`vi_driver_statement_membership_change`（append-only 不可变历史表，仅 created_at、**无 version 列**）在 V6.4 001 中被误加 `CONSTRAINT ck_ver_driver_statement_membership_change CHECK (version >= 0)`，引用不存在的 `version` 列——**原样执行 001 建此表必失败**（MySQL 1054 Unknown column 'version'）。全量扫描 001 确认**仅此 1 处**无效 CHECK。
- 处理：ry 完整初始化与 xinyi 增量库**均不建此无效约束**（该表本无 version，移除是正确的，非加列）；`gen_align_constraints.py`/`verify_schema.py` 自动识别"CHECK 引用不存在列"并排除、留痕。
- 建议：反馈资料包维护方修正 001（该表应无 ck_ver，或按其它不可变历史表口径核对）。

## 可复现
- `v64_adapt/gen_migration.py` → 生成 migration_01（从 001 提取 15 表 + 4 表加列/约束/索引）
- `v64_adapt/gen_align_constraints.py` → 生成 migration_02（V6.1↔V6.4 两份 SQL 静态 diff，排除 01 已处理表与无效 CHECK）
- `v64_adapt/sync_ry.py` → 同步 ry 的 vi_ 段到 001
- `v64_adapt/verify_schema.py` → 结构一致性校验（xinyi 或任意 VI_DB_NAME）

## 数据安全
不 DROP 任何既有表；退役 2 表保留；4 加列表与各 vi_ 表当前 0 行（新系统无业务数据），加列/DROP CHECK 无数据风险、不回填猜测值；全程先备份。业务库 xinyi 仅按 V6.4 前向适配增量变更。
