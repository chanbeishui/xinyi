package com.xinyi.rongli.qualification.mapper;

import com.xinyi.rongli.qualification.domain.*;
import com.xinyi.rongli.qualification.dto.QualificationAdminRequests;

import org.apache.ibatis.annotations.Param;

import java.util.*;

/** 查询绑定实际申请和公司，事务锁由统一审核服务按目录、账号、档案、申请顺序持有。 */
public interface QualificationAdminMapper {
    List<Long> list(
            @Param("scope") Set<Long> scope,
            @Param("query") QualificationAdminRequests.Query query,
            @Param("offset") long offset);

    long count(
            @Param("scope") Set<Long> scope,
            @Param("query") QualificationAdminRequests.Query query);

    Map<String, Object> context(long applicationId);

    Map<String, Object> lockAccount(long accountId);

    Map<String, Object> lockEvidence(long fileId);

    List<Long> boundUsers(long accountId);

    Map<String, Object> verifiedPhone(long accountId);

    List<Map<String, Object>> usersWithPhone();

    QualificationSubmission submission(
            @Param("application") long application, @Param("revision") long revision);

    QualificationSubmission submissionById(long id);

    void insertSubmission(QualificationSubmission row);

    int sealSubmission(@Param("id") long id, @Param("seal") byte[] seal);

    QualificationReview review(long id);

    List<QualificationReview> reviews(long applicationId);

    int normalReviewCount(long applicationId);

    void insertReview(QualificationReview row);

    int sealReviewOpinion(@Param("id") long id, @Param("cipher") byte[] cipher);

    QualificationSnapshot snapshot(long id);

    void insertSnapshot(QualificationSnapshot row);

    int sealSnapshot(@Param("id") long id, @Param("seal") byte[] seal);

    int decideApplication(
            @Param("id") long id,
            @Param("version") long version,
            @Param("review") QualificationReview review);

    int approveProfile(
            @Param("id") long id,
            @Param("version") long version,
            @Param("snapshot") QualificationSnapshot snapshot);
}
