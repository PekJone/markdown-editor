-- 为 users 表添加 phone 字段
ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER email;
