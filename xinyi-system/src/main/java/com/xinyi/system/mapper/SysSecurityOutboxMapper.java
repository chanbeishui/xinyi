package com.xinyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xinyi.system.domain.SysSecurityOutbox;

/**
 * 安全outbox数据层。
 */
public interface SysSecurityOutboxMapper
{
    int insert(SysSecurityOutbox event);

    List<SysSecurityOutbox> selectPending(@Param("now") Date now, @Param("limit") int limit);

    int markDone(Long eventId);

    int markRetry(@Param("eventId") Long eventId, @Param("retryCount") int retryCount,
            @Param("nextRetryTime") Date nextRetryTime, @Param("lastError") String lastError);
}
