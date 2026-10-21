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
 * 限定用途上传与文件核验状态。
 */
@Mapper
public interface MediaMapper
{
    @Insert("""
            INSERT INTO vi_file (uploader_account_id, purpose, order_id, application_id, object_key, status,
                   mime_type, retain_until, created_at, updated_at, version)
            VALUES (#{uploaderAccountId}, #{purpose}, #{orderId}, #{applicationId}, #{objectKey}, 'UPLOADING',
                   #{mimeType}, #{retainUntil}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFile(Map<String, Object> row);

    @Select("SELECT * FROM vi_file WHERE id = #{fileId}")
    Map<String, Object> findFile(@Param("fileId") Long fileId);

    @Select("SELECT * FROM vi_file WHERE object_key = #{objectKey}")
    Map<String, Object> findByKey(@Param("objectKey") String objectKey);

    /** 服务器核验通过：记录真实大小与摘要，状态推进到 READY */
    @Update("""
            UPDATE vi_file SET status = 'READY', server_size = #{size}, server_sha256 = #{sha},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{fileId} AND status IN ('UPLOADING','VERIFYING')
            """)
    int markReady(@Param("fileId") Long fileId, @Param("size") long size, @Param("sha") byte[] sha);

    @Update("""
            UPDATE vi_file SET status = 'REJECTED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{fileId} AND status IN ('UPLOADING','VERIFYING')
            """)
    int markRejected(@Param("fileId") Long fileId);

    @Select("""
            SELECT * FROM vi_file WHERE uploader_account_id = #{accountId} ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listOfUploader(@Param("accountId") Long accountId, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT * FROM vi_file WHERE order_id = #{orderId} ORDER BY id DESC")
    List<Map<String, Object>> listOfOrder(@Param("orderId") Long orderId);
}
