"""Real backend configuration HTTP/DB tests; NOT_RUN until xinyi tasks X01..X07 are implemented.
No supplier network call is required; these tests do not mark real vendor verification complete.
"""
import json,uuid,pytest
from conftest import Client
pytestmark=pytest.mark.integration_config

def new_profile(base,run,kind='TENCENT_SMS'):
    c=Client(base,run['adminToken'],'admin')
    p=c.call('POST','/api/admin/integrations/profiles',{'kind':kind,'name':'IT-'+uuid.uuid4().hex[:10],'scope':{'ownerDeptId':run['ownerDeptId'],'environment':'TEST'}},expect=201)['data']
    assert p['activeRevisionId'] is None
    return c,p

def save(c,p,config=None,credentials=None):
    body={'kind':p['kind'],'expectedVersion':p['version'],'config':config or {'apiVersion':'2021-01-11','enabled':True},'reason':'隔离测试配置，不连接真实短信'}
    if credentials is not None:body['credentials']=credentials
    return c.call('PUT','/api/admin/integrations/profiles/'+p['profileId']+'/draft',body)['data']

def test_create_draft_does_not_publish(base,run):
    c,p=new_profile(base,run);p=save(c,p)
    current=c.call('GET','/api/admin/integrations/profiles/'+p['profileId'])['data']
    assert current['draftRevisionId'] is not None and current['activeRevisionId'] is None

def test_incomplete_enabled_channel_fails_validation(base,run):
    c,p=new_profile(base,run);p=save(c,p)
    r=c.call('POST',f"/api/admin/integrations/profiles/{p['profileId']}/checks",{'revisionId':p['draftRevisionId'],'expectedVersion':p['version'],'level':'LOCAL'})['data']
    assert r['status']=='FAIL' and len(r['issues'])>=1 and r['realPaymentVerified'] is False

def test_invalid_check_cannot_publish(base,run):
    c,p=new_profile(base,run);p=save(c,p)
    r=c.call('POST',f"/api/admin/integrations/profiles/{p['profileId']}/checks",{'revisionId':p['draftRevisionId'],'expectedVersion':p['version'],'level':'LOCAL'})['data']
    c.call('POST',f"/api/admin/integrations/profiles/{p['profileId']}/publish",{'revisionId':p['draftRevisionId'],'expectedVersion':p['version'],'contentHash':p['draft']['contentHash'],'checkId':r['checkId'],'reason':'不应发布无效配置'},expect=422)
    assert c.call('GET','/api/admin/integrations/profiles/'+p['profileId'])['data']['activeRevisionId'] is None

def test_stale_update_rejected(base,run):
    c,p=new_profile(base,run);saved=save(c,p)
    c.call('PUT','/api/admin/integrations/profiles/'+p['profileId']+'/draft',{'kind':'TENCENT_SMS','expectedVersion':p['version'],'config':{'apiVersion':'2021-01-11','enabled':False},'reason':'并发旧版不得覆盖'},expect=409)
    assert c.call('GET','/api/admin/integrations/profiles/'+p['profileId'])['data']['version']==saved['version']

def test_readonly_admin_cannot_change(base,run):
    _,p=new_profile(base,run);c=Client(base,run['readOnlyToken'],'admin')
    c.call('PUT','/api/admin/integrations/profiles/'+p['profileId']+'/draft',{'kind':'TENCENT_SMS','expectedVersion':p['version'],'config':{'apiVersion':'2021-01-11','enabled':False},'reason':'只读无写权限'},expect=403)

def test_mini_token_cannot_read_cloud_settings(base,run,login):
    _,p=new_profile(base,run);mini,_=login()
    mini.call('GET','/api/admin/integrations/profiles/'+p['profileId'],expect=401)

def test_secret_is_not_returned_and_is_encrypted_at_rest(base,run,db):
    c,p=new_profile(base,run);secret='TEST-ONLY-'+uuid.uuid4().hex
    saved=save(c,p,credentials={'secretKey':{'action':'REPLACE','value':secret}})
    assert secret not in json.dumps(saved)
    current=c.call('GET','/api/admin/integrations/profiles/'+p['profileId'])
    assert secret not in json.dumps(current)
    with db.cursor() as cur:
        cur.execute('SELECT ciphertext,nonce,key_version FROM vi_integration_secret WHERE profile_id=%s',(int(p['profileId']),));rows=cur.fetchall()
    assert rows and all(secret.encode() not in bytes(r['ciphertext']) for r in rows)
    assert all(r['nonce'] and r['key_version'] for r in rows)

def test_unchanged_secret_keep_does_not_clear(base,run):
    c,p=new_profile(base,run);p=save(c,p,credentials={'secretKey':{'action':'REPLACE','value':'TEST-ONLY-'+uuid.uuid4().hex}})
    p=save(c,p,credentials={'secretKey':{'action':'KEEP'}})
    assert p['draft']['settings']['credentialStates']['secretKey']['configured'] is True
