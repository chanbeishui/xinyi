"""Cross-artifact checks: current visible content and derivation, not production verification."""
from pathlib import Path
import json,re,hashlib,html
from bs4 import BeautifulSoup
from playwright.sync_api import sync_playwright
R=Path(__file__).resolve().parents[1]
if not(R/'02_UI/index.html').exists():R=Path(Path('/mnt/data/v64_work/root.txt').read_text().strip())
O=R/'09_验证记录';checks=[]
def need(v,msg='condition false'):
 if not v:raise AssertionError(msg)
def check(n,fn):
 try:fn();checks.append({'name':n,'result':'PASS'})
 except Exception as e:checks.append({'name':n,'result':'FAIL','reason':str(e)[:1000]})
def read(p):return (R/p).read_text(encoding='utf8')
def j(p):return json.loads(read(p))
cat=j('03_交互与设计规范/画板索引.json');cases=j('06_验收/test_cases_V6.4.json');tasks=j('12_工程执行/tasks.json');prd=read('01_PRD/机动车检测预约_PRD_V6.4_完整整合版.md')
records=[];errors=[]
with sync_playwright()as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 pg=b.new_page(viewport={'width':1440,'height':1050});pg.on('pageerror',lambda e:errors.append(str(e)));pg.set_content(read('02_UI/index.html'),wait_until='load')
 for x in cat:
  pg.evaluate("UI.store=UI.makeStore();UI.closeDialog();document.body.className='export';document.getElementById('toast-root').innerHTML=''")
  pg.evaluate('(id)=>UI.testing.render(id)',x['id'])
  d=pg.locator('[data-screen]').evaluate("el=>({id:el.dataset.screen,text:el.innerText,buttons:[...el.querySelectorAll('button')].map(x=>({text:x.innerText,action:x.dataset.action||null,go:x.dataset.go||null,disabled:x.disabled})),fields:[...el.querySelectorAll('[data-field]')].map(x=>x.dataset.field)})")
  d['title']=pg.title();records.append(d)
 b.close()
