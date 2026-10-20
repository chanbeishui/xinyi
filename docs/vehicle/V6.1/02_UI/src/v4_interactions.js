/* All V4 mutations below operate in browser memory only. Backend invariants are specified separately. */
(function(U){
'use strict';
const{button:b,goBtn:g,field:f,check:c,notice:n,kv,esc:e}=U;
const originalAction=U.action,rawGo=U.go,rawDialog=U.dialog,rawClose=U.closeDialog;
let lastFocus=null,visibleReviewVersion=null,visibleStatementVersion=null;
const now=()=>new Date().toISOString();
function rerender(){U.render();}
function clean(){U.v4().dirty=false;}
function jump(id){clean();rawGo(id);}
function error(key,message){U.store.errors[key]=message;let el=document.querySelector(`[data-field="${key}"]`);if(el){el.setAttribute('aria-invalid','true');el.closest('.field')?.classList.add('invalid');let m=document.querySelector(`[data-error-for="${key}"]`);if(m){m.hidden=false;m.textContent=message;el.setAttribute('aria-describedby',m.id||'err-'+key.replace(/\W/g,'-'));m.id=el.getAttribute('aria-describedby');}el.focus();el.scrollIntoView({block:'center',behavior:'smooth'});}U.toast(message);return false;}
function required(keys){for(const key of keys){let node=document.querySelector(`[data-field="${key}"]`);let val=node?(node.type==='checkbox'?node.checked:node.value):U.get(key);if(val===false||val===null||val===undefined||String(val).trim()==='')return error(key,'请填写或确认此项');U.set(key,val);}return true;}
function parseMoney(key,options={}){let value=U.minor(U.get(key,''));if(value===null||(!options.signed&&value<0)||(!options.zero&&value===0)){error(key,'请输入'+(options.signed?'可为负数的':'')+'有效金额，最多两位小数'+(options.zero?'':'，且不能为0'));return null;}return value;}
function log(action,detail){U.log(action,detail);U.v4().events.push({action,detail,at:now()});}
function clearFields(prefix){for(const k of Object.keys(U.store.forms))if(k.startsWith(prefix))delete U.store.forms[k];}
function pickCase(id,target='B14'){U.v4().caseId=id;clearFields('as.');let c=U.caseFor();U.v4().reviewVersion=c.version;visibleReviewVersion=c.version;jump(target);}
function selectDriver(id){U.v4().selectedDriver=id;U.v4().statementId=U.v4().statements.find(x=>x.driverId===id&&x.status!=='VOID')?.id||null;clearFields('recon.driver');}
function ensureStatement(){const st=U.statement();if(!st){U.toast('请先生成并选择对账单');return null;}return st;}
function download(name,rows){const escape=value=>{let s=String(value??'');if(/^[=+\-@\t\r]/.test(s))s="'"+s;return '"'+s.replace(/"/g,'""')+'"';};const text='\ufeff'+rows.map(r=>r.map(escape).join(',')).join('\r\n');const url=URL.createObjectURL(new Blob([text],{type:'text/csv;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
U.applyDisplayPrefs=function(){const flags={dark:'v4.dark','more-contrast':'v4.contrast','no-transparency':'v4.transparency','no-motion':'v4.motion'};for(const[cls,key]of Object.entries(flags))document.body.classList.toggle(cls,!!U.get(key,false));};
U.refinePageCopy=function(v,d){
 // Keep implementation notes in the review sidebar, not as long technical page subtitles.
 const brief={B03:'查询预约、接送安排和服务状态。',B04:'查看本次服务、车辆交接与售后记录。',B05:'按预约时间安排代驾，及时处理等待中的任务。',B07:'核对车辆、到站资料和交接信息。',B08:'对照证件核实登记内容。',B10:'处理资料核验和最终结果登记。',B11:'核对车辆与站点，填写本次最终检测结果。',B15:'记录问题、跟踪沟通与处理结果。',B16:'按同一标准审核普通申请人和检测员工。',B17:'维护服务站点、车型和营业信息。',B19:'安排开放时段与可接受的预约数量。',B20:'配置面向车主的收费规则。',B21:'维护服务通知与公开内容。',B22:'查看关键操作与访问记录。',B23:'查看预约、履约及资金核对统计。',B24:'维护预约派单与服务参数。',B25:'查看员工账号与检测点授权。',B26:'选择员工可以操作的检测点。',B30:'对照更正前后的检测结果。',B31:'核实订单付款与渠道退款状态。',B32:'审核必要的资料访问与导出。'};
 if(brief[v.base])d.desc=brief[v.base];
};
U.afterRender=function(v){
 if(v.base==='B14'){visibleReviewVersion=U.caseFor(v).version;U.v4().reviewVersion=visibleReviewVersion;}
 if(v.base==='D38'||v.base==='B38')visibleStatementVersion=U.statement()?.version||null;
 document.querySelectorAll('[role="dialog"]').forEach(x=>x.setAttribute('aria-modal','true'));
 document.querySelectorAll('.segmented').forEach(el=>{el.setAttribute('role','tablist');el.querySelectorAll('button').forEach(x=>{x.setAttribute('role','tab');x.setAttribute('aria-selected',String(x.classList.contains('active')));});});
 document.querySelectorAll('.drawer-head h2,.phone-sheet h2').forEach(x=>{x.tabIndex=-1;});
};
U.dialog=function(title,html,buttons){lastFocus=document.activeElement;return rawDialog(title,html,buttons);};
U.closeDialog=function(){rawClose();if(lastFocus?.isConnected)lastFocus.focus();lastFocus=null;};
U.go=function(id,replace=false){if(U.v4().dirty&&U.resolve(id)!==U.currentId()){
 U.v4().pendingNavigation={id,replace};U.dialog('离开前保存修改？','<p>当前内容尚未提交，返回后也可以继续处理保存的草稿。</p>',[b('继续编辑','close-dialog','secondary'),b('不保存并离开','v4-discard','danger'),b('保存草稿并离开','v4-save-and-leave')]);return;
}return rawGo(id,replace);};
U.v4Action=async function(action,payload='',element=null){const s=U.v4();let a;
 switch(action){
 case 'v4-discard':case 'v4-save-and-leave':{const target=s.pendingNavigation;if(action==='v4-discard'){let base=U.current()?.base;const p={B09:'vehicle.edit.',B27:'owner.edit.',B28:'driver.',B14:'as.',B39:'df.',B41:'adjust.'}[base];if(p)clearFields(p);}else log('保存本地草稿','未提交业务审核');clean();U.closeDialog();if(target)rawGo(target.id,target.replace);return;}
 case 'v4-open-vehicle':s.selectedVehicle=payload;clearFields('vehicle.edit.');U.set('profile.tab.vehicle',0);jump('B09-detail');return;
 case 'v4-open-owner':s.selectedOwner=payload;clearFields('owner.edit.');U.set('profile.tab.owner',0);jump('B27-detail');return;
 case 'v4-open-driver':selectDriver(payload);U.set('profile.tab.driver',0);clearFields('driver.record.');jump('B28-detail');return;
 case 'v4-driver-recon':selectDriver(payload);jump('B38');return;
 case 'v4-search':s.queries[payload]={...(s.queries[payload]||{}),text:String(U.get('dir.'+payload+'.text','')).trim(),status:U.get('dir.'+payload+'.status','全部'),page:1};rerender();return;
 case 'v4-reset-search':s.queries[payload]={text:'',status:'全部',page:1,sort:'asc'};clearFields('dir.'+payload+'.');rerender();return;
 case 'v4-sort':s.queries[payload]={text:'',status:'全部',page:1,...s.queries[payload],sort:s.queries[payload]?.sort==='desc'?'asc':'desc'};rerender();return;
 case 'v4-page':{let[k,p]=payload.split('|');s.queries[k]={text:'',status:'全部',sort:'asc',...s.queries[k],page:Math.max(1,Number(p))};rerender();return;}
 case 'v4-list-reset':jump(U.current().base);return;
 case 'v4-vehicle-save':if(!required(['vehicle.edit.model','vehicle.edit.type','vehicle.edit.reason']))return;Object.assign(U.vehicle(),{model:U.get('vehicle.edit.model'),type:U.get('vehicle.edit.type')});log('修改车辆档案',U.vehicle().id+'，未覆盖历史订单快照');jump('B09-detail');return;
 case 'v4-owner-save':if(!required(['owner.edit.name','owner.edit.reason']))return;Object.assign(U.owner(),{name:U.get('owner.edit.name'),flag:U.get('owner.edit.flag','正常'),remark:U.get('owner.edit.remark','')});log('维护车主档案',U.owner().id+'，未修改已验证手机，不禁止下单');jump('B27-detail');return;
 case 'v4-driver-save':if(!required(['driver.edit.reason']))return;U.driver().remark=U.get('driver.edit.remark','');log('维护代驾备注',U.driver().id);jump('B28-detail');return;
 case 'v4-driver-availability':if(!required(['driver.record.reason']))return;U.driver().listening=!!U.get('driver.record.listening');log('更新新邀约开关',U.driver().id+'；现有承接仍保留');clean();U.toast('已保存接单设置，现有任务不受影响');rerender();return;
 case 'v4-export-request':U.set('access.scope',payload+' · 当前授权范围');U.set('access.type','订单字段导出');jump('B32-request');return;
 case 'v4-case-filter':s.caseId=null;clean();if(U.currentId()==='B13-return')jump('B13');else rerender();return;
 case 'v4-open-case':pickCase(payload);return;
 case 'v4-case-apply':{
  if(!required(['as.applyReason','as.applyAmount']))return;const amount=parseMoney('as.applyAmount',{zero:true});if(amount===null)return;
  if(amount>U.store.live.paid)return error('as.applyAmount','申请金额不能超过本单实付金额');
  const live=U.store.live,kind=U.get('as.applyKind',payload==='refund'?'REFUND_ONLY':'CANCEL_REFUND');
  let existing=s.cases.find(x=>x.orderNo===live.orderNo&&x.review!=='REJECTED'&&U.caseState(x)!=='已办结');
  if(existing){pickCase(existing.id,'O19-duplicate');return;}
  // Seed records are examples. Starting a guided flow creates its own single case for the live order.
  const id=existing?.id||'AS-DEMO-'+String(s.cases.length+1).padStart(3,'0');
  const item={id,orderNo:live.orderNo,plate:live.plate,owner:live.owner,driver:live.driver,driverId:'D012',assignmentId:live.assignmentId,custody:U.store.flow?live.custody:payload==='picked'?'STATION':'OWNER',stage:U.store.flow?live.stage:payload==='picked'?'INSPECTING':'SCHEDULED',kind,reason:U.get('as.applyReason'),paid:live.paid,refunded:live.refunded,reserved:live.reservedRefund,requested:amount,approved:null,review:'PENDING',returnState:'UNDETERMINED',refundState:'NONE',created:'本次演示',version:1,auditCount:0,notes:[]};
  if(existing)s.cases.splice(s.cases.indexOf(existing),1,item);else s.cases.push(item);s.caseId=id;U.store.saved.newCaseCreated=true;live.cancel=kind==='CANCEL_REFUND'?'APPLIED':live.cancel;log('提交统一售后申请',id+' · '+kind);jump('O21');return;}
 case 'v4-case-review':{
  a=U.caseFor(U.current());if(a.review!=='PENDING'){U.toast('该申请已经处理，请刷新结果');return;}
  if(!required(['as.reason']))return;let amount=0;
  if(payload==='APPROVE'){if(!required(['as.amount','as.checked']))return;amount=parseMoney('as.amount',{zero:a.kind==='CANCEL_REFUND'});if(amount===null)return;if(amount>a.paid-a.refunded-a.reserved)return error('as.amount','超过当前可退金额，已退及预留的金额不能重复退');}
  s.pendingDecision={id:a.id,version:visibleReviewVersion||a.version,result:payload,amount,reason:String(U.get('as.reason'))};
  U.dialog(payload==='APPROVE'?'确认本次统一审核':'确认驳回申请',kv('申请',a.id)+kv('操作',payload==='REJECT'?'驳回申请':U.caseKind(a))+kv('批准退款',U.money(amount))+n(payload==='REJECT'?'将保留本次意见，原有车辆交接事实不回退。':a.kind==='CANCEL_REFUND'&&a.custody!=='OWNER'?'批准后仍须实际归还车辆，司机不会立即释放。':a.kind==='REFUND_ONLY'?'仅执行退款，不取消服务。':'批准后取消订单，按本次金额执行退款。','warn'),[b('返回修改','close-dialog','secondary'),b('确认提交','v4-case-commit',payload==='REJECT'?'danger':'')]);return;}
 case 'v4-case-commit':{
  let p=s.pendingDecision;a=s.cases.find(x=>x.id===p?.id);if(!a||a.review!=='PENDING'||a.version!==p.version){U.closeDialog();U.toast('申请已变更，保留意见并刷新后再处理');jump('B14-conflict');return;}
  if(p.amount>a.paid-a.refunded-a.reserved){U.closeDialog();U.toast('可退余额已变化，请重新核对');return;}
  a.auditCount++;a.version++;a.notes.push(p.reason);a.review=p.result==='REJECT'?'REJECTED':'APPROVED';a.approved=p.result==='REJECT'?null:p.amount;
  if(a.review==='APPROVED'){
   a.reserved+=p.amount;
   if(a.kind==='CANCEL_REFUND'&&a.custody!=='OWNER'){a.returnState='WAIT_RETURN';a.refundState=p.amount>0?'WAIT_RETURN':'NOT_REQUIRED';if(U.store.flow){U.store.live.cancel='APPROVED_WAIT_RETURN';U.store.live.reservedRefund+=p.amount;}U.store.saved.cancelApproved=true;}
   else {a.returnState='NOT_REQUIRED';a.refundState=p.amount>0?'READY':'NOT_REQUIRED';if(a.kind==='CANCEL_REFUND'){a.stage='CANCELLED';if(U.store.flow){const o=U.store.live;o.stage='CANCELLED';o.cancel='COMPLETED';if(U.store.occupancy?.assignmentId===o.assignmentId)U.store.occupancy=null;o.occupied=false;o.occupiedHolder=null;}}}
  }else if(U.store.flow&&a.kind==='CANCEL_REFUND')U.store.live.cancel='REJECTED';
  log('统一售后审核',a.id+' · '+a.review+' · 批准金额='+p.amount+'分（仅本地演示）');delete s.pendingDecision;U.closeDialog();jump('B14');return;}
 case 'v4-case-query':{
  a=U.caseFor(U.current());if(a.returnState==='WAIT_RETURN'){U.toast('尚需完成车辆返还，不发起退款');return;}
  if(a.review!=='APPROVED'||!a.approved){U.toast('本申请没有待执行退款');return;}
  U.dialog('本地渠道结果演示',n('未接入微信支付。下面只切换模拟状态，不代表退款已实际到账。','warn'),[b('保持结果未知','v4-case-channel','secondary',{payload:'UNKNOWN'}),b('模拟渠道确认成功','v4-case-channel','',{payload:'SUCCESS'})]);return;}
 case 'v4-case-channel':a=U.caseFor(U.current());if(a.review!=='APPROVED'||a.returnState==='WAIT_RETURN'){U.toast('当前不满足退款执行条件');return;}if(payload==='SUCCESS'&&a.refundState!=='SUCCESS'){a.refunded+=a.approved||0;a.reserved=Math.max(0,a.reserved-(a.approved||0));}a.refundState=payload;log('渠道结果演示',a.id+' · '+payload+'；无真实渠道连接');clean();U.closeDialog();rerender();return;
 case 'v4-case-refresh':U.toast('已刷新本地记录，真实系统以服务端结果为准');rerender();return;
 case 'v4-recon-filter':if(String(U.get('recon.from'))>String(U.get('recon.to')))return error('recon.to','结束日期不能早于开始日期');rerender();return;
 case 'v4-recon-new':s.statementId=null;clean();jump('B38-generate');return;
 case 'v4-recon-generate': case 'v4-recon-send': case 'v4-recon-confirm': case 'v4-recon-confirm-commit': case 'v4-recon-dispute': case 'v4-recon-resolve': case 'v4-recon-export': return U.action(action,payload,element);
 case 'v4-fee-calculate':{if(!required(['df.base','df.included','df.unit','df.distance']))return;let base=parseMoney('df.base',{zero:true}),unit=parseMoney('df.unit',{zero:true}),included=Number(U.get('df.included')),distance=Number(U.get('df.distance'));if(base===null||unit===null)return;if(!Number.isFinite(distance)||distance<0||included<0){U.toast('请输入有效里程');return;}U.store.saved.dfCalculated=base+Math.ceil(Math.max(0,distance-included))*unit;clean();rerender();return;}
 case 'v4-fee-publish':{if(!required(['df.name','df.scope','df.base','df.included','df.unit','df.cancel','df.effective','df.reason','df.checked']))return;let base=parseMoney('df.base',{zero:true}),unit=parseMoney('df.unit',{zero:true}),cancel=parseMoney('df.cancel',{zero:true});if([base,unit,cancel].some(x=>x===null))return;s.fees={base,unit,cancel,includedKm:Number(U.get('df.included')),effective:U.get('df.effective'),version:'DF-01 / V3'};log('发布费用规则演示','不追改历史台账；真实发布须独立复核');jump('B39');return;}
 case 'v4-adjust-submit':{if(!required(['adjust.entry','adjust.amount','adjust.reason']))return;let amount=parseMoney('adjust.amount',{signed:true});if(amount===null)return;if(!U.store.saved['upload:adjust-proof']){U.toast('请添加调整依据');return;}let entry=s.ledger.find(x=>x.id===U.get('adjust.entry'));if(!entry){U.toast('原费用明细不存在');return;}let item={id:'ADJ-DEMO-'+(s.adjustments.length+1),entryId:entry.id,driverId:entry.driverId,amount,reason:U.get('adjust.reason'),status:'PENDING',creator:'ADMIN01',proof:true};s.adjustments.push(item);log('提交调整复核',item.id);jump('B41');return;}
 case 'v4-adjust-review':{const item=s.adjustments.find(x=>x.id===payload);if(!item||item.status!=='PENDING'){U.toast('该申请不在待复核状态');return;}s.reviewingAdjustment=item.id;U.dialog('独立复核费用调整',kv('调整金额',U.money(item.amount))+kv('理由',item.reason)+f('复核意见','adjust.reviewReason','','textarea',{required:true})+c('已核对原服务及依据，不是代替客户退款扣款','adjust.reviewed',false),[b('返回','close-dialog','secondary'),b('批准调整','v4-adjust-approve','',{require:'adjust.reviewed'})]);return;}
 case 'v4-adjust-approve':{const item=s.adjustments.find(x=>x.id===s.reviewingAdjustment);if(!item||item.status!=='PENDING')return;if(item.creator===s.reviewUser){U.toast('不能审核自己提交的调整');return;}if(!required(['adjust.reviewReason','adjust.reviewed']))return;item.status='APPROVED';const src=s.ledger.find(x=>x.id===item.entryId);s.ledger.push({...src,id:'LE-'+item.id,kind:'费用调整',ended:'2026-10-10 16:00',base:0,mileage:0,adjustment:item.amount,rule:'审批调整 '+item.id,status:'可对账',adjustmentSource:item.id});log('独立批准费用调整',item.id+'；原账单未修改，新条目待纳入后续账单');U.closeDialog();jump('B41');return;}
 case 'v4-payment-record':{if(!required(['payment.statementId','payment.amount','payment.reference','payment.date']))return;let st=s.statements.find(x=>x.id===U.get('payment.statementId'));if(!st||st.status!=='CONFIRMED'){U.toast('仅能对已确认账单登记付款凭证');return;}let amount=parseMoney('payment.amount');if(amount===null)return;if(amount>st.amount-U.confirmedPaid(st.id)){error('payment.amount','本次金额超过已确认的未付款额，需按差错核实处理');return;}const ref=String(U.get('payment.reference')).trim();if(s.payments.some(x=>x.reference===ref)){error('payment.reference','该付款流水已有记录，不要重复登记');return;}if(!U.store.saved['upload:external-payment']){U.toast('请附上外部付款凭证');return;}s.payments.push({id:'PAYREC-'+(s.payments.length+1),statementId:st.id,amount,reference:ref,date:U.get('payment.date'),status:'PENDING',creator:'ADMIN01'});log('登记外部付款',ref+'；未发起任何转账');jump('B40');return;}
 case 'v4-payment-verify':{const pay=s.payments.filter(x=>x.status==='PENDING').at(-1);if(!pay){U.toast('没有待复核付款记录');return;}if(pay.creator===s.reviewUser){U.toast('不可审核本人登记的付款');return;}if(!required(['payment.verifyReason','payment.verified']))return;const st=s.statements.find(x=>x.id===pay.statementId);if(!st||st.status!=='CONFIRMED'||pay.amount>st.amount-U.confirmedPaid(st.id)){U.toast('金额或账单状态已变化，请核实差错');return;}pay.status='VERIFIED';pay.reviewer=s.reviewUser;log('付款凭证独立复核',pay.id+'，才计入已付');jump('B40');return;}
 default:throw Error('Unknown V4 action '+action);
 }
};
U.action=async function(action,payload='',element=null){
 if(action==='start-flow'&&(payload==='driverReconcile'||payload==='directories')){U.store=U.makeStore();U.store.flow=payload;U.go(U.flows[payload].ids[0],true);return;}
 if(action.startsWith('v4-'))return U.v4Action(action,payload,element);
 const before=U.store.live.returnedAt;const result=await originalAction(action,payload,element);
 if(action==='start-flow'&&['cancelReturn','cancelBefore','refund'].includes(payload)){U.v4().cases=U.v4().cases.filter(x=>x.orderNo!==U.store.live.orderNo);U.v4().caseId=null;U.render();}
 if(action==='handover-return'&&!before&&U.store.live.returnedAt){const o=U.store.live;for(const a of U.v4().cases.filter(x=>x.orderNo===o.orderNo&&x.review==='APPROVED'&&x.returnState==='WAIT_RETURN')){a.returnState='RETURNED';a.custody='OWNER';a.stage='CANCELLED';a.refundState=a.approved>0?'READY':'NOT_REQUIRED';a.version++;log('返还后执行已批准决定',a.id+'，无需再次退款审核');}}
 return result;
};
U.flows.driverReconcile={name:'个人对账与差异处理',desc:'按人生成 → 发给代驾 → 确认/差异 → 付款凭证核对（不打款）',ids:['B28','B38','B38-generate','B38','D37','D38','B40-record','B40-verify'],seed:'WAIT_CONFIRM'};
U.flows.directories={name:'车辆、车主、代驾独立档案',desc:'列表筛选 → 详情 → 关联记录 → 编辑保留与返回',ids:['B09','B09-detail','B27','B27-detail','B28','B28-detail','B38'],seed:'WAIT_CONFIRM'};
U.flows.cancelBefore.ids=['O19','O21','B14-before','O21'];
U.flows.cancelReturn.ids=['O19-picked','O21','B14','B14-return','D19','S13-cancel','D20-cancel','D21','D22','O35-return','D23-cancel','D25-cancelled','O21'];
// Support working examples and proper protected state variants on direct review navigation.
const rawTesting=U.testing.render;
U.seedPreviewScene=function(id){if(id==='B14-before'){U.v4().caseId='AS20261010-003';}else if(id==='B14-refund'){U.v4().caseId='AS20261010-002';}else if(id==='B14-return'||id==='O21-return'){U.v4().caseId='AS20261010-004';}
 if(id==='B14-rejected'){const a=U.caseFor();a.review='REJECTED';a.auditCount=1;a.notes=['提交的说明尚不足以支持该申请，可补充资料后再次申请。'];}
 if(id==='B14-unknown'||id==='B14-approved'||id==='B14-complete'||id==='O21-success'||id==='O21-unknown'){U.v4().caseId='AS20261010-002';const a=U.caseFor();a.review='APPROVED';a.auditCount=1;a.approved=4200;a.reserved=4200;a.refundState=id.includes('unknown')?'UNKNOWN':id.includes('complete')||id.includes('success')?'SUCCESS':'READY';if(a.refundState==='SUCCESS'){a.refunded=4200;a.reserved=0;}}

};
U.testing.render=function(id){U.seedPreviewScene(id);return rawTesting(id);};
// The state selector belongs to the review tool, not the production app.
// Switching a reference state explicitly loads an isolated example; normal workflow buttons do not.
document.addEventListener('change',event=>{
 if(event.target.id==='state-picker'&&!U.store.flow){
  const id=event.target.value;U.store=U.makeStore();U.seedPreviewScene(id);U.toast('已载入独立状态样本，不代表执行了业务操作');
 }
},true);
document.addEventListener('input',event=>{const key=event.target.dataset.field||'';if(/^(vehicle\.edit\.|owner\.edit\.|driver\.edit\.|driver\.record\.|as\.(reason|amount)|df\.|adjust\.)/.test(key))U.v4().dirty=true;});
document.addEventListener('change',event=>{const key=event.target.dataset.field||'';if(key.startsWith('v4.')){U.applyDisplayPrefs();return;}if(key==='case.filter'){U.v4().caseId=null;clean();U.render();}});
document.addEventListener('keydown',event=>{
 if(document.querySelector('#modal-root [role="dialog"]'))return;
 const dialog=document.querySelector('.pc-drawer,.phone-sheet');if(!dialog)return;
 if(event.key==='Tab'){let targets=[...dialog.querySelectorAll('button:not(:disabled),a,input:not(:disabled),select:not(:disabled),textarea:not(:disabled)')].filter(x=>x.offsetParent!==null);const first=targets[0],last=targets.at(-1);if(event.shiftKey&&document.activeElement===first){event.preventDefault();last?.focus();}else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first?.focus();}}
 if(event.key==='Escape'){event.preventDefault();U.go(U.current().base);}
});
// Public prototype tests deliberately exercise only local models, not production permissions or money.
U.v4Test={required,parseMoney,caseFor:U.caseFor,entryAmount:U.entryAmount};
U.seedPreviewScene(U.currentId());
U.render();
})(window.UI);
