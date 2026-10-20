"""V6.1 rebaseline tooling tests. Synthetic worktrees, not a build/test of xinyi."""
from pathlib import Path
import importlib.util,json
import pytest
B=Path(__file__).resolve().parents[2]
def load(name):
 spec=importlib.util.spec_from_file_location(name,B/'12_工程执行'/f'{name}.py')
 module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);return module
E=load('engineer');S=load('scaffold');T=load('taskctl')
SHA='890cfbbfc5e0db404a932ab5ef8b449d07c7f444'

def test_source_is_exact_user_branch():
 b=json.loads((B/'12_工程执行/baseline.json').read_text())
 assert b['observed_refs']=={'rongli-wx':SHA}
 assert b['m0_completed'] is False
 assert b['user_confirmed_stage']=='FRAMEWORK_ONLY_M0_NOT_STARTED'

def test_first_ready_task_is_e00_not_m1():
 tasks=json.loads((B/'12_工程执行/tasks.json').read_text())
 assert all(t['status']=='NOT_ASSESSED' for t in tasks)
 assert [t['id'] for t in T.ordered_next(tasks)]==['E00']

def test_nothing_in_diagnostic_marks_m0_done(tmp_path):
 x=E.baseline_observation(tmp_path,{'gitHead':{'stdout':SHA+'\n'}},['xinyi-admin'])
 assert x['headMatchesObservation'] is True and x['businessModuleAction']=='CREATE_NEW'
 assert x['m0Completion'].startswith('NOT_INFERRED')
 assert x['existingDatabaseStatus']=='NOT_INSPECTED'

def test_new_commits_are_not_forced_back(tmp_path):
 x=E.baseline_observation(tmp_path,{'gitHead':{'stdout':'1'*40}},['xinyi-admin'])
 assert x['headMatchesObservation'] is False
 assert x['m0Completion'].startswith('NOT_INFERRED')

def test_module_presence_still_not_stage_completed(tmp_path):
 x=E.baseline_observation(tmp_path,{'gitHead':{'stdout':SHA}},['xinyi-admin','xinyi-rongli'])
 assert x['businessModuleAction']=='INSPECT_PRESENT_MODULE_NO_COMPLETION_INFERENCE'
 assert x['m0Completion'].startswith('NOT_INFERRED')

def test_existing_managed_block_updated_not_duplicated():
 old='SECURITY ORIGINAL\n<!-- VEHICLE_V6_EXECUTION_BEGIN -->\nread docs/vehicle/V6.0/START-CODEX.md\n<!-- VEHICLE_V6_EXECUTION_END -->\nCUSTOM TAIL'
 new=E.merge_managed_guidance(old)
 assert new.startswith('SECURITY ORIGINAL') and new.endswith('CUSTOM TAIL')
 assert 'docs/vehicle/V6.1/'in new and 'docs/vehicle/V6.0/'not in new
 assert new.count('VEHICLE_V6_EXECUTION_BEGIN')==1
 assert E.merge_managed_guidance(new)==new

@pytest.mark.parametrize('bad',[
 '<!-- VEHICLE_V6_EXECUTION_BEGIN -->',
 '<!-- VEHICLE_V6_EXECUTION_END -->',
 '<!-- VEHICLE_V6_EXECUTION_END --><!-- VEHICLE_V6_EXECUTION_BEGIN -->',
 '<!-- VEHICLE_V6_EXECUTION_BEGIN --><!-- VEHICLE_V6_EXECUTION_END -->'*2])
def test_malformed_managed_blocks_refused(bad):
 with pytest.raises(ValueError):E.merge_managed_guidance(bad)

def test_initial_scaffold_is_dry_run_and_preserves_framework(tmp_path):
 root='<project><modules><module>xinyi-admin</module><module>xinyi-system</module></modules></project>'
 admin='<project><dependencies><dependency><groupId>com.xinyi</groupId><artifactId>xinyi-framework</artifactId></dependency></dependencies></project>'
 (tmp_path/'pom.xml').write_text(root);(tmp_path/'xinyi-admin').mkdir();(tmp_path/'xinyi-admin/pom.xml').write_text(admin)
 files=S.plan(tmp_path);paths={str(p.relative_to(tmp_path)) for p,s in files}
 assert 'xinyi-rongli/pom.xml' in paths and 'xinyi-miniapp/package.json'in paths
 assert (tmp_path/'pom.xml').read_text()==root
 next_root=next(s for p,s in files if p==tmp_path/'pom.xml')
 assert '<module>xinyi-admin</module>'in next_root and '<module>xinyi-system</module>'in next_root
 assert next_root.count('<module>xinyi-rongli</module>')==1

def test_platform_code_waits_for_created_module():
 tasks={t['id']:t for t in json.loads((B/'12_工程执行/tasks.json').read_text())}
 assert 'E04'in tasks['E06']['depends_on'] and 'E04'in tasks['E07']['depends_on']
 assert '新建' in tasks['E04']['title']

def test_partial_tables_are_not_m0_completion():
 text=(B/'12_工程执行/database.py').read_text()
 assert 'PARTIAL_VI_SCHEMA_NEEDS_REVIEW'in text and 'M0_CANDIDATE'not in text
 assert 'if existing:raise ValueError'in text

def test_active_prompt_copies_same_and_no_old_sha():
 text=(B/'START-CODEX.md').read_text()
 assert '没有做 M0'in text and SHA in text and '95319ad'not in text
 assert text==(B/'07_Agent/首次交给Agent的提示词.md').read_text()
 assert text==(B/'10_技术架构_T3.1/00_交给Agent的任务.md').read_text()
