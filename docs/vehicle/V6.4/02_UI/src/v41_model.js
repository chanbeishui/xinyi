/* V5.0 reconciliation working copies. LOCAL DEMO ONLY: no real authorization, ledger or transfer. */
(function(U){
'use strict';
const make=U.makeStore;
U.makeStore=function(){const s=make();s.v41={actor:'ADMIN01',permissions:{edit:true,review:true,send:true},editorSet:[],edit:null,pending:null};return s;};
U.store=U.makeStore();
U.version='V6.4 · 全量一致性修正版';
U.recon41={};const A=U.recon41;
A.labels={DRAFT:'整理中',INTERNAL_REVIEW:'待内部复核',SENT:'待代驾确认',DISPUTED:'代驾有差异',CONFIRMED:'已确认',VOID:'已作废'};
A.status=s=>A.labels[s]||s;
A.actor=()=>U.store.v41.actor;
A.now=()=>new Date().toISOString();
A.initLine=function(x){
 if(!Object.prototype.hasOwnProperty.call(x,'proposed')){x.reference=U.entryAmount(x);x.originalAmount=x.reference;x.proposed=x.reference;x.manual=false;x.priceHistory=[];x.lineVersion=1;x.priceSource=x.reference===null?'UNPRICED':'RULE';}
 return x;
};
A.normalize=function(st){if(!st)return null;st.entries.forEach(A.initLine);st.revision??=1;st.reviewHistory??=[];st.snapshots??=[];st.contributors??=[];st.issuedBy??='ADMIN01';st.events??=[];st.version??=1;return A.recompute(st);};
A.recompute=function(st){st.unpriced=st.entries.filter(x=>x.proposed===null).length;st.knownAmount=st.entries.reduce((n,x)=>n+(x.proposed??0),0);st.amount=st.unpriced?null:st.knownAmount;st.manualCount=st.entries.filter(x=>x.manual).length;return st;};
A.current=()=>A.normalize(U.statement());
A.paidLock=st=>U.v4().payments.some(x=>x.statementId===st.id); // Any registered payment locks original statements.
A.isLocked=st=>st.status==='CONFIRMED'||A.paidLock(st);
A.canEdit=st=>!!st&&['DRAFT','DISPUTED'].includes(st.status)&&!A.isLocked(st)&&U.store.v41.permissions.edit;
A.amount=x=>Object.prototype.hasOwnProperty.call(x,'proposed')?x.proposed:U.entryAmount(x);
A.hash=function(st){return JSON.stringify({id:st.id,revision:st.revision,entries:st.entries.map(x=>[x.id,x.proposed,x.lineVersion])});}; // Demo snapshot string; production canonical SHA-256.
A.bump=function(st,material=false){st.version++;if(material)st.revision++;A.recompute(st);};
A.event=function(st,text){st.events.push({at:A.now(),actor:A.actor(),text});U.log('对账V5.0',text);};
A.fail=message=>{U.toast(message);return false;};
// Explicit-selection creation is implemented in v42_model.js; no period-generated membership.
A.openEdit=function(id){const st=A.current();if(!st)return A.fail('请先建立对账草稿');
 const x=st.entries.find(x=>x.id===id);if(!x)return A.fail('费用明细不存在');
 if(x.kind==='费用调整')return A.fail('已批准的独立调整记录保持只读');
 if(A.isLocked(st))return 'ADJUSTMENT';
 if(['SENT','INTERNAL_REVIEW'].includes(st.status))return 'WITHDRAW';
 if(!A.canEdit(st))return A.fail('当前账单或人员权限不允许修改');
 U.store.v41.edit={statementId:st.id,entryId:id,version:st.version,lineVersion:x.lineVersion,initialValue:x.proposed===null?'':(x.proposed/100).toFixed(2)};return true;
};
A.savePrice=function(value,reason,evidence=''){
 const st=A.current(),edit=U.store.v41.edit;if(!st||!edit||edit.statementId!==st.id||!A.canEdit(st))return A.fail('账单当前不可修改');
 const x=st.entries.find(x=>x.id===edit.entryId);if(st.version!==edit.version||x?.lineVersion!==edit.lineVersion)return A.fail('内容已被其他人修改，请保留填写内容并刷新后重试');
 const amount=U.minor(value);if(amount===null||amount<0||amount>100000000)return A.fail('请输入0至1,000,000元的金额，最多两位小数；演示上限非生产默认');
 if(!String(reason).trim())return A.fail('请填写修改原因或计价依据');
 if(amount===x.proposed)return A.fail('金额没有变化，无须重复保存');
 const old=x.proposed;x.priceHistory.push({at:A.now(),actor:A.actor(),old,new:amount,reason:String(reason).trim(),evidence:String(evidence).trim(),revision:st.revision+1});
 x.proposed=amount;x.manual=true;x.priceSource=old===null?'MANUAL_FILLED':'MANUAL_CHANGED';x.reason=String(reason).trim();x.lineVersion++;st.contributors=[...new Set([...st.contributors,A.actor()])];st.reviewApprovedRevision=null;A.bump(st,true);A.event(st,'保存拟价：'+x.assignmentId+'，'+(old===null?'待填写':U.money(old))+' → '+U.money(amount));U.store.v41.edit=null;return true;
};
A.ready=function(st){if(!st||!['DRAFT','DISPUTED'].includes(st.status))return A.fail('当前状态不能提交或发送');if(st.status==='DISPUTED'&&!String(st.resolveReason||'').trim())return A.fail('请先保存本次差异的处理说明');if(st.unpriced)return A.fail('还有'+st.unpriced+'条待填写金额，请在明细行填写价格');if(st.amount<0)return A.fail('净额为负，按已批准冲抵规则人工处理；本演示不自动扣款');return true;};
A.submitReview=function(){const st=A.current();if(!A.ready(st))return false;if(!st.manualCount)return A.fail('本单无人工改价，可直接发给代驾核对');if(!U.store.v41.permissions.edit)return A.fail('无提交权限');
 st.status='INTERNAL_REVIEW';st.submittedBy=A.actor();st.reviewSnapshot={revision:st.revision,hash:A.hash(st),contributors:[...st.contributors],entries:structuredClone(st.entries)};A.bump(st);A.event(st,'提交整单复核；冻结当前拟价版本');return true;};
A.send=function(){const st=A.current();if(!A.ready(st))return false;if(!U.store.v41.permissions.send)return A.fail('无账单发送权限');if(st.manualCount)return A.fail('存在人工填价或改价，需整单独立复核，不能直接发出');return A.publish(st,'RULE_DIRECT');};
A.publish=function(st,via){st.status='SENT';st.sentAt=A.now();st.sentRevision=st.revision;st.sentHash=A.hash(st);st.snapshots.push({revision:st.revision,hash:st.sentHash,amount:st.amount,entries:structuredClone(st.entries),at:st.sentAt,via});A.bump(st);A.event(st,'发给代驾核对 V'+st.revision+'；尚未付款');return true;};
A.review=function(decision,reason,expected){const st=A.current();if(!st||st.status!=='INTERNAL_REVIEW'||st.version!==expected)return A.fail('复核版本已变化，请刷新后核对');if(!U.store.v41.permissions.review)return A.fail('无复核权限');if(!String(reason).trim())return A.fail('请填写复核意见');
 if(st.reviewSnapshot?.contributors.includes(A.actor())||st.submittedBy===A.actor()||A.actor()==='DRIVER:'+st.driverId)return A.fail('不得复核本人参与填价或改价的账单');
 if(st.reviewSnapshot?.revision!==st.revision||st.reviewSnapshot?.hash!==A.hash(st))return A.fail('拟价内容发生变化，当前复核版本已失效');
 if(decision==='APPROVE'&&(st.unpriced||!U.store.v41.permissions.send))return A.fail('金额不完整或无发送权限');
 st.reviewHistory.push({decision,reason,actor:A.actor(),revision:st.revision,at:A.now()});
 if(decision==='REJECT'){st.status='DRAFT';st.rejectReason=reason;A.bump(st);A.event(st,'整单复核退回：'+reason);return true;}
 if(st.unpriced||!U.store.v41.permissions.send)return A.fail('金额不完整或无发送权限');st.reviewApprovedRevision=st.revision;A.event(st,'独立复核通过：'+reason);return A.publish(st,'INDEPENDENT_REVIEW');};
A.withdraw=function(expected,reason){const st=A.current();if(!st||!['SENT','INTERNAL_REVIEW'].includes(st.status)||st.version!==expected||A.isLocked(st))return A.fail('撤回失败：账单状态已变化，可能已被代驾确认');if(!String(reason).trim())return A.fail('请填写撤回原因');
 st.status='DRAFT';st.reviewApprovedRevision=null;st.sentHash=null;st.sentRevision=null;A.bump(st,true);A.event(st,'撤回核对版本：'+reason+'；旧确认请求失效');return true;};
A.confirm=function(expected,hash){const st=A.current();if(st&&st.driverId===U.v4().driverUser&&st.status==='CONFIRMED'&&st.confirmation?.hash===hash&&st.confirmation?.requestVersion===expected)return true;if(!st||st.driverId!==U.v4().driverUser||st.status!=='SENT'||st.version!==expected||st.sentHash!==hash||hash!==A.hash(st))return A.fail('账单已变化或不属于本人，请刷新最新版本');
 if(st.unpriced||st.amount===null)return A.fail('金额尚未确定，不能确认');if(st.manualCount&&st.reviewApprovedRevision!==st.revision)return A.fail('人工改价尚未经当前版本的独立复核');
 st.status='CONFIRMED';st.confirmation={driver:st.driverId,at:A.now(),snapshotVersion:st.revision,requestVersion:expected,hash};st.finalizationKeys=st.entries.filter(x=>x.manual).map(x=>st.id+':CONFIRM:'+st.revision+':'+x.id);A.bump(st);A.event(st,'代驾确认 V'+st.revision+'；示意生成核价结转唯一键，非实际入账');return true;};
A.dispute=function(expected,reason){const st=A.current();if(!st||st.driverId!==U.v4().driverUser||st.status!=='SENT'||st.version!==expected)return A.fail('只可对本人的当前待确认版本提出差异');if(!String(reason).trim())return A.fail('请说明差异');st.status='DISPUTED';st.dispute=reason;st.resolveReason=null;st.sentHash=null;st.reviewApprovedRevision=null;A.bump(st);A.event(st,'代驾提出差异：'+reason);return true;};
A.adjust=function(entryId,value,reason){const st=A.current();if(!st||!A.isLocked(st))return A.fail('未确认账单请使用行内修改价格');const x=st.entries.find(x=>x.id===entryId);if(!x||x.kind==='费用调整')return A.fail('请选择原服务明细');const delta=U.minor(value);if(delta===null||delta===0||!String(reason).trim())return A.fail('请填写非零调差金额及原因');
 const s=U.v4();if(s.adjustments.some(a=>a.entryId===x.id&&a.statementId===st.id&&a.status==='PENDING'))return A.fail('该条已有待复核调差，请查看原申请');
 s.adjustments.push({id:'ADJ-DEMO-'+(s.adjustments.length+1),statementId:st.id,entryId:x.id,driverId:st.driverId,amount:delta,reason,status:'PENDING',creator:A.actor(),proof:true});A.event(st,'提交后续调差申请；原账单金额不变');return true;};
A.seed=function(id){
 const b=id.split('-')[0];if(!['B38','D37','D38','B40'].includes(b))return;
 const s=U.v4();if(id==='B38-empty'){s.selectedDriver='D035';s.statementId=null;return;}
 if(U.statement()){A.normalize(U.statement());return;}
 if(id==='B38'||id==='B38-generate'||id==='D37')return;
 const unpriced=['B38-unpriced','B38-price-empty'].includes(id);s.selectedDriver=unpriced?'D030':'D012';
 const st=A.create(s.selectedDriver,'E001',U.driverEntries(s.selectedDriver).filter(x=>x.entityId==='E001'&&x.endedTask!==false&&!U.recon42.member(x.id)).map(x=>x.id));if(!st)return;
 if(['B38-price','B38-price-empty','B38-price-conflict'].includes(id)){A.openEdit(st.entries[0].id);if(id==='B38-price-conflict')st.version++;return;}
 if(['B38-review','B38-review-rejected','B38-self-review','B38-history'].includes(id)){
  A.openEdit(st.entries[0].id);A.savePrice('70.00','演示：按已约定补偿核定本次服务');
  if(id==='B38-history')return;A.submitReview();if(id==='B38-review-rejected'){U.store.v41.actor='ADMIN02';A.review('REJECT','请补充该笔补偿的约定依据',st.version);U.store.v41.actor='ADMIN01';}else U.store.v41.actor=id==='B38-self-review'?'ADMIN01':'ADMIN02';return;
 }
 if(['B38-sent','B38-confirmed','B38-difference','B38-adjust','B38-readonly','D38','D38-confirmed','D38-difference','B40-record','B40-verify','B38-withdraw'].includes(id)){
  A.send();if(['B38-confirmed','B38-adjust','B38-readonly','D38-confirmed','B40-record','B40-verify'].includes(id))A.confirm(st.version,st.sentHash);
  if(id==='B38-difference')A.dispute(st.version,'第二条服务金额请再核对。');
 }
};
})(window.UI);
