#!/usr/bin/env python3
"""扫描仓库文本源码的可读性信号；输出清单，不将启发式计数视为缺陷。"""

import argparse
import collections
import datetime
import json
from pathlib import Path
import re
import subprocess


SUFFIXES = {
    ".java", ".vue", ".ts", ".tsx", ".js", ".jsx", ".mjs", ".cjs",
    ".xml", ".sql", ".py", ".sh", ".bat", ".css", ".scss", ".less", ".vm",
}
EXCLUDED = {"node_modules", "target", "dist", ".git", ".vi-local", "logs", "uploadPath"}


def inspect(path, root):
    lines = path.read_text(encoding="utf-8").splitlines()
    long_lines = [i for i, line in enumerate(lines, 1) if len(line) > 160]
    # 分号可能来自 for、SQL、类型声明或字符串；只作为定位信号，必须人工确认。
    dense_lines = [
        i for i, line in enumerate(lines, 1)
        if line.count(";") >= 3 and not re.match(r"\s*(//|/\*|\*|#|<!--)", line)
    ]
    return {
        "file": path.relative_to(root).as_posix(),
        "lines": len(lines),
        "longestLine": max(map(len, lines), default=0),
        "longLineNumbers": long_lines,
        "denseLineNumbers": dense_lines,
        "commentLines": sum(bool(re.match(r"\s*(//|/\*|\*|#|<!--)", line)) for line in lines),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    names = subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"], cwd=root
    ).decode().split("\0")
    rows = []
    for name in sorted(set(names)):
        relative = Path(name)
        path = root / relative
        if relative.suffix not in SUFFIXES or set(relative.parts) & EXCLUDED:
            continue
        if not path.is_file() or path.is_symlink():
            continue
        try:
            row = inspect(path, root)
        except UnicodeDecodeError:
            continue
        # 历史资料、生成模板和嵌入静态资源单列，避免与手写业务逻辑混为一谈。
        row["group"] = (
            "资料与工程工具" if relative.parts[0] in {"doc", "docs"}
            else "静态资源与模板" if set(relative.parts) & {"public", "assets", "static", "vm"}
            else relative.parts[0] if len(relative.parts) > 1 else "根目录"
        )
        rows.append(row)
    result = {
        "at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "scope": "Git 管理及未忽略的文本源码，含业务、测试、脚本、样式、SQL、XML、工程工具和模板；排除依赖、产物及私有运行文件",
        "limitation": "全量静态扫描加重点人工抽查，不等于逐行语义审计；计数不作为缺陷或注释覆盖率",
        "filesScanned": len(rows),
        "groups": dict(collections.Counter(row["group"] for row in rows)),
        "files": rows,
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
    print(json.dumps({key: result[key] for key in ("filesScanned", "groups")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
