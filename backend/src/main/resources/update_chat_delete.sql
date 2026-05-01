-- 添加逻辑删除字段
ALTER TABLE chat_messages ADD COLUMN deleted TINYINT(1) DEFAULT 0 COMMENT '是否被删除：0-否，1-是';

-- 创建聊天设置表（如果不存在）
CREATE TABLE IF NOT EXISTS chat_settings (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `chat_partner_id` BIGINT NOT NULL COMMENT '聊天对象ID',
  `is_pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `do_not_disturb` TINYINT(1) DEFAULT 0 COMMENT '是否免打扰：0-否，1-是',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除对话：0-否，1-是',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_partner` (`user_id`, `chat_partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天设置表';

-- 创建黑名单表（如果不存在）
CREATE TABLE IF NOT EXISTS blacklist (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `blocked_user_id` BIGINT NOT NULL COMMENT '被拉黑的用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';
