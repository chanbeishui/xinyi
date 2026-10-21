package com.xinyi.rongli.platform.service;

import com.xinyi.rongli.platform.domain.ViOutbox;

/**
 * outbox 事件分发 SPI。E06 只提供基座与中继，不实现任何具体业务分发；
 * 通知/支付查单/补偿等分发器由对应业务任务注册（Spring 收集所有 OutboxDispatcher Bean）。
 * 分发在事务外执行（ADR-08：外部支付/文件/消息调用在事务外），失败由中继按退避重试。
 */
public interface OutboxDispatcher {

    /** 是否处理该事件类型。 */
    boolean supports(String eventType);

    /**
     * 执行分发。抛异常表示本次失败，中继将退避重试；正常返回表示成功，中继置 DONE。
     * 实现必须幂等（同一 event_key 可能因租约恢复被重复投递）。
     */
    void dispatch(ViOutbox event);
}
