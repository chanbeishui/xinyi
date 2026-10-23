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
 * 小程序账户、会话、令牌、车主/代驾档案与员工绑定。
 */
@Mapper
public interface AccountMapper
{
    @Select("""
            SELECT id, app_id, open_id, union_id, nickname, status, phone_lookup IS NOT NULL AS has_phone,
                   created_at, updated_at, version
            FROM vi_account WHERE app_id = #{appId} AND open_id = #{openId}
            """)
    Map<String, Object> findAccountByOpenId(@Param("appId") String appId, @Param("openId") String openId);

    @Select("""
            SELECT id, app_id, open_id, nickname, status, phone_lookup IS NOT NULL AS has_phone,
                   created_at, updated_at, version
            FROM vi_account WHERE id = #{accountId}
            """)
    Map<String, Object> findAccount(@Param("accountId") Long accountId);

    @Insert("""
            INSERT INTO vi_account (app_id, open_id, union_id, nickname, status, created_at, updated_at, version)
            VALUES (#{appId}, #{openId}, #{unionId}, #{nickname}, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertAccount(Map<String, Object> row);

    @Insert("""
            INSERT INTO vi_account (app_id, open_id, union_id, nickname, phone_cipher, phone_lookup, status, created_at, updated_at, version)
            VALUES (#{appId}, #{openId}, #{unionId}, #{nickname}, #{phoneCipher}, #{phoneLookup}, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertAccountWithPhone(Map<String, Object> row);

    @Update("""
            UPDATE vi_account SET nickname = #{nickname}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{accountId}
            """)
    int updateNickname(@Param("accountId") Long accountId, @Param("nickname") String nickname);

    @Update("""
            UPDATE vi_account SET phone_cipher = #{phoneCipher}, phone_lookup = #{phoneLookup},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{accountId}
            """)
    int updatePhone(@Param("accountId") Long accountId, @Param("phoneCipher") byte[] phoneCipher,
            @Param("phoneLookup") byte[] phoneLookup);

    /**
     * 只取手机号密文。
     *
     * <p>手机号密文属于敏感数据，不放进通用的 {@code findAccount}（那会被十几个
     * 业务场景调用），需要解密手机号的少数链路（短信下发、验证码发送）单独取。</p>
     *
     * <p>用 Map 承接而不是直接声明 {@code byte[]} 返回值：MyBatis 对裸数组返回类型
     * 会解析成单字节标量类型处理器，binary 列直接抛「outside of valid range for
     * java.lang.Byte」。这里统一在 default 方法里归一化成 byte[]。</p>
     */
    @Select("SELECT phone_cipher AS phoneCipher FROM vi_account WHERE id = #{accountId}")
    Map<String, Object> findPhoneCipherRow(@Param("accountId") Long accountId);

    /** 手机号密文；未绑定返回 null */
    default byte[] findPhoneCipher(Long accountId)
    {
        Map<String, Object> row = findPhoneCipherRow(accountId);
        Object value = row == null ? null : row.get("phoneCipher");
        if (value instanceof byte[] bytes)
        {
            return bytes.length == 0 ? null : bytes;
        }
        if (value instanceof java.sql.Blob blob)
        {
            try
            {
                byte[] bytes = blob.getBytes(1, (int) blob.length());
                return bytes.length == 0 ? null : bytes;
            }
            catch (java.sql.SQLException e)
            {
                return null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ 会话

    @Insert("""
            INSERT INTO vi_session (account_id, app_id, status, session_epoch, context_version, active_role,
                   station_id, linked_sys_user_id, absolute_expires_at, device_label, created_at, version)
            VALUES (#{accountId}, #{appId}, 'ACTIVE', #{epoch}, 1, #{role}, #{stationId}, #{linkedSysUserId},
                   #{absoluteExpiresAt}, #{deviceLabel}, UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertSession(Map<String, Object> row);

    @Select("""
            SELECT s.id, s.account_id, s.app_id, s.status, s.session_epoch, s.context_version, s.active_role,
                   s.station_id, s.linked_sys_user_id, s.absolute_expires_at, s.revoked_at,
                   a.status AS account_status, a.nickname, sec.security_status, sec.session_epoch AS current_epoch
            FROM vi_session s
            JOIN vi_account a ON a.id = s.account_id
            LEFT JOIN vi_account_security sec ON sec.account_id = s.account_id
            WHERE s.id = #{sessionId}
            """)
    Map<String, Object> findSession(@Param("sessionId") Long sessionId);

    @Update("""
            UPDATE vi_session SET active_role = #{role}, context_version = context_version + 1,
                   version = version + 1
            WHERE id = #{sessionId}
            """)
    int updateSessionRole(@Param("sessionId") Long sessionId, @Param("role") String role);

    @Update("""
            UPDATE vi_session SET station_id = #{stationId}, context_version = context_version + 1,
                   version = version + 1
            WHERE id = #{sessionId}
            """)
    int updateSessionStation(@Param("sessionId") Long sessionId, @Param("stationId") Long stationId);

    @Update("""
            UPDATE vi_session SET linked_sys_user_id = #{sysUserId}, context_version = context_version + 1,
                   version = version + 1
            WHERE id = #{sessionId}
            """)
    int updateSessionBinding(@Param("sessionId") Long sessionId, @Param("sysUserId") Long sysUserId);

    @Update("""
            UPDATE vi_session SET linked_sys_user_id = NULL, active_role = 'OWNER', station_id = NULL,
                   context_version = context_version + 1, version = version + 1
            WHERE account_id = #{accountId} AND status = 'ACTIVE'
            """)
    int clearBindingByAccount(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_session SET status = #{status}, revoked_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{sessionId} AND status = 'ACTIVE'
            """)
    int revokeSession(@Param("sessionId") Long sessionId, @Param("status") String status);

    @Update("""
            UPDATE vi_session SET status = 'REVOKED', revoked_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE account_id = #{accountId} AND status = 'ACTIVE'
            """)
    int revokeAllSessions(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_session SET linked_sys_user_id = NULL, active_role = 'OWNER', station_id = NULL,
                   context_version = context_version + 1, version = version + 1
            WHERE linked_sys_user_id = #{sysUserId} AND status = 'ACTIVE'
            """)
    int revokeBySysUser(@Param("sysUserId") Long sysUserId);

    // ------------------------------------------------------------------ 令牌

    @Insert("""
            INSERT INTO vi_session_token (session_id, token_hash, token_type, generation, context_version,
                   parent_token_id, expires_at, created_at, version)
            VALUES (#{sessionId}, #{tokenHash}, #{tokenType}, #{generation}, #{contextVersion},
                   #{parentTokenId}, #{expiresAt}, UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertToken(Map<String, Object> row);

    @Select("""
            SELECT t.id, t.session_id, t.token_type, t.generation, t.context_version, t.expires_at,
                   t.consumed_at, t.revoked_at, t.parent_token_id
            FROM vi_session_token t WHERE t.token_hash = #{tokenHash}
            """)
    Map<String, Object> findToken(@Param("tokenHash") byte[] tokenHash);

    @Update("""
            UPDATE vi_session_token SET consumed_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{tokenId} AND consumed_at IS NULL AND revoked_at IS NULL
            """)
    int consumeToken(@Param("tokenId") Long tokenId);

    @Update("""
            UPDATE vi_session_token SET replaced_by_token_id = #{newTokenId}, version = version + 1
            WHERE id = #{tokenId}
            """)
    int markTokenReplaced(@Param("tokenId") Long tokenId, @Param("newTokenId") Long newTokenId);

    @Update("""
            UPDATE vi_session_token SET revoked_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE session_id = #{sessionId} AND revoked_at IS NULL
            """)
    int revokeTokensBySession(@Param("sessionId") Long sessionId);

    // ------------------------------------------------------------------ 安全版本

    @Select("SELECT * FROM vi_account_security WHERE account_id = #{accountId}")
    Map<String, Object> findSecurity(@Param("accountId") Long accountId);

    @Insert("""
            INSERT INTO vi_account_security (account_id, session_epoch, permission_version, security_status, changed_at, created_at, version)
            VALUES (#{accountId}, 0, 0, 'ACTIVE', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertSecurity(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_account_security SET session_epoch = session_epoch + 1, changed_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE account_id = #{accountId}
            """)
    int bumpEpoch(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_account_security SET permission_version = permission_version + 1, changed_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE account_id = #{accountId}
            """)
    int bumpPermission(@Param("accountId") Long accountId);

    // ------------------------------------------------------------------ 车主档案

    @Select("SELECT * FROM vi_owner_profile WHERE account_id = #{accountId}")
    Map<String, Object> findOwnerProfile(@Param("accountId") Long accountId);

    @Insert("""
            INSERT INTO vi_owner_profile (account_id, service_name, service_flag, created_at, updated_at, version)
            VALUES (#{accountId}, #{serviceName}, 'NORMAL', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertOwnerProfile(@Param("accountId") Long accountId, @Param("serviceName") String serviceName);

    @Update("""
            UPDATE vi_owner_profile SET service_name = #{serviceName}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE account_id = #{accountId}
            """)
    int updateOwnerServiceName(@Param("accountId") Long accountId, @Param("serviceName") String serviceName);

    @Update("""
            UPDATE vi_owner_profile SET service_flag = #{flag}, internal_remark = #{remark},
                   maintained_by = #{maintainedBy}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE account_id = #{accountId}
            """)
    int updateOwnerMaintenance(@Param("accountId") Long accountId, @Param("flag") String flag,
            @Param("remark") String remark, @Param("maintainedBy") Long maintainedBy);

    // ------------------------------------------------------------------ 代驾档案

    @Select("SELECT * FROM vi_driver_profile WHERE account_id = #{accountId}")
    Map<String, Object> findDriverProfile(@Param("accountId") Long accountId);

    @Select("SELECT * FROM vi_driver_profile WHERE id = #{driverId}")
    Map<String, Object> findDriverById(@Param("driverId") Long driverId);

    @Insert("""
            INSERT INTO vi_driver_profile (account_id, qualification_status, new_assignment_enabled,
                   listening_enabled, license_class, license_valid_to, license_long_term, created_at, updated_at, version)
            VALUES (#{accountId}, 'DRAFT', 0, 0, #{licenseClass}, #{licenseValidTo}, #{licenseLongTerm},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertDriverProfile(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_profile SET license_class = #{licenseClass}, license_valid_to = #{licenseValidTo},
                   license_long_term = #{licenseLongTerm}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int updateDriverLicense(@Param("driverId") Long driverId, @Param("licenseClass") String licenseClass,
            @Param("licenseValidTo") LocalDateTime licenseValidTo, @Param("licenseLongTerm") boolean licenseLongTerm);

    @Update("""
            UPDATE vi_driver_profile SET qualification_status = #{status}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int updateDriverQualificationStatus(@Param("driverId") Long driverId, @Param("status") String status);

    @Update("""
            UPDATE vi_driver_profile SET qualification_status = 'APPROVED', new_assignment_enabled = 1,
                   qualification_revision = #{revision}, review_due_at = #{reviewDueAt},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int approveDriver(@Param("driverId") Long driverId, @Param("revision") Long revision,
            @Param("reviewDueAt") LocalDateTime reviewDueAt);

    @Update("""
            UPDATE vi_driver_profile SET listening_enabled = #{enabled}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int updateListening(@Param("driverId") Long driverId, @Param("enabled") boolean enabled);

    @Update("""
            UPDATE vi_driver_profile SET new_assignment_enabled = #{enabled}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int updateNewAssignmentEnabled(@Param("driverId") Long driverId, @Param("enabled") boolean enabled);

    @Update("""
            UPDATE vi_driver_profile SET internal_remark = #{remark}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId}
            """)
    int updateDriverRemark(@Param("driverId") Long driverId, @Param("remark") String remark);

    /**
     * 年度复审到期的已认证代驾：停掉新承接并置为 EXPIRED。
     *
     * <p>只处理 APPROVED 且 review_due_at 已过的行；review_due_at 为空表示长期有效，不在此列。
     * 已经在手承接的任务不受影响，仍可正常履约到归还。</p>
     */
    @Select("""
            SELECT id, account_id, review_due_at FROM vi_driver_profile
            WHERE qualification_status = 'APPROVED' AND review_due_at IS NOT NULL
              AND review_due_at < UTC_TIMESTAMP(3)
            ORDER BY review_due_at ASC LIMIT #{limit}
            """)
    List<java.util.Map<String, Object>> findExpiredQualifications(@Param("limit") int limit);

    @Update("""
            UPDATE vi_driver_profile SET qualification_status = 'EXPIRED', new_assignment_enabled = 0,
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{driverId} AND qualification_status = 'APPROVED'
            """)
    int expireQualification(@Param("driverId") Long driverId);

    // ------------------------------------------------------------------ 资格申请

    @Select("""
            SELECT * FROM vi_driver_application WHERE driver_id = #{driverId} ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findLatestApplication(@Param("driverId") Long driverId);

    @Select("SELECT * FROM vi_driver_application WHERE id = #{applicationId}")
    Map<String, Object> findApplication(@Param("applicationId") Long applicationId);

    @Insert("""
            INSERT INTO vi_driver_application (driver_id, status, sensitive_payload_cipher, evidence_file_ids,
                   submitted_at, created_at, updated_at, version)
            VALUES (#{driverId}, #{status}, #{payload}, #{evidence}, #{submittedAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertApplication(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_application SET status = 'SUBMITTED', submitted_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{applicationId} AND status = 'DRAFT'
            """)
    int submitApplication(@Param("applicationId") Long applicationId);

    @Update("""
            UPDATE vi_driver_application SET status = #{status}, reviewer_user_id = #{reviewer},
                   reviewed_at = UTC_TIMESTAMP(3), review_opinion = #{opinion},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{applicationId} AND status = 'SUBMITTED'
            """)
    int auditApplication(@Param("applicationId") Long applicationId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("opinion") String opinion);

    @Update("""
            UPDATE vi_driver_application SET sensitive_payload_cipher = #{payload}, evidence_file_ids = #{evidence},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{applicationId} AND status = 'DRAFT'
            """)
    int updateApplicationDraft(@Param("applicationId") Long applicationId, @Param("payload") byte[] payload,
            @Param("evidence") String evidence);

    @Select("""
            SELECT a.id, a.driver_id, a.status, a.submitted_at, a.reviewed_at, a.review_opinion,
                   d.account_id, acc.nickname, d.qualification_status, d.listening_enabled,
                   d.new_assignment_enabled, d.license_class, d.license_valid_to
            FROM vi_driver_application a
            JOIN vi_driver_profile d ON d.id = a.driver_id
            JOIN vi_account acc ON acc.id = d.account_id
            WHERE a.status = #{status}
            ORDER BY a.submitted_at ASC, a.id ASC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> listApplicationsByStatus(@Param("status") String status, @Param("limit") int limit);

    // ------------------------------------------------------------------ 员工绑定

    @Select("""
            SELECT u.user_id, u.user_name, u.nick_name, u.status, u.dept_id, u.auth_version, d.dept_name
            FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id = u.dept_id
            WHERE u.user_id = #{userId} AND u.del_flag = '0'
            """)
    Map<String, Object> findSysUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1) FROM sys_user_role ur
            JOIN sys_role r ON r.role_id = ur.role_id AND r.status = '0' AND r.del_flag = '0'
            JOIN sys_role_menu rm ON rm.role_id = ur.role_id
            JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.status = '0'
            WHERE ur.user_id = #{userId} AND (m.perms = #{perm} OR m.perms = '*:*:*')
            """)
    int countPerm(@Param("userId") Long userId, @Param("perm") String perm);

    /**
     * 按手机号匹配框架内的员工账号。
     *
     * <p>只认未删除且状态正常的账号；手机号在 sys_user 上不保证唯一，命中多条时由调用方
     * 视为歧义并拒绝自动绑定，避免把微信账号绑到错误的员工身上。</p>
     */
    @Select("""
            SELECT u.user_id, u.user_name, u.nick_name, u.phonenumber, u.dept_id, d.dept_name
            FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id = u.dept_id
            WHERE u.phonenumber = #{phone} AND u.del_flag = '0' AND u.status = '0'
            ORDER BY u.user_id LIMIT 5
            """)
    List<Map<String, Object>> listSysUserByPhone(@Param("phone") String phone);

    /** 同手机号是否已被其它小程序账号占用（用于换微信后的绑定迁移判断） */
    @Select("""
            SELECT id, app_id, open_id, nickname FROM vi_account
            WHERE phone_lookup = #{phoneLookup} AND id <> #{accountId}
            ORDER BY id LIMIT 5
            """)
    List<Map<String, Object>> listOtherAccountsByPhone(@Param("phoneLookup") byte[] phoneLookup,
            @Param("accountId") Long accountId);

    /** 该员工当前是否已被其他小程序账号绑定（唯一约束 active_user 只允许一条） */
    @Select("""
            SELECT b.id, b.account_id, a.nickname FROM vi_employee_binding b
            LEFT JOIN vi_account a ON a.id = b.account_id
            WHERE b.sys_user_id = #{sysUserId} AND b.revoked_at IS NULL
            """)
    Map<String, Object> findActiveBindingByUser(@Param("sysUserId") Long sysUserId);

    /** 后台新增站点授权时的员工候选：按手机号/姓名/账号搜索 */
    @Select("""
            <script>
            SELECT u.user_id, u.user_name, u.nick_name, u.phonenumber, d.dept_name,
                   (SELECT COUNT(1) FROM vi_station_grant g WHERE g.sys_user_id = u.user_id AND g.enabled = 1)
                     AS granted_station_count
            FROM sys_user u LEFT JOIN sys_dept d ON d.dept_id = u.dept_id
            WHERE u.del_flag = '0' AND u.status = '0'
            <if test="keyword != null and keyword != ''">
              AND (u.phonenumber LIKE CONCAT('%', #{keyword}, '%')
                   OR u.nick_name LIKE CONCAT('%', #{keyword}, '%')
                   OR u.user_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY u.user_id LIMIT 50
            </script>
            """)
    List<Map<String, Object>> searchEmployeeCandidates(@Param("keyword") String keyword);

    @Select("""
            SELECT b.*, u.user_name, u.nick_name FROM vi_employee_binding b
            LEFT JOIN sys_user u ON u.user_id = b.sys_user_id
            WHERE b.account_id = #{accountId} AND b.revoked_at IS NULL
            """)
    Map<String, Object> findActiveBinding(@Param("accountId") Long accountId);

    /**
     * 建立员工关联（V6.4 手机号自动关联）。
     *
     * <p>自动关联由系统发起，{@code bound_by} 必须为空——数据库
     * {@code ck_employee_link_source_actor} 会拦住挂管理员身份的行。
     * {@code phone_lookup} / {@code last_phone_verified_at} /
     * {@code employee_auth_version} 由 {@code ck_employee_link_identity} 强制非空。</p>
     */
    @Insert("""
            INSERT INTO vi_employee_binding (account_id, sys_user_id, app_id, source, status, bound_by, bound_at,
                   phone_lookup, last_phone_verified_at, employee_auth_version, created_at, updated_at, version)
            VALUES (#{accountId}, #{sysUserId}, #{appId}, 'WECHAT_PHONE_MATCH', 'ACTIVE', NULL, UTC_TIMESTAMP(3),
                   #{phoneLookup}, UTC_TIMESTAMP(3), #{employeeAuthVersion}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertBinding(@Param("accountId") Long accountId, @Param("sysUserId") Long sysUserId,
            @Param("appId") String appId, @Param("phoneLookup") byte[] phoneLookup,
            @Param("employeeAuthVersion") Long employeeAuthVersion);

    /** 同应用下该员工的有效关联；V6.4 的唯一占用从「全库」改为「按应用」 */
    @Select("""
            SELECT b.id, b.account_id, a.nickname, a.open_id, b.app_id
            FROM vi_employee_binding b LEFT JOIN vi_account a ON a.id = b.account_id
            WHERE b.app_id = #{appId} AND b.sys_user_id = #{sysUserId} AND b.revoked_at IS NULL
            """)
    Map<String, Object> findActiveBindingByUserInApp(@Param("appId") String appId,
            @Param("sysUserId") Long sysUserId);

    /** 该账号或该员工是否被管理员设置过禁止自动重绑（V6.4 ALLOW_REMATCH 才解除） */
    @Select("""
            SELECT b.id, b.account_id, b.sys_user_id, b.status FROM vi_employee_binding b
            WHERE b.app_id = #{appId}
              AND (b.account_id = #{accountId} OR b.sys_user_id = #{sysUserId})
              AND b.auto_rebind_blocked = 1
            LIMIT 1
            """)
    Map<String, Object> findRebindBlocked(@Param("appId") String appId,
            @Param("accountId") Long accountId, @Param("sysUserId") Long sysUserId);

    /** 撤销关联。status 与 revoked_at 必须同时变更，否则违反 ck_employee_link_revoke */
    @Update("""
            UPDATE vi_employee_binding SET status = 'REVOKED', revoked_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE account_id = #{accountId} AND revoked_at IS NULL
            """)
    int revokeBinding(@Param("accountId") Long accountId);

    // ---- 员工绑定申请 / 一次性凭证兑换流程已于 V6.4 退役（改用微信手机号自动关联），
    // ---- vi_employee_binding_request 与 vi_employee_binding_credential 两张表一并删除。

    // ------------------------------------------------------------------ 定位

    @Insert("""
            INSERT INTO vi_driver_location (driver_id, location_cipher, captured_at, received_at, created_at, updated_at, version)
            VALUES (#{driverId}, #{location}, #{capturedAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE location_cipher = VALUES(location_cipher), captured_at = VALUES(captured_at),
                   received_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int upsertLocation(@Param("driverId") Long driverId, @Param("location") byte[] location,
            @Param("capturedAt") LocalDateTime capturedAt);

    @Select("SELECT * FROM vi_driver_location WHERE driver_id = #{driverId}")
    Map<String, Object> findLocation(@Param("driverId") Long driverId);
}
