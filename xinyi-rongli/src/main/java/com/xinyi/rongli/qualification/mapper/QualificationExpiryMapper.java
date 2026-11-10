package com.xinyi.rongli.qualification.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 到期任务仅索引服务器已批准快照，不用当前配置反推历史截止。 */
public interface QualificationExpiryMapper {
    Long account(@Param("driverId") long driverId);

    Map<String, Object> review(@Param("id") long reviewId);

    List<Long> missingTimers();

    int expire(
            @Param("id") long driverId,
            @Param("snapshotId") long snapshotId,
            @Param("revision") long revision,
            @Param("version") long version);
}
