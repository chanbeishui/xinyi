import uuid
from conftest import Client
G={'lng':110.943,'lat':22.354,'coordinateSystem':'GCJ02'}
def address(label=None):return {'address':'集成测试位置-'+uuid.uuid4().hex[:8],'location':G,'label':label or '测试','isDefault':False}
def rows(response):
    data=response.get('data')
    if isinstance(data,list):return data
    if isinstance(data,dict) and isinstance(data.get('rows'),list):return data['rows']
    if isinstance(response.get('rows'),list):return response['rows']
    raise AssertionError('列表返回结构与预期契约不一致')

def test_login_creates_persisted_identity(login,db):
    _,s=login()
    with db.cursor() as cur:
        cur.execute('SELECT id,status FROM vi_account WHERE id=%s',(int(s['accountId']),));r=cur.fetchone()
        assert r and r['status']=='ACTIVE'
        cur.execute('SELECT COUNT(*) AS n FROM vi_account_security WHERE account_id=%s',(int(s['accountId']),));assert cur.fetchone()['n']==1

def test_address_create_cipher_and_owner(login,db):
    c,s=login();body=address();v=c.call('POST','/api/app/owner/addresses',body)['data'];assert v['address']==body['address'] and isinstance(v['addressId'],str)
    with db.cursor() as cur:
        cur.execute('SELECT owner_account_id,address_cipher,version FROM vi_address WHERE id=%s',(int(v['addressId']),));r=cur.fetchone()
        assert r['owner_account_id']==int(s['accountId']) and r['address_cipher'] and body['address'].encode() not in bytes(r['address_cipher'])
        assert r['version']==v['version']

def test_address_survives_logout_login(login):
    c,_=login();x=c.call('POST','/api/app/owner/addresses',address())['data'];c.call('POST','/api/common/session/logout',{'allDevices':False})
    c.call('GET','/api/common/profile',expect=401)
    other,_=login();assert x['addressId'] in {r['addressId'] for r in rows(other.call('GET','/api/app/owner/addresses'))}

def test_address_cross_owner_list_and_update(login,db):
    a,_=login('owner-a');b,_=login('owner-b');x=a.call('POST','/api/app/owner/addresses',address())['data'];before=x['version']
    assert x['addressId'] not in {r['addressId'] for r in rows(b.call('GET','/api/app/owner/addresses'))}
    b.call('PUT','/api/app/owner/addresses/'+x['addressId'],{**address(),'expectedVersion':before},expect=404)
    with db.cursor() as cur:
        cur.execute('SELECT version FROM vi_address WHERE id=%s',(int(x['addressId']),));assert cur.fetchone()['version']==before

def test_address_update_compare_and_swap(login,db):
    a,_=login();x=a.call('POST','/api/app/owner/addresses',address())['data'];body={**address(),'expectedVersion':x['version']};u=a.call('PUT','/api/app/owner/addresses/'+x['addressId'],body)['data']
    assert u['version']>x['version'];a.call('PUT','/api/app/owner/addresses/'+x['addressId'],body,expect=409)

def test_address_missing_version_rejected(login):
    c,_=login();x=c.call('POST','/api/app/owner/addresses',address())['data'];c.call('PUT','/api/app/owner/addresses/'+x['addressId'],address(),expect=400)

def test_idempotent_address_create(login,db):
    c,s=login();body=address();key=str(uuid.uuid4());a=c.call('POST','/api/app/owner/addresses',body,idem=key)['data'];b=c.call('POST','/api/app/owner/addresses',body,idem=key)['data'];assert a['addressId']==b['addressId']
    c.call('POST','/api/app/owner/addresses',{**body,'label':'改了'},idem=key,expect=409)
    with db.cursor() as cur:
        cur.execute('SELECT COUNT(*) AS n FROM vi_address WHERE owner_account_id=%s AND deleted_at IS NULL',(int(s['accountId']),));assert cur.fetchone()['n']==1

def test_mini_token_cannot_read_admin(login):
    c,_=login();c.call('GET','/api/admin/vehicles',expect=401)

def test_role_parameter_does_not_grant_station(login):
    c,s=login();c.call('POST','/api/common/session/role',{'role':'STATION','expectedContextVersion':s['contextVersion']},expect=403)

def test_vehicle_create_persistence(login,run,db):
    c,s=login();p=run['vehiclePlate'];v=c.call('POST','/api/app/owner/vehicles',{'plateNo':p,'vehicleTypeCode':run['vehicleTypeCode']})['data'];assert isinstance(v['vehicleId'],str)
    with db.cursor() as cur:
        cur.execute('SELECT owner_account_id,plate_no FROM vi_vehicle WHERE id=%s',(int(v['vehicleId']),));r=cur.fetchone();assert r['owner_account_id']==int(s['accountId']) and r['plate_no']==p
