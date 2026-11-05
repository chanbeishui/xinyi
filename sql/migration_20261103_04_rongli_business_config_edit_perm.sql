-- ---------------------------------------------------------------------------
-- 补齐「业务参数」页的编辑权限点
--
-- 背景：旧配置页（微信支付 / 短信通道 / 订阅消息）下线后，业务参数页成了唯一
-- 保留的配置入口，但它的保存接口 `PUT /api/admin/config` 一直检查
-- `biz:config:edit`，而该权限点在 sys_menu 里根本不存在——所以只有超管
-- （`*:*:*` 直通）能保存，其他运营角色点了必然 403。
--
-- 同时把「拉取平台证书」的权限收敛到 biz:config:edit：平台证书属于运行时快照，
-- 与改配置同义；原来的 biz:config:payment 已无菜单承载，无法再分配给新角色。
--
-- 可重复执行：按 perms 判重，已存在则跳过。
-- ---------------------------------------------------------------------------

INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`,
   `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
   `create_by`, `create_time`, `remark`)
SELECT
  2144, '业务参数保存', 2140, 1, '', '', '',
  1, 0, 'F', 0, 0, 'biz:config:edit', '#',
  'admin', NOW(), '业务参数保存（原 biz:config:payment 已无菜单承载）'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `perms` = 'biz:config:edit'
);

-- 超管角色（role_id=1）虽然是 *:*:* 直通，仍补一条授权关系，
-- 避免角色分配页看不到该按钮而误以为权限缺失。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 2144
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2144
);

-- 自检：期望返回 1 行（perms = biz:config:edit）
SELECT `menu_id`, `menu_name`, `parent_id`, `perms`
FROM `sys_menu`
WHERE `perms` = 'biz:config:edit';
