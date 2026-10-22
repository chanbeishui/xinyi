/* Local interaction demonstration only. Production must use T2.0 realm/scope/transaction gates. */
(function(U){
'use strict';const A=U.recon41,{button:b,field:f,check:c,kv,notice:n}=U;
const oldAction=U.action,oldAfter=U.afterRender,oldSeed=U.seedPreviewScene,oldGo=U.go;
let visibleVersion=null,visibleHash=null,dialogVersion=null;
function clear(prefix){for(const k of Object.keys(U.store.forms))if(k.startsWith(prefix))delete U.store.forms[k];}
function go(id){U.v4().dirty=false;U.go(id);}
function back(){U.store.v41.edit=null;clear('r41.');go('B38');}
function render(){U.render();}
function confirmWithdraw(entryId=null){const st=A.current();if(!st)return;U.store.v41.withdraw={version:st.version,entryId};U.dialog('撤回当前核对版本？',n('撤回成功后才能修改。旧版本的代驾确认和内部复核将失效；已经确认的账单不能撤回。','warn')+f('撤回原因','r41.withdrawReason','','textarea',{required:true}),[b('保留当前','close-dialog','secondary'),b('确认撤回','v41-withdraw-commit')]);}
function edit(id){const r=A.openEdit(id);if(r==='ADJUSTMENT'){U.store.v41.adjustEntryId=id;clear('r41.');go('B38-adjust');}else if(r==='WITHDRAW')confirmWithdraw(id);else if(r){clear('r41.');go(A.current().entries.find(x=>x.id===id).proposed===null?'B38-price-empty':'B38-price');}}
const mapped={'v4-recon-generate':'v41-create','v4-recon-send':'v41-send','v4-recon-confirm':'v41-confirm-open','v4-recon-confirm-commit':'v41-confirm-commit','v4-recon-dispute':'v41-dispute','v4-recon-resolve':'v41-resolve-note','v4-recon-export':'v41-export'};
U.action=async function(action,payload='',element=null){
 if(mapped[action])action=mapped[action];
 if(action==='start-flow'&&payload==='reconUnpriced'){U.store=U.makeStore();U.store.flow=payload;A.seed('B38-unpriced');go('B38-unpriced');return;}
 if(action==='start-flow'&&payload==='driverReconcile'){U.store=U.makeStore();U.store.flow=payload;go('B28');return;}
 if(action==='v4-open-statement'){U.v4().statementId=payload;go('D38');return;}
 if(action==='v4-driver-recon'){const s=U.v4();s.selectedDriver=payload;s.statementId=s.statements.filter(x=>x.driverId===payload&&x.status!=='VOID').at(-1)?.id||null;go('B38');return;}
 if(!action.startsWith('v41-'))return oldAction(action,payload,element);
 let st=A.current();
 switch(action){
 case 'v41-actor':U.store.v41.actor=payload;render();U.toast('仅切换评审用的演示操作者；生产由真实会话决定');return;
 case 'v41-create':if(!U.get('recon.checked'))return A.fail('请先核对主体、代驾与期间');if(A.create(U.v4().selectedDriver,String(U.get('recon.from','2026-10-01')),String(U.get('recon.to','2026-10-10'))))go('B38');return;
 case 'v41-preview-edit':{if(!st){const xs=A.create(U.v4().selectedDriver,String(U.get('recon.from','2026-10-01')),String(U.get('recon.to','2026-10-10')));if(!xs)return;}edit(payload);return;}
 case 'v41-price-open':edit(payload);return;
 case 'v41-edit-cancel':{let dirty=String(U.get('r41.amount',U.store.v41.edit?.initialValue||''))!==(U.store.v41.edit?.initialValue||'')||String(U.get('r41.reason','')).trim()||String(U.get('r41.evidence','')).trim();if(dirty)U.dialog('放弃本次尚未保存的修改？','<p>已保存的账单草稿保持不变。</p>',[b('继续编辑','close-dialog','secondary'),b('放弃本次修改','v41-edit-discard','danger')]);else back();return;}
 case 'v41-edit-discard':U.closeDialog();back();return;
 case 'v41-price-save':if(A.savePrice(U.get('r41.amount',''),U.get('r41.reason',''),U.get('r41.evidence',''))){U.toast('价格已保存到草稿，待整单复核');back();}return;
 case 'v41-history':U.store.v41.historyEntryId=payload||null;go('B38-history');return;
 case 'v41-save-draft':if(st){A.event(st,'保存草稿；未向代驾发送');U.toast('草稿已保存在本地演示内存');render();}return;
 case 'v41-review-submit':if(A.submitReview()){U.toast('已提交整单复核');go('B38');}return;
 case 'v41-review-approve':case 'v41-review-reject':{if(action==='v41-review-approve'&&!U.get('r41.reviewChecked'))return A.fail('请核对全部变更');const reason=String(U.get('r41.reviewReason','')).trim();if(A.review(action==='v41-review-approve'?'APPROVE':'REJECT',reason,visibleVersion)){clear('r41.');U.toast(action==='v41-review-approve'?'复核通过，已发给代驾':'已退回整理');go('B38');}return;}
 case 'v41-send':if(A.send()){U.toast('已发给代驾核对');go('B38');}return;
 case 'v41-withdraw-open':confirmWithdraw();return;
 case 'v41-withdraw-commit':{const w=U.store.v41.withdraw;if(!w)return;if(A.withdraw(w.version,String(U.get('r41.withdrawReason','')))){U.closeDialog();clear('r41.');if(w.entryId)edit(w.entryId);else go('B38');}return;}
 case 'v41-resolve-note':{if(!st||st.status!=='DISPUTED')return A.fail('当前没有可处理的差异');const r=String(U.get('r41.resolveReason','')).trim();if(!r)return A.fail('请填写处理说明');st.resolveReason=r;A.bump(st);A.event(st,'差异处理说明：'+r);U.toast('说明已保存，请核对金额后重新发送');render();return;}
 case 'v41-confirm-open':if(!U.get('r41.driverChecked')&&!U.get('recon.driverChecked'))return A.fail('请先核对本期全部服务金额');dialogVersion={version:visibleVersion,hash:visibleHash};U.dialog('确认这份账单无误？',kv('本期应付',st?U.money(st.amount):'—')+n('仅确认当前版本的服务和报酬，不代表已收款。'),[b('继续核对','close-dialog','secondary'),b('确认无误','v41-confirm-commit')]);return;
 case 'v41-confirm-commit':if(dialogVersion&&A.confirm(dialogVersion.version,dialogVersion.hash)){U.closeDialog();go('D38-confirmed');}return;
 case 'v41-dispute':if(A.dispute(visibleVersion,String(U.get('r41.dispute',U.get('recon.dispute',''))))){U.toast('差异已提交，等待重新发送核对版本');go('D37');}return;
 case 'v41-adjust-submit':if(st&&A.adjust(U.store.v41.adjustEntryId||st.entries[0].id,U.get('r41.delta',''),String(U.get('r41.adjustReason','')))){clear('r41.');U.toast('调差已提交独立复核，原账单未改');go('B41');}return;
 case 'v41-export':{if(!st)return;const escape=x=>'"'+String(x??'').replace(/^[=+@\-\t\r]/,"'$&").replace(/"/g,'""')+'"';const rs=[['对账单',st.id,'版本',st.revision,'状态',A.status(st.status)],['说明','本地演示；草稿小计不是已确认应付；对账不是付款'],['任务','订单','车牌','规则分','拟应付分','来源'],...st.entries.map(x=>[x.assignmentId,x.orderNo,x.plate,x.reference??'待核价',x.proposed??'待核价',x.priceSource])];const url=URL.createObjectURL(new Blob(['\ufeff'+rs.map(r=>r.map(escape).join(',')).join('\r\n')],{type:'text/csv;charset=utf-8'}));const el=document.createElement('a');el.href=url;el.download=st.id+'_V'+st.revision+'_'+st.status+'_演示.csv';el.click();setTimeout(()=>URL.revokeObjectURL(url),500);return;}
 default:throw Error('Unknown V5.0 action '+action);
 }
};
U.seedPreviewScene=function(id){if(['B38','D37','D38','B40'].includes(id.split('-')[0])){A.seed(id);if(id==='B40-verify'&&U.statement()&&!U.v4().payments.length)U.v4().payments.push({id:'PAYREC-DEMO-01',statementId:U.statement().id,amount:5000,reference:'BANK-DEMO-0001',date:'2026-10-10',status:'PENDING',creator:'ADMIN01'});return;}oldSeed(id);};
U.afterRender=function(v){oldAfter?.(v);const st=A.current();if(['B38','D38'].includes(v.base)){visibleVersion=st?.version||null;visibleHash=st?.sentHash||null;}
 const toolbar=document.querySelector('#review-toolbar');document.querySelector('.r41-demo-actor')?.remove();
 if(toolbar&&['B38','B41','B40'].includes(v.base)){const el=document.createElement('div');el.className='r41-demo-actor';el.innerHTML='<span>演示操作者：'+A.actor()+'</span>'+b('经办人','v41-actor','secondary',{payload:'ADMIN01'})+b('独立复核人','v41-actor','secondary',{payload:'ADMIN02'});toolbar.appendChild(el);}
 if(['B38','B41','B40'].includes(v.base)&&U.v4().reviewUser!==A.actor())U.v4().reviewUser=A.actor();
 // Keep drawer actions inside the current viewport; the drawer body scrolls independently.
 const product=document.querySelector('.pc-product:has(.pc-overlay)');
 if(product&&!document.body.classList.contains('full')){const y=product.getBoundingClientRect().top;product.style.minHeight='0';product.style.height=Math.max(320,innerHeight-Math.max(0,y)-(document.body.classList.contains('export')?0:16))+'px';}

};
const oldTest=U.testing.render;
U.testing.render=function(id){U.seedPreviewScene(id);return oldTest(id);};
U.flows.driverReconcile={name:'对账内改价与整单复核',desc:'代驾列表 → 整理草稿 → 行内填写/修改 → 独立整单复核 → 本人确认 → 后续调差',ids:['B28','B38','B38-generate','B38','D37','D38','B38-confirmed','B40-record'],seed:'WAIT_CONFIRM'};
U.flows.reconUnpriced={name:'无规则的服务在对账内填写价格',desc:'选择梁志辉 → 建立含待核价的草稿 → 填写价格 → 整单独立复核',ids:['B37','B38-unpriced','B38','D37'],seed:'WAIT_CONFIRM'};
// Explicit navigation from a dirty price editor uses the same discard guard (no silent saving).
U.go=function(id,replace=false){if(U.store.v41.edit&&U.resolve(id)!==U.currentId()&&(String(U.get('r41.amount',U.store.v41.edit.initialValue))!==U.store.v41.edit.initialValue||String(U.get('r41.reason','')).trim()||String(U.get('r41.evidence','')).trim())&&!String(id).startsWith('B38-price')){U.dialog('尚有未保存的价格修改','<p>请先保存修改，或返回编辑弹窗后选择取消。</p>',[b('继续编辑','close-dialog','secondary')]);return;}return oldGo(id,replace);};
U.seedPreviewScene(U.currentId());U.render();
})(window.UI);
