-- migration_20261023_01_fix_v64_comment_encoding.sql
-- 用途: (1) 修复 stage② 应用 migration_20261022_01/02 时客户端连接字符集非 utf8mb4
--          导致的 vi_ 表/列注释双重编码乱码(及生成列字面量被误标 _latin1 前缀);
--       (2) 将既有 E03 列的注释对齐到 V6.4 权威措辞(migration_20261022 增量只加表/列,
--          未回填既有列的 V6.4 注释), 使"增量升级结果"与"完整初始化(ry)结果"注释一致
--          (根 AGENTS.md 数据库变更同步规范: 增量结果须与完整初始化结果结构一致)。
-- 权威源: sql/ry_20260417.sql (V6.4 全量, 正确 UTF-8, 已核验 == V6.4 001) 与
--         migration_20261022_01 (2 张退役表的表注释; 退役表不在 ry 中)。
-- 前置版本: 已应用 migration_20261022_01/02 的库。执行顺序: 在 01/02 之后。
-- 非破坏性: 仅 ALTER 表注释 / MODIFY 列(沿用 ry 原定义, 只改注释与生成列字面量字符集);
--          不 DROP 表, 不改业务数据(受影响 vi_ 表在 M0 均为空)。
-- 可重复执行: 是(注释幂等; MODIFY 用与源相同定义)。备份: 常规 mysqldump 即可。
-- 关键: 必须以 utf8mb4 连接应用(SET NAMES utf8mb4 已置顶), 否则会再次双重编码。
SET NAMES utf8mb4;

ALTER TABLE `vi_employee_binding_credential` COMMENT = '[V6.4退役] 员工绑定凭证链已废弃；保留历史不DROP，应用层停写，改用后台直接预授站点';
ALTER TABLE `vi_employee_binding_request` COMMENT = '[V6.4退役] 员工绑定申请链已废弃；保留历史不DROP，应用层停写，改用手机号自动关联';
ALTER TABLE `vi_employee_directory_guard` COMMENT = '员工手机号匹配与目录变更串行防护';
ALTER TABLE `vi_integration_check` COMMENT = '指定配置版本校验记录';
ALTER TABLE `vi_integration_profile` COMMENT = '第三方配置档案与生效指针';
ALTER TABLE `vi_integration_publication` COMMENT = '配置发布与回滚审计';
ALTER TABLE `vi_integration_revision` COMMENT = '不可变第三方配置版本';
ALTER TABLE `vi_integration_secret` COMMENT = '安全凭据密文与受控生命周期';
ALTER TABLE `vi_integration_test_recipient` COMMENT = '第三方测试收件人白名单';
ALTER TABLE `vi_message_template` COMMENT = '外部短信与订阅模板档案';
ALTER TABLE `vi_message_template_revision` COMMENT = '不可变外部模板版本';
ALTER TABLE `vi_notification_delivery` COMMENT = '外部发送执行与回执';
ALTER TABLE `vi_notification_route` COMMENT = '业务事件到唯一主通道的路由';
ALTER TABLE `vi_offline_payment_record` COMMENT = '客户线下收退款证明与核实';
ALTER TABLE `vi_phone_verification_receipt` COMMENT = '手机号授权处理与重试回执';
ALTER TABLE `vi_station_grant_set` COMMENT = '员工站点授权集合版本';
ALTER TABLE `vi_subscription_prompt` COMMENT = '一次用户触发订阅请求与结果';

