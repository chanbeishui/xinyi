package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import org.apache.ibatis.annotations.Param;

/** vi_employee_binding 写/读（自动关联）。生成列 active_account/active_user 不写；唯一键为第二道防线。 */
public interface ViEmployeeBindingMapper {

    int insert(ViEmployeeBinding binding);

    /** 当前账号未撤销的绑定（uk_active_account 保证至多一条）。 */
    ViEmployeeBinding selectActiveByAccount(@Param("accountId") long accountId);

    /** 同应用下该员工是否已被（其他账号）未撤销关联占用（uk_active_user）。 */
    int countActiveByAppUser(@Param("appId") String appId, @Param("sysUserId") long sysUserId);
}
