"""Executable document/reference-policy tests; NOT a production API/backend test."""
from pathlib import Path
import json,copy,hashlib,sys
import yaml,jsonschema
R=Path(__file__).resolve().parents[1]
s=yaml.safe_load((R/'04_接口/openapi_V6.4.yaml').read_text(encoding='utf-8'));S=s['components']['schemas'];results=[]
def conv(x):
 if isinstance(x,list):return [conv(v)for v in x]
 if not isinstance(x,dict):return x
 d={k:conv(v)for k,v in x.items()if k not in ['nullable','example','xml','discriminator','externalDocs','readOnly','writeOnly']}
 return {'anyOf':[d,{'type':'null'}]}if x.get('nullable')else d
root=conv(s)
def validate(n,x):
 schema={'$ref':'#/components/schemas/'+n,'components':root['components']}
 jsonschema.Draft7Validator(schema,format_checker=jsonschema.FormatChecker()).validate(x)
def rejected(n,x):
 try:validate(n,x)
 except jsonschema.ValidationError:return
 raise AssertionError('Unexpectedly accepted invalid '+n)
def run(name,fn):
 try:fn();results.append({'name':name,'result':'PASS'})
 except Exception as e:results.append({'name':name,'result':'FAIL','reason':str(e)[:900]})
def expect(a,msg='condition failed'):
 if not a:raise AssertionError(msg)
for n,d in S.items():
 if 'example'in d:run('Schema example valid '+n,lambda n=n,d=d:validate(n,d['example']))
for v in ['Owner','Driver','Station','Admin']:
 n=v+'OrderView';x=copy.deepcopy(S[n]['example'])
 for key,val in [('pickupCode','1234'),('idCardNo','SECRET'),('internalReview','SECRET')]:run(n+' rejects '+key,lambda n=n,x=x,key=key,val=val:rejected(n,{**x,key:val}))
 for parent,key in [('vehicle','ownerAccountId'),('station','privateCredential')]:
  xx=copy.deepcopy(x);xx[parent][key]='INTERNAL';run(n+' nested rejects '+key,lambda n=n,xx=xx:rejected(n,xx))
for v in ['Driver','Station']:
 n=v+'OrderView';run(n+' rejects customer money',lambda n=n:rejected(n,{**S[n]['example'],'money':{'totalAmount':34000}}))
# Runtime authorization must still implement x-conditions. These tests check the reference policy.
def projection_policy(n,x,permissions=()):
 validate(n,x)
 if n=='DriverOrderView'and x['accessPhase']=='HISTORY':
  expect(all(x[k] is None for k in ['pickupAddress','returnAddress','ownerContact']),'history contains sensitive contact/address')
  expect(set(x['allowedActions'])<={'VIEW_DETAIL','VIEW_RESULT','VIEW_EVIDENCE'},'history has active action')
 if n=='AdminOrderView':
  if 'biz:order:finance'not in permissions:expect(x['money']is None,'unprivileged money')
  if 'biz:order:address'not in permissions:expect(x['pickupAddress']is None and x['returnAddress']is None,'unprivileged address')
def historical():
 x=copy.deepcopy(S['DriverOrderView']['example']);x.update(accessPhase='HISTORY',pickupAddress=None,returnAddress=None,ownerContact=None,allowedActions=['VIEW_DETAIL'],assignmentEndedAt='2026-10-20T09:00:00.000Z',driverReleased=True)
 projection_policy('DriverOrderView',x)
 x['ownerContact']={'displayName':'hidden','phoneMask':'138****0000','canRequestContact':False}
 try:projection_policy('DriverOrderView',x)
 except AssertionError:return
 raise AssertionError('reference policy failed to block old contact')
run('HISTORY reference policy protects all parent fields',historical)
def response_names(o):
 seen=set()
 def rec(x):
  if isinstance(x,dict):
   if '$ref'in x and x['$ref'].startswith('#/components/schemas/'):
    n=x['$ref'].rsplit('/',1)[-1]
    if n not in seen:seen.add(n);rec(S[n])
   for k,v in x.items():
    if k!='$ref':rec(v)
  elif isinstance(x,list):
   for v in x:rec(v)
 rec(o);return seen
