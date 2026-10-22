#!/usr/bin/env python3
"""V6 engineering runner. Never starts Codex, mutates Git history or deploys production.
All arguments are passed as argv (not shell); unknown/missing commands fail closed.
"""
from __future__ import annotations
import argparse, datetime as dt, hashlib, json, os, re, secrets, shutil, subprocess, sys
from pathlib import Path
BUNDLE=Path(__file__).resolve().parents[1]
SELF=BUNDLE/'12_工程执行'
STATE=Path('docs/implementation/vehicle')
SPEC=Path('docs/vehicle/V6.4')

def write_json(p:Path,data:object)->None:
 p.parent.mkdir(parents=True,exist_ok=True)
 tmp=p.with_suffix(p.suffix+'.tmp');tmp.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf8');tmp.replace(p)
def load_json(p:Path):return json.loads(p.read_text(encoding='utf8'))
def now():return dt.datetime.now(dt.timezone.utc).isoformat()
def capture(argv:list[str],cwd:Path,timeout:int=30):
 try:
  r=subprocess.run(argv,cwd=cwd,capture_output=True,text=True,errors='replace',timeout=timeout,check=False)
  return {'argv':argv,'exitCode':r.returncode,'stdout':r.stdout[-20000:],'stderr':r.stderr[-10000:]}
 except (OSError,subprocess.TimeoutExpired) as ex:return {'argv':argv,'exitCode':127,'error':type(ex).__name__}
def repo_at(p:str)->Path:
 root=Path(p).expanduser().resolve()
 if not (root/'pom.xml').exists() or not (root/'xinyi-admin').is_dir():raise ValueError('不是xinyi仓库根目录（需pom.xml和xinyi-admin）')
 return root

def baseline_observation(root:Path,checks:dict,modules:list[str])->dict:
 baseline=load_json(SELF/'baseline.json')
 expected=baseline['observed_refs'][baseline['source_branch']]
 actual=checks.get('gitHead',{}).get('stdout','').strip()
 return {
  'sourceBranch':baseline['source_branch'], 'observedSourceCommit':expected,
  'actualHead':actual or None,
  'headMatchesObservation':actual==expected if actual else None,
  'userConfirmedStage':baseline['user_confirmed_stage'],
  'm0Completion':'NOT_INFERRED_REQUIRES_CURRENT_TASK_EVIDENCE',
  'businessModuleAction':'CREATE_NEW' if not any(x in modules for x in ['xinyi-rongli','xinyi-vehicle']) else 'INSPECT_PRESENT_MODULE_NO_COMPLETION_INFERENCE',
  'databasePlan':'FRESH_ISOLATED_OR_INSPECTED_FORWARD_ADAPTATION',
  'existingDatabaseStatus':'NOT_INSPECTED',
  'note':'历史框架起点不代表当前进度；读取真实工作区，SHA差异不自动回退。已有模块/表和dirty保留，不推定阶段完成。'
 }

def merge_managed_guidance(text:str)->str:
 begin='<!-- VEHICLE_V6_EXECUTION_BEGIN -->'
 end='<!-- VEHICLE_V6_EXECUTION_END -->'
 block=begin+'\n先实勘当前rongli-wx工作区；未开发从E00，已有进度保留并按V6.4身份范围适配。阅读 `docs/vehicle/V6.4/START-CODEX.md`，状态在 `docs/implementation/vehicle/`。此入口替换本包旧V6入口，旧规范目录不并行执行；不删除原目录或真实进度。不得从历史文档、SQL、SDK样例或模块存在推定M0完成。核对后按就绪任务持续实现至G01；遵守本仓库已有安全、数据、独立复核约束。不授权生产变更、真实转账、删除数据或绕过运行环境审批。\n'+end
 if text.count(begin)!=text.count(end) or text.count(begin)>1:
  raise ValueError('工程管理区段不完整或重复；请人工核对，拒绝覆盖AGENTS')
 if begin in text:
  start=text.index(begin);stop=text.index(end)+len(end)
  if stop<=start:raise ValueError('工程管理区段顺序非法')
  return text[:start]+block+text[stop:]
 return text.rstrip()+'\n\n'+block+'\n'

