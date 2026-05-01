-- 创建文档收藏表
CREATE TABLE IF NOT EXISTS `document_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `document_id` BIGINT NOT NULL COMMENT '文档ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_user` (`document_id`, `user_id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档收藏表';
