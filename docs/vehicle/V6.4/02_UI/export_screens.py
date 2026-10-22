import os,shutil
from playwright.sync_api import sync_playwright
from pathlib import Path
from collections import Counter
import re,json,time,argparse
args=argparse.ArgumentParser();args.add_argument("--start",type=int,default=0);args.add_argument("--stop",type=int);args=args.parse_args()
root=Path(__file__).parent
out=root/'全量页面图';(out/'首屏').mkdir(parents=True,exist_ok=True);(out/'完整内容').mkdir(parents=True,exist_ok=True)
app='\n'.join((root/'src'/f).read_text(encoding='utf-8') for f in ['app.js','v4_interactions.js','v41_interactions.js','v42_interactions.js','v5_interactions.js','identity_interactions.js','integration_interactions.js'])
known=set(re.findall(r"case '([^']+)'",app)) | set(re.findall(r"action==='([^']+)'",app))
results=[];errs=[];missing_go=set();missing_actions=set();dup_ids=[];overflows=[]
with sync_playwright() as p:
 b=p.chromium.launch(executable_path=os.environ.get('CHROMIUM_PATH') or shutil.which('chromium') or shutil.which('google-chrome'),headless=True,args=['--no-sandbox','--disable-dev-shm-usage'])
 page=b.new_page(viewport={'width':1440,'height':1100},device_scale_factor=1)
 page.on('pageerror',lambda e:errs.append(str(e)))
 page.set_content((root/'index.html').read_text(encoding='utf-8'),wait_until='load')
 cat=page.evaluate('UI.testing.catalog');valid={x['id'] for x in cat}|set(page.evaluate('UI_ALIASES'))
 for i,item in enumerate(cat[args.start:args.stop],start=args.start):
  id=item['id'];mobile=id[0]!='B';page.set_viewport_size({'width':390 if mobile else 1440,'height':844 if mobile else 1050})
  page.evaluate("UI.store=UI.makeStore();document.body.className='export';window.scrollTo(0,0)")
  before=len(errs)
  page.evaluate('(id)=>UI.testing.render(id)',id);page.wait_for_timeout(15)
  page.evaluate("document.getElementById('toast-root').innerHTML=''")
  target=page.locator('[data-screen]')
  data=target.evaluate('''el=>({id:el.dataset.screen,text:el.innerText.length,width:el.getBoundingClientRect().width,height:el.getBoundingClientRect().height,bodyOverflow:el.querySelector('.mobile-body')?el.querySelector('.mobile-body').scrollWidth-el.querySelector('.mobile-body').clientWidth:0,go:[...el.querySelectorAll('[data-go]')].map(x=>x.dataset.go),actions:[...el.querySelectorAll('[data-action]')].filter(x=>x.dataset.action).map(x=>x.dataset.action),ids:[...el.querySelectorAll('[id]')].map(x=>x.id)})''')
  missing_go|={x for x in data['go'] if x not in valid};missing_actions|={x for x in data['actions'] if x not in known}
  duplicates=[x for x,c in Counter(data['ids']).items() if c>1]
  if duplicates:dup_ids.append({'id':id,'duplicates':duplicates})
  if data['bodyOverflow']>2:overflows.append({'id':id,'px':data['bodyOverflow']})
  h=844 if mobile else min(int(data['height']),1050)
  page.screenshot(path=str(out/'首屏'/f'{id}.png'),clip={'x':0,'y':0,'width':390 if mobile else 1440,'height':h},animations='disabled')
  page.evaluate("document.body.classList.add('full');let product=document.querySelector('.pc-product');if(product){product.style.height='';product.style.minHeight='';}window.scrollTo(0,0)")
  # Expand long drawers for complete-content export only, preserving normal scrollable interaction.
  if not mobile:
   page.evaluate("""(()=>{let d=document.querySelector('.pc-drawer');if(d){let inner=d.querySelector('.drawer-body'),p=document.querySelector('.pc-product');let h=inner.scrollHeight+d.querySelector('.drawer-head').offsetHeight+d.querySelector('.drawer-footer').offsetHeight+12;p.style.minHeight=Math.max(980,h)+'px';}})()""")
  target.screenshot(path=str(out/'完整内容'/f'{id}.png'),animations='disabled')
  results.append({'id':id,'kind':item['kind'],'base':item['base'],'passed':len(errs)==before and data['text']>25,'textLength':data['text'],'mobileHorizontalOverflow':data['bodyOverflow'],'firstImage':'全量页面图/首屏/'+id+'.png','fullImage':'全量页面图/完整内容/'+id+'.png'})
  if i%25==0:print(f'{i+1}/{len(cat)} {id}',flush=True)
 # Mobile preview sizes in the normal review shell.
 responsive=[]
 for w in [375,390,428]:
  page.set_viewport_size({'width':w,'height':900});page.evaluate("document.body.className='';UI.store=UI.makeStore();window.scrollTo(0,0)")
  page.evaluate("UI.testing.render('O03')")
  metrics=page.evaluate("({viewport:innerWidth,document:document.documentElement.scrollWidth,device:document.querySelector('.device').getBoundingClientRect().width})")
  page.screenshot(path=str(root.parent/'09_验证记录'/f'mobile_{w}.png'),full_page=False)
  responsive.append({'width':w,**metrics,'passed':metrics['document']<=w+1})
 report={'environment':'Chromium with Playwright; V6.4 self-contained HTML loaded via set_content; product routes driven by same original JS renderer','browserPlugin':'Browser connector exists but browser runtime skill is not listed; local Playwright used per validation skill','screenCount':len(results),'catalogCount':len(cat),'range':[args.start,args.stop],'screens':results,'pageErrors':errs,'unresolvedRoutes':sorted(missing_go),'unhandledActions':sorted(missing_actions),'duplicateControlIds':dup_ids,'horizontalOverflow':overflows,'responsive':responsive,'allScreenshotsRegenerated':True,'scope':'All catalog views rendered; not a test of every interaction or production service'}
 (root.parent/('09_验证记录/全量画板导出与核验.json' if args.start==0 and args.stop is None else f'09_验证记录/画板核验_{args.start}_{args.stop}.json')).write_text(json.dumps(report,ensure_ascii=False,indent=2))
 print('DONE',len(results),'errors',errs,'bad_routes',missing_go,'bad_actions',missing_actions,'dups',dup_ids,'overflow',overflows,'responsive',responsive,flush=True)
 b.close()
