"""系统设置三项配置页的接口级验收（G01/G02/G03）。

验收的不是"页面能打开"，而是三件事：
  一、必填与格式校验真的生效，且提示能指导操作（不是笼统的"参数错误"）；
  二、校验失败时不会把半截错误值落库（失败即不改配置）；
  三、保存后立刻生效——通过"保存→重新读取"验证 Redis 缓存被主动清掉。

用法：python logs/verify_config_pages.py
前置：后端运行在 18080
注意：脚本会先备份三项配置，结束时按备份还原，可反复运行。
"""
import json
import os
import sys
import urllib.error
import urllib.request

BASE = os.environ.get("BACKEND_VERIFY", "http://127.0.0.1:18080")
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

PAY = "/api/admin/config/payment"
SMS = "/api/admin/config/sms"
SUB = "/api/admin/config/subscribe"

STEPS = []
FAILS = []


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:300]}


def check(name, passed, detail=""):
    STEPS.append((name, 0, bool(passed), detail))
    print(f"[{'OK' if passed else 'FAIL'}] --- {name}" + (f"  {detail}" if detail else ""))
    if not passed:
        FAILS.append(name)


def admin_token():
    code, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    if code != 200:
        return None
    return body.get("token")


def main():
    print(f"目标：{BASE}\n")
    tok = admin_token()
    if not tok:
        print("后台登录失败，无法继续")
        return 1

    # ---------------------------------------------------------------- 备份
    _, pay0 = call("GET", PAY, token=tok)
    _, sms0 = call("GET", SMS, token=tok)
    _, sub0 = call("GET", SUB, token=tok)

    print("== 一、读取：三页都能给出状态与缺失清单")
    for label, view in (("微信支付", pay0), ("短信通道", sms0), ("订阅消息", sub0)):
        keys = set(view.keys())
        check(f"{label}配置可读",
              "status" in keys and "ready" in keys and "statusText" in keys,
              f"status={view.get('status')} ready={view.get('ready')}")

    print("\n== 二、微信支付：必填清单与格式校验")
    code, body = call("PUT", PAY, {"channel": "WECHAT"}, tok)
    msg = str(body.get("message") or "")
    check("切真实通道但未填凭据被拒绝", code == 400, f"HTTP {code}")
    check("缺失项逐条列出（至少 7 项）", msg.count("；") >= 6, msg[:150])
    _, now = call("GET", PAY, token=tok)
    check("被拒后通道未被改动", now.get("channel") == pay0.get("channel"),
          f"channel={now.get('channel')}")

    code, body = call("PUT", PAY, {"appId": "wx123"}, tok)
    check("AppID 格式错误被拒绝", code == 400, f"HTTP {code} {body.get('message')}")
    code, body = call("PUT", PAY, {"mchId": "abc"}, tok)
    check("商户号格式错误被拒绝", code == 400, f"HTTP {code} {body.get('message')}")
    code, body = call("PUT", PAY, {"apiV3Key": "short"}, tok)
    check("APIv3 密钥长度不符被拒绝", code == 400, f"HTTP {code} {body.get('message')}")

    print("\n== 三、微信支付：联调通道保存后立即生效")
    code, body = call("PUT", PAY, {"description": "配置页验收用描述"}, tok)
    check("联调通道可保存非凭据字段", code == 200, f"HTTP {code} {body.get('message')}")
    _, after = call("GET", PAY, token=tok)
    check("重新读取即为新值（缓存已失效）",
          after.get("description") == "配置页验收用描述",
          f"description={after.get('description')}")

    print("\n== 四、短信通道：必填、区间与开关")
    code, body = call("PUT", SMS, {"enabled": True}, tok)
    msg = str(body.get("message") or "")
    check("开启但未填凭据被拒绝", code == 400, f"HTTP {code}")
    check("缺失项点名到具体字段", "签名" in msg or "AccessKey" in msg, msg[:150])
    _, now = call("GET", SMS, token=tok)
    check("被拒后开关未被打开", now.get("enabled") == sms0.get("enabled"),
          f"enabled={now.get('enabled')}")
    for field, value, label in (("resendSeconds", 5, "重发间隔过小"),
                                ("resendSeconds", 601, "重发间隔过大"),
                                ("dailyLimit", 0, "日上限过小"),
                                ("dailyLimit", 99, "日上限过大")):
        code, body = call("PUT", SMS, {field: value}, tok)
        check(f"{label}被拒绝", code == 400, f"HTTP {code} {body.get('message')}")

    print("\n== 五、订阅消息：格式错误拒绝、缺失放行")
    code, body = call("PUT", SUB, {"dispatchInvite": "ab"}, tok)
    check("模板 ID 格式错误被拒绝", code == 400, f"HTTP {code} {body.get('message')}")
    code, body = call("PUT", SUB, {"miniprogramState": "prod"}, tok)
    check("运行环境非法值被拒绝", code == 400, f"HTTP {code} {body.get('message')}")
    code, body = call("PUT", SUB, {"fields": "{\"Thing\":\"content\"}"}, tok)
    check("字段映射键名非法被拒绝", code == 400, f"HTTP {code} {body.get('message')}")

    code, body = call("PUT", SUB, {"enabled": False}, tok)
    check("关闭订阅可以保存（不被必填项卡死）", code == 200, f"HTTP {code} {body.get('message')}")
    code, body = call("PUT", SUB, {"enabled": True}, tok)
    check("再次开启也能保存（不锁死）", code == 200, f"HTTP {code} {body.get('message')}")
    check("缺模板时状态为 INCOMPLETE 且有提示",
          body.get("status") == "INCOMPLETE" and "还差" in str(body.get("message")),
          f"status={body.get('status')} message={body.get('message')}")

    # ---------------------------------------------------------------- 还原
    print("\n== 六、还原备份")
    restore_pay = {k: pay0.get(k) for k in
                   ("channel", "mode", "appId", "mchId", "serialNo", "platformSerialNo",
                    "notifyUrl", "refundNotifyUrl", "description", "verifyNotify")}
    restore_pay["apiV3Key"] = "__UNCHANGED__"
    restore_pay["privateKey"] = "__UNCHANGED__"
    restore_pay["platformCert"] = "__UNCHANGED__"
    code, _ = call("PUT", PAY, restore_pay, tok)
    check("微信支付已还原", code == 200, f"HTTP {code}")

    restore_sms = {k: sms0.get(k) for k in
                   ("enabled", "provider", "signName", "templateCode", "accessKeyId", "secretId",
                    "sdkAppId", "region", "codeExpireSeconds", "resendSeconds", "dailyLimit")}
    restore_sms["accessKeySecret"] = "__UNCHANGED__"
    restore_sms["secretKey"] = "__UNCHANGED__"
    code, _ = call("PUT", SMS, restore_sms, tok)
    check("短信通道已还原", code == 200, f"HTTP {code}")

    restore_sub = {k: sub0.get(k) for k in
                   ("enabled", "dispatchInvite", "assignmentAccepted", "resultReady", "refundDone",
                    "page", "miniprogramState", "fields")}
    restore_sub["miniAppId"] = sub0.get("miniAppId") or ""
    restore_sub["miniAppSecret"] = "__UNCHANGED__"
    code, _ = call("PUT", SUB, restore_sub, tok)
    check("订阅消息已还原", code == 200, f"HTTP {code}")

    return finish()


def finish():
    print()
    total = len(STEPS)
    passed = total - len(FAILS)
    print(f"== 汇总：{total} 项，通过 {passed}，失败 {len(FAILS)}")
    if FAILS:
        print("   失败项：" + "、".join(FAILS))
        return 1
    print("== 三项配置的必填校验、失败不落库、保存即生效 均通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
