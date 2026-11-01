package com.xinyi.rongli.owner.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 车主档案后台读 Mapper（A03）。数据范围按订单权威 dept_id 过滤；deptIds=null 表示全量，空集合 fail-closed。 */
@Mapper
public interface OwnerAdminReadMapper {

    List<Map<String, Object>> selectOwners(@Param("deptIds") Set<Long> deptIds, @Param("keyword") String keyword,
                                           @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    long countOwners(@Param("deptIds") Set<Long> deptIds, @Param("keyword") String keyword, @Param("status") String status);

    Map<String, Object> selectOwnerById(@Param("id") long id, @Param("deptIds") Set<Long> deptIds);

    long countVisible(@Param("id") long id, @Param("deptIds") Set<Long> deptIds);

    List<Map<String, Object>> selectVehiclesByOwner(@Param("accountId") long accountId,
                                                    @Param("offset") int offset, @Param("limit") int limit);

    long countVehiclesByOwner(@Param("accountId") long accountId);

    List<Map<String, Object>> selectOrdersByOwner(@Param("accountId") long accountId, @Param("deptIds") Set<Long> deptIds,
                                                  @Param("offset") int offset, @Param("limit") int limit);

    long countOrdersByOwner(@Param("accountId") long accountId, @Param("deptIds") Set<Long> deptIds);
}