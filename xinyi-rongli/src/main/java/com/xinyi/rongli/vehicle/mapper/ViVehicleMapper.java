package com.xinyi.rongli.vehicle.mapper;

import com.xinyi.rongli.vehicle.domain.ViVehicle;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** vi_vehicle（A01）。生成列 active_plate 不写；uk_owner_plate 同车主同车牌仅一条活跃；软删释放车牌；CAS。 */
public interface ViVehicleMapper {

    int insert(ViVehicle vehicle);

    ViVehicle selectById(@Param("id") long id);

    ViVehicle selectByIdForUpdate(@Param("id") long id);

    List<ViVehicle> selectByOwner(@Param("ownerAccountId") long ownerAccountId);

    ViVehicle selectActiveByOwnerPlate(@Param("ownerAccountId") long ownerAccountId, @Param("plateNo") String plateNo);

    int update(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
               @Param("plateNo") String plateNo, @Param("vehicleTypeCode") String vehicleTypeCode,
               @Param("brandModel") String brandModel, @Param("useType") String useType,
               @Param("vinLast4") String vinLast4, @Param("isDefault") int isDefault,
               @Param("expectedVersion") long expectedVersion);

    int softDelete(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
                   @Param("deletedAt") Instant deletedAt, @Param("expectedVersion") long expectedVersion);

    int clearDefault(@Param("ownerAccountId") long ownerAccountId, @Param("excludeId") long excludeId);

    int setDefaultFlag(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
                       @Param("flag") int flag, @Param("expectedVersion") long expectedVersion);

    int adminUpdate(@Param("id") long id, @Param("brandModel") String brandModel,
                    @Param("vehicleTypeCode") String vehicleTypeCode, @Param("useType") String useType,
                    @Param("registeredOn") LocalDate registeredOn, @Param("internalRemark") String internalRemark,
                    @Param("expectedVersion") long expectedVersion);
}