"""Actual MySQL schema and transaction probes; never substitute H2/SQLite."""
import hashlib,json,os,re,uuid
from pathlib import Path
import pytest
B=Path(__file__).resolve().parents[2]
MODEL=json.loads((B/'05_数据设计/data_model_V6.0.json').read_text(encoding='utf8'))
@pytest.fixture(scope='module')
def db():
 import pymysql
 assert os.getenv('VI_ENV') in {'local','test'}
 name=os.getenv('VI_DB_NAME','');assert re.fullmatch(r'xinyi_vi_(dev|it_[a-z0-9_]+)',name)
 assert os.getenv('VI_DB_HOST','127.0.0.1') in {'127.0.0.1','localhost','::1'}
 c=pymysql.connect(host=os.getenv('VI_DB_HOST','127.0.0.1'),port=int(os.getenv('VI_DB_PORT','33306')),user=os.environ['VI_DB_USER'],password=os.environ['VI_DB_PASSWORD'],database=name,autocommit=False,cursorclass=pymysql.cursors.DictCursor)
 with c.cursor() as cur:
  cur.execute('SELECT kind,key_hash FROM test_environment_guard WHERE id=1');x=cur.fetchone();assert x['kind']=='XINYI_VEHICLE_ISOLATED' and x['key_hash']==hashlib.sha256(os.environ['VI_TEST_CONTROL_KEY'].encode()).hexdigest()
 yield c;c.rollback();c.close()

def test_table_and_primary_key_manifest(db):
 with db.cursor() as cur:
  for t in MODEL:
   cur.execute("SELECT COLUMN_NAME FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name=%s AND index_name='PRIMARY' ORDER BY seq_in_index",(t['name'],));assert [r['COLUMN_NAME'] for r in cur.fetchall()]==t['primaryKey'],t['name']

def test_generated_columns_are_computed(db):
 with db.cursor() as cur:
  for t in MODEL:
   for n,rule in t.get('columnRules',{}).items():
    if not rule.get('generated'):continue
    cur.execute('SELECT GENERATION_EXPRESSION,EXTRA FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=%s AND column_name=%s',(t['name'],n));r=cur.fetchone();assert r and r['GENERATION_EXPRESSION'],(t['name'],n)
    assert not rule['insertable'] and not rule['updatable'] and not rule['clientWritable']

def test_single_driver_occupancy_constraint(db):
 import pymysql
 with db.cursor() as cur:
  key=8500000000000000+int(uuid.uuid4().hex[:8],16)
  try:
   cur.execute("INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES(%s,'INVITE',%s,%s)",(key,key,key))
   with pytest.raises(pymysql.err.IntegrityError):cur.execute("INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES(%s,'ASSIGNMENT',%s,%s)",(key,key+1,key+1))
  finally:db.rollback()

def test_old_assignment_cannot_release_new_occupancy(db):
 with db.cursor() as cur:
  key=8500000000000000+int(uuid.uuid4().hex[:8],16)
  try:
   cur.execute("INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES(%s,'ASSIGNMENT',%s,%s)",(key,key,key))
   cur.execute("DELETE FROM vi_driver_occupancy WHERE driver_id=%s AND holder_type='ASSIGNMENT' AND holder_id=%s",(key,key-1));assert cur.rowcount==0
   cur.execute('SELECT COUNT(*) AS n FROM vi_driver_occupancy WHERE driver_id=%s',(key,));assert cur.fetchone()['n']==1
  finally:db.rollback()

def test_explicit_cas_row_count(db):
 with db.cursor() as cur:
  ident='sim-cas-'+uuid.uuid4().hex
  try:
   cur.execute("INSERT INTO vi_account(app_id,open_id,status) VALUES('SIM_TEST',%s,'ACTIVE')",(ident,));aid=cur.lastrowid
   cur.execute("UPDATE vi_account SET nickname='first',version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE id=%s AND version=0",(aid,));assert cur.rowcount==1
   cur.execute("UPDATE vi_account SET nickname='stale',version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE id=%s AND version=0",(aid,));assert cur.rowcount==0
  finally:db.rollback()

def test_utc_default_independent_of_connection_display_zone(db):
 with db.cursor() as cur:
  try:
   cur.execute("SET time_zone='+08:00'")
   cur.execute("INSERT INTO vi_account(app_id,open_id,status) VALUES('SIM_TEST',%s,'ACTIVE')",('sim-tz-'+uuid.uuid4().hex,));aid=cur.lastrowid
   cur.execute('SELECT ABS(TIMESTAMPDIFF(SECOND,created_at,UTC_TIMESTAMP(3))) AS delta FROM vi_account WHERE id=%s',(aid,));assert cur.fetchone()['delta']<5
  finally:db.rollback();cur.execute("SET time_zone='+00:00'")
