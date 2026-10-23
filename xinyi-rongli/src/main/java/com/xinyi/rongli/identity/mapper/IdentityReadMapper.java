package com.xinyi.rongli.identity.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * I01 身份上下文只读投影：登录/档案计算 availableRoles、driverQualification、employeeLink 时，
 * 读取代驾资格、员工绑定、站点授权的权威状态。仅只读；这些表的写入分别属代驾资格、
 * 手机号自动关联、后台站点预授权等任务，I01 不写、不复制其业务规则。
 * 空结果按"无该资格/关联"处理（新账号即 OWNER-only）。
 */
public interface IdentityReadMapper {

    /** vi_driver_profile.qualification_status（无档案返回 null）。 */
    String selectDriverQualificationStatus(@Param("accountId") long accountId);

    /** 已批准且证件准驾当前有效的代驾资格数（1=可授 DRIVER 角色）。 */
    int countDriverApprovedCurrent(@Param("accountId") long accountId);

    /** 当前未撤销的员工绑定 {sys_user_id, status}；无则返回 null（uk_active_account 保证至多一条）。 */
    Map<String, Object> selectActiveBinding(@Param("accountId") long accountId);

    /** 某 sys_user 已启用的站点授权数（>0 才可能有 STATION 工作能力）。 */
    int countEnabledStationGrants(@Param("sysUserId") long sysUserId);

    /** 某 sys_user 已启用的站点 ID 列表（升序）；STATION 视角的 stationIds。 */
    List<Long> selectGrantedStationIds(@Param("sysUserId") long sysUserId);
}
