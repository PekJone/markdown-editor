CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID，系统消息为0',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `type` varchar(20) NOT NULL COMMENT '消息类型：comment, like, follow, collect, chat',
  `content` text NOT NULL COMMENT '消息内容',
  `related_id` bigint(20) DEFAULT NULL COMMENT '相关实体ID，如评论ID、文章ID等',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
