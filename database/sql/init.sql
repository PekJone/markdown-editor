-- 创建数据库
CREATE DATABASE IF NOT EXISTS markdown_editor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE markdown_editor;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(50) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    user_id BIGINT NOT NULL,
    category VARCHAR(50),
    tags VARCHAR(255),
    permission ENUM('public', 'private', 'secret') DEFAULT 'private',
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_permission (permission),
    INDEX idx_updated_at (updated_at),
    FULLTEXT INDEX idx_content (title, content),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认管理员用户（密码: admin123）
INSERT INTO users (username, password, email, nickname, role) VALUES
('admin', '$2a$10$Ro7jcgrTZfIvKFufF0bSG.zFiBJFdJnggmhbQwXcpMW90RPvfbaVC', 'admin@example.com', '管理员', 'admin');

-- 插入测试用户（密码: test123）
INSERT INTO users (username, password, email, nickname, role) VALUES
('test', '$2a$10$S9IHxtBlRZA0ON/PlUVG0OjR/Uk.jsrVBIDb8JE/IMErI5Snp1wJ6', 'test@example.com', '测试用户', 'user');

-- 插入客服用户（密码: cs123）
INSERT INTO users (username, password, email, nickname, role) VALUES
('cs', '$2a$10$S9IHxtBlRZA0ON/PlUVG0OjR/Uk.jsrVBIDb8JE/IMErI5Snp1wJ6', 'cs@example.com', '客服', 'customer_service');

-- 字典表
CREATE TABLE IF NOT EXISTS dictionary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_code VARCHAR(50) NOT NULL,
    dict_label VARCHAR(100) NOT NULL,
    status VARCHAR(10) DEFAULT '1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code),
    INDEX idx_dict_type (dict_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入文章分类字典数据
INSERT INTO dictionary (dict_type, dict_code, dict_label, status) VALUES
('article_category', 'tech', '技术', '1'),
('article_category', 'life', '生活', '1'),
('article_category', 'work', '工作', '1'),
('article_category', 'entertainment', '娱乐', '1'),
('article_category', 'other', '其他', '1');

-- 插入文章权限字典数据
INSERT INTO dictionary (dict_type, dict_code, dict_label, status) VALUES
('article_permission', 'public', '公开', '1'),
('article_permission', 'private', '私密', '1'),
('article_permission', 'secret', '绝密', '1');

-- 插入角色字典数据
INSERT INTO dictionary (dict_type, dict_code, dict_label, status) VALUES
('user_role', 'admin', '管理员', '1'),
('user_role', 'customer_service', '客服', '1'),
('user_role', 'user', '普通用户', '1');

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_category_tag (user_id, category, tag_name),
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 创建评论表
CREATE TABLE IF NOT EXISTS `comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `document_id` BIGINT NOT NULL COMMENT '文档ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` VARCHAR(450) NOT NULL COMMENT '评论内容，最多150个汉字',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 创建关注表
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