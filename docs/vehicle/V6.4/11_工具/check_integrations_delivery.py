"""V6.4 integration configuration delivery consistency, no backend or vendor calls."""
from pathlib import Path
import json,yaml,re,hashlib
B=Path(__file__).resolve().parents[1];out=B/'09_验证记录'
def j(p):return json.loads((B/p).read_text())
a=yaml.safe_load((B/'04_接口/openapi_V6.4.yaml').read_text());S=a['components']['schemas'];M=j('05_数据设计/data_model_V6.4.json');T=j('12_工程执行/tasks.json');C=j('03_交互与设计规范/画板索引.json');V=j('当前全量画板可见文本.json') if (B/'当前全量画板可见文本.json').exists() else j('09_验证记录/当前全量画板可见文本.json');D={v['id']:v for v in V};checks=[]
def check(n,f):
 try:
  assert f();checks.append({'name':n,'result':'PASS'})
 except Exception as e:checks.append({'name':n,'result':'FAIL','detail':str(e)})
ops=[(p,m,o)for p,ms in a['paths'].items()for m,o in ms.items()if isinstance(o,dict)and'operationId'in o]
check('01 main PRD contains config, independent payment and old payment/notice integration',lambda:all(x in (B/'01_PRD/机动车检测预约_PRD_V6.4_完整整合版.md').read_text()for x in ['## 23','### 5.','### 23.4','独立开关','待核实','腾讯云','阿里云']))
check('02 four vendor configuration shapes exist',lambda:all(x in S for x in ['WechatPayConfig','TencentSmsConfig','AliyunSmsConfig','WechatAppConfig']))
check('03 payment switches are separate booleans',lambda:all(S['PaymentPolicyConfig']['properties'][x]['type']=='boolean'for x in ['offlineEnabled','wechatEnabled']))
check('04 current UI catalog has all eight new admin pages',lambda:all(any(x['id']==f'B{i}'for x in C)for i in range(46,54)))
check('05 all legacy B21 editing removes supplier template input',lambda:'template.id'not in D['B21-edit']['fields'])
check('06 actual offline page remains pending not paid',lambda:'等待线下付款'in D['O10-offline']['text'] and '收款主体'in D['O10-offline']['text'])
check('07 current operator sheets include supplier-specific fields',lambda:all(x in D['B48']['text']for x in ['SmsSdkAppId','SecretId'])and all(x in D['B48-aliyun']['text']for x in ['AccessKeyId','AccessKeySecret'] ))
check('08 current instructions synchronize all copies',lambda:(B/'START-CODEX.md').read_bytes()==(B/'07_Agent/首次交给Agent的提示词.md').read_bytes()==(B/'10_技术架构_T3.4/00_交给Agent的任务.md').read_bytes())
check('09 new seven task IDs exist and G01 depends on X07',lambda:all(any(t['id']==f'X{i:02}'for t in T)for i in range(1,8))and 'X07'in next(t for t in T if t['id']=='G01')['depends_on'])
check('10 every operation has exact one main owner task',lambda:sorted(o['operationId']for _,_,o in ops)==sorted(k for t in T for k in t['api_ids']))
check('11 every board has exact one main owner task',lambda:sorted(x['id']for x in C)==sorted(k for t in T for k in t['page_ids']))
check('12 every table has database primary key source',lambda:all(t['primaryKey']for t in M)and len(M)==79)
check('13 configuration history is not ordinary mutable record',lambda:all(not w['updateColumns']for w in j('05_数据设计/生成列与写入白名单.json')if w['table']in ['vi_integration_revision','vi_integration_publication','vi_message_template_revision']))
check('14 62 new cases present and all business cases NOT_RUN',lambda:len([c for c in j('06_验收/test_cases_V6.4.json')if c['id'].startswith('TC-CFG-')])==62 and all(c['status']=='NOT_RUN'for c in j('06_验收/test_cases_V6.4.json')))
check('15 standalone UI includes actual latest integration script',lambda:all((B/'02_UI/src'/n).read_text().replace('</script>','<\/script>')in (B/'02_UI/index.html').read_text()for n in ['integration_model.js','integration_pages.js','integration_interactions.js']))
check('16 710 current PNG exports exist',lambda:all((B/'02_UI/全量页面图'/k/(x['id']+'.png')).exists()for x in C for k in ['首屏','完整内容']))
check('17 actual 41 config UI tests passed without errors',lambda:j('09_验证记录/V6.4_第三方配置交互实测.json')['passed']==41 and j('09_验证记录/V6.4_第三方配置交互实测.json')['failed']==0)
check('18 no draft assignment written as production success',lambda:all('NOT_RUN'==c['status']for c in j('06_验收/test_cases_V6.4.json')))
r={'kind':'V6.4_NEW_SCOPE_CROSS_ARTIFACT_CHECK','checks':checks,'passed':sum(x['result']=='PASS'for x in checks),'failed':sum(x['result']=='FAIL'for x in checks),'scope':'current documents, captured visible UI and machine models only'}
(out/'V6.4_第三方新增一致性检查.json').write_text(json.dumps(r,ensure_ascii=False,indent=2));print(json.dumps(r,ensure_ascii=False,indent=2));raise SystemExit(bool(r['failed']))
