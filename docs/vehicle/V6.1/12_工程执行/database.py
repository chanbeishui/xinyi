#!/usr/bin/env python3
"""Read-only schema inventory or explicitly initialize a NEW local disposable database.
Never upgrades or resets an existing database. Root/production remote connections rejected.
"""
from __future__ import annotations
import argparse,datetime as dt,hashlib,json,os,re,sys
from pathlib import Path
B=Path(__file__).resolve().parents[1]

def split_sql(source:str)->list[str]:
    statements=[];buf=[];quote=None;line_comment=False;block=False;i=0
    while i<len(source):
        c=source[i];n=source[i+1:i+2]
        if line_comment:
            if c=='\n':line_comment=False;buf.append('\n')
        elif block:
            if c=='*' and n=='/':block=False;i+=1
        elif quote:
            buf.append(c)
            if c=='\\' and quote!='`' and n:buf.append(n);i+=1
            elif c==quote:
                if n==quote:buf.append(n);i+=1
                else:quote=None
        elif c in "'\"`":quote=c;buf.append(c)
        elif c=='#' or (c=='-' and n=='-' and (i+2==len(source) or source[i+2].isspace())):line_comment=True
        elif c=='/' and n=='*':block=True;i+=1
        elif c==';':
            if ''.join(buf).strip():statements.append(''.join(buf).strip())
            buf=[]
        else:buf.append(c)
        i+=1
    if quote or block:raise ValueError('未闭合的SQL引号/注释；拒绝执行')
    if ''.join(buf).strip():statements.append(''.join(buf).strip())
    return statements

def read_env(repo):
    values={}
    for line in (repo/'.env.vi.local').read_text(encoding='utf8').splitlines():
        if line and not line.startswith('#') and '=' in line:k,v=line.split('=',1);values[k]=v
    return values

def connect(env,probe=False):
    import pymysql
    host=env.get('VI_DB_HOST','127.0.0.1');name=env.get('VI_DB_NAME','')
    if env.get('VI_ENV') not in {'local','test'} or host not in {'127.0.0.1','localhost','::1'} or not re.fullmatch(r'xinyi_vi_(dev|it_[a-z0-9_]+)',name):
        raise ValueError('只允许显式local/test和loopback xinyi_vi_dev/xinyi_vi_it_*；现库探测请用只读SQL清单另行执行')
    return pymysql.connect(host=host,port=int(env.get('VI_DB_PORT','33306')),user=env['VI_DB_USER'],password=env['VI_DB_PASSWORD'],database=name,charset='utf8mb4',autocommit=True,cursorclass=pymysql.cursors.DictCursor,connect_timeout=5)

def inspect_db(c):
    with c.cursor() as cur:
        cur.execute("SELECT VERSION() AS server_version,DATABASE() AS database_name,@@session.time_zone AS session_time_zone,@@system_time_zone AS system_time_zone")
        out={'observedAt':dt.datetime.now(dt.timezone.utc).isoformat(),**cur.fetchone(),'tables':[]}
        cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY table_name")
        names=[x['TABLE_NAME'] if 'TABLE_NAME' in x else x['table_name'] for x in cur.fetchall()]
        for name in names:
            if not name.startswith('vi_'):continue
            cur.execute("SELECT COLUMN_NAME, COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT,EXTRA,GENERATION_EXPRESSION,COLLATION_NAME FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=%s ORDER BY ordinal_position",(name,));cols=cur.fetchall()
            cur.execute("SELECT INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name=%s ORDER BY index_name,seq_in_index",(name,));idx=cur.fetchall()
            # Only count, never dump business or credential rows.
            if not re.fullmatch(r'vi_[a-z0-9_]+',name):raise ValueError('非法标识符')
            cur.execute('SELECT COUNT(*) AS n FROM `'+name+'`');count=cur.fetchone()['n']
            cur.execute("SELECT tc.CONSTRAINT_NAME,cc.CHECK_CLAUSE FROM information_schema.table_constraints tc JOIN information_schema.check_constraints cc ON cc.constraint_schema=tc.constraint_schema AND cc.constraint_name=tc.constraint_name WHERE tc.table_schema=DATABASE() AND tc.table_name=%s AND tc.constraint_type='CHECK'",(name,));checks=cur.fetchall()
            out['tables'].append({'name':name,'columns':cols,'indexes':idx,'checks':checks,'rowCount':count})
        current={t['name'] for t in out['tables']};target={t['name'] for t in json.loads((B/'05_数据设计/data_model_V6.0.json').read_text())}
        out['classification']='NO_VI_TABLES' if not current else 'V6_NAMES_NEEDS_STRUCTURE_CHECK' if current==target else 'PARTIAL_VI_SCHEMA_NEEDS_REVIEW' if current<target else 'OTHER_DO_NOT_WRITE'
        out['missingTargetTables']=sorted(target-current);out['unexpectedViTables']=sorted(current-target)
        return out

