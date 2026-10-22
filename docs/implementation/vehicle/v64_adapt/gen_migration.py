#!/usr/bin/env python3
"""从 V6.4 001 权威 DDL 自动生成 xinyi 库的 schema 前向适配增量 migration。

不手抄 DDL：15 新表 CREATE 原样提取；4 表按精确 diff 生成 ADD COLUMN / DROP+ADD CHECK / ADD KEY。
产物：sql/migration_20261022_01_integration_config.sql（符合根 AGENTS 迁移脚本规范）。
可复现：重跑本脚本即重生成；权威来源为资料包 001_vi业务表_初始化_V6.4.sql。
"""
import re
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[4] / "docs/vehicle/V6.4"
SQL001 = BASE / "05_数据设计/001_vi业务表_初始化_V6.4.sql"
OUT = Path("sql/migration_20261022_01_integration_config.sql")

sql = SQL001.read_text(encoding="utf8")
blocks = {}
for m in re.finditer(r'(CREATE TABLE `?(vi_[a-z0-9_]+)`?\s*\(.*?\n\)\s*ENGINE[^;]*;)', sql, re.S):
    blocks[m.group(2)] = m.group(1)

NEW15 = ['vi_employee_directory_guard', 'vi_phone_verification_receipt', 'vi_station_grant_set',
         'vi_integration_profile', 'vi_integration_revision', 'vi_integration_secret',
         'vi_integration_check', 'vi_integration_publication', 'vi_integration_test_recipient',
         'vi_message_template', 'vi_message_template_revision', 'vi_notification_route',
         'vi_notification_delivery', 'vi_subscription_prompt', 'vi_offline_payment_record']

PLAN = {
    'vi_account': {
        'add_cols': ['phone_verified_at', 'phone_source', 'employee_auto_link_blocked'],
        'drop_check': ['ck_66_592d8ece9cae'],
        'add_check': ['ck_592d8ece9c', 'ck_87f78b0fed'], 'add_key': []},
    'vi_payment_order': {
        'add_cols': ['payment_method', 'receiver_dept_id', 'policy_revision_id', 'merchant_profile_id',
                     'merchant_revision_id', 'app_id', 'mchid', 'method_locked_at', 'capacity_resolution'],
        'drop_check': ['ck_66_a1f02dcffc3b'],
        'add_check': ['ck_3029b56e69d651', 'ck_af21e8f90ed6d9', 'ck_51d858e7957115', 'ck_pay_method_identity'],
        'add_key': []},
    'vi_refund_order': {
        'add_cols': ['payment_method', 'execution_mode'],
        'drop_check': [], 'add_check': ['ck_be46cf0b350473', 'ck_35feb49f221ec5'], 'add_key': []},
    'vi_employee_binding': {
        'add_cols': ['app_id', 'source', 'status', 'phone_lookup', 'last_phone_verified_at',
                     'employee_auth_version', 'auto_rebind_blocked', 'suspended_reason', 'revoked_by', 'revoked_reason'],
        'drop_check': [], 'add_check': ['ck_0322202dab', 'ck_3c0e2a701f', 'ck_employee_link_revoke',
                                        'ck_employee_link_source_actor'],
        'add_key': ['ix_employee_link_phone', 'ix_employee_link_block']},
}


def body(table):
    m = re.search(r'CREATE TABLE `?' + table + r'`?\s*\((.*?)\n\)\s*ENGINE', blocks[table], re.S)
    return m.group(1)


def line_for(table, pattern):
    for line in body(table).split('\n'):
        s = line.strip().rstrip(',')
        if re.match(pattern, s):
            return s
    raise ValueError(f"{table}: 未找到匹配 {pattern} 的定义行")


def col_def(table, col):
    return line_for(table, r'`' + col + r'`\s+')


def check_def(table, name):
    return line_for(table, r'CONSTRAINT\s+`?' + name + r'`?\s+CHECK')


def key_def(table, name):
    return line_for(table, r'(UNIQUE KEY|KEY)\s+`?' + name + r'`?\s+')


out = []
out.append("""-- =============================================================================
-- migration_20261022_01_integration_config.sql
-- 用途：将 xinyi 库 vi_ schema 从 V6.1(66 表) 前向适配到 V6.4「第三方接入配置版」(79 表)。
--       新增 15 表（12 第三方配置 + 员工目录锁 + 手机号证明回执 + 站点授权集合版本）；
--       4 表加列（vi_account/vi_employee_binding/vi_payment_order/vi_refund_order）；
--       扩展 vi_payment_order.status 枚举（新增 REVIEW_PENDING）；退役 2 表保留不 DROP。
-- 适用前置：已按 V6.1 执行 migration_20261021_vehicle_initial.sql（xinyi 库已有 66 张 vi_ 表）。
-- 执行顺序：单文件内按 A 新增表 → B~E 逐表 ALTER → F 退役标注；DDL 隐式提交、无全脚本事务回滚。
-- 备份要求：执行前必须 mysqldump 备份 xinyi（本次已备份至 .vi-local/backup/xinyi_pre_v64_*.sql）。
-- 可重复性：不可重复执行（CREATE TABLE / ADD COLUMN 无 IF NOT EXISTS，重复将报错，以此暴露误重跑）。
-- 失败恢复：从备份整库恢复；或按对象逆序 DROP 本次新增列/约束/索引/表（见文件尾回滚说明）。
-- 数据安全：不 DROP 任何既有表；退役表 vi_employee_binding_request/credential 保留历史、应用层停写；
--           4 张加列表当前均 0 行（新系统无业务数据），加列不回填、不猜测历史值；
--           若目标库这些表存在历史行，须先按 V6.4 前向适配规则查原微信资金身份/主体，
--           无法证明的记录生成异常清单并暂停其新资金动作，禁止用当前商户/默认管理员猜测回填。
-- 权威来源：资料包 V6.4 05_数据设计/001_vi业务表_初始化_V6.4.sql；本文件由
--           docs/implementation/vehicle/v64_adapt/gen_migration.py 自动提取生成（可复现）。
-- =============================================================================

-- ---------------------------------------------------------------------------
-- A. 新增 15 张表（V6.4 001 权威 DDL 原样）
-- ---------------------------------------------------------------------------""")

