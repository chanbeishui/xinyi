# 线上快速调试手册（备案期 / 域名未就绪）

目标：把「改一行代码 → 看到效果」的时间从「改代码 + 打包 + 上传 + 重启 + 刷新」压到秒级，
同时又能随时在真实服务器、真实手机上验证。

---

## 0. 四条通道，先看时间成本

| 通道 | 适合改什么 | 首次准备 | 单次循环 | 看到效果的地方 |
|---|---|---|---|---|
| **A 本地全栈** | 后端业务逻辑 | 5 分钟 | 3~5 秒（devtools 重启） | 本机浏览器 |
| **B 本地前端 + 远程后端** | 页面、样式、交互（占日常 70%） | 1 分钟 | **1~2 秒** | 本机浏览器 |
| **C 一键发布** | 需要真机/准生产验收 | 配一次 `XINYI_HOST` | 后端 ~40 秒 / 前端 ~15 秒 | 手机、公网 |
| **D 反向隧道** | 手机上要看**你本机正在改的版本** | 1 分钟 | **0 秒**（改完即生效） | 手机、公网 |

日常节奏建议：**B 通道写代码 → D 通道上手机看 → C 通道做验收**。

---

## 通道 B：本地前端 dev + 远程后端（最常用）

已给 `xinyi-ui/vite.config.ts` 加了 `VITE_PROXY_TARGET` 支持，不用再改代码：

```bash
cd xinyi-ui
VITE_PROXY_TARGET=http://服务器IP:8081/prod-api npm run dev
```

原理：浏览器请求 `/dev-api/system/user/list` → 代理剥掉 `/dev-api` → 拼到
`http://服务器IP:8081/prod-api` 后面 → Nginx 再剥 `/prod-api` → 后端 `/system/user/list`。

想固定下来（不用每次带环境变量），新建 `xinyi-ui/.env.development.local`（已被 git 忽略）：

```ini
VITE_PROXY_TARGET=http://服务器IP:8081/prod-api
```

**注意**：后端如果重启，前端会短暂 502，刷新即可；跨域由 Vite 代理解掉，不涉及 CORS 配置。

---

## 通道 D：反向隧道，把本机后端挂到公网

场景：你在本机改后端，但要让手机 / 小程序 / 同事立刻看到——不用发布。

```bash
# 本机执行（Git Bash 也可以，前提是 ssh 免密登录已配好）
ssh -R 18080:127.0.0.1:8080 root@服务器IP -N
```

服务器侧（`deploy/prod/nginx-xinyi-temp.conf` 里已预留这段，取消注释后 reload）：

```nginx
location /dev-api/ {
    proxy_pass http://127.0.0.1:18080/;
    ...
}
```

然后：

- 浏览器/接口工具：`http://服务器IP:8081/dev-api/api/common/...`
- 小程序 `.env`：`VITE_API_BASE=http://服务器IP:8081/dev-api`

改了后端代码，本地 devtools 重启 3 秒，手机端刷新就能看到，**公网访问的其实是你本机**。

**用完必须收尾**：关闭 ssh 隧道 + 注释回 Nginx 段并 reload，否则站点会一直 502。

> 默认 sshd 只把远端端口绑在 `127.0.0.1`，所以 18080 不会直接暴露公网，只能经 Nginx 访问——这正是我们要的。

### 变体 D0：服务器还没配好时的临时穿透

如果 Nginx/1Panel 还没就绪，用现成的穿透服务（cpolar、natapp、花生壳）把本机 `8080` 映射出去，
拿到一个公网域名后，把小程序 `.env` 的 `VITE_API_BASE` 指向它即可（仍需开「不校验合法域名」）。

- 优点：5 分钟起步，零服务器配置
- 缺点：域名不是你的、免费版限速/掉线、生产环境绝不能用
- 只适合「今天就要给别人看一眼」的场景

---

## 通道 C：一键发布（`deploy/prod/deploy.sh`）

```bash
export XINYI_HOST=root@服务器IP        # 建议写进 ~/.bashrc 或 deploy/prod/deploy.env

./deploy/prod/deploy.sh frontend       # 只发前端，约 15 秒
./deploy/prod/deploy.sh backend        # 只发后端，约 40 秒（含健康检查）
./deploy/prod/deploy.sh all            # 前后端全发
./deploy/prod/deploy.sh build          # 只打包不上传
```

脚本做了什么：

