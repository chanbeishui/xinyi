-- =============================================================================
-- migration_20261022_02_align_v64_constraints.sql
-- 用途：将 xinyi 库 vi_ 表 CHECK 约束收敛到 V6.4 001 权威结构（用户选定“对齐纯 V6.4 001”）。
--   仅处理 migration_20261022_01 未覆盖的表；01 已处理 vi_account/vi_employee_binding/
--   vi_payment_order/vi_refund_order 的列与约束变更。
--   多余约束（E01 R1 给 V6.1 补、V6.4 001 未采用的语义名枚举 CHECK）→ DROP；
--   这些枚举改由应用层（E06 ViErrorCode + 业务 Service）+ 枚举字典 + COMMENT 约束；
--   actor_realm 等按 ADR-02 为服务端受控写入。R1 分析价值保留在 E01 记录，DB 约束以 V6.4 为准。
-- 前置：已执行 migration_20261022_01_integration_config.sql。
-- 备份：执行前须 mysqldump（已备份 .vi-local/backup/xinyi_pre_v64_*.sql）。
-- 可重复性：不可重复执行（DROP 具名约束，重复报错暴露误重跑）。
-- 数据安全：仅 DROP CHECK 约束，不改数据、不 DROP 表；相关 vi_ 表当前 0 行，无数据风险。
-- 权威来源：V6.4 001；由 v64_adapt/gen_align_constraints.py 对 V6.1↔V6.4 两份 SQL 静态 diff 生成（可复现）。
-- =============================================================================
ALTER TABLE `vi_aftersale_case` DROP CHECK `ck_aftersale_case_kind`;
ALTER TABLE `vi_aftersale_case` DROP CHECK `ck_aftersale_case_refund_state`;
ALTER TABLE `vi_aftersale_case` DROP CHECK `ck_aftersale_case_return_state`;
ALTER TABLE `vi_aftersale_review` DROP CHECK `ck_aftersale_review_decision`;
ALTER TABLE `vi_dispatch_invite` DROP CHECK `ck_dispatch_invite_source`;
ALTER TABLE `vi_driver_fee_ledger` DROP CHECK `ck_driver_fee_ledger_currency`;
ALTER TABLE `vi_driver_fee_ledger` DROP CHECK `ck_driver_fee_ledger_entry_kind`;
ALTER TABLE `vi_driver_fee_rule` DROP CHECK `ck_driver_fee_rule_currency`;
ALTER TABLE `vi_driver_statement` DROP CHECK `ck_driver_statement_currency`;
ALTER TABLE `vi_driver_statement_membership_change` DROP CHECK `ck_driver_statement_membership_change_action`;
ALTER TABLE `vi_event_log` DROP CHECK `ck_event_log_actor_realm`;
ALTER TABLE `vi_evidence_group` DROP CHECK `ck_evidence_group_mileage_source`;
ALTER TABLE `vi_inspection_result` DROP CHECK `ck_inspection_result_conclusion`;
ALTER TABLE `vi_inspection_result` DROP CHECK `ck_inspection_result_source`;
ALTER TABLE `vi_session` DROP CHECK `ck_session_active_role`;
-- ---------------------------------------------------------------------------
-- V6.4 001 自身缺陷（引用不存在的列，无法建立；已在 ry 完整初始化与本库同步剔除，
--   不作为 ADD 目标，也不误删同名既有约束；详见 E03 V6.4 前向适配报告 finding）：
--   vi_driver_statement_membership_change.ck_ver_driver_statement_membership_change 引用不存在列 ['version']
-- 执行后校验：python docs/implementation/vehicle/v64_adapt/verify_schema.py 应输出 STRUCTURE_CONSISTENT。
