package com.xinyi.rongli.dto;

/**
 * 代驾与站点端请求体。
 */
public final class DriverDtos
{
    private DriverDtos()
    {
    }

    public static class QualificationDraft
    {
        public String realName;
        public String idCardNo;
        public String licenseClass;
        public String licenseValidTo;
        public Boolean licenseLongTerm;
        public String evidenceFileIds;
    }

    public static class QualificationCreate extends QualificationDraft
    {
    }

    public static class ListeningReq
    {
        public Boolean enabled;
    }

    public static class LocationReq
    {
        public Double lng;
        public Double lat;
        public String capturedAt;
    }

    public static class InviteResponseReq
    {
        public Boolean accept;
        public String reason;
    }

    public static class ActionReq
    {
        public Long version;
        public String note;
    }

    public static class DriverCancelReq
    {
        public Long version;
        public String reason;
    }

    public static class EvidenceCreateReq
    {
        public Integer nodeNo;
        public Long mileageKm;
        public String mileageSource;
        public String mileageReason;
    }

    public static class PhotoReq
    {
        public String photoType;
        public Long fileId;
        public String clientCaptureId;
        public String capturedAt;
        public Double lng;
        public Double lat;
    }

    public static class MileageReq
    {
        public String mileageKm;
        public String source;
        public String reason;
    }

    public static class EvidenceReadyReq
    {
        public Long version;
    }

    public static class HandoverCreateReq
    {
        public String handoverType;
    }

    public static class HandoverConfirmReq
    {
        public String code;
        public Long version;
        public EvidenceCreateReq evidence;
    }

    public static class ExceptionReq
    {
        public String type;
        public String description;
        public String fileIds;
    }

    public static class LicenseReviewReq
    {
        public String ownerName;
        public String plateNo;
        public String vin;
        public String registeredOn;
        public Long mainFileId;
        public Long subFileId;
        public Boolean verified;
    }

    public static class ResultReq
    {
        public String conclusion;
        public String items;
        public String inspectedAt;
    }

    public static class CorrectionReq
    {
        public String conclusion;
        public String items;
        public String reason;
    }

    public static class CapacityChangeReq
    {
        public Integer totalQuota;
        public Boolean enabled;
    }

    public static class StationUpdateReq
    {
        public String name;
        public String address;
        public java.math.BigDecimal lng;
        public java.math.BigDecimal lat;
        public Integer serviceRadiusMeter;
        public String supportedVehicleCodes;
        public String businessHours;
        public Boolean enabled;
    }

    public static class ContactAccessReq
    {
        public String purpose;
    }

    public static class ProgressReq
    {
        public String sessionId;
        public Integer deltaSeconds;
        public Integer positionSeconds;
    }

    public static class AttemptSubmitReq
    {
        public Long attemptId;
        public java.util.Map<String, String> answers;
    }

    public static class TrainingQuestionReq
    {
        public String questionCode;
        public String questionType;
        public String prompt;
        public java.util.List<String> options;
        public String correctOptionCodes;
        public Integer sortNo;
    }

    public static class TrainingCourseReq
    {
        public String courseKey;
        public String title;
        public String description;
        public Long mediaFileId;
        public Integer requiredDurationSeconds;
        public Integer passScore;
        public Integer attemptTimeLimitSeconds;
        public java.util.List<TrainingQuestionReq> questions;
    }

    /** 申诉提交 */
    public static class AppealReq
    {
        public String reason;
        public String evidenceFileIds;
    }
}
