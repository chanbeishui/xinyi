const {chromium}=require('playwright');const fs=require('node:fs');
(async()=>{const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});try{
const context=await browser.newContext({viewport:{width:1440,height:1000}});await context.addCookies([{name:'Admin-Token',value:'ui-fixture-only',url:'http://127.0.0.1:5173'}]);const page=await context.newPage();const errors=[],expectedErrors=[],writes=[];let row;
page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error'){if(m.text().includes('503 (Service Unavailable)'))expectedErrors.push(m.text());else errors.push(m.text())}});
await page.route('**/dev-api/**',async route=>{const req=route.request(),u=new URL(req.url()),path=u.pathname.replace('/dev-api','');let body={code:200,msg:'操作成功',data:[]};
if(path==='/getInfo')body={code:200,user:{userId:1,userName:'ui-qa'},roles:['admin'],permissions:['*:*:*']};
else if(path==='/system/dept/list')body={code:200,data:[{deptId:994001,deptName:'新益检测公司',status:'0'}]};
else if(path.startsWith('/system/dict/data/type/'))body={code:200,data:[path.endsWith('vi_vehicle_type')?{dictLabel:'小型汽车',dictValue:'C04_CAR'}:{dictLabel:'年度检测',dictValue:'C04_ANNUAL'}]};
else if(path==='/api/admin/prices'&&req.method()==='GET')body={code:200,rows:row?[row]:[],total:row?1:0,environment:'TEST'};
else if(path==='/api/admin/prices'&&req.method()==='POST'){
 const input=JSON.parse(req.postData());writes.push({body:input,key:req.headers()['idempotency-key']});
 if(writes.length===1){await route.fulfill({status:503,contentType:'application/json',body:JSON.stringify({code:503,errorCode:'VI_UPSTREAM_UNAVAILABLE',msg:'计价测试响应中断',retryable:true})});return;}
 row={ruleId:'8801',ownerDeptId:'994001',companyName:'新益检测公司',environment:'TEST',ruleKey:'ui-fixture',revision:1,status:'DRAFT',config:input,version:0,createdBy:'1',contentHash:'a'.repeat(64)};body={code:200,data:row};
}else if(path==='/api/admin/prices/8801/trial'){
 body={code:200,data:{ruleId:'8801',revision:1,version:0,matchedPublished:false,billableDistanceMeter:1001,excessDistanceMeter:1,inspectionFee:20029,driverBaseFee:3001,distanceFee:105,totalFen:23135}};
}else if(path==='/api/admin/prices/8801/submit'){
 const input=JSON.parse(req.postData());writes.push({body:input,key:req.headers()['idempotency-key'],action:'submit'});row={...row,status:'IN_REVIEW',version:1};body={code:200,data:row};
}
await route.fulfill({contentType:'application/json',body:JSON.stringify(body)});
});
await page.goto('http://127.0.0.1:5173/rongli/pricing');await page.getByRole('button',{name:'新增计价方案',exact:true}).click();const dialog=page.getByRole('dialog',{name:'新增计价方案',exact:true});
const field=label=>dialog.locator('.el-form-item').filter({has:page.locator('label').filter({hasText:label})});
await dialog.locator('.el-select').nth(0).click();await page.getByRole('option',{name:'新益检测公司',exact:true}).click();
await field('方案名称').locator('input').fill('公司统一年度方案');await dialog.locator('.el-select').nth(1).click();await page.getByRole('option',{name:'小型汽车',exact:true}).click();await dialog.locator('.el-select').nth(2).click();await page.getByRole('option',{name:'年度检测',exact:true}).click();
await field('检测费（元）').locator('input').fill('200.291');await field('代驾起步价（元）').locator('input').fill('30.01');await field('每单位超里程费（元）').locator('input').fill('1.05');await field('包含里程（米）').getByRole('spinbutton').fill('1000');
await dialog.getByRole('button',{name:'保存草稿',exact:true}).click();await page.getByText('金额须为非负数且最多两位小数',{exact:true}).waitFor();if(writes.length)throw Error('非法小数不应提交');
await field('检测费（元）').locator('input').fill('200.29');await dialog.getByRole('button',{name:'保存草稿',exact:true}).click();await page.getByText('计价测试响应中断',{exact:true}).waitFor();await dialog.getByRole('button',{name:'保存草稿',exact:true}).click();await page.getByText('草稿已保存，尚未生效',{exact:true}).waitFor();
if(writes.length!==2||writes[0].key!==writes[1].key||JSON.stringify(writes[0].body)!==JSON.stringify(writes[1].body)||writes[1].body.inspectionFee!==20029||writes[1].body.driverBaseFee!==3001||writes[1].body.distanceUnitFee!==105)throw Error('分转换或重试幂等错误');
await page.getByRole('button',{name:'试算',exact:true}).click();const trial=page.getByRole('dialog',{name:'已保存规则试算',exact:true});await trial.getByRole('spinbutton').fill('1001');await trial.getByRole('button',{name:'开始试算',exact:true}).click();await trial.getByText('231.35 元',{exact:true}).waitFor();
await page.locator('.el-message').last().waitFor({state:'hidden'});await page.waitForFunction(()=>{let e=[...document.querySelectorAll('.el-dialog')].find(e=>e.innerText.includes('231.35'));if(!e)return false;for(;e;e=e.parentElement)if(Number(getComputedStyle(e).opacity)<.99)return false;return true;});await page.screenshot({path:'/private/tmp/xinyi-pricing-trial-desktop.png',animations:'disabled'});
await trial.locator('.el-dialog__headerbtn').click();await page.getByRole('button',{name:'提交审核',exact:true}).click();const action=page.getByRole('dialog',{name:'提交审核',exact:true});await action.locator('textarea').fill('核实公司统一费用及生效区间');await action.getByRole('button',{name:'提交审核',exact:true}).click();await page.getByText('第 1 版 · 待审核',{exact:true}).waitFor();if(writes.at(-1).body.expectedVersion!==0)throw Error('丢失原始版本');
await page.getByRole('button',{name:'新增计价方案',exact:true}).click();await page.locator('.el-message').last().waitFor({state:'hidden'});await page.setViewportSize({width:390,height:844});await page.waitForFunction(()=>{const e=[...document.querySelectorAll('.el-dialog')].find(e=>getComputedStyle(e).display!=='none');return e&&e.getBoundingClientRect().width<=innerWidth});await page.screenshot({path:'/private/tmp/xinyi-pricing-mobile.png',animations:'disabled'});
const summary={url:page.url(),title:await page.title(),nonblank:(await page.locator('body').innerText()).includes('公司部门'),overlay:await page.locator('vite-error-overlay').count(),errors,expectedErrors,writes,viewports:['1440x1000','390x844'],mode:'明确UI API夹具；业务权限及金额由真实HTTP/MySQL回归另证',checks:['三位小数阻止提交','整数分转换','503重试复用同一请求和幂等键','保存版本试算展示分项和合计','提交审核使用原始版本','桌面与手机布局']};if(errors.length||summary.overlay||!summary.nonblank)throw Error(JSON.stringify(summary));fs.writeFileSync('/private/tmp/xinyi-pricing-qa.json',JSON.stringify(summary,null,2));console.log(JSON.stringify(summary));
}finally{await browser.close()}})().catch(e=>{console.error(e);process.exit(1)});
