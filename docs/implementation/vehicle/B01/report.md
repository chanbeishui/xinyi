# B01 · 候选时段与预约前置流程

本轮完成候选时段API和小程序前置流程；任务为IMPLEMENTED，未标VERIFIED。正式报价O34的运行时闭环依赖B02，建单依赖B03；TC-019至TC-036不整体标PASS。

## 实现与权威归属

接车额度仍以vi_quota_slot.station_id→vi_station.dept_id为权威归属。后台维护范围沿用C03。车主读取的是跨可服务站点的匿名时间候选投影，服务层要求真实MINI/OWNER；不返回站点、部门或剩余额度，不提供写入口。ADMIN令牌不能进入MINI域。

按Asia/Shanghai业务日期查询未来、启用、尚有接车供给、站点及直接归属部门有效的PICKUP行；STATION_RECEIVE不作为上门时间。相同起止时间聚合成一个候选，candidateOnly=true，status=CANDIDATE，无remainQuota。查询和选择不锁定、不扣减任何额度。

slotId沿用BIGINT契约，取当前同时间组最小有效行ID作为时间锚点，并不指定履约站点。B02必须从实际锚点加载时间，再按地址、车型、路线及两类容量重新匹配；不能将该ID所在站点当作客户端已选站点。锚点失效须重新获取候选，不猜测时段。当前无权威serviceAreaCode映射，非空筛选明确400而非忽略；地址输入前不宣称服务范围已核实。

小程序入口位于“我的车辆→预约上门检测”。顺序为时段→已有或临时地址→已有或临时车辆/字典检测类型→请求正式报价。临时资料无需预存车辆/地址记录，不要求证件上传。地图失败允许使用已有地址或人工输入完整地址和坐标，坐标不填默认值。返回保留当前页草稿，改动清空报价，异步旧报价响应丢弃；同一报价意图重试保留幂等键，会话更换清空敏感草稿。

正式报价调用主契约POST /api/app/owner/orders/quote，未使用C04后台试算或前端假金额。B02接口尚未实现，现阶段请求失败显示真实错误并保留草稿；O34报价确认和B03建单不能据此宣布可用。

## 验证

- B01BookingIT真实HTTP、微信外部模拟器、MySQL：3/3；C03CapacityIT：6/6，合计9/9，0失败/错误/跳过；本轮Maven单元29项通过。
- 测试验证同时段去重、Asia/Shanghai边界转UTC、不泄站点与额度、不扣名额，满额/停约/停用站点/接收额度排除，非法筛选/内部权限字段/未登录及跨域令牌拒绝。
- 小程序vue-tsc类型检查、微信目标构建通过。首次车辆入口直接引用uni导致模板类型错误，已改为脚本方法并复跑通过。
- 使用frontend-testing-debugging技能；Browser plugin not available，使用临时Playwright和本机Chrome。H5在390×844下实测时段→已有地址→已有车辆→返回保留草稿，无pageerror或Vite错误覆盖层。API为明确UI夹具；未代表真机或B02服务验证。

[手机界面](booking-mobile.png) · [UI结果](ui-qa.json) · [真实HTTP/MySQL结果](http-mysql.json)。测试命令：JAVA_HOME指向JDK21，VI_PYTHON使用仓库.vi-local虚拟环境的绝对路径，mvn -o -pl xinyi-admin -am -Pvi-it -Dit.test=B01BookingIT,C03CapacityIT verify。

无数据库结构或基础数据变更，无新增迁移。部署代码需重启后端、更新小程序构建。本轮未改现有xinyi业务库、未执行生产操作。

## 待续

B02接入地址服务范围与可信路线、公司唯一已审核价格、接车/接收时段校验及持久化报价快照，再回归O34、改地址/车型重报价、过期/价格变化/无价格等状态。B03实现建单原子预占。B01运行验证及独立复核门禁继续保留。
