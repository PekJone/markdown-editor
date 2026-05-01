-- 在documents表中添加comment_count和collection_count字段
ALTER TABLE documents ADD COLUMN comment_count INT DEFAULT 0 COMMENT '评论数';
ALTER TABLE documents ADD COLUMN collection_count INT DEFAULT 0 COMMENT '收藏数';
