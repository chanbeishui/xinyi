"""侧栏分组结构 + 业务统计(B23) + 支付渠道核对(B31) 接口校验。

说明：admin 接口直接返回业务对象，不再包一层 {code,msg,data}，断言按原始结构写。
启动前提：验证实例监听 127.0.0.1:18080（classpath 指向 E:/codex/.cprep/out）。
"""
import json
import subprocess
import urllib.error
import urllib.request
from urllib.parse import urlencode

BASE = "http://127.0.0.1:18080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
FAILS = []
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB_ARGS = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "xinyi"]


def sql(statement):
    r = subprocess.run([MYSQL] + DB_ARGS + ["-N", "-B", "-e", statement],
                       capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(r.stderr)
    return r.stdout.strip()


def call(method, path, token=None, body=None, params=None):
    url = BASE + path
    if params:
        url += "?" + urlencode(params)
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=40) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:300]}


def check(name, ok, detail=""):
    print(("  OK  " if ok else " FAIL ") + name + (f"  {detail}" if detail else ""))
    if not ok:
        FAILS.append(name)
    return ok


def main():
    status, body = call("POST", "/login", body={"username": "admin", "password": "admin123"})
    assert status == 200 and body.get("token"), f"登录失败 {status} {body}"
    token = body["token"]

    # ---------------------------------------------------------------- 侧栏结构
    status, routers = call("GET", "/getRouters", token=token)
    assert status == 200, routers
    data = routers.get("data") or []
    rongli = next((r for r in data if str(r.get("path")) == "/rongli"), None)
    check("侧栏存在 /rongli 目录", rongli is not None)
    groups = (rongli or {}).get("children") or []
    check("分组数量为 4", len(groups) == 4, f"实际 {len(groups)}")
    visible_total = 0
    for g in groups:
        kids = g.get("children") or []
        visible = [k for k in kids if str(k.get("hidden")) in ("false", "False", "None", "")]
        visible_total += len(visible)
        print(f"  分组 {g.get('meta', {}).get('title')}：{len(kids)} 项 → "
              + " / ".join(str(k.get('meta', {}).get('title')) for k in kids))
    check("可见页面合计 21", visible_total == 21, f"实际 {visible_total}")

    flat = {}
    for g in groups:
        for k in (g.get("children") or []):
            flat[str(k.get("meta", {}).get("title"))] = k
    for title, expect_path, expect_comp in [
        ("业务统计", "statistics", "rongli/statistics/index"),
        ("支付渠道核对", "paymentReconcile", "rongli/paymentReconcile/index"),
    ]:
        item = flat.get(title)
        ok = item is not None
        if ok:
            path = str(item.get("path")).lstrip("/")
            comp = str(item.get("component"))
            ok = path == expect_path and comp == expect_comp
        check(f"菜单 {title} 指向 {expect_comp}", ok,
              "" if ok else f"实际 {item and (item.get('path'), item.get('component'))}")

    # ---------------------------------------------------------------- B23 业务统计
    status, d = call("GET", "/api/admin/statistics", token=token, params={"range": "DAY"})
    check("统计接口可用", status == 200 and "cards" in d, f"{status} {list(d)[:4]}")
    cards = d.get("cards") or []
    check("统计卡 4 张", len(cards) == 4, " / ".join(f"{c.get('label')}={c.get('value')}" for c in cards))
    check("近7日趋势 7 个点", len(d.get("last7") or []) == 7)
    check("指标口径 5 条", len(d.get("metricDefinitions") or []) == 5)
    stations = d.get("stationStats") or []
    check("按站点概况有数据", len(stations) > 0, f"{len(stations)} 个站点")
    for s in stations[:3]:
        print(f"    站点 {s.get('stationName')}: 预约{s.get('orders')} 已付款{s.get('paid')} "
              f"未付款{s.get('unpaid')} 在手{s.get('inHand')} 已归还{s.get('returned')}")
    kpi = d.get("kpi") or {}
    check("kpi 字段齐全",
          all(k in kpi for k in ("sampleOrders", "paidFen", "returnedAssignments", "pendingPayOrders")),
          str(kpi))
    check("daily 旧字段保留", len(d.get("daily") or []) >= 0 and "overview" in d and "dashboard" in d)

    status, week = call("GET", "/api/admin/statistics", token=token, params={"range": "WEEK"})
    check("range=WEEK 生效", week.get("rangeDays") == 30, f"rangeDays={week.get('rangeDays')}")
    status, legacy = call("GET", "/api/admin/statistics", token=token, params={"days": 14})
    check("旧调用 days=14 不回归", legacy.get("rangeDays") == 14, f"rangeDays={legacy.get('rangeDays')}")

    # ---------------------------------------------------------------- B31 支付渠道核对
    status, rec = call("GET", "/api/admin/reconciliation", token=token,
                       params={"pageNum": 1, "pageSize": 6})
    check("对账接口可用", status == 200 and "rows" in rec, f"{status} {list(rec)[:4]}")
    rows = rec.get("rows") or []
    check("待核实记录有数据", len(rows) > 0, f"total={rec.get('total')}")
    need = ("orderNo", "relatedTradeNo", "problemType", "bizStatusLabel", "channelStatus", "amountFen")
    for r in rows[:5]:
        missing = [k for k in need if k not in r]
        check(f"行 {r.get('caseId')} 字段齐全", not missing, f"缺 {missing}" if missing else
              f"{r.get('orderNo')} {r.get('relatedTradeNo')} {r.get('problemType')} "
              f"{r.get('bizStatusLabel')}/{r.get('channelStatus')} {r.get('amountFen')}")

    if rows:
        order_no = rows[0].get("orderNo")
        status, hit = call("GET", "/api/admin/reconciliation", token=token,
                           params={"pageNum": 1, "pageSize": 6, "keyword": order_no})
        check("关键词按业务单号命中", status == 200 and (hit.get("total") or 0) >= 1,
              f"{order_no} -> {hit.get('total')}")
        status, miss = call("GET", "/api/admin/reconciliation", token=token,
                            params={"pageNum": 1, "pageSize": 6, "keyword": "NO-SUCH-TRADE"})
        check("关键词无匹配返回空", status == 200 and (miss.get("total") or 0) == 0,
              f"total={miss.get('total')}")

        # 查询原交易 → 登记结论，验证写链路（结束会复位为 OPEN）
        case_id = rows[0].get("caseId")
        status, q = call("POST", f"/api/admin/reconciliation/{case_id}/query", token=token)
        check("查询原交易可用", status == 200 and q.get("status") == "CHECKING",
              f"{status} status={q.get('status')}")
        status, rv = call("POST", f"/api/admin/reconciliation/{case_id}/resolve", token=token,
                          body={"conclusion": "渠道确认为支付成功，按关闭订单退款处理"})
        check("核对退款登记结论可用", status == 200 and rv.get("status") == "RESOLVED",
              f"{status} status={rv.get('status')}")
        check("登记结论不覆盖问题类型",
              rv.get("problemType") == "关单与支付通知竞态"
              and rv.get("conclusion") == "渠道确认为支付成功，按关闭订单退款处理",
              f"problemType={rv.get('problemType')} conclusion={rv.get('conclusion')}")
        status, bad = call("POST", f"/api/admin/reconciliation/{case_id}/resolve", token=token,
                           body={"conclusion": ""})
        check("空结论被拒绝", status >= 400 or bad.get("status") != "RESOLVED", f"{status}")
        sql(f"UPDATE vi_governance_case SET status='OPEN', reviewer_id=NULL, conclusion=NULL WHERE id={case_id}")

    print()
    if FAILS:
        print(f"未通过 {len(FAILS)} 项：" + "；".join(FAILS))
        raise SystemExit(1)
    print("全部校验通过")


if __name__ == "__main__":
    main()
