# 从零到上线：完整部署手册

本文是一次真实上线（阿里云轻量 2C4G + 1panel + Spring Boot + Vue 后台 + 微信小程序）的完整记录，
按实际执行顺序编排。每一步都说明**做什么、为什么做、不这么做会怎样**，可以直接照着走。

> 示例值：服务器 `8.138.122.95`、库名 `rongli_wx`、运行用户 `xinyi`、临时站点端口 `8081`。
> 换项目时替换这些值即可，其余步骤与结构不变。

---

## 0. 开工前

### 0.1 适用场景

- 单机部署：应用、MySQL、Redis 全在一台 2C4G 服务器上
- 后端是 Spring Boot fat jar，前端是 Vite 静态包，另有微信小程序
- 不做容器编排，中间件用面板的应用商店托管

### 0.2 需要提前准备

| 项 | 说明 |
|---|---|
| 服务器 | 2C4G 起，Ubuntu 22.04/24.04，root 或 sudo |
| 面板 | 1panel（本文按 1panel 写，宝塔同理） |
| 域名 | 可先不备案，用 `IP:端口` 上线；备案完成后再切 |
| 数据库脚本 | 基础脚本 + 附加脚本（如定时任务表）+ 增量迁移脚本 |
| 凭据 | MySQL root 密码、业务库密码、Redis 密码（记录在密码管理器） |

### 0.3 时间预估

| 阶段 | 首次 | 后续重装 |
|---|---|---|
| 服务器底座 | 40~60 分钟 | 15 分钟（有脚本） |
| 数据库 | 10 分钟 | 5 分钟 |
| 后端 | 15 分钟 | 5 分钟 |
| 前端 | 15 分钟 | 2 分钟 |
| CI/CD | 20 分钟 | — |
| 备份 | 10 分钟 | — |

---

## 阶段一：服务器底座

### 1.1 系统基线

**前提**：能用 root 登录服务器。

```bash
timedatectl set-timezone Asia/Shanghai && timedatectl | head -3
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
apt update && apt upgrade -y
```

**为什么**：

- 时区必须与应用、数据库一致。三者不统一时，日志时间对不上，排查事件顺序会出错
- 4G 内存同时跑 JVM（1.5G）+ MySQL（768M）+ Redis（256M）会吃紧，**没有 swap 时 MySQL 可能直接起不来**
- 写进 `/etc/fstab` 才能重启后仍然生效，否则重启即失效

### 1.2 防火墙与端口

阿里云控制台 → 防火墙，按需放行：

| 端口 | 用途 | 何时放行 |
|---|---|---|
| 22 | SSH | 立即 |
| 面板端口 | 1panel 管理 | 立即 |
| 8081 | 备案期的临时站点 | 立即 |
| 80 / 443 | 正式站点 | 备案完成后 |

**为什么**：容器发布的端口会绕过 ufw，真正的边界是云厂商防火墙；只在系统里开 ufw 容易被误导。

### 1.3 安装面板与容器运行时

```bash
curl -sSL https://resource.fit2cloud.com/1panel/package/quick_start.sh -o quick_start.sh
bash quick_start.sh
docker version
```

**装完立刻做三件事**（面板默认端口和安全入口是公开信息）：

1. 改面板端口（如 `23816`）
2. 改安全入口路径
3. 开启二次验证

### 1.4 安装 MySQL / Redis

面板 → 应用商店 → 安装 MySQL 8 与 Redis，**两者都不要勾选「端口对外暴露」**。

装完核对：

