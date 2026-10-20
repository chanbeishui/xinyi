from pathlib import Path
root=Path(__file__).parent
s=root/'src'
files=['icons.js','catalog.js','data.js','components.js','mobile_owner.js','mobile_driver.js','mobile_station.js','desktop.js','v4_data.js','v41_model.js','v42_model.js','v4_pages.js','v41_pages.js','v42_pages.js','v5_pages.js','app.js','v4_interactions.js','v41_interactions.js','v42_interactions.js','v5_interactions.js']
html=(s/'shell.html').read_text(encoding='utf-8')
html=html.replace('<!--STYLE-->','<style>'+ (s/'styles.css').read_text(encoding='utf-8')+'\n'+(s/'apple_v4.css').read_text(encoding='utf-8')+'\n'+(s/'v42.css').read_text(encoding='utf-8')+'</style>')
html=html.replace('<!--SCRIPTS-->','\n'.join('<script>\n'+(s/f).read_text(encoding='utf-8').replace('</script>','<\\/script>')+'\n</script>' for f in files))
(root/'index.html').write_text(html,encoding='utf-8')
print('Built',len(html),'characters')
