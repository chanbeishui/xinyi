#!/usr/bin/env python3
"""隔离环境的标准化第三方测试网关。不是微信支付API仿冒实现，不提供业务订单CRUD。
Controller/Service/Mapper/权限/幂等必须仍走真实xinyi。SQLite仅存本测试网关状态，不能用于业务MySQL验收。
"""
from __future__ import annotations
import argparse,hashlib,hmac,json,os,secrets,sqlite3,time
from http.server import BaseHTTPRequestHandler,ThreadingHTTPServer
from pathlib import Path
MAX_BODY=1_048_576
class Conflict(Exception):pass
class Invalid(Exception):pass
class Store:
 def __init__(self,path,secret):
  self.path=str(path);self.secret=secret
  Path(path).parent.mkdir(parents=True,exist_ok=True)
  with self.db() as c:
   c.executescript('''CREATE TABLE IF NOT EXISTS runs(id TEXT PRIMARY KEY, created REAL);
   CREATE TABLE IF NOT EXISTS codes(hash TEXT PRIMARY KEY, run TEXT, persona TEXT, kind TEXT, expires REAL, consumed INTEGER DEFAULT 0);
   CREATE TABLE IF NOT EXISTS payments(run TEXT,no TEXT,amount INTEGER,merchant TEXT,status TEXT,PRIMARY KEY(run,no));
   CREATE TABLE IF NOT EXISTS refunds(run TEXT,no TEXT,payment_no TEXT,amount INTEGER,status TEXT,PRIMARY KEY(run,no));
   CREATE TABLE IF NOT EXISTS checks(run TEXT,id TEXT,kind TEXT,status TEXT,PRIMARY KEY(run,id));''')
 def db(self):
  c=sqlite3.connect(self.path,timeout=10,isolation_level='IMMEDIATE');c.row_factory=sqlite3.Row;return c
 def run(self,c,b):
  r=b.get('runId','')
  if not isinstance(r,str) or not c.execute('SELECT id FROM runs WHERE id=?',(r,)).fetchone():raise Invalid('unknown runId')
  return r
 @staticmethod
 def amount(b):
  n=b.get('amount')
  if type(n)!=int or not 0<n<=100_000_000:raise Invalid('amount must be positive integer fen within test bound')
  return n
 @staticmethod
 def text(b,k):
  v=b.get(k)
  if not isinstance(v,str) or not v or len(v)>128:raise Invalid('invalid '+k)
  return v
 def call(self,path,b):
  with self.db() as c:
   c.execute('BEGIN IMMEDIATE')
   if path=='/control/runs':
    r='it_'+secrets.token_hex(10);c.execute('INSERT INTO runs VALUES(?,?)',(r,time.time()));return {'runId':r,'mode':'SIMULATOR'}
   if path in ['/wechat/exchange','/phone/exchange']:
    kind='wechat' if path.startswith('/wechat') else 'phone';h=hashlib.sha256(self.text(b,'code').encode()).hexdigest();row=c.execute('SELECT * FROM codes WHERE hash=?',(h,)).fetchone()
    if row is None or row['kind']!=kind or row['expires']<=time.time() or row['consumed']:raise Invalid('code invalid, expired or consumed')
    if b.get('runId') is not None and b['runId']!=row['run']:raise Invalid('code run mismatch')
    run=row['run']
    c.execute('UPDATE codes SET consumed=1 WHERE hash=?',(h,))
    result={'mode':'SIMULATOR','providerRunId':run,'persona':row['persona'],'providerEventId':'sim_code_'+h[:12]}
    if kind=='wechat':result.update(appId='SIM_APP',openId='sim_'+run+'_'+row['persona'])
    else:result['verifiedPhone']='199'+str(int(hashlib.sha256(row['persona'].encode()).hexdigest()[:10],16)%100000000).zfill(8)
    return result
   run=self.run(c,b)
   if path=='/control/codes':
    persona=self.text(b,'persona');kind=b.get('kind','wechat')
    if kind not in ['wechat','phone']:raise Invalid('unknown code kind')
    code=secrets.token_urlsafe(32);h=hashlib.sha256(code.encode()).hexdigest();c.execute('INSERT INTO codes(hash,run,persona,kind,expires)VALUES(?,?,?,?,?)',(h,run,persona,kind,time.time()+120))
    return {'code':code,'expiresIn':120,'testOnly':True}
   if path=='/routes/query':
    o=self.text(b,'originKey');d=self.text(b,'destinationKey')
    known={('PICKUP_A','STATION_A'):6000,('STATION_A','PICKUP_A'):7000,('PICKUP_A','STATION_B'):12000,('STATION_B','PICKUP_A'):13000}
    if (o,d) not in known:raise Invalid('route fixture missing; never return fake zero distance')
    return {'distanceMeter':known[o,d],'durationSecond':1200,'basis':'SIMULATOR_FIXTURE','routeId':o+'_'+d}
   if path=='/payments/prepay':
    no=self.text(b,'merchantOrderNo');amount=self.amount(b);merchant=self.text(b,'merchantAlias');row=c.execute('SELECT * FROM payments WHERE run=? AND no=?',(run,no)).fetchone()
    if row:
     if row['amount']!=amount or row['merchant']!=merchant:raise Conflict('same payment reference different input')
    else:c.execute('INSERT INTO payments VALUES(?,?,?,?,?)',(run,no,amount,merchant,'PENDING'))
    return {'paymentReference':no,'status':row['status'] if row else 'PENDING','mode':'SIMULATOR','launch':{'type':'SIMULATOR_ONLY','reference':no}}
   if path in ['/payments/query','/payments/close','/control/payments/paid']:
    no=self.text(b,'merchantOrderNo');row=c.execute('SELECT * FROM payments WHERE run=? AND no=?',(run,no)).fetchone()
    if not row:raise Invalid('unknown payment')
    status=row['status']
    if path.endswith('/close'):
     if status=='PAID':raise Conflict('already paid')
     status='CLOSED';c.execute('UPDATE payments SET status=? WHERE run=? AND no=?',(status,run,no))
    if path.endswith('/paid'):
     if status=='CLOSED':raise Conflict('closed payment cannot be paid')
     status='PAID';c.execute('UPDATE payments SET status=? WHERE run=? AND no=?',(status,run,no))
    data={'runId':run,'merchantOrderNo':no,'merchantAlias':row['merchant'],'amount':row['amount'],'currency':'CNY','status':status,'eventId':'sim_pay_'+run+'_'+no+'_'+status,'mode':'SIMULATOR'}
    if path.startswith('/control/'):
     raw=json.dumps(data,separators=(',',':'),sort_keys=True);data={'rawBody':raw,'signature':hmac.new(self.secret.encode(),raw.encode(),hashlib.sha256).hexdigest(),'algorithm':'TEST_HMAC_SHA256'}
    return data
   if path=='/refunds/create':
    no=self.text(b,'refundNo');payment=self.text(b,'merchantOrderNo');amount=self.amount(b)
    row=c.execute('SELECT * FROM refunds WHERE run=? AND no=?',(run,no)).fetchone()
    if row:
     if row['payment_no']!=payment or row['amount']!=amount:raise Conflict('same refund reference different input')
     return dict(row)
    pay=c.execute('SELECT * FROM payments WHERE run=? AND no=?',(run,payment)).fetchone()
    if not pay or pay['status']!='PAID':raise Conflict('original payment not paid')
    used=c.execute("SELECT coalesce(sum(amount),0) FROM refunds WHERE run=? AND payment_no=? AND status!='FAILED'",(run,payment)).fetchone()[0]
    if used+amount>pay['amount']:raise Conflict('refund exceeds original amount')
    c.execute('INSERT INTO refunds VALUES(?,?,?,?,?)',(run,no,payment,amount,'PROCESSING'));return {'refundNo':no,'status':'PROCESSING','mode':'SIMULATOR'}
   if path in ['/refunds/query','/control/refunds/result']:
    no=self.text(b,'refundNo');row=c.execute('SELECT * FROM refunds WHERE run=? AND no=?',(run,no)).fetchone()
    if not row:raise Invalid('unknown refund')
    result=dict(row)
    if path.startswith('/control/'):
     status=b.get('status')
     if status not in ['SUCCESS','FAILED','UNKNOWN']:raise Invalid('invalid refund state')
     if row['status']=='SUCCESS' and status!='SUCCESS':raise Conflict('successful refund cannot rewind')
     c.execute('UPDATE refunds SET status=? WHERE run=? AND no=?',(status,run,no));result['status']=status
    result['mode']='SIMULATOR';return result
   if path=='/verifications/start':
    kind=self.text(b,'kind');vid='sim_v_'+secrets.token_hex(12);c.execute('INSERT INTO checks VALUES(?,?,?,?)',(run,vid,kind,'PENDING'));return {'id':vid,'status':'PENDING','mode':'SIMULATOR'}
   if path in ['/verifications/query','/control/verifications/result']:
    vid=self.text(b,'id');row=c.execute('SELECT * FROM checks WHERE run=? AND id=?',(run,vid)).fetchone()
    if not row:raise Invalid('unknown verification')
    result=dict(row)
    if path.startswith('/control/'):
     st=b.get('status')
     if st not in ['PASSED','FAILED']:raise Invalid('invalid verification state')
     c.execute('UPDATE checks SET status=? WHERE run=? AND id=?',(st,run,vid));result['status']=st
    result['mode']='SIMULATOR';return result
   if path=='/messages/send':return {'providerMessageId':'sim_msg_'+secrets.token_hex(12),'status':'ACCEPTED','mode':'SIMULATOR'}
   raise Invalid('unknown operation')

