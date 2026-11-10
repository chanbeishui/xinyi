package com.xinyi.rongli.driver.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.platform.error.*;
import java.time.*;
import org.springframework.stereotype.Service;

/** 新承接资格与身份视角分离；停新单或资格异常不能删除已有任务，D01/D02后续复用此基础规则。 */
@Service
public class DriverNewAssignmentPolicy {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final Clock clock;

    public DriverNewAssignmentPolicy(
            @org.springframework.beans.factory.annotation.Qualifier("viDeadlineClock")
                    Clock clock) {
        this.clock = clock;
    }

    public String qualificationBlock(DriverProfile row, Instant now) {
        if (!"ACTIVE".equals(row.getAccountStatus()) || !"ACTIVE".equals(row.getSecurityStatus()))
            return "ACCOUNT_UNAVAILABLE";
        if (!"APPROVED".equals(row.getQualificationStatus())) return "QUALIFICATION_NOT_APPROVED";
        // 新生效快照只适用于批准时公司epoch；公司A转B再转A也不能复活旧资格的新单准入。
        // 旧档案没有快照指针时保留既有判据，不从NULL伪造本轮已批准快照。
        if (row.getEffectiveSnapshotId() != null
                && (!java.util.Objects.equals(
                                row.getOwnerDeptId(), row.getQualificationOwnerDeptId())
                        || !java.util.Objects.equals(
                                row.getTrainingContextRevision(),
                                row.getQualificationCompanyRevision())))
            return "QUALIFICATION_COMPANY_CHANGED";
        if (row.getLicenseClass() == null || row.getLicenseClass().isBlank())
            return "LICENSE_CLASS_UNKNOWN";
        // 驾驶证是到期日，上海当日仍有效；复审是精确UTC截止时刻，两者不能混用同一比较规则。
        if (!row.getLicenseLongTerm()
                && (row.getLicenseValidTo() == null
                        || row.getLicenseValidTo()
                                .atZone(BUSINESS_ZONE)
                                .toLocalDate()
                                .isBefore(now.atZone(BUSINESS_ZONE).toLocalDate())))
            return "LICENSE_EXPIRED_OR_UNKNOWN";
        if (row.getReviewDueAt() == null || !row.getReviewDueAt().isAfter(now))
            return "REVIEW_EXPIRED_OR_UNKNOWN";
        return null;
    }

    public void requireQualification(DriverProfile row) {
        String reason = qualificationBlock(row, clock.instant());
        if (reason != null)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "资格、准驾、证件或复审尚未明确有效：" + reason);
    }

    public String availabilityReason(DriverProfile row, boolean companyActive, boolean busy) {
        if (row.getOwnerDeptId() == null) return "DRIVER_COMPANY_UNASSIGNED";
        if (!companyActive) return "DRIVER_COMPANY_UNAVAILABLE";
        String qualification = qualificationBlock(row, clock.instant());
        if (qualification != null) return qualification;
        if (!row.getNewAssignmentEnabled()) return "NEW_ASSIGNMENTS_DISABLED";
        if (!row.getListeningEnabled()) return "LISTENING_DISABLED";
        if (busy) return "OCCUPIED";
        // A04只恢复行政开关；位置新鲜度、半径、处罚轮次等尚待D01/D02，绝不能在此宣称可邀约。
        return "DISPATCH_ELIGIBILITY_NOT_READY";
    }
}
