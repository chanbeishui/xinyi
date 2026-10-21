-- 机动车检测预约 V6.0：首次新增 vi_* 业务表（66 张）到现有框架库
-- 用途：为机动车检测预约业务初装 66 张 vi_* 表（账号会话/站点授权/车辆车主/计价名额/订单派单/交接证据/检测结果/支付退款/售后/代驾对账核价付款/通知outbox幂等审计治理）。
-- 适用前置：已执行 ry_20260417.sql 初始化的框架库（含 sys_* 表）；目标库排序规则须为 utf8mb4_0900_ai_ci（与本脚本一致）。
-- 执行顺序：在框架初始化之后、机动车业务上线之前；先于 sql/migration_20261021_vehicle_menu_dict.sql（菜单/权限/字典种子）。
-- 备份要求：执行前必须 mysqldump 备份目标库（本仓库开发库已备份至 .vi-local/backup/，不入库）。
-- 可重复执行：否。脚本不含 IF NOT EXISTS（按资料包 check#29，不用其掩盖结构差异）；任一 vi_ 表已存在将直接报错中止。
-- 失败恢复：DDL 在 MySQL 隐式提交、不整体回滚；中途失败时记录已建表，用 DROP TABLE 清理本次已建 vi_ 表后从备份恢复或重跑，绝不 DROP/ALTER 任何 sys_ 或既有业务表。
-- 数据安全：仅 CREATE TABLE vi_*；不创建/修改/删除任何 sys_ 表，不插入员工/真实价格/商户/个人数据。
-- 目标版本：MySQL 8.0.22+（CHECK 强制、表达式 DEFAULT）；本机开发实测 9.6.0，生产目标 8.4 须复跑同样 SQL/CAS 验收。
-- 时间约定：DATETIME(3) 用 UTC 分量，DEFAULT (UTC_TIMESTAMP(3)) 不依赖连接时区；updated_at 由 Mapper 显式写 UTC_TIMESTAMP(3)。
-- 来源：docs/vehicle/V6.1/05_数据设计/001_vi业务表_初始化_V6.0.sql（含 E01 R1/R2 补齐的枚举 CHECK）。

-- 机动车检测预约 V6.0：从零初始化 vi_* 业务表，不是V4升级脚本。
-- 不创建/修改/删除任何sys_*表，不插入员工、真实价格或商户。
-- 仅在明确选定的空业务库/已备份的测试框架库执行；已有vi_*表会直接报错，禁止IF NOT EXISTS掩盖结构差异。
-- 目标MySQL 8.0.22+（CHECK及表达式DEFAULT）；正式采用版本须在目标环境执行验收。
-- DATETIME使用UTC分量；DEFAULT(UTC_TIMESTAMP(3))不依赖连接时区；UPDATE由Mapper显式写UTC_TIMESTAMP(3)。
-- DDL在MySQL中会隐式提交，不宣称全脚本事务回滚。请先跑环境预检。
SET NAMES utf8mb4;

-- 微信统一基础账户 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '微信应用标识',
  `open_id` VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '同一小程序身份',
  `union_id` VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '仅供应商可信返回时记录',
  `phone_cipher` VARBINARY(512) COMMENT '手机号密文',
  `phone_lookup` BINARY(32) COMMENT '带密钥摘要，非裸哈希',
  `nickname` VARCHAR(100) COMMENT '选填昵称',
  `status` VARCHAR(24) NOT NULL COMMENT 'AccountStatus: ACTIVE,LOCKED',
  `privacy_version` VARCHAR(64) COMMENT '最近同意版本',
  `avatar_file_id` BIGINT NULL COMMENT '本人已验证头像引用，不直接存URL',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_app_open (app_id,open_id),
  KEY ix_phone_lookup (phone_lookup),
  CONSTRAINT ck_66_592d8ece9cae CHECK (`status` IN ('ACTIVE','LOCKED')),
  CONSTRAINT ck_ver_account CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信统一基础账户';

-- 微信账户与框架员工受控绑定 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '关联vi_account',
  `sys_user_id` BIGINT NOT NULL COMMENT '引用现有sys_user，不重建员工',
  `bound_by` BIGINT NOT NULL COMMENT '受控绑定审批人',
  `bound_at` DATETIME(3) NOT NULL COMMENT '绑定时间',
  `revoked_at` DATETIME(3) COMMENT '解除时间',
  `active_account` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, account_id, NULL)) STORED COMMENT '生效账户唯一',
  `active_user` BIGINT GENERATED ALWAYS AS (IF(revoked_at IS NULL, sys_user_id, NULL)) STORED COMMENT '生效员工唯一',
  `permission_version` BIGINT NOT NULL DEFAULT 0 COMMENT '员工关联权限版本；撤权事务更新',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_account (active_account),
  UNIQUE KEY uk_active_user (active_user),
  CONSTRAINT ck_ver_employee_binding CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信账户与框架员工受控绑定';

-- 代驾资格与听单偏好 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '同一个基础账户',
  `qualification_status` VARCHAR(24) NOT NULL COMMENT 'QualificationStatus: DRAFT,SUBMITTED,APPROVED,REJECTED,EXPIRED,SUSPENDED',
  `new_assignment_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许新增承接',
  `listening_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '本人听单偏好',
  `review_due_at` DATETIME(3) COMMENT '年度复审到期',
  `license_class` VARCHAR(64) COMMENT '实际准驾类别，不用字母排序比较',
  `license_valid_to` DATETIME(3) COMMENT '到期日',
  `license_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT '长期有效标记',
  `qualification_revision` BIGINT COMMENT '生效认证版本',
  `internal_remark` VARCHAR(1000) COMMENT '授权运营备注，变更留痕',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_account (account_id),
  KEY ix_pool (qualification_status,new_assignment_enabled,listening_enabled),
  CONSTRAINT ck_66_c6bf03f0ceed CHECK (`qualification_status` IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','EXPIRED','SUSPENDED')),
  CONSTRAINT ck_ver_driver_profile CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾资格与听单偏好';

-- 代驾资格申请与复审版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '关联代驾档案',
  `status` VARCHAR(24) NOT NULL COMMENT 'QualificationStatus: DRAFT,SUBMITTED,APPROVED,REJECTED,EXPIRED,SUSPENDED',
  `sensitive_payload_cipher` LONGBLOB COMMENT '证件信息加密，不存明文JSON',
  `evidence_file_ids` JSON COMMENT '证件文件关联ID',
  `provider_result_ids` JSON COMMENT '可信核验和培训记录ID',
  `submitted_at` DATETIME(3) COMMENT '完整提交时间',
  `reviewer_user_id` BIGINT COMMENT '审核人',
  `reviewed_at` DATETIME(3) COMMENT '审核时间',
  `review_opinion` VARCHAR(1000) COMMENT '审批意见',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_driver_status (driver_id,status),
  KEY ix_review_queue (status,submitted_at),
  CONSTRAINT ck_66_ef6019b02577 CHECK (`status` IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','EXPIRED','SUSPENDED')),
  CONSTRAINT ck_ver_driver_application CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾资格申请与复审版本';

-- 第三方核验与培训任务记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_provider_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `application_id` BIGINT NOT NULL COMMENT '代驾申请',
  `provider` VARCHAR(64) COMMENT '供应商或培训服务',
  `task_type` VARCHAR(64) COMMENT 'VerificationType: REALNAME,FACE,BACKGROUND,LICENSE,TRAINING',
  `provider_task_id` VARCHAR(128) COMMENT '外部任务标识',
  `status` VARCHAR(64) NOT NULL COMMENT 'VerificationStatus: CREATED,PROCESSING,PASSED,FAILED,EXPIRED,UNAVAILABLE',
  `result_digest` VARCHAR(64) COMMENT '结果摘要，不泄露生物信息',
  `verified_at` DATETIME(3) COMMENT '可信回调或查单确认时刻',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_provider_task (provider,provider_task_id),
  KEY ix_app_type (application_id,task_type),
  CONSTRAINT ck_66_2612e28e561f CHECK (`task_type` IN ('REALNAME','FACE','BACKGROUND','LICENSE','TRAINING')),
  CONSTRAINT ck_66_fd8e93f22cc6 CHECK (`status` IN ('CREATED','PROCESSING','PASSED','FAILED','EXPIRED','UNAVAILABLE')),
  CONSTRAINT ck_ver_provider_verification CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方核验与培训任务记录';

-- 检测点与可服务设置 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_station` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `name` VARCHAR(128) NOT NULL COMMENT '名称',
  `dept_id` BIGINT NOT NULL COMMENT '框架部门归属',
  `address` VARCHAR(255) NOT NULL COMMENT '检测点公开地址',
  `lng` DECIMAL(10,6) NOT NULL COMMENT 'GCJ02经度',
  `lat` DECIMAL(10,6) NOT NULL COMMENT 'GCJ02纬度',
  `service_radius_meter` INT NOT NULL COMMENT '服务半径',
  `supported_vehicle_codes` JSON NOT NULL COMMENT '引用现有vehicle_type数据字典',
  `business_hours` JSON NOT NULL COMMENT '营业日和接收边界',
  `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '配置完成后启用',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_dept_enabled (dept_id,enabled),
  CONSTRAINT ck_ver_station CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检测点与可服务设置';

-- 员工跨检测点操作范围 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_station_grant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `sys_user_id` BIGINT NOT NULL COMMENT '复用框架人员',
  `station_id` BIGINT NOT NULL COMMENT '获授权站点',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '功能权限仍单独验证',
  `granted_by` BIGINT NOT NULL COMMENT '授权人',
  `reason` VARCHAR(500) NOT NULL COMMENT '授权变更说明',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_user_station (sys_user_id,station_id),
  CONSTRAINT ck_ver_station_grant CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工跨检测点操作范围';

-- 授权客户-车辆关联档案，供独立车辆列表 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_vehicle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '基础账户',
  `plate_no` VARCHAR(20) NOT NULL COMMENT '归一化车牌',
  `vehicle_type_code` VARCHAR(64) NOT NULL COMMENT '数据字典编码',
  `brand_model` VARCHAR(128) COMMENT '可选',
  `vin_last4` VARCHAR(4) COMMENT '只读核验摘要',
  `registered_on` DATE COMMENT '登记日期，按需核验',
  `use_type` VARCHAR(64) COMMENT '使用性质',
  `verification_status` VARCHAR(24) NOT NULL DEFAULT 'PENDING' COMMENT 'VehicleVerificationStatus: PENDING,VERIFIED,REJECTED',
  `internal_remark` VARCHAR(1000) COMMENT '运营资料备注，不回写历史订单',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '服务层保证本人至多一辆默认',
  `deleted_at` DATETIME(3) COMMENT '软删除',
  `active_plate` VARCHAR(20) GENERATED ALWAYS AS (IF(deleted_at IS NULL,plate_no,NULL)) STORED COMMENT '软删除后不占用本人车牌约束',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_owner_plate (owner_account_id,active_plate),
  CONSTRAINT ck_66_c3de1069a647 CHECK (`verification_status` IN ('PENDING','VERIFIED','REJECTED')),
  CONSTRAINT ck_ver_vehicle CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='授权客户-车辆关联档案，供独立车辆列表';

-- 客户常用地址 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '所属账户',
  `label` VARCHAR(40) COMMENT '家/公司等',
  `address_cipher` VARBINARY(2048) COMMENT '精确地址加密',
  `location_cipher` VARBINARY(1024) COMMENT '坐标敏感数据',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '常用',
  `deleted_at` DATETIME(3) COMMENT '软删除',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_owner (owner_account_id,deleted_at),
  CONSTRAINT ck_ver_address CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户常用地址';

-- 不可变生效计价版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_price_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `rule_key` VARCHAR(64) NOT NULL COMMENT '稳定方案标识',
  `revision` INT NOT NULL COMMENT '新版本号',
  `vehicle_type_code` VARCHAR(64) NOT NULL COMMENT '车型字典编码',
  `inspection_type_code` VARCHAR(64) NOT NULL COMMENT '检测业务类型',
  `status` VARCHAR(64) NOT NULL COMMENT 'PriceRuleStatus: DRAFT,IN_REVIEW,APPROVED,PUBLISHED,DISABLED',
  `config_json` JSON NOT NULL COMMENT '按PriceInput约束的计价参数，分与米',
  `effective_from` DATETIME(3) COMMENT '生效时间',
  `effective_to` DATETIME(3) COMMENT '失效时间',
  `submitted_by` BIGINT COMMENT '提交人',
  `approved_by` BIGINT COMMENT '审核人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_rule_revision (rule_key,revision),
  KEY ix_lookup (vehicle_type_code,inspection_type_code,status,effective_from),
  CONSTRAINT ck_66_9d65819638e0 CHECK (`status` IN ('DRAFT','IN_REVIEW','APPROVED','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_price_rule CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变生效计价版本';

-- 上门服务与站点接收额度 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quota_slot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `quota_type` VARCHAR(24) NOT NULL COMMENT 'QuotaType: PICKUP,STATION_RECEIVE',
  `scope_key` VARCHAR(64) NOT NULL COMMENT '区域标识或站点标识',
  `station_id` BIGINT COMMENT '接收额度所属站点',
  `starts_at` DATETIME(3) NOT NULL COMMENT '时间区间起',
  `ends_at` DATETIME(3) NOT NULL COMMENT '时间区间止',
  `total_quota` INT NOT NULL COMMENT '可提供数量',
  `held_quota` INT NOT NULL DEFAULT 0 COMMENT '未付预占数量',
  `confirmed_quota` INT NOT NULL DEFAULT 0 COMMENT '已付预约尚未消耗',
  `consumed_quota` INT NOT NULL DEFAULT 0 COMMENT '已经真实消耗',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '停约开关',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_scope_slot (quota_type,scope_key,starts_at,ends_at),
  CONSTRAINT ck_quota_nonnegative CHECK (total_quota>=0 AND held_quota>=0 AND confirmed_quota>=0 AND consumed_quota>=0),
  CONSTRAINT ck_quota_limit CHECK (held_quota+confirmed_quota+consumed_quota<=total_quota),
  CONSTRAINT ck_66_6d097fde07cc CHECK (`quota_type` IN ('PICKUP','STATION_RECEIVE')),
  CONSTRAINT ck_ver_quota_slot CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上门服务与站点接收额度';

-- 短期报价快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quote` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `owner_account_id` BIGINT NOT NULL COMMENT '报价人',
  `station_id` BIGINT NOT NULL COMMENT '匹配站点',
  `vehicle_snapshot` JSON NOT NULL COMMENT '车牌、车型编码与名称快照',
  `address_snapshot_cipher` LONGBLOB NOT NULL COMMENT '加密接车地址快照',
  `pickup_slot_id` BIGINT NOT NULL COMMENT '上门时段',
  `receive_slot_id` BIGINT COMMENT '内部接收排期',
  `rule_id` BIGINT NOT NULL COMMENT '计价版本ID',
  `route_snapshot` JSON NOT NULL COMMENT '供应商路线距离、方向、时效',
  `amount_fen` BIGINT NOT NULL COMMENT '总费用分',
  `items_json` JSON NOT NULL COMMENT '逐项明细及计算依据',
  `expires_at` DATETIME(3) NOT NULL COMMENT '报价失效时间',
  `used_order_id` BIGINT COMMENT '已用于建单的订单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_owner_expire (owner_account_id,expires_at),
  UNIQUE KEY uk_used_order (used_order_id),
  CONSTRAINT ck_quote_amount CHECK (amount_fen>=0),
  CONSTRAINT ck_ver_quote CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短期报价快照';

