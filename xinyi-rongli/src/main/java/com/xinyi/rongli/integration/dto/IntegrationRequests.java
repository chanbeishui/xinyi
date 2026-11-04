package com.xinyi.rongli.integration.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/** 请求对象不携带内部权限范围；部门归属和实际可写范围仍由服务端加载校验。 */
public final class IntegrationRequests {
    private IntegrationRequests() {}

    public record Scope(String ownerDeptId, String environment) {}

    public record Create(String kind, String name, Scope scope) {}

    public record Draft(
            String kind,
            long expectedVersion,
            IntegrationSettings.Config config,
            Map<String, IntegrationSettings.SecretMutation> credentials,
            String reason,
            JsonNode publicConfig) {}

    public record Query(
            String kind, Long ownerDeptId, String environment, int pageNum, int pageSize) {
        public int offset() {
            return Math.multiplyExact(pageNum - 1, pageSize);
        }
    }
}
