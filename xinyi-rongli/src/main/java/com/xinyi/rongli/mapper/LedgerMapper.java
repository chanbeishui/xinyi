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
 * 代驾费用规则、费用台账、对账单、行内核价、付款凭证与调差。
 */
@Mapper
public interface LedgerMapper
{
    // ------------------------------------------------------------------ 费用规则

    @Select("""
            SELECT * FROM vi_driver_fee_rule
            WHERE payor_entity_id = #{payorEntityId} AND scenario = #{scenario} AND status = 'PUBLISHED'
              AND effective_from <= UTC_TIMESTAMP(3) AND (effective_to IS NULL OR effective_to > UTC_TIMESTAMP(3))
            ORDER BY revision DESC LIMIT 1
            """)
    Map<String, Object> findPublishedFeeRule(@Param("payorEntityId") Long payorEntityId,
            @Param("scenario") String scenario);

    @Select("""
            <script>
            SELECT * FROM vi_driver_fee_rule
            <where>
              <if test="status != null and status != ''"> AND status = #{status} </if>
            </where>
            ORDER BY payor_entity_id, rule_code, revision DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listFeeRules(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT * FROM vi_driver_fee_rule WHERE id = #{ruleId}")
    Map<String, Object> findFeeRule(@Param("ruleId") Long ruleId);

    @Select("SELECT COALESCE(MAX(revision), 0) FROM vi_driver_fee_rule WHERE payor_entity_id = #{payorEntityId} AND rule_code = #{ruleCode}")
    int maxFeeRuleRevision(@Param("payorEntityId") Long payorEntityId, @Param("ruleCode") String ruleCode);

    @Insert("""
            INSERT INTO vi_driver_fee_rule (payor_entity_id, rule_code, revision, currency, scenario, scope_json,
                   pricing_json, status, effective_from, effective_to, created_by, created_at, updated_at, version)
            VALUES (#{payorEntityId}, #{ruleCode}, #{revision}, 'CNY', #{scenario}, #{scopeJson},
                   #{pricingJson}, #{status}, #{effectiveFrom}, #{effectiveTo}, #{createdBy},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFeeRule(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_fee_rule SET scope_json = #{scopeJson}, pricing_json = #{pricingJson},
                   effective_from = #{effectiveFrom}, effective_to = #{effectiveTo},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ruleId} AND status = 'DRAFT'
            """)
    int updateFeeRuleDraft(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_fee_rule SET status = #{to}, approved_by = #{actor}, approved_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ruleId} AND status = #{from}
            """)
    int casFeeRuleStatus(@Param("ruleId") Long ruleId, @Param("from") String from, @Param("to") String to,
            @Param("actor") Long actor);

    // ------------------------------------------------------------------ 费用台账

    @Insert("""
            INSERT INTO vi_driver_fee_ledger (driver_id, payor_entity_id, currency, assignment_id, order_id,
                   source_event_key, entry_kind, service_ended_at, pricing_status, fee_rule_id, rule_snapshot,
                   amount_fen, fee_breakdown, source_entry_id, created_at, updated_at, version)
            VALUES (#{driverId}, #{payorEntityId}, 'CNY', #{assignmentId}, #{orderId},
                   #{sourceEventKey}, #{entryKind}, #{serviceEndedAt}, #{pricingStatus}, #{feeRuleId}, #{ruleSnapshot},
                   #{amountFen}, #{feeBreakdown}, #{sourceEntryId}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLedger(Map<String, Object> row);

    @Select("SELECT * FROM vi_driver_fee_ledger WHERE id = #{ledgerId}")
    Map<String, Object> findLedger(@Param("ledgerId") Long ledgerId);

    @Select("""
            SELECT * FROM vi_driver_fee_ledger
            WHERE payor_entity_id = #{payorEntityId} AND driver_id = #{driverId} AND source_event_key = #{sourceEventKey}
            """)
    Map<String, Object> findLedgerBySource(@Param("payorEntityId") Long payorEntityId,
            @Param("driverId") Long driverId, @Param("sourceEventKey") String sourceEventKey);

    @Select("""
            SELECT * FROM vi_driver_fee_ledger WHERE order_id = #{orderId} ORDER BY id
            """)
    List<Map<String, Object>> listLedgerOfOrder(@Param("orderId") Long orderId);

    @Select("""
            <script>
            SELECT l.id, l.driver_id, l.payor_entity_id, l.assignment_id, l.order_id, l.entry_kind,
                   l.service_ended_at, l.pricing_status, l.amount_fen, l.included_statement_id,
                   o.order_no, JSON_UNQUOTE(JSON_EXTRACT(o.vehicle_snapshot, '$.plateNo')) AS plate_no,
                   al.statement_id, s.statement_no, s.status AS statement_status
            FROM vi_driver_fee_ledger l
            JOIN vi_order o ON o.id = l.order_id
            LEFT JOIN vi_driver_ledger_allocation al ON al.ledger_id = l.id AND al.released_at IS NULL
            LEFT JOIN vi_driver_statement s ON s.id = al.statement_id
            WHERE l.driver_id = #{driverId}
            <if test="from != null"> AND l.service_ended_at &gt;= #{from} </if>
            <if test="to != null"> AND l.service_ended_at &lt; #{to} </if>
            <if test="keyword != null and keyword != ''">
              AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')
                   OR JSON_UNQUOTE(JSON_EXTRACT(o.vehicle_snapshot, '$.plateNo')) LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="onlySelectable"> AND al.id IS NULL AND l.entry_kind &lt;&gt; 'PRICE_DELTA' AND l.entry_kind &lt;&gt; 'INITIAL_VALUATION' </if>
            ORDER BY l.service_ended_at DESC, l.id DESC LIMIT 500
            </script>
            """)
    List<Map<String, Object>> listEntries(@Param("driverId") Long driverId, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to, @Param("keyword") String keyword,
            @Param("onlySelectable") boolean onlySelectable);

    @Select("""
            SELECT
              SUM(CASE WHEN al.id IS NULL AND l.included_statement_id IS NULL THEN 1 ELSE 0 END) AS pending_count,
              SUM(CASE WHEN l.pricing_status = 'PENDING_PRICING' AND al.id IS NULL THEN 1 ELSE 0 END) AS unpriced_count,
              COALESCE(SUM(CASE WHEN s.status = 'CONFIRMED' AND l.amount_fen IS NOT NULL THEN l.amount_fen ELSE 0 END), 0) AS confirmed_fen,
              COUNT(DISTINCT s.id) AS statement_count
            FROM vi_driver_fee_ledger l
            LEFT JOIN vi_driver_ledger_allocation al ON al.ledger_id = l.id AND al.released_at IS NULL
            LEFT JOIN vi_driver_statement s ON s.id = al.statement_id
            WHERE l.driver_id = #{driverId}
            """)
    Map<String, Object> overview(@Param("driverId") Long driverId);

    @Update("""
            UPDATE vi_driver_fee_ledger SET amount_fen = #{amountFen}, pricing_status = 'READY',
                   fee_breakdown = #{breakdown}, verified_by = #{userId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ledgerId} AND included_statement_id IS NULL
            """)
    int priceLedger(@Param("ledgerId") Long ledgerId, @Param("amountFen") long amountFen,
            @Param("breakdown") String breakdown, @Param("userId") Long userId);

    @Update("""
            UPDATE vi_driver_fee_ledger SET included_statement_id = #{statementId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{ledgerId}
            """)
    int markLedgerIncluded(@Param("ledgerId") Long ledgerId, @Param("statementId") Long statementId);

    // ------------------------------------------------------------------ 对账单

    @Insert("""
            INSERT INTO vi_driver_statement (statement_no, driver_id, payor_entity_id, currency, cutoff_at, status,
                   revision, amount_fen, snapshot_hash, issued_by, known_amount_fen, unpriced_count,
                   manual_price_count, review_status, selection_mode, filter_snapshot, active_entry_count,
                   created_at, updated_at, version)
            VALUES (#{statementNo}, #{driverId}, #{payorEntityId}, 'CNY', UTC_TIMESTAMP(3), 'DRAFT',
                   1, #{amountFen}, #{snapshotHash}, #{issuedBy}, #{knownAmountFen}, #{unpricedCount},
                   0, 'NOT_REQUIRED', 'EXPLICIT', #{filterSnapshot}, #{activeEntryCount},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStatement(Map<String, Object> row);

    @Select("SELECT * FROM vi_driver_statement WHERE id = #{statementId}")
    Map<String, Object> findStatement(@Param("statementId") Long statementId);

    @Select("""
            <script>
            SELECT s.*, d.account_id, a.nickname AS driver_name
            FROM vi_driver_statement s
            JOIN vi_driver_profile d ON d.id = s.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="driverId != null"> AND s.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND s.status = #{status} </if>
            ORDER BY s.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listStatements(@Param("driverId") Long driverId, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_driver_statement s WHERE 1 = 1
            <if test="driverId != null"> AND s.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND s.status = #{status} </if>
            </script>
            """)
    long countStatements(@Param("driverId") Long driverId, @Param("status") String status);

    @Select("""
            SELECT s.*, d.account_id, a.nickname AS driver_name
            FROM vi_driver_statement s
            JOIN vi_driver_profile d ON d.id = s.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE s.id = #{statementId}
            """)
    Map<String, Object> findStatementDetail(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_statement SET revision = revision + 1, amount_fen = #{amountFen},
                   known_amount_fen = #{knownAmountFen}, unpriced_count = #{unpricedCount},
                   manual_price_count = #{manualPriceCount}, active_entry_count = #{activeEntryCount},
                   snapshot_hash = #{snapshotHash}, review_status = #{reviewStatus},
                   service_range_start = #{rangeStart}, service_range_end_exclusive = #{rangeEnd},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status IN ('DRAFT','DISPUTED')
            """)
    int refreshStatement(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_statement SET status = #{to}, review_status = #{reviewStatus},
                   submitted_by = #{actor}, published_revision = #{publishedRevision},
                   published_hash = #{publishedHash}, sent_at = #{sentAt},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status = #{from} AND revision = #{revision}
            """)
    int casStatementStatus(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_statement SET status = 'DRAFT', review_status = 'NOT_REQUIRED',
                   published_revision = NULL, published_hash = NULL,
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status IN ('SENT','DISPUTED','INTERNAL_REVIEW')
            """)
    int withdrawStatement(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_statement SET status = 'VOID', voided_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status = 'DRAFT'
            """)
    int voidStatement(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_statement SET status = 'CONFIRMED', confirmed_account_id = #{accountId},
                   confirmed_at = UTC_TIMESTAMP(3), confirmation_revision = #{revision},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status = 'SENT' AND revision = #{revision}
            """)
    int confirmStatement(@Param("statementId") Long statementId, @Param("accountId") Long accountId,
            @Param("revision") int revision);

    @Update("""
            UPDATE vi_driver_statement SET status = 'DISPUTED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{statementId} AND status = 'SENT'
            """)
    int markDisputed(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_statement SET review_status = #{reviewStatus}, updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{statementId} AND status = 'INTERNAL_REVIEW'
            """)
    int setReviewStatus(@Param("statementId") Long statementId, @Param("reviewStatus") String reviewStatus);

    @Select("""
            SELECT COUNT(1) FROM vi_driver_statement
            WHERE driver_id = #{driverId} AND status IN ('DRAFT','INTERNAL_REVIEW','SENT','DISPUTED','CONFIRMED')
            """)
    long activeStatementCount(@Param("driverId") Long driverId);

    // ------------------------------------------------------------------ 明细行

    @Insert("""
            INSERT INTO vi_driver_statement_line (statement_id, statement_revision, ledger_id, assignment_id,
                   amount_fen, snapshot, line_hash, reference_amount_fen, pricing_source, manual_changed,
                   price_reason, last_editor_user_id, source_version, is_active, created_at, updated_at, version)
            VALUES (#{statementId}, #{revision}, #{ledgerId}, #{assignmentId}, #{amountFen}, #{snapshot},
                   #{lineHash}, #{referenceAmountFen}, #{pricingSource}, 0, NULL, NULL, #{sourceVersion}, 1,
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE is_active = 1, removed_at = NULL, statement_revision = VALUES(statement_revision),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLine(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_driver_statement_line WHERE statement_id = #{statementId} ORDER BY id
            """)
    List<Map<String, Object>> listLines(@Param("statementId") Long statementId);

    @Select("""
            SELECT * FROM vi_driver_statement_line WHERE statement_id = #{statementId} AND is_active = 1 ORDER BY id
            """)
    List<Map<String, Object>> listActiveLines(@Param("statementId") Long statementId);

    @Select("SELECT * FROM vi_driver_statement_line WHERE id = #{lineId}")
    Map<String, Object> findLine(@Param("lineId") Long lineId);

    @Update("""
            UPDATE vi_driver_statement_line SET amount_fen = #{amountFen}, pricing_source = #{pricingSource},
                   manual_changed = 1, price_reason = #{reason}, last_editor_user_id = #{userId},
                   statement_revision = #{revision}, line_hash = #{lineHash},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{lineId} AND is_active = 1
            """)
    int priceLine(Map<String, Object> row);

    @Update("""
            UPDATE vi_driver_statement_line SET is_active = 0, removed_at = UTC_TIMESTAMP(3), removed_by = #{userId},
                   remove_reason = #{reason}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{lineId} AND is_active = 1
            """)
    int removeLine(@Param("lineId") Long lineId, @Param("userId") Long userId, @Param("reason") String reason);

    // ------------------------------------------------------------------ 分配

    @Insert("""
            INSERT INTO vi_driver_ledger_allocation (ledger_id, statement_id, created_at, updated_at, version)
            VALUES (#{ledgerId}, #{statementId}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int allocate(@Param("ledgerId") Long ledgerId, @Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_ledger_allocation SET released_at = UTC_TIMESTAMP(3), release_reason = #{reason},
                   released_by = #{userId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE ledger_id = #{ledgerId} AND released_at IS NULL
            """)
    int releaseAllocation(@Param("ledgerId") Long ledgerId, @Param("reason") String reason,
            @Param("userId") Long userId);

    @Update("""
            UPDATE vi_driver_ledger_allocation SET released_at = UTC_TIMESTAMP(3), release_reason = #{reason},
                   released_by = #{userId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE statement_id = #{statementId} AND released_at IS NULL
            """)
    int releaseAllocationsOfStatement(@Param("statementId") Long statementId, @Param("reason") String reason,
            @Param("userId") Long userId);

    @Select("SELECT * FROM vi_driver_ledger_allocation WHERE ledger_id = #{ledgerId} AND released_at IS NULL")
    Map<String, Object> findActiveAllocation(@Param("ledgerId") Long ledgerId);

    // ------------------------------------------------------------------ 快照 / 改价 / 复核 / 付款 / 差异

    @Insert("""
            INSERT INTO vi_driver_statement_snapshot (statement_id, revision, snapshot_hash, snapshot_json,
                   amount_fen, published_by, public_snapshot_json, public_snapshot_hash, published_at, created_at, version)
            VALUES (#{statementId}, #{revision}, #{snapshotHash}, #{snapshotJson}, #{amountFen}, #{publishedBy},
                   #{publicJson}, #{publicHash}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE id = id
            """)
    int insertSnapshot(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_driver_statement_snapshot WHERE statement_id = #{statementId} AND revision = #{revision}
            """)
    Map<String, Object> findSnapshot(@Param("statementId") Long statementId, @Param("revision") int revision);

    @Insert("""
            INSERT INTO vi_driver_price_change (statement_id, statement_line_id, ledger_id, revision,
                   old_amount_fen, new_amount_fen, reason, actor_user_id, idempotency_key, created_at, version)
            VALUES (#{statementId}, #{lineId}, #{ledgerId}, #{revision}, #{oldAmountFen}, #{newAmountFen},
                   #{reason}, #{actorUserId}, #{idempotencyKey}, UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE version = version + 1
            """)
    int insertPriceChange(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_driver_price_change WHERE statement_line_id = #{lineId} ORDER BY id DESC
            """)
    List<Map<String, Object>> listPriceHistory(@Param("lineId") Long lineId);

    @Insert("""
            INSERT INTO vi_driver_statement_review (statement_id, revision, snapshot_hash, snapshot_json,
                   contributor_ids, submitted_by, status, created_at, version)
            VALUES (#{statementId}, #{revision}, #{snapshotHash}, #{snapshotJson}, #{contributors},
                   #{submittedBy}, 'PENDING', UTC_TIMESTAMP(3), 0)
            """)
    int insertReviewRecord(Map<String, Object> row);

    @Select("SELECT * FROM vi_driver_statement_review WHERE statement_id = #{statementId} ORDER BY id DESC")
    List<Map<String, Object>> listReviewRecords(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_statement_review SET status = #{status}, reviewed_by = #{reviewedBy},
                   opinion = #{opinion}, decided_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE statement_id = #{statementId} AND status = 'PENDING'
            """)
    int decideReviewRecord(@Param("statementId") Long statementId, @Param("status") String status,
            @Param("reviewedBy") Long reviewedBy, @Param("opinion") String opinion);

    @Insert("""
            INSERT INTO vi_driver_external_payment (statement_id, driver_id, payor_entity_id, amount_fen,
                   external_reference_cipher, reference_lookup, paid_at, evidence_ids, status, registered_by,
                   statement_revision, created_at, updated_at, version)
            VALUES (#{statementId}, #{driverId}, #{payorEntityId}, #{amountFen}, #{referenceCipher},
                   #{referenceLookup}, #{paidAt}, #{evidenceIds}, 'PENDING', #{registeredBy},
                   #{statementRevision}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExternalPayment(Map<String, Object> row);

    @Select("SELECT * FROM vi_driver_external_payment WHERE id = #{recordId}")
    Map<String, Object> findExternalPayment(@Param("recordId") Long recordId);

    @Select("""
            SELECT * FROM vi_driver_external_payment WHERE statement_id = #{statementId} ORDER BY id DESC
            """)
    List<Map<String, Object>> listPaymentsOfStatement(@Param("statementId") Long statementId);

    @Select("""
            SELECT COALESCE(SUM(amount_fen), 0) FROM vi_driver_external_payment
            WHERE statement_id = #{statementId} AND status = 'VERIFIED'
            """)
    long sumVerifiedPayment(@Param("statementId") Long statementId);

    @Select("""
            SELECT COALESCE(SUM(amount_fen), 0) FROM vi_driver_external_payment
            WHERE statement_id = #{statementId} AND status = 'PENDING'
            """)
    long sumPendingPayment(@Param("statementId") Long statementId);

    @Update("""
            UPDATE vi_driver_external_payment SET status = #{status}, verified_by = #{verifiedBy},
                   verified_at = UTC_TIMESTAMP(3), reason = #{reason}, updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{recordId} AND status = 'PENDING'
            """)
    int verifyPayment(@Param("recordId") Long recordId, @Param("status") String status,
            @Param("verifiedBy") Long verifiedBy, @Param("reason") String reason);

    @Select("""
            <script>
            SELECT p.id, p.statement_id, p.driver_id, p.payor_entity_id, p.amount_fen, p.paid_at,
                   p.evidence_ids, p.status, p.registered_by, p.verified_by, p.verified_at, p.reason,
                   p.reversal_of, p.statement_revision, p.created_at, p.updated_at, p.version,
                   s.statement_no, a.nickname AS driver_name
            FROM vi_driver_external_payment p
            JOIN vi_driver_statement s ON s.id = p.statement_id
            JOIN vi_driver_profile d ON d.id = p.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND p.status = #{status} </if>
            <if test="statementId != null"> AND p.statement_id = #{statementId} </if>
            ORDER BY p.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listPayments(@Param("status") String status, @Param("statementId") Long statementId,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_driver_external_payment p
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND p.status = #{status} </if>
            <if test="statementId != null"> AND p.statement_id = #{statementId} </if>
            </script>
            """)
    long countPayments(@Param("status") String status, @Param("statementId") Long statementId);

    /** 司机本人可见字段：显式列名，避免 external_reference_cipher 密文随响应体外泄 */
    @Select("""
            SELECT p.id, p.statement_id, p.amount_fen, p.paid_at, p.evidence_ids, p.status,
                   p.verified_at, p.reason, p.reversal_of, p.created_at, s.statement_no
            FROM vi_driver_external_payment p
            JOIN vi_driver_statement s ON s.id = p.statement_id
            WHERE p.driver_id = #{driverId} ORDER BY p.id DESC LIMIT 100
            """)
    List<Map<String, Object>> listPaymentsOfDriver(@Param("driverId") Long driverId);

    @Insert("""
            INSERT INTO vi_driver_statement_difference (statement_id, statement_revision, driver_account_id,
                   entry_ids, description, evidence_ids, status, created_at, updated_at, version)
            VALUES (#{statementId}, #{revision}, #{accountId}, #{entryIds}, #{description}, #{evidenceIds},
                   'OPEN', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDifference(Map<String, Object> row);

    @Select("""
            SELECT * FROM vi_driver_statement_difference WHERE statement_id = #{statementId} ORDER BY id DESC
            """)
    List<Map<String, Object>> listDifferences(@Param("statementId") Long statementId);

    @Select("""
            <script>
            SELECT d.*, s.statement_no, a.nickname AS driver_name
            FROM vi_driver_statement_difference d
            JOIN vi_driver_statement s ON s.id = d.statement_id
            JOIN vi_account a ON a.id = d.driver_account_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND d.status = #{status} </if>
            ORDER BY d.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listDifferencesAdmin(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT * FROM vi_driver_statement_difference WHERE id = #{differenceId}")
    Map<String, Object> findDifference(@Param("differenceId") Long differenceId);

    @Update("""
            UPDATE vi_driver_statement_difference SET status = 'RESOLVED', resolution = #{resolution},
                   resolved_by = #{userId}, resolved_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE id = #{differenceId} AND status = 'OPEN'
            """)
    int resolveDifference(@Param("differenceId") Long differenceId, @Param("resolution") String resolution,
            @Param("userId") Long userId);

    @Insert("""
            INSERT INTO vi_driver_fee_adjustment (source_entry_id, driver_id, payor_entity_id, amount_fen, reason,
                   evidence_ids, status, created_by, created_at, updated_at, version)
            VALUES (#{sourceEntryId}, #{driverId}, #{payorEntityId}, #{amountFen}, #{reason}, #{evidenceIds},
                   'PENDING', #{createdBy}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdjustment(Map<String, Object> row);

    @Select("""
            <script>
            SELECT ad.*, a.nickname AS driver_name,
                   l.amount_fen AS source_amount_fen, l.entry_kind AS source_entry_kind,
                   o.order_no AS source_order_no, l.service_ended_at AS source_service_ended_at
            FROM vi_driver_fee_adjustment ad
            JOIN vi_driver_profile d ON d.id = ad.driver_id
            JOIN vi_account a ON a.id = d.account_id
            LEFT JOIN vi_driver_fee_ledger l ON l.id = ad.source_entry_id
            LEFT JOIN vi_order o ON o.id = l.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND ad.status = #{status} </if>
            ORDER BY ad.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listAdjustments(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_driver_fee_adjustment ad
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND ad.status = #{status} </if>
            </script>
            """)
    long countAdjustments(@Param("status") String status);

    @Select("SELECT * FROM vi_driver_fee_adjustment WHERE id = #{adjustmentId}")
    Map<String, Object> findAdjustment(@Param("adjustmentId") Long adjustmentId);

    @Update("""
            UPDATE vi_driver_fee_adjustment SET status = #{status}, reviewer_id = #{reviewer},
                   opinion = #{opinion}, generated_ledger_id = #{ledgerId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{adjustmentId} AND status = 'PENDING'
            """)
    int auditAdjustment(@Param("adjustmentId") Long adjustmentId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("opinion") String opinion,
            @Param("ledgerId") Long ledgerId);
}
