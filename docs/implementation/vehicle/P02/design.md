# P02 设计与资金边界

订单归属、支付方式、收款公司和商户均从原数据库记录解析。车主HTTP读取/关闭只允许本人订单，复用P01目录锁、账号、会话和安全代际复核；关闭前校验订单版本及幂等键，客户端金额/主体/状态字段一律拒绝。后台权限不新增，未改auth_version。定时任务与通知统一走PaymentLifecycleService，不绕开资金事务。

原始UTF-8报文与四个唯一签名头交给官方APIv3 SDK。仅从数据库已发生交易的可信商户身份构建验签/解密候选，不依赖未验签的mchid/部门选择密钥；解密后再按原商户单号定位并交叉核对商户、应用、金额、币种。时间戳窗口±300秒；原报文不落库，只存SHA-256和永久事件键。相同事件ID不同摘要拒绝；不同通知ID相同交易由原支付单资金事实和transaction_id唯一键防重。验签或金额错误不记成功，资金金额异常另登记outbox核查事件。

回调事务锁顺序为全局身份目录、订单/支付记录、按slot_id排序的额度凭证/额度。有效支付把两份HELD凭证转换CONFIRMED，held_quota转confirmed_quota；支付SUCCESS、订单SCHEDULED、审计、唯一PAYMENT_CONFIRMED事件同事务提交。未到计划派单时间不创建司机邀请；dispatchAt策略和后续推进留P03。不从缺失运营参数猜测提前分钟或立刻派单。

关单先持久化close_requested_at，禁止后续prepay；在途预支付租约未结束则保持额度。微信先可信查单，仅NOTPAY发起关单，并再次查到CLOSED才本地关闭；SUCCESS优先记实付。未知/网络故障不释放额度，下次按数据库next_query_at重试。未发起微信的UNSELECTED/OFFLINE到期可按本地事实关闭，REVIEW_PENDING不由此任务释放。自动扫描使用同一Service、每批25条、30秒查询租约；重启从DB恢复。外部请求在事务外，最长24秒（每调用8秒），重复结果靠事务状态防重。

已关闭并释放额度后的真实成功记实付、MANUAL_CONFLICT和独立补偿事件，订单保持关闭，不超卖重开。退款通知必须对应真实原退款单/原交易号/商户/金额；SUCCESS仅一次把预留转已退，CLOSED释放预留，ABNORMAL保留预留。新建退款/审批/调用执行及对账人工处理属于P04/S02，不能由通知伪造批准。

部署的VI_PAYMENT_RECONCILIATION_ENABLED默认false；通过环境配置显式启用自动扫描，测试直接调用同一服务，不接真实商户。没有运行生产扫描、真实转账或退款。协议实现参考官方SDK：https://github.com/wechatpay-apiv3/wechatpay-java 。
