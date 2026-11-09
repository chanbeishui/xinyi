package com.xinyi.rongli.training.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
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

    /** 不依赖“当前生效政策==冻结政策”，因此正常发布新课不会撤销已冻结学员的有效进度。 */
    public boolean allPassed(TrainingPlan plan, TrainingRun run) {
        List<FrozenTrainingCourse> selected = courses(plan);
        if (selected.isEmpty()
                || !Objects.equals(run.getPlanSnapshotId(), plan.getId())
                || !Objects.equals(run.getApplicationId(), plan.getApplicationId())) return false;
        Map<Long, Map<String, Object>> facts = new HashMap<>();
        for (var row : mapper.requiredPassedFacts(run.getId()))
            facts.put(number(row.get("training_id")), row);
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
