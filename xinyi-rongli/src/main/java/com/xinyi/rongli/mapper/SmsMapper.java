package com.xinyi.rongli.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 短信发送日志与频控计数（G02）。
 *
 * <p>只存手机号摘要与脱敏号，不落明文；频控按摘要统计，避免不同账号用同一号码绕过限制。</p>
 */
@Mapper
public interface SmsMapper
{
    @Insert("""
            INSERT INTO vi_sms_send_log (account_id, phone_lookup, phone_masked, scene, provider, template_code,
                   status, error_code, error_message, created_at)
            VALUES (#{accountId}, #{phoneLookup}, #{phoneMasked}, #{scene}, #{provider}, #{templateCode},
                   #{status}, #{errorCode}, #{errorMessage}, UTC_TIMESTAMP(3))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLog(Map<String, Object> row);

    /** 同一手机号在时间窗内的发送条数（含失败，避免用失败重试绕过频控） */
    @Select("""
            SELECT COUNT(1) FROM vi_sms_send_log
            WHERE phone_lookup = #{phoneLookup} AND created_at >= #{since}
            """)
    int countByPhoneSince(@Param("phoneLookup") byte[] phoneLookup, @Param("since") LocalDateTime since);

    /** 同一手机号最近一次发送时刻，用于重发间隔 */
    @Select("""
            SELECT MAX(created_at) FROM vi_sms_send_log
            WHERE phone_lookup = #{phoneLookup} AND status = 'OK'
            """)
    LocalDateTime lastSentAt(@Param("phoneLookup") byte[] phoneLookup);

    /** 同一账号在时间窗内的发送条数，防止单账号刷不同号码 */
    @Select("""
            SELECT COUNT(1) FROM vi_sms_send_log
            WHERE account_id = #{accountId} AND created_at >= #{since}
            """)
    int countByAccountSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    @Select("""
            SELECT id, account_id, phone_masked, scene, provider, status, error_code, error_message, created_at
            FROM vi_sms_send_log ORDER BY id DESC LIMIT #{limit}
            """)
    List<Map<String, Object>> listRecent(@Param("limit") int limit);
}
