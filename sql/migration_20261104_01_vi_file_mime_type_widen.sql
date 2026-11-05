-- ---------------------------------------------------------------------------
-- 放宽 vi_file.mime_type：VARCHAR(64) → VARCHAR(128)
--
-- 背景：影像存储改为对象存储后，上传类型从「只有 jpg/png/webp」放开到文档与视频。
-- docx 的标准 MIME 是
--   application/vnd.openxmlformats-officedocument.wordprocessingml.document
-- 共 71 个字符，超过 64 会被截断或直接报 Data too long，表现为「文件传上去了，
-- 但类型判错 / 打不开」——这类问题在写入侧不明显，到读取时才发现。
--
-- 只放宽不收紧，对存量数据无影响。
-- 可重复执行：重复 ALTER 结果一致（幂等）。
-- ---------------------------------------------------------------------------

ALTER TABLE `vi_file`
  MODIFY COLUMN `mime_type` VARCHAR(128) COMMENT '内容识别类型';

-- 自检：期望返回 varchar(128)
SELECT COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'vi_file'
  AND COLUMN_NAME = 'mime_type';
