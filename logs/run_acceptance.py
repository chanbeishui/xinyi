"""验收用例执行器：把仓库内所有可执行校验接成一张台账，输出 evidence.json / evidence.md。

设计要点
--------
1. **不编造用例**。台账条目全部来自真实执行的断言（脚本输出的 [OK]/[FAIL] 行、
   或静态校验脚本的结论行），没有"应该通过"这种自评。
2. **可复核**。每条台账记录脚本名 + 原始输出行 + 耗时，任何人可单独重跑。
3. **分级**。P0 = 主链路与数据正确性（派单、学时、订阅、数据权限、接口契约、表结构）；
   P1 = 侧栏结构、后台页面连通性、存量接口冒烟。
4. **失败即非零退出**，可以直接串进 CI 或提交流程。

用法
----
    python logs/run_acceptance.py                 # 全量（需 8080/18080 已启动）
    python logs/run_acceptance.py --static-only   # 只跑不依赖服务的静态校验
    python logs/run_acceptance.py --out logs/evidence.json
"""
import argparse
import datetime
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# 可用环境变量指向其他实例（例如后台页面检查也可以打验证实例 18080）
BACKEND_DEV = os.environ.get("BACKEND_DEV", "http://127.0.0.1:8080")        # check_admin_pages.py 的目标
BACKEND_VERIFY = os.environ.get("BACKEND_VERIFY", "http://127.0.0.1:18080")  # 其余接口脚本的目标
CASES_PATH = os.path.join(ROOT, "docs", "acceptance", "test_cases_V6.0.json")

# 套件定义：id 前缀 / 脚本 / 模块 / 优先级 / 是否需要起服务 / 超时秒
SUITES = [
    ("ST", "logs/check_api_routes.py",       "接口契约",   "P0", False, 180),
    ("SS", "logs/check_mapper_schema.py",    "数据访问",   "P0", False, 180),
    ("SW", "logs/check_wxml.py",             "小程序构建", "P1", False, 180),
    ("SG", "logs/verify_groups_stats.py",    "侧栏与统计", "P1", True,  180),
    ("SP", "logs/check_admin_pages.py",      "后台页面",   "P1", True,  180),
    ("SK", "logs/smoke_regression_rongli.py", "存量接口",   "P1", True,  240),
    ("SR", "logs/verify_remaining.py",       "本轮新增",   "P0", True,  300),
    ("STK", "logs/verify_token_refresh.py",  "令牌续期",   "P0", True,  180),
    ("SPH", "logs/verify_phone_login.py",    "手机号授权", "P0", True,  240),
    ("SDF", "logs/verify_driver_flow.py",    "履约全链路", "P0", True,  300),
    ("SAF", "logs/verify_aftersale_flow.py", "售后申诉对账", "P0", True, 300),
    ("SCF", "logs/verify_config_pages.py",   "配置页校验", "P0", True,  180),
    ("STM", "logs/verify_time_display.py",   "时间口径",   "P0", True,  180),
    ("SIC", "logs/verify_integration_config.py", "第三方配置", "P0", True, 300),
    ("SND", "logs/verify_notification_dispatch.py", "消息自动下发", "P0", True, 300),
]

# 断言行两种风格：  [OK] 标题  -> 说明      /      OK  标题 路径  ...
RE_BRACKET = re.compile(r"^\s*\[(OK|FAIL|PASS)\]\s+(.+?)(?:\s+->\s+(.*))?$")
RE_PLAIN = re.compile(r"^\s{2}(OK|FAIL|PASS)\s{2}(.+?)$")

OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def reachable(base, timeout=3):
    try:
        with OPENER.open(base + "/captchaImage", timeout=timeout) as resp:
            return resp.status < 500
    except urllib.error.HTTPError as e:
        return e.code < 500
    except Exception:
        return False


def parse_cases(stdout):
    """从脚本输出里提取逐条断言，返回 [(status, title, detail)]。"""
    cases = []
    for raw in stdout.splitlines():
        line = raw.rstrip()
        m = RE_BRACKET.match(line) or RE_PLAIN.match(line)
        if m:
            status = "PASS" if m.group(1) in ("OK", "PASS") else "FAIL"
            title = m.group(2).strip()
            detail = (m.group(3) or "").strip() if m.lastindex and m.lastindex >= 3 else ""
            cases.append((status, title, detail))
    return cases


