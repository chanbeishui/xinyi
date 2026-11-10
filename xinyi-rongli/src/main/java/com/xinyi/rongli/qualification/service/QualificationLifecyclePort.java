package com.xinyi.rongli.qualification.service;

import java.time.Instant;

/** 审核与资格生命周期的事务边界：资格事实、会话能力版本及到期任务必须共同提交。 */
public interface QualificationLifecyclePort {
    /** 仅可在审核事务中调用。snapshotId为空表示驳回或历史补正，不创建生效期限； 角色能力变化不注销登录、不自动切换身份，也不修改司机行政开关、听单或在手责任。 */
    void decisionRecorded(
            long accountId,
            long driverId,
            long reviewId,
            Long snapshotId,
            long qualificationRevision,
            Instant expiresAt);
}