run('MINI confirm returns only public statement graph',lambda:expect('DriverStatementPublic'in response_names(s['paths']['/api/app/driver/statements/{statementId}/confirm']['post']['responses']['200'])and'DriverStatement'not in response_names(s['paths']['/api/app/driver/statements/{statementId}/confirm']['post']['responses']['200'])))
line={'lineId':'1','entryId':'2','assignmentId':'3','orderNo':'VI001','plateNo':'粤K12345','serviceEndedAt':'2026-10-20T08:00:00.000Z','amount':6500,'pricingExplanation':'约定金额'}
public={'statementId':'100','statementNo':'DS001','driverId':'10','payorEntityId':'1','currency':'CNY','serviceRangeStart':'2026-10-19T16:00:00.000Z','serviceRangeEndExclusive':'2026-10-20T16:00:00.000Z','status':'SENT','revision':1,'snapshotHash':'a'*64,'amount':6500,'driverConfirmedAt':None,'version':1,'paymentSummary':{'paymentStatus':'UNPAID','verifiedPaidAmount':0,'remainingPayableAmount':6500},'entries':[line]}
run('Public statement full send sample valid',lambda:validate('DriverStatementPublic',public))
for key in ['knownAmount','manualPriceCount','reviewStatus']:
 run('Public statement rejects '+key,lambda key=key:rejected('DriverStatementPublic',{**public,key:0}))
run('Public line rejects internal reasons',lambda:rejected('DriverStatementLinePublic',{**line,'internalReason':'secret'}))
for status in ['DRAFT','INTERNAL_REVIEW','VOID']:run('Public statement rejects '+status,lambda status=status:rejected('DriverStatementPublic',{**public,'status':status}))
run('Address update requires expectedVersion',lambda:rejected('AddressUpdate',{'label':'家','address':'示例','location':{'lng':110,'lat':22,'coordinateSystem':'GCJ02'},'isDefault':False}))
run('Profile patch requires expectedVersion',lambda:rejected('ProfilePatch',{'nickname':'demo'}))
run('Profile patch rejects permission field',lambda:rejected('ProfilePatch',{'nickname':'demo','expectedVersion':0,'permissions':['*:*:*']}))
run('Long ID remains exact JSON string',lambda:expect(json.loads(json.dumps({'id':'9223372036854775807'}))['id']=='9223372036854775807'))
for v in ['-1','0','01','1.1','abc']:
 run('ID lexical reject '+v,lambda v=v:rejected('Address',{'addressId':v,'label':'x','address':'y','location':{'lng':110,'lat':22,'coordinateSystem':'GCJ02'},'isDefault':False,'version':0}))
run('UTC suffix rejects ambiguous time',lambda:rejected('DriverStatementLinePublic',{**line,'serviceEndedAt':'2026-10-20 16:00:00'}))
run('Money integer rejects fractional fen',lambda:rejected('DriverStatementLinePublic',{**line,'amount':65.001}))
def testhash():
 f=json.loads((R/'04_接口/公开快照哈希夹具.json').read_text());b=json.dumps(f['payload'],ensure_ascii=False,separators=(',',':')).encode('utf-8');expect(b.decode()==f['canonicalJson']);expect(hashlib.sha256(b).hexdigest()==f['sha256'])
 changed=copy.deepcopy(f['payload']);changed[-1][0][-2]='6600';expect(hashlib.sha256(json.dumps(changed,ensure_ascii=False,separators=(',',':')).encode()).hexdigest()!=f['sha256'])
run('Public hash fixture stable and amount change detected',testhash)
run('Training questions do not contain standard answer fields',lambda:expect('correctOptionCodes'not in json.dumps({k:S[k]for k in response_names({'$ref':'#/components/schemas/TrainingAttempt'})})))
run('Appeal record rejects NONE',lambda:rejected('AppealRecordStatus','NONE'))
run('Schema definitions accepted by Draft7 after explicit OAS nullable conversion',lambda:[jsonschema.Draft7Validator.check_schema(d)for d in root['components']['schemas'].values()])
report={'kind':'DOCUMENT_AND_REFERENCE_POLICY','scope':'Schema/fixture/reference-policy validation only; no real endpoint, service, database, vendor, or xinyi execution','passed':sum(x['result']=='PASS'for x in results),'failed':sum(x['result']=='FAIL'for x in results),'tests':results}
(R/'09_验证记录/契约样本与负向字段测试.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in report.items()if k!='tests'},ensure_ascii=False));
for t in results:
 if t['result']=='FAIL':print(t['name'],t['reason'])
sys.exit(bool(report['failed']))
