"""静态校验：荣利模块 MyBatis Mapper 中的 SQL 是否与真实表结构一致。

校验内容：
  1) 引用的表是否存在；
  2) `别名.字段` 解析到的表是否真的有该字段；
  3) INSERT 列表字段、UPDATE SET 字段是否存在。

用法：python logs/check_mapper_schema.py
退出码 1 表示发现问题。
"""
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

MYSQL = r"D:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
DB_ARGS = ["-h127.0.0.1", "-P3307", "-uroot", "-pchenbeishui",
           "--default-character-set=utf8mb4", "-N", "-B", "xinyi"]
MAPPER_DIR = Path(r"E:/codex/xinyi/xinyi-rongli/src/main/java/com/xinyi/rongli/mapper")

SQL_KEYWORDS = {
    "select", "from", "where", "and", "or", "not", "insert", "into", "update", "set",
    "values", "join", "left", "right", "inner", "outer", "on", "group", "order", "by",
    "having", "limit", "offset", "as", "case", "when", "then", "else", "end", "distinct",
    "count", "sum", "max", "min", "avg", "coalesce", "ifnull", "null", "true", "false",
    "asc", "desc", "union", "all", "exists", "in", "is", "like", "between", "cast",
    "date", "datetime", "now", "utc_timestamp", "interval", "dual", "for", "with",
    "cross", "natural", "using", "if", "nullif", "greatest", "least", "abs", "round",
}


def load_schema():
    out = subprocess.run([MYSQL] + DB_ARGS + ["-e",
        "SELECT LOWER(TABLE_NAME), LOWER(COLUMN_NAME) FROM information_schema.COLUMNS "
        "WHERE TABLE_SCHEMA = 'xinyi'"],
        capture_output=True, text=True, encoding="utf-8")
    if out.returncode != 0:
        raise SystemExit("读取表结构失败：" + out.stderr)
    schema = defaultdict(set)
    tables = set()
    for line in out.stdout.splitlines():
        if "\t" not in line:
            continue
        table, column = line.split("\t", 1)
        schema[table].add(column)
        tables.add(table)
    return schema, tables


def extract_sql_strings(text):
    """提取注解中的 SQL：优先 triple-quote 文本块，其次普通双引号字符串。"""
    blocks = re.findall(r'"""(.*?)"""', text, re.S)
    rest = re.sub(r'"""(.*?)"""', "", text, flags=re.S)
    singles = re.findall(r'"((?:[^"\\]|\\.)*)"', rest)
    return blocks + [s.replace('\\"', '"') for s in singles]


def analyze(sql, schema, tables, file, idx, problems):
    # ON DUPLICATE KEY UPDATE 的列属于 INSERT 的目标表，先摘出来，避免被当成表名
    dup_columns = []

    def _strip_dup(m):
        dup_columns.append(m.group(1))
        return " "

    sql_core = re.sub(r'on\s+duplicate\s+key\s+update\s+(.*?)(?=\bwhere\b|\Z)',
                      _strip_dup, sql, flags=re.I | re.S)

    refs = re.findall(r'\b(?:from|join|update|into)\s+`?([a-zA-Z_][\w]*)`?\s*(?:as\s+)?([a-zA-Z_][\w]*)?',
                      sql_core, re.I)
    alias_map = {}
    insert_tables = []
    for table, alias in refs:
        table = table.lower()
        if table in SQL_KEYWORDS:
            continue
        if table not in tables:
            problems.append((file, idx, f"表不存在：{table}"))
            continue
        if alias and alias.lower() not in SQL_KEYWORDS:
            alias_map[alias.lower()] = table
        alias_map.setdefault(table, table)
    for table in re.findall(r'insert\s+into\s+`?([a-zA-Z_][\w]*)`?', sql, re.I):
        insert_tables.append(table.lower())

    # ON DUPLICATE KEY UPDATE 列校验
    for segment in dup_columns:
        for col in re.findall(r'(?:^|,)\s*`?([a-zA-Z_][\w]*)`?\s*=', segment):
            col = col.lower()
            for table in insert_tables:
                if table in tables and col not in SQL_KEYWORDS and col not in schema[table]:
                    problems.append((file, idx, f"ON DUPLICATE 字段不存在：{table}.{col}"))

    # 别名.字段
    for alias, column in re.findall(r'\b([a-zA-Z_][\w]*)\.([a-zA-Z_][\w]*)\b', sql_core):
        table = alias_map.get(alias.lower())
        if not table:
            continue
        if column.lower() not in schema[table] and column != "*":
            problems.append((file, idx, f"字段不存在：{table}.{column}"))

    # INSERT INTO t (a, b, c)
    for table, cols in re.findall(r'insert\s+into\s+`?([a-zA-Z_][\w]*)`?\s*\(([^)]*)\)', sql_core, re.I):
        table = table.lower()
        if table not in tables:
            continue
        for col in re.split(r"[,\s]+", cols):
            col = col.strip().strip("`").split(".")[-1].lower()
            if col and col not in SQL_KEYWORDS and col not in schema[table]:
                problems.append((file, idx, f"INSERT 字段不存在：{table}.{col}"))

    # UPDATE t SET a = ..., b = ...
    for table, assignments in re.findall(
            r'update\s+`?([a-zA-Z_][\w]*)`?\s+set\s+(.*?)(?:\bwhere\b|\Z)', sql_core, re.I | re.S):
        table = table.lower()
        if table not in tables:
            continue
        for col in re.findall(r'(?:^|,)\s*`?([a-zA-Z_][\w]*)`?\s*=', assignments):
            col = col.lower()
            if col not in SQL_KEYWORDS and col not in schema[table]:
                problems.append((file, idx, f"UPDATE 字段不存在：{table}.{col}"))


def main():
    schema, tables = load_schema()
    print(f"已加载 {len(tables)} 张表的表结构")
    problems = []
    files = sorted(MAPPER_DIR.glob("*.java"))
    for f in files:
        text = f.read_text(encoding="utf-8")
        for i, sql in enumerate(extract_sql_strings(text)):
            if not re.search(r"\b(select|insert|update|delete)\b", sql, re.I):
                continue
            analyze(sql, schema, tables, f.name, i, problems)

    if not problems:
        print(f"OK：{len(files)} 个 Mapper 的 SQL 与表结构一致")
        return 0

    print(f"发现 {len(problems)} 处不一致：")
    for file, idx, msg in problems:
        print(f"  [{file} #{idx}] {msg}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
