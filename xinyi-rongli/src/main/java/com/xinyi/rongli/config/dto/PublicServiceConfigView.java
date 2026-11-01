package com.xinyi.rongli.config.dto;

import java.util.List;

/**
 * 公开服务配置（C01，PublicServiceConfig）。仅公开白名单（发布值），不含供应商密钥/内部财务主体/员工名单；
 * 缺已发布协议不得假造版本开放下单；第三方未配置不宣称可支付。
 */
public record PublicServiceConfigView(
        long version,
        String companyName,
        String supportPhone,
        String termsVersion,
        String termsText,
        String privacyVersion,
        String privacyText,
        List<String> serviceCities,
        boolean bookingEnabled,
        List<String> availablePaymentMethods,
        String paymentNotice) {
}