package com.xinyi.rongli.owner.mapper;

import com.xinyi.rongli.owner.domain.ViOwnerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 车主运营档案写 Mapper（A03）。行锁+CAS；不触碰微信标识/已验证手机。 */
@Mapper
public interface ViOwnerProfileMapper {

    ViOwnerProfile selectByIdForUpdate(@Param("id") long id);

    int adminUpdate(@Param("id") long id, @Param("serviceName") String serviceName,
                    @Param("serviceFlag") String serviceFlag, @Param("internalRemark") String internalRemark,
                    @Param("maintainedBy") Long maintainedBy, @Param("expectedVersion") long expectedVersion);
}