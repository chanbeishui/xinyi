"""Deep static consistency checks for the V6.0 design package; no production calls."""
from pathlib import Path
import json,re,csv,sys,subprocess,hashlib
from collections import Counter
from urllib.parse import unquote,urlparse
import yaml
R=Path(__file__).resolve().parents[1];SPE=R/'04_接口/openapi_V6.0.yaml';s=yaml.safe_load(SPE.read_text(encoding='utf-8'));S=s['components']['schemas'];checks=[]
def run(name,fn):
 try:detail=fn();checks.append({'name':name,'result':'PASS','detail':detail})
 except Exception as e:checks.append({'name':name,'result':'FAIL','reason':str(e)[:1600]})
def require(x,msg):
 if not x:raise AssertionError(msg)
def ops():
 for p,item in s['paths'].items():
  for m,o in item.items():
   if m in ['get','post','put','patch','delete','options','head']:yield p,m,o
O=list(ops())
def walk(x,path=''):
 yield path,x
 if isinstance(x,dict):
  for k,v in x.items():yield from walk(v,path+'/'+k)
 elif isinstance(x,list):
  for i,v in enumerate(x):yield from walk(v,path+'/'+str(i))
def resolve(r):
 v=s
 for k in r[2:].split('/'):v=v[k.replace('~1','/').replace('~0','~')]
 return v
run('01 所有内部ref可解析',lambda:[resolve(x['$ref'])is not None for _,x in walk(s)if isinstance(x,dict)and'$ref'in x and x['$ref'].startswith('#/')][-1:])
run('02 operationId唯一',lambda:require(len({o['operationId']for p,m,o in O})==len(O),'duplicate operationId'))
def params():
 for p,m,o in O:
  keys={x['name']for x in o.get('parameters',[])if x.get('in')=='path'and x.get('required')}
  require(keys==set(re.findall(r'{([^}]+)}',p)),m+' '+p+' path mismatch')
run('03 路径参数声明完整',params)
def required():
 def check(x, path='', inherited=None):
  if isinstance(x,dict):
   props=set(x.get('properties',{})) | set(inherited or ())
   if isinstance(x.get('required'),list):require(set(x['required'])<=props,path+' undefined required')
   for k,v in x.items():
    if k in ['allOf','oneOf','anyOf']:
     for i,item in enumerate(v):check(item,path+'/'+k+'/'+str(i),props)
    elif k=='not':check(v,path+'/not',props)
    elif k=='properties':
     for name,val in v.items():check(val,path+'/'+name,None)
    elif isinstance(v,(dict,list)):check(v,path+'/'+k,None)
  elif isinstance(x,list):
   for i,v in enumerate(x):check(v,path+'/'+str(i),inherited)
 for name,schema in S.items():check(schema,name)
run('04 Schema必填字段存在',required)
run('05 不再存在泛型Order',lambda:require('Order'not in S and '#/components/schemas/Order"'not in json.dumps(s),'generic Order retained'))
def privacy():
 def names(x,seen):
  if isinstance(x,dict):
   if '$ref'in x and x['$ref'].startswith('#/components/schemas/'):
    n=x['$ref'].rsplit('/',1)[-1]
    if n not in seen:seen.add(n);names(S[n],seen)
   for k,v in x.items():
    if k!='$ref':names(v,seen)
  elif isinstance(x,list):
   for v in x:names(v,seen)
 for p,m,o in O:
  if p.startswith('/api/app/'):
   used=set();names(o['responses'],used)
   require(not used&{'Order','DriverStatement','DriverStatementLine','DriverStatementReview','TrainingCourseAdmin','AdminOrderView','AdminPenaltyView'},p+' '+str(used))
run('06 MINI响应递归无内部账单/管理员投影',privacy)
run('07 MINI确认使用Public',lambda:require('DriverStatementPublic'in json.dumps(s['paths']['/api/app/driver/statements/{statementId}/confirm']['post']['responses']['200']),'wrong confirm DTO'))
run('08 旧双域logout已删除',lambda:require('/api/common/logout'not in s['paths']and'/api/common/session/logout'in s['paths'],'duplicate logout'))
run('09 根上下文明确/api不重写',lambda:require(s['x-baseline']['servletContextPath']=='/'and s['x-baseline']['gatewayRewrite']=='NONE'and all(not a['url'].endswith('/api')for a in s['servers']),'routing ambiguity'))
def errors():
 for p,m,o in O:
  if p.startswith('/api/notify/'):continue
  require('429'in o['responses'],p+' no rate limit response')
  for code,r in o['responses'].items():
   if code[0]not in '45':continue
   if '$ref'in r:r=resolve(r['$ref'])
   require('content'in r and 'ApiError'in json.dumps(r['content']),p+' '+code+' missing error body')
