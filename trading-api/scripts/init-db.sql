-- Create database
CREATE DATABASE IF NOT EXISTS trading_wisdom DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trading_wisdom;

-- Create admin_config table
CREATE TABLE IF NOT EXISTS `admin_config` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `password_hash` VARCHAR(255) NOT NULL,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin password (password: admin123)
INSERT INTO `admin_config` (`id`, `password_hash`) VALUES
(1, '$2a$10$SE2e9SO1o6nykGxrMeyjYu.bwCWDb/Rqh4fGqSR/THehVQhmpWhWW')
ON DUPLICATE KEY UPDATE `password_hash` = VALUES(`password_hash`);

-- Create quotes table
CREATE TABLE IF NOT EXISTS `quotes` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `content` TEXT NOT NULL,
  `author` VARCHAR(255) DEFAULT NULL,
  `category` ENUM('RISK_MGMT', 'DISCIPLINE', 'MINDSET', 'TECHNICAL', 'FUNDAMENTAL', 'PSYCHOLOGY', 'STRATEGY', 'GENERAL') NOT NULL DEFAULT 'GENERAL',
  `source` ENUM('SYSTEM', 'USER') NOT NULL DEFAULT 'SYSTEM',
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_source` (`source`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert system preset quotes
INSERT INTO `quotes` (`content`, `author`, `category`, `source`, `is_active`) VALUES
('止损是交易的第一课，学会止损才能生存。', NULL, 'RISK_MGMT', 'SYSTEM', TRUE),
('计划你的交易，交易你的计划。', NULL, 'DISCIPLINE', 'SYSTEM', TRUE),
('不要让情绪左右你的交易决策。', NULL, 'MINDSET', 'SYSTEM', TRUE),
('趋势是你的朋友，永远不要逆势而为。', NULL, 'TECHNICAL', 'SYSTEM', TRUE),
('仓位管理比入场点更重要。', NULL, 'RISK_MGMT', 'SYSTEM', TRUE),
('市场永远是对的，承认错误才能进步。', NULL, 'MINDSET', 'SYSTEM', TRUE),
('不要追涨杀跌，耐心等待最佳时机。', NULL, 'DISCIPLINE', 'SYSTEM', TRUE),
('复利是世界的第八大奇迹。', '爱因斯坦', 'GENERAL', 'SYSTEM', TRUE);
