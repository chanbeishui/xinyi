-- =====================================================================
-- 荣利 · G01~G06 整改支撑迁移
--   G01 微信支付真实通道配置
--   G02 短信通道配置 + 发送日志
--   G03 订阅消息配置
--   G04 站内交接双方确认
--   G05 sys_user.phonenumber 唯一约束
-- 幂等：可重复执行
-- 执行：mysql -h127.0.0.1 -P3307 -uroot -p*** --default-character-set=utf8mb4 xinyi < 本文件
-- =====================================================================

-- ---------------------------------------------------------------- G04 双方确认
-- 持码方与核验方各自的确认时刻；两方齐备才推进状态机（仅对配置为需双方确认的交接类型）
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND COLUMN_NAME = 'holder_confirmed_at');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` ADD COLUMN `holder_confirmed_at` DATETIME(3) NULL COMMENT ''持码方确认已交付时刻'' AFTER `confirmed_at`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND COLUMN_NAME = 'verifier_confirmed_at');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` ADD COLUMN `verifier_confirmed_at` DATETIME(3) NULL COMMENT ''核验方确认已接收时刻'' AFTER `holder_confirmed_at`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 状态枚举增加 PARTIAL：一方已确认、等待另一方确认
SET @c := (SELECT COUNT(1) FROM information_schema.CHECK_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'ck_handover_status');
SET @s := IF(@c > 0,
  'ALTER TABLE `vi_handover` DROP CHECK `ck_handover_status`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 旧库的匿名 CHECK 约束名形如 ck_66_xxx，逐个尝试移除后统一重建为具名约束
SET @c := (SELECT COUNT(1) FROM information_schema.CHECK_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'ck_66_87e8157e4010');
SET @s := IF(@c > 0,
  'ALTER TABLE `vi_handover` DROP CHECK `ck_66_87e8157e4010`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.CHECK_CONSTRAINTS
           WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'ck_handover_status');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` ADD CONSTRAINT `ck_handover_status` CHECK (`status` IN (''PENDING'',''PARTIAL'',''CONFIRMED'',''LOCKED'',''EXPIRED''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- active_key 原先只在 PENDING 时非空，且为 STORED 生成列（不能原地改表达式）。
-- 引入 PARTIAL（一方已确认）后若仍按原表达式，半确认的交接单会让"唯一活跃交接"
-- 约束失效，从而可以重复发起同一阶段交接；因此删列重建为同样的 STORED 生成列。
SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND INDEX_NAME = 'uk_active_handover');
SET @s := IF(@c > 0,
  'ALTER TABLE `vi_handover` DROP INDEX `uk_active_handover`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND COLUMN_NAME = 'active_key'
             AND GENERATION_EXPRESSION LIKE '%PARTIAL%');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` DROP COLUMN `active_key`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND COLUMN_NAME = 'active_key');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` ADD COLUMN `active_key` VARCHAR(160) GENERATED ALWAYS AS (IF(`status` IN (''PENDING'',''PARTIAL''), CONCAT(`order_id`, '':'', `assignment_id`, '':'', `handover_type`), NULL)) STORED COMMENT ''唯一活跃交接：PENDING/PARTIAL 期间非空'' AFTER `evidence_group_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_handover' AND INDEX_NAME = 'uk_active_handover');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_handover` ADD UNIQUE KEY `uk_active_handover` (`active_key`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------- G05 手机号唯一
