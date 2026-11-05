# 1Panel 部署手册（阿里云轻量 2 核 4G · 数据库用面板托管）

面向环境：阿里云轻量应用服务器，2 核 4G，系统 **Ubuntu 22.04 LTS**。
形态：前端静态站 + 后端 jar 常驻 + MySQL 8 / Redis 7 由 1Panel 应用商店托管。
配套文件：`deploy/prod/`（`application-prod.yml`、`xinyi.env.example`、`xinyi-admin.service`、`nginx-xinyi.conf`、`backup-xinyi.sh`）。
上线总流程（含备案与小程序发布的依赖关系、切换清单）见 `docs/deploy/go-live-runbook.md`。

> 为什么后端不放进容器：jar 用 systemd 跑，日志、堆栈、重启都最好排查；数据库在容器里通过宿主机发布端口（`127.0.0.1:3306`）访问，完全够用，也不必处理容器网络。

---

## 0. 2C4G 的内存分配基线

| 组件 | 建议占用 | 说明 |
|---|---|---|
| JVM（后端） | ≤ 1.5G | `-Xmx1536m`，见 systemd 单元 |
| MySQL 容器 | ~1G | `innodb_buffer_pool_size=768M` |
| Redis 容器 | ~150M | `maxmemory 256mb` |
| OpenResty + 1Panel | ~350M | |
| 系统 + 其他 | ~400M | |
| **swap** | 2G | 必须有，防 OOM Killer 杀 MySQL |

4G 内存跑「Java + MySQL」是紧平衡，**不要**再把 MySQL buffer pool 调到 1G 以上，也不要给 JVM 超过 2G。

---

## 1. 系统准备

```bash
# 时区：数据库、日志、令牌过期全依赖它
timedatectl set-timezone Asia/Shanghai

# 更新 + 基础工具
apt update && apt upgrade -y
apt install -y curl vim unzip tar

# 加 2G swap（轻量服务器默认无 swap）
fallocate -l 2G /swapfile && chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
swapon --show

# 确认内存空闲（至少留 3G 可用再继续）
free -h
```

**阿里云控制台 → 防火墙**只需要放行：`22`、`80`、`443`、1Panel 面板端口。`3306 / 6379 / 8080` **一律不放行**。

---

## 2. 安装 1Panel 并加固

按官网脚本安装后，进面板第一件事：

1. 改**面板端口**（默认 10086 很容易被扫）
2. 设置**安全入口**（随机路径）
3. 开启**面板 SSL**（自签即可，浏览器信任一次）
4. 开启**登录二次验证**

面板本身暴露公网就是最大的攻击面，这四步别省。

---

## 3. 应用商店安装三个组件

「应用商店」里安装，安装时注意参数：

| 应用 | 关键参数 | 2C4G 建议 |
|---|---|---|
| **OpenResty** | — | 默认即可，占用小 |
| **MySQL** | 版本选 **8.0**；端口 `3306`；root 密码设强密码 | 装完改 `my.cnf`（见下） |
| **Redis** | 端口 `6379`；**必须设密码**；`maxmemory 256mb`；淘汰策略 `allkeys-lru` | 默认 |

> **MySQL 必须是 8.0**：基础 SQL 用了 `utf8mb4_0900_ai_ci` 排序规则，5.7 不支持，导入会直接报错。

装完 MySQL 后改配置（应用详情 → 参数/配置文件 `my.cnf`，改完重启应用）：

```ini
innodb_buffer_pool_size = 768M
max_connections = 150
innodb_flush_log_at_trx_commit = 1
slow_query_log = 1
long_query_time = 2
character-set-server = utf8mb4
collation-server = utf8mb4_0900_ai_ci
default-time-zone = '+08:00'
```

> `default-time-zone` 一定要设。面板默认容器是 UTC，而后端连接串是 `Asia/Shanghai`，两边不一致会出现「订单时间差 8 小时」。

### 端口暴露的安全提醒

Docker 发布的端口**会绕过 ufw/firewalld**，宿主机防火墙拦不住。所以：

- MySQL / Redis 的端口绑定尽量限制在 `127.0.0.1`（应用参数里能改就改）
- 最终防线是**阿里云控制台的防火墙**，那里不放行就进不来，这是唯一可靠的一层

---

## 4. 数据库初始化

### 4.1 建库建账号

1Panel → 数据库 → MySQL → 创建数据库：

- 数据库名：`xinyi`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_0900_ai_ci`
- 同时创建用户 `xinyi`（**不要用 root 跑业务**），并授予该库全部权限

### 4.2 导入脚本（顺序不能乱）

`sql/` 目录按下面顺序逐个导入。基础脚本自身**不带** `CREATE DATABASE / USE`，执行时必须指定库名。

**推荐用容器内 mysql 客户端批量跑**（比面板逐个上传快，也不受上传大小限制）：

```bash
MYSQL_CONTAINER=$(docker ps --format '{{.Names}}' | grep -i mysql | head -1)
echo "容器: $MYSQL_CONTAINER"

