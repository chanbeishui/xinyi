# C03 · 站点独立双额度与容量管理

范围：V6.4-STATION-QUOTA-20261101。状态：VERIFIED，独立复核待办。

## 业务与数据归属

2026-11-01用户明确选择每站独立维护接车和接收额度。两类数据均填写真实 station_id，通过 vi_station.dept_id 确定权威部门；scope_key 由服务端生成 station:<id>，客户端不可提交。旧的 station_id=NULL 数据不猜测回填、不删除，本轮不对其开放读取或修改。

接车时段不代表到站时段。C03提供供给维护；车主候选时段、地址后匹配及建单预占由B01/B02/B03继续接入。查询和影响预览不扣减、不预占，也不向尚未提供地址的用户承诺站点名额。

## 实现

- 后台新增未来时段创建API，明确站点、类型、UTC起止时间、总量和原因；同站同类重叠区间拒绝，接车/接收及不同站点相互独立。
- 原有4个查询/更新接口真实落地。GET的 slotId+proposedTotal 为只读影响预览，包含预占、正式、已消耗、可用量与可调容性。
- 仅能调整未来时段；总量不能小于预占+正式+已消耗。停约只关闭新预约，不取消已有预约、不释放已消耗容量、不改订单。
- ADMIN按功能对应部门范围过滤，员工按当前站点实时组合权限校验；查询及写入均fail-closed。拒绝伪造占用数、站点/内部权限字段、非整数和非法UTC时间。
- 目录→站点→容量行固定锁序；CAS和数据库条件更新防并发覆盖。幂等、业务、成功审计和outbox同事务；拒绝审计独立保留。重试保持原请求及版本，不将不确定结果当作新调容。
- PC B19/B19-impact：列表、创建、影响确认及停约；小程序S17/S17-impact：当前站点容量、分页、影响确认及调容。小程序入口接在站点资料页。

## 验证

C03CapacityIT 6/6；与C02/I06合并29/29；15类完整HTTP/MySQL回归130/130，0失败、0错误、0跳过；后端单元22/22。PC生产构建、小程序类型检查及微信目标构建通过。工程一致性34/34、契约回归14/14；资料深度一致性检查42/42。

Playwright验证真实页面及控件，接口使用明确UI夹具：低于占用的缩容不提交；影响确认展示真实预览计数；停约提交；故意503后重试复用原幂等键和版本。1440×1000、390×844截图检查通过，无空白、框架错误覆盖层或意外控制台错误；503为刻意注入。真机尚未执行。

## SQL与部署

本轮未改变当前运行库结构，无新增增量迁移。修正的资料包 05_数据设计/001_vi业务表_初始化_V6.4.sql 对齐仓库已有初始化：补齐前轮C02两列、删除不可执行的不存在version字段CHECK。使用随机新建临时库实际执行79张表DDL，列集合与当前模型0差异；该临时库已按随机归属标记清理，未操作xinyi。此验证不替代T06的全部索引、约束和升级路径验收。

新环境以sql/ry_20260417.sql完整初始化为主；已有库禁止重跑初装DDL，缺前轮站点列时先按既有migration_20261101_station_contact_qualification.sql升级。本轮部署重启后端、更新PC/小程序构建，无需Redis清缓存。

## 恢复与边界

下一实施任务：C04客户计价规则和试算，随后B01/B02/B03接续预约报价与建单。独立复核仍需另一位复核者；不自标REVIEWED。真实商户、微信手机号、短信、订阅、微信真机及生产部署均未执行。

完整回归命令（运行时从本机受控配置注入VI_TEST_CONTROL_KEY，不输出其值）：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home VI_PYTHON=.vi-local/venv/bin/python mvn -o -pl xinyi-admin -am -Pvi-it -Dit.test=E06PlatformIT,E07TestingIT,E08PlatformIT,I01IdentityIT,I02IdentityIT,I03IdentityIT,I04IdentityIT,I05IdentityIT,I06ScopeIT,A02AddressIT,A01VehicleIT,C01ConfigIT,A03OwnerIT,C02StationIT,C03CapacityIT verify
```
