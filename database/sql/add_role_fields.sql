-- 为现有数据库添加角色相关字段和数据

-- 1. 在users表中添加role字段
ALTER TABLE users ADD COLUMN role VARCHAR(50) DEFAULT 'user' AFTER avatar;

-- 2. 为users表添加role索引
CREATE INDEX idx_role ON users(role);

-- 3. 更新现有用户的角色
UPDATE users SET role = 'admin' WHERE username = 'admin';
UPDATE users SET role = 'user' WHERE username = 'test';

-- 4. 插入客服用户
INSERT INTO users (username, password, email, nickname, role) VALUES
('cs', '$2a$10$S9IHxtBlRZA0ON/PlUVG0OjR/Uk.jsrVBIDb8JE/IMErI5Snp1wJ6', 'cs@example.com', '客服', 'customer_service');

-- 5. 插入角色字典数据
INSERT INTO dictionary (dict_type, dict_code, dict_label, status) VALUES
('user_role', 'admin', '管理员', '1'),
('user_role', 'customer_service', '客服', '1'),
('user_role', 'user', '普通用户', '1');

-- 6. 创建关注表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';
