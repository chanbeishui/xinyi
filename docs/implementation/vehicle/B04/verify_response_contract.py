from pathlib import Path
import yaml,json,copy
from jsonschema import Draft4Validator,FormatChecker
s=yaml.safe_load(Path('docs/vehicle/V6.4/12_工程执行/task_packets/B04/openapi.yaml').read_text())
def adapt(v):
 if isinstance(v,list):return [adapt(i) for i in v]
 if not isinstance(v,dict):return v
 v={k:adapt(x) for k,x in v.items()}
 if v.pop('nullable',False):
  return {'anyOf':[v,{'type':'null'}]}
 return v
s=adapt(s);checks=[]
for p in Path('.vi-local').glob('b04-*.json'):
 name=p.stem.removeprefix('b04-').removesuffix('-history').removesuffix('-live')
 if name not in s['components']['schemas']:continue
 schema={'$ref':'#/components/schemas/'+name,'components':s['components']};data=json.loads(p.read_text());errors=list(Draft4Validator(schema,format_checker=FormatChecker()).iter_errors(data));checks.append({'schema':name,'sample':p.name,'passed':not errors});
 if errors:
  print(name,[(list(e.path),e.message) for e in errors]);raise SystemExit(1)
assert len(checks)==11,len(checks)
for path,methods in s['paths'].items():
 payload=Path('.vi-local/b04-envelope-'+path.replace('/','_')+'.json')
 schema=copy.deepcopy(methods['get']['responses']['200']['content']['application/json']['schema']);schema['components']=s['components'];Draft4Validator(schema,format_checker=FormatChecker()).validate(json.loads(payload.read_text()));checks.append({'endpoint':path,'envelope':True,'passed':True})
assert len(checks)==20
Path('docs/implementation/vehicle/B04/response-contract.json').write_text(json.dumps({'passed':True,'checks':checks},ensure_ascii=False,indent=2));print('20 actual HTTP projection/envelope checks conform to B04 schemas')
