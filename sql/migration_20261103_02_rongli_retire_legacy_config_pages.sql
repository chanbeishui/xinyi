-- ================================================================
-- 下线重复的旧配置页（微信支付 / 短信通道 / 订阅消息）
--
-- 背景：第三方参数曾有两套并存
--   1) 系统设置（2113）下三个页面：微信支付 2141、短信通道 2142、订阅消息 2143，
--      直接写 sys_config；
--   2) 第三方服务（2240）下：微信支付商户 2242、短信通道、短信场景模板、
--      订阅消息模板、小程序公共凭据，写集成档案 vi_integration_profile。
-- 同一组商户号/私钥/AK-SK 要在两处各填一遍，填漏一处就出现"配了不生效"。
--
-- 现在统一到集成档案：人工只在「第三方服务」页面维护，发布/回滚时由
-- RuntimeProjection 投影到 sys_config 供运行时读取，sys_config 降级为生效快照。
-- 因此三个旧页面连同其保存端点、前端页面一并删除。
--
-- 顺带把「业务参数」（2140）从只剩一个空壳的「系统设置」目录里提出来，
-- 直接挂到「检测与配置」下，路径由 /rongli/inspect/config/base 变为
-- /rongli/inspect/config（前端 routes.ts 的 RL.config 本来就是后者）。
-- ================================================================

-- 1) 删除三个重复页面的角色授权与菜单
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (2141, 2142, 2143);
DELETE FROM `sys_menu` WHERE `menu_id` IN (2141, 2142, 2143);

-- 2) 业务参数上提一级，成为「检测与配置」下的独立页面
UPDATE `sys_menu`
   SET `parent_id` = 2192,
       `path` = 'config',
       `order_num` = 6,
       `route_name` = 'RongliInspectConfig',
       `menu_name` = '业务参数',
       `remark` = '派单、支付时限、自动确认等基础业务参数；第三方凭据在「第三方服务」维护'
 WHERE `menu_id` = 2140;

-- 3) 删除已经成为空壳的「系统设置」目录
DELETE FROM `sys_role_menu` WHERE `menu_id` = 2113;
DELETE FROM `sys_menu` WHERE `menu_id` = 2113;

-- 4) 自检：不应再有指向旧页面的菜单，也不应留下孤儿目录
SELECT 'residual_legacy_menu' AS check_name, COUNT(*) AS cnt
  FROM `sys_menu`
 WHERE `menu_id` IN (2113, 2141, 2142, 2143)
    OR `component` IN ('rongli/configWechatPay/index', 'rongli/configSms/index', 'rongli/configSubscribe/index');

SELECT 'orphan_menu' AS check_name, COUNT(*) AS cnt
  FROM `sys_menu` m
 WHERE m.`parent_id` <> 0
   AND NOT EXISTS (SELECT 1 FROM `sys_menu` p WHERE p.`menu_id` = m.`parent_id`);

SELECT 'migration_20261103_02 done' AS result;
