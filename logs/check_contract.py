"""契约比对：资料包《接口全量清单.csv》↔ 后端 Spring 路由。

用途：核对报告里「249 个契约、243 匹配、6 处差异」这类结论是否成立，
并给出机器可复现的缺口清单（缺失 / 方法不符 / 契约外新增）。

用法：python logs/check_contract.py
退出码 1 表示存在缺失或方法不符。
"""
import csv
import re
import sys
from pathlib import Path

PACK = Path(r"E:/workbuddy/日常办公/机动车检测预约/gpt生成/机动车检测预约_完整开发资料包_V6.1_框架起点版")
CSV = PACK / "04_接口" / "接口全量清单.csv"
BACKEND = Path(r"E:/codex/xinyi/xinyi-rongli/src/main/java/com/xinyi/rongli")

METHOD_ANN = {
    "GetMapping": "GET",
    "PostMapping": "POST",
    "PutMapping": "PUT",
    "PatchMapping": "PATCH",
    "DeleteMapping": "DELETE",
}


def norm(path):
    """把路径变量统一成 {}，容忍尾斜杠与重复斜杠。"""
    path = re.sub(r"\{[^}]*\}", "{}", (path or "").strip())
    path = re.sub(r"/+", "/", path)
    return path.rstrip("/") or "/"


def backend_routes():
    routes = {}
    for f in BACKEND.rglob("*Controller.java"):
        text = f.read_text(encoding="utf-8")
        # 类级前缀只认 public class 之前出现的 @RequestMapping，避免把方法级绝对路径当类前缀
        class_pos = text.find("public class")
        head = text[:class_pos] if class_pos > 0 else ""
        cm = re.search(r'@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]*)"', head)
        prefix = cm.group(1) if cm else ""
        for ann, method in METHOD_ANN.items():
            # 有参注解取显式路径；裸注解（如 @PostMapping 单独一行）沿用类前缀
            pattern = r'@' + ann + r'(?:\(\s*(?:value\s*=\s*)?"([^"]*)"[^)]*\))?'
            for pm in re.finditer(pattern, text):
                sub = pm.group(1) or ""
                routes.setdefault((norm(prefix + "/" + sub), method), []).append(f.name)
    return routes


def contracts():
    rows = []
    with open(CSV, encoding="utf-8-sig", newline="") as fh:
        for row in csv.DictReader(fh):
            rows.append((row["method"].strip().upper(), norm(row["path"]), row["summary"], row["operationId"]))
    return rows


def main():
    if not CSV.exists():
        print("未找到契约清单:", CSV)
        return 2
    routes = backend_routes()
    items = contracts()
    print(f"契约接口: {len(items)} 个；后端已注册路由: {len(routes)} 条\n")

    missing, method_mismatch = [], []
    for method, path, summary, op in items:
        if (path, method) in routes:
            continue
        allowed = sorted({m for p, m in routes if p == path})
        if allowed:
            method_mismatch.append((method, path, summary, allowed))
        else:
            missing.append((method, path, summary))

    print(f"完全匹配: {len(items) - len(missing) - len(method_mismatch)}")
    print(f"方法/路径不符: {len(method_mismatch)}")
    for method, path, summary, allowed in method_mismatch:
        print(f"  - {method} {path}  {summary}（后端提供 {', '.join(allowed)}）")
    print(f"未实现: {len(missing)}")
    for method, path, summary in missing:
        print(f"  - {method} {path}  {summary}")

    # 契约外新增（用于说明联调增强端点，不计为缺口）
    contract_keys = {(p, m) for m, p, _, _ in items}
    extra = sorted(k for k in routes if k not in contract_keys)
    print(f"\n契约外新增端点: {len(extra)}")
    for path, method in extra:
        print(f"  + {method} {path}")

    return 1 if (missing or method_mismatch) else 0


if __name__ == "__main__":
    sys.exit(main())
