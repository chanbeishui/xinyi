#!/usr/bin/env bash
# =====================================================================
# 服务器初始化脚本（1panel + 容器 MySQL/Redis，Ubuntu 24.04）
#
# 一次做完：
#   1) 前置检查（docker、MySQL 容器、SQL 文件）
#   2) 建运行用户 xinyi 与目录结构
#   3) 导入 26 个 SQL（跳过 2 个 demo seed）
#   4) 校验结构（vi=80 / QRTZ=11 / 菜单数）
#   5) 校验业务账号能否连库
#   6) 生成 .env（含随机 token 密钥，权限 600）
#   7) 生成 config/application-prod.yml（自动填实际端口）
#   8) 安装 systemd 单元（暂不启动，等 jar 上传）
#
# 用法：
#   scp deploy/prod/server-bootstrap.sh root@服务器IP:/root/
#   ssh -t root@服务器IP 'bash /root/server-bootstrap.sh'
#
# 交互只问三个密码：MySQL root、业务库密码、Redis 密码。
# 可重复执行：已存在的用户/目录会跳过；SQL 导入只在库为空时进行。
# =====================================================================
set -euo pipefail

SQL_DIR=${SQL_DIR:-/root/xinyi-sql}
DB_NAME=${DB_NAME:-xinyi}
DB_USER=${DB_USER:-xinyi}
APP_USER=${APP_USER:-xinyi}
HOME_DIR=/home/xinyi
APP_DIR=$HOME_DIR/app

C_OK=$'\033[32m'; C_BAD=$'\033[31m'; C_INF=$'\033[36m'; C_END=$'\033[0m'
ok()   { printf '%s[ OK ]%s %s\n' "$C_OK" "$C_END" "$*"; }
bad()  { printf '%s[FAIL]%s %s\n' "$C_BAD" "$C_END" "$*"; }
info() { printf '%s[ .. ]%s %s\n' "$C_INF" "$C_END" "$*"; }
die()  { bad "$*"; exit 1; }

[ "$(id -u)" = "0" ] || die "请用 root 执行（或 sudo bash $0）"

# ---------------------------------------------------------------- 1 前置检查
info "1/9 前置检查"
command -v docker >/dev/null || die "未安装 docker"

MYSQL_C=${MYSQL_C:-$(docker ps --format '{{.Names}} {{.Image}}' | grep -i mysql | awk '{print $1}' | head -1)}
[ -n "$MYSQL_C" ] || die "没找到运行中的 MySQL 容器，先到 1panel 应用商店装 MySQL 8"

REDIS_C=${REDIS_C:-$(docker ps --format '{{.Names}} {{.Image}}' | grep -i redis | awk '{print $1}' | head -1)}
[ -n "$REDIS_C" ] || die "没找到运行中的 Redis 容器"

MYSQL_IMAGE=$(docker inspect -f '{{.Config.Image}}' "$MYSQL_C")
MYSQL_PORT=$(docker port "$MYSQL_C" 3306 2>/dev/null | head -1 | sed 's/.*://')
REDIS_PORT=$(docker port "$REDIS_C" 6379 2>/dev/null | head -1 | sed 's/.*://')
MYSQL_PORT=${MYSQL_PORT:-3306}
REDIS_PORT=${REDIS_PORT:-6379}

