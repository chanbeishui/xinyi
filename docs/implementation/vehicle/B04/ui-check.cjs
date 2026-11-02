const {chromium}=require('playwright'),fs=require('fs');
(async()=>{const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});const results=[],errors=[];
const owner=JSON.parse(fs.readFileSync('/Users/chenbeishui/Downloads/code_project/xinyi/.vi-local/b04-OwnerOrderView.json','utf8'));
const driver={...JSON.parse(fs.readFileSync('/Users/chenbeishui/Downloads/code_project/xinyi/.vi-local/b04-DriverOrderView-history.json','utf8')),orderNo:owner.orderNo};
const station={...JSON.parse(fs.readFileSync('/Users/chenbeishui/Downloads/code_project/xinyi/.vi-local/b04-StationOrderView.json','utf8')),orderNo:owner.orderNo};
const admin=JSON.parse(fs.readFileSync('/Users/chenbeishui/Downloads/code_project/xinyi/.vi-local/b04-AdminOrderView.json','utf8'));
try{
for(const role of ['OWNER','DRIVER','STATION']){
 const page=await browser.newPage({viewport:{width:390,height:844}});page.on('pageerror',e=>errors.push(role+':'+e.message));await page.addInitScript(role=>localStorage.setItem('vi.session.local.http://127.0.0.1:8080',JSON.stringify({type:'object',data:{sessionId:'1',activeRole:role,contextVersion:1,token:'ui-fixture'}})),role);
 let mode='success',listCalls=0;const view=role==='OWNER'?owner:role==='DRIVER'?driver:station;
 await page.route('http://127.0.0.1:8080/api/**',async r=>{const u=new URL(r.request().url());let body={code:200,msg:'操作成功',data:view},status=200;
 if(u.pathname.endsWith('/orders')){listCalls++;if(mode==='error'){status=503;body={code:503,errorCode:'VI_SERVICE_UNAVAILABLE',msg:'订单测试暂不可用'};mode='success';}else body={code:200,msg:'操作成功',rows:mode==='empty'?[]:[{...view,allowedActions:['VIEW_DETAIL']}],total:mode==='empty'?0:1};}
 else if(u.pathname.endsWith('/timeline')) body={code:200,msg:'操作成功',rows:[{eventId:'1',orderNo:view.orderNo,action:'ORDER_CREATE',displayText:'预约已创建',actorLabel:'车主',at:'2026-11-02T10:00:00Z'}],total:1};
 await r.fulfill({status,json:body,headers:{'access-control-allow-origin':'*','access-control-allow-headers':'*'}});
 });
 await page.goto('http://127.0.0.1:5174/#/pages/order/list');await page.getByText('查看详情',{exact:true}).click();await page.getByText('刷新订单状态',{exact:true}).waitFor();
 if(role==='OWNER'){await page.getByText('客户费用',{exact:true}).waitFor();await page.getByText('预约已创建',{exact:true}).waitFor();}
 if(role==='DRIVER'){await page.getByText('历史服务，地址与联系人已隐藏',{exact:true}).waitFor();if((await page.locator('body').innerText()).includes('测试接车地址'))throw Error('历史司机泄露地址');}
 if(role==='STATION'&&((await page.locator('body').innerText()).includes('客户费用')||(await page.locator('body').innerText()).includes('测试接车地址')))throw Error('检测端泄露金额地址');
 await page.getByText('刷新订单状态',{exact:true}).click();await page.getByText('刷新订单状态',{exact:true}).waitFor();await page.screenshot({path:'/private/tmp/b04-'+role.toLowerCase()+'-mobile.png',fullPage:true});
 if(await page.locator('vite-error-overlay').count())throw Error('Vite overlay');results.push({role,url:page.url(),title:await page.title(),listCalls,detailRefresh:true,redaction:true});
 if(role==='OWNER'){mode='empty';await page.goto('http://127.0.0.1:5174/#/pages/order/list');await page.getByText('暂无符合条件的订单',{exact:true}).waitFor();mode='error';await page.reload();await page.getByText('订单测试暂不可用',{exact:true}).waitFor();await page.getByText('重新加载',{exact:true}).click();await page.getByText('查看详情',{exact:true}).waitFor();results.push({empty:true,errorRetry:true});}
 await page.close();
}
const context=await browser.newContext({viewport:{width:1440,height:1000}});await context.addCookies([{name:'Admin-Token',value:'ui-fixture-only',url:'http://127.0.0.1:5173'}]);const p=await context.newPage();p.on('pageerror',e=>errors.push('PC:'+e.message));let restricted=true;
await p.route('**/dev-api/**',async r=>{const u=new URL(r.request().url()),path=u.pathname.replace('/dev-api','');let body={code:200,msg:'操作成功',data:[]};
 if(path==='/getInfo')body={code:200,user:{userId:1,userName:'ui-qa'},roles:['admin'],permissions:['*:*:*']};
 else if(path==='/getRouters')body={code:200,data:[{path:'/rongli-orders',component:'Layout',children:[{path:'index',component:'rongli/order/order',name:'OrderList',meta:{title:'客户订单'}}]}]};
 else if(path==='/api/admin/orders')body={code:200,msg:'操作成功',rows:[{...admin,allowedActions:['VIEW_DETAIL']}],total:1};
 else if(path.startsWith('/api/admin/orders/'))body={code:200,msg:'操作成功',data:{...admin,...(restricted?{money:null,pickupAddress:null,returnAddress:null,afterSale:null}:{})}};
 await r.fulfill({json:body});
});
await p.goto('http://127.0.0.1:5173/rongli-orders/index');await p.getByRole('button',{name:'查看详情',exact:true}).click();await p.getByRole('button',{name:'刷新详情',exact:true}).waitFor();if(await p.getByText('客户费用',{exact:true}).count())throw Error('未授权金额在PC显示');await p.waitForFunction(()=>{const d=document.querySelector('.el-drawer');if(!d)return false;const r=d.getBoundingClientRect();return r.x>=0&&r.x<800&&r.right<=innerWidth+1});await p.screenshot({path:'/private/tmp/b04-admin-restricted.png',fullPage:false});restricted=false;await p.getByRole('button',{name:'刷新详情',exact:true}).click();await p.getByText('客户费用',{exact:true}).waitFor();await p.getByText('测试接车地址',{exact:true}).first().waitFor();await p.screenshot({path:'/private/tmp/b04-admin-full.png',fullPage:false});if(await p.locator('vite-error-overlay').count())throw Error('PC overlay');results.push({role:'ADMIN',url:p.url(),title:await p.title(),restrictedThenFull:true});
if(errors.length)throw Error(errors.join('\n'));fs.writeFileSync('/private/tmp/b04-ui.json',JSON.stringify({passed:true,browser:'Browser plugin not available; temporary Playwright + local Chrome',mode:'真实页面+明确API界面夹具；后端业务另由MySQL/HTTP集成验证',checks:results,errors},null,2));console.log('B04 UI PASS');
}finally{await browser.close()}})().catch(e=>{console.error(e);process.exit(1)});
