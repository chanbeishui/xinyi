package com.xinyi.rongli.scope.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 站点目录只读（vi_station）与员工列表（sys_user，部门范围过滤）。仅只读。 */
public interface StationScopeReadMapper {

    /** 给定 ID 中真实存在且启用的站点 {id, dept_id}（批量越权/存在性校验）。 */
    List<Map<String, Object>> selectExistingStations(@Param("ids") Set<Long> ids);

    /** 站点公开投影（API_094 获授权站点）。 */
    List<Map<String, Object>> selectStationsByIds(@Param("ids") List<Long> ids);

    /** 部门范围内有效员工 user_id（分页）；deptIds=null 全量。 */
    List<Long> selectEmployeeIds(@Param("deptIds") Set<Long> deptIds, @Param("offset") int offset, @Param("limit") int limit);

    int countEmployees(@Param("deptIds") Set<Long> deptIds);
}