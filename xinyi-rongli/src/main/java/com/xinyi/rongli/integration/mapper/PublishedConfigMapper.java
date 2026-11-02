package com.xinyi.rongli.integration.mapper;
import java.util.*;
import org.apache.ibatis.annotations.Param;
public interface PublishedConfigMapper {
 List<Long> companyPolicies(@Param("stationId")long stationId,@Param("environment")String environment);
 Map<String,Object> lockPublished(long id);
 int publicationValid(@Param("profileId")long profileId,@Param("revisionId")long revisionId,@Param("hash")String hash);
 int usableSecret(@Param("profileId")long profileId,@Param("id")long id,@Param("slot")String slot,@Param("kind")String kind);
}
