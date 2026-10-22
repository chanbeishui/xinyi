#!/usr/bin/env python3
"""V6.4 schema 前向适配结构一致性校验。

比对 xinyi 库（增量迁移后）实际 vi_ 表结构 vs V6.4 001 权威 DDL（= ry 完整初始化的 vi_ 段）：
字段集合、CHECK 约束集合、主键、生成列。证明“增量结果 == 标准初始化结果”（根 AGENTS 要求）。
退役 2 表（employee_binding_request/credential）在 xinyi 保留但不在 001，单独确认存在。
只读校验，不改库。
"""
import os
import re
import sys
from pathlib import Path

import pymysql

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[4] / "docs/vehicle/V6.4"
SQL001 = BASE / "05_数据设计/001_vi业务表_初始化_V6.4.sql"
DB = os.environ.get("VI_DB_NAME", "xinyi")

# CHECK 子句中可能出现的 SQL 关键字/函数（非列名），用于识别“引用不存在列”的无效约束
SQL_KEYWORDS = {'is', 'not', 'null', 'and', 'or', 'in', 'true', 'false', 'case', 'when', 'then',
                'else', 'end', 'between', 'exists', 'select', 'from', 'where', 'like', 'distinct',
                'count', 'sum', 'min', 'max', 'abs', 'coalesce', 'ifnull', 'nullif', 'char_length',
                'length', 'substring', 'date', 'timestamp', 'utc_timestamp', 'current_timestamp',
                'unknown', 'div', 'mod', 'xor', 'regexp', 'rlike'}

sql = SQL001.read_text(encoding="utf8")
expected = {}
invalid_checks = []
for m in re.finditer(r'CREATE TABLE `?(vi_[a-z0-9_]+)`?\s*\((.*?)\n\)\s*ENGINE', sql, re.S):
    name, body = m.group(1), m.group(2)
    cols, checks, pk, gens = set(), set(), [], set()
    for line in body.split("\n"):
        s = line.strip().rstrip(",")
        if not s:
            continue
        cm = re.match(r'`([a-z0-9_]+)`\s+(.*)', s)
        ckm = re.match(r'CONSTRAINT\s+`?([a-z0-9_]+)`?\s+CHECK', s, re.I)
        pkm = re.match(r'PRIMARY KEY\s*\(([^)]*)\)', s, re.I)
        if ckm:
            checks.add(ckm.group(1))
        elif pkm:
            pk = [c.strip().strip("`") for c in pkm.group(1).split(",")]
        elif cm and not re.match(r'(PRIMARY|UNIQUE|KEY|CONSTRAINT|INDEX|FOREIGN)', s, re.I):
            cols.add(cm.group(1))
            if re.search(r'\bGENERATED\b|\bAS\s*\(', cm.group(2), re.I):
                gens.add(cm.group(1))
    # 剔除 V6.4 001 自身缺陷：CHECK 引用不存在列（无法建立，不纳入期望）
    valid = set()
    for line in body.split("\n"):
        s = line.strip().rstrip(",")
        mc = re.match(r'CONSTRAINT\s+`?([a-z0-9_]+)`?\s+CHECK\s*\((.*)\)\s*$', s, re.I)
        if not mc:
            continue
        nolit = re.sub(r"'[^']*'", "", mc.group(2))
        refs = {i.lower() for i in re.findall(r'\b([a-z_][a-z0-9_]*)\b', nolit, re.I)}
        refs -= SQL_KEYWORDS
        if refs - {c.lower() for c in cols}:
            invalid_checks.append((name, mc.group(1), sorted(refs - {c.lower() for c in cols})))
        else:
            valid.add(mc.group(1))
    checks = checks & valid
    expected[name] = {"cols": cols, "checks": checks, "pk": pk, "gens": gens}

conn = pymysql.connect(host="127.0.0.1", port=3306, user="root",
                       password=os.environ["VI_ROOT_PASS"], database=DB, charset="utf8mb4")
cur = conn.cursor()
report = {"db": DB, "expectedTables": len(expected), "mismatches": [], "retiredPresent": [],
          "v64_001_invalidChecks": [f"{t}.{n} 引用不存在列 {miss}" for t, n, miss in invalid_checks]}

for t, exp in expected.items():
    cur.execute("SELECT COLUMN_NAME,EXTRA FROM information_schema.columns "
                "WHERE table_schema=%s AND table_name=%s", (DB, t))
    rows = cur.fetchall()
    if not rows:
        report["mismatches"].append({"table": t, "issue": "TABLE_MISSING"})
        continue
    actual_cols = {r[0] for r in rows}
    actual_gens = {r[0] for r in rows if "GENERATED" in (r[1] or "").upper()}
    cur.execute("SELECT CONSTRAINT_NAME FROM information_schema.check_constraints "
                "WHERE constraint_schema=%s", (DB,))
    # 限定本表 CHECK：用 table_constraints 关联
    cur.execute("SELECT cc.CONSTRAINT_NAME FROM information_schema.check_constraints cc "
                "JOIN information_schema.table_constraints tc ON cc.CONSTRAINT_NAME=tc.CONSTRAINT_NAME "
                "AND cc.CONSTRAINT_SCHEMA=tc.CONSTRAINT_SCHEMA "
                "WHERE tc.table_schema=%s AND tc.table_name=%s AND tc.constraint_type='CHECK'", (DB, t))
    actual_checks = {r[0] for r in cur.fetchall()}
    cur.execute("SELECT COLUMN_NAME FROM information_schema.statistics "
                "WHERE table_schema=%s AND table_name=%s AND index_name='PRIMARY' ORDER BY seq_in_index", (DB, t))
    actual_pk = [r[0] for r in cur.fetchall()]

    miss_c = exp["cols"] - actual_cols
    extra_c = actual_cols - exp["cols"]
    miss_ck = exp["checks"] - actual_checks
    miss_g = exp["gens"] - actual_gens
    if miss_c or extra_c or miss_ck or exp["pk"] != actual_pk or miss_g:
        report["mismatches"].append({"table": t, "missingCols": sorted(miss_c),
                                     "extraCols": sorted(extra_c), "missingChecks": sorted(miss_ck),
                                     "missingGens": sorted(miss_g),
                                     "pkExpected": exp["pk"], "pkActual": actual_pk})

for t in ("vi_employee_binding_request", "vi_employee_binding_credential"):
    cur.execute("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=%s AND table_name=%s", (DB, t))
    if cur.fetchone()[0]:
        report["retiredPresent"].append(t)

conn.close()
import json
print(json.dumps(report, ensure_ascii=False, indent=2))
ok = not report["mismatches"] and len(report["retiredPresent"]) == 2
print("STRUCTURE_CONSISTENT" if ok else "STRUCTURE_MISMATCH", file=sys.stderr)
sys.exit(0 if ok else 1)
