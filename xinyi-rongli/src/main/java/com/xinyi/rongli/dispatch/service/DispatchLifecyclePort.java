package com.xinyi.rongli.dispatch.service;

/** 调用者已在原订单取消事务内持目录和订单锁；只结束派单邀约，不处理资金、容量或已接受承接。 */
public interface DispatchLifecyclePort {
    void closeForCancellation(long orderId);
}
