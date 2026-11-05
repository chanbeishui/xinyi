#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阿里云 OSS 配置自检（纯标准库，不依赖 oss2 / SDK）

用途：在把 RONGLI_OSS_* 填进 application.yml / systemd env 之前，先用它确认
      1) 六个变量是否齐全、格式对不对
      2) AK/SK 是否有效、RAM 授权是否够（真实 PUT + GET + DELETE 一个测试对象）
      3) endpoint 是公网还是内网（内网会导致小程序里的图片打不开）
      4) 预签名短链能不能从公网直接打开（这一条决定业务能否跑通）

用法：
    # 方式一：已在当前 shell 导出环境变量
    python logs/check_oss.py

    # 方式二：直接读部署用的 env 文件（systemd EnvironmentFile 同格式）
    python logs/check_oss.py --env-file deploy/prod/xinyi.env

    # 方式三：临时覆盖
    python logs/check_oss.py --endpoint oss-cn-hangzhou.aliyuncs.com --bucket my-bucket

退出码：0 全部通过；1 有失败项。
"""

import argparse
import base64
import hashlib
import hmac
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from email.utils import formatdate

VARS = {
    "RONGLI_OSS_ENABLED": "是否启用对象存储（必须为 true）",
    "RONGLI_OSS_ENDPOINT": "地域节点，如 oss-cn-hangzhou.aliyuncs.com（不要带 -internal）",
    "RONGLI_OSS_BUCKET": "Bucket 名称",
    "RONGLI_OSS_ACCESS_KEY": "RAM 用户的 AccessKey ID",
    "RONGLI_OSS_SECRET_KEY": "RAM 用户的 AccessKey Secret",
    "RONGLI_OSS_DOMAIN": "访问域名，以 / 结尾（仅影响直链，不影响短链）",
}

# OSS 返回的错误码 → 人话
HINTS = {
    "InvalidAccessKeyId": "AccessKey ID 不存在：多半是复制错了，或该 AK 已被删除/禁用",
    "SignatureDoesNotMatch": "Secret 不对（也常见于 Secret 前后多了空格/换行）",
    "AccessDenied": "AK 有效但没权限：检查 RAM 用户是否绑定了只含该 Bucket 的授权策略",
    "NoSuchBucket": "Bucket 名字写错，或地域节点与 Bucket 所在地域不一致",
    "SecondLevelDomainForbidden": "必须用 <bucket>.<endpoint> 的三级域名访问，检查 endpoint 是否写成了带 bucket 的形式",
    "InvalidBucketName": "Bucket 名称不合法",
    "RequestTimeTooSkewed": "本机时间与服务器偏差过大，校准系统时间",
}

ok_count = 0
fail_count = 0
warn_count = 0


def ok(msg):
    global ok_count
    ok_count += 1
    print("  [OK]   " + msg)


def fail(msg):
    global fail_count
    fail_count += 1
    print("  [FAIL] " + msg)


def warn(msg):
    global warn_count
    warn_count += 1
    print("  [WARN] " + msg)


def load_env_file(path):
    """按 systemd EnvironmentFile / .env 格式读取：KEY=VALUE，忽略 # 注释。"""
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
    """允许用户写 https://oss-cn-xxx.aliyuncs.com/ ，统一成裸域名。"""
    ep = (raw or "").strip()
    for prefix in ("https://", "http://"):
        if ep.lower().startswith(prefix):
            ep = ep[len(prefix):]
    return ep.rstrip("/")


def sign(secret, string_to_sign):
    digest = hmac.new(secret.encode("utf-8"), string_to_sign.encode("utf-8"), hashlib.sha1).digest()
    return base64.b64encode(digest).decode("ascii")


def object_url(bucket, endpoint, key):
    return "https://%s.%s/%s" % (bucket, endpoint, urllib.parse.quote(key, safe="/"))


