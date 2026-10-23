"""小程序主链路实测核查。

目的：不是回归断言，而是把「进入 → 授权 → 登录 → 各核心业务」逐步打一遍，
输出每一步的真实状态码与关键返回，用于定位流程断点。

用法：
    python logs/audit_mini_flows.py [--base http://127.0.0.1:18080]
"""
import argparse
import json
import sys
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
ROWS = []


def call(method, path, token=None, body=None, params=None):
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
    except Exception as e:  # noqa: BLE001
        return 0, {"error": str(e)}


def step(name, method, path, **kw):
    code, body = call(method, path, **kw)
    ok = 200 <= code < 300
    brief = json.dumps(body, ensure_ascii=False)
    if len(brief) > 200:
        brief = brief[:200] + "…"
    ROWS.append((name, method, path, code, ok, brief))
    flag = "OK  " if ok else "FAIL"
    print(f"[{flag}] {code} {name:28} {method:4} {path}")
    if not ok:
        print(f"        {brief}")
    return body, ok


def main():
    global BASE
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default=BASE)
    args = ap.parse_args()
    BASE = args.base
    print(f"目标：{BASE}\n")

    print("== 一、进入与登录 ==")
    # 1) 无任何凭据访问受保护接口
    step("未登录访问会话", "GET", "/api/common/session")
    # 2) 标准 openid 登录
    login, ok = step("微信登录（仅 code）", "POST", "/api/common/login/wechat",
                     body={"code": "dev_owner_001", "deviceLabel": "flow-audit"})
    token = login.get("accessToken") if ok else None
    # 3) 带手机号授权 code 登录（当前后端是否识别）
    body, ok2 = step("登录并携带手机号授权码", "POST", "/api/common/login/wechat",
                     body={"code": "dev_owner_001", "phoneCode": "phone-code-probe",
                           "deviceLabel": "flow-audit"})
    print(f"        返回是否含手机号字段: phoneBound={body.get('profile', {}).get('phoneBound')}")

    print("\n== 二、手机号绑定（短信兜底链路）==")
    step("发送短信验证码", "POST", "/api/common/phone/sms/send", token=token, body={"phone": "13800138000"})
    sms = call("POST", "/api/common/phone/sms/send", token=token, body={"phone": "13800138000"})[1]
    dev_code = sms.get("devCode") if isinstance(sms, dict) else None
    if dev_code:
        step("用验证码绑定手机号", "POST", "/api/common/phone/bind", token=token,
             body={"phone": "13800138000", "smsCode": dev_code})
        prof = call("GET", "/api/common/profile", token=token)[1]
        print(f"        绑定后 phoneBound={prof.get('phoneBound')} roles={prof.get('roles')} "
              f"employeeBound={prof.get('employeeBound')}")
    else:
        print("        [SKIP] 未取到联调验证码")

    print("\n== 三、车主：资料与预约 ==")
    veh, ok = step("车辆列表", "GET", "/api/app/owner/vehicles", token=token)
    vehicle_id = None
    if ok and not veh:
        created, ok = step("新增车辆", "POST", "/api/app/owner/vehicles", token=token,
                           body={"plateNo": "川A00001", "vehicleTypeCode": "SMALL", "brandModel": "大众朗逸",
                                 "vinLast4": "1234", "useType": "NON_OPERATING", "isDefault": True})
        vehicle_id = created.get("vehicleId") if ok else None
    elif ok and veh:
        vehicle_id = veh[0].get("vehicleId")
        print(f"        复用已有车辆 id={vehicle_id}")

    addr, ok = step("地址列表", "GET", "/api/app/owner/addresses", token=token)
    address_id = None
    if ok and not addr:
        created, ok = step("新增地址", "POST", "/api/app/owner/addresses", token=token,
                           body={"label": "家", "address": "成都市高新区天府大道 1 号",
                                 "lng": 104.0668, "lat": 30.5728, "isDefault": True})
        address_id = created.get("addressId") if ok else None
    elif ok and addr:
        address_id = addr[0].get("addressId")
        print(f"        复用已有地址 id={address_id}")

    slots, ok = step("可选时段", "GET", "/api/app/owner/schedule/slots", token=token)
    slot_id = None
    if ok and isinstance(slots, list) and slots:
        avail = [s for s in slots if s.get("selectable") or s.get("available")]
        slot_id = (avail or slots)[0].get("slotId")
        print(f"        时段 {len(slots)} 个，选中 {slot_id}")
    else:
        print("        [WARN] 无可选时段，预约链路无法继续")

    quote_body = {"vehicleId": vehicle_id, "addressId": address_id, "pickupSlotId": slot_id,
                  "inspectionTypeCode": "ANNUAL"}
    quote, okq = step("试算报价", "POST", "/api/app/owner/orders/quote", token=token, body=quote_body)

    order_no = None
    if okq:
        order, oko = step("提交预约单", "POST", "/api/app/owner/orders", token=token,
                          body={"quoteId": quote.get("quoteId"), "vehicleId": vehicle_id,
                                "addressId": address_id, "contactName": "审计",
                                "contactPhone": "13800138000"})
        order_no = order.get("orderNo") if oko else None
        if order_no:
            step("订单详情", "GET", f"/api/app/owner/orders/{order_no}", token=token)
            step("订单时间线", "GET", f"/api/app/owner/orders/{order_no}/timeline", token=token)
            step("发起预支付", "POST", f"/api/app/owner/orders/{order_no}/prepay", token=token)
            step("联调模拟支付", "POST", f"/api/app/owner/orders/{order_no}/mock-pay", token=token)
            step("支付结果查询", "GET", f"/api/app/owner/orders/{order_no}/payment", token=token)
            step("订单详情（支付后）", "GET", f"/api/app/owner/orders/{order_no}", token=token)

    print("\n== 四、代驾：资格与听单 ==")
    step("代驾资格档案", "GET", "/api/app/driver/qualification", token=token)
    step("听单状态", "GET", "/api/app/driver/availability", token=token)
    step("邀约列表", "GET", "/api/app/driver/invites", token=token)
    step("当前承接", "GET", "/api/app/driver/assignments/current", token=token)
    step("代驾档案", "GET", "/api/app/driver/profile", token=token)

    print("\n== 五、检测员工：站点与工单 ==")
    step("授权站点", "GET", "/api/app/station/authorized-stations", token=token)
    step("站点工单", "GET", "/api/app/station/orders", token=token, params={"pageNum": 1, "pageSize": 5})
    step("站点产能", "GET", "/api/app/station/capacity", token=token)

    print("\n== 六、公共能力 ==")
    step("消息中心", "GET", "/api/common/messages", token=token, params={"pageNum": 1, "pageSize": 5})
    step("站点列表", "GET", "/api/common/stations", token=token)
    step("员工绑定申请列表", "GET", "/api/common/employee-binding-requests", token=token)

    fails = [r for r in ROWS if not r[4]]
    print(f"\n总计 {len(ROWS)} 步，失败 {len(fails)} 步")
    for name, method, path, code, _ok, brief in fails:
        print(f"  - {name}: {code} {method} {path}\n    {brief}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
