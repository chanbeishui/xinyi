# B02实施设计

用户本轮确认：车主从可用站点中选择，使用腾讯位置服务。新增只读POST /api/app/owner/orders/stations获取可用站点；QuoteRequest增加stationId，正式报价必填且重新校验。站点列表不占名额、不产生报价、不默认选择第一站。

四组合输入严格互斥：本人vehicleId或临时vehicleDraft，本人addressId或临时addressDraft。未保存草稿不写车辆/地址簿。报价归owner_account_id；站点权威归属为vi_station.dept_id，客户只获取本次可服务候选的公开字段，不使用后台部门查询权。

以B01锚点的真实起止时间重新匹配同时间PICKUP。服务范围按站点配置半径及可信路线核验，支持车型必须有明确字典编码。排期按上门区间、配置交接耗时、腾讯行车耗时、缓冲计算保守到站区间，必须完整落入唯一有效STATION_RECEIVE区间。仅查询余量，建单原子预占留给B03。

P04数值从现有C01后台业务配置读取，无生产演示默认值：vi.biz.booking.handover.seconds、route-buffer.seconds、min-lead.seconds、quote-ttl.seconds。缺值明确不可报价。TEST/LIVE及审核价格复用C04。

腾讯调用在数据库事务外。距离为米；路线规划duration为分钟，转秒向上取整。密钥仅环境变量注入，日志不得含请求URI、坐标或响应正文。真实供应商未调用，HTTP协议模拟只能验证适配器行为。

保存报价时重新加载本人输入、候选时段、站点、配置版本、价格哈希和两个额度版本；与取路线前的输入不符返回409。报价快照包含源ID/版本、实际选站、两时段、路线、计价与协议版本及有效期；地址和坐标密文。幂等、报价、成功审计、outbox同事务；不扣额度、不建订单。B03消费时再次检查过期、已使用、真实归属及快照版本。

参考腾讯官方路线接口说明：https://github.com/TencentLBS/tencentmap-webservice-skill/blob/main/references/api-direction.md 。
