"""验证"修改资料/岗位不再导致重新登录"。

回归点：PUT /system/user 走 UserAuthorizationMutationServiceImpl.updateUserBase，
它只改昵称/邮箱/手机/性别/备注/岗位，不递增 auth_version，因此同一 token
在保存后仍能继续访问 /system/user/list。旧实现会在保存后强退，列表接口返回 401。

用法：python logs/verify_profile_no_relogin.py
环境：BACKEND_BASE 可指向其它实例，默认 http://127.0.0.1:8080
"""
import json
import os
import urllib.error
import urllib.request
from urllib.parse import urlencode

BASE = os.environ.get("BACKEND_BASE") or "http://127.0.0.1:8080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
RESULTS = []


def call(method, path, token=None, body=None, params=None):
    url = BASE + path
    if params:
        url += "?" + urlencode(params)
    headers = {"Content-Type": "application/json"}
    data = json.dumps(body).encode() if body is not None else None
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=30) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"raw": raw[:200]}


def check(name, ok, detail=""):
    RESULTS.append(ok)
    print(("  OK  " if ok else " FAIL ") + name + (f"  {detail}" if detail else ""))


def main():
    status, body = call("POST", "/login", body={"username": "admin", "password": "admin123"})
    assert status == 200 and body.get("token"), f"登录失败: {status} {body}"
    token = body["token"]

    status, detail = call("GET", "/system/user/1", token=token)
    user = detail.get("data") or {}
    # postIds 放在响应根节点，不在 data 里；取错会传出空数组，把该用户的岗位关联清空
    post_ids = detail.get("postIds")
    check("读取 admin 当前资料", status == 200 and user.get("userId") == 1, str(status))
    check("读取到岗位关联", isinstance(post_ids, list), f"postIds={post_ids}")

    status, info_before = call("GET", "/getInfo", token=token)
    version_before = (info_before.get("user") or {}).get("authVersion")
    check("登录态可用", status == 200 and info_before.get("code") == 200,
          f"authVersion={version_before}")

    payload = {
        "userId": 1,
        "nickName": user.get("nickName"),
        "phonenumber": user.get("phonenumber"),
        "email": user.get("email"),
        "sex": user.get("sex"),
        "remark": user.get("remark"),
        "postIds": post_ids,
        "reason": "回归验证：修改资料与岗位不应使会话失效",
    }
    status, saved = call("PUT", "/system/user", token=token, body=payload)
    check("保存资料/岗位", status == 200 and saved.get("code") == 200, f"{status} {saved.get('msg')}")

    status, listing = call("GET", "/system/user/list", token=token,
                           params={"pageNum": 1, "pageSize": 10})
    check("保存后列表接口仍可用（旧实现此处 401）",
          status == 200 and listing.get("code") == 200,
          f"{status} {listing.get('msg')}")

    status, info_after = call("GET", "/getInfo", token=token)
    version_after = (info_after.get("user") or {}).get("authVersion")
    check("会话未被撤销", status == 200 and info_after.get("code") == 200,
          f"authVersion={version_after}")
    check("auth_version 未递增", version_before == version_after,
          f"{version_before} -> {version_after}")
    check("昵称已回显", (info_after.get("user") or {}).get("nickName") == user.get("nickName"))

    # 改密码仍然必须失效会话，这条边界不能被本次改动破坏
    passed = sum(1 for r in RESULTS if r)
    print(f"\n{passed}/{len(RESULTS)} 通过")
    return 0 if passed == len(RESULTS) else 1


if __name__ == "__main__":
    raise SystemExit(main())
