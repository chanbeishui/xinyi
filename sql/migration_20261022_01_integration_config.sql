-- =============================================================================
-- migration_20261022_01_integration_config.sql
-- 用途：将 xinyi 库 vi_ schema 从 V6.1(66 表) 前向适配到 V6.4「第三方接入配置版」(79 表)。
--       新增 15 表（12 第三方配置 + 员工目录锁 + 手机号证明回执 + 站点授权集合版本）；
--       4 表加列（vi_account/vi_employee_binding/vi_payment_order/vi_refund_order）；
--       扩展 vi_payment_order.status 枚举（新增 REVIEW_PENDING）；退役 2 表保留不 DROP。
-- 适用前置：已按 V6.1 执行 migration_20261021_vehicle_initial.sql（xinyi 库已有 66 张 vi_ 表）。
-- 执行顺序：单文件内按 A 新增表 → B~E 逐表 ALTER → F 退役标注；DDL 隐式提交、无全脚本事务回滚。
-- 备份要求：执行前必须 mysqldump 备份 xinyi（本次已备份至 .vi-local/backup/xinyi_pre_v64_*.sql）。
-- 可重复性：不可重复执行（CREATE TABLE / ADD COLUMN 无 IF NOT EXISTS，重复将报错，以此暴露误重跑）。
-- 失败恢复：从备份整库恢复；或按对象逆序 DROP 本次新增列/约束/索引/表（见文件尾回滚说明）。
-- 数据安全：不 DROP 任何既有表；退役表 vi_employee_binding_request/credential 保留历史、应用层停写；
--           4 张加列表当前均 0 行（新系统无业务数据），加列不回填、不猜测历史值；
--           若目标库这些表存在历史行，须先按 V6.4 前向适配规则查原微信资金身份/主体，
--           无法证明的记录生成异常清单并暂停其新资金动作，禁止用当前商户/默认管理员猜测回填。
-- 权威来源：资料包 V6.4 05_数据设计/001_vi业务表_初始化_V6.4.sql；本文件由
--           docs/implementation/vehicle/v64_adapt/gen_migration.py 自动提取生成（可复现）。
-- =============================================================================

-- ---------------------------------------------------------------------------
-- A. 新增 15 张表（V6.4 001 权威 DDL 原样）
-- ---------------------------------------------------------------------------

CREATE TABLE `vi_employee_directory_guard` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `guard_key` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '首期唯一GLOBAL_DIRECTORY；同库轻量互斥，不是用户权限',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_employee_directory_guard (guard_key),
  CONSTRAINT ck_ver_employee_directory_guard CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工手机号匹配与目录变更串行防护';

