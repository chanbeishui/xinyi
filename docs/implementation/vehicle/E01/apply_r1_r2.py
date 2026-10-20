#!/usr/bin/env python3
"""E01 · R1+R2 迁移脚本：为 15 个未绑定枚举列补齐 字典/data_model/openapi/SQL 四处一致的枚举约束。
可复现、幂等友好（重复运行会跳过已存在项）。R2 裁决：vi_aftersale_case.refund_state 绑定既有 RefundStatus(9值,含CLOSED)，
与 vi_refund_order.status->ChannelRefundStatus(渠道执行态) 分开，不取并集。
运行后必须复跑 check_package.py / engineer.py check / pytest 契约回归，保持全绿。"""
import json, re, io, sys
from pathlib import Path

R = Path("docs/vehicle/V6.1")
ENUM = R/"05_数据设计/枚举字典_V6.0.json"
MODEL = R/"05_数据设计/data_model_V6.0.json"
SQLF = R/"05_数据设计/001_vi业务表_初始化_V6.0.sql"
OAS = R/"04_接口/openapi_V6.0.yaml"

# (table, column, enumName, values, nullable)
BIND = [
 ("vi_aftersale_case","kind","AfterSaleKind",["CANCEL_REFUND","REFUND_ONLY"],False),
 ("vi_aftersale_case","return_state","ReturnState",["UNDETERMINED","NOT_REQUIRED","WAIT_RETURN","RETURNED"],False),
 ("vi_aftersale_case","refund_state","RefundStatus",["NONE","NOT_REQUIRED","WAIT_RETURN","READY","PROCESSING","SUCCESS","UNKNOWN","ABNORMAL","CLOSED"],False),
 ("vi_aftersale_review","decision","AfterSaleReviewDecision",["APPROVE","REJECT"],False),
 ("vi_dispatch_invite","source","InviteSource",["AUTO","MANUAL"],False),
 ("vi_driver_fee_ledger","entry_kind","FeeEntryKind",["NORMAL_RETURN","CANCEL_RETURN","CANCEL_BEFORE_PICKUP","ADJUSTMENT","INITIAL_VALUATION","PRICE_DELTA"],False),
 ("vi_driver_fee_ledger","currency","Currency",["CNY"],False),
 ("vi_driver_fee_rule","currency","Currency",["CNY"],False),
 ("vi_driver_statement","currency","Currency",["CNY"],False),
 ("vi_driver_statement_membership_change","action","MembershipAction",["CREATE","ADD","REMOVE","REACTIVATE","VOID"],False),
 ("vi_event_log","actor_realm","ActorRealm",["MINI","ADMIN","SYSTEM","CALLBACK"],True),
 ("vi_evidence_group","mileage_source","MileageSource",["MANUAL","OCR_REVIEWED"],True),
 ("vi_inspection_result","conclusion","InspectionConclusion",["PASS","FAIL"],False),
 ("vi_inspection_result","source","ResultSource",["STATION","ADMIN"],False),
 ("vi_session","active_role","SessionRole",["OWNER","DRIVER","STATION"],False),
]
NEW_ENUM_DESC = {
 "AfterSaleKind":"统一售后申请类型","ReturnState":"售后车辆返还编排状态",
 "AfterSaleReviewDecision":"统一售后审核决定","InviteSource":"派单邀约来源",
 "FeeEntryKind":"代驾费用台账条目类型","MembershipAction":"对账单成员变更动作",
 "ActorRealm":"操作者认证域","MileageSource":"里程读数来源",
 "InspectionConclusion":"检测结论","ResultSource":"检测结果登记来源",
 "SessionRole":"小程序会话视角(非授权来源)","Currency":"币种(首期仅CNY)",
}

def ck_name(table,col):
    n = "ck_"+table[3:]+"_"+col   # 去 vi_ 前缀
    return n[:64]

def check_sql(table,col,vals,nullable):
    q = ",".join("'%s'"%v for v in vals)
    if nullable:
        return "CONSTRAINT %s CHECK (`%s` IS NULL OR `%s` IN (%s))"%(ck_name(table,col),col,col,q)
    return "CONSTRAINT %s CHECK (`%s` IN (%s))"%(ck_name(table,col),col,q)

