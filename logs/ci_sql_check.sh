#!/usr/bin/env bash
# 数据库初始化与迁移的幂等校验（CI 与本地共用同一份逻辑）。
#
# 校验内容：
#   1. 基础脚本 ry_20260417.sql + 全部 migration_*.sql 能按序执行成功
#   2. 同一批迁移脚本**再执行一遍**仍然成功（幂等）
#   3. 结构断言：vi_* 表 >= 66；侧栏四分组挂在 2100 目录下
#
# 用法（本地，起一个空库）：
#   mysql -h127.0.0.1 -P3307 -uroot -p... -e "CREATE DATABASE xinyi_ci"
#   bash logs/ci_sql_check.sh xinyi_ci
#
# 用法（CI）：DB_HOST/DB_PORT/DB_PASS 由环境变量注入，见 .github/workflows/backend.yml
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DB_NAME="${1:-xinyi}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-13307}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-xinyi_dev}"

MYSQL_BIN="${MYSQL_BIN:-mysql}"
run_sql() {
  "$MYSQL_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    --default-character-set=utf8mb4 "$@"
}

fail=0
cd "$ROOT"

echo "== 0/4 docker-compose 是否挂载了全部迁移脚本"
# 全新环境靠 docker-compose 的 /docker-entrypoint-initdb.d 建库，挂载清单是手写的，
# 极易在新增迁移后忘记同步（本仓库已发生过：V6.4 的六个迁移与 20261022_06 一度全缺）。
if [ -f docker-compose.yml ]; then
  missing=""
  for f in $(ls sql/migration_*.sql | sort); do
    if ! grep -q "$(basename "$f")" docker-compose.yml; then
      missing="$missing $f"
    fi
  done
  if [ -z "$missing" ]; then
    echo "  [OK]   全部迁移均已挂载"
  else
    echo "  [FAIL] docker-compose.yml 漏挂:$missing"; fail=1
  fi
else
  echo "  [SKIP] 无 docker-compose.yml"
fi

echo "== 1/4 基础脚本 + 迁移首轮执行（库 $DB_NAME @ $DB_HOST:$DB_PORT）"
if run_sql "$DB_NAME" < sql/ry_20260417.sql > /dev/null 2>/tmp/ci_sql_err.txt; then
  echo "  [OK]   sql/ry_20260417.sql"
else
  echo "  [FAIL] sql/ry_20260417.sql"; grep -v Warning /tmp/ci_sql_err.txt | head -3; fail=1
fi

for f in $(ls sql/migration_*.sql | sort); do
  if run_sql "$DB_NAME" < "$f" > /dev/null 2>/tmp/ci_sql_err.txt; then
    echo "  [OK]   $f"
  else
    echo "  [FAIL] $f"; grep -v Warning /tmp/ci_sql_err.txt | head -3; fail=1
  fi
done

echo "== 2/4 迁移幂等复跑"
for f in $(ls sql/migration_*.sql | sort); do
  if run_sql "$DB_NAME" < "$f" > /dev/null 2>/tmp/ci_sql_err.txt; then
    echo "  [OK]   $f"
  else
    echo "  [FAIL] $f"; grep -v Warning /tmp/ci_sql_err.txt | head -3; fail=1
  fi
done

echo "== 3/4 结构断言"
vi=$(run_sql -N -B -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME LIKE 'vi\\_%'" 2>/dev/null)
groups=$(run_sql "$DB_NAME" -N -B -e "SELECT COUNT(*) FROM sys_menu WHERE parent_id=2100 AND path IN ('business','people','inspect','runtime') AND menu_type='M'" 2>/dev/null)
visible=$(run_sql "$DB_NAME" -N -B -e "SELECT COUNT(*) FROM sys_menu WHERE parent_id IN (2190,2191,2192,2193) AND visible='0' AND menu_type='C'" 2>/dev/null)

check_ge() { if [ "$2" -ge "$3" ]; then echo "  [OK]   $1=$2 (需 >= $3)"; else echo "  [FAIL] $1=$2 (需 >= $3)"; fail=1; fi; }
check_eq() { if [ "$2" -eq "$3" ]; then echo "  [OK]   $1=$2 (需 = $3)"; else echo "  [FAIL] $1=$2 (需 = $3)"; fail=1; fi; }

# 80 = 资料包 V6.4 的 79 张 + 本库保留的 vi_sms_send_log（验证码频控与留痕，
# V6.4 的 vi_notification_delivery 不覆盖该用例）。2026-10-23 已退役
# vi_employee_binding_request / vi_employee_binding_credential 两张表。
check_ge "vi_* 表数" "${vi:-0}" 80
check_eq "侧栏分组数" "${groups:-0}" 4
# 20 = 业务 6 + 人员 11 + 巡检 9 + 运行时 5 中的可见 C 页；此前写 21 是被开发库的
# 分组漂移带偏的（2122/2124 被手工挪到业务组），已由 migration_20261023_03 对齐。
check_eq "分组下可见页面数" "${visible:-0}" 20

if [ $fail -eq 0 ]; then echo "== 全部通过"; else echo "== 存在失败项"; fi
exit $fail
