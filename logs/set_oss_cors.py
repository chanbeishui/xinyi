#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
给阿里云 OSS 配置跨域规则（纯标准库，不依赖 oss2 / SDK）

背景：浏览器里凡是需要「读像素」的图片，canvas 都必须先把图以 CORS 模式加载
      （crossOrigin="anonymous"）。OSS 直链默认不返回 Access-Control-Allow-Origin，
      于是头像裁剪（vue-cropper）、富文本插图、缩略图导出这类功能会取不到图，
      表现是「页面上图片好好的，裁剪框却是空白」。

      应用侧已经退到同源 /profile 兼容层绕开这个问题；本脚本是从根上补一条 Bucket 级
      CORS 规则，让直链也能被 canvas 读取，避免以后每个新功能都要再绕一次。

为什么是独立脚本而不是代码里做：CORS 是 Bucket 级配置，不是应用逻辑；
      而且 PutBucketCors 是「整份替换」语义，草率调用会把别人配的规则冲掉，
      所以这里先读、合并、再写，并且默认只做 dry-run。

用法：
    # 只看当前规则 + 将要提交的内容（默认，不发写请求）
    python logs/set_oss_cors.py --env-file /tmp/xinyi_oss.env

    # 真正提交
    python logs/set_oss_cors.py --env-file /tmp/xinyi_oss.env --apply

    # 只放行指定来源（可重复；默认放行任意来源，理由见下）
    python logs/set_oss_cors.py --env-file /tmp/xinyi_oss.env \
        --origin https://admin.example.com --origin http://localhost:5173 --apply

关于 AllowedOrigin 为什么默认 *：
      CORS 不是访问控制手段，它只决定浏览器允不允许脚本读响应。对象本身是
      public-read（头像/富文本图）或带签名的短链（业务影像），能读到什么仍由
      URL 自身权限决定；写 * 不会让私有对象变得可读。若后续想收紧，
      用 --origin 显式列出站点域名即可。

需要的环境变量：RONGLI_OSS_ENDPOINT / RONGLI_OSS_BUCKET / RONGLI_OSS_ACCESS_KEY / RONGLI_OSS_SECRET_KEY

RAM 授权：除对象读写外，还需要 Bucket 级的 oss:GetBucketCors 与 oss:PutBucketCors。
          没有这两条时脚本会明确报出来（AccessDenied），不影响业务上传下载。

