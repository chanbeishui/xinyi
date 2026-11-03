package com.xinyi.rongli.fulfill.mapper;

import com.xinyi.rongli.common.mapper.MiscMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * fulfill 域管理端查询。由原 AdminMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface AdminFulfillMapper
{
    @Select("""
            <script>
            SELECT c.*, o.order_no FROM vi_result_correction c JOIN vi_order o ON o.id = c.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.status = #{status} </if>
            ORDER BY c.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listCorrections(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);
    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_result_correction c WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.status = #{status} </if>
            </script>
            """)
    long countCorrections(@Param("status") String status);
    @Select("""
            SELECT c.*, o.order_no FROM vi_result_correction c JOIN vi_order o ON o.id = c.order_id
            WHERE c.id = #{correctionId}
            """)
    Map<String, Object> findCorrection(@Param("correctionId") Long correctionId);
    @Update("""
            UPDATE vi_result_correction SET status = #{status}, reviewer_id = #{reviewerId},
                   opinion = #{opinion}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{correctionId} AND status = 'APPLIED'
            """)
    int auditCorrection(@Param("correctionId") Long correctionId, @Param("status") String status,
            @Param("reviewerId") Long reviewerId, @Param("opinion") String opinion);
    @Select("SELECT * FROM vi_result_correction WHERE order_id = #{orderId} ORDER BY id DESC")
    List<Map<String, Object>> listCorrectionsOfOrder(@Param("orderId") Long orderId);
}