```bash
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

期望端口映射形如 `127.0.0.1:3306->3306/tcp`（不是 `0.0.0.0`）。

**为什么**：数据库只需要被本机的应用访问。暴露到公网后，被扫描爆破只是时间问题。

### 1.5 中间件参数

MySQL 配置文件（面板 → 数据库 → 配置）追加到 `[mysqld]` 段：

```ini
innodb_buffer_pool_size = 768M
max_connections = 150
default-time-zone = '+08:00'
character-set-server = utf8mb4
collation-server = utf8mb4_0900_ai_ci
```

**必须注意**：参数要写在 `[mysqld]` 段内。若文件里 `[client]` 段夹在中间，写在它后面的参数会被归到客户端组，
`SHOW VARIABLES` 里看不到变化——这类"改了没生效"最常见。

Redis 配置：

```ini
requirepass <强密码，只用字母数字>
maxmemory 256mb
maxmemory-policy allkeys-lru
```

**为什么密码只用字母数字**：面板的 compose 会把密码拼进命令行，含 `$`、引号、反引号会被 shell 截断，
表现为"密码明明对，就是连不上"。

### 1.6 安装 JDK（容易漏，漏了必然启动失败）

```bash
apt install -y openjdk-17-jre-headless
java -version
```

**为什么单独强调**：`systemd` 的 `ExecStart=/usr/bin/java ...` 在 java 不存在时报 `status=203/EXEC`，
日志里只有 `Main process exited`，看着像配置错误，实际是解释器没装，且会疯狂重启刷屏。

---

## 阶段二：数据库

### 2.1 建库与业务账号

```bash
MYSQL_C=$(docker ps --format '{{.Names}}' | grep -i mysql | head -1)
read -rsp "MySQL root 密码: " P; echo
docker exec -e MYSQL_PWD="$P" -i "$MYSQL_C" mysql -uroot -e "
CREATE DATABASE IF NOT EXISTS rongli_wx DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'xinyi'@'%' IDENTIFIED BY '<业务密码>';
GRANT ALL PRIVILEGES ON rongli_wx.* TO 'xinyi'@'%';
FLUSH PRIVILEGES;"
```

**为什么**：

- 业务跑在独立账号上，root 只用于运维与备份，泄露面更小
- 账号 host 必须是 `'%'`：应用从宿主机经 `127.0.0.1` 连入容器，来源不是容器内的 `localhost`
- 排序规则 `utf8mb4_0900_ai_ci` 是 MySQL 8 专有，5.7 建库直接失败

### 2.2 导入顺序（顺序错了会缺表）

```bash
# 在服务器上，把脚本放到 /root/sql 后执行
for f in ry_20260417.sql quartz.sql $(ls migration_*.sql | sort); do
  echo "==> $f"
  docker exec -e MYSQL_PWD="$P" -i "$MYSQL_C" mysql -uroot --default-character-set=utf8mb4 rongli_wx < "/root/sql/$f" || break
done
```

**为什么必须是这个顺序**：

1. **基础脚本**建系统表与业务表骨架
2. **附加脚本**（`quartz.sql`）单独存在，基础脚本里没有 `QRTZ_*` 表；漏了它定时任务启动即报表不存在
3. **迁移脚本**按日期顺序叠加，后一个可能依赖前一个加的列

**不要导入**：`seed_*` / demo 数据。它们会污染生产台账，且常硬编码库名。

**必须带** `--default-character-set=utf8mb4`：不带时客户端按默认字符集读取 UTF-8 文件，
中文配置名会报 `Data too long for column 'config_name'`——看着像字段太短，其实是编码问题。

### 2.3 结构断言（别只看"导入成功"）

```bash
docker exec -e MYSQL_PWD="$P" -i "$MYSQL_C" mysql -uroot -N -e "
SELECT 'tables', COUNT(*) FROM information_schema.tables WHERE table_schema='rongli_wx'
UNION ALL SELECT 'qrtz', COUNT(*) FROM information_schema.tables WHERE table_schema='rongli_wx' AND table_name LIKE 'QRTZ%'
UNION ALL SELECT 'admin', COUNT(*) FROM rongli_wx.sys_user WHERE user_name='admin';"
```

**为什么**：导入中断时 mysql 客户端仍可能返回 0，缺的表只有比对数量才发现。
建议把期望值写死（如 `QRTZ_*=11`），不达标直接判失败。

### 2.4 迁移脚本预检

```bash
python skills/sql-migration-audit/scripts/audit_migrations.py \
  --sql-dir sql --base ry_20260417.sql --fail-on error