-- 订单主履约与实际保管 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_no` VARCHAR(64) NOT NULL COMMENT '对外业务单号',
  `owner_account_id` BIGINT NOT NULL COMMENT '下单账户',
  `vehicle_id` BIGINT COMMENT '常用车辆档案',
  `station_id` BIGINT NOT NULL COMMENT '业务权威检测点归属',
  `dept_id` BIGINT NOT NULL COMMENT '框架数据归属快照',
  `quote_id` BIGINT NOT NULL COMMENT '报价及价格快照',
  `vehicle_snapshot` JSON NOT NULL COMMENT '历史车辆快照',
  `fulfillment_status` VARCHAR(32) NOT NULL COMMENT 'FulfillmentStatus: PENDING_PAY,SCHEDULED,PENDING_DISPATCH,DISPATCHING,MANUAL_DISPATCH,ACCEPTED,GOING_PICKUP,ARRIVED_PICKUP,PICKED_UP,ARRIVED_STATION,INSPECTING,INSPECTED,CAR_TAKEN,WAIT_CONFIRM,COMPLETED,CANCELLED,CLOSED',
  `cancel_status` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'CancelStatus: NONE,APPLIED,REJECTED,APPROVED_WAIT_RETURN,COMPLETED',
  `custody_type` VARCHAR(16) NOT NULL COMMENT 'CustodyType: OWNER,DRIVER,STATION',
  `custody_ref_id` BIGINT NOT NULL COMMENT '实际保管方ID，须与类型联查',
  `active_assignment_id` BIGINT COMMENT '当前承接指针，已释放时为空',
  `pickup_starts_at` DATETIME(3) NOT NULL COMMENT '对外约定上门时间',
  `pickup_ends_at` DATETIME(3) NOT NULL COMMENT '对外时段结束',
  `pay_expires_at` DATETIME(3) NOT NULL COMMENT '创建后30分钟',
  `dispatch_at` DATETIME(3) COMMENT '预约前N分钟',
  `dispatch_config_version` BIGINT COMMENT '派单参数快照',
  `returned_at` DATETIME(3) COMMENT '实际归还成功时间',
  `auto_confirm_at` DATETIME(3) COMMENT '归还后24小时服务确认截止',
  `completed_at` DATETIME(3) COMMENT '业务服务确认完成',
  `cancelled_at` DATETIME(3) COMMENT '取消正式办结',
  `total_fen` BIGINT NOT NULL COMMENT '应付分',
  `terms_version` VARCHAR(64) NOT NULL COMMENT '服务协议版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_quote (quote_id),
  KEY ix_owner_list (owner_account_id,created_at,id),
  KEY ix_station_queue (station_id,fulfillment_status,created_at,id),
  KEY ix_dispatch_due (fulfillment_status,dispatch_at,id),
  KEY ix_auto_confirm (fulfillment_status,auto_confirm_at,id),
  CONSTRAINT ck_order_total CHECK (total_fen>=0),
  CONSTRAINT ck_66_0ff90a4c30f3 CHECK (`fulfillment_status` IN ('PENDING_PAY','SCHEDULED','PENDING_DISPATCH','DISPATCHING','MANUAL_DISPATCH','ACCEPTED','GOING_PICKUP','ARRIVED_PICKUP','PICKED_UP','ARRIVED_STATION','INSPECTING','INSPECTED','CAR_TAKEN','WAIT_CONFIRM','COMPLETED','CANCELLED','CLOSED')),
  CONSTRAINT ck_66_0237ff511e2d CHECK (`cancel_status` IN ('NONE','APPLIED','REJECTED','APPROVED_WAIT_RETURN','COMPLETED')),
  CONSTRAINT ck_66_daacf35a5049 CHECK (`custody_type` IN ('OWNER','DRIVER','STATION')),
  CONSTRAINT ck_ver_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主履约与实际保管';

-- 每单额度占用凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_quota_reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '关联订单',
  `slot_id` BIGINT NOT NULL COMMENT '关联quota_slot',
  `status` VARCHAR(24) NOT NULL COMMENT 'ReservationStatus: HELD,CONFIRMED,CONSUMED,RELEASED',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `expires_at` DATETIME(3) COMMENT '未付锁额截止',
  `released_at` DATETIME(3) COMMENT '释放时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_slot (order_id,slot_id),
  KEY ix_due (status,expires_at),
  CONSTRAINT ck_66_44d120d4cf5a CHECK (`status` IN ('HELD','CONFIRMED','CONSUMED','RELEASED')),
  CONSTRAINT ck_ver_quota_reservation CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每单额度占用凭证';

-- 单人派单邀约 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_dispatch_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `driver_id` BIGINT NOT NULL COMMENT '候选代驾',
  `status` VARCHAR(24) NOT NULL COMMENT 'InviteStatus: WAIT_RESPONSE,ACCEPTED,REJECTED,EXPIRED,REVOKED',
  `source` VARCHAR(16) NOT NULL COMMENT 'AUTO/MANUAL',
  `round_key` VARCHAR(64) COMMENT '派单轮次',
  `issued_at` DATETIME(3) NOT NULL COMMENT '邀约时刻',
  `expires_at` DATETIME(3) NOT NULL COMMENT '绝对失效时间',
  `responded_at` DATETIME(3) COMMENT '响应',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(status='WAIT_RESPONSE',order_id,NULL)) STORED COMMENT '同单最多一个待响应邀约',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_order (active_order),
  KEY ix_driver_status (driver_id,status),
  KEY ix_expire (status,expires_at),
  CONSTRAINT ck_66_22cd3ab6b830 CHECK (`status` IN ('WAIT_RESPONSE','ACCEPTED','REJECTED','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_dispatch_invite CHECK (version >= 0),
  CONSTRAINT ck_dispatch_invite_source CHECK (`source` IN ('AUTO','MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单人派单邀约';

-- 司机承接历史，结束后仍保留 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `invite_id` BIGINT NOT NULL COMMENT '接受的邀约',
  `accepted_at` DATETIME(3) NOT NULL COMMENT '接受时间',
  `ended_at` DATETIME(3) COMMENT '解除或归还时结束',
  `end_reason` VARCHAR(64) COMMENT 'AssignmentEndReason: RETURNED,CANCELLED,DRIVER_CANCEL',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(ended_at IS NULL,order_id,NULL)) STORED COMMENT '生效承接唯一订单',
  `active_driver` BIGINT GENERATED ALWAYS AS (IF(ended_at IS NULL,driver_id,NULL)) STORED COMMENT '生效承接唯一司机',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_invite (invite_id),
  UNIQUE KEY uk_active_order (active_order),
  UNIQUE KEY uk_active_driver (active_driver),
  KEY ix_history (driver_id,accepted_at),
  CONSTRAINT ck_66_8c02041ed220 CHECK (`end_reason` IN ('RETURNED','CANCELLED','DRIVER_CANCEL')),
  CONSTRAINT ck_ver_driver_assignment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='司机承接历史，结束后仍保留';

-- 司机唯一资源占用，不受订单待确认影响 | BUSINESS_KEY
CREATE TABLE `vi_driver_occupancy` (
  `driver_id` BIGINT NOT NULL COMMENT '主键，唯一占用',
  `holder_type` VARCHAR(16) NOT NULL COMMENT 'OccupancyHolderType: INVITE,ASSIGNMENT',
  `holder_id` BIGINT NOT NULL COMMENT '被占用邀约或承接ID',
  `order_id` BIGINT NOT NULL COMMENT '对应订单',
  `expires_at` DATETIME(3) COMMENT '仅邀约有自动失效',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`driver_id`),
  UNIQUE KEY uk_holder (holder_type,holder_id),
  CONSTRAINT ck_66_0d4b483893ce CHECK (`holder_type` IN ('INVITE','ASSIGNMENT')),
  CONSTRAINT ck_ver_driver_occupancy CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='司机唯一资源占用，不受订单待确认影响';

-- 全局轮转游标与版本 | BUSINESS_KEY
CREATE TABLE `vi_dispatch_cursor` (
  `scope_key` VARCHAR(64) NOT NULL COMMENT '全局或已确认调度域主键',
  `last_driver_id` BIGINT COMMENT '上次派发指针',
  `round_no` BIGINT NOT NULL DEFAULT 0 COMMENT '轮次定义须P01评审',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`scope_key`),
  CONSTRAINT ck_ver_dispatch_cursor CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局轮转游标与版本';

-- 代驾最近前台定位 | BUSINESS_KEY
CREATE TABLE `vi_driver_location` (
  `driver_id` BIGINT NOT NULL COMMENT '主键',
  `location_cipher` VARBINARY(1024) NOT NULL COMMENT '加密位置',
  `captured_at` DATETIME(3) NOT NULL COMMENT '设备采集时间',
  `received_at` DATETIME(3) NOT NULL COMMENT '服务器接收时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`driver_id`),
  CONSTRAINT ck_ver_driver_location CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾最近前台定位';

-- 一次实物交接事务凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_handover` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `assignment_id` BIGINT NOT NULL COMMENT '当前承接',
  `handover_type` VARCHAR(32) NOT NULL COMMENT 'HandoverType: OWNER_TO_DRIVER,DRIVER_TO_STATION,STATION_TO_DRIVER,DRIVER_TO_OWNER',
  `holder_account_id` BIGINT NOT NULL COMMENT '持码基础账号',
  `verifier_account_id` BIGINT NOT NULL COMMENT '核验基础账号',
  `code_mac` BINARY(32) NOT NULL COMMENT '含订单/类型/代次随机盐的服务端带密钥MAC；4位码不能只裸哈希',
  `code_cipher` VARBINARY(512) NOT NULL COMMENT '仅持码方短期读取所需的加密值，使用后销毁可读密文',
  `generation_no` INT NOT NULL COMMENT '码代次',
  `status` VARCHAR(24) NOT NULL COMMENT 'HandoverStatus: PENDING,CONFIRMED,LOCKED,EXPIRED',
  `expires_at` DATETIME(3) NOT NULL COMMENT '时效',
  `confirmed_at` DATETIME(3) COMMENT '实物交接成功',
  `evidence_group_id` BIGINT COMMENT '已经封存证据组',
  `active_key` VARCHAR(160) GENERATED ALWAYS AS (IF(status='PENDING',CONCAT(order_id,':',assignment_id,':',handover_type),NULL)) STORED COMMENT '同时仅一个有效码',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_handover (active_key),
  KEY ix_order_type (order_id,handover_type,confirmed_at),
  CONSTRAINT ck_66_73b760054eb8 CHECK (`handover_type` IN ('OWNER_TO_DRIVER','DRIVER_TO_STATION','STATION_TO_DRIVER','DRIVER_TO_OWNER')),
  CONSTRAINT ck_66_87e8157e4010 CHECK (`status` IN ('PENDING','CONFIRMED','LOCKED','EXPIRED')),
  CONSTRAINT ck_ver_handover CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一次实物交接事务凭证';

