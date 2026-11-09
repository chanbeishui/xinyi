package com.xinyi.rongli.verification.mapper;

import com.xinyi.rongli.verification.domain.ManualVerificationRecord;
import com.xinyi.rongli.verification.dto.ManualVerificationRequests;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/** 部门条件来自统一Service，空集合必须生成AND 1=0。 */
public interface ManualVerificationMapper {
    List<ManualVerificationRecord> list(
            @Param("q") ManualVerificationRequests.Query query,
            @Param("departments") Set<Long> departments);

    long count(
            @Param("q") ManualVerificationRequests.Query query,
            @Param("departments") Set<Long> departments);

    List<Map<String, Object>> applications(
            @Param("q") ManualVerificationRequests.Query query,
            @Param("departments") Set<Long> departments);

    long applicationCount(
            @Param("q") ManualVerificationRequests.Query query,
            @Param("departments") Set<Long> departments);

    ManualVerificationRecord find(long id);

    ManualVerificationRecord lock(long id);

    Map<String, Object> context(long applicationId);

    long applicantUser(@Param("account") long account, @Param("user") long user);

    long pending(
            @Param("application") long application,
            @Param("epoch") long epoch,
            @Param("material") long material,
            @Param("type") String type);

    void insert(ManualVerificationRecord row);

    int setNoteCipher(ManualVerificationRecord row);

    List<Long> boundUsers(long account);

    Map<String, Object> verifiedPhoneLookup(long account);

    List<Map<String, Object>> usersWithPhone();

    String reviewerPhone(long user);

    int review(ManualVerificationRecord row);

    int linkProof(@Param("id") long id, @Param("proof") long proof);
}
