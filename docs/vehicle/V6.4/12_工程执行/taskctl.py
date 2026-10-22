#!/usr/bin/env python3
"""Persistent task ledger; bookkeeping only, never runs an agent or fabricates evidence."""
from __future__ import annotations
import argparse,datetime as dt,json,sys,hashlib
from pathlib import Path
B=Path(__file__).resolve().parents[1]
STATES={'NOT_ASSESSED','PLANNED','IN_PROGRESS','IMPLEMENTED','VERIFIED','REVIEWED','BLOCKED'}
READY={'IMPLEMENTED','VERIFIED','REVIEWED'}
def read(p):return json.loads(Path(p).read_text(encoding='utf8'))
def write(p,x):
 p=Path(p);p.parent.mkdir(parents=True,exist_ok=True);tmp=p.with_suffix('.tmp');tmp.write_text(json.dumps(x,ensure_ascii=False,indent=2),encoding='utf8');tmp.replace(p)
def ordered_next(tasks,include_release=False):
 by={x['id']:x for x in tasks}
 return [x for x in tasks if (include_release or x['scope']=='DEVELOPMENT') and x['status'] in {'NOT_ASSESSED','PLANNED','IN_PROGRESS'} and all(by[y]['status'] in READY and not by[y].get('needsAdaptation') for y in x['depends_on'])]
def validate_dag(tasks):
 by={x['id']:x for x in tasks};vis=set();active=set()
 def visit(k):
  if k in active:raise ValueError('依赖环:'+k)
  if k in vis:return
  if k not in by:raise ValueError('缺任务:'+k)
  active.add(k)
  for d in by[k]['depends_on']:visit(d)
  active.remove(k);vis.add(k)
 for k in by:visit(k)
 return True

def safe_evidence(root,path):
 p=(root/path).resolve()
 if not p.is_relative_to(root.resolve()) or not p.is_file():raise ValueError('证据必须是仓库内现存文件，不接受URL或任意路径')
 return p

def update(root,task,status,evidence=None,reason=None):
 state=root/'docs/implementation/vehicle';p=state/'tasks.json';tasks=read(p);by={x['id']:x for x in tasks}
 if task not in by:raise ValueError('未知任务')
 t=by[task]
 if status not in STATES:raise ValueError('非法状态')
 if status in {'IN_PROGRESS','IMPLEMENTED','VERIFIED','REVIEWED'} and not all(by[d]['status'] in READY and not by[d].get('needsAdaptation') for d in t['depends_on']):raise ValueError('前置任务尚未实现，不能开始；可先补前置')
 if status in {'IMPLEMENTED','VERIFIED','REVIEWED'}:
  if not evidence:raise ValueError('此状态必须提供结构化证据文件')
  ep=safe_evidence(root,evidence);data=read(ep)
  if data.get('taskId')!=task:raise ValueError('证据任务ID不一致')
  if t.get('needsAdaptation') and data.get('scopeRevision')!=t.get('scopeRevision'):raise ValueError('身份范围已变化，证据必须对应当前scopeRevision')
  paths=data.get('codeFiles',[])
  if not paths:raise ValueError('缺具体代码/工程产物路径')
  for f in paths:safe_evidence(root,f)
  if status in {'VERIFIED','REVIEWED'}:
   runs=data.get('runs',[])
   if not runs or any(r.get('exitCode')!=0 for r in runs):raise ValueError('缺命令记录或存在失败')
   for r in runs:
    safe_evidence(root,r.get('reportPath',''))
    count=r.get('checksRun',0) if r.get('layer') in {'WORKSPACE_OBSERVATION','ENGINEERING_ENVIRONMENT','BUILD'} and not t['api_ids'] else r.get('testsRun',0)
    if count<1 or r.get('skipped',0)>0:raise ValueError('无实际检查/测试或skip不能通过本任务验收')
   if t['api_ids'] and not any(r.get('layer')=='LIVE_HTTP_MYSQL' for r in runs):raise ValueError('业务任务必须包含真实HTTP/MySQL测试证据')
  if status=='REVIEWED':
   rev=data.get('review',{})
   if not rev.get('approved') or not rev.get('reviewer') or rev.get('reviewer')==data.get('implementer'):raise ValueError('独立复核信息不足')
   safe_evidence(root,rev.get('reportPath',''))
  t.setdefault('evidence',[]).append({'path':str(ep.relative_to(root)),'sha256':hashlib.sha256(ep.read_bytes()).hexdigest()})
 if status=='BLOCKED' and not reason:raise ValueError('阻塞必须说明类型/具体命令/恢复条件；不能仅填“无法开发”')
 if status in {'IMPLEMENTED','VERIFIED','REVIEWED'}:t['needsAdaptation']=False
 t['status']=status;t['updatedAt']=dt.datetime.now(dt.timezone.utc).isoformat();t['blocker']=reason if status=='BLOCKED' else None
 write(p,tasks)
 nxt=ordered_next(tasks)
 (state/'RESUME.md').write_text('# 当前恢复点\n\n最近任务：'+task+' · '+status+'\n\n下一可执行：'+(', '.join(x['id'] for x in nxt) or '无；核对BLOCKED和测试/复核待办')+'\n\n保留工作区，不重新初始化。证据完整性只校验格式和存在，仍需复核报告真实性。\n',encoding='utf8')
 return t

