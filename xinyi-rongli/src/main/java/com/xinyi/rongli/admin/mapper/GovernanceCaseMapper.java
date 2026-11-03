package com.xinyi.rongli.admin.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * admin 域查询。由原 MiscMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface GovernanceCaseMapper
{
    @Insert("""
            INSERT INTO vi_governance_case (case_type, order_id, requester_account_id, requester_user_id, status,
                   payload_cipher, reason, created_at, updated_at, version)
            VALUES (#{caseType}, #{orderId}, #{requesterAccountId}, #{requesterUserId}, 'APPLIED',
                   #{payload}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGovernanceCase(Map<String, Object> row);
    @Select("""
            <script>
            SELECT * FROM vi_governance_case
            <where>
              <if test="caseType != null and caseType != ''"> AND case_type = #{caseType} </if>
              <if test="status != null and status != ''"> AND status = #{status} </if>
            </where>
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listGovernanceCases(@Param("caseType") String caseType,
            @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);
    @Select("""
            SELECT * FROM vi_governance_case
            WHERE requester_account_id = #{accountId}
            ORDER BY id DESC LIMIT 50
            """)
    List<Map<String, Object>> listMyGovernanceCases(@Param("accountId") Long accountId);
    @Update("""
            UPDATE vi_governance_case SET status = #{status}, reviewer_id = #{reviewer}, reason = #{reason},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{caseId}
            """)
    int resolveGovernanceCase(@Param("caseId") Long caseId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("reason") String reason);
}
