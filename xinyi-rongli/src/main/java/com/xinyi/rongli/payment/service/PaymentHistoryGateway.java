package com.xinyi.rongli.payment.service;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.payment.gateway.WechatPrepayGateway;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.platform.error.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.jsapi.*;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.*;
import com.wechat.pay.java.service.refund.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Objects;
/** 供回调/对账/退款服务消费的内部适配器，无客户可直接调用的HTTP入口；不修改资金事实。 */
@Service
public class PaymentHistoryGateway {
 private final PaymentMapper mapper;private final PaymentConfiguration configuration;private final WechatPrepayGateway gateway;private final EmployeeDirectoryGuardMapper directory;private final TransactionTemplate tx;
 public PaymentHistoryGateway(PaymentMapper mapper,PaymentConfiguration configuration,WechatPrepayGateway gateway,EmployeeDirectoryGuardMapper directory,PlatformTransactionManager manager){this.mapper=mapper;this.configuration=configuration;this.gateway=gateway;this.directory=directory;tx=new TransactionTemplate(manager);}
 private record Runtime(PaymentTransaction payment,Config sdk){}
 private Runtime runtime(long orderId){return tx.execute(s->{directory.ensureGuard();directory.lockGuard();mapper.lockOrder(orderId);var p=mapper.lockPayment(orderId);if(p==null||!"WECHAT".equals(p.getPaymentMethod()))throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"原交易不是微信支付");var sdk=configuration.sdk(p);return new Runtime(p,sdk);});}
 public Transaction queryOrder(long orderId){var r=runtime(orderId);try{var input=new QueryOrderByOutTradeNoRequest();input.setMchid(r.payment().getMchid());input.setOutTradeNo(r.payment().getOutTradeNo());var result=new JsapiService.Builder().httpClient(gateway.client(r.sdk())).build().queryOrderByOutTradeNo(input);identity(r.payment(),result);return result;}catch(Exception e){throw unavailable();}}
 public Refund queryRefund(long refundId){var row=mapper.refund(refundId);if(row==null)throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);var r=runtime(((Number)row.get("order_id")).longValue());try{var input=new QueryByOutRefundNoRequest();input.setOutRefundNo((String)row.get("out_refund_no"));var result=new RefundService.Builder().httpClient(gateway.client(r.sdk())).build().queryByOutRefundNo(input);if(!Objects.equals(result.getOutRefundNo(),row.get("out_refund_no"))||!Objects.equals(result.getOutTradeNo(),r.payment().getOutTradeNo()))throw new IllegalArgumentException();return result;}catch(Exception e){throw unavailable();}}
 public Transaction readNotification(long orderId,RequestParam request){var r=runtime(orderId);try{long timestamp=Long.parseLong(request.getMessage().split("\n",2)[0]);long now=java.time.Instant.now().getEpochSecond();if(timestamp<now-300||timestamp>now+300)throw new IllegalArgumentException();var result=tx.execute(s->{directory.ensureGuard();directory.lockGuard();return configuration.parseNotification(r.payment(),request);});identity(r.payment(),result);return result;}catch(Exception e){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"微信通知验签、解密或原交易身份校验失败");}}
 private static void identity(PaymentTransaction p,Transaction t){if(t==null||!Objects.equals(t.getMchid(),p.getMchid())||!Objects.equals(t.getAppid(),p.getAppId())||!Objects.equals(t.getOutTradeNo(),p.getOutTradeNo())||t.getAmount()==null||t.getAmount().getTotal()==null||t.getAmount().getTotal().longValue()!=p.getTotalFen()||!"CNY".equals(t.getAmount().getCurrency()))throw new IllegalArgumentException();}
 private static ViException unavailable(){return new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE,"历史交易查询未确认，请稍后重试");}
}
