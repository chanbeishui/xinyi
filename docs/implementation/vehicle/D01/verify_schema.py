"""D01专属隔离库前向迁移与完整初装结构比对；拒绝业务库，保留旧行，不猜测修复。"""
from pathlib import Path
import datetime
import importlib.util
import json
import os
import re
import secrets
import subprocess
from urllib.parse import urlparse

import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[4]
BASELINE = 'c898769'
MIGRATION = ROOT / 'sql/migration_20261112_driver_listening.sql'
REPORT = ROOT / 'docs/implementation/vehicle/D01/schema-verification.json'
spec = importlib.util.spec_from_file_location('vehicle_db', ROOT / 'docs/vehicle/V6.4/12_工程执行/database.py')
database = importlib.util.module_from_spec(spec)
spec.loader.exec_module(database)


def resolve(value):
    match = re.fullmatch(r'\$\{([^:}]+)(?::(.*))?\}', str(value))
    return os.environ.get(match[1], match[2] or '') if match else value


def metadata(connection, name, table):
    with connection.cursor() as cursor:
        cursor.execute("""SELECT column_name,column_type,is_nullable,column_default,extra,
                      generation_expression,collation_name,column_comment
               FROM information_schema.columns
               WHERE table_schema=%s AND table_name=%s
               ORDER BY ordinal_position""", (name, table))
        columns = cursor.fetchall()
        cursor.execute("""SELECT index_name,non_unique,seq_in_index,column_name,index_type
               FROM information_schema.statistics
               WHERE table_schema=%s AND table_name=%s
               ORDER BY index_name,seq_in_index""", (name, table))
        indexes = cursor.fetchall()
        cursor.execute("""SELECT tc.constraint_name,cc.check_clause
               FROM information_schema.table_constraints tc
               JOIN information_schema.check_constraints cc
                 ON cc.constraint_schema=tc.constraint_schema AND cc.constraint_name=tc.constraint_name
               WHERE tc.table_schema=%s AND tc.table_name=%s AND tc.constraint_type='CHECK'
               ORDER BY tc.constraint_name""", (name, table))
        return {'columns': columns, 'indexes': indexes, 'checks': cursor.fetchall()}


