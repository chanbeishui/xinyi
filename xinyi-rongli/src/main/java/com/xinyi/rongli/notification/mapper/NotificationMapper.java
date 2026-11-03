package com.xinyi.rongli.notification.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * notification 域查询。由原 MiscMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface NotificationMapper
{
    @Insert("""
            INSERT INTO vi_notification (account_id, role, order_id, scene, outbox_id, send_status, content,
                   created_at, updated_at, version)
            VALUES (#{accountId}, #{role}, #{orderId}, #{scene}, #{outboxId}, 'ACCEPTED', #{content},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertNotification(Map<String, Object> row);
    @Update("""
            UPDATE vi_notification SET channel = #{channel}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int markNotificationChannel(@Param("id") Long id, @Param("channel") String channel);
    @Select("""
            SELECT * FROM vi_notification WHERE account_id = #{accountId} AND role = #{role}
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listNotifications(@Param("accountId") Long accountId, @Param("role") String role,
            @Param("offset") int offset, @Param("limit") int limit);
    @Update("""
            UPDATE vi_notification SET read_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{messageId} AND account_id = #{accountId}
            """)
    int markNotificationRead(@Param("messageId") Long messageId, @Param("accountId") Long accountId);
    @Select("SELECT COUNT(1) FROM vi_notification WHERE account_id = #{accountId} AND read_at IS NULL")
    long countUnread(@Param("accountId") Long accountId);
}
