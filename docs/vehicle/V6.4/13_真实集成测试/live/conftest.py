"""Real HTTP+MySQL tests. Missing app/fixture control FAILS, never skips or uses prototype data."""
from __future__ import annotations
import hashlib,json,os,re,uuid
from pathlib import Path
from urllib.parse import urlparse
import pytest,requests,yaml,jsonschema
B=Path(__file__).resolve().parents[2]
SC=yaml.safe_load((B/'04_接口/openapi_V6.4.yaml').read_text(encoding='utf8'))

def convert(v):
    if isinstance(v,list):return [convert(x) for x in v]
    if not isinstance(v,dict):return v
    d={k:convert(x) for k,x in v.items() if k not in {'nullable','example','discriminator','xml'}}
    return {'anyOf':[d,{'type':'null'}]} if v.get('nullable') else d

class Client:
    def __init__(self,base,token=None,role='owner'):self.base=base;self.token=token;self.role=role
    def call(self,method,path,body=None,expect=200,idem=None):
        headers={'X-Client-Type':self.role,'X-App-Version':'INTEGRATION-V6','Idempotency-Key':idem or str(uuid.uuid4())}
        if self.token:headers['Authorization']='Bearer '+self.token
        r=requests.request(method,self.base+path,json=body,headers=headers,timeout=8,allow_redirects=False)
        assert r.status_code==expect,f'{method} {path} expected {expect}, got {r.status_code}; inspect redacted backend trace (tokens deliberately omitted)'
        data=r.json()
        template=next((p for p in SC['paths'] if re.fullmatch(re.sub(r'\{[^}]+\}',r'[^/]+',p),path.split('?')[0])),None)
        assert template is not None,'测试请求未进入主契约'
        resp=SC['paths'][template][method.lower()]['responses'].get(str(expect))
        if resp and '$ref' in resp:resp=SC['components']['responses'][resp['$ref'].split('/')[-1]]
        if resp and 'application/json' in resp.get('content',{}):
            root=convert(SC);schema=convert(resp['content']['application/json']['schema'])
            jsonschema.Draft7Validator(schema,resolver=jsonschema.RefResolver.from_schema(root),format_checker=jsonschema.FormatChecker()).validate(data)
        return data

def safe_url(name,default):
    v=os.getenv(name,default).rstrip('/');p=urlparse(v)
    assert p.scheme=='http' and p.hostname in {'127.0.0.1','localhost','::1'} and not p.username,'只允许本地隔离HTTP目标'
    return v

@pytest.fixture(scope='session')
def base():
    url=safe_url('VI_BASE_URL','http://127.0.0.1:18080');key=os.getenv('VI_TEST_CONTROL_KEY','')
    assert len(key)>=32,'缺少隔离环境控制密钥；先env/init并启动vi-it，不是跳过测试'
    try:r=requests.get(url+'/__integration/runtime',headers={'X-Test-Control-Key':key},timeout=3)
    except requests.RequestException as e:pytest.fail('真实后端测试入口不可达：先实现E07并运行vi-it；没有回退到模拟业务后端。'+type(e).__name__)
    assert r.status_code==200,'缺真实vi-it控制入口或认证失败（不允许临时permitAll）'
    v=r.json();assert v.get('environment')=='integration' and v.get('databaseGuard')=='XINYI_VEHICLE_ISOLATED' and v.get('providerMode')=='SIMULATOR' and v.get('contractVersion')==SC['info']['version']
    return url

@pytest.fixture(scope='session')
def sim():
    url=safe_url('VI_SIM_URL','http://127.0.0.1:19091');secret=os.environ.get('VI_SIM_SECRET','');assert len(secret)>=32
    def post(path,body):
        r=requests.post(url+path,json=body,headers={'X-Sim-Secret':secret},timeout=5);assert r.status_code==200,f'供应商测试替身错误 {path} {r.status_code}';return r.json()
    return post

@pytest.fixture()
def run(base,sim,request):
    rid=sim('/control/runs',{})['runId'];r=requests.post(base+'/__integration/fixtures',headers={'X-Test-Control-Key':os.environ['VI_TEST_CONTROL_KEY']},json={'fixture':'INTEGRATION_CONFIG' if request.node.get_closest_marker('integration_config') else 'EMPLOYEE_IDENTITY' if request.node.get_closest_marker('employee_identity') else 'BOOKING' if request.node.get_closest_marker('booking') else 'IDENTITY','providerRunId':rid},timeout=20)
    assert r.status_code==201,'夹具必须由真实后端测试服务创建；不得在测试client直改生产订单状态'
    data=r.json();assert data.get('providerRunId')==rid
    data['runId']=rid;return data

@pytest.fixture()
def login(base,sim,run):
    def go(persona='owner-a'):
        code=sim('/control/codes',{'runId':run['runId'],'persona':persona,'kind':'wechat'})['code']
        s=Client(base).call('POST','/api/common/login/wechat',{'code':code})['data']
        assert isinstance(s['accountId'],str) and s['activeRole']=='OWNER'
        assert s['availableRoles']==['OWNER'],'基础新账号不应默认取得司机或检测权限'
        return Client(base,s['token']),s
    return go

@pytest.fixture()
def db(base):
    import pymysql
    name=os.getenv('VI_DB_NAME','');host=os.getenv('VI_DB_HOST','127.0.0.1')
    assert re.fullmatch(r'xinyi_vi_(dev|it_[a-z0-9_]+)',name) and host in {'127.0.0.1','localhost','::1'}
    conn=pymysql.connect(host=host,port=int(os.getenv('VI_DB_PORT','33306')),user=os.environ['VI_DB_USER'],password=os.environ['VI_DB_PASSWORD'],database=name,autocommit=True,cursorclass=pymysql.cursors.DictCursor)
    with conn.cursor() as cur:
        cur.execute('SELECT kind,key_hash FROM test_environment_guard WHERE id=1');r=cur.fetchone()
        assert r and r['kind']=='XINYI_VEHICLE_ISOLATED' and r['key_hash']==hashlib.sha256(os.environ['VI_TEST_CONTROL_KEY'].encode()).hexdigest()
    yield conn;conn.close()

def verify_phone(client,session,sim,run,persona):
    import uuid
    code=sim('/control/codes',{'runId':run['runId'],'persona':persona,'kind':'phone'})['code']
    request_id=str(uuid.uuid4())
    result=client.call('POST','/api/common/phone/bind',{
        'phoneCode':code,'expectedAccountVersion':session['accountVersion'],
        'verificationRequestId':request_id})['data']
    assert result['verificationRequestId']==request_id
    assert result['verificationStatus']=='VERIFIED' and result['phoneBound'] is True
    if result['requiresSessionRefresh']:
        new=Client(client.base).call('POST','/api/common/token/refresh',{'refreshToken':session['refreshToken']})['data']
        assert new['accountId']==session['accountId']
        client.token=new['token'];session.clear();session.update(new)
    return result
