from pathlib import Path
import importlib.util,json,sys
import pytest
B=Path(__file__).resolve().parents[2]
def load(n):
 s=importlib.util.spec_from_file_location(n,B/'12_工程执行'/f'{n}.py');m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
T=load('taskctl');D=load('database');E=load('engineer');S=load('scaffold');G=load('completion_gate')
def one(i,dep=[],status='PLANNED',scope='DEVELOPMENT',apis=[]):return{'id':i,'depends_on':dep,'status':status,'scope':scope,'api_ids':apis,'title':i}
def write(p,x):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x))
def state(tmp,tasks):write(tmp/'docs/implementation/vehicle/tasks.json',tasks)
def evidence(tmp,task='A',tests=1,skipped=0,layer='LIVE_HTTP_MYSQL',reviewer='reviewer'):
 (tmp/'code.py').write_text('# code');(tmp/'test.xml').write_text('<testsuite/>');(tmp/'review.md').write_text('independent review')
 write(tmp/'evidence.json',{'taskId':task,'implementer':'author','codeFiles':['code.py'],'runs':[{'exitCode':0,'testsRun':tests,'skipped':skipped,'reportPath':'test.xml','layer':layer}],'review':{'approved':True,'reviewer':reviewer,'reportPath':'review.md'}})
 return 'evidence.json'
def test_dag_cycle():
 with pytest.raises(ValueError):T.validate_dag([one('A',['B']),one('B',['A'])])
def test_missing_dependency():
 with pytest.raises(ValueError):T.validate_dag([one('A',['X'])])
def test_ready_local_not_release():assert[x['id']for x in T.ordered_next([one('A'),one('B',['A']),one('L',scope='RELEASE')])]==['A']
def test_missing_evidence(tmp_path):
 state(tmp_path,[one('A')])
 with pytest.raises(ValueError):T.update(tmp_path,'A','IMPLEMENTED')
def test_no_bypass_predecessor(tmp_path):
 state(tmp_path,[one('A'),one('B',['A'])]);p=evidence(tmp_path,'B')
 with pytest.raises(ValueError):T.update(tmp_path,'B','IMPLEMENTED',p)
@pytest.mark.parametrize('tests,skip',[(0,0),(1,1)])
def test_no_zero_or_skipped_verification(tmp_path,tests,skip):
 state(tmp_path,[one('A')]);p=evidence(tmp_path,tests=tests,skipped=skip)
 with pytest.raises(ValueError):T.update(tmp_path,'A','VERIFIED',p)
def test_business_requires_live_layer(tmp_path):
 state(tmp_path,[one('A',apis=['api'])]);p=evidence(tmp_path,layer='SCHEMA')
 with pytest.raises(ValueError):T.update(tmp_path,'A','VERIFIED',p)
def test_self_review_rejected(tmp_path):
 state(tmp_path,[one('A')]);p=evidence(tmp_path,reviewer='author')
 with pytest.raises(ValueError):T.update(tmp_path,'A','REVIEWED',p)
def test_review_and_resume_saved(tmp_path):
 state(tmp_path,[one('A'),one('B',['A'])]);p=evidence(tmp_path);T.update(tmp_path,'A','REVIEWED',p)
 assert 'B'in(tmp_path/'docs/implementation/vehicle/RESUME.md').read_text()
def test_evidence_path_escape(tmp_path):
 with pytest.raises(ValueError):T.safe_evidence(tmp_path,'../../etc/passwd')
def test_block_needs_reason(tmp_path):
 state(tmp_path,[one('A')])
 with pytest.raises(ValueError):T.update(tmp_path,'A','BLOCKED')
def test_sql_literals_semicolon():assert len(D.split_sql("INSERT INTO t VALUES('x;y','it''s'); -- hi\nSELECT 1;"))==2
def test_sql_unclosed():
 with pytest.raises(ValueError):D.split_sql("SELECT 'bad")
def test_sql_filter_rejects_drop_and_vi():
 x=D.selected_sys_seed('DROP TABLE sys_user; CREATE TABLE sys_user (id int); INSERT INTO sys_user VALUES(1); CREATE TABLE vi_account(id int);')
 assert len(x)==2 and all('DROP'not in s and 'vi_account'not in s for s in x)
def test_env_no_overwrite(tmp_path):
 E.init_env(tmp_path,True);a=(tmp_path/'.env.vi.local').read_bytes()
 with pytest.raises(ValueError):E.init_env(tmp_path,True)
 assert(tmp_path/'.env.vi.local').read_bytes()==a
 assert b'VI_PUBLIC_FILES_DIR' in a and b'VI_FILES_DIR' in a