CREATE TABLE `vi_phone_verification_receipt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `request_id` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '客户端一次流程UUID，绑定当前账号',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端应用标识',
  `account_id` BIGINT NOT NULL COMMENT '已认证MINI账号；不从请求接收',
  `session_id` BIGINT NOT NULL COMMENT '发起的基础会话',
  `code_hash` BINARY(32) NOT NULL COMMENT '微信phoneCode摘要，防跨账号重放；绝不保存原始code',
  `status` VARCHAR(24) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PhoneVerificationStatus: PROCESSING,VERIFIED,UNKNOWN,FAILED',
  `phone_lookup` BINARY(32) NULL COMMENT '成功后可信手机号HMAC',
  `phone_cipher` VARBINARY(512) NULL COMMENT '成功结果恢复所需加密号码；敏感回执到期按留存规则清理，审计不记录原文',
  `verified_at` DATETIME(3) NULL COMMENT '微信已成功核验的UTC时间',
  `expires_at` DATETIME(3) NOT NULL COMMENT '关联重试窗口截至时间；默认10分钟是技术值，不是微信code有效期',
  `employee_link_outcome` VARCHAR(40) NOT NULL DEFAULT 'NOT_ATTEMPTED' COMMENT 'EmployeeLinkOutcome；内部原因不直接下发小程序',
  `lease_until` DATETIME(3) NULL COMMENT '处理租约；处理中未知不盲目重复消费供应商凭据',
  `finished_at` DATETIME(3) NULL COMMENT '最终处理时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_phone_request (account_id,request_id),
  UNIQUE KEY uk_phone_code (app_id,code_hash),
  KEY ix_phone_receipt_status (status,lease_until),
  CONSTRAINT ck_cbadb1ceb7 CHECK (`status` IN ('PROCESSING','VERIFIED','UNKNOWN','FAILED')),
  CONSTRAINT ck_b584437700 CHECK (`employee_link_outcome` IN ('NOT_ATTEMPTED','NO_MATCH','LINKED','EXISTING_LINK','AMBIGUOUS_PHONE','EMPLOYEE_UNAVAILABLE','USER_ALREADY_LINKED','ACCOUNT_ALREADY_LINKED','REBIND_BLOCKED','PHONE_CHANGED','DIRECTORY_UNAVAILABLE')),
  CONSTRAINT ck_ver_phone_verification_receipt CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手机号授权处理与重试回执';

CREATE TABLE `vi_station_grant_set` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串',
  `sys_user_id` BIGINT NOT NULL COMMENT '目标框架员工；可无微信账号',
  `own_station_id` BIGINT NULL COMMENT '仅行政展示，不自动授权',
  `changed_by` BIGINT NULL COMMENT '后台修改人',
  `reason` VARCHAR(500) NULL COMMENT '本次集合调整原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_station_grant_employee (sys_user_id),
  CONSTRAINT ck_ver_station_grant_set CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工站点授权集合版本';

CREATE TABLE `vi_integration_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationKind: PAYMENT_POLICY,WECHAT_PAY,TENCENT_SMS,ALIYUN_SMS,WECHAT_APP；',
  `owner_dept_id` BIGINT NOT NULL COMMENT '真实管理/收款主体，复用框架部门归属',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；',
  `config_key` VARCHAR(80) NOT NULL COMMENT 'PAYMENT_POLICY与WECHAT_APP用DEFAULT；其他由服务端生成，不由用户改身份',
  `display_name` VARCHAR(100) NOT NULL COMMENT '',
  `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationLifecycle: DRAFT,ACTIVE,RETIRED；',
  `active_revision_id` BIGINT NULL COMMENT '当前发布版本',
  `draft_revision_id` BIGINT NULL COMMENT '最新草稿版本',
  `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '分配版本号的CAS计数',
  `created_by` BIGINT NOT NULL COMMENT '配置创建人',
  `last_published_by` BIGINT NULL COMMENT '最近发布人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_integration_scope (kind,owner_dept_id,environment,config_key),
  KEY ix_integration_active (active_revision_id),
  CONSTRAINT ck_ver_integration_profile CHECK (version >= 0),
  CONSTRAINT ck_fdb65a8cb0d9d1 CHECK (`kind` IN ('PAYMENT_POLICY','WECHAT_PAY','TENCENT_SMS','ALIYUN_SMS','WECHAT_APP')),
  CONSTRAINT ck_5d0e7efd82afb9 CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_7d6eefe158cca1 CHECK (`lifecycle` IN ('DRAFT','ACTIVE','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方配置档案与生效指针';

CREATE TABLE `vi_integration_revision` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `profile_id` BIGINT NOT NULL COMMENT '',
  `revision_no` BIGINT NOT NULL COMMENT '',
  `config_json` JSON NOT NULL COMMENT '经kind专用DTO验证的非秘密配置',
  `secret_refs_json` JSON NOT NULL COMMENT '本配置凭据槽位到secret id的内部映射，不下发',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '规范化内容与秘密引用版本摘要',
  `created_by` BIGINT NOT NULL COMMENT '',
  `reason` VARCHAR(500) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_config_revision (profile_id,revision_no),
  UNIQUE KEY uk_config_hash (profile_id,content_hash),
  CONSTRAINT ck_ver_integration_revision CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变第三方配置版本';

CREATE TABLE `vi_integration_secret` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `profile_id` BIGINT NOT NULL COMMENT '',
  `slot_name` VARCHAR(64) NOT NULL COMMENT '固定允许的凭据槽，禁止用户自造密钥用途',
  `credential_kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationCredentialKind: PRIVATE_KEY,API_V3_KEY,CLOUD_SECRET,APP_SECRET,BANK_ACCOUNT；',
  `ciphertext` MEDIUMBLOB NOT NULL COMMENT 'AEAD密文或KMS加密数据键封装，绝非base64明文',
  `nonce` VARBINARY(32) NOT NULL COMMENT '每密文独立随机nonce',
  `key_version` VARCHAR(128) NOT NULL COMMENT '主密钥版本在服务端安全配置',
  `aad_context` VARCHAR(255) NOT NULL COMMENT '绑定profile/slot/env的不可更改上下文',
  `fingerprint` VARCHAR(64) NOT NULL COMMENT '安全标识不可逆；不显示明文末几位',
  `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationSecretLifecycle: ACTIVE,RETIRED,REVOKED；',
  `expires_at` DATETIME(3) NULL COMMENT '',
  `retired_at` DATETIME(3) NULL COMMENT '',
  `created_by` BIGINT NOT NULL COMMENT '',
  `retired_by` BIGINT NULL COMMENT '受控撤销人',
  `retire_reason` VARCHAR(500) NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_secret_owner (profile_id,slot_name,lifecycle),
  CONSTRAINT ck_ver_integration_secret CHECK (version >= 0),
  CONSTRAINT ck_db3f36fab3347a CHECK (`credential_kind` IN ('PRIVATE_KEY','API_V3_KEY','CLOUD_SECRET','APP_SECRET','BANK_ACCOUNT')),
  CONSTRAINT ck_83aefc040222fa CHECK (`lifecycle` IN ('ACTIVE','RETIRED','REVOKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='安全凭据密文与受控生命周期';

CREATE TABLE `vi_integration_check` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE；',
  `target_id` BIGINT NOT NULL COMMENT '',
  `revision_id` BIGINT NOT NULL COMMENT '',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '',
  `check_level` VARCHAR(24) NOT NULL COMMENT 'IntegrationCheckLevel: LOCAL,READ_ONLY,LIVE_EVIDENCE；',
  `status` VARCHAR(16) NOT NULL COMMENT 'IntegrationCheckStatus: NOT_RUN,PASS,FAIL,PARTIAL；',
  `issues_json` JSON NOT NULL COMMENT '脱敏逐字段错误/警告/未验证项',
  `checked_at` DATETIME(3) NOT NULL COMMENT '',
  `expires_at` DATETIME(3) NOT NULL COMMENT '',
  `checked_by` BIGINT NOT NULL COMMENT '',
  `real_payment_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '仅真实授权资金测试证据才能标记；只读连接不是支付成功',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_check_revision (target_type,target_id,revision_id,checked_at),
  CONSTRAINT ck_ver_integration_check CHECK (version >= 0),
  CONSTRAINT ck_88cd710155a461 CHECK (`target_type` IN ('PROFILE','TEMPLATE')),
  CONSTRAINT ck_4c4d3c7687eba0 CHECK (`check_level` IN ('LOCAL','READ_ONLY','LIVE_EVIDENCE')),
  CONSTRAINT ck_5bc059710176ef CHECK (`status` IN ('NOT_RUN','PASS','FAIL','PARTIAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='指定配置版本校验记录';

CREATE TABLE `vi_integration_publication` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE；',
  `target_id` BIGINT NOT NULL COMMENT '',
  `revision_id` BIGINT NOT NULL COMMENT '',
  `previous_revision_id` BIGINT NULL COMMENT '旧生效版本',
  `check_id` BIGINT NOT NULL COMMENT '',
  `action` VARCHAR(16) NOT NULL COMMENT 'IntegrationPublishAction: PUBLISH,ROLLBACK；',
  `published_by` BIGINT NOT NULL COMMENT '',
  `published_at` DATETIME(3) NOT NULL COMMENT '',
  `reason` VARCHAR(500) NOT NULL COMMENT '',
  `dependency_versions` JSON NOT NULL COMMENT '发布时依赖版本快照',
  `target_version` BIGINT NOT NULL COMMENT '发布事务后的根版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_publication_target (target_type,target_id,published_at),
  CONSTRAINT ck_ver_integration_publication CHECK (version >= 0),
  CONSTRAINT ck_1da14a68f349f2 CHECK (`target_type` IN ('PROFILE','TEMPLATE')),
  CONSTRAINT ck_fc819ff643e13c CHECK (`action` IN ('PUBLISH','ROLLBACK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置发布与回滚审计';

CREATE TABLE `vi_integration_test_recipient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `name` VARCHAR(80) NOT NULL COMMENT '',
  `kind` VARCHAR(16) NOT NULL COMMENT 'IntegrationRecipientKind: PHONE,MINI_ACCOUNT；',
  `owner_dept_id` BIGINT NOT NULL COMMENT '',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；',
  `phone_cipher` VARBINARY(512) NULL COMMENT '仅PHONE',
  `account_id` BIGINT NULL COMMENT '仅MINI_ACCOUNT',
  `app_profile_id` BIGINT NULL COMMENT '小程序白名单同应用',
  `enabled` TINYINT NOT NULL COMMENT '',
  `created_by` BIGINT NOT NULL COMMENT '',
  `reason` VARCHAR(500) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_test_recipient_scope (owner_dept_id,environment,enabled),
  CONSTRAINT ck_ver_integration_test_recipient CHECK (version >= 0),
  CONSTRAINT ck_1117ecf2dcdc35 CHECK (`kind` IN ('PHONE','MINI_ACCOUNT')),
  CONSTRAINT ck_95800f15c7f107 CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_recipient_shape CHECK ((kind='PHONE' AND phone_cipher IS NOT NULL AND account_id IS NULL) OR (kind='MINI_ACCOUNT' AND phone_cipher IS NULL AND account_id IS NOT NULL AND app_profile_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方测试收件人白名单';

CREATE TABLE `vi_message_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `name` VARCHAR(100) NOT NULL COMMENT '',
  `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS,WECHAT_SUBSCRIBE；',
  `owner_dept_id` BIGINT NOT NULL COMMENT '',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；',
  `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION；',
  `active_revision_id` BIGINT NULL COMMENT '当前已发布模板',
  `draft_revision_id` BIGINT NULL COMMENT '最新草稿',
  `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '模板版本分配计数',
  `created_by` BIGINT NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_template_scene (owner_dept_id,environment,scene,audience,provider),
  CONSTRAINT ck_ver_message_template CHECK (version >= 0),
  CONSTRAINT ck_d4792ebbf8b6a6 CHECK (`provider` IN ('TENCENT_SMS','ALIYUN_SMS','WECHAT_SUBSCRIBE')),
  CONSTRAINT ck_fc161ac5a52b5d CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_a22fd0ee77b18a CHECK (`scene` IN ('LOGIN_OTP','PHONE_VERIFY','BOOKING_CONFIRMED','DRIVER_ASSIGNED','PICKUP_ARRIVED','INSPECTION_FINISHED','RETURN_COMPLETED','AFTERSALE_DECIDED','REFUND_CHANGED','DRIVER_INVITE','STATEMENT_READY')),
  CONSTRAINT ck_992903d6097179 CHECK (`audience` IN ('OWNER','DRIVER','STATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部短信与订阅模板档案';

CREATE TABLE `vi_message_template_revision` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `template_id` BIGINT NOT NULL COMMENT '',
  `revision_no` BIGINT NOT NULL COMMENT '',
  `profile_id` BIGINT NOT NULL COMMENT '',
  `profile_revision_id` BIGINT NOT NULL COMMENT '',
  `vendor_template_id` VARCHAR(256) NULL COMMENT '实际供应商审核通过的模板ID；草稿可空，发布时强制',
  `parameter_bindings` JSON NOT NULL COMMENT '白名单变量、实际key/type/length；腾讯顺序/阿里命名',
  `options_json` JSON NOT NULL COMMENT '签名/内容快照/小程序page/env/lang等非秘密',
  `enabled` TINYINT NOT NULL COMMENT '发送开关',
  `approval_status` VARCHAR(16) NOT NULL COMMENT 'TemplateApprovalStatus: UNKNOWN,APPROVED,REJECTED；',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '',
  `created_by` BIGINT NOT NULL COMMENT '',
  `reason` VARCHAR(500) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_template_revision (template_id,revision_no),
  CONSTRAINT ck_ver_message_template_revision CHECK (version >= 0),
  CONSTRAINT ck_b7dcf36d038099 CHECK (`approval_status` IN ('UNKNOWN','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变外部模板版本';

CREATE TABLE `vi_notification_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `owner_dept_id` BIGINT NOT NULL COMMENT '',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；',
  `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION；',
  `primary_template_id` BIGINT NOT NULL COMMENT '',
  `fallback_template_id` BIGINT NULL COMMENT '只允许明确未发送时按规则用备用',
  `fallback_policy` VARCHAR(32) NOT NULL COMMENT 'MessageFallbackPolicy: NONE,DEFINITELY_NOT_SENT_ONLY；',
  `enabled` TINYINT NOT NULL COMMENT '',
  `updated_by` BIGINT NOT NULL COMMENT '',
  `reason` VARCHAR(500) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_message_route (owner_dept_id,environment,scene,audience),
  CONSTRAINT ck_ver_notification_route CHECK (version >= 0),
  CONSTRAINT ck_545a6078080a6c CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_abb77f22beb38f CHECK (`scene` IN ('LOGIN_OTP','PHONE_VERIFY','BOOKING_CONFIRMED','DRIVER_ASSIGNED','PICKUP_ARRIVED','INSPECTION_FINISHED','RETURN_COMPLETED','AFTERSALE_DECIDED','REFUND_CHANGED','DRIVER_INVITE','STATEMENT_READY')),
  CONSTRAINT ck_e031dc9c69e46e CHECK (`audience` IN ('OWNER','DRIVER','STATION')),
  CONSTRAINT ck_fbf39632b0a6fb CHECK (`fallback_policy` IN ('NONE','DEFINITELY_NOT_SENT_ONLY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务事件到唯一主通道的路由';

CREATE TABLE `vi_notification_delivery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `notification_id` BIGINT NOT NULL COMMENT '关联已有站内消息/OTP任务；OTP可使用内部通知目标记录',
  `event_key` VARCHAR(128) NOT NULL COMMENT '逻辑事件业务键',
  `recipient_key` VARCHAR(128) NOT NULL COMMENT 'accountId或脱敏HMAC，禁止裸号',
  `recipient_cipher` VARBINARY(1024) NOT NULL COMMENT '必要收件手机号/OpenID密文',
  `owner_dept_id` BIGINT NOT NULL COMMENT '',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；',
  `route_id` BIGINT NOT NULL COMMENT '',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；',
  `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS,WECHAT_SUBSCRIBE；',
  `template_id` BIGINT NOT NULL COMMENT '',
  `template_revision_id` BIGINT NOT NULL COMMENT '',
  `profile_id` BIGINT NOT NULL COMMENT '',
  `profile_revision_id` BIGINT NOT NULL COMMENT '',
  `payload_cipher_refs` JSON NOT NULL COMMENT '消息内容加密引用/服务器事件引用，不含明文验证码',
  `status` VARCHAR(24) NOT NULL COMMENT 'ExternalDeliveryStatus: PENDING,SENDING,ACCEPTED,DELIVERED,FAILED,UNKNOWN,SKIPPED；',
  `vendor_request_id` VARCHAR(128) NULL COMMENT '',
  `vendor_biz_id` VARCHAR(128) NULL COMMENT '',
  `safe_error_code` VARCHAR(128) NULL COMMENT '',
  `safe_error_message` VARCHAR(500) NULL COMMENT '',
  `attempt_count` INT NOT NULL DEFAULT 0 COMMENT '',
  `next_attempt_at` DATETIME(3) NULL COMMENT '',
  `lease_until` DATETIME(3) NULL COMMENT '',
  `accepted_at` DATETIME(3) NULL COMMENT '',
  `delivered_at` DATETIME(3) NULL COMMENT '',
  `business_expires_at` DATETIME(3) NOT NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_delivery_event (event_key,recipient_key,scene),
  KEY ix_delivery_retry (status,next_attempt_at),
  KEY ix_vendor_message (provider,vendor_biz_id),
  CONSTRAINT ck_ver_notification_delivery CHECK (version >= 0),
  CONSTRAINT ck_3295c253da0860 CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_53d61b6a3a8ac2 CHECK (`scene` IN ('LOGIN_OTP','PHONE_VERIFY','BOOKING_CONFIRMED','DRIVER_ASSIGNED','PICKUP_ARRIVED','INSPECTION_FINISHED','RETURN_COMPLETED','AFTERSALE_DECIDED','REFUND_CHANGED','DRIVER_INVITE','STATEMENT_READY')),
  CONSTRAINT ck_6066945178ca2a CHECK (`provider` IN ('TENCENT_SMS','ALIYUN_SMS','WECHAT_SUBSCRIBE')),
  CONSTRAINT ck_9de57b85c185c7 CHECK (`status` IN ('PENDING','SENDING','ACCEPTED','DELIVERED','FAILED','UNKNOWN','SKIPPED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部发送执行与回执';

CREATE TABLE `vi_subscription_prompt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '',
  `app_id` VARCHAR(64) NOT NULL COMMENT '',
  `nonce_hash` VARCHAR(64) NOT NULL COMMENT '一次性nonce摘要',
  `template_versions` JSON NOT NULL COMMENT '模板ID、公开版本及场景',
  `business_object_type` VARCHAR(32) NOT NULL COMMENT 'SubscriptionObjectType: ORDER,ASSIGNMENT,STATEMENT,DRIVER_APPLICATION；',
  `business_object_id` BIGINT NOT NULL COMMENT '',
  `status` VARCHAR(16) NOT NULL COMMENT 'SubscriptionPromptStatus: CREATED,REPORTED,EXPIRED；',
  `expires_at` DATETIME(3) NOT NULL COMMENT '',
  `reported_at` DATETIME(3) NULL COMMENT '',
  `client_report` JSON NULL COMMENT '微信前端结果不等于服务端真实剩余额度',
  `report_hash` VARCHAR(64) NULL COMMENT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_prompt_nonce (nonce_hash),
  KEY ix_prompt_account (account_id,created_at),
  CONSTRAINT ck_ver_subscription_prompt CHECK (version >= 0),
  CONSTRAINT ck_31c0259441de72 CHECK (`business_object_type` IN ('ORDER','ASSIGNMENT','STATEMENT','DRIVER_APPLICATION')),
  CONSTRAINT ck_dfa4511a54a323 CHECK (`status` IN ('CREATED','REPORTED','EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一次用户触发订阅请求与结果';

CREATE TABLE `vi_offline_payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `payment_id` BIGINT NOT NULL COMMENT '',
  `refund_id` BIGINT NULL COMMENT '仅REFUND有值，必须来自统一审核已批准执行单',
  `direction` VARCHAR(16) NOT NULL COMMENT 'OfflineProofDirection: COLLECTION,REFUND；',
  `owner_dept_id` BIGINT NOT NULL COMMENT '原支付收款主体快照',
  `amount_fen` BIGINT NOT NULL COMMENT '本期完整金额；不支持混合/客户拆单付款',
  `paid_at` DATETIME(3) NOT NULL COMMENT '真实线下发生时刻',
  `method` VARCHAR(24) NOT NULL COMMENT 'OfflineProofMethod: CASH,BANK_TRANSFER；',
  `external_reference_cipher` VARBINARY(1024) NOT NULL COMMENT '流水/收据编号密文',
  `reference_hmac` VARCHAR(64) NOT NULL COMMENT '主体+方向+方式+流水永久去重摘要',
  `voucher_file_ids` JSON NOT NULL COMMENT '私有文件，服务端核查归属与可用性',
  `status` VARCHAR(16) NOT NULL COMMENT 'OfflineProofStatus: SUBMITTED,VERIFIED,REJECTED；',
  `registered_by` BIGINT NOT NULL COMMENT '',
  `registered_at` DATETIME(3) NOT NULL COMMENT '',
  `verified_by` BIGINT NULL COMMENT '',
  `verified_at` DATETIME(3) NULL COMMENT '',
  `register_note` VARCHAR(500) NOT NULL COMMENT '',
  `verify_reason` VARCHAR(500) NULL COMMENT '',
  `active_payment` BIGINT GENERATED ALWAYS AS (IF(direction='COLLECTION' AND status IN ('SUBMITTED','VERIFIED'),payment_id,NULL)) STORED COMMENT '当前有效收款证明唯一占位',
  `active_refund` BIGINT GENERATED ALWAYS AS (IF(direction='REFUND' AND status IN ('SUBMITTED','VERIFIED'),refund_id,NULL)) STORED COMMENT '当前有效退款证明唯一占位',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_offline_reference (owner_dept_id,direction,method,reference_hmac),
  UNIQUE KEY uk_offline_payment (active_payment),
  UNIQUE KEY uk_offline_refund (active_refund),
  KEY ix_offline_status (owner_dept_id,status),
  CONSTRAINT ck_ver_offline_payment_record CHECK (version >= 0),
  CONSTRAINT ck_e240ee818b669d CHECK (`direction` IN ('COLLECTION','REFUND')),
  CONSTRAINT ck_1cc104f12b184a CHECK (`status` IN ('SUBMITTED','VERIFIED','REJECTED')),
  CONSTRAINT ck_538550895b553c CHECK (`method` IN ('CASH','BANK_TRANSFER')),
  CONSTRAINT ck_offline_amount CHECK (amount_fen>0),
  CONSTRAINT ck_offline_direction CHECK ((direction='COLLECTION' AND refund_id IS NULL) OR (direction='REFUND' AND refund_id IS NOT NULL)),
  CONSTRAINT ck_offline_reviewer CHECK (verified_by IS NULL OR verified_by<>registered_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户线下收退款证明与核实';

-- ---------------------------------------------------------------------------
-- B. vi_account：加列 / 约束调整 / 索引（空表，前向新增，不 DROP 表）
-- ---------------------------------------------------------------------------

ALTER TABLE `vi_account`
  ADD COLUMN `phone_verified_at` DATETIME(3) NULL COMMENT '服务端最后一次可信手机号验证UTC时刻',
  ADD COLUMN `phone_source` VARCHAR(24) NULL COMMENT 'PhoneProofSource: WECHAT,SMS；仅WECHAT可触发新员工关联',
  ADD COLUMN `employee_auto_link_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT '管理员解除后阻止该账号自动换绑；仅后台明确恢复，可不影响车主';

-- 替换旧约束（V6.4 语义/命名变更）：先 DROP 旧 CHECK
ALTER TABLE `vi_account` DROP CHECK `ck_66_592d8ece9cae`;

ALTER TABLE `vi_account` ADD CONSTRAINT ck_592d8ece9c CHECK (`status` IN ('ACTIVE','LOCKED'));

ALTER TABLE `vi_account` ADD CONSTRAINT ck_87f78b0fed CHECK (`phone_source` IN ('WECHAT','SMS'));

-- ---------------------------------------------------------------------------
-- C. vi_payment_order：加列 / 约束调整 / 索引（空表，前向新增，不 DROP 表）
-- ---------------------------------------------------------------------------

ALTER TABLE `vi_payment_order`
  ADD COLUMN `payment_method` VARCHAR(16) NOT NULL COMMENT 'PaymentMethod: UNSELECTED,WECHAT,OFFLINE；',
  ADD COLUMN `receiver_dept_id` BIGINT NOT NULL COMMENT '原始收款主体部门',
  ADD COLUMN `policy_revision_id` BIGINT NULL COMMENT '创建/选择时采用的配置版',
  ADD COLUMN `merchant_profile_id` BIGINT NULL COMMENT '仅微信',
  ADD COLUMN `merchant_revision_id` BIGINT NULL COMMENT '原资金身份快照版本',
  ADD COLUMN `app_id` VARCHAR(64) NULL COMMENT '',
  ADD COLUMN `mchid` VARCHAR(32) NULL COMMENT '',
  ADD COLUMN `method_locked_at` DATETIME(3) NULL COMMENT '首次有效预下单/线下意图时写入',
  ADD COLUMN `capacity_resolution` VARCHAR(24) NOT NULL COMMENT 'PaymentCapacityResolution: HELD,RELEASED,REACQUIRED,MANUAL_CONFLICT；';

-- 替换旧约束（V6.4 语义/命名变更）：先 DROP 旧 CHECK
ALTER TABLE `vi_payment_order` DROP CHECK `ck_66_a1f02dcffc3b`;

ALTER TABLE `vi_payment_order` ADD CONSTRAINT ck_3029b56e69d651 CHECK (`status` IN ('PENDING','REVIEW_PENDING','SUCCESS','CLOSED','UNKNOWN'));

ALTER TABLE `vi_payment_order` ADD CONSTRAINT ck_af21e8f90ed6d9 CHECK (`payment_method` IN ('UNSELECTED','WECHAT','OFFLINE'));

ALTER TABLE `vi_payment_order` ADD CONSTRAINT ck_51d858e7957115 CHECK (`capacity_resolution` IN ('HELD','RELEASED','REACQUIRED','MANUAL_CONFLICT'));

ALTER TABLE `vi_payment_order` ADD CONSTRAINT ck_pay_method_identity CHECK (payment_method<>'WECHAT' OR (merchant_profile_id IS NOT NULL AND merchant_revision_id IS NOT NULL AND app_id IS NOT NULL AND mchid IS NOT NULL));

-- ---------------------------------------------------------------------------
-- D. vi_refund_order：加列 / 约束调整 / 索引（空表，前向新增，不 DROP 表）
-- ---------------------------------------------------------------------------

ALTER TABLE `vi_refund_order`
  ADD COLUMN `payment_method` VARCHAR(16) NOT NULL COMMENT 'RefundPaymentMethod: WECHAT,OFFLINE；',
  ADD COLUMN `execution_mode` VARCHAR(24) NOT NULL COMMENT 'RefundExecutionMode: WECHAT_API,MANUAL_OFFLINE；';

ALTER TABLE `vi_refund_order` ADD CONSTRAINT ck_be46cf0b350473 CHECK (`payment_method` IN ('WECHAT','OFFLINE'));

ALTER TABLE `vi_refund_order` ADD CONSTRAINT ck_35feb49f221ec5 CHECK (`execution_mode` IN ('WECHAT_API','MANUAL_OFFLINE'));

-- ---------------------------------------------------------------------------
-- E. vi_employee_binding：加列 / 约束调整 / 索引（空表，前向新增，不 DROP 表）
-- ---------------------------------------------------------------------------

ALTER TABLE `vi_employee_binding`
  ADD COLUMN `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '关联应用命名空间',
  ADD COLUMN `source` VARCHAR(32) NOT NULL DEFAULT 'WECHAT_PHONE_MATCH' COMMENT 'EmployeeLinkSource: WECHAT_PHONE_MATCH',
  ADD COLUMN `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'EmployeeBindingLifecycle: ACTIVE,SUSPENDED,REVOKED',
  ADD COLUMN `phone_lookup` BINARY(32) NOT NULL COMMENT '关联时一致的规范化手机号HMAC；不是可还原裸手机号',
  ADD COLUMN `last_phone_verified_at` DATETIME(3) NOT NULL COMMENT '最近本人微信手机号核验时间',
  ADD COLUMN `employee_auth_version` BIGINT NOT NULL COMMENT '建立关联时的框架授权版本快照；每次工作请求仍检查当前员工状态和权限',
  ADD COLUMN `auto_rebind_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT '管理员撤销后拦截同应用同员工新自动关联；明确allow-rematch才解除',
  ADD COLUMN `suspended_reason` VARCHAR(64) NULL COMMENT '手机号变更、账号停用等失效原因；不泄漏其他账号',
  ADD COLUMN `revoked_by` BIGINT NULL COMMENT '后台受控撤销操作者',
  ADD COLUMN `revoked_reason` VARCHAR(500) NULL COMMENT '解除依据';

ALTER TABLE `vi_employee_binding` ADD CONSTRAINT ck_0322202dab CHECK (`source` IN ('WECHAT_PHONE_MATCH'));

ALTER TABLE `vi_employee_binding` ADD CONSTRAINT ck_3c0e2a701f CHECK (`status` IN ('ACTIVE','SUSPENDED','REVOKED'));

ALTER TABLE `vi_employee_binding` ADD CONSTRAINT ck_employee_link_revoke CHECK ((status='REVOKED' AND revoked_at IS NOT NULL) OR (status IN ('ACTIVE','SUSPENDED') AND revoked_at IS NULL));

ALTER TABLE `vi_employee_binding` ADD CONSTRAINT ck_employee_link_source_actor CHECK (source='WECHAT_PHONE_MATCH' AND bound_by IS NULL);

ALTER TABLE `vi_employee_binding`
  ADD KEY ix_employee_link_phone (phone_lookup),
  ADD KEY ix_employee_link_block (app_id,sys_user_id,auto_rebind_blocked);

-- ---------------------------------------------------------------------------
-- F. 退役表标注（V6.4 移除员工绑定申请/凭证链；保留历史，不 DROP，应用层停写旧接口）
--    vi_employee_binding_request / vi_employee_binding_credential：
--    新安装（完整初始化 ry_20260417.sql）不再创建这两张表；已存在的库保留其结构与历史数据，
--    旧 employee-binding/request/credential 写接口从主 OpenAPI 与任务移除、入口显式拒绝。
--    此处不改其结构，仅记录退役事实（表注释）。
-- ---------------------------------------------------------------------------
ALTER TABLE `vi_employee_binding_request`
  COMMENT = '[V6.4退役] 员工绑定申请链已废弃；保留历史不DROP，应用层停写，改用手机号自动关联';
ALTER TABLE `vi_employee_binding_credential`
  COMMENT = '[V6.4退役] 员工绑定凭证链已废弃；保留历史不DROP，应用层停写，改用后台直接预授站点';

-- =============================================================================
-- 执行后校验（人工/脚本核对，非本脚本自动执行）：
--   1) SELECT COUNT(*) FROM information_schema.tables
--        WHERE table_schema='xinyi' AND table_name LIKE 'vi\_%';  -- 期望 79 + 2 退役 = 81
--   2) 15 新表逐一存在；4 表新列存在；vi_payment_order.status CHECK 含 REVIEW_PENDING；
--      vi_employee_binding 有 ix_employee_link_phone / ix_employee_link_block。
--   3) 与完整初始化 ry_20260417.sql 结果比对字段/索引/生成列/约束/排序规则一致（退役 2 表除外）。
--
-- 回滚说明（仅在需要撤销本次适配时，逆序手动执行；会丢失新增结构，不影响既有数据）：
--   ALTER TABLE vi_employee_binding DROP KEY ix_employee_link_block, DROP KEY ix_employee_link_phone;
--   ALTER TABLE <t> DROP CHECK <新增约束名>;  -- 逐表逐约束
--   ALTER TABLE vi_payment_order ADD CONSTRAINT ck_66_a1f02dcffc3b
--     CHECK (status IN ('PENDING','SUCCESS','CLOSED','UNKNOWN'));  -- 恢复旧枚举(须先无REVIEW_PENDING行)
--   ALTER TABLE vi_account ADD CONSTRAINT ck_66_592d8ece9cae CHECK (status IN ('ACTIVE','LOCKED'));
--   ALTER TABLE <t> DROP COLUMN <新增列>;  -- 逐表逐列
--   DROP TABLE <15 新表>;  -- 逆序
--   或直接从 .vi-local/backup/xinyi_pre_v64_*.sql 整库恢复。
-- =============================================================================
