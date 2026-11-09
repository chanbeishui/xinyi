#!/usr/bin/env bash
# =====================================================================
# 数据库每日备份（适配 1panel 应用商店 MySQL 容器）
#
# 部署：/home/xinyi/bin/backup-xinyi.sh，chmod 700
# 调度：1panel → 计划任务 → Shell 脚本，每天 02:30
# 手工：bash /home/xinyi/bin/backup-xinyi.sh
#
# 设计要点：
#   1. 容器名自动探测 —— 1panel 重建容器后不需要改脚本
#   2. 用**业务账号**导出，不需要 root 密码；密码从 /home/xinyi/app/.env 读取，
#      不写死在脚本里，也不出现在命令行（mysqldump 走 MYSQL_PWD 环境变量）
#   3. 备份后校验完整性（gzip 可解 + 结尾有 Dump completed），半截文件直接删
#   4. 同步打包上传目录 —— 附件不在库里，光有 SQL 恢复不了
# =====================================================================
set -euo pipefail

BACKUP_DIR=${BACKUP_DIR:-/home/xinyi/backup}
KEEP_DAYS=${KEEP_DAYS:-14}
DB_NAME=${DB_NAME:-rongli_wx}
DB_USER=${DB_USER:-xinyi}
ENV_FILE=${ENV_FILE:-/home/xinyi/app/.env}
UPLOAD_DIR=${UPLOAD_DIR:-/home/xinyi/upload}

MYSQL_C=${MYSQL_C:-$(docker ps --format '{{.Names}} {{.Image}}' | grep -i mysql | awk '{print $1}' | head -1)}
[ -n "$MYSQL_C" ] || { echo "[FAIL] 没找到运行中的 MySQL 容器" >&2; exit 1; }

# 密码优先级：环境变量 DB_PASSWORD > .env 里的 XINYI_DB_PASSWORD
if [ -z "${DB_PASSWORD:-}" ]; then
  [ -f "$ENV_FILE" ] || { echo "[FAIL] 缺少 $ENV_FILE" >&2; exit 1; }
  DB_PASSWORD=$(grep -E '^XINYI_DB_PASSWORD=' "$ENV_FILE" | tail -1 | cut -d= -f2-)
fi
[ -n "${DB_PASSWORD:-}" ] || { echo "[FAIL] 未取到数据库密码（检查 $ENV_FILE 的 XINYI_DB_PASSWORD）" >&2; exit 1; }

TS=$(date +%F_%H%M)
OUT="$BACKUP_DIR/xinyi_${TS}.sql.gz"
mkdir -p "$BACKUP_DIR"

# --single-transaction：InnoDB 一致性快照，不锁表，业务可正常写入
# --no-tablespaces：非 root 账号缺 PROCESS 权限时，不加会直接报错
# 不传 --set-gtid-purged：非 root 无 RELOAD 权限时该参数同样会失败
if ! docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MYSQL_C" mysqldump \
      -u"$DB_USER" \
      --single-transaction --quick --no-tablespaces \
      --default-character-set=utf8mb4 \
      --routines --triggers --events \
      "$DB_NAME" 2>/tmp/dump.err | gzip -9 > "$OUT"; then
  echo "[FAIL] mysqldump 失败：" >&2
  grep -v "^mysql: \[Warning\]" /tmp/dump.err | head -5 >&2
  rm -f "$OUT"
  exit 1
fi

# 完整性校验：半截备份比没有备份更危险
gzip -t "$OUT" || { echo "[FAIL] gzip 校验失败" >&2; rm -f "$OUT"; exit 1; }
if ! gzip -dc "$OUT" | tail -1 | grep -q "Dump completed"; then
  echo "[FAIL] dump 未正常结束，已删除 $OUT" >&2
  rm -f "$OUT"
  exit 1
fi

# 上传目录同步一份（附件不在数据库里）
if [ -d "$UPLOAD_DIR" ]; then
  tar -czf "$BACKUP_DIR/upload_${TS}.tar.gz" \
    -C "$(dirname "$UPLOAD_DIR")" "$(basename "$UPLOAD_DIR")" 2>/dev/null || true
fi

# 保留策略
find "$BACKUP_DIR" -name 'xinyi_*.sql.gz'  -mtime +"$KEEP_DAYS" -delete
find "$BACKUP_DIR" -name 'upload_*.tar.gz' -mtime +"$KEEP_DAYS" -delete

SIZE=$(du -h "$OUT" | cut -f1)
TABLES=$(gzip -dc "$OUT" | grep -c "^CREATE TABLE" || true)
echo "[OK] $(date '+%F %T') $OUT ($SIZE, ${TABLES} 张表) 容器=$MYSQL_C 库=$DB_NAME 账号=$DB_USER"
