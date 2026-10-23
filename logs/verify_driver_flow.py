"""代驾履约链路实测：资格 → 听单 → 派单 → 接单 → 行程 → 交接。

目的：确认小程序代驾端从"申请通过"到"交接完成"的每个环节是否真的能推进，
不是断言回归，失败时给出真实状态码与响应，便于定位断点。

用法：python logs/verify_driver_flow.py
"""
import datetime
import json
import subprocess
import sys
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "-N", "-B", "xinyi"]
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

OWNER_CODE = "dev_flow_owner"
DRIVER_CODE = "dev_flow_driver"
PICKUP_LNG = 110.9254
PICKUP_LAT = 21.6629

STEPS = []
FAILS = []


def sql(statement):
    out = subprocess.run([MYSQL] + DB + ["-e", statement], capture_output=True, text=True, encoding="utf-8")
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
    ok = code in expect and biz_error(body) is None
    STEPS.append((name, code, ok, body))
    brief = json.dumps(body, ensure_ascii=False)
    print(f"[{'OK' if ok else 'FAIL'}] {code:3} {name}"
          + ("" if ok else f"\n        {brief[:220]}"))
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


def admin_call(method, path, body=None, token=None):
    url = BASE + path
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


def cleanup():
    # 先删订单：账号一旦删掉，按账号反查订单就查不到了
    accounts = "(SELECT id FROM vi_account WHERE open_id LIKE 'dev_flow_%')"
    # 支付单先于订单删除，否则会留下「孤儿支付单」（本库无外键，删订单不带走支付单）
    sql(f"DELETE FROM vi_payment_order WHERE order_id IN (SELECT id FROM vi_order "
        f"WHERE owner_account_id IN {accounts})")
    sql(f"DELETE FROM vi_order WHERE owner_account_id IN {accounts}")
    sql(f"DELETE t FROM vi_session_token t JOIN vi_session s ON s.id = t.session_id "
        f"WHERE s.account_id IN {accounts}")
    sql(f"DELETE FROM vi_session WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_employee_binding WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_driver_profile WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_owner_profile WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_account_security WHERE account_id IN {accounts}")
    sql(f"DELETE FROM vi_account WHERE open_id LIKE 'dev_flow_%'")
    sql("DELETE FROM vi_station_grant WHERE sys_user_id = 9202")
    sql("DELETE FROM sys_user_role WHERE user_id = 9202")
    sql("DELETE FROM sys_user WHERE user_id = 9202")


