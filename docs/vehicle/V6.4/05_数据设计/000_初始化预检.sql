-- 只读。由执行者检查目标；出现已有vi表必须中止，不自动删除。
SELECT DATABASE() AS target_database, VERSION() AS mysql_version,
       @@session.time_zone AS session_tz, @@global.time_zone AS global_tz,
       UTC_TIMESTAMP(3) AS utc_now, CURRENT_TIMESTAMP(3) AS session_now;
SELECT table_name, table_rows FROM information_schema.tables
WHERE table_schema=DATABASE() AND LEFT(table_name,3)='vi_' ORDER BY table_name;
SELECT table_name FROM information_schema.tables
WHERE table_schema=DATABASE() AND table_name IN ('sys_user','sys_dept','sys_user_dept','sys_menu');
