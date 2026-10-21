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
 * 车主侧计价规则与报价快照。
 */
@Mapper
public interface PriceMapper
{
    /** 取当前生效的已发布版本；同车型同类型多版本时取 revision 最大者 */
    @Select("""
            SELECT * FROM vi_price_rule
            WHERE vehicle_type_code = #{vehicleTypeCode} AND inspection_type_code = #{inspectionTypeCode}
              AND status = 'PUBLISHED'
              AND (effective_from IS NULL OR effective_from <= UTC_TIMESTAMP(3))
              AND (effective_to IS NULL OR effective_to > UTC_TIMESTAMP(3))
            ORDER BY revision DESC, id DESC LIMIT 1
            """)
    Map<String, Object> findPublished(@Param("vehicleTypeCode") String vehicleTypeCode,
            @Param("inspectionTypeCode") String inspectionTypeCode);

    @Select("SELECT * FROM vi_price_rule WHERE id = #{ruleId}")
    Map<String, Object> findRule(@Param("ruleId") Long ruleId);

    @Select("""
            <script>
            SELECT r.*, d.dict_label AS vehicle_type_name
            FROM vi_price_rule r
            LEFT JOIN sys_dict_data d ON d.dict_type = 'vehicle_type' AND d.dict_value = r.vehicle_type_code
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND r.status = #{status} </if>
            <if test="vehicleTypeCode != null and vehicleTypeCode != ''"> AND r.vehicle_type_code = #{vehicleTypeCode} </if>
            ORDER BY r.rule_key, r.revision DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminList(@Param("status") String status, @Param("vehicleTypeCode") String vehicleTypeCode,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_price_rule WHERE 1 = 1
            <if test="status != null and status != ''"> AND status = #{status} </if>
            <if test="vehicleTypeCode != null and vehicleTypeCode != ''"> AND vehicle_type_code = #{vehicleTypeCode} </if>
            </script>
            """)
    long adminCount(@Param("status") String status, @Param("vehicleTypeCode") String vehicleTypeCode);

    @Select("SELECT COALESCE(MAX(revision), 0) FROM vi_price_rule WHERE rule_key = #{ruleKey}")
    int maxRevision(@Param("ruleKey") String ruleKey);

    @Insert("""
            INSERT INTO vi_price_rule (rule_key, revision, vehicle_type_code, inspection_type_code, status,
                   config_json, effective_from, submitted_by, created_at, updated_at, version)
            VALUES (#{ruleKey}, #{revision}, #{vehicleTypeCode}, #{inspectionTypeCode}, #{status},
                   #{configJson}, #{effectiveFrom}, #{submittedBy}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRule(Map<String, Object> row);

    @Update("""
            UPDATE vi_price_rule SET config_json = #{configJson}, effective_from = #{effectiveFrom},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ruleId} AND status = 'DRAFT'
            """)
    int updateDraft(@Param("ruleId") Long ruleId, @Param("configJson") String configJson,
            @Param("effectiveFrom") LocalDateTime effectiveFrom);

    @Update("""
            UPDATE vi_price_rule SET status = #{to}, submitted_by = COALESCE(#{actor}, submitted_by),
                   approved_by = IF(#{to} = 'APPROVED' OR #{to} = 'PUBLISHED', #{actor}, approved_by),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ruleId} AND status = #{from}
            """)
    int changeRuleStatus(@Param("ruleId") Long ruleId, @Param("from") String from, @Param("to") String to,
            @Param("actor") Long actor);

    @Update("""
            UPDATE vi_price_rule SET status = 'DISABLED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE rule_key = #{ruleKey} AND status = 'PUBLISHED' AND id <> #{keepId}
            """)
    int disableOtherPublished(@Param("ruleKey") String ruleKey, @Param("keepId") Long keepId);

    // ------------------------------------------------------------------ 报价

    @Insert("""
            INSERT INTO vi_quote (owner_account_id, station_id, vehicle_snapshot, address_snapshot_cipher,
                   pickup_slot_id, receive_slot_id, rule_id, route_snapshot, amount_fen, items_json,
                   expires_at, created_at, updated_at, version)
            VALUES (#{ownerAccountId}, #{stationId}, #{vehicleSnapshot}, #{addressCipher},
                   #{pickupSlotId}, #{receiveSlotId}, #{ruleId}, #{routeSnapshot}, #{amountFen}, #{itemsJson},
                   #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuote(Map<String, Object> row);

    @Select("SELECT * FROM vi_quote WHERE id = #{quoteId}")
    Map<String, Object> findQuote(@Param("quoteId") Long quoteId);

    @Update("""
            UPDATE vi_quote SET used_order_id = #{orderId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{quoteId} AND used_order_id IS NULL
            """)
    int markQuoteUsed(@Param("quoteId") Long quoteId, @Param("orderId") Long orderId);
}
