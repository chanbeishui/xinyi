#!/usr/bin/env bash
# E02 · 隔离开发环境供给（Docker 不可用时的独立测试实例路径）
# 策略：使用本机已运行的 MySQL 服务端(3306)，新建【独立数据库 xinyi_vi_dev + 独立用户 vi_dev】，
#       并启动【独立 Redis 实例(36379)】。绝不触碰用户原有库(ry-vue/xinyi/sys)与原 Redis(6379)。
# 安全：root 密码从环境变量 MYSQL_ROOT_PASSWORD 读取（不硬编码、不打印、不入库）；
#       vi_dev 使用 .env.vi.local 中的随机密码；脚本输出不含任何密钥值。
# 可重复运行（幂等：库/用户 IF NOT EXISTS；redis 已运行则跳过）。
set -euo pipefail
REPO="$(cd "$(dirname "$0")/../../../.." && pwd)"; cd "$REPO"
[ -f pom.xml ] || { echo "BLOCKED: 未定位到仓库根(缺 pom.xml)，REPO=$REPO"; exit 2; }
LOC=".vi-local"; ENVF=".env.vi.local"
[ -f "$ENVF" ] || { echo "BLOCKED: 缺 .env.vi.local，先跑 engineer.py env --apply"; exit 2; }
[ -n "${MYSQL_ROOT_PASSWORD:-}" ] || { echo "BLOCKED: 需环境变量 MYSQL_ROOT_PASSWORD（仅本次引导建库用，不写入文件）"; exit 2; }
getv(){ grep -E "^$1=" "$ENVF" | head -1 | cut -d= -f2-; }
DB_NAME="$(getv VI_DB_NAME)"; DB_USER="$(getv VI_DB_USER)"; DB_PASS="$(getv VI_DB_PASSWORD)"
RD_PASS="$(getv VI_REDIS_PASSWORD)"; RD_PORT="$(getv VI_REDIS_PORT)"; RD_PORT="${RD_PORT:-36379}"
MY_HOST="127.0.0.1"; MY_PORT="3306"   # 本机已运行服务端；独立库提供逻辑隔离
mkdir -p "$LOC/files" "$LOC/public"

echo "== 隔离 MySQL（本机服务端 $MY_HOST:$MY_PORT，独立库 $DB_NAME）=="
echo "  服务端版本: $(MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root -h "$MY_HOST" -P "$MY_PORT" --connect-timeout=5 -N -e 'SELECT VERSION();' 2>/dev/null)"
echo "  建库（utf8mb4/utf8mb4_0900_ai_ci，IF NOT EXISTS，不动其他库）..."
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root -h "$MY_HOST" -P "$MY_PORT" <<SQL
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'%';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
SQL
echo "  vi_dev 连接自测: $(MYSQL_PWD="$DB_PASS" mysql -u "$DB_USER" -h "$MY_HOST" -P "$MY_PORT" -N -e 'SELECT CURRENT_USER(), DATABASE();' "$DB_NAME" 2>/dev/null)"
echo "  vi_dev 越权自测(应被拒，证明仅授权 $DB_NAME): $(MYSQL_PWD="$DB_PASS" mysql -u "$DB_USER" -h "$MY_HOST" -P "$MY_PORT" -N -e 'SHOW DATABASES;' 2>/dev/null | tr '\n' ' ')"
# 将 .env.vi.local 的 DB 端口指向本机服务端(3306)，不打印其他值
if [ "$(getv VI_DB_PORT)" != "$MY_PORT" ]; then
  tmp="$(mktemp)"; while IFS= read -r line; do
    case "$line" in VI_DB_PORT=*) echo "VI_DB_PORT=$MY_PORT";; VI_DB_HOST=*) echo "VI_DB_HOST=$MY_HOST";; *) echo "$line";; esac
  done < "$ENVF" > "$tmp" && cat "$tmp" > "$ENVF" && rm -f "$tmp"; chmod 600 "$ENVF"
  echo "  已更新 .env.vi.local: VI_DB_PORT=$MY_PORT VI_DB_HOST=$MY_HOST（值未回显）"
fi

echo "== 隔离 Redis（独立实例 :$RD_PORT，不碰 6379）=="
LOCABS="$REPO/$LOC"
if redis-cli -p "$RD_PORT" -a "$RD_PASS" --no-auth-warning PING >/dev/null 2>&1; then
  echo "  已在运行"
else
  redis-server --port "$RD_PORT" --requirepass "$RD_PASS" --dir "$LOCABS" --pidfile "$LOCABS/redis.pid" --logfile "$LOCABS/redis.log" --save '' --daemonize yes
  for i in $(seq 1 15); do redis-cli -p "$RD_PORT" -a "$RD_PASS" --no-auth-warning PING >/dev/null 2>&1 && break; sleep 1; done
fi
echo "  Redis PING(:$RD_PORT): $(redis-cli -p "$RD_PORT" -a "$RD_PASS" --no-auth-warning PING 2>&1)"
echo "== 完成：独立库 $DB_NAME@3306 + 独立 Redis:$RD_PORT 就绪；用户原库/原Redis 未触碰 =="
