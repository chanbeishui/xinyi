package com.xinyi.rongli.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 车主车辆档案与常用地址。
 */
@Mapper
public interface VehicleMapper
{
    @Select("""
            SELECT v.*, d.dict_label AS vehicle_type_name
            FROM vi_vehicle v LEFT JOIN sys_dict_data d ON d.dict_type = 'vehicle_type' AND d.dict_value = v.vehicle_type_code
            WHERE v.owner_account_id = #{accountId} AND v.deleted_at IS NULL
            ORDER BY v.is_default DESC, v.id DESC
            """)
    List<Map<String, Object>> listByOwner(@Param("accountId") Long accountId);

    @Select("SELECT * FROM vi_vehicle WHERE id = #{id}")
    Map<String, Object> findById(@Param("id") Long id);

    @Select("""
            SELECT v.*, a.nickname AS owner_name FROM vi_vehicle v
            LEFT JOIN vi_account a ON a.id = v.owner_account_id
            WHERE v.id = #{id} AND v.owner_account_id = #{accountId} AND v.deleted_at IS NULL
            """)
    Map<String, Object> findOwned(@Param("id") Long id, @Param("accountId") Long accountId);

    @Select("SELECT COUNT(1) FROM vi_vehicle WHERE owner_account_id = #{accountId} AND active_plate = #{plateNo} AND deleted_at IS NULL")
    int countByPlate(@Param("accountId") Long accountId, @Param("plateNo") String plateNo);

