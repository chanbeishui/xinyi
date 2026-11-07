package com.xinyi.rongli.refund.service;

import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import java.util.Base64;
import org.springframework.stereotype.Service;

/** 人工资金操作说明只留加密审计；AAD隔离操作者、动作和真实记录，禁止跨记录挪用密文。 */
@Service
public class PaymentOperationReason {
    private final IntegrationCrypto crypto;

    public PaymentOperationReason(IntegrationCrypto crypto) {
        this.crypto = crypto;
    }

    public String envelope(Actor actor, String action, long recordId, String reason) {
        if (!actor.isAdmin()) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        String aad = "P04:ADMIN:" + actor.sysUserId() + ":" + action + ":" + recordId;
        return Base64.getEncoder().encodeToString(crypto.envelope(reason, aad));
    }
}
