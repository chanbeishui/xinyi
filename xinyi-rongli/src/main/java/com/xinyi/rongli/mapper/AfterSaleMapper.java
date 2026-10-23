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
 * 统一售后申请与追加式审核决定。
 */
@Mapper
public interface AfterSaleMapper
{
    @Insert("""
            INSERT INTO vi_aftersale_case (case_no, order_id, payment_id, kind, case_status, review_status,
                   review_revision, requested_fen, return_state, refund_state, applicant_type, applicant_id,
                   reason, evidence_ids, created_at, updated_at, version)
            VALUES (#{caseNo}, #{orderId}, #{paymentId}, #{kind}, 'PENDING_REVIEW', 'PENDING',
                   1, #{requestedFen}, 'UNDETERMINED', 'NONE', #{applicantType}, #{applicantId},
                   #{reason}, #{evidenceIds}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCase(Map<String, Object> row);

    @Select("SELECT * FROM vi_aftersale_case WHERE id = #{caseId}")
    Map<String, Object> findCase(@Param("caseId") Long caseId);

    @Select("""
            SELECT c.*, o.order_no, o.fulfillment_status, o.custody_type, o.total_fen AS order_total_fen,
                   o.pickup_starts_at, o.vehicle_snapshot
            FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id WHERE c.id = #{caseId}
            """)
    Map<String, Object> findCaseDetail(@Param("caseId") Long caseId);

    @Select("SELECT * FROM vi_aftersale_case WHERE order_id = #{orderId} AND active_order IS NOT NULL")
    Map<String, Object> findOpenCaseOfOrder(@Param("orderId") Long orderId);

    @Select("""
            <script>
            SELECT c.*, o.order_no, o.fulfillment_status, o.vehicle_snapshot
            FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.case_status = #{status} </if>
            <if test="kind != null and kind != ''"> AND c.kind = #{kind} </if>
            <if test="deptId != null">
              AND o.dept_id IN (SELECT dept_id FROM sys_dept
                                WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors))
            </if>
            ORDER BY c.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminList(@Param("status") String status, @Param("kind") String kind,
            @Param("deptId") Long deptId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.case_status = #{status} </if>
            <if test="kind != null and kind != ''"> AND c.kind = #{kind} </if>
            <if test="deptId != null">
              AND o.dept_id IN (SELECT dept_id FROM sys_dept
                                WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors))
            </if>
            </script>
            """)
    long adminCount(@Param("status") String status, @Param("kind") String kind,
            @Param("deptId") Long deptId);

    /** 二次授权：该售后单是否落在当前部门数据范围内 */
    @Select("""
            SELECT COUNT(1) FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
            WHERE c.id = #{caseId}
              AND o.dept_id IN (SELECT dept_id FROM sys_dept
                                WHERE dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, ancestors))
            """)
    long countInDept(@Param("caseId") Long caseId, @Param("deptId") Long deptId);

    @Select("""
            SELECT c.*, o.order_no, o.fulfillment_status, o.vehicle_snapshot
            FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
            WHERE c.order_id = #{orderId} ORDER BY c.id DESC
            """)
    List<Map<String, Object>> listByOrder(@Param("orderId") Long orderId);

    @Select("""
            SELECT c.*, o.order_no, o.fulfillment_status, o.vehicle_snapshot
            FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
            WHERE c.applicant_type = 'ACCOUNT' AND c.applicant_id = #{accountId}
            ORDER BY c.id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listByApplicant(@Param("accountId") Long accountId, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            SELECT COUNT(1) FROM vi_aftersale_case
            WHERE applicant_type = 'ACCOUNT' AND applicant_id = #{accountId}
            """)
    long countByApplicant(@Param("accountId") Long accountId);

    /** 审核决定：同时写 case 结果与 review_revision，review 表保留追加式决定凭证 */
    @Update("""
            UPDATE vi_aftersale_case SET case_status = #{caseStatus}, review_status = #{reviewStatus},
                   review_revision = review_revision + 1, approved_fen = #{approvedFen},
                   return_state = #{returnState}, refund_state = #{refundState},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{caseId} AND review_status = 'PENDING'
            """)
    int applyReview(@Param("caseId") Long caseId, @Param("caseStatus") String caseStatus,
            @Param("reviewStatus") String reviewStatus, @Param("approvedFen") Long approvedFen,
            @Param("returnState") String returnState, @Param("refundState") String refundState);

    @Update("""
            UPDATE vi_aftersale_case SET case_status = 'REJECTED', review_status = 'REJECTED',
                   review_revision = review_revision + 1, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{caseId} AND review_status = 'PENDING'
            """)
    int rejectCase(@Param("caseId") Long caseId);

    @Update("""
            UPDATE vi_aftersale_case SET case_status = #{caseStatus}, return_state = #{returnState},
                   refund_state = #{refundState}, resolved_at = #{resolvedAt},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{caseId}
            """)
    int updateProgress(@Param("caseId") Long caseId, @Param("caseStatus") String caseStatus,
            @Param("returnState") String returnState, @Param("refundState") String refundState,
            @Param("resolvedAt") java.time.LocalDateTime resolvedAt);

    @Insert("""
            INSERT INTO vi_aftersale_review (case_id, revision, decision, approved_fen, observed_order_version,
                   custody_at_review, return_required, reviewer_user_id, opinion, reviewed_at, created_at, updated_at, version)
            VALUES (#{caseId}, #{revision}, #{decision}, #{approvedFen}, #{observedOrderVersion},
                   #{custodyAtReview}, #{returnRequired}, #{reviewerUserId}, #{opinion}, UTC_TIMESTAMP(3),
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertReview(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_aftersale_review WHERE case_id = #{caseId} ORDER BY revision DESC
            """)
    List<Map<String, Object>> listReviews(@Param("caseId") Long caseId);
}
