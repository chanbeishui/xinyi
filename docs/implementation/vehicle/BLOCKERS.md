# 阻塞记录

2026-11-01更新：已实现模块完整隔离回归130/130、后端单元22/22；C02补强与C03为VERIFIED，仍待独立复核。下方早期NOT_RUN记录是历史快照，不能据此抹去已有进度。

- 本轮E07首轮因缺少测试控制密钥失败，已按本机受控配置注入后复跑通过；密钥未输出、未提交、未改保护记录。
- E08原硬编码287接口已同步为新增容量创建后的288；未降低业务或安全断言。
- 默认Maven仍可能绑定JDK25，本轮全部后端验证显式JAVA_HOME=JDK21；不宣称已修改全局环境。
- 完整412源业务用例、T01–T08、独立复核、真实供应商、微信真机及生产门槛仍未全部完成；不因局部通过宣布G01。
- 资料包初装SQL已对齐前轮C02两列并修复无效CHECK，随机临时库79表执行成功，列集合0差异。T06索引/CHECK/升级路径全面验收仍待执行。


## ENVIRONMENT

- BLK-ENV-1 · Docker/compose 不可用（`docker version` exitCode 127, FileNotFoundError）— **已解除（2026-10-21）**
  - 影响：E02 规定的 `compose.yaml` 隔离 MySQL8.4 + Redis7.4 容器路径无法运行。
  - 处置（用户决定，见 DECISIONS D-ENV-1）：复用现有本机服务——MySQL 3306 的 `xinyi` 库 + Redis 6379，不另建实例。已删除误建的 xinyi_vi_dev/vi_dev、停止误起的 Redis 36379。E02 健康检查通过（MySQL 可连、Redis PONG、JDK21 maven OK）。
  - 残留：MySQL 服务端 9.6.0 与生产目标 8.4 偏差→记 RELEASE 门槛，生产 8.4 须复跑 SQL/CAS（非 E02 阻塞）。

- BLK-ENV-2 · Maven 绑定 JDK 与运行 JDK 不一致（mvn 跑在 Homebrew openjdk 25.0.2，java 运行时 21.0.8）
  - 影响：E04/E05 后端构建若直接用 mvn，编译落在 JDK25，与"运行21/编译release17"计划组合未验证。
  - 处置：构建前统一 JAVA_HOME→21 或配置 maven toolchain（release17）。
  - 恢复条件：构建实测通过并记录实际 JDK/commit。

## VERIFICATION（待环境就绪后执行，当前不标 VERIFIED）

- 全部 298 业务用例、71 技术门禁、MySQL 真实探针、微信/支付/OCR 联调：NOT_RUN。
- MySQL 服务端版本尚未连库确认（需 ≥8.0.22）；E02/E03 连库时核实。
