from pathlib import Path
import json,shutil
from playwright.sync_api import sync_playwright
R=Path(__file__).parent;result=[]
with sync_playwright()as p:
 b=p.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':1440,'height':1050});errs=[];page.on('pageerror',lambda e:errs.append(str(e)))
 page.set_content((R/'index.html').read_text(),wait_until='load');page.set_default_timeout(4000)
 def ev(x):return page.evaluate(x)
 def render(id):page.evaluate('(id)=>UI.testing.render(id)',id);page.wait_for_timeout(40)
 def reset(id):ev("UI.store=UI.makeStore();document.body.className='export';UI.closeDialog()");render(id)
 def click(a):page.locator('[data-action="'+a+'"]').first.click();page.wait_for_timeout(90)
 def fill(k,v):page.locator('[data-field="'+k+'"]').fill(str(v))
 def sel(k,v):page.locator('[data-field="'+k+'"]').select_option(v)
 def expect(x,msg='condition'):assert x,msg
 def run(n,fn):
  try:fn();result.append({'name':n,'result':'PASS'});print('PASS',n,flush=True)
  except Exception as e:result.append({'name':n,'result':'FAIL','reason':str(e)});print('FAIL',n,str(e)[:300],flush=True)
 def training():
  reset('D40');click('v5-training-read');click('v5-attempt-start');expect(ev('!!UI.v5().attempt.id'));sel('v5.answer','RETURN');click('v5-attempt-submit');expect(ev('UI.currentId()')=='D42');expect(ev('UI.v5().passed'));page.screenshot(path=str(R.parent/'09_验证记录/培训_结果.png'),full_page=False)
 run('N01 学习→尝试→答案→结果',training)
 def notready():
  reset('D40');expect(page.locator('[data-action="v5-attempt-start"]').is_disabled());ev("UI.action('v5-attempt-start')");expect(ev('UI.v5().attempt===null'))
 run('N02 未学习不能创建尝试',notready)
 def wrong():
  reset('D40');click('v5-training-read');click('v5-attempt-start');sel('v5.answer','CONFIRM');click('v5-attempt-submit');expect(not ev('UI.v5().passed'));expect(ev('UI.v5().attempt.submitted'))
 run('N03 未通过保留尝试结果',wrong)
 def appeal():
  reset('D45');fill('v5.appealReason','请核实本次车主联系情况');click('v5-appeal-submit');expect(ev('UI.currentId()')=='D44');expect(ev("UI.v5().appeal.id==='APPEAL-DEMO-001'"));render('B45');fill('v5.appealOpinion','核对后通过，原限制记录保留并撤销。');click('v5-appeal-approve');render('D44');expect('已通过'in page.locator('[data-screen]').inner_text());page.screenshot(path=str(R.parent/'09_验证记录/本人申诉_公开处理结果.png'))
 run('N04 本人申诉→后台处理→本人读取',appeal)
 def appealdup():
  reset('D45');fill('v5.appealReason','请核实');click('v5-appeal-submit');old=ev('UI.v5().appeal.id');ev("UI.action('v5-appeal-submit')");expect(ev('UI.v5().appeal.id')==old)
 run('N05 同一待审申诉不重复创建',appealdup)
 def face():
  reset('D46');click('v5-face-start');expect(ev("UI.v5().faceStatus==='PENDING'"));click('v5-face-query');expect(ev("UI.v5().faceStatus==='DEMO_PASSED'"));expect('模拟'in page.locator('[data-screen]').inner_text()or'演示'in page.locator('[data-screen]').inner_text())
 run('N09 人脸业务会话与恢复查询明确模拟',face)
 def course():
  reset('B44');click('v5-course-save');expect(ev('UI.currentId()')=='B43');expect(not ev('UI.v5().coursePublished'));render('B44');click('v5-course-publish');expect(ev('UI.v5().coursePublished'))
 run('N10 管理课程草稿和发布反馈',course)
 def responsive():
  reset('D40');click('v5-training-read');click('v5-attempt-start')
  for w in [375,390,428]:
   page.set_viewport_size({'width':w,'height':900});render('D41');n=page.locator('.mobile-body').evaluate('x=>x.scrollWidth-x.clientWidth');expect(n<=1,'mobile overflow '+str(w))
  page.screenshot(path=str(R.parent/'09_验证记录/培训_答题移动端.png'))
 run('N11 新答题界面375/390/428宽度',responsive)
 def localfile():
  p2=b.new_page();e=[];p2.on('pageerror',lambda x:e.append(str(x)))
  try:
   p2.goto((R/'index.html').as_uri(),wait_until='load',timeout=8000)
   expect(len(p2.locator('[data-screen]').inner_text())>30);expect(not e,'file console errors');result.append({'name':'N12 file URI入口','result':'PASS','url':'file://.../02_UI/index.html'})
  except Exception as ex:result.append({'name':'N12 file URI入口','result':'BLOCKED_ENVIRONMENT','reason':str(ex)[:350]})
  finally:p2.close()
 localfile()
 report={'scope':'Local HTML UI simulation only; does not verify real identities, suppliers, payments, MySQL or Spring authorization','environment':'Chromium/Playwright; Browser runtime skill not listed','pass':sum(x['result']=='PASS'for x in result),'fail':sum(x['result']=='FAIL'for x in result),'blocked':sum(x['result'].startswith('BLOCKED')for x in result),'tests':result,'pageErrors':errs}
 (R.parent/'09_验证记录/V6.4_培训申诉能力回归.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(report['pass'],report['fail'],report['blocked'],errs);b.close()
