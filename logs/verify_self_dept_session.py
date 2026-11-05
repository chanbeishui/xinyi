"""验证：管理员修改自己的任职部门后，当前会话保留、其他会话照旧淘汰。

背景：updateDepartments 会递增 auth_version（部门参与数据范围判定，必须失效旧权限），
旧实现在提交后删除该用户全部 token，连操作者当前会话一起删，于是"改自己的部门"
必然重新登录。现在改为先重载当前会话、再按版本淘汰其余会话。
"""
import json
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:8080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
FAILED = []


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with OPENER.open(req, timeout=15) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8") or "{}")
    except urllib.error.HTTPError as err:
        raw = err.read().decode("utf-8")
        try:
            return err.code, json.loads(raw or "{}")
        except json.JSONDecodeError:
            return err.code, {"msg": raw}


def check(name, ok, detail=""):
    print(("OK   " if ok else "FAIL ") + name + ("   " + str(detail) if detail else ""))
    if not ok:
        FAILED.append(name)


def allowed(body):
    """本系统业务成功看响应体 code；未登录/失效时可能是 HTTP 200 + body.code=401。"""
    return body.get("code") == 200


def rejected(body):
    return body.get("code") == 401


def login():
    status, body = call("POST", "/login", {"username": "admin", "password": "admin123"})
    if status != 200 or not body.get("token"):
        raise SystemExit("登录失败，无法继续: %s %s" % (status, body.get("msg")))
    return body["token"]


print("=== 1. 建立两个会话（模拟两台设备） ===")
token_a = login()
token_b = login()
check("两次登录成功且 token 不同", token_a != token_b)
check("会话A 初始可用", allowed(call("GET", "/system/user/list", token=token_a)[1]))
check("会话B 初始可用", allowed(call("GET", "/system/user/list", token=token_b)[1]))

print("=== 2. 读出 admin 当前任职部门（用于还原） ===")
status, body = call("GET", "/system/user/1", token=token_a)
# 注意：deptIds / deptId 在 data 节点内（postIds 却在根节点，接口结构不统一）
detail = body.get("data") or {}
original_ids = detail.get("deptIds") or []
primary = detail.get("deptId")
check("读取到任职部门", bool(original_ids), "deptIds=%s primary=%s" % (original_ids, primary))
if not original_ids:
    raise SystemExit("未能读取任职部门，终止以免误清空")

print("=== 3. 用会话A 修改自己的任职部门 ===")
status, body = call("PUT", "/system/user/1/departments", {
    "primaryDeptId": original_ids[0],
    "deptIds": [original_ids[0]],
    "reason": "回归验证：管理员修改自己的任职部门",
}, token=token_a)
check("修改自己的部门成功", allowed(body), str(body.get("msg")))

print("=== 4. 关键断言：会话A 应保留，会话B 应被淘汰 ===")
status_a, body_a = call("GET", "/system/user/list", token=token_a)
status_b, body_b = call("GET", "/system/user/list", token=token_b)
check("操作者当前会话仍可用（不再被自己踢下线）", allowed(body_a),
      "HTTP %s code=%s %s" % (status_a, body_a.get("code"), body_a.get("msg")))
check("同一账号的其他设备会话被淘汰", rejected(body_b),
      "HTTP %s code=%s %s" % (status_b, body_b.get("code"), body_b.get("msg")))

print("=== 5. 新权限已生效：会话A 读到的是新部门 ===")
status, body = call("GET", "/system/user/1", token=token_a)
current = (body.get("data") or {}).get("deptIds") or []
check("部门已是变更后的值", current == [original_ids[0]], "deptIds=%s" % current)

print("=== 6. 还原为原始多部门 ===")
status, body = call("PUT", "/system/user/1/departments", {
    "primaryDeptId": primary,
    "deptIds": original_ids,
    "reason": "回归验证结束，还原原始任职部门",
}, token=token_a)
check("还原成功", allowed(body), str(body.get("msg")))
check("还原后会话A 仍可用", allowed(call("GET", "/system/user/list", token=token_a)[1]))

status, body = call("GET", "/system/user/1", token=token_a)
check("部门已还原", sorted((body.get("data") or {}).get("deptIds") or []) == sorted(original_ids),
      "deptIds=%s" % ((body.get("data") or {}).get("deptIds")))

print()
print("失败项: %s" % (FAILED if FAILED else "无"))
