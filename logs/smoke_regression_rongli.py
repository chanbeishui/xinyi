"""回归冒烟：确认本次改动没有影响其他后台接口。

覆盖本次改动涉及或可能受牵连的读接口：治理台账（EXPORT/PRIVACY 共用 listGovernance）、
审计事件、导出任务、统计、对账，以及本轮新增页面对应的收车/检测待办、派单记录、
外部付款核对、对账调整、个人对账汇总等。
"""
import json
import urllib.error
import urllib.request
from urllib.parse import urlencode

BASE = "http://127.0.0.1:18080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
FAILS = []


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
            return e.code, {"raw": raw[:200]}


CHECKS = [
    ("治理与导出列表", "/api/admin/exports", {"status": "", "pageNum": 1, "pageSize": 10}),
    ("隐私案件列表", "/api/admin/privacy-cases", {"status": "", "pageNum": 1, "pageSize": 10}),
    ("审计事件", "/api/admin/audit-events", {"pageNum": 1, "pageSize": 10}),
    ("业务统计", "/api/admin/statistics", {"range": "MONTH"}),
    ("支付渠道核对", "/api/admin/reconciliation", {"pageNum": 1, "pageSize": 6}),
    ("争议列表", "/api/admin/disputes", {"pageNum": 1, "pageSize": 10}),
    ("服务异常列表", "/api/admin/exceptions", {"pageNum": 1, "pageSize": 10}),
    ("检测结果更正", "/api/admin/result-corrections", {"pageNum": 1, "pageSize": 10}),
    ("通知模板", "/api/admin/notification-templates", None),
    ("补偿任务", "/api/admin/jobs", {"pageNum": 1, "pageSize": 10}),
    ("业务配置", "/api/admin/config", None),
    ("处罚列表", "/api/admin/penalties", {"pageNum": 1, "pageSize": 10}),
    ("处罚申诉", "/api/admin/penalty-appeals", {"pageNum": 1, "pageSize": 10}),
    ("培训课程", "/api/admin/training-courses", {"pageNum": 1, "pageSize": 10}),
    ("收车待办", "/api/admin/receiving/pending", {"pageNum": 1, "pageSize": 10}),
    ("检测结果待办", "/api/admin/inspection/todos", {"pageNum": 1, "pageSize": 10}),
    ("派单邀约记录", "/api/admin/dispatch/invites", {"pageNum": 1, "pageSize": 10}),
    ("工作台看板", "/api/admin/dashboard", None),
    ("对账费用调整", "/api/admin/driver-fee-adjustments", {"pageNum": 1, "pageSize": 10}),
    ("对账支付流水", "/api/admin/driver-payment-records", {"pageNum": 1, "pageSize": 10}),
    ("对账单列表", "/api/admin/driver-statements", {"pageNum": 1, "pageSize": 10}),
    ("检测点与容量", "/api/admin/stations", None),
    ("计价规则", "/api/admin/prices", None),
    ("代驾认证", "/api/admin/driver-qualifications", {"pageNum": 1, "pageSize": 10}),
    ("代驾档案", "/api/admin/drivers", {"pageNum": 1, "pageSize": 10}),
    ("订单列表", "/api/admin/orders", {"pageNum": 1, "pageSize": 10}),
    ("车辆列表", "/api/admin/vehicles", {"pageNum": 1, "pageSize": 10}),
    ("车主列表", "/api/admin/owners", {"pageNum": 1, "pageSize": 10}),
]


def main():
    status, body = call("POST", "/login", body={"username": "admin", "password": "admin123"})
    assert status == 200 and body.get("token"), f"登录失败 {status} {body}"
    token = body["token"]
    inner = {"Content-Type": "application/json"}
    if token:
        inner["Authorization"] = "Bearer " + token

    for label, path, params in CHECKS:
        status, payload = call("GET", path, token=token, params=params)
        ok = status == 200 and not (isinstance(payload, dict) and payload.get("raw"))
        detail = ""
        if isinstance(payload, dict) and "rows" in payload:
            detail = f"rows={len(payload.get('rows') or [])} total={payload.get('total')}"
        elif isinstance(payload, dict):
            detail = f"keys={list(payload)[:4]}"
        print(("  OK  " if ok else " FAIL ") + f"{label} {path}" + (f"  {detail}" if detail else ""))
        if not ok:
            FAILS.append(label)

    print()
    if FAILS:
        print(f"未通过 {len(FAILS)} 项：" + "；".join(FAILS))
        raise SystemExit(1)
    print(f"{len(CHECKS)} 个接口全部返回 200")


if __name__ == "__main__":
    main()
