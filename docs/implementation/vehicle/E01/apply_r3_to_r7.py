#!/usr/bin/env python3
"""E01 · R3–R7 修正脚本（在 R1/R2 已绿的基线上做增量）。可复现。运行后必须复跑 check_package / check_engineering / pytest 保持全绿。"""
import json, re
from pathlib import Path
R = Path("docs/vehicle/V6.1")
WL = R/"05_数据设计/生成列与写入白名单.json"
REG = R/"04_接口/权限与能力注册表.json"
LIST = R/"04_接口/接口全量清单.md"
AUTH = R/"04_接口/认证_权限_错误与路由.md"
VIREC_REF = R/"10_技术架构_T3.1/参考代码/ViRecord.java"

report = {}

# ---------------- R3 写白名单分三层 ----------------
IMMUTABLE = {  # 纯追加不可变历史：insert-only
 "vi_event_log","vi_driver_statement_membership_change","vi_driver_statement_snapshot",
 "vi_driver_price_change","vi_aftersale_review","vi_channel_event","vi_driver_fee_ledger",
}
CONTROLLED_RESTRICTED = {  # 受控生命周期：仅允许特定受控更新列（角色切换/消费/撤销/轮换/代际）
 "vi_session": ["status","context_version","active_role","station_id","revoked_at","linked_sys_user_id"],
 "vi_session_token": ["consumed_at","revoked_at","replaced_by_token_id"],
 "vi_account_security": ["session_epoch","permission_version","security_status","changed_at"],
}
EDITABLE_DRAFT = {  # 可编辑草稿/档案：常规字段更新
 "vi_owner_profile","vi_driver_profile","vi_address","vi_vehicle","vi_station",
 "vi_price_rule","vi_driver_fee_rule","vi_quota_slot",
}
wl = json.loads(WL.read_text(encoding="utf8"))
r3 = {"IMMUTABLE_HISTORY":[], "CONTROLLED_LIFECYCLE":[], "EDITABLE_DRAFT":[]}
for t in wl:
    name = t["table"]
    if name in IMMUTABLE:
        t["writeClass"] = "IMMUTABLE_HISTORY"
        t["updateColumns"] = []
        t["notes"] = (t.get("notes","") + " ｜R3:不可变历史，insert-only，updateColumns置空；更正/状态变化以追加新行表达。").strip(" ｜")
        r3["IMMUTABLE_HISTORY"].append(name)
    elif name in CONTROLLED_RESTRICTED:
        t["writeClass"] = "CONTROLLED_LIFECYCLE"
        t["updateColumns"] = CONTROLLED_RESTRICTED[name]
        t["notes"] = (t.get("notes","") + " ｜R3:受控生命周期，仅允许受控更新列(角色切换/消费/撤销/轮换/代际)，非全列可写。").strip(" ｜")
        r3["CONTROLLED_LIFECYCLE"].append(name)
    elif name in EDITABLE_DRAFT:
        t["writeClass"] = "EDITABLE_DRAFT"
        r3["EDITABLE_DRAFT"].append(name)
    else:
        t["writeClass"] = "CONTROLLED_LIFECYCLE"  # 其余多为状态机表，保留既有 updateColumns 白名单
        r3["CONTROLLED_LIFECYCLE"].append(name)
WL.write_text(json.dumps(wl, ensure_ascii=False, indent=2), encoding="utf8")
report["R3"] = {k: len(v) for k, v in r3.items()}

# ---------------- R4 指定唯一 ViRecord 权威源 ----------------
if VIREC_REF.exists():
    txt = VIREC_REF.read_text(encoding="utf8")
    banner = "// [R4] 参考草稿，非权威实现。唯一权威 ViRecord 见 14_工程模板/backend/src/main/java/com/xinyi/rongli/core/ViRecord.java（含 Long id）。\n// 自然键实体(vi_driver_occupancy/vi_dispatch_cursor/vi_driver_location/vi_handover_attempt_guard)不套本基类，单独映射。\n"
    if "[R4]" not in txt:
        VIREC_REF.write_text(banner + txt, encoding="utf8")
        report["R4"] = "已在 T3.1 参考版 ViRecord.java 顶部标注非权威，指向工程模板版(含id)"
    else:
        report["R4"] = "已标注，跳过"

