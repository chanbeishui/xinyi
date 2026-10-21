"""后台补齐页面接口连通性校验。

覆盖：运营统计、导出任务列表、培训课程与题库、处罚与申诉、通知模板、
隐私案件、任务重试列表，以及小程序端培训/处罚/流水接口的字段契约。
"""
import json
import urllib.request
import urllib.error
from urllib.parse import urlencode

BASE = "http://127.0.0.1:8080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def call(method, path, token=None, body=None, params=None, mini=None):
    url = BASE + path
    if params:
        url += "?" + urlencode(params)
    data = None
    headers = {"Content-Type": "application/json"}
    if body is not None:
        data = json.dumps(body).encode()
    if token:
        headers["Authorization"] = "Bearer " + token
    if mini:
        headers["X-Mini-Token"] = mini
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:200]}


def admin_login():
    status, body = call("POST", "/login", body={"username": "admin", "password": "admin123"})
    assert status == 200 and body.get("token"), f"后台登录失败: {status} {body}"
    return body["token"]


def mini_login(open_id):
    status, body = call("POST", "/api/common/login/wechat", body={"code": open_id})
    assert status == 200 and body.get("accessToken"), f"小程序登录失败 {open_id}: {status} {body}"
    return body["accessToken"]


def check(name, ok, detail=""):
    print(("  OK  " if ok else " FAIL ") + name + (f"  {detail}" if detail else ""))
    return ok


def main():
    results = []
    token = admin_login()

    # 1) 运营工作台 / 统计
    s, b = call("GET", "/api/admin/statistics", token, params={"days": 14})
    ok = s == 200 and "overview" in b and "dashboard" in b and "daily" in b
    results.append(check("统计概览 /api/admin/statistics", ok, f"http={s} daily={len(b.get('daily') or [])}"))

    # 2) 导出任务列表（本轮新增）
    s, b = call("GET", "/api/admin/exports", token, params={"pageNum": 1, "pageSize": 10})
    results.append(check("导出任务列表 /api/admin/exports", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))

    # 3) 培训课程 + 题库
    s, b = call("GET", "/api/admin/training-courses", token, params={"pageNum": 1, "pageSize": 20})
    rows = (b or {}).get("rows") or []
    course = next((r for r in rows if r.get("courseKey") == "SERVICE_BASIC"), None)
    results.append(check("培训课程列表 /api/admin/training-courses", s == 200 and course is not None,
                         f"http={s} total={b.get('total')}"))
    demo_questions = []
    if course:
        s, detail = call("GET", f"/api/admin/training-courses/{course['trainingId']}", token)
        demo_questions = (detail or {}).get("questions") or []
        opts_ok = bool(demo_questions) and isinstance(demo_questions[0].get("options")[0], dict) \
            and "code" in demo_questions[0]["options"][0]
        results.append(check("课程题库详情（选项为 {code,text}）", s == 200 and opts_ok,
                             f"http={s} questions={len(demo_questions)}"))

    # 4) 处罚与申诉
    s, b = call("GET", "/api/admin/penalties", token, params={"pageNum": 1, "pageSize": 10})
    results.append(check("处罚记录 /api/admin/penalties", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))
    s, b = call("GET", "/api/admin/penalty-appeals", token, params={"pageNum": 1, "pageSize": 10})
    results.append(check("申诉列表 /api/admin/penalty-appeals", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))

    # 5) 通知模板与任务
    s, b = call("GET", "/api/admin/notification-templates", token)
    results.append(check("通知模板 /api/admin/notification-templates", s == 200 and (b.get("total") or 0) > 0,
                         f"http={s} total={b.get('total')}"))
    s, b = call("GET", "/api/admin/jobs", token, params={"pageNum": 1, "pageSize": 20})
    results.append(check("补偿任务 /api/admin/jobs", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))

    # 6) 治理：隐私、绑定
    s, b = call("GET", "/api/admin/privacy-cases", token, params={"pageNum": 1, "pageSize": 10})
    results.append(check("隐私案件 /api/admin/privacy-cases", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))
    s, b = call("GET", "/api/admin/employee-binding-requests", token, params={"pageNum": 1, "pageSize": 10})
    results.append(check("员工绑定申请 /api/admin/employee-binding-requests", s == 200 and "rows" in b,
                         f"http={s} total={b.get('total')}"))

    # 7) 异常与争议四类列表
    for name, path in [
        ("服务争议", "/api/admin/disputes"),
        ("服务异常", "/api/admin/exceptions"),
        ("结果更正", "/api/admin/result-corrections"),
        ("支付对账异常", "/api/admin/reconciliation"),
    ]:
        s, b = call("GET", path, token, params={"pageNum": 1, "pageSize": 10})
        results.append(check(f"{name} {path}", s == 200 and "rows" in b, f"http={s} total={b.get('total')}"))

    # 8) 小程序端：培训、处罚、流水
    mini = mini_login("dev_driver_001")
    s, b = call("GET", "/api/app/driver/qualification/training", mini=mini)
    rows = (b or {}).get("rows") or []
    results.append(check("小程序培训列表 /api/app/driver/qualification/training", s == 200 and len(rows) > 0,
                         f"http={s} rows={len(rows)}"))
    if rows:
        tid = rows[0]["trainingId"]
        s, detail = call("GET", f"/api/app/driver/qualification/training/{tid}", mini=mini)
        results.append(check("小程序课程详情", s == 200 and detail.get("trainingId") == tid,
                             f"http={s} questionCount={detail.get('questionCount')}"))
    s, b = call("GET", "/api/app/driver/penalties", mini=mini)
    results.append(check("小程序处罚列表 /api/app/driver/penalties", s == 200 and "rows" in b, f"http={s}"))
    s, b = call("GET", "/api/app/driver/penalty-appeals", mini=mini)
    results.append(check("小程序申诉列表 /api/app/driver/penalty-appeals", s == 200 and "rows" in b, f"http={s}"))
    s, b = call("GET", "/api/app/driver/reconciliation-entries", mini=mini)
    results.append(check("小程序对账明细 /api/app/driver/reconciliation-entries", s == 200 and "rows" in b,
                         f"http={s} rows={len(b.get('rows') or [])}"))
    s, b = call("GET", "/api/app/driver/payment-records", mini=mini)
    results.append(check("小程序收款记录 /api/app/driver/payment-records", s == 200 and "rows" in b, f"http={s}"))

    failed = results.count(False)
    print(f"\n结果：{len(results) - failed}/{len(results)} 通过")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