# ---------- 1) 枚举字典 ----------
en = json.loads(ENUM.read_text(encoding="utf8"))
added_enums=[]; added_bindings=[]
for t,c,name,vals,_ in BIND:
    if name not in en["enums"]:
        en["enums"][name]={"values":vals,"description":NEW_ENUM_DESC.get(name,"")}
        added_enums.append(name)
    else:
        # 既有枚举(RefundStatus)：核对值一致
        if en["enums"][name]["values"]!=vals:
            print("WARN 既有枚举 %s 值不一致: %s vs %s"%(name,en["enums"][name]["values"],vals))
    key="%s.%s"%(t,c)
    if key not in en["columnBindings"]:
        en["columnBindings"][key]=name; added_bindings.append(key)
ENUM.write_text(json.dumps(en,ensure_ascii=False,indent=2),encoding="utf8")

# ---------- 2) data_model ----------
dm = json.loads(MODEL.read_text(encoding="utf8"))
tmap={t["name"]:t for t in dm}
added_checks=[]; added_refs=[]
for t,c,name,vals,nullable in BIND:
    tb=tmap[t]
    cr=tb.setdefault("columnRules",{})
    if c in cr and cr[c].get("enumRef")!=name:
        cr[c]["enumRef"]=name; added_refs.append("%s.%s"%(t,c))
    elif c in cr and "enumRef" not in cr[c]:
        cr[c]["enumRef"]=name; added_refs.append("%s.%s"%(t,c))
    chk=check_sql(t,c,vals,nullable)
    if not any(("`%s` IN ("%c) in x for x in tb.get("checks",[])):
        tb.setdefault("checks",[]).append(chk); added_checks.append("%s.%s"%(t,c))
    # 同步列注释为 "Enum: v1,v2,..."（仅改注释，不改列名，满足 #30）
    for col in tb["cols"]:
        if col[0]==c:
            col[2]="%s: %s"%(name,",".join(vals))
MODEL.write_text(json.dumps(dm,ensure_ascii=False,indent=2),encoding="utf8")

# ---------- 3) SQL：在每张受影响表的 ) ENGINE= 前插入 CHECK ----------
sql=SQLF.read_text(encoding="utf8")
by_table={}
for t,c,name,vals,nullable in BIND:
    by_table.setdefault(t,[]).append(check_sql(t,c,vals,nullable))
inserted=0
for t,chks in by_table.items():
    # 定位该表块
    m=re.search(r"(CREATE TABLE `%s` \(.*?)(\n\) ENGINE=)"%re.escape(t),sql,re.S)
    if not m:
        print("ERR 未找到表块",t); continue
    body=m.group(1)
    new=[x for x in chks if x not in body]
    if new:
        # body 以最后一个列/约束结尾。若其末尾无逗号则补逗号，再以逗号分隔插入新约束(最后一条不带逗号)
        prefix = "" if body.rstrip().endswith(",") else ","
        addition = prefix + "\n" + ",\n".join("  "+x for x in new)
        sql = sql[:m.end(1)] + addition + sql[m.end(1):]
        inserted += len(new)
# refund_state 注释补 CLOSED（与9值枚举一致）
sql=sql.replace("`refund_state` VARCHAR(24) NOT NULL COMMENT 'NONE/NOT_REQUIRED/WAIT_RETURN/READY/PROCESSING/SUCCESS/UNKNOWN/ABNORMAL'",
                "`refund_state` VARCHAR(24) NOT NULL COMMENT 'RefundStatus: NONE,NOT_REQUIRED,WAIT_RETURN,READY,PROCESSING,SUCCESS,UNKNOWN,ABNORMAL,CLOSED'")
SQLF.write_text(sql,encoding="utf8")

# ---------- 4) openapi：在 components.schemas 下插入缺失枚举 schema ----------
oas=OAS.read_text(encoding="utf8")
need=[]
for t,c,name,vals,_ in BIND:
    if name not in [n for n in re.findall(r"^    ([A-Za-z0-9_]+):$",oas,re.M)]:
        if name not in [x[0] for x in need]:
            need.append((name,vals))
block=""
for name,vals in need:
    block+="    %s:\n      type: string\n      enum:\n"%name
    for v in vals: block+="      - %s\n"%v
if block:
    anchor="  schemas:\n"
    i=oas.index(anchor)+len(anchor)
    oas=oas[:i]+block+oas[i:]
OAS.write_text(oas,encoding="utf8")

print(json.dumps({
 "addedEnums":added_enums,"addedBindings":added_bindings,
 "addedEnumRefs":added_refs,"addedChecks":added_checks,
 "sqlChecksInserted":inserted,"openapiSchemasAdded":[n for n,_ in need],
},ensure_ascii=False,indent=1))
