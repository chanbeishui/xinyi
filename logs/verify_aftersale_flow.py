"""售后 / 申诉 / 对账 端到端回归（G06）。

覆盖三条此前只做过接口冒烟的链路，重点是"状态真的走完了"：
  一、售后：车主发起取消退款 → 后台预览可退金额 → 审核通过 → 渠道退款 → 订单取消
  二、申诉：罚款 → 代驾申诉 → 后台审核通过 → 处罚撤销
  三、对账：对账异常单 → 发起渠道重查 → 登记结论 → 单据关闭

用法：python logs/verify_aftersale_flow.py
前置：后端运行在 18080 且支付为联调通道（rongli.payment.channel=MOCK）
"""
import datetime
import json
import subprocess
import sys
import urllib.error
import urllib.request
import uuid

BASE = "http://127.0.0.1:18080"
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "-N", "-B", "xinyi"]
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

OWNER_CODE = "dev_g06_owner"
DRIVER_CODE = "dev_g06_driver"
PICKUP_LNG = 110.9254
PICKUP_LAT = 21.6629
TAG = "G06" + uuid.uuid4().hex[:6].upper()

STEPS = []
FAILS = []


def sql(statement):
    out = subprocess.run([MYSQL] + DB + ["-e", statement], capture_output=True, text=True, encoding="utf-8")
    if out.returncode != 0:
        print(f"        [SQL ERROR] {out.stderr.strip()[:200]}")
    return [l for l in (out.stdout or "").strip().splitlines() if "Warning" not in l]


def call(method, path, body=None, token=None, params=None):
    url = BASE + path
    if params:
        url += "?" + "&".join(f"{k}={v}" for k, v in params.items() if v not in (None, ""))
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["X-Mini-Token"] = token
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


def biz_error(body):
    """小程序接口业务失败时仍返回 HTTP 200，靠 body.code 表示错误，必须单独识别。

    注意：交接码接口也用 code 字段承载 4 位数字字符串，因此这里只认
    「整数 code + 错误文案同时出现」的组合，避免把交接码误判成业务错误。
    """
    if isinstance(body, dict):
        code = body.get("code")
        if isinstance(code, int) and not isinstance(code, bool) and code not in (0, 200):
            detail = body.get("msg") or body.get("message") or body.get("error")
            if detail:
                return str(detail)
    return None


def step(name, method, path, expect=(200,), **kw):
    code, body = call(method, path, **kw)
    err = biz_error(body) if code in expect else None
    ok = code in expect and err is None
    STEPS.append((name, code, ok, body))
    brief = json.dumps(body, ensure_ascii=False)
    detail = f"\n        {brief[:260]}" if not ok else ""
    print(f"[{'OK' if ok else 'FAIL'}] {code:3} {name}" + detail)
    if not ok:
        FAILS.append(name)
    return body if ok else None


def check(name, passed, detail=""):
    STEPS.append((name, 0, bool(passed), detail))
    print(f"[{'OK' if passed else 'FAIL'}] {'---':>3} {name}" + (f"  {detail}" if detail else ""))
    if not passed:
        FAILS.append(name)


def admin_token():
    code, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    return body.get("token") if code == 200 else None


def admin_call(method, path, body=None, token=None, params=None):
    url = BASE + path
    if params:
        url += "?" + "&".join(f"{k}={v}" for k, v in params.items() if v not in (None, ""))
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


def admin_step(name, method, path, expect=(200,), **kw):
    code, body = admin_call(method, path, **kw)
    err = biz_error(body) if code in expect else None
    ok = code in expect and err is None
    STEPS.append((name, code, ok, body))
    brief = json.dumps(body, ensure_ascii=False)
    detail = f"\n        {brief[:260]}" if not ok else ""
    print(f"[{'OK' if ok else 'FAIL'}] {code:3} {name}" + detail)
    if not ok:
        FAILS.append(name)
    return body if ok else None