-- 跨重发的交接错误次数 | BUSINESS_KEY
CREATE TABLE `vi_handover_attempt_guard` (
  `guard_key` VARCHAR(160) NOT NULL COMMENT '订单+承接+交接类型',
  `failed_attempts` INT NOT NULL DEFAULT 0 COMMENT '累计错误次数',
  `locked_at` DATETIME(3) COMMENT '错3次锁定',
  `unlocked_by` BIGINT COMMENT '人工复核授权人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`guard_key`),
  CONSTRAINT ck_ver_handover_attempt_guard CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨重发的交接错误次数';

-- 照片节点分组 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_evidence_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `assignment_id` BIGINT NOT NULL COMMENT '承接',
  `node_no` TINYINT NOT NULL COMMENT '1接车2到站3取车4归还',
  `status` VARCHAR(16) NOT NULL COMMENT 'EvidenceStatus: DRAFT,READY,SEALED',
  `mileage_km` DECIMAL(7,1) COMMENT '仪表公里数',
  `mileage_source` VARCHAR(16) COMMENT 'MANUAL/OCR_REVIEWED',
  `mileage_reason` VARCHAR(1000) COMMENT '异常说明',
  `sealed_at` DATETIME(3) COMMENT '交接时封存',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_assignment_node (assignment_id,node_no),
  CONSTRAINT ck_node CHECK (node_no BETWEEN 1 AND 4),
  CONSTRAINT ck_mileage CHECK (mileage_km IS NULL OR mileage_km>=0),
  CONSTRAINT ck_66_6c92d2f8851d CHECK (`status` IN ('DRAFT','READY','SEALED')),
  CONSTRAINT ck_ver_evidence_group CHECK (version >= 0),
  CONSTRAINT ck_evidence_group_mileage_source CHECK (`mileage_source` IS NULL OR `mileage_source` IN ('MANUAL','OCR_REVIEWED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='照片节点分组';

-- 照片版本与服务端元数据 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_photo` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `group_id` BIGINT NOT NULL COMMENT '证据组',
  `photo_type` VARCHAR(24) NOT NULL COMMENT 'PhotoType: FRONT,BACK,LEFT,RIGHT,DASHBOARD,INTERIOR,EXTRA',
  `file_id` BIGINT NOT NULL COMMENT '私有文件',
  `client_capture_id` VARCHAR(64) NOT NULL COMMENT '同次重传去重',
  `revision` INT NOT NULL COMMENT '版本',
  `superseded_at` DATETIME(3) COMMENT '草稿重拍历史',
  `captured_at` DATETIME(3) NOT NULL COMMENT '客户端采集，仅作声明',
  `received_at` DATETIME(3) NOT NULL COMMENT '服务端收件时间',
  `location_cipher` VARBINARY(1024) COMMENT '节点位置',
  `watermark_file_id` BIGINT COMMENT '服务端生成水印副本',
  `active_type` VARCHAR(24) GENERATED ALWAYS AS (IF(superseded_at IS NULL AND photo_type<>'EXTRA',photo_type,NULL)) STORED COMMENT '每类有效图唯一，EXTRA不覆盖',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_capture (group_id,client_capture_id),
  UNIQUE KEY uk_current_type (group_id,active_type),
  CONSTRAINT ck_66_37b55c2a7354 CHECK (`photo_type` IN ('FRONT','BACK','LEFT','RIGHT','DASHBOARD','INTERIOR','EXTRA')),
  CONSTRAINT ck_ver_photo CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='照片版本与服务端元数据';

-- 私有文件登记与校验 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `uploader_account_id` BIGINT NOT NULL COMMENT '上传者',
  `purpose` VARCHAR(32) NOT NULL COMMENT '限定用途',
  `order_id` BIGINT COMMENT '订单归属',
  `application_id` BIGINT COMMENT '认证等申请归属',
  `object_key` VARCHAR(255) NOT NULL COMMENT '服务端生成不可猜键',
  `status` VARCHAR(24) NOT NULL COMMENT 'FileStatus: UPLOADING,VERIFYING,READY,REJECTED',
  `mime_type` VARCHAR(64) COMMENT '内容识别类型',
  `server_size` BIGINT COMMENT '实际大小',
  `server_sha256` BINARY(32) COMMENT '服务器复核摘要',
  `retain_until` DATETIME(3) COMMENT '按已审批保留政策',
  `legal_hold` TINYINT NOT NULL DEFAULT 0 COMMENT '争议等必要留存',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_object_key (object_key),
  KEY ix_order_purpose (order_id,purpose),
  CONSTRAINT ck_66_c1389ba0baeb CHECK (`status` IN ('UPLOADING','VERIFYING','READY','REJECTED')),
  CONSTRAINT ck_ver_file CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私有文件登记与校验';

-- 行驶证登记与人工复核 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_vehicle_license` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `sensitive_payload_cipher` LONGBLOB NOT NULL COMMENT '车主/VIN等密文',
  `main_file_id` BIGINT COMMENT '主页',
  `sub_file_id` BIGINT COMMENT '副页',
  `ocr_task_id` BIGINT COMMENT '服务端OCR任务',
  `review_status` VARCHAR(24) NOT NULL COMMENT 'LicenseReviewStatus: MISSING,PENDING,VERIFIED',
  `reviewer_user_id` BIGINT COMMENT '框架核验人',
  `reviewed_at` DATETIME(3) COMMENT '复核时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_66_1355e77a455a CHECK (`review_status` IN ('MISSING','PENDING','VERIFIED')),
  CONSTRAINT ck_ver_vehicle_license CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行驶证登记与人工复核';

-- 追加式结构化结果版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_inspection_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `revision` INT NOT NULL COMMENT '结果版本',
  `conclusion` VARCHAR(8) NOT NULL COMMENT 'PASS/FAIL',
  `items_json` JSON NOT NULL COMMENT '最终项目与凭证；没有实时阶段',
  `inspector_user_id` BIGINT NOT NULL COMMENT '真实检测人，验证可操作站点',
  `submitted_by` BIGINT NOT NULL COMMENT '记录人',
  `source` VARCHAR(16) NOT NULL COMMENT 'STATION/ADMIN服务端写入',
  `inspected_at` DATETIME(3) NOT NULL COMMENT '实际检测完成时间',
  `submitted_at` DATETIME(3) NOT NULL COMMENT '登记时间',
  `superseded_at` DATETIME(3) COMMENT '被更正替换',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(superseded_at IS NULL,order_id,NULL)) STORED COMMENT '当前生效结果唯一',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order_revision (order_id,revision),
  UNIQUE KEY uk_current_order (active_order),
  CONSTRAINT ck_ver_inspection_result CHECK (version >= 0),
  CONSTRAINT ck_inspection_result_conclusion CHECK (`conclusion` IN ('PASS','FAIL')),
  CONSTRAINT ck_inspection_result_source CHECK (`source` IN ('STATION','ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='追加式结构化结果版本';

-- 检测结果更正审批 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_result_correction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `base_result_id` BIGINT NOT NULL COMMENT '原版本',
  `replacement_json` JSON NOT NULL COMMENT '拟更正内容',
  `status` VARCHAR(24) NOT NULL COMMENT 'CorrectionStatus: APPLIED,APPROVED,REJECTED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '原因',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人',
  `reviewer_id` BIGINT COMMENT '审核人',
  `opinion` VARCHAR(1000) COMMENT '意见',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_queue (status,created_at),
  CONSTRAINT ck_66_875cb83b2599 CHECK (`status` IN ('APPLIED','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_result_correction CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检测结果更正审批';

-- 支付交易与可退资金 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `out_trade_no` VARCHAR(64) NOT NULL COMMENT '稳定微信商户单号',
  `transaction_id` VARCHAR(64) COMMENT '渠道交易号',
  `status` VARCHAR(24) NOT NULL COMMENT 'PaymentStatus: PENDING,SUCCESS,CLOSED,UNKNOWN',
  `total_fen` BIGINT NOT NULL COMMENT '应付',
  `paid_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '实付',
  `refund_reserved_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '处理中退款金额预留',
  `refunded_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '已成功退款',
  `paid_at` DATETIME(3) COMMENT '支付成功',
  `expires_at` DATETIME(3) NOT NULL COMMENT '对应30分钟时点',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_trade (out_trade_no),
  UNIQUE KEY uk_transaction (transaction_id),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_refund_balance CHECK (paid_fen>=0 AND refund_reserved_fen>=0 AND refunded_fen>=0 AND refund_reserved_fen+refunded_fen<=paid_fen),
  CONSTRAINT ck_66_a1f02dcffc3b CHECK (`status` IN ('PENDING','SUCCESS','CLOSED','UNKNOWN')),
  CONSTRAINT ck_ver_payment_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付交易与可退资金';

-- 稳定退款单及渠道执行 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_refund_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_id` BIGINT NOT NULL COMMENT '关联唯一统一售后申请',
  `review_revision` INT NOT NULL COMMENT '批准决定版本，金额变更必须新版本审核',
  `payment_id` BIGINT NOT NULL COMMENT '支付单',
  `out_refund_no` VARCHAR(64) NOT NULL COMMENT '幂等商户退款号',
  `channel_refund_id` VARCHAR(64) COMMENT '渠道退款ID',
  `amount_fen` BIGINT NOT NULL COMMENT '金额分',
  `status` VARCHAR(24) NOT NULL COMMENT 'ChannelRefundStatus: CREATED,PROCESSING,SUCCESS,CLOSED,ABNORMAL,UNKNOWN',
  `success_at` DATETIME(3) COMMENT '只有SUCCESS存在',
  `last_query_at` DATETIME(3) COMMENT '查单',
  `next_query_at` DATETIME(3) COMMENT '退避后查单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_review (case_id,review_revision),
  UNIQUE KEY uk_refund_no (out_refund_no),
  KEY ix_query (status,next_query_at),
  CONSTRAINT ck_refund_amount CHECK (amount_fen>0),
  CONSTRAINT ck_66_8132f268f5fd CHECK (`status` IN ('CREATED','PROCESSING','SUCCESS','CLOSED','ABNORMAL','UNKNOWN')),
  CONSTRAINT ck_ver_refund_order CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='稳定退款单及渠道执行';

-- 永久渠道回调去重 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_channel_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `provider` VARCHAR(64) NOT NULL COMMENT '微信支付等',
  `event_key` VARCHAR(128) NOT NULL COMMENT '通知ID/交易业务唯一标识',
  `trade_no` VARCHAR(64) COMMENT '关联支付或退款号',
  `event_type` VARCHAR(64) COMMENT '事件类型',
  `verified_digest` BINARY(32) COMMENT '已验签原报文摘要',
  `processed_at` DATETIME(3) COMMENT '已持久化时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_event (provider,event_key),
  KEY ix_trade (trade_no),
  CONSTRAINT ck_ver_channel_event CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='永久渠道回调去重';

-- 普通服务争议 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_dispute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `type` VARCHAR(64) NOT NULL COMMENT '普通服务类型，不是事故',
  `status` VARCHAR(24) NOT NULL COMMENT 'ServiceCaseStatus: OPEN,PROCESSING,RESOLVED',
  `description` VARCHAR(2000) NOT NULL COMMENT '用户说明',
  `file_ids` JSON COMMENT '凭证',
  `handler_id` BIGINT COMMENT '处理人',
  `conclusion` VARCHAR(2000) COMMENT '结论',
  `resolved_at` DATETIME(3) COMMENT '解决时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_order (order_id,status),
  CONSTRAINT ck_66_c11343410b93 CHECK (`status` IN ('OPEN','PROCESSING','RESOLVED')),
  CONSTRAINT ck_ver_dispute CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通服务争议';

-- 普通履约阻碍与客服待办 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_service_exception` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `type` VARCHAR(64) NOT NULL COMMENT '失联/拒收/资料/上传等',
  `status` VARCHAR(24) NOT NULL COMMENT 'ServiceCaseStatus: OPEN,PROCESSING,RESOLVED',
  `description` VARCHAR(2000) COMMENT '说明',
  `file_ids` JSON COMMENT '可后补证据',
  `handler_id` BIGINT COMMENT '处理人',
  `resolution` VARCHAR(2000) COMMENT '实际处置依据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_queue (status,created_at),
  CONSTRAINT ck_66_0faa9ee9aef5 CHECK (`status` IN ('OPEN','PROCESSING','RESOLVED')),
  CONSTRAINT ck_ver_service_exception CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通履约阻碍与客服待办';

-- 服务评价 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `owner_account_id` BIGINT NOT NULL COMMENT '下单人',
  `score` TINYINT NOT NULL COMMENT '1到5',
  `tags` JSON COMMENT '评价标签',
  `content` VARCHAR(500) COMMENT '评语',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_order (order_id),
  CONSTRAINT ck_review_score CHECK (score BETWEEN 1 AND 5),
  CONSTRAINT ck_ver_review CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务评价';

-- 普通取消计次与轮转处罚 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_penalty` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '司机',
  `assignment_id` BIGINT NOT NULL COMMENT '取消承接',
  `business_date` DATE NOT NULL COMMENT '北京时间自然日',
  `counted` TINYINT NOT NULL DEFAULT 1 COMMENT '审核后是否计入',
  `round_key` VARCHAR(64) COMMENT '恢复轮次：P01评审',
  `penalty_type` VARCHAR(24) NOT NULL COMMENT 'PenaltyType: SKIP_ROUND,STOP_TODAY',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'PenaltyStatus: ACTIVE,EXPIRED,REVOKED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '处罚依据，对本人可读',
  `rule_version` VARCHAR(64) NOT NULL COMMENT '实际适用轮转及计次规则版本',
  `effective_until` DATETIME(3) NULL COMMENT '明确时刻；轮转型另用round_key',
  `revoked_at` DATETIME(3) NULL COMMENT '通过申诉撤销时刻',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_assignment (assignment_id),
  KEY ix_day (driver_id,business_date,counted),
  CONSTRAINT ck_66_04646c41d887 CHECK (`penalty_type` IN ('SKIP_ROUND','STOP_TODAY')),
  CONSTRAINT ck_66_cdebbbec3ff7 CHECK (`status` IN ('ACTIVE','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_penalty CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通取消计次与轮转处罚';

-- 不可变业务事实流水 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `order_id` BIGINT COMMENT '可关联订单',
  `aggregate_type` VARCHAR(64) NOT NULL COMMENT '聚合类型',
  `aggregate_id` BIGINT NOT NULL COMMENT '聚合ID',
  `action` VARCHAR(64) NOT NULL COMMENT '事件或动作',
  `actor_account_id` BIGINT COMMENT '小程序账户',
  `actor_user_id` BIGINT COMMENT '框架人员',
  `acting_role` VARCHAR(64) COMMENT '视角',
  `station_id` BIGINT COMMENT '操作上下文',
  `before_summary` JSON COMMENT '脱敏前值',
  `after_summary` JSON COMMENT '脱敏后值',
  `reason` VARCHAR(1000) COMMENT '原因',
  `actor_realm` VARCHAR(24) COMMENT 'MINI/ADMIN/SYSTEM/CALLBACK；不可混淆数值ID',
  `session_id` BIGINT COMMENT '可信小程序会话，可空',
  `context_version` BIGINT COMMENT '业务动作的可信上下文版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_order_time (order_id,created_at,id),
  CONSTRAINT ck_ver_event_log CHECK (version >= 0),
  CONSTRAINT ck_event_log_actor_realm CHECK (`actor_realm` IS NULL OR `actor_realm` IN ('MINI','ADMIN','SYSTEM','CALLBACK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可变业务事实流水';

-- 可靠异步事务消息 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_outbox` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `event_key` VARCHAR(128) NOT NULL COMMENT '事件唯一键',
  `aggregate_type` VARCHAR(64) NOT NULL COMMENT '所属对象',
  `aggregate_id` BIGINT NOT NULL COMMENT '对象ID',
  `event_type` VARCHAR(64) NOT NULL COMMENT '通知/支付查单/补偿等',
  `payload` JSON NOT NULL COMMENT '最小必要载荷，不塞敏感原文',
  `status` VARCHAR(24) NOT NULL COMMENT 'OutboxStatus: READY,PROCESSING,DONE,FAILED',
  `attempts` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_attempt_at` DATETIME(3) NOT NULL COMMENT '退避时间',
  `lease_until` DATETIME(3) COMMENT '工作者租约',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_event (event_key),
  KEY ix_due (status,next_attempt_at,id),
  CONSTRAINT ck_66_61a047126522 CHECK (`status` IN ('READY','PROCESSING','DONE','FAILED')),
  CONSTRAINT ck_ver_outbox CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可靠异步事务消息';

-- 站内消息与下发记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '接收人',
  `role` VARCHAR(64) NOT NULL COMMENT '业务视角',
  `order_id` BIGINT COMMENT '订单',
  `scene` VARCHAR(64) NOT NULL COMMENT '场景',
  `outbox_id` BIGINT COMMENT '触发事件',
  `send_status` VARCHAR(64) COMMENT 'NotificationSendStatus: PENDING,ACCEPTED,FAILED,DISCARDED',
  `content` VARCHAR(2000) COMMENT '脱敏消息',
  `read_at` DATETIME(3) COMMENT '站内显式已读',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_inbox (account_id,role,created_at,id),
  CONSTRAINT ck_66_236222a99f9d CHECK (`send_status` IN ('PENDING','ACCEPTED','FAILED','DISCARDED')),
  CONSTRAINT ck_ver_notification CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内消息与下发记录';

-- 请求级幂等 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_idempotency` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `subject_key` VARCHAR(96) NOT NULL COMMENT '统一主体命名空间',
  `action_key` VARCHAR(160) NOT NULL COMMENT 'HTTP动作+资源',
  `request_key` VARCHAR(64) NOT NULL COMMENT 'UUID幂等头',
  `request_mac` BINARY(32) NOT NULL COMMENT '请求摘要',
  `result_json` JSON COMMENT '成功结果或可重放结果',
  `status` VARCHAR(64) NOT NULL COMMENT 'IdempotencyStatus: PROCESSING,DONE',
  `expires_at` DATETIME(3) NOT NULL COMMENT '至少24h，请求级保存时限',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_request (subject_key,action_key,request_key),
  KEY ix_expire (expires_at),
  CONSTRAINT ck_66_0b655b918367 CHECK (`status` IN ('PROCESSING','DONE')),
  CONSTRAINT ck_ver_idempotency CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请求级幂等';

-- 隐私、导出、敏感访问与对账审批台账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_governance_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_type` VARCHAR(64) NOT NULL COMMENT 'GovernanceType: PRIVACY,EXPORT,SENSITIVE_ACCESS,RECONCILE',
  `order_id` BIGINT COMMENT '可关联订单',
  `requester_account_id` BIGINT COMMENT '申请客户',
  `requester_user_id` BIGINT COMMENT '申请后台人员',
  `status` VARCHAR(64) NOT NULL COMMENT 'GovernanceStatus: APPLIED,APPROVED,CHECKING,COMPLETED,EXPIRED,FAILED,OPEN,PROCESSING,READY,REJECTED,RESOLVED,REVOKED',
  `payload_cipher` LONGBLOB COMMENT '必要申请与结果加密',
  `reason` VARCHAR(1000) COMMENT '申请/处置依据',
  `reviewer_id` BIGINT COMMENT '审批人',
  `expires_at` DATETIME(3) COMMENT '授权或下载失效',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时间，统一UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY ix_type_status (case_type,status,created_at),
  CONSTRAINT ck_66_4f31235a8dc4 CHECK (`case_type` IN ('PRIVACY','EXPORT','SENSITIVE_ACCESS','RECONCILE')),
  CONSTRAINT ck_66_54fd86e51e4e CHECK (`status` IN ('APPLIED','APPROVED','CHECKING','COMPLETED','EXPIRED','FAILED','OPEN','PROCESSING','READY','REJECTED','RESOLVED','REVOKED')),
  CONSTRAINT ck_ver_governance_case CHECK (version >= 0),
  CONSTRAINT ck_governance_type_status CHECK ((case_type='PRIVACY' AND status IN ('APPLIED','PROCESSING','COMPLETED','REJECTED')) OR (case_type='EXPORT' AND status IN ('APPLIED','APPROVED','REJECTED','PROCESSING','READY','FAILED','EXPIRED')) OR (case_type='SENSITIVE_ACCESS' AND status IN ('APPLIED','APPROVED','REJECTED','EXPIRED','REVOKED')) OR (case_type='RECONCILE' AND status IN ('OPEN','CHECKING','RESOLVED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='隐私、导出、敏感访问与对账审批台账';

-- 车主运营档案扩展，非另一套登录账户 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_owner_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '关联vi_account',
  `service_name` VARCHAR(100) COMMENT '服务称呼',
  `service_flag` VARCHAR(24) NOT NULL DEFAULT 'NORMAL' COMMENT 'OwnerServiceFlag: NORMAL,VERIFY_AGAIN',
  `internal_remark` VARCHAR(1000) COMMENT '内部备注',
  `maintained_by` BIGINT COMMENT '框架维护人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_owner_profile (account_id),
  CONSTRAINT ck_66_49501f04f5eb CHECK (`service_flag` IN ('NORMAL','VERIFY_AGAIN')),
  CONSTRAINT ck_ver_owner_profile CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车主运营档案扩展，非另一套登录账户';

-- 取消与退款的唯一申请与处理记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_aftersale_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_no` VARCHAR(64) NOT NULL COMMENT '对外售后号',
  `order_id` BIGINT NOT NULL COMMENT '订单',
  `payment_id` BIGINT NOT NULL COMMENT '支付记录',
  `kind` VARCHAR(24) NOT NULL COMMENT 'CANCEL_REFUND/REFUND_ONLY',
  `case_status` VARCHAR(24) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT 'AfterSaleStatus: PENDING_REVIEW,EXECUTING,CLOSED,REJECTED',
  `review_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'AfterSaleReviewStatus: PENDING,APPROVED,REJECTED',
  `review_revision` INT NOT NULL DEFAULT 1 COMMENT '审核修订号',
  `requested_fen` BIGINT NOT NULL COMMENT '申请金额',
  `approved_fen` BIGINT COMMENT '批准金额，未审为NULL',
  `return_state` VARCHAR(24) NOT NULL COMMENT 'UNDETERMINED/NOT_REQUIRED/WAIT_RETURN/RETURNED',
  `refund_state` VARCHAR(24) NOT NULL COMMENT 'RefundStatus: NONE,NOT_REQUIRED,WAIT_RETURN,READY,PROCESSING,SUCCESS,UNKNOWN,ABNORMAL,CLOSED',
  `applicant_type` VARCHAR(16) NOT NULL COMMENT 'ApplicantType: ACCOUNT,SYS_USER',
  `applicant_id` BIGINT NOT NULL COMMENT '实际申请身份',
  `reason` VARCHAR(1000) NOT NULL COMMENT '原始申请说明',
  `evidence_ids` JSON COMMENT '关联证据文件ID',
  `resolved_at` DATETIME(3) COMMENT '业务及资金均已办结',
  `active_order` BIGINT GENERATED ALWAYS AS (IF(case_status IN ('PENDING_REVIEW','EXECUTING'),order_id,NULL)) STORED COMMENT '一个未办结申请',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_no (case_no),
  UNIQUE KEY uk_active_order (active_order),
  KEY ix_review (review_status,created_at),
  KEY ix_return (return_state,updated_at),
  CONSTRAINT ck_after_amount CHECK (requested_fen>=0 AND (approved_fen IS NULL OR approved_fen>=0)),
  CONSTRAINT ck_66_55e0ea12e908 CHECK (`case_status` IN ('PENDING_REVIEW','EXECUTING','CLOSED','REJECTED')),
  CONSTRAINT ck_66_dc8bfeb94c93 CHECK (`review_status` IN ('PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_66_c205c5c1a697 CHECK (`applicant_type` IN ('ACCOUNT','SYS_USER')),
  CONSTRAINT ck_ver_aftersale_case CHECK (version >= 0),
  CONSTRAINT ck_aftersale_case_kind CHECK (`kind` IN ('CANCEL_REFUND','REFUND_ONLY')),
  CONSTRAINT ck_aftersale_case_return_state CHECK (`return_state` IN ('UNDETERMINED','NOT_REQUIRED','WAIT_RETURN','RETURNED')),
  CONSTRAINT ck_aftersale_case_refund_state CHECK (`refund_state` IN ('NONE','NOT_REQUIRED','WAIT_RETURN','READY','PROCESSING','SUCCESS','UNKNOWN','ABNORMAL','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取消与退款的唯一申请与处理记录';

-- 统一审核的追加式决定凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_aftersale_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `case_id` BIGINT NOT NULL COMMENT '申请',
  `revision` INT NOT NULL COMMENT '唯一决定版本',
  `decision` VARCHAR(16) NOT NULL COMMENT 'APPROVE/REJECT',
  `approved_fen` BIGINT NOT NULL COMMENT '本决定金额',
  `observed_order_version` BIGINT NOT NULL COMMENT '审批时重新读取订单版本',
  `custody_at_review` VARCHAR(16) NOT NULL COMMENT '实际保管方快照',
  `return_required` TINYINT NOT NULL COMMENT '由服务端产生',
  `reviewer_user_id` BIGINT NOT NULL COMMENT '不能与申请人为同一真实身份',
  `opinion` VARCHAR(1000) NOT NULL COMMENT '审核意见',
  `reviewed_at` DATETIME(3) NOT NULL COMMENT 'UTC',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_case_revision (case_id,revision),
  CONSTRAINT ck_review_fen CHECK (approved_fen>=0),
  CONSTRAINT ck_ver_aftersale_review CHECK (version >= 0),
  CONSTRAINT ck_aftersale_review_decision CHECK (`decision` IN ('APPROVE','REJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一审核的追加式决定凭证';

-- 代驾报酬规则独立版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `payor_entity_id` BIGINT NOT NULL COMMENT '承担应付的主体',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '稳定规则标识',
  `revision` INT NOT NULL COMMENT '版本',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `scenario` VARCHAR(32) NOT NULL COMMENT '正常归还/取消返还/接车前解除',
  `scope_json` JSON NOT NULL COMMENT '车型/代驾/服务范围条件',
  `pricing_json` JSON NOT NULL COMMENT '固定费、含路线米、计费单位、单价及进位',
  `status` VARCHAR(24) NOT NULL COMMENT 'DriverFeeRuleStatus: DRAFT,PENDING_REVIEW,PUBLISHED,DISABLED',
  `effective_from` DATETIME(3) NOT NULL COMMENT '生效起点UTC',
  `effective_to` DATETIME(3) COMMENT '不含截止',
  `created_by` BIGINT NOT NULL COMMENT '规则编辑人',
  `approved_by` BIGINT COMMENT '独立审核人',
  `approved_at` DATETIME(3) COMMENT '发布确认时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_fee_rule_revision (payor_entity_id,rule_code,revision),
  KEY ix_fee_effective (payor_entity_id,scenario,status,effective_from),
  CONSTRAINT ck_66_78d970a2e61f CHECK (`status` IN ('DRAFT','PENDING_REVIEW','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_driver_fee_rule CHECK (version >= 0),
  CONSTRAINT ck_driver_fee_rule_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾报酬规则独立版本';

-- 按真实承接形成的不可覆盖费用台账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_ledger` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '付款责任主体',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `assignment_id` BIGINT NOT NULL COMMENT '具体承接，不仅订单',
  `order_id` BIGINT NOT NULL COMMENT '客户订单',
  `source_event_key` VARCHAR(128) NOT NULL COMMENT '归还/解除/调整永久去重键',
  `entry_kind` VARCHAR(32) NOT NULL COMMENT 'NORMAL_RETURN/CANCEL_RETURN/CANCEL_BEFORE_PICKUP/ADJUSTMENT/INITIAL_VALUATION/PRICE_DELTA',
  `service_ended_at` DATETIME(3) NOT NULL COMMENT '实际任务结束UTC；非客户服务确认时间',
  `pricing_status` VARCHAR(24) NOT NULL COMMENT 'LedgerPricingStatus: PENDING_PRICING,READY',
  `fee_rule_id` BIGINT COMMENT '匹配规则版本ID',
  `rule_snapshot` JSON COMMENT '冻结规则',
  `amount_fen` BIGINT COMMENT '未知为NULL；调整可负',
  `fee_breakdown` JSON COMMENT '基础/路线/调整明细',
  `source_entry_id` BIGINT COMMENT '调整源条目',
  `verified_by` BIGINT COMMENT '人工核价复核人',
  `included_statement_id` BIGINT COMMENT '确认结转生成的核价/差额已直接纳入本单，不能再入未来账单',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_fee_source (payor_entity_id,driver_id,source_event_key),
  KEY ix_fee_period (payor_entity_id,driver_id,service_ended_at),
  CONSTRAINT ck_fee_known CHECK ((pricing_status='PENDING_PRICING' AND amount_fen IS NULL) OR (pricing_status='READY' AND amount_fen IS NOT NULL)),
  CONSTRAINT ck_66_fadb078c6aab CHECK (`pricing_status` IN ('PENDING_PRICING','READY')),
  CONSTRAINT ck_ver_driver_fee_ledger CHECK (version >= 0),
  CONSTRAINT ck_driver_fee_ledger_entry_kind CHECK (`entry_kind` IN ('NORMAL_RETURN','CANCEL_RETURN','CANCEL_BEFORE_PICKUP','ADJUSTMENT','INITIAL_VALUATION','PRICE_DELTA')),
  CONSTRAINT ck_driver_fee_ledger_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='按真实承接形成的不可覆盖费用台账';

-- 单主体单司机单币种对账单 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_no` VARCHAR(64) NOT NULL COMMENT '对外账单编号',
  `driver_id` BIGINT NOT NULL COMMENT '代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '应付主体',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `service_range_start` DATETIME(3) NULL COMMENT '当前有效明细最早服务结束日UTC；仅说明跨度，不用于组单；空草稿NULL',
  `service_range_end_exclusive` DATETIME(3) NULL COMMENT '有效明细最晚结束日次日UTC；仅说明，不限定成员；空草稿NULL',
  `cutoff_at` DATETIME(3) NOT NULL COMMENT '本次数据截点',
  `status` VARCHAR(24) NOT NULL COMMENT 'StatementStatus: DRAFT,INTERNAL_REVIEW,SENT,DISPUTED,CONFIRMED,VOID',
  `revision` INT NOT NULL DEFAULT 1 COMMENT '未确认修订版本',
  `amount_fen` BIGINT NULL COMMENT '草稿有未核价行时NULL；不是0；金额齐全时为净拟应付',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '当前工作副本规范化摘要，可包含NULL；发送前金额必须完整',
  `issued_by` BIGINT NOT NULL COMMENT '平台制作人',
  `sent_at` DATETIME(3) COMMENT '发出时冻结当前版本',
  `confirmed_account_id` BIGINT COMMENT '本人微信账户确认',
  `confirmed_at` DATETIME(3) COMMENT '实际确认时间',
  `confirmation_revision` INT COMMENT '确认的版本',
  `voided_at` DATETIME(3) COMMENT '仅未确认账单可作废',
  `known_amount_fen` BIGINT NOT NULL DEFAULT 0 COMMENT '已填写金额小计；不能冒充完整应付',
  `unpriced_count` INT NOT NULL DEFAULT 0 COMMENT '待填写行数，发送和确认要求为0',
  `manual_price_count` INT NOT NULL DEFAULT 0 COMMENT '本次有人工填价/改价的行数',
  `review_status` VARCHAR(24) NOT NULL DEFAULT 'NOT_REQUIRED' COMMENT 'StatementReviewStatus: NOT_REQUIRED,REQUIRED,PENDING,APPROVED,REJECTED',
  `review_revision` INT COMMENT '仅对匹配revision和hash的整单复核生效',
  `published_revision` INT COMMENT '当前发送给司机的版本；撤回后失效',
  `published_hash` BINARY(32) COMMENT '当前有效公开快照SHA256，与public_snapshot_hash一致；内部复核用snapshot_hash',
  `submitted_by` BIGINT COMMENT '复核提交人，需与复核人分离',
  `selection_mode` VARCHAR(24) NOT NULL DEFAULT 'EXPLICIT' COMMENT 'SelectionMode: EXPLICIT',
  `filter_snapshot` JSON NULL COMMENT '查询条件留痕，不是组单成员定义；不能据此重跑批量选择',
  `active_entry_count` INT NOT NULL DEFAULT 0 COMMENT '当前有效工作行数；0草稿可保存不可发出',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_no (statement_no),
  KEY ix_statement_owner (driver_id,payor_entity_id,created_at,status),
  CONSTRAINT ck_statement_range CHECK ((service_range_start IS NULL AND service_range_end_exclusive IS NULL) OR (service_range_start IS NOT NULL AND service_range_end_exclusive IS NOT NULL AND service_range_end_exclusive>service_range_start)),
  CONSTRAINT ck_66_09ea0dc1cb1b CHECK (`status` IN ('DRAFT','INTERNAL_REVIEW','SENT','DISPUTED','CONFIRMED','VOID')),
  CONSTRAINT ck_66_8c948c332a52 CHECK (`review_status` IN ('NOT_REQUIRED','REQUIRED','PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_66_83b18b98eb4a CHECK (`selection_mode` IN ('EXPLICIT')),
  CONSTRAINT ck_ver_driver_statement CHECK (version >= 0),
  CONSTRAINT ck_driver_statement_currency CHECK (`currency` IN ('CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单主体单司机单币种对账单';

-- 对账工作明细；历史发布版本另表保存 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_line` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '对账单',
  `statement_revision` INT NOT NULL COMMENT '所属账单修订',
  `ledger_id` BIGINT NOT NULL COMMENT '费用源条目',
  `assignment_id` BIGINT NOT NULL COMMENT '承接关联',
  `amount_fen` BIGINT NULL COMMENT '本工作版本拟金额，可空；已发送快照必须完整',
  `snapshot` JSON NOT NULL COMMENT '车牌/订单/服务时间/类型/规则/金额',
  `line_hash` BINARY(32) NOT NULL COMMENT '内容摘要',
  `reference_amount_fen` BIGINT NULL COMMENT '规则/源台账金额，只读，NULL不伪装成0',
  `pricing_source` VARCHAR(24) NOT NULL COMMENT 'PricingSource: RULE,UNPRICED,MANUAL_FILLED,MANUAL_CHANGED,APPROVED_ADJUSTMENT',
  `manual_changed` TINYINT NOT NULL DEFAULT 0 COMMENT '人工参与标记；改回原金额不擦除操作轨迹',
  `price_reason` VARCHAR(1000) COMMENT '当前拟价依据',
  `last_editor_user_id` BIGINT COMMENT '最近编辑人；复核排除需查本轮全部改价人',
  `source_version` BIGINT NOT NULL COMMENT '源台账快照版本',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '0为从草稿移出，保留价格与审计；只对有效行求合计和发布快照',
  `removed_at` DATETIME(3) NULL COMMENT '最近移出时间',
  `removed_by` BIGINT NULL COMMENT '移出操作者',
  `remove_reason` VARCHAR(1000) NULL COMMENT '移出原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_source (statement_id,ledger_id),
  KEY ix_statement_lines (statement_id,id),
  KEY ix_statement_active_lines (statement_id,is_active,id),
  CONSTRAINT ck_66_e02515a7b248 CHECK (`pricing_source` IN ('RULE','UNPRICED','MANUAL_FILLED','MANUAL_CHANGED','APPROVED_ADJUSTMENT')),
  CONSTRAINT ck_ver_driver_statement_line CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账工作明细；历史发布版本另表保存';

-- 每费用条目至多一个活跃账单的分配历史 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_ledger_allocation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `ledger_id` BIGINT NOT NULL COMMENT '费用条目',
  `statement_id` BIGINT NOT NULL COMMENT '有效账单',
  `released_at` DATETIME(3) COMMENT '草稿合法移出或作废释放；已确认未付不能释放',
  `active_ledger` BIGINT GENERATED ALWAYS AS (IF(released_at IS NULL,ledger_id,NULL)) STORED COMMENT '每费用条目至多一个有效账单',
  `release_reason` VARCHAR(1000) NULL COMMENT '精确释放原因',
  `released_by` BIGINT NULL COMMENT '释放者',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_ledger (active_ledger),
  KEY ix_allocation_statement (statement_id),
  CONSTRAINT ck_ver_driver_ledger_allocation CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每费用条目至多一个活跃账单的分配历史';

-- 本人对账差异与处理记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_difference` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `statement_revision` INT NOT NULL COMMENT '被质疑版本',
  `driver_account_id` BIGINT NOT NULL COMMENT '本人',
  `entry_ids` JSON COMMENT '相关条目',
  `description` VARCHAR(2000) NOT NULL COMMENT '差异说明',
  `evidence_ids` JSON COMMENT '凭证引用',
  `status` VARCHAR(24) NOT NULL COMMENT 'StatementDifferenceStatus: OPEN,RESOLVED',
  `resolution` VARCHAR(2000) COMMENT '处理说明',
  `approved_adjustment_ids` JSON COMMENT '金额变更引用已审核调整',
  `resolved_by` BIGINT COMMENT '处理人',
  `resolved_at` DATETIME(3) COMMENT 'UTC',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY ix_difference_queue (status,created_at),
  CONSTRAINT ck_66_c02249164615 CHECK (`status` IN ('OPEN','RESOLVED')),
  CONSTRAINT ck_ver_driver_statement_difference CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人对账差异与处理记录';

-- 独立审核的费用调整 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_fee_adjustment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `source_entry_id` BIGINT NOT NULL COMMENT '源费用记录',
  `driver_id` BIGINT NOT NULL COMMENT '源代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '源主体',
  `amount_fen` BIGINT NOT NULL COMMENT '非0有符号调整',
  `reason` VARCHAR(2000) NOT NULL COMMENT '业务依据',
  `evidence_ids` JSON NOT NULL COMMENT '必填凭证引用',
  `status` VARCHAR(16) NOT NULL COMMENT 'AdjustmentStatus: PENDING,APPROVED,REJECTED',
  `created_by` BIGINT NOT NULL COMMENT '经办人',
  `reviewer_id` BIGINT COMMENT '独立审核人',
  `opinion` VARCHAR(1000) COMMENT '意见',
  `generated_ledger_id` BIGINT COMMENT '审批后唯一派生费用条目',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_generated_entry (generated_ledger_id),
  CONSTRAINT ck_adjust_nonzero CHECK (amount_fen<>0),
  CONSTRAINT ck_66_32e34f614311 CHECK (`status` IN ('PENDING','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_driver_fee_adjustment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='独立审核的费用调整';

-- 外部已付款证据登记与核实，不发起转账 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_external_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '已确认对账单',
  `driver_id` BIGINT NOT NULL COMMENT '收款代驾',
  `payor_entity_id` BIGINT NOT NULL COMMENT '付款主体',
  `amount_fen` BIGINT NOT NULL COMMENT '正数已发生付款',
  `external_reference_cipher` VARBINARY(1024) NOT NULL COMMENT '外部流水密文',
  `reference_lookup` BINARY(32) NOT NULL COMMENT '带密钥查询摘要',
  `paid_at` DATETIME(3) NOT NULL COMMENT '外部付款时间',
  `evidence_ids` JSON NOT NULL COMMENT '凭证',
  `status` VARCHAR(24) NOT NULL COMMENT 'ExternalPaymentStatus: PENDING,VERIFIED,REJECTED,REVERSED',
  `registered_by` BIGINT NOT NULL COMMENT '登记人',
  `verified_by` BIGINT COMMENT '不可同一真实身份',
  `verified_at` DATETIME(3) COMMENT '核对时间',
  `reason` VARCHAR(1000) COMMENT '核对说明/冲正原因',
  `reversal_of` BIGINT COMMENT '有错通过冲正记录处理',
  `statement_revision` INT NOT NULL COMMENT '关联的已确认内容版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP(3)，不依赖连接时区',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_external_reference (payor_entity_id,reference_lookup),
  KEY ix_statement_payment (statement_id,status),
  CONSTRAINT ck_external_positive CHECK (amount_fen>0),
  CONSTRAINT ck_66_cd7059501d32 CHECK (`status` IN ('PENDING','VERIFIED','REJECTED','REVERSED')),
  CONSTRAINT ck_ver_driver_external_payment CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部已付款证据登记与核实，不发起转账';

-- 小程序账号安全版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_account_security` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT 'vi_account',
  `session_epoch` BIGINT NOT NULL DEFAULT 0 COMMENT '全局退出/安全封禁代际',
  `permission_version` BIGINT NOT NULL DEFAULT 0 COMMENT '角色/能力变化版本，不等于资料version',
  `security_status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'AccountStatus: ACTIVE,LOCKED',
  `changed_at` DATETIME(3) NOT NULL COMMENT '最近权限/安全变化时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_account_security (account_id),
  CONSTRAINT ck_66_553d429ab631 CHECK (`security_status` IN ('ACTIVE','LOCKED')),
  CONSTRAINT ck_ver_account_security CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小程序账号安全版本';

-- 小程序独立设备会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `account_id` BIGINT NOT NULL COMMENT '基础账号',
  `app_id` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '微信应用',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT 'SessionStatus: ACTIVE,REVOKED,EXPIRED',
  `session_epoch` BIGINT NOT NULL COMMENT '签发时代际',
  `context_version` BIGINT NOT NULL DEFAULT 1 COMMENT '当前设备角色/站点切换版本',
  `active_role` VARCHAR(24) NOT NULL DEFAULT 'OWNER' COMMENT 'OWNER/DRIVER/STATION；不是授权来源',
  `station_id` BIGINT COMMENT '只存获授权当前站点上下文',
  `linked_sys_user_id` BIGINT COMMENT '可信受控绑定快照；每次工作操作再查实时状态',
  `absolute_expires_at` DATETIME(3) NOT NULL COMMENT '会话绝对截止，不无限滑动',
  `revoked_at` DATETIME(3) COMMENT '退出或安全撤销',
  `device_label` VARCHAR(120) COMMENT '最小必要设备标识摘要，不作为授权凭据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  KEY ix_session_account (account_id,status),
  KEY ix_session_expiry (status,absolute_expires_at),
  CONSTRAINT ck_66_b41d9706dfcd CHECK (`status` IN ('ACTIVE','REVOKED','EXPIRED')),
  CONSTRAINT ck_ver_session CHECK (version >= 0),
  CONSTRAINT ck_session_active_role CHECK (`active_role` IN ('OWNER','DRIVER','STATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小程序独立设备会话';

-- 访问与刷新令牌摘要 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_session_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `session_id` BIGINT NOT NULL COMMENT 'vi_session',
  `token_hash` BINARY(32) NOT NULL COMMENT '安全随机令牌的摘要；不存明文',
  `token_type` VARCHAR(16) NOT NULL COMMENT 'SessionTokenType: ACCESS,REFRESH',
  `generation` BIGINT NOT NULL COMMENT '刷新令牌代际',
  `context_version` BIGINT COMMENT 'ACCESS绑定上下文',
  `parent_token_id` BIGINT COMMENT '刷新祖先关系',
  `replaced_by_token_id` BIGINT COMMENT '轮换后的令牌引用',
  `expires_at` DATETIME(3) NOT NULL COMMENT '到期',
  `consumed_at` DATETIME(3) COMMENT '刷新原子消费时间',
  `revoked_at` DATETIME(3) COMMENT '撤销时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_token_hash (token_hash),
  KEY ix_token_session (session_id,token_type,generation),
  KEY ix_token_expiry (expires_at),
  CONSTRAINT ck_66_dafe3b97503e CHECK (`token_type` IN ('ACCESS','REFRESH')),
  CONSTRAINT ck_ver_session_token CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访问与刷新令牌摘要';

-- 对账内填写与改价不可变记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_price_change` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `statement_line_id` BIGINT NOT NULL COMMENT '工作明细',
  `ledger_id` BIGINT NOT NULL COMMENT '不可变费用源',
  `revision` INT NOT NULL COMMENT '修改后的内容修订',
  `old_amount_fen` BIGINT NULL COMMENT '原拟价，未填写为NULL',
  `new_amount_fen` BIGINT NOT NULL COMMENT '新拟价，总额输入，不是重复加差额',
  `reason` VARCHAR(1000) NOT NULL COMMENT '修改原因或初次定价依据',
  `evidence_file_ids` JSON COMMENT '经权限及归属验证的文件引用',
  `actor_user_id` BIGINT NOT NULL COMMENT '从ADMIN会话读取',
  `actor_account_id` BIGINT COMMENT '与受控绑定一致的真实人员关联，可空',
  `idempotency_key` VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '同主体动作资源请求键',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_price_change_retry (actor_user_id,statement_line_id,idempotency_key),
  KEY ix_price_history (statement_id,revision,id),
  CONSTRAINT ck_price_nonnegative CHECK (new_amount_fen>=0),
  CONSTRAINT ck_ver_driver_price_change CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账内填写与改价不可变记录';

-- 整单内部复核记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `revision` INT NOT NULL COMMENT '提交内容版本',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '待审全部明细摘要',
  `snapshot_json` JSON NOT NULL COMMENT '冻结的明细与变更，不能只查当前值',
  `contributor_ids` JSON NOT NULL COMMENT '服务端查出的本轮所有实际改价人',
  `submitted_by` BIGINT NOT NULL COMMENT '提交人',
  `reviewed_by` BIGINT COMMENT '独立复核人',
  `status` VARCHAR(24) NOT NULL COMMENT 'InternalReviewRecordStatus: PENDING,APPROVED,REJECTED,WITHDRAWN',
  `opinion` VARCHAR(1000) COMMENT '复核或撤回意见',
  `decided_at` DATETIME(3) COMMENT '决定时间',
  `active_statement` BIGINT GENERATED ALWAYS AS (IF(status='PENDING',statement_id,NULL)) STORED COMMENT '每账单至多一份待审',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_active_statement_review (active_statement),
  KEY ix_statement_review_history (statement_id,revision),
  CONSTRAINT ck_66_081decdd6dd7 CHECK (`status` IN ('PENDING','APPROVED','REJECTED','WITHDRAWN')),
  CONSTRAINT ck_ver_driver_statement_review CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整单内部复核记录';

-- 已发送与确认的不可变版本快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `revision` INT NOT NULL COMMENT '内容版本',
  `snapshot_hash` BINARY(32) NOT NULL COMMENT '规范化SHA-256，包含主体司机币种和明细',
  `snapshot_json` JSON NOT NULL COMMENT '完整明细、理由、总额与审核来源',
  `amount_fen` BIGINT NOT NULL COMMENT '此版明确净应付',
  `published_by` BIGINT NOT NULL COMMENT '发送或复核人',
  `public_snapshot_json` JSON NOT NULL COMMENT '仅对本人发布的规范化字段，不含内部理由/改价者/复核者',
  `public_snapshot_hash` BINARY(32) NOT NULL COMMENT 'SHA256公开快照；MINI snapshotHash与此匹配，不用内部snapshot_hash',
  `published_at` DATETIME(3) NOT NULL COMMENT '版本发出时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '并发控制；不可变行不更新内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_statement_snapshot (statement_id,revision),
  CONSTRAINT ck_ver_driver_statement_snapshot CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='已发送与确认的不可变版本快照';

-- 对账成员变更不可变历史 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_driver_statement_membership_change` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串',
  `statement_id` BIGINT NOT NULL COMMENT '账单',
  `ledger_id` BIGINT NULL COMMENT '源条目；整单作废时可为空',
  `line_id` BIGINT NULL COMMENT '可重复激活的工作行',
  `action` VARCHAR(24) NOT NULL COMMENT 'CREATE/ADD/REMOVE/REACTIVATE/VOID',
  `revision_before` INT NOT NULL COMMENT '变更前内容版本',
  `revision_after` INT NOT NULL COMMENT '变更后内容版本',
  `actor_user_id` BIGINT NOT NULL COMMENT '真实授权后台操作者',
  `reason` VARCHAR(1000) NOT NULL COMMENT '操作依据',
  `snapshot_json` JSON NOT NULL COMMENT '集合变化及涉及行完整快照；不存凭据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC',
  PRIMARY KEY (`id`),
  KEY ix_membership_history (statement_id,revision_after,id),
  CONSTRAINT ck_driver_statement_membership_change_action CHECK (`action` IN ('CREATE','ADD','REMOVE','REACTIVATE','VOID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账成员变更不可变历史';

-- 普通取消处罚申诉记录 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_penalty_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `penalty_id` BIGINT NOT NULL COMMENT '处罚主键',
  `driver_id` BIGINT NOT NULL COMMENT '本人司机档案',
  `status` VARCHAR(24) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'AppealRecordStatus: SUBMITTED,APPROVED,REJECTED',
  `reason` VARCHAR(1000) NOT NULL COMMENT '本人说明',
  `evidence_file_ids` JSON NULL COMMENT '通过文件权限检查的证据引用',
  `submitted_at` DATETIME(3) NOT NULL COMMENT 'UTC提交时刻',
  `reviewer_user_id` BIGINT NULL COMMENT '独立后台审核人',
  `reviewed_at` DATETIME(3) NULL COMMENT 'UTC审核时刻',
  `public_opinion` VARCHAR(1000) NULL COMMENT '对本人公开结论',
  `active_penalty` BIGINT GENERATED ALWAYS AS (IF(status='SUBMITTED',penalty_id,NULL)) STORED COMMENT '同一处罚只允许一项待审申诉',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_pending_penalty_appeal (active_penalty),
  KEY ix_driver_appeal (driver_id,created_at),
  CONSTRAINT ck_66_623fb1878076 CHECK (`status` IN ('SUBMITTED','APPROVED','REJECTED')),
  CONSTRAINT ck_ver_penalty_appeal CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通取消处罚申诉记录';

-- 本人发起的受控员工绑定申请 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `account_id` BIGINT NOT NULL COMMENT '不可由客户端选择申请主体',
  `request_code` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '公开核对编号，不是授权秘密',
  `status` VARCHAR(32) NOT NULL DEFAULT 'REQUESTED' COMMENT 'EmployeeBindingStatus: REQUESTED,CREDENTIAL_ISSUED,BOUND,REJECTED,EXPIRED,REVOKED',
  `request_reason` VARCHAR(500) NOT NULL COMMENT '本人申请理由',
  `expires_at` DATETIME(3) NOT NULL COMMENT '到期不自动绑定',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_binding_request_code (request_code),
  KEY ix_binding_request_owner (account_id,status),
  CONSTRAINT ck_66_8efc7aae47ab CHECK (`status` IN ('REQUESTED','CREDENTIAL_ISSUED','BOUND','REJECTED','EXPIRED','REVOKED')),
  CONSTRAINT ck_ver_employee_binding_request CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人发起的受控员工绑定申请';

-- 管理员签发的一次性员工绑定凭证 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_employee_binding_credential` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `request_id` BIGINT NOT NULL COMMENT '绑定申请',
  `target_account_id` BIGINT NOT NULL COMMENT '凭证只能由此已认证account消费',
  `target_sys_user_id` BIGINT NOT NULL COMMENT '核对现有sys_user及其全部任职',
  `token_hash` BINARY(32) NOT NULL COMMENT '随机凭证摘要；不保存或日志输出明文',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ISSUED' COMMENT 'BindingCredentialStatus: ISSUED,CONSUMED,REVOKED,EXPIRED',
  `issued_by_user_id` BIGINT NOT NULL COMMENT '真正后台签发人',
  `expires_at` DATETIME(3) NOT NULL COMMENT 'UTC短期失效',
  `consumed_at` DATETIME(3) NULL COMMENT '一次性消费',
  `reason` VARCHAR(500) NOT NULL COMMENT '签发理由',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_binding_token_hash (token_hash),
  KEY ix_binding_credential_request (request_id,status),
  CONSTRAINT ck_66_ec6a315733a1 CHECK (`status` IN ('ISSUED','CONSUMED','REVOKED','EXPIRED')),
  CONSTRAINT ck_ver_employee_binding_credential CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员签发的一次性员工绑定凭证';

-- 代驾培训不可变课程版本 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `course_key` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '稳定课程编码',
  `course_version` INT NOT NULL COMMENT '课程内容版本',
  `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
  `description` TEXT NULL COMMENT '培训说明',
  `media_file_id` BIGINT NOT NULL COMMENT '私有培训媒体',
  `required_duration_seconds` INT NOT NULL COMMENT '规定学习时长；发布时校验>0',
  `pass_score` INT NOT NULL COMMENT '0至100的配置门槛',
  `attempt_time_limit_seconds` INT NOT NULL COMMENT '考试时间配置',
  `status` VARCHAR(24) NOT NULL DEFAULT 'DRAFT' COMMENT 'TrainingCourseStatus: DRAFT,PUBLISHED,DISABLED',
  `published_at` DATETIME(3) NULL COMMENT 'UTC发布时间',
  `published_by_user_id` BIGINT NULL COMMENT '后台发布人',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_course_revision (course_key,course_version),
  CONSTRAINT ck_course_score CHECK (pass_score BETWEEN 0 AND 100),
  CONSTRAINT ck_course_duration CHECK (required_duration_seconds>0 AND attempt_time_limit_seconds>=60),
  CONSTRAINT ck_66_0aa07ede2e69 CHECK (`status` IN ('DRAFT','PUBLISHED','DISABLED')),
  CONSTRAINT ck_ver_training_course CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾培训不可变课程版本';

-- 培训课程版本的后台题库 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `training_id` BIGINT NOT NULL COMMENT '确切课程版本',
  `question_code` VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '课程内稳定编码',
  `question_type` VARCHAR(24) NOT NULL COMMENT 'QuestionType: SINGLE,MULTIPLE,TRUE_FALSE',
  `prompt` TEXT NOT NULL COMMENT '题干',
  `options_json` JSON NOT NULL COMMENT 'code和text',
  `correct_option_codes` JSON NOT NULL COMMENT '仅课程管理/服务端阅卷可见，禁止MINI输出',
  `sort_no` INT NOT NULL COMMENT '排序',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_course_question (training_id,question_code),
  CONSTRAINT ck_66_3f48408ab1be CHECK (`question_type` IN ('SINGLE','MULTIPLE','TRUE_FALSE')),
  CONSTRAINT ck_ver_training_question CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训课程版本的后台题库';

-- 本人每个课程版本的学习进度 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `driver_id` BIGINT NOT NULL COMMENT '真正司机档案',
  `training_id` BIGINT NOT NULL COMMENT '课程版本主键',
  `verified_duration_seconds` INT NOT NULL DEFAULT 0 COMMENT '服务端按会话时间限幅的有效学习时长',
  `status` VARCHAR(24) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'TrainingStatus: NOT_STARTED,IN_PROGRESS,READY_FOR_EXAM,PASSED,FAILED',
  `last_position_seconds` INT NOT NULL DEFAULT 0 COMMENT '仅恢复位置，不等于有效学习时长',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_training_progress (application_id,training_id),
  CONSTRAINT ck_66_57bd05d422b1 CHECK (`status` IN ('NOT_STARTED','IN_PROGRESS','READY_FOR_EXAM','PASSED','FAILED')),
  CONSTRAINT ck_ver_training_progress CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人每个课程版本的学习进度';

-- 短期受控学习会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_playback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `progress_id` BIGINT NOT NULL COMMENT '本人进度',
  `session_id` BIGINT NOT NULL COMMENT '小程序设备会话',
  `started_at` DATETIME(3) NOT NULL COMMENT '服务端开始时间',
  `expires_at` DATETIME(3) NOT NULL COMMENT '服务端结束边界',
  `last_reported_at` DATETIME(3) NULL COMMENT '最近服务器接收时刻',
  `last_sequence` INT NOT NULL DEFAULT 0 COMMENT '乱序和重复不增加时长',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  KEY ix_playback_progress (progress_id,expires_at),
  CONSTRAINT ck_ver_training_playback CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短期受控学习会话';

-- 本人考试尝试及不可变题卷快照 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_training_attempt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `driver_id` BIGINT NOT NULL COMMENT '本人',
  `training_id` BIGINT NOT NULL COMMENT '确切课程版本',
  `attempt_no` INT NOT NULL COMMENT '本课程尝试序号',
  `status` VARCHAR(24) NOT NULL DEFAULT 'OPEN' COMMENT 'AttemptStatus: OPEN,SUBMITTED,PASSED,FAILED,EXPIRED',
  `question_snapshot` JSON NOT NULL COMMENT '确切题干/选项；无答案的对外投影单独构造',
  `answer_snapshot` JSON NULL COMMENT '本人作答',
  `score` INT NULL COMMENT '服务器阅卷0至100',
  `started_at` DATETIME(3) NOT NULL COMMENT 'UTC开始',
  `expires_at` DATETIME(3) NOT NULL COMMENT 'UTC失效',
  `submitted_at` DATETIME(3) NULL COMMENT 'UTC提交',
  `active_attempt` VARCHAR(128) GENERATED ALWAYS AS (IF(status='OPEN',CONCAT(application_id,':',training_id),NULL)) STORED COMMENT '同申请同课程仅一个开放尝试',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_attempt_number (application_id,training_id,attempt_no),
  UNIQUE KEY uk_open_training_attempt (active_attempt),
  CONSTRAINT ck_66_5b693ca15a96 CHECK (`status` IN ('OPEN','SUBMITTED','PASSED','FAILED','EXPIRED')),
  CONSTRAINT ck_ver_training_attempt CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人考试尝试及不可变题卷快照';

-- 本人实名活体启动与可信结果会话 | MYSQL_AUTO_INCREMENT
CREATE TABLE `vi_face_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库生成业务主键；插入不提供id',
  `application_id` BIGINT NOT NULL COMMENT '资格申请',
  `verification_id` BIGINT NULL COMMENT '供应商核验记录',
  `account_id` BIGINT NOT NULL COMMENT '本人基础账户',
  `status` VARCHAR(24) NOT NULL DEFAULT 'CREATED' COMMENT 'FaceSessionStatus: CREATED,PROCESSING,PASSED,FAILED,EXPIRED',
  `launch_mode` VARCHAR(24) NOT NULL DEFAULT 'WEBVIEW_URL' COMMENT 'FaceLaunchMode: WEBVIEW_URL',
  `launch_payload_cipher` BLOB NULL COMMENT '加密短期URL；失效或结束销毁',
  `consent_version` VARCHAR(64) NOT NULL COMMENT '此次明确同意版本',
  `expires_at` DATETIME(3) NOT NULL COMMENT '供应商与本地较短有效期',
  `result_available` TINYINT NOT NULL DEFAULT 0 COMMENT '可信查询后赋值',
  `failure_code` VARCHAR(64) NULL COMMENT '安全映射错误，无原始生物数据',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建时刻UTC',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3)',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '手写CAS版本',
  PRIMARY KEY (`id`),
  KEY ix_face_app_status (application_id,status),
  CONSTRAINT ck_66_b67f9761e849 CHECK (`status` IN ('CREATED','PROCESSING','PASSED','FAILED','EXPIRED')),
  CONSTRAINT ck_66_a153de145e1b CHECK (`launch_mode` IN ('WEBVIEW_URL')),
  CONSTRAINT ck_ver_face_session CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本人实名活体启动与可信结果会话';
