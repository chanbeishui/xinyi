# 发布操作手册（可执行版）

按本机环境填好的一次性配置 + 三端发布命令 + 回滚。所有命令可直接复制执行。

> 环境常量：服务器 `8.138.122.95`、部署密钥 `~/.ssh/xinyi_deploy_ed25519`、
> 生产库 `rongli_wx`、分支 `rongli-wx`、站点端口 `8081`。

---

## 0. 一次性准备（做过就跳过）

### 0.1 本机 SSH 别名（让 deploy.sh 不用带 -i）

```bash
cat >> ~/.ssh/config <<'EOF'

Host xinyi-prod
  HostName 8.138.122.95
  User root
  IdentityFile ~/.ssh/xinyi_deploy_ed25519
  IdentitiesOnly yes
EOF

# 验证：不应再问密码
ssh xinyi-prod 'hostname'
```

### 0.2 GitHub Secrets（Settings → Secrets and variables → Actions）

| Name | Value |
|---|---|
| `DEPLOY_HOST` | `8.138.122.95` |
| `DEPLOY_USER` | `root` |
| `DEPLOY_PORT` | `22` |
| `DEPLOY_SSH_KEY` | 私钥全文 |

取私钥到剪贴板（不显示）：`cat ~/.ssh/xinyi_deploy_ed25519 | clip`

### 0.3 小程序上传前置（开发者工具 CLI）

走 CLI 上传，**不需要上传密钥，也不需要 IP 白名单**，只要两项设置：

1. 开发者工具已扫码登录
2. 开发者工具 → 设置 → 安全设置 → 开启「服务端口」（CLI/HTTP 调用）

### 0.4 服务器备份脚本（每台服务器一次）

```bash
# 本机上传
scp xinyi-prod:/dev/null /dev/null 2>/dev/null; scp deploy/prod/backup-xinyi.sh xinyi-prod:/home/xinyi/bin/
ssh xinyi-prod 'chmod 700 /home/xinyi/bin/backup-xinyi.sh && /home/xinyi/bin/backup-xinyi.sh'
```

在 1panel → 计划任务 → Shell 脚本，每天 02:30 执行该脚本。

---

## 1. Web 后台发布

### 方式 A：GitHub Actions（推荐）

网页：Actions → `deploy` → Run workflow → 参数如下

| 参数 | 值 |
|---|---|
| Use workflow from | `rongli-wx` |
| 部署范围 scope | `frontend` |
| 分支 / tag / commit SHA | 留空 |
| 允许手动触发时发布后端 | 不勾 |
| 跳过后端单元测试 | 不勾 |

耗时约 1 分钟。若配了 `production` 环境审批，需点 Approve。

### 方式 B：本机一键

```bash
cd /e/codex/xinyi
XINYI_HOST=xinyi-prod ./deploy/prod/deploy.sh frontend     # 构建 + 上传 + 切换
# 只重传不构建：
XINYI_SKIP_BUILD=1 XINYI_HOST=xinyi-prod ./deploy/prod/deploy.sh frontend
```

### 验证

```bash
ssh xinyi-prod 'grep -o "index-[A-Za-z0-9_-]*\.js" /home/xinyi/web/index.html | head -1;
  curl -s -o /dev/null -w "site=%{http_code}\n" http://127.0.0.1:8081;
  curl -s -o /dev/null -w "api=%{http_code}\n" http://127.0.0.1:8081/prod-api/captchaImage;
  cat /home/xinyi/app/DEPLOY_INFO'
```

期望 `site=200`、`api=200`，`operator` 是 `github-actions/...`（走方式 B 则是 `manual`）。

---

## 2. 后端发布

### 2.1 有数据库变更时（必做，且必须最先做）

```bash
# ① 备份
ssh xinyi-prod '/home/xinyi/bin/backup-xinyi.sh'

# ② 传迁移脚本并执行（示例：新迁移文件）
scp sql/migration_YYYYMMDD_xx.sql xinyi-prod:/root/xinyi-sql/
ssh xinyi-prod 'MYSQL_C=$(docker ps --format "{{.Names}}" | grep -i mysql | head -1);
  read -rsp "MySQL root 密码: " P; echo;
  docker exec -e MYSQL_PWD="$P" -i "$MYSQL_C" mysql -uroot --default-character-set=utf8mb4 rongli_wx \
    < /root/xinyi-sql/migration_YYYYMMDD_xx.sql; unset P'

# ③ 校验（脚本内自带断言；无断言时至少跑一次结构快照）
ssh xinyi-prod 'MYSQL_C=$(docker ps --format "{{.Names}}" | grep -i mysql | head -1);
  read -rsp "MySQL root 密码: " P; echo;
  docker exec -e MYSQL_PWD="$P" -i "$MYSQL_C" mysql -uroot -N -e "
    SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=\"rongli_wx\" AND table_name LIKE \"vi\\_%\";
    SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=\"rongli_wx\" AND table_name LIKE \"QRTZ%\";"; unset P'
```

