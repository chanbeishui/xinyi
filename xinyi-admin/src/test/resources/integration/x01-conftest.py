"""X01 bounded fixture adapter: real JUnit server, DB and issued tokens; original Client validates main OpenAPI."""
import os,json
from pathlib import Path
import pytest,pymysql
source=Path(os.environ['X01_SOURCE'])/'conftest.py'
ns={'__file__':str(source),'__name__':'x01_original_client'}
exec(compile(source.read_text().split('def safe_url')[0],str(source),'exec'),ns)
Client=ns['Client']
@pytest.fixture(scope='session')
def base():return os.environ['X01_BASE']
class FixtureRun(dict):
    def __repr__(self):return "<redacted integration fixture>"
@pytest.fixture
def run():return FixtureRun(json.loads(os.environ['X01_RUN']))
@pytest.fixture
def login(base):
    def issued():return Client(base,os.environ['X01_MINI'],'owner'),{}
    return issued
@pytest.fixture
def db():
    conn=pymysql.connect(host='127.0.0.1',port=3306,user=os.environ['X01_DB_USER'],password=os.environ['X01_DB_PASSWORD'],database='xinyi_vi_it_e07',autocommit=True,cursorclass=pymysql.cursors.DictCursor)
    with conn.cursor() as c:
        c.execute('SELECT kind FROM test_environment_guard WHERE id=1')
        assert c.fetchone()['kind']=='XINYI_VEHICLE_ISOLATED'
    yield conn
    conn.close()
