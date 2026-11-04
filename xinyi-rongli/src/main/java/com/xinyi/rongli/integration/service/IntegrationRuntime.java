package com.xinyi.rongli.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.stereotype.Service;
import java.util.*;

/** 供应商适配器专用入口，主库发布指针权威；秘密仅在调用期间存活，不可作为响应返回。 */
@Service
public class IntegrationRuntime {
    private final IntegrationMapper mapper;
    private final IntegrationSecrets secrets;
    private final IntegrationValidation validation;
    private final IntegrationAuthority authority;

    public IntegrationRuntime(
            IntegrationMapper mapper,
            IntegrationSecrets secrets,
            IntegrationValidation validation,
            IntegrationAuthority authority) {
        this.mapper = mapper;
        this.secrets = secrets;
        this.validation = validation;
        this.authority = authority;
    }

    @FunctionalInterface
    public interface Operation<T> {
        T run(long revisionId, JsonNode config, Map<String, byte[]> credentials);
    }

    /** 消费者必须提供业务记录的权威公司；只取已发布指针，在回调结束后清零秘密缓冲区。 */
    public <T> T withPublished(
            long profileId, long authoritativeOwnerDeptId, String kind, Operation<T> operation) {
        var p = mapper.profile(profileId);
        if (p == null
                || p.getOwnerDeptId() != authoritativeOwnerDeptId
                || !kind.equals(p.getKind())
                || !authority.environment.equals(p.getEnvironment())
                || !"ACTIVE".equals(p.getLifecycle())
                || p.getActiveRevisionId() == null
                || mapper.wasPublished(profileId, p.getActiveRevisionId()) == 0)
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID);
        var r = mapper.revision(profileId, p.getActiveRevisionId());
        if (!validation.validate(p, r).passed())
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID);
        var config = ViJson.tree(r.getConfigJson());
        if (config.has("enabled") && !config.path("enabled").asBoolean())
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID);
        var values = new HashMap<String, byte[]>();
        try {
            for (String slot : secrets.refs(r).keySet()) values.put(slot, secrets.read(p, r, slot));
            return operation.run(r.getId(), config, Collections.unmodifiableMap(values));
        } finally {
            values.values().forEach(v -> Arrays.fill(v, (byte) 0));
        }
    }
}
