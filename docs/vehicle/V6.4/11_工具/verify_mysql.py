"""Read-only actual schema comparison. Requires mysql CLI and an explicitly chosen database.
Example: python verify_mysql.py --database xinyi_test --defaults-file /secure/mysql-test.cnf
No passwords on CLI, no CREATE/ALTER/DROP, no implicit database creation.
"""
import argparse,json,re,shutil,subprocess,sys
from pathlib import Path
R=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--database',required=True);p.add_argument('--defaults-file',required=True);p.add_argument('--output',default='mysql_structure_result.json');a=p.parse_args()
if not re.fullmatch('[A-Za-z0-9_]+',a.database):raise SystemExit('Invalid database name')
if not shutil.which('mysql'):raise SystemExit('BLOCKED_ENVIRONMENT: mysql CLI missing. No SQL executed.')
if not Path(a.defaults_file).is_file():raise SystemExit('Credential config file is missing')
base=['mysql','--defaults-extra-file='+str(Path(a.defaults_file).resolve()),'--batch','--raw','--skip-column-names','--database='+a.database]
def query(text):
 cp=subprocess.run(base+['--execute',text],capture_output=True,text=True,timeout=30,check=True)
 return [row.split('\t')for row in cp.stdout.strip().splitlines()if row]
model=json.loads((R/'05_数据设计/data_model_V6.4.json').read_text());rows=query("SELECT table_name,column_name,column_key,extra,COALESCE(generation_expression,'') FROM information_schema.columns WHERE table_schema=DATABASE() AND LEFT(table_name,3)='vi_' ORDER BY table_name,ordinal_position")
actual={}
for table,col,key,extra,exp in rows:actual.setdefault(table,[]).append({'column':col,'key':key,'extra':extra,'generation':exp})
checks=[]
for t in model:
 cols=actual.get(t['name'],[]);checks.append({'table':t['name'],'columnsMatch':{x['column']for x in cols}=={c[0]for c in t['cols']},'primaryKeyMatch':[x['column']for x in cols if x['key']=='PRI']==t['primaryKey'],'generatedMatch':{x['column']for x in cols if x['generation']}=={c[0]for c in t['cols']if'GENERATED ALWAYS'in c[1]},'idStrategyMatch':all(('auto_increment'in x['extra'])==(t['idStrategy']=='MYSQL_AUTO_INCREMENT'and x['column']=='id')for x in cols)})
report={'scope':'Actual information_schema structure only; no transactional correctness or time/SQL write tests','database':a.database,'checks':checks,'extraTables':sorted(set(actual)-{t['name']for t in model})}
Path(a.output).write_text(json.dumps(report,ensure_ascii=False,indent=2));print('Read-only comparison written:',a.output)
sys.exit(0 if all(all(v for k,v in c.items()if k!='table')for c in checks)and not report['extraTables']else 1)
