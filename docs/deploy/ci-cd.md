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
feature/xxx  ──push──▶  backend + frontend  ──▶  main（受保护）  ──▶  tag v1.2.3
                           全绿才能合并                  │                或手动触发 deploy
                                                                        │
                                                          ⑤ 构建（含测试）──▶ ⑥ 上传暂存
                                                                        │
                                              ⑦ 原子部署：备份→切换→重启→健康检查
                                                        │
                                          成功 → 写 DEPLOY_INFO   失败 → 自动回滚
```

**分支模型（保持简单）**

- `main`：随时可发布，**开启分支保护**：禁止直接 push，必须 PR，必须通过 `backend` / `frontend` 检查
- `feature/xxx`、`fix/xxx`：短生命周期，天级合并
- `rongli-wx`：小程序联调分支，现有 CI 已覆盖
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
2. Settings → Branches → `main` 保护规则，勾选 `backend` / `frontend` 为必需检查

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
ssh root@服务器IP 'docker exec -i 1Panel-mysql-XXXX mysql -uroot -p"密码" xinyi' < sql/migration_20261104_01_xxx.sql
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
- **镜像化**：把 jar 打进镜像，配合 1Panel 的应用管理，回滚变成切 tag
- **构建产物归档**：tag 发布时把 jar + dist 挂到 GitHub Release，方便只回滚不重建
