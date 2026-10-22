"""Local policy/schema/tool tests only. Not a live xinyi/WeChat/MySQL test."""
from pathlib import Path
import importlib.util,sys,json
import pytest,yaml,jsonschema
B=Path(__file__).resolve().parents[2]
def load(name,path):
 s=importlib.util.spec_from_file_location(name,B/path);m=importlib.util.module_from_spec(s);sys.modules[name]=m;s.loader.exec_module(m);return m
P=load("identity_policy_reference","12_工程执行/identity_policy_reference.py")
E=load("v62_engineer","12_工程执行/engineer.py")
S=yaml.safe_load((B/"04_接口/openapi_V6.4.yaml").read_text())
def validate(name,value):
 return jsonschema.Draft7Validator(S["components"]["schemas"][name],
   resolver=jsonschema.RefResolver.from_schema(S),format_checker=jsonschema.FormatChecker()).is_valid(value)
def test_phone_only_and_credential_removed():
 assert "BindingCredential" not in S["components"]["schemas"]
 assert not any("employee-binding" in p for p in S["paths"])
 assert validate("PhoneRequest",{"phoneCode":"test","expectedAccountVersion":0,"verificationRequestId":"cc73f4e6-bd36-481d-9f75-514ca9148f83"})
 assert not validate("PhoneRequest",{"phone":"19900000000"})
 assert not validate("LoginRequest",{"code":"ok","role":"STATION"})
@pytest.mark.parametrize("source",["SMS","USER_INPUT","CONTACT",""])
def test_non_wechat_never_links(source):
 assert P.decide(1,"p",source,[P.Employee(12,"p")],[]).outcome=="NOT_ATTEMPTED"
def test_unique_match():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p")],[]).status=="LINKED"
def test_no_match_keeps_owner():
 assert P.decide(1,"p","WECHAT",[],[]).status=="UNLINKED"
def test_duplicate_join_rows_are_one_employee():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p"),P.Employee(12,"p")],[]).employee_id==12
def test_duplicate_people_fail_closed():
 x=P.decide(1,"p","WECHAT",[P.Employee(12,"p"),P.Employee(13,"p")],[])
 assert x.status=="NEEDS_ADMIN" and x.employee_id is None
@pytest.mark.parametrize("e",[P.Employee(12,"p",active=False),P.Employee(12,"p",deleted=True)])
def test_inactive_not_new_link(e):
 assert P.decide(1,"p","WECHAT",[e],[]).status=="UNLINKED"
def test_other_wechat_conflict():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p")],[P.Link(2,12)]).outcome=="USER_ALREADY_LINKED"
def test_other_employee_conflict():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p")],[P.Link(1,13)]).outcome=="ACCOUNT_ALREADY_LINKED"
def test_revoke_tombstone_blocks():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p")],[P.Link(1,12,"REVOKED",True)]).status=="REVOKED"
def test_same_active_idempotent():
 assert P.decide(1,"p","WECHAT",[P.Employee(12,"p")],[P.Link(1,12)]).outcome=="EXISTING_LINK"
def test_suspended_reserved_for_same_account():
 assert P.decide(2,"p","WECHAT",[P.Employee(12,"p")],[P.Link(1,12,"SUSPENDED")]).status=="NEEDS_ADMIN"
@pytest.mark.parametrize("driver,link,active,count,function,expected",[
 (False,"UNLINKED",True,1,True,["OWNER"]),
 (True,"UNLINKED",True,1,True,["OWNER","DRIVER"]),
 (False,"LINKED",True,1,True,["OWNER","STATION"]),
 (True,"LINKED",True,1,True,["OWNER","DRIVER","STATION"]),
 (False,"LINKED",True,0,True,["OWNER"]),
 (False,"LINKED",False,1,True,["OWNER"]),
 (False,"LINKED",True,1,False,["OWNER"]),
 (False,"REVOKED",True,1,True,["OWNER"])])
def test_roles_no_station_implies_driver(driver,link,active,count,function,expected):
 assert P.roles(driver,link,active,count,function)==expected
def test_install_preserves_evidence_but_reopens_changed_scope():
 old=[{"id":"I05","status":"REVIEWED","evidence":[{"path":"old.json"}],"scopeRevision":"V6.1"}]
 defs=[{"id":"I05","status":"NOT_ASSESSED","scopeRevision":"V6.4-IDENTITY-20261022"}]
 out=E.merge_tasks(old,defs)
 assert out[0]["status"]=="PLANNED" and out[0]["needsAdaptation"]
 assert out[0]["evidence"]==old[0]["evidence"]
 assert E.merge_tasks(out,defs)==out
def test_install_keeps_unchanged_task_completion():
 old=[{"id":"R02","status":"REVIEWED","evidence":[{"path":"done.json"}]}]
 defs=[{"id":"R02","status":"NOT_ASSESSED","scopeRevision":"V6.1-UNCHANGED"}]
 assert E.merge_tasks(old,defs)[0]["status"]=="REVIEWED"
def test_reference_record_is_identical():
 assert (B/"10_技术架构_T3.4/参考代码/ViRecord.java").read_bytes()==(B/"14_工程模板/backend/src/main/java/com/xinyi/rongli/core/ViRecord.java").read_bytes()
