# -*- coding: utf-8 -*-
"""
小程序登录令牌失效专项验收。

验证三件事：
1. 切换角色/站点会在服务端撤销整条旧令牌链并签发新令牌 —— 前端若不保存新令牌，
   后续请求必然 401，且旧 refresh 已撤销无法自愈（这就是"频繁失效"的成因）。
2. 刷新令牌单次消费的并发保护：并发刷新只能成功一个，且成功者的新令牌不被误伤。
3. 请求层的"令牌快照比对"策略：令牌已被别处续期时应直接重放，不应再轮换一次。
"""
import json
import sys
import threading
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
LOGIN_CODE = "dev_owner_001"

results = []


def check(name, ok, detail=""):
    results.append((name, bool(ok), detail))
    print(("  [PASS] " if ok else "  [FAIL] ") + name + (("  " + detail) if detail else ""))


def call(method, path, body=None, token=None, timeout=15):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["X-Mini-Token"] = token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            raw = r.read().decode("utf-8")
            try:
                return r.status, json.loads(raw)
            except Exception:
                return r.status, raw
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw
    except Exception as e:  # 网络层
        return 0, str(e)


def login():
    return call("POST", "/api/common/login/wechat", {"code": LOGIN_CODE, "deviceLabel": "token-verify"})


def main():
    print("\n== 1. 基础登录 ==")
    st, view = login()
    if st != 200:
        print(f"  登录失败，无法继续：{st} {view}")
        return 1
    access, refresh = view.get("accessToken"), view.get("refreshToken")
    check("登录返回访问令牌与刷新令牌", bool(access) and bool(refresh), "sessionId=" + str(view.get("sessionId")))

    st, _ = call("GET", "/api/common/session", token=access)
    check("新签发的访问令牌可用", st == 200, f"HTTP={st}")

    print("\n== 2. 切换角色会轮换令牌（根因复现） ==")
    st, rotated = call("POST", "/api/common/session/role", {"role": "OWNER"}, token=access)
    check("切换角色成功", st == 200, f"HTTP={st}")
    new_access, new_refresh = rotated.get("accessToken"), rotated.get("refreshToken")
    check("切换角色返回了新令牌", bool(new_access) and new_access != access,
          f"ctx={rotated.get('contextVersion')}")

    st, body = call("GET", "/api/common/session", token=access)
    check("切换后的旧访问令牌立即失效", st == 401, f"HTTP={st} {body}")

    st, body = call("GET", "/api/common/session", token=new_access)
    check("切换后的新访问令牌可用（前端必须落盘）", st == 200, f"HTTP={st} {body}")

    st, body = call("POST", "/api/common/token/refresh", {"refreshToken": refresh})
    check("旧刷新令牌已被撤销，无法续期（不落盘就无法自愈）", st == 401, f"HTTP={st} {body}")

    st, refreshed = call("POST", "/api/common/token/refresh", {"refreshToken": new_refresh})
    check("新刷新令牌可正常续期", st == 200 and bool(refreshed.get("accessToken")), f"HTTP={st}")

    print("\n== 3. 并发刷新保护（单次消费） ==")
    st, b2 = call("POST", "/api/common/token/refresh", {"refreshToken": refreshed.get("refreshToken")})
    if st != 200:
        print(f"  无法取得用于并发测试的刷新令牌：{st} {b2}")
        return 1
    race_refresh = b2.get("refreshToken")
    race_access = b2.get("accessToken")

    barrier = threading.Barrier(2)
    outcomes = [None, None]

    def worker(idx):
        barrier.wait()
        outcomes[idx] = call("POST", "/api/common/token/refresh", {"refreshToken": race_refresh})

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(2)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    codes = sorted(o[0] for o in outcomes)
    check("并发刷新恰好一个成功、一个被拒", codes == [200, 401], f"HTTP 分布={codes}")

    winner = next((o[1] for o in outcomes if o[0] == 200), None)
    loser = next((o[1] for o in outcomes if o[0] == 401), None)
    check("失败方给出明确的重复使用提示",
          bool(loser) and ("已被使用" in json.dumps(loser, ensure_ascii=False)),
          json.dumps(loser, ensure_ascii=False)[:120] if loser else "无")

    if winner:
        st, body = call("GET", "/api/common/session", token=winner.get("accessToken"))
        check("并发中成功签发的令牌未被对方撤销", st == 200, f"HTTP={st} {body}")

    st, body = call("GET", "/api/common/session", token=race_access)
    check("刷新后上一代访问令牌按预期失效", st == 401, f"HTTP={st}")

    print("\n== 4. 令牌快照比对（迟到的 401 不应再轮换） ==")
    st, latest = call("POST", "/api/common/token/refresh", {"refreshToken": winner.get("refreshToken")})
    if st != 200:
        print(f"  续期失败，跳过：{st} {latest}")
        return 1
    stored_access = latest.get("accessToken")
    stored_refresh = latest.get("refreshToken")

    stale_access = race_access  # 模拟某请求飞行期间用的是上一代令牌
    st, _ = call("GET", "/api/common/session", token=stale_access)
    stale_401 = st == 401
    # 请求层策略：发现存储中的令牌已变，直接用新令牌重放，不再调用 refresh
    st, body = call("GET", "/api/common/session", token=stored_access)
    check("迟到 401 用当前令牌重放即可成功", stale_401 and st == 200, f"stale={stale_401} replay={st}")

    st, body = call("POST", "/api/common/token/refresh", {"refreshToken": stored_refresh})
    check("未发生多余轮换，刷新令牌仍然有效", st == 200, f"HTTP={st}")

    total = len(results)
    passed = sum(1 for r in results if r[1])
    print(f"\n== 汇总：{passed}/{total} 通过 ==")
    if passed != total:
        for name, ok, detail in results:
            if not ok:
                print(f"   失败：{name}  {detail}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
