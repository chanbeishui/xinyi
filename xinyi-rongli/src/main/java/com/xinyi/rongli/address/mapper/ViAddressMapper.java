package com.xinyi.rongli.address.mapper;

import com.xinyi.rongli.address.domain.ViAddress;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/** vi_address 车主地址簿（A02）。软删除；is_default 同事务串行单默认；version CAS；归属 owner_account_id。 */
public interface ViAddressMapper {

    int insert(ViAddress address);

    ViAddress selectById(@Param("id") long id);

    ViAddress selectByIdForUpdate(@Param("id") long id);

    List<ViAddress> selectByOwner(@Param("ownerAccountId") long ownerAccountId);

    int update(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
               @Param("label") String label, @Param("addressCipher") byte[] addressCipher,
               @Param("locationCipher") byte[] locationCipher, @Param("isDefault") int isDefault,
               @Param("expectedVersion") long expectedVersion);

    int softDelete(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
                   @Param("deletedAt") Instant deletedAt, @Param("expectedVersion") long expectedVersion);

    int clearDefault(@Param("ownerAccountId") long ownerAccountId, @Param("excludeId") long excludeId);

    int setDefaultFlag(@Param("id") long id, @Param("ownerAccountId") long ownerAccountId,
                       @Param("flag") int flag, @Param("expectedVersion") long expectedVersion);
}