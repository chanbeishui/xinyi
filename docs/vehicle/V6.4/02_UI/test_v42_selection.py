"""Local prototype regression. No backend/database/WeChat calls are made."""
from pathlib import Path
from playwright.sync_api import sync_playwright
import json,shutil
root=Path(__file__).parent;out=root.parent/'09_验证记录';results=[];errors=[]
with sync_playwright() as p:
 browser=p.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox','--disable-dev-shm-usage'])
 page=browser.new_page(viewport={'width':1440,'height':1100});page.on('pageerror',lambda x:errors.append(str(x)));page.set_default_timeout(5000)
 page.set_content((root/'index.html').read_text(),wait_until='load')
 def ev(code):return page.evaluate(code)
 def reset(id='B42'):
  ev("UI.store=UI.makeStore();UI.closeDialog();document.body.className='export';UI.v4().dirty=false")
  page.evaluate('(id)=>UI.testing.render(id)',id);page.wait_for_timeout(30)
 def click(a,payload=None):
  sel=f'[data-action="{a}"]'+(f'[data-payload="{payload}"]' if payload is not None else '')
  page.locator(sel).first.click();page.wait_for_timeout(60)
 def field(k,v):page.locator(f'[data-field="{k}"]').fill(v)
 def check(k):page.locator(f'[data-field="{k}"]').check()
 def select(id):page.locator(f'[data-r42-select="{id}"]').check();page.wait_for_timeout(40)
 def assert_(x,msg='condition failed'):
  if not x:raise AssertionError(msg)
 def run(name,fn,mode='LOCAL_MODEL_GUARD'):
  try:fn();results.append(dict(name=name,mode=mode,result='PASS'));print('PASS',name,flush=True)
  except Exception as ex:results.append(dict(name=name,mode=mode,result='FAIL',reason=str(ex)));print('FAIL',name,str(ex)[:240],flush=True)
 def model(code):return ev('(()=>{const R=UI.recon42,A=UI.recon41,s=UI.v4();'+code+'})()')
 def t01():
  reset('B28');click('v4-driver-recon','D012');assert_(ev('UI.currentId()')=='B42');assert_(page.get_by_role('tab').count()==3);assert_(ev('UI.statement()') is None)
 run('S01 代驾对账入口进入三标签个人中心，不默认最近账单',t01,'DOM_FLOW')
 def t02():
  reset('B42-select');select('LE-H01');click('v42-page','2');select('LE-H02');assert_(ev('UI.recon42.state().selected')==['LE-H01','LE-H02']);click('v42-generate-open');click('v42-create');assert_(ev('UI.statement().entries.map(x=>x.id)')==['LE-H01','LE-H02']);assert_(ev('UI.statement().amount') is None);assert_(ev('UI.recon42.member("LE001")') is None)
 run('S02 跨页跨月只勾选两笔生成，其他同期间服务不补选',t02,'DOM_FLOW')
 def t03():
  reset('B42-select');select('LE-H01');field('r42.from','2026-10-01');click('v42-filter');assert_(ev('UI.recon42.state().selected')==['LE-H01']);click('v42-selected-open');assert_('ASG-H001' in page.locator('.pc-drawer').inner_text())
 run('S03 改日期筛选不丢已有选择，已选摘要显示隐藏项',t03,'DOM_FLOW')
 def t04():
  reset('B42-select');page.get_by_role('checkbox',name='全选本页可对账服务').check();assert_(len(ev('UI.recon42.state().selected'))==5);assert_(ev('UI.v4().statements.length')==4)
 run('S04 全选仅本页，浏览器勾选不占用费用',t04,'DOM_FLOW')
 def t05():
  reset('B42-select');select('LE001');page.locator('[data-field="r42.entity"]').select_option('E002');click('v42-entity-apply');click('v42-entity-cancel');assert_(ev('UI.recon42.state().entityId')=='E001');assert_(ev('UI.recon42.state().selected')==['LE001']);page.locator('[data-field="r42.entity"]').select_option('E002');click('v42-entity-apply');click('v42-entity-confirm');assert_(ev('UI.recon42.state().selected')==[]);assert_(ev('UI.recon42.state().entityId')=='E002')
 run('S05 跨付款主体切换确认后才清空选择，取消保持原选择',t05,'DOM_FLOW')
 def t06():
  reset();r=model("const before=s.statements.length;const st=A.create('D012','E001',['LE001','LE-E2']);return[st,s.statements.length===before,R.member('LE001')===null];");assert_(r==[False,True,True])
 run('S06 混入跨主体明细整个组单失败无部分占用',t06)
 def t07():
  reset();r=model("const before=s.statements.length;return [A.create('D012','E001',[]),A.create('D012','E001',['LE001','LE001']),A.create('D012','2026-10-01','2026-10-10'),s.statements.length===before];");assert_(r==[False,False,False,True])
 run('S07 空选择/重复ID/旧日期生成形式均拒绝',t07)
 def t08():
  reset();r=model("R.toggle('LE001',true);R.toggle('LE002',true);A.create('D012','E001',['LE001']);let before=s.statements.length;return[R.create(),s.statements.length===before,R.member('LE002')===null,R.state().conflicts[0].id];");assert_(r==[False,True,True,'LE001'])
 run('S08 另一个经办抢先占用一笔时，整个所选提交失败',t08)
 def t09():
  reset();r=model("R.toggle('LE001',true);R.entry('LE001').version++;return[R.create(),R.member('LE001')===null];");assert_(r==[False,True])
 run('S09 选择后源费用版本变化须重新确认',t09)
 def t10():
  reset();r=model("return[A.create('D012','E001',['LE-ACTIVE']),R.eligible(R.entry('LE-ACTIVE'))];");assert_(r==[False,False])
 run('S10 未结束承接不允许组单',t10)
 def t11():
  reset();r=model("const st=A.create('D012','E001',['LE001']);A.openEdit('LE001');A.savePrice('70','按约定补充');let v=st.version;R.remove('LE001',v,'本次暂缓');let history=st.removedLines[0].priceHistory.length;let eligible=R.eligible(R.entry('LE001'));R.add(['LE001'],{},st.version);return[history,eligible,st.entries.length,st.entries[0].proposed,st.entries[0].priceHistory.length,st.revision>1];");assert_(r==[1,True,1,7000,1,True])
 run('S11 草稿移出释放占用，再加入保留原行价格历史与版本',t11)
 def t12():
  reset();r=model("const st=A.create('D012','E001',['LE001']);R.remove('LE001',st.version,'暂缓');return[st.amount,A.send(),st.status];");assert_(r==[0,False,'DRAFT'])
 run('S12 可保留空草稿但不能发送空账单',t12)
 def t13():
  reset();r=model("const st=A.create('D012','E001',['LE001']);const original=st.entries[0].id;let result=R.void(st.version,'重新整理');return[result,st.status,R.eligible(R.entry('LE001')),st.entries[0].id===original,R.bills().some(x=>x.id===st.id)];");assert_(r==[True,'VOID',True,True,True])
 run('S13 作废草稿恢复服务可选且历史账单保留',t13)
 def t14():
  reset();r=model("s.statementId='DS-HISTORY-003';const st=A.current();return[R.add(['LE001'],{},st.version),R.remove(st.entries[0].id,st.version,'修改'),R.void(st.version,'作废'),st.status];");assert_(r==[False,False,False,'SENT'])
 run('S14 已发送不能绕撤回直接改成员或作废',t14)
 def t15():
  reset();r=model("s.statementId='DS-HISTORY-002';const st=A.current();return[R.remove(st.entries[0].id,st.version,'删除'),R.void(st.version,'作废'),st.entries.length];");assert_(r==[False,False,2])
 run('S15 已确认/存在付款凭证不得移出原账服务',t15)
 def t16():
  reset();r=model("const x=R.serviceRows();return['LE-PART1','LE-SENT','LE-DRAFT'].every(id=>x.some(y=>y.id===id))&&!x.some(y=>y.id==='LE-PAID1');");assert_(r)
 run('S16 未结清视图包括整理中/待确认/部分付款，排除已付清',t16)
 def t17():
  reset('B42-bills');assert_(page.locator('.data-table tbody tr').count()==4);assert_('DS-HISTORY-001' in page.locator('[data-screen]').inner_text());click('v42-bill-open','DS-HISTORY-002');assert_(ev('UI.statement().id')=='DS-HISTORY-002');assert_('¥70.00' in page.locator('.statement-payment').inner_text())
 run('S17 历次账单全部可查并打开指定账单非最后一张',t17,'DOM_FLOW')
 def t18():
  reset('B42-payments');assert_(page.locator('.data-table tbody tr').count()==3);click('v42-payment-view','PAY-HISTORY-1');assert_('PAY-HISTORY-1' in page.locator('.pc-drawer').inner_text());assert_('DS-HISTORY-001' in page.locator('.pc-drawer').inner_text())
 run('S18 从个人付款记录查看所点击的准确凭证和账单',t18,'DOM_FLOW')
 def t19():
  reset();r=model("const st=R.statement('DS-HISTORY-002'),p=R.payment(st);return[p.paid,p.pending,p.remaining,p.recordable,R.serviceState(R.entry('LE-PART1')).label,R.serviceState(R.entry('LE-PART2')).label];");assert_(r==[5000,2000,7000,5000,'所在账单部分付款','所在账单部分付款'])
 run('S19 部分付款不自动分配服务，待核实不计已付且预留登记额',t19)
 def t20():
  reset('B38-partial');click('v42-payment-open','DS-HISTORY-002');field('payment.amount','50.00');field('payment.reference','REAL-DOM-DEMO-ONLY');page.locator('#r42-proof').set_input_files({'name':'示例凭证.txt','mimeType':'text/plain','buffer':b'Local test, not a real payment.'});click('v4-payment-record');rec=ev('UI.recon42.state().recordId');assert_(ev('UI.confirmedPaid("DS-HISTORY-002")')==5000);ev("UI.store.v41.actor='ADMIN02'");page.locator('.pc-drawer [data-action="v42-payment-review"]').click();page.wait_for_timeout(40);field('payment.verifyReason','核对演示付款对象金额与流水');check('payment.verified');click('v4-payment-verify');assert_(ev('UI.confirmedPaid("DS-HISTORY-002")')==10000)
 run('S20 账单内自动关联→选择本地文件→登记→独立核实部分付款',t20,'DOM_FLOW')
 def t21():
  reset();r=model("const st=R.statement('DS-HISTORY-002');return [R.record(st.id,'50.01','X','2026-10-15',{name:'fixture'},st.version),R.paid(st),R.pending(st)];");assert_(r==[False,5000,2000])
 run('S21 超过扣除已登记待核实后的可登记余额被拒绝',t21)
 def t22():
  reset();r=model("UI.store.v41.actor='ADMIN02';const rec=s.payments.find(x=>x.id==='PAY-HISTORY-3');R.verify(rec.id,'APPROVE','核实',rec.version);const st=R.statement('DS-HISTORY-002');return[R.payment(st).paid,R.payment(st).pending,R.verify(rec.id,'APPROVE','重复核实',rec.version),R.payment(st).paid];");assert_(r==[7000,0,False,7000])
 run('S22 同一凭证重复核实不重复加已付',t22)
 def t23():
  reset();r=model("UI.store.v41.actor='ADMIN02';const rec=s.payments.find(x=>x.id==='PAY-HISTORY-3'),st=R.statement(rec.statementId);R.verify(rec.id,'REJECT','凭证不符',rec.version);return[rec.status,R.payment(st).paid,R.payment(st).pending,R.payment(st).recordable];");assert_(r==['REJECTED',5000,0,7000])
 run('S23 凭证不符保留历史、不计付款并释放登记预留',t23)
 def t24():
  reset('B38-zero');assert_(ev('UI.recon42.payment(UI.statement()).code')=='NO_PAYMENT_REQUIRED');assert_(page.locator('[data-action="v42-payment-open"]').count()==0);assert_(ev('UI.recon42.serviceState(UI.recon42.entry("LE005")).code')=='NO_PAYMENT_REQUIRED')
 run('S24 明确0元确认后无需付款凭证且服务不再未结清',t24,'DOM_FLOW')
 def t25():
  reset('D37-services');txt=page.locator('[data-screen]').inner_text();assert_('对账整理中' in txt);assert_('ADMIN01' not in txt);assert_('¥45.00' not in txt)
 run('S25 司机未结清服务可查，但不泄露草稿拟价和内部复核信息',t25,'DOM_FLOW')
 def t26():
  reset('D37-payments');txt=page.locator('[data-screen]').inner_text();assert_('¥100.00' in txt);assert_('BANK-HISTORY' not in txt);assert_('付款样本' not in txt)
 run('S26 司机可查本人付款摘要，不直接暴露原始流水/银行凭证',t26,'DOM_FLOW')
 def t27():
  reset();r=model("R.state().allowedEntities=[];return[R.bills().length,R.serviceRows().length,R.payments().length,A.create('D012','E001',['LE001'])];");assert_(r==[0,0,0,False])
 run('S27 主体撤权后查询失败关闭且不能组单',t27)
 def t28():
  reset();r=model("const st=A.create('D012','E001',['LE001']);const v=st.version;R.add(['LE002'],{},v);return[R.add(['LE003'],{},v),st.entries.map(x=>x.id),R.member('LE003')===null];");assert_(r==[False,['LE001','LE002'],True])
 run('S28 加入成员遇到账单旧版本，全部拒绝且不占用其他服务',t28)
 def t29():
  reset();r=model("const st=A.create('D012','E001',['LE001']);A.openEdit('LE001');A.savePrice('70','核价');A.submitReview();let v=st.version,h=st.reviewSnapshot.hash;A.withdraw(v,'需要补服务');R.add(['LE002'],{},st.version);return[st.reviewApprovedRevision,st.revision>st.reviewSnapshot.revision,A.hash(st)!==h];");assert_(r==[None,True,True])
 run('S29 撤回后调整成员使旧复核内容版本/hash失效',t29)
 def t30():
  reset('B42');click('v42-choose-services');select('LE001');click('v42-generate-open');click('v42-create');click('v42-add-open');select('LE002');click('v42-add-save');assert_(ev('UI.statement().entries.map(x=>x.id)')==['LE001','LE002']);click('v42-remove-open','LE002');field('r42.removeReason','本次暂缓第二条，下次单独处理');click('v42-remove-commit');assert_(ev('UI.statement().entries.map(x=>x.id)')==['LE001'])
 run('S30 草稿添加服务及行内移出为真实可点交互',t30,'DOM_FLOW')
 # Evidence, using normal rendering and a mobile viewport as well.
 for route,name in [('B42-selected','个人对账_勾选组单'),('B42-bills','个人对账_历次账单'),('B38-partial','对账单_部分付款'),('B40-record','对账单_付款凭证')]:
  reset(route);ev("document.getElementById('toast-root').innerHTML=''");page.screenshot(path=str(out/(name+'.png')),full_page=True)
 for w in (375,390,428):
  page.set_viewport_size({'width':w,'height':844});reset('D37-services');page.screenshot(path=str(out/f'本人未结清服务_{w}.png'),full_page=False)
  run(f'S{31+(w!=375)+(w==428):02d} 本人未结清服务{w}px无横向内容溢出',lambda:assert_(ev("document.querySelector('.mobile-body').scrollWidth<=document.querySelector('.mobile-body').clientWidth+1")),'DOM_RENDER')
 browser.close()
report={'suite':'V5.0明确勾选、历史、未结清与付款新增验证','environment':'Playwright + Chromium; self-contained HTML via set_content; no backend','browserPlugin':'absent; regular Playwright used','scope':'DOM_FLOW为真实点击/输入/本地文件选择；LOCAL_MODEL_GUARD为前端模型预置。均不是MySQL并发、安全或外部付款验证。','passed':sum(x['result']=='PASS' for x in results),'failed':sum(x['result']=='FAIL' for x in results),'pageErrors':errors,'tests':results}
(out/'V6.4_勾选组单回归结果.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(report['passed'],report['failed'],errors)
