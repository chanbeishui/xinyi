package com.xinyi.rongli.verification.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.integration.service.IntegrationSecrets;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.verification.mapper.SoterConfigurationMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/** 设备认证只使用本人实际AppID对应的正式已发布凭据；历史挑战不跟随新AppID漂移。 */
@Service
public class PublishedSoterSettings {
    private final SoterConfigurationMapper lookup;
    private final IntegrationMapper mapper;
    private final IntegrationSecrets secrets;
    private final IntegrationAuthority authority;

    public PublishedSoterSettings(SoterConfigurationMapper lookup, IntegrationMapper mapper,
            IntegrationSecrets secrets, IntegrationAuthority authority) {
        this.lookup = lookup;
        this.mapper = mapper;
        this.secrets = secrets;
        this.authority = authority;
    }

    public VerificationProvider.Configuration current(long owner) {
        var ids = lookup.profiles(owner, authority.environment);
        if (ids.size() != 1) throw unavailable();
        var profile = mapper.profile(ids.get(0));
        if (profile == null || profile.getActiveRevisionId() == null) throw unavailable();
        var revision = mapper.revision(profile.getId(), profile.getActiveRevisionId());
        if (revision == null) throw unavailable();
        var config = checked(profile.getId(), revision.getId(), owner);
        if (!config.path("enabled").asBoolean()) throw unavailable();
        // 发挑战前先验证根密钥与发布秘密可用，不能等用户完成本机操作后才发现无法验签。
        byte[] secret = secrets.read(profile, revision, "appSecret");
        Arrays.fill(secret, (byte) 0);
        return identity(owner, profile.getId(), revision.getId(), config.path("appId").asText());
    }

    public <T> T secret(VerificationProvider.Configuration frozen, Function<byte[], T> operation) {
        var config = checked(frozen.profileId(), frozen.revisionId(), frozen.ownerDeptId());
        if (!identity(frozen.ownerDeptId(), frozen.profileId(), frozen.revisionId(),
                config.path("appId").asText()).equals(frozen)) throw unavailable();
        byte[] secret = secrets.read(mapper.profile(frozen.profileId()),
                mapper.revision(frozen.profileId(), frozen.revisionId()), "appSecret");
        try {
            return operation.apply(secret);
        } finally {
            Arrays.fill(secret, (byte) 0);
        }
    }

    private JsonNode checked(long profileId, long revisionId, long owner) {
        var profile = mapper.profile(profileId);
        var revision = mapper.revision(profileId, revisionId);
        if (profile == null || revision == null || !"WECHAT_APP".equals(profile.getKind())
                || !Objects.equals(profile.getOwnerDeptId(), owner)
                || !authority.environment.equals(profile.getEnvironment())
                || mapper.wasPublished(profileId, revisionId) == 0) throw unavailable();
        var config = ViJson.tree(revision.getConfigJson());
        if (!config.path("appId").asText().matches("wx[0-9a-fA-F]{16}")) throw unavailable();
        return config;
    }

    private VerificationProvider.Configuration identity(long owner, long profile, long revision, String appId) {
        String digest = digest(owner + ":" + profile + ":" + revision + ":" + authority.environment + ":" + appId);
        return new VerificationProvider.Configuration(owner, profile, revision, "WECHAT_SOTER",
                "WX_SOTER_DEVICE_AUTH", appId, digest);
    }

    static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static ViException unavailable() {
        return new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "公司尚未发布匹配的小程序公共凭据");
    }
}
