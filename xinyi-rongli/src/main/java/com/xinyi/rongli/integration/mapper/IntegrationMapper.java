package com.xinyi.rongli.integration.mapper;
import com.xinyi.rongli.integration.domain.*;
import com.xinyi.rongli.integration.dto.IntegrationRequests.Query;
import org.apache.ibatis.annotations.Param;
import java.util.*;
public interface IntegrationMapper {
 List<IntegrationProfile> profiles(@Param("query")Query query,@Param("environment")String environment,@Param("departments")Set<Long> departments);
 long countProfiles(@Param("query")Query query,@Param("environment")String environment,@Param("departments")Set<Long> departments);
 IntegrationProfile profile(long id);IntegrationProfile lockProfile(long id);int insertProfile(IntegrationProfile row);
 IntegrationRevision revision(@Param("profileId")long profileId,@Param("id")long id);
 IntegrationRevision revisionByHash(@Param("profileId")long profileId,@Param("hash")String hash);
 List<IntegrationRevision> revisions(@Param("profileId")long profileId,@Param("offset")int offset,@Param("limit")int limit);long countRevisions(long id);int insertRevision(IntegrationRevision row);
 int pointDraft(@Param("id")long id,@Param("revisionId")long revisionId,@Param("sequence")long sequence,@Param("expectedVersion")long expectedVersion);
 int pointActive(@Param("id")long id,@Param("revisionId")long revisionId,@Param("userId")long userId,@Param("expectedVersion")long expectedVersion);
 IntegrationSecret secret(@Param("profileId")long profileId,@Param("id")long id,@Param("slot")String slot);int insertSecret(IntegrationSecret row);
 int clearSecret(@Param("profileId")long profileId,@Param("id")long id,@Param("userId")long userId);
 int wasPublished(@Param("profileId")long profileId,@Param("revisionId")long revisionId);List<IntegrationRevision> publishedRevisions(long profileId);
 IntegrationCheck check(@Param("profileId")long profileId,@Param("id")long id);IntegrationCheck latestCheck(@Param("profileId")long profileId,@Param("revisionId")long revisionId);int insertCheck(IntegrationCheck row);
 int insertPublication(IntegrationPublication row);IntegrationPublication publication(@Param("profileId")long profileId,@Param("id")long id);
 List<IntegrationPublication> publications(@Param("profileId")long profileId,@Param("offset")int offset,@Param("limit")int limit);long countPublications(long id);
 List<IntegrationRecipient> recipients(@Param("environment")String environment,@Param("departments")Set<Long> departments,@Param("offset")int offset,@Param("limit")int limit);
 long countRecipients(@Param("environment")String environment,@Param("departments")Set<Long> departments);
 IntegrationRecipient recipient(long id);IntegrationRecipient lockRecipient(long id);int insertRecipient(IntegrationRecipient row);
 int setRecipientCipher(@Param("id")long id,@Param("cipher")byte[] cipher);int revokeRecipient(@Param("id")long id,@Param("expectedVersion")long expectedVersion);
 String accountApp(long id);long accountCount(String appId);long paymentCount(long id);
 Map<String,Object> lockUser(long id);Map<String,Object> lockDept(long id);String now();
}
