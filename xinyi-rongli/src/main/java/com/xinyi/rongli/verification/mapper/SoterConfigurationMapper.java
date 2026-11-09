package com.xinyi.rongli.verification.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 沿用已发布小程序公共凭据，不为设备认证创建腾讯云商户或密钥。 */
public interface SoterConfigurationMapper {
    @Select("""
            SELECT id FROM vi_integration_profile
            WHERE owner_dept_id=#{owner} AND environment=#{environment}
                AND kind='WECHAT_APP' AND config_key='DEFAULT' AND lifecycle='ACTIVE'
            ORDER BY id
            """)
    List<Long> profiles(@Param("owner") long owner, @Param("environment") String environment);
}