```

**为什么**：硬编码 `USE 旧库名`、`TABLE_SCHEMA='旧库名'` 在开发机上永远不会报错（库名恰好一致），
换环境才集中爆发，而那时导入已经做了一半。

---

## 阶段三：后端

### 3.1 运行用户与目录

```bash
useradd -r -m -d /home/xinyi -s /usr/sbin/nologin xinyi
mkdir -p /home/xinyi/app/config /home/xinyi/upload /home/xinyi/logs /home/xinyi/backup /home/xinyi/bin
chown -R xinyi:xinyi /home/xinyi
```

**为什么**：进程不以 root 运行，即使应用被攻破也拿不到系统权限；日志与上传目录独立，便于备份与清理。

### 3.2 配置与环境变量

`/home/xinyi/app/.env`（权限 600，属主 `xinyi`）：

```ini
DB_USER=xinyi
DB_PASSWORD=<业务密码>
REDIS_PASSWORD=<Redis 密码>
APP_SECRET=<openssl rand -base64 48 的输出>
```

`/home/xinyi/app/config/application-prod.yml` 关键项：

```yaml
server:
  port: 8080
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://127.0.0.1:3306/rongli_wx?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
        username: ${DB_USER}
        password: ${DB_PASSWORD}
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: ${REDIS_PASSWORD}
```

**为什么**：

- 密码放 `.env` 而不是 jar 里：同一份 jar 可以部署到多环境，改密码不用重新打包
- `.env` 用 600：`EnvironmentFile` 由 systemd 以 root 读取，进程拿到变量但不落盘到日志
- `allowPublicKeyRetrieval=true`：MySQL 8 默认 `caching_sha2_password`，非 TLS 连接不带这个参数会直接拒绝
- `serverTimezone` 与数据库 `default-time-zone` 必须一致，否则时间差 8 小时

### 3.3 systemd 单元

`/etc/systemd/system/xinyi-admin.service`：

```ini
[Unit]
Description=xinyi-admin
After=network-online.target docker.service
Wants=network-online.target

[Service]
Type=simple
User=xinyi
WorkingDirectory=/home/xinyi/app
EnvironmentFile=/home/xinyi/app/.env
Environment="TZ=Asia/Shanghai"
ExecStart=/usr/bin/java -Xms512m -Xmx1536m -XX:MaxMetaspaceSize=320m \
  -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/xinyi/logs \
  -Duser.timezone=Asia/Shanghai -jar /home/xinyi/app/xinyi-admin.jar \
  --spring.profiles.active=druid,prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**为什么**：

- **`druid,prod` 顺序不能反**：Spring 后加载的 profile 覆盖前面的，写成 `prod,druid` 会用开发库配置覆盖生产配置
- `-Xmx1536m` 是 4G 机器的上限取值，再大加上 MySQL 就会 OOM
- `Restart=on-failure` + `RestartSec=10`：崩溃后自动拉起，但不会疯狂重启

### 3.4 上传并启动

```bash
scp xinyi-admin/target/xinyi-admin.jar root@<服务器>:/home/xinyi/app/
ssh root@<服务器> 'chown xinyi:xinyi /home/xinyi/app/xinyi-admin.jar
  systemctl daemon-reload && systemctl enable --now xinyi-admin'
sleep 25
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/captchaImage
```

**期望** `200`。若为 `000`：

```bash
systemctl status xinyi-admin --no-pager -l
journalctl -u xinyi-admin -n 100 --no-pager | tail -30
```

**为什么用验证码/健康端点探活**：不需要登录态，且能验证 Spring 上下文、数据库连接、Redis 连接都已就绪。

---

## 阶段四：前端

### 4.1 构建

```bash
cd xinyi-ui
npm ci
npm run build:prod          # 产物在 dist/
```

**为什么固定 Node 版本**：不同 Node 版本产出的资源 hash 不同，排查"线上是哪次构建"时会混淆。

### 4.2 安装 nginx 与站点

```bash
apt install -y nginx && systemctl enable --now nginx
mkdir -p /home/xinyi/web && chown xinyi:xinyi /home/xinyi/web
```

`/etc/nginx/conf.d/xinyi.conf`：

