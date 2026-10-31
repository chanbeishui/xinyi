package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/** vi_employee_binding 写/读（自动关联+后台生命周期）。生成列 active_account/active_user 不写；唯一键为第二道防线。 */
public interface ViEmployeeBindingMapper {

    int insert(ViEmployeeBinding binding);

    /** 当前账号未撤销的绑定（uk_active_account 保证至多一条）。 */
    ViEmployeeBinding selectActiveByAccount(@Param("accountId") long accountId);

    /** 同应用下该员工是否已被（其他账号）未撤销关联占用（uk_active_user）。 */
    int countActiveByAppUser(@Param("appId") String appId, @Param("sysUserId") long sysUserId);

    ViEmployeeBinding selectById(@Param("id") long id);

    ViEmployeeBinding selectByIdForUpdate(@Param("id") long id);

    /** 账号最近一条绑定（含 SUSPENDED/REVOKED，U02 状态投影用）。 */
    ViEmployeeBinding selectLatestByAccount(@Param("accountId") long accountId);

    /** 员工当前未撤销绑定（目录钩子用，uk_active_user 保证同应用至多一条）。 */
    ViEmployeeBinding selectActiveBySysUser(@Param("sysUserId") long sysUserId);

    /** 暂停：ACTIVE→SUSPENDED，保留唯一占用与历史（CAS）。 */
    int suspend(@Param("id") long id, @Param("reason") String reason, @Param("expectedVersion") long expectedVersion);

    /** 撤销：ACTIVE/SUSPENDED→REVOKED，置 auto_rebind_blocked=1 防自动复绑（CAS）。 */
    int revoke(@Param("id") long id, @Param("revokedBy") long revokedBy, @Param("reason") String reason,
               @Param("revokedAt") Instant revokedAt, @Param("expectedVersion") long expectedVersion);

    /** 允许本人重新核验：仅清 auto_rebind_blocked，不改状态、不直接绑定（CAS）。 */
    int allowRematch(@Param("id") long id, @Param("expectedVersion") long expectedVersion);
}
