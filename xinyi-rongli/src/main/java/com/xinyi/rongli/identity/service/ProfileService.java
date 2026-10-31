package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.dto.ProfileResponse;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 当前统一档案（API_005_get）。从可信 MiniPrincipal 的 accountId 读取本人账号，绝不接收主体伪造。
 * availableRoles/driverQualification/employeeLink/permissions 由 IdentityContextService 按权威状态实时计算。
 * phoneMask 由已核验手机号密文解密脱敏（与会话 phoneBound 同源，TC-R63-001）；
 * realName（实名核验）随实名任务、defaultAddressId 随车主地址任务落地，未产生事实时为 null，不臆造。
 */
@Service
public class ProfileService {

    private final ViAccountMapper accountMapper;
    private final IdentityContextService contextService;
    private final PhoneCrypto phoneCrypto;

    public ProfileService(ViAccountMapper accountMapper, IdentityContextService contextService,
                          PhoneCrypto phoneCrypto) {
        this.accountMapper = accountMapper;
        this.contextService = contextService;
        this.phoneCrypto = phoneCrypto;
    }

    public ProfileResponse getProfile(long accountId) {
        ViAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        IdentityContext ctx = contextService.compute(accountId);
        String phoneMask = account.getPhoneVerifiedAt() != null && account.getPhoneCipher() != null
                ? PhoneCrypto.mask(phoneCrypto.decrypt(account.getPhoneCipher())) : null;
        return new ProfileResponse(
                ViId.external(account.getId()),
                account.getNickname(),
                phoneMask,
                ctx.driverQualification(),
                List.copyOf(ctx.availableRoles()),
                ctx.permissions(),
                account.getVersion(),
                null,
                account.getAvatarFileId() == null ? null : ViId.external(account.getAvatarFileId()),
                null,
                ctx.employeeLink());
    }
}
