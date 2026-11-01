# C01 正式计价上下文适配

范围V6.4-PRICE-CONTEXT-20261101。旧版本证据保留；本轮关闭仅车型计数的旧assertPriced入口，新增可信站点+车型+检测类型正式匹配，委托C04 PricingService核验公司、环境及独立审核发布版本。未配置、歧义、无归属旧记录不能0元放行。

完整17类HTTP/MySQL回归145/145，其中C01ConfigIT 6/6；单元29/29，详情见../C04/report.md及../evidence/C01-20261101.json。隔离测试库xinyi_vi_it_e07，独立复核待办，不自标REVIEWED。
