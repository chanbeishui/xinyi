# 当前恢复点

工作区 `/Users/chenbeishui/Downloads/code_project/xinyi`，分支 `rongli-wx-home`。保留现有代码与数据，不重新初始化。

P02 可信支付查询、未付关闭、微信支付/退款通知和调度补偿已实现并通过隔离验证，状态 VERIFIED，独立复核 PENDING。证据 P02/report.md 与 evidence/P02-20261104.json。全量244集成、最终支付专项48、单元36、6项浏览器验证、构建及SQL一致性通过。

P03计划派单及时间驱动任务已VERIFIED；当前全量26类263集成与36单元通过。下一主线S01售后申请与进度，随后S02审核→P04退款执行。退款执行及B31完整后台对账界面/API属于P04；X06负责线下凭证核实。真实支付、真机、生产MySQL8.4、独立复核及部署门槛未完成。

按用户反馈已全库扫描排版与注释问题，见 readability/report.md。历史97项清单已按DIR04—DIR07全部整理完成，包含配置、订单、报价、测试、小程序与生成类型。根AGENTS补充关键注释和可读性要求。

当前分支15条历史英文提交及3条标签描述已中文化，代码树不变；旧新SHA见 history-chinese/commit-map.json，忽略目录有原历史bundle。2026-11-05已按用户确认同步GitHub开发分支及11个标签，旧远端历史保存在codex/backup-rongli-wx-home-20261105；其他既有分支未改。后续任务坚持独立中文提交、账本与中文标签，版本见VERSIONS.md。

SQL：20261104_01 → _02 → _03_payment_reconciliation；完整初始化已同步。部署需备份、暂停入口/扫描、迁移后重启后端，无需清缓存。自动扫描默认关闭，受控上线时显式开启VI_PAYMENT_RECONCILIATION_ENABLED；本轮未部署。

## 历史代码可读性整理
97/97项已完成；无遗漏、重复或未处理项。逐项摘要及静态信号归类见readability/completion.json，四批测试与中文版本见readability/report.md及VERSIONS.md。收尾复核三处测试注释已校正，36项单元复跑通过；总闭环标签vehicle-readability-complete-20261105。维护任务不计入85项业务进度。

## P03当前交付
P03已完成且通过实际隔离验证，独立复核PENDING。计划冻结、可信支付同事务登记、到期扫描、租约恢复、重复投递/并发防重和异常协调见P03/report.md。复用已有字段/outbox，无SQL迁移；上线需先配置vi.biz.dispatch.lead.minutes，再按发布门槛启用VI_DISPATCH_JOBS_ENABLED并重启。D02邀约/受控重排未实现。当前30项VERIFIED、1项IMPLEMENTED、54项NOT_ASSESSED；下一主线S01→S02→P04。P03中文本地提交及标签见VERSIONS.md，本轮未另行推送或部署。

## 2026-11-05 F01交付
按用户选择先补F01再做S01。F01私有文件与小程序文件客户端已VERIFIED，30文件专项、310组合全量集成与37单元通过；独立代码安全复核通过。迁移_01已隔离验证；部署配置独立私有持久卷后需重启。下一任务S01售后申请与进度（本轮已准备并将在下一任务版本单独提交）。当前31项VERIFIED、1项IMPLEMENTED、53项NOT_ASSESSED。本地中文提交与标签见VERSIONS.md，未推送或生产部署。
