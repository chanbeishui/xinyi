package com.xinyi.rongli.config.mapper;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 业务配置/字典/价格守卫读取（C01）。sys_config没有config_key唯一约束，不能把ON DUPLICATE当作按键更新。
 * 单键必须先验证0/1/>1条真实记录；重复历史明确拒绝，不猜保留哪条，不删除数据。
 */
public interface ConfigMapper {
    List<Map<String, Object>> selectEntries(@Param("key") String key, @Param("lock") boolean lock);

    default String selectValue(String key) {
        List<Map<String, Object>> rows = selectEntries(key, false);
        requireUnique(key, rows);
        return rows.isEmpty() ? null : (String) rows.get(0).get("config_value");
    }

    List<Map<String, Object>> lockVersion();

    List<Map<String, Object>> selectBusiness();

    List<Map<String, Object>> selectQualificationConsent(@Param("lock") boolean lock);

    List<Map<String, Object>> selectVerificationConsent(@Param("lock") boolean lock);

    int insertValue(@Param("key") String key, @Param("value") String value);

    int updateValue(@Param("id") long id, @Param("key") String key, @Param("value") String value);

    int ensureVersion();

    /** 公共只读不创建版本行；缺失仅代表技术版本0，不产生任何业务运营默认值。 */
    default long selectVersion() {
        List<Map<String, Object>> rows = selectEntries("vi.config.version", false);
        requireUnique("vi.config.version", rows);
        return rows.isEmpty() ? 0 : versionValue((String) rows.get(0).get("config_value"));
    }

    int casVersion(
            @Param("id") long id, @Param("expected") String expected, @Param("next") String next);

    List<Map<String, Object>> selectDict(@Param("dictType") String dictType);

    int countPublishedPrice(@Param("vehicleTypeCode") String vehicleTypeCode);

    static void requireUnique(String key, List<?> rows) {
        if (rows.size() > 1)
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "配置键存在重复记录，请先核查处理：" + key);
    }

    static long versionValue(String value) {
        try {
            if (value == null || !value.matches("0|[1-9][0-9]*")) throw new NumberFormatException();
            long parsed = Long.parseLong(value);
            if (parsed > 9007199254740991L) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException failure) {
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "业务配置版本记录无效，请先核查处理");
        }
    }
}
