#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
给阿里云 OSS 配置生命周期规则（纯标准库，不依赖 oss2 / SDK）

背景：Excel 导出文件落在 download/yyyy/MM/dd/ 下，属于一次性交付物，
      不清理就是只增不减。本脚本给该前缀加一条「N 天后过期删除」的规则。

为什么是独立脚本而不是代码里做：生命周期是 Bucket 级配置，不是应用逻辑；
      而且 PutBucketLifecycle 是「整份替换」语义，草率调用会把别人配的规则冲掉，
      所以这里先读、合并、再写，并且默认只做 dry-run。

用法：
    # 只看当前规则 + 将要提交的内容（默认，不发写请求）
    python logs/set_oss_lifecycle.py --env-file deploy/prod/xinyi.env

    # 真正提交
    python logs/set_oss_lifecycle.py --env-file deploy/prod/xinyi.env --apply

    # 改前缀/天数
    python logs/set_oss_lifecycle.py --env-file deploy/prod/xinyi.env --prefix download/ --days 7

需要的环境变量：RONGLI_OSS_ENDPOINT / RONGLI_OSS_BUCKET / RONGLI_OSS_ACCESS_KEY / RONGLI_OSS_SECRET_KEY

RAM 授权：除对象读写外，还需要 Bucket 级的 oss:GetBucketLifecycle 与 oss:PutBucketLifecycle。
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

RULE_ID = "xinyi-download-expire"
REQUIRED = ("RONGLI_OSS_ENDPOINT", "RONGLI_OSS_BUCKET", "RONGLI_OSS_ACCESS_KEY", "RONGLI_OSS_SECRET_KEY")

HINTS = {
    "AccessDenied": "RAM 策略缺 oss:GetBucketLifecycle / oss:PutBucketLifecycle（Bucket 级动作，Resource 写 acs:oss:*:*:<bucket> 不带 /*）",
    "InvalidAccessKeyId": "AccessKey ID 不存在：多半是复制错了，或该 AK 已被删除/禁用",
    "SignatureDoesNotMatch": "Secret 不对（也常见于 Secret 前后多了空格/换行）",
    "NoSuchBucket": "Bucket 名字写错，或地域节点与 Bucket 所在地域不一致",
}


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
    不能从 URL 里截——截出来会变成 {@code /?lifecycle}，签名必然对不上。
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
    """把已有配置里的 <Rule> 逐块取出（整份替换语义下，必须先把别人的规则原样带上）。"""
    rules = []
    idx = 0
    while True:
        start = xml.find("<Rule>", idx)
        if start < 0:
            break
        end = xml.find("</Rule>", start)
        if end < 0:
            break
        end += len("</Rule>")
        rules.append(xml[start:end])
        idx = end
    return rules


def rule_id(block):
    if "<ID>" in block:
        return block.split("<ID>", 1)[1].split("</ID>", 1)[0]
    return ""


def build_rule(prefix, days):
    return (
        "  <Rule>\n"
        "    <ID>%s</ID>\n"
        "    <Prefix>%s</Prefix>\n"
        "    <Status>Enabled</Status>\n"
        "    <Expiration>\n"
        "      <Days>%d</Days>\n"
        "    </Expiration>\n"
        "  </Rule>" % (RULE_ID, prefix, days)
    )


def main():
    parser = argparse.ArgumentParser(add_help=True)
    parser.add_argument("--env-file", help="从 env 文件读取配置")
    parser.add_argument("--prefix", default="download/", help="要清理的前缀，默认 download/")
    parser.add_argument("--days", type=int, default=7, help="N 天后过期删除，默认 7 天")
    parser.add_argument("--apply", action="store_true", help="真正提交；不加只做 dry-run")
    args = parser.parse_args()

    if args.env_file:
        load_env_file(args.env_file)
    missing = [k for k in REQUIRED if not os.environ.get(k)]
    if missing:
        print("缺少环境变量：%s" % ", ".join(missing))
        return 1

    endpoint = normalize_endpoint(os.environ["RONGLI_OSS_ENDPOINT"])
    bucket = os.environ["RONGLI_OSS_BUCKET"].strip()
    ak = os.environ["RONGLI_OSS_ACCESS_KEY"].strip()
    sk = os.environ["RONGLI_OSS_SECRET_KEY"].strip()
    base = "https://%s.%s/?lifecycle" % (bucket, endpoint)
    resource = "/%s/?lifecycle" % bucket

    print("[1/3] 读取现有生命周期规则")
    status, body = call(base, resource, "GET", ak, sk)
    if status == 200:
        xml = body.decode("utf-8", "replace")
        print("  已有规则：")
        seen = 0
        for block in split_rules(xml):
            seen += 1
            prefix = block.split("<Prefix>", 1)[1].split("</Prefix>", 1)[0] if "<Prefix>" in block else ""
            state = block.split("<Status>", 1)[1].split("</Status>", 1)[0] if "<Status>" in block else ""
            print("    - id=%s prefix=%s status=%s" % (rule_id(block) or "(无)", prefix, state))
        if not seen:
            print("    （空配置）")
    elif status == 404:
        xml = ""
        print("  当前没有任何生命周期规则（NoSuchLifecycle），将从空白开始")
    else:
        code = body.decode("utf-8", "replace")
        print("  读取失败：%s" % code)
        for key, hint in HINTS.items():
            if key in code:
                print("  说明：%s" % hint)
        return 1

    print("\n[2/3] 合并规则（保留已有条目，只增/改 id=%s）" % RULE_ID)
    kept = [b for b in split_rules(xml) if rule_id(b) != RULE_ID]
    our = build_rule(args.prefix, args.days)
    new_xml = "<LifecycleConfiguration>\n" + "\n".join(kept + [our]) + "\n</LifecycleConfiguration>\n"
    print("  保留原有 %d 条，写入后共 %d 条" % (len(kept), len(kept) + 1))
    print("  本次写入：prefix=%s，%d 天后过期删除" % (args.prefix, args.days))

    if not args.apply:
        print("\n[3/3] dry-run：未提交。确认无误后加 --apply 执行")
        return 0

    print("\n[3/3] 提交")
    # PutBucketLifecycle 是整份替换：上面的合并结果就是最终状态
    status, body = call(base, resource, "PUT", ak, sk, new_xml.encode("utf-8"))
    if status != 200:
        code = body.decode("utf-8", "replace")
        print("  提交失败：%s" % code)
        for key, hint in HINTS.items():
            if key in code:
                print("  说明：%s" % hint)
        return 1
    print("  已生效：%s 下的对象在 %d 天后自动删除" % (args.prefix, args.days))
    print("  注意：规则只对「之后满足天数」的对象生效，已存在的对象按最后修改时间计算，同样会被清理")
    return 0


if __name__ == "__main__":
    sys.exit(main())