(O/'当前全量画板可见文本.json').write_text(json.dumps(records,ensure_ascii=False,indent=2))
by={x['id']:x for x in records}
check('01 当前全部画板有真实渲染且无JS运行异常',lambda:need(len(records)==len(cat) and not errors))
check('02 全部画板浏览器标题使用V6.4',lambda:need(all('V6.4' in x['title'] for x in records)))
check('03 B25无正常员工申请或确认绑定按钮',lambda:need(not any(b['text'] in ['员工绑定申请','确认绑定','签发凭证']for b in by['B25']['buttons'])))
check('04 B25有直接配置站点与关联详情操作',lambda:need({'identity-open-grants','identity-open-link'}.issubset({b['action']for b in by['B25']['buttons']})))
check('05 统一登录不要求选注册角色或员工',lambda:need(not any('role'in x.lower()or'employee'in x.lower()for x in by['O29']['fields'])))
check('06 普通新号身份切换仅有车主',lambda:need(sum(b['action']=='auth62-role'for b in by['U01']['buttons'])==1))
check('07 已关联画板手机号也已核验',lambda:need('已核验'in by['U02-linked']['text'] and '尚未完成手机号'not in by['U02-linked']['text']))
check('08 无站点画板保持关联成功但工作未授权',lambda:need('已自动关联'in by['U02-unconfigured']['text'] and '0 个'in by['U02-unconfigured']['text'] and any(b['action']=='auth62-refresh'for b in by['U02-unconfigured']['buttons'])))
check('09 独立代驾通过画板与可用按钮一致',lambda:need('审核已通过'in by['D05-passed']['text']and any(b['action']=='auth62-role'and not b['disabled']for b in by['D05-passed']['buttons'])))
check('10 我的页面按审核状态显示申请或进度',lambda:need('申请成为代驾'in by['O25']['text']and '查看代驾审核进度'in by['O25-pending']['text']and '代驾资格已通过'in by['O25-approved']['text']))
check('11 后台未关联人员预授权无需OpenID表单',lambda:need('未关联微信'in by['B26-unlinked']['text']and not any('openid'in f.lower()for f in by['B26-unlinked']['fields'])))
check('12 当前所有按钮无旧员工凭证动作',lambda:need(not any(b['action']in ['bind-submit','binding-approve','binding-reject','v5-bind-request','v5-bind-issue','v5-bind-consume','v5-bind-revoke']for x in records for b in x['buttons'])))
check('13 两类工作我的页没有硬编码其他人姓名',lambda:need('林工'in by['S15']['text']and '周师傅'in by['D27']['text']and '林晓'not in by['D27']['text']))
check('14 主PRD直接写明车主/代驾/员工获得方式',lambda:need(all(s in prd for s in ['默认','审核通过','自动','后台','首次微信登录'])))
check('15 主PRD F03图注没有旧凭证正向链',lambda:need('管理员签发不代表已经绑定；仅本人消费成功才建立关联'not in prd and '后台直接预配置，不签发或消费员工绑定凭证'in prd))
check('16 主PRD不再宣称M0五表必已存在',lambda:need('已存在的M0代码和五张基座表则是真实工程产物'not in prd))
check('17 INT73为自动关联而不是旧申请链',lambda:need('本人请求→后台签发→本人消费'not in read('03_交互与设计规范/02_交互设计规范.md')))
check('18 IT02采用新自动关联联调流程',lambda:need('申请→签发→本人消费'not in read('13_真实集成测试/02_全功能联调场景.md')))
check('19 重建说明的权威版本是V6.4',lambda:need('当前唯一需求为V6.4'in read('02_UI/开发与重建说明.md')))
check('20 当前PRD Markdown生成的HTML有相同可见条款',lambda:need(all(s in BeautifulSoup(read('01_PRD/机动车检测预约_PRD_V6.4_完整整合版.html'),'html.parser').get_text() for s in ['手机号','自动关联','首次微信登录','B25','审核通过'])))
check('21 每张画板有首屏及完整图',lambda:need(all((R/'02_UI/全量页面图'/kind/(x['id']+'.png')).is_file()for x in cat for kind in ['首屏','完整内容'])))
check('22 图库中仅有当前画板',lambda:need({p.stem for p in (R/'02_UI/全量页面图/首屏').glob('*.png')}==set(by)))
check('23 逐画板说明与索引ID集合一致',lambda:need(set(re.findall(r'^## ([A-Z]\d+[-\w]*) ·',read('03_交互与设计规范/03_逐画板交互说明.md'),re.M))==set(by)))
check('24 单文件内嵌索引与机器索引相同',lambda:need(json.loads(re.search(r'window.UI_CATALOG=(\[.*?\]);\n',read('02_UI/index.html'),re.S)[1])==cat))
check('25 任务中画板/用例无遗漏或重复主责任',lambda:need(sum(len(t['page_ids'])for t in tasks)==len(cat) and sum(len(t['case_ids'])for t in tasks)==len(cases)))
def packetcheck():
 for t in tasks:
  md=read('12_工程执行/task_packets/'+t['id']+'/TASK.md');need('画板：'+(', '.join(t['page_ids'])or'无')in md,t['id']);need(j('12_工程执行/task_packets/'+t['id']+'/acceptance.json')==[c for c in cases if c['id']in t['case_ids']],t['id']+' cases')
check('26 每个任务说明和验收片段与主JSON一致',packetcheck)
check('27 真实业务用例仍全部NOT_RUN',lambda:need(all(c['status']=='NOT_RUN'for c in cases)))
# Link target policy is checked by check_package; source HTML regenerated equality for all active md.
check('28 PRD图片确实嵌入当前身份与后台页面',lambda:need(all((R/'09_验证记录'/f).exists()and f in prd for f in ['身份主流程_实图.png','后台员工授权_实图.png'])))
check('29 原型所有修改身份渲染器只有一份权威定义',lambda:need(all(sum(len(re.findall(r'\b[MR]\.'+id+r'\s*=',p.read_text()))for p in (R/'02_UI/src').glob('*.js'))==1 for id in ['O29','O25','U01','U02','D01','D05','D27','S01','S15','B25','B26','B36'])))
check('30 原型可见内容不再有保险/钱包/提现/检测分步骤操作',lambda:need(not any(b['text'].strip()in ['申请提现','我的钱包','一键报案','暂停检测','开始检测','结束检测','保险安全与客服']for x in records for b in x['buttons'])))
report={'kind':'CURRENT_CROSS_ARTIFACT_AND_RENDER_CHECK','scope':'Full file/index linkage and visible local prototype semantics; not proof of real services or every UI branch','passed':sum(x['result']=='PASS'for x in checks),'failed':sum(x['result']=='FAIL'for x in checks),'checks':checks,'catalogCount':len(cat),'pageErrors':errors}
(O/'V6.4_跨文件及可见内容核验.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(json.dumps(report,ensure_ascii=False,indent=2))
raise SystemExit(bool(report['failed']))
