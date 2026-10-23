package com.xinyi.rongli.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * V6.4 第三方服务接入配置的读写。
 *
 * <p>约定：配置本体（profile/revision/secret/check/publication）只增不改；
 * 生效指针是 profile 上的 {@code active_revision_id}，切换用 CAS 版本号，
 * 不允许用“最后写入者胜”覆盖别人的发布。</p>
 */
@Mapper
public interface IntegrationMapper
{
    // ------------------------------------------------------------ 配置档案

    @Select("""
            <script>
            SELECT * FROM vi_integration_profile WHERE 1 = 1
            <if test="kind != null and kind != ''"> AND kind = #{kind} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            ORDER BY kind ASC, owner_dept_id ASC, id ASC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> listProfiles(@Param("kind") String kind, @Param("deptId") Long deptId,
            @Param("environment") String environment, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_integration_profile WHERE 1 = 1
            <if test="kind != null and kind != ''"> AND kind = #{kind} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            </script>
            """)
    long countProfiles(@Param("kind") String kind, @Param("deptId") Long deptId,
            @Param("environment") String environment);

    @Select("SELECT * FROM vi_integration_profile WHERE id = #{id}")
    Map<String, Object> findProfile(@Param("id") Long id);

    @Select("SELECT * FROM vi_integration_profile WHERE id = #{id} FOR UPDATE")
    Map<String, Object> lockProfile(@Param("id") Long id);

    @Select("""
            SELECT * FROM vi_integration_profile
            WHERE kind = #{kind} AND owner_dept_id = #{deptId} AND environment = #{environment} AND config_key = #{configKey}
            """)
    Map<String, Object> findProfileByScope(@Param("kind") String kind, @Param("deptId") Long deptId,
            @Param("environment") String environment, @Param("configKey") String configKey);

    /** 运行时解析：优先订单归属主体，其次同环境下最早建立的生效配置（单主体部署） */
    @Select("""
            SELECT * FROM vi_integration_profile
            WHERE kind = #{kind} AND environment = #{environment} AND active_revision_id IS NOT NULL
              AND lifecycle = 'ACTIVE'
            ORDER BY (owner_dept_id = #{deptId}) DESC, owner_dept_id ASC, id ASC
            LIMIT 1
            """)
    Map<String, Object> findActiveProfile(@Param("kind") String kind, @Param("deptId") Long deptId,
            @Param("environment") String environment);

    @Insert("""
            INSERT INTO vi_integration_profile (kind, owner_dept_id, environment, config_key, display_name,
                   lifecycle, created_by, created_at, updated_at, version)
            VALUES (#{kind}, #{ownerDeptId}, #{environment}, #{configKey}, #{displayName},
                   'DRAFT', #{createdBy}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertProfile(Map<String, Object> row);

    @Update("""
            UPDATE vi_integration_profile
            SET draft_revision_id = #{draftRevisionId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int updateDraftPointer(@Param("id") Long id, @Param("draftRevisionId") Long draftRevisionId,
            @Param("version") long version);

    @Update("""
            UPDATE vi_integration_profile
            SET active_revision_id = #{revisionId}, draft_revision_id = #{revisionId}, lifecycle = 'ACTIVE',
                last_published_by = #{userId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int publishProfile(@Param("id") Long id, @Param("revisionId") Long revisionId,
            @Param("userId") Long userId, @Param("version") long version);

    @Update("UPDATE vi_integration_profile SET revision_seq = revision_seq + 1 WHERE id = #{id}")
    int bumpRevisionSeq(@Param("id") Long id);

    @Select("SELECT revision_seq FROM vi_integration_profile WHERE id = #{id}")
    Long currentRevisionSeq(@Param("id") Long id);

    @Update("""
            UPDATE vi_integration_profile SET lifecycle = #{lifecycle}, updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int updateLifecycle(@Param("id") Long id, @Param("lifecycle") String lifecycle,
            @Param("version") long version);

    // ------------------------------------------------------------ 不可变版本

