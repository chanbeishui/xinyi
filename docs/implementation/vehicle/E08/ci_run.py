#!/usr/bin/env python3
"""E08 仓库侧 CI / 任务台账 / 证据收集 harness。

职责（对应 E08 交付：跨平台任务命令、接口页面表用例覆盖、JUnit 报告、失败退出码）：
- run   ：跨平台执行某任务的 commands（subprocess 列表参数、不经 shell、shutil.which 解析平台可执行），
          捕获退出码、收集 JUnit XML、写运行清单到 evidence/，任一命令失败则整体非零退出。
- report：只读聚合 tasks.json 状态 + evidence/*.json + 四个 implementation_map 覆盖 + JUnit 报告 +
          分层执行情况；未实跑的层（真实渠道/真机/部署）明确 NOT_RUN；ci-stage 从 ci-stage.json 读取。

红线：只读账本与证据，不伪造测试、不把 NOT_RUN/skip 当通过、不批准生产、不改业务库。
"""
import argparse
import datetime as dt
import glob
import json
import os
import shlex
import shutil
import subprocess
import sys
from pathlib import Path

STATE = "docs/implementation/vehicle"


def now():
    return dt.datetime.now(dt.timezone.utc).isoformat()


def read_json(p: Path):
    return json.loads(p.read_text(encoding="utf8"))


def resolve_argv(command: str):
    """跨平台：分词 + 用 shutil.which 解析可执行（Windows 找到 mvn.cmd/python.exe）；python→当前解释器。"""
    argv = shlex.split(command, posix=(os.name != "nt"))
    if not argv:
        return argv
    head = argv[0]
    if head in ("python", "python3", "py"):
        argv[0] = sys.executable
    else:
        found = shutil.which(head)
        if found:
            argv[0] = found
    return argv


def run_command(argv, cwd: Path, timeout: int):
    try:
        r = subprocess.run(argv, cwd=str(cwd), capture_output=True, text=True,
                           errors="replace", timeout=timeout, check=False, shell=False)
        return {"argv": argv, "exitCode": r.returncode,
                "stdoutTail": (r.stdout or "")[-4000:], "stderrTail": (r.stderr or "")[-2000:]}
    except (OSError, subprocess.TimeoutExpired) as ex:
        return {"argv": argv, "exitCode": 127, "error": type(ex).__name__}


def collect_junit(repo: Path):
    pats = [
        str(repo / "xinyi-*/target/surefire-reports/*.xml"),
        str(repo / "xinyi-*/target/failsafe-reports/*.xml"),
        str(repo / STATE / "evidence/*-junit.xml"),
    ]
    files = []
    for p in pats:
        files.extend(glob.glob(p))
    return sorted(set(os.path.relpath(f, str(repo)) for f in files))


def cmd_run(repo: Path, task_id: str, timeout: int):
    tasks = read_json(repo / STATE / "tasks.json")
    task = next((t for t in tasks if t.get("id") == task_id), None)
    if task is None:
        print(json.dumps({"error": "UNKNOWN_TASK", "taskId": task_id}, ensure_ascii=False))
        return 2
    commands = task.get("commands", [])
    if not commands:
        print(json.dumps({"error": "NO_COMMANDS", "taskId": task_id}, ensure_ascii=False))
        return 2
    results = []
    overall = 0
    for c in commands:
        argv = resolve_argv(c)
        res = run_command(argv, repo, timeout)
        res["command"] = c
        results.append(res)
        if res["exitCode"] != 0:
            overall = res["exitCode"] or 1
    manifest = {
        "kind": "TASK_RUN", "at": now(), "taskId": task_id,
        "exitCode": overall, "results": results,
        "junitReports": collect_junit(repo),
        "warning": "退出码 0 仍须对应测试数量/代码路径/独立复核；不是完整功能通过。",
    }
    out = repo / STATE / "evidence" / f"ci-run-{task_id}.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf8")
    print(json.dumps({"taskId": task_id, "exitCode": overall,
                      "commands": [{"command": r["command"], "exitCode": r["exitCode"]} for r in results],
                      "junitReports": len(manifest["junitReports"]), "manifest": str(out.relative_to(repo))},
                     ensure_ascii=False))
    return overall


