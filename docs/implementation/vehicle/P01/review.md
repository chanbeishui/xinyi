# P01 独立复核

PENDING。已完成实现者自检、真实HTTP/MySQL回归、签名验签替身、契约和浏览器检查；不等同独立审核，不标REVIEWED，不推送或部署。

复核重点：PaymentService两段事务、目录/会话/安全锁顺序、租约CAS、超时与终态竞态、已签发预支付在停用开关后的行为；PaymentConfiguration原始公司/商户/应用身份与当前凭据轮换；WechatPrepayGateway真实SDK验签和测试端点隔离；前端绝不以客户端成功改paid。确认线下意图、回调/查单/关单、真机验证保持未完成。核查数据库脚本只修正未交易的历史长编号及全部异常报告。
