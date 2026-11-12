-- D02：公司派单策略、冻结服务公司、持久轮转批次与真实邀约唯一占用。
-- 前置：migration_20261112_driver_listening.sql；顺序：备份→前验异常→DDL→菜单→后验→重启。
-- 执行前备份vi_order、vi_dispatch_invite、vi_dispatch_cursor、vi_driver_occupancy、sys_menu及SHOW CREATE TABLE。
-- 暂停建单/派单/邀约响应/取消审批写入。DDL隐式提交且不可重复执行，部分失败核现场，不盲目重跑。
-- 历史service_company_id/策略/batch_id保持NULL，不从当前站点或收款记录猜回填；统一调度转人工协调。
-- 若发现同司机多份WAIT_RESPONSE，输出下列异常并拒绝添加唯一约束；不可静默撤销或删除真实历史。
-- 新环境使用完整初始化，不叠加本增量。菜单稳定ID冲突拒绝，不自动授权角色。
-- 回滚：先关闭派单/邀约入口与任务，保留新表/列和真实承接事实，禁止删除数据回退。
SELECT driver_id,COUNT(*) AS pending_invites
FROM vi_dispatch_invite
WHERE status='WAIT_RESPONSE'
GROUP BY driver_id
HAVING COUNT(*)>1;
SELECT id,source FROM vi_dispatch_invite WHERE source NOT IN ('AUTO','MANUAL');
CREATE TEMPORARY TABLE d02_migration_guard (ok INT CHECK(ok=1));
INSERT INTO d02_migration_guard
SELECT CASE WHEN COUNT(*)=0 THEN 1 ELSE 0 END
FROM (SELECT driver_id FROM vi_dispatch_invite WHERE status='WAIT_RESPONSE'
      GROUP BY driver_id HAVING COUNT(*)>1) duplicate_drivers;
INSERT INTO d02_migration_guard
SELECT CASE WHEN COUNT(*)=0 THEN 1 ELSE 0 END
FROM sys_menu
WHERE menu_id BETWEEN 2971600 AND 2971607;


ALTER TABLE vi_order
    ADD COLUMN `service_company_id` BIGINT NULL COMMENT '下单时冻结站点所属服务公司；历史未知不猜回填',
    ADD COLUMN `dispatch_policy_version` BIGINT NULL COMMENT '下单时冻结公司派单策略版本；缺失转人工',
    ADD COLUMN `dispatch_policy_snapshot` JSON NULL COMMENT '冻结公司派单策略及内容认证，不追用当前策略',
    ADD KEY ix_order_service_company (service_company_id,id);

ALTER TABLE vi_dispatch_invite
    ADD COLUMN `batch_id` BIGINT NULL COMMENT '所属持久派单批次；历史NULL不猜',
    ADD COLUMN `service_company_id` BIGINT NULL COMMENT '原订单冻结服务公司',
    ADD COLUMN `company_revision` BIGINT NULL COMMENT '发邀约时真实司机公司epoch',
    ADD COLUMN `distance_meter` INT NULL COMMENT 'GCJ02直线距离，非道路里程',
    ADD COLUMN `driver_session_id` BIGINT NULL COMMENT '候选位置和交互绑定的真实有效设备会话',
    ADD COLUMN `driver_context_version` BIGINT NULL COMMENT '可信设备DRIVER上下文版本',
    ADD COLUMN `driver_location_version` BIGINT NULL COMMENT '发出时最近点版本',
    ADD COLUMN `driver_presence_version` BIGINT NULL COMMENT '发出时最近交互版本',
    ADD COLUMN `issued_by_user_id` BIGINT NULL COMMENT '人工邀约操作者，自动为NULL',
    ADD COLUMN `reason_cipher` MEDIUMBLOB NULL COMMENT '人工或响应原因的认证加密封装',
    ADD COLUMN `active_driver` BIGINT GENERATED ALWAYS AS (IF(status='WAIT_RESPONSE',driver_id,NULL)) STORED COMMENT '同司机最多一份待响应邀约，旧终态不占用',
    ADD UNIQUE KEY uk_dispatch_batch_driver (batch_id,driver_id),
    ADD UNIQUE KEY uk_dispatch_active_driver (active_driver),
    ADD KEY ix_dispatch_batch_status (batch_id,status,id),
    ADD CONSTRAINT ck_dispatch_invite_distance CHECK (distance_meter IS NULL OR distance_meter>=0),
    ADD CONSTRAINT ck_dispatch_invite_source CHECK (`source` IN ('AUTO','MANUAL'));

