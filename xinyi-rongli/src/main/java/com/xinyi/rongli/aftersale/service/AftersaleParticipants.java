package com.xinyi.rongli.aftersale.service;

import com.xinyi.rongli.aftersale.mapper.AdminAftersaleMapper;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.platform.error.*;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

/** 贡献者与审核人隔离。MINI/ADMIN编号从不直接比较，历史绑定和可信手机号只用于服务端同人核对。 */
@Service
public class AftersaleParticipants {
    private final AdminAftersaleMapper mapper;
    private final PhoneCrypto crypto;

    public AftersaleParticipants(AdminAftersaleMapper mapper, PhoneCrypto crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
    }

    public void admin(long caseId, long userId) {
        mapper.addParticipant(caseId, userId);
    }

    /** 在申请/补充原事务中冻结当时已知关联；以后解绑、换手机号都不能抹掉已经参与过的事实。 */
    public void account(long caseId, long accountId) {
        for (long user : mapper.boundUsers(accountId)) mapper.addParticipant(caseId, user);
        byte[] phone = accountLookup(accountId);
        if (phone == null) return;
        for (var user : mapper.usersWithPhone()) {
            if (MessageDigest.isEqual(phone, crypto.lookup(user.get("phonenumber").toString())))
                mapper.addParticipant(caseId, ((Number) user.get("user_id")).longValue());
        }
    }

    public boolean contributed(long caseId, long userId) {
        if (mapper.contributor(caseId, userId) > 0) return true;
        String reviewerPhone = mapper.reviewerPhone(userId);
        byte[] reviewerLookup =
                reviewerPhone == null || reviewerPhone.isBlank()
                        ? null
                        : crypto.lookup(reviewerPhone);
        // 不按ACTIVE过滤：解绑只撤销工作权限，不能让自己曾提交的申请变成可自审。
        for (long account : mapper.contributorAccounts(caseId)) {
            if (mapper.boundUsers(account).contains(userId)) return true;
            byte[] lookup = accountLookup(account);
            if (lookup != null
                    && reviewerLookup != null
                    && MessageDigest.isEqual(lookup, reviewerLookup)) return true;
        }
        return false;
    }

    private byte[] accountLookup(long accountId) {
        var row = mapper.accountPhone(accountId);
        return row == null ? null : (byte[]) row.get("phone_lookup");
    }

    public void requireIndependent(long caseId, long userId) {
        if (contributed(caseId, userId)) throw new ViException(ViErrorCode.VI_SELF_REVIEW_DENIED);
    }
}
