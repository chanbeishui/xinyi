"""手机号授权登录链路验收。

覆盖本轮改造的核心规则：
  1) 手机号授权登录可用（联调模式用手机号代替微信 code）
  2) 新账号默认只有车主身份
  3) 手机号与框架员工账号一致时自动绑定 openid，无需小程序申请
  4) 未配置检测点时不产生检测员工视角；后台配置后才出现
  5) 代驾视角仍需申请并通过审核
  6) 同一员工换微信登录时**拒绝静默迁移**并留冲突审计（V6.4 跨账号身份转移需后台核实）
  7) 短信兜底绑定手机号不产生员工关联（只完成普通手机号绑定）
  8) 后台「新增站点-员工绑定」相关接口可用

前置：后端 18080 运行最新代码，MySQL 127.0.0.1:3307/xinyi 可访问。
用法：python logs/verify_phone_login.py
"""
import json
import subprocess
import sys
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui", "--default-character-set=utf8mb4", "-N", "-B", "xinyi"]
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

TEST_USER_ID = 9201
STAFF_USER_ID = 9211
TEST_USER_NAME = "phone_login_test"
TEST_PHONE = "13900000001"
STAFF_PHONE = "13900000002"       # 无检测点授权的员工
STATION_ID = 1

PASS = []
FAIL = []


def sql(statement):
    out = subprocess.run([MYSQL] + DB + ["-e", statement], capture_output=True, text=True, encoding="utf-8")
    text = (out.stdout or "").strip()
    lines = [l for l in text.splitlines() if "Warning" not in l]
    return lines


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


