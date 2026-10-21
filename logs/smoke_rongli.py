"""荣利预约主链路冒烟测试：车主下单支付 → 派单邀约 → 代驾接单 → 交接留证。

直接使用标准库 urllib，避免额外依赖；所有断言失败会抛出并打印响应体。

说明：自动派单在「接车时间 - 提前量(默认30分钟)」触发，刚下单时派单时点必然在未来。
为了不必等待，脚本用一条 SQL 把本单 dispatch_at 提到当前时刻，作为端到端测试的前置条件；
随后仍由定时任务走完整的候选筛选 → 邀约 → 接单流程。
"""
import json
import subprocess
import time
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:8080"

MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB_ARGS = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "xinyi"]


def sql(statement):
    """执行一条 SQL（仅用于测试前置条件）。出错直接抛出，避免清理静默失败。"""
    r = subprocess.run([MYSQL] + DB_ARGS + ["-N", "-B", "-e", statement],
                       capture_output=True, text=True)
    err = "\n".join(l for l in r.stderr.splitlines() if "Warning" not in l)
    if r.returncode != 0 or err.strip():
        raise RuntimeError(f"SQL 执行失败：{err.strip()}\n{statement}")
    return r.stdout

# 本机环境存在 HTTP_PROXY 指向 127.0.0.1:1122，会让 localhost 请求走代理并 502。
# 强制清空代理，全部直连。
_OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
urllib.request.install_opener(_OPENER)


def call(method, path, body=None, token=None, expect=200):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("X-Mini-Token", token)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            text = resp.read().decode("utf-8")
            payload = json.loads(text) if text else None
            _assert_biz_ok(f"{method} {path}", resp.status, payload)
            return resp.status, payload
    except urllib.error.HTTPError as e:
        text = e.read().decode("utf-8")
        payload = None
        try:
            payload = json.loads(text)
        except Exception:
            payload = text
        if expect and e.code != expect:
            print(f"!! {method} {path} -> {e.code} {payload}")
            raise
        return e.code, payload


def _assert_biz_ok(label, status, payload):
    """框架的全局异常处理会返回 HTTP 200 + body.code，需要单独识别。"""
    if isinstance(payload, dict) and "code" in payload and payload.get("code") != 200 \
            and set(payload.keys()) <= {"code", "msg", "data"}:
        raise AssertionError(f"!! {label} -> HTTP {status} 业务错误 {payload}")


def login(code):
    status, body = call("POST", "/api/common/login/wechat", {"code": code, "deviceLabel": "smoke"})
    assert status == 200, body
    return body["accessToken"], body


def reset_leftovers():
    """清理上一轮中断留下的承接与占用，保证脚本可重复运行。

    仅影响联调库：把仍在履约中的遗留订单置为已关闭，并释放代驾占用。
    """
    sql("""
        UPDATE vi_driver_assignment SET ended_at = UTC_TIMESTAMP(3), end_reason = 'CANCELLED',
               updated_at = UTC_TIMESTAMP(3), version = version + 1
        WHERE ended_at IS NULL
    """)
    sql("DELETE FROM vi_driver_occupancy")
    sql("""
        UPDATE vi_order SET fulfillment_status = 'CLOSED', updated_at = UTC_TIMESTAMP(3), version = version + 1
        WHERE fulfillment_status IN ('ACCEPTED','GOING_PICKUP','ARRIVED_PICKUP','PICKED_UP',
              'ARRIVED_STATION','INSPECTING','INSPECTED','CAR_TAKEN','WAIT_CONFIRM','MANUAL_DISPATCH','DISPATCHING')
    """)
    sql("""
        UPDATE vi_dispatch_invite SET status = 'EXPIRED', updated_at = UTC_TIMESTAMP(3), version = version + 1
        WHERE status = 'WAIT_RESPONSE'
    """)
    print("   （已清理上一轮遗留的履约中订单与代驾占用）")


