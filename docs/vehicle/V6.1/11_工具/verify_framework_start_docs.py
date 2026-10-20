from pathlib import Path
from playwright.sync_api import sync_playwright
import json,datetime
B=Path(__file__).resolve().parents[1];out=B/'09_验证记录';out.mkdir(exist_ok=True)
rep={'kind':'DOCUMENT_BROWSER_CHECK','at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'scope':'rebaselined docs only, not business UI','browser':'Chromium/Playwright','browserSkill':'ABSENT: Browser skill/runtime not listed; regular Playwright','checks':[],'directNavigation':'NOT_TESTED','consoleErrors':[]}
with sync_playwright()as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 pg=b.new_page(viewport={'width':1440,'height':1000});pg.on('pageerror',lambda e:rep['consoleErrors'].append(str(e)))
 try:
  pg.goto((B/'00_开始.html').as_uri(),wait_until='load',timeout=8000);rep['directNavigation']='PASS'
 except Exception as exc:
  rep['directNavigation']='BLOCKED';rep['navigationError']=str(exc)[:700]
  pg.close();pg=b.new_page(viewport={'width':1440,'height':1000});pg.on('pageerror',lambda e:rep['consoleErrors'].append(str(e)))
  pg.set_content((B/'00_开始.html').read_text(),wait_until='load')
 assert 'V6.1'in pg.title()
 assert '用户未做 M0' in pg.locator('body').inner_text()
 assert pg.locator('.card').count()==7
 pg.screenshot(path=str(out/'V6.1_框架初装入口.png'),full_page=True)
 rep['checks'].append({'name':'home identity, nonblank, current starting state, desktop screenshot','result':'PASS','url':pg.url,'viewport':[1440,1000]})
 pg.close();pg=b.new_page(viewport={'width':1440,'height':1000});pg.on('pageerror',lambda e:rep['consoleErrors'].append(str(e)))
 pg.set_content((B/'12_工程执行/任务导航.html').read_text(),wait_until='load')
 if pg.locator('.task').count()!=78:print(rep,pg.locator('body').inner_text()[:500])
 assert pg.locator('.task').count()==78
 pg.get_by_label('搜索任务').fill('框架基线')
 assert pg.locator('.task').count()==1 and 'E00'in pg.locator('.task').inner_text()
 pg.screenshot(path=str(out/'V6.1_首任务E00.png'),full_page=False)
 rep['checks'].append({'name':'78 task catalog filters to framework E00 start','result':'PASS','transport':'set_content'})
 pg.get_by_label('搜索任务').fill('新建业务Maven')
 assert pg.locator('.task').count()==1 and 'E04'in pg.locator('.task').inner_text()
 rep['checks'].append({'name':'E04 explicitly creates business module and miniapp','result':'PASS'})
 pg.set_viewport_size({'width':390,'height':844});pg.set_content((B/'00_开始.html').read_text(),wait_until='load')
 assert pg.evaluate('document.documentElement.scrollWidth<=window.innerWidth+1')
 pg.screenshot(path=str(out/'V6.1_入口390.png'),full_page=True)
 rep['checks'].append({'name':'mobile390 document no horizontal overflow','result':'PASS'})
 assert not rep['consoleErrors']
 rep['checks'].append({'name':'no document page errors','result':'PASS'})
 b.close()
rep['passed']=len(rep['checks']);rep['failed']=0
(out/'V6.1_文档浏览验证.json').write_text(json.dumps(rep,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(rep,ensure_ascii=False,indent=2))
