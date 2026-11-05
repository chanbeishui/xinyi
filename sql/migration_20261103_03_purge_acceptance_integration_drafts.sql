-- ---------------------------------------------------------------------------
-- 清理历史验收残留的集成档案
--
-- 背景：集成配置档案的凭据写入曾有一个 Bug（applyCredentials 先插新凭据、
-- 再用「把该槽位所有 ACTIVE 置 RETIRED」的语句清理，把刚写入的那条一起退役），
-- 导致档案永远校验失败、永远发布不出去。每跑一次验收就留下一条「验收用微信支付
-- （无凭据）」DRAFT 档案。
--
-- 该 Bug 已修复，这里把历史残留删掉。注意 *不是* 软删（lifecycle 置 RETIRED）——
-- 它们从未发布过（active_revision_id 恒为 NULL），没有任何审计价值，留着只会
-- 让「第三方服务」列表里堆一屏同名空档案。
--
-- 安全护栏（同时满足才删）：
--   1. lifecycle = 'DRAFT'（从未发布）
--   2. active_revision_id IS NULL（没有生效版本）
--   3. display_name 精确等于验收脚本生成的名字
-- 因此真实业务档案（哪怕也叫 DRAFT）不会被误删。
--
-- 实现说明：待删 id 先落一张普通表而不是 TEMPORARY 表——临时表在同一语句里被
-- 引用两次会报 `ERROR 1137 Can't reopen table`；普通表没有这个限制，脚本结束即 DROP。
--
-- 可重复执行：第二次运行匹配 0 行。
-- ---------------------------------------------------------------------------

DROP TABLE IF EXISTS `_purge_profile_ids`;

CREATE TABLE `_purge_profile_ids` (
  `id` BIGINT NOT NULL PRIMARY KEY
);

INSERT INTO `_purge_profile_ids` (`id`)
SELECT `id`
FROM `vi_integration_profile`
WHERE `lifecycle` = 'DRAFT'
  AND `active_revision_id` IS NULL
  AND `display_name` = '验收用微信支付（无凭据）';

-- 自建验证残留（profile 52「投影验证（自动化）」，已 RETIRED、无生效版本）
INSERT IGNORE INTO `_purge_profile_ids` (`id`)
SELECT `id`
FROM `vi_integration_profile`
WHERE `lifecycle` = 'RETIRED'
  AND `active_revision_id` IS NULL
  AND `display_name` = '投影验证（自动化）';

-- 先删子表（本组表之间没有外键，但按引用顺序删更安全）
-- 注意 check / publication 是多态表：用 target_type + target_id，没有 profile_id 列
DELETE FROM `vi_integration_publication`
 WHERE `target_type` = 'PROFILE' AND `target_id` IN (SELECT `id` FROM `_purge_profile_ids`);
DELETE FROM `vi_integration_check`
 WHERE `target_type` = 'PROFILE' AND `target_id` IN (SELECT `id` FROM `_purge_profile_ids`);
DELETE FROM `vi_integration_secret`
 WHERE `profile_id` IN (SELECT `id` FROM `_purge_profile_ids`);
DELETE FROM `vi_integration_revision`
 WHERE `profile_id` IN (SELECT `id` FROM `_purge_profile_ids`);
DELETE FROM `vi_integration_profile`
 WHERE `id` IN (SELECT `id` FROM `_purge_profile_ids`);

-- 自检：期望 remaining_draft = 0
SELECT
  (SELECT COUNT(*) FROM `vi_integration_profile`
    WHERE `lifecycle` = 'DRAFT' AND `active_revision_id` IS NULL
      AND `display_name` = '验收用微信支付（无凭据）') AS `remaining_draft`,
  (SELECT COUNT(*) FROM `vi_integration_profile`) AS `profiles_left`;

DROP TABLE IF EXISTS `_purge_profile_ids`;
