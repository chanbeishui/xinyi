import hashlib,hmac,json,os,subprocess,sys,urllib.request,urllib.error,time
from pathlib import Path
import pytest
B=Path(__file__).resolve().parents[2]
@pytest.fixture(scope='module')
def server(tmp_path_factory):
 secret='a'*64;folder=tmp_path_factory.mktemp('sim')
 p=subprocess.Popen([sys.executable,str(B/'13_真实集成测试/provider_simulator/simulator.py'),'--port','0','--db',str(folder/'db.sqlite')],env={**os.environ,'VI_SIM_SECRET':secret},stdout=subprocess.PIPE,text=True)
 line=p.stdout.readline();port=json.loads(line)['port'];base=f'http://127.0.0.1:{port}'
 yield base,secret
 p.terminate();p.wait(timeout=10)
def call(server,path,body,secret=None):
 base,key=server;r=urllib.request.Request(base+path,data=json.dumps(body).encode(),headers={'Content-Type':'application/json','X-Sim-Secret':secret if secret is not None else key})
 try:
  with urllib.request.urlopen(r,timeout=5) as s:return s.status,json.load(s)
 except urllib.error.HTTPError as e:return e.code,json.load(e)
@pytest.fixture
def run(server):return call(server,'/control/runs',{})[1]['runId']
def test_health(server):assert json.load(urllib.request.urlopen(server[0]+'/health'))['mode']=='SIMULATOR'
def test_secret(server):assert call(server,'/control/runs',{},'wrong')[0]==403
def test_unknown_run(server):assert call(server,'/payments/query',{'runId':'missing','merchantOrderNo':'x'})[0]==422
def test_code_consumed(server,run):
 b={'runId':run,'persona':'OWNER_A'};code=call(server,'/control/codes',b)[1]['code'];x={**b,'code':code}
 assert call(server,'/wechat/exchange',x)[0]==200;assert call(server,'/wechat/exchange',x)[0]==422
def test_code_same_identity(server,run):
 ids=[]
 for _ in range(2):
  code=call(server,'/control/codes',{'runId':run,'persona':'OWNER_A'})[1]['code'];ids.append(call(server,'/wechat/exchange',{'runId':run,'code':code})[1]['openId'])
 assert ids[0]==ids[1]
def test_route_missing(server,run):assert call(server,'/routes/query',{'runId':run,'originKey':'BAD','destinationKey':'STATION_A'})[0]==422
def test_route_directed(server,run):
 a=call(server,'/routes/query',{'runId':run,'originKey':'PICKUP_A','destinationKey':'STATION_A'})[1];b=call(server,'/routes/query',{'runId':run,'originKey':'STATION_A','destinationKey':'PICKUP_A'})[1];assert a['distanceMeter']!=b['distanceMeter']
def pay(server,run,no='p1',amount=34000):return call(server,'/payments/prepay',{'runId':run,'merchantOrderNo':no,'amount':amount,'merchantAlias':'TEST_MERCHANT'})
def test_prepay_not_paid(server,run):assert pay(server,run)[1]['status']=='PENDING'
def test_prepay_conflict(server,run):pay(server,run);assert pay(server,run,amount=1)[0]==409
def test_close_paid_conflict(server,run):
 pay(server,run);call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'});assert call(server,'/payments/close',{'runId':run,'merchantOrderNo':'p1'})[0]==409
def test_closed_payment_cannot_pay(server,run):
 pay(server,run);call(server,'/payments/close',{'runId':run,'merchantOrderNo':'p1'});assert call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'})[0]==409
def test_signed_event(server,run):
 pay(server,run);d=call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'})[1]
 assert hmac.compare_digest(d['signature'],hmac.new(server[1].encode(),d['rawBody'].encode(),hashlib.sha256).hexdigest())
def test_refund_requires_paid(server,run):
 pay(server,run);assert call(server,'/refunds/create',{'runId':run,'refundNo':'r1','merchantOrderNo':'p1','amount':1})[0]==409
def test_refund_received_not_success(server,run):
 pay(server,run);call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'});assert call(server,'/refunds/create',{'runId':run,'refundNo':'r1','merchantOrderNo':'p1','amount':100})[1]['status']=='PROCESSING'
def test_refund_cap(server,run):
 pay(server,run,amount=100);call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'});assert call(server,'/refunds/create',{'runId':run,'refundNo':'r1','merchantOrderNo':'p1','amount':101})[0]==409
def test_refund_same_id(server,run):
 pay(server,run);call(server,'/control/payments/paid',{'runId':run,'merchantOrderNo':'p1'});b={'runId':run,'refundNo':'r1','merchantOrderNo':'p1','amount':100};assert call(server,'/refunds/create',b)[0]==200;assert call(server,'/refunds/create',b)[0]==200;assert call(server,'/refunds/create',{**b,'amount':101})[0]==409
def test_face_pending(server,run):assert call(server,'/verifications/start',{'runId':run,'kind':'FACE'})[1]['status']=='PENDING'
def test_float_money_rejected(server,run):assert pay(server,run,amount=1.1)[0]==422
def test_cross_run_payment(server,run):
 pay(server,run);r2=call(server,'/control/runs',{})[1]['runId'];assert call(server,'/payments/query',{'runId':r2,'merchantOrderNo':'p1'})[0]==422

def test_exchange_infers_run_without_public_body_field(server,run):
 code=call(server,'/control/codes',{'runId':run,'persona':'owner-a','kind':'wechat'})[1]['code']
 status,data=call(server,'/wechat/exchange',{'code':code})
 assert status==200 and data['providerRunId']==run