    /** 校验字典值是否合法（直接查库，避免字典缓存未预热时误判）。 */
    @Select("SELECT COUNT(1) FROM sys_dict_data WHERE dict_type = #{dictType} AND dict_value = #{dictValue}")
    int countDictValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    @Insert("""
            INSERT INTO vi_vehicle (owner_account_id, plate_no, vehicle_type_code, brand_model, vin_last4,
                   registered_on, use_type, verification_status, is_default, created_at, updated_at, version)
            VALUES (#{ownerAccountId}, #{plateNo}, #{vehicleTypeCode}, #{brandModel}, #{vinLast4},
                   #{registeredOn}, #{useType}, 'PENDING', #{isDefault}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Map<String, Object> row);

    @Update("""
            UPDATE vi_vehicle SET plate_no = #{plateNo}, vehicle_type_code = #{vehicleTypeCode},
                   brand_model = #{brandModel}, vin_last4 = #{vinLast4}, registered_on = #{registeredOn},
                   use_type = #{useType}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{ownerAccountId} AND deleted_at IS NULL
            """)
    int update(Map<String, Object> row);

    @Update("""
            UPDATE vi_vehicle SET is_default = 0, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE owner_account_id = #{accountId} AND is_default = 1 AND deleted_at IS NULL
            """)
    int clearDefault(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_vehicle SET is_default = 1, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{accountId} AND deleted_at IS NULL
            """)
    int markDefault(@Param("id") Long id, @Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_vehicle SET deleted_at = UTC_TIMESTAMP(3), is_default = 0,
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{accountId} AND deleted_at IS NULL
            """)
    int softDelete(@Param("id") Long id, @Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_vehicle SET internal_remark = #{remark}, verification_status = #{verificationStatus},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int adminMaintain(@Param("id") Long id, @Param("remark") String remark,
            @Param("verificationStatus") String verificationStatus);

    @Select("""
            <script>
            SELECT v.id, v.plate_no, v.vehicle_type_code, v.brand_model, v.verification_status, v.is_default,
                   v.created_at, v.updated_at, a.id AS owner_account_id, a.nickname AS owner_name,
                   d.dict_label AS vehicle_type_name
            FROM vi_vehicle v
            LEFT JOIN vi_account a ON a.id = v.owner_account_id
            LEFT JOIN sys_dict_data d ON d.dict_type = 'vehicle_type' AND d.dict_value = v.vehicle_type_code
            WHERE v.deleted_at IS NULL
            <if test="keyword != null and keyword != ''">
              AND (v.plate_no LIKE CONCAT('%', #{keyword}, '%') OR a.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="vehicleTypeCode != null and vehicleTypeCode != ''">
              AND v.vehicle_type_code = #{vehicleTypeCode}
            </if>
            <if test="deptId != null">
              AND (v.dept_id IS NULL OR v.dept_id IN (SELECT dept_id FROM sys_dept
                                     WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors)))
            </if>
            ORDER BY v.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminList(@Param("keyword") String keyword, @Param("vehicleTypeCode") String vehicleTypeCode,
            @Param("deptId") Long deptId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_vehicle v LEFT JOIN vi_account a ON a.id = v.owner_account_id
            WHERE v.deleted_at IS NULL
            <if test="keyword != null and keyword != ''">
              AND (v.plate_no LIKE CONCAT('%', #{keyword}, '%') OR a.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="vehicleTypeCode != null and vehicleTypeCode != ''">
              AND v.vehicle_type_code = #{vehicleTypeCode}
            </if>
            <if test="deptId != null">
              AND (v.dept_id IS NULL OR v.dept_id IN (SELECT dept_id FROM sys_dept
                                     WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors)))
            </if>
            </script>
            """)
    long adminCount(@Param("keyword") String keyword, @Param("vehicleTypeCode") String vehicleTypeCode,
            @Param("deptId") Long deptId);

    // ------------------------------------------------------------------ 地址

    @Select("""
            SELECT id, label, is_default, created_at, updated_at, version, address_cipher
            FROM vi_address WHERE owner_account_id = #{accountId} AND deleted_at IS NULL
            ORDER BY is_default DESC, id DESC
            """)
    List<Map<String, Object>> listAddresses(@Param("accountId") Long accountId);

    @Select("SELECT * FROM vi_address WHERE id = #{id} AND owner_account_id = #{accountId} AND deleted_at IS NULL")
    Map<String, Object> findAddress(@Param("id") Long id, @Param("accountId") Long accountId);

    @Insert("""
            INSERT INTO vi_address (owner_account_id, label, address_cipher, location_cipher, is_default,
                   created_at, updated_at, version)
            VALUES (#{ownerAccountId}, #{label}, #{addressCipher}, #{locationCipher}, #{isDefault},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAddress(Map<String, Object> row);

    @Update("""
            UPDATE vi_address SET label = #{label}, address_cipher = #{addressCipher},
                   location_cipher = #{locationCipher}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{ownerAccountId} AND deleted_at IS NULL
            """)
    int updateAddress(Map<String, Object> row);

    @Update("""
            UPDATE vi_address SET is_default = 0, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE owner_account_id = #{accountId} AND is_default = 1 AND deleted_at IS NULL
            """)
    int clearAddressDefault(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_address SET is_default = 1, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{accountId} AND deleted_at IS NULL
            """)
    int markAddressDefault(@Param("id") Long id, @Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_address SET deleted_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND owner_account_id = #{accountId} AND deleted_at IS NULL
            """)
    int softDeleteAddress(@Param("id") Long id, @Param("accountId") Long accountId);

    @Select("""
            <script>
            SELECT o.id AS owner_id, o.account_id, o.service_name, o.service_flag, o.internal_remark,
                   a.nickname, a.status, a.created_at,
                   (SELECT COUNT(1) FROM vi_vehicle v WHERE v.owner_account_id = o.account_id AND v.deleted_at IS NULL) AS vehicle_count,
                   (SELECT COUNT(1) FROM vi_order od WHERE od.owner_account_id = o.account_id) AS order_count
            FROM vi_owner_profile o JOIN vi_account a ON a.id = o.account_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (a.nickname LIKE CONCAT('%', #{keyword}, '%') OR o.service_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY o.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminOwnerList(@Param("keyword") String keyword, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_owner_profile o JOIN vi_account a ON a.id = o.account_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (a.nickname LIKE CONCAT('%', #{keyword}, '%') OR o.service_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long adminOwnerCount(@Param("keyword") String keyword);

    @Select("""
            SELECT o.*, a.nickname, a.status, a.created_at AS account_created_at
            FROM vi_owner_profile o JOIN vi_account a ON a.id = o.account_id WHERE o.id = #{ownerId}
            """)
    Map<String, Object> adminOwnerDetail(@Param("ownerId") Long ownerId);

    @Select("""
            <script>
            SELECT d.id AS driver_id, d.account_id, d.qualification_status, d.listening_enabled,
                   d.new_assignment_enabled, d.license_class, d.license_valid_to, d.review_due_at,
                   a.nickname, a.status,
                   (SELECT COUNT(1) FROM vi_driver_assignment x WHERE x.driver_id = d.id AND x.ended_at IS NULL) AS active_assignments,
                   (SELECT COUNT(1) FROM vi_driver_assignment x WHERE x.driver_id = d.id) AS total_assignments
            FROM vi_driver_profile d JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (a.nickname LIKE CONCAT('%', #{keyword}, '%') OR a.open_id LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="status != null and status != ''">
              AND d.qualification_status = #{status}
            </if>
            <if test="deptId != null">
              AND (d.dept_id IS NULL OR d.dept_id IN (SELECT dept_id FROM sys_dept
                                     WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors)))
            </if>
            ORDER BY d.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminDriverList(@Param("keyword") String keyword, @Param("status") String status,
            @Param("deptId") Long deptId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_driver_profile d JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (a.nickname LIKE CONCAT('%', #{keyword}, '%') OR a.open_id LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="status != null and status != ''">
              AND d.qualification_status = #{status}
            </if>
            <if test="deptId != null">
              AND (d.dept_id IS NULL OR d.dept_id IN (SELECT dept_id FROM sys_dept
                                     WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors)))
            </if>
            </script>
            """)
    long adminDriverCount(@Param("keyword") String keyword, @Param("status") String status,
            @Param("deptId") Long deptId);

    @Select("""
            SELECT d.*, a.nickname, a.status, a.created_at AS account_created_at,
                   (SELECT MAX(captured_at) FROM vi_driver_location l WHERE l.driver_id = d.id) AS last_location_at
            FROM vi_driver_profile d JOIN vi_account a ON a.id = d.account_id WHERE d.id = #{driverId}
            """)
    Map<String, Object> adminDriverDetail(@Param("driverId") Long driverId);

    @Select("""
            SELECT o.order_no, o.fulfillment_status, o.pickup_starts_at, o.total_fen, o.created_at
            FROM vi_order o WHERE o.vehicle_id = #{vehicleId} ORDER BY o.id DESC LIMIT 50
            """)
    List<Map<String, Object>> ordersByVehicle(@Param("vehicleId") Long vehicleId);

    @Select("""
            SELECT r.id, r.order_id, r.revision, r.conclusion, r.items_json, r.inspected_at
            FROM vi_inspection_result r WHERE r.order_id IN (SELECT id FROM vi_order WHERE vehicle_id = #{vehicleId})
            ORDER BY r.id DESC LIMIT 50
            """)
    List<Map<String, Object>> resultsByVehicle(@Param("vehicleId") Long vehicleId);

    @Select("""
            SELECT o.id, o.order_no, o.station_id, r.revision, r.conclusion, r.items_json, r.inspected_at
            FROM vi_inspection_result r JOIN vi_order o ON o.id = r.order_id
            WHERE r.superseded_at IS NULL AND o.vehicle_id = #{vehicleId}
            ORDER BY r.id DESC LIMIT 20
            """)
    List<Map<String, Object>> currentResultsByVehicle(@Param("vehicleId") Long vehicleId);

    @Select("""
            SELECT id, plate_no, vehicle_type_code, brand_model, verification_status, internal_remark, updated_at
            FROM vi_vehicle WHERE owner_account_id = #{accountId} AND deleted_at IS NULL ORDER BY id DESC
            """)
    List<Map<String, Object>> vehiclesByOwner(@Param("accountId") Long accountId);
}
