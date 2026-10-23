package com.xinyi.rongli.dto;

import java.math.BigDecimal;

/**
 * 小程序端请求体。字段与资料包 OpenAPI 的输入结构同名，服务端只信任这些业务输入，
 * 不接受金额、操作者身份、目标状态等由客户端提出的越权字段。
 */
public final class MiniDtos
{
    private MiniDtos()
    {
    }

    public static class LoginReq
    {
        /** wx.login 的 code */
        public String code;

        /** getPhoneNumber 回调里的 code；带上即同时完成手机号授权与登录 */
        public String phoneCode;

        public String appId;
        public String nickname;
        public String deviceLabel;
    }

    /** 已登录账号补充手机号授权 */
    public static class WechatPhoneReq
    {
        public String phoneCode;
    }

    public static class RoleSwitchReq
    {
        public String role;
    }

    public static class StationSwitchReq
    {
        public Long stationId;
    }

    public static class PhoneReq
    {
        public String phone;
        public String smsCode;
    }

    public static class SmsSendReq
    {
        public String phone;
    }

    public static class ProfilePatch
    {
        public String nickname;
        public String serviceName;
    }

    // BindingRequestCreate / EmployeeBindingReq 随 V6.4 员工绑定流程退役一并删除

    public static class VehicleReq
    {
        public String plateNo;
        public String vehicleTypeCode;
        public String brandModel;
        public String vinLast4;
        public String registeredOn;
        public String useType;
        public Boolean isDefault;
    }

    public static class AddressReq
    {
        public String label;
        public String address;
        public BigDecimal lng;
        public BigDecimal lat;
        public Boolean isDefault;
    }

    public static class VersionReq
    {
        public Long version;
        public String reason;
    }

    public static class QuoteReq
    {
        public Long vehicleId;
        public String vehicleTypeCode;
        public String plateNo;
        public Long addressId;
        public AddressReq addressDraft;
        public Long pickupSlotId;
        public String inspectionTypeCode;
    }

    public static class OrderCreateReq
    {
        public Long quoteId;
        public Long vehicleId;
        public Long addressId;
        public AddressReq addressDraft;
        public String contactName;
        public String contactPhone;
    }

    public static class ServiceConfirmReq
    {
        public Long version;
    }

    public static class DisputeReq
    {
        public String type;
        public String description;
    }

    public static class ReviewReq
    {
        public Integer score;
        public String tags;
        public String content;
    }

    public static class AfterSaleApply
    {
        public String kind;
        public Long requestedFen;
        public String reason;
        public String evidenceIds;
    }

    public static class AfterSaleNote
    {
        public String content;
    }

    public static class SmsVerifyReq
    {
        public String code;
    }

    public static class UploadReq
    {
        public String purpose;
        public String mimeType;
        public Long size;
        public String orderNo;
    }

    public static class PrivacyReq
    {
        public String reason;
        public String orderNo;
    }

    public static class DisputeCreateReq
    {
        public String type;
        public String description;
        public String fileIds;
    }

    public static class ReviewCreateReq
    {
        public Integer score;
        public String tags;
        public String content;
    }

    public static class RefundReq
    {
        public Boolean approve;
        public Long approvedFen;
        public Boolean returnRequired;
        public String opinion;
    }

    public static class AfterSaleReviewReq
    {
        public Boolean approve;
        public Long approvedFen;
        public Boolean returnRequired;
        public String opinion;
    }
}
