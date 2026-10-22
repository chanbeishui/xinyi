/* V6.4 third-party configuration. Demonstration state only; never real credentials or sends. */
(function(U){'use strict';
U.integration64=function(){if(!U.store.integration64)U.store.integration64={touched:false,previewId:null,
 active:{wechatEnabled:true,offlineEnabled:false,revision:1},draft:{wechatEnabled:true,offlineEnabled:false,revision:1},
 wx:{draft:{merchantName:'示例检测公司（演示）',mchid:'1900000001',appId:'wx0000000000000000',serial:'DEMO_CERT_SERIAL',mode:'PUBLIC_KEY',keyId:'PUB_KEY_ID_DEMO',publicKey:'演示公钥占位',binding:true},secrets:{privateKey:true,apiKey:true},saved:null,activeReady:true,revision:1,checked:false},
 sms:{provider:'TENCENT',tencent:{secretId:'DEMO_SECRET_ID',region:'ap-guangzhou',appId:'1400000000',sign:'示例检测',secret:true,enabled:true},aliyun:{accessKeyId:'DEMO_ACCESS_KEY_ID',region:'cn-hangzhou',sign:'示例检测',secret:true,enabled:true},activeProvider:'TENCENT',activeProfiles:{TENCENT:{enabled:true,revision:1},ALIYUN:{enabled:true,revision:1}},routes:{BOOKING_CONFIRMED:'TENCENT'},revision:1,checked:false},
 template:{provider:'TENCENT',scene:'BOOKING_CONFIRMED',templateId:'DEMO_APPROVED_TEMPLATE',sign:'示例检测',mapping:'1:orderNo\n2:pickupWindow',fallback:'NONE',fallbackTemplate:'',enabled:true,active:null,checked:false,revision:1},
 sub:{templateId:'DEMO_SUBSCRIBE_TEMPLATE',scene:'INSPECTION_FINISHED',audience:'OWNER',page:'pages/orders/detail',env:'formal',lang:'zh_CN',type:'ONCE',mapping:'thing1:plateNo\ntime2:serviceDate\nphrase3:resultText',enabled:true,checked:false,active:null,revision:1},
 app:{appId:'wx0000000000000000',name:'车检小程序（演示）',secret:true,activeReady:true,revision:1,checked:false},
 offline:{receiver:'示例检测公司',methods:['CASH','BANK_TRANSFER'],bankName:'示例银行',accountName:'示例检测公司',bankConfigured:true,contact:'公司财务',phone:'0668-0000000',instructions:'请向公司对公账户或柜台付款，由财务核实。'},method:'UNSELECTED',proof:null,history:[],deliveries:[],consent:false,editor:'财务甲',errors:[],activeTab:'CONFIG',checks:{},conflict:false};return U.store.integration64;};
U.g64Error=function(field,message){return{field,message};};
U.g64Validate=function(kind){const s=U.integration64(),E=[],v=(k,d='')=>String(U.get('g64.'+k,d)).trim(),req=(key,label,d)=>{if(!v(key,d))E.push(U.g64Error('g64.'+key,'请填写'+label));};
 if(kind==='policy'){
  if(!!U.get('g64.wechat',s.draft.wechatEnabled)&&!s.wx.activeReady)E.push(U.g64Error('g64.wechat','微信支付商户未完整校验发布，请先完成商户信息。'));
  if(!!U.get('g64.offline',s.draft.offlineEnabled)){req('receiver','企业收款主体名称','示例检测公司');req('instructions','线下收款说明',s.offline.instructions);req('contact','企业收款联系人',s.offline.contact);req('phone','企业服务电话',s.offline.phone);const cash=U.get('g64.cash',s.offline.methods.includes('CASH')),bank=U.get('g64.transfer',s.offline.methods.includes('BANK_TRANSFER'));if(!cash&&!bank)E.push(U.g64Error('g64.methods','启用线下支付时至少选择一种收款方式。'));if(bank){req('bank','开户行',s.offline.bankName);req('accountName','对公户名',s.offline.accountName);if(!s.offline.bankConfigured)E.push(U.g64Error('g64.bankAccount','启用对公转账需配置公司收款账号。'));}}
 }
 if(kind==='wx'){
  const d=s.wx.draft;[['merchantName','商户主体名称'],['mchid','商户号'],['appId','关联小程序AppID'],['serial','商户证书序列号']].forEach(([k,l])=>req('wx.'+k,l,d[k]));
  if(!s.wx.secrets.privateKey)E.push(U.g64Error('g64.wx.privateKey','请配置商户私钥。'));
  if(!s.wx.secrets.apiKey)E.push(U.g64Error('g64.wx.apiKey','请配置32字节API v3密钥。'));
  const mode=v('wx.mode',d.mode);if(mode==='PUBLIC_KEY'){req('wx.keyId','微信支付公钥ID',d.keyId);req('wx.publicKey','微信支付公钥PEM',d.publicKey);}else req('wx.platformCert','受信任微信支付平台证书','');
  if(!U.get('g64.wx.binding',d.binding))E.push(U.g64Error('g64.wx.binding','请确认AppID与商户号已在微信平台完成关联。'));
 }
 if(kind==='sms'){
  let provider=v('sms.provider',s.sms.provider),d=provider==='TENCENT'?s.sms.tencent:s.sms.aliyun;
  if(provider==='TENCENT'){req('sms.secretId','SecretId',d.secretId);req('sms.appId','SmsSdkAppId',d.appId);}else req('sms.accessKeyId','AccessKeyId',d.accessKeyId);
  req('sms.region','Region',d.region);req('sms.sign','已审核签名',d.sign);
  if(!d.secret)E.push(U.g64Error('g64.sms.secret','请配置该通道SecretKey / AccessKeySecret。'));
 }
 if(kind==='template'){
  const selectedProvider=String(U.get('g64.template.provider',s.template.provider));if(!s.sms.activeProfiles[selectedProvider]?.enabled)E.push(U.g64Error('g64.template.provider','所选主通道未发布启用，不能发送。'));if(v('template.fallback',s.template.fallback)!=='NONE'&&!v('template.fallbackTemplate',s.template.fallbackTemplate))E.push(U.g64Error('g64.template.fallbackTemplate','请指定已发布备用模板；仅确定未发送时才能尝试备用。'));
  req('template.id','供应商模板ID',s.template.templateId);req('template.sign','已审核签名',s.template.sign);req('template.mapping','模板参数映射',s.template.mapping);
  const pro=v('template.provider',s.template.provider),mp=v('template.mapping',s.template.mapping);
  if(pro==='TENCENT'&&mp&&!mp.split('\n').every((l,i)=>l.startsWith((i+1)+':')))E.push(U.g64Error('g64.template.mapping','腾讯云必须使用从1开始的连续顺序参数。'));
  if(pro==='ALIYUN'&&mp&&mp.split('\n').some(l=>/^\d+:/.test(l)))E.push(U.g64Error('g64.template.mapping','阿里云请按模板变量名映射，不能沿用腾讯云数字顺序。'));
 }
 if(kind==='sub'){
  req('sub.id','订阅模板ID',s.sub.templateId);req('sub.mapping','模板字段映射',s.sub.mapping);req('sub.page','合法小程序页面',s.sub.page);
  if(/^https?:|\.\.|(?:phone|token|secret)=/i.test(v('sub.page',s.sub.page)))E.push(U.g64Error('g64.sub.page','仅允许已登记的小程序页面，参数不能包含手机号或Token。'));
  if(v('sub.env',s.sub.env)!=='formal')E.push(U.g64Error('g64.sub.env','当前正式发布示例只能使用formal页面版本。'));
  if(v('sub.type',s.sub.type)==='LONG')E.push(U.g64Error('g64.sub.type','尚未提供长期订阅资格；普通业务请使用一次性订阅。'));
  if(!s.app.activeReady)E.push(U.g64Error('g64.sub.appId','小程序公共凭据尚未发布。'));
 }
 if(kind==='app'){req('app.appId','小程序AppID',s.app.appId);if(!s.app.secret)E.push(U.g64Error('g64.app.secret','请配置AppSecret。'));}
 if(!v('reason'))E.push(U.g64Error('g64.reason','请填写变更原因。'));
 s.errors=E;return E;
};
U.g64Log=function(x){const s=U.integration64();s.history.unshift({at:new Date().toLocaleTimeString(),text:x,revision:s.active.revision});};
U.prepareIntegrationPreview=function(v){const s=U.integration64();if(s.touched||s.previewId===v.id)return;s.previewId=v.id;
 const clear=prefix=>Object.keys(U.store.forms).filter(k=>k.startsWith(prefix)).forEach(k=>delete U.store.forms[k]);clear('g64.');s.errors=[];
 if(v.base==='B46'){
  const off=['both','offline'].includes(v.state),wx=['both','default'].includes(v.state);s.active={wechatEnabled:wx,offlineEnabled:off,revision:2};s.draft={...s.active};
  if(v.state==='draft'){s.draft={wechatEnabled:false,offlineEnabled:true,revision:3};}
  if(v.state==='invalid'){s.draft.wechatEnabled=true;s.wx.activeReady=false;s.errors=[{field:'g64.wechat',message:'开启微信支付前，请先完成商户号、私钥、API v3密钥和验签配置。'}];}
  if(v.state==='conflict'){s.conflict=true;}if(v.state==='conflict')s.errors=[{field:'g64.version',message:'配置已由另一位管理员发布，请刷新后重新核对。'}];
 }
 if(v.base==='B47'&&v.state==='empty'){s.wx.draft={merchantName:'',mchid:'',appId:'',serial:'',mode:'PUBLIC_KEY',keyId:'',publicKey:'',binding:false};s.wx.secrets={privateKey:false,apiKey:false};s.wx.activeReady=false;}
 if(v.base==='B47'&&v.state==='certificate'){s.wx.draft.mode='PLATFORM_CERT';U.set('g64.wx.platformCert','演示平台证书占位（非真实证书）');}
 if(v.base==='B47'&&v.state==='rotating')s.errors=[{field:'g64.wx.serial',message:'换证提示：新证书须先校验，历史交易仍使用原商户身份及有效轮换凭据。'}];
 if(v.base==='B48'&&v.state==='disabled')s.sms.tencent.enabled=false;
 if(v.base==='B48'&&v.state==='failed')s.errors=[{field:'g64.sms.secret',message:'只读连接检查失败：请核对Secret和短信服务授权；未切换线上通道。'}];
 if(v.base==='B49'&&v.state==='invalid'){s.template.mapping='';s.errors=[{field:'g64.template.mapping',message:'该模板缺少必填参数映射，不能发布。'}];}
 if(v.base==='B49'&&v.state==='test')s.errors=[{field:'g64.test',message:'测试须选择白名单收件人并确认费用。当前仅演示，不实际发送。'}];
 if(v.base==='B50'&&v.state==='no-consent'){s.consent=false;s.errors=[{field:'g64.sub',message:'该用户暂无可用订阅授权：跳过微信通知，站内消息和业务继续。'}];}
 if(v.base==='B50'&&v.state==='disabled')s.sub.enabled=false;
 if(v.base==='B53'&&v.state==='empty'){s.app.appId='';s.app.secret=false;s.app.activeReady=false;}
 if(v.base==='B48'&&v.state==='aliyun')s.sms.provider='ALIYUN';
 if(v.base==='B49'&&v.state==='aliyun'){s.template.provider='ALIYUN';s.template.mapping='order:orderNo\ntime:pickupWindow';}
 if(v.base==='B50'&&v.state==='invalid')s.errors=[{field:'g64.sub.mapping',message:'模板thing1尚未映射；不能发布。'}];
 if(v.base==='B51')s.deliveries=[{id:'N-001',provider:'腾讯云短信',scene:'预约确认',status:v.state==='unknown'?'UNKNOWN':v.state==='failed'?'FAILED':'ACCEPTED',recipient:'139****0001'},{id:'N-002',provider:'微信订阅',scene:'检测完成',status:'SKIPPED',recipient:'车主本人'}];
 if(v.base==='B52'&&v.state!=='default'){s.proof={id:'OFF-001',direction:v.state==='refund'?'REFUND':'COLLECTION',amount:34000,status:v.state==='verified'?'VERIFIED':'SUBMITTED',operator:'财务甲',reference:'演示凭证-001',late:v.state==='late'};s.method='OFFLINE';}
 if(v.base==='O10'){
  if(['offline','both','review','offline-paid'].includes(v.state))s.active.offlineEnabled=true;
  if(v.state==='offline'){s.active.wechatEnabled=false;s.method='OFFLINE';}
  if(v.state==='disabled'){s.active.offlineEnabled=false;s.active.wechatEnabled=false;}
  if(['review','offline-paid'].includes(v.state)){s.method='OFFLINE';s.proof={status:v.state==='offline-paid'?'VERIFIED':'SUBMITTED',amount:34000,operator:'财务甲'};}
 }
};
})(window.UI);
