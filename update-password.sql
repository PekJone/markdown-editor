-- 更新管理员密码为 admin123
UPDATE users SET password = '$2a$10$Ro7jcgrTZfIvKFufF0bSG.zFiBJFdJnggmhbQwXcpMW90RPvfbaVC' WHERE username = 'admin';

-- 更新测试用户密码为 test123
UPDATE users SET password = '$2a$10$S9IHxtBlRZA0ON/PlUVG0OjR/Uk.jsrVBIDb8JE/IMErI5Snp1wJ6' WHERE username = 'test';