def parse_error(body):
    """从 OSS 的 XML 错误体里抠出 Code / Message。"""
    code = ""
    message = ""
    if "<Code>" in body:
        code = body.split("<Code>", 1)[1].split("</Code>", 1)[0]
    if "<Message>" in body:
        message = body.split("<Message>", 1)[1].split("</Message>", 1)[0]
    return code, message


def do_request(req, method_desc):
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.status, resp.read(), None
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", "replace")
        code, message = parse_error(body)
        return e.code, b"", (code, message, method_desc)
    except urllib.error.URLError as e:
        return None, b"", ("NETWORK", str(e.reason), method_desc)
    except Exception as e:  # noqa: BLE001
        return None, b"", ("NETWORK", str(e), method_desc)


def main():
    parser = argparse.ArgumentParser(add_help=True)
    parser.add_argument("--env-file", help="从 env 文件读取配置（如 deploy/prod/xinyi.env）")
    parser.add_argument("--endpoint")
    parser.add_argument("--bucket")
    parser.add_argument("--access-key")
    parser.add_argument("--secret-key")
    parser.add_argument("--domain")
    parser.add_argument("--keep", action="store_true", help="自检对象不删除，便于到控制台核对")
    args = parser.parse_args()

    if args.env_file:
        load_env_file(args.env_file)
    for key, attr in (
        ("RONGLI_OSS_ENDPOINT", "endpoint"),
        ("RONGLI_OSS_BUCKET", "bucket"),
        ("RONGLI_OSS_ACCESS_KEY", "access_key"),
        ("RONGLI_OSS_SECRET_KEY", "secret_key"),
        ("RONGLI_OSS_DOMAIN", "domain"),
    ):
        v = getattr(args, attr)
        if v:
            os.environ[key] = v

    cfg = {k: (os.environ.get(k) or "").strip() for k in VARS}

    print("=" * 72)
    print("阿里云 OSS 配置自检")
    print("=" * 72)

    # ---------- 1. 变量齐全性 ----------
    print("\n[1/5] 检查环境变量")
    missing = []
    for key, desc in VARS.items():
        val = cfg[key]
        if not val:
            missing.append(key)
            fail("%s 未设置 —— %s" % (key, desc))
        else:
            shown = "******" if "KEY" in key else val
            ok("%s = %s" % (key, shown))
    if missing:
        print("\n缺少必填项，先补齐再跑。可参考 deploy/prod/xinyi.env.example")
        return 1

    if cfg["RONGLI_OSS_ENABLED"].lower() != "true":
        fail("RONGLI_OSS_ENABLED 不是 true，后端会走「对象存储未启用」分支，上传必然失败")
    else:
        ok("对象存储已启用")

    endpoint = normalize_endpoint(cfg["RONGLI_OSS_ENDPOINT"])
    bucket = cfg["RONGLI_OSS_BUCKET"]
    ak = cfg["RONGLI_OSS_ACCESS_KEY"]
    sk = cfg["RONGLI_OSS_SECRET_KEY"]
    domain = cfg["RONGLI_OSS_DOMAIN"]

    # ---------- 2. 形态检查 ----------
    print("\n[2/5] 检查配置形态")
    if "-internal" in endpoint:
        warn("endpoint 是内网节点（%s）：后端上传更快且免费，但小程序拿到的短链也是这个域名，"
             "公网解析不到 —— 除非你确认客户端和服务端在同一 VPC，否则必须换成公网节点" % endpoint)
    elif ".aliyuncs.com" not in endpoint:
        warn("endpoint 看起来不是阿里云 OSS 域名：%s" % endpoint)
    else:
        ok("endpoint 为公网节点：%s" % endpoint)

    if "." in bucket or bucket != bucket.lower():
        fail("Bucket 名称不应含点、必须全小写：%s" % bucket)
    else:
        ok("Bucket 名称形态正常：%s" % bucket)

    if not domain.endswith("/"):
        fail("RONGLI_OSS_DOMAIN 必须以 / 结尾，否则拼出来的直链会少一个分隔符：%s" % domain)
    else:
        ok("domain 以 / 结尾")

    if not (domain.startswith("http://") or domain.startswith("https://")):
        fail("RONGLI_OSS_DOMAIN 必须带 http:// 或 https:// 前缀")

    # ---------- 3. 真实读写 ----------
    print("\n[3/5] 用当前凭据真实上传 / 下载 / 删除一个测试对象")
    key = "_selfcheck/check-%d.txt" % int(time.time())
    body = ("rongli oss selfcheck %s\n" % time.strftime("%Y-%m-%d %H:%M:%S")).encode("utf-8")
    content_md5 = base64.b64encode(hashlib.md5(body).digest()).decode("ascii")
    content_type = "text/plain; charset=utf-8"

    # PUT
    date = formatdate(usegmt=True)
    sts = "PUT\n%s\n%s\n%s\n/%s/%s" % (content_md5, content_type, date, bucket, key)
    headers = {
        "Date": date,
        "Content-Type": content_type,
        "Content-MD5": content_md5,
        "Authorization": "OSS %s:%s" % (ak, sign(sk, sts)),
    }
    req = urllib.request.Request(object_url(bucket, endpoint, key), data=body, headers=headers, method="PUT")
    status, _, err = do_request(req, "上传")
    if err:
        code, message, _ = err
        fail("上传失败：%s %s" % (code, message))
        hint = HINTS.get(code)
        if hint:
            print("         可能原因：%s" % hint)
        if code == "NETWORK":
            print("         若是 -internal 节点，本机（非 ECS）本来就不可达，属预期；"
                  "但线上小程序同样打不开，请改用公网节点")
        print("\n凭据或权限没通过，后面的检查没有意义，先修这一项。")
        return 1
    ok("上传成功（HTTP %s），对象 key = %s" % (status, key))

    # GET
    date = formatdate(usegmt=True)
    sts = "GET\n\n\n%s\n/%s/%s" % (date, bucket, key)
    req = urllib.request.Request(
        object_url(bucket, endpoint, key),
        headers={
            "Date": date,
            "Authorization": "OSS %s:%s" % (ak, sign(sk, sts)),
        },
        method="GET",
    )
    status, got, err = do_request(req, "下载")
    if err:
        code, message, _ = err
        fail("下载失败：%s %s" % (code, message))
    elif got != body:
        fail("下载回来的内容与上传不一致（对象存储被中间层改写了？）")
    else:
        ok("下载成功，内容一致")

    # 预签名短链
    expires = int(time.time()) + 3600
    sts = "GET\n\n\n%d\n/%s/%s" % (expires, bucket, key)
    sig = sign(sk, sts)
    presigned = "%s?OSSAccessKeyId=%s&Expires=%d&Signature=%s" % (
        object_url(bucket, endpoint, key),
        urllib.parse.quote(ak, safe=""),
        expires,
        urllib.parse.quote(sig, safe=""),
    )
    req = urllib.request.Request(presigned, method="GET")
    status, got, err = do_request(req, "短链")
    if err:
        code, message, _ = err
        fail("预签名短链访问失败：%s %s（业务上小程序就是靠这条链接看图）" % (code, message))
    else:
        ok("预签名短链访问成功 —— 这条链接手机端也能打开")

    # DELETE
    date = formatdate(usegmt=True)
    sts = "DELETE\n\n\n%s\n/%s/%s" % (date, bucket, key)
    req = urllib.request.Request(
        object_url(bucket, endpoint, key),
        headers={
            "Date": date,
            "Authorization": "OSS %s:%s" % (ak, sign(sk, sts)),
        },
        method="DELETE",
    )
    status, _, err = do_request(req, "删除")
    if err:
        code, message, _ = err
        if code == "AccessDenied":
            warn("删除测试对象被拒绝（只给了读写权限）：请到控制台手工删掉 %s" % key)
        else:
            fail("删除失败：%s %s" % (code, message))
    else:
        ok("测试对象已删除，桶内无残留")

    # ---------- 4. 通用上传的 public-read ----------
    # 头像/富文本图走对象级 ACL，直链要长期可访问；这条链路和业务影像的私有+短链是分开的，
    # 单独失败不会在前面的 PUT/GET 里暴露出来，必须单独验一次。
    print("\n[4/5] 验证通用上传的 public-read（头像、富文本图靠它）")
    acl_key = "_selfcheck/acl-%d.txt" % int(time.time())
    acr_header = "public-read"
    content_md5 = base64.b64encode(hashlib.md5(body).digest()).decode("ascii")
    date = formatdate(usegmt=True)
    # 带 x-oss-* 头时，签名要把它按 "小写名:值\n" 拼进 CanonicalizedOSSHeaders 位置
    sts = "PUT\n%s\n%s\n%s\nx-oss-object-acl:%s\n/%s/%s" % (
        content_md5, content_type, date, acr_header, bucket, acl_key)
    req = urllib.request.Request(
        object_url(bucket, endpoint, acl_key),
        data=body,
        headers={
            "Date": date,
            "Content-Type": content_type,
            "Content-MD5": content_md5,
            "x-oss-object-acl": acr_header,
            "Authorization": "OSS %s:%s" % (ak, sign(sk, sts)),
        },
        method="PUT",
    )
    status, _, err = do_request(req, "带 ACL 上传")
    acl_ok = True
    if err:
        acl_ok = False
        code, message, _ = err
        fail("带 ACL 的普通上传失败：%s %s" % (code, message))
        if code == "AccessDenied":
            print("         两种可能，都要检查：")
            print("         ① RAM 策略缺 oss:PutObjectAcl（框架设 ACL 是单独调 SetObjectACL，不随 PUT 走）")
            print("         ② Bucket「权限控制 → 阻止公共访问」是开启状态，需关闭")
    else:
        ok("带 ACL 上传成功（x-oss-object-acl: %s）" % acr_header)

    if acl_ok:
        # 不带任何签名参数直接 GET，能读到才说明 ACL 真的生效、且公共访问没被拦
        req = urllib.request.Request(object_url(bucket, endpoint, acl_key), method="GET")
        status, got, err = do_request(req, "匿名读取")
        if err:
            code, message, _ = err
            fail("匿名直链读不到对象：%s %s —— 头像/富文本图会显示为裂图" % (code, message))
            print("         对象 ACL 设了 public-read 仍被拦，基本是 Bucket「阻止公共访问」还开着")
        elif got != body:
            fail("匿名读取内容不一致")
        else:
            ok("匿名直链可读 —— 头像、富文本图片地址不依赖签名，长期有效")

        # 清理
        date = formatdate(usegmt=True)
        sts = "DELETE\n\n\n%s\n/%s/%s" % (date, bucket, acl_key)
        req = urllib.request.Request(
            object_url(bucket, endpoint, acl_key),
            headers={"Date": date, "Authorization": "OSS %s:%s" % (ak, sign(sk, sts))},
            method="DELETE",
        )
        _, _, err = do_request(req, "删除")
        if err:
            warn("ACL 测试对象未删掉，请手工清理 %s" % acl_key)

    # ---------- 5. 交付说明 ----------
    print("\n[5/5] 下一步")
    print("  短链样例（1 小时内有效，可直接粘到浏览器/手机验证）：")
    print("  " + presigned)
    print("")
    print("  后端生效方式（二选一）：")
    print("    · 开发：把这 6 个变量导出到启动环境后重启，或用 IDEA 的 Environment variables 填")
    print("    · 生产：写进 /home/xinyi/app/.env，systemctl restart xinyi-admin")
    print("")
    print("-" * 72)
    print("通过 %d 项，失败 %d 项，警告 %d 项" % (ok_count, fail_count, warn_count))
    return 0 if fail_count == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
