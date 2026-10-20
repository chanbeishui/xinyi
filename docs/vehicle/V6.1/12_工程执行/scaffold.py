#!/usr/bin/env python3
"""Copy only missing engineering templates; refuse unknown conflicts. No dependency installation or Git writes."""
from pathlib import Path
import argparse,json,re,sys
B=Path(__file__).resolve().parents[1]
def plan(repo):
 if not(repo/'pom.xml').exists()or not(repo/'xinyi-admin/pom.xml').exists():raise ValueError('需xinyi仓库根目录')
 modules=[n for n in ['xinyi-rongli','xinyi-vehicle'] if (repo/n/'pom.xml').exists()]
 if len(modules)>1:raise ValueError('存在两套机动车模块，先E00核对职责，不自动合并')
 if modules==['xinyi-vehicle']:raise ValueError('检测到已有xinyi-vehicle。复用该模块并按E00映射模板包名；禁止另造rongli。先人工/Agent审查差异，不自动覆写。')
 files=[];conflicts=[]
 for source_dir,dest_dir in [('backend','xinyi-rongli'),('miniapp','xinyi-miniapp')]:
  for src in (B/'14_工程模板'/source_dir).rglob('*'):
   if not src.is_file():continue
   dst=repo/dest_dir/src.relative_to(B/'14_工程模板'/source_dir)
   if dst.exists():
    # Existing project manifests are authoritative; preserve them for dependency-aware merging.
    if dst.name in ['pom.xml','package.json','tsconfig.json','vite.config.ts','manifest.json','pages.json','types.ts','App.vue','main.ts','index.vue','http.ts','env.d.ts']:continue
    if dst.read_bytes()!=src.read_bytes():conflicts.append(str(dst.relative_to(repo)))
   else:files.append((dst,src.read_text(encoding='utf8')))
 if conflicts:raise ValueError('已有同名文件内容不同，不覆盖：'+','.join(conflicts))
 root=repo/'pom.xml';txt=root.read_text(encoding='utf8')
 if '<module>xinyi-rongli</module>' not in txt:
  if txt.count('</modules>')!=1:raise ValueError('根POM模块声明非单一，按E00手工最小合并')
  files.append((root,txt.replace('</modules>','    <module>xinyi-rongli</module>\n    </modules>')))
 admin=repo/'xinyi-admin/pom.xml';txt=admin.read_text(encoding='utf8')
 if '<artifactId>xinyi-rongli</artifactId>' not in txt:
  if txt.count('</dependencies>')!=1:raise ValueError('启动模块POM依赖区非单一，按E00手工最小合并')
  insert='\n        <dependency><groupId>com.xinyi</groupId><artifactId>xinyi-rongli</artifactId><version>${project.version}</version></dependency>\n    '
  files.append((admin,txt.replace('</dependencies>',insert+'</dependencies>')))
 # Only test-classpath control/fixture API; must be implemented with actual services by E07.
 for src in (B/'14_工程模板/integration-java/src/test').rglob('*.java'):
  dst=repo/'xinyi-admin/src/test'/src.relative_to(B/'14_工程模板/integration-java/src/test')
  if dst.exists() and dst.read_bytes()!=src.read_bytes():raise ValueError('已有测试源码不同，拒绝覆盖：'+str(dst.relative_to(repo)))
  if not dst.exists():files.append((dst,src.read_text(encoding='utf8')))
 current=next((content for target,content in reversed(files) if target==admin),admin.read_text(encoding='utf8'))
 if '<id>vi-it</id>' not in current:
  profile=(B/'14_工程模板/integration-java/profile.xml').read_text(encoding='utf8')
  if current.count('</profiles>')>1:raise ValueError('多个profiles，请按E07最小合并')
  current=current.replace('</profiles>',profile+'\n</profiles>') if '</profiles>' in current else current.replace('</project>','<profiles>'+profile+'</profiles>\n</project>')
  files=[(target,content)for target,content in files if target!=admin];files.append((admin,current))
 env=repo/'xinyi-miniapp/.env.local'
 if not env.exists():files.append((env,'VITE_API_BASE=http://127.0.0.1:18080\nVITE_APP_REALM=local\n'))
 return files

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--repo',default='.');p.add_argument('--apply',action='store_true');a=p.parse_args();repo=Path(a.repo).resolve();files=plan(repo)
 print(json.dumps({'apply':a.apply,'writeFiles':[str(p.relative_to(repo)) for p,s in files],'note':'现有manifest/pom依赖不替换；新增公共安全链需按I01/06实现，不在此猜测配置。'},ensure_ascii=False,indent=2))
 if a.apply:
  for p,s in files:p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf8')
if __name__=='__main__':
 try:main()
 except (ValueError,OSError) as e:print('BLOCKED_SCAFFOLD:',e,file=sys.stderr);sys.exit(2)
