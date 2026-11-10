package com.xinyi.rongli.training.mapper;

import com.xinyi.rongli.training.domain.*;

import org.apache.ibatis.annotations.Param;

import java.util.*;

/** 调用者已持目录→账号/档案→申请锁；持久化层不接受客户端数据权限片段。 */
public interface LearningMapper {
    TrainingPolicy policy(long ownerDeptId);

    void insertPolicy(TrainingPolicy row);

    TrainingPolicyRevision revision(long id);

    int maxPolicyRevision(long id);

    void insertRevision(TrainingPolicyRevision row);

    int pointDraft(TrainingPolicy row);

    int publishRevision(@Param("id") long id, @Param("user") long user);

    int pointActive(TrainingPolicy row);

    TrainingPlan plan(@Param("application") long application, @Param("epoch") long epoch);

    void insertPlan(TrainingPlan row);

    TrainingRun run(@Param("plan") long plan, @Param("material") long material);

    TrainingRun runById(long id);

    void insertRun(TrainingRun row);

    List<TrainingProgress> progresses(long run);

    TrainingProgress progress(@Param("run") long run, @Param("training") long training);

    void insertProgress(TrainingProgress row);

    int saveProgress(TrainingProgress row);

    TrainingPlaybackSession playback(long id);

    void insertPlayback(TrainingPlaybackSession row);

    int savePlayback(TrainingPlaybackSession row);

    TrainingExamAttempt attempt(long id);

    TrainingExamAttempt openAttempt(
            @Param("application") long application, @Param("key") String key);

    TrainingExamAttempt latestAttempt(
            @Param("application") long application, @Param("key") String key);

    TrainingExamAttempt runLatestAttempt(@Param("run") long run, @Param("training") long training);

    int attemptCount(@Param("application") long application, @Param("key") String key);

    int maxAttemptNo(@Param("application") long application, @Param("key") String key);

    void insertAttempt(TrainingExamAttempt row);

    int saveAttempt(TrainingExamAttempt row);

    void insertTrainingProof(Map<String, Object> row);

    int completeRun(
            @Param("id") long id, @Param("version") long version, @Param("proof") long proof);

    Map<String, Object> trainingProof(long id);

    List<Map<String, Object>> requiredPassedFacts(long run);

    List<Map<String, Object>> requiredPassedFactsAt(
            @Param("run") long run, @Param("asOf") java.time.Instant asOf);

    Map<String, Object> currentMediaContext(
            @Param("account") long account, @Param("file") long file);
}