cd /home/xinyi/sql
for f in ry_20260417.sql quartz.sql migration_*.sql; do
  echo "==> $f"
  docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p'ROOT密码' xinyi < "$f" || { echo "失败: $f"; break; }
done
```

顺序含义：

| # | 文件 | 说明 |
|---|---|---|
| 1 | `ry_20260417.sql` | 全量表结构 + 菜单 + 字典 + 超管账号 |
| 2 | `quartz.sql` | **必须单独导入**：基础脚本不含 `QRTZ_*` 表，缺了「定时任务」模块直接报错 |
| 3 | `migration_20260804_digital_twin_menu.sql` | 按文件名时间顺序全部执行 |
| 4 | `migration_20260915_data_screen_menu.sql` | |
| 5–7 | `migration_20261021_01 ~ 03` | 荣利业务表、菜单字典、后台页面 |
| 8–13 | `migration_20261022_01 ~ 06` | 菜单分组、统计权限、治理、轮次回放、主部门、支付短信 |
| 14–19 | `migration_20261023_01 ~ 06` | 第三方集成配置、身份 V6.4 对齐、支付身份 |
| 20–23 | `migration_20261103_01 ~ 04` | 路由名唯一、退役旧配置页、清理验收残留、业务配置编辑权 |
| 24 | `migration_multi_dept_authorization.sql` | 多部门任职与安全审计，**最后执行** |

### 4.3 生产环境不要导入的文件

| 文件 | 原因 |
|---|---|
| `seed_20261021_rongli_demo_accounts.sql` | 联调/演示账号，脚本自带说明「生产环境不要执行」 |
| `seed_20261022_rongli_reconcile_demo.sql` | 支付核对页演示数据，生产导入会污染治理台账 |

> `migration_20261103_03_purge_acceptance_integration_drafts.sql` 是清理历史 Bug 残留，可以正常执行。

### 4.4 导入后校验

```sql
-- 应为 80（荣利业务表 + 本库保留的 vi_sms_send_log）
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema='xinyi' AND table_name LIKE 'vi\_%';

-- 定时任务表存在（11 张）
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema='xinyi' AND table_name LIKE 'QRTZ%';

-- 排序规则没有跑偏
SELECT table_collation, COUNT(*) FROM information_schema.tables
WHERE table_schema='xinyi' GROUP BY table_collation;

-- 超管账号在
SELECT user_id, user_name, status FROM sys_user WHERE user_name='admin';
```

---

## 5. 后端部署

### 5.1 目录规划

```
/home/xinyi/
├── app/
│   ├── xinyi-admin.jar
│   ├── .env                      # 600 权限，含数据库/Redis 密码
│   └── config/application-prod.yml
├── upload/                       # ruoyi.profile 指向这里
├── logs/                         # LOG_PATH
├── web/                          # 前端 dist
├── sql/                          # 迁移脚本留档
└── backup/
```

### 5.2 安装 JDK 与建用户

```bash
apt install -y openjdk-17-jre-headless
java -version        # 需 17.x

# 专用运行用户，不用 root 跑业务
useradd -r -m -d /home/xinyi -s /usr/sbin/nologin xinyi
mkdir -p /home/xinyi/{app,app/config,upload,logs,web,backup,bin}
chown -R xinyi:xinyi /home/xinyi
chmod 755 /home/xinyi/logs
```

### 5.3 上传产物与配置

```bash
# 本机（开发机）打包
mvn clean package -DskipTests
# 产物：xinyi-admin/target/xinyi-admin.jar

# 上传到服务器
scp xinyi-admin/target/xinyi-admin.jar root@服务器IP:/home/xinyi/app/
scp deploy/prod/application-prod.yml   root@服务器IP:/home/xinyi/app/config/
scp deploy/prod/xinyi.env.example      root@服务器IP:/home/xinyi/app/.env
scp deploy/prod/xinyi-admin.service    root@服务器IP:/tmp/
scp deploy/prod/backup-xinyi.sh        root@服务器IP:/home/xinyi/bin/
```

服务器上把 `.env` 里的 `CHANGE_ME` 换成真实值，生成令牌密钥：

```bash
openssl rand -base64 48 | tr -d '\n'      # 填进 XINYI_TOKEN_SECRET
chown xinyi:xinyi /home/xinyi/app/.env
chmod 600 /home/xinyi/app/.env
```

> `application-prod.yml` 放在 `app/config/` 下即可生效（Spring Boot 自动扫 jar 同级 `config/`），改配置**不用重新打包**。

### 5.4 注册服务并启动

```bash
cp /tmp/xinyi-admin.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now xinyi-admin

