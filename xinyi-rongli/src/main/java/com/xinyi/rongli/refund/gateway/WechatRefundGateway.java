package com.xinyi.rongli.refund.gateway;

import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.payment.service.*;
import com.xinyi.rongli.payment.gateway.WechatPrepayGateway;
import com.xinyi.rongli.refund.domain.RefundRecord;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import com.xinyi.rongli.refund.service.RefundSettlementService.Fact;
import com.xinyi.rongli.platform.error.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 官方SDK与历史支付凭据适配；不读取当前支付开关，原商户身份和冻结金额不能由重试请求覆盖。 */
@Component
public class WechatRefundGateway {
    private record Runtime(PaymentTransaction payment, RefundRecord refund, Config sdk) {}

    private final PaymentMapper payments;
    private final RefundMapper refunds;
    private final PaymentConfiguration configuration;
    private final PaymentHistoryGateway history;
    private final WechatPrepayGateway transport;
    private final EmployeeDirectoryGuardMapper directory;
    private final TransactionTemplate tx;

    public WechatRefundGateway(
            PaymentMapper payments,
            RefundMapper refunds,
            PaymentConfiguration configuration,
            PaymentHistoryGateway history,
            WechatPrepayGateway transport,
            EmployeeDirectoryGuardMapper directory,
            PlatformTransactionManager manager) {
        this.payments = payments;
        this.refunds = refunds;
        this.configuration = configuration;
        this.history = history;
        this.transport = transport;
        this.directory = directory;
        tx = new TransactionTemplate(manager);
    }

    private Runtime runtime(long id) {
        return tx.execute(
                status -> {
                    directory.ensureGuard();
                    directory.lockGuard();
                    RefundRecord refund = refunds.find(id);
                    if (refund == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    Long order = refunds.orderId(refund.getPaymentId());
                    if (order == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                    payments.lockOrder(order);
                    PaymentTransaction payment = payments.lockPayment(order);
                    if (!"WECHAT".equals(payment.getPaymentMethod())
                            || !"WECHAT".equals(refund.getPaymentMethod())
                            || !"WECHAT_API".equals(refund.getExecutionMode()))
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                    return new Runtime(payment, refund, configuration.sdk(payment));
                });
    }

    public Fact query(long id) {
        Runtime runtime = runtime(id);
        return verify(runtime, history.queryRefund(id));
    }

    public void preflight(long id) {
        Runtime runtime = runtime(id);
        // 已核实支付仍按原mchid/appid/原总额验证；只有确实付过款才可创建退款。
        var original = history.queryOrder(runtime.payment().getOrderId());
        if (runtime.payment().getTransactionId() == null
                || original.getTransactionId() == null
                || original.getTradeState() == null
                || !Set.of("SUCCESS", "REFUND").contains(original.getTradeState().name())
                || !Objects.equals(
                        original.getTransactionId(), runtime.payment().getTransactionId()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "原支付渠道尚未核实为已付，不能发起退款");
    }

    /** 调用者必须在preflight之后重新验证执行权限；此处只做事务外原参POST。 */
    public Fact submitVerified(long id) {
        Runtime runtime = runtime(id);
        try {
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(runtime.payment().getOutTradeNo());
            request.setOutRefundNo(runtime.refund().getOutRefundNo());
            request.setNotifyUrl(transport.refundNotifyUrl());
            request.setReason("售后退款");
            AmountReq amount = new AmountReq();
            amount.setRefund(runtime.refund().getAmountFen());
            amount.setTotal(runtime.payment().getTotalFen());
            amount.setCurrency("CNY");
            request.setAmount(amount);
            Refund result =
                    new RefundService.Builder()
                            .httpClient(transport.clientForRefund(runtime.sdk()))
                            .build()
                            .create(request);
            return verify(runtime, result);
        } catch (Exception error) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "退款受理结果未确认，请查询原退款号");
        }
    }

    private Fact verify(Runtime runtime, Refund result) {
        if (result == null
                || runtime.payment().getTransactionId() == null
                || result.getTransactionId() == null
                || !Objects.equals(result.getOutRefundNo(), runtime.refund().getOutRefundNo())
                || !Objects.equals(result.getOutTradeNo(), runtime.payment().getOutTradeNo())
                || !Objects.equals(result.getTransactionId(), runtime.payment().getTransactionId())
                || result.getAmount() == null
                || result.getAmount().getRefund() == null
                || result.getAmount().getTotal() == null
                || result.getAmount().getRefund() != runtime.refund().getAmountFen()
                || result.getAmount().getTotal() != runtime.payment().getTotalFen()
                || !"CNY".equals(result.getAmount().getCurrency())
                || result.getStatus() == null)
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "退款响应身份或金额未通过核验");
        return new Fact(
                result.getStatus().name(),
                result.getRefundId(),
                result.getSuccessTime() == null ? null : Instant.parse(result.getSuccessTime()));
    }
}