# ---------------- R5 service-config 登记 ----------------
reg = json.loads(REG.read_text(encoding="utf8"))
ops = reg["operations"]
if not any(o.get("path") == "/api/public/service-config" for o in ops):
    ops.append({
        "method": "GET", "path": "/api/public/service-config",
        "operationId": "V6_get_public_service_config",
        "authorization": {"realm": "PUBLIC", "scope": "PUBLIC_WHITELIST"},
        "permission": None, "capability": None, "scope": None,
    })
    reg["operations"] = ops
    REG.write_text(json.dumps(reg, ensure_ascii=False, indent=2), encoding="utf8")
    report["R5_registry"] = "已补录 service-config，operations 现 %d" % len(ops)
else:
    report["R5_registry"] = "已存在，跳过"

md = LIST.read_text(encoding="utf8")
md = md.replace("从主 OpenAPI 当前内容生成，共 248 个操作。", "从主 OpenAPI 当前内容生成，共 249 个操作。")
row = "|`GET /api/public/service-config`<br>V6_get_public_service_config|首页及登录前公开服务说明与已发布协议|PUBLIC<br><br>||PublicServiceConfig|\n"
if "service-config" not in md:
    anchor = "|`GET /api/common/stations`<br>API_011_get|可见站点目录|MINI<br><br>COMMON_STATIONS_LIST||PublicStationView|\n"
    md = md.replace(anchor, anchor + row, 1)
    LIST.write_text(md, encoding="utf8")
    report["R5_list"] = "清单已加 service-config 行并改计数为 249"
else:
    report["R5_list"] = "清单已含，仅确保计数"
    LIST.write_text(md, encoding="utf8")

# ---------------- R7 术语统一 x-data-scope -> x-authorization.scope ----------------
auth = AUTH.read_text(encoding="utf8")
before = auth
auth = auth.replace("本人、本站、承担费用主体在x-data-scope/authorization中表达",
                    "本人、本站、承担费用主体在 x-authorization.scope 中表达")

# ---------------- R6 共享 common 端点四要素授权 ----------------
R6_SECTION = """
## 共享 common 端点授权（R6）
`/api/common/**` 中 handover、files、uploads、employee-binding 等端点为多调用方共享（车主 MINI / 员工 MINI(STATION 视角) / ADMIN 后台代办），在 OpenAPI 以 `x-capability` 表达 C 端能力，在权限注册表以 `biz:*` 表达后台功能权限。两者不是“任一满足即放行”，服务端按调用方认证域选择判据并叠加四要素，全部成立才放行：

1. 能力/功能权限：MINI 调用方校验 `x-capability`（如 COMMON_FILES_ACCESS、COMMON_HANDOVER_*）；ADMIN 调用方校验对应 `biz:*` 功能权限码。注册表 `biz:*` 与 yaml `x-capability` 是同一能力在两个认证域的登记，**以调用方所属认证域的判据为准**，不跨域借用。
2. 数据范围：本人/本站/承担费用主体范围由 `x-authorization.scope` 表达；handover 区分 HOLDER_ONLY（持码方读 secret）与 VERIFIER_ONLY（核验方仅提交，403 拒绝读码）；files/uploads 校验对象归属与业务绑定。
3. 状态：交接阶段/文件 READY/绑定凭证有效等前置状态必须满足。
4. 认证域：MINI token 不得用于 ADMIN 判据，反之亦然；员工 MINI(STATION) 取“真实员工有效权限 ∩ 小程序检测端允许动作”，不把后台通配权限复制给 MiniPrincipal。

任一要素不成立即拒绝（无权 403、未知/不可见目标统一 404、状态/版本冲突 409），不以“能力码匹配”单独放行。`employee-binding` 的签发(ADMIN, biz:employeebinding:issue)与消费(MINI, 能力 EMPLOYEE_BINDING + 一次性凭证)分属不同认证域与不同人，凭证仅此次返回、库内只存摘要。
"""
if "共享 common 端点授权（R6）" not in auth:
    anchor = "## 普通业务错误"
    auth = auth.replace(anchor, R6_SECTION.strip() + "\n\n" + anchor, 1)
AUTH.write_text(auth, encoding="utf8")
report["R6"] = "已新增『共享 common 端点授权』章节（四要素，全部成立才放行）"
report["R7"] = "已统一为 x-authorization.scope" if before != auth or "x-data-scope" not in auth else "未检出 x-data-scope"
report["R7_residual_x_data_scope"] = AUTH.read_text(encoding="utf8").count("x-data-scope")

print(json.dumps(report, ensure_ascii=False, indent=1))
