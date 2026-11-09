package com.xinyi.rongli.file.service;

import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.error.*;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** 短期票据绑定动作、文件、主体和小程序会话上下文；使用独立域前缀隔离其他 HMAC 用途。 */
@Component
public class FileTickets {
    private final ViPlatformProperties properties;

    public FileTickets(ViPlatformProperties properties) {
        this.properties = properties;
    }

    public String sign(Actor actor, long id, String action, Instant expires) {
        return sign(actor, id, action, expires, "");
    }

    public String sign(Actor actor, long id, String action, Instant expires, String context) {
        long seconds = expires.getEpochSecond();
        return seconds + "." + mac(actor, id, action, seconds, context);
    }

    public void verify(Actor actor, long id, String action, String ticket) {
        verify(actor, id, action, ticket, "");
    }

    public void verify(Actor actor, long id, String action, String ticket, String context) {
        try {
            String[] parts = ticket.split("\\.", -1);
            if (parts.length != 2) throw new IllegalArgumentException();
            long expires = Long.parseLong(parts[0]);
            long now = Instant.now().getEpochSecond();
            if (expires <= now
                    || expires > now + 300
                    || !MessageDigest.isEqual(
                            parts[1].getBytes(StandardCharsets.US_ASCII),
                            mac(actor, id, action, expires, context)
                                    .getBytes(StandardCharsets.US_ASCII)))
                throw new IllegalArgumentException();
        } catch (RuntimeException error) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "文件授权已失效，请重新获取");
        }
    }

    private String mac(Actor actor, long id, String action, long expires, String context) {
        if (!properties.hasLookupKey()) throw new ViException(ViErrorCode.VI_SECRET_UNAVAILABLE);
        try {
            String subject =
                    actor.realm() + ":" + (actor.isMini() ? actor.accountId() : actor.sysUserId());
            String content =
                    "VI_PRIVATE_FILE_V1|"
                            + action
                            + "|"
                            + id
                            + "|"
                            + expires
                            + "|"
                            + subject
                            + "|"
                            + actor.sessionId()
                            + "|"
                            + actor.contextVersion()
                            + (context == null || context.isEmpty() ? "" : "|" + context);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(
                    new SecretKeySpec(
                            properties.getLookupKey().getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception error) {
            throw new ViException(ViErrorCode.VI_SECRET_UNAVAILABLE);
        }
    }
}
