from pathlib import Path
import json,shutil,traceback
from playwright.sync_api import sync_playwright
root=Path(__file__).parent
results=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox','--disable-dev-shm-usage'])
 page=b.new_page(viewport={'width':1440,'height':1100},accept_downloads=True)
 console=[];page.on('pageerror',lambda e:console.append(str(e)))
 page.set_content((root/'index.html').read_text(encoding='utf-8'),wait_until='load');page.set_default_timeout(4500)
 def ev(s):return page.evaluate(s)
 def reset(id='B28'):
  ev("UI.store=UI.makeStore();document.body.className='export';UI.closeDialog()")
  page.evaluate('(id)=>UI.testing.render(id)',id);page.wait_for_timeout(40)
 def render(id):page.evaluate('(id)=>UI.testing.render(id)',id);page.wait_for_timeout(40)
 def fill(key,v):page.locator(f'[data-field="{key}"]').fill(str(v))
 def check(key):page.locator(f'[data-field="{key}"]').check()
 def click(action,payload=None):
  sel=f'[data-action="{action}"]'+(f'[data-payload="{payload}"]' if payload is not None else '')
  page.locator(sel).first.click();page.wait_for_timeout(90)
 def run(name,fn):
  try:fn();results.append({'name':name,'result':'PASS'});print('PASS',name,flush=True)
  except Exception as exc:results.append({'name':name,'result':'FAIL','reason':str(exc)});print('FAIL',name,str(exc)[:300],flush=True)
 def expect(cond,msg):
  if not cond:raise AssertionError(msg)
 def prepare_review(kind='cancel',zero=False):
  reset('B14-refund' if kind=='refund' else 'B14-before' if kind=='before' else 'B14')
  ev("UI.store.flow='cancelReturn';UI.store.live.stage='INSPECTING';UI.store.live.custody='STATION'")
  fill('as.reason','已核对本次申请和当前车辆交接事实。');fill('as.amount','0.00' if zero else '42.00' if kind=='refund' else '280.00');check('as.checked')
  click('v4-case-review','APPROVE');click('v4-case-commit')
 def make_statement():
  reset();ev("UI.recon41.create('D012','E001',['LE001','LE002','LE003','LE004','LE005']);UI.testing.render('B38')")
  expect(ev('UI.statement()?.amount')==19000,'生成金额应为19000分')
 def confirmed_statement():
  make_statement();click('v41-send');render('D38');check('r41.driverChecked');click('v41-confirm-open');click('v41-confirm-commit')
 def t01():
  reset();click('v4-open-driver','D012');expect(ev('UI.driver().id')=='D012','未打开点击的代驾');expect(page.locator('.pc-drawer').inner_text().find('林晓')>=0,'详情身份错误')
 run('T01 从代驾列表进入点击对象的详情',t01)
 def t02():
  reset('B09');click('v4-open-vehicle','V002');expect(ev('UI.vehicle().id')=='V002','车辆身份错误');expect('粤K·5M9Q7' in page.locator('.pc-drawer').inner_text(),'车牌错误')
 run('T02 车辆详情保留实体ID，不默认打开第一条',t02)
 def t03():
  reset('B27');fill('dir.owners.text','陈女士');click('v4-search','owners');expect(page.locator('.data-table tbody tr').count()==1,'筛选未生效');click('v4-reset-search','owners');expect(page.locator('.data-table tbody tr').count()==5,'重置未恢复分页')
 run('T03 车主查询与重置',t03)
 def t04():
  reset('B28');click('v4-page','drivers|2');expect(page.locator('.data-table tbody tr').count()==1,'第二页应有1条')
 run('T04 列表分页更新实际行',t04)
 def t05():
  reset('B09-edit');fill('vehicle.edit.model','本田雅阁（档案修订）');fill('vehicle.edit.reason','按最新核验资料修正');click('v4-vehicle-save');expect(ev('UI.vehicle().model')=='本田雅阁（档案修订）','未保存');expect(ev('UI.demoOrder.model')=='本田雅阁','历史订单被追改')
 run('T05 车辆维护不追改历史订单快照',t05)
 def t06():
  reset('B27-edit');fill('owner.edit.remark','这是一份未提交的草稿');page.locator('[data-go="B27"]').last.click();page.wait_for_timeout(50);expect(page.locator('#modal-root').inner_text().find('保存修改')>=0,'未拦截未保存离开');click('close-dialog');expect(page.locator('[data-field="owner.edit.remark"]').input_value()=='这是一份未提交的草稿','草稿丢失')
 run('T06 离开编辑时提示，继续编辑保留草稿',t06)
 def t07():
  prepare_review();expect(ev('UI.caseFor().review')=='APPROVED','未批准');expect(ev('UI.caseFor().returnState')=='WAIT_RETURN','未进入待返还');expect(ev('UI.store.live.occupied') is True,'批准时提前释放司机');expect(ev('UI.caseFor().auditCount')==1,'重复审核')
 run('T07 已接车取消一次审批，待返还期间仍占用司机',t07)
 def t08():
  prepare_review();ev("UI.v4Action('v4-case-commit')");expect(ev('UI.caseFor().auditCount')==1,'重复提交增加审核次数')
 run('T08 重复统一审核不重复审批或预留',t08)
 def t09():
  reset('B14');fill('as.reason','检查金额');fill('as.amount','340.01');check('as.checked');click('v4-case-review','APPROVE');expect(ev('UI.caseFor().review')=='PENDING','超额仍然批准');expect('超过当前可退' in page.locator('#toast-root').inner_text(),'缺少超额反馈')
 run('T09 超额退款阻止提交并提示字段',t09)
 def t10():
  prepare_review(zero=True);expect(ev('UI.caseFor().refundState')=='NOT_REQUIRED','零额不应创建渠道退款');expect(ev('UI.caseFor().returnState')=='WAIT_RETURN','零退款仍需归还')
 run('T10 零元退款的取消仍需实际返还',t10)
 def t11():
  prepare_review('refund');expect(ev('UI.store.live.stage')=='INSPECTING','仅退款改变了履约');expect(ev('UI.store.live.occupied') is True,'仅退款释放了司机');expect(ev('UI.caseFor().refundState')=='READY','仅退款未准备执行')
 run('T11 仅退款不取消服务、不释放司机',t11)
 def t12():
  prepare_review();ev("UI.store.live.custody='DRIVER';UI.store.live.stage='CAR_TAKEN';UI.store.photos.return=['前','后','左','右','仪表盘','内饰'];UI.set('evidence.returnMileage','48230')")
  render('D23-cancel');fill('handover.returnCode','7248');check('handover.returnCar');check('handover.returnItems');click('handover-return','cancel');expect(ev('UI.store.live.occupied') is False,'实物归还未释放');expect(ev('UI.store.live.stage')=='CANCELLED','未正式取消');expect(ev('UI.caseFor().refundState')=='READY','返还后未执行原决定');expect(ev('UI.caseFor().auditCount')==1,'返还后增加第二次审批')
 run('T12 实际返还后正式取消并释放，退款无第二次审核',t12)
 def t13():
  prepare_review('refund');ev("UI.v4Action('v4-case-channel','SUCCESS')");amount=ev('UI.caseFor().refunded');ev("UI.v4Action('v4-case-channel','SUCCESS')");expect(ev('UI.caseFor().refunded')==amount,'成功回执重复记账')
 run('T13 模拟重复退款成功仅记账一次',t13)
 def t14():
  reset('O19');fill('as.applyReason','申请取消');fill('as.applyAmount','340.00');click('v4-case-apply','before');expect(ev('UI.v4().cases.length')==4,'重复申请生成新售后');expect(ev('UI.currentId()')=='O19-duplicate','未提示原处理中申请')
 run('T14 同单未办结申请不重复生成',t14)
 run('T15 正常费用与明确0元明细合计生成',make_statement)
 def t16():
  make_statement();count=ev('UI.v4().statements.length');ev("UI.recon41.create('D012','E001',['LE001'])");expect(ev('UI.v4().statements.length')==count,'同一费用重复纳入账单')
 run('T16 同一费用已占用，不重复纳入其他账单',t16)
 def t17():
  reset('B38-unpriced');expect(ev('UI.v4().statements.length')==1,'未核价草稿没有建立');expect(ev('UI.statement().amount') is None,'未知总额不能当0');ev("UI.action('v41-send')");expect(ev('UI.statement().status')=='DRAFT','未核价被发送')
 run('T17 待核价可进草稿，不能发出或当0元',t17)
 def t18():
  confirmed_statement();expect(ev('UI.statement().status')=='CONFIRMED','本人未确认成功');expect(ev('UI.confirmedPaid(UI.statement().id)')==0,'对账确认被当成付款')
 run('T18 本人确认账单不产生付款',t18)
 def t19():
  make_statement();click('v41-send');render('D38-difference');fill('r41.dispute','第二条服务的路线费用需要重新核对。');click('v41-dispute');expect(ev('UI.statement().status')=='DISPUTED','未进入差异处理');expect(ev('UI.statement().amount')==19000,'差异申请直接改了金额')
 run('T19 差异提交不直接修改账单金额',t19)
 def t20():
  make_statement();click('v41-send');render('D38');check('r41.driverChecked');ev('UI.statement().version++');click('v41-confirm-open');expect(ev('UI.statement().status')=='SENT','确认了旧版本')
 run('T20 账单版本变化阻止确认旧版',t20)
 def t21():
  make_statement();ev("UI.statement().driverId='D018'");render('D38');expect('当前版本不可确认' in page.locator('[data-screen]').inner_text(),'可查看他人账单')
 run('T21 代驾仅查看本人账单',t21)
 def t22():
  confirmed_statement();render('B40-record');fill('payment.amount','50.00');fill('payment.reference','BANKTEST-001');ev("UI.store.v42.proof={name:'测试凭证.txt',localDemo:true}");click('v4-payment-record');expect(ev('UI.confirmedPaid(UI.statement().id)')==0,'登记未复核就计为已付');ev("UI.store.v41.actor='ADMIN02'");render('B40-verify');fill('payment.verifyReason','独立核对付款凭证与收款人相符');check('payment.verified');click('v4-payment-verify');expect(ev('UI.confirmedPaid(UI.statement().id)')==5000,'部分付款核实金额错误')
 run('T22 外部部分付款登记及独立复核',t22)
 def t23():
  confirmed_statement();render('B40-record');fill('payment.amount','190.01');fill('payment.reference','OVER-001');ev("UI.store.v42.proof={name:'测试凭证.txt',localDemo:true}");click('v4-payment-record');expect(ev('UI.v4().payments.length')==0,'超额付款被正常录入')
 run('T23 超过已确认应付不能正常计付',t23)
 def t24():
  confirmed_statement();render('B40-record');fill('payment.amount','50.00');fill('payment.reference','DUP-001');ev("UI.store.v42.proof={name:'测试凭证.txt',localDemo:true}");click('v4-payment-record');render('B40-record');fill('payment.amount','50.00');fill('payment.reference','DUP-001');click('v4-payment-record');expect(ev('UI.v4().payments.length')==1,'重复外部流水增加记录')
 run('T24 相同外部付款流水不可重复登记',t24)
 def t25():
  reset('B40-verify');ev("UI.store.v41.actor='ADMIN01'");fill('payment.verifyReason','测试本人不能审核本人');check('payment.verified');click('v4-payment-verify');expect(ev("UI.v4().payments[0].status")=='PENDING','自审付款通过')
 run('T25 外部付款不可自审',t25)
 def t26():
  reset('B14-refund');before=ev('JSON.stringify(UI.v4().ledger)');fill('as.reason','客户费用核实后批准部分退款');fill('as.amount','42');check('as.checked');click('v4-case-review','APPROVE');click('v4-case-commit');expect(ev('JSON.stringify(UI.v4().ledger)')==before,'客户退款自动扣减代驾报酬')
 run('T26 客户退款不自动扣代驾台账',t26)
 def t27():
  confirmed_statement();snapshot=ev('JSON.stringify(UI.statement().entries)');render('B41-create');fill('adjust.amount','-10.00');fill('adjust.reason','经双方核对，原费用多计10元。');ev("UI.store.saved['upload:adjust-proof']=true");click('v4-adjust-submit');ev("UI.store.v41.actor='ADMIN02';UI.v4().reviewUser='ADMIN02'");click('v4-adjust-review','ADJ-DEMO-1');fill('adjust.reviewReason','独立核对相关服务与凭证一致');check('adjust.reviewed');click('v4-adjust-approve');expect(ev('JSON.stringify(UI.statement().entries)')==snapshot,'追改已确认原单');expect(ev('UI.v4().ledger.at(-1).adjustment')==-1000,'未追加调整条目')
 run('T27 跨期更正保留已确认账单并追加调整',t27)
 def t28():
  confirmed_statement();render('B38');
  with page.expect_download() as info:click('v41-export')
  dl=info.value;expect(dl.suggested_filename.endswith('.csv'),'没有实际导出文件')
 run('T28 对账单可实际导出CSV',t28)
 def t29():
  reset('U06');page.locator('[data-field="v4.transparency"]').check();expect(ev("document.body.classList.contains('no-transparency')"),'减少透明未生效');page.locator('[data-field="v4.motion"]').check();expect(ev("document.body.classList.contains('no-motion')"),'减少动态未生效')
 run('T29 减少透明和动态设置响应',t29)
 def t30():
  reset('B09-edit');fill('vehicle.edit.model','新的草稿');page.locator('[data-go="B09"]').last.click();page.wait_for_timeout(50);page.keyboard.press('Escape');expect(page.locator('[data-field="vehicle.edit.model"]').input_value()=='新的草稿','Escape丢失编辑内容')
 run('T30 关闭离开确认弹窗不丢失输入',t30)
 b.close()
report={'suite':'V5.0浏览器本地交互回归','environment':'Playwright + Chromium / set_content / no backend connections','scope':'真实DOM点击与输入；少量前置条件由本地样本注入，不代表数据库并发或微信联调','passed':sum(x['result']=='PASS'for x in results),'failed':sum(x['result']=='FAIL'for x in results),'pageErrors':console,'tests':results}
(root.parent/'09_验证记录/V5.0基础交互回归结果.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'pass':report['passed'],'fail':report['failed'],'pageErrors':console},ensure_ascii=False),flush=True)
