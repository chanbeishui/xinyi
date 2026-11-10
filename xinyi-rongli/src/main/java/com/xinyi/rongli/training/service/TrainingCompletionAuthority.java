package com.xinyi.rongli.training.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
import com.xinyi.rongli.qualification.domain.QualificationSubmission;
import com.xinyi.rongli.training.domain.*;
import com.xinyi.rongli.training.dto.FrozenTrainingCourse;
import com.xinyi.rongli.training.mapper.LearningMapper;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Q01与Q03共用真实培训完成判据；独立PASSED行、旧公司或旧材料证明均不能恢复资格提交通道。 */
@Service
public class TrainingCompletionAuthority {
    private final LearningMapper mapper;

    public TrainingCompletionAuthority(LearningMapper mapper) {
        this.mapper = mapper;
    }

    public Long currentProof(DriverProfile profile, QualificationApplication application) {
        if (profile.getOwnerDeptId() == null || application.getMaterialHash() == null) return null;
        TrainingPlan plan = mapper.plan(application.getId(), profile.getTrainingContextRevision());
        if (plan == null
                || !Objects.equals(plan.getOwnerDeptId(), profile.getOwnerDeptId())
                || !Objects.equals(plan.getApplicationId(), application.getId())) return null;
        TrainingRun run = mapper.run(plan.getId(), application.getMaterialRevision());
        if (run == null
                || !Objects.equals(run.getApplicationId(), application.getId())
                || !equal(run.getMaterialHash(), application.getMaterialHash())
                || !"PASSED".equals(run.getStatus())
                || run.getProviderVerificationId() == null
                || run.getCompletedAt() == null
                || run.getCompletedAt().isAfter(Instant.now())) return null;
        var proof = mapper.trainingProof(run.getProviderVerificationId());
        if (proof == null
                || number(proof.get("application_id")) != application.getId()
                || number(proof.get("material_revision")) != application.getMaterialRevision()
                || !equal((byte[]) proof.get("material_hash"), application.getMaterialHash())
                || !allPassed(plan, run)) return null;
        return run.getProviderVerificationId();
    }

    /** 审核只核对原提交时刻已经完成的冻结集合，不依赖当前活跃政策或原MINI登录。 */
    public Long frozenProof(QualificationSubmission submission) {
        if (submission == null
                || submission.getCompanyRevision() == null
                || submission.getSubmittedAt() == null) return null;
        TrainingPlan plan =
                mapper.plan(submission.getApplicationId(), submission.getCompanyRevision());
        if (plan == null || !Objects.equals(plan.getOwnerDeptId(), submission.getOwnerDeptId()))
            return null;
        TrainingRun run = mapper.run(plan.getId(), submission.getMaterialRevision());
        if (run == null
                || !Objects.equals(run.getApplicationId(), submission.getApplicationId())
                || !equal(run.getMaterialHash(), submission.getMaterialHash())
                || !"PASSED".equals(run.getStatus())
                || run.getProviderVerificationId() == null
                || run.getCompletedAt() == null
                || run.getCompletedAt().isAfter(submission.getSubmittedAt())) return null;
        var proof = mapper.trainingProof(run.getProviderVerificationId());
        if (proof == null
                || number(proof.get("application_id")) != submission.getApplicationId()
                || number(proof.get("material_revision")) != submission.getMaterialRevision()
                || !equal((byte[]) proof.get("material_hash"), submission.getMaterialHash())
                || asInstant(proof.get("verified_at")) == null
                || asInstant(proof.get("verified_at")).isAfter(submission.getSubmittedAt()))
            return null;
        List<?> ids = ViJson.read(submission.getProviderResultIds(), List.class);
        if (ids.stream()
                .noneMatch(id -> run.getProviderVerificationId().toString().equals(id.toString())))
            return null;
        return passedFacts(
                        plan,
                        mapper.requiredPassedFactsAt(run.getId(), submission.getSubmittedAt()))
                ? run.getProviderVerificationId()
                : null;
    }

    private static Instant asInstant(Object value) {
        if (value instanceof Instant instant) return instant;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.time.LocalDateTime time)
            return time.toInstant(java.time.ZoneOffset.UTC);
        return null;
    }

    /** 不依赖“当前生效政策==冻结政策”，因此正常发布新课不会撤销已冻结学员的有效进度。 */
    public boolean allPassed(TrainingPlan plan, TrainingRun run) {
        List<FrozenTrainingCourse> selected = courses(plan);
        if (selected.isEmpty()
                || !Objects.equals(run.getPlanSnapshotId(), plan.getId())
                || !Objects.equals(run.getApplicationId(), plan.getApplicationId())) return false;
        return passedFacts(plan, mapper.requiredPassedFacts(run.getId()));
    }

    private boolean passedFacts(TrainingPlan plan, List<Map<String, Object>> rows) {
        List<FrozenTrainingCourse> selected = courses(plan);
        if (selected.isEmpty()) return false;
        Map<Long, Map<String, Object>> facts = new HashMap<>();
        for (var row : rows) facts.put(number(row.get("training_id")), row);
        for (var course : selected) {
            var fact = facts.get(course.trainingId());
            if (fact == null
                    || !"PASSED".equals(fact.get("status"))
                    || number(fact.get("verified_duration_millis"))
                            < course.requiredDurationSeconds() * 1000L
                    || fact.get("passed_score") == null
                    || number(fact.get("passed_score")) < course.passScore()) return false;
        }
        return true;
    }

    public static List<FrozenTrainingCourse> courses(TrainingPlan plan) {
        return Arrays.asList(ViJson.read(plan.getCourseSnapshot(), FrozenTrainingCourse[].class));
    }

    public static boolean equal(byte[] left, byte[] right) {
        return left != null && right != null && MessageDigest.isEqual(left, right);
    }

    private static long number(Object value) {
        return value == null ? Long.MIN_VALUE : ((Number) value).longValue();
    }
}