def main():
    print("== 0. 清理遗留状态")
    reset_leftovers()

    print("== 1. 车主登录")
    owner_token, session = login("dev_owner_001")
    print("   roles =", session["profile"]["roles"])

    print("== 2. 车辆与地址")
    _, vehicles = call("GET", "/api/app/owner/vehicles", token=owner_token)
    if not vehicles:
        _, v = call("POST", "/api/app/owner/vehicles", {
            "plateNo": "粤K88001",
            "vehicleTypeCode": "sedan",
            "brandModel": "大众 朗逸",
            "vinLast4": "8821",
            "registeredOn": "2021-06-01",
            "useType": "NON_OPERATING",
            "isDefault": True,
        }, token=owner_token)
        vehicles = [v]
    vehicle_id = vehicles[0]["vehicleId"]

    _, addresses = call("GET", "/api/app/owner/addresses", token=owner_token)
    if not addresses:
        _, a = call("POST", "/api/app/owner/addresses", {
            "label": "家",
            "address": "茂名市茂南区站北路 8 号",
            "lng": 110.9254,
            "lat": 21.6629,
            "isDefault": True,
        }, token=owner_token)
        addresses = [a]
    address_id = addresses[0]["addressId"]

    print("== 3. 可约时段")
    _, slots = call("GET", "/api/app/owner/schedule/slots", token=owner_token)
    usable = [s for s in slots if s["selectable"]]
    assert usable, "没有可用时段，请先检查 vi_station_slot 数据"
    slot = usable[0]
    print("   选中时段", slot["label"], "剩余", slot["remaining"])

    print("== 4. 报价")
    _, quote = call("POST", "/api/app/owner/orders/quote", {
        "vehicleId": vehicle_id,
        "addressId": address_id,
        "pickupSlotId": slot["slotId"],
        "inspectionTypeCode": "ANNUAL",
    }, token=owner_token)
    print("   金额 =", quote["amountYuan"], "距离 =", quote["distanceMeters"], "米")

    print("== 5. 下单")
    _, order = call("POST", "/api/app/owner/orders", {
        "quoteId": quote["quoteId"],
        "vehicleId": vehicle_id,
        "addressId": address_id,
        "contactName": "王先生",
        "contactPhone": "13800000001",
    }, token=owner_token)
    order_no = order["orderNo"]
    print("   订单号", order_no, order["fulfillmentStatusLabel"])

    print("== 6. 支付")
    _, prepay = call("POST", f"/api/app/owner/orders/{order_no}/prepay", {}, token=owner_token)
    print("   通道", prepay["channel"])
    _, paid = call("POST", f"/api/app/owner/orders/{order_no}/mock-pay", {}, token=owner_token)
    print("   支付后状态", paid["fulfillmentStatusLabel"])

    print("== 7. 代驾登录并接单")
    # 前置：把派单时点提到当前，让定时任务（60 秒一轮）立即发起邀约
    sql(f"UPDATE vi_order SET dispatch_at = UTC_TIMESTAMP(3) WHERE order_no = '{order_no}'")

    # 派单按游标轮转，可能落到任一在线代驾；依次试探各演示代驾谁收到了邀约
    driver_tokens = {}
    for code in ("dev_driver_001", "dev_driver_002"):
        token, session = login(code)
        call("POST", "/api/app/driver/location", {"lng": 110.9254, "lat": 21.6629}, token=token)
        call("POST", "/api/app/driver/listening", {"enabled": True}, token=token)
        driver_tokens[code] = (token, session)

    driver_token = driver_session = None
    invites = {"total": 0, "rows": []}
    for _ in range(45):
        for code, (token, session) in driver_tokens.items():
            _, mine = call("GET", "/api/app/driver/invites", token=token)
            hit = [r for r in mine.get("rows", []) if r.get("orderNo") == order_no]
            if hit:
                driver_token, driver_session = token, session
                invites = {"total": len(hit), "rows": hit}
                break
        if invites.get("total"):
            break
        time.sleep(2)

    print("   邀约数", invites.get("total"))
    if not invites.get("total"):
        print("   !! 没有邀约：请确认派单定时任务与代驾资格状态")
        return
    print("   代驾 =", driver_session["profile"]["nickname"], driver_session["profile"]["roles"])
    invite_id = invites["rows"][0]["inviteId"]
    _, accepted = call("POST", f"/api/app/driver/invites/{invite_id}/response", {"accept": True}, token=driver_token)
    print("   接单结果", accepted.get("accepted"))

    print("== 8. 代驾出发 → 到达接车点")
    call("POST", f"/api/app/driver/orders/{order_no}/depart", {}, token=driver_token)
    _, arrived = call("POST", f"/api/app/driver/orders/{order_no}/arrive-pickup", {}, token=driver_token)
    print("   状态", arrived["fulfillmentStatusLabel"])

    print("== 9. 车主生成交接码")
    _, handover = call("POST", f"/api/common/orders/{order_no}/handovers",
                       {"handoverType": "OWNER_TO_DRIVER"}, token=owner_token)
    _, secret = call("GET", f"/api/common/orders/{order_no}/handovers/{handover['handoverId']}/secret",
                     token=owner_token)
    print("   交接码", secret["code"])

    print("== 10. 留证（六类照片合成文件）")
    photos = []
    import base64
    png = base64.b64decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    )
    photo_types = ["FRONT", "BACK", "LEFT", "RIGHT", "DASHBOARD", "INTERIOR"]
    for ptype in photo_types:
        _, up = call("POST", "/api/common/uploads", {
            "purpose": "EVIDENCE_PHOTO",
            "mimeType": "image/png",
            "size": len(png),
            "orderNo": order_no,
        }, token=driver_token)
        # multipart 上传
        boundary = "----smoke" + str(int(time.time() * 1000))
        body = b""
        body += f"--{boundary}\r\n".encode()
        body += b'Content-Disposition: form-data; name="file"; filename="p.png"\r\n'
        body += b"Content-Type: image/png\r\n\r\n"
        body += png + b"\r\n"
        body += f"--{boundary}--\r\n".encode()
        req = urllib.request.Request(BASE + f"/api/common/uploads/{up['uploadId']}/content", data=body, method="POST")
        req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
        req.add_header("X-Mini-Token", driver_token)
        with urllib.request.urlopen(req, timeout=30) as resp:
            json.loads(resp.read().decode("utf-8"))
        _, file_view = call("POST", f"/api/common/uploads/{up['uploadId']}/complete",
                            {"size": len(png)}, token=driver_token)
        photos.append(file_view["fileId"])

    _, group = call("POST", f"/api/app/driver/orders/{order_no}/evidence",
                    {"nodeNo": 1, "mileageKm": 38652}, token=driver_token)
    group_id = group["groupId"]
    for ptype, file_id in zip(photo_types, photos):
        call("POST", f"/api/app/driver/orders/{order_no}/evidence/{group_id}/photos", {
            "photoType": ptype,
            "fileId": file_id,
            "clientCaptureId": f"{ptype}-{int(time.time() * 1000)}",
            "capturedAt": time.strftime("%Y-%m-%dT%H:%M:%SZ"),
        }, token=driver_token)
    call("PUT", f"/api/app/driver/orders/{order_no}/evidence/{group_id}/mileage",
         {"mileageKm": "38652", "source": "DASHBOARD", "reason": "冒烟测试"}, token=driver_token)
    _, sealed = call("POST", f"/api/app/driver/orders/{order_no}/evidence/{group_id}/ready", {}, token=driver_token)
    print("   留证状态", sealed["status"], "照片", sealed["photoCount"])

    print("== 11. 代驾确认交接")
    _, confirmed = call("POST", f"/api/common/orders/{order_no}/handovers/{handover['handoverId']}/confirm",
                        {"code": secret["code"], "version": arrived["version"]}, token=driver_token)
    print("   交接", confirmed.get("fulfillmentStatus"), "保管方", confirmed.get("custodyType"))

    _, detail = call("GET", f"/api/app/owner/orders/{order_no}", token=owner_token)
    print("== 12. 最终订单状态", detail["fulfillmentStatusLabel"])

    print("== 13. 代驾送达检测点 → 站点收车")
    call("POST", f"/api/app/driver/orders/{order_no}/arrive-station", {}, token=driver_token)

    station_token, station_session = login("dev_station_001")
    print("   站点视角 =", station_session["profile"]["roles"])
    station_token = call("POST", "/api/common/session/role", {"role": "STATION"},
                         token=station_token)[1]["accessToken"]

    # 站点发起收车交接（站点为核验方），司机出示交接码
    _, h2 = call("POST", f"/api/common/orders/{order_no}/handovers",
                 {"handoverType": "DRIVER_TO_STATION"}, token=station_token)
    _, s2 = call("GET", f"/api/common/orders/{order_no}/handovers/{h2['handoverId']}/secret",
                 token=driver_token)
    _, received = call("POST", f"/api/common/orders/{order_no}/handovers/{h2['handoverId']}/confirm",
                       {"code": s2["code"]}, token=station_token)
    print("   收车后状态", received.get("fulfillmentStatus"))

    print("== 14. 行驶证登记核验")
    _, license = call("PUT", f"/api/app/station/orders/{order_no}/license", {
        "ownerName": "王先生",
        "plateNo": "粤K88001",
        "vin": "LSVAM4180E2108821",
        "registeredOn": "2021-06-01",
        "verified": True,
    }, token=station_token)
    print("   核验状态", license.get("reviewStatus"))

    print("== 15. 检测结果登记")
    _, result = call("POST", f"/api/app/station/orders/{order_no}/results", {
        "conclusion": "PASS",
        "items": json.dumps([
            {"code": "BRAKE", "name": "制动性能", "conclusion": "PASS"},
            {"code": "LIGHT", "name": "灯光", "conclusion": "PASS"},
        ], ensure_ascii=False),
    }, token=station_token)
    print("   检测结论", result.get("conclusion"), result.get("conclusionLabel"))

    print("== 16. 站点交车 → 代驾取车")
    _, h3 = call("POST", f"/api/common/orders/{order_no}/handovers",
                 {"handoverType": "STATION_TO_DRIVER"}, token=station_token)
    _, s3 = call("GET", f"/api/common/orders/{order_no}/handovers/{h3['handoverId']}/secret",
                 token=station_token)
    _, taken = call("POST", f"/api/common/orders/{order_no}/handovers/{h3['handoverId']}/confirm",
                    {"code": s3["code"]}, token=driver_token)
    print("   取车后状态", taken.get("fulfillmentStatus"))

    print("== 16. 代驾归还 → 车主确认收车")
    _, h4 = call("POST", f"/api/common/orders/{order_no}/handovers",
                 {"handoverType": "DRIVER_TO_OWNER"}, token=driver_token)
    _, s4 = call("GET", f"/api/common/orders/{order_no}/handovers/{h4['handoverId']}/secret",
                 token=driver_token)
    _, returned = call("POST", f"/api/common/orders/{order_no}/handovers/{h4['handoverId']}/confirm",
                       {"code": s4["code"]}, token=owner_token)
    print("   归还后状态", returned.get("fulfillmentStatus"), "保管方", returned.get("custodyType"))

    print("== 17. 售后申请（归还后、确认前）")
    _, after_sale = call("POST", f"/api/app/owner/orders/{order_no}/after-sales", {
        "kind": "REFUND_ONLY",
        "requestedFen": 5000,
        "reason": "冒烟测试：申请部分退款",
    }, token=owner_token)
    print("   售后单", after_sale.get("caseNo"), after_sale.get("caseStatusLabel"))

    print("== 18. 车主确认服务")
    _, confirmed_service = call("POST", f"/api/app/owner/orders/{order_no}/service-confirmation",
                                {"satisfied": True}, token=owner_token)
    print("   终态", confirmed_service.get("fulfillmentStatusLabel") or confirmed_service.get("fulfillmentStatus"))

    print("== 19. 时间线")
    _, timeline = call("GET", f"/api/app/owner/orders/{order_no}/timeline", token=owner_token)
    print("   节点数", timeline["total"])
    for n in timeline["nodes"][-8:]:
        print("   -", n["label"], n["at"])

    _, detail2 = call("GET", f"/api/app/owner/orders/{order_no}", token=owner_token)
    print("== 20. 订单终态", detail2["fulfillmentStatusLabel"], "| 可执行操作",
          ",".join(detail2.get("allowedActions") or []) or "无")

    print("\n主链路冒烟测试完成：", order_no)


if __name__ == "__main__":
    main()