-- 生成列只对「未删除且手机号非空」的行取值，软删除与空号不参与唯一性，
-- 既满足一员工一手机号，也不破坏历史软删数据。
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'phone_unique');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `phone_unique` VARCHAR(20) GENERATED ALWAYS AS (IF(`del_flag` = ''0'' AND `phonenumber` IS NOT NULL AND `phonenumber` <> '''', `phonenumber`, NULL)) VIRTUAL COMMENT ''手机号唯一性生成列：仅未删除且非空时取值''',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_phone');
SET @s := IF(@c = 0,
  'ALTER TABLE `sys_user` ADD UNIQUE KEY `uk_sys_user_phone` (`phone_unique`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------- G02 短信发送日志
CREATE TABLE IF NOT EXISTS `vi_sms_send_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NULL COMMENT '发起账号；后台代发时为空',
  `phone_lookup` BINARY(32) NULL COMMENT '手机号 SHA-256 摘要，用于频控计数，不落明文',
  `phone_masked` VARCHAR(20) NOT NULL COMMENT '脱敏手机号，便于人工核查',
  `scene` VARCHAR(32) NOT NULL DEFAULT 'LOGIN' COMMENT 'LOGIN=登录/绑定手机号，TEST=管理端连通性测试',
  `provider` VARCHAR(16) NOT NULL COMMENT 'ALIYUN / TENCENT / MOCK',
  `template_code` VARCHAR(64) NULL,
  `status` VARCHAR(16) NOT NULL COMMENT 'OK=供应商受理，FAIL=调用失败，BLOCKED=频控拦截',
  `error_code` VARCHAR(64) NULL,
  `error_message` VARCHAR(500) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)),
  PRIMARY KEY (`id`),
  KEY `ix_phone_time` (`phone_lookup`, `created_at`),
  KEY `ix_account_time` (`account_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短信发送与频控日志';

-- ---------------------------------------------------------------- 配置项
-- sys_config.config_key 无唯一索引，用 NOT EXISTS 保证幂等
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT * FROM (
  SELECT '支付通道' AS a, 'rongli.payment.channel' AS b, 'MOCK' AS c, 'N' AS d, 'admin' AS e, UTC_TIMESTAMP() AS f,
         'MOCK=联调通道；WECHAT=真实微信支付（需在微信支付配置页补全商户参数）' AS g
  UNION ALL SELECT '微信支付-小程序AppID', 'rongli.pay.appId', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，wx 开头 18 位'
  UNION ALL SELECT '微信支付-商户号', 'rongli.pay.mchId', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，8~12 位数字'
  UNION ALL SELECT '微信支付-APIv3密钥', 'rongli.pay.apiV3Key', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，32 位字母数字，用于回调报文解密'
  UNION ALL SELECT '微信支付-商户证书序列号', 'rongli.pay.serialNo', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，商户 API 证书序列号'
  UNION ALL SELECT '微信支付-商户私钥', 'rongli.pay.privateKey', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，apiclient_key.pem 的 PKCS#8 内容'
  UNION ALL SELECT '微信支付-平台证书序列号', 'rongli.pay.platformSerialNo', '', 'N', 'admin', UTC_TIMESTAMP(), '选填，配置后回调强制验签'
  UNION ALL SELECT '微信支付-平台证书', 'rongli.pay.platformCert', '', 'N', 'admin', UTC_TIMESTAMP(), '选填，微信支付平台证书 PEM，配置后回调强制验签'
  UNION ALL SELECT '微信支付-支付回调地址', 'rongli.pay.notifyUrl', '', 'N', 'admin', UTC_TIMESTAMP(), '真实收款必填，必须为 https 且公网可达'
  UNION ALL SELECT '微信支付-退款回调地址', 'rongli.pay.refundNotifyUrl', '', 'N', 'admin', UTC_TIMESTAMP(), '选填，留空自动取支付回调地址并把路径换成 /refund'
  UNION ALL SELECT '微信支付-商品描述', 'rongli.pay.description', '机动车检测代驾预约', 'N', 'admin', UTC_TIMESTAMP(), '下单时展示给用户的商品说明，1~127 字符'
  UNION ALL SELECT '支付回调验签开关', 'rongli.pay.verifyNotify', 'true', 'N', 'admin', UTC_TIMESTAMP(), 'true=回调必须通过平台证书验签；仅在无平台证书的联调环境才允许关闭'
  UNION ALL SELECT '支付配置模式', 'rongli.pay.mode', 'NORMAL', 'N', 'admin', UTC_TIMESTAMP(), 'NORMAL=正式；SANDBOX=沙箱（仅用于联调验证签名链路）'
  UNION ALL SELECT '短信通道开关', 'rongli.sms.enabled', 'false', 'N', 'admin', UTC_TIMESTAMP(), 'true=通过供应商真实下发；false=联调模式回显验证码'
  UNION ALL SELECT '短信供应商', 'rongli.sms.provider', 'ALIYUN', 'N', 'admin', UTC_TIMESTAMP(), 'ALIYUN=阿里云；TENCENT=腾讯云'
  UNION ALL SELECT '短信签名', 'rongli.sms.signName', '', 'N', 'admin', UTC_TIMESTAMP(), '真实下发必填，须为已审核通过的签名'
  UNION ALL SELECT '短信模板编号', 'rongli.sms.templateCode', '', 'N', 'admin', UTC_TIMESTAMP(), '真实下发必填，验证码模板编号/TemplateId'
  UNION ALL SELECT '短信-阿里云AccessKeyId', 'rongli.sms.accessKeyId', '', 'N', 'admin', UTC_TIMESTAMP(), '供应商为阿里云时必填'
  UNION ALL SELECT '短信-阿里云AccessKeySecret', 'rongli.sms.accessKeySecret', '', 'N', 'admin', UTC_TIMESTAMP(), '供应商为阿里云时必填'
  UNION ALL SELECT '短信-腾讯云SecretId', 'rongli.sms.secretId', '', 'N', 'admin', UTC_TIMESTAMP(), '供应商为腾讯云时必填'
  UNION ALL SELECT '短信-腾讯云SecretKey', 'rongli.sms.secretKey', '', 'N', 'admin', UTC_TIMESTAMP(), '供应商为腾讯云时必填'
  UNION ALL SELECT '短信-腾讯云SdkAppId', 'rongli.sms.sdkAppId', '', 'N', 'admin', UTC_TIMESTAMP(), '供应商为腾讯云时必填'
  UNION ALL SELECT '短信-腾讯云地域', 'rongli.sms.region', 'ap-guangzhou', 'N', 'admin', UTC_TIMESTAMP(), '供应商为腾讯云时使用'
  UNION ALL SELECT '短信验证码有效期', 'rongli.sms.codeExpireSeconds', '300', 'N', 'admin', UTC_TIMESTAMP(), '60~1800 秒'
  UNION ALL SELECT '短信重发间隔', 'rongli.sms.resendSeconds', '60', 'N', 'admin', UTC_TIMESTAMP(), '同一账号两次发送的最小间隔，10~600 秒'
  UNION ALL SELECT '短信单日上限', 'rongli.sms.dailyLimit', '10', 'N', 'admin', UTC_TIMESTAMP(), '同一手机号每日最多发送条数，1~50'
  UNION ALL SELECT '订阅消息-跳转页面', 'rongli.subscribe.page', 'pages/index/index', 'N', 'admin', UTC_TIMESTAMP(), '用户点击订阅消息后打开的小程序页面路径'
  UNION ALL SELECT '订阅消息-运行环境', 'rongli.subscribe.miniprogramState', 'formal', 'N', 'admin', UTC_TIMESTAMP(), 'developer=开发版；trial=体验版；formal=正式版'
  UNION ALL SELECT '订阅消息-字段映射', 'rongli.subscribe.fields', '{"thing1":"content","time2":"now"}', 'N', 'admin', UTC_TIMESTAMP(), 'JSON：模板字段名 -> 取值来源（content/now/scene）'
  UNION ALL SELECT '小程序-AppID', 'rongli.mini.appId', '', 'N', 'admin', UTC_TIMESTAMP(), '订阅消息与手机号授权登录共用；留空时沿用 application.yml 的 rongli.wechat.appId'
  UNION ALL SELECT '小程序-AppSecret', 'rongli.mini.appSecret', '', 'N', 'admin', UTC_TIMESTAMP(), '订阅消息与手机号授权登录共用；加密存储，留空时沿用 application.yml'
  UNION ALL SELECT '交接双方确认类型', 'rongli.handover.mutualConfirmTypes', 'DRIVER_TO_STATION', 'N', 'admin', UTC_TIMESTAMP(), '逗号分隔；列出的交接类型需持码方与核验方都确认后才推进，留空表示全部单方确认'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` c WHERE c.`config_key` = seed.b);

-- ---------------------------------------------------------------- 菜单：系统设置改为分组
-- 2113 原为「系统设置」单页，改为目录并挂 4 个子页面；原页面下沉为「业务参数」
UPDATE `sys_menu`
   SET `menu_name` = '系统设置', `menu_type` = 'M', `component` = NULL, `perms` = '', `icon` = 'tool', `order_num` = 1
 WHERE `menu_id` = 2113;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT * FROM (
  SELECT 2140 AS menu_id, '业务参数' AS menu_name, 2113 AS parent_id, 1 AS order_num, 'base' AS path,
         'rongli/config/index' AS component, '' AS query, '' AS route_name, 1 AS is_frame, 0 AS is_cache,
         'C' AS menu_type, '0' AS visible, '0' AS status, 'biz:config:query' AS perms, 'edit' AS icon,
         'admin' AS create_by, UTC_TIMESTAMP() AS create_time, '' AS update_by, NULL AS update_time,
         '派单、支付时限、自动确认等基础业务参数' AS remark
  UNION ALL SELECT 2141, '微信支付', 2113, 2, 'wechatPay', 'rongli/configWechatPay/index', '', '', 1, 0, 'C', '0', '0',
         'biz:config:payment', 'money', 'admin', UTC_TIMESTAMP(), '', NULL, '微信支付商户参数与回调验签配置'
  UNION ALL SELECT 2142, '短信通道', 2113, 3, 'sms', 'rongli/configSms/index', '', '', 1, 0, 'C', '0', '0',
         'biz:config:sms', 'message', 'admin', UTC_TIMESTAMP(), '', NULL, '短信供应商凭据、模板与频控配置'
  UNION ALL SELECT 2143, '订阅消息', 2113, 4, 'subscribe', 'rongli/configSubscribe/index', '', '', 1, 0, 'C', '0', '0',
         'biz:config:subscribe', 'bell', 'admin', UTC_TIMESTAMP(), '', NULL, '微信订阅消息模板与字段映射配置'
) AS m
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` x WHERE x.`menu_id` = m.menu_id);

-- 超管授权新菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m
 WHERE m.`menu_id` IN (2140, 2141, 2142, 2143)
   AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`menu_id`);

SELECT 'migration_20261022_06 done' AS result;