def check(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(f"[{'PASS' if ok else 'FAIL'}] {name}" + (f"  {detail}" if detail and not ok else ""))


def admin_login():
    _, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    return body.get("token")


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


def mini_login(code, phone_code=None):
    body = {"code": code, "deviceLabel": "phone-verify"}
    if phone_code:
        body["phoneCode"] = phone_code
    return call("POST", "/api/common/login/wechat", body)


def cleanup():
    """只清本脚本造的数据，避免误删正常业务记录"""
    sql(f"DELETE b FROM vi_session_token b JOIN vi_session s ON s.id=b.session_id WHERE s.account_id IN "
        f"(SELECT id FROM vi_account WHERE open_id LIKE 'dev_phone_%')")
    sql("DELETE FROM vi_session WHERE account_id IN (SELECT id FROM vi_account WHERE open_id LIKE 'dev_phone_%')")
    sql("DELETE FROM vi_employee_binding WHERE account_id IN (SELECT id FROM vi_account WHERE open_id LIKE 'dev_phone_%')")
    sql("DELETE FROM vi_employee_binding WHERE sys_user_id = %d" % TEST_USER_ID)
    sql("DELETE FROM vi_owner_profile WHERE account_id IN (SELECT id FROM vi_account WHERE open_id LIKE 'dev_phone_%')")
    sql("DELETE FROM vi_account_security WHERE account_id IN (SELECT id FROM vi_account WHERE open_id LIKE 'dev_phone_%')")
    sql("DELETE FROM vi_account WHERE open_id LIKE 'dev_phone_%'")
    sql(f"DELETE FROM vi_station_grant WHERE sys_user_id IN ({TEST_USER_ID}, {STAFF_USER_ID})")
    sql(f"DELETE FROM sys_user_role WHERE user_id IN ({TEST_USER_ID}, {STAFF_USER_ID})")
    sql(f"DELETE FROM sys_user WHERE user_id IN ({TEST_USER_ID}, {STAFF_USER_ID})")


def setup():
    cleanup()
    sql(f"""INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber,
             sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by,
             create_time, update_by, update_time, remark)
           VALUES ({TEST_USER_ID}, 103, '{TEST_USER_NAME}', '手机绑定测试员工', '00', '',
             '{TEST_PHONE}', '0', '', '', '0', '0', '127.0.0.1', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3),
             'verify', UTC_TIMESTAMP(3), '', UTC_TIMESTAMP(3), 'phone login verify')""")
    sql(f"""INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber,
             sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by,
             create_time, update_by, update_time, remark)
           VALUES ({STAFF_USER_ID}, 103, '{TEST_USER_NAME}_2', '无站点权限员工', '00', '',
             '{STAFF_PHONE}', '0', '', '', '0', '0', '127.0.0.1', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3),
             'verify', UTC_TIMESTAMP(3), '', UTC_TIMESTAMP(3), 'phone login verify')""")


def main():
    print(f"目标：{BASE}\n")
    setup()
    admin = admin_login()
    check("后台管理员可登录（用于配置站点授权）", bool(admin))

    # ---------------------------------------------------------------- 一、授权登录
    print("\n== 一、手机号授权登录 ==")
    code, body = mini_login("dev_phone_a", TEST_PHONE)
    ok = code == 200 and body.get("accessToken")
    check("手机号授权登录成功", ok, f"{code} {body}")
    token_a = body.get("accessToken")
    prof = body.get("profile", {})
    check("登录返回手机号已绑定", prof.get("phoneBound") is True, str(prof.get("phoneBound")))
    check("员工身份按手机号自动识别", prof.get("employeeBound") is True, str(prof))
    check("绑定来源标记为手机号", prof.get("employeeBindSource") == "PHONE", str(prof.get("employeeBindSource")))
    check("未配置检测点时不产生检测员工视角",
          prof.get("roles") == ["OWNER"], str(prof.get("roles")))
    check("新用户默认只有车主视角", body.get("activeRole") == "OWNER", str(body.get("activeRole")))

    binding = sql(f"SELECT sys_user_id, bound_by, source, phone_lookup IS NOT NULL, employee_auth_version IS NOT NULL "
                  f"FROM vi_employee_binding WHERE account_id = "
                  f"(SELECT id FROM vi_account WHERE open_id = 'dev_phone_a') AND revoked_at IS NULL")
    check("openid 已写入员工绑定关系", bool(binding) and binding[0].split("\t")[0] == str(TEST_USER_ID),
          str(binding))
    # V6.4：自动关联由系统发起，bound_by 必须为 NULL（挂管理员身份会被 ck_employee_link_source_actor 拒绝）
    check("自动绑定标记为系统发起(bound_by IS NULL)",
          bool(binding) and binding[0].split("\t")[1] == "NULL", str(binding))
    cols = binding[0].split("\t") if binding else []
    check("绑定来源为微信手机号唯一匹配", len(cols) > 2 and cols[2] == "WECHAT_PHONE_MATCH", str(cols))
    check("自动绑定已固化手机号HMAC与授权版本快照",
          len(cols) > 4 and cols[3] == "1" and cols[4] == "1", str(cols))

    phone_row = sql("SELECT phone_lookup IS NOT NULL FROM vi_account WHERE open_id = 'dev_phone_a'")
    check("手机号已加密落库", bool(phone_row) and phone_row[0] == "1", str(phone_row))

    # ---------------------------------------------------------------- 二、身份边界
    print("\n== 二、身份边界 ==")
    code, body = mini_login("dev_phone_b")
    check("无手机号登录仍可用（车主身份）", code == 200, f"{code} {body}")
    prof_b = body.get("profile", {})
    check("未授权手机号时无员工身份", prof_b.get("employeeBound") is False, str(prof_b))
    check("仅车主视角", prof_b.get("roles") == ["OWNER"], str(prof_b.get("roles")))

    code, body = call("POST", "/api/common/session/role", {"role": "DRIVER"}, token=body.get("accessToken"))
    check("未通过代驾审核不能切换代驾视角", code == 403, f"{code} {body}")

    code, body = call("POST", "/api/common/login/wechat", {"code": "dev_phone_c", "phoneCode": "123"})
    check("手机号格式非法被拒绝", code == 400, f"{code} {body}")

    # ---------------------------------------------------------------- 三、后台配站点
    print("\n== 三、后台新增站点绑定 ==")
    code, cands = admin_call("GET", "/api/admin/employee-candidates", token=admin,
                             params={"keyword": TEST_PHONE})
    ok = code == 200 and isinstance(cands, list) and any(c.get("userId") == str(TEST_USER_ID) for c in cands)
    check("后台可按手机号检索到员工", ok, f"{code} {cands}")

    code, res = admin_call("PUT", f"/api/admin/station-grants/{TEST_USER_ID}", token=admin,
                           body={"stationId": STATION_ID, "enabled": True, "reason": "验收：新增站点绑定"})
    check("后台新增站点绑定成功", code == 200, f"{code} {res}")

    code, sess = call("GET", "/api/common/session", token=token_a)
    roles = sess.get("profile", {}).get("roles", [])
    check("配置检测点后出现检测员工视角", "STATION" in roles, str(roles))
    check("会话返回已授权站点", len(sess.get("stations", [])) >= 1, str(sess.get("stations")))

    code, body = call("POST", "/api/common/session/role", {"role": "STATION"}, token=token_a)
    check("可切换到检测员工视角", code == 200, f"{code} {body}")
    token_station = body.get("accessToken")
    code, orders = call("GET", "/api/app/station/orders", token=token_station, params={"pageNum": 1, "pageSize": 5})
    check("检测员工可访问站点工单", code == 200, f"{code} {orders}")

    # ---------------------------------------------------------------- 四、会话内补授权
    print("\n== 四、登录后补授权 ==")
    code, body = mini_login("dev_phone_d")
    token_d = body.get("accessToken")
    check("先以车主身份登录", code == 200, f"{code}")
    code, view = call("POST", "/api/common/phone/wechat", {"phoneCode": STAFF_PHONE}, token=token_d)
    ok = code == 200 and view.get("profile", {}).get("employeeBound") is True
    check("会话内授权手机号后自动绑定员工", ok, f"{code} {view}")
    check("绑定变化后换发新令牌", bool(view.get("accessToken")), str(view.keys()))
    check("该员工无检测点授权仍是纯车主",
          view.get("profile", {}).get("roles") == ["OWNER"], str(view.get("profile", {}).get("roles")))

    # ---------------------------------------------------------------- 五、换微信不再静默迁移
    # V6.4：跨账号身份转移不属于普通操作。同一员工已关联其他微信账号时必须拒绝，
    # 不能让一次手机号授权就把别人的检测工作权限挪走。
    print("\n== 五、同一员工换微信登录（应拒绝静默迁移）==")
    code, body = mini_login("dev_phone_e", TEST_PHONE)
    check("同一手机号新微信可登录", code == 200, f"{code}")
    actives = sql(f"SELECT account_id FROM vi_employee_binding WHERE sys_user_id = {TEST_USER_ID} "
                  f"AND revoked_at IS NULL")
    check("同一员工仍只有一条有效绑定", len(actives) == 1, str(actives))
    new_open = sql(f"SELECT a.open_id FROM vi_employee_binding b JOIN vi_account a ON a.id=b.account_id "
                   f"WHERE b.sys_user_id={TEST_USER_ID} AND b.revoked_at IS NULL")
    check("原微信账号仍持有绑定（未被新账号抢占）",
          bool(new_open) and new_open[0] == "dev_phone_a", str(new_open))
    new_prof = body.get("profile", {})
    check("新微信账号不获得员工身份", new_prof.get("employeeBound") is False, str(new_prof))
    conflict = sql("SELECT COUNT(*) FROM vi_event_log WHERE action='EMPLOYEE_LINK_CONFLICT'")
    check("冲突已留审计（EMPLOYEE_LINK_CONFLICT）", bool(conflict) and conflict[0] != "0", str(conflict))

    # ---------------------------------------------------------------- 六、短信兜底不参与关联
    print("\n== 六、短信绑定手机号不作为员工关联依据 ==")
    code, body = mini_login("dev_phone_f")
    token_f = body.get("accessToken")
    _, sms = call("POST", "/api/common/phone/sms/send", {"phone": TEST_PHONE}, token=token_f)
    dev_code = sms.get("devCode") if isinstance(sms, dict) else None
    if dev_code:
        code, res = call("POST", "/api/common/phone/bind",
                         {"phone": TEST_PHONE, "smsCode": dev_code}, token=token_f)
        # bindPhone 直接返回档案视图，不再包一层 profile；两种形状都兼容
        prof = res.get("profile") if isinstance(res, dict) and "profile" in res else res
        prof = prof if isinstance(prof, dict) else {}
        # V6.4 §2.5：短信兜底不能作为新员工自动关联的依据，只完成普通手机号绑定
        check("短信绑定手机号成功", code == 200 and prof.get("phoneBound") is True, f"{code} {res}")
        check("短信绑定不产生员工关联", prof.get("employeeBound") is False, f"{code} {prof}")
    else:
        check("短信验证码可获取（联调回显）", False, str(sms))

    cleanup()

    print(f"\n== 汇总：通过 {len(PASS)}，失败 {len(FAIL)} ==")
    for name in FAIL:
        print("  FAIL:", name)
    return 0 if not FAIL else 1


if __name__ == "__main__":
    sys.exit(main())
