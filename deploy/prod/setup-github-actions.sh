#!/usr/bin/env bash
# =====================================================================
# 一次性配置 GitHub Actions 部署（前端走流水线，后端留本机脚本）
#
# 用法（Windows Git Bash / Linux / macOS）：
#   bash deploy/prod/setup-github-actions.sh root@8.138.122.95
#
# 做四件事：
#   1) 生成专用部署密钥（放 ~/.ssh，不进仓库）
#   2) 把公钥追加到服务器 authorized_keys（幂等，需要输一次服务器密码）
#   3) 验证密钥能免密登录
#   4) 若本机装了 gh 且已登录，直接把 Secrets 写进仓库；否则打印待执行的命令
#
# 之后仍需在 GitHub 网页做两件事（脚本会提示）：
#   · Settings → Environments → 新建 production 并加 Required reviewers
#   · Settings → Branches → rongli-wx 勾选 backend / frontend 为必需检查（分支名连字符，别和库名 rongli_wx 混）
# =====================================================================
set -euo pipefail

TARGET=${1:-}
if [ -z "$TARGET" ]; then
  echo "用法: $0 root@服务器IP" >&2
  exit 1
fi

KEY_FILE=${KEY_FILE:-$HOME/.ssh/xinyi_deploy_ed25519}
HOST_ONLY=${TARGET#*@}
USER_ONLY=${TARGET%@*}
[ "$USER_ONLY" = "$TARGET" ] && USER_ONLY=root

C_OK=$'\033[32m'; C_INF=$'\033[36m'; C_BAD=$'\033[31m'; C_END=$'\033[0m'
info() { printf '%s[ .. ]%s %s\n' "$C_INF" "$C_END" "$*"; }
ok()   { printf '%s[ OK ]%s %s\n' "$C_OK" "$C_END" "$*"; }
bad()  { printf '%s[FAIL]%s %s\n' "$C_BAD" "$C_END" "$*"; }

# ---------------------------------------------------------------- 1 密钥
info "1/4 准备部署密钥 $KEY_FILE"
mkdir -p "$(dirname "$KEY_FILE")"
chmod 700 "$(dirname "$KEY_FILE")"
if [ -f "$KEY_FILE" ]; then
  ok "已存在，复用（删除该文件可重新生成）"
else
  ssh-keygen -t ed25519 -N "" -C "github-actions-deploy@xinyi" -f "$KEY_FILE" >/dev/null
  chmod 600 "$KEY_FILE"
  ok "已生成"
fi
PUB=$(cat "$KEY_FILE.pub")

# ---------------------------------------------------------------- 2 安装公钥
info "2/4 写入服务器 authorized_keys（此时会要一次密码）"
ssh "$TARGET" "mkdir -p ~/.ssh && chmod 700 ~/.ssh && touch ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
if ssh "$TARGET" "grep -qF '$PUB' ~/.ssh/authorized_keys" 2>/dev/null; then
  ok "公钥已存在，跳过"
else
  printf '%s\n' "$PUB" | ssh "$TARGET" "cat >> ~/.ssh/authorized_keys"
  ok "公钥已追加"
fi

# ---------------------------------------------------------------- 3 验证
info "3/4 验证免密登录"
if ssh -i "$KEY_FILE" -o BatchMode=yes -o ConnectTimeout=10 "$TARGET" 'echo ready' >/dev/null 2>&1; then
  ok "密钥登录可用"
else
  bad "密钥登录失败，检查服务器 sshd 是否允许公钥、~/.ssh 权限是否为 700"
  exit 1
fi

# ---------------------------------------------------------------- 4 Secrets
info "4/4 写入仓库 Secrets"
if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
  REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)
  if [ -n "$REPO" ]; then
    gh secret set DEPLOY_HOST --body "$HOST_ONLY" --repo "$REPO"
    gh secret set DEPLOY_USER --body "$USER_ONLY" --repo "$REPO"
    gh secret set DEPLOY_SSH_KEY --repo "$REPO" < "$KEY_FILE"
    gh secret set DEPLOY_PORT --body "22" --repo "$REPO"
    ok "已写入 $REPO 的 4 个 Secrets"
  else
    bad "无法识别当前仓库，请手动配置"
  fi
else
  echo
  echo "本机没有可用的 gh CLI，请到 Settings → Secrets and variables → Actions 手动添加："
  echo "  DEPLOY_HOST    = $HOST_ONLY"
  echo "  DEPLOY_USER    = $USER_ONLY"
  echo "  DEPLOY_PORT    = 22"
  echo "  DEPLOY_SSH_KEY = 下面文件的全部内容（含首尾 ----- 行）"
  echo "                   $KEY_FILE"
  echo
  echo "或安装 gh 后执行："
  echo "  gh secret set DEPLOY_HOST --body '$HOST_ONLY'"
  echo "  gh secret set DEPLOY_USER --body '$USER_ONLY'"
  echo "  gh secret set DEPLOY_SSH_KEY < '$KEY_FILE'"
  echo "  gh secret set DEPLOY_PORT --body '22'"
fi

cat <<EOF

==== 还剩两步要在 GitHub 网页完成 ====
1) Settings → Environments → New environment → 名称填 production → 勾 Required reviewers 选自己
2) Settings → Branches → rongli-wx → 勾选 backend / frontend 为必需检查

==== 安全提醒 ====
· 私钥在 $KEY_FILE，权限 600，不要提交进仓库，也不要贴进聊天
· 既已开启密钥部署，建议关闭密码登录：
    sed -i 's/^#*PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
    systemctl restart ssh   （先在另一个窗口确认密钥能登录，别把自己锁外面）
· 该密钥只用于部署，泄露后从 ~/.ssh/authorized_keys 删掉对应行即可吊销

==== 日常用法 ====
  前端：Actions → deploy → Run workflow（默认 scope=frontend）
  后端：本机 ./deploy/prod/deploy.sh backend
  发版：git tag v1.2.3 && git push origin v1.2.3
EOF
