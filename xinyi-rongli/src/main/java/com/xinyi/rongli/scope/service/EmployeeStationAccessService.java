package com.xinyi.rongli.scope.service;

import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.scope.mapper.EmployeeStationAccessMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/** 工作动作取实时员工、角色、部门和站点授权的交集；Redis 故障不延长工作权限。 */
@Service
public class EmployeeStationAccessService {
    private static final Set<String> ALLOWED = Set.of("biz:station:query", "biz:station:edit",
            "biz:capacity:list", "biz:capacity:edit");
    private final BusinessActorResolver actors;
    private final EmployeeStationAccessMapper mapper;
    private final AuditService audit;

    public EmployeeStationAccessService(BusinessActorResolver actors, EmployeeStationAccessMapper mapper,
                                       AuditService audit) {
        this.actors = actors;
        this.mapper = mapper;
        this.audit = audit;
    }

    public long require(String permission) {
        Actor actor = actors.requireMini();
        if (!ALLOWED.contains(permission)) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        }
        if (!"STATION".equals(actor.actingRole()) || actor.stationId() == null
                || mapper.countAllowed(actor.accountId(), actor.stationId(), permission) == 0) {
            audit.recordFailure(actor, "VI_STATION", actor.stationId() == null ? 0L : actor.stationId(),
                    "STATION_ACCESS_DENIED", null, Map.of("permission", permission), "工作权限已失效或未获授权");
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "当前员工或站点工作权限不可用，请刷新身份或联系管理员");
        }
        return actor.stationId();
    }
}
