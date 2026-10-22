/* Canonical identity demonstration model. No WeChat requests, real accounts or real credentials. */
(function(U){'use strict';
U.auth62=function(){return U.store.identity||(U.store.identity={
 loggedIn:false,phoneVerified:false,phoneState:'UNVERIFIED',account:'owner-a',name:'微信用户',phone:'139****0101',scenario:'owner',link:'UNLINKED',employeeId:null,
 driverApproved:false,driverStatus:'NONE',blocked:false,version:0,journeyStarted:false,accounts:{},history:[],loginCount:0,
 employees:[
 {id:'12',name:'林工',phone:'139****0012',enabled:true,grants:['ST01','ST02'],linkedAccount:null,linkStatus:'UNLINKED',blocked:false,allowRematch:false,version:0},
 {id:'13',name:'陈工',phone:'139****0013',enabled:true,grants:[],linkedAccount:null,linkStatus:'UNLINKED',blocked:false,allowRematch:false,version:0}
 ]
});};
U.identityCurrentEmployee=()=>U.auth62().employees.find(x=>x.id===U.auth62().employeeId);
U.saveIdentityAccount=function(){const a=U.auth62();a.accounts[a.account]={name:a.name,phone:a.phone,link:a.link,employeeId:a.employeeId,driverApproved:a.driverApproved,driverStatus:a.driverStatus,blocked:a.blocked};};
U.roles62=function(){const a=U.auth62(),roles=['O'],emp=U.identityCurrentEmployee();
 if(a.loggedIn&&a.phoneVerified&&a.driverApproved)roles.push('D');
 if(a.loggedIn&&a.phoneVerified&&a.link==='LINKED'&&emp?.enabled&&emp.linkStatus==='LINKED'&&emp.grants.length)roles.push('S');
 return roles;
};
U.sync62=function(){const a=U.auth62(),emp=U.identityCurrentEmployee();
 if(emp&&(!emp.enabled||emp.linkStatus==='SUSPENDED'))a.link='SUSPENDED';
 if(emp&&emp.linkStatus==='REVOKED')a.link='REVOKED';
 U.store.qualification=a.driverApproved?'PASSED':a.driverStatus==='PENDING'?'AUDITING':a.driverStatus;
 U.store.binding=a.link==='LINKED'?'BOUND':a.link;
 U.store.authorized=a.loggedIn&&a.phoneVerified&&a.link==='LINKED'&&emp?.enabled&&emp.linkStatus==='LINKED'?[...emp.grants]:[];
 if(!U.roles62().includes(U.store.role))U.store.role='O';
 if(U.store.authorized.length&&!U.store.authorized.includes(U.store.stationId))U.store.stationId=U.store.authorized[0];
 U.saveIdentityAccount();
};
U.authorize62=function(scenario){
 const a=U.auth62();a.scenario=scenario;a.journeyStarted=true;
 if(scenario==='denied'){a.phoneState='DENIED';return 'DENIED';}
 if(scenario==='error'){a.phoneState='FAILED';return 'FAILED';}
 U.saveIdentityAccount();
 const target=scenario==='unconfigured'?'13':['staff','duplicate','conflict','suspended','revoked'].includes(scenario)?'12':null;
 const key=target?'staff-'+target:scenario==='driver'?'driver-a':'owner-a';
 const prev=a.accounts[key];
 if(a.account!==key){Object.assign(a,{account:key,name:target?(target==='12'?'林工':'陈工'):'微信用户',phone:target?(target==='12'?'139****0012':'139****0013'):'139****0101',link:'UNLINKED',employeeId:null,driverApproved:false,driverStatus:'NONE',blocked:false},prev||{});}
 if(scenario==='driver'&&!prev){a.driverApproved=true;a.driverStatus='APPROVED';a.name='周师傅';}
 a.loggedIn=true;a.phoneVerified=true;a.phoneState='VERIFIED';a.loginCount++;U.store.role='O';
 const emp=a.employees.find(x=>x.id===target);
 if(!target){if(!a.employeeId)a.link='UNLINKED';}
 else if(a.blocked||emp.blocked||emp.linkStatus==='REVOKED'&&!emp.allowRematch){a.employeeId=emp.id;a.link='REVOKED';}
 else if(!emp.enabled||emp.linkStatus==='SUSPENDED'&&!emp.allowRematch){a.employeeId=emp.id;a.link='SUSPENDED';}
 else if(scenario==='duplicate'||scenario==='conflict'||emp.linkedAccount&&emp.linkedAccount!==a.account||a.employeeId&&a.employeeId!==emp.id){a.link='NEEDS_ADMIN';if(!prev?.employeeId)a.employeeId=null;}
 else{a.employeeId=emp.id;a.name=emp.name;a.phone=emp.phone;emp.linkedAccount=a.account;emp.linkStatus='LINKED';emp.allowRematch=false;a.link='LINKED';a.blocked=false;a.history.push('手机号核验后自动关联：'+emp.name);}
 a.version++;U.sync62();return a.link;
};
U.identityLabels={UNLINKED:'未关联员工',LINKED:'已自动关联',NEEDS_ADMIN:'需管理员核实',SUSPENDED:'工作关联已暂停',REVOKED:'关联已解除'};
U.driverLabels={NONE:'尚未申请',DRAFT:'申请草稿',PENDING:'审核中',REJECTED:'待补充资料',APPROVED:'审核已通过'};
U.prepareIdentityPreview=function(v){const a=U.auth62();if(a.journeyStarted||a.previewId===v.id)return;
 // Review fixtures are explicit states; product actions never call this to grant a role.
 const id=v.id;let scenario=null;
 if(id==='U02-linked'||id==='U01-staff'||id==='S01-ready'||id==='S15'||id==='S21')scenario='staff';
 if(id==='U02-unconfigured'||id==='S01-unconfigured')scenario='unconfigured';
 if(id==='U01-driver'||id==='D01-approved'||id==='D05-passed'||id==='O25-approved'||id==='D27')scenario='driver';
 if(id==='U01-all')scenario='staff';
 if(id==='U02-conflict')scenario='duplicate';
 if(['U02-revoked','U02-suspended','B36','B36-revoke','B36-suspend','B36-rematch'].includes(id))scenario='staff';
 if(['O25','O25-pending','U01','U01-unqualified','D01','D01-draft','D01-pending','D01-rejected','D05','D05-rejected'].includes(id))scenario='owner';
 if(scenario){U.authorize62(scenario);a.journeyStarted=false;}
 if(id==='U01-all'){a.driverApproved=true;a.driverStatus='APPROVED';}
 if(['D01-pending','D05','O25-pending'].includes(id))a.driverStatus='PENDING';
 if(id==='D01-draft')a.driverStatus='DRAFT';
 if(['D01-rejected','D05-rejected'].includes(id))a.driverStatus='REJECTED';
 if(id==='U02-revoked'||id==='B36-rematch'){const e=U.identityCurrentEmployee();e.linkStatus='REVOKED';e.blocked=true;a.link='REVOKED';a.blocked=true;}
 if(id==='U02-suspended'){U.identityCurrentEmployee().linkStatus='SUSPENDED';a.link='SUSPENDED';}
 if(id==='U02-verifying'){a.loggedIn=true;a.phoneVerified=false;a.phoneState='PROCESSING';}
 if(id==='U02-unknown'){a.loggedIn=true;a.phoneVerified=false;a.phoneState='UNKNOWN';}
 if(id==='B26-unlinked'){U.set('identity.employee','13');}
 if(id==='B26-revoke'){U.set('identity.employee','12');a.employees[0].grants=['ST01','ST02'];U.set('identity.grant.12.ST01',false);U.set('identity.grant.12.ST02',false);}
 if(id==='B26-conflict'){U.set('identity.employee','12');U.set('identity.expectedVersion.12',0);a.employees[0].version=1;}
 if(id==='B36-unlinked'){a.employees.forEach(e=>{e.linkedAccount=null;e.linkStatus='UNLINKED';e.blocked=false;});a.employeeId=null;a.link='UNLINKED';}
 U.sync62();a.previewId=id;
};
})(window.UI);
