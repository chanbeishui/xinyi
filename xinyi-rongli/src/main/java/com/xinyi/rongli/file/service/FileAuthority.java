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

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

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
        if (file.getTrainingId() != null || "TRAINING_MEDIA".equals(purpose)) {
            checkTraining(actor, file, permission, write);
            return;
        }
        if ("AVATAR".equals(purpose)) {
            if (!uploader(actor, file)) denied();
            return;
        }
        if (file.getApplicationId() != null) {
            // Q01先锁资格父记录再锁文件。提交冻结后，旧上传票据和IO后的回写也必须重新拒绝。
            // 本轮仍仅本人证件读写，不借公司字段提前开放Q05后台原图。
            var application = files.lockApplication(file.getApplicationId());
            if (!actor.isMini()
                    || application == null
                    || !Objects.equals(
                            actor.accountId(), ((Number) application.get("account_id")).longValue())
                    || !Set.of("ID_CARD", "DRIVER_LICENSE").contains(purpose)) denied();
            if (write) requireEditableApplication(application, purpose);
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

    private void checkTraining(Actor actor, PrivateFile file, String permission, boolean write) {
        if (!actor.isAdmin()
                || file.getTrainingId() == null
                || !"TRAINING_MEDIA".equals(file.getPurpose())) denied();
        var parent = files.lockTraining(file.getTrainingId());
        if (parent == null || parent.get("owner_dept_id") == null) denied();
        long owner = ((Number) parent.get("owner_dept_id")).longValue();
        requireAdminDepartment(actor, owner, permission);
        requireAdminDepartment(
                actor, owner, write ? "biz:trainingcourse:edit" : "biz:trainingcourse:query");
        if (write
                && (!"0".equals(parent.get("dept_status"))
                        || !"0".equals(parent.get("dept_delete"))))
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "归属公司当前不可用，不能上传新培训媒体");
        if (write && !"DRAFT".equals(parent.get("status"))) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "已发布课程媒体只读，请派生新草稿维护");
        }
    }

    /** 内部业务已验证其父记录后仍独立核对文件功能权和真实公司，不能以有课程权代替私有文件权。 */
    public void requireAdminDepartment(Actor actor, long owner, String permission) {
        if (!actor.isAdmin() || !SecurityUtils.hasPermi(permission))
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        var scope = scopes.resolve(SecurityUtils.getLoginUser(), permission);
        if (!scope.isAllData() && !scope.getDeptIds().contains(owner)) denied();
    }

    private static void requireEditableApplication(
            java.util.Map<String, Object> application, String purpose) {
        String status = application.get("status").toString();
        if (!Set.of("DRAFT", "REJECTED").contains(status)) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "资格申请已冻结，不可继续上传或替换证件");
        }
        if (!"REJECTED".equals(status)) return;
        Object raw = application.get("reject_fields");
        if (raw == null) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "历史驳回缺少补正字段，请联系管理员");
        }
        try {
            var fields =
                    com.xinyi.rongli.platform.json.ViJson.read(
                            raw.toString(), java.util.List.class);
            Set<String> slots =
                    "ID_CARD".equals(purpose)
                            ? Set.of("idCardFrontFileId", "idCardBackFileId")
                            : Set.of("licenseMainFileId", "licenseSubFileId");
            if (fields.stream().noneMatch(slots::contains)) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "本类证件不在驳回补正范围");
            }
        } catch (ViException error) {
            throw error;
        } catch (RuntimeException error) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "驳回补正记录不可用，请联系管理员");
        }
    }

    private static void denied() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
