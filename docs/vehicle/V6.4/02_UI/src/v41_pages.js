/* V5.0: one reconciliation workspace. Editing amounts is not modifying client order prices. */
(function(U){
'use strict';const A=U.recon41,R=U.desktopPages,M=U.mobilePages;
const{panel:p,button:b,goBtn:g,kv,notice:n,field:f,check:c,badge:tag,table:t,esc:e}=U;
const act=(...x)=>'<div class="flex wrap">'+x.join('')+'</div>';
const screen=(title,desc,body,actions='',overlay='')=>({title,desc,body,actions,overlay});
const money=x=>x===null?'待填写':U.money(x);
function history(st,x){let xs=x?x.priceHistory:st.entries.flatMap(y=>y.priceHistory.map(h=>({...h,task:y.assignmentId})));return xs.length?t(['时间 / 操作人','服务任务','原金额','拟金额','原因'],xs.map(h=>[e(h.at.slice(0,19).replace('T',' '))+'<small>'+e(h.actor)+'</small>',e(h.task||x.assignmentId),money(h.old),money(h.new),e(h.reason)])):'<p class="hint">暂无人工改价记录。</p>';}
function rows(st,xs){return t(['服务任务 / 订单','车辆 / 服务类型','规则计算','本次拟应付','金额来源','操作'],xs.map(x=>{
 const v=A.amount(x);let controls='';
 if(st){const text=x.kind==='费用调整'?'查看依据':A.isLocked(st)?'申请调差':v===null?'填写价格':'修改价格';controls=act(b(text,'v41-price-open','link',{payload:x.id}),b('记录','v41-history','link',{payload:x.id}),A.canEdit(st)?b('移出本账单','v42-remove-open','link',{payload:x.id}):'');}
 else controls=b(v===null?'填写价格':'修改价格','v41-preview-edit','link',{payload:x.id});
 return [e(x.assignmentId)+'<small>'+e(x.orderNo)+'</small>',e(x.plate)+'<small>'+e(x.kind)+'</small>',money(x.reference??U.entryAmount(x)),`<b class="amount-emphasis">${money(v)}</b>`,tag(x.manual?(st?.status==='CONFIRMED'?'人工核定':'人工拟价')+(st?.status==='INTERNAL_REVIEW'?' · 待复核':''):v===null?'待填写':'按规则',x.manual||v===null?'orange':'gray'),controls];}));}
function summary(st,xs){let un=xs.filter(x=>A.amount(x)===null).length,total=xs.reduce((a,x)=>a+(A.amount(x)??0),0);return `<div class="balance-strip"><div><span>${un?'已填写小计（非最终）':st?.status==='CONFIRMED'?'本期已确认应付':'本期拟应付'}</span><b>${U.money(total)}</b></div><div><span>服务明细</span><b>${xs.length} 笔</b></div><div><span>待填写金额</span><b>${un} 笔</b></div><div><span>人工填价 / 改价</span><b>${xs.filter(x=>x.manual).length} 笔</b></div></div>`;}
R.B37=function(){const s=U.v4();return screen('代驾对账','选择代驾与期间，在同一张账单核对服务、填写价格和处理差异。',p('',`<div class="directory-tools">${f('承担费用主体','recon.entity','集信车检服务主体','select',{options:['集信车检服务主体']})}${f('开始日期','recon.from','2026-10-01','date')}${f('结束日期（含）','recon.to','2026-10-10','date')}${b('查询','v4-recon-filter','',{icon:'search'})}</div>`+t(['代驾员','本期已结束任务','待填写金额','当前账单','操作'],s.drivers.map(d=>{const xs=U.driverEntries(d.id).filter(x=>x.ended.slice(0,10)>=String(U.get('recon.from','2026-10-01'))&&x.ended.slice(0,10)<=String(U.get('recon.to','2026-10-10'))),st=s.statements.filter(x=>x.driverId===d.id&&x.status!=='VOID').at(-1);return[e(d.name)+'<small>'+e(d.id)+'</small>',xs.length+' 笔',xs.filter(x=>U.entryAmount(x)===null).length+' 笔',st?tag(A.status(st.status),'gray'):'尚未整理',b('个人对账','v4-driver-recon','link',{payload:d.id})];}))),act(g('代驾费用规则','B39','secondary'),g('已有付款核对','B40','secondary')));};
R.B38=function(v){const s=U.v4(),driver=U.driver(),st=A.current(),xs=st?st.entries:U.previewStatement().map(x=>A.initLine({...x}));
 if(v.state==='forbidden')return screen('个人对账','费用及账单需要对应主体授权。',U.empty('无权查看本账单','请联系管理员核对费用主体与账单范围。',g('返回代驾列表','B28')));
 let header=`<div class="statement-heading"><div><b>${e(st?.entity||'集信车检服务主体')}</b><p class="hint">${st?e(st.id)+' · '+e(st.from)+' 至 '+e(st.to):'选择期间后整理；无价格明细也可保存草稿。'}</p></div>${st?tag(A.status(st.status),'gray'):tag('尚未建立草稿','gray')}</div>`;
 let body=header+summary(st,xs);
 if(st?.unpriced)body+=n('还有 '+st.unpriced+' 条费用未填写。可以保留草稿，点击对应行「填写价格」；补齐并复核后才能发送。','warn');
 if(st?.rejectReason&&st.status==='DRAFT')body+=n('复核退回：'+e(st.rejectReason)+'。请修改相关明细后重新提交。','warn');
 if(st?.status==='SENT')body+=n('已发给代驾核对 V'+st.revision+'。需要改价时，先撤回当前版本，避免双方核对不同金额。');
 if(A.isLocked(st||{}))body+=n('原账单只读。更正请在对应行申请调差，后续核对差额；不会修改本单或已付款记录。','success');
 if(st?.status==='DISPUTED')body+=p('代驾提出的差异',`<p>${e(st.dispute||'请核对相关服务金额。')}</p>`+f('处理说明','r41.resolveReason',st.resolveReason||'','textarea',{required:true,placeholder:'说明处理结果；需要改价可直接在下面对应行操作。'})+b('保存差异说明','v41-resolve-note','secondary'));
 body+=p('服务与费用明细',xs.length?rows(st,xs):U.empty('本期没有可纳入的服务','检查所选期间；已进入其他有效账单的明细不会重复纳入。'),st?tag('内容版本 V'+st.revision,'gray'):'');
 if(st?.status==='INTERNAL_REVIEW')body+=p('整单价格复核',n('核对下方人工填价与改价记录，提交者及参与本轮改价人员不能复核。')+history(st)+kv('待复核金额',money(st.amount))+f('复核意见','r41.reviewReason','','textarea',{required:true})+c('已核对全部改价依据与本期金额','r41.reviewChecked',false)+act(b('退回修改','v41-review-reject','danger'),b('复核通过并发送','v41-review-approve','',{require:'r41.reviewChecked'})));
 if(st){body+=p('核对记录',st.events.length?U.timeline(st.events.map(x=>[x.text,x.actor+' · '+x.at.slice(0,19).replace('T',' ')])):'<p class="hint">等待整理服务费用。</p>');}
 let buttons=[g('返回对账列表','B37','secondary')];
 if(!st)buttons.push(g('整理本期账单','B38-generate'));
 else if(['DRAFT','DISPUTED'].includes(st.status)){
  buttons.push(b('保存草稿','v41-save-draft','secondary'));
  buttons.push(b(st.manualCount?'提交整单复核':'发给代驾核对',st.manualCount?'v41-review-submit':'v41-send','',{disabled:st.unpriced>0}));
 }else if(['INTERNAL_REVIEW','SENT'].includes(st.status))buttons.push(b(st.status==='SENT'?'撤回后修改':'撤回复核','v41-withdraw-open','secondary'));
 if(st)buttons.push(b('导出当前版本','v41-export','secondary'),b('继续勾选服务','v4-recon-new','secondary'));
 const d=screen(driver.name+' · 个人对账','这里修改的是本次代驾报酬，不改变车主已支付金额。',body,act(...buttons));
 if(v.state==='generate')d.overlay=U.drawer('整理本期对账单',kv('代驾员',driver.name+' · '+driver.id)+f('开始日期','recon.from','2026-10-01','date',{required:true})+f('结束日期（含）','recon.to','2026-10-10','date',{required:true})+n('待核价不阻止建立草稿。新草稿纳入尚未被其他有效账单占用的服务明细。')+c('已核对代驾、费用承担主体和期间','recon.checked',false),g('取消','B38','secondary')+b('建立草稿','v41-create','',{require:'recon.checked'}),'B38');
 if(['price','price-empty','price-conflict'].includes(v.state)&&st){const edit=U.store.v41.edit,x=st.entries.find(x=>x.id===edit?.entryId)||st.entries[0];d.overlay=U.drawer(x.proposed===null?'填写本次代驾报酬':'修改本次代驾报酬',kv('代驾员',driver.name)+kv('服务任务',x.assignmentId)+kv('车辆 / 服务类型',x.plate+' · '+x.kind)+kv('规则计算金额',money(x.reference))+kv('当前拟应付金额',money(x.proposed))+(v.state==='price-conflict'?n('其他人已更新这份账单。您的输入保留，保存将进行版本校验。','warn'):'')+f('修改后应付金额（元）','r41.amount',edit?.initialValue||'','text',{required:true,inputmode:'decimal',placeholder:'填写本次服务的最终拟应付金额'})+f('修改原因 / 计价依据','r41.reason','','textarea',{required:true,placeholder:'例如双方约定的取消返还补偿，不要填无意义说明。'})+f('关联依据（选填）','r41.evidence','','text',{placeholder:'既有工单编号、约定记录或凭证说明'})+n('保存后只更新本账单的拟价。差额自动计算，客户金额和全局规则不变。'),b('取消','v41-edit-cancel','secondary')+b('保存修改','v41-price-save'),'B38');}
 if(v.state==='history'&&st)d.overlay=U.drawer('价格修改记录',history(st,st.entries.find(x=>x.id===U.store.v41.historyEntryId)),g('关闭','B38','secondary'),'B38');
 if(v.state==='adjust'&&st){const id=U.store.v41.adjustEntryId||st.entries[0].id,x=st.entries.find(x=>x.id===id);d.overlay=U.drawer('申请后续调差',kv('原账单',st.id)+kv('服务任务',x.assignmentId)+kv('已确认金额',money(x.proposed))+f('本次调差金额（元）','r41.delta','','text',{required:true,inputmode:'decimal',hint:'补付填正数，减少填负数；此处不是填写新的总价。'})+f('调差原因 / 依据','r41.adjustReason','','textarea',{required:true})+n('原账保持不变。复核通过后形成独立差额条目，纳入后续账单；不自动扣款。'),g('取消','B38','secondary')+b('提交调差复核','v41-adjust-submit'),'B38');}
 return d;
};
M.D37=function(){const xs=U.v4().statements.filter(x=>x.driverId===U.v4().driverUser&&['SENT','DISPUTED','CONFIRMED'].includes(x.status));return{title:'我的对账单',body:p('待核对与历史账单',xs.length?xs.map(st=>U.row('doc',st.from+' 至 '+st.to,st.id+' · '+A.status(st.status),b(st.status==='CONFIRMED'?'查看':'去核对','v4-open-statement','link',{payload:st.id}))).join(''):U.empty('暂无已发给您的账单','平台整理中的草稿、内部复核版本不会在这里出现。'))+n('确认对账是认可服务与金额，不表示已经收到款项。'),footer:g('返回我的','D27','secondary')};};
M.D38=function(v){const st=A.current();if(!st||st.driverId!==U.v4().driverUser||!['SENT','DISPUTED','CONFIRMED'].includes(st.status))return{title:'本人对账',body:U.empty('当前版本不可确认','平台可能已撤回或仍在整理，请回到列表刷新。',g('返回本人账单','D37'))};
 let body=`<div class="recon-amount"><span>本期应付 · V${st.revision}</span><strong>${money(st.amount)}</strong><p>${e(st.from)} 至 ${e(st.to)} · ${st.entries.length} 笔</p></div>`+p('服务明细',st.entries.map(x=>U.row('car',e(x.plate),e(x.assignmentId)+' · '+e(x.kind),'<b>'+money(x.proposed)+'</b>')).join(''));
 if(st.status==='CONFIRMED')body+=n('已确认 V'+(st.confirmation?.snapshotVersion||st.revision)+'。后续调差保留原记录；本页不代表已付款。','success');
 else if(st.status==='DISPUTED')body+=n('差异已提交，等待平台核实；重新发送后请核对新版本。','warn');
 else if(v.state==='difference')body+=p('说明差异',f('具体说明','r41.dispute','','textarea',{required:true}));
 else body+=c('我已核对本期服务及全部费用','r41.driverChecked',false);
 return{title:'核对账单',body,footer:st.status!=='SENT'?g('返回账单列表','D37','secondary'):v.state==='difference'?b('提交差异','v41-dispute'):act(g('有差异','D38-difference','secondary'),b('确认本版本无误','v41-confirm-open','',{require:'r41.driverChecked'}))};};
})(window.UI);
