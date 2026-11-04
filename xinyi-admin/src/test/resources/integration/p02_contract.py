from pathlib import Path
import sys,json,yaml
from jsonschema import Draft4Validator,FormatChecker
root,out=map(Path,sys.argv[1:])
def adapt(v):
 if isinstance(v,list):return [adapt(x) for x in v]
 if not isinstance(v,dict):return v
 v={k:adapt(x) for k,x in v.items()}
 return {'anyOf':[v,{'type':'null'}]} if v.pop('nullable',False) else v
spec=adapt(yaml.safe_load((root/'docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml').read_text()))
for name,method,path in [('payment','get','payment'),('close','post','close-unpaid')]:
 schema=spec['paths']['/api/app/owner/orders/{orderNo}/'+path][method]['responses']['200']['content']['application/json']['schema']
 schema['components']=spec['components']
 Draft4Validator(schema,format_checker=FormatChecker()).validate(json.loads((out/(name+'.json')).read_text()))
print('2 actual HTTP envelopes conform to primary OpenAPI')
