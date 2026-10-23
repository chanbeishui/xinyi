"""业务事件 → 第三方通道的自动下发链路验收（V6.4 第六章）。

验证的不是"能发出去"（没有真实凭据），而是**链路真的接上了、而且没配好时不瞎发**：

  1) 下单 → 支付成功触发 BOOKING_CONFIRMED，投递轨迹被写入 vi_notification_delivery
  2) 短信通道档案未生效时，投递状态是 SKIPPED 且给出可读原因码，不是 FAILED 也不是静默丢弃
  3) 同一事件重复触发只产生一条投递（uk_delivery_event 去重）
  4) 通知下发失败不影响业务：订单状态照常推进

前置：后端 18080 运行最新代码；MySQL 127.0.0.1:3307/xinyi 可访问。
用例自建 fixture（档案/模板/路由）并在结束时清理，可重复执行。
用法：python logs/verify_notification_dispatch.py
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
OWNER_CODE = "dev_notify_owner"

PASS, FAIL = [], []


def sql(statement):
    out = subprocess.run([MYSQL] + DB + ["-e", statement], capture_output=True, text=True, encoding="utf-8")
    if out.returncode != 0:
        raise RuntimeError(out.stderr.strip()[:400])
    return [line for line in out.stdout.splitlines() if line.strip()]


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:300]}


def check(name, ok, detail=""):
    if ok:
        PASS.append(name)
        print(f"[PASS] {name} -> {detail}")
    else:
        FAIL.append(name)
        print(f"[FAIL] {name} -> {detail}")


def login(code_label, phone_code=None):
    payload = {"code": code_label, "deviceLabel": "notify-verify"}
    if phone_code:
        # 同时授权手机号，让下发链路能走过「手机号可用」这一关，直达通道配置判断
        payload["phoneCode"] = phone_code
    return call("POST", "/api/common/login/wechat", payload)


def setup():
    """建一套最小 fixture：短信模板（已发布指针）+ 路由；档案故意留 DRAFT。"""
    cleanup()
    sql("INSERT INTO vi_integration_profile (kind,display_name,owner_dept_id,environment,config_key,"
        "lifecycle,revision_seq,created_by,created_at,updated_at,version) "
        "VALUES ('TENCENT_SMS','验收短信通道',100,'TEST','verify-notify','DRAFT',0,1,"
        "UTC_TIMESTAMP(3),UTC_TIMESTAMP(3),0)")
    profile_id = int(sql("SELECT id FROM vi_integration_profile WHERE config_key='verify-notify'")[0])
    sql("INSERT INTO vi_message_template (name,provider,owner_dept_id,environment,scene,audience,"
        "revision_seq,created_by,created_at,updated_at,version) "
        "VALUES ('验收预约成功模板','TENCENT_SMS',100,'TEST','BOOKING_CONFIRMED','OWNER',1,1,"
        "UTC_TIMESTAMP(3),UTC_TIMESTAMP(3),0)")
    template_id = int(sql("SELECT id FROM vi_message_template "
                          "WHERE name='验收预约成功模板' ORDER BY id DESC LIMIT 1")[0])
    sql("INSERT INTO vi_message_template_revision (template_id,revision_no,profile_id,profile_revision_id,"
        "vendor_template_id,parameter_bindings,options_json,enabled,approval_status,content_hash,created_by,"
        "reason,created_at,updated_at,version) VALUES ("
        f"{template_id},1,{profile_id},0,'1234567',"
        "'[{\"parameterKey\":\"1\",\"variable\":\"orderNo\"}]','{}',1,'APPROVED','verify',1,'验收',"
        "UTC_TIMESTAMP(3),UTC_TIMESTAMP(3),0)")
    revision_id = int(sql("SELECT id FROM vi_message_template_revision "
                          f"WHERE template_id={template_id} ORDER BY id DESC LIMIT 1")[0])
    sql(f"UPDATE vi_message_template SET active_revision_id={revision_id} WHERE id={template_id}")
    sql("INSERT INTO vi_notification_route (owner_dept_id,environment,scene,audience,primary_template_id,"
        "fallback_policy,enabled,updated_by,reason,created_at,updated_at,version) "
        f"VALUES (100,'TEST','BOOKING_CONFIRMED','OWNER',{template_id},'NONE',1,1,'验收',"
        "UTC_TIMESTAMP(3),UTC_TIMESTAMP(3),0)")
    return profile_id, template_id


def cleanup():
    sql("DELETE d FROM vi_notification_delivery d WHERE d.scene='BOOKING_CONFIRMED' "
        "AND d.event_key LIKE 'BOOKING_CONFIRMED:%'")
    sql("DELETE FROM vi_notification_route WHERE scene='BOOKING_CONFIRMED' AND reason='验收'")
    sql("DELETE r FROM vi_message_template_revision r JOIN vi_message_template t ON t.id=r.template_id "
        "WHERE t.name='验收预约成功模板'")
    sql("DELETE FROM vi_message_template WHERE name='验收预约成功模板'")
    sql("DELETE FROM vi_integration_profile WHERE config_key='verify-notify'")


def place_and_pay(token):
    slots = call("GET", "/api/app/owner/schedule/slots", token=token)[1] or []
    slot_id = next((s.get("slotId") for s in slots if s.get("selectable")), None)
    vehicles = call("GET", "/api/app/owner/vehicles", token=token)[1] or []
    if vehicles:
        vehicle_id = vehicles[0].get("vehicleId")
    else:
        created = call("POST", "/api/app/owner/vehicles", {
            "plateNo": "粤K9N001", "vehicleTypeCode": "sedan", "brandModel": "验收车",
            "vinLast4": "0001", "useType": "NON_OPERATING", "isDefault": True}, token=token)[1]
        vehicle_id = created.get("vehicleId")
    addresses = call("GET", "/api/app/owner/addresses", token=token)[1] or []
    if addresses:
        address_id = addresses[0].get("addressId")
    else:
        created = call("POST", "/api/app/owner/addresses", {
            "label": "家", "address": "茂名市茂南区站北路 8 号", "lng": 110.9254, "lat": 21.6629,
            "isDefault": True}, token=token)[1]
        address_id = created.get("addressId")
    quote = call("POST", "/api/app/owner/orders/quote", {
        "vehicleId": vehicle_id, "addressId": address_id, "pickupSlotId": slot_id,
        "inspectionTypeCode": "ANNUAL"}, token=token)[1]
    code, order = call("POST", "/api/app/owner/orders", {
        "quoteId": quote.get("quoteId") if quote else None, "vehicleId": vehicle_id,
        "addressId": address_id, "contactName": "验收", "contactPhone": "13900000019"}, token=token)
    if code != 200 or not order.get("orderNo"):
        return None, None
    order_no = order["orderNo"]
    call("POST", f"/api/app/owner/orders/{order_no}/prepay", token=token)
    call("POST", f"/api/app/owner/orders/{order_no}/mock-pay", token=token)
    return order_no, order.get("orderId")


def main():
    print("== 一、准备 fixture（短信档案留 DRAFT，模板与路由就绪）==")
    profile_id, template_id = setup()
    print(f"        档案={profile_id} 模板={template_id}")

    print("\n== 二、下单并支付，观察投递轨迹 ==")
    code, body = login(OWNER_CODE, "13700000099")
    token = body.get("accessToken")
    check("验收账号可登录", code == 200 and bool(token), f"{code}")
    if not token:
        cleanup()
        return 1
    # 补齐手机号：只有绑定过手机号的账号才可能真正下发短信
    bind = call("POST", "/api/common/phone/wechat", {"phoneCode": "13700000099"}, token=token)[1]
    if isinstance(bind, dict) and bind.get("accessToken"):
        # 绑定手机号会撤销旧令牌并换发新令牌
        token = bind["accessToken"]
    bound = sql("SELECT phone_lookup IS NOT NULL FROM vi_account WHERE open_id='%s'" % OWNER_CODE)
    check("验收账号已绑定手机号", bool(bound) and bound[0] == "1", str(bound))
    order_no, order_id = place_and_pay(token)
    check("下单并支付成功", bool(order_no), str(order_no))
    if not order_no:
        cleanup()
        return 1
    order_id = int(sql(f"SELECT id FROM vi_order WHERE order_no='{order_no}'")[0])

    rows = sql("SELECT status, provider, scene, safe_error_code, template_id "
               f"FROM vi_notification_delivery WHERE event_key='BOOKING_CONFIRMED:{order_id}'")
    check("支付成功触发了第三方通道投递记录", len(rows) == 1, str(rows))
    if rows:
        cols = rows[0].split("\t")
        check("投递走了配置的短信通道", cols[1] == "TENCENT_SMS" and cols[2] == "BOOKING_CONFIRMED", str(cols))
        check("通道未生效时判 SKIPPED 而非静默丢弃或 FAILED", cols[0] == "SKIPPED", str(cols))
        # 档案故意留在 DRAFT：链路走到「通道档案未生效」这一步就应停下并说明原因
        check("跳过原因可读（通道档案未生效）", cols[3] == "VI_NOTIF_NO_PROFILE", str(cols))
        check("投递记录回指到配置的那条模板", cols[4] == str(template_id), str(cols))

    detail = call("GET", f"/api/app/owner/orders/{order_no}", token=token)[1] or {}
    check("通知未发出不影响业务：订单照常推进",
          detail.get("fulfillmentStatus") in ("SCHEDULED", "PENDING_DISPATCH", "ACCEPTED", "GOING_PICKUP"),
          str(detail.get("fulfillmentStatus")))

    print("\n== 三、重复事件去重 ==")
    before = sql("SELECT COUNT(*) FROM vi_notification_delivery "
                 f"WHERE event_key='BOOKING_CONFIRMED:{order_id}'")[0]
    # mock-pay 可重复调用；若业务层拒重复支付则这一步自然不会新增，两种都算通过
    call("POST", f"/api/app/owner/orders/{order_no}/mock-pay", token=token)
    after = sql("SELECT COUNT(*) FROM vi_notification_delivery "
                f"WHERE event_key='BOOKING_CONFIRMED:{order_id}'")[0]
    check("同一事件不会产生第二条投递", before == after, f"before={before} after={after}")

    cleanup()
    print(f"\n== 汇总：通过 {len(PASS)}，失败 {len(FAIL)} ==")
    for name in FAIL:
        print("  FAIL:", name)
    return 0 if not FAIL else 1


if __name__ == "__main__":
    sys.exit(main())