def selected_sys_seed(text):
    keep=[]
    for s in split_sql(text):
        if re.match(r'(?is)^CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?sys_[a-z0-9_]+`?\s*\(',s) or re.match(r'(?is)^INSERT\s+INTO\s+`?sys_[a-z0-9_]+`?(?:\s|\()',s):keep.append(s)
        # Only create+base inserts in a proven-empty isolated DB; DROP/ALTER/USE/vi statements never forwarded.
    if not any(re.match(r'(?is)^CREATE\s+TABLE\s+`?sys_user`?\s*\(',s) for s in keep):raise ValueError('未找到明确sys_user定义；请核对框架完整初始化脚本')
    return keep

def bootstrap(c,repo,env,confirm):
    if confirm!=env['VI_DB_NAME']:raise ValueError('必须用--confirm-db明确重复目标库名')
    import bcrypt
    with c.cursor() as cur:
        cur.execute('SHOW TABLES');existing=cur.fetchall()
        if existing:raise ValueError('非空数据库：拒绝初装；运行inventory并按当前框架初装文档的异常同名表分支处理，不删除已有表')
        framework=repo/'sql/ry_20260417.sql'
        if not framework.exists():raise ValueError('缺框架完整初始化SQL')
        base=selected_sys_seed(framework.read_text(encoding='utf8'))
        ddl=next((B/'05_数据设计').glob('*001*.sql'),None)
        if ddl is None:
            ddl=next((B/'05_数据设计').glob('*建表*.sql'),None)
        if ddl is None:raise ValueError('找不到001业务建表文件')
        vi=[]
        for s in split_sql(ddl.read_text(encoding='utf8')):
            if re.match(r'(?is)^CREATE\s+TABLE\s+`?vi_[a-z0-9_]+`?\s*\(',s):vi.append(s)
            elif s.strip():
                if not re.match(r'(?is)^SET\s',s):raise ValueError('001含非预期语句，拒绝隐式执行：'+s[:60])
        target=json.loads((B/'05_数据设计/data_model_V6.0.json').read_text())
        if len(vi)!=len(target):raise ValueError('建表数量不符')
        cur.execute("SET time_zone='+00:00'")
        for sql in base+vi:cur.execute(sql)
        # Newly imported framework defaults must not retain a known sample password.
        password_hash=bcrypt.hashpw(env['VI_ADMIN_TEST_PASSWORD'].encode(),bcrypt.gensalt()).decode()
        cur.execute("UPDATE sys_user SET password=%s, status=CASE WHEN user_name='admin' THEN '0' ELSE '1' END",(password_hash,))
        cur.execute("CREATE TABLE test_environment_guard (id INT PRIMARY KEY,kind VARCHAR(64) NOT NULL, key_hash CHAR(64) NOT NULL, created_at DATETIME(3) NOT NULL)")
        cur.execute("INSERT INTO test_environment_guard VALUES(1,'XINYI_VEHICLE_ISOLATED',%s,UTC_TIMESTAMP(3))",(hashlib.sha256(env['VI_TEST_CONTROL_KEY'].encode()).hexdigest(),))
    return {'frameworkSource':str(framework.relative_to(repo)),'frameworkSha256':hashlib.sha256(framework.read_bytes()).hexdigest(),'viDdlSha256':hashlib.sha256(ddl.read_bytes()).hexdigest(),'viTablesCreated':len(vi),'note':'仅建表与框架基础初始化；业务fixture由测试classpath受控服务创建。非可重复执行；中途失败保留现场。'}

def main():
    ap=argparse.ArgumentParser();ap.add_argument('command',choices=['inventory','bootstrap']);ap.add_argument('--repo',default='.');ap.add_argument('--confirm-db');a=ap.parse_args();repo=Path(a.repo).resolve();env=read_env(repo);c=connect(env)
    try:
        data=inspect_db(c) if a.command=='inventory' else bootstrap(c,repo,env,a.confirm_db)
        p=repo/'docs/implementation/vehicle/evidence'/('database-'+a.command+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2,default=str),encoding='utf8');print('已写入',p)
    finally:c.close()
if __name__=='__main__':
    try:main()
    except Exception as e:print('BLOCKED_DATABASE:',type(e).__name__,str(e),file=sys.stderr);sys.exit(2)
