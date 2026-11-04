# 荣利 Controller 按业务迁移

2026-11-02，按用户要求将荣利接口层集中到 `xinyi-rongli`。这次调整只改变代码组织，没有增加业务功能或提升任务完成状态。

## 目录与装配

迁移 24 个 Controller 和 `StationWebSupport`，新位置为 `xinyi-rongli/src/main/java/com/xinyi/rongli/<业务>/controller/`，包名为 `com.xinyi.rongli.<业务>.controller`。覆盖 identity、scope、vehicle、address、owner、config、station、capacity、pricing、booking 十个业务域。每个迁移文件与 HEAD 比较仅包声明变化，方法、路由、请求/响应、身份及权限检查均保留。

`xinyi-admin` 继续提供启动程序、平台接口和应用配置，通过原有 Maven 依赖装配荣利模块。启动类位于 `com.xinyi`，原有组件扫描已覆盖新包；控制器依赖由荣利模块已有依赖提供，无需新增依赖或引入反向依赖。`ViApiErrorAdvice` 的适用包更新为 `com.xinyi.rongli`，保证迁移后的接口继续使用原有错误信封及真实 HTTP 状态。

根 `AGENTS.md` 已明确新约定，当前任务账本 `tasks.json` 和 `api_implementation_map.json` 已同步路径。当前实现路径以此约定为准；资料包任务切片中的旧目录只作为原规划参考。历史 evidence、源码摘要及原规范目录未改写；[moves.json](moves.json) 提供旧路径到新路径及迁移前后 SHA256 的完整映射。

## 验证

- 迁移前后端单元测试：31/31 通过。
- 迁移后 JDK 21 下离线 `mvn clean verify -pl xinyi-admin -am -Pvi-it`：构建成功，退出码 0。显式选择既有 19 类回归测试，不运行引用旧规范的 `ViHttpSuiteIT`。
- 单元测试 31/31，集成回归 154/154；失败、错误和跳过均为 0。覆盖现有登录、身份切换、员工关联、数据范围、车辆、地址、车主、配置、站点、额度、计价、预约报价及平台能力。
- 工程静态检查 34/34；70 条已实现接口的 Controller 路径全部存在；任务与接口状态保持原样。
- 25 个源文件逐字节归一化包声明后与迁移前一致，`git diff --check` 通过。
- 干净构建后荣利 JAR 包含全部 25 个迁移类，admin 可执行 JAR 包含荣利依赖；两个构建产物均不含旧 Controller 包下的类。

命令与逐类数量见 [verification.json](verification.json)，脱敏测试报告见 [unit-junit.xml](unit-junit.xml)、[integration-junit.xml](integration-junit.xml)。集成命令显式选择 `E06PlatformIT,E07TestingIT,E08PlatformIT,I01IdentityIT,I02IdentityIT,I03IdentityIT,I04IdentityIT,I05IdentityIT,I06ScopeIT,A02AddressIT,A01VehicleIT,C01ConfigIT,A03OwnerIT,C02StationIT,C03CapacityIT,C04PricingIT,C04LivePricingIT,B01BookingIT,B02BookingIT`，Python 使用仓库既有 `.vi-local/venv/bin/python`，测试密钥只经环境注入。

基线首次遇到沙箱禁止 Mockito 动态附加及本机端口绑定；使用显式 Mockito javaagent 并经工具审批运行后通过，未修改测试断言。完整回归的测试 JVM 在退出阶段超过 30 秒，被 Surefire 回收；所有测试已执行完成、汇总无失败且 Maven 退出 0，此退出诊断保留在验证摘要中。

## 数据、安全与恢复点

本次没有修改 Service、Mapper、数据库归属、查询/写入权限、会话失效策略、审计事件或事务。沿用原有规则，回归在既有隔离测试库 `xinyi_vi_it_e07` 和本机协议模拟器上运行。无数据库结构或基础数据迁移，无新增/修改 SQL，不需要清缓存；部署本次代码需重启后端。未部署生产，未调用真实地图、支付、短信或微信渠道。

主线保持 B02 已验证、下一任务 B03；既有独立复核及发布门禁仍待完成。

## 2026-11-04 版本归档

用户要求补齐此前未版本管理的文件。本轮重新逐字节核对25个移动文件仅包名变化，并执行干净构建：36单元、23类210集成全部通过，无跳过；新证据见version-verification-20261104.json。旧报告的“下一B03”为当时历史恢复点，当前业务恢复点仍为P01已完成、X02开发中。此次独立提交归档迁移和对应AGENTS/路径账本，不改变业务任务状态；无SQL、无需清缓存，部署需重启后端。
