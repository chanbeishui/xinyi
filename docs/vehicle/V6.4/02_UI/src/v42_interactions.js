/* Interactions exercise local demo data only; backend gates are specified in T2.0. */
(function(U){
'use strict';const R=U.recon42,A=U.recon41,{button:b,field:f,notice:n,kv,esc:e}=U;
const oldAction=U.action,oldAfter=U.afterRender,oldSeed=U.seedPreviewScene;
let visibleBillVersion=null,visibleRecordVersion=null;
const clear=p=>{for(const k of Object.keys(U.store.forms))if(k.startsWith(p))delete U.store.forms[k];};
function go(id){U.v4().dirty=false;U.go(id);}
function hub(tab='services'){R.state().tab=tab;U.v4().statementId=null;go(tab==='bills'?'B42-bills':tab==='payments'?'B42-payments':'B42');}
function openBill(id){const st=R.statement(id);if(!st||!R.authorized(st.entityId))return A.fail('账单不存在或没有本主体权限');U.v4().statementId=id;U.v4().selectedDriver=st.driverId;R.state().entityId=st.entityId;go('B38');}
function paymentOpen(id){const st=R.statement(id);if(!st||st.status!=='CONFIRMED'||!R.authorized(st.entityId)||R.payment(st).recordable<=0)return A.fail('请从有可登记余额的已确认账单录入凭证');U.v4().statementId=id;U.v4().selectedDriver=st.driverId;R.state().entityId=st.entityId;R.state().proof=null;clear('payment.');go('B40-record');}
U.action=async function(action,payload='',element=null){
 if(action==='v4-driver-recon'){U.v4().selectedDriver=payload;R.clear();R.state().filters={query:'',from:'',to:'',state:'OUTSTANDING'};clear('r42.');R.state().page=1;hub();return;}
 if(action==='v4-recon-new'||action==='v41-create'||action==='v41-preview-edit'){R.clear();hub();U.toast('请明确勾选本次服务；不再按日期自动生成');return;}
 if(action==='start-flow'&&payload==='driverReconcile'){U.store=U.makeStore();U.store.flow=payload;go('B28');return;}
 if(action==='v4-payment-record'){const st=A.current(),s=R.state();if(!st)return A.fail('请选择已确认账单');const rec=R.record(st.id,U.get('payment.amount',''),U.get('payment.reference',''),String(U.get('payment.date','')),s.proof,visibleBillVersion);if(rec){s.recordId=rec.id;U.toast('凭证已登记，等待独立核实；尚未计入已付');go('B40-detail');}return;}
 if(action==='v4-payment-verify'||action==='v42-payment-reject'){if(action==='v4-payment-verify'&&!U.get('payment.verified'))return A.fail('请确认已核对真实付款对象和金额');if(R.verify(R.state().recordId,action==='v4-payment-verify'?'APPROVE':'REJECT',String(U.get('payment.verifyReason','')),visibleRecordVersion)){U.toast(action==='v4-payment-verify'?'凭证已核实，账单付款状态已更新':'凭证已退回，不计入已付');go('B40-detail');}return;}
 if(!action.startsWith('v42-'))return oldAction(action,payload,element);
 const s=R.state(),st=A.current();
 switch(action){
 case 'v42-tab':if(!['services','bills','payments'].includes(payload))return;hub(payload);return;
 case 'v42-driver-tab':s.driverTab=payload;U.render();return;
 case 'v42-choose-services':s.filters.state='UNBILLED';s.page=1;U.set('r42.state','UNBILLED');hub();return;
 case 'v42-entity-apply':{const id=String(U.get('r42.entity',''));if(!R.entities.some(x=>x.id===id))return A.fail('费用主体无效');if(id===s.entityId)return;if(s.selected.length){s.nextEntity=id;U.dialog('切换费用主体并清空选择？',n('不同费用主体不能合成一张账单。已有草稿和历史记录保持不变。'),[b('保留当前选择','v42-entity-cancel','secondary'),b('切换并清空','v42-entity-confirm')]);return;}s.nextEntity=id;return U.action('v42-entity-confirm');}
 case 'v42-entity-cancel':U.set('r42.entity',s.entityId);U.closeDialog();return;
 case 'v42-entity-confirm':s.entityId=s.nextEntity;R.clear();s.page=1;s.filters={query:'',from:'',to:'',state:'OUTSTANDING'};clear('r42.');U.closeDialog();hub();return;
 case 'v42-filter':{const from=String(U.get('r42.from','')),to=String(U.get('r42.to',''));if(!R.validDate(from)||!R.validDate(to)||(from&&to&&from>to))return A.fail('请填写有效日期，结束日期不能早于开始日期');s.filters={query:String(U.get('r42.query','')).trim(),from,to,state:U.get('r42.state','OUTSTANDING')};s.page=1;U.render();return;}
 case 'v42-filter-reset':s.filters={query:'',from:'',to:'',state:'OUTSTANDING'};s.page=1;clear('r42.');U.render();return;
 case 'v42-bill-filter':s.billFilters={query:String(U.get('r42.billQuery','')).trim(),status:U.get('r42.billStatus','ALL'),payment:U.get('r42.billPayment','ALL')};U.render();return;
 case 'v42-payment-filter':s.paymentFilters={query:String(U.get('r42.payQuery','')).trim(),status:U.get('r42.payStatus','ALL')};U.render();return;
 case 'v42-page':s.page=Math.max(1,Number(payload)||1);U.render();return;
 case 'v42-clear':R.clear();U.render();return;
 case 'v42-add-clear':R.clear(true);U.render();return;
 case 'v42-selected-open':go('B42-selected');return;
 case 'v42-generate-open':if(!s.selected.length)return A.fail('请先勾选服务');go('B42-generate');return;
 case 'v42-create':{const made=R.create();if(made){U.toast('已按所选 '+made.entries.length+' 条服务建立草稿');go('B38');}else if(s.conflicts.length)go('B42-conflict');return;}
 case 'v42-deselect':s.selected=s.selected.filter(x=>x!==payload);delete s.selectedVersions[payload];s.conflicts=s.conflicts.filter(x=>x.id!==payload);U.render();return;
 case 'v42-bill-open':openBill(payload);return;
 case 'v42-service-detail':{const x=R.entry(payload);if(!x||x.driverId!==U.v4().selectedDriver||!R.authorized(x.entityId))return A.fail('无权查看');U.dialog('服务与费用依据',kv('承接任务',x.assignmentId)+kv('客户订单',x.orderNo)+kv('车辆',x.plate)+kv('实际任务结束',x.ended)+kv('费用规则',x.rule)+kv('源金额',A.amount(x)===null?'待核价':U.money(A.amount(x)))+n('这是司机报酬依据，不是修改客户收费。'),[b('返回列表','close-dialog','secondary')]);return;}
 case 'v42-add-open':if(!st||!A.canEdit(st))return A.fail('请先撤回当前核对版本，再修改服务范围');R.clear(true);s.operation={type:'ADD',version:st.version};go('B38-add');return;
 case 'v42-add-save':if(R.add([...s.addSelected],s.addVersions,s.operation?.version)){U.toast('已添加所选服务，内容版本已更新');go('B38');}return;
 case 'v42-remove-open':if(!st||!A.canEdit(st))return A.fail('当前版本不能移出服务');s.operation={type:'REMOVE',id:payload,version:st.version};U.dialog('将这次服务移出本草稿？',n('仅移出本账单，原服务和价格修改历史保留。成功后该服务可以另行组单。')+f('移出原因','r42.removeReason','','textarea',{required:true}),[b('保留','close-dialog','secondary'),b('移出本账单','v42-remove-commit')]);return;
 case 'v42-remove-commit':if(R.remove(s.operation?.id,s.operation?.version,String(U.get('r42.removeReason','')))){clear('r42.remove');U.closeDialog();U.render();}return;
 case 'v42-void-open':if(!st)return;s.operation={type:'VOID',version:st.version};U.dialog('作废当前草稿？',n('作废后保留历史记录，并释放本草稿对服务明细的占用。已确认或已有付款记录的账单不能作废。')+f('作废原因','r42.voidReason','','textarea',{required:true}),[b('保留','close-dialog','secondary'),b('确认作废','v42-void-commit','danger')]);return;
 case 'v42-void-commit':if(R.void(s.operation?.version,String(U.get('r42.voidReason','')))){clear('r42.void');U.closeDialog();hub('bills');}return;
 case 'v42-payment-open':paymentOpen(payload);return;
 case 'v42-payment-view':case 'v42-payment-review':{const rec=U.v4().payments.find(x=>x.id===payload),bill=rec&&R.statement(rec.statementId);if(!bill||!R.authorized(bill.entityId))return A.fail('无权查看付款记录');s.recordId=rec.id;U.v4().statementId=bill.id;U.v4().selectedDriver=bill.driverId;s.entityId=bill.entityId;clear('payment.');go(action==='v42-payment-review'?'B40-verify':'B40-detail');return;}
 default:throw Error('Unknown V5.0 action '+action);
 }
};
U.seedPreviewScene=function(id){const base=id.split('-')[0],state=id.split('-').slice(1).join('-'),s=R.state();
 if(base==='B42'){
  R.demoHistory();if(state==='empty')U.v4().selectedDriver='D035';if(state==='forbidden')s.allowedEntities=[];
  s.tab=['bills','payments'].includes(state)?state:'services';
  if(['select','selected','generate','conflict'].includes(state)){s.filters.state='UNBILLED';if(['selected','generate','conflict'].includes(state)&&!s.selected.length)['LE001','LE-H01','LE-H02'].forEach(x=>R.toggle(x,true));}
  if(state==='conflict'&&s.selected.includes('LE001')&&!R.member('LE001')){A.create('D012','E001',['LE001']);U.v4().statementId=null;R.validate('D012','E001',s.selected,s.selectedVersions);}
  if(state==='bills-partial') {s.tab='bills';s.billFilters.payment='PARTIAL';}
  return;
 }
 if(['B38-partial','B38-paid','B38-zero','B38-add','B38-removed','D37-services','D37-payments','D38-partial','B40-detail'].includes(id)){
  R.demoHistory();if(id==='D37-services'||id==='D37-payments'){s.driverTab=state;return;}
  if(id==='B40-detail'){s.recordId='PAY-HISTORY-2';U.v4().statementId='DS-HISTORY-002';return;}
  if(['B38-partial','D38-partial'].includes(id)){U.v4().statementId='DS-HISTORY-002';U.v4().driverUser='D012';return;}
  if(id==='B38-paid'){U.v4().statementId='DS-HISTORY-001';return;}
  if(id==='B38-zero'){if(!R.member('LE005')){const st=A.create('D012','E001',['LE005']);A.send();A.confirm(st.version,st.sentHash);}return;}
  if(!A.current())A.create('D012','E001',['LE001','LE003']);if(id==='B38-add')s.operation={type:'ADD',version:A.current().version};return;
 }
 oldSeed(id);
 if(base==='B40'&&state==='verify'){const rec=U.v4().payments.at(-1);if(rec){rec.entityId??=A.current()?.entityId||'E001';rec.version??=1;rec.evidence??={name:'演示付款凭证.txt',demo:true};s.recordId=rec.id;}}
};
U.afterRender=function(v){oldAfter?.(v);const s=R.state(),st=A.current();visibleBillVersion=st?.version||null;visibleRecordVersion=U.v4().payments.find(x=>x.id===s.recordId)?.version||null;
 document.querySelectorAll('[data-r42-select]').forEach(el=>el.addEventListener('change',()=>{R.toggle(el.dataset.r42Select,el.checked,el.dataset.add==='1');U.render();}));
 document.querySelectorAll('[data-r42-all]').forEach(el=>{const add=el.dataset.r42All==='add',inputs=[...document.querySelectorAll('[data-r42-select]')].filter(x=>x.dataset.add===(add?'1':'0')&&!x.disabled);const count=inputs.filter(x=>x.checked).length;el.checked=inputs.length>0&&count===inputs.length;el.indeterminate=count>0&&count<inputs.length;el.addEventListener('change',()=>{inputs.forEach(x=>R.toggle(x.dataset.r42Select,el.checked,add));U.render();});});
 const file=document.querySelector('#r42-proof');if(file)file.addEventListener('change',()=>{const x=file.files[0];if(x){s.proof={name:x.name,size:x.size,type:x.type,url:URL.createObjectURL(x),localDemo:true};file.parentElement.querySelector('.hint').textContent='已选择：'+x.name+'。仅保留在本浏览器，不是实际上传或付款验证。';}});
 if(v.base==='B38'&&v.state==='add'){const product=document.querySelector('.pc-product'),y=product?.getBoundingClientRect().top;if(product&&!document.body.classList.contains('full')){product.style.height=Math.max(320,innerHeight-Math.max(0,y)-16)+'px';}}
};
U.flows.driverReconcile={name:'勾选服务组单与个人对账',desc:'个人汇总 → 明确勾选 → 草稿核价 → 必要整单复核 → 本人确认 → 凭证核实 → 未结清追踪',ids:['B28','B42','B42-generate','B38','D37','D38','B38','B40-record','B42-bills','B42-payments'],seed:'WAIT_CONFIRM'};
U.flows.reconHistory={name:'历史账单与部分付款',desc:'个人页三标签 → 账单级部分付款 → 录入剩余凭证 → 未结清服务不重复组单',ids:['B42','B42-bills','B38-partial','B40-record','B42-payments','D37-payments'],seed:'WAIT_CONFIRM'};
// Bring initial #route into the current renderer, but no server/third-party action is performed.
U.seedPreviewScene(U.currentId());U.render();
})(window.UI);
