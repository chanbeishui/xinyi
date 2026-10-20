#!/usr/bin/env python3
from pathlib import Path
import argparse,json,sys

def validate(root,preflight=False):
 state=root/'docs/implementation/vehicle';errors=[]
 def exists(p):
  if not isinstance(p,str) or not p:return False
  x=(root/p).resolve();return x.is_relative_to(root.resolve()) and x.is_file()
 tasks=json.loads((state/'tasks.json').read_text())
 ci=state/'ci-stage.json'
 if not ci.exists() or json.loads(ci.read_text()).get('stage')!='FULL':errors.append('CI尚未进入FULL全量验收阶段')
 for t in tasks:
  if t['scope']=='DEVELOPMENT' and t['status']!='REVIEWED' and not (preflight and t['id']=='G01'):errors.append(t['id']+' 未完成验证复核')
 for file,key,fields in [('api_implementation_map.json','operationId',['controllerFile','serviceFile']),('page_implementation_map.json','boardId',['implementationFile']),('table_implementation_map.json','table',['schemaEvidence'])]:
  for r in json.loads((state/file).read_text()):
   ident=r[key]
   for k in fields:
    if not exists(r.get(k)):errors.append(str(ident)+' 缺真实 '+k)
   if not r.get('testFiles') or not all(exists(p) for p in r['testFiles']):errors.append(str(ident)+' 缺测试实现文件')
   if not exists(r.get('reviewEvidence')):errors.append(str(ident)+' 缺独立复核报告')
 for r in json.loads((state/'case_implementation_map.json').read_text()):
  if r.get('lastResult')!='PASS' or not exists(r.get('testFile')) or not exists(r.get('resultEvidence')):errors.append(r['caseId']+' 未真实实现/运行')
 return errors
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--repo',default='.');a.add_argument('--preflight',action='store_true',help='G01审计前检查全部其他任务；不标代码完成');p=a.parse_args()
 try:
  errors=validate(Path(p.repo).resolve(),p.preflight);print(json.dumps({'codeComplete':not errors and not p.preflight,'readyForFinalAudit':not errors if p.preflight else None,'remainingCount':len(errors),'findings':errors[:100],'note':'格式门禁不能证明测试真实性，仍须独立复核；本工具不批准生产'},ensure_ascii=False,indent=2));sys.exit(bool(errors))
 except Exception as e:print('BLOCKED:',type(e).__name__,str(e));sys.exit(2)
