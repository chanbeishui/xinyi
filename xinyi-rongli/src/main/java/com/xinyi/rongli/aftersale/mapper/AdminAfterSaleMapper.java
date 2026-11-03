package com.xinyi.rongli.aftersale.mapper;

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
 * aftersale 域管理端查询。由原 AdminMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface AdminAfterSaleMapper
{
    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_dispute d WHERE 1 = 1
            <if test="status != null and status != ''"> AND d.status = #{status} </if>
            </script>
            """)
    long countDisputes(@Param("status") String status);
    @Select("SELECT * FROM vi_dispute WHERE order_id = #{orderId} ORDER BY id DESC")
    List<Map<String, Object>> listDisputesOfOrder(@Param("orderId") Long orderId);
    @Select("""
            SELECT d.*, o.order_no, o.fulfillment_status FROM vi_dispute d
            JOIN vi_order o ON o.id = d.order_id WHERE d.id = #{disputeId}
            """)
    Map<String, Object> findDisputeDetail(@Param("disputeId") Long disputeId);
    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_service_exception e WHERE 1 = 1
            <if test="status != null and status != ''"> AND e.status = #{status} </if>
            </script>
            """)
    long countExceptions(@Param("status") String status);
    @Select("""
            SELECT e.*, o.order_no, o.fulfillment_status FROM vi_service_exception e
            JOIN vi_order o ON o.id = e.order_id WHERE e.id = #{exceptionId}
            """)
    Map<String, Object> findExceptionDetail(@Param("exceptionId") Long exceptionId);
}
