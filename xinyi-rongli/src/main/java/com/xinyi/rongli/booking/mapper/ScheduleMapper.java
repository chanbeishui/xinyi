package com.xinyi.rongli.booking.mapper;
import org.apache.ibatis.annotations.Param;
import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import java.time.Instant;
import java.util.List;
public interface ScheduleMapper {
 List<ViQuotaSlot> candidates(@Param("start") Instant start,@Param("end") Instant end);
}
