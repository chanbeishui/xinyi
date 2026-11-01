package com.xinyi.rongli.booking.mapper;
import java.util.*;
import org.apache.ibatis.annotations.Param;
import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import com.xinyi.rongli.booking.domain.ViQuote;
public interface QuoteMapper {
 ViQuotaSlot slot(@Param("id") long id);
 List<ViQuotaSlot> slots(@Param("stationId") long stationId);
 List<Long> stations();
 int activeDepartment(@Param("id") long id);
 String dictLabel(@Param("type") String type,@Param("code") String code);
 String lockConfig();
 String ruleContent(@Param("id") long id);
 int insert(ViQuote quote);
 ViQuote find(@Param("id") long id);
}
