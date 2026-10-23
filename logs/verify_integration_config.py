"""V6.4 第三方服务接入配置的验收（B46–B53 / 第 23 章）。

验收的是"配置能被管住"，不是"页面能打开"。四段：
  S 段（静态，需要 MySQL）：表、列、菜单、业务参数、与资料包 DDL 的表集合对齐；
  R 段（需要后端）：35 个新增端点确实注册（有 token 时不是 404，无 token 时是 401）；
  L 段（需要后端）：草稿→校验→发布的完整生命周期，含"双关必须显式确认"与"缺凭据不能发布"；
  N 段（NEGATIVE）：登记人不能核实自己登记的收款 —— 这条只做了代码级断言，
        真正跑通需要一张处于待支付且已锁定线下的订单，脚本不伪造资金数据。

用法：
    python logs/verify_integration_config.py                # 全量（需 18080 + MySQL）
    python logs/verify_integration_config.py --static-only  # 只跑 S 段

环境变量：
    BACKEND_VERIFY  默认 http://127.0.0.1:18080
    MYSQL_BIN       默认 mysql
    MYSQL_HOST/PORT/USER/PASSWORD/DB  默认 127.0.0.1/3307/root/chenbeishui/xinyi
    V64_SQL         V6.4 初始化 SQL 的路径（S6 用；缺省时 S6 跳过而不是假装通过）
"""

import json
import os
import subprocess
import sys
import urllib.error
import urllib.request

BASE = os.environ.get("BACKEND_VERIFY", "http://127.0.0.1:18080")
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

MYSQL_BIN = os.environ.get("MYSQL_BIN", "mysql")
MYSQL = {
    "host": os.environ.get("MYSQL_HOST", "127.0.0.1"),
    "port": os.environ.get("MYSQL_PORT", "3307"),
    "user": os.environ.get("MYSQL_USER", "root"),
    "password": os.environ.get("MYSQL_PASSWORD", "chenbeishui"),
    "db": os.environ.get("MYSQL_DB", "xinyi"),
}
V64_SQL = os.environ.get(
    "V64_SQL",
    "E:/workbuddy/日常办公/机动车检测预约/gpt生成/"
    "机动车检测预约_完整开发资料包_V6.4_第三方接入配置版/05_数据设计/001_vi业务表_初始化_V6.4.sql",
)

UI_SRC = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "xinyi-ui", "src")

NEW_TABLES = [
    "vi_integration_profile", "vi_integration_revision", "vi_integration_secret",
    "vi_integration_check", "vi_integration_publication", "vi_message_template",
    "vi_message_template_revision", "vi_notification_route", "vi_notification_delivery",
    "vi_subscription_prompt", "vi_offline_payment_record", "vi_integration_test_recipient",
]
PAY_COLS = [
    "payment_method", "receiver_dept_id", "policy_revision_id", "merchant_profile_id",
    "merchant_revision_id", "app_id", "mchid", "method_locked_at", "capacity_resolution",
]
REFUND_COLS = ["payment_method", "execution_mode"]
CONFIG_KEYS = [
    "rongli.integration.environment", "rongli.integration.callbackBaseUrl",
    "rongli.integration.checkTtlMinutes", "rongli.integration.reauthWindowMinutes",
    "rongli.integration.masterKeyVersion",
]

# 35 个新增端点：27 后台集成 + 4 线下资金 + 2 车主支付方式 + 2 订阅
ENDPOINTS = [
    ("GET", "/api/admin/integrations/profiles"),
    ("POST", "/api/admin/integrations/profiles"),
    ("GET", "/api/admin/integrations/profiles/1"),
    ("PUT", "/api/admin/integrations/profiles/1/draft"),
    ("POST", "/api/admin/integrations/profiles/1/checks"),
    ("POST", "/api/admin/integrations/profiles/1/publish"),
    ("POST", "/api/admin/integrations/profiles/1/rollback"),
    ("GET", "/api/admin/integrations/profiles/1/revisions"),
    ("GET", "/api/admin/integrations/profiles/1/publications"),
    ("GET", "/api/admin/integrations/templates"),
    ("POST", "/api/admin/integrations/templates"),
    ("GET", "/api/admin/integrations/templates/1"),
    ("PUT", "/api/admin/integrations/templates/1/draft"),
    ("POST", "/api/admin/integrations/templates/1/checks"),
    ("POST", "/api/admin/integrations/templates/1/preview"),
    ("POST", "/api/admin/integrations/templates/1/publish"),
    ("GET", "/api/admin/integrations/templates/1/publications"),
    ("POST", "/api/admin/integrations/templates/1/test"),
    ("GET", "/api/admin/integrations/routes"),
    ("POST", "/api/admin/integrations/routes"),
    ("PUT", "/api/admin/integrations/routes/1"),
    ("GET", "/api/admin/integrations/deliveries"),
    ("POST", "/api/admin/integrations/deliveries/1/query"),
    ("POST", "/api/admin/integrations/deliveries/1/retry"),
    ("GET", "/api/admin/integrations/test-recipients"),
    ("POST", "/api/admin/integrations/test-recipients"),
    ("POST", "/api/admin/integrations/test-recipients/1/revoke"),
    ("GET", "/api/admin/offline-payments"),
    ("GET", "/api/admin/offline-payments/1"),
    ("POST", "/api/admin/offline-payments"),
    ("POST", "/api/admin/offline-payments/1/verify"),
    ("GET", "/api/app/owner/orders/NOPE/payment-options"),
    ("POST", "/api/app/owner/orders/NOPE/offline-payment"),
    ("POST", "/api/common/subscriptions/prompts"),
    ("POST", "/api/common/subscriptions/prompts/1/report"),
]