def handler(store):
 class Handler(BaseHTTPRequestHandler):
  server_version='VehicleTestProvider/6.0'
  def log_message(self,*args):pass # request bodies, codes and headers never logged
  def response(self,status,data):
   raw=json.dumps(data,ensure_ascii=False).encode();self.send_response(status);self.send_header('Content-Type','application/json');self.send_header('Content-Length',str(len(raw)));self.end_headers();self.wfile.write(raw)
  def do_GET(self):
   if self.path=='/health':self.response(200,{'status':'UP','mode':'SIMULATOR'});return
   self.response(404,{'error':'not found'})
  def do_POST(self):
   if not hmac.compare_digest(self.headers.get('X-Sim-Secret',''),store.secret):self.response(403,{'error':'forbidden'});return
   try:
    n=int(self.headers.get('Content-Length','0'))
    if n<=0 or n>MAX_BODY:raise Invalid('invalid payload size')
    b=json.loads(self.rfile.read(n))
    if not isinstance(b,dict):raise Invalid('object required')
    self.response(200,store.call(self.path,b))
   except Conflict as e:self.response(409,{'error':str(e),'mode':'SIMULATOR'})
   except (Invalid,ValueError,TypeError) as e:self.response(422,{'error':str(e),'mode':'SIMULATOR'})
   except sqlite3.Error:self.response(503,{'error':'simulator database unavailable','mode':'SIMULATOR'})
 return Handler

def main():
 p=argparse.ArgumentParser();p.add_argument('--host',default='127.0.0.1');p.add_argument('--port',type=int,default=19091);p.add_argument('--db',default='./simulator.sqlite');a=p.parse_args();secret=os.environ.get('VI_SIM_SECRET','')
 if len(secret)<32:raise SystemExit('VI_SIM_SECRET >=32 chars required')
 server=ThreadingHTTPServer((a.host,a.port),handler(Store(a.db,secret)))
 print(json.dumps({'mode':'SIMULATOR','port':server.server_port}),flush=True);server.serve_forever()
if __name__=='__main__':main()
