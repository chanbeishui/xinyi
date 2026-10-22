"""Prototype DOM/local state only. Not a real payment, SMS, DB or server test."""
from pathlib import Path
import json,shutil
from playwright.sync_api import sync_playwright
R=Path(__file__).resolve().parent; OUT=R.parent/'09_验证记录';OUT.mkdir(exist_ok=True)
checks=[];errors=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path=shutil.which('chromium'),headless=True,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':1440,'height':1050});page.set_default_timeout(5000)
 page.on('pageerror',lambda e:errors.append(str(e)))
 page.set_content((R/'index.html').read_text(),wait_until='load')
 def ev(x):return page.evaluate(x)
 def render(id):page.evaluate('(id)=>UI.testing.render(id)',id)
 def reset(id='B46'):
  ev("UI.store=UI.makeStore();UI.closeDialog();document.body.className='export'");render(id)
 def click(a,payload=None):
  q='[data-action="'+a+'"]'+('' if payload is None else '[data-payload="'+payload+'"]')
  page.locator(q).first.click();page.wait_for_timeout(45)
 def field(k):return page.locator('[data-field="g64.'+k+'"]')
 def fill(k,v):field(k).fill(v);field(k).dispatch_event('change');page.wait_for_timeout(30)
 def tick(k,v=True):field(k).set_checked(v);page.wait_for_timeout(40)
 def select(k,v):field(k).select_option(v);page.wait_for_timeout(40)
 def text():return page.locator('[data-screen]').inner_text()
 def toast():return page.locator('#toast-root').inner_text()
 def state(x):return ev('UI.integration64().'+x)
 def reason():fill('reason','本轮配置演示核验')
 def check(name,fn):
  try:fn();checks.append({'name':name,'result':'PASS'})
  except Exception as e:checks.append({'name':name,'result':'FAIL','error':str(e)[:1200]})
 def independent():
  reset();tick('offline');assert field('wechat').is_checked();tick('wechat',False);assert field('offline').is_checked();assert state('active.wechatEnabled') and not state('active.offlineEnabled')
 check('01 独立开关不互斥且未发布不改变线上',independent)
 def draft():
  reset();tick('offline');reason();click('i64-save','policy');assert state('draft.offlineEnabled') and not state('active.offlineEnabled');assert '尚未生效' in toast()
 check('02 保存草稿不改变支付方式',draft)
 def requirecheck():
  reset();reason();click('i64-publish','policy');assert '先校验' in toast()
 check('03 当前草稿未校验不能发布',requirecheck)
 def publish():
  reset();tick('offline');reason();click('i64-check','policy');click('i64-publish','policy');click('i64-publish-confirm','policy');assert state('active.offlineEnabled') and state('active.wechatEnabled');render('O10');assert '线下支付' in text() and '微信支付' in text()
 check('04 校验发布后客户端显示两种方式',publish)
 def disableboth():
  reset();tick('wechat',False);reason();click('i64-check','policy');click('i64-publish','policy');assert '暂停新付费预约' in page.locator('.dialog').inner_text();click('i64-publish-confirm','policy');render('O10');assert page.locator('[data-action="pay-start"]').is_disabled();assert '免费' in text()
 check('05 双关闭须警告且禁止新付款，不默认为免费',disableboth)
 def invalidwx():
  reset('B46-invalid');reason();click('i64-check','policy');assert state('errors.length')>0;assert '商户' in text()
 check('06 微信启用但商户不完整拒绝发布',invalidwx)
 def offlineonly():
  reset('B46-offline');ev('UI.integration64().wx.activeReady=false');reason();click('i64-check','policy');assert state('errors.length')==0
 check('07 仅线下支付无需微信商户配置',offlineonly)
 def empty():
  reset('B47-empty');reason();click('i64-save','wx');assert state('wx.saved.mchid')=='';click('i64-check','wx');assert state('errors.length')>=5
 check('08 商户允许不完整草稿，校验逐项提示必填',empty)
 def certbranch():
  reset('B47');select('wx.mode','PLATFORM_CERT');assert field('wx.platformCert').count()==1;reason();click('i64-check','wx');assert '平台证书' in text()
 check('09 平台证书模式显示并校验对应字段',certbranch)
 def secretmask():
  reset('B47-empty');click('i64-secret-dialog','apiKey');page.locator('#g64-secret-demo').fill('***');click('i64-secret-save','apiKey');assert '掩码' in toast();page.locator('#g64-secret-demo').fill('too-short');click('i64-secret-save','apiKey');assert '32字节' in toast()
 check('10 拒绝掩码与错误长度API密钥',secretmask)
 def secretstore():
  reset('B47-empty');click('i64-secret-dialog','apiKey');value='D'*32;assert len(value)==32;page.locator('#g64-secret-demo').fill(value);click('i64-secret-save','apiKey');assert state('wx.secrets.apiKey');assert value not in ev('JSON.stringify(UI.store)');assert '仅保存' in toast()
 check('11 密钥演示只存配置标记，不留输入原文',secretstore)
 def keyinvalidates():
  reset('B47');reason();click('i64-check','wx');assert state('checks.wx');click('i64-secret-dialog','apiKey');page.locator('#g64-secret-demo').fill('0'*32);click('i64-secret-save','apiKey');assert not ev('Boolean(UI.integration64().checks.wx)')
 check('12 更换密钥须重新校验，不沿用旧校验',keyinvalidates)
 def editsinvalidate():
  reset('B47');reason();click('i64-check','wx');fill('wx.mchid','1900000002');click('i64-publish','wx');assert '先校验' in toast()
 check('13 字段编辑失效此前校验',editsinvalidate)
 def conflict():
  reset('B46-conflict');reason();click('i64-publish','policy');assert '版本冲突' in toast()
 check('14 配置并发版本冲突拒绝覆盖',conflict)
 def providers():
  reset('B48');assert field('sms.secretId').count();select('sms.provider','ALIYUN');assert field('sms.accessKeyId').count();assert field('sms.secretId').count()==0;assert 'TemplateParam JSON' in text()
 check('15 腾讯云/阿里云字段分支真实切换',providers)
 def smsdraft():
  reset('B48');select('sms.provider','ALIYUN');reason();click('i64-save','sms');assert state('sms.activeProvider')=='TENCENT';click('i64-check','sms');click('i64-publish','sms');click('i64-publish-confirm','sms');assert state('sms.activeProvider')=='TENCENT';assert state('sms.activeProfiles.ALIYUN.enabled')
 check('16 通道发布不擅自切场景路由，两个供应商可并存',smsdraft)
 def routeswitch():
  reset('B49');select('template.provider','ALIYUN');reason();click('i64-check','template');click('i64-publish','template');click('i64-publish-confirm','template');assert state('sms.routes.BOOKING_CONFIRMED')=='ALIYUN';assert state('template.active.provider')=='ALIYUN'
 check('38 场景模板发布才切该场景主通道',routeswitch)
 def routedisabled():
  reset('B49');ev('UI.integration64().sms.activeProfiles.TENCENT.enabled=false');reason();click('i64-check','template');assert '未发布启用' in text()
 check('39 场景不得发布到未启用供应商',routedisabled)
 def fallbackmissing():
  reset('B49');select('template.fallback','DEFINITELY_NOT_SENT_ONLY');reason();click('i64-check','template');assert '备用模板' in text();select('template.fallbackTemplate','DEMO_BACKUP');click('i64-check','template');assert state('errors.length')==0
 check('40 备用策略必须关联备用模板且未知结果不能使用',fallbackmissing)
 def template():
  reset('B49');fill('template.mapping','order:orderNo');reason();click('i64-check','template');assert '连续顺序' in text();select('template.provider','ALIYUN');click('i64-check','template');assert state('errors.length')==0
 check('17 腾讯顺序参数与阿里命名参数不得混用',template)
 def preview():
  reset('B49');click('i64-preview','template');assert '不会发送' in page.locator('.dialog').inner_text();assert state('deliveries.length')==0
 check('18 消息预览无发送副作用',preview)
 def testack():
  reset('B49');click('i64-test-dialog','SMS');click('i64-test-send','SMS');assert state('deliveries.length')==0;tick('test.ack');click('i64-test-send','SMS');assert state('deliveries[0].status')=='ACCEPTED'
 check('19 白名单测试需要费用确认且受理不等于送达',testack)
 def unknown():
  reset('B51-unknown');assert page.locator('[data-action="i64-retry-send"][data-payload="N-001"]').is_disabled();click('i64-query-receipt','N-001');assert state('deliveries[0].status')=='UNKNOWN'
 check('20 发送未知只能查询，不自动切通道重发',unknown)
 def failed():
  reset('B51-failed');click('i64-retry-send','N-001');assert state('deliveries[0].status')=='ACCEPTED';click('i64-query-receipt','N-001');assert state('deliveries[0].status')=='DELIVERED'
 check('21 确认未发送失败可演示重试，回执另行更新',failed)
 def history():
  reset('B46-paused');render('B51');click('i64-history-query');assert '原商户' in page.locator('.dialog').inner_text();assert '关闭' in page.locator('.dialog').inner_text()
 check('22 关闭新支付仍有历史查单维护入口',history)
 def suburl():
  reset('B50');fill('sub.page','https://evil.invalid/?token=x');reason();click('i64-check','sub');assert '合法小程序' in text() or '仅允许' in text()
 check('23 订阅目标拒绝外部URL和敏感参数',suburl)
 def subtype():
  reset('B50');select('sub.type','LONG');reason();click('i64-check','sub');assert '长期订阅资格' in text()
 check('24 无资格不得启用长期订阅',subtype)
 def subenv():
  reset('B50');select('sub.env','trial');reason();click('i64-check','sub');assert 'formal' in text()
 check('25 正式配置不发送体验版页面',subenv)
 def subtest():
  reset('B50');click('i64-test-dialog','SUBSCRIBE');tick('test.ack');click('i64-test-send','SUBSCRIBE');assert state('deliveries.length')==0;assert '订阅授权' in toast()
 check('26 订阅测试没有授权不发送',subtest)
 def refuse():
  reset('O28');before=ev('JSON.stringify(UI.store.live)');click('i64-subscribe-prompt');click('i64-subscribe-decline');assert not state('consent');assert ev('JSON.stringify(UI.store.live)')==before
 check('27 拒绝订阅不改变订单履约',refuse)
 def offline():
  reset('O10-both');select('pay.method','OFFLINE');click('pay-start');click('i64-offline-confirm');assert state('method')=='OFFLINE';assert not state('proof');assert '等待线下付款' in text()
 check('28 选择线下不标记支付成功、不派单',offline)
 def submitproof():
  reset('B52');fill('proof.ref','TEST-RECEIPT-01');fill('proof.note','实际公司柜台收款演示');tick('proof.file');click('i64-proof-submit');assert state('proof.status')=='SUBMITTED';assert '尚未算支付成功' in toast()
 check('29 登记线下凭证仍待独立核实',submitproof)
 def badamount():
  reset('B52');fill('proof.amount','339');fill('proof.ref','TEST-02');fill('proof.note','金额不一致');tick('proof.file');click('i64-proof-submit');assert not state('proof');assert '金额' in toast()
 check('30 不等额凭证不直接确认足额',badamount)
 def selfreview():
  reset('B52-review');select('proof.reviewer','财务甲');fill('proof.verifyReason','同一人尝试');click('i64-proof-verify');assert state('proof.status')=='SUBMITTED';assert '不能相同' in toast()
 check('31 收款登记人不能自核',selfreview)
 def verify():
  reset('B52-review');fill('proof.verifyReason','已核对真实企业流水（演示）');click('i64-proof-verify');assert state('proof.status')=='VERIFIED';assert ev('UI.store.live.paid')==34000;assert ev('UI.store.live.stage')=='SCHEDULED'
 check('32 独立核实后才更新资金与计划安排',verify)
 def late():
  reset('B52-late');before=ev('UI.store.live.stage');fill('proof.verifyReason','迟到凭证核实');click('i64-proof-verify');assert ev('UI.store.live.paid')==34000;assert ev('UI.store.live.stage')==before;assert '人工' in toast()
 check('33 迟到收款保留资金事实但不静默恢复容量',late)
 def refund():
  reset('B52-refund');before=ev('UI.store.live.paid');fill('proof.verifyReason','核对已批准退款的实际银行流水');click('i64-proof-verify');assert state('proof.status')=='VERIFIED';assert ev('UI.store.live.paid')==before;assert '未调用微信' in state('history[0].text')
 check('34 线下退款独立核实，不走微信或改原实付',refund)
 def proofreject():
  reset('B52-review');fill('proof.verifyReason','收据与订单不符');click('i64-proof-reject');assert state('proof.status')=='REJECTED'
 check('35 驳回收款证明不计入已付',proofreject)
 def applink():
  reset('B53-empty');reason();click('i64-check','app');assert state('errors.length')>=2;assert 'AppSecret' in text()
 check('36 小程序公共凭据不完整不能发布',applink)
 def readonlycheck():
  reset('B47');before=state('deliveries.length');click('i64-readonly-check','wx');assert '未连接' in page.locator('.dialog').inner_text();assert state('deliveries.length')==before
 check('41 只读连接检查明确未联网，不产生付款发信副作用',readonlycheck)
 def screenshots():
  for id in ['B46','B46-both','B46-invalid','B47','B48-aliyun','B49','B50','B52-review','B53','O10-both','O10-offline']:
   reset(id);page.set_viewport_size({'width':390 if id[0]!='B' else 1440,'height':844 if id[0]!='B' else 1050});ev("document.getElementById('toast-root').innerHTML=''");page.locator('[data-screen]').screenshot(path=str(OUT/('第三方实测_'+id+'.png')),animations='disabled')
 check('37 新页面实图与运行时无异常检查',screenshots)
 result={'scope':'PROTOTYPE_DOM_AND_LOCAL_STATE_ONLY','environment':'Playwright/Chromium set_content; Browser connector only supports read tabs, no browser control runtime','checks':checks,'passed':sum(x['result']=='PASS' for x in checks),'failed':sum(x['result']=='FAIL' for x in checks),'pageErrors':errors,'NOT_RUN':['real xinyi','MySQL','WeChat pay','SMS','subscription delivery']}
 (OUT/'V6.4_第三方配置交互实测.json').write_text(json.dumps(result,ensure_ascii=False,indent=2));print(json.dumps(result,ensure_ascii=False,indent=2));b.close()
 if result['failed'] or errors:raise SystemExit(1)
