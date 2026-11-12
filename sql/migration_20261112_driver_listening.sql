-- D01 公司每日听单配置、固定当日截止、最近定位与独立可达交互。
-- 前置：migration_20261110_qualification_review.sql；先备份vi_driver_profile、vi_driver_location、sys_menu及SHOW CREATE TABLE。
-- 暂停听单/定位/公司配置写入；先异常清单，再前向DDL、新表、稳定菜单、后验，最后重启后端。
-- DDL不可重复执行且隐式提交；不删除/缩窄已有列。部分失败保留现场，核对结构后恢复，不盲目重跑。
-- 旧开启记录期限/公司epoch及旧点来源保持NULL，不猜生产下班时间或伪造实时位置；只关闭旧意愿由统一到期服务收敛，不删除在手责任。
-- 没有自动开启任务；次日本人重新开启。回滚应用前停听单入口与任务，保留新列/表及真实事实，不删除数据回退。
-- 新环境直接使用完整初始化，不叠加本增量；菜单重复执行有防重，但稳定ID占用异常需先处理。
SELECT id,owner_dept_id,listening_enabled
FROM vi_driver_profile
WHERE listening_enabled=1
ORDER BY id;
SELECT driver_id,captured_at,received_at
FROM vi_driver_location
ORDER BY driver_id;
CREATE TEMPORARY TABLE d01_menu_guard (ok INT CHECK(ok=1));
INSERT INTO d01_menu_guard
SELECT CASE WHEN COUNT(*)=0 THEN 1 ELSE 0 END
FROM sys_menu
WHERE menu_id BETWEEN 2971500 AND 2971502;


ALTER TABLE vi_driver_profile
    ADD COLUMN `listening_revision` BIGINT NOT NULL DEFAULT 0 COMMENT 'D01本人听单独立CAS，不复用档案备注版本',
    ADD COLUMN `listening_until` DATETIME(3) NULL COMMENT '本人本次开启固定当日UTC截止；历史NULL不猜填',
    ADD COLUMN `listening_company_revision` BIGINT NULL COMMENT '开启时真实公司epoch；转公司不可复用',
    ADD COLUMN `listening_config_version` BIGINT NULL COMMENT '开启对应明确公司配置版本',
    ADD KEY ix_driver_listening_expiry (listening_enabled,listening_until,id),
    ADD CONSTRAINT ck_driver_listening_revision CHECK (listening_revision >= 0);

ALTER TABLE vi_driver_location
    ADD COLUMN `owner_dept_id` BIGINT NULL COMMENT '最近点真实父档案公司；历史NULL不可信',
    ADD COLUMN `company_revision` BIGINT NULL COMMENT '采点时真实公司epoch',
    ADD COLUMN `session_id` BIGINT NULL COMMENT '实际MINI采点会话',
    ADD COLUMN `context_version` BIGINT NULL COMMENT '实际会话视角版本',
    ADD COLUMN `accuracy_meter` INT NULL COMMENT '设备报告精度；未知精度不能作为候选点',
    ADD COLUMN `valid_until` DATETIME(3) NULL COMMENT '按采集与接收较早时间计算UTC有效期',
    ADD COLUMN `config_version` BIGINT NULL COMMENT '计算有效期的公司配置版本',
    ADD KEY ix_driver_location_company (owner_dept_id,driver_id),
    ADD CONSTRAINT ck_driver_location_accuracy CHECK (accuracy_meter IS NULL OR accuracy_meter BETWEEN 0 AND 100000);

