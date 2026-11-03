"""部门数据权限（多部门任职）断言。

背景：后台数据范围早期只取 sys_user.dept_id 主部门，多部门任职的账号只能看到主部门的数据。
现在改为以 sys_user_dept 的全部有效任职部门为基础，按角色 data_scope 解析。

本脚本用数据库算出的期望值与接口返回值对比，证明：
1. 超管/持全部数据权限的账号不受部门限制；
2. 多部门账号看到的是任职部门的并集，而不是主部门；
3. 订单、车辆、售后三类数据的口径一致。

启动前提：验证实例监听 127.0.0.1:18080（python logs/run_acceptance.py 会自动跳过不可达实例）。
用法：python logs/verify_dept_scope.py
"""
import json
import subprocess
import sys
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:18080"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
FAILS = []
MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB_ARGS = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui",
           "--default-character-set=utf8mb4", "xinyi"]

# 角色数据范围（与 RuoYi/RuoYi-Vue3 常量一致）
SCOPE_ALL = "1"
SCOPE_CUSTOM = "2"
SCOPE_DEPT = "3"
SCOPE_DEPT_AND_CHILD = "4"
SCOPE_SELF = "5"


def sql(statement):
    result = subprocess.run([MYSQL] + DB_ARGS + ["-N", "-B", "-e", statement],
                            capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(result.stderr[:400])
    return result.stdout.strip()


def call(method, path, token=None, body=None):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
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
    print(("  OK  " if ok else " FAIL ") + name + (f"  {detail}" if detail else ""))
    if not ok:
        FAILS.append(name)
    return ok


def membership_dept_ids(user_name):
    raw = sql("SELECT GROUP_CONCAT(d.dept_id) FROM sys_user u "
              "JOIN sys_user_dept d ON d.user_id = u.user_id "
              f"WHERE u.user_name = '{user_name}' AND u.del_flag = '0'")
    return [int(x) for x in raw.split(",") if x]


def data_scopes(user_name):
    raw = sql("SELECT GROUP_CONCAT(DISTINCT r.data_scope) FROM sys_user u "
              "JOIN sys_user_role ur ON ur.user_id = u.user_id "
              "JOIN sys_role r ON r.role_id = ur.role_id "
              f"WHERE u.user_name = '{user_name}' AND u.del_flag = '0' AND r.status = '0'")
    return [x for x in (raw or "").split(",") if x]


def role_dept_ids(user_name):
    raw = sql("SELECT GROUP_CONCAT(DISTINCT rd.dept_id) FROM sys_user u "
              "JOIN sys_user_role ur ON ur.user_id = u.user_id "
              "JOIN sys_role r ON r.role_id = ur.role_id "
              "JOIN sys_role_dept rd ON rd.role_id = r.role_id "
              f"WHERE u.user_name = '{user_name}' AND u.del_flag = '0' AND r.status = '0'")
    return [int(x) for x in (raw or "").split(",") if x]


def expand_with_children(dept_ids):
    """本部门及以下：dept 本身 + ancestors 含它的所有部门。"""
    if not dept_ids:
        return []
    in_list = ",".join(str(x) for x in dept_ids)
    raw = sql("SELECT GROUP_CONCAT(dept_id) FROM sys_dept WHERE del_flag = '0' AND ("
              f"dept_id IN ({in_list}) OR " +
              " OR ".join(f"FIND_IN_SET({x}, ancestors)" for x in dept_ids) + ")")
    return [int(x) for x in (raw or "").split(",") if x]


def expected_depts(user_name):
    """按角色数据范围算出「应该可见」的部门集合；None 表示不受限。"""
    scopes = data_scopes(user_name)
    if SCOPE_ALL in scopes or not scopes:
        return None if SCOPE_ALL in scopes else []
    depts = set()
    if SCOPE_CUSTOM in scopes:
        depts.update(role_dept_ids(user_name))
    if SCOPE_DEPT in scopes:
        depts.update(membership_dept_ids(user_name))
    if SCOPE_DEPT_AND_CHILD in scopes:
        depts.update(expand_with_children(membership_dept_ids(user_name)))
    if SCOPE_SELF in scopes and not depts:
        # 仅本人：不展开部门，交给业务自行按主体过滤；此处按空集合记录
        depts.update(membership_dept_ids(user_name))
    return sorted(depts)


def in_clause(depts):
    return "(" + ",".join(str(x) for x in depts) + ")"


def count_where(table, depts, extra=""):
    if depts is None:
        where = "1 = 1"
    else:
        where = f"dept_id IN {in_clause(depts)}"
    return int(sql(f"SELECT COUNT(1) FROM {table} WHERE {where}{extra}") or 0)


def main():
    admin_status, admin_body = call("POST", "/login", body={"username": "admin", "password": "admin123"})
    if not check("管理员可登录", admin_status == 200 and admin_body.get("token"),
                 f"{admin_status} {str(admin_body)[:120]}"):
        return 1
    admin_token = admin_body["token"]

    ry_status, ry_body = call("POST", "/login", body={"username": "ry", "password": "admin123"})
    if not check("多部门账号可登录", ry_status == 200 and ry_body.get("token"),
                 f"{ry_status} {str(ry_body)[:120]}"):
        return 1
    ry_token = ry_body["token"]

    scopes = data_scopes("ry")
    members = membership_dept_ids("ry")
    expected = expected_depts("ry")
    print(f"  ry 角色数据范围={scopes} 任职部门={members} 期望可见部门={expected}")

    # ---- 1. 不受限账号：不做部门过滤
    admin_orders = call("GET", "/api/admin/orders?pageNum=1&pageSize=1", admin_token)[1].get("total")
    all_orders = int(sql("SELECT COUNT(1) FROM vi_order") or 0)
    check("超管订单数等于全量（不受部门限制）", admin_orders == all_orders,
          f"接口={admin_orders} 全量={all_orders}")

    # ---- 2. 多部门账号：可见数据是任职部门并集
    ry_orders = call("GET", "/api/admin/orders?pageNum=1&pageSize=1", ry_token)[1].get("total")
    expected_orders = count_where("vi_order", expected)
    check("多部门账号订单数等于任职部门并集", ry_orders == expected_orders,
          f"接口={ry_orders} 期望={expected_orders} 部门={expected}")

    # ---- 3. 与「只看主部门」的旧口径对比，证明修复生效
    primary = sql("SELECT dept_id FROM sys_user WHERE user_name = 'ry' AND del_flag = '0'")
    primary_depts = expand_with_children([int(primary)]) if primary else []
    primary_orders = count_where("vi_order", primary_depts)
    if primary_orders != expected_orders:
        check("可见范围不再等于主部门（多部门已展开）", ry_orders == expected_orders,
              f"主部门口径={primary_orders} 任职并集={expected_orders}")
    else:
        print(f"  主部门口径与任职并集相同（{primary_orders}），当前数据无法区分")

    # ---- 4. 车辆与售后口径一致
    ry_vehicles = call("GET", "/api/admin/vehicles?pageNum=1&pageSize=1", ry_token)[1].get("total")
    expected_vehicles = int(sql(
        "SELECT COUNT(1) FROM vi_vehicle WHERE deleted_at IS NULL AND "
        + ("1 = 1" if expected is None
           else f"(dept_id IS NULL OR dept_id IN {in_clause(expected)})")) or 0)
    check("多部门账号车辆数与任职部门并集一致", ry_vehicles == expected_vehicles,
          f"接口={ry_vehicles} 期望={expected_vehicles}")

    expected_cases = int(sql(
        "SELECT COUNT(1) FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id WHERE "
        + ("1 = 1" if expected is None else f"o.dept_id IN {in_clause(expected)}")) or 0)
    ry_cases = call("GET", "/api/admin/after-sales?pageNum=1&pageSize=1", ry_token)[1].get("total")
    check("多部门账号售后数与任职部门并集一致", ry_cases == expected_cases,
          f"接口={ry_cases} 期望={expected_cases}")

    # ---- 5. 统计口径与列表口径一致（同一套部门范围）
    ry_stats = call("GET", "/api/admin/statistics?range=DAY", ry_token)[1]
    stats_orders = (ry_stats.get("kpi") or {}).get("sampleOrders")
    if stats_orders is not None:
        check("统计卡样本订单数不超过可见订单数", stats_orders <= (ry_orders or 0),
              f"统计={stats_orders} 列表={ry_orders}")

    print(f"\n结论：{'通过' if not FAILS else '失败 ' + str(len(FAILS)) + ' 项'}")
    return 0 if not FAILS else 1


if __name__ == "__main__":
    sys.exit(main())
