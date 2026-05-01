-- 为用户4,5,6,10批量插入文章数据

-- 定义分类列表（使用拼音code而不是中文）
SET @categories = 'tech,life,work,entertainment,other';

-- 定义权限列表
SET @permissions = 'public,private,secret';

-- 定义标签列表
SET @tags = 'Java,Python,前端,后端,数据库,运维,工具,阅读,旅行,美食,健身,职场,管理,创意,学习';

-- 为用户4插入5-10篇文章
DELIMITER //
CREATE PROCEDURE insert_articles_for_user4()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_articles INT DEFAULT FLOOR(RAND() * 6) + 5; -- 5-10
    DECLARE category VARCHAR(50);
    DECLARE permission VARCHAR(20);
    DECLARE tag VARCHAR(50);
    
    WHILE i <= max_articles DO
        -- 随机选择分类
        SET category = SUBSTRING_INDEX(SUBSTRING_INDEX(@categories, ',', FLOOR(RAND() * 4) + 1), ',', -1);
        
        -- 随机选择权限
        SET permission = SUBSTRING_INDEX(SUBSTRING_INDEX(@permissions, ',', FLOOR(RAND() * 3) + 1), ',', -1);
        
        -- 随机选择标签
        SET tag = SUBSTRING_INDEX(SUBSTRING_INDEX(@tags, ',', FLOOR(RAND() * 15) + 1), ',', -1);
        
        -- 插入文章
        INSERT INTO document (
            title, 
            content, 
            category, 
            tags, 
            permission, 
            user_id, 
            view_count, 
            like_count, 
            created_at, 
            updated_at
        ) VALUES (
            CONCAT('用户4的', category, '文章', i),
            CONCAT('# 用户4的', category, '文章', i, '\n\n这是一篇关于', category, '的文章，标签为', tag, '。\n\n## 内容\n\n这是文章的主要内容，包含多个段落。\n\n- 列表项1\n- 列表项2\n- 列表项3\n\n```java\n// 示例代码\npublic class Example {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}\n```\n\n> 这是一个引用。\n\n这是文章的结尾部分。'),
            category,
            tag,
            permission,
            4,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 100),
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY
        );
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 为用户5插入5-10篇文章
DELIMITER //
CREATE PROCEDURE insert_articles_for_user5()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_articles INT DEFAULT FLOOR(RAND() * 6) + 5; -- 5-10
    DECLARE category VARCHAR(50);
    DECLARE permission VARCHAR(20);
    DECLARE tag VARCHAR(50);
    
    WHILE i <= max_articles DO
        -- 随机选择分类
        SET category = SUBSTRING_INDEX(SUBSTRING_INDEX(@categories, ',', FLOOR(RAND() * 4) + 1), ',', -1);
        
        -- 随机选择权限
        SET permission = SUBSTRING_INDEX(SUBSTRING_INDEX(@permissions, ',', FLOOR(RAND() * 3) + 1), ',', -1);
        
        -- 随机选择标签
        SET tag = SUBSTRING_INDEX(SUBSTRING_INDEX(@tags, ',', FLOOR(RAND() * 15) + 1), ',', -1);
        
        -- 插入文章
        INSERT INTO document (
            title, 
            content, 
            category, 
            tags, 
            permission, 
            user_id, 
            view_count, 
            like_count, 
            created_at, 
            updated_at
        ) VALUES (
            CONCAT('用户5的', category, '文章', i),
            CONCAT('# 用户5的', category, '文章', i, '\n\n这是一篇关于', category, '的文章，标签为', tag, '。\n\n## 内容\n\n这是文章的主要内容，包含多个段落。\n\n- 列表项1\n- 列表项2\n- 列表项3\n\n```python\n# 示例代码\ndef example():\n    print("Hello World!")\n\nexample()\n```\n\n> 这是一个引用。\n\n这是文章的结尾部分。'),
            category,
            tag,
            permission,
            5,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 100),
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY
        );
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 为用户6插入5-10篇文章
DELIMITER //
CREATE PROCEDURE insert_articles_for_user6()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_articles INT DEFAULT FLOOR(RAND() * 6) + 5; -- 5-10
    DECLARE category VARCHAR(50);
    DECLARE permission VARCHAR(20);
    DECLARE tag VARCHAR(50);
    
    WHILE i <= max_articles DO
        -- 随机选择分类
        SET category = SUBSTRING_INDEX(SUBSTRING_INDEX(@categories, ',', FLOOR(RAND() * 4) + 1), ',', -1);
        
        -- 随机选择权限
        SET permission = SUBSTRING_INDEX(SUBSTRING_INDEX(@permissions, ',', FLOOR(RAND() * 3) + 1), ',', -1);
        
        -- 随机选择标签
        SET tag = SUBSTRING_INDEX(SUBSTRING_INDEX(@tags, ',', FLOOR(RAND() * 15) + 1), ',', -1);
        
        -- 插入文章
        INSERT INTO document (
            title, 
            content, 
            category, 
            tags, 
            permission, 
            user_id, 
            view_count, 
            like_count, 
            created_at, 
            updated_at
        ) VALUES (
            CONCAT('用户6的', category, '文章', i),
            CONCAT('# 用户6的', category, '文章', i, '\n\n这是一篇关于', category, '的文章，标签为', tag, '。\n\n## 内容\n\n这是文章的主要内容，包含多个段落。\n\n- 列表项1\n- 列表项2\n- 列表项3\n\n```javascript\n// 示例代码\nfunction example() {\n    console.log("Hello World!");\n}\n\nexample();\n```\n\n> 这是一个引用。\n\n这是文章的结尾部分。'),
            category,
            tag,
            permission,
            6,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 100),
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY
        );
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 为用户10插入5-10篇文章
DELIMITER //
CREATE PROCEDURE insert_articles_for_user10()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_articles INT DEFAULT FLOOR(RAND() * 6) + 5; -- 5-10
    DECLARE category VARCHAR(50);
    DECLARE permission VARCHAR(20);
    DECLARE tag VARCHAR(50);
    
    WHILE i <= max_articles DO
        -- 随机选择分类
        SET category = SUBSTRING_INDEX(SUBSTRING_INDEX(@categories, ',', FLOOR(RAND() * 4) + 1), ',', -1);
        
        -- 随机选择权限
        SET permission = SUBSTRING_INDEX(SUBSTRING_INDEX(@permissions, ',', FLOOR(RAND() * 3) + 1), ',', -1);
        
        -- 随机选择标签
        SET tag = SUBSTRING_INDEX(SUBSTRING_INDEX(@tags, ',', FLOOR(RAND() * 15) + 1), ',', -1);
        
        -- 插入文章
        INSERT INTO document (
            title, 
            content, 
            category, 
            tags, 
            permission, 
            user_id, 
            view_count, 
            like_count, 
            created_at, 
            updated_at
        ) VALUES (
            CONCAT('用户10的', category, '文章', i),
            CONCAT('# 用户10的', category, '文章', i, '\n\n这是一篇关于', category, '的文章，标签为', tag, '。\n\n## 内容\n\n这是文章的主要内容，包含多个段落。\n\n- 列表项1\n- 列表项2\n- 列表项3\n\n```sql\n-- 示例代码\nSELECT * FROM users WHERE id = 1;\n```\n\n> 这是一个引用。\n\n这是文章的结尾部分。'),
            category,
            tag,
            permission,
            10,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 100),
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY
        );
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行存储过程
CALL insert_articles_for_user4();
CALL insert_articles_for_user5();
CALL insert_articles_for_user6();
CALL insert_articles_for_user10();

-- 删除存储过程
DROP PROCEDURE IF EXISTS insert_articles_for_user4;
DROP PROCEDURE IF EXISTS insert_articles_for_user5;
DROP PROCEDURE IF EXISTS insert_articles_for_user6;
DROP PROCEDURE IF EXISTS insert_articles_for_user10;

-- 查看插入结果
SELECT user_id, COUNT(*) as article_count FROM document GROUP BY user_id ORDER BY user_id;