run('10 业务所有错误体及429统一引用',errors)
registry=json.loads((R/'04_接口/权限与能力注册表.json').read_text())
# Registries may be mapping or records, kept machine readable.
def keys(v):return set(v)if isinstance(v,dict)else{a if isinstance(a,str)else a.get('code',a.get('name'))for a in v}
perms=keys(registry['permissions']);caps=keys(registry['capabilities'])
def auth():
 for p,m,o in O:
  require('x-authorization'in o,p+' no auth policy')
  if o.get('x-permission'):
   require(o['x-permission']in perms,p+' unregistered permission');require(re.fullmatch(r'biz:[a-z0-9]+:[a-z0-9]+',o['x-permission']),p+' free text permission')
   if m=='get':require(o['x-permission'].split(':')[-1]not in ['edit','add','publish','approve','review','audit'],p+' write permission on read')
  if o.get('x-capability'):require(o['x-capability']in caps,p+' missing capability')
run('11 权限注册与查询写权限分离',auth)
run('12 无biz:orders复数混用',lambda:require('biz:orders:'not in json.dumps(s),'plural naming'))
run('13 能力/动作有稳定字典',lambda:require(all(S[n].get('enum')for n in ['CapabilityCode','AllowedAction','PermissionCode']),'empty enum'))
run('14 Profile地址更新必填版本',lambda:require(all('expectedVersion'in S[n]['required']for n in ['ProfilePatch','AddressUpdate','VehicleUpdate','StationUpdate']),'missing expectedVersion'))
run('15 创建DTO不要求旧版本',lambda:require(all('expectedVersion'not in S[n]['properties']for n in ['AddressCreate','VehicleCreate','StationCreate','PriceCreate','DriverFeeRuleCreate']),'creation requires old version'))
run('16 地址Profile读取有version',lambda:require(all('version'in S[n]['required']for n in ['Address','Profile']),'no read version'))
run('17 处罚申诉生产读取DTO',lambda:require(all(n in S for n in ['Penalty','PenaltyAppeal'])and'/api/app/driver/penalties'in s['paths']and'/api/app/driver/penalty-appeals/{appealId}'in s['paths'],'broken penalty lifecycle'))
run('18 培训及尝试不是JobTask',lambda:require('Training'in json.dumps(s['paths']['/api/app/driver/qualification/training']['get']['responses']['200'])and'/api/app/driver/qualification/training/{trainingId}/attempts'in s['paths'],'training producer absent'))
run('19 活体会话不再Ack',lambda:require('FaceSession'in json.dumps(s['paths']['/api/app/driver/qualification/face-session']['post']['responses']['200']),'face Ack'))
run('20 员工绑定签发和消费完整',lambda:require(all(p in s['paths']for p in ['/api/common/employee-binding-requests','/api/admin/employee-binding-credentials','/api/common/employee-binding']),'binding missing'))
model=json.loads((R/'05_数据设计/data_model_V6.0.json').read_text());sql=(R/'05_数据设计/001_vi业务表_初始化_V6.0.sql').read_text();writecols=json.loads((R/'05_数据设计/生成列与写入白名单.json').read_text());wm={x['table']:x for x in writecols}
blocks={m.group(1):m.group(2)for m in re.finditer(r'CREATE TABLE `([^`]+)` \((.*?)\) ENGINE=',sql,re.S)}
run('21 SQL与模型全表相同',lambda:require(set(blocks)=={t['name']for t in model},'table sets differ'))
def pk():
 for t in model:
  b=blocks[t['name']];m=re.findall(r'PRIMARY KEY \(([^)]+)\)',b)
  require(len(m)==1,t['name']+' missing/multiple PK');require(re.findall(r'`([^`]+)`',m[0])==t['primaryKey'],t['name']+' wrong PK')
