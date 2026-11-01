package com.xinyi.rongli.owner.dto;

import java.util.List;

/** 车主档案投影（A03，OwnerRecord）。默认脱敏；phoneVerified 来自认证服务手机核验时刻，不由运营字段覆盖。 */
public record OwnerView(
        String ownerId,
        String nickname,
        String serviceName,
        String phoneMask,
        boolean phoneVerified,
        long vehicleCount,
        long orderCount,
        String lastServiceAt,
        String serviceFlag,
        String internalRemark,
        long version,
        List<String> allowedActions) {
}