def main():
    print(f"目标：{BASE}\n")
    sql(f"DELETE FROM vi_payment_order WHERE order_id IN (SELECT id FROM vi_order "
        f"WHERE order_no LIKE 'RLFLOW%')")
    sql(f"DELETE FROM vi_order WHERE order_no LIKE 'RLFLOW%'")
    cleanup()

    print("== 一、代驾资格 ==")
    owner = step("车主登录", "POST", "/api/common/login/wechat",
                 body={"code": OWNER_CODE, "deviceLabel": "flow"})
    driver = step("代驾账号登录（未申请）", "POST", "/api/common/login/wechat",
                  body={"code": DRIVER_CODE, "deviceLabel": "flow"})
    if not owner or not driver:
        return 1
    owner_tok = owner["accessToken"]
    driver_tok = driver["accessToken"]

    q = step("查询代驾资格档案", "GET", "/api/app/driver/qualification", token=driver_tok)
    print(f"        资格状态：{q.get('status') if q else '—'}")
    if q and q.get("status") == "NOT_APPLIED":
        step("提交代驾申请", "POST", "/api/app/driver/qualification", token=driver_tok,
             body={"realName": "流程测试", "idCardNo": "440900199001011234",
                   "licenseNo": "440900199001011234", "licenseClass": "C1",
                   "licenseValidTo": "2030-01-01", "phone": "13900000009"})
    roles_before = (call("GET", "/api/common/session", token=driver_tok)[1].get("profile") or {}).get("roles")
    print(f"        申请后视角：{roles_before}")
    neg_code, neg_body = call("POST", "/api/common/session/role", {"role": "DRIVER"}, token=driver_tok)
    check("未审核不能切换代驾视角", biz_error(neg_body) is not None or neg_code in (401, 403),
          f"{neg_code} {json.dumps(neg_body, ensure_ascii=False)[:90]}")

    # 模拟后台审核通过（真实流程由后台审核页面完成）
    sql("UPDATE vi_driver_profile SET qualification_status='APPROVED', listening_enabled=1, "
        "new_assignment_enabled=1 WHERE account_id=(SELECT id FROM vi_account "
        f"WHERE open_id='{DRIVER_CODE}')")

    print("\n== 二、听单与派单 ==")
    step("听单开关", "POST", "/api/app/driver/listening", token=driver_tok, body={"enabled": True})
    step("上报位置", "POST", "/api/app/driver/location", token=driver_tok,
         body={"lng": PICKUP_LNG, "lat": PICKUP_LAT,
               "capturedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="milliseconds")})
    switched = step("切换代驾视角", "POST", "/api/common/session/role",
                    body={"role": "DRIVER"}, token=driver_tok)
    if switched and switched.get("accessToken"):
        # 切换视角会撤销旧令牌并换发新令牌，后续请求必须用新的
        driver_tok = switched["accessToken"]

    # 车主下单并支付，支付成功会触发派单
    slots = step("可选时段", "GET", "/api/app/owner/schedule/slots", token=owner_tok) or []
    slot_id = next((s.get("slotId") for s in slots if s.get("selectable")), None)
    vehicles = step("车辆列表", "GET", "/api/app/owner/vehicles", token=owner_tok) or []
    if vehicles:
        vehicle_id = vehicles[0].get("vehicleId")
    else:
        created = step("新增车辆", "POST", "/api/app/owner/vehicles", token=owner_tok,
                       body={"plateNo": "粤K00002", "vehicleTypeCode": "sedan", "brandModel": "测试车",
                             "vinLast4": "5678", "useType": "NON_OPERATING", "isDefault": True})
        vehicle_id = created.get("vehicleId") if created else None
    addresses = step("地址列表", "GET", "/api/app/owner/addresses", token=owner_tok) or []
    if addresses:
        address_id = addresses[0].get("addressId")
    else:
        created = step("新增地址", "POST", "/api/app/owner/addresses", token=owner_tok,
                       body={"label": "家", "address": "茂名市茂南区站北路 8 号",
                             "lng": PICKUP_LNG, "lat": PICKUP_LAT, "isDefault": True})
        address_id = created.get("addressId") if created else None

    quote = step("试算报价", "POST", "/api/app/owner/orders/quote", token=owner_tok,
                 body={"vehicleId": vehicle_id, "addressId": address_id,
                       "pickupSlotId": slot_id, "inspectionTypeCode": "ANNUAL"})
    order = step("提交预约单", "POST", "/api/app/owner/orders", token=owner_tok,
                 body={"quoteId": quote.get("quoteId") if quote else None, "vehicleId": vehicle_id,
                       "addressId": address_id, "contactName": "流程", "contactPhone": "13900000009"})
    if not order:
        print("\n下单失败，后续链路无法继续")
        return 1
    order_no = order["orderNo"]
    step("预支付", "POST", f"/api/app/owner/orders/{order_no}/prepay", token=owner_tok)
    step("模拟支付", "POST", f"/api/app/owner/orders/{order_no}/mock-pay", token=owner_tok)

    detail = call("GET", f"/api/app/owner/orders/{order_no}", token=owner_tok)[1]
    print(f"        支付后状态：{detail.get('fulfillmentStatus')}"
          f"（dispatch_at={detail.get('dispatchAt')}）")

    # 未到提前派单窗口时订单是 SCHEDULED，用后台「立即派单」把链路推到派单
    adm = admin_token()
    code, res = admin_call("POST", f"/api/admin/dispatch/orders/{order_no}/dispatch-now", {}, adm)
    print(f"        立即派单：{code} {json.dumps(res, ensure_ascii=False)[:160]}")

    raw_invites = call("GET", "/api/app/driver/invites", token=driver_tok)[1]
    invites = raw_invites.get("rows") if isinstance(raw_invites, dict) else raw_invites
    invites = invites or []
    print(f"        代驾收到的邀约：{json.dumps(raw_invites, ensure_ascii=False)[:240]}")
    if not invites:
        print("        [WARN] 走完派单仍未产生邀约，派单链路存在断点")
        FAILS.append("派单后生成邀约")
        return 1
    invite_id = invites[0].get("inviteId") or invites[0].get("id")

    print("\n== 三、接单与行程 ==")
    step("接受邀约", "POST", f"/api/app/driver/invites/{invite_id}/response", token=driver_tok,
         body={"accept": True})
    step("当前承接", "GET", "/api/app/driver/assignments/current", token=driver_tok)
    step("出发取车", "POST", f"/api/app/driver/orders/{order_no}/depart", token=driver_tok)
    step("到达取车点", "POST", f"/api/app/driver/orders/{order_no}/arrive-pickup", token=driver_tok)

    print("\n== 四、交接 ==")
    ho = step("创建交车交接", "POST", f"/api/common/orders/{order_no}/handovers", token=driver_tok,
              body={"handoverType": "OWNER_TO_DRIVER"})
    if ho:
        hid = ho.get("handoverId") or ho.get("id")
        secret = step("获取交接码（车主侧）", "GET", f"/api/common/orders/{order_no}/handovers/{hid}/secret",
                      token=owner_tok)
        code = None
        if isinstance(secret, dict):
            code = secret.get("code") or secret.get("secret") or secret.get("handoverCode")
            print(f"        交接码：{code}")
        step("确认交接", "POST", f"/api/common/orders/{order_no}/handovers/{hid}/confirm",
             token=driver_tok, body={"code": code, "version": ho.get("version")})

    print("\n== 五、检测点处理 ==")
    step("司机到达检测站", "POST", f"/api/app/driver/orders/{order_no}/arrive-station", token=driver_tok)

    staff_phone = "13900000003"
    staff_id = 9202
    sql(f"DELETE FROM sys_user WHERE user_id={staff_id}")
    sql(f"""INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber,
             sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by,
             create_time, update_by, update_time, remark)
           VALUES ({staff_id}, 103, 'flow_station_staff', '流程检测员工', '00', '', '{staff_phone}',
             '0', '', '', '0', '0', '127.0.0.1', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 'verify',
             UTC_TIMESTAMP(3), '', UTC_TIMESTAMP(3), 'driver flow verify')""")
    admin_call("PUT", f"/api/admin/station-grants/{staff_id}", {"stationId": 1, "enabled": True,
                                                              "reason": "流程验收"}, adm)
    staff = call("POST", "/api/common/login/wechat",
                 {"code": "dev_flow_staff", "phoneCode": staff_phone, "deviceLabel": "flow"})[1]
    staff_tok = staff.get("accessToken")
    if not staff_tok:
        FAILS.append("检测员工登录")
        return 1
    sw = call("POST", "/api/common/session/role", {"role": "STATION"}, token=staff_tok)[1]
    staff_tok = sw.get("accessToken") or staff_tok

    step("站点工单列表", "GET", "/api/app/station/orders", token=staff_tok,
         params={"pageNum": 1, "pageSize": 10})

    # 收车必须走 DRIVER_TO_STATION 交接，司乘双方确认后订单才进入检测中
    # 站点侧交接必须由检测员工发起（他是接收方），司机确认送出
    ho_in = step("检测点发起收车交接", "POST", f"/api/common/orders/{order_no}/handovers",
                 token=staff_tok, body={"handoverType": "DRIVER_TO_STATION"})
    if ho_in:
        hid_in = ho_in.get("handoverId") or ho_in.get("id")
        # 交接码由持码方（司机）出示，站点员工核验
        sec_in = call("GET", f"/api/common/orders/{order_no}/handovers/{hid_in}/secret",
                      token=driver_tok)[1]
        code_in = None
        if isinstance(sec_in, dict):
            code_in = sec_in.get("code") or sec_in.get("secret") or sec_in.get("handoverCode")
        # 站内交接的核验方是检测员工：由站点输码确认收车
        confirmed = step("检测点确认收车（核验方）",
                         "POST", f"/api/common/orders/{order_no}/handovers/{hid_in}/confirm",
                         token=staff_tok, body={"code": code_in, "version": ho_in.get("version")})
        # G04：站内交接需双方确认，核验方确认后停在 PARTIAL，等持码方（司机）确认
        if isinstance(confirmed, dict) and confirmed.get("status"):
            print(f"        核验方确认后状态：{confirmed.get('status')}")
            check("核验方确认后为待对方确认", confirmed.get("status") == "PARTIAL",
                  f"status={confirmed.get('status')}")
        held = step("司机确认已交付（持码方）",
                    "POST", f"/api/common/orders/{order_no}/handovers/{hid_in}/holder-confirm",
                    token=driver_tok)
        if isinstance(held, dict) and held.get("status"):
            print(f"        持码方确认后状态：{held.get('status')}")
            check("双方确认后交接完成", held.get("status") == "CONFIRMED",
                  f"status={held.get('status')}")

    step("工单详情", "GET", f"/api/app/station/orders/{order_no}", token=staff_tok)
    plate = sql(f"SELECT JSON_UNQUOTE(JSON_EXTRACT(vehicle_snapshot,'$.plateNo')) FROM vi_order "
                f"WHERE order_no='{order_no}'")
    plate_no = plate[0] if plate else "粤K00002"
    step("核对并保存行驶证", "PUT", f"/api/app/station/orders/{order_no}/license", token=staff_tok,
         body={"ownerName": "流程", "plateNo": plate_no, "vin": "LSV12345678901234",
               "registeredOn": "2021-06-01", "verified": True})
    step("登记检测结果", "POST", f"/api/app/station/orders/{order_no}/results", token=staff_tok,
         body={"conclusion": "PASS", "items": "[]", "inspectedAt": "2026-10-22T09:00:00.000Z"})
    step("发送取车通知", "POST", f"/api/app/station/orders/{order_no}/pickup-notification",
         token=staff_tok)

    print("\n== 六、取车交还与完成 ==")
    ho_out = step("站点发起交车给司机", "POST", f"/api/common/orders/{order_no}/handovers",
                  token=staff_tok, body={"handoverType": "STATION_TO_DRIVER"})
    if ho_out:
        hid_out = ho_out.get("handoverId") or ho_out.get("id")
        # 站点是持码方，司机核验取车
        sec_out = call("GET", f"/api/common/orders/{order_no}/handovers/{hid_out}/secret",
                       token=staff_tok)[1]
        code_out = None
        if isinstance(sec_out, dict):
            code_out = sec_out.get("code") or sec_out.get("secret") or sec_out.get("handoverCode")
        step("司机确认取车", "POST", f"/api/common/orders/{order_no}/handovers/{hid_out}/confirm",
             token=driver_tok, body={"code": code_out, "version": ho_out.get("version")})

    ho_back = step("司机发起还车交接", "POST", f"/api/common/orders/{order_no}/handovers",
                   token=driver_tok, body={"handoverType": "DRIVER_TO_OWNER"})
    if ho_back:
        hid_back = ho_back.get("handoverId") or ho_back.get("id")
        # 代驾出码、车主核验（PRD D22/D23）
        sec_back = call("GET", f"/api/common/orders/{order_no}/handovers/{hid_back}/secret",
                        token=driver_tok)[1]
        code_back = None
        if isinstance(sec_back, dict):
            code_back = sec_back.get("code") or sec_back.get("secret") or sec_back.get("handoverCode")
        step("车主确认收车", "POST", f"/api/common/orders/{order_no}/handovers/{hid_back}/confirm",
             token=owner_tok, body={"code": code_back, "version": ho_back.get("version")})
    step("车主确认服务", "POST", f"/api/app/owner/orders/{order_no}/service-confirmation",
         token=owner_tok, body={"version": None})
    step("订单进度（车主侧）", "GET", f"/api/app/owner/orders/{order_no}/timeline", token=owner_tok)

    final = call("GET", f"/api/app/owner/orders/{order_no}", token=owner_tok)[1]
    print(f"\n最终订单状态：{final.get('fulfillmentStatus')}")

    cleanup()

    print(f"\n== 汇总：{len(STEPS)} 步，失败 {len(FAILS)} 步 ==")
    for name in FAILS:
        print("  FAIL:", name)
    return 0 if not FAILS else 1


if __name__ == "__main__":
    sys.exit(main())
