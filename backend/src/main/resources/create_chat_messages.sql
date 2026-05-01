CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';


CREATE TABLE `chat_settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `chat_partner_id` BIGINT NOT NULL COMMENT '聊天对象ID',
  `is_pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `do_not_disturb` TINYINT(1) DEFAULT 0 COMMENT '是否免打扰：0-否，1-是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_partner` (`user_id`, `chat_partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天设置表';