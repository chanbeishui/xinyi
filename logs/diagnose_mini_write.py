#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""小程序提交数据未落库的逐层排查工具。

按「配置 → 连通 → 鉴权 → 写入 → 落库 → 环境」六层依次验证，
每层给出明确结论，定位到底断在哪一环。

用法：
    python logs/diagnose_mini_write.py                    # 按小程序 .env 里的地址排查
    python logs/diagnose_mini_write.py --base http://127.0.0.1:18080
    python logs/diagnose_mini_write.py --no-write         # 只查不写（不产生测试数据）
    python logs/diagnose_mini_write.py --keep             # 保留探针数据，方便去后台核对

设计原则：
  · 写入探针只加一条车辆 + 一条地址，默认跑完自动删掉，可反复执行
  · 每一步失败都直接给出「下一步该看哪里」，不做无意义的继续
  · 数据库核对走 mysql 客户端，不依赖额外 Python 包
"""
import argparse
import json
import os
import re
import shutil
import socket
import subprocess
import sys
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WX_DIR = os.path.join(ROOT, "xinyi-rongli-wx")

MYSQL_CANDIDATES = [
    r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe",
    r"C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe",
    "mysql",
]
DB = dict(host="127.0.0.1", port="3307", user="root", password="chenbeishui", schema="xinyi")

PROBE_PLATE = "粤K9D001"
PROBE_LABEL = "排查探针"

results = []   # (层, 项目, 是否通过, 说明)
warnings = []  # 不构成失败、但需要人注意的点


def record(layer, item, ok, detail=""):
    results.append((layer, item, ok, detail))
    mark = "OK  " if ok else "FAIL"
    print(f"[{mark}] {layer:<6} {item}" + (f"  → {detail}" if detail else ""))
    return ok


def warn(layer, item, detail):
    """提示项：不算失败，但要在汇总里单独列出来。"""
    warnings.append((layer, item, detail))
    print(f"[WARN] {layer:<6} {item}  → {detail}")


def section(title):
    print(f"\n{'=' * 72}\n{title}\n{'=' * 72}")


# ---------------------------------------------------------------- 环境准备

def find_mysql():
    for c in MYSQL_CANDIDATES:
        if c == "mysql":
            if shutil.which("mysql"):
                return "mysql"
            continue
        if os.path.exists(c):
            return c
    return None


def mysql_bin():
    exe = find_mysql()
    if not exe:
        return None
    return exe


def sql(query, schema=None):
    """执行 SQL，返回以 \t 分隔的行列表；失败返回 None。"""
    exe = mysql_bin()
    if not exe:
        return None
    cmd = [exe, f"-h{DB['host']}", f"-P{DB['port']}", f"-u{DB['user']}",
           f"-p{DB['password']}", "--default-character-set=utf8mb4", "-N", "-B",
           schema or DB["schema"], "-e", query]
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=25)
        if p.returncode != 0:
            return None
        return [ln for ln in p.stdout.strip().splitlines() if ln]
    except Exception:
        return None


def read_wx_env():
    """读取小程序 .env，拿到 API_BASE / 登录模式 / 登录 code。"""
    env = {}
    path = os.path.join(WX_DIR, ".env")
    if os.path.exists(path):
        for line in open(path, encoding="utf-8"):
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            env[k.strip()] = v.strip()
    cfg = os.path.join(WX_DIR, "src", "utils", "config.ts")
    default_base = "http://localhost:8080"
    if os.path.exists(cfg):
        m = re.search(r"\|\|\s*'(https?://[^']+)'", open(cfg, encoding="utf-8").read())
        if m:
            default_base = m.group(1)
    return {
        "api_base": env.get("VITE_API_BASE") or default_base,
        "login_mode": env.get("VITE_LOGIN_MODE") or "mock",
        "login_code": env.get("VITE_LOGIN_CODE") or "dev_owner_001",
        "env_file": path if os.path.exists(path) else "(无 .env，用 config.ts 默认值)",
    }


# ---------------------------------------------------------------- HTTP

def http(method, base, path, body=None, token=None, timeout=15):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["X-Mini-Token"] = token
    req = urllib.request.Request(base + path, data=data, headers=headers, method=method)
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))  # 绕过系统代理
    try:
        with opener.open(req, timeout=timeout) as r:
            raw = r.read().decode()
            return r.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:300]}
    except Exception as e:
        return -1, {"error": str(e)}


def probe_connect(host, port):
    """分别用 IPv4 / IPv6 建 TCP 连接，暴露 localhost 解析差异。"""
    out = {}
    for fam, name in ((socket.AF_INET, "IPv4"), (socket.AF_INET6, "IPv6")):
        s = socket.socket(fam, socket.SOCK_STREAM)
        s.settimeout(3)
        try:
            s.connect((host, port))
            out[name] = True
        except Exception:
            out[name] = False
        finally:
            s.close()
    return out


# ---------------------------------------------------------------- 主流程

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", help="后端根地址，默认取小程序 .env 的 VITE_API_BASE")
    ap.add_argument("--code", help="登录 code，默认取 VITE_LOGIN_CODE")
    ap.add_argument("--no-write", action="store_true", help="跳过写入探针，只做只读检查")
    ap.add_argument("--keep", action="store_true", help="保留探针数据")
    args = ap.parse_args()

    env = read_wx_env()
    base = (args.base or env["api_base"]).rstrip("/")
    code = args.code or env["login_code"]

    # ---------- 第 1 层：小程序侧配置 ----------
    section("第 1 层  小程序侧配置（请求到底发去哪）")
    record("配置", "VITE_API_BASE", True, base)
    record("配置", "环境变量来源", True, env["env_file"])
    if env["login_mode"] != "wechat":
        record("配置", "登录模式", True,
               f"mock —— 所有操作都会记到固定账号 {code} 名下；"
               f"若按自己的微信号/手机号查，必然查不到")
    else:
        record("配置", "登录模式", True, f"wechat —— 走真实 code，账号由 openid 决定（{code} 不生效）")

    m = re.match(r"https?://([^/:]+)(?::(\d+))?", base)
    host = m.group(1) if m else "localhost"
    port = int(m.group(2) or (443 if base.startswith("https") else 80))
    record("配置", "解析出的目标", True, f"host={host} port={port}")
    if host in ("localhost", "127.0.0.1", "::1"):
        record("配置", "回环地址提醒", True,
               "开发者工具模拟器里 localhost 指 PC，可用；"
               "但「真机调试」时 localhost 指手机自己，必然连不上 → 需换局域网 IP 或内网穿透")

    # ---------- 第 2 层：网络连通 ----------
    section("第 2 层  网络连通（请求能不能到达后端）")
    conn = probe_connect(host if host != "localhost" else "127.0.0.1", port)
    ipv4_ok, ipv6_ok = conn["IPv4"], conn["IPv6"]
    if host == "localhost":
        record("连通", "TCP localhost", ipv4_ok or ipv6_ok,
               f"IPv4={ipv4_ok} IPv6={ipv6_ok}")
    else:
        record("连通", f"TCP {host}:{port}", ipv4_ok or ipv6_ok,
               f"IPv4={ipv4_ok} IPv6={ipv6_ok}")

    if not (ipv4_ok or ipv6_ok):
        record("连通", "结论", False,
               f"{host}:{port} 没有进程监听 —— 后端没启动。"
               f"先在 IDEA 启动 XinyiApplication，或 java -jar xinyi-admin/target/xinyi-admin.jar --server.port={port}")
        # 顺带看看别的端口有没有活着的实例，避免用户白等
        alive = []
        for p in (8080, 18080):
            c = probe_connect("127.0.0.1", p)
            if c["IPv4"] or c["IPv6"]:
                alive.append(p)
        if alive:
            record("连通", "提示", True,
                   f"检测到其它端口在跑：{alive}，可临时用 --base http://127.0.0.1:{alive[0]} 复验")
        return finish()

    s, body = http("GET", base, "/api/public/service-config")
    record("连通", "HTTP 探活", s == 200, f"GET /api/public/service-config → {s}")
    if s != 200:
        record("连通", "结论", False, "端口通了但 HTTP 不通，可能是别的程序占用了该端口")
        return finish()

    # ---------- 第 3 层：鉴权 ----------
    section("第 3 层  鉴权（后端是否认这个身份）")
    s, login = http("POST", base, "/api/common/login/wechat",
                    {"code": code, "deviceLabel": "diagnose"})
    token = login.get("accessToken") if isinstance(login, dict) else None
    profile = login.get("profile") or {} if isinstance(login, dict) else {}
    account_id = profile.get("accountId")
    role = login.get("activeRole") if isinstance(login, dict) else None
    record("鉴权", "登录接口", s == 200 and bool(token),
           f"HTTP {s} accountId={account_id} activeRole={role} token={'有' if token else '无'}")
    if not token:
        record("鉴权", "结论", False,
               f"登录失败：{json.dumps(login, ensure_ascii=False)[:200]}")
        return finish()
    record("鉴权", "数据归属账号", True,
           f"本次提交会写到 vi_account.id={account_id}（{profile.get('nickname')}）；"
           f"核对数据时要用这个 id 查，不要按昵称/手机号猜")
    if role and role != "OWNER":
        record("鉴权", "当前角色", False,
               f"activeRole={role}，车主端接口可能被拒或数据记到别的身份下；"
               f"先在小程序里确认身份/角色是否切换过")

    # ---------- 第 4 层：写入接口 ----------
    section("第 4 层  写入接口（后端有没有真的执行 insert）")
    created_id = None
    if args.no_write:
        record("写入", "写入探针", True, "已跳过（--no-write）")
    else:
        # 先删掉可能残留的同名探针车辆，保证可重复执行
        s, vlist = http("GET", base, "/api/app/owner/vehicles", token=token)
        for v in (vlist if isinstance(vlist, list) else []):
            if v.get("plateNo") == PROBE_PLATE:
                http("DELETE", base, f"/api/app/owner/vehicles/{v.get('vehicleId')}", token=token)

        s, nv = http("POST", base, "/api/app/owner/vehicles",
                     {"plateNo": PROBE_PLATE, "vehicleTypeCode": "sedan",
                      "brandModel": PROBE_LABEL, "vinLast4": "D001",
                      "useType": "NON_OPERATING", "isDefault": False}, token)
        created_id = nv.get("vehicleId") if isinstance(nv, dict) else None
        ok = s in (200, 201) and created_id is not None
        record("写入", "POST /api/app/owner/vehicles", ok,
               f"HTTP {s} vehicleId={created_id}"
               + ("" if ok else f"  body={json.dumps(nv, ensure_ascii=False)[:200]}"))
        if not ok:
            record("写入", "结论", False,
                   "接口没返回 id。看后端控制台异常栈或 logs/sys-error.log；"
                   "常见是参数校验失败（车牌格式、必填项）、鉴权过期、唯一约束冲突")

    # ---------- 第 5 层：数据库落库 ----------
    section("第 5 层  数据库落库（数据到底在不在库里）")
    if mysql_bin() is None:
        record("落库", "MySQL 客户端", False, "未找到 mysql.exe，跳过落库核对")
    else:
        row = sql("SELECT COUNT(*) FROM vi_account;")
        record("落库", "连接目标库", row is not None,
               f"{DB['schema']}@{DB['host']}:{DB['port']}，vi_account 共 {row[0] if row else '?'} 条")

        if created_id:
            hit = sql(f"SELECT id, owner_account_id, plate_no, created_at FROM vi_vehicle "
                      f"WHERE id = {int(created_id)};")
            if hit:
                cols = hit[0].split("\t")
                record("落库", "探针车辆已入库", True,
                       f"id={cols[0]} owner_account_id={cols[1]} plate={cols[2]} at={cols[3]}")
                if account_id is not None and str(cols[1]) != str(account_id):
                    record("落库", "归属校验", False,
                           f"写入时 accountId={account_id}，库里是 {cols[1]} —— 说明中间有账号切换或数据串号")
            else:
                record("落库", "探针车辆已入库", False,
                       f"接口返回了 vehicleId={created_id} 但库里查不到："
                       f"事务回滚、写到了别的库、或连的不是这个实例")

        # 环境混淆：同名表在哪些库
        dup = sql("SELECT table_schema, COUNT(*) FROM information_schema.tables "
                  "WHERE table_name='vi_account' GROUP BY table_schema;", schema="information_schema")
        if dup:
            schemas = [ln.split("\t")[0] for ln in dup]
            if len(schemas) > 1:
                warn("落库", "存在多套 vi_ 表",
                     f"{schemas} —— 确认后端 spring.datasource.url 指向哪个库；"
                     f"「写在一个库、查另一个库」是多环境项目最常见的假故障")
            else:
                record("落库", "多库同名表", True, f"仅 {schemas[0]} 一套，无环境混淆")

    # ---------- 第 6 层：环境一致性 ----------
    section("第 6 层  环境一致性（有没有写到别的实例）")
    listening = []
    for p in (8080, 18080, 3000):
        c = probe_connect("127.0.0.1", p)
        if c["IPv4"] or c["IPv6"]:
            listening.append(p)
    record("环境", "存活的后端端口", True, str(listening) or "无")
    if mysql_bin():
        recent = sql("SELECT id, order_no, owner_account_id, created_at FROM vi_order "
                     "ORDER BY id DESC LIMIT 3;")
        if recent:
            for ln in recent:
                f = ln.split("\t")
                record("环境", "库内最近订单", True,
                       f"id={f[0]} no={f[1]} owner={f[2]} at={f[3]} (UTC，+8 为北京时间)")

    # ---------- 收尾 ----------
    if created_id and not args.keep:
        s, _ = http("DELETE", base, f"/api/app/owner/vehicles/{created_id}", token=token)
        record("清理", "删除探针车辆", s in (200, 204), f"HTTP {s}")
    return finish()


def finish():
    section("结论汇总")
    fails = [r for r in results if not r[2]]
    if not fails:
        print("全部通过：请求发出 → 后端接收 → 数据库落库 三层都正常。")
        print("若页面上仍看不到数据，问题在查询侧：确认按 owner_account_id 查，"
              "且连的是同一个库、同一条记录过滤器。")
    else:
        layer = fails[0][0]
        print(f"卡在【{layer}】层，共 {len(fails)} 项未通过：")
        for layer, item, _ok, detail in fails:
            print(f"  · {layer} / {item}：{detail}")
        print("\n按上面的顺序逐条修：先保证服务在监听，再确认账号，再确认落库。")
    if warnings:
        print("\n需要人工确认的提示：")
        for layer, item, detail in warnings:
            print(f"  · {layer} / {item}：{detail}")
    return 0 if not fails else 1


if __name__ == "__main__":
    sys.exit(main())
