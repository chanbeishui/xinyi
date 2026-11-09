-- =====================================================================
-- 迁移：地图服务配置（检测点经纬度选点）
--
-- 用途：检测点维护抽屉需要地图选点，选点结果要和小程序端 uni.getLocation 的
--       gcj02 坐标对齐，否则派单半径判定会偏几百米。地图凭据放在 sys_config
--       而不是前端 .env，是为了换 Key / 停用地图时不用重新打包发版。
--
-- 前置版本：2026-11-09 及以后
-- 执行顺序：无依赖，可单独执行
-- 可重复执行：是。sys_config 没有 config_key 唯一索引，不能用
--             ON DUPLICATE KEY UPDATE，这里按 config_key 判重。
-- 破坏性：无，仅新增三行配置。
--
-- 执行后注意：系统参数有缓存，需在后台「参数设置」里保存一次，或删除
--   Redis 键 sys_config:rongli.map.provider / .key / .securityCode，或重启后端。
-- =====================================================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '地图服务-供应商', 'rongli.map.provider', 'amap', 'Y', 'admin', NOW(),
       '检测点经纬度选点使用；目前支持 amap，留空或其它值表示不启用地图选点'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'rongli.map.provider');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '地图服务-Key', 'rongli.map.key', '', 'Y', 'admin', NOW(),
       '高德开放平台 Web 端（JS API）Key；建议在控制台配置域名白名单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'rongli.map.key');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '地图服务-安全密钥', 'rongli.map.securityCode', '', 'Y', 'admin', NOW(),
       'JS API 2.0 安全密钥（jscode）；与 Key 配套，留空则按无安全密钥方式加载'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'rongli.map.securityCode');

-- 执行后校验：应返回三行，provider=amap，其余两项待运营填写
SELECT config_key, config_value, remark
FROM sys_config
WHERE config_key LIKE 'rongli.map.%'
ORDER BY config_key;
