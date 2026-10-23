package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.auth.MiniPrincipalImpl;
import com.xinyi.rongli.identity.config.ViIdentityProperties;
import com.xinyi.rongli.identity.domain.SessionStatus;
import com.xinyi.rongli.identity.domain.SessionTokenType;
import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.domain.ViSessionToken;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.mapper.ViSessionTokenMapper;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * MINI 不透明令牌服务（ADR-01）。令牌为至少 32 字节安全随机数，数据库只存 SHA-256 摘要
 * （vi_session_token.token_hash BINARY(32)，唯一键）；明文仅在登录响应返回一次，绝不落库/日志/幂等缓存。
 * 解析访问令牌时校验：令牌存在/未撤销/未消费/未过期 → 会话 ACTIVE/未撤销/未过绝对截止 →
 * session_epoch 与账号安全代际一致（全局退出/封禁即时失效）→ 账号未 LOCKED。
 * 刷新轮换/重复旧刷新令牌撤销会话族属 I03，本任务只签发初始 ACCESS/REFRESH 并解析 ACCESS。
 */
@Service
public class MiniTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

    private final ViSessionTokenMapper tokenMapper;
    private final ViSessionMapper sessionMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final ViIdentityProperties props;

    public MiniTokenService(ViSessionTokenMapper tokenMapper, ViSessionMapper sessionMapper,
                            ViAccountSecurityMapper securityMapper, ViIdentityProperties props) {
        this.tokenMapper = tokenMapper;
        this.sessionMapper = sessionMapper;
        this.securityMapper = securityMapper;
        this.props = props;
    }

    /** 签发结果：明文令牌 + 有效期秒。仅此处持有明文，调用方写入响应后不得留存。 */
    public record IssuedTokens(String accessToken, long expiresIn, String refreshToken, long refreshExpiresIn) {
    }

    /** 为已持久化的会话签发初始 ACCESS + REFRESH 令牌（同事务内调用）。 */
    public IssuedTokens issueForSession(ViSession session) {
        Instant now = Instant.now();
        long accessTtl = props.getAccessTtlSeconds();
        long absoluteTtl = props.getAbsoluteTtlSeconds();

        String accessPlain = newToken();
        insertToken(session.getId(), accessPlain, SessionTokenType.ACCESS.name(), 0L,
                session.getContextVersion(), now.plusSeconds(accessTtl));

        String refreshPlain = newToken();
        long refreshExpiresIn = absoluteTtl;
        insertToken(session.getId(), refreshPlain, SessionTokenType.REFRESH.name(), 0L,
                null, session.getAbsoluteExpiresAt());

        return new IssuedTokens(accessPlain, accessTtl, refreshPlain, refreshExpiresIn);
    }

    private void insertToken(Long sessionId, String plain, String type, long generation,
                             Long contextVersion, Instant expiresAt) {
        ViSessionToken t = new ViSessionToken();
        t.setSessionId(sessionId);
        t.setTokenHash(sha256(plain));
        t.setTokenType(type);
        t.setGeneration(generation);
        t.setContextVersion(contextVersion);
        t.setExpiresAt(expiresAt);
        tokenMapper.insert(t);
    }

    /** 解析访问令牌为 MiniPrincipal；任何无效/过期/撤销/代际不符/锁定都拒绝（不泄露细节）。 */
    public MiniPrincipalImpl resolveAccess(String plaintextToken) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViSessionToken token = tokenMapper.selectByHash(sha256(plaintextToken), SessionTokenType.ACCESS.name());
        Instant now = Instant.now();
        if (token == null || token.getRevokedAt() != null || token.getConsumedAt() != null
                || token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViSession session = sessionMapper.selectById(token.getSessionId());
        if (session == null || !SessionStatus.ACTIVE.name().equals(session.getStatus())
                || session.getRevokedAt() != null
                || session.getAbsoluteExpiresAt() == null || !session.getAbsoluteExpiresAt().isAfter(now)) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViAccountSecurity security = securityMapper.selectByAccountId(session.getAccountId());
        if (security == null
                || !java.util.Objects.equals(security.getSessionEpoch(), session.getSessionEpoch())) {
            // 代际不符 = 全局退出/安全封禁已提升 epoch，旧会话立即失效
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (!"ACTIVE".equals(security.getSecurityStatus())) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "账号已被限制");
        }
        return new MiniPrincipalImpl(session.getAccountId(), session.getLinkedSysUserId(), session.getId(),
                session.getActiveRole(), session.getStationId(), session.getContextVersion());
    }

    private static String newToken() {
        byte[] buf = new byte[32];
        RANDOM.nextBytes(buf);
        return B64URL.encodeToString(buf);
    }

    private static byte[] sha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(plain.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "SHA-256 不可用", e);
        }
    }
}