def conclusion_of(stdout):
    """静态脚本没有逐条断言时，取最后的结论行。"""
    lines = [l.strip() for l in stdout.splitlines() if l.strip()]
    return lines[-1] if lines else "（无输出）"


def run_suite(prefix, script, module, priority, needs_server, timeout, seq_start):
    path = os.path.join(ROOT, script)
    started = time.time()
    if not os.path.exists(path):
        return [{
            "id": f"{prefix}-000", "module": module, "priority": priority,
            "title": script, "status": "SKIP", "detail": "脚本不存在",
            "suite": script, "durationMs": 0,
        }], 0
    proc = subprocess.run([sys.executable, script], cwd=ROOT, capture_output=True,
                          text=True, timeout=timeout, encoding="utf-8", errors="replace")
    elapsed = int((time.time() - started) * 1000)
    out = (proc.stdout or "") + "\n" + (proc.stderr or "")
    cases = parse_cases(proc.stdout or "")

    if not cases:
        # 没有逐条断言：用退出码 + 结论行表达一条用例
        status = "PASS" if proc.returncode == 0 else "FAIL"
        cases = [(status, conclusion_of(proc.stdout or ""), "")]

    records = []
    for i, (status, title, detail) in enumerate(cases, start=1):
        records.append({
            "id": f"{prefix}-{i:03d}",
            "module": module,
            "priority": priority,
            "title": title,
            "status": status,
            "detail": detail,
            "suite": script,
            "durationMs": elapsed if len(cases) == 1 else None,
        })
    return records, elapsed


