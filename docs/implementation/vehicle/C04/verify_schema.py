"""仅本机专属测试库：执行缺失的C04迁移，比较旧版升级/完整初装/测试库结构。
既有测试库行只备份不删除；仅清理由本次nonce创建的临时库；输出不含凭据。
依赖pymysql、PyYAML，旧版DDL固定于实施前a43be1a，避免提交后HEAD变化。
"""
from pathlib import Path
import os,re,json,secrets,subprocess,datetime
from urllib.parse import urlparse
import pymysql,yaml
root=Path(__file__).resolve().parents[4]
cfg=yaml.safe_load((root/'xinyi-admin/src/main/resources/application-druid.yml').read_text())['spring']['datasource']['druid']['master']
def resolve(v):
 m=re.fullmatch(r'\$\{([^:}]+)(?::(.*))?\}',str(v));return os.environ.get(m[1],m[2] or '') if m else v
url=urlparse(resolve(cfg['url']).removeprefix('jdbc:'))
if url.hostname not in ['localhost','127.0.0.1']:raise SystemExit('拒绝连接非本机测试服务')
conn=pymysql.connect(host=url.hostname,port=url.port or 3306,user=resolve(cfg['username']),password=resolve(cfg['password']),charset='utf8mb4',autocommit=True,client_flag=pymysql.constants.CLIENT.MULTI_STATEMENTS)
def execute(sql):
 with conn.cursor() as c:
  c.execute(sql)
  while c.nextset():pass
migration=(root/'sql/migration_20261101_02_customer_pricing.sql').read_text()
expected={'owner_dept_id','environment','created_by','config_hash','approved_hash','published_at','source_rule_id'}
backup_dir=root/'.vi-local/backup';backup_dir.mkdir(exist_ok=True)
with conn.cursor(pymysql.cursors.DictCursor) as c:
 c.execute('USE xinyi_vi_it_e07');c.execute("SELECT kind FROM test_environment_guard WHERE id=1")
 if c.fetchone()['kind']!='XINYI_VEHICLE_ISOLATED':raise SystemExit('测试保护标记无效')
 c.execute('SELECT * FROM vi_price_rule');rows=c.fetchall()
 backup=backup_dir/('c04_test_price_before_'+secrets.token_hex(5)+'.json');backup.write_text(json.dumps(rows,ensure_ascii=False,indent=2,default=str))
 c.execute("SELECT column_name FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='vi_price_rule'");existing={x['COLUMN_NAME'] if 'COLUMN_NAME'in x else x['column_name'] for x in c.fetchall()}
 added=expected&existing
 if added and added!=expected:raise SystemExit('存在部分C04列，拒绝猜测补迁移')
 before_count=len(rows)
if not added:execute(migration)
with conn.cursor() as c:
 c.execute('SELECT COUNT(*) FROM vi_price_rule');assert c.fetchone()[0]==before_count
 c.execute("SELECT COUNT(*) FROM vi_price_rule WHERE owner_dept_id IS NULL OR environment IS NULL OR config_hash IS NULL");legacy_count=c.fetchone()[0]
old_sql=subprocess.check_output(['git','show','a43be1a:sql/ry_20260417.sql'],cwd=root).decode()
new_sql=(root/'sql/ry_20260417.sql').read_text()
pattern=r'CREATE TABLE\s+`?vi_price_rule`?\s*\(.*?\)\s*ENGINE[^;]*;'
old=re.search(pattern,old_sql,re.S|re.I).group();new=re.search(pattern,new_sql,re.S|re.I).group()
created=[];nonce=secrets.token_hex(12)
try:
 for mode,ddl in [('upgrade',old),('full',new)]:
  db='xinyi_vi_it_c04_'+mode+'_'+secrets.token_hex(5)
  with conn.cursor() as c:
   c.execute('CREATE DATABASE `'+db+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci');created.append(db)
   c.execute('USE `'+db+'`');c.execute('CREATE TABLE test_c04_owner (nonce VARCHAR(48))');c.execute('INSERT INTO test_c04_owner VALUES (%s)',(nonce,))
  execute(ddl)
  if mode=='upgrade':
   execute("INSERT INTO vi_price_rule (rule_key,revision,vehicle_type_code,inspection_type_code,status,config_json) VALUES ('__C04_LEGACY__',1,'OLD','OLD','PUBLISHED','{}')")
   execute(migration)
   with conn.cursor() as c:
    c.execute("SELECT owner_dept_id,environment,config_hash,status FROM vi_price_rule WHERE rule_key='__C04_LEGACY__'");assert c.fetchone()==(None,None,None,'PUBLISHED')
 def metadata(db):
  with conn.cursor() as c:
   c.execute('SELECT column_name,column_type,is_nullable,column_default,extra,collation_name,column_comment FROM information_schema.columns WHERE table_schema=%s AND table_name=%s ORDER BY ordinal_position',(db,'vi_price_rule'));columns=c.fetchall()
   c.execute('SELECT index_name,non_unique,seq_in_index,column_name,index_type FROM information_schema.statistics WHERE table_schema=%s AND table_name=%s ORDER BY index_name,seq_in_index',(db,'vi_price_rule'));indexes=c.fetchall()
   c.execute('SELECT tc.constraint_name,cc.check_clause FROM information_schema.table_constraints tc JOIN information_schema.check_constraints cc ON cc.constraint_schema=tc.constraint_schema AND cc.constraint_name=tc.constraint_name WHERE tc.table_schema=%s AND tc.table_name=%s AND tc.constraint_type=%s ORDER BY tc.constraint_name',(db,'vi_price_rule','CHECK'));checks=c.fetchall()
   return {'columns':columns,'indexes':indexes,'checks':checks}
 a,b=map(metadata,created);assert a==b,'增量与完整初始化结构不一致'
 assert metadata('xinyi_vi_it_e07')==b,'实际测试库结构与完整初始化不一致'
 report={'kind':'C04_MYSQL_MIGRATION_AND_FULL_INIT','passed':True,'columns':len(b['columns']),'indexes':len({x[0]for x in b['indexes']}),'checks':len(b['checks']),'upgradeEqualsFull':True,'testSchemaEqualsFull':True,'legacyRowPreservedWithoutGuessing':True,'testRowsBeforeMigration':before_count,'testRowsAfterMigration':before_count,'legacyRowsReported':legacy_count,'productionOrBusinessSchemaChanged':False,'migration':'sql/migration_20261101_02_customer_pricing.sql','backup':'仅专属测试库vi_price_rule已备份至忽略目录.vi-local/backup/','at':datetime.datetime.now(datetime.timezone.utc).isoformat()}
 Path('/private/tmp/xinyi-c04-schema.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print(json.dumps(report,ensure_ascii=False))
finally:
 for db in created:
  with conn.cursor() as c:
   c.execute('SELECT nonce FROM `'+db+'`.test_c04_owner');row=c.fetchone()
   if row and row[0]==nonce and re.fullmatch(r'xinyi_vi_it_c04_(upgrade|full)_[a-f0-9]{10}',db):c.execute('DROP DATABASE `'+db+'`')
 conn.close()
