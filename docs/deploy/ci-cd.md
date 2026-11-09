# CI/CD 标准流程

面向本仓库（Spring Boot 多模块 + Vue3 后台 + uni-app 小程序），基于 **GitHub Actions**。

一句话原则：**CI 负责"能不能发"，CD 负责"怎么安全地发"，人只负责决定"什么时候发"。**

---

## 1. 现在有哪些流水线

| 工作流 | 触发 | 做什么 | 拦截什么 |
|---|---|---|---|
| `backend.yml` | push（master/main/rongli-wx）、PR、手动 | JDK 17 编译 → **单元测试** → 打包 → 上传 jar；`sql-check` 用 MySQL 8 服务容器跑 `logs/ci_sql_check.sh`；`scripts` 做 Python 语法检查 | 编译不过、测试回归、迁移脚本不能重复执行、缺表 |
| `frontend.yml` | push、PR、手动 | 断言 `package-lock.json` 已入库 → `npm ci` → `build:prod` → 上传 dist | 锁文件缺失、前端构建失败 |
| `deploy.yml`（新增） | tag `v*` 自动 / 手动触发 | JDK 17 + Node 20 构建 → 上传产物到服务器暂存 → 执行服务器端原子部署 → 健康检查 → 失败自动回滚 → 写部署记录 | 部署失败、发版后服务不可用 |

`sql-check` 这次补了一处真实缺口：之前只跑 `ry_20260417.sql` + `migration_*.sql`，
**从未建过 `QRTZ_*` 表**，所以「定时任务」模块的缺表问题在 CI 里永远是绿的。
现在脚本会执行 `sql/quartz.sql` 并断言 `QRTZ_*` 表数 = 11，`docker-compose.yml` 也补了挂载。

---

## 2. 全流程

```
① 开发                    ② PR 门禁                ③ 合并                ④ 发布
feature/xxx  ──push──▶  backend + frontend  ──▶  rongli-wx（受保护）  ──▶  tag v1.2.3
                           全绿才能合并                  │                或手动触发 deploy
                                                                        │
                                                          ⑤ 构建（含测试）──▶ ⑥ 上传暂存
                                                                        │
                                              ⑦ 原子部署：备份→切换→重启→健康检查
                                                        │
                                          成功 → 写 DEPLOY_INFO   失败 → 自动回滚
```

**分支模型（保持简单）**

- **`rongli-wx`（当前发布分支）**：随时可发布，**必须开启分支保护**：禁止直接 push，走 PR，必须通过 `backend` / `frontend` 检查
- `main`：历史主线，保持可构建即可；若将来切到 main 发布，把保护规则一起搬过去
- `feature/xxx`、`fix/xxx`：短生命周期，天级合并
- 注意区分：分支是 `rongli-wx`（连字符），生产库是 `rongli_wx`（下划线），两者无关
- 提交粒度按 `AGENTS.md`：**一次提交只做一件事**，fix 必须写清复现条件与根因

**版本号**

- 用 tag 表达发布版本：`v1.2.3`（语义化），或按日期 `v2026.11.04-1`
- tag 推上去即自动发布；不想自动发就只用手动触发
- 服务器 `$APP_DIR/DEPLOY_INFO` 记录 commit / 分支 / 时间 / 操作人，排查"线上到底是哪版"直接看这个文件

---

## 3. 部署流水线怎么用

**方式一：手动（推荐日常）**

Actions → `deploy` → Run workflow，参数：

| 参数 | 说明 |
|---|---|
| `scope` | `all` / `backend` / `frontend`，只改前端就别发后端 |
| `ref` | 分支、tag 或 commit SHA，留空用当前提交 |
| `skip_tests` | 仅紧急修复时勾选，勾了就等于放弃这道门禁 |

**方式二：打 tag 自动发**

```bash
git tag v1.2.3 && git push origin v1.2.3
```

**首次接入要配的东西**

Secrets（Settings → Secrets and variables → Actions）：

