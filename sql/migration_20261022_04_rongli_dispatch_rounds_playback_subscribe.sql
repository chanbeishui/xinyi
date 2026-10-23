-- =====================================================================
-- 荣利 · D02 多轮扩圈 / Q03 播放心跳 / N01 订阅消息 支撑迁移
-- 幂等：可重复执行
-- 执行：mysql -h127.0.0.1 -P3307 -uroot -p*** --default-character-set=utf8mb4 xinyi < 本文件
-- =====================================================================

-- ---------------------------------------------------------------- D02 扩圈轮次
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_dispatch_invite' AND COLUMN_NAME = 'round_no');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_dispatch_invite` ADD COLUMN `round_no` INT NOT NULL DEFAULT 1 COMMENT ''扩圈轮次，第 1 轮为初始半径'' AFTER `round_key`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c := (SELECT COUNT(1) FROM information_schema.STATISTICS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_dispatch_invite' AND INDEX_NAME = 'ix_order_round');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_dispatch_invite` ADD KEY `ix_order_round` (`order_id`, `round_no`)',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------- Q03 播放会话
-- session_id 原为 BIGINT，但服务端签发的是 Base64Url(32B) 字符串，
-- 严格模式下插入直接报 1366，导致「开始学习」接口不可用；改为定长字符串。
ALTER TABLE `vi_training_playback`
  MODIFY COLUMN `session_id` VARCHAR(64) NOT NULL COMMENT '服务端签发的学习会话串';

SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_training_playback' AND COLUMN_NAME = 'last_position_seconds');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_training_playback` ADD COLUMN `last_position_seconds` INT NOT NULL DEFAULT 0 COMMENT ''最近一次上报的播放位置，用于位置单调与跳变校验'' AFTER `last_sequence`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------- N01 订阅消息
SET @c := (SELECT COUNT(1) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vi_notification' AND COLUMN_NAME = 'channel');
SET @s := IF(@c = 0,
  'ALTER TABLE `vi_notification` ADD COLUMN `channel` VARCHAR(16) NOT NULL DEFAULT ''INBOX'' COMMENT ''INBOX=仅站内；SUBSCRIBE=已投订阅消息；SUBSCRIBE_FAILED=订阅失败'' AFTER `send_status`',
  'DO 0');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------- 配置项
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
  ('代驾扩圈半径阶梯', 'rongli.dispatch.radiusSteps', '3000,8000,20000', 'N', 'admin', UTC_TIMESTAMP(), '逗号分隔的逐轮派单半径（米）；轮次用尽仍无人应答即转待人工安排'),
  ('学习心跳最大间隔', 'rongli.training.heartbeatMaxGapSeconds', '30', 'N', 'admin', UTC_TIMESTAMP(), '相邻两次心跳的真实间隔超过该秒数时，超出部分不计入有效学时'),
  ('订阅消息开关', 'rongli.subscribe.enabled', 'true', 'N', 'admin', UTC_TIMESTAMP(), 'true=尝试下发微信订阅消息；false=只发站内信'),
  ('订阅消息模板-派单邀约', 'rongli.subscribe.template.dispatchInvite', '', 'N', 'admin', UTC_TIMESTAMP(), '微信订阅消息模板 ID；留空则该场景只发站内信'),
  ('订阅消息模板-接单成功', 'rongli.subscribe.template.assignmentAccepted', '', 'N', 'admin', UTC_TIMESTAMP(), '微信订阅消息模板 ID；留空则该场景只发站内信'),
  ('订阅消息模板-结果可查看', 'rongli.subscribe.template.resultReady', '', 'N', 'admin', UTC_TIMESTAMP(), '微信订阅消息模板 ID；留空则该场景只发站内信'),
  ('订阅消息模板-退款完成', 'rongli.subscribe.template.refundDone', '', 'N', 'admin', UTC_TIMESTAMP(), '微信订阅消息模板 ID；留空则该场景只发站内信')
AS new
ON DUPLICATE KEY UPDATE `config_name` = new.`config_name`, `remark` = new.`remark`;

-- 已存在同名配置时不覆盖运维改过的值：上面 ON DUPLICATE 只更新名称与备注
SELECT 'migration_04 done' AS result;