run('22 每张表PRIMARY KEY对应结构源',pk)
run('23 四热点业务键正确',lambda:require({t['name']:t['primaryKey']for t in model if t['name']in ['vi_driver_occupancy','vi_driver_location','vi_dispatch_cursor','vi_handover_attempt_guard']}=={'vi_driver_occupancy':['driver_id'],'vi_driver_location':['driver_id'],'vi_dispatch_cursor':['scope_key'],'vi_handover_attempt_guard':['guard_key']},'natural keys incorrect'))
def ids():
 for t in model:
  cols={c[0]:c[1]for c in t['cols']}
  if 'id'in cols:require('AUTO_INCREMENT'in cols['id']and t['idStrategy']=='MYSQL_AUTO_INCREMENT',t['name']+' not auto')
  else:require('AUTO_INCREMENT'not in blocks[t['name']],t['name']+' natural key has auto')
run('24 普通id自增与自然键策略冻结',ids)
def gens():
 for t in model:
  gen={c[0]for c in t['cols']if'GENERATED ALWAYS'in c[1]};w=wm[t['name']]
  require(gen==set(w['generatedColumns']),t['name']+' generated mismatch');require(not gen&(set(w['insertColumns'])|set(w['updateColumns'])),t['name']+' generated writable')
  require(set(t['primaryKey']).isdisjoint(w['updateColumns']),t['name']+' primary key writable')
run('25 所有生成列排除Mapper写清单',gens)
en=json.loads((R/'05_数据设计/枚举字典_V6.0.json').read_text());tm={t['name']:t for t in model}
def enums():
 for key,n in en['columnBindings'].items():
  tn,col=key.split('.');t=tm[tn];vals=en['enums'][n]['values'];require(t['columnRules'][col]['enumRef']==n,key+' metadata ref')
  require(S[n]['enum']==vals,key+' API enum differs')
  checks=' '.join(t['checks']);require('`'+col+'` IN ('in checks,key+' no CHECK')
  require(all("'"+v+"'"in checks for v in vals),key+' enum value missing')
run('26 枚举源/Schema/字段/CHECK对应',enums)
run('27 UTC默认表达式无本地CURRENT_TIMESTAMP',lambda:require('DEFAULT CURRENT_TIMESTAMP'not in sql and 'ON UPDATE CURRENT_TIMESTAMP'not in sql and '(UTC_TIMESTAMP(3))'in sql,'ambiguous timestamp default'))
run('28 初始化无sys写/破坏命令',lambda:require(not re.search(r'(DROP\s+TABLE|TRUNCATE|DELETE\s+FROM|ALTER\s+TABLE\s+`?sys_|CREATE\s+TABLE\s+`?sys_)',sql,re.I),'unsafe initialization'))
run('29 不用IF NOT EXISTS掩盖结构差异',lambda:require('CREATE TABLE IF NOT EXISTS'not in sql,'silent skip'))
run('30 数据列与SQL列集合精确相同',lambda:[require(set(re.findall(r'^  `([^`]+)`',blocks[t['name']],re.M))=={c[0]for c in t['cols']},t['name']+' columns differ')for t in model])
cat=json.loads((R/'03_交互与设计规范/画板索引.json').read_text());C={i['id']for i in cat};cases=json.loads((R/'06_验收/test_cases_V6.0.json').read_text());feature=(R/'06_验收/全量业务验收_V6.0.feature').read_text()
run('31 画板ID唯一',lambda:require(len(C)==len(cat),'duplicate views'))
def scenario_sync():
 ids=re.findall(r'^  @(TC-[\w-]+)',feature,re.M);require(len(ids)==len(cases)and set(ids)=={c['id']for c in cases},'case ID mismatch')
 for c in cases:
  block='  @'+c['id']+' @'+c['priority']+'\n  场景: '+c['title']+'\n    假如 '+c['given'].replace('\n',' ')+'\n    当 '+c['when'].replace('\n',' ')+'\n    那么 '+c['then'].replace('\n',' ')
  require(block in feature,c['id']+' semantic mismatch')
run('32 全量Gherkin与JSON逐条内容相同',scenario_sync)
run('33 核价30场景一一具备TC-R41标签',lambda:require(len([c for c in cases if c['id'].startswith('TC-R41-')])==30 and len(re.findall(r'^  @TC-R41-',feature,re.M))==30,'pricing coverage missing'))
run('34 旧期间语义不再作为并发唯一',lambda:require('不同Idempotency-Key'in next(c['when']for c in cases if c['id']=='TC-173')and any(c['title']=='同期间不重叠允许两账'for c in cases),'period tests old'))
run('35 真实业务全部标NOT_RUN',lambda:require(all(c['status']=='NOT_RUN'for c in cases),'unearned runtime pass'))
flows=json.loads((R/'08_业务流程图/flows_V6.0.json').read_text())
def graphcheck():
 for f in flows:
  ns={n['id']for n in f['nodes']}
  require(all(a['from']in ns and a['to']in ns for a in f['edges']),f['id']+' dangling edge')
  require(not re.search(r'\b(O06|O07|O08|S09)\b',f['pages']),f['id']+' noncanonical alias')
  for ext in ['PNG','SVG','DOT','Mermaid']:
   suf={'PNG':'png','SVG':'svg','DOT':'dot','Mermaid':'mmd'}[ext];require((R/'08_业务流程图'/ext/(f['id']+'.'+suf)).exists(),f['id']+' missing '+ext)
