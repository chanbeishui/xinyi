package com.xinyi.rongli.support;

/**
 * 荣利预约业务常量。状态字面量与数据模型、OpenAPI 枚举保持一致，禁止在代码中另造同义值。
 */
public final class Codes
{
    private Codes()
    {
    }

    /** 主履约状态 */
    public static final class Fulfill
    {
        public static final String PENDING_PAY = "PENDING_PAY";
        public static final String SCHEDULED = "SCHEDULED";
        public static final String PENDING_DISPATCH = "PENDING_DISPATCH";
        public static final String DISPATCHING = "DISPATCHING";
        public static final String MANUAL_DISPATCH = "MANUAL_DISPATCH";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String GOING_PICKUP = "GOING_PICKUP";
        public static final String ARRIVED_PICKUP = "ARRIVED_PICKUP";
        public static final String PICKED_UP = "PICKED_UP";
        public static final String ARRIVED_STATION = "ARRIVED_STATION";
        public static final String INSPECTING = "INSPECTING";
        public static final String INSPECTED = "INSPECTED";
        public static final String CAR_TAKEN = "CAR_TAKEN";
        public static final String WAIT_CONFIRM = "WAIT_CONFIRM";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";
        public static final String CLOSED = "CLOSED";
    }

    /** 取消状态 */
    public static final class Cancel
    {
        public static final String NONE = "NONE";
        public static final String APPLIED = "APPLIED";
        public static final String REJECTED = "REJECTED";
        public static final String APPROVED_WAIT_RETURN = "APPROVED_WAIT_RETURN";
        public static final String COMPLETED = "COMPLETED";
    }

    /** 保管方 */
    public static final class Custody
    {
        public static final String OWNER = "OWNER";
        public static final String DRIVER = "DRIVER";
        public static final String STATION = "STATION";
    }

    /** 交接类型 */
    public static final class Handover
    {
        public static final String OWNER_TO_DRIVER = "OWNER_TO_DRIVER";
        public static final String DRIVER_TO_STATION = "DRIVER_TO_STATION";
        public static final String STATION_TO_DRIVER = "STATION_TO_DRIVER";
        public static final String DRIVER_TO_OWNER = "DRIVER_TO_OWNER";

        /** 交接单状态：一方已确认、等待另一方确认（仅双方确认模式下出现） */
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_PARTIAL = "PARTIAL";
        public static final String STATUS_CONFIRMED = "CONFIRMED";
    }

    /** 承运结束原因 */
    public static final class EndReason
    {
        public static final String RETURNED = "RETURNED";
        public static final String CANCELLED = "CANCELLED";
        public static final String DRIVER_CANCEL = "DRIVER_CANCEL";
    }

    /** 邀约状态 */
    public static final class Invite
    {
        public static final String WAIT_RESPONSE = "WAIT_RESPONSE";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String REJECTED = "REJECTED";
        public static final String EXPIRED = "EXPIRED";
        public static final String REVOKED = "REVOKED";
    }

    /** 额度占用状态 */
    public static final class Reservation
    {
        public static final String HELD = "HELD";
        public static final String CONFIRMED = "CONFIRMED";
        public static final String CONSUMED = "CONSUMED";
        public static final String RELEASED = "RELEASED";
    }

    /** 支付状态 */
    public static final class Payment
    {
        public static final String PENDING = "PENDING";
        public static final String SUCCESS = "SUCCESS";
        public static final String CLOSED = "CLOSED";
        public static final String UNKNOWN = "UNKNOWN";
    }

    /** 售后 */
    public static final class AfterSale
    {
        public static final String CANCEL_REFUND = "CANCEL_REFUND";
        public static final String REFUND_ONLY = "REFUND_ONLY";

        public static final String PENDING_REVIEW = "PENDING_REVIEW";
        public static final String EXECUTING = "EXECUTING";
        public static final String CLOSED = "CLOSED";
        public static final String CASE_REJECTED = "REJECTED";

        public static final String REVIEW_PENDING = "PENDING";
        public static final String REVIEW_APPROVED = "APPROVED";
        public static final String REVIEW_REJECTED = "REJECTED";

        public static final String RETURN_UNDETERMINED = "UNDETERMINED";
        public static final String RETURN_NOT_REQUIRED = "NOT_REQUIRED";
        public static final String RETURN_WAIT = "WAIT_RETURN";
        public static final String RETURN_DONE = "RETURNED";

        public static final String REFUND_NONE = "NONE";
        public static final String REFUND_NOT_REQUIRED = "NOT_REQUIRED";
        public static final String REFUND_WAIT_RETURN = "WAIT_RETURN";
        public static final String REFUND_READY = "READY";
        public static final String REFUND_PROCESSING = "PROCESSING";
        public static final String REFUND_SUCCESS = "SUCCESS";
        public static final String REFUND_UNKNOWN = "UNKNOWN";
        public static final String REFUND_ABNORMAL = "ABNORMAL";
    }

    /** 对账单状态 */
    public static final class Statement
    {
        public static final String DRAFT = "DRAFT";
        public static final String INTERNAL_REVIEW = "INTERNAL_REVIEW";
        public static final String SENT = "SENT";
        public static final String DISPUTED = "DISPUTED";
        public static final String CONFIRMED = "CONFIRMED";
        public static final String VOID = "VOID";
    }

    /** 费用台账状态 */
    public static final class Ledger
    {
        public static final String PENDING_PRICING = "PENDING_PRICING";
        public static final String READY = "READY";

        public static final String NORMAL_RETURN = "NORMAL_RETURN";
        public static final String CANCEL_RETURN = "CANCEL_RETURN";
        public static final String CANCEL_BEFORE_PICKUP = "CANCEL_BEFORE_PICKUP";
        public static final String INITIAL_VALUATION = "INITIAL_VALUATION";
        public static final String PRICE_DELTA = "PRICE_DELTA";
        public static final String ADJUSTMENT = "ADJUSTMENT";
    }

    /** 角色视角 */
    public static final class Role
    {
        public static final String OWNER = "OWNER";
        public static final String DRIVER = "DRIVER";
        public static final String STATION = "STATION";
    }

    /** 证据节点 */
    public static final class Node
    {
        public static final int PICKUP = 1;
        public static final int ARRIVE_STATION = 2;
        public static final int TAKE_CAR = 3;
        public static final int RETURN = 4;
    }

    /** 稳定错误码 */
    public static final class Error
    {
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
        public static final String VERSION_CONFLICT = "VERSION_CONFLICT";
        public static final String STATE_CONFLICT = "STATE_CONFLICT";
        public static final String NOT_SERVICEABLE = "NOT_SERVICEABLE";
        public static final String QUOTE_EXPIRED = "QUOTE_EXPIRED";
        public static final String QUOTA_EXHAUSTED = "QUOTA_EXHAUSTED";
        public static final String IDEMPOTENCY_MISMATCH = "IDEMPOTENCY_MISMATCH";
        public static final String HANDOVER_CODE_INVALID = "HANDOVER_CODE_INVALID";
        public static final String HANDOVER_LOCKED = "HANDOVER_LOCKED";
        public static final String CHANNEL_UNAVAILABLE = "CHANNEL_UNAVAILABLE";
        public static final String SIGNATURE_INVALID = "SIGNATURE_INVALID";
        public static final String INTERNAL = "INTERNAL_ERROR";
    }
}
