# 阻塞记录

2026-11-02 B02更新：实现与验证完成（VERIFIED），154项真实HTTP/MySQL、31单元、18契约、34工程、42资料检查通过；小程序/PC构建及H5选站报价交互通过。用户选择站点和腾讯地图已落实；无新增SQL。本轮无实现阻塞，下一主线B03。B01已补O34，整体仍待建单闭环。真实腾讯密钥及P04运行参数须部署配置，真实地图调用、真机、独立复核与发布门禁仍未完成。以下日期段落是历史快照，不替代当前证据B02/report.md。

B01本轮：候选接口和前置流程已IMPLEMENTED，9项HTTP/MySQL及小程序构建、H5交互通过。整体VERIFIED需B02正式报价/O34和后续B03建单闭环，当前保持待验收；这是后续开发依赖，不是供应商或权限审批阻塞。详见B01/report.md。

2026-11-01 C04更新：17类隔离HTTP/MySQL回归145/145，后端单元29/29；C04实现及验证完成，独立复核待办。C01正式价格上下文守卫已重新验证。当前API291，新增3个计价试算/停用动作；E08覆盖断言同步。下方早期记录是历史快照，不能抹去已有验证。

- C01首跑测试Python相对路径问题已改为绝对路径；完整重跑通过，未更改全局环境。
- 本轮迁移仅在xinyi_vi_it_e07及随机归属临时库执行，升级与完整初装21列/4索引/6约束一致；现有xinyi和生产未变更。生产MySQL8.4复跑仍是发布门禁。
- 等待费P02未批准继续拒绝；独立复核、412源验收、T01–T08、真实渠道、真机和生产门槛仍未全部完成，不标G01。
- 恢复主线B01/B02/B03；本次C04不扩大到生产部署或真实收费。


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