def cleanup():
    # 顺序：订单 → 售后/退款 → 治理单 → 罚款/申诉 → 会话 → 账号
    accounts = "(SELECT id FROM vi_account WHERE open_id LIKE 'dev_g06_%')"
    orders = f"(SELECT id FROM vi_order WHERE owner_account_id IN {accounts})"
    sql(f"DELETE FROM vi_governance_case WHERE reason LIKE '%{TAG}%'")
    sql(f"DELETE FROM vi_penalty_appeal WHERE penalty_id IN "
        f"(SELECT id FROM vi_penalty WHERE reason LIKE '%{TAG}%')")
    sql(f"DELETE FROM vi_penalty WHERE reason LIKE '%{TAG}%'")
    sql(f"DELETE FROM vi_aftersale_review WHERE case_id IN "
        f"(SELECT id FROM vi_aftersale_case WHERE order_id IN {orders})")
    sql(f"DELETE FROM vi_aftersale_case WHERE order_id IN {orders}")
    sql(f"DELETE FROM vi_refund_order WHERE case_id NOT IN (SELECT id FROM vi_aftersale_case)")
    # 支付单必须先于订单删除：本库没有外键，删订单不会带走支付单，留下的是「孤儿支付单」
    sql(f"DELETE FROM vi_payment_order WHERE order_id IN (SELECT id FROM vi_order "
        f"WHERE owner_account_id IN {accounts})")
    sql(f"DELETE FROM vi_order WHERE owner_account_id IN {accounts}")
    sql(f"DELETE FROM vi_quote WHERE owner_account_id IN {accounts}")
    sql(f"DELETE FROM vi_vehicle WHERE owner_account_id IN {accounts}")
    sql(f"DELETE FROM vi_address WHERE owner_account_id IN {accounts}")
    sql(f"DELETE t FROM vi_session_token t JOIN vi_session s ON s.id = t.session_id "
        f"WHERE s.account_id IN {accounts}")
    sql(f"DELETE FROM vi_session WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_employee_binding WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_driver_profile WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_owner_profile WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_account_security WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_account WHERE open_id LIKE 'dev_g06_%'")
    sql(f"DELETE FROM vi_sms_send_log WHERE scene = 'TEST' AND error_message LIKE '%{TAG}%'")


