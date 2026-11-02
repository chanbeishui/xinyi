package com.xinyi.rongli.order.service;
/** 只读取已发布支付能力，不选支付方法、不发起交易。主体须从真实站点归属解析。 */
public interface PaymentPolicyGateway {
 record Policy(long receiverDeptId,long revisionId){}
 Policy requireForStation(long stationId,String accountAppId);
}
