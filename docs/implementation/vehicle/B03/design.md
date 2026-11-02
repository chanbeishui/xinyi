# B03 建单与双额度预占设计

范围版本：V6.4-COMPANY-COLLECTION-20261102。2026-11-02用户确认同公司统一收款。保留工作区既有Controller迁移，新增Controller位于xinyi-rongli的order.controller。

数据归属：订单owner_account_id来自真实MINI/OWNER会话，dept_id来自报价站点真实vi_station.dept_id。公司收款通过该部门及祖先部门唯一匹配同环境PAYMENT_POLICY/DEFAULT；绝不使用用户主部门、创建人部门、客户端部门或价格承担主体。命中0或多个均拒绝，不取最近/第一个。支付主体和策略版本固定写入vi_payment_order。

调用边界：OrderService统一读写门禁；读取本人报价及协议、再读公司已发布策略。PublishedPaymentPolicy只读消费现有integration发布结构，不提供绕过X01/X02的发布入口，不把sys_config开关当支付权威。线下需完整企业预收资料；微信需同公司、同环境、同真实AppID的已发布商户与应用，凭据槽位有效；读取仅校验引用及发布证据，不重新执行证书密码学验证或调用供应商。发布完整性校验归X01/X02。双关或无可用方式拒绝，初始化资金为PENDING/UNSELECTED，不发起真实交易。

事务：目录锁→C01配置锁→账户锁→会话锁→安全代际锁→幂等→本人报价锁→真实站点锁→双额度ID升序锁→支付配置发布指针/依赖锁；重验后CAS递增held_quota，创建订单/快照/付款/两条HELD、消费quote、成功审计/outbox。B04/P01/关单须沿用统一锁序，不跨事务单独改聚合计数。30分钟截止由DB UTC生成；不足完整付款窗口拒绝，不静默压缩为更短截止。任何失败全部回滚，拒绝审计独立保存。

报价的容量指纹改为时段/站点/类型/总额/启用等配置字段；held/confirmed/consumed及其版本因其他订单推进时，仅复核剩余容量，不无故使所有旧报价失效。已有旧指纹报价在部署后需重报，最长原报价有效期内可自然失效。

新表vi_order_snapshot以order_id为主键，只能随父订单创建：地址、联系人和备注AES-GCM密文，公开站点JSON，完整协议内容/版本/acceptedAt。历史缺证据不补造。读取必须沿父订单权限投影。幂等缓存只存orderId；HTTP仅OwnerOrderView白名单，手机号脱敏，无密文或内部部门字段。临时vehicleId为null，不生成隐藏常用档案。

前端：公开配置版本与报价一致才展示正文，默认未勾协议；未知结果锁定编辑并重放同请求键；成功展示待付，不生成假支付成功页。付款选择/调用、四端订单查询、超时关单由后续任务完成。