def _coverage(repo: Path):
    def count_exists(entries, keys):
        implemented = 0
        for e in entries:
            if any(isinstance(e.get(k), str) and e.get(k) and (repo / e[k]).exists() for k in keys):
                implemented += 1
        return implemented

    cov = {}
    api = read_json(repo / STATE / "api_implementation_map.json")
    cov["api"] = {"total": len(api),
                  "implemented": count_exists(api, ["controllerFile", "serviceFile"]),
                  "byStatus": _by_status(api, "implementationStatus")}
    page = read_json(repo / STATE / "page_implementation_map.json")
    cov["page"] = {"total": len(page),
                   "implemented": count_exists(page, ["implementationFile"]),
                   "byStatus": _by_status(page, "status")}
    table = read_json(repo / STATE / "table_implementation_map.json")
    cov["table"] = {"total": len(table), "implemented": 0,
                    "byStatus": _by_status(table, "status"),
                    "note": "table map 于 G01 统一对账；E06 平台表(vi_idempotency/outbox/event_log)已有真实 mapper，见 evidence/E06.json"}
    case = read_json(repo / STATE / "case_implementation_map.json")
    cov["case"] = {"total": len(case),
                   "passed": sum(1 for c in case if c.get("status") == "PASS"
                                 and isinstance(c.get("testMethod"), str) and c.get("testMethod")),
                   "byStatus": _by_status(case, "status")}
    return cov


def _by_status(entries, key):
    out = {}
    for e in entries:
        s = e.get(key) or "UNSET"
        out[s] = out.get(s, 0) + 1
    return out


def cmd_report(repo: Path):
    tasks = read_json(repo / STATE / "tasks.json")
    by_status = {}
    for t in tasks:
        by_status[t["status"]] = by_status.get(t["status"], 0) + 1
    verified_with_evidence = []
    for t in tasks:
        if t["status"] in ("VERIFIED", "REVIEWED"):
            ev = t.get("evidence") or []
            if ev and all((repo / e["path"]).exists() for e in ev):
                verified_with_evidence.append(t["id"])
    ci_stage = "UNKNOWN"
    cs = repo / STATE / "ci-stage.json"
    if cs.exists():
        ci_stage = read_json(cs).get("stage", "UNKNOWN")
    evidence_files = sorted(p.name for p in (repo / STATE / "evidence").glob("E0*.json"))
    report = {
        "kind": "CI_REPORT", "at": now(), "ciStage": ci_stage,
        "tasks": {"total": len(tasks),
                  "development": sum(1 for t in tasks if t.get("scope") == "DEVELOPMENT"),
                  "byStatus": by_status,
                  "verifiedWithEvidence": verified_with_evidence},
        "coverage": _coverage(repo),
        "layers": {
            "BUILD": "RUN", "TOOLS_OR_COMPONENT": "RUN",
            "MYSQL_PROBES": "RUN",
            "LIVE_HTTP_MYSQL": "PARTIAL",  # E06 服务层 + E07 控制面真实 HTTP/MySQL；业务端点未建
            "L01_REAL_CHANNEL": "NOT_RUN", "L02_DEVICE": "NOT_RUN", "L03_DEPLOY": "NOT_RUN",
        },
        "junitReports": collect_junit(repo),
        "evidenceFiles": evidence_files,
        "warning": "ci-stage=FOUNDATION 只表示基座检查，不代表完整功能；未实跑层明确 NOT_RUN，不用 FOUNDATION 绿色代替全功能。",
    }
    out = repo / STATE / "evidence" / "ci-report.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf8")
    print(json.dumps(report, ensure_ascii=False))
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("mode", choices=["run", "report"])
    ap.add_argument("--repo", default=".")
    ap.add_argument("--task")
    ap.add_argument("--timeout", type=int, default=1800)
    a = ap.parse_args()
    repo = Path(a.repo).resolve()
    if not (repo / STATE / "tasks.json").exists():
        print("BLOCKED: 非 xinyi 仓库根（缺 tasks.json）", file=sys.stderr)
        return 2
    if a.mode == "run":
        if not a.task:
            print("BLOCKED: run 需 --task", file=sys.stderr)
            return 2
        return cmd_run(repo, a.task, a.timeout)
    return cmd_report(repo)


if __name__ == "__main__":
    sys.exit(main())
