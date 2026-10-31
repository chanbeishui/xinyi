package com.xinyi.rongli.platform.error;

/**
 * V6 普通业务错误码（后端单一来源，与 04_接口/错误码表.json 对齐）。
 * HTTP 真实状态与 body.code 相同；errorCode 是稳定枚举，客户端按 errorCode 分支而非中文 msg。
 * 第三方回调不套本枚举（ADR-04）。
 */
public enum ViErrorCode {
    VI_REQUEST_INVALID(400, false, "请求格式不正确"),
    VI_AUTH_REQUIRED(401, false, "登录已失效，请重新登录"),
    VI_TOKEN_REALM_MISMATCH(401, false, "凭证不适用于当前入口"),
    VI_REFRESH_REUSED(401, false, "登录凭证已失效，请重新登录"),
    VI_FORBIDDEN(403, false, "没有执行此操作的权限"),
    VI_SELF_REVIEW_DENIED(403, false, "不能复核本人提交或改价的记录"),
    VI_RESOURCE_NOT_VISIBLE(404, false, "记录不存在或不可访问"),
    VI_VERSION_CONFLICT(409, true, "记录已变化，请刷新后重试"),
    VI_STATE_CONFLICT(409, false, "当前状态不允许此操作"),
    VI_IDEMPOTENCY_CONFLICT(409, false, "相同请求编号对应了不同内容"),
    VI_SELECTION_CONFLICT(409, false, "所选服务已变化或被其他账单占用"),
    VI_CONTEXT_CHANGED(409, false, "身份或站点已切换，请刷新"),
    VI_SNAPSHOT_CHANGED(409, false, "账单版本已变化，请重新核对"),
    VI_DATA_INVALID(422, false, "填写内容不符合要求"),
    VI_EVIDENCE_INCOMPLETE(422, false, "请补齐照片、里程及交接条件"),
    VI_HANDOVER_CODE_INVALID(422, false, "交接码不正确"),
    VI_HANDOVER_LOCKED(422, false, "交接核验已锁定"),
    VI_UNPRICED_LINES(422, false, "还有费用未填写，暂不能发送"),
    VI_REFUND_LIMIT(422, false, "批准退款超过当前可退金额"),
    VI_PAYMENT_LIMIT(422, false, "付款记录超过可登记余额"),
    VI_SERVICE_UNAVAILABLE(422, false, "当前地址、车型或时段暂不可约"),
    VI_CAPABILITY_NOT_CONFIGURED(422, false, "该功能尚未配置完成"),
    VI_TRAINING_NOT_READY(422, false, "请先完成规定培训"),
    VI_PHONE_PROOF_REQUIRED(422, false, "请重新进行微信手机号授权"),
    VI_PHONE_PROOF_REUSED(409, false, "授权凭据已处理或不可重复使用"),
    VI_EMPLOYEE_LINK_CONFLICT(409, false, "员工关联需管理员核实"),
    VI_EMPLOYEE_LINK_BLOCKED(403, false, "工作关联已暂停或解除"),
    VI_CONFIG_INVALID(422, false, "配置尚不完整或不匹配"),
    VI_CONFIG_CHANGED(409, true, "配置版本已变化"),
    VI_SECRET_UNAVAILABLE(503, true, "安全凭据暂不可用"),
    VI_PAYMENT_METHOD_DISABLED(422, false, "该支付方式已暂停"),
    VI_PAYMENT_METHOD_LOCKED(409, false, "已有支付意图，不能直接切换"),
    VI_SEND_RESULT_UNKNOWN(409, true, "发送结果尚未确定"),
    VI_SUBSCRIPTION_UNAVAILABLE(422, false, "当前没有可用订阅授权或模板"),
    VI_OFFLINE_PROOF_CONFLICT(409, false, "凭证或资金状态已变化"),
    VI_BINDING_CREDENTIAL_INVALID(422, false, "绑定凭证已失效或不匹配"),
    VI_RATE_LIMITED(429, true, "操作过于频繁，请稍后重试"),
    VI_INTERNAL_ERROR(500, false, "处理失败，请联系服务人员"),
    VI_AUTHORITY_UNAVAILABLE(503, true, "权限权威状态暂不可读取"),
    VI_UPSTREAM_UNAVAILABLE(503, true, "外部服务暂不可用");

    private final int httpStatus;
    private final boolean retryable;
    private final String defaultMsg;

    ViErrorCode(int httpStatus, boolean retryable, String defaultMsg) {
        this.httpStatus = httpStatus;
        this.retryable = retryable;
        this.defaultMsg = defaultMsg;
    }

    public int httpStatus() { return httpStatus; }
    public boolean retryable() { return retryable; }
    public String defaultMsg() { return defaultMsg; }
    public String code() { return name(); }
}