def load_cases():
    """读取资料包验收规格（298 条），保留其原始 status，不替它下结论。"""
    if not os.path.exists(CASES_PATH):
        return []
    with open(CASES_PATH, encoding="utf-8") as f:
        return json.load(f)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--static-only", action="store_true", help="只跑不依赖后端服务的校验")
    ap.add_argument("--out", default=os.path.join(ROOT, "logs", "evidence.json"))
    args = ap.parse_args()

    server_ok = {
        BACKEND_DEV: reachable(BACKEND_DEV),
        BACKEND_VERIFY: reachable(BACKEND_VERIFY),
    }
    print("== 服务可达性")
    for k, v in server_ok.items():
        print(f"   {k}  {'可用' if v else '不可用'}")

    records = []
    print("\n== 执行套件")
    for prefix, script, module, priority, needs_server, timeout in SUITES:
        if needs_server and args.static_only:
            print(f"   [SKIP] {script}（--static-only）")
            continue
        if needs_server:
            base = BACKEND_DEV if "admin_pages" in script else BACKEND_VERIFY
            if not server_ok.get(base):
                print(f"   [SKIP] {script}（{base} 不可达）")
                records.append({
                    "id": f"{prefix}-000", "module": module, "priority": priority,
                    "title": script, "status": "SKIP",
                    "detail": f"{base} 不可达，未执行", "suite": script, "durationMs": 0,
                })
                continue
        try:
            suite_records, elapsed = run_suite(prefix, script, module, priority,
                                               needs_server, timeout, 0)
        except subprocess.TimeoutExpired:
            print(f"   [FAIL] {script} 超时 {timeout}s")
            suite_records = [{
                "id": f"{prefix}-000", "module": module, "priority": priority,
                "title": script, "status": "FAIL", "detail": f"执行超时 {timeout}s",
                "suite": script, "durationMs": timeout * 1000,
            }]
            elapsed = timeout * 1000
        passed = sum(1 for r in suite_records if r["status"] == "PASS")
        failed = sum(1 for r in suite_records if r["status"] == "FAIL")
        print(f"   {script:<42} 通过 {passed:>3} / 失败 {failed:>3}  {elapsed}ms")
        for r in suite_records:
            if r["status"] == "FAIL":
                print(f"        FAIL {r['id']} {r['title']}  {r['detail']}")
        records.extend(suite_records)

    total = len(records)
    passed = sum(1 for r in records if r["status"] == "PASS")
    failed = sum(1 for r in records if r["status"] == "FAIL")
    skipped = sum(1 for r in records if r["status"] == "SKIP")

    cases = load_cases()
    case_status = {}
    case_modules = {}
    for c in cases:
        st = (c.get("status") or "NOT_RUN").upper()
        case_status[st] = case_status.get(st, 0) + 1
        m = c.get("module", "未分类")
        case_modules.setdefault(m, {"total": 0, "p0": 0, "p1": 0, "done": 0})
        case_modules[m]["total"] += 1
        case_modules[m]["p0" if c.get("priority") == "P0" else "p1"] += 1
        if st in ("PASS", "DONE", "COVERED"):
            case_modules[m]["done"] += 1

    evidence = {
        "generatedAt": datetime.datetime.now().astimezone().isoformat(timespec="seconds"),
        "environment": {
            "root": ROOT,
            "python": sys.version.split()[0],
            "backendDev": {"url": BACKEND_DEV, "reachable": server_ok[BACKEND_DEV]},
            "backendVerify": {"url": BACKEND_VERIFY, "reachable": server_ok[BACKEND_VERIFY]},
        },
        "totals": {"total": total, "passed": passed, "failed": failed, "skipped": skipped,
                   "passRate": round(passed / total * 100, 1) if total else 0.0},
        "suites": [{"script": s[1], "module": s[2], "priority": s[3], "needsServer": s[4]}
                   for s in SUITES],
        "assertions": records,
        "spec": {
            "source": os.path.relpath(CASES_PATH, ROOT).replace("\\", "/"),
            "total": len(cases),
            "byStatus": case_status,
            "byModule": {k: v for k, v in sorted(case_modules.items(),
                                                key=lambda kv: -kv[1]["total"])},
            "cases": [{
                "id": c.get("id"), "priority": c.get("priority"),
                "module": c.get("module"), "flow": c.get("flow"),
                "title": c.get("title"), "given": c.get("given"),
                "when": c.get("when"), "then": c.get("then"),
                "status": (c.get("status") or "NOT_RUN").upper(),
                "evidence": c.get("evidence", ""),
            } for c in cases],
        },
    }

    out_path = args.out if os.path.isabs(args.out) else os.path.join(ROOT, args.out)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(evidence, f, ensure_ascii=False, indent=2)

    md_path = os.path.splitext(out_path)[0] + ".md"
    with open(md_path, "w", encoding="utf-8") as f:
        f.write(f"# 验收执行台账\n\n生成时间：{evidence['generatedAt']}\n\n")
        f.write("## 一、自动化断言\n\n")
        f.write(f"共 {total} 条断言：通过 {passed}，失败 {failed}，跳过 {skipped}"
                f"（通过率 {evidence['totals']['passRate']}%）\n\n")
        f.write("| 断言 | 模块 | 级别 | 结果 | 断言项 | 说明 |\n|---|---|---|---|---|---|\n")
        for r in records:
            f.write(f"| {r['id']} | {r['module']} | {r['priority']} | {r['status']} "
                    f"| {r['title']} | {r['detail'][:120]} |\n")

        if cases:
            f.write("\n## 二、资料包验收规格（298 条）执行现状\n\n")
            f.write(f"来源：`{evidence['spec']['source']}`，共 {len(cases)} 条。\n\n")
            f.write("> 本节状态取自规格文件本身（`status` 字段），执行器**不代替人工判定**。\n"
                    "> 自动化断言只覆盖其中一部分实现不变量，不能据此宣称对应业务用例通过。\n\n")
            f.write("按状态：" + "，".join(f"{k} {v}" for k, v in case_status.items()) + "\n\n")
            f.write("| 模块 | 总数 | P0 | P1 | 已标记完成 |\n|---|---|---|---|---|\n")
            for m, v in sorted(case_modules.items(), key=lambda kv: -kv[1]["total"]):
                f.write(f"| {m} | {v['total']} | {v['p0']} | {v['p1']} | {v['done']} |\n")

    print(f"\n== 汇总：{total} 条，通过 {passed}，失败 {failed}，跳过 {skipped}"
          f"（通过率 {evidence['totals']['passRate']}%）")
    if cases:
        print(f"   规格用例 {len(cases)} 条，状态分布：" +
              "，".join(f"{k} {v}" for k, v in case_status.items()))
    print(f"   台账：{out_path}")
    print(f"   便于人读：{md_path}")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