- 后端：`mvn package` → `scp` 上传为 `.new` → 旧 jar 备份为 `.prev` → `systemctl restart` →
  **轮询探活 60 秒，失败自动回滚并提示查日志**
- 前端：`tar` 走 ssh 直传（比 scp 几千个小文件快得多）→ 解到 `web.new` →
  旧目录挪成 `web.old` → `mv` 原子切换，切换窗口毫秒级，回滚只需 `mv web.old web`

Windows 首次使用：`bash deploy/prod/deploy.sh all`（Git Bash 自带 tar/ssh/scp）。
如果 `mvn` 不在 PATH：`export MVN_BIN=/e/codex/.cprep/tools/apache-maven-3.9.16/bin/mvn`。

---

## 小程序三条提速（别再用"上传体验版"当日常手段）

| 手段 | 循环时间 | 说明 |
|---|---|---|
| **预览二维码** | 5~10 秒 | 开发者工具点「预览」，扫码即开发版，不排队编译 |
| **真机调试** | 改完即刷新 | 工具里开「真机调试」，手机端自动重载；前提是勾了「不校验合法域名」 |
| **H5 兜底** | 1~2 秒 | `npm run dev:h5`，手机浏览器直连同一后端，验业务流最快 |

H5 的边界：能验页面、接口、状态流转；**验不了**微信登录、手机号授权、订阅消息、支付（这些依赖小程序运行时）。

小程序 `.env` 两个值随通道切换：

```ini
# 通道 D（隧道）
VITE_API_BASE=http://服务器IP:8081/dev-api
# 通道 C（发布）
VITE_API_BASE=http://服务器IP:8081/prod-api
```

---

## 日志 / 接口 / 数据库速查

```bash
# 后端实时日志（最常用）
ssh root@服务器IP 'journalctl -u xinyi-admin -f'

# 业务错误日志（logback 单独一份，带堆栈）
ssh root@服务器IP 'tail -f /home/xinyi/logs/sys-error.log'

# 探活
ssh root@服务器IP 'curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8080/captchaImage'

# 重启只重启不发布
ssh root@服务器IP 'systemctl restart xinyi-admin && journalctl -u xinyi-admin -n 30 --no-pager'

# 数据库用本地客户端连（不开放 3306 到公网）
ssh -L 13306:127.0.0.1:3306 root@服务器IP -N
# 然后 Navicat / DBeaver 连 127.0.0.1:13306

# Redis 同理
ssh -L 16379:127.0.0.1:6379 root@服务器IP -N
```

接口调试建议用 Apifox/Postman 存一套「线上环境」，Base URL 设 `http://服务器IP:8081/prod-api`，
登录接口拿到 token 后用 `Authorization: Bearer xxx` 复用，比每次点页面快。

---

## 强烈建议：测试库与生产库分开

线上一旦录入真实业务数据，就不该再拿它当测试池。服务器上建第二个库：

```sql
CREATE DATABASE xinyi_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- 按 go-live-runbook 的顺序同样导一遍
```

联调时用参数指向测试库（不改配置文件，jar 通用）：

```bash
java -jar xinyi-admin.jar --spring.profiles.active=druid,prod \
  --spring.datasource.druid.master.url='jdbc:mysql://127.0.0.1:3306/xinyi_dev?...'
```

或者更省事：给测试环境单独一份 `config/application-dev.yml` + 第二个 systemd 单元跑在 8081 端口，
正式环境仍占 8080。两个实例共用同一个 jar，数据彻底隔离。

---

## 调试期三个"别动"

1. **别动 `token.secret`**：RuoYi 的 token 存 Redis，换密钥所有人立刻掉线，前端表现为"操作成功后跳登录页"
2. **别删 Redis 的 key**（`FLUSHDB`）：所有会话一起失效，测试同学会以为是自己被踢
3. **别在生产库上跑 `migration_*` 之外的实验 SQL**：结构改动只走 `sql/` 目录的迁移脚本，否则服务器库和仓库脚本会分叉

---

## 什么时候必须走通道 C（不能只靠本地）

- 上传附件 / 导出报表 / 打印（依赖真实路径、磁盘权限、字体）
- 定时任务（Quartz 只在服务端调度，本地跑不出真实效果）
- Nginx 相关（路由回退、缓存头、gzip、反代超时）
- 给业务方或领导演示
- 发版前的最后一轮回归