-- vi_account
ALTER TABLE `vi_account` MODIFY COLUMN `employee_auto_link_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT '管理员解除后阻止该账号自动换绑；仅后台明确恢复，可不影响车主';
ALTER TABLE `vi_account` MODIFY COLUMN `phone_source` VARCHAR(24) NULL COMMENT 'PhoneProofSource: WECHAT,SMS；仅WECHAT可触发新员工关联';
ALTER TABLE `vi_account` MODIFY COLUMN `phone_verified_at` DATETIME(3) NULL COMMENT '服务端最后一次可信手机号验证UTC时刻';
-- vi_aftersale_case
ALTER TABLE `vi_aftersale_case` MODIFY COLUMN `refund_state` VARCHAR(24) NOT NULL COMMENT 'NONE/NOT_REQUIRED/WAIT_RETURN/READY/PROCESSING/SUCCESS/UNKNOWN/ABNORMAL';
-- vi_employee_binding
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `active_account` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, account_id, NULL)) STORED COMMENT '有效或暂停关联占用；只读生成列';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `active_user` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, sys_user_id, NULL)) STORED COMMENT '有效或暂停关联占用；与app_id联合唯一';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '关联应用命名空间';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `auto_rebind_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT '管理员撤销后拦截同应用同员工新自动关联；明确allow-rematch才解除';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `bound_by` BIGINT NULL COMMENT '自动关联为NULL，操作者为系统；管理员后续操作单独审计，绝不填假管理员';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `employee_auth_version` BIGINT NOT NULL COMMENT '建立关联时的框架授权版本快照；每次工作请求仍检查当前员工状态和权限';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `last_phone_verified_at` DATETIME(3) NOT NULL COMMENT '最近本人微信手机号核验时间';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `phone_lookup` BINARY(32) NOT NULL COMMENT '关联时一致的规范化手机号HMAC；不是可还原裸手机号';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `revoked_by` BIGINT NULL COMMENT '后台受控撤销操作者';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `revoked_reason` VARCHAR(500) NULL COMMENT '解除依据';
ALTER TABLE `vi_employee_binding` MODIFY COLUMN `suspended_reason` VARCHAR(64) NULL COMMENT '手机号变更、账号停用等失效原因；不泄漏其他账号';
-- vi_employee_directory_guard
ALTER TABLE `vi_employee_directory_guard` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_employee_directory_guard` MODIFY COLUMN `guard_key` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '首期唯一GLOBAL_DIRECTORY；同库轻量互斥，不是用户权限';
ALTER TABLE `vi_employee_directory_guard` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串';
ALTER TABLE `vi_employee_directory_guard` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)';
ALTER TABLE `vi_employee_directory_guard` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本';
-- vi_integration_check
ALTER TABLE `vi_integration_check` MODIFY COLUMN `check_level` VARCHAR(24) NOT NULL COMMENT 'IntegrationCheckLevel: LOCAL,READ_ONLY,LIVE_EVIDENCE；';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `issues_json` JSON NOT NULL COMMENT '脱敏逐字段错误/警告/未验证项';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `real_payment_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '仅真实授权资金测试证据才能标记；只读连接不是支付成功';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `status` VARCHAR(16) NOT NULL COMMENT 'IntegrationCheckStatus: NOT_RUN,PASS,FAIL,PARTIAL；';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE；';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_check` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_integration_profile
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `active_revision_id` BIGINT NULL COMMENT '当前发布版本';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `config_key` VARCHAR(80) NOT NULL COMMENT 'PAYMENT_POLICY与WECHAT_APP用DEFAULT；其他由服务端生成，不由用户改身份';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `created_by` BIGINT NOT NULL COMMENT '配置创建人';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `draft_revision_id` BIGINT NULL COMMENT '最新草稿版本';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationKind: PAYMENT_POLICY,WECHAT_PAY,TENCENT_SMS,ALIYUN_SMS,WECHAT_APP；';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `last_published_by` BIGINT NULL COMMENT '最近发布人';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationLifecycle: DRAFT,ACTIVE,RETIRED；';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `owner_dept_id` BIGINT NOT NULL COMMENT '真实管理/收款主体，复用框架部门归属';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '分配版本号的CAS计数';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_profile` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_integration_publication
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `action` VARCHAR(16) NOT NULL COMMENT 'IntegrationPublishAction: PUBLISH,ROLLBACK；';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `dependency_versions` JSON NOT NULL COMMENT '发布时依赖版本快照';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `previous_revision_id` BIGINT NULL COMMENT '旧生效版本';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `target_type` VARCHAR(16) NOT NULL COMMENT 'IntegrationTargetType: PROFILE,TEMPLATE；';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `target_version` BIGINT NOT NULL COMMENT '发布事务后的根版本';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_publication` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_integration_revision
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `config_json` JSON NOT NULL COMMENT '经kind专用DTO验证的非秘密配置';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `content_hash` VARCHAR(64) NOT NULL COMMENT '规范化内容与秘密引用版本摘要';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `secret_refs_json` JSON NOT NULL COMMENT '本配置凭据槽位到secret id的内部映射，不下发';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_revision` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_integration_secret
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `aad_context` VARCHAR(255) NOT NULL COMMENT '绑定profile/slot/env的不可更改上下文';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `ciphertext` MEDIUMBLOB NOT NULL COMMENT 'AEAD密文或KMS加密数据键封装，绝非base64明文';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `credential_kind` VARCHAR(32) NOT NULL COMMENT 'IntegrationCredentialKind: PRIVATE_KEY,API_V3_KEY,CLOUD_SECRET,APP_SECRET,BANK_ACCOUNT；';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `fingerprint` VARCHAR(64) NOT NULL COMMENT '安全标识不可逆；不显示明文末几位';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `key_version` VARCHAR(128) NOT NULL COMMENT '主密钥版本在服务端安全配置';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `lifecycle` VARCHAR(16) NOT NULL COMMENT 'IntegrationSecretLifecycle: ACTIVE,RETIRED,REVOKED；';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `nonce` VARBINARY(32) NOT NULL COMMENT '每密文独立随机nonce';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `retired_by` BIGINT NULL COMMENT '受控撤销人';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `slot_name` VARCHAR(64) NOT NULL COMMENT '固定允许的凭据槽，禁止用户自造密钥用途';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_secret` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_integration_test_recipient
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `account_id` BIGINT NULL COMMENT '仅MINI_ACCOUNT';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `app_profile_id` BIGINT NULL COMMENT '小程序白名单同应用';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `kind` VARCHAR(16) NOT NULL COMMENT 'IntegrationRecipientKind: PHONE,MINI_ACCOUNT；';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `phone_cipher` VARBINARY(512) NULL COMMENT '仅PHONE';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_integration_test_recipient` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_message_template
ALTER TABLE `vi_message_template` MODIFY COLUMN `active_revision_id` BIGINT NULL COMMENT '当前已发布模板';
ALTER TABLE `vi_message_template` MODIFY COLUMN `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION；';
ALTER TABLE `vi_message_template` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_message_template` MODIFY COLUMN `draft_revision_id` BIGINT NULL COMMENT '最新草稿';
ALTER TABLE `vi_message_template` MODIFY COLUMN `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；';
ALTER TABLE `vi_message_template` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_message_template` MODIFY COLUMN `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS,WECHAT_SUBSCRIBE；';
ALTER TABLE `vi_message_template` MODIFY COLUMN `revision_seq` BIGINT NOT NULL DEFAULT 0 COMMENT '模板版本分配计数';
ALTER TABLE `vi_message_template` MODIFY COLUMN `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；';
ALTER TABLE `vi_message_template` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_message_template` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_message_template_revision
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `approval_status` VARCHAR(16) NOT NULL COMMENT 'TemplateApprovalStatus: UNKNOWN,APPROVED,REJECTED；';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `enabled` TINYINT NOT NULL COMMENT '发送开关';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `options_json` JSON NOT NULL COMMENT '签名/内容快照/小程序page/env/lang等非秘密';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `parameter_bindings` JSON NOT NULL COMMENT '白名单变量、实际key/type/length；腾讯顺序/阿里命名';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `vendor_template_id` VARCHAR(256) NULL COMMENT '实际供应商审核通过的模板ID；草稿可空，发布时强制';
ALTER TABLE `vi_message_template_revision` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_notification_delivery
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `event_key` VARCHAR(128) NOT NULL COMMENT '逻辑事件业务键';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `notification_id` BIGINT NOT NULL COMMENT '关联已有站内消息/OTP任务；OTP可使用内部通知目标记录';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `payload_cipher_refs` JSON NOT NULL COMMENT '消息内容加密引用/服务器事件引用，不含明文验证码';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `provider` VARCHAR(32) NOT NULL COMMENT 'ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS,WECHAT_SUBSCRIBE；';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `recipient_cipher` VARBINARY(1024) NOT NULL COMMENT '必要收件手机号/OpenID密文';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `recipient_key` VARCHAR(128) NOT NULL COMMENT 'accountId或脱敏HMAC，禁止裸号';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `status` VARCHAR(24) NOT NULL COMMENT 'ExternalDeliveryStatus: PENDING,SENDING,ACCEPTED,DELIVERED,FAILED,UNKNOWN,SKIPPED；';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_notification_delivery` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_notification_route
ALTER TABLE `vi_notification_route` MODIFY COLUMN `audience` VARCHAR(16) NOT NULL COMMENT 'NotificationAudience: OWNER,DRIVER,STATION；';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `environment` VARCHAR(12) NOT NULL COMMENT 'IntegrationEnvironment: TEST,LIVE；';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `fallback_policy` VARCHAR(32) NOT NULL COMMENT 'MessageFallbackPolicy: NONE,DEFINITELY_NOT_SENT_ONLY；';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `fallback_template_id` BIGINT NULL COMMENT '只允许明确未发送时按规则用备用';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `scene` VARCHAR(40) NOT NULL COMMENT 'NotificationScene: LOGIN_OTP,PHONE_VERIFY,BOOKING_CONFIRMED,DRIVER_ASSIGNED,PICKUP_ARRIVED,INSPECTION_FINISHED,RETURN_COMPLETED,AFTERSALE_DECIDED,REFUND_CHANGED,DRIVER_INVITE,STATEMENT_READY；';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_notification_route` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
-- vi_offline_payment_record
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `active_payment` BIGINT GENERATED ALWAYS AS (IF(direction='COLLECTION' AND status IN ('SUBMITTED','VERIFIED'),payment_id,NULL)) STORED COMMENT '当前有效收款证明唯一占位';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `active_refund` BIGINT GENERATED ALWAYS AS (IF(direction='REFUND' AND status IN ('SUBMITTED','VERIFIED'),refund_id,NULL)) STORED COMMENT '当前有效退款证明唯一占位';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `amount_fen` BIGINT NOT NULL COMMENT '本期完整金额；不支持混合/客户拆单付款';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `direction` VARCHAR(16) NOT NULL COMMENT 'OfflineProofDirection: COLLECTION,REFUND；';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `external_reference_cipher` VARBINARY(1024) NOT NULL COMMENT '流水/收据编号密文';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `method` VARCHAR(24) NOT NULL COMMENT 'OfflineProofMethod: CASH,BANK_TRANSFER；';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `owner_dept_id` BIGINT NOT NULL COMMENT '原支付收款主体快照';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `paid_at` DATETIME(3) NOT NULL COMMENT '真实线下发生时刻';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `reference_hmac` VARCHAR(64) NOT NULL COMMENT '主体+方向+方式+流水永久去重摘要';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `refund_id` BIGINT NULL COMMENT '仅REFUND有值，必须来自统一审核已批准执行单';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `status` VARCHAR(16) NOT NULL COMMENT 'OfflineProofStatus: SUBMITTED,VERIFIED,REJECTED；';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';
ALTER TABLE `vi_offline_payment_record` MODIFY COLUMN `voucher_file_ids` JSON NOT NULL COMMENT '私有文件，服务端核查归属与可用性';
-- vi_payment_order
ALTER TABLE `vi_payment_order` MODIFY COLUMN `capacity_resolution` VARCHAR(24) NOT NULL COMMENT 'PaymentCapacityResolution: HELD,RELEASED,REACQUIRED,MANUAL_CONFLICT；';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `merchant_profile_id` BIGINT NULL COMMENT '仅微信';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `merchant_revision_id` BIGINT NULL COMMENT '原资金身份快照版本';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `method_locked_at` DATETIME(3) NULL COMMENT '首次有效预下单/线下意图时写入';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `out_trade_no` VARCHAR(64) NOT NULL COMMENT '稳定支付编号；WECHAT作为商户单号，OFFLINE只作本地资金编号';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `payment_method` VARCHAR(16) NOT NULL COMMENT 'PaymentMethod: UNSELECTED,WECHAT,OFFLINE；';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `policy_revision_id` BIGINT NULL COMMENT '创建/选择时采用的配置版';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `receiver_dept_id` BIGINT NOT NULL COMMENT '原始收款主体部门';
ALTER TABLE `vi_payment_order` MODIFY COLUMN `status` VARCHAR(24) NOT NULL COMMENT 'PaymentStatus: PENDING,REVIEW_PENDING,SUCCESS,CLOSED,UNKNOWN；';
-- vi_phone_verification_receipt
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `account_id` BIGINT NOT NULL COMMENT '已认证MINI账号；不从请求接收';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端应用标识';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `code_hash` BINARY(32) NOT NULL COMMENT '微信phoneCode摘要，防跨账号重放；绝不保存原始code';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `employee_link_outcome` VARCHAR(40) NOT NULL DEFAULT 'NOT_ATTEMPTED' COMMENT 'EmployeeLinkOutcome；内部原因不直接下发小程序';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `expires_at` DATETIME(3) NOT NULL COMMENT '关联重试窗口截至时间；默认10分钟是技术值，不是微信code有效期';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `finished_at` DATETIME(3) NULL COMMENT '最终处理时间';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `lease_until` DATETIME(3) NULL COMMENT '处理租约；处理中未知不盲目重复消费供应商凭据';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `phone_cipher` VARBINARY(512) NULL COMMENT '成功结果恢复所需加密号码；敏感回执到期按留存规则清理，审计不记录原文';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `phone_lookup` BINARY(32) NULL COMMENT '成功后可信手机号HMAC';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `request_id` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '客户端一次流程UUID，绑定当前账号';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `session_id` BIGINT NOT NULL COMMENT '发起的基础会话';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `verified_at` DATETIME(3) NULL COMMENT '微信已成功核验的UTC时间';
ALTER TABLE `vi_phone_verification_receipt` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本';
-- vi_refund_order
ALTER TABLE `vi_refund_order` MODIFY COLUMN `execution_mode` VARCHAR(24) NOT NULL COMMENT 'RefundExecutionMode: WECHAT_API,MANUAL_OFFLINE；';
ALTER TABLE `vi_refund_order` MODIFY COLUMN `payment_method` VARCHAR(16) NOT NULL COMMENT 'RefundPaymentMethod: WECHAT,OFFLINE；';
-- vi_station_grant_set
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `changed_by` BIGINT NULL COMMENT '后台修改人';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成内部Long，API字符串';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `own_station_id` BIGINT NULL COMMENT '仅行政展示，不自动授权';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `reason` VARCHAR(500) NULL COMMENT '本次集合调整原因';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `sys_user_id` BIGINT NOT NULL COMMENT '目标框架员工；可无微信账号';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新显式UTC_TIMESTAMP(3)';
ALTER TABLE `vi_station_grant_set` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '受控CAS版本';
-- vi_subscription_prompt
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `business_object_type` VARCHAR(32) NOT NULL COMMENT 'SubscriptionObjectType: ORDER,ASSIGNMENT,STATEMENT,DRIVER_APPLICATION；';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `client_report` JSON NULL COMMENT '微信前端结果不等于服务端真实剩余额度';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC创建时间';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部自增Long；API十进制字符串';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `nonce_hash` VARCHAR(64) NOT NULL COMMENT '一次性nonce摘要';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `status` VARCHAR(16) NOT NULL COMMENT 'SubscriptionPromptStatus: CREATED,REPORTED,EXPIRED；';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `template_versions` JSON NOT NULL COMMENT '模板ID、公开版本及场景';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'Mapper显式UTC更新时间';
ALTER TABLE `vi_subscription_prompt` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0 COMMENT '显式CAS；不是注解自动生效';

-- 执行后校验(应全部 0 行): 下列查询列出仍与源不一致的注释
-- 见 verify: 重新运行本仓库 docs/implementation/vehicle/v64_adapt/verify_schema.py 或对比 information_schema。
