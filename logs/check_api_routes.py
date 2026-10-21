"""静态校验：小程序端 API 调用路径是否都能在后端找到对应路由。

做法：
  1) 扫描 xinyi-rongli 的 Controller，解析类级 @RequestMapping 前缀与方法级注解，
     得到 (METHOD, 路径) 集合（路径变量统一成 {}）；
  2) 扫描 xinyi-rongli-wx/src/api/index.ts，解析 http.get/post/put/del 的路径；
  3) 对比，输出小程序调用了但后端不存在的组合。

用法：python logs/check_api_routes.py
退出码 1 表示存在不匹配。
"""
import re
import sys
from pathlib import Path

BACKEND = Path(r"E:/codex/xinyi/xinyi-rongli/src/main/java/com/xinyi/rongli")
MINI_API = Path(r"E:/codex/xinyi/xinyi-rongli-wx/src/api/index.ts")
UI_API = Path(r"E:/codex/xinyi/xinyi-ui/src/api/rongli/index.ts")

METHOD_ANN = {
    "GetMapping": "GET",
    "PostMapping": "POST",
    "PutMapping": "PUT",
    "PatchMapping": "PATCH",
    "DeleteMapping": "DELETE",
    "RequestMapping": "ANY",
}


def norm(path):
    path = re.sub(r"\{[^}]*\}", "{}", path.strip())
    path = re.sub(r"/+", "/", path)
    return path.rstrip("/") or "/"


def backend_routes():
    routes = set()
    for f in BACKEND.rglob("*Controller.java"):
        text = f.read_text(encoding="utf-8")
        m = re.search(r'@RequestMapping\(\s*"([^"]*)"', text)
        prefix = m.group(1) if m else ""
        for ann, method in METHOD_ANN.items():
            for pm in re.finditer(r'@' + ann + r'(?:\(\s*(?:value\s*=\s*)?"([^"]*)"[^)]*\))?', text):
                sub = pm.group(1) or ""
                routes.add((norm(prefix + "/" + sub), method))
    return routes


def frontend_calls(path):
    """解析两种前端封装写法：
       - http.get('/x') / http.post(`/x/${id}`)
       - request({ url: '/x', method: 'get' })
    """
    calls = []
    text = path.read_text(encoding="utf-8")
    for m in re.finditer(r'http\.(get|post|put|del|patch)\s*(?:<[^>]*>)?\s*\(\s*(`[^`]*`|\'[^\']*\'|"[^"]*")', text):
        verb = {"get": "GET", "post": "POST", "put": "PUT", "del": "DELETE", "patch": "PATCH"}[m.group(1)]
        calls.append((norm(_clean(m.group(2))), verb, _clean(m.group(2))))
    for m in re.finditer(r'url:\s*(`[^`]*`|\'[^\']*\'|"[^"]*")[^}]*?method:\s*[\'"]([a-zA-Z]+)[\'"]', text):
        raw = _clean(m.group(1))
        calls.append((norm(raw), m.group(2).upper(), raw))
    return calls


def _clean(literal):
    raw = literal[1:-1]
    raw = re.sub(r'\$\{[^}]*\}', '{}', raw)
    return raw.split("?")[0]


def main():
    routes = backend_routes()
    paths = {p for p, _ in routes}
    print(f"后端路由 {len(routes)} 条（去重路径 {len(paths)} 条）")

    bad = []
    for label, target in (("小程序", MINI_API), ("后台", UI_API)):
        if not target.exists():
            print(f"跳过 {label}：{target} 不存在")
            continue
        calls = frontend_calls(target)
        print(f"{label}接口 {len(calls)} 个")
        for p, verb, raw in calls:
            if not p.startswith("/api"):
                continue
            ok = (p, verb) in routes or (p, "ANY") in routes
            if not ok:
                # 路径存在但方法不同，单独提示
                allowed = sorted({m for pp, m in routes if pp == p})
                bad.append((label, verb, raw, allowed))

    if not bad:
        print("OK：两端接口路径与请求方法完全一致")
        return 0

    print(f"\n发现 {len(bad)} 处不匹配：")
    for label, verb, raw, allowed in bad:
        hint = f"（后端支持 {', '.join(allowed)}）" if allowed else "（后端无此路径）"
        print(f"  [{label}] {verb} {raw} {hint}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