| 名称 | 内容 | 怎么来 |
|---|---|---|
| `DEPLOY_HOST` | 服务器 IP / 域名 | — |
| `DEPLOY_USER` | 部署账号 | 建议 `root` 或能免密 `systemctl` 的账号 |
| `DEPLOY_SSH_KEY` | 私钥全文 | `ssh-keygen -t ed25519 -C deploy -f deploy_key`，公钥追加到服务器 `~/.ssh/authorized_keys` |
| `DEPLOY_PORT` | SSH 端口，可选 | 默认 22 |

Variables（可选，覆盖默认路径）：`DEPLOY_APP_DIR`（默认 `/home/xinyi/app`）、`DEPLOY_WEB_DIR`（默认 `/home/xinyi/web`）。

还需两步人工配置：

1. Settings → Environments → 新建 `production`，加 **Required reviewers**，这样点下 Run workflow 后仍需审批，防误发
2. Settings → Branches → `rongli-wx` 保护规则，勾选 `backend` / `frontend` 为必需检查

**网络注意**：GitHub 托管的 runner 要 SSH 连到你的服务器，阿里云控制台防火墙必须放行 22（建议只允许密钥登录 + 装 fail2ban）。
如果不想对公网开 22，改用**自托管 runner**（把 runner 装在内网机器上），工作流文件不用改。

---

## 4. 服务器端到底做了什么（`deploy/prod/remote-deploy.sh`）

CI 和本地脚本**共用同一份逻辑**，不存在"手工发和流水线发行为不一致"的问题。

后端：

1. 备份当前 jar → `xinyi-admin.jar.prev`
2. 新 jar 就位 → `systemctl restart xinyi-admin`
3. 轮询 `http://127.0.0.1:8080/captchaImage`，最多 60 秒
4. 超时 → **自动 `mv .prev` 回去并重启**，脚本以非零退出，流水线标红

前端：

1. 解包到 `web.new`
2. **校验 `web.new/index.html` 存在**（防止上传时多套一层 `dist/` 导致整站 403）
3. 旧目录挪 `web.old` → `web.new` 改名 `web`（`mv` 原子操作，切换窗口毫秒级）

顺序很关键：**先后端、后前端**。后端没过健康检查就**不动前端**，避免出现"新后端 + 旧前端"的半吊子状态。

---

## 5. 数据库迁移策略（这条最容易被做成事故）

**CI 只验证，生产手动执行。**

- CI：起一个空 MySQL 8，跑一遍全量脚本 + 再跑一遍验证幂等 + 结构断言（`vi_*` ≥ 80、`QRTZ_*` = 11、菜单分组）
- 生产：**不自动跑**。理由：CI 里的库是空的，生产库有数据；DDL 不可回滚，必须配一次备份窗口

发布顺序固定为：

```
① 先执行迁移脚本（必须向前兼容：只加不删、新列可空）
② 再发后端（新代码兼容新旧两种表结构）
③ 最后发前端
```

任何一步失败，前一步都不会造成"代码与库不匹配"。

配套约定（`AGENTS.md` 已要求）：

- 每个结构变更都有 `sql/migration_YYYYMMDD_*.sql`
- 同时更新 `sql/ry_20260417.sql`，保证新环境一次初始化到最新
- 删字段/删表/类型收窄要拆成两次发布：先停用，再删

需要执行迁移时：

```bash
# 先备份（别跳）
/home/xinyi/bin/backup-xinyi.sh
# 再执行（容器内 mysql 客户端）
ssh root@服务器IP 'docker exec -i 1panel-mysql-XXXX mysql -uroot -p"密码" rongli_wx' < sql/migration_20261104_01_xxx.sql
# 再校验（脚本里的断言 SQL 或 ci_sql_check.sh 的结构段）
```

---

## 6. 回滚（三层，都是分钟级）

| 层 | 操作 | 生效时间 |
|---|---|---|
| 后端 | `cd /home/xinyi/app && mv xinyi-admin.jar.prev xinyi-admin.jar && systemctl restart xinyi-admin` | ~20 秒 |
| 前端 | `cd /home/xinyi && rm -rf web && mv web.old web` | 立即 |
| 数据库 | 恢复最近一次备份（`gunzip -c ... \| mysql ...`） | 取决于库大小 |
| 小程序 | 微信后台回退到上一线上版本 | 立即 |