[ -d "$SQL_DIR" ] || die "SQL 目录不存在：$SQL_DIR（先在 Windows 上 scp -r 上传）"
sql_count=$(ls "$SQL_DIR"/*.sql 2>/dev/null | wc -l)
[ "$sql_count" -ge 28 ] || die "SQL 文件只有 $sql_count 个，应为 28 个"

ok "MySQL 容器 $MYSQL_C ($MYSQL_IMAGE) 端口 $MYSQL_PORT"
ok "Redis 容器 $REDIS_C 端口 $REDIS_PORT"
ok "SQL 文件 $sql_count 个"

# ---------------------------------------------------------------- 2 密码
info "2/9 读取密码"
read -rsp "MySQL root 密码: " MYSQL_ROOT_PW; echo
read -rsp "业务库账号 $DB_USER 的密码: " DB_PW; echo
read -rsp "Redis 密码: " REDIS_PW; echo

mysqlq() {
  docker exec -e MYSQL_PWD="$MYSQL_ROOT_PW" -i "$MYSQL_C" \
    mysql -uroot --default-character-set=utf8mb4 "$@"
}

# root 密码先验一下，避免后面导入全挂
if ! mysqlq -N -B -e "SELECT 1" >/dev/null 2>&1; then
  die "MySQL root 密码不正确，或容器未就绪。可到 1panel 数据库详情页核对"
fi
ok "root 密码验证通过"

# ---------------------------------------------------------------- 3 用户与目录
info "3/9 建运行用户与目录"
if id -u "$APP_USER" >/dev/null 2>&1; then
  ok "用户 $APP_USER 已存在，跳过"
else
  useradd -r -m -d "$HOME_DIR" -s /usr/sbin/nologin "$APP_USER"
  ok "已创建用户 $APP_USER"
fi

# 注意：单元素花括号 {config} 不会被展开（bash 只在含逗号或区间时展开），
# 必须写成完整路径，否则会建出一个字面量名为 {config} 的目录。
mkdir -p "$APP_DIR/config" "$HOME_DIR"/{upload,logs,web,backup,bin,sql}
chown -R "$APP_USER:$APP_USER" "$HOME_DIR"
chmod 755 "$HOME_DIR/logs"
ok "目录就绪：$APP_DIR / upload / logs / web / backup / bin"

# ---------------------------------------------------------------- 4 库与账号
info "4/9 准备数据库与业务账号"
# FORCE_REIMPORT=1 时先删库：用于上次导入中断、库里残留半截结构的场景。
# ⚠️ 会丢掉该库全部数据，只允许在初始化阶段使用，生产跑起来后绝对不能带这个参数。
if [ "${FORCE_REIMPORT:-0}" = "1" ]; then
  info "FORCE_REIMPORT=1：删除并重建库 $DB_NAME（库内数据将全部丢失）"
  mysqlq -e "DROP DATABASE IF EXISTS \`$DB_NAME\`;" || die "删库失败"
fi
# 不依赖面板是否真的建成功：缺库补库，缺账号补账号，密码不一致就对齐成你刚输入的
EPW=${DB_PW//\'/\'\'}
if ! mysqlq -e "
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$EPW';
ALTER USER '$DB_USER'@'%' IDENTIFIED BY '$EPW';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;" 2>/tmp/bs_db_err.txt; then
  bad "建库/建账号失败："
  grep -v "^mysql: \[Warning\]" /tmp/bs_db_err.txt | head -5
  die "请把上面错误发我"
fi
ok "库 $DB_NAME 与账号 $DB_USER 就绪"

# ---------------------------------------------------------------- 5 导入 SQL
info "5/9 导入 SQL"
existing=$(mysqlq -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME'")
if [ "${FORCE_REIMPORT:-0}" != "1" ] && [ "${existing:-0}" -gt 5 ]; then
  ok "库 $DB_NAME 已有 $existing 张表，跳过导入（如需重建请手工 DROP 后重跑）"
else
  LOG=/root/import-$(date +%F_%H%M).log
  cd "$SQL_DIR"
  for f in ry_20260417.sql quartz.sql $(ls migration_*.sql | sort); do
    printf '   ==> %s\n' "$f"
    if ! mysqlq "$DB_NAME" < "$f" 2>>"$LOG"; then
      bad "导入失败：$f"
      echo "------ 最近错误 ------"
      grep -i "error" "$LOG" | tail -5 || true
      echo "----------------------"
      echo "整库重建命令："
      echo "  docker exec -e MYSQL_PWD='***' -i $MYSQL_C mysql -uroot -e \\"
      echo "    \"DROP DATABASE $DB_NAME; CREATE DATABASE $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;\""
      die "已停止，未继续执行后续步骤"
    fi
  done
  ok "导入完成，日志 $LOG"
fi

# ---------------------------------------------------------------- 5 校验结构
info "6/9 校验表结构"
vi=$(mysqlq -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name LIKE 'vi\\_%'")
qrtz=$(mysqlq -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name LIKE 'QRTZ%'")
menu=$(mysqlq -N -B -e "SELECT COUNT(*) FROM $DB_NAME.sys_menu")
admin=$(mysqlq -N -B -e "SELECT COUNT(*) FROM $DB_NAME.sys_user WHERE user_name='admin'")
# 侧栏四分组与分组下可见页面：与 logs/ci_sql_check.sh 的断言保持一致，
# 比"菜单总数 > N"更可靠（总数会随菜单增删漂移，分组结构不会）
groups=$(mysqlq -N -B -e "SELECT COUNT(*) FROM $DB_NAME.sys_menu WHERE parent_id=2100 AND path IN ('business','people','inspect','runtime') AND menu_type='M'")
visible=$(mysqlq -N -B -e "SELECT COUNT(*) FROM $DB_NAME.sys_menu WHERE parent_id IN (2190,2191,2192,2193) AND visible='0' AND menu_type='C'")

ok "vi_* 表 $vi（期望 80）"
ok "QRTZ_* 表 $qrtz（期望 11）"
ok "侧栏分组 $groups（期望 4）"
ok "分组下可见页面 $visible（期望 20）"
ok "菜单总数 $menu（仅参考，不设阈值）"
ok "admin 账号 $admin（期望 1）"

[ "${vi:-0}" -ge 80 ]      || die "vi_* 表不足 80，某个 migration 没执行成功"
[ "${qrtz:-0}" -eq 11 ]    || die "QRTZ 表不是 11 张，quartz.sql 没导成功"
[ "${groups:-0}" -eq 4 ]   || die "侧栏四分组缺失，菜单迁移没执行成功"
[ "${visible:-0}" -eq 20 ] || die "分组下可见页面不是 20，菜单可见性被改坏了"
[ "${admin:-0}" -eq 1 ]    || die "admin 账号不存在"

# ---------------------------------------------------------------- 6 业务账号连通性
info "7/9 校验业务账号 $DB_USER 能否连库"
# 用已存在的 MySQL 镜像临时起一个客户端，从宿主机网络连 127.0.0.1:$MYSQL_PORT
if docker run --rm --network host -e MYSQL_PWD="$DB_PW" "$MYSQL_IMAGE" \
     mysql -h127.0.0.1 -P"$MYSQL_PORT" -u"$DB_USER" -N -B -e "SELECT 1" >/dev/null 2>&1; then
  ok "业务账号连接成功"
else
  bad "业务账号 $DB_USER 连不上（密码错 / 账号不存在 / 没授权 $DB_NAME）"
  echo "   到 1panel → 数据库 → $DB_NAME → 用户，确认账号与权限；"
  echo "   或手工执行："
  echo "   docker exec -e MYSQL_PWD='***' -i $MYSQL_C mysql -uroot -e \\"
  echo "     \"CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '新密码'; \\"
  echo "      GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%'; FLUSH PRIVILEGES;\""
  die "账号不通，后面的后端启动一定失败，先修这个"
fi

# ---------------------------------------------------------------- 7 .env 与配置
info "8/9 生成运行配置"
TOKEN_SECRET=$(openssl rand -base64 48 | tr -d '\n')

cat > "$APP_DIR/.env" <<EOF
# 由 server-bootstrap.sh 生成于 $(date '+%F %T')
XINYI_DB_USER=$DB_USER
XINYI_DB_PASSWORD=$DB_PW
XINYI_REDIS_PASSWORD=$REDIS_PW
XINYI_TOKEN_SECRET=$TOKEN_SECRET
EOF
chown "$APP_USER:$APP_USER" "$APP_DIR/.env"
chmod 600 "$APP_DIR/.env"
ok ".env 已生成（600）"

cat > "$APP_DIR/config/application-prod.yml" <<EOF
# 由 server-bootstrap.sh 生成于 $(date '+%F %T')
# 启动参数必须是 --spring.profiles.active=druid,prod（顺序反了会连回开发库）
server:
  port: 8080

spring:
  devtools:
    restart:
      enabled: false
  datasource:
    druid:
      master:
        url: jdbc:mysql://127.0.0.1:${MYSQL_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
        username: \${XINYI_DB_USER:$DB_USER}
        password: \${XINYI_DB_PASSWORD}
      statViewServlet:
        enabled: false
      initialSize: 5
      minIdle: 5
      maxActive: 30
      maxWait: 30000
  data:
    redis:
      host: 127.0.0.1
      port: ${REDIS_PORT}
      database: 0
      password: \${XINYI_REDIS_PASSWORD}
      timeout: 10s
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 16
          max-wait: 3000ms

ruoyi:
  profile: ${HOME_DIR}/upload

token:
  secret: \${XINYI_TOKEN_SECRET}
  expireTime: 60

logging:
  level:
    com.xinyi: info
    org.springframework: warn

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

security:
  multi-dept-query-enabled: true
  multi-dept-write-enabled: true
EOF
chown -R "$APP_USER:$APP_USER" "$APP_DIR/config"
chmod 644 "$APP_DIR/config/application-prod.yml"
ok "application-prod.yml 已生成（库端口 ${MYSQL_PORT} / Redis 端口 ${REDIS_PORT}）"

# ---------------------------------------------------------------- 8 systemd
info "9/9 安装 systemd 单元（暂不启动）"
cat > /etc/systemd/system/xinyi-admin.service <<'EOF'
[Unit]
Description=xinyi-admin backend
After=network-online.target docker.service
Wants=network-online.target

[Service]
Type=simple
User=xinyi
Group=xinyi
WorkingDirectory=/home/xinyi/app
EnvironmentFile=/home/xinyi/app/.env
Environment="LOG_PATH=/home/xinyi/logs"
Environment="TZ=Asia/Shanghai"
ExecStart=/usr/bin/java \
  -Xms512m -Xmx1536m \
  -XX:MaxMetaspaceSize=320m \
  -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/home/xinyi/logs \
  -Duser.timezone=Asia/Shanghai \
  -Dfile.encoding=UTF-8 \
  -jar /home/xinyi/app/xinyi-admin.jar \
  --spring.profiles.active=druid,prod
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
LimitNOFILE=65535
StandardOutput=append:/home/xinyi/logs/stdout.log
StandardError=append:/home/xinyi/logs/stderr.log

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
ok "单元已安装（未启动，等 jar 上传后 enable）"

# ---------------------------------------------------------------- 汇总
unset MYSQL_ROOT_PW DB_PW REDIS_PW TOKEN_SECRET

printf '\n%s==== 初始化完成 ====%s\n' "$C_OK" "$C_END"
cat <<EOF
库表：vi=$vi  QRTZ=$qrtz  菜单=$menu
配置：$APP_DIR/.env            （600，含密码与 token 密钥）
      $APP_DIR/config/application-prod.yml
目录：$HOME_DIR/{app,upload,logs,web,backup,bin}

下一步（在 Windows 上执行）：
  1. 打包： mvn clean package -DskipTests
  2. 上传： scp xinyi-admin/target/xinyi-admin.jar root@本机IP:$APP_DIR/
  3. 启动： systemctl enable --now xinyi-admin
  4. 探活： curl -s -o /dev/null -w '%{http_code}\\n' http://127.0.0.1:8080/captchaImage
EOF
