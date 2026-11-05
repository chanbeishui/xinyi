package com.xinyi.rongli.file.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.file.mapper.PrivateFileMapper;
import com.xinyi.rongli.identity.mapper.*;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.scope.mapper.EmployeeStationAccessMapper;
import com.xinyi.system.service.IDeptScopeService;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

/** 文件权限随父业务实时判断；临时签名和客户端声明的用途都不能扩大授权范围。 */
@Service
public class FileAuthority {
    private final BusinessActorResolver actors;
    private final EmployeeDirectoryGuardMapper directory;
    private final ViAccountMapper accounts;
    private final ViSessionMapper sessions;
    private final OrderMapper orders;
    private final PaymentMapper payments;
    private final IntegrationMapper integration;
    private final IDeptScopeService scopes;
    private final EmployeeStationAccessMapper stations;
    private final PrivateFileMapper files;

    public FileAuthority(
            BusinessActorResolver actors,
            EmployeeDirectoryGuardMapper directory,
            ViAccountMapper accounts,
            ViSessionMapper sessions,
            OrderMapper orders,
            PaymentMapper payments,
            IntegrationMapper integration,
            IDeptScopeService scopes,
            EmployeeStationAccessMapper stations,
            PrivateFileMapper files) {
        this.actors = actors;
        this.directory = directory;
        this.accounts = accounts;
        this.sessions = sessions;
        this.orders = orders;
        this.payments = payments;
        this.integration = integration;
        this.scopes = scopes;
        this.stations = stations;
        this.files = files;
    }

    public Actor actor() {
        Actor actor = actors.currentActor();
        if (!actor.isMini() && !actor.isAdmin())
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        return actor;
    }

    /** 所有文件业务事务先锁目录守卫，后锁会话/订单/文件，避免撤权与上传完成并发穿透。 */
    public void guard(Actor actor, String permission) {
        directory.ensureGuard();
        directory.lockGuard();
        if (actor.isAdmin()) {
            var row = integration.lockUser(actor.sysUserId());
            if (row == null
                    || !"0".equals(row.get("status"))
                    || !"0".equals(row.get("del_flag"))
                    || !Objects.equals(
                            ((Number) row.get("auth_version")).longValue(),
                            SecurityUtils.getLoginUser().getAuthVersion()))
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            if (!SecurityUtils.hasPermi(permission))
                throw new ViException(ViErrorCode.VI_FORBIDDEN);
            return;
        }
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (account == null || !"ACTIVE".equals(account.getStatus()) || actor.sessionId() == null)
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        var session = sessions.selectByIdForUpdate(actor.sessionId());
        var security = orders.lockSecurity(actor.accountId());
        if (session == null
                || security == null
                || !Objects.equals(session.getAccountId(), actor.accountId())
                || !"ACTIVE".equals(session.getStatus())
                || session.getRevokedAt() != null
                || !session.getAbsoluteExpiresAt().isAfter(Instant.now())
                || !"ACTIVE".equals(security.getSecurityStatus())
                || !Objects.equals(session.getSessionEpoch(), security.getSessionEpoch()))
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        if (!Objects.equals(session.getActiveRole(), actor.actingRole())
                || !Objects.equals(session.getContextVersion(), actor.contextVersion()))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
    }

    public boolean uploader(Actor actor, PrivateFile file) {
        return actor.isMini()
                ? Objects.equals(actor.accountId(), file.getUploaderAccountId())
                : Objects.equals(actor.sysUserId(), file.getUploaderUserId());
    }

    public void check(Actor actor, PrivateFile file, String permission, boolean write) {
        if (file == null || write && !uploader(actor, file)) denied();
        String purpose = file.getPurpose();
        if ("AVATAR".equals(purpose)) {
            if (!uploader(actor, file)) denied();
            return;
        }
        if (file.getApplicationId() != null) {
            // 申请证件没有权威部门归属。当前仅本人可读写，不能拿管理员主部门猜测审核范围。
            if (!actor.isMini()
                    || !Objects.equals(
                            actor.accountId(), files.applicationOwner(file.getApplicationId()))
                    || !Set.of("ID_CARD", "DRIVER_LICENSE").contains(purpose)) denied();
            return;
        }
        Long department;
        Long station;
        if (file.getOrderId() != null) {
            var order = payments.lockOrder(file.getOrderId());
            if (order == null) {
                denied();
                return;
            }
            department = order.getDeptId();
            station = order.getStationId();
            if (actor.isMini() && "OWNER".equals(actor.actingRole())) {
                if (!Objects.equals(order.getOwnerAccountId(), actor.accountId())
                        || !Set.of("VEHICLE_PHOTO", "VEHICLE_LICENSE", "DISPUTE", "RESULT_EVIDENCE")
                                .contains(purpose)
                        || write && "RESULT_EVIDENCE".equals(purpose)
                        || "DISPUTE".equals(purpose)
                                && !uploader(actor, file)
                                && files.aftersaleEvidence(order.getId(), file.getId()) != 1)
                    denied();
                return;
            }
            if (actor.isMini() && "DRIVER".equals(actor.actingRole())) {
                if (files.activeDriver(order.getId(), actor.accountId()) != 1
                        || !Set.of("VEHICLE_PHOTO", "VEHICLE_LICENSE", "DISPUTE").contains(purpose)
                        || "DISPUTE".equals(purpose) && !uploader(actor, file)) denied();
                return;
            }
        } else {
            station = file.getStationId();
            department = station == null ? null : files.stationDept(station);
            if (!"STATION_QUALIFICATION".equals(purpose)) denied();
        }
        if (department == null) denied();
        if (actor.isAdmin()) {
            var scope = scopes.resolve(SecurityUtils.getLoginUser(), permission);
            if (!scope.isAllData() && !scope.getDeptIds().contains(department)) denied();
            return;
        }
        if (!"STATION".equals(actor.actingRole())
                || !Objects.equals(station, actor.stationId())
                || !Set.of(
                                "VEHICLE_PHOTO",
                                "VEHICLE_LICENSE",
                                "RESULT_EVIDENCE",
                                "STATION_QUALIFICATION")
                        .contains(purpose)
                || stations.countAllowed(actor.accountId(), station, permission) != 1
                || file.getOrderId() != null
                        && !stations.orderDepartments(actor.accountId(), station, permission)
                                .contains(department)) denied();
    }

    private static void denied() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
