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
            SELECT u.user_id, u.user_name, u.nick_name, u.status, u.dept_id, d.dept_name
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

    @Select("""
            SELECT b.*, u.user_name, u.nick_name FROM vi_employee_binding b
            LEFT JOIN sys_user u ON u.user_id = b.sys_user_id
            WHERE b.account_id = #{accountId} AND b.revoked_at IS NULL
            """)
    Map<String, Object> findActiveBinding(@Param("accountId") Long accountId);

    @Insert("""
            INSERT INTO vi_employee_binding (account_id, sys_user_id, bound_by, bound_at, created_at, updated_at, version)
            VALUES (#{accountId}, #{sysUserId}, #{boundBy}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertBinding(@Param("accountId") Long accountId, @Param("sysUserId") Long sysUserId,
            @Param("boundBy") Long boundBy);

    @Update("""
            UPDATE vi_employee_binding SET revoked_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE account_id = #{accountId} AND revoked_at IS NULL
            """)
    int revokeBinding(@Param("accountId") Long accountId);

    @Select("""
            SELECT b.id, b.request_code, b.status, b.request_reason, b.expires_at, b.created_at,
                   a.nickname, a.open_id
            FROM vi_employee_binding_request b JOIN vi_account a ON a.id = b.account_id
            WHERE b.status IN ('REQUESTED','CREDENTIAL_ISSUED')
            ORDER BY b.created_at ASC LIMIT 100
            """)
    List<Map<String, Object>> listPendingBindingRequests();

    @Select("""
            SELECT b.*, a.nickname FROM vi_employee_binding_request b JOIN vi_account a ON a.id = b.account_id
            WHERE b.account_id = #{accountId} ORDER BY b.id DESC LIMIT 20
            """)
    List<Map<String, Object>> listBindingRequests(@Param("accountId") Long accountId);

    @Select("SELECT * FROM vi_employee_binding_request WHERE id = #{requestId}")
    Map<String, Object> findBindingRequest(@Param("requestId") Long requestId);

    @Insert("""
            INSERT INTO vi_employee_binding_request (account_id, request_code, status, request_reason, expires_at, created_at, updated_at, version)
            VALUES (#{accountId}, #{code}, 'REQUESTED', #{reason}, #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertBindingRequest(Map<String, Object> row);

    @Update("""
            UPDATE vi_employee_binding_request SET status = #{status}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{requestId}
            """)
    int updateBindingRequestStatus(@Param("requestId") Long requestId, @Param("status") String status);

    @Insert("""
            INSERT INTO vi_employee_binding_credential (request_id, target_account_id, target_sys_user_id, token_hash,
                   status, issued_by_user_id, expires_at, reason, created_at, updated_at, version)
            VALUES (#{requestId}, #{targetAccountId}, #{targetSysUserId}, #{tokenHash}, 'ISSUED', #{issuedBy},
                   #{expiresAt}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertBindingCredential(Map<String, Object> row);

    @Select("SELECT * FROM vi_employee_binding_credential WHERE token_hash = #{tokenHash}")
    Map<String, Object> findCredentialByHash(@Param("tokenHash") byte[] tokenHash);

    @Select("SELECT * FROM vi_employee_binding_credential WHERE id = #{credentialId}")
    Map<String, Object> findCredential(@Param("credentialId") Long credentialId);

    @Update("""
            UPDATE vi_employee_binding_credential SET status = 'CONSUMED', consumed_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{credentialId} AND status = 'ISSUED'
            """)
    int consumeCredential(@Param("credentialId") Long credentialId);

    @Update("""
            UPDATE vi_employee_binding_credential SET status = 'REVOKED', updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{credentialId} AND status = 'ISSUED'
            """)
    int revokeCredential(@Param("credentialId") Long credentialId);

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
