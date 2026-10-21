#!/usr/bin/env bash
# E07 隔离集成测试库引导脚本（可复现）。
# 在【现有 MySQL 服务端】新建一次性隔离库 xinyi_vi_it_e07，供 E07TestingIT 的控制面/夹具运行；
# 绝不触碰业务库 xinyi。库名固定前缀 xinyi_vi_it_（TestGuard 与 test_schema.py 的 guard 正则要求）。
#
# 用法（密码/密钥经环境注入，不写入仓库）：
#   set -a; . ./.env.vi.local; set +a
#   export MYSQL_ROOT_PASSWORD='<本机 MySQL root 密码，仅本次引导用>'
#   bash docs/implementation/vehicle/E07/bootstrap_it_db.sh
#
# 依赖：mysql/mysqldump 客户端；VI_TEST_CONTROL_KEY（>=32 字节，来自 .env.vi.local）。
set -euo pipefail

DB="${IT_DB:-xinyi_vi_it_e07}"
SRC_DB="${SRC_DB:-xinyi}"
MYSQL="${MYSQL_BIN:-mysql}"
MYSQLDUMP="${MYSQLDUMP_BIN:-mysqldump}"

# 安全闸：目标必须是隔离 IT 库，源必须是业务库；二者不得相同
case "$DB" in xinyi_vi_it_*) : ;; *) echo "REFUSE: 目标库名必须匹配 xinyi_vi_it_*，当前=$DB" >&2; exit 1;; esac
[ "$DB" != "$SRC_DB" ] || { echo "REFUSE: 目标库不得等于源业务库" >&2; exit 1; }
: "${MYSQL_ROOT_PASSWORD:?须导出 MYSQL_ROOT_PASSWORD（仅本次引导用，不入库）}"
: "${VI_TEST_CONTROL_KEY:?须导出 VI_TEST_CONTROL_KEY（>=32 字节）}"
[ "${#VI_TEST_CONTROL_KEY}" -ge 32 ] || { echo "REFUSE: VI_TEST_CONTROL_KEY 长度须 >=32" >&2; exit 1; }

export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"

echo ">> 1. 建隔离库 $DB（utf8mb4/utf8mb4_0900_ai_ci）"
"$MYSQL" --user=root -e "DROP DATABASE IF EXISTS \`$DB\`; CREATE DATABASE \`$DB\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

echo ">> 2. 克隆 $SRC_DB 完整结构+数据（含视图）到 $DB"
# 说明：仓库 init 脚本(ry_20260417.sql)未含 sys_user_dept_integrity_issue 视图（被 UserDeptIntegrityPatrol 依赖），
# 纯脚本初始化会与实况不一致；克隆已验证可启动的 $SRC_DB 结构最可靠。$DB 为一次性隔离库，可弃。
"$MYSQLDUMP" --user=root --single-transaction --no-tablespaces --column-statistics=0 \
  --set-gtid-purged=OFF --routines --triggers --events "$SRC_DB" | "$MYSQL" --user=root "$DB"

echo ">> 3. 清空 vi_ 业务表（夹具干净起点）"
"$MYSQL" --user=root -N -e "SELECT CONCAT('TRUNCATE TABLE \`',table_name,'\`;') FROM information_schema.tables WHERE table_schema='$DB' AND table_name LIKE 'vi\\_%';" \
  | { echo "SET FOREIGN_KEY_CHECKS=0;"; cat; echo "SET FOREIGN_KEY_CHECKS=1;"; } \
  | "$MYSQL" --user=root "$DB"

echo ">> 4. 建测试守卫表 test_environment_guard（kind + key_hash=SHA256(VI_TEST_CONTROL_KEY)）"
KEY_HASH=$(python3 -c "import hashlib,os;print(hashlib.sha256(os.environ['VI_TEST_CONTROL_KEY'].encode()).hexdigest())")
"$MYSQL" --user=root "$DB" <<SQL
DROP TABLE IF EXISTS test_environment_guard;
CREATE TABLE test_environment_guard (id INT PRIMARY KEY, kind VARCHAR(64) NOT NULL, key_hash CHAR(64) NOT NULL, created_at DATETIME(3) NOT NULL);
INSERT INTO test_environment_guard VALUES(1,'XINYI_VEHICLE_ISOLATED','$KEY_HASH',UTC_TIMESTAMP(3));
SQL

echo ">> 5. 建测试专用 run 登记表 test_fixture_run（非 66 业务主模型）"
"$MYSQL" --user=root "$DB" < "$(dirname "$0")/test_fixture_run.sql"

echo ">> 完成。校验："
"$MYSQL" --user=root -N -e "SELECT CONCAT('  表数=',COUNT(*)) FROM information_schema.tables WHERE table_schema='$DB';"
"$MYSQL" --user=root -N -e "SELECT CONCAT('  守卫行 kind=',kind,' hash前12=',LEFT(key_hash,12)) FROM \`$DB\`.test_environment_guard WHERE id=1;"
"$MYSQL" --user=root -N -e "SELECT CONCAT('  vi_account 行数(应0)=',COUNT(*)) FROM \`$DB\`.vi_account;"
unset MYSQL_PWD
echo ">> 隔离库 $DB 就绪；业务库 $SRC_DB 未被修改。"
