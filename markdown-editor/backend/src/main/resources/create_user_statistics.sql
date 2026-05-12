-- 创建用户统计表
CREATE TABLE IF NOT EXISTS `user_statistics` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `likes_count` INT DEFAULT 0 COMMENT '获赞总数',
  `collections_count` INT DEFAULT 0 COMMENT '文章被收藏总数',
  `comments_count` INT DEFAULT 0 COMMENT '评论总数',
  `following_count` INT DEFAULT 0 COMMENT '关注用户数',
  `followers_count` INT DEFAULT 0 COMMENT '粉丝数',
  `blog_count` INT DEFAULT 0 COMMENT '发布的博客总数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';