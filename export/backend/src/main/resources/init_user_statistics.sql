-- 初始化用户统计数据
INSERT INTO `user_statistics` (`user_id`, `likes_count`, `collections_count`, `comments_count`, `following_count`, `followers_count`, `blog_count`)
SELECT
  u.`id` AS `user_id`,
  COALESCE((SELECT SUM(d.`like_count`) FROM `documents` d WHERE d.`user_id` = u.`id`), 0) AS `likes_count`,
  COALESCE((SELECT COUNT(*) FROM `document_collections` dc JOIN `documents` d ON dc.`document_id` = d.`id` WHERE d.`user_id` = u.`id`), 0) AS `collections_count`,
  COALESCE((SELECT COUNT(*) FROM `comments` c JOIN `documents` d ON c.`document_id` = d.`id` WHERE d.`user_id` = u.`id`), 0) AS `comments_count`,
  COALESCE((SELECT COUNT(*) FROM `follow` f WHERE f.`follower_id` = u.`id`), 0) AS `following_count`,
  COALESCE((SELECT COUNT(*) FROM `follow` f WHERE f.`following_id` = u.`id`), 0) AS `followers_count`,
  COALESCE((SELECT COUNT(*) FROM `documents` d WHERE d.`user_id` = u.`id`), 0) AS `blog_count`
FROM `users` u
ON DUPLICATE KEY UPDATE
  `likes_count` = VALUES(`likes_count`),
  `collections_count` = VALUES(`collections_count`),
  `comments_count` = VALUES(`comments_count`),
  `following_count` = VALUES(`following_count`),
  `followers_count` = VALUES(`followers_count`),
  `blog_count` = VALUES(`blog_count`);