def note_blocker(root,task,layer,reason):
 if layer not in {'IMPLEMENTATION','VERIFICATION','RELEASE'} or not reason:raise ValueError('阻塞必须明确层次与恢复条件')
 p=root/'docs/implementation/vehicle/tasks.json';tasks=read(p);t=next((x for x in tasks if x['id']==task),None)
 if t is None:raise ValueError('未知任务')
 t.setdefault('blockers',[]).append({'layer':layer,'reason':reason,'at':dt.datetime.now(dt.timezone.utc).isoformat()})
 if layer=='IMPLEMENTATION':t['status']='BLOCKED'
 # Environment prevents verification, not necessarily implementation of dependent code.
 # Keep IMPLEMENTED as implemented; VERIFIED/REVIEWED are never newly asserted by this action.
 write(p,tasks);return t

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('command',choices=['next','list','set','summary','note-blocker']);p.add_argument('--repo',default='.');p.add_argument('--task');p.add_argument('--status');p.add_argument('--evidence');p.add_argument('--reason');p.add_argument('--include-release',action='store_true');p.add_argument('--layer',choices=['IMPLEMENTATION','VERIFICATION','RELEASE']);a=p.parse_args();root=Path(a.repo).resolve();path=root/'docs/implementation/vehicle/tasks.json'
 if not path.exists():raise ValueError('先执行engineer install --apply')
 tasks=read(path);validate_dag(tasks)
 if a.command=='note-blocker':print(json.dumps(note_blocker(root,a.task,a.layer,a.reason),ensure_ascii=False,indent=2));return
 if a.command=='set':print(json.dumps(update(root,a.task,a.status,a.evidence,a.reason),ensure_ascii=False,indent=2));return
 if a.command=='next':
  nxt=ordered_next(tasks,a.include_release); queue=[{'id':t['id'],'mode':'IMPLEMENT','title':t['title'],'packet':'docs/vehicle/V6.4/12_工程执行/task_packets/'+t['id']+'/TASK.md'} for t in nxt]; queue += [{'id':t['id'],'mode':'VERIFY' if t['status']=='IMPLEMENTED' else 'INDEPENDENT_REVIEW','title':t['title']} for t in tasks if t['status'] in {'IMPLEMENTED','VERIFIED'} and (a.include_release or t['scope']=='DEVELOPMENT')];print(json.dumps(queue,ensure_ascii=False,indent=2));return
 if a.command=='list':print('\n'.join(f"{t['id']:4} {t['status']:15} {t['title']}" for t in tasks));return
 dev=[t for t in tasks if t['scope']=='DEVELOPMENT'];release=[t for t in tasks if t['scope']=='RELEASE']
 print(json.dumps({'developmentTotal':len(dev),'reviewed':sum(t['status']=='REVIEWED' for t in dev),'codeComplete':all(t['status']=='REVIEWED' for t in dev),'releaseReady':all(t['status']=='REVIEWED' for t in tasks),'remaining':[t['id'] for t in tasks if t['status']!='REVIEWED'],'warning':'IMPLEMENTED只代表代码已写；资料检查、原型和外部SDK单测不能替代LIVE_HTTP_MYSQL。此工具不是后台调度。'},ensure_ascii=False,indent=2))
if __name__=='__main__':
 try:main()
 except (ValueError,KeyError,OSError) as e:print('BLOCKED:',e,file=sys.stderr);sys.exit(2)