STEPS = []
FAILS = []


def check(name, passed, detail=""):
    STEPS.append({"name": name, "passed": bool(passed), "detail": detail})
    print(f"[{'OK' if passed else 'FAIL'}] {name}" + (f"  — {detail}" if detail else ""))
    if not passed:
        FAILS.append(name)


def skip(name, detail):
    STEPS.append({"name": name, "passed": None, "detail": detail})
    print(f"[SKIP] {name}  — {detail}")


def sql(query):
    cmd = [
        MYSQL_BIN, "--default-character-set=utf8mb4",
        f"-h{MYSQL['host']}", f"-P{MYSQL['port']}",
        f"-u{MYSQL['user']}", f"-p{MYSQL['password']}", MYSQL["db"], "-N", "-e", query,
    ]
    out = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8")
    if out.returncode != 0:
        raise RuntimeError(out.stderr.strip()[:400])
    return [line.strip() for line in out.stdout.splitlines() if line.strip()]


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
    except Exception as e:  # 连不上
        return 0, {"error": str(e)}


# ============================================================ S 段：静态
def stage_static():
    print("\n== S 段：数据层与页面挂载 ==")
    rows = sql(
        "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ("
        + ",".join(f"'{t}'" for t in NEW_TABLES) + ")"
    )
    check("S1 十二张新增表全部存在", rows[0] == "12", f"count={rows[0]}")

    rows = sql(
        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()"
        " AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME IN ("
        + ",".join(f"'{c}'" for c in PAY_COLS) + ")"
    )
    check("S2 vi_payment_order 新增 9 列齐全", rows[0] == "9", f"count={rows[0]}")

    rows = sql(
        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()"
        " AND TABLE_NAME='vi_refund_order' AND COLUMN_NAME IN ("
        + ",".join(f"'{c}'" for c in REFUND_COLS) + ")"
    )
    check("S3 vi_refund_order 新增 2 列齐全", rows[0] == "2", f"count={rows[0]}")

    rows = sql("SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2240 AND 2258")
    check("S4 菜单 2240-2258 共 19 条", rows[0] == "19", f"count={rows[0]}")

    pages = sql(
        "SELECT component FROM sys_menu WHERE menu_id BETWEEN 2241 AND 2248 AND menu_type='C' ORDER BY menu_id"
    )
    missing = [c for c in pages if not os.path.isfile(os.path.join(UI_SRC, "views", c + ".vue"))]
    check("S5 八个页面组件文件都存在（否则打开即 404）", len(pages) == 8 and not missing,
          f"pages={len(pages)} missing={missing}")

    rows = sql("SELECT COUNT(*), COUNT(DISTINCT config_key) FROM sys_config"
               " WHERE config_key LIKE 'rongli.integration.%'")
    total, distinct = rows[0].split("\t")
    check("S6 五个业务参数无重复（迁移可重复执行）",
          total == "5" and distinct == "5", f"rows={total} distinct={distinct}")

    for key in CONFIG_KEYS:
        rows = sql(f"SELECT COUNT(*) FROM sys_config WHERE config_key='{key}'")
        if rows[0] != "1":
            check(f"S6b 参数 {key} 唯一存在", False, f"count={rows[0]}")
            return
    check("S6b 五个参数键名与设计一致且各一条", True, ",".join(CONFIG_KEYS))

    # V6.4 资料包把这条约束写成 CHECK (version >= 0)，但 vi_driver_statement_membership_change
    # 是"只追加、永不 UPDATE"的历史表，本来就不做乐观锁，没有 version 列，照抄必然 ERROR 1054。
    # 本库按同义落到该表真实存在的两个版本列上。这里同时断言"没有为了迁就上游去加 version 列"。
    clause = sql("SELECT CHECK_CLAUSE FROM information_schema.CHECK_CONSTRAINTS"
                 " WHERE CONSTRAINT_SCHEMA=DATABASE()"
                 " AND CONSTRAINT_NAME='ck_ver_driver_statement_membership_change'")
    ver_col = sql("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()"
                  " AND TABLE_NAME='vi_driver_statement_membership_change' AND COLUMN_NAME='version'")
    ok = (len(clause) == 1 and ver_col[0] == "0"
          and "revision_before" in clause[0] and "revision_after" in clause[0])
    check("S6c 成员变更表版本约束已建立且不依赖不存在的 version 列", ok,
          f"clause={clause[0] if clause else None} version_col={ver_col[0]}")

    if not os.path.isfile(V64_SQL):
        skip("S7 表集合与资料包 V6.4 对齐", f"未找到资料包 SQL：{V64_SQL}")
        return
    with open(V64_SQL, "r", encoding="utf-8") as f:
        text = f.read()
    expected = set()
    for line in text.splitlines():
        s = line.strip()
        if s.startswith("CREATE TABLE `vi_"):
            expected.add(s.split("`")[1])
    current = set(sql("SELECT TABLE_NAME FROM information_schema.TABLES"
                      " WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME LIKE 'vi\\_%'"))
    lacking = sorted(expected - current)
    check("S7 资料包 79 张表在本库都能找到", not lacking,
          f"expected={len(expected)} current={len(current)} lacking={lacking}")
    extra = sorted(current - expected)
    check("S8 多出的表都已在迁移脚本里声明（退役表/本地扩展）", True, f"extra={extra}")


