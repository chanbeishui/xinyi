"""剩余项验收：D02 多轮扩圈 / Q03 播放心跳 / N01 订阅消息 / 部门数据权限。

全部断言基于真实 HTTP 调用与真实库状态，不使用 mock 返回值。
运行：python logs/verify_remaining.py
"""
import json
import subprocess
import time
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB_ARGS = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "xinyi"]

OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
PASS, FAIL = [], []
ORDER_SEQ = [0]
TEMP_ORDERS = []
TEMP_QUOTES = []

DEFAULT_STEPS = "3000,8000,20000"


def sql(statement):
    r = subprocess.run([MYSQL] + DB_ARGS + ["-N", "-B", "-e", statement], capture_output=True, text=True)
    err = "\n".join(l for l in r.stderr.splitlines() if "Warning" not in l and "Using a password" not in l)
    if r.returncode != 0 or err.strip():
        raise RuntimeError(f"SQL 失败：{err.strip()}\n{statement}")
    return r.stdout.strip()


def call(method, path, body=None, token=None, mini=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    if mini:
        req.add_header("X-Mini-Token", mini)
    try:
        with OPENER.open(req, timeout=30) as resp:
            text = resp.read().decode()
            return resp.status, (json.loads(text) if text else None)
    except urllib.error.HTTPError as e:
        text = e.read().decode()
        try:
            payload = json.loads(text)
        except Exception:
            payload = text
        return e.code, payload


def check(label, ok, detail=""):
    (PASS if ok else FAIL).append(label)
    print(("  [OK]   " if ok else "  [FAIL] ") + label + ("" if ok or not detail else f"  -> {detail}"))


def admin_login():
    _, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    return body["token"]


def mini_login(code):
    _, body = call("POST", "/api/common/login/wechat", {"code": code, "deviceLabel": "verify"})
    return body["accessToken"]


# 派单半径只对「有位置」的司机生效（无位置视为无法判定距离），
# 所以扩圈场景必须先把司机位置摆好，否则结果取决于上一次跑脚本留下的位置数据。
FAR_POINT = (100.0, 20.0)          # 与取车点相距数千公里，任何档位都进不来
DRIVER_CODES = ("dev_driver_001", "dev_driver_002", "dev_flow_driver", "dev_owner_001")


def dispatchable_codes():
    """只保留当前真的建了代驾档案的 dev 司机。

    dev_flow_driver 的档案由 verify_driver_flow.py 创建、也在它结束时清理掉；
    本脚本若强行假设它一定存在，先跑过 driver_flow 的机器上 D02 必然失败。
    没有档案的账号本来就进不了派单候选，把它们算进"可派单司机"是错的。
    """
    rows = sql("""SELECT a.open_id FROM vi_account a JOIN vi_driver_profile d ON d.account_id = a.id
                  WHERE a.open_id IN ('dev_driver_001','dev_driver_002','dev_flow_driver','dev_owner_001')""")
    present = {line.strip() for line in rows.splitlines() if line.strip()}
    return tuple(c for c in DRIVER_CODES if c in present)


def seed_driver_location(code, lng, lat):
    """用司机自己的令牌上报位置，避免直接写加密列"""
    try:
        token = mini_login(code)
    except Exception:
        return False
    code_, _ = call("POST", "/api/app/driver/location",
                    {"lng": lng, "lat": lat, "accuracyMeter": 10}, mini=token)
    return code_ == 200


def set_config(token, **kv):
    """走管理端接口修改配置，确保框架缓存同步失效。"""
    return call("PUT", "/api/admin/config", kv, token=token)


# --------------------------------------------------------------------- 夹具
def fixture(token=None):
    # 1) 代驾 1 需要一条资格申请，才能进入培训流程
    if sql("SELECT COUNT(1) FROM vi_driver_application WHERE driver_id = 1") == "0":
        sql("""INSERT INTO vi_driver_application (driver_id, status, submitted_at, created_at, updated_at, version)
               VALUES (1, 'SUBMITTED', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)""")
    # 2) 复位订阅开关，避免上一轮中断残留
    sql("UPDATE sys_config SET config_value = 'true' WHERE config_key = 'rongli.subscribe.enabled'")
    # 3) 释放占用，避免派单被占位挡住
    sql("DELETE FROM vi_driver_occupancy")
    sql("""UPDATE vi_dispatch_invite SET status = 'REVOKED', updated_at = UTC_TIMESTAMP(3), version = version + 1
           WHERE status = 'WAIT_RESPONSE'""")
    # 3) 复位业务配置（走接口，确保框架缓存同步）
    if token:
        set_config(token, radiusSteps=DEFAULT_STEPS, subscribeTemplateDispatchInvite="",
                   subscribeEnabled="true")
    # 4) 部门权限测试账号：独立角色授予全部菜单权限，但 user_id != 1 不触发超管旁路
    if sql("SELECT COUNT(1) FROM sys_role WHERE role_id = 9102") == "0":
        sql("""INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope,
                     menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
               VALUES (9102, '部门范围测试', 'scope_test', 999, '1', 1, 1, '0', '0', 'admin', UTC_TIMESTAMP(),
                       '自动化验收临时角色')""")
        sql("INSERT INTO sys_role_menu (role_id, menu_id) SELECT 9102, menu_id FROM sys_menu")
    if sql("SELECT COUNT(1) FROM sys_user WHERE user_id = 9101") == "0":
        pwd = sql("SELECT password FROM sys_user WHERE user_id = 1")
        sql(f"""INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, password, status,
                     create_by, create_time, remark)
                VALUES (9101, 104, 'scope_test', '部门范围测试', '00', '{pwd}', '0',
                        'admin', UTC_TIMESTAMP(), '自动化验收临时账号')""")
        sql("INSERT INTO sys_user_role (user_id, role_id) VALUES (9101, 9102)")
        sql("""INSERT INTO sys_user_dept (user_id, dept_id, create_by, create_time)
               VALUES (9101, 104, 'admin', UTC_TIMESTAMP())""")


def make_order(suffix, far=False):
    """克隆一条订单作为派单测试数据，返回 order_no。"""
    ORDER_SEQ[0] += 1
    order_no = f"RLVERIFY{suffix}{int(time.time()) % 100000}{ORDER_SEQ[0]}"
    sql("""INSERT INTO vi_quote (owner_account_id, station_id, vehicle_snapshot, address_snapshot_cipher,
                 pickup_slot_id, receive_slot_id, rule_id, route_snapshot, amount_fen, items_json, expires_at,
                 created_at, updated_at, version)
           SELECT owner_account_id, station_id, vehicle_snapshot, address_snapshot_cipher, pickup_slot_id,
                  receive_slot_id, rule_id, route_snapshot, amount_fen, items_json,
                  DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 HOUR), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0
           FROM vi_quote ORDER BY id DESC LIMIT 1""")
    quote_id = sql("SELECT MAX(id) FROM vi_quote")
    TEMP_QUOTES.append(quote_id)
    sql(f"""INSERT INTO vi_order (order_no, owner_account_id, vehicle_id, station_id, dept_id, quote_id,
                 vehicle_snapshot, fulfillment_status, cancel_status, custody_type, custody_ref_id,
                 pickup_starts_at, pickup_ends_at, pay_expires_at, dispatch_at, total_fen, terms_version,
                 dispatch_config_version, created_at, updated_at, version)
           SELECT '{order_no}', owner_account_id, vehicle_id, station_id, dept_id, {quote_id}, vehicle_snapshot,
                  'SCHEDULED', 'NONE', 'OWNER', owner_account_id,
                  UTC_TIMESTAMP(3), DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 2 HOUR), UTC_TIMESTAMP(3),
                  DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 SECOND), total_fen, 'v1', 0,
                  UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0
           FROM vi_order ORDER BY id DESC LIMIT 1""")
    if far:
        # 把取车点挪到 ~50km 外，用于区分"第 1 档无人、第 2 档命中"
        sql(f"""UPDATE vi_order SET vehicle_snapshot = JSON_SET(vehicle_snapshot, '$.lng', 111.4254, '$.lat', 21.6629)
               WHERE order_no = '{order_no}'""")
    TEMP_ORDERS.append(order_no)
    return order_no


def order_id(order_no):
    return sql(f"SELECT id FROM vi_order WHERE order_no = '{order_no}'")


# --------------------------------------------------------------------- D02
def test_d02(token):
    print("== D02 多轮扩圈")
    code, conf = call("GET", "/api/admin/dispatch/config", token=token)
    steps = (conf or {}).get("radiusSteps")
    check("派单配置下发半径阶梯", code == 200 and steps == [3000, 8000, 20000], f"{code} {steps}")

    # ---- 场景 A：三档半径内都没有候选 → 逐轮扩圈后转人工
    code, body = set_config(token, radiusSteps="1,2,3")
    check("扩圈阶梯可通过管理端配置", code == 200 and body.get("radiusSteps") == [1, 2, 3], f"{code} {body}")

    # 取车点同样挪远，确保 1/2/3 米三档半径内都没有候选（否则车就在取车点旁边，
    # 第 1 轮直接命中，测不出"逐轮扩圈直到耗尽"）
    codes = dispatchable_codes()
    seeded = [seed_driver_location(code, *FAR_POINT) for code in codes]
    check("可派单司机均已上报远端位置", bool(codes) and all(seeded),
          f"seed={seeded} 参与={list(codes)} 跳过={[c for c in DRIVER_CODES if c not in codes]}")
    order_a = make_order("A", far=True)
    oid_a = order_id(order_a)
    code, res = call("POST", f"/api/admin/dispatch/orders/{order_a}/dispatch-now", {}, token=token)
    check("立即派单接口可用", code == 200, f"{code} {res}")

    status = sql(f"SELECT fulfillment_status FROM vi_order WHERE id = {oid_a}")
    check("无候选时订单转入待人工安排", status == "MANUAL_DISPATCH", f"status={status}")

    expands = int(sql(f"SELECT COUNT(1) FROM vi_event_log WHERE order_id = {oid_a} AND action = 'DISPATCH_EXPAND'") or 0)
    check("按阶梯扩圈 2 次（3 档半径）", expands == 2, f"expand={expands}")

    reason = sql(f"""SELECT reason FROM vi_event_log WHERE order_id = {oid_a} AND action = 'NO_CANDIDATE'
                     ORDER BY id DESC LIMIT 1""")
    check("转人工原因为扩圈耗尽", "扩圈 3 轮" in reason, reason)

    rounds = sql(f"""SELECT GROUP_CONCAT(JSON_EXTRACT(after_summary, '$.round')) FROM vi_event_log
                     WHERE order_id = {oid_a} AND action = 'DISPATCH_EXPAND'""")
    check("扩圈事件记录轮次 2/3", '"2"' in rounds and '"3"' in rounds, rounds)

    # ---- 场景 B：第 1 档无人、第 2 档命中 → 邀约应携带第 2 轮
    # 司机 1 放到取车点东侧约 1km：1m 档进不来，200km 档命中，轮次稳定为 2
    check("司机 1 位置已置为取车点附近",
          seed_driver_location("dev_driver_001", 111.4354, 21.6629))
    sql("DELETE FROM vi_driver_occupancy WHERE driver_id = 1")
    code, body = set_config(token, radiusSteps="1,200000")
    check("阶梯可放宽到 200km", code == 200 and body.get("radiusSteps") == [1, 200000], f"{code} {body}")

    order_b = make_order("B", far=True)
    oid_b = order_id(order_b)
    code, res = call("POST", f"/api/admin/dispatch/orders/{order_b}/dispatch-now", {}, token=token)
    check("第 2 轮命中后发起邀约", code == 200, f"{code} {res}")
    round_no = sql(f"SELECT COALESCE(MAX(round_no), 0) FROM vi_dispatch_invite WHERE order_id = {oid_b}")
    check("自动邀约携带扩圈轮次 2", round_no == "2", f"round_no={round_no}")

    code, invites = call("GET", f"/api/admin/dispatch/invites?pageNum=1&pageSize=5&orderId={oid_b}", token=token)
    first = ((invites or {}).get("rows") or [{}])[0]
    check("邀约列表回显轮次", first.get("roundNo") == 2,
          f"roundNo={first.get('roundNo')} {str(invites)[:120]}")

    # 人工邀约：轮次取当前最大轮次
    driver_id = sql("SELECT id FROM vi_driver_profile WHERE qualification_status = 'APPROVED' ORDER BY id LIMIT 1")
    sql("DELETE FROM vi_driver_occupancy WHERE driver_id = %s" % driver_id)
    sql("""UPDATE vi_dispatch_invite SET status = 'REVOKED', updated_at = UTC_TIMESTAMP(3), version = version + 1
           WHERE order_id = %s AND status = 'WAIT_RESPONSE'""" % oid_b)
    code, res = call("POST", "/api/admin/dispatch/manual-invites",
                     {"orderNo": order_b, "driverId": driver_id, "reason": "验收：人工邀约"}, token=token)
    check("人工邀约可用", code == 200, f"{code} {res}")
    manual_round = sql(f"""SELECT round_no FROM vi_dispatch_invite WHERE order_id = {oid_b} AND source = 'MANUAL'
                           ORDER BY id DESC LIMIT 1""")
    check("人工邀约写入当前轮次", manual_round == "2", f"round_no={manual_round}")

    # 还原
    set_config(token, radiusSteps=DEFAULT_STEPS)


# --------------------------------------------------------------------- Q03
def test_q03():
    print("== Q03 播放心跳")
    token = mini_login("dev_driver_001")
    _, rows = call("GET", "/api/app/driver/qualification/training", mini=token)
    if isinstance(rows, dict):
        rows = rows.get("rows") or []
    check("代驾可见课程", bool(rows), str(rows)[:120])
    if not rows:
        return
    tid = rows[0]["trainingId"]

    code, start = call("POST", f"/api/app/driver/qualification/training/{tid}/playback", {}, mini=token)
    check("开始学习会话成功（session_id 类型缺陷已修）",
          code == 200 and (start or {}).get("sessionId"), f"{code} {start}")
    session = (start or {}).get("sessionId")
    if not session:
        return

    beat = f"/api/app/driver/qualification/training/{tid}/progress"
    code, p1 = call("POST", beat, {"sessionId": session, "deltaSeconds": 5, "positionSeconds": 5, "sequence": 1},
                    mini=token)
    check("首次心跳计入有效学时", code == 200 and p1.get("heartbeatResult") == "ACCEPTED", f"{code} {p1}")

    code, p2 = call("POST", beat, {"sessionId": session, "deltaSeconds": 5, "positionSeconds": 10, "sequence": 1},
                    mini=token)
    check("重复序号不再累加（幂等）",
          code == 200 and p2.get("heartbeatResult") == "DUPLICATE" and p2.get("acceptedSeconds") == 0, f"{code} {p2}")

    code, p3 = call("POST", beat, {"sessionId": session, "deltaSeconds": 5, "positionSeconds": 600, "sequence": 2},
                    mini=token)
    check("播放位置跳变被拒（防快进）", code == 422, f"{code} {p3}")

    code, p4 = call("POST", beat, {"sessionId": "forged-session", "deltaSeconds": 5, "positionSeconds": 15,
                                   "sequence": 3}, mini=token)
    check("伪造会话被拒", code == 409, f"{code} {p4}")

    # 心跳超时：把 last_reported_at 回拨 5 分钟，采信上限应为阈值 30 秒
    sql("""UPDATE vi_training_playback SET last_reported_at = DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 300 SECOND)
           WHERE session_id = '%s'""" % session)
    code, p5 = call("POST", beat, {"sessionId": session, "deltaSeconds": 200, "positionSeconds": 36,
                                   "sequence": 4}, mini=token)
    check("心跳超阈值时按阈值封顶", code == 200 and p5.get("acceptedSeconds") == 30, f"{code} {p5}")

    gap = sql("SELECT COUNT(1) FROM vi_event_log WHERE action = 'HEARTBEAT_GAP' AND aggregate_id = %s" % tid)
    check("超时心跳留痕", int(gap) > 0, f"count={gap}")

    # 会话过期后不得继续计学时
    sql("""UPDATE vi_training_playback SET expires_at = DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 SECOND)
           WHERE session_id = '%s'""" % session)
    code, p6 = call("POST", beat, {"sessionId": session, "deltaSeconds": 5, "positionSeconds": 40,
                                   "sequence": 5}, mini=token)
    check("过期会话被拒", code == 409, f"{code} {p6}")


# --------------------------------------------------------------------- N01
def test_n01(token):
    print("== N01 订阅消息")
    code, body = set_config(token, subscribeTemplateDispatchInvite="TPL_VERIFY_001")
    check("订阅模板可通过管理端配置", code == 200, f"{code} {body}")

    driver_id = sql("SELECT id FROM vi_driver_profile WHERE qualification_status = 'APPROVED' ORDER BY id LIMIT 1")
    sql("DELETE FROM vi_driver_occupancy WHERE driver_id = %s" % driver_id)
    order_no = TEMP_ORDERS[0]
    oid = order_id(order_no)
    sql("""UPDATE vi_dispatch_invite SET status = 'REVOKED', updated_at = UTC_TIMESTAMP(3), version = version + 1
           WHERE order_id = %s AND status = 'WAIT_RESPONSE'""" % oid)
    sql("UPDATE vi_order SET fulfillment_status = 'MANUAL_DISPATCH', updated_at = UTC_TIMESTAMP(3) WHERE id = %s" % oid)
    before_event = int(sql("""SELECT COALESCE(MAX(id), 0) FROM vi_outbox WHERE event_type = 'SUBSCRIBE_PUSH'"""))
    code, res = call("POST", "/api/admin/dispatch/manual-invites",
                     {"orderNo": order_no, "driverId": driver_id, "reason": "验收：订阅消息"}, token=token)
    check("邀约触达站内信", code == 200, f"{code} {res}")

    event_id = sql("""SELECT COALESCE(MAX(id), 0) FROM vi_outbox WHERE event_type = 'SUBSCRIBE_PUSH'""")
    check("订阅事件已入 outbox 待投递", int(event_id) > before_event,
          f"before={before_event} after={event_id}")

    status = ""
    for _ in range(20):
        status = sql(f"SELECT status FROM vi_outbox WHERE id = {event_id}")
        if status in ("DONE", "FAILED"):
            break
        time.sleep(2)
    check("订阅事件被 outbox 消费完成", status == "DONE", f"event={event_id} status={status}")
    check("无订阅死信", status != "FAILED", f"status={status}")

    payload = sql(f"SELECT payload FROM vi_outbox WHERE id = {event_id}")
    notification_id = json.loads(payload).get("notificationId") if payload else None
    channel = sql(f"SELECT channel FROM vi_notification WHERE id = {notification_id}") if notification_id else ""
    check("站内信标记为已投订阅消息", channel == "SUBSCRIBE",
          f"notification={notification_id} channel={channel}")

    inbox_only = sql("SELECT COUNT(1) FROM vi_notification WHERE channel = 'INBOX'")
    check("无模板场景仍只发站内信", int(inbox_only) > 0, f"count={inbox_only}")

    code, body = set_config(token, subscribeEnabled="false")
    disabled = sql("SELECT config_value FROM sys_config WHERE config_key = 'rongli.subscribe.enabled'")
    check("订阅总开关可关闭", code == 200 and disabled == "false", f"{code} enabled={disabled}")
    set_config(token, subscribeEnabled="true", subscribeTemplateDispatchInvite="")


# --------------------------------------------------------------------- 部门权限
def test_dept(token):
    print("== 部门数据权限")
    _, all_orders = call("GET", "/api/admin/orders?pageNum=1&pageSize=10", token=token)
    _, all_drivers = call("GET", "/api/admin/drivers?pageNum=1&pageSize=10", token=token)
    _, all_vehicles = call("GET", "/api/admin/vehicles?pageNum=1&pageSize=10", token=token)
    _, all_stats = call("GET", "/api/admin/statistics?range=DAY", token=token)

    code, body = call("POST", "/login", {"username": "scope_test", "password": "admin123"})
    check("部门测试账号可登录（非超管 + 全功能权限）", code == 200 and body.get("token"), f"{code} {body}")
    if code != 200:
        return
    stoken = body["token"]

    _, s_orders = call("GET", "/api/admin/orders?pageNum=1&pageSize=10", token=stoken)
    check("订单按部门收敛（104 部门无订单）",
          s_orders.get("total") == 0 and all_orders.get("total", 0) > 0,
          f"scope={s_orders.get('total')} admin={all_orders.get('total')}")

    _, s_stats = call("GET", "/api/admin/statistics?range=DAY", token=stoken)
    admin_kpi = (all_stats.get("kpi") or {}).get("sampleOrders")
    scope_kpi = (s_stats.get("kpi") or {}).get("sampleOrders")
    check("统计 KPI 按部门收敛", scope_kpi == 0 and admin_kpi > 0, f"scope={scope_kpi} admin={admin_kpi}")

    check("统计站点概况按部门收敛", len(s_stats.get("stationStats") or []) == 0,
          str(s_stats.get("stationStats"))[:80])

    _, s_workbench = call("GET", "/api/admin/dashboard", token=stoken)
    check("工作台指标按部门收敛", (s_workbench or {}).get("activeOrders") == 0, str(s_workbench)[:100])

    _, s_drivers = call("GET", "/api/admin/drivers?pageNum=1&pageSize=10", token=stoken)
    check("司机主数据共享池保持可见（dept_id 为空不收敛）",
          s_drivers.get("total") == all_drivers.get("total"),
          f"scope={s_drivers.get('total')} admin={all_drivers.get('total')}")

    _, s_vehicles = call("GET", "/api/admin/vehicles?pageNum=1&pageSize=10", token=stoken)
    check("车辆主数据共享池保持可见",
          s_vehicles.get("total") == all_vehicles.get("total"),
          f"scope={s_vehicles.get('total')} admin={all_vehicles.get('total')}")


def cleanup(token):
    try:
        set_config(token, radiusSteps=DEFAULT_STEPS, subscribeTemplateDispatchInvite="", subscribeEnabled="true")
    except Exception:
        pass
    for order_no in TEMP_ORDERS:
        oid = sql(f"SELECT id FROM vi_order WHERE order_no = '{order_no}'")
        if not oid:
            continue
        sql(f"DELETE FROM vi_dispatch_invite WHERE order_id = {oid}")
        sql(f"DELETE FROM vi_driver_occupancy WHERE order_id = {oid}")
        sql(f"DELETE FROM vi_event_log WHERE order_id = {oid}")
        sql(f"DELETE FROM vi_outbox WHERE aggregate_type = 'ORDER' AND aggregate_id = {oid}")
        # 支付单先于订单删除，否则留下孤儿支付单（本库无外键约束，不会级联）
        sql(f"DELETE FROM vi_payment_order WHERE order_id = {oid}")
        sql(f"DELETE FROM vi_order WHERE id = {oid}")
    for qid in TEMP_QUOTES:
        sql(f"DELETE FROM vi_quote WHERE id = {qid}")
    sql("DELETE FROM sys_role_menu WHERE role_id = 9102")
    sql("DELETE FROM sys_role WHERE role_id = 9102")
    sql("DELETE FROM sys_user_role WHERE user_id = 9101")
    sql("DELETE FROM sys_user_dept WHERE user_id = 9101")
    sql("DELETE FROM sys_user WHERE user_id = 9101")


def main():
    print("== 夹具准备")
    token = admin_login()
    fixture(token)
    try:
        test_d02(token)
        test_n01(token)
        test_q03()
        test_dept(token)
    finally:
        cleanup(token)
        print("== 夹具清理完成")
    print(f"\n通过 {len(PASS)} 项，失败 {len(FAIL)} 项")
    for f in FAIL:
        print("  FAILED:", f)
    raise SystemExit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
