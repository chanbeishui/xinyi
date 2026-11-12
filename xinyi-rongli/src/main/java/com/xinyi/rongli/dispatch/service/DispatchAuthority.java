package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.dto.DispatchRequests;
import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.identity.service.IdentityContextService;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;

import org.springframework.stereotype.Service;

import java.util.Objects;

/** 当前DRIVER仅是工作视角，历史/在手可进入；新单资格另由实时规则判断，不能反向阻断关闭意愿。 */
@Service
public class DispatchAuthority {
    private final BusinessActorResolver actors;
    private final FileAuthority files;
    private final DispatchListeningMapper mapper;
    private final IdentityContextService identities;

    public DispatchAuthority(
            BusinessActorResolver actors,
            FileAuthority files,
            DispatchListeningMapper mapper,
            IdentityContextService identities) {
        this.actors = actors;
        this.files = files;
        this.mapper = mapper;
        this.identities = identities;
    }

    public Actor actor() {
        return actors.requireMini();
    }

    public DriverProfile lock(Actor actor) {
        // FileAuthority沿目录→账号→会话/security实时核验；后台切换不能重用旧context访问位置或听单。
        files.guard(actor, "biz:driver:query");
        if (!"DRIVER".equals(actor.actingRole())
                || !identities.compute(actor.accountId()).availableRoles().contains("DRIVER"))
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        DriverProfile row = mapper.lockProfile(actor.accountId());
        if (row == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return row;
    }

    public void source(Actor actor, DriverProfile row, DispatchRequests.Source source) {
        if (!Objects.equals(actor.sessionId(), source.expectedSessionId())
                || !Objects.equals(actor.contextVersion(), source.expectedContextVersion()))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
        if (row.getTrainingContextRevision() != source.expectedCompanyRevision())
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED, "公司归属已变化，请重新读取本人工作状态");
    }
}
