-- 创建文档浏览量统计表
CREATE TABLE IF NOT EXISTS `document_view_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `document_id` BIGINT NOT NULL COMMENT '文档ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `view_date` DATE NOT NULL COMMENT '浏览日期',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '当日浏览量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_date` (`document_id`, `view_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_view_date` (`view_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档每日浏览量统计表';

-- 插入测试数据（模拟近7天的浏览量）
INSERT INTO `document_view_stat` (`document_id`, `user_id`, `view_date`, `view_count`) VALUES
(1, 1, '2026-04-24', 21),
(2, 1, '2026-04-24', 8),
(1, 1, '2026-04-25', 15),
(2, 1, '2026-04-25', 13),
(1, 1, '2026-04-26', 25),
(2, 1, '2026-04-26', 20),
(1, 1, '2026-04-27', 10),
(2, 1, '2026-04-27', 8),
(1, 1, '2026-04-28', 7),
(2, 1, '2026-04-28', 5),
(1, 1, '2026-04-29', 18),
(2, 1, '2026-04-29', 12),
(1, 1, '2026-04-30', 10),
(2, 1, '2026-04-30', 6);
