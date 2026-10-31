package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.auth.MiniCapabilityRegistry;
import com.xinyi.rongli.identity.dto.EmployeeLinkPublic;
import com.xinyi.rongli.identity.mapper.IdentityReadMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计算 MINI 账号的身份上下文（availableRoles / driverQualification / employeeLink / 能力 / 权限 / 站点）。
 * 全部读取权威状态：DRIVER 须已批准且证件准驾当前有效；STATION 须有效员工绑定 + 至少一个启用站点授权。
 * 新账号无任何资格/关联 → 仅 OWNER、employeeLink=UNLINKED（V6.4 身份架构 §1/§3）。
 * 只读，不写代驾/员工/站点表（其写入分属对应任务）；不复制 sys_user 超管权限。
 */
@Service
public class IdentityContextService {

    private final IdentityReadMapper readMapper;

    public IdentityContextService(IdentityReadMapper readMapper) {
        this.readMapper = readMapper;
    }

    public IdentityContext compute(long accountId) {
        Set<String> roles = new LinkedHashSet<>();
        roles.add("OWNER");

        String driverQualification = readMapper.selectDriverQualificationStatus(accountId);
        if (readMapper.countDriverApprovedCurrent(accountId) > 0) {
            roles.add("DRIVER");
        }

        Map<String, Object> binding = readMapper.selectActiveBinding(accountId);
        EmployeeLinkPublic link;
        List<String> stationIds = List.of();
        boolean workAccessDisabled = false;
        Long linkedSysUserId = null;

        if (binding == null) {
            link = EmployeeLinkPublic.unlinked();
        } else {
            long sysUserId = ((Number) binding.get("sys_user_id")).longValue();
            linkedSysUserId = sysUserId;
            String status = String.valueOf(binding.get("status"));
            int grantCount = readMapper.countEnabledStationGrants(sysUserId);
            List<Long> ids = readMapper.selectGrantedStationIds(sysUserId);
            stationIds = new ArrayList<>(ids.size());
            for (Long id : ids) {
                stationIds.add(ViId.external(id));
            }
            if ("ACTIVE".equals(status)) {
                // 工作能力实时 fail-closed：须员工在职有效 + 至少一个启用站点授权（停用/删除立即失权，不靠旧缓存）
                boolean employeeActive = readMapper.countActiveUserById(sysUserId) > 0;
                boolean work = grantCount > 0 && employeeActive;
                if (work) {
                    roles.add("STATION");
                }
                workAccessDisabled = !work;
                link = new EmployeeLinkPublic("LINKED", work, grantCount,
                        work ? "EMPLOYEE_LINKED" : (employeeActive ? "WAIT_ADMIN_CONFIGURATION" : "CONTACT_ADMIN"), false);
            } else {
                // SUSPENDED：暂停工作能力，保留唯一关系与历史；不自动恢复，需本人重新核验/后台处理
                workAccessDisabled = true;
                link = new EmployeeLinkPublic("SUSPENDED", false, grantCount, "CONTACT_ADMIN", true);
            }
        }

        return new IdentityContext(
                roles,
                driverQualification,
                link,
                MiniCapabilityRegistry.capabilitiesFor(roles),
                MiniCapabilityRegistry.permissionsFor(roles),
                stationIds,
                workAccessDisabled,
                linkedSysUserId);
    }
}