systemctl status xinyi-admin
journalctl -u xinyi-admin -f          # 看启动过程
```

启动成功的标志是日志出现 `Started XinyiApplication`（约 10–20 秒；首次连容器 MySQL 可能更慢）。

### 5.5 探活

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/captchaImage
# 期望 200；返回 401 说明连上了但被鉴权拦了，也是进程正常的证据
# 连接被拒 = 没起来；502 = 起来了但 Nginx 反代配错
```

---

## 6. 前端部署

```bash
# 开发机
cd xinyi-ui
npm ci
npm run build:prod               # 产物 dist/

# 上传（打包成 zip 更快）
cd dist && zip -r ../dist.zip . && cd ..
scp dist.zip root@服务器IP:/tmp/
```

服务器上：

```bash
rm -rf /home/xinyi/web/*
unzip -o /tmp/dist.zip -d /home/xinyi/web
chown -R xinyi:xinyi /home/xinyi/web
```

`dist` 里必须是 `index.html` 直接在根，不要出现 `/home/xinyi/web/dist/index.html` 这种多套一层。

1Panel → 网站 → 创建网站 → **静态网站**，域名填你的，根目录选 `/home/xinyi/web`，然后**直接编辑站点配置文件**，用 `deploy/prod/nginx-xinyi.conf` 的内容替换。

> 关键两点：`try_files $uri $uri/ /index.html`（否则刷新子页面 404）；`/prod-api/` 反代必须带尾斜杠 `proxy_pass http://127.0.0.1:8080/;`（否则前缀不去掉，接口全 404）。

---

## 7. 域名与 HTTPS

1. 域名解析 A 记录指向轻量服务器公网 IP
2. 1Panel → 网站 → 站点 → HTTPS → 申请 Let's Encrypt 证书（自动续期）
3. 开启「强制 HTTPS」
4. 阿里云控制台放行 443

---

## 8. 备份

```bash
mkdir -p /home/xinyi/bin
# 上传 deploy/prod/backup-xinyi.sh 后改两处：MYSQL_CONTAINER、DB_ROOT_PASS
chmod 700 /home/xinyi/bin/backup-xinyi.sh
/home/xinyi/bin/backup-xinyi.sh      # 先手工跑一次，确认输出 [OK]
```

1Panel → 计划任务 → Shell 脚本，每天 `02:30` 执行上面的脚本。

再补两条：
- 每周打包一次 `/home/xinyi/upload`（已含在上面的脚本里）
- **异地副本**：备份留在同一台机器上等于没有备份，务必 `ossutil` 推到 OSS 内网端点（轻量服务器外网流量有限，别走公网）

**恢复演练**（上线前做一次，别等真出事才试）：

```bash
gunzip -c xinyi_2026-11-04_0230.sql.gz | \
  docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p'ROOT密码' xinyi
```

---

## 9. 上线验收清单

- [ ] 浏览器打开域名，能出登录页（不是 502/404）
- [ ] 验证码图片正常显示（说明 `/prod-api` 反代通）
- [ ] `admin` 能登录，登录后**立即改默认密码**
- [ ] 左侧菜单完整渲染（说明动态路由与 `sys_menu` 数据正常，不是前端写死）
- [ ] 刷新任意子页面不 404（`try_files` 生效）
- [ ] 上传一个附件成功，且 `/home/xinyi/upload` 出现文件
- [ ] 新建/编辑一条业务数据成功（验证写权限与事务）
- [ ] 定时任务页面能打开（验证 `QRTZ_*` 表已导入）
- [ ] 用无权账号访问越权数据被拒绝（多部门数据权限生效）
- [ ] `systemctl restart xinyi-admin` 后仍能正常访问
- [ ] 手工跑备份脚本输出 `[OK]`，并做过一次恢复演练

---

## 10. 排障速查

| 现象 | 常见原因 |
|---|---|
| 启动即报「Communications link failure」 | `application-prod.yml` 里端口写错；或 MySQL 容器还在初始化（`docker logs` 看） |
| 连的库不对 / 仍连 3307 | profile 顺序写成 `prod,druid`，被开发配置覆盖，改成 `druid,prod` |
| `Unknown collation: utf8mb4_0900_ai_ci` | MySQL 装成 5.7，重装 8.0 |
| 登录页 502 | Nginx 反代指向错误或后端没起，先 `curl 127.0.0.1:8080` |
| 接口全 404 但后端正常 | 反代少了尾斜杠，`/prod-api` 前缀没被剥掉 |
| 刷新子页面 404 | 站点缺 `try_files $uri $uri/ /index.html` |
| 定时任务页 500 | `quartz.sql` 没导入 |
| 上传报错 | `/home/xinyi/upload` 不存在或属主不是 `xinyi` |
| 时间差 8 小时 | 容器 MySQL `default-time-zone` 没设，仍是 UTC |
| 隔几天服务被杀 | 内存不足触发 OOM，检查 `dmesg | grep -i oom`，压 `-Xmx` 或加 swap |
| Redis 连不上 | 密码没填 / 容器重启后端口变了，1Panel 应用详情核对端口 |
