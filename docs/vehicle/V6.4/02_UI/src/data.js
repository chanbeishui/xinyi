window.UI=window.UI||{};
(function(U){
'use strict';
U.version='V6.4 · 全量一致性修正版';
U.demoOrder={orderNo:'DEMO20261011-0451',plate:'粤K·8H3T2',vehicleType:'小型载客汽车',typeCode:'SMALL',model:'本田雅阁',owner:'陈女士',phone:'139****6688',driver:'林晓',station:'城东检测站',stationId:'ST01',address:'信宜市东镇街道迎宾大道 88 号 · 东门停车区',returnAddress:'原接车地点 · 东门停车区',slot:'2026-10-11 09:00–10:00',internalSlot:'2026-10-11 09:40–10:40',createdAt:'2026-10-10 16:20',dispatchAt:'2026-10-11 08:30',price:34000,paid:34000,refunded:0,reservedRefund:0,priceVersion:'PRICE-DEMO-03',assignmentId:'ASG-DEMO-451',stage:'INSPECTING',custody:'STATION',cancel:'NONE',refund:'NONE',dispute:'NONE',returnedAt:null,releasedAt:null,occupied:true,occupiedHolder:'ASG-DEMO-451',result:'FAIL',resultSaved:false,licenseVerified:false,stationReleased:false,photos:{pickup:[],arrival:[],take:[],return:[]}};
U.filterConfig={pricing:['方案 / 车型',['全部','已发布','不可报价']],stations:['检测点 / 地址',['全部','启用','暂停新预约']],employees:['员工姓名 / 编号',['全部','可工作','待绑定']],drivers:['代驾姓名 / 手机号',['全部','已通过','复审中']],owners:['车主昵称 / 手机号',['全部','正常','需二次核实']],audit:['操作人 / 动作 / 对象',['全部','小程序','PC 后台']],templates:['事件 / 接收方',['全部','演示配置']],qualification:['申请人 / 手机号',['全部','待审核']],correction:['申请号 / 订单',['全部','待审核']],refunds:['退款号 / 订单',['全部','待审核','已批准','处理中','结果未知']],exceptions:['工单 / 车辆',['全部','待响应','处理中','核实中']],receive:['车牌 / 订单号',['全部','代驾']],vehicles:['车牌 / 品牌 / 车主',['全部']], 'result-todo':['车牌 / 订单号',['全部','待复核','已核实']], 'dispatch-records':['邀约 / 订单 / 代驾',['全部','接受','已解除','超时','拒绝']],reconcile:['订单 / 交易号',['全部','待核实','UNKNOWN']],access:['申请号 / 用途',['全部','待审核','已批准']], 'vehicle-types':['车型编码 / 名称',['全部','启用','停用','不可报价']],notifications:['事件 / 接收方',['全部','失败','已接受','已失效']]};
U.stageLabels={PENDING_PAY:'待支付',SCHEDULED:'预约成功，待安排',DISPATCHING:'正在安排代驾',MANUAL_DISPATCH:'运营协调中',ACCEPTED:'代驾已接单',GOING_PICKUP:'代驾前往接车',ARRIVED_PICKUP:'代驾已到达',PICKED_UP:'已接车，送检中',ARRIVED_STATION:'已到站，待收车',INSPECTING:'检测中',INSPECTED:'检测完成，待取车',CAR_TAKEN:'返还途中',WAIT_CONFIRM:'车辆已归还，待服务确认',COMPLETED:'服务已完成',CANCELLED:'订单已取消',CLOSED:'未支付已关闭'};
U.custodyLabels={OWNER:'车主',DRIVER:'代驾 · 林晓',STATION:'检测点 · 城东检测站'};
U.seeds={
 types:[{code:'SMALL',name:'小型载客汽车',enabled:true,priced:true,order:1},{code:'LIGHT',name:'轻型货车',enabled:true,priced:false,order:2}],
 pricing:{inspection:29800,driver:4200,includedKm:10,unitPrice:250,unitKm:1,route:'ONE_WAY',rounding:'CEIL',version:'PRICE-DEMO-03',status:'已发布'},
 stations:[{id:'ST01',name:'城东检测站',address:'工业大道 12 号',hours:'08:00–18:00',orders:12,used:12,total:18,enabled:true},{id:'ST02',name:'城北检测站',address:'北环路 26 号',hours:'08:30–17:30',orders:8,used:8,total:14,enabled:true}],
 settings:{lead:30,response:60,radius:5,expand:8,totalDeadline:300,minimumLead:60,reviewReminder:30},
 candidates:[{id:'D018',name:'周明',distance:'2.4 km',locationTime:'09:28',score:'4.9',tasks:2,status:'可接受邀约',eligible:true},{id:'D023',name:'吴杰',distance:'3.1 km',locationTime:'09:29',score:'4.8',tasks:1,status:'在手任务未结束',eligible:false},{id:'D009',name:'赵志远',distance:'4.0 km',locationTime:'09:27',score:'4.9',tasks:3,status:'可接受邀约',eligible:true}],
 orders:[
  {no:'DEMO20261011-0451',plate:'粤K·8H3T2',owner:'陈女士',station:'城东检测站',driver:'林晓',slot:'10-11 09:00–10:00',stage:'INSPECTING',cancel:'取消待审核',custody:'检测点',amount:34000,pay:'已支付'},
  {no:'DEMO20261011-0452',plate:'粤K·5M9Q7',owner:'吴先生',station:'城北检测站',driver:'周明',slot:'10-11 10:00–11:00',stage:'CAR_TAKEN',cancel:'批准待返还',custody:'代驾',amount:34000,pay:'已支付'},
  {no:'DEMO20261011-0453',plate:'粤K·1R8V3',owner:'何女士',station:'城东检测站',driver:'待安排',slot:'10-11 11:00–12:00',stage:'MANUAL_DISPATCH',cancel:'无申请',custody:'车主',amount:34000,pay:'已支付'},
  {no:'DEMO20261012-0454',plate:'粤K·3N2C5',owner:'杨先生',station:'城东检测站',driver:'未到派单时间',slot:'10-12 14:00–15:00',stage:'SCHEDULED',cancel:'无申请',custody:'车主',amount:34000,pay:'已支付'},
  {no:'DEMO20261011-0455',plate:'粤K·9G6K1',owner:'黄女士',station:'城北检测站',driver:'吴杰',slot:'10-11 08:00–09:00',stage:'WAIT_CONFIRM',cancel:'无申请',custody:'车主',amount:34000,pay:'已支付'},
  {no:'DEMO20261011-0456',plate:'粤K·2P8K9',owner:'王先生',station:'城东检测站',driver:'无承接',slot:'10-11 14:00–15:00',stage:'PENDING_PAY',cancel:'无申请',custody:'车主',amount:34000,pay:'待支付'},
 ],
 resultItems:[['APPEARANCE','外观检查','PASS','已登记合格'],['BRAKE','制动性能','FAIL','登记人员确认不合格，详见检测凭证'],['LIGHT','灯光检测','PASS','已登记合格'],['EMISSION','尾气排放','PASS','已登记合格'],['CHASSIS','底盘检查','PASS','已登记合格']],
 audit:[['10-11 12:00','林晓','实际归还交接','ASG-DEMO-449','代驾 → 车主；结束匹配承接','小程序'],['10-11 11:20','周工','检测结果登记','DEMO20261011-0449','创建结果版本 R01','小程序'],['10-11 10:32','运营 · 陈工','批准取消','DEMO20261011-0452','取消待审 → 批准待返还','PC 后台'],['10-11 09:50','周工','检测点确认收车','DEMO20261011-0451','保管：代驾 → 检测点','小程序'],['10-11 09:12','管理员','站点授权更新','员工 E012','增加城北检测站授权','PC 后台']],
};
U.makeStore=function(){return{forms:{},errors:{},filterGroup:'ALL',search:'',role:'O',stationId:'ST01',permissions:['receive','result','release','station-edit'],authorized:[],qualification:'NONE',listening:true,flow:null,flowStep:0,live:structuredClone(U.demoOrder),occupancy:{assignmentId:U.demoOrder.assignmentId,orderNo:U.demoOrder.orderNo},saved:{},readMessages:[],binding:'NONE',largeText:false,codeErrors:{},photos:{pickup:[],arrival:[],take:[],return:[]},types:structuredClone(U.seeds.types),pricing:structuredClone(U.seeds.pricing),pricingRules:{SMALL:structuredClone(U.seeds.pricing)},settings:structuredClone(U.seeds.settings),stations:structuredClone(U.seeds.stations),history:[],logs:[],pendingAction:null,selectedCandidate:'D018',paidOrderCount:0};};
U.store=U.makeStore();
U.get=function(key,def=''){return Object.prototype.hasOwnProperty.call(U.store.forms,key)?U.store.forms[key]:def;};
U.set=function(key,value){U.store.forms[key]=value;};
U.money=function(cents){return '¥'+(Number(cents||0)/100).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2});};
U.station=function(){return U.store.stations.find(x=>x.id===U.store.stationId)||U.store.stations[0];};
U.order=function(stage){if(U.store.flow)return U.store.live;const o=structuredClone(U.demoOrder);if(stage)o.stage=stage;const s=o.stage;o.custody=['PENDING_PAY','SCHEDULED','DISPATCHING','MANUAL_DISPATCH','ACCEPTED','GOING_PICKUP','ARRIVED_PICKUP','WAIT_CONFIRM','COMPLETED','CANCELLED','CLOSED'].includes(s)?'OWNER':['INSPECTING','INSPECTED'].includes(s)?'STATION':'DRIVER';o.occupied=!['PENDING_PAY','SCHEDULED','DISPATCHING','MANUAL_DISPATCH','WAIT_CONFIRM','COMPLETED','CANCELLED','CLOSED'].includes(s);o.station=U.station().name;o.stationId=U.station().id;if(s==='SCHEDULED'){o.slot='2026-10-12 14:00–15:00';o.dispatchAt='2026-10-12 13:30';}return o;};
U.log=function(action,detail){U.store.logs.unshift({time:new Date().toLocaleTimeString('zh-CN'),action,detail});};
})(window.UI);