def test_scaffold_conflicting_modules(tmp_path):
 (tmp_path/'pom.xml').write_text('<project><modules></modules></project>')
 for n in ['xinyi-admin','xinyi-rongli','xinyi-vehicle']:(tmp_path/n).mkdir();(tmp_path/n/'pom.xml').write_text('<project><dependencies></dependencies></project>')
 with pytest.raises(ValueError):S.plan(tmp_path)
def test_scaffold_never_replaces_business_file(tmp_path):
 (tmp_path/'pom.xml').write_text('<project><modules></modules></project>');(tmp_path/'xinyi-admin').mkdir();(tmp_path/'xinyi-admin/pom.xml').write_text('<project><dependencies></dependencies></project>')
 existing=tmp_path/'xinyi-rongli/src/main/java/com/xinyi/rongli/core/ViMoney.java';existing.parent.mkdir(parents=True);existing.write_text('CUSTOM')
 with pytest.raises(ValueError):S.plan(tmp_path)
 assert existing.read_text()=='CUSTOM'
def test_unassessed_not_complete(tmp_path):
 state(tmp_path,[one('A',status='NOT_ASSESSED')]);p=tmp_path/'docs/implementation/vehicle'
 for name in ['api_implementation_map','page_implementation_map','table_implementation_map','case_implementation_map']:write(p/(name+'.json'),[])
 assert G.validate(tmp_path)

def test_scaffold_pom_and_test_profile_plan(tmp_path):
 (tmp_path/'pom.xml').write_text('<project><modules></modules></project>')
 (tmp_path/'xinyi-admin').mkdir();(tmp_path/'xinyi-admin/pom.xml').write_text('<project><dependencies></dependencies></project>')
 planned=S.plan(tmp_path);admin=next(text for path,text in planned if path==tmp_path/'xinyi-admin/pom.xml')
 assert '<id>vi-it</id>' in admin and '<artifactId>xinyi-rongli</artifactId>' in admin
 assert any('src/test/java/com/xinyi/web/viit/FixturePort.java' in str(p) for p,t in planned)
 assert not (tmp_path/'xinyi-rongli').exists(), 'dry-run must not write'

def test_install_preserves_progress_and_root_guidance(tmp_path,monkeypatch):
 src=tmp_path/'bundle';src.mkdir();tools=src/'12_工程执行';tools.mkdir()
 write(tools/'baseline.json',{'version':'test'});write(tools/'tasks.json',[one('A')])
 for name in ['api_implementation_map','page_implementation_map','table_implementation_map','case_implementation_map']:write(tools/(name+'.json'),[])
 repo=tmp_path/'repo';repo.mkdir();(repo/'AGENTS.md').write_text('ORIGINAL SECURITY RULES')
 monkeypatch.setattr(E,'BUNDLE',src);monkeypatch.setattr(E,'SELF',tools);monkeypatch.setattr(E,'inspect_repo',lambda r:{'branch':'test'})
 E.install(repo,True);p=repo/'docs/implementation/vehicle/tasks.json';d=json.loads(p.read_text());d[0]['status']='IMPLEMENTED';write(p,d)
 E.install(repo,True)
 assert json.loads(p.read_text())[0]['status']=='IMPLEMENTED'
 a=(repo/'AGENTS.md').read_text();assert a.startswith('ORIGINAL SECURITY RULES') and a.count('VEHICLE_V6_EXECUTION_BEGIN')==1

def test_full_stage_required_even_empty_task_set(tmp_path):
 state(tmp_path,[]);p=tmp_path/'docs/implementation/vehicle'
 for n in ['api_implementation_map','page_implementation_map','table_implementation_map','case_implementation_map']:write(p/(n+'.json'),[])
 write(p/'ci-stage.json',{'stage':'FOUNDATION'});assert G.validate(tmp_path)

def test_g01_preflight_does_not_require_itself(tmp_path):
 state(tmp_path,[one('G01',status='IN_PROGRESS')]);p=tmp_path/'docs/implementation/vehicle'
 for n in ['api_implementation_map','page_implementation_map','table_implementation_map','case_implementation_map']:write(p/(n+'.json'),[])
 write(p/'ci-stage.json',{'stage':'FULL'});assert G.validate(tmp_path);assert not G.validate(tmp_path,preflight=True)

def test_verification_blocker_does_not_lock_downstream_implementation(tmp_path):
 state(tmp_path,[one('A',status='IMPLEMENTED'),one('B',['A'])]);T.note_blocker(tmp_path,'A','VERIFICATION','ENVIRONMENT: Docker unavailable; restore then execute DB test')
 d=json.loads((tmp_path/'docs/implementation/vehicle/tasks.json').read_text());assert d[0]['status']=='IMPLEMENTED';assert[x['id']for x in T.ordered_next(d)]==['B']
