package com.xinyi.rongli.platform.domain;

/** vi_outbox.status，对应 CHECK 约束。 */
public enum OutboxStatus {
    READY, PROCESSING, DONE, FAILED
}
