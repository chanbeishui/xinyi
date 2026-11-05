package com.xinyi.rongli.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.PaySettings;

/**
 * 支付参数快照入口。
 *
 * <p>第三方参数统一在「第三方服务 → 微信支付商户」维护，发布时由
 * {@code RuntimeProjection} 投影到 {@code sys_config}；本类只负责把当前生效快照读出来，
 * 供下单、退款、回调各链路复用同一个判定口径。配置的编辑与校验不再经过这里。</p>
 */
@Service
public class PayConfigService
{
    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    /** 供下单/退款/回调链路使用的配置快照；每次调用重新加载，发布后下一次请求即生效 */
    public PaySettings settings()
    {
        return PaySettings.load(config, crypto);
    }
}
