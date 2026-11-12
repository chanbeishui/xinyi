package com.xinyi.rongli.vehicle.dto;

/**
 * 管理端车辆写入 DTO。与实体、查询视图分离：只暴露允许运营修改的字段。
 */
public final class VehicleDtos
{
    private VehicleDtos()
    {
    }

    /**
     * 车辆维护：字段为 null 表示本次不改；reason 必填，写审计。
     */
    public static class VehicleUpdateReq
    {
        public String plateNo;
        public String vehicleTypeCode;
        public String brandModel;
        /** yyyy-MM-dd */
        public String registeredOn;
        public String useType;
        public String internalRemark;
        public Boolean isDefault;
        public String reason;
    }
}
