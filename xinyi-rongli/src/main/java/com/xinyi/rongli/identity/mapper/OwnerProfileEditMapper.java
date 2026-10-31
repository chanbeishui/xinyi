package com.xinyi.rongli.identity.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 车主选填档案编辑（I02 API_012）。realName→vi_owner_profile.service_name（服务称呼，非实名核验）；
 * defaultAddressId→vi_address.is_default 切换（先校验本人归属）。完整地址簿 CRUD 属 A02，此处仅默认切换。
 */
public interface OwnerProfileEditMapper {

    /** 幂等写入服务称呼（uk_owner_profile）。 */
    int upsertServiceName(@Param("accountId") long accountId, @Param("serviceName") String serviceName);

    /** 校验地址属于本人且未删除（>0 才可设为默认）。 */
    int countOwnedAddress(@Param("accountId") long accountId, @Param("addressId") long addressId);

    int clearDefaultAddresses(@Param("accountId") long accountId);

    int setDefaultAddress(@Param("accountId") long accountId, @Param("addressId") long addressId);
}
