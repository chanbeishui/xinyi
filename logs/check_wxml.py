#!/usr/bin/env python3
"""WXML 结构自检：捕获 uni build 不会报、只在微信开发者工具编译期暴露的问题。

背景：Vue 模板里用单引号包裹属性、值中又含双引号时（如 placeholder='{"a":"b"}'），
uni 编译 WXML 会统一用双引号包裹属性，内层引号未转义会截断属性，开发者工具报
`unexpected character`。`uni build` 本身不校验 WXML，因此需要单独扫描。

用法：
    python logs/check_wxml.py [dist 目录]
默认扫描 xinyi-rongli-wx/dist/dev/mp-weixin（不存在则退回 dist/build/mp-weixin）。
"""
import glob
import os
import re
import sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CANDIDATES = [
    os.path.join(BASE, "xinyi-rongli-wx", "dist", "dev", "mp-weixin"),
    os.path.join(BASE, "xinyi-rongli-wx", "dist", "build", "mp-weixin"),
]

# 属性值内出现裸双引号时，正则匹配会在错误位置提前闭合，闭合引号后紧跟的字符不是
# 空白 / `/` / `>`，据此识别截断。
ATTR_RE = re.compile(r'="[^"]*"')


def scan(dist: str):
    files = sorted(glob.glob(os.path.join(dist, "**", "*.wxml"), recursive=True))
    problems = []
    for path in files:
        with open(path, encoding="utf-8") as fh:
            text = fh.read()
        for m in ATTR_RE.finditer(text):
            nxt = text[m.end():m.end() + 1]
            if nxt and nxt not in " \t\r\n/>":
                problems.append((
                    os.path.relpath(path, dist),
                    "属性值内未转义双引号",
                    text[max(0, m.start() - 50):m.end() + 50],
                ))
        # 无效 UTF-8 字节会让开发者工具报 unexpected character
        raw = open(path, "rb").read()
        try:
            raw.decode("utf-8")
        except UnicodeDecodeError as exc:
            problems.append((
                os.path.relpath(path, dist),
                "非法 UTF-8 字节 @%d" % exc.start,
                repr(raw[max(0, exc.start - 30):exc.start + 30]),
            ))
    return len(files), problems


def main():
    dist = sys.argv[1] if len(sys.argv) > 1 else next(
        (d for d in CANDIDATES if os.path.isdir(d)), None
    )
    if not dist or not os.path.isdir(dist):
        print("未找到编译产物，请先执行 npm run dev:mp-weixin")
        return 2
    total, problems = scan(dist)
    print("扫描目录:", dist)
    print("wxml 文件数:", total, "问题数:", len(problems))
    for name, reason, ctx in problems[:20]:
        print(" -", name, "|", reason)
        print("   ", repr(ctx))
    if problems:
        print("结论: FAIL")
        return 1
    print("结论: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