```nginx
server {
    listen 8081;
    root /home/xinyi/web;
    index index.html;
    client_max_body_size 20m;

    location / { try_files $uri $uri/ /index.html; }

    location = /index.html { add_header Cache-Control "no-cache, no-store, must-revalidate"; }

    location /static/ { expires 30d; add_header Cache-Control "public, immutable"; }

    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 120s;
    }
}
```

**为什么**：

- `try_files ... /index.html`：Vue history 路由刷新子页面时不会 404
- `/prod-api/` 的 `proxy_pass` **末尾必须有斜杠**：它会把 `/prod-api` 前缀剥掉，否则后端收到 `/prod-api/api/xxx` 全部 404
- `index.html` 不缓存、静态资源长缓存：发版后用户立刻拿到新资源清单，同时老资源不必重复下载

### 4.3 目录权限（403 的根源）

```bash
chmod 751 /home/xinyi          # 让 nginx 能穿过，但列不出目录内容
chmod 755 /home/xinyi/web
find /home/xinyi/web -type f -exec chmod 644 {} \;
```

**为什么 751 而不是 755**：`x` 权限足够 nginx 读取 `web/` 下的文件，`r` 权限则会让任何本机用户能列出家目录内容。

### 4.4 发布与验证

```bash
# 打包上传（tar 流式，比逐个 scp 快得多）
tar -czf - -C dist . | ssh root@<服务器> 'mkdir -p /tmp/release && cat > /tmp/release/dist.tar.gz'

# 原子切换（旧目录保留为 .old）
ssh root@<服务器> 'cd /home/xinyi && rm -rf web.new && mkdir web.new \
  && tar -xzf /tmp/release/dist.tar.gz -C web.new \
  && rm -rf web.old && mv web web.old && mv web.new web'
```

验证：

```bash
curl -s -o /dev/null -w 'site=%{http_code}\n' http://127.0.0.1:8081
curl -s -o /dev/null -w 'api=%{http_code}\n' http://127.0.0.1:8081/prod-api/captchaImage
grep -o 'index-[A-Za-z0-9_-]*\.js' /home/xinyi/web/index.html | head -1
```

**为什么要比对入口 hash**：只看 HTTP 200 无法区分新旧版本（浏览器/CDN 会缓存）。
**发布后必须强刷（Ctrl+F5）**，否则可能仍加载旧 chunk。

---

## 阶段五：小程序

用开发者工具 CLI 上传（走登录态，**不需要上传密钥、不受 IP 白名单限制**）。

前提：工具已扫码登录，且「设置 → 安全设置」开启 **服务端口**（CLI/HTTP 调用）。

```bash
cd xinyi-rongli-wx
npm run build:mp-weixin
node scripts/upload-mp-cli.mjs --version 1.0.0 --desc "本次改动"
```

上传后是开发版，需人工三步：

1. 公众平台 → 版本管理 → **设为体验版**
2. 成员管理 → 添加体验成员
3. 提审（正式发布还需小程序备案完成）

**为什么不用 miniprogram-ci**：它要求「代码上传密钥 + 出口 IP 白名单」，
家庭宽带 IP 变动即失效，CI runner 出口更不固定。

---

## 阶段六：CI/CD（推荐）

### 6.1 部署密钥与 Secrets

```bash
ssh-keygen -t ed25519 -N "" -C deploy -f ~/.ssh/deploy_key
ssh-copy-id -i ~/.ssh/deploy_key.pub root@<服务器>   # 或手工追加到 authorized_keys
```

GitHub → Settings → Secrets and variables → Actions：

| Name | Value |
|---|---|
| `DEPLOY_HOST` | 服务器 IP（不带 `root@`） |
| `DEPLOY_USER` | `root` |
| `DEPLOY_PORT` | `22` |
| `DEPLOY_SSH_KEY` | 私钥全文（含首尾 `-----`，末尾保留换行） |

### 6.2 流水线注意

- `ssh` 用 `-p` 指定端口、**`scp` 用 `-P`**：共用参数会让 `scp -p 22` 把 `22` 当成本地文件
- Secret 值使用前 `tr -d '[:space:]'` 去空白：粘贴带入的空格会让远端目标被解析成本地路径
- 手动触发默认只发前端（体积小）；后端留本机执行，跨境传 94MB 不稳定
- CI 的数据库名**故意与生产不同**，否则硬编码库名的问题永远不会被 CI 拦住

