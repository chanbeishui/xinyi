import pytest
from test_identity_address import address
pytestmark = pytest.mark.booking

@pytest.mark.parametrize('saved_vehicle,saved_address',[(False,False),(True,False),(False,True),(True,True)])
def test_quote_four_valid_sources(login,run,sim,db,saved_vehicle,saved_address):
    c,s=login();phone=sim('/control/codes',{'runId':run['runId'],'persona':'owner-a','kind':'phone'})['code'];c.call('POST','/api/common/phone/bind',{'phoneCode':phone})
    vehicle={'plateNo':run['vehiclePlate'],'vehicleTypeCode':run['vehicleTypeCode']};ad=address();req={'slotId':run['slotId'],'inspectionTypeCode':run['inspectionTypeCode']}
    if saved_vehicle:req['vehicleId']=c.call('POST','/api/app/owner/vehicles',vehicle)['data']['vehicleId']
    else:req['vehicleDraft']=vehicle
    if saved_address:req['addressId']=c.call('POST','/api/app/owner/addresses',ad)['data']['addressId']
    else:req['addressDraft']=ad
    with db.cursor() as cur:
        cur.execute('SELECT COUNT(*) AS n FROM vi_address WHERE owner_account_id=%s',(int(s['accountId']),));before=cur.fetchone()['n']
    q=c.call('POST','/api/app/owner/orders/quote',req)['data']
    assert q['slotId']==run['slotId'] and q['totalAmount']==run['expectedQuoteFen'] and q['termsVersion']
    with db.cursor() as cur:
        cur.execute('SELECT COUNT(*) AS n FROM vi_address WHERE owner_account_id=%s',(int(s['accountId']),));assert cur.fetchone()['n']==before,'临时报价不能私自写入地址簿'

@pytest.mark.parametrize('bad',[
 {},{'vehicleId':'1','vehicleDraft':{'plateNo':'粤A12345','vehicleTypeCode':'TEST_CAR'}},
 {'vehicleId':None},{'vehicleDraft':None},
 {'vehicleId':'1','addressId':'2','addressDraft':{'address':'a','location':{'lng':1,'lat':1,'coordinateSystem':'GCJ02'}}}
])
def test_quote_source_constraints(login,run,sim,bad):
    c,_=login();phone=sim('/control/codes',{'runId':run['runId'],'persona':'owner-a','kind':'phone'})['code'];c.call('POST','/api/common/phone/bind',{'phoneCode':phone});c.call('POST','/api/app/owner/orders/quote',{'slotId':run['slotId'],'inspectionTypeCode':run['inspectionTypeCode'],**bad},expect=400)