CREATE TABLE `vi_dispatch_policy` (
  `owner_dept_id` BIGINT NOT NULL COMMENT '权威服务公司，不从管理员主部门推断',
  `invite_response_seconds` INT NOT NULL COMMENT '1..3600，明确配置无默认',
  `radius_meter` INT NOT NULL COMMENT '1..1000000，初圈直线半径',
  `expanded_radius_meter` INT NOT NULL COMMENT '不小于初圈且不超过1000000',
  `overall_deadline_seconds` INT NOT NULL COMMENT '1..86400，自动总截止',
  `license_compatibility` JSON NOT NULL COMMENT '明确业务车型与准驾类别允许集合，不自动推导法律准驾',
  `config_hash` BINARY(32) NOT NULL COMMENT '策略内容HMAC认证摘要',
  `updated_by_user_id` BIGINT NOT NULL COMMENT '真实后台操作者',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '独立CAS，首次成功写入1',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建UTC时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间，Mapper显式维护',
  PRIMARY KEY (`owner_dept_id`),
  CONSTRAINT ck_dispatch_policy_ranges CHECK (
    invite_response_seconds BETWEEN 1 AND 3600
    AND radius_meter BETWEEN 1 AND 1000000
    AND expanded_radius_meter BETWEEN radius_meter AND 1000000
    AND overall_deadline_seconds BETWEEN 1 AND 86400
  ),
  CONSTRAINT ck_dispatch_policy_license CHECK (
    JSON_TYPE(license_compatibility)='ARRAY' AND JSON_LENGTH(license_compatibility) BETWEEN 1 AND 200
  ),
  CONSTRAINT ck_dispatch_policy_version CHECK (version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同公司共享的明确派单策略；新订单冻结，不追改旧单';

CREATE TABLE `vi_dispatch_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库自增Long，API十进制字符串',
  `order_id` BIGINT NOT NULL COMMENT '原订单',
  `run_no` INT NOT NULL COMMENT '本订单递增批次号',
  `service_company_id` BIGINT NOT NULL COMMENT '原订单冻结服务公司',
  `source` VARCHAR(16) NOT NULL COMMENT 'DispatchSource: AUTO,MANUAL',
  `status` VARCHAR(24) NOT NULL COMMENT 'DispatchBatchStatus: RUNNING,WAIT_RESPONSE,PAUSED,ASSIGNED,MANUAL,CLOSED',
  `policy_version` BIGINT NOT NULL COMMENT '原订单冻结策略版本',
  `policy_snapshot` JSON NOT NULL COMMENT '原订单明确策略和认证，不随当前配置变化',
  `started_at` DATETIME(3) NOT NULL COMMENT '开始UTC时刻',
  `deadline_at` DATETIME(3) NOT NULL COMMENT '绝对总截止，与接车开始取较早者',
  `radius_stage` INT NOT NULL DEFAULT 0 COMMENT '0初圈，1唯一扩圈',
  `reason_code` VARCHAR(64) NULL COMMENT '当前转人工或暂停原因，非内部明文意见',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT 'CAS版本',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建UTC时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间，Mapper显式维护',
  `active_order` BIGINT GENERATED ALWAYS AS (
    IF(status IN ('RUNNING','WAIT_RESPONSE','PAUSED'),order_id,NULL)
  ) STORED COMMENT '每订单唯一活动派单批次',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_dispatch_batch_run (order_id,run_no),
  UNIQUE KEY uk_dispatch_batch_active_order (active_order),
  KEY ix_dispatch_batch_deadline (status,deadline_at,id),
  CONSTRAINT ck_dispatch_batch_source CHECK (`source` IN ('AUTO','MANUAL')),
  CONSTRAINT ck_dispatch_batch_status CHECK (
    `status` IN ('RUNNING','WAIT_RESPONSE','PAUSED','ASSIGNED','MANUAL','CLOSED')
  ),
  CONSTRAINT ck_dispatch_batch_version CHECK (version>=0 AND run_no>0 AND policy_version>=0),
  CONSTRAINT ck_dispatch_batch_radius CHECK (radius_stage IN (0,1)),
  CONSTRAINT ck_dispatch_batch_deadline CHECK (deadline_at>started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单订单持久轮转批次，绝对截止和一次扩圈';

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971600,'调度管理',0,8,'dispatch',NULL,1,1,
       'M','0','0','','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971600);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971601,'调度队列',2971600,1,'queue','rongli/dispatch/queue',1,1,
       'C','0','0','biz:dispatch:list','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971601);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971602,'邀约记录',2971600,2,'invites','rongli/dispatch/invites',1,1,
       'C','0','0','biz:dispatch:list','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971602);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971603,'公司派单策略',2971600,3,'policy','rongli/dispatch/policy',1,1,
       'C','0','0','biz:dispatchpolicy:query','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971603);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971604,'派单策略查询',2971603,1,'#',NULL,1,1,
       'F','0','0','biz:dispatchpolicy:query','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971604);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971605,'派单策略维护',2971603,2,'#',NULL,1,1,
       'F','0','0','biz:dispatchpolicy:edit','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971605);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971606,'发出人工邀约',2971601,1,'#',NULL,1,1,
       'F','0','0','biz:dispatch:create','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971606);

INSERT INTO sys_menu (
  menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,
  menu_type,visible,status,perms,icon,create_by,create_time,remark
)
SELECT 2971607,'撤回未响应邀约',2971601,2,'#',NULL,1,1,
       'F','0','0','biz:dispatch:revoke','guide','system',SYSDATE(),'D02；不自动授予角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971607);

DROP TEMPORARY TABLE d02_migration_guard;
-- 后验：旧缺失只报告，不直接修复；生产运营参数须由后台明确维护。
SELECT COUNT(*) AS legacy_without_service_company
FROM vi_order WHERE service_company_id IS NULL;
SELECT COUNT(*) AS legacy_without_policy
FROM vi_order WHERE dispatch_policy_version IS NULL OR dispatch_policy_snapshot IS NULL;
SELECT COUNT(*) AS legacy_invites_without_batch
FROM vi_dispatch_invite WHERE batch_id IS NULL;
SELECT menu_id,perms FROM sys_menu
WHERE menu_id BETWEEN 2971600 AND 2971607 ORDER BY menu_id;
