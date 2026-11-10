package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.qualification.mapper.QualificationAdminMapper;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.*;

/** ADMIN与MINI的同人关系只从可信绑定、已核验手机号推导，永不比较两域ID数值。 */
@Service
public class QualificationApplicantAuthority {
    private final QualificationAdminMapper mapper;
    private final PhoneCrypto phones;

    public QualificationApplicantAuthority(QualificationAdminMapper mapper, PhoneCrypto phones) {
        this.mapper = mapper;
        this.phones = phones;
    }

    public Set<Long> users(long accountId) {
        Set<Long> users = new TreeSet<>(mapper.boundUsers(accountId));
        var row = mapper.verifiedPhone(accountId);
        byte[] lookup = row == null ? null : (byte[]) row.get("phone_lookup");
        if (lookup != null) {
            for (var user : mapper.usersWithPhone()) {
                String number = Objects.toString(user.get("phonenumber"), "");
                if (MessageDigest.isEqual(lookup, phones.lookup(number)))
                    users.add(((Number) user.get("user_id")).longValue());
            }
        }
        return users;
    }
}
