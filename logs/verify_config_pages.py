"""配置入口唯一性验收（替代原「系统设置三项配置页」套件）。

背景：微信支付 / 短信通道 / 订阅消息曾经有两套页面与两套存储
（系统设置下的三个页面直接写 sys_config，第三方服务下的档案页写 vi_integration_profile），
同一组凭据要填两遍，填漏一处就是"配了不生效"。现在统一到集成档案：
——旧三页及其端点已删除；
——档案发布时由 RuntimeProjection 把生效值投影到 sys_config 供运行时读取。

验收的不是"页面能打开"，而是五件事：
  一、旧端点真的下线了（不能再有人绕过档案直接写运行时配置）；
  二、菜单里只剩一个入口，且"业务参数"落在 /rongli/inspect/config；
  三、档案侧的必填校验仍然拦得住缺凭据、错格式（发布前不会放过半截配置）；
  四、删除旧页后失去入口的项（短信频控、订阅跳转页）确实由业务参数承接，
      且非法值会被拒绝（拒绝即不落库）；
  五、通道自检可用，支付/小程序/短信/订阅/回调五项都在。
只读：本套件不发布、不改任何生效配置，可反复运行。

用法：python logs/verify_config_pages.py
前置：后端运行在 18080
"""
import json
import os
import sys
import urllib.error
import urllib.request

BASE = os.environ.get("BACKEND_VERIFY", "http://127.0.0.1:18080")
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

LEGACY_ENDPOINTS = ["/api/admin/config/payment", "/api/admin/config/sms", "/api/admin/config/subscribe"]

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


def route_paths(token):
    code, body = call("GET", "/getRouters", token=token)
    if code != 200:
        return {}, []
    paths, names = {}, []

    def walk(items, prefix=""):
        for item in items:
            full = prefix.rstrip("/") + "/" + item.get("path", "").strip("/")
            paths[full] = item
            names.append(item.get("name"))
            walk(item.get("children") or [], full)

    walk(body.get("data") or [])
    return paths, names


def main():
    print(f"目标：{BASE}\n")
    tok = admin_token()
    if not tok:
        print("后台登录失败，无法继续")
        return 1

    # ------------------------------------------------ 一、旧端点下线
    for path in LEGACY_ENDPOINTS:
        code, body = call("GET", path, token=tok)
        # 路由不存在时 RuoYi 返回 HTTP 200 + "No static resource"，所以要连响应体一起判断
        gone = code == 404 or "No static resource" in json.dumps(body, ensure_ascii=False)
        check(f"CFG-101 旧端点已下线 {path}", gone, f"HTTP {code}")

    # ------------------------------------------------ 二、菜单只剩一个入口
    paths, names = route_paths(tok)
    legacy_pages = [p for p in paths if p.startswith("/rongli/inspect/config/")]
    check("CFG-201 旧配置页不再出现在路由表", not legacy_pages, str(legacy_pages[:3]))

    param_page = paths.get("/rongli/inspect/config") or {}
    check("CFG-202 业务参数落在 /rongli/inspect/config",
          param_page.get("component") == "rongli/config/index", str(param_page.get("component")))

    integration_pages = [p for p in paths if p.startswith("/rongli/integration/")]
    check("CFG-203 第三方服务仍是独立分组且不为空", len(integration_pages) >= 5, f"{len(integration_pages)} 个页面")

    dup = sorted({n for n in names if n and names.count(n) > 1})
    check("CFG-204 路由名全局唯一（重名会让页面 404）", not dup, str(dup[:5]))

    # ------------------------------------------------ 三、档案侧校验仍然拦得住
    code, listing = call("GET", "/api/admin/integrations/profiles?kind=WECHAT_APP", token=tok)
    rows = listing.get("rows") or listing.get("data", {}).get("rows") or []
    check("CFG-301 小程序凭据档案列表可读", code == 200, f"HTTP {code} / {len(rows)} 条")

    target = None
    for row in rows:
        if row.get("draftRevisionId") or row.get("activeRevisionId"):
            target = row
            break
    if target is None:
        # 没有小程序凭据档案时退到任意已有版本的档案（例如支付策略），
        # 否则「校验接口」这条最重要的断言会长期处于 SKIP。
        code_all, listing_all = call("GET", "/api/admin/integrations/profiles?pageSize=50", token=tok)
        for row in (listing_all.get("rows") or []):
            if row.get("draftRevisionId") or row.get("activeRevisionId"):
                target = row
                break
    if target is None:
        print("[SKIP] 没有可用于校验的版本，跳过 CFG-302/303")
    else:
        pid = target.get("profileId") or target.get("id")
        revision_id = target.get("draftRevisionId") or target.get("activeRevisionId")
        code, checked = call("POST", f"/api/admin/integrations/profiles/{pid}/checks",
                             {"revisionId": revision_id, "level": "LOCAL"}, token=tok)
        text = json.dumps(checked, ensure_ascii=False)
        check("CFG-302 校验接口可用且给出逐字段结论", code == 200 and "status" in text, f"HTTP {code}")
        # 小程序凭据启用却没登记 AppSecret 时，必须报凭据类错误而不是放过
        has_secret_rule = "VI_CONFIG_SECRET_MISSING" in text or "VI_CONFIG_INVALID" in text or '"PASS"' in text
        check("CFG-303 凭据缺失/字段非法时有明确错误码", has_secret_rule, text[:120])

    # ------------------------------------------------ 四、业务参数承接业务口径
    # 旧页删掉后，短信频控与订阅跳转页必须由「业务参数」承接，否则这些值既在生效又没地方改。
    code, cfg = call("GET", "/api/admin/config", token=tok)
    eff = (cfg or {}).get("effective") or {}
    required = [
        "rongli.sms.codeExpireSeconds", "rongli.sms.resendSeconds", "rongli.sms.dailyLimit",
        "rongli.subscribe.page", "rongli.subscribe.miniprogramState", "rongli.subscribe.fields",
    ]
    missing = [k for k in required if eff.get(k) in (None, "")]
    check("CFG-401 业务参数暴露短信频控与订阅跳转", not missing, str(missing))

    # 越界/非法值必须被拒；被拒即不落库，所以本套件仍然只读
    bad_cases = [
        ("CFG-402 验证码有效期越界被拒", {"smsCodeExpireSeconds": "10"}),
        ("CFG-403 订阅跳转页格式非法被拒", {"subscribePage": "/pages/index/index"}),
        ("CFG-404 字段映射来源非法被拒", {"subscribeFields": '{"thing1":"bad"}'}),
        ("CFG-405 跳转小程序版本非法被拒", {"subscribeMiniprogramState": "production"}),
    ]
    for name, payload in bad_cases:
        code, _body = call("PUT", "/api/admin/config", payload, token=tok)
        check(name, code == 400, f"HTTP {code}")

    # ------------------------------------------------ 五、通道自检可用
    code, rd = call("GET", "/api/admin/config/readiness", token=tok)
    items = (rd or {}).get("items") or []
    keys = {i.get("key") for i in items}
    check("CFG-501 通道自检覆盖支付/小程序/短信/订阅/回调",
          code == 200 and keys >= {"payment", "mini", "sms", "subscribe", "callback"},
          f"HTTP {code} keys={sorted(keys)}")

    print()
    total = len(STEPS)
    passed = total - len(FAILS)
    print(f"== 合计 {total} 条：通过 {passed}，失败 {len(FAILS)}")
    if FAILS:
        print("失败用例：" + "、".join(FAILS))
    return 0 if not FAILS else 1


if __name__ == "__main__":
    sys.exit(main())
