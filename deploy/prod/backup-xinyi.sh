#!/usr/bin/env bash
# =====================================================================
# 数据库每日备份（适配 1Panel 应用商店 MySQL 容器）
#
# 部署：放到 /home/xinyi/bin/backup-xinyi.sh，chmod 700
# 调度：1Panel → 计划任务 → Shell 脚本，每天 02:30
# 手工跑：/home/xinyi/bin/backup-xinyi.sh
#
# ⚠️ 容器名先确认： docker ps --format '{{.Names}}' | grep -i mysql
#    （1Panel 装的应用容器名形如 1Panel-mysql-AbCdEf）
# =====================================================================
set -euo pipefail

BACKUP_DIR=/home/xinyi/backup
KEEP_DAYS=14
MYSQL_CONTAINER="1Panel-mysql-XXXXXX"   # ← 改成实际容器名
DB_NAME=xinyi
DB_ROOT_PASS="CHANGE_ME"                # ← 1Panel 数据库页的 root 密码

mkdir -p "$BACKUP_DIR"
TS=$(date +%F_%H%M)
OUT="$BACKUP_DIR/xinyi_${TS}.sql.gz"

# --single-transaction：InnoDB 一致性快照，不锁表，业务可正常写入
# --set-gtid-purged=OFF：避免导入到新实例时报 GTID 相关错误
# --no-tablespaces：规避非 root 权限下 PROCESS 权限缺失报错
docker exec "$MYSQL_CONTAINER" mysqldump \
  -uroot -p"$DB_ROOT_PASS" \
  --single-transaction --quick \
  --default-character-set=utf8mb4 \
  --routines --triggers --events \
  --set-gtid-purged=OFF --no-tablespaces \
  "$DB_NAME" | gzip -9 > "$OUT"

# 校验：gzip 完整 + 结尾有 mysqldump 完成标记，避免留下半截备份
gzip -t "$OUT"
gzip -dc "$OUT" | tail -1 | grep -q "Dump completed" \
  || { echo "[ERROR] $(date '+%F %T') dump 不完整: $OUT"; exit 1; }

# 用户文件全部在阿里云 OSS，本地没有 upload 目录需要打包；
# 如果要异地副本，用 ossutil 把 db 备份推到 OSS 即可（见下方示例）

# 保留策略
find "$BACKUP_DIR" -name 'xinyi_*.sql.gz'   -mtime +"$KEEP_DAYS" -delete

echo "[OK] $(date '+%F %T') $(ls -lh "$OUT" | awk '{print $5, $9}')"

# --- 可选：同步到阿里云 OSS（轻量服务器流量包有限，建议走 ossutil 内网端点） ---
# ossutil64 cp "$OUT" oss://你的bucket/xinyi-backup/ --endpoint oss-cn-hangzhou-internal.aliyuncs.com