def main():
    config_path = ROOT / 'xinyi-admin/src/main/resources/application-druid.yml'
    config = yaml.safe_load(config_path.read_text())['spring']['datasource']['druid']['master']
    url = urlparse(resolve(config['url']).removeprefix('jdbc:'))
    assert url.hostname in ['localhost', '127.0.0.1'], '只允许本机专属测试服务'
    connection = pymysql.connect(
        host=url.hostname,
        port=url.port or 3306,
        user=resolve(config['username']),
        password=resolve(config['password']),
        database='xinyi_vi_it_e07',
        charset='utf8mb4',
        autocommit=True,
    )
    created = []
    nonce = secrets.token_hex(24)
    statements = database.split_sql(MIGRATION.read_text())
    changed = sorted({
        m[1] for s in statements
        if (m := re.match(r'(?i)(?:ALTER TABLE|CREATE TABLE)\s+`?(vi_[a-z0-9_]+)`?', s))
    })
    assert changed, '未找到业务迁移目标'
    old_sql = subprocess.check_output(['git', 'show', BASELINE + ':sql/ry_20260417.sql'], cwd=ROOT).decode()
    full_sql = (ROOT / 'sql/ry_20260417.sql').read_text()

    def definitions(text):
        return {
            m[1]: m[0]
            for m in re.finditer(
                r'CREATE TABLE\s+`?([a-z0-9_]+)`?\s*\(.*?\)\s*ENGINE[^;]*;', text, re.S | re.I
            )
        }

    old, full = definitions(old_sql), definitions(full_sql)
    menus = [s for s in statements if re.match(r'(?i)INSERT INTO sys_menu\b', s)]
    assert len(menus) == 3
    for menu in menus:
        assert menu in database.split_sql(full_sql), '菜单增量和完整初始化不同'

    def execute(sqls):
        with connection.cursor() as cursor:
            for sql in sqls:
                cursor.execute(sql)
                while cursor.nextset():
                    pass

    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT kind FROM test_environment_guard WHERE id=1")
            assert cursor.fetchone() == ('XINYI_VEHICLE_ISOLATED',)
            cursor.execute("SET time_zone='+00:00'")
            # 备份仅写忽略目录，不把测试身份、定位或密文输出进报告和Git。
            before = {}
            for table in changed:
                cursor.execute(
                    'SELECT COUNT(*) FROM information_schema.tables '
                    'WHERE table_schema=DATABASE() AND table_name=%s', (table,)
                )
                if cursor.fetchone()[0]:
                    cursor.execute('SELECT * FROM `' + table + '`')
                    before[table] = cursor.fetchall()
            cursor.execute('SELECT * FROM sys_menu WHERE menu_id BETWEEN 2971500 AND 2971502')
            before['menus'] = cursor.fetchall()
            backup = ROOT / '.vi-local/backup'
            backup.mkdir(exist_ok=True)
            (backup / ('d01-before-' + secrets.token_hex(8) + '.json')).write_text(json.dumps(before, default=str))
        # 首次执行必须完整，部分迁移应保留现场。再次比对仅在新目标表全部存在时跳过DDL。
        new_tables = [t for t in changed if t not in old]
        present = [t for t in new_tables if t in before]
        assert not present or len(present) == len(new_tables), '部分D01结构，拒绝猜测补迁移'
        if not present:
            execute(statements)
        for mode in ['upgrade', 'full']:
            name = 'xinyi_vi_it_d01_' + mode + '_' + secrets.token_hex(5)
            with connection.cursor() as cursor:
                cursor.execute(
                    'CREATE DATABASE `' + name + '` '
                    'CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci'
                )
                created.append(name)
                cursor.execute('USE `' + name + '`')
                cursor.execute('CREATE TABLE d01_schema_owner(nonce VARCHAR(48) NOT NULL)')
                cursor.execute('INSERT INTO d01_schema_owner VALUES (%s)', (nonce,))
            selected = old if mode == 'upgrade' else full
            execute([selected['sys_menu']] + [selected[t] for t in changed if t in selected])
            execute(statements if mode == 'upgrade' else menus)
        for table in changed:
            upgraded = metadata(connection, created[0], table)
            initialized = metadata(connection, created[1], table)
            assert upgraded == initialized, table + '升级与完整初始化不一致'
            assert metadata(connection, 'xinyi_vi_it_e07', table) == initialized, table + '测试库结构不一致'
        with connection.cursor() as cursor:
            for table, rows in before.items():
                if table == 'menus':
                    continue
                cursor.execute('SELECT COUNT(*) FROM xinyi_vi_it_e07.`' + table + '`')
                assert cursor.fetchone()[0] == len(rows), table + '迁移改变既有行数'
            values = []
            for name in ['xinyi_vi_it_e07', *created]:
                cursor.execute(
                    'SELECT menu_id,menu_name,parent_id,path,component,menu_type,perms '
                    'FROM `' + name + '`.sys_menu '
                    'WHERE menu_id BETWEEN 2971500 AND 2971502 ORDER BY menu_id'
                )
                values.append(cursor.fetchall())
            assert values[0] == values[1] == values[2] and len(values[0]) == 3
        result = {
            'passed': True,
            'upgradeEqualsFull': True,
            'testSchemaEqualsFull': True,
            'tables': changed,
            'comparedTables': len(changed),
            'menuRows': 3,
            'existingRowsPreserved': True,
            'columnOrdinalCompared': True,
            'checksCompared': True,
            'businessOrProductionChanged': False,
            'migration': str(MIGRATION.relative_to(ROOT)),
            'at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
        }
        REPORT.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
        print(json.dumps(result, ensure_ascii=False))
    finally:
        # 只清理由本进程创建且随机所有权标记仍匹配的临时库。
        for name in created:
            with connection.cursor() as cursor:
                cursor.execute('SELECT nonce FROM `' + name + '`.d01_schema_owner')
                if cursor.fetchone() == (nonce,) and re.fullmatch(
                    r'xinyi_vi_it_d01_(upgrade|full)_[a-f0-9]{10}', name
                ):
                    cursor.execute('DROP DATABASE `' + name + '`')
        connection.close()


if __name__ == '__main__':
    main()