流水线在健康检查失败时会**自动执行后端回滚**，不需要人守着。

---

## 7. 环境矩阵

| 环境 | 地址 | 用途 | 谁来发 |
|---|---|---|---|
| 本地 | `localhost:8080` + `npm run dev` | 日常开发 | 开发者 |
| 服务器联调 | `http://IP:8081`（备案前） | 验收、真机、演示 | 手动 `deploy.sh` 或工作流 |
| 生产 | `https://域名`（备案后） | 正式业务 | 仅工作流（带审批） |

同一个 jar 跑三个环境，差异只在 `config/application-*.yml` 与启动参数，**不重新打包**。

---

## 8. 常见失败

| 现象 | 原因 | 处理 |
|---|---|---|
| 健康检查超时后自动回滚 | 新代码启动失败（配置/依赖/SQL 不兼容） | `journalctl -u xinyi-admin -n 100` 看首个异常 |
| `Permission denied (publickey)` | Secret 私钥不对 / 公钥没进 `authorized_keys` | 本地用同一把钥匙手工 `ssh` 验证 |
| `Connection timed out` | 防火墙没放行 22 | 阿里云防火墙放行 runner 来源 |
| 前端部署后白屏 | 上传包多套了一层目录 | 脚本已拦（`index.html` 校验），按报错重打 zip |
| 两次发布互相覆盖 | 并发部署 | `concurrency: deploy-production` 已串行化 |
| 发版后用户仍看到旧页面 | `index.html` 被缓存 | Nginx 已对 `index.html` 设 `no-store` |
| 工作流一直是"排队"状态 | 需要审批（production environment） | 到 Actions 页面点 Approve |

---

## 9. 还没做、建议排期

- **小程序构建纳入 CI**：`xinyi-rongli-wx` 目前完全没有流水线覆盖，建议加 `npm ci && npm run build:mp-weixin` 的构建校验
- **集成测试**：现有 2 个用例都是纯单元测试；后续可用 MySQL 服务容器 + Testcontainers 覆盖数据权限边界（`AGENTS.md` 要求的那几类场景）
- **灰度/蓝绿**：2C4G 起不了双实例，当前"原地替换 + 自动回滚"是这台机器的合理上限；换 4C8G 后可考虑蓝绿
- **镜像化**：把 jar 打进镜像，配合 1panel 的应用管理，回滚变成切 tag
- **构建产物归档**：tag 发布时把 jar + dist 挂到 GitHub Release，方便只回滚不重建

---

## 附：当前采用的发布分工（2026-11 起生效）

跨境链路的现实：GitHub runner 在境外，前端产物 4.5MB、后端 jar 94MB。
所以**不追求全自动，而是按体积分工**。

| 内容 | 走哪条 | 触发方式 | 耗时 |
|---|---|---|---|
| 前端 `dist` | GitHub Actions `deploy.yml` | Actions 页面点运行（默认 `scope=frontend`） | ~1 分钟 |
| 后端 `jar` | 本机 `deploy/prod/deploy.sh backend` | 命令行 | ~60 秒 |
| 全量发版 | tag 触发 `deploy.yml`（`v*`） | `git push origin v1.2.3` | ~3 分钟 |
| 数据库迁移 | 人工 | 备份 → 执行 → 校验 | — |

### 手动触发为什么默认只发前端

`deploy.yml` 的 `scope` 默认值是 `frontend`；若手动选了 `backend` 或 `all`，
必须同时勾选 `allow_backend`，否则流水线在「校验部署范围」这步直接失败并提示改用本机脚本。
tag 触发（发版）不受此限制。

### 一次性配置

```bash
bash deploy/prod/setup-github-actions.sh root@服务器IP
```

脚本会：生成专用部署密钥（放 `~/.ssh`，不进仓库）→ 写入服务器 `authorized_keys` →
验证免密 → 有 `gh` 时自动写 4 个 Secrets，没有则打印待执行命令。

之后在 GitHub 网页完成两件事：

