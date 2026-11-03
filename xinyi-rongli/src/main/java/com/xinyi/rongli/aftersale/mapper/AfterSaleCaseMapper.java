package com.xinyi.rongli.aftersale.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * aftersale 域查询。由原 MiscMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface AfterSaleCaseMapper
{
    @Insert("""
            INSERT INTO vi_dispute (order_id, type, status, description, file_ids, created_at, updated_at, version)
            VALUES (#{orderId}, #{type}, 'OPEN', #{description}, #{fileIds}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDispute(Map<String, Object> row);
    @Select("SELECT * FROM vi_dispute WHERE order_id = #{orderId} ORDER BY id DESC LIMIT 1")
    Map<String, Object> findLatestDispute(@Param("orderId") Long orderId);
    @Select("SELECT * FROM vi_dispute WHERE id = #{id}")
    Map<String, Object> findDispute(@Param("id") Long id);
    @Select("""
            <script>
            SELECT d.*, o.order_no, o.custody_type, o.vehicle_snapshot, d.updated_at AS changed_at
            FROM vi_dispute d JOIN vi_order o ON o.id = d.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND d.status = #{status} </if>
            ORDER BY d.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listDisputes(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);
    @Update("""
            UPDATE vi_dispute SET status = 'RESOLVED', conclusion = #{conclusion}, handler_id = #{handler},
                   resolved_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'RESOLVED'
            """)
    int resolveDispute(@Param("id") Long id, @Param("conclusion") String conclusion, @Param("handler") Long handler);
    @Insert("""
            INSERT INTO vi_service_exception (order_id, type, status, description, file_ids, created_at, updated_at, version)
            VALUES (#{orderId}, #{type}, 'OPEN', #{description}, #{fileIds}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertException(Map<String, Object> row);
    @Select("""
            <script>
            SELECT e.*, o.order_no, o.custody_type, o.vehicle_snapshot, e.updated_at AS changed_at
            FROM vi_service_exception e JOIN vi_order o ON o.id = e.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND e.status = #{status} </if>
            ORDER BY e.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listExceptions(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);
    @Select("SELECT * FROM vi_service_exception WHERE id = #{id}")
    Map<String, Object> findException(@Param("id") Long id);
    @Update("""
            UPDATE vi_service_exception SET status = 'RESOLVED', resolution = #{resolution}, handler_id = #{handler},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'RESOLVED'
            """)
    int resolveException(@Param("id") Long id, @Param("resolution") String resolution, @Param("handler") Long handler);
    @Insert("""
            INSERT INTO vi_review (order_id, owner_account_id, score, tags, content, created_at, updated_at, version)
            VALUES (#{orderId}, #{accountId}, #{score}, #{tags}, #{content}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE score = VALUES(score), tags = VALUES(tags), content = VALUES(content),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int insertReview(Map<String, Object> row);
    @Select("SELECT * FROM vi_review WHERE order_id = #{orderId}")
    Map<String, Object> findReview(@Param("orderId") Long orderId);
}
