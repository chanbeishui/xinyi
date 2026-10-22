from pathlib import Path
from playwright.sync_api import sync_playwright
import json,shutil
root=Path(__file__).parent
results=[];errors=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox','--disable-dev-shm-usage'])
 page=b.new_page(viewport={'width':1440,'height':1100});page.on('pageerror',lambda e:errors.append(str(e)));page.set_content((root/'index.html').read_text(),wait_until='load');page.set_default_timeout(5000)
 def ev(s):return page.evaluate(s)
 def reset(id='B38'):
  ev("UI.store=UI.makeStore();UI.closeDialog();document.body.className='export'")
  page.evaluate('(id)=>UI.testing.render(id)',id)
 def click(a,p=None):
  sel=f'[data-action="{a}"]'+(f'[data-payload="{p}"]' if p is not None else '')
  page.locator(sel).first.click();page.wait_for_timeout(40)
 def fill(k,v):page.locator(f'[data-field="{k}"]').fill(v)
 def check(k):page.locator(f'[data-field="{k}"]').check()
 def ok(c,m='断言不成立'):
  if not c:raise AssertionError(m)
 def run(name,fn,mode='LOCAL_MODEL_GUARD'):
  try:fn();results.append({'name':name,'result':'PASS','mode':mode});print('PASS',name,flush=True)
  except Exception as e:results.append({'name':name,'result':'FAIL','mode':mode,'reason':str(e)});print('FAIL',name,str(e)[:200],flush=True)
 def model(s):return ev('(()=>{const A=UI.recon41;'+s+'})()')
 def t1():
  reset('B38-unpriced');ok(ev('UI.statement().amount') is None);click('v41-price-open','LE007');fill('r41.amount','30.00');fill('r41.reason','按本次取消返还服务的约定核定30元');click('v41-price-save');ok(ev('UI.statement().amount')==3000);ok(ev('UI.v4().ledger.find(x=>x.id==="LE007").base') is None,'覆盖了原源数据');click('v41-review-submit');ok(ev('UI.statement().status')=='INTERNAL_REVIEW');fill('r41.reviewReason','核对全部人工价格与约定');check('r41.reviewChecked');click('v41-review-approve');ok(ev('UI.statement().status')=='INTERNAL_REVIEW','本人复核被放行');ev("UI.action('v41-actor','ADMIN02')");click('v41-review-approve');ok(ev('UI.statement().status')=='SENT');ev("UI.v4().driverUser='D030';UI.testing.render('D38')");check('r41.driverChecked');click('v41-confirm-open');click('v41-confirm-commit');ok(ev('UI.statement().status')=='CONFIRMED');ok(ev('UI.confirmedPaid(UI.statement().id)')==0)
 run('P01 待核价草稿→行内填30元→整单拒绝自审→独立复核→本人确认',t1,'DOM_FLOW')
 def t2():
  reset();r=model("UI.v4().ledger[0].base=6000;UI.v4().ledger[0].mileage=0;const st=A.create('D012','E001',['LE001','LE002','LE003','LE004','LE005']);const id=st.entries[0].id;A.openEdit(id);A.savePrice('70','首次核对');A.openEdit(id);A.savePrice('65','再次核对');return [st.entries[0].proposed,st.entries[0].priceHistory.length,UI.v4().ledger[0].base];")
  ok(r==[6500,2,6000],str(r))
 run('P02 60→70→65保存最终65而非累加差额',t2)
 def t3():
  reset('B38-price');fill('r41.amount','70.001');fill('r41.reason','精度测试');click('v41-price-save');ok(ev('UI.statement().entries[0].manual') is False)
 run('P03 超过两位小数拒绝且保留输入',t3,'DOM_FLOW')
 def t4():
  reset('B38-price');fill('r41.amount','-1');fill('r41.reason','负数不能修改普通服务');click('v41-price-save');ok(ev('UI.statement().entries[0].manual') is False)
 run('P04 普通服务行拟价格不可负数',t4,'DOM_FLOW')
 def t5():
  reset('B38-price-empty');fill('r41.amount','0.00');fill('r41.reason','明确约定此类接车前解除为0元');click('v41-price-save');ok(ev('UI.statement().amount')==0);ok(ev('UI.statement().unpriced')==0)
 run('P05 明确0元与待核价NULL严格区分',t5,'DOM_FLOW')
 def t6():
  reset('B38-price');fill('r41.amount','70');click('v41-price-save');ok(ev('UI.statement().entries[0].manual') is False);ok(page.locator('[data-field="r41.amount"]').input_value()=='70')
 run('P06 原因必填，校验失败不清输入',t6,'DOM_FLOW')
 def t7():
  reset('B38-price-conflict');fill('r41.amount','70');fill('r41.reason','并发版本验证');click('v41-price-save');ok(ev('UI.statement().entries[0].manual') is False);ok('已被其他人修改' in page.locator('#toast-root').inner_text())
 run('P07 模拟外部版本变更后保存被拒绝',t7,'DOM_FLOW')
 def t8():
  reset();r=model("const st=A.create('D012','E001',['LE001','LE002','LE003','LE004','LE005']);A.openEdit(st.entries[0].id);A.savePrice('70','补偿');return [A.send(),st.status];");ok(r==[False,'DRAFT'])
 run('P08 人工改价不能绕过review直接send',t8)
 def t9():
  reset();r=model("const st=A.create('D012','E001',['LE001','LE002','LE003','LE004','LE005']);A.openEdit(st.entries[0].id);A.savePrice('70','A改价');UI.store.v41.actor='ADMIN02';A.openEdit(st.entries[1].id);A.savePrice('80','B改价');A.submitReview();UI.store.v41.actor='ADMIN01';const rejected=A.review('APPROVE','复核',st.version);return [rejected,st.status,st.reviewHistory.length];");ok(r==[False,'INTERNAL_REVIEW',0])
 run('P09 任一改价参与者都不能复核整单',t9)
 def t10():
  reset('B38-review');r=model("const st=A.current();const q=A.review('REJECT','请补充依据',st.version);return[q,st.status,st.reviewHistory[0].decision];");ok(r==[True,'DRAFT','REJECT'])
 run('P10 复核退回原稿并留审查历史',t10)
 def t11():
  reset('B38-sent');r=model("const st=A.current(),v=st.version,h=st.sentHash;A.withdraw(v,'需要修改');return[A.confirm(v,h),st.status,st.snapshots.length];");ok(r==[False,'DRAFT',1])
 run('P11 撤回先成功后旧确认失效，发布快照仍保留',t11)
 def t12():
  reset('B38-sent');r=model("const st=A.current(),v=st.version,h=st.sentHash;A.confirm(v,h);return[A.withdraw(v,'来晚的撤回'),st.status];");ok(r==[False,'CONFIRMED'])
 run('P12 确认先成功后撤回拒绝',t12)
 def t13():
  reset('B38-review');r=model("const st=A.current();A.review('APPROVE','核实',st.version);const v=st.version,h=st.sentHash;A.confirm(v,h);const keys=JSON.stringify(st.finalizationKeys);const twice=A.confirm(v,h);return[twice,keys===JSON.stringify(st.finalizationKeys),st.status];");ok(r==[True,True,'CONFIRMED'])
 run('P13 相同确认重试不追加第二组结转键（本地模型）',t13)
 def t14():
  reset('B38-unpriced');ev("UI.v4().driverUser='D030';UI.testing.render('D37')");ok(page.locator('[data-action="v4-open-statement"]').count()==0)
 run('P14 司机列表不泄露平台整理中草稿',t14,'DOM_FLOW')
 def t15():
  reset('B38-confirmed');old=ev('UI.statement().amount');click('v41-price-open','LE001');ok('申请后续调差' in page.locator('.pc-drawer').inner_text());fill('r41.delta','5.00');fill('r41.adjustReason','已确认后发现5元差额，请单独核实');click('v41-adjust-submit');ok(ev('UI.statement().amount')==old);ok(ev('UI.v4().adjustments.at(-1).amount')==500)
 run('P15 已确认行内改为调差弹窗，原账不改',t15,'DOM_FLOW')
 def t16():
  reset('B38-price');r=model("const st=A.current();UI.v4().payments.push({statementId:st.id,status:'PENDING',amount:100});return[A.openEdit(st.entries[0].id),A.canEdit(st)];");ok(r==['ADJUSTMENT',False])
 run('P16 任意已登记付款记录锁原账，未核实也不能改价',t16)
 def t17():
  reset('B38-price');r=model("const st=A.current();UI.store.v41.permissions.edit=false;return A.openEdit(st.entries[0].id);");ok(r is False)
 run('P17 无改价权限不开放模型保存',t17)
 def t18():
  reset('B38-price');before=ev('JSON.stringify(UI.store.live)');fill('r41.amount','70');fill('r41.reason','代驾报酬核价与客户金额无关');click('v41-price-save');ok(ev('JSON.stringify(UI.store.live)')==before)
 run('P18 改司机拟价不影响客户订单和司机占用',t18,'DOM_FLOW')
 def t19():
  reset('B38-price');fill('r41.amount','70');fill('r41.reason','测试取消不保存');click('v41-edit-cancel');click('v41-edit-discard');ok(ev('UI.statement().entries[0].manual') is False)
 run('P19 取消未保存弹窗不写改价历史',t19,'DOM_FLOW')
 def t20():
  reset('B38-difference');r=model("const st=A.current();const x=st.entries[0];A.openEdit(x.id);A.savePrice('70','按差异修正');const a=A.submitReview();st.resolveReason='已按差异核定';const b=A.submitReview();return[a,b,st.status];");ok(r==[False,True,'INTERNAL_REVIEW'])
 run('P20 差异说明与金额处理在同一账单完成',t20)
 def t21():
  reset('B38-price');fill('r41.amount','70');ev("UI.go('B28')");ok('未保存' in page.locator('#modal-root').inner_text());ok(ev('UI.currentId()')=='B38-price')
 run('P21 仅修改金额也会阻止未保存离开',t21,'DOM_FLOW')
 def t22():
  page.set_viewport_size({'width':1440,'height':900});reset('B38-price');dims=page.locator('.drawer-footer').bounding_box();ok(dims['y']+dims['height']<=901,'保存按钮超出首屏');ok(page.locator('.drawer-body').evaluate('(x)=>getComputedStyle(x).overflowY')=='auto');page.set_viewport_size({'width':1440,'height':1100})
 run('P22 短桌面视口内弹窗保存按钮可见且表单可滚动',t22,'DOM_FLOW')
 b.close()
report={'suite':'V5.0对账内核价新增交互','environment':'Chromium + Playwright / local self-contained HTML','browserPlugin':'absent; regular Playwright used','scope':'DOM_FLOW真实点击输入；LOCAL_MODEL_GUARD使用同一前端演示模型注入前置条件，不能证明后端授权、真实并发或入账','passed':sum(x['result']=='PASS' for x in results),'failed':sum(x['result']=='FAIL' for x in results),'pageErrors':errors,'tests':results}
(root.parent/'09_验证记录/V6.4_核价回归结果.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(report['passed'],report['failed'],errors)
