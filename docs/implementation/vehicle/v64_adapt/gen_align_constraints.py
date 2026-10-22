#!/usr/bin/env python3
"""生成 xinyi 库约束收敛到 V6.4 001 的对齐 migration（用户选定：对齐纯 V6.4 001）。

状态无关、可复现：直接对比两份 SQL 文件的 CHECK 约束集合——
  V6.1 基线 sql/migration_20261021_vehicle_initial.sql（含 E01 R1 补的语义名枚举 CHECK）
  V6.4 权威 <包>/05_数据设计/001_vi业务表_初始化_V6.4.sql
仅处理 migration_20261022_01 未覆盖的表（01 已处理 vi_account/vi_employee_binding/
vi_payment_order/vi_refund_order 的列与约束）；本脚本产出其余表的：
  DROP = V6.1 有、V6.4 无（E01 R1 多加、V6.4 改用注释+枚举字典+应用层校验的枚举 CHECK）
  ADD  = V6.4 有、V6.1 无（且非 V6.4 001 自身缺陷）
V6.4 001 自身缺陷（CHECK 引用不存在列，如 ck_ver_driver_statement_membership_change 之于
无 version 列的 append-only 历史表）自动排除并留痕，不生成不可能的 ADD。
"""
import re
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[4] / "docs/vehicle/V6.4"
SQL001 = BASE / "05_数据设计/001_vi业务表_初始化_V6.4.sql"
V61 = Path("sql/migration_20261021_vehicle_initial.sql")
OUT = Path("sql/migration_20261022_02_align_v64_constraints.sql")
HANDLED_BY_01 = {"vi_account", "vi_employee_binding", "vi_payment_order", "vi_refund_order"}
SQL_KEYWORDS = {'is', 'not', 'null', 'and', 'or', 'in', 'true', 'false', 'case', 'when', 'then',
                'else', 'end', 'between', 'exists', 'select', 'from', 'where', 'like', 'distinct',
                'count', 'sum', 'min', 'max', 'abs', 'coalesce', 'ifnull', 'nullif', 'char_length',
                'length', 'substring', 'date', 'timestamp', 'utc_timestamp', 'current_timestamp',
                'unknown', 'div', 'mod', 'xor', 'regexp', 'rlike'}


def parse(path):
    sql = path.read_text(encoding="utf8")
    tables = {}
    for m in re.finditer(r'CREATE TABLE `?(vi_[a-z0-9_]+)`?\s*\((.*?)\n\)\s*ENGINE', sql, re.S):
        t, body = m.group(1), m.group(2)
        cols, checks = set(), {}
        for line in body.split("\n"):
            s = line.strip().rstrip(",")
            cm = re.match(r'`([a-z0-9_]+)`\s+[A-Za-z]', s)
            if cm and not re.match(r'(PRIMARY|UNIQUE|KEY|CONSTRAINT|INDEX|FOREIGN)', s, re.I):
                cols.add(cm.group(1).lower())
            mc = re.match(r'CONSTRAINT\s+`?([a-z0-9_]+)`?\s+(CHECK\s*\((.*)\))\s*$', s, re.I)
            if mc:
                checks[mc.group(1)] = (mc.group(2), mc.group(3))
        tables[t] = {"cols": cols, "checks": checks}
    return tables


v61 = parse(V61)
v64 = parse(SQL001)

drop_ops, add_ops, invalid = [], [], []
for t in sorted(set(v61) & set(v64)):
    if t in HANDLED_BY_01:
        continue
    cols64 = v64[t]["cols"]
    # V6.4 001 自身缺陷：CHECK 引用不存在列 → 排除，不作为 ADD 目标
    valid64 = {}
    for name, (clause, inner) in v64[t]["checks"].items():
        nolit = re.sub(r"'[^']*'", "", inner)
        refs = {i.lower() for i in re.findall(r'\b([a-z_][a-z0-9_]*)\b', nolit, re.I)} - SQL_KEYWORDS
        if refs - cols64:
            invalid.append((t, name, sorted(refs - cols64)))
        else:
            valid64[name] = clause
    for name in sorted(set(v61[t]["checks"]) - set(valid64)):
        # V6.1 有、V6.4 无（含被排除的无效约束不在此列）→ DROP
        if name in v64[t]["checks"]:
            continue  # V6.4 有此名（仅被判无效），不 DROP（避免误删）；实际无效约束本就不该建
        drop_ops.append((t, name))
    for name in sorted(set(valid64) - set(v61[t]["checks"])):
        add_ops.append((t, f"ADD CONSTRAINT {name} {valid64[name]}"))

lines = ["""-- =============================================================================
-- migration_20261022_02_align_v64_constraints.sql
-- 用途：将 xinyi 库 vi_ 表 CHECK 约束收敛到 V6.4 001 权威结构（用户选定“对齐纯 V6.4 001”）。
--   仅处理 migration_20261022_01 未覆盖的表；01 已处理 vi_account/vi_employee_binding/
--   vi_payment_order/vi_refund_order 的列与约束变更。
--   多余约束（E01 R1 给 V6.1 补、V6.4 001 未采用的语义名枚举 CHECK）→ DROP；
--   这些枚举改由应用层（E06 ViErrorCode + 业务 Service）+ 枚举字典 + COMMENT 约束；
--   actor_realm 等按 ADR-02 为服务端受控写入。R1 分析价值保留在 E01 记录，DB 约束以 V6.4 为准。
-- 前置：已执行 migration_20261022_01_integration_config.sql。
-- 备份：执行前须 mysqldump（已备份 .vi-local/backup/xinyi_pre_v64_*.sql）。
-- 可重复性：不可重复执行（DROP 具名约束，重复报错暴露误重跑）。
-- 数据安全：仅 DROP CHECK 约束，不改数据、不 DROP 表；相关 vi_ 表当前 0 行，无数据风险。
-- 权威来源：V6.4 001；由 v64_adapt/gen_align_constraints.py 对 V6.1↔V6.4 两份 SQL 静态 diff 生成（可复现）。
-- ============================================================================="""]
for t, name in drop_ops:
    lines.append(f"ALTER TABLE `{t}` DROP CHECK `{name}`;")
for t, frag in add_ops:
    lines.append(f"ALTER TABLE `{t}` {frag};")
if invalid:
    lines.append("-- ---------------------------------------------------------------------------")
    lines.append("-- V6.4 001 自身缺陷（引用不存在的列，无法建立；已在 ry 完整初始化与本库同步剔除，")
    lines.append("--   不作为 ADD 目标，也不误删同名既有约束；详见 E03 V6.4 前向适配报告 finding）：")
    for t, name, miss in invalid:
        lines.append(f"--   {t}.{name} 引用不存在列 {miss}")
lines.append("-- 执行后校验：python docs/implementation/vehicle/v64_adapt/verify_schema.py 应输出 STRUCTURE_CONSISTENT。")
OUT.write_text("\n".join(lines) + "\n", encoding="utf8")
print(f"生成 {OUT}")
print(f"DROP {len(drop_ops)} 个；ADD {len(add_ops)} 个；排除 V6.4 001 无效 CHECK {len(invalid)} 个")
print("DROP（V6.1有V6.4无的 R1 枚举 CHECK）:")
for t, name in drop_ops:
    print(f"  {t}.{name}")
for t, name, miss in invalid:
    print(f"  [V6.4缺陷排除] {t}.{name} 引用不存在列 {miss}")
