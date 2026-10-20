# E02 隔离运行环境与依赖锁定 · 报告

状态：环境已就绪（按用户决定复用现有服务，不另建实例）。执行者：Qoder-Agent。

## 用户决定（覆盖资料包默认的容器隔离路径）
- MySQL：66 张 vi_ 表初装到**现有 `xinyi` 库**（与框架 sys_ 表同库；RuoYi 单数据源最省事；vi_station.dept_id / vi_employee_binding.sys_user_id 与 sys_ 同库直接关联）。
- Redis：复用现有 **6379**（探测为无密码 PING 通）。vi 业务模块作为 xinyi-admin 的一部分，运行时复用框架 Redis 配置；E02/E03 建库阶段不需要 Redis。
- **不另建实例**：已停止我之前误起的隔离 Redis 36379；已删除我之前误建的空库 `xinyi_vi_dev` 及 `vi_dev` 用户。用户原库 `ry-vue/xinyi/sys` 与原 Redis 6379 全程未受影响。

## 环境实勘与健康检查（真实执行，exit 0）
| 项 | 实际 | 目标 | 结论 |
|---|---|---|---|
| MySQL 服务端 | 127.0.0.1:3306，**Ver 9.6.0** | 镜像 mysql:8.4 | 偏差：9.6 满足 ≥8.0.22（CHECK/表达式默认/生成列均支持），M0 开发探针可用；**生产目标 8.4 上线前须重跑同样 SQL/CAS 测试**（资料包本要求） |
| 目标库 xinyi | 36 表、**0 张 vi_ 表**、排序规则 **utf8mb4_0900_ai_ci** | — | 与 vi_ 表排序规则一致→无 R11 collation 混用风险；E03 增量加 66 表 |
| vi_dev 连接 | 已删除（改用现有库） | — | 清理完成 |
| Redis | 6379 PING=PONG（无密码） | 镜像 redis:7.4 / 本机 8.6.1 | 复用现有；运行时由框架配置接管 |
| 文件目录 | `.vi-local/files`、`.vi-local/public`（已 gitignore） | 私有/公开分离 | 已建 |
| JDK | 运行时 java 21.0.8；`JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home` | 运行 JDK21 / 编译 release17 | ✅ Maven 在 JDK21 下 `mvn -version` = Java 21.0.8（构建用 JAVA_HOME 指向 21，规避此前 mvn 落在 Homebrew JDK25 的问题，见 D-ENV-2） |
| Maven | 3.9.14 | — | ✅ |
| Docker/compose | 不可用（exitCode 127） | compose 起隔离栈 | N/A：按用户决定走现有服务路径（E02 TASK.md 允许"Docker不可用有独立测试实例路径"） |

## 依赖与版本锁定
- Python 工具 venv：`~/.vi-vehicle-venv`（仓库外，Python 3.11.15）。锁定：PyYAML==6.0.2、jsonschema==4.25.1、pytest==8.4.2、PyMySQL==1.1.1、requests==2.32.5、bcrypt==4.3.0（来自 12_工程执行/requirements.txt）。
- 镜像版本（记录，未拉取，Docker 不可用）：mysql:8.4、redis:7.4、python:3.13-slim（.env.vi.local 中 VI_*_IMAGE）。实际开发用本机 MySQL 9.6.0 / Redis 8.6.1，偏差已记。
- 小程序 npm 锁：xinyi-miniapp 尚未创建（E04），届时 `npm install` 生成 package-lock.json 并提交，之后 `npm ci`。Node v22.16.0 满足模板 engines(>=22<23)。
- 后端 Maven 锁：xinyi-rongli 模块尚未创建（E04），届时构建生成有效依赖树。
- 模拟供应商：`13_真实集成测试/provider_simulator/simulator.py` 语法 OK、可经 venv 运行（SQLite 存替身状态、需 VI_SIM_SECRET、仅 localhost）；**完整绑定与 19 项自测在 E07 执行**，E02 仅确认可运行。

## .env.vi.local（gitignore，0600，不回显密钥）
指向现有服务：VI_DB_HOST=127.0.0.1、VI_DB_PORT=3306、VI_DB_NAME=xinyi、VI_REDIS_HOST=127.0.0.1、VI_REDIS_PORT=6379；保留随机生成的 VI_DATA_KEY/VI_LOOKUP_KEY/VI_SIM_SECRET/VI_ADMIN_JWT_SECRET 等（vi 表加密与模拟器用）。

## 边界与未做
- 未拉取镜像、未跑 compose（Docker 不可用）；未启动模拟器（E07）；未连真实供应商。
- 未读取/复制 application*.yml 中的密码或框架 Redis 密码；root 密码仅本轮引导建/删库临时使用，未写入任何文件。
- E02 不要求未开发的身份/报价/付款提前通过。

## 结论
开发环境就绪：现有 MySQL(3306/xinyi) + Redis(6379) + JDK21 + 文件目录 + 工具 venv + 加密密钥。E03 可在 `xinyi` 库增量初装 66 张 vi_ 表（符合根 AGENTS 迁移规范）。版本偏差（MySQL 9.6 vs 8.4）记入 RELEASE 门槛，生产 8.4 须复跑。