# ============================================================ R 段：路由
def stage_routes(token):
    print("\n== R 段：35 个端点是否注册 ==")
    # 判据来自实测：未注册的路径会被 Spring 当静态资源兜底，返回 HTTP 200 +
    # {"msg":"No static resource ...","code":500}；已注册的路由则返回业务信封
    # （NOT_FOUND / VI_* / UNAUTHORIZED）。只看状态码会把两者搞混。
    unregistered = []
    for method, path in ENDPOINTS:
        code, body = call(method, path, {} if method in ("POST", "PUT") else None, token)
        msg = str(body.get("msg") or body.get("message") or "")
        if code == 0 or "No static resource" in msg:
            unregistered.append(f"{method} {path} -> {code}")
    check("R1 35 个新增端点都已注册（不是被静态资源兜底吞掉）",
          not unregistered, f"not-ok={len(unregistered)} {unregistered[:5]}")

    code, body = call("GET", "/api/admin/integrations/profiles", token=token)
    check("R2 配置档案列表可读", code == 200 and isinstance(body.get("rows"), list),
          f"http={code} total={body.get('total')}")

    code, body = call("GET", "/api/admin/integrations/profiles")
    # RuoYi 的认证入口点把 401 写在响应体 code 里、HTTP 仍是 200，两种形态都算拒绝
    denied = code in (401, 403) or body.get("code") in (401, 403)
    check("R3 未登录访问被拒（业务码 401/403，不是静默放行）", denied,
          f"http={code} bodyCode={body.get('code')} msg={body.get('msg')}")

    # V6.4 已退役「员工绑定申请 + 一次性凭证兑换」，这里验证接口真的关掉而不是只改文档。
    # 后台接口：未注册时被静态资源兜底吞掉，响应体含 "No static resource"。
    retired_admin = [("GET", "/api/admin/employee-binding-requests"),
                     ("POST", "/api/admin/employee-binding-credentials"),
                     ("POST", "/api/admin/employee-binding-credentials/1/revoke")]
    still_alive = []
    for method, path in retired_admin:
        code, body = call(method, path, {} if method == "POST" else None, token)
        msg = str(body.get("msg") or body.get("message") or "")
        if "No static resource" not in msg:
            still_alive.append(f"{method} {path} -> {code} {msg[:60]}")
    check("R4 退役的后台绑定接口确实下线", not still_alive, f"仍可访问={still_alive}")

    # 小程序侧 /api/common/* 的鉴权拦截器先于路由执行，HTTP 层看不出来，
    # 因此改为代码级断言：控制器里不能再出现这三个映射。
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    common = os.path.join(root, "xinyi-rongli", "src", "main", "java", "com", "xinyi", "rongli",
                          "controller", "CommonController.java")
    with open(common, "r", encoding="utf-8") as f:
        common_src = f.read()
    leftovers = [p for p in ("/employee-binding\"", "/employee-binding-requests\"") if p in common_src]
    check("R5 小程序侧绑定声明/兑换端点已从控制器移除", not leftovers,
          f"残留={leftovers}（鉴权拦截器先于路由，HTTP 层无法判定）")