期望 `vi_* = 80`、`QRTZ_* = 11`。

### 2.2 发 jar

```bash
cd /e/codex/xinyi
XINYI_HOST=xinyi-prod ./deploy/prod/deploy.sh backend
```

内部动作：`mvn package` → scp 94MB → 旧 jar 存 `.prev` → `systemctl restart` →
轮询 `/captchaImage` 60 秒 → 失败自动回滚并退出非零。

`mvn` 不在 PATH 时：

```bash
export MVN_BIN=/e/codex/.cprep/tools/apache-maven-3.9.16/bin/mvn
```

### 2.3 验证

```bash
ssh xinyi-prod 'systemctl is-active xinyi-admin;
  curl -s -o /dev/null -w "health=%{http_code}\n" http://127.0.0.1:8080/captchaImage;
  tail -5 /home/xinyi/logs/DEPLOY_INFO 2>/dev/null; cat /home/xinyi/app/DEPLOY_INFO'
```

`active` + `health=200` 即成功。

---

## 3. 小程序发布

```bash
cd /e/codex/xinyi/xinyi-rongli-wx
npm run release:mp                              # 构建 + 上传（版本号默认 YYMMDD.1）
node scripts/upload-mp-cli.mjs --version 1.1.0 --desc "修复派单超时"   # 指定版本与描述
```

上传成功后到公众平台（人工三步）：

1. 版本管理 → 找到刚上传的开发版 → **设为体验版**
2. 成员管理 → 添加体验成员
3. 提审 → 审核通过 → 发布（**需小程序备案已完成**）

---

## 4. 回滚速查

| 端 | 命令 | 生效 |
|---|---|---|
| Web | `ssh xinyi-prod 'cd /home/xinyi && rm -rf web && mv web.old web'` | 立即 |
| 后端 | `ssh xinyi-prod 'cd /home/xinyi/app && mv xinyi-admin.jar.prev xinyi-admin.jar && systemctl restart xinyi-admin'` | ~20 秒 |
| 数据库 | `ssh xinyi-prod 'gunzip -c /home/xinyi/backup/xinyi_YYYY-MM-DD_HHMM.sql.gz \| docker exec -i $(docker ps -qf "name=mysql") mysql -uroot -p*** rongli_wx'` | 视数据量 |
| 小程序 | 公众平台 → 版本管理 → 回退到上一线上版本 | 立即 |

---

## 5. 发布前检查清单

- [ ] `git status` 干净，改动已提交并推送
- [ ] 若改动了 `xinyi-ui/**`，前面板已构建通过（Actions 会自动验证）
- [ ] 若改动了 `sql/**`，迁移脚本已按顺序执行并校验
- [ ] 若改动了 `xinyi-*/**`，`mvn test` 通过（CI 的 `backend-build` 会跑）
- [ ] 服务器磁盘剩余 > 5G（`ssh xinyi-prod 'df -h /'`）
- [ ] 发布后三端验证：网页能登录、`/prod-api` 通、小程序能进首页

---

## 6. 常见故障

| 现象 | 原因 | 处理 |
|---|---|---|
| Actions 报 `stat local "22"` | scp 端口参数写错 | 已修，确保 workflow 是最新版 |
| Actions 报 `Permission denied (publickey)` | Secret 里贴了 `.pub` 或换行丢失 | 重贴私钥全文 |
| Actions 超时连不上服务器 | 阿里云防火墙 22 未对公网放行 | 放行 `22 / 0.0.0.0/0` |
| 后端起不来，`journalctl` 报 Access denied | `.env` 里库密码与容器实际不一致 | 改 `/home/xinyi/app/.env` 后重启 |
| 网页 403 | `/home/xinyi` 权限不是 751 | `chmod 751 /home/xinyi` |
| 前端白屏 | 部署包多套了一层目录 | 重新 `deploy.sh frontend`，脚本会校验 `index.html` 在根 |
| 小程序请求失败 | 未开「不校验合法域名」 | 开发者工具详情 → 本地设置勾选；真机需「打开调试」 |