    @Insert("""
            INSERT INTO vi_integration_revision (profile_id, revision_no, config_json, secret_refs_json,
                   content_hash, created_by, reason, created_at, updated_at, version)
            VALUES (#{profileId}, #{revisionNo}, #{configJson}, #{secretRefsJson},
                   #{contentHash}, #{createdBy}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertRevision(Map<String, Object> row);

    @Select("SELECT * FROM vi_integration_revision WHERE id = #{id}")
    Map<String, Object> findRevision(@Param("id") Long id);

    @Select("SELECT * FROM vi_integration_revision WHERE profile_id = #{profileId} AND content_hash = #{hash}")
    Map<String, Object> findRevisionByHash(@Param("profileId") Long profileId, @Param("hash") String hash);

    @Select("""
            SELECT * FROM vi_integration_revision WHERE profile_id = #{profileId}
            ORDER BY revision_no DESC LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> listRevisions(@Param("profileId") Long profileId, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM vi_integration_revision WHERE profile_id = #{profileId}")
    long countRevisions(@Param("profileId") Long profileId);

    // ------------------------------------------------------------ 凭据密文

    @Insert("""
            INSERT INTO vi_integration_secret (profile_id, slot_name, credential_kind, ciphertext, nonce,
                   key_version, aad_context, fingerprint, lifecycle, expires_at, created_by, created_at, updated_at, version)
            VALUES (#{profileId}, #{slotName}, #{credentialKind}, #{ciphertext}, #{nonce},
                   #{keyVersion}, #{aadContext}, #{fingerprint}, 'ACTIVE', #{expiresAt}, #{createdBy},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertSecret(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_integration_secret
            WHERE profile_id = #{profileId} AND slot_name = #{slotName} AND lifecycle = 'ACTIVE'
            ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findActiveSecret(@Param("profileId") Long profileId, @Param("slotName") String slotName);

    @Select("SELECT * FROM vi_integration_secret WHERE id = #{id}")
    Map<String, Object> findSecret(@Param("id") Long id);

    @Update("""
            UPDATE vi_integration_secret SET lifecycle = #{lifecycle}, retired_at = UTC_TIMESTAMP(3),
                   retired_by = #{userId}, retire_reason = #{reason}, updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE profile_id = #{profileId} AND slot_name = #{slotName} AND lifecycle = 'ACTIVE'
            """)
    int retireSecrets(@Param("profileId") Long profileId, @Param("slotName") String slotName,
            @Param("lifecycle") String lifecycle, @Param("userId") Long userId, @Param("reason") String reason);

    // ------------------------------------------------------------ 校验

    @Insert("""
            INSERT INTO vi_integration_check (target_type, target_id, revision_id, content_hash, check_level,
                   status, issues_json, checked_at, expires_at, checked_by, real_payment_verified, created_at, updated_at, version)
            VALUES (#{targetType}, #{targetId}, #{revisionId}, #{contentHash}, #{checkLevel},
                   #{status}, #{issuesJson}, UTC_TIMESTAMP(3), DATE_ADD(UTC_TIMESTAMP(3), INTERVAL #{ttlMinutes} MINUTE),
                   #{checkedBy}, #{realPaymentVerified}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertCheck(Map<String, Object> row);

    @Select("SELECT * FROM vi_integration_check WHERE id = #{id}")
    Map<String, Object> findCheck(@Param("id") Long id);

    @Select("""
            SELECT * FROM vi_integration_check
            WHERE target_type = #{targetType} AND target_id = #{targetId}
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> listChecks(@Param("targetType") String targetType, @Param("targetId") Long targetId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            SELECT COUNT(1) FROM vi_integration_check
            WHERE target_type = #{targetType} AND target_id = #{targetId} AND checked_at >= DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
            """)
    long countCheckIssues(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    // ------------------------------------------------------------ 发布审计

    @Insert("""
            INSERT INTO vi_integration_publication (target_type, target_id, revision_id, previous_revision_id,
                   check_id, action, published_by, published_at, reason, dependency_versions, target_version,
                   created_at, updated_at, version)
            VALUES (#{targetType}, #{targetId}, #{revisionId}, #{previousRevisionId},
                   #{checkId}, #{action}, #{publishedBy}, UTC_TIMESTAMP(3), #{reason}, #{dependencyVersions},
                   #{targetVersion}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertPublication(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_integration_publication
            WHERE target_type = #{targetType} AND target_id = #{targetId}
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> listPublications(@Param("targetType") String targetType,
            @Param("targetId") Long targetId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            SELECT COUNT(1) FROM vi_integration_publication
            WHERE target_type = #{targetType} AND target_id = #{targetId}
            """)
    long countPublications(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    // ------------------------------------------------------------ 测试收件人

    @Select("""
            <script>
            SELECT * FROM vi_integration_test_recipient WHERE 1 = 1
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            ORDER BY id DESC LIMIT 200
            </script>
            """)
    List<Map<String, Object>> listRecipients(@Param("deptId") Long deptId,
            @Param("environment") String environment);

    @Select("SELECT * FROM vi_integration_test_recipient WHERE id = #{id}")
    Map<String, Object> findRecipient(@Param("id") Long id);

    @Insert("""
            INSERT INTO vi_integration_test_recipient (name, kind, owner_dept_id, environment, phone_cipher,
                   account_id, app_profile_id, enabled, created_by, reason, created_at, updated_at, version)
            VALUES (#{name}, #{kind}, #{ownerDeptId}, #{environment}, #{phoneCipher},
                   #{accountId}, #{appProfileId}, 1, #{createdBy}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertRecipient(Map<String, Object> row);

    @Update("""
            UPDATE vi_integration_test_recipient SET enabled = 0, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND enabled = 1
            """)
    int revokeRecipient(@Param("id") Long id);

    // ------------------------------------------------------------ 模板档案

    @Select("""
            <script>
            SELECT * FROM vi_message_template WHERE 1 = 1
            <if test="provider != null and provider != ''"> AND provider = #{provider} </if>
            <if test="scene != null and scene != ''"> AND scene = #{scene} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> listTemplates(@Param("provider") String provider, @Param("scene") String scene,
            @Param("deptId") Long deptId, @Param("environment") String environment,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_message_template WHERE 1 = 1
            <if test="provider != null and provider != ''"> AND provider = #{provider} </if>
            <if test="scene != null and scene != ''"> AND scene = #{scene} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            </script>
            """)
    long countTemplates(@Param("provider") String provider, @Param("scene") String scene,
            @Param("deptId") Long deptId, @Param("environment") String environment);

    @Select("SELECT * FROM vi_message_template WHERE id = #{id}")
    Map<String, Object> findTemplate(@Param("id") Long id);

    @Insert("""
            INSERT INTO vi_message_template (name, provider, owner_dept_id, environment, scene, audience,
                   created_by, created_at, updated_at, version)
            VALUES (#{name}, #{provider}, #{ownerDeptId}, #{environment}, #{scene}, #{audience},
                   #{createdBy}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertTemplate(Map<String, Object> row);

    @Update("UPDATE vi_message_template SET revision_seq = revision_seq + 1 WHERE id = #{id}")
    int bumpTemplateSeq(@Param("id") Long id);

    @Select("SELECT revision_seq FROM vi_message_template WHERE id = #{id}")
    Long currentTemplateSeq(@Param("id") Long id);

    @Insert("""
            INSERT INTO vi_message_template_revision (template_id, revision_no, profile_id, profile_revision_id,
                   vendor_template_id, parameter_bindings, options_json, enabled, approval_status, content_hash,
                   created_by, reason, created_at, updated_at, version)
            VALUES (#{templateId}, #{revisionNo}, #{profileId}, #{profileRevisionId},
                   #{vendorTemplateId}, #{parameterBindings}, #{optionsJson}, #{enabled}, #{approvalStatus}, #{contentHash},
                   #{createdBy}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertTemplateRevision(Map<String, Object> row);

    @Select("SELECT * FROM vi_message_template_revision WHERE id = #{id}")
    Map<String, Object> findTemplateRevision(@Param("id") Long id);

    @Select("""
            SELECT * FROM vi_message_template_revision WHERE template_id = #{templateId}
            ORDER BY revision_no DESC LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> listTemplateRevisions(@Param("templateId") Long templateId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Update("""
            UPDATE vi_message_template SET draft_revision_id = #{draftRevisionId}, updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int updateTemplateDraftPointer(@Param("id") Long id, @Param("draftRevisionId") Long draftRevisionId,
            @Param("version") long version);

    @Update("""
            UPDATE vi_message_template SET active_revision_id = #{revisionId}, draft_revision_id = #{revisionId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int publishTemplate(@Param("id") Long id, @Param("revisionId") Long revisionId, @Param("version") long version);

    // ------------------------------------------------------------ 场景路由

    @Select("""
            <script>
            SELECT * FROM vi_notification_route WHERE 1 = 1
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="environment != null and environment != ''"> AND environment = #{environment} </if>
            ORDER BY scene ASC, audience ASC LIMIT 200
            </script>
            """)
    List<Map<String, Object>> listRoutes(@Param("deptId") Long deptId, @Param("environment") String environment);

    @Select("SELECT * FROM vi_notification_route WHERE id = #{id}")
    Map<String, Object> findRoute(@Param("id") Long id);

    /**
     * 运行时路由解析（业务事件下发用）。
     *
     * <p>优先取本组织的路由，没有则退回全局路由（owner_dept_id IS NULL）；
     * 组织与全局都没有时返回空，调用方按「未配置」处理，绝不猜一个通道发出去。</p>
     */
    @Select("""
            SELECT * FROM vi_notification_route
            WHERE environment = #{environment} AND scene = #{scene} AND audience = #{audience}
              AND (owner_dept_id = #{deptId} OR owner_dept_id IS NULL)
            ORDER BY (owner_dept_id <=> #{deptId}) DESC, id DESC
            LIMIT 1
            """)
    Map<String, Object> findApplicableRoute(@Param("deptId") Long deptId,
            @Param("environment") String environment, @Param("scene") String scene,
            @Param("audience") String audience);

    @Select("""
            SELECT * FROM vi_notification_route
            WHERE owner_dept_id = #{deptId} AND environment = #{environment} AND scene = #{scene} AND audience = #{audience}
            """)
    Map<String, Object> findRouteByKey(@Param("deptId") Long deptId, @Param("environment") String environment,
            @Param("scene") String scene, @Param("audience") String audience);

    @Insert("""
            INSERT INTO vi_notification_route (owner_dept_id, environment, scene, audience, primary_template_id,
                   fallback_template_id, fallback_policy, enabled, updated_by, reason, created_at, updated_at, version)
            VALUES (#{ownerDeptId}, #{environment}, #{scene}, #{audience}, #{primaryTemplateId},
                   #{fallbackTemplateId}, #{fallbackPolicy}, #{enabled}, #{updatedBy}, #{reason},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertRoute(Map<String, Object> row);

    @Update("""
            UPDATE vi_notification_route SET primary_template_id = #{primaryTemplateId},
                   fallback_template_id = #{fallbackTemplateId}, fallback_policy = #{fallbackPolicy},
                   enabled = #{enabled}, updated_by = #{updatedBy}, reason = #{reason},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int updateRoute(@Param("id") Long id, @Param("primaryTemplateId") Long primaryTemplateId,
            @Param("fallbackTemplateId") Long fallbackTemplateId, @Param("fallbackPolicy") String fallbackPolicy,
            @Param("enabled") boolean enabled, @Param("updatedBy") Long updatedBy,
            @Param("reason") String reason, @Param("version") long version);

    // ------------------------------------------------------------ 发送执行

    @Select("""
            <script>
            SELECT * FROM vi_notification_delivery WHERE 1 = 1
            <if test="status != null and status != ''"> AND status = #{status} </if>
            <if test="provider != null and provider != ''"> AND provider = #{provider} </if>
            <if test="scene != null and scene != ''"> AND scene = #{scene} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> listDeliveries(@Param("status") String status, @Param("provider") String provider,
            @Param("scene") String scene, @Param("deptId") Long deptId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_notification_delivery WHERE 1 = 1
            <if test="status != null and status != ''"> AND status = #{status} </if>
            <if test="provider != null and provider != ''"> AND provider = #{provider} </if>
            <if test="scene != null and scene != ''"> AND scene = #{scene} </if>
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            </script>
            """)
    long countDeliveries(@Param("status") String status, @Param("provider") String provider,
            @Param("scene") String scene, @Param("deptId") Long deptId);

    @Select("SELECT * FROM vi_notification_delivery WHERE id = #{id}")
    Map<String, Object> findDelivery(@Param("id") Long id);

    /**
     * 写入一条投递轨迹。
     *
     * <p>唯一键 (event_key, recipient_key, scene) 保证同一业务事件对同一接收人
     * 只下发一次；重复触发时 INSERT IGNORE 返回 0，调用方据此跳过发送。</p>
     */
    @Insert("""
            INSERT IGNORE INTO vi_notification_delivery (event_key, recipient_key, owner_dept_id, environment,
                   route_id, scene, provider, template_id, template_revision_id, profile_id, profile_revision_id,
                   status, attempt_count, created_at, updated_at, version)
            VALUES (#{eventKey}, #{recipientKey}, #{ownerDeptId}, #{environment},
                   #{routeId}, #{scene}, #{provider}, #{templateId}, #{templateRevisionId}, #{profileId}, #{profileRevisionId},
                   #{status}, 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertDelivery(Map<String, Object> row);

    /** 回写下发结果：ACCEPTED 只代表供应商已受理，到达与否必须走回执 */
    @Update("""
            UPDATE vi_notification_delivery SET status = #{status}, vendor_request_id = #{requestId},
                   vendor_biz_id = #{bizId}, safe_error_code = #{errorCode}, safe_error_message = #{errorMessage},
                   attempt_count = attempt_count + 1,
                   accepted_at = IF(#{status} = 'ACCEPTED', UTC_TIMESTAMP(3), accepted_at),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int markDeliveryResult(@Param("id") Long id, @Param("status") String status,
            @Param("requestId") String requestId, @Param("bizId") String bizId,
            @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage);

    @Update("""
            UPDATE vi_notification_delivery SET status = #{status}, safe_error_code = #{errorCode},
                   safe_error_message = #{errorMessage}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version}
            """)
    int updateDeliveryStatus(@Param("id") Long id, @Param("status") String status,
            @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage,
            @Param("version") long version);

    @Update("""
            UPDATE vi_notification_delivery SET status = 'PENDING', attempt_count = attempt_count + 1,
                   next_attempt_at = NULL, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'FAILED' AND version = #{version}
            """)
    int retryDelivery(@Param("id") Long id, @Param("version") long version);

    // ------------------------------------------------------------ 订单与支付

    @Select("SELECT * FROM vi_order WHERE id = #{id}")
    Map<String, Object> findOrderById(@Param("id") Long id);

    @Select("SELECT order_no FROM vi_order WHERE id = #{id}")
    String findOrderNoById(@Param("id") Long id);

    /** 业务事件下发时拼装模板变量所需的订单要素 */
    @Select("""
            SELECT o.id, o.order_no, o.dept_id, o.station_id, o.pickup_starts_at, o.pickup_ends_at,
                   o.fulfillment_status, o.vehicle_snapshot, s.name AS station_name
            FROM vi_order o LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE o.id = #{id}
            """)
    Map<String, Object> findOrderForNotify(@Param("id") Long id);

    @Select("""
            SELECT o.* FROM vi_order o WHERE o.order_no = #{orderNo}
            """)
    Map<String, Object> findOrderByNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM vi_payment_order WHERE order_id = #{orderId}")
    Map<String, Object> findPaymentByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_payment_order WHERE order_id = #{orderId} FOR UPDATE")
    Map<String, Object> lockPaymentByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_payment_order WHERE id = #{id}")
    Map<String, Object> findPaymentById(@Param("id") Long id);

    @Update("""
            UPDATE vi_payment_order SET payment_method = 'OFFLINE', method_locked_at = UTC_TIMESTAMP(3),
                   policy_revision_id = #{policyRevisionId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version} AND payment_method = 'UNSELECTED'
            """)
    int lockOfflineIntent(@Param("id") Long id, @Param("policyRevisionId") Long policyRevisionId,
            @Param("version") long version);

    @Update("""
            UPDATE vi_payment_order SET status = 'REVIEW_PENDING', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'PENDING' AND version = #{version}
            """)
    int markReviewPending(@Param("id") Long id, @Param("version") long version);

    @Update("""
            UPDATE vi_payment_order SET status = 'PENDING', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'REVIEW_PENDING' AND version = #{version}
            """)
    int revertToPending(@Param("id") Long id, @Param("version") long version);

    @Update("""
            UPDATE vi_payment_order SET payment_method = 'WECHAT', method_locked_at = UTC_TIMESTAMP(3),
                   policy_revision_id = #{policyRevisionId}, merchant_profile_id = #{merchantProfileId},
                   merchant_revision_id = #{merchantRevisionId}, app_id = #{appId}, mchid = #{mchid},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version} AND payment_method = 'UNSELECTED'
            """)
    int lockWechatIntent(@Param("id") Long id, @Param("policyRevisionId") Long policyRevisionId,
            @Param("merchantProfileId") Long merchantProfileId, @Param("merchantRevisionId") Long merchantRevisionId,
            @Param("appId") String appId, @Param("mchid") String mchid, @Param("version") long version);

    @Update("""
            UPDATE vi_payment_order SET status = 'SUCCESS', paid_fen = #{paidFen}, paid_at = #{paidAt},
                   capacity_resolution = #{capacity}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND version = #{version} AND status <> 'SUCCESS'
            """)
    int markOfflinePaid(@Param("id") Long id, @Param("paidFen") long paidFen,
            @Param("paidAt") java.time.LocalDateTime paidAt, @Param("capacity") String capacity,
            @Param("version") long version);

    /** 线下款项核实全额后，才把仍处待支付的订单推进到 SCHEDULED 并获得派单资格 */
    @Update("""
            UPDATE vi_order SET fulfillment_status = 'SCHEDULED',
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId} AND fulfillment_status = 'PENDING_PAY'
            """)
    int releaseToScheduledAfterOfflinePaid(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_refund_order WHERE id = #{id}")
    Map<String, Object> findRefundById(@Param("id") Long id);

    @Select("SELECT * FROM vi_refund_order WHERE out_refund_no = #{refundNo}")
    Map<String, Object> findRefundByNo(@Param("refundNo") String refundNo);

    @Select("""
            SELECT * FROM vi_refund_order WHERE payment_id = #{paymentId} AND status IN ('CREATED','PROCESSING','ABNORMAL','UNKNOWN')
            ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findOpenRefundByPayment(@Param("paymentId") Long paymentId);

    @Update("""
            UPDATE vi_refund_order SET status = 'SUCCESS', success_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status IN ('CREATED','PROCESSING','ABNORMAL','UNKNOWN')
            """)
    int markOfflineRefundDone(@Param("id") Long id);

    @Update("""
            UPDATE vi_payment_order SET refunded_fen = refunded_fen + #{amountFen},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND refunded_fen + refund_reserved_fen + #{amountFen} <= paid_fen
            """)
    int addRefundedFen(@Param("id") Long id, @Param("amountFen") long amountFen);

    // ------------------------------------------------------------ 线下凭证

    @Insert("""
            INSERT INTO vi_offline_payment_record (payment_id, refund_id, direction, owner_dept_id, amount_fen,
                   paid_at, method, external_reference_cipher, reference_hmac, voucher_file_ids, status,
                   registered_by, registered_at, register_note, created_at, updated_at, version)
            VALUES (#{paymentId}, #{refundId}, #{direction}, #{ownerDeptId}, #{amountFen},
                   #{paidAt}, #{method}, #{referenceCipher}, #{referenceHmac}, #{voucherFileIds}, 'SUBMITTED',
                   #{registeredBy}, UTC_TIMESTAMP(3), #{note}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertOfflineRecord(Map<String, Object> row);

    @Select("SELECT * FROM vi_offline_payment_record WHERE id = #{id}")
    Map<String, Object> findOfflineRecord(@Param("id") Long id);

    @Select("SELECT * FROM vi_offline_payment_record WHERE id = #{id} FOR UPDATE")
    Map<String, Object> lockOfflineRecord(@Param("id") Long id);

    @Select("""
            <script>
            SELECT * FROM vi_offline_payment_record WHERE 1 = 1
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="status != null and status != ''"> AND status = #{status} </if>
            <if test="direction != null and direction != ''"> AND direction = #{direction} </if>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Map<String, Object>> listOfflineRecords(@Param("deptId") Long deptId, @Param("status") String status,
            @Param("direction") String direction, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_offline_payment_record WHERE 1 = 1
            <if test="deptId != null"> AND owner_dept_id = #{deptId} </if>
            <if test="status != null and status != ''"> AND status = #{status} </if>
            <if test="direction != null and direction != ''"> AND direction = #{direction} </if>
            </script>
            """)
    long countOfflineRecords(@Param("deptId") Long deptId, @Param("status") String status,
            @Param("direction") String direction);

    @Select("""
            SELECT * FROM vi_offline_payment_record
            WHERE owner_dept_id = #{deptId} AND direction = #{direction} AND method = #{method}
              AND reference_hmac = #{referenceHmac}
            """)
    Map<String, Object> findOfflineByReference(@Param("deptId") Long deptId, @Param("direction") String direction,
            @Param("method") String method, @Param("referenceHmac") String referenceHmac);

    @Update("""
            UPDATE vi_offline_payment_record SET status = #{status}, verified_by = #{verifiedBy},
                   verified_at = UTC_TIMESTAMP(3), verify_reason = #{reason},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'SUBMITTED' AND version = #{version}
            """)
    int verifyOfflineRecord(@Param("id") Long id, @Param("status") String status,
            @Param("verifiedBy") Long verifiedBy, @Param("reason") String reason, @Param("version") long version);

    // ------------------------------------------------------------ 订阅 prompt

    @Insert("""
            INSERT INTO vi_subscription_prompt (account_id, app_id, nonce_hash, template_versions,
                   business_object_type, business_object_id, status, expires_at, created_at, updated_at, version)
            VALUES (#{accountId}, #{appId}, #{nonceHash}, #{templateVersions},
                   #{businessObjectType}, #{businessObjectId}, 'CREATED',
                   DATE_ADD(UTC_TIMESTAMP(3), INTERVAL #{ttlMinutes} MINUTE), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertPrompt(Map<String, Object> row);

    @Select("SELECT * FROM vi_subscription_prompt WHERE nonce_hash = #{nonceHash}")
    Map<String, Object> findPromptByNonce(@Param("nonceHash") String nonceHash);

    @Select("SELECT * FROM vi_subscription_prompt WHERE id = #{id}")
    Map<String, Object> findPrompt(@Param("id") Long id);

    @Update("""
            UPDATE vi_subscription_prompt SET status = 'REPORTED', reported_at = UTC_TIMESTAMP(3),
                   client_report = #{clientReport}, report_hash = #{reportHash},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'CREATED'
            """)
    int reportPrompt(@Param("id") Long id, @Param("clientReport") String clientReport,
            @Param("reportHash") String reportHash);

    @Update("""
            UPDATE vi_subscription_prompt SET status = 'EXPIRED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE status = 'CREATED' AND expires_at < UTC_TIMESTAMP(3)
            """)
    int expirePrompts();

    @Select("""
            SELECT COUNT(1) FROM vi_subscription_prompt
            WHERE account_id = #{accountId} AND created_at >= DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 MINUTE)
            """)
    long countRecentPrompts(@Param("accountId") Long accountId);

    // ------------------------------------------------------------ 站内消息（订阅被拒仍要留站内信）

    @Insert("""
            INSERT INTO vi_notification (account_id, role, order_id, scene, channel, content, created_at, updated_at, version)
            VALUES (#{accountId}, #{role}, #{orderId}, #{scene}, 'INBOX', #{content}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertInbox(Map<String, Object> row);
}