run('36 流程节点/导出/规范页面别名',graphcheck)
run('37 无旧V4交易迁移执行脚本',lambda:require(not any(p.suffix=='.sql' and re.search(r'(legacy|V4.*迁移)',p.name,re.I) for p in (R/'05_数据设计').rglob('*') if p.is_file()),'obsolete transaction migration remains'))
run('38 新视觉规范标题与交互计数',lambda:require('V6.0'in (R/'03_交互与设计规范/01_视觉规范_AppleHIG映射.md').read_text().splitlines()[0]and 'INT75'in (R/'03_交互与设计规范/02_交互设计规范.md').read_text(),'visual/interaction version'))
def js():
 for p in (R/'02_UI/src').glob('*.js'):
  cp=subprocess.run(['node','--check',str(p)],capture_output=True,text=True);require(cp.returncode==0,p.name+' '+cp.stderr)
run('39 全部JS语法检查',js)
def linkcheck():
 bad=[]
 for p in R.rglob('*.html'):
  if p.name=='index.html' or any(part.startswith('历史_') for part in p.parts) or '14_工程模板' in p.parts:continue
  for u in re.findall(r'(?:href|src)=["\']([^"\']+)["\']',p.read_text(encoding='utf-8')):
   if u.startswith(('http:','https:','data:','mailto:','javascript:','#')):continue
   path=unquote(u.split('#')[0].split('?')[0]);
   if path and not (p.parent/path).exists():bad.append(str(p.relative_to(R))+' -> '+path)
 require(not bad,'; '.join(bad[:30]))
run('40 活动HTML本地链接可解析',linkcheck)
def images():
 for i in cat:
  for k in ['首屏','完整内容']:require((R/'02_UI/全量页面图'/k/(i['id']+'.png')).exists(),i['id']+' missing screenshot')
run('41 每画板首屏与完整图存在',images)
# Compare matrix leaf paths exactly, not merely number of fields.
def leaves(x,p='',anc=()):
 if '$ref'in x:
  n=x['$ref'].rsplit('/',1)[-1];return set()if n in anc else leaves(S[n],p,anc+(n,))
 if 'allOf'in x:return set().union(*(leaves(v,p,anc)for v in x['allOf']))
 if x.get('type')=='array':return leaves(x['items'],p+'[]',anc)
 if x.get('type')=='object':return set().union(*(leaves(v,(p+'.'if p else'')+k,anc)for k,v in x.get('properties',{}).items())) if x.get('properties')else{p}
 return {p}
def matrix():
 a=json.loads((R/'04_接口/四视角字段可见矩阵.json').read_text())
 for v in ['Owner','Driver','Station','Admin']:require(leaves(S[v+'OrderView'])=={x['field']for x in a if x['schema']==v+'OrderView'},v+' field matrix incomplete')
run('42 四视角所有嵌套字段都有矩阵行',matrix)
rep={'kind':'STATIC_DOCUMENT_CHECK','checkedAt':'2026-10-20','pass':sum(x['result']=='PASS'for x in checks),'fail':sum(x['result']=='FAIL'for x in checks),'counts':{'operations':len(O),'schemas':len(S),'tables':len(model),'generatedColumns':sum(len(x['generatedColumns'])for x in writecols),'screens':len(cat),'cases':len(cases),'flows':len(flows)},'checks':checks,'notCovered':['MySQL execution','xinyi build','runtime authorization','real payment/vendors','WeChat devices','full formal OpenAPI meta-schema validation']}
(R/'09_验证记录/资料深度一致性检查.json').write_text(json.dumps(rep,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in rep.items()if k!='checks'},ensure_ascii=False))
for c in checks:
 if c['result']=='FAIL':print(c['name'],c['reason'])
sys.exit(bool(rep['fail']))
