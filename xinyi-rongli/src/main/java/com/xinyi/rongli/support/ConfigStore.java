package com.xinyi.rongli.support;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xinyi.common.constant.CacheConstants;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.system.domain.SysConfig;
import com.xinyi.system.service.ISysConfigService;

/**
 * 配置写入器：管理端保存配置后立即可被业务读到。
 *
 * <p>框架的 {@code selectConfigByKey} 会读 Redis 缓存，直接 UPDATE 数据表不会让缓存失效，
 * 表现为"页面显示已保存、下单仍走旧值"。本类在写入后主动删除对应缓存键，
 * 下一次读取即回源到新值，从而满足"保存即生效"。</p>
 */
@Component
public class ConfigStore
{
    private static final Logger log = LoggerFactory.getLogger(ConfigStore.class);

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private Crypto crypto;

    /** 写入明文配置；值未变化时不落库也不清缓存 */
    public void put(String key, String value)
    {
        String text = value == null ? "" : value;
        var existing = adminMapper.findConfigByKey(key);
        if (existing == null)
        {
            SysConfig entity = new SysConfig();
            entity.setConfigKey(key);
            entity.setConfigName(key);
            entity.setConfigValue(text);
            entity.setConfigType("N");
            configService.insertConfig(entity);
            evict(key);
            return;
        }
        String current = String.valueOf(existing.getOrDefault("config_value", ""));
        if (Objects.equals(current, text))
        {
            return;
        }
        adminMapper.updateConfigValue(key, text);
        evict(key);
    }

    /** 写入敏感配置：AES-GCM 加密后以 ENC: 前缀存储，绝不落明文 */
    public void putSecret(String key, String plain)
    {
        String text = plain == null ? "" : plain;
        if (text.isEmpty())
        {
            put(key, "");
            return;
        }
        put(key, "ENC:" + Crypto.toBase64(crypto.encrypt(text)));
    }

    /** 读取明文（自动解密 ENC: 值） */
    public String get(String key, String defaultValue)
    {
        String raw = configService.selectConfigByKey(key);
        if (raw == null || raw.isBlank())
        {
            return defaultValue;
        }
        if (raw.startsWith("ENC:"))
        {
            String plain = crypto.decrypt(Crypto.fromBase64(raw.substring(4)));
            return plain == null ? defaultValue : plain;
        }
        return raw.trim();
    }

    /** 判断某项是否已配置（敏感项只看是否有值，不返回内容） */
    public boolean configured(String key)
    {
        String raw = configService.selectConfigByKey(key);
        return raw != null && !raw.isBlank();
    }

    private void evict(String key)
    {
        try
        {
            redisCache.deleteObject(CacheConstants.SYS_CONFIG_KEY + key);
        }
        catch (Exception e)
        {
            // 缓存不可用时不阻断保存：最坏情况是等缓存自然过期，业务仍能读到新值
            log.warn("清理配置缓存失败 key={}：{}", key, e.getMessage());
        }
    }
}