def main():
    print(f"目标：{BASE}  标记：{TAG}\n")
    cleanup()
    adm = admin_token()
    if not adm:
        print("后台登录失败，无法继续")
        return 1

    print("== 一、售后：取消退款端到端 ==")
    owner = step("车主登录", "POST", "/api/common/login/wechat",
                 body={"code": OWNER_CODE, "deviceLabel": "g06"})
    if not owner:
        return 1
    owner_tok = owner["accessToken"]

    slots = step("可选时段", "GET", "/api/app/owner/schedule/slots", token=owner_tok) or []
    slot_id = next((s.get("slotId") for s in slots if s.get("selectable")), None)
    vehicles = step("车辆列表", "GET", "/api/app/owner/vehicles", token=owner_tok) or []
    if vehicles:
        vehicle_id = vehicles[0].get("vehicleId")
    else:
        created = step("新增车辆", "POST", "/api/app/owner/vehicles", token=owner_tok,
                       body={"plateNo": "粤KG0606", "vehicleTypeCode": "sedan", "brandModel": "售后测试车",
                             "vinLast4": "G006", "useType": "NON_OPERATING", "isDefault": True})
        vehicle_id = created.get("vehicleId") if created else None
    addresses = step("地址列表", "GET", "/api/app/owner/addresses", token=owner_tok) or []
    if addresses:
        address_id = addresses[0].get("addressId")
    else:
        created = step("新增地址", "POST", "/api/app/owner/addresses", token=owner_tok,
                       body={"label": "家", "address": "茂名市茂南区站北路 8 号",
                             "lng": PICKUP_LNG, "lat": PICKUP_LAT, "isDefault": True})
        address_id = created.get("addressId") if created else None

    for label, value in (("可选时段", slot_id), ("车辆", vehicle_id), ("地址", address_id)):
        if value in (None, ""):
            print(f"\n前置条件缺失：{label}，后续链路无法继续")
            return 1

    quote = step("试算报价", "POST", "/api/app/owner/orders/quote", token=owner_tok,
                 body={"vehicleId": vehicle_id, "addressId": address_id,
                       "pickupSlotId": slot_id, "inspectionTypeCode": "ANNUAL"})
    order = step("提交预约单", "POST", "/api/app/owner/orders", token=owner_tok,
                 body={"quoteId": quote.get("quoteId") if quote else None, "vehicleId": vehicle_id,
                       "addressId": address_id, "contactName": "售后测试", "contactPhone": "13900000031"})
    if not order:
        print("\n下单失败，后续链路无法继续")
        return 1
    order_no = order["orderNo"]
    total_fen = order.get("totalFen") or (quote or {}).get("totalFen") or 0
    step("预支付", "POST", f"/api/app/owner/orders/{order_no}/prepay", token=owner_tok)
    paid = step("模拟支付", "POST", f"/api/app/owner/orders/{order_no}/mock-pay", token=owner_tok)
    print(f"        订单 {order_no} 金额 {total_fen} 分，支付后状态：{(paid or {}).get('fulfillmentStatus')}")

    applied = step("车主发起售后（取消退款）", "POST", f"/api/app/owner/orders/{order_no}/after-sales",
                   token=owner_tok,
                   body={"kind": "CANCEL_REFUND", "requestedFen": total_fen,
                         "reason": f"{TAG} 行程变更，申请取消并退款"})
    case_id = (applied or {}).get("caseId")
    if not case_id:
        print("        未拿到售后单号，后面无法继续")
        return 1
    print(f"        售后单 {case_id}，状态 {(applied or {}).get('caseStatus')}")

    listed = admin_step("后台售后列表", "GET", "/api/admin/after-sales", token=adm,
                        params={"pageNum": 1, "pageSize": 20})
    if listed:
        hit = next((r for r in listed.get("rows", []) if str(r.get("caseId")) == str(case_id)), None)
        check("新售后单出现在后台列表", hit is not None)
    preview = admin_step("审核预览（可退金额）", "POST", f"/api/admin/after-sales/{case_id}/review-preview",
                         token=adm)
    if preview:
        print(f"        可退 {preview.get('refundableFen')} 分，需实还={preview.get('returnRequired')}")

    reviewed = admin_step("审核通过（无需实还，直接退款）", "POST",
                          f"/api/admin/after-sales/{case_id}/review", token=adm,
                          body={"approve": True, "approvedFen": total_fen, "returnRequired": False,
                                "opinion": f"{TAG} 核实无误，同意取消并全额退款"})
    refund_no = None
    if reviewed:
        refund_obj = reviewed.get("refund") or {}
        refund_no = refund_obj.get("refundNo")
        print(f"        审核后：case={reviewed.get('caseStatus')} refund={refund_obj.get('status')}")
        check("售后单已结案", reviewed.get("caseStatus") == "CLOSED",
              f"caseStatus={reviewed.get('caseStatus')}")
        check("订单已取消", reviewed.get("orderStatus") == "CANCELLED",
              f"orderStatus={reviewed.get('orderStatus')}")
        check("退款已完成", refund_obj.get("status") == "SUCCESS",
              f"refundStatus={refund_obj.get('status')}")
        check("退款金额与申请一致", int(refund_obj.get("amountFen") or 0) == int(total_fen),
              f"{refund_obj.get('amountFen')} vs {total_fen}")
    if refund_no:
        detail = admin_step("退款单详情", "GET", f"/api/admin/refunds/{refund_no}", token=adm)
        if detail:
            check("退款单状态为成功", detail.get("status") == "SUCCESS",
                  f"status={detail.get('status')}")
        admin_step("主动查退款状态", "POST", f"/api/admin/refunds/{refund_no}/query", token=adm)

    print("\n== 二、申诉：罚款 → 申诉 → 审核撤销 ==")
    driver = step("代驾账号登录", "POST", "/api/common/login/wechat",
                  body={"code": DRIVER_CODE, "deviceLabel": "g06"})
    if not driver:
        return 1
    driver_tok = driver["accessToken"]
    # 建档案 + 审核通过，才能看到自己的罚款
    q = step("代驾资格档案", "GET", "/api/app/driver/qualification", token=driver_tok)
    if q and q.get("status") == "NOT_APPLIED":
        step("提交代驾申请", "POST", "/api/app/driver/qualification", token=driver_tok,
             body={"realName": "申诉测试", "idCardNo": "440900199001016789",
                   "licenseNo": "440900199001016789", "licenseClass": "C1",
                   "licenseValidTo": "2030-01-01", "phone": "13900000032"})
    sql("UPDATE vi_driver_profile SET qualification_status='APPROVED', listening_enabled=1, "
        f"new_assignment_enabled=1 WHERE account_id=(SELECT id FROM vi_account WHERE open_id='{DRIVER_CODE}')")
    switched = step("切换代驾视角", "POST", "/api/common/session/role",
                    body={"role": "DRIVER"}, token=driver_tok)
    if switched and switched.get("accessToken"):
        driver_tok = switched["accessToken"]

    rows = sql(f"SELECT id FROM vi_driver_profile WHERE account_id=(SELECT id FROM vi_account "
               f"WHERE open_id='{DRIVER_CODE}')")
    driver_id = rows[0] if rows else None
    assignment_rows = sql("SELECT id FROM vi_driver_assignment ORDER BY id DESC LIMIT 1")
    assignment_id = assignment_rows[0] if assignment_rows else None
    if not driver_id:
        print("        未能创建代驾档案，申诉链路跳过")
        FAILS.append("代驾档案创建")
        return finish()
    if not assignment_id:
        print("        库内没有派单记录，无法生成罚款，申诉链路跳过")
        FAILS.append("派单记录缺失")
        return finish()

    sql(f"INSERT INTO vi_penalty (driver_id, assignment_id, business_date, counted, penalty_type, status, "
        f"reason, rule_version, created_at, updated_at, version) VALUES ({driver_id}, {assignment_id}, "
        f"CURDATE(), 1, 'SKIP_ROUND', 'ACTIVE', '{TAG} 到站超时计为跳单', 'v1', UTC_TIMESTAMP(3), "
        f"UTC_TIMESTAMP(3), 0)")
    penalty_rows = sql(f"SELECT id FROM vi_penalty WHERE driver_id={driver_id} AND reason LIKE '%{TAG}%' "
                       f"ORDER BY id DESC LIMIT 1")
    penalty_id = penalty_rows[0] if penalty_rows else None
    check("罚款记录已生成", penalty_id is not None, f"penaltyId={penalty_id}")

    mine = step("代驾查看罚款列表", "GET", "/api/app/driver/penalties", token=driver_tok,
                params={"pageNum": 1, "pageSize": 20})
    if mine and penalty_id:
        hit = next((r for r in mine.get("rows", []) if str(r.get("penaltyId")) == str(penalty_id)), None)
        check("罚款出现在代驾端列表", hit is not None)

    appealed = step("提交申诉", "POST", f"/api/app/driver/penalties/{penalty_id}/appeals",
                    token=driver_tok,
                    body={"reason": f"{TAG} 当日暴雨道路封闭，非个人原因导致到站延迟，申请撤销"})
    appeal_id = (appealed or {}).get("appealId")
    check("申诉已受理", appeal_id is not None, f"appealId={appeal_id}")
    if appeal_id:
        admin_step("后台申诉列表", "GET", "/api/admin/penalty-appeals", token=adm,
                   params={"pageNum": 1, "pageSize": 20})
        audited = admin_step("审核申诉（通过）", "POST",
                             f"/api/admin/penalty-appeals/{appeal_id}/audit", token=adm,
                             body={"approve": True, "opinion": f"{TAG} 天气原因属实，撤销处罚"})
        if audited:
            print(f"        申诉状态：{audited.get('status')}")
        penalty_status = sql(f"SELECT status FROM vi_penalty WHERE id={penalty_id}")
        check("处罚已撤销", penalty_status and penalty_status[0] == "REVOKED",
              f"status={penalty_status[0] if penalty_status else '—'}")

    print("\n== 三、对账：异常单重查与结论登记 ==")
    sql(f"INSERT INTO vi_governance_case (case_type, status, reason, created_at, updated_at, version) "
        f"VALUES ('RECONCILE', 'OPEN', '{TAG} 渠道流水与本地支付单金额不一致', UTC_TIMESTAMP(3), "
        f"UTC_TIMESTAMP(3), 0)")
    case_rows = sql(f"SELECT id FROM vi_governance_case WHERE reason LIKE '%{TAG}%' ORDER BY id DESC LIMIT 1")
    recon_id = case_rows[0] if case_rows else None
    check("对账异常单已创建", recon_id is not None, f"caseId={recon_id}")
    if recon_id:
        listed = admin_step("对账异常列表", "GET", "/api/admin/reconciliation", token=adm,
                            params={"status": "OPEN", "pageNum": 1, "pageSize": 20})
        if listed:
            hit = next((r for r in listed.get("rows", []) if str(r.get("caseId")) == str(recon_id)), None)
            check("异常单出现在对账列表", hit is not None)
        queried = admin_step("发起渠道重查", "POST", f"/api/admin/reconciliation/{recon_id}/query", token=adm)
        if queried:
            print(f"        重查后状态：{queried.get('status')}")
        resolved = admin_step("登记对账结论", "POST", f"/api/admin/reconciliation/{recon_id}/resolve",
                              token=adm,
                              body={"conclusion": f"{TAG} 已核对微信渠道流水，本地支付单金额正确，"
                                                  f"差异来自渠道手续费展示口径，无需补退"})
        if resolved:
            print(f"        结论登记后状态：{resolved.get('status')}")
        final = sql(f"SELECT status FROM vi_governance_case WHERE id={recon_id}")
        check("对账单已关闭", final and final[0] == "RESOLVED",
              f"status={final[0] if final else '—'}")

    return finish()


def finish():
    print()
    cleanup()
    total = len(STEPS)
    passed = total - len(FAILS)
    print(f"== 汇总：{total} 项，通过 {passed}，失败 {len(FAILS)}")
    if FAILS:
        print("   失败项：" + "、".join(FAILS))
        return 1
    print("== 售后 / 申诉 / 对账 三条链路端到端通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
