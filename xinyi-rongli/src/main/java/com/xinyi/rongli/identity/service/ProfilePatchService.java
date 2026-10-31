package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.dto.ProfileResponse;
import com.xinyi.rongli.identity.mapper.OwnerProfileEditMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

/**
 * 修改选填档案（API_012_patch，OWNER 本人）。CAS expectedVersion；仅本人账号，绝不接收主体伪造。
 * nickname/avatarFileId→vi_account；realName→vi_owner_profile.service_name（服务称呼，非实名结论）；
 * defaultAddressId→vi_address 默认切换（先校验本人归属）。未提供的字段保持原值（按 containsKey 合并）。
 */
@Service
public class ProfilePatchService {

    private final ViAccountMapper accountMapper;
    private final OwnerProfileEditMapper ownerEditMapper;
    private final ProfileService profileService;
    private final AuditService auditService;
    private final TransactionTemplate tx;

    public ProfilePatchService(ViAccountMapper accountMapper, OwnerProfileEditMapper ownerEditMapper,
                               ProfileService profileService, AuditService auditService,
                               PlatformTransactionManager txManager) {
        this.accountMapper = accountMapper;
        this.ownerEditMapper = ownerEditMapper;
        this.profileService = profileService;
        this.auditService = auditService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public ProfileResponse patch(long accountId, Map<String, Object> body) {
        long expectedVersion = ((Number) body.get("expectedVersion")).longValue();
        return tx.execute(s -> {
            ViAccount account = accountMapper.selectByIdForUpdate(accountId);
            if (account == null) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            }
            if (account.getVersion() != expectedVersion) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
            }
            String nickname = body.containsKey("nickname")
                    ? asString(body.get("nickname")) : account.getNickname();
            Long avatarFileId = body.containsKey("avatarFileId")
                    ? asId(body.get("avatarFileId")) : account.getAvatarFileId();

            if (body.containsKey("realName")) {
                String serviceName = asString(body.get("realName"));
                ownerEditMapper.upsertServiceName(accountId, serviceName);
            }
            if (body.containsKey("defaultAddressId")) {
                Long addressId = asId(body.get("defaultAddressId"));
                if (addressId != null) {
                    if (ownerEditMapper.countOwnedAddress(accountId, addressId) == 0) {
                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE, "地址不存在或不属于本人");
                    }
                    ownerEditMapper.clearDefaultAddresses(accountId);
                    ownerEditMapper.setDefaultAddress(accountId, addressId);
                }
            }
            int n = accountMapper.updateProfile(accountId, nickname, avatarFileId, expectedVersion);
            if (n != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
            }
            auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, null, null, null),
                    "VI_ACCOUNT", accountId, "PROFILE_PATCH", null, Map.of("fields", body.keySet()), null);
            return profileService.getProfile(accountId);
        });
    }

    private static String asString(Object v) {
        if (v == null) {
            return null;
        }
        if (!(v instanceof String s)) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "字段须为字符串");
        }
        return s;
    }

    private static Long asId(Object v) {
        if (v == null) {
            return null;
        }
        if (!(v instanceof String s) || s.isBlank()) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "ID 须为十进制字符串");
        }
        return ViId.parse(s);
    }
}
