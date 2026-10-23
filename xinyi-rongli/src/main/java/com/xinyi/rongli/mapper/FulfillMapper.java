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
 * 实物交接、交接码防爆破、证据组、私有文件、行驶证与检测结果。
 */
@Mapper
public interface FulfillMapper
{
    // ------------------------------------------------------------------ 交接

    @Insert("""
            INSERT INTO vi_handover (order_id, assignment_id, handover_type, holder_account_id, verifier_account_id,
                   code_mac, code_cipher, generation_no, status, expires_at, created_at, updated_at, version)
            VALUES (#{orderId}, #{assignmentId}, #{handoverType}, #{holderAccountId}, #{verifierAccountId},
                   #{codeMac}, #{codeCipher}, #{generationNo}, 'PENDING', #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertHandover(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_handover
            WHERE order_id = #{orderId} AND assignment_id = #{assignmentId} AND handover_type = #{handoverType}
            ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findLatestHandover(@Param("orderId") Long orderId, @Param("assignmentId") Long assignmentId,
            @Param("handoverType") String handoverType);

    @Select("SELECT * FROM vi_handover WHERE id = #{handoverId}")
    Map<String, Object> findHandover(@Param("handoverId") Long handoverId);

    @Select("""
            SELECT h.*, o.order_no, o.fulfillment_status, o.custody_type
            FROM vi_handover h JOIN vi_order o ON o.id = h.order_id
            WHERE h.id = #{handoverId}
            """)
    Map<String, Object> findHandoverDetail(@Param("handoverId") Long handoverId);

    @Select("""
            SELECT * FROM vi_handover WHERE order_id = #{orderId} AND status IN ('PENDING', 'PARTIAL')
            ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findPendingHandoverOfOrder(@Param("orderId") Long orderId);

    /** 单方确认：PENDING 直接完成，并记录核验方确认时刻 */
    @Update("""
            UPDATE vi_handover SET status = 'CONFIRMED', confirmed_at = UTC_TIMESTAMP(3),
                   verifier_confirmed_at = UTC_TIMESTAMP(3),
                   evidence_group_id = #{evidenceGroupId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{handoverId} AND status = 'PENDING'
            """)
    int confirmHandover(@Param("handoverId") Long handoverId, @Param("evidenceGroupId") Long evidenceGroupId);

    /**
     * 双方确认 · 核验方先确认。
     *
     * <p>持码方若已确认过则本步直接置为 CONFIRMED，否则停留在 PARTIAL 等待持码方。
     * {@code verifier_confirmed_at IS NULL} 保证同一方重复提交不会重复推进。</p>
     */
    @Update("""
            UPDATE vi_handover SET verifier_confirmed_at = UTC_TIMESTAMP(3),
                   evidence_group_id = COALESCE(#{evidenceGroupId}, evidence_group_id),
                   status = IF(holder_confirmed_at IS NULL, 'PARTIAL', 'CONFIRMED'),
                   confirmed_at = IF(holder_confirmed_at IS NULL, confirmed_at, UTC_TIMESTAMP(3)),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{handoverId} AND status IN ('PENDING', 'PARTIAL') AND verifier_confirmed_at IS NULL
            """)
    int confirmVerifierSide(@Param("handoverId") Long handoverId, @Param("evidenceGroupId") Long evidenceGroupId);

    /** 双方确认 · 持码方确认已交付；核验方已确认时一并完成 */
    @Update("""
            UPDATE vi_handover SET holder_confirmed_at = UTC_TIMESTAMP(3),
                   status = IF(verifier_confirmed_at IS NULL, 'PARTIAL', 'CONFIRMED'),
                   confirmed_at = IF(verifier_confirmed_at IS NULL, confirmed_at, UTC_TIMESTAMP(3)),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{handoverId} AND status IN ('PENDING', 'PARTIAL') AND holder_confirmed_at IS NULL
            """)
    int confirmHolderSide(@Param("handoverId") Long handoverId);

    @Update("""
            UPDATE vi_handover SET status = 'EXPIRED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{handoverId} AND status IN ('PENDING', 'PARTIAL')
            """)
    int expireHandover(@Param("handoverId") Long handoverId);

    @Update("""
            UPDATE vi_handover SET status = 'LOCKED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{handoverId} AND status IN ('PENDING', 'PARTIAL')
            """)
    int lockHandover(@Param("handoverId") Long handoverId);

    @Select("""
            SELECT * FROM vi_handover WHERE order_id = #{orderId} ORDER BY id DESC LIMIT 20
            """)
    List<Map<String, Object>> listHandoversOfOrder(@Param("orderId") Long orderId);

    @Insert("""
            INSERT INTO vi_handover_attempt_guard (guard_key, failed_attempts, created_at, updated_at, version)
            VALUES (#{guardKey}, 1, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE failed_attempts = failed_attempts + 1,
                   locked_at = IF(failed_attempts + 1 >= 3, COALESCE(locked_at, UTC_TIMESTAMP(3)), locked_at),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int bumpAttemptGuard(@Param("guardKey") String guardKey);

    @Select("SELECT * FROM vi_handover_attempt_guard WHERE guard_key = #{guardKey}")
    Map<String, Object> findAttemptGuard(@Param("guardKey") String guardKey);

    // ------------------------------------------------------------------ 证据

    @Select("SELECT * FROM vi_evidence_group WHERE assignment_id = #{assignmentId} AND node_no = #{nodeNo}")
    Map<String, Object> findEvidenceGroup(@Param("assignmentId") Long assignmentId, @Param("nodeNo") int nodeNo);

    @Select("SELECT * FROM vi_evidence_group WHERE id = #{groupId}")
    Map<String, Object> findGroupById(@Param("groupId") Long groupId);

    @Insert("""
            INSERT INTO vi_evidence_group (order_id, assignment_id, node_no, status, created_at, updated_at, version)
            VALUES (#{orderId}, #{assignmentId}, #{nodeNo}, 'DRAFT', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertEvidenceGroup(Map<String, Object> row);

    @Update("""
            UPDATE vi_evidence_group SET mileage_km = #{mileageKm}, mileage_source = #{source},
                   mileage_reason = #{reason}, status = IF(status = 'DRAFT', 'READY', status),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{groupId} AND status <> 'SEALED'
            """)
    int updateMileage(@Param("groupId") Long groupId, @Param("mileageKm") java.math.BigDecimal mileageKm,
            @Param("source") String source, @Param("reason") String reason);

    @Update("""
            UPDATE vi_evidence_group SET status = 'READY', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{groupId} AND status = 'DRAFT'
            """)
    int markEvidenceReady(@Param("groupId") Long groupId);

    @Update("""
            UPDATE vi_evidence_group SET status = 'SEALED', sealed_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{groupId} AND status IN ('DRAFT','READY')
            """)
    int sealEvidence(@Param("groupId") Long groupId);

    @Select("""
            SELECT id, group_id, photo_type, file_id, revision, captured_at, received_at, watermark_file_id
            FROM vi_photo WHERE group_id = #{groupId} AND superseded_at IS NULL ORDER BY id
            """)
    List<Map<String, Object>> listPhotos(@Param("groupId") Long groupId);

    @Insert("""
            INSERT INTO vi_photo (group_id, photo_type, file_id, client_capture_id, revision, captured_at,
                   received_at, location_cipher, created_at, updated_at, version)
            VALUES (#{groupId}, #{photoType}, #{fileId}, #{captureId}, #{revision}, #{capturedAt},
                   UTC_TIMESTAMP(3), #{location}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPhoto(Map<String, Object> row);

    @Update("""
            UPDATE vi_photo SET superseded_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE group_id = #{groupId} AND photo_type = #{photoType} AND superseded_at IS NULL
            """)
    int supersedePhotos(@Param("groupId") Long groupId, @Param("photoType") String photoType);

    @Select("""
            SELECT COUNT(1) FROM vi_photo WHERE group_id = #{groupId} AND superseded_at IS NULL AND photo_type <> 'EXTRA'
            """)
    int countActivePhotos(@Param("groupId") Long groupId);

    /** 已提交的有效照片类型（EXTRAS 追加照不计入完整性判定） */
    @Select("""
            SELECT DISTINCT photo_type FROM vi_photo
            WHERE group_id = #{groupId} AND superseded_at IS NULL AND photo_type <> 'EXTRA'
            """)
    List<String> listActivePhotoTypes(@Param("groupId") Long groupId);

    // ------------------------------------------------------------------ 文件

    @Insert("""
            INSERT INTO vi_file (uploader_account_id, purpose, order_id, application_id, object_key, status,
                   mime_type, server_size, created_at, updated_at, version)
            VALUES (#{uploaderAccountId}, #{purpose}, #{orderId}, #{applicationId}, #{objectKey}, 'UPLOADING',
                   #{mimeType}, #{size}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFile(Map<String, Object> row);

    @Select("SELECT * FROM vi_file WHERE id = #{fileId}")
    Map<String, Object> findFile(@Param("fileId") Long fileId);

    @Update("""
            UPDATE vi_file SET status = 'READY', server_size = #{size}, server_sha256 = #{sha256},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{fileId} AND status IN ('UPLOADING','VERIFYING')
            """)
    int markFileReady(@Param("fileId") Long fileId, @Param("size") long size, @Param("sha256") byte[] sha256);

    // ------------------------------------------------------------------ 行驶证

    @Select("SELECT * FROM vi_vehicle_license WHERE order_id = #{orderId}")
    Map<String, Object> findLicense(@Param("orderId") Long orderId);

    @Insert("""
            INSERT INTO vi_vehicle_license (order_id, sensitive_payload_cipher, main_file_id, sub_file_id,
                   review_status, created_at, updated_at, version)
            VALUES (#{orderId}, #{payload}, #{mainFileId}, #{subFileId}, 'PENDING', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE sensitive_payload_cipher = VALUES(sensitive_payload_cipher),
                   main_file_id = VALUES(main_file_id), sub_file_id = VALUES(sub_file_id),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int upsertLicense(@Param("orderId") Long orderId, @Param("payload") byte[] payload,
            @Param("mainFileId") Long mainFileId, @Param("subFileId") Long subFileId);

    @Update("""
            UPDATE vi_vehicle_license SET review_status = 'VERIFIED', reviewer_user_id = #{userId},
                   reviewed_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE order_id = #{orderId}
            """)
    int verifyLicense(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // ------------------------------------------------------------------ 检测结果

    @Select("""
            SELECT * FROM vi_inspection_result WHERE order_id = #{orderId} AND superseded_at IS NULL
            """)
    Map<String, Object> findCurrentResult(@Param("orderId") Long orderId);

    @Select("""
            SELECT * FROM vi_inspection_result WHERE order_id = #{orderId} ORDER BY revision DESC
            """)
    List<Map<String, Object>> listResults(@Param("orderId") Long orderId);

    @Select("SELECT COALESCE(MAX(revision), 0) FROM vi_inspection_result WHERE order_id = #{orderId}")
    int maxResultRevision(@Param("orderId") Long orderId);

    @Insert("""
            INSERT INTO vi_inspection_result (order_id, revision, conclusion, items_json, inspector_user_id,
                   submitted_by, source, inspected_at, submitted_at, created_at, updated_at, version)
            VALUES (#{orderId}, #{revision}, #{conclusion}, #{itemsJson}, #{inspectorUserId},
                   #{submittedBy}, #{source}, #{inspectedAt}, #{submittedAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertResult(Map<String, Object> row);

    @Update("""
            UPDATE vi_inspection_result SET superseded_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE order_id = #{orderId} AND superseded_at IS NULL
            """)
    int supersedeCurrentResult(@Param("orderId") Long orderId);

    @Insert("""
            INSERT INTO vi_result_correction (order_id, base_result_id, replacement_json, status, reason,
                   applicant_id, created_at, updated_at, version)
            VALUES (#{orderId}, #{baseResultId}, #{replacementJson}, 'APPLIED', #{reason}, #{applicantId},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCorrection(Map<String, Object> row);

    @Select("SELECT * FROM vi_result_correction WHERE id = #{correctionId}")
    Map<String, Object> findCorrection(@Param("correctionId") Long correctionId);

    @Update("""
            UPDATE vi_result_correction SET status = #{status}, reviewer_id = #{reviewer}, opinion = #{opinion},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{correctionId} AND status = 'APPLIED'
            """)
    int auditCorrection(@Param("correctionId") Long correctionId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("opinion") String opinion);

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
}
