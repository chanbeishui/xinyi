"""Rebuild current Markdown documents as portable HTML. Does not rebuild diagrams/UI screenshots."""
from pathlib import Path
import os,html,mistune
root=Path(__file__).resolve().parents[1]
md=mistune.create_markdown(escape=False,plugins=['table','strikethrough','url'])
css='body{font:16px/1.75 -apple-system,BlinkMacSystemFont,"Microsoft YaHei",sans-serif;color:#202632;margin:0;background:#f6f7f9}header{padding:14px 24px;background:white;border-bottom:1px solid #ddd}main{max-width:1120px;margin:24px auto;padding:30px;background:white}h1{font-size:30px}h2{margin-top:36px}a{color:#0661bd}table{border-collapse:collapse;width:100%;display:block;overflow:auto;font-size:14px}td,th{border:1px solid #dde2ea;padding:10px;vertical-align:top}th{background:#f1f5fb}pre{padding:15px;background:#f2f4f7;overflow:auto}img{max-width:100%;height:auto}blockquote{padding:10px 20px;border-left:3px solid #2571be;background:#f4f8fc}@media(max-width:700px){main{padding:15px;margin:8px}h1{font-size:25px}}'
for p in root.rglob('*.md'):
 if '历史' in str(p.relative_to(root)):continue
 home=os.path.relpath(root/'00_开始.html',p.parent);proto=os.path.relpath(root/'02_UI/index.html',p.parent)
 text=f'<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{html.escape(p.stem)}</title><style>{css}</style></head><body><header><a href="{home}">V6.1 完整资料</a>　<a href="{proto}">交互原型</a>　T3.1</header><main>{md(p.read_text())}</main></body></html>'
 p.with_suffix('.html').write_text(text)
print('Markdown HTML rebuilt; run check_package.py to validate references.')
