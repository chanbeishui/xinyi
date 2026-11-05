#!/usr/bin/env bash
# =====================================================================
# 服务器端原子部署（CI 与本地一键脚本共用同一份逻辑）
#
# 不在服务器上直接执行，而是由调用方通过 ssh 的 stdin 喂进去：
#   ssh user@host "DO_BACKEND=1 DO_FRONTEND=1 bash -s" < deploy/prod/remote-deploy.sh
#
# 前置：待部署产物已上传到 STAGE_DIR
#   $STAGE_DIR/xinyi-admin.jar
#   $STAGE_DIR/dist.tar.gz
#
# 流程：后端（备份 → 切换 → 重启 → 健康检查 → 失败回滚）→ 前端（解包 → 校验 → 原子切换）
# 后端健康检查不过时**不会动前端**，避免出现「新后端 + 旧前端」的半吊子状态。
# =====================================================================
set -euo pipefail

STAGE_DIR=${STAGE_DIR:-/tmp/xinyi-deploy}
APP_DIR=${APP_DIR:-/home/xinyi/app}
WEB_DIR=${WEB_DIR:-/home/xinyi/web}
SERVICE=${SERVICE:-xinyi-admin}
JAR_NAME=${JAR_NAME:-xinyi-admin.jar}
HEALTH_URL=${HEALTH_URL:-http://127.0.0.1:8080/captchaImage}
HEALTH_TIMEOUT=${HEALTH_TIMEOUT:-60}
RUN_USER=${RUN_USER:-xinyi}
DO_BACKEND=${DO_BACKEND:-1}
DO_FRONTEND=${DO_FRONTEND:-1}
GIT_SHA=${GIT_SHA:-unknown}
GIT_REF=${GIT_REF:-unknown}
OPERATOR=${OPERATOR:-manual}

log()  { printf '[deploy %s] %s\n' "$(date +%H:%M:%S)" "$*"; }
die()  { printf '[deploy ERROR] %s\n' "$*" >&2; exit 1; }

deploy_backend() {
  [ -f "$STAGE_DIR/$JAR_NAME" ] || die "缺少产物 $STAGE_DIR/$JAR_NAME"

  log "后端：备份旧版本并切换 jar"
  cd "$APP_DIR"
  if [ -f "$JAR_NAME" ]; then
    cp -f "$JAR_NAME" "$JAR_NAME.prev"
  fi
  mv -f "$STAGE_DIR/$JAR_NAME" "$JAR_NAME"
  chown "$RUN_USER:$RUN_USER" "$JAR_NAME" 2>/dev/null || true

  log "重启 $SERVICE"
  systemctl restart "$SERVICE" || die "systemctl restart $SERVICE 失败"

  log "健康检查 $HEALTH_URL（最多 ${HEALTH_TIMEOUT}s）"
  local waited=0 code
  while [ "$waited" -lt "$HEALTH_TIMEOUT" ]; do
    code=$(curl -s -o /dev/null -w '%{http_code}' "$HEALTH_URL" || true)
    if [ "$code" = "200" ]; then
      log "后端就绪，耗时 ${waited}s"
      return 0
    fi
    sleep 3
    waited=$((waited + 3))
  done

  log "健康检查超时（最后状态码 ${code:-000}），开始回滚"
  if [ -f "$JAR_NAME.prev" ]; then
    mv -f "$JAR_NAME.prev" "$JAR_NAME"
    systemctl restart "$SERVICE" || true
    log "已回滚到上一版 jar"
  else
    log "没有可回滚的上一版（首次部署）"
  fi
  die "后端部署失败。查日志： journalctl -u $SERVICE -n 100 --no-pager"
}

deploy_frontend() {
  [ -f "$STAGE_DIR/dist.tar.gz" ] || die "缺少产物 $STAGE_DIR/dist.tar.gz"

  log "前端：解包到临时目录"
  rm -rf "$WEB_DIR.new"
  mkdir -p "$WEB_DIR.new"
  tar -xzf "$STAGE_DIR/dist.tar.gz" -C "$WEB_DIR.new"
  # dist 必须是 index.html 直接在根，套一层 dist/ 的包会让站点 403
  [ -f "$WEB_DIR.new/index.html" ] || die "前端包结构异常：index.html 不在根目录"

  log "前端：原子切换"
  rm -rf "$WEB_DIR.old"
  if [ -d "$WEB_DIR" ]; then
    mv "$WEB_DIR" "$WEB_DIR.old"
  fi
  mv "$WEB_DIR.new" "$WEB_DIR"
  chown -R "$RUN_USER:$RUN_USER" "$WEB_DIR" 2>/dev/null || true
  rm -f "$STAGE_DIR/dist.tar.gz"
}

write_deploy_info() {
  {
    echo "commit=$GIT_SHA"
    echo "ref=$GIT_REF"
    echo "deployed_at=$(date '+%Y-%m-%d %H:%M:%S %Z')"
    echo "operator=$OPERATOR"
  } > "$APP_DIR/DEPLOY_INFO"
  log "部署记录已写入 $APP_DIR/DEPLOY_INFO"
}

[ "$DO_BACKEND"  = "1" ] && deploy_backend
[ "$DO_FRONTEND" = "1" ] && deploy_frontend
write_deploy_info
log "部署完成（commit=$GIT_SHA）"
