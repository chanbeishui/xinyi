"""Current document/UX catalog verification. Not production system tests."""
from pathlib import Path
from playwright.sync_api import sync_playwright
import json,datetime
B=Path(__file__).resolve().parents[1];out=B/'09_验证记录'
r={'kind':'DOCUMENT_BROWSER_CHECK','checks':[],'consoleErrors':[],'directNavigation':'NOT_TESTED','scope':'current local files; no backend or vendor','browser':'Playwright/Chromium; Browser connector read-only'}
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 pg=b.new_page(viewport={'width':1440,'height':1000});pg.on('pageerror',lambda e:r['consoleErrors'].append(str(e)))
 try:
  pg.goto((B/'00_开始.html').as_uri(),timeout=8000);r['directNavigation']='PASS'
 except Exception as e:
  r['directNavigation']='BLOCKED_ENVIRONMENT';r['navigationError']=str(e)[:500];pg.close();pg=b.new_page(viewport={'width':1440,'height':1000});pg.on('pageerror',lambda e:r['consoleErrors'].append(str(e)));pg.set_content((B/'00_开始.html').read_text())
 assert 'V6.4' in pg.title() and '第三方' in pg.locator('body').inner_text();assert len(pg.locator('body').inner_text())>200
 pg.screenshot(path=str(out/'V6.4_完整包入口.png'),full_page=True);r['checks'].append({'name':'home title and current meaningful content','result':'PASS'})
 pg.set_content((B/'12_工程执行/任务导航.html').read_text());assert pg.locator('.task').count()==85
 pg.get_by_label('搜索任务').fill('配置版本、密钥保管与发布事务');assert pg.locator('.task:visible').count()==1
 r['checks'].append({'name':'85-task catalog includes searchable X01','result':'PASS'})
 pg.set_content((B/'04_接口/契约浏览器.html').read_text());assert pg.locator('tbody tr').count()==287
 pg.locator('#q').fill('/api/admin/integrations/profiles');assert pg.locator('tbody tr:visible').count()>=5
 r['checks'].append({'name':'287-operation catalog filters integration endpoints','result':'PASS'})
 pg.set_content((B/'02_UI/全量画板总览.html').read_text());assert pg.locator('article').count()==355
 pg.get_by_label('搜索画板').fill('B46-invalid');assert pg.locator('article:visible').count()==1
 r['checks'].append({'name':'355-board gallery finds payment invalid state','result':'PASS'})
 pg.set_viewport_size({'width':390,'height':844});pg.set_content((B/'00_开始.html').read_text());assert pg.evaluate('document.documentElement.scrollWidth<=innerWidth+1')
 r['checks'].append({'name':'390px home has no horizontal overflow','result':'PASS'})
 assert not r['consoleErrors'];r['checks'].append({'name':'no document UI JavaScript errors','result':'PASS'});b.close()
r.update(passed=len(r['checks']),failed=0)
(out/'V6.4_文档浏览验证.json').write_text(json.dumps(r,ensure_ascii=False,indent=2));print(json.dumps(r,ensure_ascii=False,indent=2))