1. Settings → Environments → 新建 `production`，勾 **Required reviewers**（防误发）
2. Settings → Branches → `rongli-wx`，把 `backend` / `frontend` 设为必需检查

### 关于 22 端口

Actions 需要 SSH 到服务器，22 必须对公网可达。既然开了密钥部署，就关掉密码登录并上 fail2ban：

```bash
sed -i 's/^#*PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
systemctl restart ssh          # 先在另一窗口确认密钥能登，再执行
apt install -y fail2ban && systemctl enable --now fail2ban
```

### 后续可选：自托管 runner

如果有一天后端也想走流水线，或跨境太慢，把 runner 注册到这台服务器（或同地域一台小机器）：
代码 checkout 走 GitHub、构建与发布全在本机，跨境只传源码。工作流文件不用改。

---

## 附：三端发布流程

> 逐条可执行的命令见 `docs/deploy/release-playbook.md`。

| 端 | 产物 | 触发方式 | 耗时 | 回滚手段 |
|---|---|---|---|---|
| Web 后台 | `dist` 9.9MB | Actions 点一下（或本机 `deploy.sh frontend`） | ~1 分钟 | `mv web.old web` |
| 后端 | `jar` 94MB | 本机 `deploy.sh backend` | ~60 秒 | `mv xinyi-admin.jar.prev xinyi-admin.jar` |
| 小程序 | `dist/build/mp-weixin` | 本机 `npm run release:mp` + 后台设体验版 | ~1 分钟 | 后台回退到上一线上版本 |

### 后端

后端**不交给 GitHub Actions**：jar 94MB 跨境传输不稳定，且发后端通常伴随排障（看日志、回滚），
本机一条命令更快。工作流里保留了 `allow_backend` 开关，确需流水线发布时可用。

标准流程（顺序不能乱）：

```bash
# 1) 有数据库变更时，先备份再执行迁移，再校验
/home/xinyi/bin/backup-xinyi.sh
docker exec -i "$MYSQL_C" mysql -uroot rongli_wx < sql/migration_YYYYMMDD_xx.sql

# 2) 发布 jar（构建 → 上传 → 备份旧 jar → 重启 → 健康检查 60s → 失败自动回滚）
XINYI_HOST=xinyi-prod ./deploy/prod/deploy.sh backend

# 3) 验证
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/captchaImage   # 200
```

迁移先行的理由：新代码要能同时兼容"迁移前"和"迁移后"的表结构；
先发代码再迁移，中间窗口内新代码访问旧结构会报错。

手工回滚：

```bash
cd /home/xinyi/app && mv xinyi-admin.jar.prev xinyi-admin.jar && systemctl restart xinyi-admin
```

### 小程序

上传必须满足两个条件，且都只能在本机做（Actions 出口 IP 不固定，加不了白名单）：

1. 公众平台 → 开发管理 → 开发设置 → **小程序代码上传密钥**（生成并下载）
2. 同一页把本机公网出口 IP 加进 **IP 白名单**

```bash
cd xinyi-rongli-wx
# 密钥放 private.upload.key（已 gitignore）
npm run release:mp                       # 构建 + 上传
MP_VERSION=1.2.0 npm run release:mp      # 指定版本号
```

上传后仍是**开发版**，需人工在公众平台操作：

| 步骤 | 在哪做 | 能否自动化 |
|---|---|---|
| 上传代码 | `npm run release:mp` | ✅ 已脚本化 |
| 设为体验版 | 公众平台 → 版本管理 | ❌ 平台不支持 API |
| 提交审核 | 公众平台 → 版本管理 | ❌ 需人工确认类目与隐私协议 |
| 发布上线 | 公众平台 | ❌ 且要求**小程序备案已完成** |

所以小程序的"发布"只有上传这一步能自动化，后三步是平台强制的管理动作。

### 三端的发布顺序

改一处牵动多端时，按「迁移 → 后端 → 前端 → 小程序」推进：
迁移向后兼容、后端兼容新旧结构、前端与小程序只依赖接口，任一步失败都不会留下不匹配状态。
