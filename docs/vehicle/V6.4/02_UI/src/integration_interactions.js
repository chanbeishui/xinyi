/* V6.4 demo actions: draft/active separation, independent switches, manual financial proof. */
(function(U){'use strict';const previous=U.action;
const refresh=()=>U.testing.render(U.currentId()),val=(k,d='')=>String(U.get('g64.'+k,d)).trim();
const secretStatus=(key,value)=>{const s=U.integration64();if(['privateKey','apiKey'].includes(key))s.wx.secrets[key]=value;else if(key==='sms')s.sms[val('sms.provider',s.sms.provider)==='TENCENT'?'tencent':'aliyun'].secret=value;else if(key==='app')s.app.secret=value;else if(key==='bank')s.offline.bankConfigured=value;};
const capture=(kind)=>{const s=U.integration64();if(kind==='policy')s.draft={wechatEnabled:!!U.get('g64.wechat',s.draft.wechatEnabled),offlineEnabled:!!U.get('g64.offline',s.draft.offlineEnabled),revision:s.draft.revision+1};
 if(kind==='wx'){for(const k of ['merchantName','mchid','appId','serial','mode','keyId','publicKey'])s.wx.draft[k]=val('wx.'+k,s.wx.draft[k]);s.wx.draft.binding=!!U.get('g64.wx.binding',false);s.wx.revision++;s.wx.saved=JSON.parse(JSON.stringify(s.wx.draft));}
 if(kind==='sms'){s.sms.provider=val('sms.provider',s.sms.provider);const d=s.sms[s.sms.provider==='TENCENT'?'tencent':'aliyun'];for(const k of ['secretId','accessKeyId','region','appId','sign'])if(k in d)d[k]=val('sms.'+k,d[k]);d.enabled=!!U.get('g64.sms.enabled',d.enabled);s.sms.revision++;}
 if(kind==='template'){const d=s.template;for(const k of ['provider','scene','sign','mapping','fallback','fallbackTemplate'])d[k]=val('template.'+k,d[k]);d.templateId=val('template.id',d.templateId);d.enabled=!!U.get('g64.template.enabled',d.enabled);d.revision++;}
 if(kind==='sub'){const d=s.sub;for(const k of ['scene','audience','mapping','page','env','lang','type'])d[k]=val('sub.'+k,d[k]);d.templateId=val('sub.id',d.templateId);d.enabled=!!U.get('g64.sub.enabled',d.enabled);d.revision++;}
 if(kind==='app'){s.app.appId=val('app.appId',s.app.appId);s.app.name=val('app.name',s.app.name);s.app.revision++;}
};
U.action=async function(action,payload,el){const s=U.integration64();
 if(action==='create-order'&&!s.active.wechatEnabled&&!s.active.offlineEnabled){U.toast('暂未开放收款方式，不创建新付费订单或预占名额');return;}
 if(action==='pay-start'){
  s.touched=true;const method=val('pay.method',s.active.wechatEnabled?'WECHAT':'OFFLINE');
  if(s.method!=='UNSELECTED'&&s.method!==method){U.toast('已有支付意图，不能直接切换通道');return;}
  if(method==='WECHAT'){
   if(!s.active.wechatEnabled||!s.wx.activeReady){U.toast('微信支付暂不可用，请刷新支付方式');return;}
   s.method='WECHAT';return previous.call(this,action,payload,el);
  }
  if(!s.active.offlineEnabled){U.toast('线下支付暂未开放');return;}
  U.dialog('选择线下支付',U.notice('选择后仍为待付款，不会直接派单。请向公司柜台或对公账户付款，后台核实后才算支付成功。','warn'),[U.button('返回','close-dialog','secondary'),U.button('确认选择线下支付','i64-offline-confirm')]);return;
 }
 if(action==='pay-query'&&s.method==='OFFLINE'){U.toast('线下付款须后台核实，不能以微信查询动作确认');return;}
 if(!action.startsWith('i64-'))return previous.call(this,action,payload,el);
 s.touched=true;
 switch(action){
 case 'i64-save':capture(payload);s[payload]&&(s[payload].checked=false);s.checks[payload]=false;s.errors=[];U.g64Log('保存'+payload+'草稿，线上未改变');U.toast('草稿已保存，尚未生效');refresh();return;
 case 'i64-check':{
  const errs=U.g64Validate(payload);if(!errs.length){s.checks[payload]=true;if(s[payload])s[payload].checked=true;U.g64Log('本地演示校验通过：'+payload+'；未做真实联调');U.toast('字段演示校验通过；不代表真实供应商可用');}else U.toast(errs[0].message);refresh();return;}
 case 'i64-readonly-check':U.dialog('只读连接检测（本地演示）',U.notice('真实系统将使用服务端当前草稿凭据进行限定的只读接口校验；不创建支付、不退款、不发短信。当前原型未连接任何供应商，实际结果为未执行。','warn')+'<p>本地字段、外部鉴权、实际支付/发送全链应分别显示，不能由其中一项通过代替其他结果。</p>',[U.button('关闭','close-dialog','secondary')]);return;
 case 'i64-publish':{
  if(s.conflict){U.toast('配置版本冲突，请重新读取最新配置');return;}
  const errors=U.g64Validate(payload);if(errors.length){U.toast(errors[0].message);refresh();return;}
  if(!s.checks[payload]){U.toast('请先校验当前草稿版本，编辑后需要重新校验');return;}
  if(payload==='policy'&&!U.get('g64.wechat',s.draft.wechatEnabled)&&!U.get('g64.offline',s.draft.offlineEnabled)){
   U.dialog('暂停新付费预约？',U.notice('两个方式都关闭后，新支付和新付费预约暂停。已收款订单、历史回调、退款与线下凭证核实继续处理。','warn'),[U.button('返回修改','close-dialog','secondary'),U.button('确认关闭并发布','i64-publish-confirm','danger-solid',{payload:'policy'})]);return;}
  U.dialog('确认发布生效',`<p>发布${U.esc(payload)}配置后，新操作读取此版本。历史交易仍按原资金身份处理。</p>`+U.notice('当前只是本地交互演示，不调用真实第三方。'),[U.button('取消','close-dialog','secondary'),U.button('确认发布','i64-publish-confirm','',{payload})]);return;}
 case 'i64-publish-confirm':{
  if(!s.checks[payload]||s.conflict||U.g64Validate(payload).length){U.closeDialog();refresh();return;}
  capture(payload);
  if(payload==='policy'){s.active={...s.draft,revision:s.active.revision+1};s.offline={...s.offline,receiver:val('receiver',s.offline.receiver),methods:[...(U.get('g64.cash',true)?['CASH']:[]),...(U.get('g64.transfer',true)?['BANK_TRANSFER']:[])],accountName:val('accountName',s.offline.accountName),bankName:val('bank',s.offline.bankName),contact:val('contact',s.offline.contact),phone:val('phone',s.offline.phone),instructions:val('instructions',s.offline.instructions)};}
  if(payload==='wx')s.wx.activeReady=true;
  if(payload==='sms'){const d=s.sms[s.sms.provider==='TENCENT'?'tencent':'aliyun'];s.sms.activeProfiles[s.sms.provider]={enabled:d.enabled,revision:s.sms.revision};}
  if(payload==='template'){s.template.active=JSON.parse(JSON.stringify(s.template));s.sms.routes[s.template.scene]=s.template.provider;s.sms.activeProvider=s.sms.routes.BOOKING_CONFIRMED||'TENCENT';}
  if(payload==='sub')s.sub.active=JSON.parse(JSON.stringify(s.sub));
  if(payload==='app')s.app.activeReady=true;
  U.g64Log('发布生效：'+payload+'（仅本地演示）');U.closeDialog();U.toast('本地演示版本已生效');refresh();return;}
 case 'i64-secret-dialog':U.dialog('替换安全凭据（演示）',U.notice('不要粘贴真实密钥。此原型仅演示“已配置”状态，不提供加密保存能力。','warn')+'<label for="g64-secret-demo">输入演示字符串</label><input id="g64-secret-demo" class="input" type="password" autocomplete="new-password" placeholder="仅输入演示内容" /><p class="hint mt">真实系统必须在服务端解析、校验、加密；查询不返回原文。</p>',[U.button('保留原值','close-dialog','secondary'),U.button('保存演示状态','i64-secret-save','',{payload})]);return;
 case 'i64-secret-save':{const input=document.getElementById('g64-secret-demo');if(!input?.value){U.toast('未输入，原凭据保持不变');return;}if(/^\*+$/.test(input.value)){U.toast('掩码不能作为新密钥提交');return;}if(payload==='apiKey'&&new TextEncoder().encode(input.value).length!==32){U.toast('API v3密钥须32字节；请使用32位演示字符串');return;}input.value='';secretStatus(payload,true);s.wx.checked=false;s.sms.checked=false;s.app.checked=false;s.checks={};U.closeDialog();U.toast('仅保存演示配置状态，不保存输入原文');refresh();return;}
 case 'i64-preview':{const key=payload==='sub'?'sub':'template',d=s[key],mapping=val(key+'.mapping',d.mapping);U.dialog('消息预览（不会发送)',U.notice('示例变量来自本地假数据；实际字段仍需按正式模板校验。')+'<pre>'+U.esc(mapping)+'</pre><p>订单 JD-DEMO-001 · 粤K·演示 · 预约 14:00–15:00</p>',[U.button('关闭','close-dialog','secondary')]);return;}
 case 'i64-test-dialog':U.dialog('白名单测试发送',U.notice('正式系统会真实发送并可能计费，必须选择获授权的测试联系人。当前原型仅模拟。','warn')+U.field('测试联系人','g64.test.recipient','TEST-01','select',{options:[['TEST-01','测试联系人 · 139****0001']]})+U.check('我了解真实测试会发信并可能产生费用','g64.test.ack',false)+(payload==='SUBSCRIBE'?U.check('本人已主动订阅该模板（演示）','g64.test.consent',false):''),[U.button('取消','close-dialog','secondary'),U.button('模拟供应商受理','i64-test-send','',{payload})]);return;
 case 'i64-test-send':if(!U.get('g64.test.ack',false)){U.toast('请先确认测试发送和费用提示');return;}if(payload==='SUBSCRIBE'&&!U.get('g64.test.consent',false)){U.toast('没有订阅授权，不能测试发送；普通业务不受影响');return;}s.deliveries.unshift({id:'N-'+(s.deliveries.length+1),provider:payload==='SUBSCRIBE'?'微信订阅':'短信供应商',scene:'白名单测试',status:'ACCEPTED',recipient:'139****0001'});U.closeDialog();U.toast('模拟受理成功，不等于实际送达');U.go('B51');return;
 case 'i64-query-receipt':{const d=s.deliveries.find(x=>x.id===payload);if(d?.status==='UNKNOWN'){U.toast('本次模拟查询仍未知，不会自动重发');return;}if(d?.status==='ACCEPTED'){d.status='DELIVERED';U.toast('模拟可信送达回执，仍不代表已读');refresh();return;}U.toast('该记录没有新的可信回执');return;}
 case 'i64-retry-send':{const d=s.deliveries.find(x=>x.id===payload);if(d?.status!=='FAILED'){U.toast('仅确定未发送的失败允许重试，未知结果先查询');return;}d.status='ACCEPTED';U.toast('演示重试已受理');refresh();return;}
 case 'i64-history-query':U.dialog('历史微信交易查询',U.notice('当前新微信支付开关：'+(s.active.wechatEnabled?'开启':'关闭'))+'<p>历史交易仍使用原商户身份和有效轮换凭据查询、处理退款及接收回调。本次未调用真实微信。</p>',[U.button('关闭','close-dialog','secondary')]);return;
 case 'i64-offline-confirm':s.method='OFFLINE';U.closeDialog();U.g64Log('客户选择线下方式，仍未付款');U.go('O10-offline');return;
 case 'i64-payment-refresh':U.toast(s.proof?.status==='VERIFIED'?'线下资金已核实，请查看订单安排':s.proof?.status==='SUBMITTED'?'凭证正在独立核实，尚未支付成功':'尚未核实到款，请联系公司财务');refresh();return;
 case 'i64-proof-submit':{
  if(s.proof&&s.proof.status!=='REJECTED'){U.toast('已有有效证明，不能重复登记');return;}
  const amount=Number(val('proof.amount')),direction=val('proof.direction','COLLECTION');
  if(!Number.isFinite(amount)||Math.round(amount*100)!==34000){U.toast('金额必须与本次应收或批准退款340元一致；差异转人工处理');return;}
  if(!val('proof.ref')||!val('proof.note')||!val('proof.paidAt','2026-10-22 10:00')||!U.get('g64.proof.file',false)){U.toast('请填写流水、说明并关联凭证');return;}
  if(direction==='REFUND'&&!val('proof.refundNo')){U.toast('线下退款必须关联已批准退款单');return;}
  s.method='OFFLINE';s.proof={id:'OFF-001',direction,amount:34000,reference:val('proof.ref'),status:'SUBMITTED',operator:'财务甲',late:false};U.g64Log('客户线下证明登记，待独立核实；未记已付');U.toast('已登记，尚未算支付成功');refresh();return;}
 case 'i64-proof-verify':{
  if(s.proof?.status!=='SUBMITTED'){U.toast('当前证明不可核实');return;}
  if(val('proof.reviewer','财务乙')===s.proof.operator){U.toast('登记人与复核人不能相同');return;}
  if(!val('proof.verifyReason')){U.toast('请填写核实意见');return;}
  s.proof.status='VERIFIED';if(s.proof.direction==='COLLECTION'){U.store.live.paid=s.proof.amount;if(!s.proof.late)U.store.live.stage='SCHEDULED';}
  U.g64Log(s.proof.direction==='REFUND'?'线下退款已核实，未调用微信':'线下收款已核实'+(s.proof.late?'；容量已释放，转人工处理':'；按计划安排服务'));U.toast(s.proof.late?'资金已核实，原容量冲突需人工处理':'核实成功，资金状态已更新');refresh();return;}
 case 'i64-proof-reject':if(s.proof?.status!=='SUBMITTED')return;if(!val('proof.verifyReason')){U.toast('请填写驳回原因');return;}s.proof.status='REJECTED';U.toast('证明已驳回，未计入已付款');refresh();return;
 case 'i64-subscribe-prompt':U.dialog('订阅服务消息',U.notice('这是微信订阅授权交互的本地示意，实际小程序需由用户点击后调用微信授权弹窗。')+'<p>检测结果通知 · 车辆归还通知</p><p class="hint">不订阅仍可正常预约、支付和归还。</p>',[U.button('暂不订阅','i64-subscribe-decline','secondary'),U.button('模拟本人同意','i64-subscribe-accept')]);return;
 case 'i64-subscribe-accept':s.consent=true;U.closeDialog();U.toast('已记录演示意向；真实能否发送以微信结果为准');return;
 case 'i64-subscribe-decline':s.consent=false;U.closeDialog();U.toast('未订阅，不影响正常业务，可查看站内消息');return;
 }
};
document.addEventListener('change',ev=>{const key=ev.target?.dataset?.field;if(!key?.startsWith('g64.'))return;const s=U.integration64();s.touched=true;s.errors=[];
 if(key==='g64.pay.wechat'&&ev.target.checked){U.set('g64.pay.method','WECHAT');U.set('g64.pay.offline',false);}
 if(key==='g64.pay.offline'&&ev.target.checked){U.set('g64.pay.method','OFFLINE');U.set('g64.pay.wechat',false);}
 if(key==='g64.sms.provider'){for(const k of Object.keys(U.store.forms))if(k.startsWith('g64.sms.')&&k!==key)delete U.store.forms[k];}
 if(key==='g64.template.provider'){U.set('g64.template.mapping',ev.target.value==='TENCENT'?'1:orderNo\n2:pickupWindow':'order:orderNo\ntime:pickupWindow');}
 if(key.endsWith('.mode')||key.endsWith('.provider')||key.startsWith('g64.pay.')||['g64.offline','g64.wechat'].includes(key))setTimeout(refresh,0);
 s.wx.checked=false;s.sms.checked=false;s.template.checked=false;s.sub.checked=false;s.app.checked=false;s.checks={};
});
U.flows.integrations={name:'第三方配置与线下支付',desc:'独立开关 → 商户校验 → 发布 → 客户选择 → 凭证独立核实 → 发送诊断',ids:['B46','B47','B48','B49','B50','B53','B46','O10-both','B52','B51'],seed:'PENDING_PAY'};
})(window.UI);
