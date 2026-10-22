"""Regenerate active human indices from authoritative JSON. No repo/database access."""
from pathlib import Path
import json,hashlib,html,re,csv,copy
R=Path(__file__).resolve().parents[1]
def load(p):return json.loads((R/p).read_text(encoding='utf8'))
def dump(p,x):(R/p).write_text(json.dumps(x,ensure_ascii=False,indent=2),encoding='utf8')
cases=load('06_验收/test_cases_V6.4.json'); cat=load('03_交互与设计规范/画板索引.json');tasks=load('12_工程执行/tasks.json')
feature='# language: zh-CN\n功能: 机动车检测预约全量已确认业务\n\n'
for c in cases:
 feature+='  @'+c['id']+' @'+c['priority']+'\n  场景: '+c['title']+'\n    假如 '+c['given'].replace('\n',' ')+'\n    当 '+c['when'].replace('\n',' ')+'\n    那么 '+c['then'].replace('\n',' ')+'\n\n'
(R/'06_验收/全量业务验收_V6.4.feature').write_text(feature,encoding='utf8')
for t in tasks:
 for k in ['deliverables','done_when','negative_checks']:
  t[k]=[re.sub(r'(?:66|67|79)表',str(len(load('05_数据设计/data_model_V6.4.json')))+'表',re.sub(r'(?:287|299|317|355)画板',str(len(cat))+'画板',re.sub(r'(?:298|334|350|412)条',str(len(cases))+'条',v))) for v in t[k]]
 t['definitionHash']=hashlib.sha256(json.dumps({k:v for k,v in t.items() if k!='definitionHash'},ensure_ascii=False,sort_keys=True).encode()).hexdigest()
 p=R/'12_工程执行/task_packets'/t['id'];p.mkdir(exist_ok=True);lines=[f"# {t['id']} · {t['title']}",'',f"阶段：{t['phase']}；责任域：{t['domain']}；前置：{', '.join(t['depends_on']) or '无'}。",f"范围版本：`{t['scopeRevision']}`；definitionHash：`{t['definitionHash']}`。",'', '## 本任务交付']
 lines += ['- '+v for v in t['deliverables']];lines+=['','## 必测失败与并发']+['- '+v for v in t['negative_checks']]
 lines+=['','## 精确输入','主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。', '接口：'+(', '.join(t['api_ids'])or'无'),'画板：'+(', '.join(t['page_ids'])or'无'),'主责任表：'+(', '.join(t['tables'])or'无'),'验收：'+(', '.join(t['case_ids'])or'无'),'', '## 代码落位']+['`'+v+'`'for v in t['code_paths']]
 lines+=['','## 验证命令','```bash']+t['commands']+['```','','## 完成标准']+['- '+v for v in t['done_when']]
 lines+=['','## 执行边界','已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。']
 (p/'TASK.md').write_text('\n'.join(lines)+'\n',encoding='utf8');(p/'acceptance.json').write_text(json.dumps([c for c in cases if c['id']in t['case_ids']],ensure_ascii=False,indent=2),encoding='utf8')
dump('12_工程执行/tasks.json',tasks)
# task navigator keeps styles and search logic but replaces all task entries from JSON.
p=R/'12_工程执行/任务导航.html';s=p.read_text();entries=[]
for t in tasks:
 searchable=html.escape((' '.join([t['id'],t['title']]+t['deliverables'])).lower(),quote=True)
 badges=''.join(f'<span class="badge">{name} {len(t[k])}</span>'for name,k in [('接口','api_ids'),('画板','page_ids'),('表','tables'),('用例','case_ids')])
 entries.append(f'<section class="task" data-phase="{t["phase"]}" data-search="{searchable}"><h3><a href="task_packets/{t["id"]}/TASK.html">{t["id"]} · {html.escape(t["title"])}</a></h3><p>{t["phase"]} · 前置：{html.escape("、".join(t["depends_on"])or"无")}</p><p>{html.escape("；".join(t["deliverables"]))}</p><p>{badges}</p></section>')
s=re.sub(r'(<main id="list">).*?(</main>)',lambda m:m[1]+''.join(entries)+m[2],s,flags=re.S);p.write_text(s,encoding='utf8')
# page matrix
specs=load('01_PRD/page_specs_V6.4.json');dump('02_UI/src/prd_pages.json',specs)
lines=['# 全量页面与交互规格 V6.4','本文件是主PRD的规范性组成部分；状态画板不等于独立页面。']
for x in specs:
 lines += ['',f"## {x['id']} · {x['name']}",f"**状态**：{x['status']}",f"**字段**：{x['fields']}",f"**操作**：{x['actions']}",f"**约束**：{x['guards']}",f"**关联流程**：{x['flows']}",f"**来源**：{x['source']}"]
(R/'01_PRD/全量页面与交互规格_V6.4.md').write_text('\n\n'.join(lines)+'\n',encoding='utf8')
p=R/'02_UI/src/catalog.js';s=p.read_text();p.write_text('window.UI_CATALOG='+json.dumps(cat,ensure_ascii=False)+';\n'+s[s.index('window.UI_ALIASES'):],encoding='utf8')
print('derived JSON→feature/task/navigation/page specs/catalog synchronized',len(tasks),len(cases),len(cat))