### 6.3 分支保护

Settings → Rulesets → 目标分支加 `Require status checks`，把 job 名（如 `backend-build`、`frontend-build`）列为必需。
job 名要唯一，否则两个 workflow 都叫 `build` 时无法区分。

---

## 阶段七：备份与运维

### 7.1 备份脚本

```bash
#!/usr/bin/env bash
set -euo pipefail
MYSQL_C=$(docker ps --format '{{.Names}} {{.Image}}' | grep -i mysql | awk '{print $1}' | head -1)
set -a; . /home/xinyi/app/.env; set +a
OUT=/home/xinyi/backup/xinyi_$(date +%F_%H%M).sql.gz
docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MYSQL_C" mysqldump -u"$DB_USER" \
  --single-transaction --quick --no-tablespaces --default-character-set=utf8mb4 \
  --routines --triggers --events rongli_wx | gzip -9 > "$OUT"
gzip -t "$OUT" && gzip -dc "$OUT" | tail -1 | grep -q "Dump completed"
```

**为什么**：

- **用业务账号而不是 root**：脚本里不出现最高权限凭据；`--no-tablespaces` 让它无需 `PROCESS` 权限
- **`--single-transaction`**：InnoDB 一致性快照，备份期间不锁表
- **校验结尾 `Dump completed`**：半截备份比没有备份更危险，出错要立刻删掉并非零退出

### 7.2 定时任务

`/etc/cron.d/xinyi-backup`（644）：

```
30 2 * * * root /home/xinyi/bin/backup-xinyi.sh >> /home/xinyi/logs/backup.log 2>&1
```

**注意**：如果面板里也加了同样的计划任务，会一天跑两次。

### 7.3 恢复演练（上线前必做一次）

还原到**临时库**验证，绝不碰生产：

```bash
docker exec -i "$MYSQL_C" mysql -uroot -e "DROP DATABASE IF EXISTS restore_check; CREATE DATABASE restore_check DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
gunzip -c /home/xinyi/backup/xinyi_*.sql.gz | docker exec -i "$MYSQL_C" mysql -uroot restore_check
docker exec -i "$MYSQL_C" mysql -uroot -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='restore_check';"
docker exec -i "$MYSQL_C" mysql -uroot -e "DROP DATABASE restore_check;"
```

**为什么**：备份没验证过等于没有备份。这一步能在真正需要时省下几小时的慌乱。

---

## 阶段八：验收清单

- [ ] `systemctl is-active xinyi-admin` = `active`
- [ ] `curl 127.0.0.1:8080/captchaImage` = `200`
- [ ] `curl 127.0.0.1:8081` = `200`，`/prod-api/captchaImage` = `200`
- [ ] 浏览器能打开登录页、验证码显示、`admin` 能登录、菜单能展开
- [ ] 强刷（Ctrl+F5）后仍是新版本（比对入口 hash）
- [ ] 数据库表数量与断言一致（如 `QRTZ_* = 11`）
- [ ] 备份任务已存在，且手工跑过一次成功
- [ ] `admin` 默认密码已修改
- [ ] 服务器磁盘剩余 > 5G（`df -h /`）

---

## 常见问题与排查

### 部署与启动

| 症状 | 根因 | 排查/修法 |
|---|---|---|
| `status=203/EXEC` 无限重启 | `ExecStart` 里的解释器不存在（未装 JDK） | `which java`；装 `openjdk-17-jre-headless` |
| 启动即退出 | `.env` 路径错/权限不足/变量名不匹配 | `journalctl -u <svc> -n 100`；`EnvironmentFile` 指向的文件属主可读 |
| 连回开发库 | profile 顺序写反 | 必须 `druid,prod` |
| `Access denied for user` | 密码不一致，或账号 host 不是 `%` | `ALTER USER ... IDENTIFIED BY` 对齐 |
| `Public Key Retrieval is not allowed` | 连接串缺参数 | 加 `allowPublicKeyRetrieval=true` |
| 时间差 8 小时 | 容器 UTC / 连接串时区不一致 | 设 `default-time-zone='+08:00'` 与 `serverTimezone` |
| 图片上传报"找不到存储平台" | 对象存储凭据未注入（`enable-storage` 默认 false） | 检查环境变量数量、`.env` 格式（无空格无引号）、是否重启 |