for t in NEW15:
    out.append(blocks[t])

labels = {'vi_account': 'B', 'vi_payment_order': 'C', 'vi_refund_order': 'D', 'vi_employee_binding': 'E'}
for t, p in PLAN.items():
    out.append(f"-- ---------------------------------------------------------------------------\n"
               f"-- {labels[t]}. {t}：加列 / 约束调整 / 索引（空表，前向新增，不 DROP 表）\n"
               f"-- ---------------------------------------------------------------------------")
    cols = ",\n  ".join("ADD COLUMN " + col_def(t, c) for c in p['add_cols'])
    out.append(f"ALTER TABLE `{t}`\n  {cols};")
    for c in p['drop_check']:
        out.append(f"-- 替换旧约束（V6.4 语义/命名变更）：先 DROP 旧 CHECK\nALTER TABLE `{t}` DROP CHECK `{c}`;")
    for c in p['add_check']:
        out.append(f"ALTER TABLE `{t}` ADD {check_def(t, c)};")
    if p['add_key']:
        keys = ",\n  ".join("ADD " + key_def(t, k) for k in p['add_key'])
        out.append(f"ALTER TABLE `{t}`\n  {keys};")

out.append("""-- ---------------------------------------------------------------------------
-- F. 退役表标注（V6.4 移除员工绑定申请/凭证链；保留历史，不 DROP，应用层停写旧接口）
--    vi_employee_binding_request / vi_employee_binding_credential：
--    新安装（完整初始化 ry_20260417.sql）不再创建这两张表；已存在的库保留其结构与历史数据，
--    旧 employee-binding/request/credential 写接口从主 OpenAPI 与任务移除、入口显式拒绝。
--    此处不改其结构，仅记录退役事实（表注释）。
-- ---------------------------------------------------------------------------
ALTER TABLE `vi_employee_binding_request`
  COMMENT = '[V6.4退役] 员工绑定申请链已废弃；保留历史不DROP，应用层停写，改用手机号自动关联';
ALTER TABLE `vi_employee_binding_credential`
  COMMENT = '[V6.4退役] 员工绑定凭证链已废弃；保留历史不DROP，应用层停写，改用后台直接预授站点';

-- =============================================================================
-- 执行后校验（人工/脚本核对，非本脚本自动执行）：
--   1) SELECT COUNT(*) FROM information_schema.tables
--        WHERE table_schema='xinyi' AND table_name LIKE 'vi\\_%';  -- 期望 79 + 2 退役 = 81
--   2) 15 新表逐一存在；4 表新列存在；vi_payment_order.status CHECK 含 REVIEW_PENDING；
--      vi_employee_binding 有 ix_employee_link_phone / ix_employee_link_block。
--   3) 与完整初始化 ry_20260417.sql 结果比对字段/索引/生成列/约束/排序规则一致（退役 2 表除外）。
--
-- 回滚说明（仅在需要撤销本次适配时，逆序手动执行；会丢失新增结构，不影响既有数据）：
--   ALTER TABLE vi_employee_binding DROP KEY ix_employee_link_block, DROP KEY ix_employee_link_phone;
--   ALTER TABLE <t> DROP CHECK <新增约束名>;  -- 逐表逐约束
--   ALTER TABLE vi_payment_order ADD CONSTRAINT ck_66_a1f02dcffc3b
--     CHECK (status IN ('PENDING','SUCCESS','CLOSED','UNKNOWN'));  -- 恢复旧枚举(须先无REVIEW_PENDING行)
--   ALTER TABLE vi_account ADD CONSTRAINT ck_66_592d8ece9cae CHECK (status IN ('ACTIVE','LOCKED'));
--   ALTER TABLE <t> DROP COLUMN <新增列>;  -- 逐表逐列
--   DROP TABLE <15 新表>;  -- 逆序
--   或直接从 .vi-local/backup/xinyi_pre_v64_*.sql 整库恢复。
-- =============================================================================""")

OUT.write_text("\n\n".join(out) + "\n", encoding="utf8")
print(f"生成 {OUT}：{len(out)} 段，15 新表 + 4 表 ALTER + 退役标注")
print(f"字节数：{OUT.stat().st_size}")
