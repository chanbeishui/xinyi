"""时间展示一致性回归（G07）。

只做一件事：证明后台接口的时间「形状统一、口径可换算、文案与原始值对得上」。

背景：业务表统一存 UTC 分量，历史上有两种输出形状——
  · ISO8601 带 Z（ViTime.iso 显式格式化）
  · 裸 LocalDateTime（MyBatis 行对象直接返回，Jackson 输出 2026-10-21T02:18:02.638）
浏览器把前者按 UTC、后者按本地时间解析，于是同一页面能差 8 小时。
本脚本把「只允许一种形状」固化成断言，防止以后再漏。

用法：python logs/verify_time_display.py [--base http://127.0.0.1:18080]
前置：后端已启动，admin/admin123 可登录
"""
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request
from datetime import datetime, timedelta, timezone

BASE = os.environ.get("BACKEND_VERIFY", "http://127.0.0.1:18080")
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "-N", "-B", "xinyi"]
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

UTC_ISO = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$")
NAIVE_ISO = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}")
LOCAL_TEXT = re.compile(r"^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$")
WINDOW_LABEL = re.compile(r"^\d{4}-\d{2}-\d{2} \d{2}:\d{2} - (\d{2}:\d{2}|\d{4}-\d{2}-\d{2} \d{2}:\d{2})$")

STEPS = []
FAILS = []


def sql(statement):
    out = subprocess.run([MYSQL] + DB + ["-e", statement], capture_output=True, text=True, encoding="utf-8")
    return [l for l in (out.stdout or "").strip().splitlines() if "Warning" not in l]


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:300]}


def check(name, ok, detail=""):
    STEPS.append((name, ok, detail))
    print(f"[{'OK' if ok else 'FAIL'}] {name}" + (f"  → {detail}" if detail else ""))
    if not ok:
        FAILS.append(name)
    return ok


def beijing(value):
    """与前端 formatDateTime 同口径：带 Z 或裸 ISO 都按 UTC，转 +08"""
    text = str(value).strip()
    moment = datetime.fromisoformat(text.replace("Z", "+00:00"))
    if moment.tzinfo is None:
        moment = moment.replace(tzinfo=timezone.utc)
    return (moment + timedelta(hours=8)).strftime("%Y-%m-%d %H:%M:%S")


def rows_of(body):
    if isinstance(body, dict):
        rows = body.get("rows")
        if isinstance(rows, list):
            return [r for r in rows if isinstance(r, dict)]
        return [body]
    return []


def scan_time_shape(label, body):
    """遍历响应里的所有时间串：只允许 UTC_ISO，或已是本地展示文本"""
    bad = []

    def walk(node, path=""):
        if isinstance(node, dict):
            for key, value in node.items():
                walk(value, f"{path}.{key}" if path else key)
        elif isinstance(node, list):
            for idx, value in enumerate(node):
                walk(value, f"{path}[{idx}]")
        elif isinstance(node, str) and NAIVE_ISO.match(node.strip()) and not UTC_ISO.match(node.strip()):
            bad.append(f"{path}={node}")

    walk(body)
    return check(f"{label} 时间形状统一（无裸 LocalDateTime）", not bad,
                 "全部 ISO8601 UTC" if not bad else f"发现 {len(bad)} 处缺 Z：{bad[:3]}")


def scan_key_shape(label, body):
    """目录类接口的键必须是驼峰，避免前端绑定 row.pickupWindow 永远拿不到值"""
    keys = set()
    for row in rows_of(body)[:5]:
        keys.update(row.keys())
    snake = [k for k in keys if "_" in k]
    return check(f"{label} 字段为驼峰", not snake,
                 "无下划线键" if not snake else f"仍有 {len(snake)} 个：{sorted(snake)[:5]}")


