"""E08 CI harness 受控测试工作区自测（对应 done_when：CI/任务/证据工具在受控测试工作区验证）。

在 tmp 工作区放最小 tasks.json/implementation_map/ci-stage，驱动 ci_run.py：
- run 模式：通过命令→退出 0；失败命令→非零退出码原样传播（失败退出码交付）。
- 跨平台：resolve_argv 用 shutil.which/当前解释器、subprocess shell=False（不经平台 shell）。
- report 模式：覆盖对账（api/page/table/case 总数）+ 未实跑层 NOT_RUN + ci-stage 透传。
不依赖真实 MySQL/HTTP/mvn；纯工具行为验证。
"""
import json
import os
import subprocess
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).resolve().parent
CI_RUN = HERE / "ci_run.py"
sys.path.insert(0, str(HERE))
import ci_run  # noqa: E402


def _make_workspace(tmp_path: Path) -> Path:
    state = tmp_path / "docs" / "implementation" / "vehicle"
    (state / "evidence").mkdir(parents=True)
    tasks = [
        {"id": "PASS_TASK", "scope": "DEVELOPMENT", "status": "VERIFIED",
         "commands": [f'{sys.executable} -c "import sys; sys.exit(0)"'],
         "evidence": [{"path": "docs/implementation/vehicle/evidence/PASS_TASK.json", "sha256": "x"}]},
        {"id": "FAIL_TASK", "scope": "DEVELOPMENT", "status": "NOT_ASSESSED",
         "commands": [f'{sys.executable} -c "import sys; sys.exit(3)"'], "evidence": []},
    ]
    (state / "tasks.json").write_text(json.dumps(tasks), encoding="utf8")
    (state / "ci-stage.json").write_text(json.dumps({"stage": "FOUNDATION"}), encoding="utf8")
    (state / "evidence" / "PASS_TASK.json").write_text(json.dumps({"taskId": "PASS_TASK"}), encoding="utf8")
    # 最小覆盖图：各 1 条，均无真实实现文件
    (state / "api_implementation_map.json").write_text(json.dumps(
        [{"operationId": "op1", "taskId": "A01", "implementationStatus": "NOT_ASSESSED",
          "controllerFile": "nope.java", "serviceFile": ""}]), encoding="utf8")
    (state / "page_implementation_map.json").write_text(json.dumps(
        [{"boardId": "b1", "taskId": "A01", "status": "NOT_ASSESSED", "implementationFile": "nope.vue"}]), encoding="utf8")
    (state / "table_implementation_map.json").write_text(json.dumps(
        [{"table": "vi_x", "taskId": "E06", "status": "NOT_ASSESSED", "evidence": []}]), encoding="utf8")
    (state / "case_implementation_map.json").write_text(json.dumps(
        [{"caseId": "c1", "taskId": "A01", "testMethod": "", "status": "NOT_RUN", "evidence": []}]), encoding="utf8")
    return tmp_path


def _run_cli(repo: Path, *args):
    return subprocess.run([sys.executable, str(CI_RUN), *args, "--repo", str(repo)],
                          capture_output=True, text=True, errors="replace", check=False, shell=False)


def test_run_mode_pass_exit_zero(tmp_path):
    ws = _make_workspace(tmp_path)
    r = _run_cli(ws, "run", "--task", "PASS_TASK")
    assert r.returncode == 0, r.stderr
    manifest = json.loads((ws / "docs/implementation/vehicle/evidence/ci-run-PASS_TASK.json").read_text())
    assert manifest["exitCode"] == 0
    assert manifest["results"][0]["exitCode"] == 0


def test_run_mode_fail_propagates_exit_code(tmp_path):
    ws = _make_workspace(tmp_path)
    r = _run_cli(ws, "run", "--task", "FAIL_TASK")
    assert r.returncode == 3, f"失败退出码必须原样传播，实际 {r.returncode} / {r.stderr}"
    manifest = json.loads((ws / "docs/implementation/vehicle/evidence/ci-run-FAIL_TASK.json").read_text())
    assert manifest["exitCode"] == 3


def test_run_mode_unknown_task_exit_2(tmp_path):
    ws = _make_workspace(tmp_path)
    r = _run_cli(ws, "run", "--task", "NOPE")
    assert r.returncode == 2


def test_resolve_argv_cross_platform_no_shell():
    argv = ci_run.resolve_argv('python -c "print(1)"')
    assert argv[0] == sys.executable, "python 必须解析为当前解释器（跨平台）"
    assert argv[1] == "-c"
    # 不存在的可执行不应抛异常（交给 subprocess 处理为 127）
    assert ci_run.resolve_argv("definitely_not_a_real_exe_xyz")[0] == "definitely_not_a_real_exe_xyz"


def test_report_mode_coverage_and_not_run(tmp_path):
    ws = _make_workspace(tmp_path)
    r = _run_cli(ws, "report")
    assert r.returncode == 0, r.stderr
    rep = json.loads(r.stdout)
    assert rep["ciStage"] == "FOUNDATION"
    assert rep["coverage"]["api"]["total"] == 1
    assert rep["coverage"]["api"]["implemented"] == 0, "无真实实现文件不得计为已实现"
    assert rep["coverage"]["case"]["passed"] == 0
    assert rep["layers"]["L01_REAL_CHANNEL"] == "NOT_RUN"
    assert rep["layers"]["L02_DEVICE"] == "NOT_RUN"
    assert rep["layers"]["L03_DEPLOY"] == "NOT_RUN"
    assert "E08" not in rep["tasks"]["verifiedWithEvidence"] or True  # 工作区仅含 PASS_TASK
    assert rep["tasks"]["verifiedWithEvidence"] == ["PASS_TASK"]


def test_report_does_not_count_pending_as_done(tmp_path):
    """negative：NOT_RUN/无实现不得被算作通过/完成。"""
    ws = _make_workspace(tmp_path)
    rep = json.loads(_run_cli(ws, "report").stdout)
    # FAIL_TASK 状态 NOT_ASSESSED，不得出现在 verifiedWithEvidence
    assert "FAIL_TASK" not in rep["tasks"]["verifiedWithEvidence"]
    assert rep["coverage"]["table"]["byStatus"].get("NOT_ASSESSED") == 1
