/* Single authoritative set of login, role, driver application and employee management views. */
(function(U){'use strict';
const {panel:p,button:b,goBtn:g,kv,notice:n,field:f,check:c,row,icon,badge:tag,table:t,esc:e}=U;
const M=U.mobilePages,R=U.desktopPages,acts=(...x)=>'<div class="flex wrap">'+x.join('')+'</div>';
const demo=()=>'<p class="hint mt">以下为虚构账号的交互演示；不会调用微信或修改真实员工。</p>';
const mine=()=>{const a=U.auth62();return `<div class="profile-head"><div class="avatar">${e(a.name.slice(0,1))}</div><div class="grow"><h2>${e(a.name)}</h2><p class="hint">${a.phoneVerified?e(a.phone)+' · 手机号已核验':'尚未授权手机号'}</p></div>${tag('车主','gray')}</div>`;};
const driverRow=()=>{const a=U.auth62(),s=a.driverStatus;return row('steering',a.driverApproved?'代驾资格已通过':s==='PENDING'?'查看代驾审核进度':s==='REJECTED'?'补充代驾申请资料':s==='DRAFT'?'继续代驾资格申请':'申请成为代驾',a.driverApproved?'可切换代驾；接单仍需满足实时条件':s==='PENDING'?'审核通过前仅保留原有身份':'审核通过后才开通正式代驾身份','',a.driverApproved?'D28':s==='PENDING'?'D05':'D01');};
M.O29=v=>{const a=U.auth62();if(!a.journeyStarted||['staff','unconfigured','driver','duplicate','conflict'].includes(v.state))a.scenario=v.state==='default'?'owner':v.state;
 const busy=v.state==='processing',failed=v.state==='error',denied=v.state==='denied';return{title:'登录机动车检测预约',
 body:`<div class="login-brand"><div class="brand-symbol">检</div><h2>上门接送，安心年检</h2><p>首次登录以车主身份进入</p></div>`+
 p('手机号授权',row('phone','预约联系','用于账号验证和代驾接车联系')+row('users','公司员工自动识别','与后台登记手机号一致时自动关联，无需申请员工身份')+
 c('我已阅读并同意服务协议与隐私说明','login.agree',false)+acts(g('用户服务协议','O09','link'),g('隐私说明','U05','link')))+
 (busy?n('正在核验手机号，请勿重复提交。页面返回后会查询结果。'):failed?n('核验暂未完成。请重试，不会按失败结果开通员工权限。','error'):denied?n('你暂未授权手机号，可以重试或先了解服务。','warn'):'')+
 '<p class="hint">代驾身份需另行申请并通过审核。员工的检测点和操作权限由管理员在 PC 后台配置。</p>'+demo(),
 footer:acts(g('先了解服务','O24','secondary'),b(busy?'正在核验…':'微信手机号授权登录','auth62-login','',{disabled:busy,require:busy?'':'login.agree'}))};};
M.O25=()=>{const a=U.auth62();return{title:'我的',tabs:'owner',active:3,body:mine()+
 p('我的服务',row('order','我的预约','查看本人预约与售后','','O26')+row('car','车辆与常用地址','预约时也可直接填写','','O27')+row('doc','检测结果','查看本人车辆最终检测结论','','O18'))+
 p('身份与资格',row('users','切换身份',U.roles62().map(k=>({O:'车主',D:'代驾',S:'检测工作人员'}[k])).join(' · '),'','U01')+driverRow()+row('building','员工关联状态',U.identityLabels[a.link],'','U02'))+
 p('账号与帮助',row('user','个人资料','选填；普通联系号码不会用来识别员工','','O02')+row('support','客服与设置','通知、隐私与退出登录','','O28'))};};
M.U01=()=>{const a=U.auth62(),roles=U.roles62(),m={O:['car','车主','预约接送、支付及本人服务记录'],D:['steering','代驾','代驾资格已审核通过'],S:['building','检测工作人员','只在后台授予的站点和权限内工作']};return{title:'切换身份',body:
 '<p class="hint mb">以下是你当前可使用的身份。切换不会结束正在承接的代驾任务。</p>'+roles.map(k=>`<button class="role-card ${U.store.role===k?'selected':''}" data-action="auth62-role" data-payload="${k}"><span class="role-icon">${icon(m[k][0],24)}</span><span class="grow"><h3>${m[k][1]}</h3><p>${m[k][2]}</p></span>${tag(U.store.role===k?'当前':'可切换')}</button>`).join('')+
 p('资格和工作信息',driverRow()+row('users','员工关联',U.identityLabels[a.link],'','U02'))+n('员工身份不免除代驾审核；未获授权的身份不会显示为可切换。'),footer:g('返回我的','O25','secondary')};};
M.U02=()=>{const a=U.auth62(),emp=U.identityCurrentEmployee(),working=U.roles62().includes('S'),pending=['PROCESSING','UNKNOWN'].includes(a.phoneState);
 let title=U.identityLabels[a.link],desc='授权手机号没有匹配到有效员工时，可继续作为车主使用。';
 if(a.link==='LINKED')desc=working?'微信账号已自动关联公司员工，可以切换到检测工作。':'员工关联已完成，但尚无可操作的站点或功能权限。请联系管理员配置。';
 if(a.link==='SUSPENDED')desc='员工工作能力已暂停。普通车主功能保留，请联系管理员核实。';
 if(a.link==='REVOKED')desc='微信与员工的关联已解除。重新登录不会自动恢复，请联系管理员核实。';
 if(a.link==='NEEDS_ADMIN')desc='员工信息存在需要核实的情况。为保护账号，这里不展示其他员工信息；仍可使用车主功能。';
 if(pending){title=a.phoneState==='PROCESSING'?'手机号核验中':'核验结果待确认';desc='正在确认本人的核验结果，尚未据此新增员工身份。请查询结果后再决定是否重新授权。';}
 const phone=a.phoneVerified?a.phone+' · 已核验':a.phoneState==='PROCESSING'?'核验处理中':a.phoneState==='UNKNOWN'?'结果待确认':'尚未授权';
 return{title:'员工关联状态',body:U.banner(title,desc,working?'success':a.link==='LINKED'?'':'warn')+
 p('本人状态',kv('手机号',phone)+kv('关联方式','微信手机号自动识别')+kv('可操作检测点',working?emp.grants.length+' 个':a.link==='LINKED'?'0 个':'—'))+
 (working?p('已配置检测点',emp.grants.map(id=>row('building',U.store.stations.find(s=>s.id===id)?.name||id,'具体操作由员工角色权限决定')).join('')):'')+
 n('不需要填写员工编号或绑定码，也不需要申请加入检测点。账号和检测点关系由后台维护。'),
 footer:acts(g('返回车主','O25','secondary'),b('联系管理员','contact','secondary',{payload:'管理员'}),pending?b('查询核验结果','identity-query-phone'):working?b('进入检测工作','auth62-enter-station'):a.link==='LINKED'?b('刷新工作权限','auth62-refresh'):b('重新授权手机号','auth62-reverify'))};};
M.S01=()=>{const a=U.auth62(),can=U.roles62().includes('S'),emp=U.identityCurrentEmployee();return{title:'检测工作',body:
 U.banner(can?'可以进入检测工作':a.link==='LINKED'?'待管理员配置工作权限':'暂未取得检测工作身份',can?'员工身份已自动识别，以下检测点由后台配置。':a.link==='LINKED'?'无需在小程序提交申请。后台配置完成后，刷新即可使用。':'请使用公司登记的手机号授权，或联系管理员核实。',can?'success':'warn')+
 p('本人信息',kv('员工关联',U.identityLabels[a.link])+kv('代驾资格',a.driverApproved?'审核已通过':'尚未取得；不影响站内工作'))+
 p('获授权检测点',can?emp.grants.map(id=>row('building',U.store.stations.find(x=>x.id===id)?.name||id,'在已授权范围内收车、登记结果和放行')).join(''):'<p class="hint">暂无可操作检测点。后台可在员工首次微信登录前配置，不要求提供 OpenID。</p>')+g('查看员工关联状态','U02','link block'),
 footer:acts(g('返回车主','O03','secondary'),b('刷新权限','auth62-refresh','secondary'),b('进入工作台','auth62-enter-station','',{disabled:!can}))};};
M.D01=()=>{const a=U.auth62(),s=a.driverStatus;return{title:'申请代驾资格',body:U.banner(a.driverApproved?'代驾资格已开通':s==='PENDING'?'申请正在审核':s==='REJECTED'?'请补充申请资料':'成为平台代驾',a.driverApproved?'无需重复申请，证件和复审需保持有效。':s==='PENDING'?'审核通过前不开放接单。可以继续使用车主功能。':'使用当前车主账号申请，不需要再注册。',a.driverApproved?'success':s==='REJECTED'?'warn':'')+
 p('当前申请',kv('账号',a.name)+kv('手机号',a.phoneVerified?a.phone+' · 已核验':'尚未授权')+kv('资格状态',U.driverLabels[s]||'尚未申请'))+
 p('申请步骤',U.timeline([['填写实名与证件','按要求提交本人资料'],['完成核验与培训','核验失败可查看原因并补充'],['提交后台审核','普通用户和公司员工采用同一标准'],['审核通过后切换代驾','接单还需满足证件、听单和占用条件']],a.driverApproved?4:s==='PENDING'?2:0))+
 n('公司员工也必须通过代驾审核。代驾申请不决定其检测工作权限。'),footer:acts(g('返回我的','O25','secondary'),a.driverApproved?g('查看资格','D28'):s==='PENDING'?g('查看审核进度','D05'):b(s==='REJECTED'?'补充申请资料':s==='DRAFT'?'继续填写':'开始申请','identity-start-driver'))};};
M.D05=()=>{const a=U.auth62(),passed=a.driverApproved,rejected=a.driverStatus==='REJECTED',pending=a.driverStatus==='PENDING';return{title:'代驾审核进度',body:
 U.banner(passed?'审核通过':rejected?'申请需补充':pending?'审核中':'尚未提交审核',passed?'正式代驾身份已开通，可返回身份切换。':rejected?'请按驳回意见补充资料后重新提交。':pending?'等待后台审核，审核结果会同步到本账号。':'请先完成申请资料和必要核验。',passed?'success':rejected?'warn':'')+
 p('审核信息',kv('申请人',a.name)+kv('资格状态',U.driverLabels[a.driverStatus]||'尚未申请')+kv('正式代驾身份',passed?'已开通':'未开通')+kv('复审日期',passed?'按实际审核记录展示':'通过后确定')+(rejected?kv('需补充项','驾驶证图片不清晰（演示意见）'):''))+
 n('刷新只读取审核结果，不会自动批准资格。员工与普通申请人适用同一标准。'),
 footer:acts(g('返回车主','O25','secondary'),passed?b('切换到代驾','auth62-role','',{payload:'D'}):rejected?b('补充资料','identity-start-driver'):b('刷新审核结果','identity-refresh-driver'))};};

M.D27=()=>{const a=U.auth62();if(!a.driverApproved)return{title:'我的代驾工作',body:U.banner('尚未取得正式代驾资格','请先从车主身份申请，审核通过后再进入工作台。','warn'),footer:g('查看申请进度','D01')};return{title:'我的代驾工作',tabs:'driver',active:2,body:`<div class="profile-head"><div class="avatar">${e(a.name.slice(0,1))}</div><div class="grow"><h2>${e(a.name)}</h2><p class="hint">代驾资格已通过 · ${e(a.phone)}</p></div>${tag('已审核','green')}</div>`+U.summary([['本月接送',18],['今日结束',3],['服务评分','4.9']])+p('工作与账号',row('idcard','认证中心与复审','证件有效期与复审申请','','D28')+row('users','切换身份',U.roles62().map(k=>({O:'车主',D:'代驾',S:'检测工作'}[k])).join(' · '),'','U01')+row('support','客服与帮助','普通服务问题与联系入口','','D30'))+p('服务与费用核对',row('order','我的接送任务','在手与已结束承接','','D36')+row('doc','我的对账单','仅本人已发送账单、未结清服务和公开付款状态','','D37'))+p('资格与服务规范',row('doc','培训与测验','查询本人学习及答题记录','','D39')+row('warn','接单限制与申诉','查看本人记录及处理进度','','D43'))+n('统计及任务为演示数据；正式页面只读取当前账号本人的信息。')};};
M.S15=()=>{const a=U.auth62(),emp=U.identityCurrentEmployee(),can=U.roles62().includes('S');return{title:'我的检测工作',tabs:'station',active:3,body:`<div class="profile-head"><div class="avatar">${e(a.name.slice(0,1))}</div><div class="grow"><h2>${e(a.name)}</h2><p class="hint">${emp?'框架员工 '+e(emp.id):'尚无有效员工关联'} · ${e(U.identityLabels[a.link])}</p></div></div>`+p('工作权限',row('building','当前检测点',can?U.station().name:'暂无可操作检测点',can?'切换':'',can?'S21':'S01')+row('users','切换身份',U.roles62().map(k=>({O:'车主',D:'代驾',S:'检测工作'}[k])).join(' · '),'','U01')+row('user','员工关联状态','后台维护人员与站点；不在小程序申请','','U02'))+(can?p('站点与资料',row('building','站点资料与资质','按当前授权范围维护','','S16')+row('calendar','营业时间与预约容量','查看配置并预览变更影响','','S17')+row('users','本站工作人员','只读信息；账号维护在PC后台','','S18')+row('history','操作记录','查看当前范围操作流水','','S20')):n('关联或站点权限暂不可用，请联系管理员，个人车主身份保留。','warn'))};};

const screen=(title,desc,body,actions='',overlay='')=>({title,desc,body,actions,overlay});
R.B25=()=>{const a=U.auth62(),q=String(U.get('identity.query','')).trim(),state=U.get('identity.filter','ALL');const rows=a.employees.filter(x=>(!q||(x.name+' '+x.id+' '+x.phone).includes(q))&&(state==='ALL'||(state==='LINKED'?x.linkStatus==='LINKED':x.linkStatus==='UNLINKED')));
 return screen('员工与检测点权限','员工账号沿用系统用户管理；微信自动关联与站点授权分别维护。',
 p('员工列表',`<div class="form-grid">${f('员工姓名、编号或手机号摘要','identity.query',q)}${f('微信关联状态','identity.filter',state,'select',{options:[['ALL','全部'],['LINKED','已关联'],['UNLINKED','未关联']]})}</div>`+acts(b('查询','identity-search'),b('重置','identity-reset-search','secondary'))+
 t(['框架员工','手机号摘要','微信关联','可操作站点','操作'],rows.map(x=>[e(x.name)+'<small>员工 '+x.id+'</small>',x.phone,tag(({UNLINKED:'未关联微信',LINKED:'已关联微信',SUSPENDED:'工作关联已暂停',REVOKED:'关联已解除'}[x.linkStatus])||'未关联',x.linkStatus==='LINKED'?'green':'gray'),x.grants.length?x.grants.map(id=>e(U.store.stations.find(s=>s.id===id)?.name||id)).join('、'):'暂未配置',acts(b('配置站点','identity-open-grants','link',{payload:x.id}),b('关联详情','identity-open-link','link',{payload:x.id}))])))+
 n('未微信登录的员工也能直接配置站点。关联详情用于查询和异常处理，不存在员工绑定申请审批。'),
 acts(b('框架用户管理','framework-help','secondary'),g('查看微信关联','B36','secondary')));};
R.B26=v=>{const a=U.auth62(),id=U.get('identity.employee',v.state==='unlinked'?'13':'12'),emp=a.employees.find(x=>x.id===id)||a.employees[0];
 const key='identity.expectedVersion.'+emp.id;if(!Object.hasOwn(U.store.forms,key))U.set(key,emp.version);
 return screen('员工站点配置','选择框架已有员工，直接配置可操作检测点。无需员工微信登录或提交申请。',
 p('选择员工',f('框架员工','identity.employee',emp.id,'select',{options:a.employees.map(x=>[x.id,x.name+' · '+x.phone]),required:true})+kv('微信关联',({UNLINKED:'未关联微信',LINKED:'已关联微信',SUSPENDED:'工作关联已暂停',REVOKED:'关联已解除'}[emp.linkStatus]))+kv('账号状态',emp.enabled?'正常':'已停用')+kv('当前授权版本',String(emp.version)))+
 p('可操作检测点',U.store.stations.map(s=>c(s.name+' · '+s.address,'identity.grant.'+emp.id+'.'+s.id,U.get('identity.grant.'+emp.id+'.'+s.id,emp.grants.includes(s.id)))).join('')+b('清空已选站点','identity-clear-grants','link')+f('变更原因','identity.grantReason','','textarea',{required:true})+
 (v.state==='conflict'?n('这份授权已由其他管理员修改。请刷新最新版本后重做本次选择，不能覆盖。','error'):'')+
 n('检测点只决定数据范围；具体操作还要满足原框架角色权限和部门范围。允许撤回全部站点，不删除个人车主账号。')),
 acts(g('返回员工列表','B25','secondary'),b('重新载入授权','identity-reload-grants','secondary'),b('保存站点配置','auth62-save-grants')));};
R.B36=v=>{const a=U.auth62(),id=U.get('identity.manageEmployee','12'),emp=a.employees.find(x=>x.id===id)||a.employees[0],existing=emp.linkStatus!=='UNLINKED';let overlay='';
 if(['suspend','revoke','rematch'].includes(v.state)){const names={suspend:'暂停员工工作关联',revoke:'解除微信关联',rematch:'允许本人重新核验'};
 overlay=U.drawer(names[v.state],kv('处理员工',emp.name)+kv('手机号摘要',emp.phone)+kv('当前关联状态',({UNLINKED:'未关联微信',LINKED:'已关联微信',SUSPENDED:'工作关联已暂停',REVOKED:'关联已解除'}[emp.linkStatus]))+f('处理原因','identity.adminReason','','textarea',{required:true})+
 n(v.state==='rematch'?'仅开放本人重新核验的机会。此刻仍不恢复工作权限，须本人再次授权并通过唯一匹配。':v.state==='revoke'?'解除后不允许同号登录自动绑回。保留个人车主身份和历史任务。':'暂停工作权限，保留关联记录。重新登录不能绕过暂停。','warn'),b('确认处理','auth62-manage','',{payload:v.state,disabled:!existing}),'B36');}
 return screen('员工微信关联管理','手机号匹配正常时自动关联；后台负责查询、撤权和异常处置，不审批正常员工登录。',
 p('选择员工',f('框架员工','identity.manageEmployee',emp.id,'select',{options:a.employees.map(x=>[x.id,x.name+' · '+x.phone])}))+
 p('当前关联',kv('员工',emp.name)+kv('手机号摘要',emp.phone)+kv('微信关联状态',({UNLINKED:'未关联微信',LINKED:'已关联微信',SUSPENDED:'工作关联已暂停',REVOKED:'关联已解除'}[emp.linkStatus]))+kv('关联来源',existing?'微信核验手机号唯一匹配':'尚未关联，无需人工签发')+kv('站点数量',String(emp.grants.length))+
 acts(b('配置站点','identity-open-grants','secondary',{payload:emp.id}),b('暂停工作关联','identity-management-dialog','secondary',{payload:'suspend',disabled:emp.linkStatus!=='LINKED'}),b('解除微信关联','identity-management-dialog','danger',{payload:'revoke',disabled:!['LINKED','SUSPENDED'].includes(emp.linkStatus)}),b('允许重新核验','identity-management-dialog','secondary',{payload:'rematch',disabled:!['SUSPENDED','REVOKED'].includes(emp.linkStatus)})))+
 p('处理历史',a.history.length?a.history.map(x=>'<p class="hint">'+e(x)+'</p>').join(''):'<p class="hint">暂无演示操作记录。真实审计记录不可清空。</p>'),g('返回员工列表','B25','secondary'),overlay);};
})(window.UI);
