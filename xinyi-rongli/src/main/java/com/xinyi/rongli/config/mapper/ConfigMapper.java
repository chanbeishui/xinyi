package com.xinyi.rongli.config.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 业务配置/字典/价格守卫读取（C01）。配置存 sys_config（vi.biz.* 白名单+vi.config.version CAS）；
 * 字典 sys_dict_data；价格守卫 vi_price_rule（新车型无已发布价格不能下单）。不写供应商密钥。
 */
public interface ConfigMapper {

    String selectValue(@Param("key") String key);

    List<Map<String, Object>> selectBusiness();

    int upsert(@Param("key") String key, @Param("value") String value);

    int ensureVersion();

    long selectVersion();

    int casVersion(@Param("expected") String expected, @Param("next") String next);

    List<Map<String, Object>> selectDict(@Param("dictType") String dictType);

    int countPublishedPrice(@Param("vehicleTypeCode") String vehicleTypeCode);
}