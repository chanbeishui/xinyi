/* Identity interactions; all mutations are local simulation, never backend authorization. */
(function(U){'use strict';const previous=U.action;const value=k=>String(U.get(k,'')).trim();
const refresh=()=>U.testing.render(U.currentId());
function clear(prefix){for(const k of Object.keys(U.store.forms))if(k.startsWith(prefix))delete U.store.forms[k];}
U.action=async function(action,payload='',el=null){const a=U.auth62();
 if(action==='auth62-login'||action==='login'){
  if(!U.get('login.agree')){U.toast('请先阅读并同意服务与隐私说明');return;}
  U.dialog('微信手机号授权 · 本地演示','<p>此处模拟微信主动授权。正式系统由后台核验微信身份与手机号，不接受手填员工身份。</p>',[U.button('暂不授权','auth62-deny','secondary'),U.button('模拟同意授权','auth62-authorize')]);return;
 }
 if(action==='auth62-deny'){a.phoneState='DENIED';a.journeyStarted=true;U.closeDialog();U.toast('尚未授权手机号，可重试或查看公开说明');return;}
 if(action==='auth62-authorize'||action==='login-demo'){
  const scenario=a.scenario==='denied'?'owner':a.scenario==='processing'?'owner':a.scenario;
  const result=U.authorize62(scenario);U.closeDialog();
  if(['DENIED','FAILED'].includes(result)){U.toast('核验未完成，没有开通工作身份');return;}
  U.toast(result==='LINKED'?'登录成功，员工信息已自动识别':'登录成功，先以车主身份进入');U.go('O03');return;
 }
 if(action==='identity-query-phone'){U.toast(a.phoneState==='PROCESSING'?'仍在核验，请稍后查询':'结果尚未确认，不重复提交原授权凭据；可重新主动授权');return;}
 if(action==='auth62-role'||action==='switch-role'){
  U.sync62();if(!a.loggedIn){U.toast('请先完成统一登录');U.go('O29');return;}
  if(!U.roles62().includes(payload)){U.toast(payload==='D'?'代驾资格须先审核通过':'暂无检测工作授权');return;}
  U.store.role=payload;U.go({O:'O03',D:'D06',S:'S02'}[payload]);return;
 }
 if(action==='auth62-enter-station'||action==='enter-station'){
  U.sync62();if(!U.roles62().includes('S')){U.toast('暂无检测工作权限，请联系管理员配置');return;}U.store.role='S';U.go('S02');return;
 }
 if(action==='switch-station'){U.sync62();if(!U.roles62().includes('S')||!U.store.authorized.includes(payload)){U.go('S22');return;}}
 if(action==='auth62-refresh'){U.sync62();U.toast('已读取最新员工和站点权限');U.testing.render('S01');return;}
 if(action==='auth62-reverify'){a.scenario=a.employeeId==='13'?'unconfigured':a.employeeId==='12'?'staff':a.scenario||'owner';U.go(a.scenario==='owner'?'O29':'O29-'+a.scenario);return;}
 if(action==='identity-start-driver'){
  if(!a.loggedIn||!a.phoneVerified){U.toast('请先登录并授权手机号');U.go('O29');return;}
  if(a.driverApproved){U.go('D28');return;}
  if(a.driverStatus==='PENDING'){U.go('D05');return;}
  a.journeyStarted=true;a.driverStatus='DRAFT';U.sync62();U.go('D02');return;
 }
 if(action==='save-driver'){a.driverStatus=a.driverApproved?'APPROVED':'DRAFT';U.sync62();return previous(action,payload,el);}
 if(action==='driver-check-next'){
  if(!U.store.saved.face||!U.store.saved.background||!U.store.saved.training){U.toast('请先完成核验和培训，再提交审核');return;}
  a.driverStatus='PENDING';a.driverApproved=false;a.journeyStarted=true;U.sync62();U.log('代驾申请','完整申请已提交，未开通代驾身份');U.go('D05');return;
 }
 if(action==='identity-refresh-driver'||action==='auth-refresh'){U.sync62();U.testing.render('D05');U.toast('已刷新审核结果；刷新不会代替后台审核');return;}
 if(action==='qualification-approve'||action==='qualification-reject'){
  if(!value('qualification.opinion')){U.toast('请填写审核意见');return;}
  if(action==='qualification-approve'&&(!U.get('qualification.checked')||!value('qualification.reviewDate'))){U.toast('请核对必要材料并填写复审日期');return;}
  a.driverApproved=action==='qualification-approve';a.driverStatus=a.driverApproved?'APPROVED':'REJECTED';a.journeyStarted=true;
  a.history.push('代驾资格审核：'+a.driverStatus);U.sync62();U.log('代驾资格演示审核',a.driverStatus);U.go('B16');return;
 }
 if(action==='identity-search'){refresh();return;}
 if(action==='identity-reset-search'){clear('identity.query');clear('identity.filter');refresh();return;}
 if(action==='identity-open-grants'){
  U.set('identity.employee',payload);clear('identity.grant.');clear('identity.expectedVersion.');U.set('identity.grantReason','');U.go('B26');return;
 }
 if(action==='identity-open-link'){U.set('identity.manageEmployee',payload);U.set('identity.adminReason','');U.go('B36');return;}
 if(action==='identity-clear-grants'){const id=U.get('identity.employee','12');for(const s of U.store.stations)U.set('identity.grant.'+id+'.'+s.id,false);refresh();return;}
 if(action==='identity-reload-grants'){const id=U.get('identity.employee','12');clear('identity.grant.'+id+'.');clear('identity.expectedVersion.'+id);U.go('B26');return;}
 if(action==='auth62-save-grants'){
  const emp=a.employees.find(x=>x.id===U.get('identity.employee','12'));if(!emp){U.toast('员工不存在，请刷新');return;}
  if(!value('identity.grantReason')){U.toast('请填写变更原因');return;}
  if(Number(U.get('identity.expectedVersion.'+emp.id,-1))!==emp.version){U.toast('授权已由其他人修改，请重新载入后操作');return;}
  emp.grants=U.store.stations.filter(s=>!!U.get('identity.grant.'+emp.id+'.'+s.id,emp.grants.includes(s.id))).map(s=>s.id);emp.version++;
  U.set('identity.expectedVersion.'+emp.id,emp.version);a.journeyStarted=true;
  a.history.push('站点配置：'+emp.name+'；'+emp.grants.join('、')+'；原因：'+value('identity.grantReason'));
  U.sync62();U.toast('站点已保存，无需员工提交申请');U.testing.render('B26');return;
 }
 if(action==='identity-management-dialog'){U.set('identity.adminReason','');U.go('B36-'+payload);return;}
 if(action==='auth62-manage'){
  const emp=a.employees.find(x=>x.id===U.get('identity.manageEmployee','12'));if(!emp){U.toast('员工不存在');return;}
  if(!value('identity.adminReason')){U.toast('请填写处理原因');return;}
  if(emp.linkStatus==='UNLINKED'){U.toast('尚无微信关联可处理；可以先配置站点');return;}
  if(payload==='rematch'){
   if(!['SUSPENDED','REVOKED'].includes(emp.linkStatus)){U.toast('当前状态不需要重新核验');return;}
   emp.allowRematch=true;emp.blocked=false;if(a.employeeId===emp.id)a.blocked=false;
   a.history.push('允许 '+emp.name+' 重新核验；当前工作权限尚未恢复');
  }else if(payload==='revoke'){
   emp.linkStatus='REVOKED';emp.blocked=true;emp.allowRematch=false;
   if(a.employeeId===emp.id){a.link='REVOKED';a.blocked=true;}a.history.push('解除 '+emp.name+' 微信关联并禁止自动重绑');
  }else if(payload==='suspend'){
   if(emp.linkStatus!=='LINKED'){U.toast('当前关联不是有效状态');return;}
   emp.linkStatus='SUSPENDED';emp.allowRematch=false;if(a.employeeId===emp.id)a.link='SUSPENDED';a.history.push('暂停 '+emp.name+' 工作关联');
  }else{U.toast('不支持的关联操作');return;}
  a.journeyStarted=true;emp.version++;U.sync62();U.toast('已记录；不影响个人车主身份和在手代驾任务');U.go('B36');return;
 }
 if(action==='logout'){
  U.dialog('退出当前账号？','<p>退出仅结束当前登录，不取消订单、不解除代驾任务。</p>',[U.button('取消','close-dialog','secondary'),U.button('确认退出','identity-logout-confirm')]);return;
 }
 if(action==='identity-logout-confirm'){
  U.saveIdentityAccount();a.loggedIn=false;a.phoneVerified=false;a.phoneState='UNVERIFIED';a.journeyStarted=true;U.store.role='O';U.store.authorized=[];
  clear('login.');U.closeDialog();U.go('O29');U.toast('已退出当前演示会话，历史资格与任务保留');return;
 }
 if(['bind-submit','binding-approve','binding-reject','v5-bind-request','v5-bind-issue','v5-bind-consume','v5-bind-revoke'].includes(action)){
  U.toast('此旧员工授权动作已停用，不会创建关联');return;
 }
 return previous(action,payload,el);
};
// Re-render dependent controls when changing the target employee; never save with another person's visible form.
document.addEventListener('change',ev=>{const k=ev.target?.dataset?.field;if(k==='identity.employee'){
 clear('identity.grant.');clear('identity.expectedVersion.');U.set('identity.grantReason','');U.testing.render('B26');
}else if(k==='identity.manageEmployee'){
 U.set('identity.adminReason','');U.testing.render('B36');
}});
delete U.flows.employeeCredential;
U.flows.employeeAutomatic={name:'员工自动关联与后台预配置',desc:'先为未登录员工分配站点，再授权手机号；默认车主，主动切检测工作。',ids:['B25','B26-unlinked','O29-unconfigured','O25','U01','S01','S21'],seed:'SCHEDULED'};
U.flows.ownerToDriver={name:'车主申请代驾与审核',desc:'统一登录 → 我的 → 填申请 → 后台审核 → 返回切换身份',ids:['O29','O25','D01','D02','D03','D04','D05','B16-review','D05-passed','U01','D06'],seed:'SCHEDULED'};
U.flows.identity={name:'员工暂停、重核与工作恢复',desc:'员工登录后，后台暂停；重新登录不能绕过，允许重核后本人再次授权。',ids:['O29-staff','U01','S01','B36-suspend','U02','B36-rematch','O29-staff','U01'],seed:'SCHEDULED'};
})(window.UI);
