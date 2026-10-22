#!/usr/bin/env python3
"""Check plan linkage and support assets; never claim business functionality is implemented."""
from pathlib import Path
import json,yaml,re,sys,importlib.util,datetime
B=Path(__file__).resolve().parents[1];out=[]
def read(p):return json.loads((B/p).read_text(encoding='utf8'))
def load(n,p):
 s=importlib.util.spec_from_file_location(n,B/p);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
def test(name,fn):
 try:r=fn();out.append({'name':name,'result':'PASS','detail':str(r)[:200]})
 except Exception as e:out.append({'name':name,'result':'FAIL','detail':str(e)[:700]})
def need(v,why='condition false'):
 if not v:raise AssertionError(why)
tasks=read('12_工程执行/tasks.json');by={t['id']:t for t in tasks};sp=yaml.safe_load((B/'04_接口/openapi_V6.4.yaml').read_text());ops={o['operationId']:(p,m,o) for p,it in sp['paths'].items() for m,o in it.items() if m in ['get','post','put','patch','delete','head','options']}
cat=read('03_交互与设计规范/画板索引.json');tables=read('05_数据设计/data_model_V6.4.json');cases=read('06_验收/test_cases_V6.4.json')
test('01 85任务ID无重复',lambda:need(len(by)==len(tasks)==85))
test('02 DAG无缺失前置或环',lambda:load('taskctl','12_工程执行/taskctl.py').validate_dag(tasks))
test('03 开发与发布边界分离',lambda:need(sum(t['scope']=='DEVELOPMENT' for t in tasks)==82 and sum(t['scope']=='RELEASE' for t in tasks)==3))
for label,file,key,target,field in [('API','api_implementation_map.json','operationId',set(ops),'api_ids'),('画板','page_implementation_map.json','boardId',{c['id'] for c in cat},'page_ids'),('表','table_implementation_map.json','table',{c['name'] for c in tables},'tables'),('用例','case_implementation_map.json','caseId',{c['id'] for c in cases},'case_ids')]:
 def check(file=file,key=key,target=target,field=field):
  a=read('12_工程执行/'+file);need(len(a)==len(target) and {x[key] for x in a}==target,'覆盖集合不一致')
  assigned=[x for t in tasks for x in t[field]];need(set(assigned)==target and len(assigned)==len(target),'任务主责任分配遗漏/重复')
 test(label+'全量唯一责任覆盖',check)
def packets():
 for t in tasks:
  d=B/'12_工程执行/task_packets'/t['id'];need((d/'TASK.md').is_file(),t['id'])
  if not t['api_ids']:
   need(not (d/'openapi.yaml').exists() or not yaml.safe_load((d/'openapi.yaml').read_text()).get('paths'),t['id']+'无API任务有多余切片');continue
  q=yaml.safe_load((d/'openapi.yaml').read_text())
  for ref in re.findall(r'\$ref:\s*[\'\"]?(#[^\s\'\"]+)',(d/'openapi.yaml').read_text()):
   x=q
   for part in ref[2:].split('/'):x=x[part.replace('~1','/').replace('~0','~')]
  found={o['operationId']for p,it in q.get('paths',{}).items()for m,o in it.items()if m in ['get','post','put','patch','delete','head','options']};need(found==set(t['api_ids']),t['id']+'切片接口不一致')
