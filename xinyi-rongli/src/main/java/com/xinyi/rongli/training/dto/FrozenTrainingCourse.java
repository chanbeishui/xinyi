package com.xinyi.rongli.training.dto;

/** 申请计划的不可变业务快照；不含标准答案，contentHash是带密钥指纹且不对MINI投影。 */
public record FrozenTrainingCourse(
        long trainingId,
        String courseKey,
        int courseVersion,
        String title,
        String description,
        long mediaFileId,
        long mediaDurationMillis,
        String contentHash,
        int requiredDurationSeconds,
        int passScore,
        int attemptTimeLimitSeconds) {}