CREATE TABLE `vi_driver_presence` (
  `driver_id` BIGINT NOT NULL COMMENT '真实父代驾档案主键',
  `owner_dept_id` BIGINT NOT NULL COMMENT '实际所属公司',
  `company_revision` BIGINT NOT NULL COMMENT '真实公司epoch',
  `session_id` BIGINT NOT NULL COMMENT '实际MINI交互会话',
  `context_version` BIGINT NOT NULL COMMENT '真实会话视角版本',
  `client_state` VARCHAR(16) NOT NULL COMMENT 'DriverClientState: FOREGROUND,BACKGROUND',
  `observed_at` DATETIME(3) NOT NULL COMMENT '客户端事件UTC时间',
  `received_at` DATETIME(3) NOT NULL COMMENT '服务端收到认证事件UTC时间',
  `valid_until` DATETIME(3) NOT NULL COMMENT '最近交互有效UTC截止，不保证后续网络',
  `config_version` BIGINT NOT NULL COMMENT '公司配置版本',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '独立CAS，首次真实写入版本1',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建UTC时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间，由Mapper显式维护',
  PRIMARY KEY (`driver_id`),
  KEY ix_driver_presence_company (owner_dept_id,driver_id),
  CONSTRAINT ck_driver_presence_state CHECK (`client_state` IN ('FOREGROUND','BACKGROUND')),
  CONSTRAINT ck_driver_presence_version CHECK (version>=0 AND company_revision>=0 AND context_version>=0 AND config_version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代驾最近认证交互；不保证后台持续可达';

CREATE TABLE `vi_driver_listening_config` (
  `owner_dept_id` BIGINT NOT NULL COMMENT '明确公司主键，不从管理员主部门推断',
  `closing_time` CHAR(5) NOT NULL COMMENT 'Asia/Shanghai每日HH:mm下班时间',
  `location_max_age_seconds` INT NOT NULL COMMENT '31..86400，位置有效期，无生产默认',
  `reachability_max_age_seconds` INT NOT NULL COMMENT '31..86400，最近认证交互有效期，无生产默认',
  `max_accuracy_meter` INT NOT NULL COMMENT '1..100000，允许位置精度上限',
  `location_report_interval_seconds` INT NOT NULL COMMENT '30..3600且小于两种有效期；本系统节流约束',
  `updated_by_user_id` BIGINT NOT NULL COMMENT '最后真实ADMIN操作者',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '独立CAS，首次真实写入版本1',
  `created_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT '创建UTC时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT (UTC_TIMESTAMP(3)) COMMENT 'UTC更新时间，由Mapper显式维护',
  PRIMARY KEY (`owner_dept_id`),
  CONSTRAINT ck_driver_listening_time CHECK (closing_time REGEXP '^([01][0-9]|2[0-3]):[0-5][0-9]$'),
  CONSTRAINT ck_driver_listening_config_ranges CHECK (
    location_max_age_seconds BETWEEN 31 AND 86400
    AND reachability_max_age_seconds BETWEEN 31 AND 86400
    AND max_accuracy_meter BETWEEN 1 AND 100000
    AND location_report_interval_seconds BETWEEN 30 AND 3600
    AND location_report_interval_seconds < location_max_age_seconds
    AND location_report_interval_seconds < reachability_max_age_seconds
  ),
  CONSTRAINT ck_driver_listening_config_version CHECK (version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='同公司共用每日听单截止与位置/可达参数';

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971500,'公司听单配置',2971000,5,'listening-config','rongli/dispatch/listening-config',1,1,'C','0','0','biz:dispatchlistening:query','time','system',SYSDATE(),'D01；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971500);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971501,'听单配置查询',2971500,1,'#',NULL,1,1,'F','0','0','biz:dispatchlistening:query','time','system',SYSDATE(),'D01；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971501);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT 2971502,'听单配置维护',2971500,2,'#',NULL,1,1,'F','0','0','biz:dispatchlistening:edit','time','system',SYSDATE(),'D01；仅建菜单，不自动授权角色'
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE menu_id=2971502);

DROP TEMPORARY TABLE d01_menu_guard;

-- 后验NULL历史只报告，不猜填；上线前通过公司后台显式配置。

SELECT COUNT(*) AS legacy_listening_without_deadline
FROM vi_driver_profile
WHERE listening_enabled=1 AND listening_until IS NULL;

SELECT COUNT(*) AS untrusted_legacy_locations
FROM vi_driver_location
WHERE session_id IS NULL OR company_revision IS NULL OR valid_until IS NULL;

SELECT menu_id,perms
FROM sys_menu
WHERE menu_id BETWEEN 2971500 AND 2971502
ORDER BY menu_id;
