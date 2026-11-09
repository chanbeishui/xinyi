package com.xinyi.rongli.file.domain;

import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;

/** 私有文件持久化实体；对象键、租约和上传者不得直接作为HTTP响应。 */
public class PrivateFile extends ViRecord {
    private Long uploaderAccountId;

    public Long getUploaderAccountId() {
        return uploaderAccountId;
    }

    public void setUploaderAccountId(Long value) {
        uploaderAccountId = value;
    }

    private Long uploaderUserId;

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public void setUploaderUserId(Long value) {
        uploaderUserId = value;
    }

    private String purpose;

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String value) {
        purpose = value;
    }

    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long value) {
        orderId = value;
    }

    private Long applicationId;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    private Long stationId;

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long value) {
        stationId = value;
    }

    private String objectKey;

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String value) {
        objectKey = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String value) {
        mimeType = value;
    }

    private Long serverSize;

    public Long getServerSize() {
        return serverSize;
    }

    public void setServerSize(Long value) {
        serverSize = value;
    }

    private byte[] serverSha256;

    public byte[] getServerSha256() {
        return serverSha256;
    }

    public void setServerSha256(byte[] value) {
        serverSha256 = value;
    }

    private Long declaredSize;

    public Long getDeclaredSize() {
        return declaredSize;
    }

    public void setDeclaredSize(Long value) {
        declaredSize = value;
    }

    private String declaredMime;

    public String getDeclaredMime() {
        return declaredMime;
    }

    public void setDeclaredMime(String value) {
        declaredMime = value;
    }

    private Instant uploadExpiresAt;

    public Instant getUploadExpiresAt() {
        return uploadExpiresAt;
    }

    public void setUploadExpiresAt(Instant value) {
        uploadExpiresAt = value;
    }

    private String uploadLeaseToken;

    public String getUploadLeaseToken() {
        return uploadLeaseToken;
    }

    public void setUploadLeaseToken(String value) {
        uploadLeaseToken = value;
    }

    private Instant uploadLeaseUntil;

    public Instant getUploadLeaseUntil() {
        return uploadLeaseUntil;
    }

    public void setUploadLeaseUntil(Instant value) {
        uploadLeaseUntil = value;
    }

    private Long trainingId;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long value) {
        trainingId = value;
    }

    private Long mediaDurationMillis;

    public Long getMediaDurationMillis() {
        return mediaDurationMillis;
    }

    public void setMediaDurationMillis(Long value) {
        mediaDurationMillis = value;
    }

    private Integer mediaWidth;

    public Integer getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(Integer value) {
        mediaWidth = value;
    }

    private Integer mediaHeight;

    public Integer getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(Integer value) {
        mediaHeight = value;
    }

    private String mediaVideoCodec;

    public String getMediaVideoCodec() {
        return mediaVideoCodec;
    }

    public void setMediaVideoCodec(String value) {
        mediaVideoCodec = value;
    }

    private String mediaAudioCodec;

    public String getMediaAudioCodec() {
        return mediaAudioCodec;
    }

    public void setMediaAudioCodec(String value) {
        mediaAudioCodec = value;
    }
}
