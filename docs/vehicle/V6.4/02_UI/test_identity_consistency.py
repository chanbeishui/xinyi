"""Actual prototype DOM/model integration only; not backend or WeChat tests."""
from pathlib import Path
from playwright.sync_api import sync_playwright
import json, os, shutil
R=Path(__file__).resolve().parent
if not (R/'index.html').exists(): R=Path('/mnt/data/v64_work/root.txt').read_text().strip();R=Path(R)/'02_UI'
OUT=R.parent/'09_验证记录';OUT.mkdir(exist_ok=True)
checks=[]; errors=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path=os.environ.get('CHROMIUM_PATH') or shutil.which('chromium'),headless=True,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':1440,'height':1050});page.on('pageerror',lambda e:errors.append(str(e)));page.set_default_timeout(4500)
 page.set_content((R/'index.html').read_text(),wait_until='load')
 def ev(s):return page.evaluate(s)
 def render(i):page.evaluate('(id)=>UI.testing.render(id)',i)
 def reset(i='O29'):
  ev("UI.store=UI.makeStore();UI.closeDialog();document.body.className='export'");render(i)
 def click(a,payload=None):
  q='[data-action="'+a+'"]'+('' if payload is None else '[data-payload="'+payload+'"]')
  page.locator(q).first.click();page.wait_for_timeout(60)
 def go(i):
  page.locator('[data-go="'+i+'"]').first.click();page.wait_for_timeout(60)
 def fill(k,v):page.locator('[data-field="'+k+'"]').fill(v)
 def tick(k,v=True):page.locator('[data-field="'+k+'"]').set_checked(v)
 def authorize(s='owner',fresh=True):
  if fresh:reset('O29' if s=='owner' else 'O29-'+s)
  else:render('O29' if s=='owner' else 'O29-'+s)
  tick('login.agree');click('auth62-login');click('auth62-authorize')
 def text():return page.locator('[data-screen]').inner_text()
 def roles():return ev('UI.roles62()')
 def check(n,fn):
  try:fn();checks.append({'name':n,'result':'PASS'})
  except Exception as e:checks.append({'name':n,'result':'FAIL','error':str(e)[:1200]})
 def title():
  assert 'V6.4' in page.title();assert ev('UI.currentId()')=='O29';assert '绑定申请' not in text()
 check('01 首次打开为统一登录且浏览器版本为V6.4',title)
 def consent():
  reset();assert page.locator('[data-action="auth62-login"]').is_disabled();tick('login.agree');assert page.locator('[data-action="auth62-login"]').is_enabled()
 check('02 登录协议未同意不可触发授权',consent)
 def owner():
  authorize();assert roles()==['O'];assert ev('UI.store.role')=='O';assert ev('UI.currentId()')=='O03';render('U01');assert page.locator('[data-action="auth62-role"]').count()==1
 check('03 新账号只获得车主且主动进入车主首页',owner)
 def mine():
  authorize();render('O25');assert '申请成为代驾' in text();go('D01');assert '使用当前车主账号申请' in text();click('identity-start-driver');assert ev('UI.currentId()')=='D02';assert roles()==['O']
 check('04 我的申请入口可进入资料填写但不授予代驾',mine)
 def pending():
  authorize();ev("UI.auth62().driverStatus='PENDING';UI.sync62()");render('O25');assert '查看代驾审核进度' in text() and '申请成为代驾' not in text();go('D05');click('identity-refresh-driver');assert roles()==['O']
 check('05 审核中的我的页面与审核进度一致，刷新不能批准',pending)
 def approve():
  authorize();ev("UI.auth62().driverStatus='PENDING';UI.sync62()");render('B16-review');fill('qualification.opinion','核对全部必要资格，演示批准');fill('qualification.reviewDate','2027-10-22');tick('qualification.checked');click('qualification-approve');render('D05');assert '审核通过' in text();click('auth62-role','D');assert ev('UI.store.role')=='D';render('O25');assert '代驾资格已通过' in text() and '申请成为代驾' not in text()
 check('06 后台审核后本人的进度/我的/身份切换同步',approve)
 def relog():
  authorize('driver');render('O28');click('logout');click('identity-logout-confirm');assert not ev('UI.auth62().loggedIn');tick('login.agree');click('auth62-login');click('auth62-authorize');assert 'D' in roles();assert ev('UI.store.role')=='O'
 check('07 已批准代驾退出再登录保留资格，新会话默认车主',relog)
 def employee():
  authorize('staff');assert roles()==['O','S'] and ev('UI.store.role')=='O';render('U01');click('auth62-role','S');assert ev('UI.currentId()')=='S02';assert not ev('UI.auth62().driverApproved')
 check('08 员工唯一匹配自动关联，不免代驾审核',employee)
 def no_site():
  authorize('unconfigured');render('U02');assert '已自动关联' in text() and '0 个' in text();assert '尚未授权' not in text();assert page.locator('[data-action="auth62-refresh"]').count()==1;assert page.locator('[data-action="auth62-reverify"]').count()==0
 check('09 已关联但无站点只需后台配置和刷新，不要求重绑',no_site)
 def dropdown():
  reset('B26');assert page.locator('[data-field="identity.grant.12.ST01"]').is_checked();page.locator('[data-field="identity.employee"]').select_option('13');assert page.locator('[data-field="identity.grant.13.ST01"]').count()==1;assert not page.locator('[data-field="identity.grant.13.ST01"]').is_checked();assert page.locator('[data-field="identity.grant.12.ST01"]').count()==0
 check('10 B26切换员工时真实刷新对应站点选项',dropdown)
 def b25():
  reset('B25');buttons=page.locator('[data-screen] button').all_inner_texts();assert not any(x in buttons for x in ['确认绑定','员工绑定申请','签发凭证']);click('identity-open-grants','13');assert page.locator('[data-field="identity.employee"]').input_value()=='13'
 check('11 B25仅配置站点/关联详情，点击携带对应员工',b25)
 def pregrant():
  reset('B26-unlinked');assert ev('UI.auth62().employees[1].linkedAccount') is None;tick('identity.grant.13.ST01');fill('identity.grantReason','预先配置未登录员工');click('auth62-save-grants');assert ev('UI.auth62().employees[1].grants')==['ST01'];authorize('unconfigured',False);assert roles()==['O','S'];assert ev('UI.auth62().employeeId')=='13'
 check('12 未微信登录员工先配置站点，首次授权后即可切工作',pregrant)
 def postgrant():
  authorize('unconfigured');render('B26-unlinked');tick('identity.grant.13.ST02');fill('identity.grantReason','登录后补授站点');click('auth62-save-grants');render('S01');click('auth62-refresh');assert roles()==['O','S'];assert ev('UI.store.authorized')==['ST02']
 check('13 后台后补站点后刷新生效，不用重新申请或授权手机',postgrant)
 def duplicate():
  authorize('duplicate');assert roles()==['O'];render('U02');assert '需管理员核实' in text();assert '林工' not in text() and '陈工' not in text();assert ev('UI.auth62().phoneVerified')
 check('14 重号不泄候选员工，不阻断正常车主登录',duplicate)
 def conflict():
  authorize('conflict');assert roles()==['O'];assert ev('UI.auth62().link')=='NEEDS_ADMIN'
 check('15 关联冲突不授予检测身份',conflict)
 def suspend():
  authorize('staff');render('B36-suspend');fill('identity.adminReason','暂停以核实手机号');click('auth62-manage','suspend');assert ev('UI.auth62().link')=='SUSPENDED';assert roles()==['O'];authorize('staff',False);assert ev('UI.auth62().link')=='SUSPENDED';assert roles()==['O']
 check('16 暂停后重新手机号授权不能恢复工作能力',suspend)
 def unblock():
  authorize('staff');render('B36-revoke');fill('identity.adminReason','解除旧关联');click('auth62-manage','revoke');authorize('staff',False);assert roles()==['O'];render('B36-rematch');fill('identity.adminReason','允许核验本人');click('auth62-manage','rematch');assert roles()==['O'];authorize('staff',False);assert roles()==['O','S']
 check('17 解除不能自动重绑，后台允许重核不直接恢复，须本人新授权',unblock)
 def management_target():
  authorize('staff');render('B36');page.locator('[data-field="identity.manageEmployee"]').select_option('13');assert page.locator('[data-action="identity-management-dialog"][data-payload="suspend"]').is_disabled();click('identity-open-grants','13');assert page.locator('[data-field="identity.employee"]').input_value()=='13'
 check('18 B36切换员工同步状态和禁用动作，不处理其他员工',management_target)
 def no_bound():
  reset('B36-unlinked');assert all(page.locator('[data-action="identity-management-dialog"][data-payload="'+x+'"]').is_disabled() for x in ['suspend','revoke','rematch'])
 check('19 未关联员工不能执行暂停/解除/重核',no_bound)
 def cas():
  reset('B26-conflict');fill('identity.grantReason','并发测试');tick('identity.grant.12.ST01',False);click('auth62-save-grants');assert '其他人修改' in page.locator('#toast-root').inner_text();assert 'ST01' in ev('UI.auth62().employees[0].grants')
 check('20 站点授权版本冲突拒绝覆盖，保留原授权',cas)
 def revoke_all():
  authorize('staff');ev("UI.store.role='S'");render('B26');click('identity-clear-grants');fill('identity.grantReason','回收全部站点');click('auth62-save-grants');assert roles()==['O'];assert ev('UI.store.authorized')==[];render('S01');assert page.locator('[data-action="auth62-enter-station"]').is_disabled()
 check('21 撤回全部站点立即失去工作范围，仍有车主',revoke_all)
 def busy():
  reset('O29-processing');assert page.locator('[data-action="auth62-login"]').is_disabled();reset('O29-error');assert '核验暂未完成' in text();reset('U02-unknown');assert '核验结果待确认' in text();assert not ev('UI.auth62().phoneVerified');assert roles()==['O']
 check('22 处理中/失败/未知各有状态，不伪造身份成功',busy)
 def deny():
  reset();tick('login.agree');click('auth62-login');click('auth62-deny');assert not ev('UI.auth62().loggedIn');assert not ev('UI.auth62().phoneVerified')
 check('23 拒绝手机号授权不假成功',deny)
 def consistency():
  for id,needle in [('U02-linked','2 个'),('U02-unconfigured','0 个'),('D05-passed','审核已通过'),('O25-approved','代驾资格已通过'),('U01-staff','检测工作人员')]:
   reset(id);assert needle in text();assert not ('已自动关联' in text() and '尚未授权' in text())
 check('24 独立状态画板文字、按钮与演示模型一致',consistency)
 def old_actions():
  authorize();ev("UI.action('v5-bind-consume')");assert ev('UI.auth62().link')=='UNLINKED';assert roles()==['O']
 check('25 旧凭证动作无法再建立关联',old_actions)
 def occupant():
  authorize('staff');before=ev('JSON.stringify(UI.store.live)');render('U01');click('auth62-role','S');render('O28');click('logout');click('identity-logout-confirm');assert before==ev('JSON.stringify(UI.store.live)');assert not ev('UI.auth62().loggedIn');assert ev('UI.store.authorized')==[]
 check('26 切角色和退出不修改订单/释放司机，退出清空会话工作范围',occupant)
 def unapproved():
  authorize();ev("UI.action('auth62-role','D')");assert ev('UI.store.role')=='O';assert roles()==['O']
 check('27 未审核直接调用切代驾动作被拒绝',unapproved)
 def search():
  reset('B25');fill('identity.query','陈工');click('identity-search');assert page.locator('tbody tr').count()==1;click('identity-open-link','13');assert page.locator('[data-field="identity.manageEmployee"]').input_value()=='13'
 check('28 B25查询与关联详情定位同一员工',search)
 def work_profiles():
  authorize('staff');render('S15');assert '林工' in text() and '林晓' not in text();assert '车主 · 检测工作' in text();authorize('driver');render('D27');assert '周师傅' in text() and '林晓' not in text();assert '检测工作' not in text()
 check('29 两工作身份的我的页读取同一账号，不固定他人姓名和身份',work_profiles)
 def evidence():
  for id in ['O29','O25','O25-pending','O25-approved','U01','U01-staff','U02-linked','U02-unconfigured','D01','D05-passed','B25','B26-unlinked','B36']:
   reset(id);page.set_viewport_size({'width':390 if id[0]!='B' else 1440,'height':844 if id[0]!='B' else 1050});ev("document.getElementById('toast-root').innerHTML=''");page.screenshot(path=str(OUT/('身份实测_'+id+'.png')),animations='disabled')
 check('30 修改后关键页面取证截图',evidence)
 out={'scope':'PROTOTYPE_DOM_AND_LOCAL_STATE_ONLY','environment':'Playwright/Chromium; self-contained HTML set_content; Browser connector read-only, no control runtime','checks':checks,'passed':sum(x['result']=='PASS' for x in checks),'failed':sum(x['result']=='FAIL' for x in checks),'pageErrors':errors,'notRun':['real xinyi','MySQL','WeChat phone authorization','production auth and concurrent transactions']}
 (OUT/'V6.4_身份一致性实测.json').write_text(json.dumps(out,ensure_ascii=False,indent=2));print(json.dumps(out,ensure_ascii=False,indent=2));b.close()
 if out['failed'] or errors:raise SystemExit(1)
