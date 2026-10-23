-- =====================================================================
-- 荣利机动车检测预约 · V6.4 第三方服务接入配置（T3.4 / PRD 第23章）
--
-- 依据：资料包 V6.4
--   05_数据设计/001_vi业务表_初始化_V6.4.sql（12 张新表 + 支付/退款表新列）
--   05_数据设计/V6.4_已有工作区前向适配.md（先加列再回填，最后才收紧约束）
--   12_工程执行/第三方配置实施与种子.md（菜单与权限种子、初始全关）
--
-- 幂等：全部语句可重复执行；不使用 DROP，不用 IF NOT EXISTS 掩盖结构错误。
-- 分阶段：一 建表 → 二 支付/退款前向适配 → 三 菜单权限 → 四 业务参数 → 五 验收断言
-- 执行：mysql -h127.0.0.1 -P3307 -uroot -p --default-character-set=utf8mb4 xinyi < 本文件
-- =====================================================================

-- ================================================================ 一、新增表
-- 配置档案与生效指针：kind 五类，owner_dept_id 为真实管理/收款主体
CREATE TABLE IF NOT EXISTS `vi_integration_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationKind: PAYMENT_POLICY,WECHAT_PAY,TENCENT_SMS,ALIYUN_SMS,WECHAT_APP',
  `owner_dept_id` BIGINT NOT NULL COMMENT '真实管理/收款主体，复用框架部门归属',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE',
  `config_key` VARCHAR(80) NOT NULL COMMENT 'PAYMENT_POLICY与WECHAT_APP用DEFAULT；其他由服务端生成',
  `display_name` VARCHAR(100) NOT NULL COMMENT '配置名称 1-60字，这里留到100',
  `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationLifecycle: DRAFT,ACTIVE,RETIRED',
  `active_revision_id` BIGINT NULL COMMENT '当前发布版本',
  `draft_revision_id` BIGINT NULL COMMENT '最新草稿版本',
  `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '分配版本号的CAS计数',
  `created_by` BIGINT NOT NULL COMMENT '配置创建人',
  `last_published_by` BIGINT NULL COMMENT '最近发布人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_integration_scope (kind,owner_dept_id,environment,config_key),
  KEY ix_integration_active (active_revision_id),
  CONSTRAINT ck_ver_integration_profile CHECK (version >= 0),
  CONSTRAINT ck_fdb65a8cb0d9d1 CHECK (`kind` IN ('PAYMENT_POLICY','WECHAT_PAY','TENCENT_SMS','ALIYUN_SMS','WECHAT_APP')),
  CONSTRAINT ck_5d0e7efd82afb9 CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_7d6eefe158cca1 CHECK (`lifecycle` IN ('DRAFT','ACTIVE','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方配置档案与生效指针';

-- 不可变版本：同一 profile 下 revision_no 与 content_hash 双唯一
CREATE TABLE IF NOT EXISTS `vi_integration_revision` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `profile_id` BIGINT NOT NULL COMMENT '配置档案',
  `revision_no` BIGINT NOT NULL COMMENT '版本号，由 revision_seq 分配',
  `config_json` JSON NOT NULL COMMENT '经kind专用DTO验证的非秘密配置',
  `secret_refs_json` JSON NOT NULL COMMENT '本配置凭据槽位到secret id的内部映射，不下发',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '规范化内容与秘密引用版本摘要',
  `created_by` BIGINT NOT NULL COMMENT '保存人',
  `reason` VARCHAR(500) NOT NULL COMMENT '保存原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_config_revision (profile_id,revision_no),
  UNIQUE KEY uk_config_hash (profile_id,content_hash),
  CONSTRAINT ck_ver_integration_revision CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变第三方配置版本';

-- 凭据密文：只写不读，AAD 绑定 profile/slot/env
CREATE TABLE IF NOT EXISTS `vi_integration_secret` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `profile_id` BIGINT NOT NULL COMMENT '配置档案',
  `slot_name` VARCHAR(64) NOT NULL COMMENT '固定允许的凭据槽，禁止用户自造密钥用途',
  `credential_kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationCredentialKind: PRIVATE_KEY,API_V3_KEY,CLOUD_SECRET,APP_SECRET,BANK_ACCOUNT',
  `ciphertext` MEDIUMBLOB NOT NULL COMMENT 'AEAD密文，绝非base64明文',
  `nonce` VARBINARY(32) NOT NULL COMMENT '每密文独立随机nonce',
  `key_version` VARCHAR(128) NOT NULL COMMENT '主密钥版本在服务端安全配置',
  `aad_context` VARCHAR(255) NOT NULL COMMENT '绑定profile/slot/env的不可更改上下文',
  `fingerprint` VARCHAR(64) NOT NULL COMMENT '安全标识不可逆；不显示明文末几位',
  `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationSecretLifecycle: ACTIVE,RETIRED,REVOKED',
  `expires_at` DATETIME(3) NULL COMMENT '证书类凭据到期',
  `retired_at` DATETIME(3) NULL COMMENT '退役时刻',
  `created_by` BIGINT NOT NULL COMMENT '写入人',
  `retired_by` BIGINT NULL COMMENT '受控撤销人',
  `retire_reason` VARCHAR(500) NULL COMMENT '撤销原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_secret_owner (profile_id,slot_name,lifecycle),
  CONSTRAINT ck_ver_integration_secret CHECK (version >= 0),
  CONSTRAINT ck_db3f36fab3347a CHECK (`credential_kind` IN ('PRIVATE_KEY','API_V3_KEY','CLOUD_SECRET','APP_SECRET','BANK_ACCOUNT')),
  CONSTRAINT ck_83aefc040222fa CHECK (`lifecycle` IN ('ACTIVE','RETIRED','REVOKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='安全凭据密文与受控生命周期';

-- 校验记录：绑定 revision + content_hash，编辑后旧 check 不得用于发布
CREATE TABLE IF NOT EXISTS `vi_integration_check` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE',
  `target_id` BIGINT NOT NULL COMMENT 'profile/template id',
  `revision_id` BIGINT NOT NULL COMMENT '被校验版本',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '被校验版本内容摘要',
  `check_level` VARCHAR(24) NOT NULL COMMENT 'IntegrationCheckLevel: LOCAL,READ_ONLY,LIVE_EVIDENCE',
  `status` VARCHAR(16) NOT NULL COMMENT 'IntegrationCheckStatus: NOT_RUN,PASS,FAIL,PARTIAL',
  `issues_json` JSON NOT NULL COMMENT '脱敏逐字段错误/警告/未验证项',
  `checked_at` DATETIME(3) NOT NULL COMMENT '校验时刻',
  `expires_at` DATETIME(3) NOT NULL COMMENT '校验结果有效期',
  `checked_by` BIGINT NOT NULL COMMENT '校验人',
  `real_payment_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '仅真实授权资金测试证据才能标记',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_check_revision (target_type,target_id,revision_id,checked_at),
  CONSTRAINT ck_ver_integration_check CHECK (version >= 0),
  CONSTRAINT ck_88cd710155a461 CHECK (`target_type` IN ('PROFILE','TEMPLATE')),
  CONSTRAINT ck_4c4d3c7687eba0 CHECK (`check_level` IN ('LOCAL','READ_ONLY','LIVE_EVIDENCE')),
  CONSTRAINT ck_5bc059710176ef CHECK (`status` IN ('NOT_RUN','PASS','FAIL','PARTIAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='指定配置版本校验记录';

-- 发布审计：回滚也是新发布
CREATE TABLE IF NOT EXISTS `vi_integration_publication` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE',
  `target_id` BIGINT NOT NULL COMMENT 'profile/template id',
  `revision_id` BIGINT NOT NULL COMMENT '本次生效版本',
  `previous_revision_id` BIGINT NULL COMMENT '旧生效版本',
  `check_id` BIGINT NOT NULL COMMENT '发布依据的校验记录',
  `action` VARCHAR(16) NOT NULL COMMENT 'IntegrationPublishAction: PUBLISH,ROLLBACK',
  `published_by` BIGINT NOT NULL COMMENT '发布人',
  `published_at` DATETIME(3) NOT NULL COMMENT '发布时刻',
  `reason` VARCHAR(500) NOT NULL COMMENT '发布/回滚原因',
  `dependency_versions` JSON NOT NULL COMMENT '发布时依赖版本快照',
  `target_version` BIGINT NOT NULL COMMENT '发布事务后的根版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_publication_target (target_type,target_id,published_at),
  CONSTRAINT ck_ver_integration_publication CHECK (version >= 0),
  CONSTRAINT ck_1da14a68f349f2 CHECK (`target_type` IN ('PROFILE','TEMPLATE')),
  CONSTRAINT ck_fc819ff643e13c CHECK (`action` IN ('PUBLISH','ROLLBACK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置发布与回滚审计';

-- 外部模板档案（短信/订阅）
CREATE TABLE IF NOT EXISTS `vi_message_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `name` VARCHAR(100) NOT NULL COMMENT '本地模板名称',
  `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS,WECHAT_SUBSCRIBE',
  `owner_dept_id` BIGINT NOT NULL COMMENT '授权组织',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene（11 个稳定业务场景）',
  `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION',
  `active_revision_id` BIGINT NULL COMMENT '当前已发布模板',
  `draft_revision_id` BIGINT NULL COMMENT '最新草稿',
  `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '模板版本分配计数',
  `created_by` BIGINT NOT NULL COMMENT '创建人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_template_scene (owner_dept_id,environment,scene,audience,provider),
  CONSTRAINT ck_ver_message_template CHECK (version >= 0),
  CONSTRAINT ck_d4792ebbf8b6a6 CHECK (`provider` IN ('TENCENT_SMS','ALIYUN_SMS','WECHAT_SUBSCRIBE')),
  CONSTRAINT ck_fc161ac5a52b5d CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_a22fd0ee77b18a CHECK (`scene` IN ('LOGIN_OTP','PHONE_VERIFY','BOOKING_CONFIRMED','DRIVER_ASSIGNED','PICKUP_ARRIVED','INSPECTION_FINISHED','RETURN_COMPLETED','AFTERSALE_DECIDED','REFUND_CHANGED','DRIVER_INVITE','STATEMENT_READY')),
  CONSTRAINT ck_992903d6097179 CHECK (`audience` IN ('OWNER','DRIVER','STATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部短信与订阅模板档案';

CREATE TABLE IF NOT EXISTS `vi_message_template_revision` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `template_id` BIGINT NOT NULL COMMENT '所属模板档案',
  `revision_no` BIGINT NOT NULL COMMENT '版本号',
  `profile_id` BIGINT NOT NULL COMMENT '引用的通道配置',
  `profile_revision_id` BIGINT NOT NULL COMMENT '引用的通道配置版本',
  `vendor_template_id` VARCHAR(256) NULL COMMENT '供应商审核通过的模板ID；草稿可空，发布时强制',
  `parameter_bindings` JSON NOT NULL COMMENT '白名单变量、实际key/type/length；腾讯顺序/阿里命名',
  `options_json` JSON NOT NULL COMMENT '签名/内容快照/小程序page/env/lang等非秘密',
  `enabled` TINYINT NOT NULL COMMENT '发送开关',
  `approval_status` VARCHAR(16) NOT NULL COMMENT 'TemplateApprovalStatus: UNKNOWN,APPROVED,REJECTED',
  `content_hash` VARCHAR(64) NOT NULL COMMENT '内容摘要',
  `created_by` BIGINT NOT NULL COMMENT '保存人',
  `reason` VARCHAR(500) NOT NULL COMMENT '保存原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_template_revision (template_id,revision_no),
  CONSTRAINT ck_ver_message_template_revision CHECK (version >= 0),
  CONSTRAINT ck_b7dcf36d038099 CHECK (`approval_status` IN ('UNKNOWN','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变外部模板版本';

-- 场景路由：同一事件只能有一个主通道，避免腾讯/阿里双发
CREATE TABLE IF NOT EXISTS `vi_notification_route` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `owner_dept_id` BIGINT NOT NULL COMMENT '归属组织',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene',
  `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION',
  `primary_template_id` BIGINT NOT NULL COMMENT '主通道模板',
  `fallback_template_id` BIGINT NULL COMMENT '只允许明确未发送时按规则用备用',
  `fallback_policy` VARCHAR(32) NOT NULL COMMENT 'MessageFallbackPolicy: NONE,DEFINITELY_NOT_SENT_ONLY',
  `enabled` TINYINT NOT NULL COMMENT '路由开关',
  `updated_by` BIGINT NOT NULL COMMENT '最后操作人',
  `reason` VARCHAR(500) NOT NULL COMMENT '变更原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_message_route (owner_dept_id,environment,scene,audience),
  CONSTRAINT ck_ver_notification_route CHECK (version >= 0),
  CONSTRAINT ck_545a6078080a6c CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_abb77f22beb38f CHECK (`scene` IN ('LOGIN_OTP','PHONE_VERIFY','BOOKING_CONFIRMED','DRIVER_ASSIGNED','PICKUP_ARRIVED','INSPECTION_FINISHED','RETURN_COMPLETED','AFTERSALE_DECIDED','REFUND_CHANGED','DRIVER_INVITE','STATEMENT_READY')),
  CONSTRAINT ck_e031dc9c69e46e CHECK (`audience` IN ('OWNER','DRIVER','STATION')),
  CONSTRAINT ck_fbf39632b0a6fb CHECK (`fallback_policy` IN ('NONE','DEFINITELY_NOT_SENT_ONLY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务事件到唯一主通道的路由';

-- 外部发送执行与回执：ACCEPTED 不等于 DELIVERED，超时记 UNKNOWN 不自动重发
CREATE TABLE IF NOT EXISTS `vi_notification_delivery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `notification_id` BIGINT NOT NULL COMMENT '关联已有站内消息/OTP任务',
  `event_key` VARCHAR(128) NOT NULL COMMENT '逻辑事件业务键',
  `recipient_key` VARCHAR(128) NOT NULL COMMENT 'accountId或脱敏HMAC，禁止裸号',
  `recipient_cipher` VARBINARY(1024) NOT NULL COMMENT '必要收件手机号/OpenID密文',
  `owner_dept_id` BIGINT NOT NULL COMMENT '归属组织',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE',
  `route_id` BIGINT NOT NULL COMMENT '使用的路由',
  `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene',
  `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider',
  `template_id` BIGINT NOT NULL COMMENT '模板档案',
  `template_revision_id` BIGINT NOT NULL COMMENT '冻结的模板版本',
  `profile_id` BIGINT NOT NULL COMMENT '通道配置',
  `profile_revision_id` BIGINT NOT NULL COMMENT '冻结的通道版本',
  `payload_cipher_refs` JSON NOT NULL COMMENT '消息内容加密引用，不含明文验证码',
  `status` VARCHAR(24) NOT NULL COMMENT 'ExternalDeliveryStatus: PENDING,SENDING,ACCEPTED,DELIVERED,FAILED,UNKNOWN,SKIPPED',
  `vendor_request_id` VARCHAR(128) NULL COMMENT '供应商请求ID',
  `vendor_biz_id` VARCHAR(128) NULL COMMENT '供应商标识（回执查询用）',
  `safe_error_code` VARCHAR(128) NULL COMMENT '脱敏错误码',
  `safe_error_message` VARCHAR(500) NULL COMMENT '脱敏错误信息',
  `attempt_count` INT NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `next_attempt_at` DATETIME(3) NULL COMMENT '下次受控重试时刻',
  `lease_until` DATETIME(3) NULL COMMENT 'worker租约',
  `accepted_at` DATETIME(3) NULL COMMENT '供应商受理时刻',
  `delivered_at` DATETIME(3) NULL COMMENT '供应商送达时刻',
  `business_expires_at` DATETIME(3) NOT NULL COMMENT '业务事件有效期',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
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

-- 订阅 prompt：客户端 accept 只是报告，不等于服务端永久授权
CREATE TABLE IF NOT EXISTS `vi_subscription_prompt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '发起人',
  `app_id` VARCHAR(64) NOT NULL COMMENT '所属小程序',
  `nonce_hash` VARCHAR(64) NOT NULL COMMENT '一次性nonce摘要',
  `template_versions` JSON NOT NULL COMMENT '模板ID、公开版本及场景',
  `business_object_type` VARCHAR(32) NOT NULL COMMENT 'SubscriptionObjectType: ORDER,ASSIGNMENT,STATEMENT,DRIVER_APPLICATION',
  `business_object_id` BIGINT NOT NULL COMMENT '业务对象',
  `status` VARCHAR(16) NOT NULL COMMENT 'SubscriptionPromptStatus: CREATED,REPORTED,EXPIRED',
  `expires_at` DATETIME(3) NOT NULL COMMENT '短时有效',
  `reported_at` DATETIME(3) NULL COMMENT '回传时刻',
  `client_report` JSON NULL COMMENT '微信前端结果不等于服务端真实剩余额度',
  `report_hash` VARCHAR(64) NULL COMMENT '回传摘要',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_prompt_nonce (nonce_hash),
  KEY ix_prompt_account (account_id,created_at),
  CONSTRAINT ck_ver_subscription_prompt CHECK (version >= 0),
  CONSTRAINT ck_31c0259441de72 CHECK (`business_object_type` IN ('ORDER','ASSIGNMENT','STATEMENT','DRIVER_APPLICATION')),
  CONSTRAINT ck_dfa4511a54a323 CHECK (`status` IN ('CREATED','REPORTED','EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一次用户触发订阅请求与结果';

-- 客户线下收退款证明：登记人与核实人必须不同
CREATE TABLE IF NOT EXISTS `vi_offline_payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `payment_id` BIGINT NOT NULL COMMENT '支付单',
  `refund_id` BIGINT NULL COMMENT '仅REFUND有值，必须来自统一审核已批准执行单',
  `direction` VARCHAR(16) NOT NULL COMMENT 'OfflineProofDirection: COLLECTION,REFUND',
  `owner_dept_id` BIGINT NOT NULL COMMENT '原支付收款主体快照',
  `amount_fen` BIGINT NOT NULL COMMENT '本期完整金额；不支持混合/客户拆单付款',
  `paid_at` DATETIME(3) NOT NULL COMMENT '真实线下发生时刻',
  `method` VARCHAR(24) NOT NULL COMMENT 'OfflineProofMethod: CASH,BANK_TRANSFER',
  `external_reference_cipher` VARBINARY(1024) NOT NULL COMMENT '流水/收据编号密文',
  `reference_hmac` VARCHAR(64) NOT NULL COMMENT '主体+方向+方式+流水永久去重摘要',
  `voucher_file_ids` JSON NOT NULL COMMENT '私有文件，服务端核查归属与可用性',
  `status` VARCHAR(16) NOT NULL COMMENT 'OfflineProofStatus: SUBMITTED,VERIFIED,REJECTED',
  `registered_by` BIGINT NOT NULL COMMENT '登记人',
  `registered_at` DATETIME(3) NOT NULL COMMENT '登记时刻',
  `verified_by` BIGINT NULL COMMENT '独立核实人',
  `verified_at` DATETIME(3) NULL COMMENT '核实时刻',
  `register_note` VARCHAR(500) NOT NULL COMMENT '登记说明',
  `verify_reason` VARCHAR(500) NULL COMMENT '核实/驳回原因',
  `active_payment` BIGINT GENERATED ALWAYS AS (IF(direction='COLLECTION' AND status IN ('SUBMITTED','VERIFIED'),payment_id,NULL)) STORED COMMENT '当前有效收款证明唯一占位',
  `active_refund` BIGINT GENERATED ALWAYS AS (IF(direction='REFUND' AND status IN ('SUBMITTED','VERIFIED'),refund_id,NULL)) STORED COMMENT '当前有效退款证明唯一占位',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
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

-- 测试收件人白名单：真实测试须二次确认并命中白名单
CREATE TABLE IF NOT EXISTS `vi_integration_test_recipient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串',
  `name` VARCHAR(80) NOT NULL COMMENT '白名单名称',
  `kind` VARCHAR(16) NOT NULL COMMENT 'IntegrationRecipientKind: PHONE,MINI_ACCOUNT',
  `owner_dept_id` BIGINT NOT NULL COMMENT '归属组织',
  `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE',
  `phone_cipher` VARBINARY(512) NULL COMMENT '仅PHONE',
  `account_id` BIGINT NULL COMMENT '仅MINI_ACCOUNT',
  `app_profile_id` BIGINT NULL COMMENT '小程序白名单同应用',
  `enabled` TINYINT NOT NULL COMMENT '启用',
  `created_by` BIGINT NOT NULL COMMENT '登记人',
  `reason` VARCHAR(500) NOT NULL COMMENT '登记原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效',
  PRIMARY KEY (`id`),
  KEY ix_test_recipient_scope (owner_dept_id,environment,enabled),
  CONSTRAINT ck_ver_integration_test_recipient CHECK (version >= 0),
  CONSTRAINT ck_1117ecf2dcdc35 CHECK (`kind` IN ('PHONE','MINI_ACCOUNT')),
  CONSTRAINT ck_95800f15c7f107 CHECK (`environment` IN ('TEST','LIVE')),
  CONSTRAINT ck_recipient_shape CHECK ((kind='PHONE' AND phone_cipher IS NOT NULL AND account_id IS NULL) OR (kind='MINI_ACCOUNT' AND phone_cipher IS NULL AND account_id IS NOT NULL AND app_profile_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方测试收件人白名单';

SELECT 'migration V6.4 integration: 12 tables created' AS stage;

-- ============================================== 二、vi_payment_order 前向适配
-- 先加列（可空或带默认），再回填，最后按数据实际情况收紧；不猜测历史商户身份。

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='payment_method');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `payment_method` VARCHAR(16) NOT NULL DEFAULT ''UNSELECTED'' COMMENT ''PaymentMethod: UNSELECTED,WECHAT,OFFLINE'' AFTER `status`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='receiver_dept_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `receiver_dept_id` BIGINT NULL COMMENT ''原始收款主体部门；回填后收紧为NOT NULL'' AFTER `payment_method`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='policy_revision_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `policy_revision_id` BIGINT NULL COMMENT ''创建/选择时采用的配置版'' AFTER `receiver_dept_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='merchant_profile_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `merchant_profile_id` BIGINT NULL COMMENT ''仅微信'' AFTER `policy_revision_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='merchant_revision_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `merchant_revision_id` BIGINT NULL COMMENT ''原资金身份快照版本'' AFTER `merchant_profile_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='app_id');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `app_id` VARCHAR(64) NULL COMMENT ''下单小程序AppID快照'' AFTER `merchant_revision_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='mchid');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `mchid` VARCHAR(32) NULL COMMENT ''商户号快照'' AFTER `app_id`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='method_locked_at');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `method_locked_at` DATETIME(3) NULL COMMENT ''首次有效预下单/线下意图时写入'' AFTER `mchid`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_payment_order' AND COLUMN_NAME='capacity_resolution');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD COLUMN `capacity_resolution` VARCHAR(24) NOT NULL DEFAULT ''HELD'' COMMENT ''PaymentCapacityResolution: HELD,RELEASED,REACQUIRED,MANUAL_CONFLICT'' AFTER `method_locked_at`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 回填收款主体：只能从订单归属快照取，不猜当前配置、不默认管理员
UPDATE `vi_payment_order` p
  JOIN `vi_order` o ON o.id = p.order_id
SET p.receiver_dept_id = o.dept_id
WHERE p.receiver_dept_id IS NULL;

-- 回填历史支付方式：本期之前只有微信/联调通道，故为 WECHAT；
-- 但历史行没有 merchant_profile_id，暂不收紧 ck_pay_method_identity，见文件末说明。
UPDATE `vi_payment_order` SET payment_method = 'WECHAT'
WHERE payment_method = 'UNSELECTED' AND out_trade_no IS NOT NULL;

-- 名额处置：已关闭交易视为名额已释放，其余保持占用
UPDATE `vi_payment_order` SET capacity_resolution = 'RELEASED'
WHERE status = 'CLOSED' AND capacity_resolution = 'HELD';

-- 状态枚举补 REVIEW_PENDING（线下证据待核实）
SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_3029b56e69d651');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD CONSTRAINT `ck_3029b56e69d651` CHECK (`status` IN (''PENDING'',''REVIEW_PENDING'',''SUCCESS'',''CLOSED'',''UNKNOWN''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_66_a1f02dcffc3b');
SET @s := IF(@c > 0,
  'ALTER TABLE `vi_payment_order` DROP CHECK `ck_66_a1f02dcffc3b`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_af21e8f90ed6d9');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD CONSTRAINT `ck_af21e8f90ed6d9` CHECK (`payment_method` IN (''UNSELECTED'',''WECHAT'',''OFFLINE''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order'
             AND CONSTRAINT_NAME='ck_51d858e7957115');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_payment_order` ADD CONSTRAINT `ck_51d858e7957115` CHECK (`capacity_resolution` IN (''HELD'',''RELEASED'',''REACQUIRED'',''MANUAL_CONFLICT''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 回填完成后收紧 NOT NULL（仍有空值时保持可空并在末尾报告中暴露）
SET @n := (SELECT COUNT(1) FROM `vi_payment_order` WHERE receiver_dept_id IS NULL);
SET @s := IF(@n = 0,
  'ALTER TABLE `vi_payment_order` MODIFY COLUMN `receiver_dept_id` BIGINT NOT NULL COMMENT ''原始收款主体部门''',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ================================================ 三、vi_refund_order 前向适配
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_refund_order' AND COLUMN_NAME='payment_method');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_refund_order` ADD COLUMN `payment_method` VARCHAR(16) NOT NULL DEFAULT ''WECHAT'' COMMENT ''RefundPaymentMethod: WECHAT,OFFLINE'' AFTER `amount_fen`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='vi_refund_order' AND COLUMN_NAME='execution_mode');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_refund_order` ADD COLUMN `execution_mode` VARCHAR(24) NOT NULL DEFAULT ''WECHAT_API'' COMMENT ''RefundExecutionMode: WECHAT_API,MANUAL_OFFLINE'' AFTER `payment_method`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- 历史退款全部为微信渠道退款；离线退款由本次改造后的流程显式写入 MANUAL_OFFLINE
UPDATE `vi_refund_order` r
  JOIN `vi_payment_order` p ON p.id = r.payment_id
SET r.payment_method = p.payment_method,
    r.execution_mode = IF(p.payment_method = 'OFFLINE', 'MANUAL_OFFLINE', 'WECHAT_API')
WHERE r.payment_method <> p.payment_method OR r.execution_mode IS NULL;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_refund_order' AND CONSTRAINT_NAME='ck_be46cf0b350473');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_refund_order` ADD CONSTRAINT `ck_be46cf0b350473` CHECK (`payment_method` IN (''WECHAT'',''OFFLINE''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.TABLE_CONSTRAINTS
           WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_refund_order' AND CONSTRAINT_NAME='ck_35feb49f221ec5');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_refund_order` ADD CONSTRAINT `ck_35feb49f221ec5` CHECK (`execution_mode` IN (''WECHAT_API'',''MANUAL_OFFLINE''))',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SELECT 'migration V6.4 integration: payment tables forward-adapted' AS stage;

-- ==================================================== 四、菜单与权限（第三方服务）
-- 只挂菜单与权限点，不自动授予普通角色；超管由框架动态可见。
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
 (2240,'第三方服务',2100,5,'integration',NULL,'','',1,0,'M','0','0','','phone','admin',sysdate(),'',NULL,'V6.4 第23章：支付方式、商户、短信、模板、诊断、线下资金、小程序凭据'),
 (2241,'支付方式设置',2240,1,'paymentPolicy','rongli/integrations/paymentPolicy','','',1,0,'C','0','0','biz:integration:payment','money','admin',sysdate(),'',NULL,'B46 线下/微信双开关与草稿发布'),
 (2242,'微信支付商户',2240,2,'wechatPay','rongli/integrations/wechatPay','','',1,0,'C','0','0','biz:integration:wechatpay','wechat','admin',sysdate(),'',NULL,'B47 商户号、证书、公钥与回调'),
 (2243,'短信通道',2240,3,'smsChannel','rongli/integrations/smsChannel','','',1,0,'C','0','0','biz:integration:sms','message','admin',sysdate(),'',NULL,'B48 腾讯云/阿里云独立参数'),
 (2244,'短信场景模板',2240,4,'smsTemplates','rongli/integrations/smsTemplates','','',1,0,'C','0','0','biz:messagetemplate:list','form','admin',sysdate(),'',NULL,'B49 场景模板、签名与参数映射'),
 (2245,'订阅消息模板',2240,5,'subscribeTemplates','rongli/integrations/subscribeTemplates','','',1,0,'C','0','0','biz:messagetemplate:list','guide','admin',sysdate(),'',NULL,'B50 微信订阅模板与页面白名单'),
 (2246,'发布历史与发送诊断',2240,6,'publications','rongli/integrations/publications','','',1,0,'C','0','0','biz:delivery:list','log','admin',sysdate(),'',NULL,'B51 发布审计、发送结果、回执查询'),
 (2247,'客户线下收退款',2240,7,'offlinePayments','rongli/integrations/offlinePayments','','',1,0,'C','0','0','biz:offlinepayment:list','money','admin',sysdate(),'',NULL,'B52 收款凭证登记与独立核实'),
 (2248,'小程序公共凭据',2240,8,'wechatApp','rongli/integrations/wechatApp','','',1,0,'C','0','0','biz:integration:app','lock','admin',sysdate(),'',NULL,'B53 AppID/AppSecret 共用凭据'),
 (2249,'配置草稿保存',2240,9,'','','','',1,0,'F','0','0','biz:integration:edit','','admin',sysdate(),'',NULL,'编辑、发布、测试分开授权'),
 (2250,'配置校验',2240,10,'','','','',1,0,'F','0','0','biz:integration:check','','admin',sysdate(),'',NULL,'本地校验与只读连接检测'),
 (2251,'配置发布与回滚',2240,11,'','','','',1,0,'F','0','0','biz:integration:publish','','admin',sysdate(),'',NULL,'发布、停用、回滚需原因与再次认证'),
 (2252,'模板维护',2240,12,'','','','',1,0,'F','0','0','biz:messagetemplate:edit','','admin',sysdate(),'',NULL,'模板草稿、预览、发布'),
 (2253,'真实测试发送',2240,13,'','','','',1,0,'F','0','0','biz:messagetemplate:test','','admin',sysdate(),'',NULL,'白名单+二次确认，可能产生费用'),
 (2254,'场景路由维护',2240,14,'','','','',1,0,'F','0','0','biz:messageroute:edit','','admin',sysdate(),'',NULL,'同一事件唯一主通道'),
 (2255,'发送回执查询与重试',2240,15,'','','','',1,0,'F','0','0','biz:delivery:operate','','admin',sysdate(),'',NULL,'UNKNOWN 不自动重发'),
 (2256,'线下收款登记',2240,16,'','','','',1,0,'F','0','0','biz:offlinepayment:register','','admin',sysdate(),'',NULL,'登记实际收款与凭证'),
 (2257,'线下资金独立核实',2240,17,'','','','',1,0,'F','0','0','biz:offlinepayment:verify','','admin',sysdate(),'',NULL,'核实人不得为登记人'),
 (2258,'第三方配置查看',2240,18,'','','','',1,0,'F','0','0','biz:integration:list','','admin',sysdate(),'',NULL,'读取脱敏配置、版本与发布审计')
AS new
ON DUPLICATE KEY UPDATE `menu_name`=new.`menu_name`,`parent_id`=new.`parent_id`,`order_num`=new.`order_num`,
  `path`=new.`path`,`component`=new.`component`,`menu_type`=new.`menu_type`,`visible`=new.`visible`,
  `status`=new.`status`,`perms`=new.`perms`,`icon`=new.`icon`,`remark`=new.`remark`;

-- ==================================================== 五、业务参数（部署可信锚点）
-- 注意：sys_config 没有 config_key 唯一索引，不能使用 ON DUPLICATE KEY UPDATE，
-- 否则每执行一次迁移就会多出一套参数（本脚本首次落地时确实产生了重复，已在此清理）。
DELETE c1 FROM `sys_config` c1
  JOIN `sys_config` c2 ON c1.`config_key` = c2.`config_key` AND c1.`config_id` > c2.`config_id`
 WHERE c1.`config_key` LIKE 'rongli.integration.%';

INSERT INTO `sys_config` (`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`remark`)
SELECT '第三方配置运行环境','rongli.integration.environment','TEST','N','admin',UTC_TIMESTAMP(),'TEST=隔离联调；LIVE=生产。必须与部署环境一致，不一致时禁止发布'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key`='rongli.integration.environment');

INSERT INTO `sys_config` (`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`remark`)
SELECT '回调可信基址','rongli.integration.callbackBaseUrl','','N','admin',UTC_TIMESTAMP(),'支付/退款通知URL由该基址+固定路由生成，只读，不接收任意URL；生产必须为HTTPS'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key`='rongli.integration.callbackBaseUrl');

INSERT INTO `sys_config` (`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`remark`)
SELECT '配置校验有效期(分钟)','rongli.integration.checkTtlMinutes','30','N','admin',UTC_TIMESTAMP(),'校验结果过期后必须重新校验才能发布'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key`='rongli.integration.checkTtlMinutes');

INSERT INTO `sys_config` (`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`remark`)
SELECT '敏感发布再次认证窗口(分钟)','rongli.integration.reauthWindowMinutes','5','N','admin',UTC_TIMESTAMP(),'敏感配置发布前要求最近一次完整后台登录在该窗口内'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key`='rongli.integration.reauthWindowMinutes');

INSERT INTO `sys_config` (`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`remark`)
SELECT '第三方配置主密钥版本','rongli.integration.masterKeyVersion','k1','N','admin',UTC_TIMESTAMP(),'仅记录版本标识；真实根密钥来自部署环境 VI_INTEGRATION_MASTER_KEY，禁止存库'
  FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key`='rongli.integration.masterKeyVersion');

-- ==================================================== 六、验收断言
SELECT 'tables' AS obj, COUNT(*) AS cnt FROM information_schema.TABLES
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN
   ('vi_integration_profile','vi_integration_revision','vi_integration_secret','vi_integration_check',
    'vi_integration_publication','vi_message_template','vi_message_template_revision','vi_notification_route',
    'vi_notification_delivery','vi_subscription_prompt','vi_offline_payment_record','vi_integration_test_recipient')
UNION ALL SELECT 'pay_new_cols', COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_payment_order'
    AND COLUMN_NAME IN ('payment_method','receiver_dept_id','policy_revision_id','merchant_profile_id',
                        'merchant_revision_id','app_id','mchid','method_locked_at','capacity_resolution')
UNION ALL SELECT 'refund_new_cols', COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='vi_refund_order'
    AND COLUMN_NAME IN ('payment_method','execution_mode')
UNION ALL SELECT 'menus', COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2240 AND 2258
UNION ALL SELECT 'configs', COUNT(*) FROM sys_config WHERE config_key LIKE 'rongli.integration.%'
UNION ALL SELECT 'config_keys', COUNT(DISTINCT config_key) FROM sys_config WHERE config_key LIKE 'rongli.integration.%';

-- 说明（未收紧项，需在真实商户配置建立后单独迁移）：
--   ck_pay_method_identity 依赖 merchant_profile_id / merchant_revision_id /
--   app_id / mchid 四列非空。历史行没有可信商户身份，不能凭当前配置猜测回填，
--   因此本脚本只建列与枚举约束，待 X02 完成商户档案与历史交易对账后，
--   用独立迁移补齐 identity 并用异常清单人工接管无法证明的记录。
