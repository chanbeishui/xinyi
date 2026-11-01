package com.xinyi.rongli.pricing.mapper;

import com.xinyi.rongli.pricing.domain.ViPriceRule;
import com.xinyi.rongli.pricing.dto.PriceQuery;
import org.apache.ibatis.annotations.Param;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ViPriceRuleMapper {
    List<ViPriceRule> selectList(@Param("query") PriceQuery query, @Param("environment") String environment,
                               @Param("deptIds") Set<Long> deptIds, @Param("offset") int offset);
    long countList(@Param("query") PriceQuery query, @Param("environment") String environment, @Param("deptIds") Set<Long> deptIds);
    ViPriceRule selectById(@Param("id") long id);
    ViPriceRule lockById(@Param("id") long id);
    Map<String,Object> lockCompany(@Param("id") long id);
    int maxRevision(@Param("key") String key);
    int countContribution(@Param("id") long id, @Param("userId") long userId);
    int countDict(@Param("type") String type, @Param("code") String code);
    int countStationScope(@Param("stationId") long stationId, @Param("deptIds") Set<Long> deptIds);
    List<ViPriceRule> findPublished(@Param("stationId") long stationId, @Param("vehicle") String vehicle,
            @Param("inspection") String inspection, @Param("environment") String environment, @Param("at") Instant at);
    int countOverlap(@Param("row") ViPriceRule row, @Param("replacesId") Long replacesId);
    int insert(ViPriceRule row);
    int updateDraft(@Param("row") ViPriceRule row, @Param("expectedVersion") long expectedVersion);
    int transition(@Param("row") ViPriceRule row, @Param("expectedVersion") long expectedVersion);
}