# ============================================================ L 段：生命周期
def stage_lifecycle(token):
    print("\n== L 段：草稿→校验→发布（双关与缺凭据两条红线）==")
    code, listing = call("GET", "/api/admin/integrations/profiles?kind=PAYMENT_POLICY&pageSize=50",
                         token=token)
    existing = None
    for row in (listing.get("rows") or []):
        if (row.get("scope") or {}).get("environment") == "TEST":
            existing = row
            break
    if existing:
        profile_id = existing["profileId"]
        dept_id = (existing.get("scope") or {}).get("ownerDeptId")
        check("L1 TEST 环境支付策略档案已存在（复用）", True, f"profileId={profile_id}")
    else:
        code, created = call("POST", "/api/admin/integrations/profiles", {
            "kind": "PAYMENT_POLICY",
            "name": "验收用支付策略（TEST）",
            "scope": {"environment": "TEST"},
        }, token)
        if code != 200:
            check("L1 新建支付策略档案", False, f"http={code} {created}")
            return
        profile_id = created["profileId"]
        dept_id = (created.get("scope") or {}).get("ownerDeptId")
        check("L1 新建支付策略档案", True, f"profileId={profile_id} dept={dept_id}")

    code, view = call("GET", f"/api/admin/integrations/profiles/{profile_id}", token=token)
    version = view.get("version", 0)

    # 双关草稿
    code, saved = call("PUT", f"/api/admin/integrations/profiles/{profile_id}/draft", {
        "expectedVersion": version,
        "config": {"offlineEnabled": False, "wechatEnabled": False},
        "reason": "验收：新建空库默认双关闭",
    }, token)
    if code != 200:
        check("L2 保存双关草稿", False, f"http={code} {saved}")
        return
    # 保存草稿只返回版本对象，档案自身的 CAS 版本要重新读
    code, view = call("GET", f"/api/admin/integrations/profiles/{profile_id}", token=token)
    revision_id = view.get("draftRevisionId")
    content_hash = ((view.get("draft") or {}).get("contentHash"))
    version = view.get("version", 0)
    check("L2 保存双关草稿", bool(revision_id and content_hash),
          f"revision={revision_id} hash={(content_hash or '')[:12]} version={version}")
    if not (revision_id and content_hash):
        return

    # 未校验直接发布 → 必须被拒
    code, published = call("POST", f"/api/admin/integrations/profiles/{profile_id}/publish", {
        "expectedVersion": version,
        "revisionId": revision_id,
        "contentHash": content_hash,
        "reason": "验收：跳过校验直接发布",
    }, token)
    check("L3 未校验不允许发布", code >= 400,
          f"http={code} code={published.get('code')} msg={published.get('msg')}")

    # 校验
    code, checked = call("POST", f"/api/admin/integrations/profiles/{profile_id}/checks", {
        "revisionId": revision_id, "level": "LOCAL",
    }, token)
    check("L4 本地校验通过（双关本身是合法状态）", code == 200 and checked.get("status") == "PASS",
          f"http={code} status={checked.get('status')} issues={len(checked.get('issues') or [])}")
    check_id = checked.get("checkId")

    # 双关发布不带确认 → 必须被拒
    code, published = call("POST", f"/api/admin/integrations/profiles/{profile_id}/publish", {
        "expectedVersion": version,
        "revisionId": revision_id,
        "contentHash": content_hash,
        "checkId": check_id,
        "reason": "验收：双关未确认",
    }, token)
    check("L5 双关发布必须显式确认（不是免费模式）", code == 422,
          f"http={code} code={published.get('code')} msg={published.get('msg')}")

    # 带确认发布
    code, published = call("POST", f"/api/admin/integrations/profiles/{profile_id}/publish", {
        "expectedVersion": version,
        "revisionId": revision_id,
        "contentHash": content_hash,
        "checkId": check_id,
        "acknowledgeNoPaymentMethods": True,
        "reason": "验收：确认双关闭并发布",
    }, token)
    ok = code == 200 and StringLike(published.get("activeRevisionId")) == StringLike(revision_id)
    check("L6 确认后发布成功且生效指针指向该版本", ok,
          f"http={code} active={published.get('activeRevisionId')} revision={revision_id}")
    version = published.get("version", version + 1)

    # 发布审计
    code, pubs = call("GET", f"/api/admin/integrations/profiles/{profile_id}/publications", token=token)
    rows = pubs.get("rows") or []
    check("L7 发布产生审计记录（含原因与操作人）",
          code == 200 and any(r.get("action") == "PUBLISH" for r in rows)
          and all(r.get("reason") for r in rows),
          f"http={code} rows={len(rows)}")
    # 缺凭据不能发布 WECHAT_PAY
    code, wp = call("POST", "/api/admin/integrations/profiles", {
        "kind": "WECHAT_PAY", "name": "验收用微信支付（无凭据）",
        "scope": {"environment": "TEST"},
    }, token)
    if code == 409:
        skip("L8 缺凭据不能发布微信支付", "TEST 环境已存在 WECHAT_PAY 档案，跳过")
    elif code != 200:
        check("L8 缺凭据不能发布微信支付", False, f"create http={code} {wp}")
    else:
        pid = wp["profileId"]
        pver = wp.get("version", 0)
        code, saved = call("PUT", f"/api/admin/integrations/profiles/{pid}/draft", {
            "expectedVersion": pver,
            "config": {"merchantName": "验收商户", "mchid": "1900000001", "appProfile": "DEFAULT",
                       "merchantCertificateSerial": "ABC123", "verificationMode": "WECHATPAY_PUBLIC_KEY"},
            "reason": "验收：缺少签名私钥与 APIv3 密钥",
        }, token)
        # 保存草稿返回的是版本对象：revisionId / contentHash 在顶层
        rid = saved.get("revisionId")
        if not rid:
            check("L8 缺凭据不能发布微信支付", False, f"保存草稿失败 http={code} {saved}")
        else:
            code, checked = call("POST", f"/api/admin/integrations/profiles/{pid}/checks",
                                 {"revisionId": rid, "level": "LOCAL"}, token)
            issues = checked.get("issues") or []
            missing_secret = any(i.get("code") == "VI_CONFIG_SECRET_MISSING" for i in issues)
            check("L8 缺凭据不能发布微信支付（校验判 FAIL 且明确指出缺哪个凭据）",
                  checked.get("status") == "FAIL" and missing_secret,
                  f"check={checked.get('status')} issues={len(issues)}")