def main():
    print(f"后端：{BASE}\n")
    status, login = call("POST", "/login", {"username": "admin", "password": "admin123"})
    token = login.get("token") if isinstance(login, dict) else None
    if not check("后台登录", status == 200 and bool(token), f"HTTP {status}"):
        return finish()

    # 一、派单队列：截图出问题的页面，必须有窗口文案且与原始值一致
    status, queue = call("GET", "/api/admin/dispatch/queue", token=token)
    queue_rows = rows_of(queue)
    check("派单队列可读", status == 200, f"共 {len(queue_rows)} 条")
    scan_time_shape("派单队列", queue)
    if queue_rows:
        row = queue_rows[0]
        label = row.get("pickupWindowLabel")
        check("派单队列有上门时段文案", bool(label) and bool(WINDOW_LABEL.match(str(label))),
              f"pickupWindowLabel={label!r}")
        starts = row.get("pickupStartsAt")
        if starts and label:
            expect = beijing(starts)[:16]
            check("时段文案起点与 pickupStartsAt 一致", str(label).startswith(expect),
                  f"{label} 起点应为 {expect}")
        dispatch_at = row.get("dispatchAt")
        if dispatch_at:
            check("派单时间为北京时间文本", UTC_ISO.match(dispatch_at),
                  f"接口 {dispatch_at} → 展示 {beijing(dispatch_at)}")

    # 二、订单管理：门店列表曾经用的是 pickupWindow 这个不存在的字段
    status, orders = call("GET", "/api/admin/orders?pageNum=1&pageSize=5", token=token)
    scan_time_shape("订单列表", orders)
    checked = [r for r in rows_of(orders) if r.get("pickupStartsAt")]
    check("订单列表有时间字段", bool(checked), f"{len(checked)} 条含 pickupStartsAt")
    if checked:
        check("订单列表有上门时段文案", all(r.get("pickupWindowLabel") for r in checked),
              f"示例 {checked[0].get('pickupWindowLabel')!r}")

    # 三、档案类接口：曾经直接抛出下划线键 + 裸 LocalDateTime
    for label, path in (("车主列表", "/api/admin/owners?pageNum=1&pageSize=5"),
                        ("车辆列表", "/api/admin/vehicles?pageNum=1&pageSize=5"),
                        ("车主订单", "/api/admin/owners/1/orders?pageNum=1&pageSize=5"),
                        ("审计事件", "/api/admin/audit-events?pageNum=1&pageSize=5")):
        status, body = call("GET", path, token=token)
        if not check(f"{label}可读", status == 200, f"HTTP {status}"):
            continue
        scan_time_shape(label, body)
        scan_key_shape(label, body)

    # 四、口径锚点：order_no 内嵌北京时间，用它校对 created_at 的 UTC 分量
    lines = sql("SELECT order_no, DATE_FORMAT(created_at,'%Y-%m-%dT%H:%i:%s.000Z') "
                "FROM vi_order ORDER BY id DESC LIMIT 5;")
    mismatched = []
    for line in lines:
        order_no, created = line.split("\t")
        embedded = order_no[2:16]  # RL + yyyyMMddHHmmss
        display = beijing(created).replace("-", "").replace(" ", "").replace(":", "")
        if embedded != display[:14]:
            mismatched.append(f"{order_no}: 内嵌 {embedded} / created_at 展示 {display}")
    check("订单创建时间与单号内嵌时间一致", not mismatched,
          f"抽查 {len(lines)} 条全部一致（+08 换算后）" if not mismatched else mismatched[:2])

    # 五、前端换算自测：固定样本，防止以后有人把 +8 去掉或改成 -8
    sample = "2026-10-21T05:00:00.000Z"
    check("UTC 05:00 应展示为北京时间 13:00", beijing(sample) == "2026-10-21 13:00:00",
          f"{sample} → {beijing(sample)}")
    naive = "2026-10-21T02:18:02.638"
    check("裸 LocalDateTime 按 UTC 解释", beijing(naive) == "2026-10-21 10:18:02",
          f"{naive} → {beijing(naive)}")

    return finish()


def finish():
    print()
    if FAILS:
        print(f"未通过 {len(FAILS)} 项：{FAILS}")
        return 1
    print(f"全部 {len(STEPS)} 项通过：后台时间形状统一 ISO8601 UTC，展示统一为北京时间。")
    return 0


if __name__ == "__main__":
    for arg in sys.argv[1:]:
        if arg.startswith("--base"):
            BASE = arg.split("=", 1)[1] if "=" in arg else BASE
    sys.exit(main())
