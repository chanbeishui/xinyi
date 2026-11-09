#!/usr/bin/env bash
# =====================================================================
# 本地一键发布：打包 → 上传产物 → 调用服务器端原子部署脚本
#
# 与 CI 共用同一份部署逻辑（deploy/prod/remote-deploy.sh），
# 所以「手工发」和「流水线发」行为完全一致：备份 → 切换 → 重启 → 健康检查 → 失败回滚。
#
# 用法（仓库根目录，Git Bash / WSL / Linux / macOS 均可）：
#   ./deploy/prod/deploy.sh all          # 前后端都发
#   ./deploy/prod/deploy.sh backend
#   ./deploy/prod/deploy.sh frontend
#   ./deploy/prod/deploy.sh build        # 只打包，不上传
#
# 必需：export XINYI_HOST=root@1.2.3.4
# 可选：XINYI_APP_DIR / XINYI_WEB_DIR / XINYI_STAGE_DIR / MVN_BIN / NPM_BIN
# =====================================================================
set -euo pipefail

TARGET=${XINYI_HOST:-}
APP_DIR=${XINYI_APP_DIR:-/home/xinyi/app}
WEB_DIR=${XINYI_WEB_DIR:-/home/xinyi/web}
STAGE_DIR=${XINYI_STAGE_DIR:-/tmp/xinyi-deploy}
SERVICE=${XINYI_SERVICE:-xinyi-admin}
JAR_NAME=xinyi-admin.jar
DO_BACKEND=0
DO_FRONTEND=0
MVN_BIN=${MVN_BIN:-mvn}
NPM_BIN=${NPM_BIN:-npm}

ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
UI_DIR="$ROOT_DIR/xinyi-ui"
# 前端产物目录可覆盖：默认 xinyi-ui/dist；想复用 dist-verify-* 就设 XINYI_UI_DIST
UI_DIST=${XINYI_UI_DIST:-$UI_DIR/dist}
JAR_PATH="$ROOT_DIR/xinyi-admin/target/$JAR_NAME"
REMOTE_SCRIPT="$ROOT_DIR/deploy/prod/remote-deploy.sh"
START_TS=$(date +%s)

info() { printf '\033[36m== %s\033[0m\n' "$*"; }
ok()   { printf '\033[32mOK  %s\033[0m\n' "$*"; }
die()  { printf '\033[31mERR %s\033[0m\n' "$*" >&2; exit 1; }

require_target() {
  [ -n "$TARGET" ] || die "未设置 XINYI_HOST，例如：XINYI_HOST=root@1.2.3.4 $0 $*"
  [ -f "$REMOTE_SCRIPT" ] || die "缺少 $REMOTE_SCRIPT"
}

build_backend() {
  info "打包后端（跳过测试，测试请在提交前跑）"
  ( cd "$ROOT_DIR" && "$MVN_BIN" -q -pl xinyi-admin -am package -DskipTests ) || die "后端打包失败"
  [ -f "$JAR_PATH" ] || die "未找到 $JAR_PATH"
  ok "jar $(du -h "$JAR_PATH" | cut -f1)"
}

build_frontend() {
  info "打包前端（production）"
  ( cd "$UI_DIR" && "$NPM_BIN" run build:prod ) || die "前端打包失败"
  [ -f "$UI_DIST/index.html" ] || die "$UI_DIST/index.html 不存在"
  ok "dist $(du -sh "$UI_DIST" | cut -f1)"
}

# XINYI_SKIP_BUILD=1：跳过构建直接发已有产物，改完 CSS 反复调试时省 20 多秒
ensure_backend() {
  if [ "${XINYI_SKIP_BUILD:-0}" = "1" ]; then
    info "跳过构建（XINYI_SKIP_BUILD=1）"
    [ -f "$JAR_PATH" ] || die "没有可用的 jar：$JAR_PATH"
  else
    build_backend
  fi
}

ensure_frontend() {
  if [ "${XINYI_SKIP_BUILD:-0}" = "1" ]; then
    info "跳过构建（XINYI_SKIP_BUILD=1）"
    [ -f "$UI_DIST/index.html" ] || die "没有可用的 dist：$UI_DIST"
  else
    build_frontend
  fi
}

run_remote() {
  require_target
  local sha
  sha=$(cd "$ROOT_DIR" && git rev-parse --short HEAD 2>/dev/null || echo unknown)

  info "准备暂存目录 $TARGET:$STAGE_DIR"
  ssh "$TARGET" "mkdir -p $STAGE_DIR"

  if [ "$DO_BACKEND" = "1" ]; then
    info "上传 jar"
    scp -q "$JAR_PATH" "$TARGET:$STAGE_DIR/$JAR_NAME" || die "jar 上传失败"
  fi
  if [ "$DO_FRONTEND" = "1" ]; then
    info "上传 dist（tar 流式，比 scp 几千小文件快）"
    tar -czf - -C "$UI_DIST" . | ssh "$TARGET" "cat > $STAGE_DIR/dist.tar.gz" || die "dist 上传失败"
  fi

  info "执行服务器端部署（健康检查失败会自动回滚）"
  ssh "$TARGET" "STAGE_DIR=$STAGE_DIR APP_DIR=$APP_DIR WEB_DIR=$WEB_DIR SERVICE=$SERVICE \
    DO_BACKEND=$DO_BACKEND DO_FRONTEND=$DO_FRONTEND GIT_SHA=$sha GIT_REF=manual \
    OPERATOR=$(whoami)@$(hostname -s 2>/dev/null || echo local) bash -s" \
    < "$REMOTE_SCRIPT" || die "部署失败（服务器已完成回滚，查看上方日志）"
}

action=${1:-all}
case "$action" in
  build)
    build_backend; build_frontend ;;
  backend)
    DO_BACKEND=1; ensure_backend; run_remote ;;
  frontend)
    DO_FRONTEND=1; ensure_frontend; run_remote ;;
  all)
    DO_BACKEND=1; DO_FRONTEND=1; ensure_backend; ensure_frontend; run_remote ;;
  *)
    die "未知参数：$action（可选 all|backend|frontend|build）" ;;
esac

ok "完成：$action，耗时 $(( $(date +%s) - START_TS )) 秒"