退出码：0 成功；1 失败。
"""

import argparse
import base64
import hashlib
import hmac
import os
import sys
import urllib.error
import urllib.request
from email.utils import formatdate

RULE_ID = "xinyi-web-cors"
REQUIRED = ("RONGLI_OSS_ENDPOINT", "RONGLI_OSS_BUCKET", "RONGLI_OSS_ACCESS_KEY", "RONGLI_OSS_SECRET_KEY")
MAX_RULES = 10

HINTS = {
    "AccessDenied": "RAM 策略缺 oss:GetBucketCors / oss:PutBucketCors（Bucket 级动作，Resource 写 acs:oss:*:*:<bucket> 不带 /*）",
    "InvalidAccessKeyId": "AccessKey ID 不存在：多半是复制错了，或该 AK 已被删除/禁用",
    "SignatureDoesNotMatch": "Secret 不对（也常见于 Secret 前后多了空格/换行）",
    "NoSuchBucket": "Bucket 名字写错，或地域节点与 Bucket 所在地域不一致",
    "TooManyCorsRules": "CORS 规则超过 %d 条上限，先清理无用规则" % MAX_RULES,
    "InvalidArgument": "参数不合法：常见是 AllowedOrigin 少了 http(s):// 前缀，或 AllowedMethod 写错",
}

# OSS 对「RAM 缺 Bucket 级权限」会返回 AccessDenied + does not belong to you，
# 文案极具误导性（像是 bucket 写错了）。对象级操作有权限时尤其容易被带偏，单列说明。
BUCKET_SCOPE_HINT = (
    "这是 OSS 对「RAM 用户缺少 Bucket 级权限」的遮掩式报错，不是 bucket 写错。\n"
    "  判据：同一个 AK 读对象能通（GET 对象 200/404），而 ?cors / ?acl / ?lifecycle 全是这句 403。\n"
    "  处理：主账号给该 RAM 用户补一条策略，Resource 写 acs:oss:*:*:<bucket>（不带 /*），\n"
    "        Action 至少含 oss:GetBucketCors、oss:PutBucketCors；补完重跑本脚本即可。"
)


def print_hint(code):
    if "does not belong to you" in code:
        print("  说明：%s" % BUCKET_SCOPE_HINT)
        return
    for key, hint in HINTS.items():
        if key in code:
            print("  说明：%s" % hint)


def load_env_file(path):
    if not os.path.isfile(path):
        print("找不到 env 文件：%s" % path)
        sys.exit(1)
    with open(path, "r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            os.environ.setdefault(k.strip(), v.strip())


def normalize_endpoint(raw):
    ep = (raw or "").strip()
    for prefix in ("https://", "http://"):
        if ep.lower().startswith(prefix):
            ep = ep[len(prefix):]
    return ep.rstrip("/")


def sign(secret, string_to_sign):
    digest = hmac.new(secret.encode("utf-8"), string_to_sign.encode("utf-8"), hashlib.sha1).digest()
    return base64.b64encode(digest).decode("ascii")


def parse_error(body):
    code = ""
    message = ""
    if "<Code>" in body:
        code = body.split("<Code>", 1)[1].split("</Code>", 1)[0]
    if "<Message>" in body:
        message = body.split("<Message>", 1)[1].split("</Message>", 1)[0]
    return code, message


def call(url, resource, method, ak, sk, body=None, content_type="application/xml"):
    """发一个带 OSS V1 签名的请求。

    CanonicalizedResource 必须是 {@code /<bucket>/<object>?<子资源>}，
    而请求 URL 走的是 bucket 三级域名（不含 bucket 段），所以 resource 要单独传，
    不能从 URL 里截——截出来会变成 {@code /?cors}，签名必然对不上。
    """
    date = formatdate(usegmt=True)
    md5 = ""
    if body is not None:
        md5 = base64.b64encode(hashlib.md5(body).digest()).decode("ascii")
    sts = "%s\n%s\n%s\n%s\n%s" % (method, md5, content_type if body is not None else "", date, resource)
    headers = {"Date": date, "Authorization": "OSS %s:%s" % (ak, sign(sk, sts))}
    if body is not None:
        headers["Content-Type"] = content_type
        headers["Content-MD5"] = md5
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.status, resp.read()
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        code, message = parse_error(raw)
        return e.code, ("%s %s" % (code, message)).encode("utf-8")


def split_rules(xml):
    """把已有配置里的 <CORSRule> 逐块取出（整份替换语义下，必须先把别人的规则原样带上）。"""
    rules = []
    idx = 0
    tag_open, tag_close = "<CORSRule>", "</CORSRule>"
    while True:
        start = xml.find(tag_open, idx)
        if start < 0:
            break
        end = xml.find(tag_close, start)
        if end < 0:
            break
        end += len(tag_close)
        rules.append(xml[start:end])
        idx = end
    return rules


def tag_values(block, tag):
    out = []
    idx = 0
    open_tag, close_tag = "<%s>" % tag, "</%s>" % tag
    while True:
        start = block.find(open_tag, idx)
        if start < 0:
            break
        start += len(open_tag)
        end = block.find(close_tag, start)
        if end < 0:
            break
        out.append(block[start:end])
        idx = end
    return out


def rule_id(block):
    ids = tag_values(block, "ID")
    return ids[0] if ids else ""


def build_rule(origins, methods, headers, max_age):
    lines = ["  <CORSRule>", "    <ID>%s</ID>" % RULE_ID]
    for origin in origins:
        lines.append("    <AllowedOrigin>%s</AllowedOrigin>" % origin)
    for method in methods:
        lines.append("    <AllowedMethod>%s</AllowedMethod>" % method)
    for header in headers:
        lines.append("    <AllowedHeader>%s</AllowedHeader>" % header)
    # 让前端能读到这几个响应头（图片尺寸/缓存调试会用到）
    for expose in ("ETag", "Content-Length", "Content-Range", "x-oss-request-id"):
        lines.append("    <ExposeHeader>%s</ExposeHeader>" % expose)
    lines.append("    <MaxAgeSeconds>%d</MaxAgeSeconds>" % max_age)
    lines.append("  </CORSRule>")
    return "\n".join(lines)


def summarize(block):
    origins = ",".join(tag_values(block, "AllowedOrigin")) or "-"
    methods = ",".join(tag_values(block, "AllowedMethod")) or "-"
    return "id=%s origins=%s methods=%s" % (rule_id(block) or "(无)", origins, methods)


def main():
    parser = argparse.ArgumentParser(add_help=True)
    parser.add_argument("--env-file", help="从 env 文件读取配置")
    parser.add_argument("--origin", action="append",
                        help="允许的来源，可重复；默认 * （公开读对象，CORS 不是访问控制）")
    parser.add_argument("--method", action="append", default=None,
                        help="允许的方法，可重复；默认 GET 和 HEAD")
    parser.add_argument("--header", action="append", default=None,
                        help="允许的请求头，可重复；默认 *")
    parser.add_argument("--max-age", type=int, default=3600, help="预检结果缓存秒数，默认 3600")
    parser.add_argument("--apply", action="store_true", help="真正提交；不加只做 dry-run")
    args = parser.parse_args()

    if args.env_file:
        load_env_file(args.env_file)
    missing = [k for k in REQUIRED if not os.environ.get(k)]
    if missing:
        print("缺少环境变量：%s" % ", ".join(missing))
        return 1

    origins = args.origin or ["*"]
    methods = args.method or ["GET", "HEAD"]
    headers = args.header or ["*"]

    endpoint = normalize_endpoint(os.environ["RONGLI_OSS_ENDPOINT"])
    bucket = os.environ["RONGLI_OSS_BUCKET"].strip()
    ak = os.environ["RONGLI_OSS_ACCESS_KEY"].strip()
    sk = os.environ["RONGLI_OSS_SECRET_KEY"].strip()
    base = "https://%s.%s/?cors" % (bucket, endpoint)
    resource = "/%s/?cors" % bucket

    print("[1/3] 读取现有 CORS 规则")
    status, body = call(base, resource, "GET", ak, sk)
    if status == 200:
        xml = body.decode("utf-8", "replace")
        existing = split_rules(xml)
        print("  已有 %d 条：" % len(existing))
        for block in existing:
            print("    - %s" % summarize(block))
        if not existing:
            print("    （空配置）")
    elif status == 404:
        xml = ""
        existing = []
        print("  当前没有任何 CORS 规则（NoSuchCORSConfiguration），将从空白开始")
    else:
        code = body.decode("utf-8", "replace")
        print("  读取失败：%s" % code.strip())
        print_hint(code)
        return 1

    print("\n[2/3] 合并规则（保留已有条目，只增/改 id=%s）" % RULE_ID)
    kept = [b for b in existing if rule_id(b) != RULE_ID]
    our = build_rule(origins, methods, headers, args.max_age)
    if len(kept) + 1 > MAX_RULES:
        print("  合并后 %d 条，超过 OSS 上限 %d，请先清理无用规则" % (len(kept) + 1, MAX_RULES))
        return 1
    new_xml = "<CORSConfiguration>\n" + "\n".join(kept + [our]) + "\n</CORSConfiguration>\n"
    print("  保留原有 %d 条，写入后共 %d 条" % (len(kept), len(kept) + 1))
    print("  本次写入：origins=%s methods=%s headers=%s max-age=%ds"
          % (",".join(origins), ",".join(methods), ",".join(headers), args.max_age))

    if not args.apply:
        print("\n[3/3] dry-run：未提交。确认无误后加 --apply 执行")
        return 0

    print("\n[3/3] 提交")
    # PutBucketCors 是整份替换：上面的合并结果就是最终状态
    status, body = call(base, resource, "PUT", ak, sk, new_xml.encode("utf-8"))
    if status != 200:
        code = body.decode("utf-8", "replace")
        print("  提交失败：%s" % code.strip())
        print_hint(code)
        return 1
    print("  已生效：直链图片现在可以带 crossOrigin 被 canvas 读取")
    print("  验证：curl -sSI <任一对象直链> -H 'Origin: http://localhost:5173' | grep -i access-control")
    return 0


if __name__ == "__main__":
    sys.exit(main())
