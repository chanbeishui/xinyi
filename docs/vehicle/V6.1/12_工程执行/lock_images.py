#!/usr/bin/env python3
"""Resolve actual pulled Docker images to digests; never invent a digest."""
import argparse,json,subprocess,sys,datetime
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--repo',default='.');a=p.parse_args();root=Path(a.repo).resolve();env=root/'.env.vi.local'
if not env.exists():raise SystemExit('env file missing')
lines=env.read_text().splitlines();values=dict(x.split('=',1) for x in lines if '=' in x and not x.startswith('#'));mapping={}
for key in ['VI_MYSQL_IMAGE','VI_REDIS_IMAGE','VI_PYTHON_IMAGE']:
 image=values[key];subprocess.run(['docker','pull',image],check=True)
 out=subprocess.check_output(['docker','image','inspect',image,'--format','{{json .RepoDigests}}'],text=True);digests=json.loads(out)
 if not digests:raise SystemExit('No verifiable RepoDigest: '+image)
 mapping[key]={'requested':image,'resolved':digests[0]};values[key]=digests[0]
env.write_text('\n'.join((x.split('=',1)[0]+'='+values[x.split('=',1)[0]]) if '=' in x and x.split('=',1)[0] in mapping else x for x in lines)+'\n')
out=root/'docs/implementation/vehicle/images.lock.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps({'at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'images':mapping},indent=2))
print('Image digest lock written; commit lock, never commit .env.vi.local')
