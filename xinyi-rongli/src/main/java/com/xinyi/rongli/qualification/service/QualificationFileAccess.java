package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.qualification.mapper.QualificationFileMapper;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 资格原图是独立敏感能力，仅允许已认证提交的确切四图；不能扩为同公司全部文件。 */
@Service
public class QualificationFileAccess {
    private static final Set<String> SLOTS =
            Set.of(
                    "idCardFrontFileId",
                    "idCardBackFileId",
                    "licenseMainFileId",
                    "licenseSubFileId");
    private final QualificationFileMapper mapper;
    private final QualificationSubmissionAuthority submissions;
    private final IntegrationAuthority authority;

    public QualificationFileAccess(
            QualificationFileMapper mapper,
            QualificationSubmissionAuthority submissions,
            IntegrationAuthority authority) {
        this.mapper = mapper;
        this.submissions = submissions;
        this.authority = authority;
    }

    public String context(Actor actor, PrivateFile file, String filePermission) {
        if (!actor.isAdmin() || file == null || file.getApplicationId() == null) denied();
        authority.lockAuthority();
        if (!Objects.equals(authority.actor().sysUserId(), actor.sysUserId())) denied();
        var parent = mapper.parent(file.getApplicationId());
        if (parent == null) denied();
        long revision = ((Number) parent.get("submission_revision")).longValue();
        var submission = submissions.authenticated(file.getApplicationId(), revision);
        if (submission == null
                || submission.getOwnerDeptId() == null
                || !Objects.equals(
                        file.getUploaderAccountId(),
                        ((Number) parent.get("account_id")).longValue())) denied();
        long owner = submission.getOwnerDeptId();
        // query、专门敏感能力以及文件query/access分别按原公司范围，客户端字段和短票不扩大授权。
        require(owner, "biz:driverqualification:query");
        require(owner, "biz:driverqualification:sensitive");
        require(owner, "biz:files:query");
        require(owner, filePermission);
        Map<?, ?> selected = ViJson.read(submission.getEvidenceFileIds(), Map.class);
        if (!selected.keySet().equals(SLOTS) || selected.values().stream().distinct().count() != 4)
            denied();
        boolean matched = false;
        for (String slot : SLOTS) {
            String expectedPurpose = slot.startsWith("idCard") ? "ID_CARD" : "DRIVER_LICENSE";
            if (file.getId().toString().equals(Objects.toString(selected.get(slot), ""))
                    && expectedPurpose.equals(file.getPurpose())) matched = true;
        }
        if (!matched) denied();
        try {
            // 票据绑定提交修订及认证封装；同文件在新提交再次出现也不能复活旧票。
            String seal =
                    HexFormat.of()
                            .formatHex(
                                    MessageDigest.getInstance("SHA-256")
                                            .digest(submission.getSubmissionSeal()));
            return "Q05:SUBMISSION:"
                    + submission.getId()
                    + ":REVISION:"
                    + revision
                    + ":SEAL:"
                    + seal;
        } catch (NoSuchAlgorithmException impossible) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR);
        }
    }

    private void require(long owner, String permission) {
        Set<Long> allowed = authority.scope(permission);
        if (allowed != null && !allowed.contains(owner)) denied();
    }

    private static void denied() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
