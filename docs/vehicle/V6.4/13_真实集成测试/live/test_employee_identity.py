"""V6.4真实HTTP/MySQL身份链。无后端或fixture则失败，不skip不使用内存业务替代。"""
import uuid
import pytest
from conftest import verify_phone
pytestmark=pytest.mark.employee_identity

def test_unique_verified_phone_links_employee_without_application(login,run,sim,db):
    c,s=login('employee-unique')
    result=verify_phone(c,s,sim,run,'employee-unique')
    assert result['employeeLink']['status']=='LINKED'
    assert result['employeeLink']['workAccessEnabled'] is True
    with db.cursor() as cur:
        cur.execute("SELECT sys_user_id,source,status FROM vi_employee_binding WHERE account_id=%s AND revoked_at IS NULL",(int(s['accountId']),))
        rows=cur.fetchall()
        assert len(rows)==1 and str(rows[0]['sys_user_id'])==run['employeeUserId']
        assert rows[0]['source']=='WECHAT_PHONE_MATCH' and rows[0]['status']=='ACTIVE'
    public=c.call('GET','/api/common/employee-link')['data']
    assert 'sysUserId' not in public and 'openId' not in public
    assert public['authorizedStationCount']>=1

def test_employee_without_station_is_linked_not_driver_or_work_role(login,run,sim):
    c,s=login('employee-no-station')
    result=verify_phone(c,s,sim,run,'employee-no-station')
    assert result['employeeLink']['status']=='LINKED'
    assert result['employeeLink']['workAccessEnabled'] is False
    assert result['employeeLink']['authorizedStationCount']==0

def test_duplicate_phone_keeps_owner_without_employee_disclosure(login,run,sim,db):
    c,s=login('employee-duplicate')
    result=verify_phone(c,s,sim,run,'employee-duplicate')
    assert result['employeeLink']['status']=='NEEDS_ADMIN'
    assert set(result['employeeLink'])<=set(['status','workAccessEnabled','authorizedStationCount','messageKey','canReverifyPhone'])
    with db.cursor() as cur:
        cur.execute("SELECT COUNT(*) n FROM vi_employee_binding WHERE account_id=%s AND revoked_at IS NULL",(int(s['accountId']),))
        assert cur.fetchone()['n']==0

def test_disabled_employee_does_not_gain_station(login,run,sim):
    c,s=login('employee-disabled')
    result=verify_phone(c,s,sim,run,'employee-disabled')
    assert result['employeeLink']['workAccessEnabled'] is False

def test_raw_phone_cannot_replace_wechat_proof(login):
    c,s=login('owner-a')
    c.call('POST','/api/common/phone/bind',{'phone':'19900000000','openId':'fake','expectedAccountVersion':s['accountVersion'],'verificationRequestId':str(uuid.uuid4())},expect=400)

def test_no_matching_employee_is_normal_owner(login,run,sim):
    c,s=login('owner-a')
    result=verify_phone(c,s,sim,run,'owner-a')
    assert result['employeeLink']['status']=='UNLINKED' and result['employeeLink']['workAccessEnabled'] is False
