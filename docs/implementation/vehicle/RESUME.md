# 当前恢复点

工作区 `/Users/chenbeishui/Downloads/code_project/xinyi`，分支 `rongli-wx-home`。保留现有代码与数据，不重新初始化。

P02 可信支付查询、未付关闭、微信支付/退款通知和调度补偿已实现并通过隔离验证，状态 VERIFIED，独立复核 PENDING。证据 P02/report.md 与 evidence/P02-20261104.json。全量244集成、最终支付专项48、单元36、6项浏览器验证、构建及SQL一致性通过。

下一业务主线 P03 计划派单和时间驱动任务。退款执行及B31完整后台对账界面/API属于P04；X06负责线下凭证核实。真实支付、真机、生产MySQL8.4、独立复核及部署门槛未完成。

按用户反馈已全库扫描排版与注释问题，见 readability/report.md。本轮先整理支付相关代码；历史配置、订单、报价及测试仍有可读性债务，后续按任务分批整理。根AGENTS补充关键注释和可读性要求。

当前分支15条历史英文提交及3条标签描述已中文化，代码树不变；旧新SHA见 history-chinese/commit-map.json，忽略目录有原历史bundle。其他分支/远端未改、未推送。后续任务坚持独立中文提交、账本与中文标签，版本见VERSIONS.md。

SQL：20261104_01 → _02 → _03_payment_reconciliation；完整初始化已同步。部署需备份、暂停入口/扫描、迁移后重启后端，无需清缓存。自动扫描默认关闭，受控上线时显式开启VI_PAYMENT_RECONCILIATION_ENABLED；本轮未部署。
