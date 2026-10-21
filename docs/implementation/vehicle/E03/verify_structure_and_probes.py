#!/usr/bin/env python3
"""E03 · xinyi 库 vi_ 表结构一致性核验 + 6 项真实 MySQL 探针。
凭据从环境变量 VI_ROOT_USER/VI_ROOT_PASS 读取（不硬编码、不打印）。探针用事务 ROLLBACK，不在工作库留数据。
替代 verify_mysql.py（其在 MySQL9.6 输出格式下解包报错）。"""
import json, os, sys, datetime
import pymysql

DB = os.environ.get("VI_DB_NAME", "xinyi")
conn = pymysql.connect(host="127.0.0.1", port=3306, user=os.environ["VI_ROOT_USER"],
                       password=os.environ["VI_ROOT_PASS"], database=DB, charset="utf8mb4",
                       autocommit=False, cursorclass=pymysql.cursors.DictCursor)
R = "docs/vehicle/V6.1/05_数据设计/"
model = json.load(open(R + "data_model_V6.0.json"))
wl = {x["table"]: x for x in json.load(open(R + "生成列与写入白名单.json"))}
report = {"database": DB, "checkedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(),
          "structure": {}, "probes": [], "serverVersion": None}

def q(sql, args=None):
    with conn.cursor() as c:
        c.execute(sql, args or ()); return c.fetchall()

report["serverVersion"] = q("SELECT VERSION() v")[0]["v"]

