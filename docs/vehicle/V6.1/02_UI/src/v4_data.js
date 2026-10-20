/* V4 local demonstration model. Never use this in a production payment or authorization service. */
(function(U){
'use strict';
U.version='V4.0 · 统一售后与代驾对账';
U.v4Seeds={
 vehicles:[
  {id:'V001',plate:'粤K·8H3T2',type:'小型载客汽车',model:'本田雅阁',ownerId:'C001',owner:'陈女士',vin:'LHG******4821',date:'2019-05-20',use:'非营运',last:'2026-10-09',result:'合格',status:'资料已核验',orders:12},
  {id:'V002',plate:'粤K·5M9Q7',type:'小型载客汽车',model:'丰田凯美瑞',ownerId:'C002',owner:'吴先生',vin:'LVG******2367',date:'2021-08-16',use:'非营运',last:'2026-10-08',result:'不合格',status:'资料已核验',orders:6},
  {id:'V003',plate:'粤K·AD1234',type:'小型载客汽车',model:'比亚迪宋 PLUS',ownerId:'C003',owner:'何女士',vin:'LGX******1130',date:'2023-06-22',use:'非营运',last:'2026-10-06',result:'合格',status:'资料待核验',orders:2},
  {id:'V004',plate:'粤K·3N2C5',type:'轻型货车',model:'江铃顺达',ownerId:'C004',owner:'杨先生',vin:'LEF******9162',date:'2020-11-08',use:'货运',last:'2026-09-28',result:'合格',status:'资料已核验',orders:8},
  {id:'V005',plate:'粤K·9G6K1',type:'小型载客汽车',model:'大众帕萨特',ownerId:'C005',owner:'黄女士',vin:'LSV******1182',date:'2022-04-12',use:'非营运',last:'2026-10-09',result:'合格',status:'资料已核验',orders:3},
  {id:'V006',plate:'粤K·2P8K9',type:'小型载客汽车',model:'日产轩逸',ownerId:'C006',owner:'王先生',vin:'LGB******3275',date:'2022-03-16',use:'非营运',last:'—',result:'暂无结果',status:'资料待核验',orders:1}
 ],
 owners:[
  {id:'C001',name:'陈女士',nickname:'北岸',phone:'139****6688',vehicles:1,orders:12,last:'2026-10-09',flag:'正常',remark:''},
  {id:'C002',name:'吴先生',nickname:'吴先生',phone:'138****1122',vehicles:1,orders:6,last:'2026-10-08',flag:'需二次核实',remark:'预约前再次核对接车位置'},
  {id:'C003',name:'何女士',nickname:'小何',phone:'137****9021',vehicles:1,orders:2,last:'2026-10-06',flag:'正常',remark:''},
  {id:'C004',name:'杨先生',nickname:'杨先生',phone:'136****1802',vehicles:1,orders:8,last:'2026-09-28',flag:'正常',remark:''},
  {id:'C005',name:'黄女士',nickname:'晴天',phone:'135****6062',vehicles:1,orders:3,last:'2026-10-09',flag:'正常',remark:''},
  {id:'C006',name:'王先生',nickname:'阿文',phone:'134****3379',vehicles:1,orders:1,last:'2026-10-10',flag:'正常',remark:''}
 ],
 drivers:[
  {id:'D012',name:'林晓',phone:'139****0218',type:'员工兼代驾',qualification:'已通过',listening:true,busy:true,license:'C1',expires:'2031-03-12',review:'2027-03-12',tasks:42,score:'4.98',station:'城东检测站',remark:''},
  {id:'D018',name:'周明',phone:'138****8365',type:'专职代驾',qualification:'已通过',listening:true,busy:false,license:'C1',expires:'2029-08-20',review:'2027-02-18',tasks:38,score:'4.96',station:'城北检测站',remark:''},
  {id:'D023',name:'吴杰',phone:'137****5721',type:'专职代驾',qualification:'已通过',listening:true,busy:true,license:'C1',expires:'2030-01-06',review:'2027-01-06',tasks:31,score:'4.93',station:'城东检测站',remark:''},
  {id:'D009',name:'赵志远',phone:'136****3982',type:'员工兼代驾',qualification:'已通过',listening:false,busy:false,license:'C1',expires:'2028-05-11',review:'2027-05-11',tasks:28,score:'4.97',station:'城北检测站',remark:''},
  {id:'D030',name:'梁志辉',phone:'135****8063',type:'专职代驾',qualification:'复审中',listening:false,busy:false,license:'C1',expires:'2027-12-09',review:'2026-10-09',tasks:17,score:'4.89',station:'城东检测站',remark:''},
  {id:'D035',name:'黄海',phone:'134****9016',type:'专职代驾',qualification:'待审核',listening:false,busy:false,license:'待核验',expires:'待核验',review:'待核验',tasks:0,score:'—',station:'未分配',remark:''}
 ],
 cases:[
  {id:'AS20261010-001',orderNo:'DEMO20261011-0451',plate:'粤K·8H3T2',owner:'陈女士',driver:'林晓',driverId:'D012',assignmentId:'ASG-DEMO-451',custody:'STATION',stage:'INSPECTING',kind:'CANCEL_REFUND',reason:'临时调整用车安排，希望停止本次服务并返还车辆。',paid:34000,refunded:0,reserved:0,requested:34000,approved:null,review:'PENDING',returnState:'UNDETERMINED',refundState:'NONE',created:'2026-10-10 09:32',version:1,auditCount:0,notes:[]},
  {id:'AS20261010-002',orderNo:'DEMO20261011-0455',plate:'粤K·9G6K1',owner:'黄女士',driver:'吴杰',driverId:'D023',assignmentId:'ASG-DEMO-455',custody:'OWNER',stage:'WAIT_CONFIRM',kind:'REFUND_ONLY',reason:'对服务费用有疑问，申请部分退款，不取消已完成的接送。',paid:34000,refunded:0,reserved:0,requested:4200,approved:null,review:'PENDING',returnState:'NOT_REQUIRED',refundState:'NONE',created:'2026-10-10 09:15',version:1,auditCount:0,notes:[]},
  {id:'AS20261010-003',orderNo:'DEMO20261012-0454',plate:'粤K·3N2C5',owner:'杨先生',driver:'未承接',driverId:null,assignmentId:null,custody:'OWNER',stage:'SCHEDULED',kind:'CANCEL_REFUND',reason:'计划有变，车辆尚未交付。',paid:34000,refunded:0,reserved:0,requested:34000,approved:null,review:'PENDING',returnState:'NOT_REQUIRED',refundState:'NONE',created:'2026-10-10 08:51',version:1,auditCount:0,notes:[]},
  {id:'AS20261010-004',orderNo:'DEMO20261011-0452',plate:'粤K·5M9Q7',owner:'吴先生',driver:'周明',driverId:'D018',assignmentId:'ASG-DEMO-452',custody:'DRIVER',stage:'CAR_TAKEN',kind:'CANCEL_REFUND',reason:'已批准终止服务，正在返还途中。',paid:34000,refunded:0,reserved:28000,requested:34000,approved:28000,review:'APPROVED',returnState:'WAIT_RETURN',refundState:'WAIT_RETURN',created:'2026-10-10 08:22',version:2,auditCount:1,notes:['同意取消，已发生的服务按审核说明保留。']}
 ],
 ledger:[
  {id:'LE001',driverId:'D012',assignmentId:'ASG-1001',orderNo:'DEMO20261001-0101',plate:'粤K·8H3T2',ended:'2026-10-01 11:32',kind:'正常归还',base:4500,mileage:800,adjustment:0,rule:'DF-01 / V2',status:'可对账'},
  {id:'LE002',driverId:'D012',assignmentId:'ASG-1002',orderNo:'DEMO20261003-0109',plate:'粤K·5M9Q7',ended:'2026-10-03 15:06',kind:'正常归还',base:4500,mileage:1200,adjustment:0,rule:'DF-01 / V2',status:'可对账'},
  {id:'LE003',driverId:'D012',assignmentId:'ASG-1003',orderNo:'DEMO20261005-0131',plate:'粤K·AD1234',ended:'2026-10-05 10:28',kind:'取消后返还',base:3000,mileage:0,adjustment:0,rule:'DF-02 / V1',status:'可对账'},
  {id:'LE004',driverId:'D012',assignmentId:'ASG-1004',orderNo:'DEMO20261007-0166',plate:'粤K·2P8K9',ended:'2026-10-07 12:52',kind:'正常归还',base:4500,mileage:500,adjustment:0,rule:'DF-01 / V2',status:'可对账'},
  {id:'LE005',driverId:'D012',assignmentId:'ASG-1005',orderNo:'DEMO20261008-0182',plate:'粤K·1R8V3',ended:'2026-10-08 09:21',kind:'接车前解除',base:0,mileage:0,adjustment:0,rule:'DF-03 / V1（明确0元）',status:'零额留痕'},
  {id:'LE006',driverId:'D018',assignmentId:'ASG-1006',orderNo:'DEMO20261006-0144',plate:'粤K·3N2C5',ended:'2026-10-06 14:16',kind:'正常归还',base:4500,mileage:700,adjustment:0,rule:'DF-01 / V2',status:'可对账'},
  {id:'LE007',driverId:'D030',assignmentId:'ASG-1007',orderNo:'DEMO20261009-0201',plate:'粤K·7A8D6',ended:'2026-10-09 11:13',kind:'取消后返还',base:null,mileage:null,adjustment:0,rule:'未匹配取消服务费规则',status:'待核价'}
 ]
};
const original=U.makeStore;
U.makeStore=function(){const s=original();s.v4={...structuredClone(U.v4Seeds),selectedVehicle:'V001',selectedOwner:'C001',selectedDriver:'D012',caseId:null,statements:[],statementId:null,adjustments:[],payments:[],queries:{},dirty:false,prefs:{dark:false,contrast:false,reduceMotion:false,reduceTransparency:false},reviewUser:'ADMIN02',driverUser:'D012',fees:{base:4500,includedKm:10,unit:200,cancel:3000,version:'DF-01 / V2'},events:[]};return s;};
U.store=U.makeStore();
U.minor=function(value){let s=String(value).trim();if(!/^-?\d+(\.\d{1,2})?$/.test(s))return null;let neg=s.startsWith('-'),[a,b='']=s.replace('-','').split('.');let n=Number(a)*100+Number(b.padEnd(2,'0'));return Number.isSafeInteger(n)?(neg?-n:n):null;};
U.v4=()=>U.store.v4;
U.vehicle=()=>U.v4().vehicles.find(x=>x.id===U.v4().selectedVehicle)||U.v4().vehicles[0];
U.owner=()=>U.v4().owners.find(x=>x.id===U.v4().selectedOwner)||U.v4().owners[0];
U.driver=()=>U.v4().drivers.find(x=>x.id===U.v4().selectedDriver)||U.v4().drivers[0];
U.caseFor=function(v={}){
 let s=U.v4(), id=s.caseId;
 if(!id){id=v.state==='before'?'AS20261010-003':v.state==='refund'?'AS20261010-002':v.state==='return'?'AS20261010-004':'AS20261010-001';}
 let c=s.cases.find(x=>x.id===id)||s.cases[0];
 return c;
};
U.caseState=c=>c.review==='PENDING'?'待审核':c.review==='REJECTED'?'已驳回':c.returnState==='WAIT_RETURN'?'取消已通过，待归还车辆':['UNKNOWN','ABNORMAL'].includes(c.refundState)?'退款待核实':c.refundState==='PROCESSING'?'退款处理中':['SUCCESS','NOT_REQUIRED'].includes(c.refundState)?'已办结':'审核已通过';
U.caseKind=c=>c.kind==='CANCEL_REFUND'?'取消并处理退款':'仅退款，不取消服务';
U.refundLabel=s=>({NONE:'尚未审核',WAIT_RETURN:'返还后执行',READY:'等待渠道处理',PROCESSING:'渠道处理中',SUCCESS:'退款成功',UNKNOWN:'结果待核实',ABNORMAL:'渠道异常',NOT_REQUIRED:'本次无退款'})[s]||s;
U.driverEntries=(id)=>U.v4().ledger.filter(x=>x.driverId===id);
U.entryAmount=x=>x.base===null||x.mileage===null?null:x.base+x.mileage+x.adjustment;
U.statement=function(){const s=U.v4();return s.statements.find(x=>x.id===s.statementId)||null;};
U.previewStatement=function(){const id=U.v4().selectedDriver;const from=String(U.get('recon.from','2026-10-01')),to=String(U.get('recon.to','2026-10-10'));const used=new Set(U.v4().statements.filter(x=>x.status!=='VOID').flatMap(x=>x.entries.map(y=>y.id)));return U.driverEntries(id).filter(x=>x.ended.slice(0,10)>=from&&x.ended.slice(0,10)<=to&&!used.has(x.id));};
U.totalEntries=xs=>xs.reduce((n,x)=>n+(U.entryAmount(x)||0),0);
U.confirmedPaid=statementId=>U.v4().payments.filter(x=>x.statementId===statementId&&x.status==='VERIFIED').reduce((n,x)=>n+x.amount,0);
U.tableState=(kind,title='暂无匹配结果')=>kind==='loading'?'<div class="loading-state" aria-busy="true"><div class="skeleton big"></div><div class="skeleton"></div><div class="skeleton"></div></div>':U.empty(kind==='forbidden'?'暂无查看权限':kind==='error'?'暂时无法加载':title,kind==='forbidden'?'请联系管理员核对资料访问范围。':kind==='error'?'筛选条件已保留，请重试。':'调整筛选条件，或者清除当前筛选。',U.button(kind==='error'?'重新加载':'返回列表','v4-list-reset','secondary'));
})(window.UI);