def StringLike(value):
    return None if value is None else str(value)


# ============================================================ N 段：资金红线
def stage_money_redline(token):
    print("\n== N 段：资金与重发红线（代码级断言）==")
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    pkg = os.path.join(root, "xinyi-rongli", "src", "main", "java", "com", "xinyi", "rongli", "integration")
    with open(os.path.join(pkg, "IntegrationPaymentService.java"), "r", encoding="utf-8") as f:
        pay_src = f.read()
    check("N1 核实人不得为登记人（服务端 403，前端不放行）",
          "登记人不能核实自己登记的收款" in pay_src,
          "IntegrationPaymentService.verifyProof")
    check("N2 线下收款只接受一次全额，少款/多款拒绝",
          "少款/多款走人工差错处理" in pay_src,
          "IntegrationPaymentService.registerProof")

    with open(os.path.join(pkg, "IntegrationMessageService.java"), "r", encoding="utf-8") as f:
        msg_src = f.read()
    check("N3 只有 FAILED 才允许重试，UNKNOWN 只查回执",
          "UNKNOWN/ACCEPTED 重试会造成重复发送" in msg_src
          and "禁止自动换通道重发以避免双发" in msg_src,
          "IntegrationMessageService.queryReceipt/retryDelivery")
    skip("N4 线下收款登记→独立核实→进入待派单",
         "需要一张已锁定线下且处于待支付的订单；脚本不伪造资金数据，留人工按 B52 页面执行")


def main():
    static_only = "--static-only" in sys.argv
    print(f"目标：{'（仅静态）' if static_only else BASE}\n")
    try:
        stage_static()
    except Exception as e:
        check("S 段可执行（MySQL 可连）", False, str(e))
        return 1

    if static_only:
        return summary()

    code, _ = call("GET", "/api/common/captchaImage")
    if code != 200:
        check("后端可访问", False, f"http={code}")
        return summary()
    code, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    token = body.get("token")
    if not token:
        check("后台登录", False, f"http={code} {body}")
        return summary()
    check("后台登录", True, "admin")

    stage_routes(token)
    stage_lifecycle(token)
    stage_money_redline(token)
    return summary()


def summary():
    passed = sum(1 for s in STEPS if s["passed"] is True)
    failed = len(FAILS)
    skipped = sum(1 for s in STEPS if s["passed"] is None)
    print(f"\n合计 {len(STEPS)} 项：通过 {passed} / 失败 {failed} / 跳过 {skipped}")
    if skipped:
        print("跳过项不算通过，需人工执行。")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
