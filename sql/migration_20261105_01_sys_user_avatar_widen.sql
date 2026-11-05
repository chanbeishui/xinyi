-- ---------------------------------------------------------------------------
-- 放宽 sys_user.avatar：VARCHAR(100) → VARCHAR(500)
--
-- 背景：用户文件统一改走对象存储后，avatar 存的已不是 /profile/avatar/2026/11/05/xxx.png
-- 这类三十来字符的相对路径，而是完整直链，例如
--   https://rongli-miniwx.oss-cn-shenzhen.aliyuncs.com/avatar/2026/11/05/avatar_ab12cd34ef56.png
-- 长度 100 上下，正好卡在原列宽上：换个桶名、绑自定义域名、多一层目录，
-- 立刻超限并报 Data too long for column 'avatar'，表现为「图片传上去了，但头像保存失败」。
--
-- 存直链而不是存 key，是因为 RuoYi 的 <img :src="avatar"> 直接消费该字段，
-- 存相对值会解析到前端域名上。列宽必须按直链预算。
--
-- 只放宽不收紧，存量数据无影响；可重复执行（幂等）。
-- ---------------------------------------------------------------------------

ALTER TABLE `sys_user`
  MODIFY COLUMN `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像地址（对象存储直链）';

-- 自检：期望返回 varchar(500)
SELECT COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_user'
  AND COLUMN_NAME = 'avatar';