# ---- 结构一致性 ----
actual_tables = {r["TABLE_NAME"] for r in q(
    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=%s AND TABLE_NAME LIKE 'vi\\_%%'", (DB,))}
target_tables = {t["name"] for t in model}
report["structure"]["actualViTables"] = len(actual_tables)
report["structure"]["targetTables"] = len(target_tables)
report["structure"]["missing"] = sorted(target_tables - actual_tables)
report["structure"]["unexpected"] = sorted(actual_tables - target_tables)

col_mismatch, pk_mismatch, gen_mismatch, check_missing = [], [], [], []
for t in model:
    tn = t["name"]
    if tn not in actual_tables: continue
    cols = {r["COLUMN_NAME"]: r for r in q(
        "SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_KEY,EXTRA,COLUMN_DEFAULT,GENERATION_EXPRESSION "
        "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s", (DB, tn))}
    # 列集合
    model_cols = {c[0] for c in t["cols"]}
    if set(cols) != model_cols:
        col_mismatch.append({"table": tn, "onlyActual": sorted(set(cols) - model_cols), "onlyModel": sorted(model_cols - set(cols))})
    # 主键
    pk_actual = sorted(r["COLUMN_NAME"] for r in q(
        "SELECT COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s AND CONSTRAINT_NAME='PRIMARY' ORDER BY ORDINAL_POSITION", (DB, tn)))
    if pk_actual != list(t["primaryKey"]):
        pk_mismatch.append({"table": tn, "actual": pk_actual, "model": t["primaryKey"]})
    # 生成列（仅 STORED/VIRTUAL GENERATED；排除表达式默认值的 DEFAULT_GENERATED）
    gen_actual = {c for c, v in cols.items() if ("STORED GENERATED" in (v["EXTRA"] or "") or "VIRTUAL GENERATED" in (v["EXTRA"] or ""))}
    gen_model = set(wl.get(tn, {}).get("generatedColumns", []))
    if gen_actual != gen_model:
        gen_mismatch.append({"table": tn, "actual": sorted(gen_actual), "model": sorted(gen_model)})
    # CHECK 约束存在性
    ck = q("SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s AND CONSTRAINT_TYPE='CHECK'", (DB, tn))
    if len(t.get("checks", [])) and len(ck) == 0:
        check_missing.append(tn)
report["structure"]["columnMismatch"] = col_mismatch
report["structure"]["pkMismatch"] = pk_mismatch
report["structure"]["generatedMismatch"] = gen_mismatch
report["structure"]["tablesWithChecksButNoneInDB"] = check_missing
report["structure"]["totalChecksInDB"] = q(
    "SELECT COUNT(*) n FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=%s AND CONSTRAINT_TYPE='CHECK' AND TABLE_NAME LIKE 'vi\\_%%'", (DB,))[0]["n"]

# ---- 6 探针（事务 ROLLBACK，不留数据）----
def probe(name, fn):
    try:
        ok, detail = fn(); report["probes"].append({"probe": name, "pass": bool(ok), "detail": detail})
    except Exception as e:
        report["probes"].append({"probe": name, "pass": False, "detail": f"{type(e).__name__}: {e}"})
    finally:
        conn.rollback()

def p_check_reject():
    try:
        with conn.cursor() as c:
            c.execute("INSERT INTO vi_account (app_id,open_id,status) VALUES ('probe_app','probe_open','INVALID_STATUS')")
        return False, "非法枚举未被 CHECK 拒绝"
    except pymysql.err.OperationalError as e:
        return (e.args[0] == 3819), f"errno={e.args[0]} (期望3819 CHECK 违反)"

def p_generated_write_reject():
    try:
        with conn.cursor() as c:
            c.execute("INSERT INTO vi_aftersale_case (order_id,payment_id,kind,requested_fen,return_state,refund_state,applicant_type,applicant_id,reason,active_order) "
                      "VALUES (1,1,'CANCEL_REFUND',0,'UNDETERMINED','NONE','ACCOUNT',1,'x',999)")
        return False, "生成列 active_order 被手动写入未被拒绝"
    except pymysql.err.OperationalError as e:
        return (e.args[0] == 3105), f"errno={e.args[0]} (期望3105 生成列不可写)"

def p_natural_key_unique():
    with conn.cursor() as c:
        c.execute("INSERT INTO vi_dispatch_cursor (scope_key) VALUES ('probe_scope')")
        try:
            c.execute("INSERT INTO vi_dispatch_cursor (scope_key) VALUES ('probe_scope')")
            return False, "自然键重复插入未被拒绝"
        except pymysql.err.IntegrityError as e:
            return (e.args[0] == 1062), f"errno={e.args[0]} (期望1062 主键/唯一冲突)"

def p_unique_constraint():
    with conn.cursor() as c:
        c.execute("INSERT INTO vi_account (app_id,open_id,status) VALUES ('probe_app2','probe_open2','ACTIVE')")
        try:
            c.execute("INSERT INTO vi_account (app_id,open_id,status) VALUES ('probe_app2','probe_open2','ACTIVE')")
            return False, "uk_app_open 重复未被拒绝"
        except pymysql.err.IntegrityError as e:
            return (e.args[0] == 1062), f"errno={e.args[0]} (期望1062 唯一约束)"

def p_utc_default():
    with conn.cursor() as c:
        c.execute("INSERT INTO vi_dispatch_cursor (scope_key) VALUES ('probe_utc')")
        c.execute("SELECT created_at, UTC_TIMESTAMP(3) u, @@session.time_zone tz FROM vi_dispatch_cursor WHERE scope_key='probe_utc'")
        r = c.fetchone()
        ca, u = r["created_at"], r["u"]
        diff = abs((ca - u).total_seconds())
        return (diff < 5), f"created_at={ca} vs UTC_TIMESTAMP={u} 差{diff:.2f}s, session_tz={r['tz']} (默认应为UTC,差<5s)"

def p_version_default():
    with conn.cursor() as c:
        c.execute("INSERT INTO vi_dispatch_cursor (scope_key) VALUES ('probe_ver')")
        c.execute("SELECT version FROM vi_dispatch_cursor WHERE scope_key='probe_ver'")
        v = c.fetchone()["version"]
        return (v == 0), f"乐观锁 version 默认={v} (期望0)"

probe("CHECK拒绝非法枚举", p_check_reject)
probe("生成列禁止手动写入", p_generated_write_reject)
probe("自然键唯一(主键)", p_natural_key_unique)
probe("唯一约束uk_app_open", p_unique_constraint)
probe("UTC默认值(不依赖连接时区)", p_utc_default)
probe("乐观锁version默认0", p_version_default)
conn.rollback()

# 汇总
struct_ok = (not report["structure"]["missing"] and not report["structure"]["unexpected"]
             and not col_mismatch and not pk_mismatch and not gen_mismatch and not check_missing)
report["structurePass"] = struct_ok
report["probesPass"] = all(p["pass"] for p in report["probes"])
report["overallPass"] = struct_ok and report["probesPass"]
conn.close()
out = "docs/implementation/vehicle/E03/structure_and_probes.json"
os.makedirs(os.path.dirname(out), exist_ok=True)
json.dump(report, open(out, "w", encoding="utf8"), ensure_ascii=False, indent=2, default=str)
print(json.dumps({"serverVersion": report["serverVersion"], "viTables": report["structure"]["actualViTables"],
                  "missing": report["structure"]["missing"], "unexpected": report["structure"]["unexpected"],
                  "colMismatch": len(col_mismatch), "pkMismatch": len(pk_mismatch), "genMismatch": len(gen_mismatch),
                  "checksInDB": report["structure"]["totalChecksInDB"],
                  "structurePass": struct_ok, "probes": [(p["probe"], p["pass"]) for p in report["probes"]],
                  "overallPass": report["overallPass"]}, ensure_ascii=False, indent=1))