test('08 每任务输入闭包可读取',packets)
test('09 Quote地址草稿不是查询DTO',lambda:need(sp['components']['schemas']['QuoteRequest']['properties']['addressDraft']['$ref'].endswith('/AddressCreate')))
test('10 Quote两组oneOf机器约束',lambda:need(len(sp['components']['schemas']['QuoteRequest']['allOf'])==2))
test('11 接受条款只能true',lambda:need(sp['components']['schemas']['OrderCreate']['properties']['acceptedTerms']['enum']==[True]))
test('12 公开配置只读无认证且单独Schema',lambda:need(sp['paths']['/api/public/service-config']['get']['security']==[] and 'PublicServiceConfig'in sp['components']['schemas']))
test('13 当前画板与两类截图逐一覆盖',lambda:need((B/'02_UI/V6_适用说明.md').exists() and len(list((B/'02_UI/全量页面图').rglob('*.png')))==len(cat)*2))
test('14 主包与状态映射',lambda:need(len(read('12_工程执行/route-plan.json'))==len(cat) and any('src/pages/workbench'in x['targetPath']for x in read('12_工程执行/route-plan.json'))))
test('15 全量源用例映射实际目标测试',lambda:need(len(read('13_真实集成测试/case-test-plan.json'))==len(cases)))
test('16 实际工作区仍须核对且不可默认清空',lambda:need('实际' in (B/'START-CODEX.md').read_text() and 'reset/clean' in (B/'START-CODEX.md').read_text()))
test('17 私有文件与公开目录隔离',lambda:need('VI_PUBLIC_FILES_DIR' in (B/'14_工程模板/infra/application-vi-local.yml').read_text() and 'root: ${VI_FILES_DIR}' in (B/'14_工程模板/infra/application-vi-local.yml').read_text()))
test('18 环境无明文固定密码',lambda:need('secrets.token_hex' in (B/'12_工程执行/engineer.py').read_text() and 'MYSQL_PASSWORD: ${VI_DB_PASSWORD:?' in (B/'14_工程模板/infra/compose.yaml').read_text()))
test('19 真实HTTP无原型后备',lambda:need('requests.request' in (B/'13_真实集成测试/live/conftest.py').read_text() and 'pymysql.connect' in (B/'13_真实集成测试/live/conftest.py').read_text()))
test('20 测试控制只在test源码',lambda:need(all('/src/test/'in str(p) for p in (B/'14_工程模板/integration-java').rglob('*.java'))))
test('21 模拟供应商不提供业务订单API',lambda:need('/api/app/'not in (B/'13_真实集成测试/provider_simulator/simulator.py').read_text()))
test('22 初装拒绝非空库',lambda:need('if existing:raise ValueError' in (B/'12_工程执行/database.py').read_text()))
test('23 并发状态与不自证复核检查存在',lambda:need("rev.get('reviewer')==data.get('implementer')" in (B/'12_工程执行/taskctl.py').read_text()))
test('24 CI真实测试不得跳过',lambda:need('integration-suite' in (B/'15_部署与运维/ci-vehicle.yml').read_text() and 'continue-on-error'not in (B/'15_部署与运维/ci-vehicle.yml').read_text()))
test('25 生产入口拒绝测试控制',lambda:need('__integration' in (B/'15_部署与运维/nginx-public.conf').read_text()))
test('26 源验收状态未冒充运行',lambda:need(all(x['status']=='NOT_RUN'for x in cases)))
# Definition of done is a gate, not automatic discovery of true correctness.
test('27 全功能完成检查覆盖四类台账',lambda:need('case_implementation_map.json' in (B/'12_工程执行/completion_gate.py').read_text() and 'page_implementation_map.json' in (B/'12_工程执行/completion_gate.py').read_text()))
test('28 主启动入口副本一致',lambda:need((B/'START-CODEX.md').read_bytes()==(B/'07_Agent/首次交给Agent的提示词.md').read_bytes()))

test('29 当前阶段实勘与历史指纹分开',lambda:need(read('12_工程执行/baseline.json')['user_confirmed_stage']=='CURRENT_WORKTREE_REQUIRES_ASSESSMENT' and read('12_工程执行/baseline.json')['fingerprint_snapshot_commit']=='890cfbbfc5e0db404a932ab5ef8b449d07c7f444'))
test('30 首次任务无完成推定',lambda:need(all(t['status']=='NOT_ASSESSED'for t in tasks) and by['E00']['depends_on']==[]))
test('31 模块新建有任务和前置',lambda:need('新建' in by['E04']['title'] and 'E04'in by['E06']['depends_on'] and 'E04'in by['E07']['depends_on']))
test('32 新库初装既有库前向保护',lambda:need(read('12_工程执行/baseline.json')['initialization_strategy']=='FRESH_ISOLATED_OR_INSPECTED_FORWARD_ADAPTATION' and 'inventory' in ' '.join(by['E03']['deliverables'])))
test('33 当前来源仅为指定分支',lambda:need(read('12_工程执行/当前源码证据.json')['commit']==read('12_工程执行/baseline.json')['observed_refs']['rongli-wx']))
test('34 旧引用不在活动启动和适配文档',lambda:need(all('95319ad'not in (B/name).read_text()for name in ['START-CODEX.md','12_工程执行/01_执行总设计.md','12_工程执行/03_当前框架与M0初建.md'])))
report={'kind':'ENGINEERING_STATIC_CHECK','at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'pass':sum(x['result']=='PASS'for x in out),'fail':sum(x['result']=='FAIL'for x in out),'checks':out,'notCovered':['target repository build','MySQL execution','npm resolution','live backend test execution','real providers','WeChat devices']}
p=B/'09_验证记录/V6.4_工程一致性检查.json';p.parent.mkdir(exist_ok=True);p.write_text(json.dumps(report,ensure_ascii=False,indent=2));print(json.dumps(report,ensure_ascii=False,indent=2));sys.exit(bool(report['fail']))
