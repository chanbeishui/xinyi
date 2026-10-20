# 阻塞记录

尚未实测；不得推断所有模块不能开发。

## ENVIRONMENT

- BLK-ENV-1 · Docker/compose 不可用（`docker version` exitCode 127, FileNotFoundError）
  - 影响：E02 规定的 `compose.yaml` 隔离 MySQL8.4 + Redis7.4 容器路径无法运行；E03 的 db-test、E07 的 live-test/integration-suite 原设计依赖容器。
  - 处置（依用户指示 II.2 + DECISIONS D-ENV-1）：适配为本机运行中的 MySQL（3306）建独立库 `xinyi_vi_dev` + 本机 Redis 独立实例/命名空间；仍执行 CHECK/生成列/唯一约束/UTC 真实探针。不连、不写、不删用户原 xinyi 库。
  - 恢复条件：若后续需要严格容器隔离或 MySQL8.4 精确版本，安装 Docker 后按原 compose 路径复跑；当前以本机独立库满足"独立开发数据库"要求。
  - 状态：E02/E03 可在此适配下推进实现与探针；涉及"容器化隔离"的验收项在 Docker 恢复前记 VERIFICATION 阻塞，不标 VERIFIED。

- BLK-ENV-2 · Maven 绑定 JDK 与运行 JDK 不一致（mvn 跑在 Homebrew openjdk 25.0.2，java 运行时 21.0.8）
  - 影响：E04/E05 后端构建若直接用 mvn，编译落在 JDK25，与"运行21/编译release17"计划组合未验证。
  - 处置：构建前统一 JAVA_HOME→21 或配置 maven toolchain（release17）。
  - 恢复条件：构建实测通过并记录实际 JDK/commit。

## VERIFICATION（待环境就绪后执行，当前不标 VERIFIED）

- 全部 298 业务用例、71 技术门禁、MySQL 真实探针、微信/支付/OCR 联调：NOT_RUN。
- MySQL 服务端版本尚未连库确认（需 ≥8.0.22）；E02/E03 连库时核实。