### 数据库

| 症状 | 根因 | 修法 |
|---|---|---|
| `Unknown database 'x'` | 脚本里有 `USE x`，或库没建 | 去掉 `USE`；先建库 |
| `Duplicate column` | 守卫用固定库名，换库名后失效 | 守卫改 `DATABASE()` |
| `Data too long for column 'config_name'` | 导入时缺 `--default-character-set=utf8mb4` | 加参数重导 |
| `QRTZ_*` 表不存在 | 漏执行 `quartz.sql` | 单独导入 |
| 迁移重跑报重复键 | 缺 `information_schema` 守卫 | 用动态 SQL + 守卫 |

### 前端

| 症状 | 根因 | 修法 |
|---|---|---|
| 403 | 站点父目录缺 `x` 权限 | `chmod 751 <parent>` |
| 404 | 包内多套一层目录 | 重打包，`index.html` 必须在根 |
| 刷新子路由 404 | 缺 history 回退 | `try_files $uri $uri/ /index.html` |
| 接口全 404 | `proxy_pass` 末尾少斜杠 | `proxy_pass http://127.0.0.1:8080/;` |
| 发版后仍是旧页面 | 浏览器缓存 | `index.html` 设 no-store，用户强刷 |

### CI 与传输

| 症状 | 根因 | 修法 |
|---|---|---|
| `scp: stat local "22"` | scp 用了 `-p`（应为 `-P`） | 分开两份参数 |
| 同上但端口不是 22 | Secret 带空格/换行 | 使用前去空白 |
| `Permission denied (publickey)` | 公钥未进服务器，或贴了 `.pub` | 追加公钥，权限 700/600 |
| `Connection timed out` | 防火墙未放行 SSH | 放行端口或用自托管 runner |

### 终端环境

| 症状 | 根因 | 修法 |
|---|---|---|
| 命令报 `^[[200~docker: not found` | 终端括号粘贴模式未启用 | `echo 'set enable-bracketed-paste on' >> ~/.inputrc && exec bash` |
| `type: xxx: not found` | `type` 是 bash 内建，不是读文件工具 | 用 `cat` |

---

## 回滚速查

| 目标 | 命令 | 生效 |
|---|---|---|
| 后端 | `cd /home/xinyi/app && mv xinyi-admin.jar.prev xinyi-admin.jar && systemctl restart xinyi-admin` | ~20 秒 |
| 前端 | `cd /home/xinyi && rm -rf web && mv web.old web` | 立即 |
| 数据库 | `gunzip -c <备份>.sql.gz \| docker exec -i "$MYSQL_C" mysql -uroot rongli_wx` | 视数据量 |
| 小程序 | 公众平台 → 版本管理 → 回退上一线上版本 | 立即 |

---

## 附录 A：关键路径与端口

| 项 | 值 |
|---|---|
| 应用目录 | `/home/xinyi/app`（jar、`.env`、`config/`） |
| 日志 | `/home/xinyi/logs`（`stdout.log`、`stderr.log`、`sys-*.log`） |
| 站点根 | `/home/xinyi/web`（旧版 `web.old`） |
| 备份 | `/home/xinyi/backup` |
| 服务名 | `xinyi-admin` |
| 应用端口 | 8080（只绑 127.0.0.1） |
| 站点端口 | 8081（备案期）→ 80/443 |
| 数据库 | `rongli_wx` @ 127.0.0.1:3306 |

## 附录 B：备案完成后的切换

1. 域名解析到服务器
2. 防火墙放行 80/443
3. 申请证书并配置 HTTPS（强制跳转）
4. 站点改为 `listen 443 ssl`，停用 8081
5. 前端 `VITE_API_BASE=https://<域名>/prod-api` 重新构建发布
6. 公众平台 → 开发设置 → 把域名加入 request 合法域名
7. 小程序端切真实微信登录，重新上传体验版
8. 微信支付切生产配置后提审发布