def inspect_repo(root:Path)->dict:
 checks={k:capture(v,root) for k,v in {'gitHead':['git','rev-parse','HEAD'],'gitBranch':['git','branch','--show-current'],'gitStatus':['git','status','--short'],'java':['java','-version'],'node':['node','--version'],'npm':['npm.cmd' if os.name=='nt' else 'npm','--version'],'maven':[os.getenv('MVN','mvn.cmd' if os.name=='nt' else 'mvn'),'-version'],'docker':['docker','version'],'compose':['docker','compose','version']}.items()}
 modules=[p.name for p in root.glob('xinyi-*') if (p/'pom.xml').exists()]
 candidates=[m for m in ['xinyi-rongli','xinyi-vehicle'] if m in modules]
 module=candidates[0] if len(candidates)==1 else 'xinyi-rongli' if not candidates else None
 guidance=[]
 for name in ['AGENTS.md','AGENTS.override.md','CLAUDE.md']:
  p=root/name
  if p.is_file():guidance.append({'path':name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
 return {'baselineObservation':baseline_observation(root,checks,modules),'observedAt':now(),'repoRoot':str(root),'checks':checks,'guidance':guidance,'modules':modules,'suggestedBusinessModule':module,'moduleConflict':len(candidates)>1,'miniappExists':(root/'xinyi-miniapp/package.json').exists(),'databaseStatus':'NOT_INSPECTED','note':'只记录工具版本/路径/Git；不读取输出application*.yml或环境变量秘密；存在代码不自动判完成。'}

def merge_tasks(existing:list[dict],definitions:list[dict])->list[dict]:
 old={t['id']:t for t in existing};merged=[]
 runtime=['status','evidence','implementation','review','blocker','blockers','updatedAt','needsAdaptation','scopeHistory']
 for d in definitions:
  prior=old.get(d['id'])
  t=dict(d)
  if prior:
   for k in runtime:
    if k in prior:t[k]=prior[k]
   affected=d.get('scopeRevision','').startswith('V6.4-') and prior.get('scopeRevision')!=d.get('scopeRevision')
   if affected:
    t.setdefault('scopeHistory',[]).append({'fromScope':prior.get('scopeRevision','V6.1'),'toScope':d['scopeRevision'],
      'priorStatus':prior.get('status'),'priorEvidence':prior.get('evidence',[]),'reason':'用户确认身份流程变更，保留代码和旧证据但需适配新范围','at':now()})
    if prior.get('status') not in ['NOT_ASSESSED','PLANNED']:
     t['status']='PLANNED';t['needsAdaptation']=True
     t['blocker']=None
  else:t.update(status='NOT_ASSESSED',evidence=[],implementation=None,review=None,blocker=None)
  merged.append(t)
 return merged

def merge_coverage(existing:list[dict],definitions:list[dict],key:str,changed_tasks:set[str])->tuple[list[dict],list[dict]]:
 old={x[key]:x for x in existing};active=[];newids={x[key] for x in definitions}
 for d in definitions:
  prior=old.get(d[key],{})
  x={**d,**prior}
  # source metadata and responsibility are authoritative; implementation paths/history remain.
  for k in ['taskId','summary','method','path']:
   if k in d:x[k]=d[k]
  if d.get('taskId') in changed_tasks and prior:
   x['previousVerification']={'status':prior.get('status',prior.get('implementationStatus')),'lastResult':prior.get('lastResult'),'evidence':prior.get('evidence')}
   x['needsRevalidation']=True
   if 'implementationStatus'in x:x['implementationStatus']='NOT_ASSESSED'
   if 'status'in x:x['status']='NOT_ASSESSED'if key!='caseId'else'NOT_RUN'
   if 'lastResult'in x:x['lastResult']='NOT_RUN'
  active.append(x)
 retired=[x for k,x in old.items()if k not in newids]
 return active,retired

def install(root:Path,apply:bool):
 target=root/SPEC
 if target==BUNDLE:print('当前包已在规范目录；将仅核对工作账本。')
 elif target.exists():
  sentinel=target/'12_工程执行/baseline.json'
  if not sentinel.exists() or load_json(sentinel)!=load_json(SELF/'baseline.json'):raise ValueError('规范目录已存在且基线不同；拒绝覆盖，先对齐。')
 actions=['实勘当前进度；新项目从E00，已有代码不重置','保存当前Git/工具实勘到 docs/implementation/vehicle/workspace.json','将完整规范复制到 docs/vehicle/V6.4（已存在则不覆盖）','仅更新本包管理的根AGENTS工程入口；保留其他正文及真实进度','合并任务定义；保留进度证据；已变化范围需适配/重验，旧证据不作为新规则通过']
 print(json.dumps({'apply':apply,'actions':actions},ensure_ascii=False,indent=2))
 if not apply:return
 _agents=root/'AGENTS.md'
 merge_managed_guidance(_agents.read_text(encoding='utf8') if _agents.exists() else '')
 if target!=BUNDLE and not target.exists():shutil.copytree(BUNDLE,target,ignore=shutil.ignore_patterns('__pycache__','*.pyc','.env','node_modules','.pytest_cache'))
 state=root/STATE;state.mkdir(parents=True,exist_ok=True)
 write_json(state/'workspace.json',inspect_repo(root))
 previous=load_json(state/'tasks.json') if (state/'tasks.json').exists() else []
 defs=load_json(SELF/'tasks.json');merged=merge_tasks(previous,defs)
 changed={t['id'] for t in merged if t.get('needsAdaptation')}
 write_json(state/'tasks.json',merged)
 for file,key in [('api_implementation_map.json','operationId'),('page_implementation_map.json','boardId'),('table_implementation_map.json','table'),('case_implementation_map.json','caseId')]:
  path=state/file
  active,retired=merge_coverage(load_json(path)if path.exists()else[],load_json(SELF/file),key,changed)
  write_json(path,active)
  if retired:
   archive=state/'retired'/('V6.4_'+file)
   # Idempotent install never discards prior retired evidence.
   before=load_json(archive)if archive.exists()else[]
   bykey={x[key]:x for x in before}
   for x in retired:bykey.setdefault(x[key],x)
   write_json(archive,list(bykey.values()))
 if not (state/'ci-stage.json').exists():write_json(state/'ci-stage.json',{'stage':'FOUNDATION','meaning':'只有基座检查，不表示完整功能；按阶段实际验证后前移'})
 for f,text in [('RESUME.md','# 恢复点\n\n读取当前账本：未开发从E00，已有实现按需适配。不预标完成，不重置工作。\n'),('BLOCKERS.md','# 阻塞记录\n\n尚未实测；不得推断所有模块不能开发。\n'),('DECISIONS.md','# 当前工作区适配裁决\n\n工程路径/基线以workspace.json为输入；不得自行降低安全边界。\n')]:
  if not (state/f).exists():(state/f).write_text(text,encoding='utf8')
 agents=root/'AGENTS.md';text=agents.read_text(encoding='utf8') if agents.exists() else ''
 agents.write_text(merge_managed_guidance(text),encoding='utf8')
 if (root/'AGENTS.override.md').exists():
  print('注意：根AGENTS.override.md存在。请按实际Codex加载作用域合并工程入口；安装器不覆盖更高优先级的用户规则。')
 print('安装已完成（仅文档/工具/进度文件）；业务模块和已有配置没有被覆盖。下一步 task next。')

def init_env(root:Path,apply:bool):
 env=root/'.env.vi.local'
 if env.exists():raise ValueError('.env.vi.local已存在，禁止覆盖已有密钥')
 if not apply:print('将生成本地独立测试环境密钥和端口，不连接远程服务。加--apply执行。');return
 items={'VI_ENV':'local','VI_DB_HOST':'127.0.0.1','VI_DB_PORT':'33306','VI_DB_NAME':'xinyi_vi_dev','VI_DB_USER':'vi_dev','VI_DB_PASSWORD':secrets.token_hex(24),'VI_DB_ROOT_PASSWORD':secrets.token_hex(24),'VI_REDIS_HOST':'127.0.0.1','VI_REDIS_PORT':'36379','VI_REDIS_PASSWORD':secrets.token_hex(24),'VI_SIM_PORT':'19091','VI_SIM_SECRET':secrets.token_hex(32),'VI_TEST_CONTROL_KEY':secrets.token_hex(32),'VI_DATA_KEY':secrets.token_hex(32),'VI_LOOKUP_KEY':secrets.token_hex(32),'VI_ADMIN_TEST_PASSWORD':secrets.token_hex(20),'VI_APP_PORT':'18080','VI_FILES_DIR':str(root/'.vi-local/files'),'VI_PUBLIC_FILES_DIR':str(root/'.vi-local/public'),'VI_ADMIN_JWT_SECRET':secrets.token_hex(32),'VI_MYSQL_IMAGE':'mysql:8.4','VI_REDIS_IMAGE':'redis:7.4','VI_PYTHON_IMAGE':'python:3.13-slim'}
 env.write_text('\n'.join(f'{k}={v}' for k,v in items.items())+'\n',encoding='utf8')
 try:os.chmod(env,0o600)
 except OSError:pass
 gitignore=root/'.gitignore';old=gitignore.read_text(encoding='utf8') if gitignore.exists() else ''
 extra=['.env.vi.local','.vi-local/','**/.env.local','**/node_modules/','**/.pytest_cache/']
 gitignore.write_text(old+'\n'+'\n'.join(x for x in extra if x not in old.splitlines())+'\n',encoding='utf8')
 print('已生成本地密钥（不打印值），已追加.gitignore。')

def read_env(root:Path)->dict:
 p=root/'.env.vi.local'
 if not p.exists():raise ValueError('先执行env --apply，或按说明建立受控环境文件')
 out={}
 for line in p.read_text(encoding='utf8').splitlines():
  if line and not line.startswith('#') and '=' in line:k,v=line.split('=',1);out[k]=v
 return out

def execute(root:Path,kind:str):
 env=read_env(root) if kind in ['infra','infra-status','live-test','db-test','backend-run','integration-suite','integration-identity','integration-booking','integration-employee','integration-full'] else {}
 if env and env.get('VI_ENV')!='local':raise ValueError('runner只执行本地infra，不接受production')
 cmds={
 'infra':['docker','compose','--env-file',str(root/'.env.vi.local'),'-p','xinyi-vi-local','-f',str(BUNDLE/'14_工程模板/infra/compose.yaml'),'up','-d','--wait'],
 'infra-status':['docker','compose','--env-file',str(root/'.env.vi.local'),'-p','xinyi-vi-local','-f',str(BUNDLE/'14_工程模板/infra/compose.yaml'),'ps'],
 'integration-suite':[os.getenv('MVN','mvn.cmd' if os.name=='nt' else 'mvn'),'-pl','xinyi-admin','-am','-Pvi-it','-Dit.test=ViHttpSuiteIT','verify'],
 'backend-build':[os.getenv('MVN','mvn.cmd' if os.name=='nt' else 'mvn'),'-pl','xinyi-admin','-am','-DskipTests','package'],
 'backend-unit':[os.getenv('MVN','mvn.cmd' if os.name=='nt' else 'mvn'),'-pl','xinyi-admin','-am','test'],
 'pc-build':['npm.cmd' if os.name=='nt' else 'npm','--prefix','xinyi-ui','run','build:prod'],
 'mini-build':['npm.cmd' if os.name=='nt' else 'npm','--prefix','xinyi-miniapp','run','build:mp-weixin'],
 'db-test':[sys.executable,'-m','pytest',str(BUNDLE/'13_真实集成测试/database'),'-q','--junitxml='+str(root/STATE/'evidence/db-junit.xml')],
 'backend-run':['java','-jar',str(root/'xinyi-admin/target/xinyi-admin.jar'),'--spring.profiles.active=druid,vi-local','--spring.config.additional-location=file:'+str(BUNDLE/'14_工程模板/infra/application-vi-local.yml')],
 'live-test':[sys.executable,'-m','pytest',str(BUNDLE/'13_真实集成测试/live'),'-q','--junitxml='+str(root/STATE/'evidence/live-junit.xml')],
 'tools-test':[sys.executable,'-m','pytest',str(BUNDLE/'12_工程执行/tests'),'-q'],
 }
 if kind in ['integration-identity','integration-booking','integration-employee']:cmds[kind]=cmds['integration-suite']
 if kind=='integration-full':cmds[kind]=[x for x in cmds['integration-suite'] if not x.startswith('-Dit.test=')]
 if kind not in cmds:raise ValueError('不支持的命令')
 (root/STATE/'evidence').mkdir(parents=True,exist_ok=True)
 if kind in ['live-test','db-test']:env.update(VI_BASE_URL='http://127.0.0.1:'+env.get('VI_APP_PORT','18080'),VI_SIM_URL='http://127.0.0.1:'+env.get('VI_SIM_PORT','19091'))
 if kind.startswith('integration-'):env.update(VI_PYTEST_SUITE={'integration-identity':'identity','integration-booking':'booking','integration-employee':'employee','integration-suite':'all','integration-full':'all'}[kind],SPRING_CONFIG_ADDITIONAL_LOCATION='file:'+str(BUNDLE/'14_工程模板/infra/application-vi-it.yml'),VI_PYTHON=sys.executable,VI_SIM_URL='http://127.0.0.1:'+env.get('VI_SIM_PORT','19091'))
 # Use direct argv; tests must fail if environment/profile is missing. Do not swallow nonzero codes.
 proc=subprocess.run(cmds[kind],cwd=root,env={**os.environ,**env,'VI_REPO_ROOT':str(root)},check=False)
 write_json(root/STATE/'evidence'/f'{kind}-last.json',{'at':now(),'kind':kind,'exitCode':proc.returncode,'scope':'BUILD_ONLY' if kind.endswith('build') else 'LIVE_HTTP_MYSQL' if kind=='live-test' or kind.startswith('integration-') else 'MYSQL_PROBES' if kind=='db-test' else 'TOOLS_OR_COMPONENT','warning':'成功退出仍须对应测试数量、代码路径和独立复核；不是完整功能通过。'})
 return proc.returncode

def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('command',choices=['doctor','install','env','run','check']);ap.add_argument('--repo',default='.');ap.add_argument('--apply',action='store_true');ap.add_argument('--kind');args=ap.parse_args()
 root=repo_at(args.repo)
 if args.command=='doctor':print(json.dumps(inspect_repo(root),ensure_ascii=False,indent=2));return 0
 if args.command=='install':install(root,args.apply);return 0
 if args.command=='env':init_env(root,args.apply);return 0
 if args.command=='run':return execute(root,args.kind or '')
 if args.command=='check':return subprocess.run([sys.executable,str(SELF/'check_engineering.py')],cwd=root,check=False).returncode
 return 1
if __name__=='__main__':
 try:sys.exit(main())
 except (ValueError,OSError) as ex:print('BLOCKED:',str(ex),file=sys.stderr);sys.exit(2)
