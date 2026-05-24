-- ============================================
-- Trading Wisdom (交易智慧) - Database Schema
-- ============================================
-- This script initializes the trading_wisdom database
-- with all required tables and initial data.
--
-- Usage:
--   mysql -u root -p < schema.sql
--
-- Default admin password: admin123 (CHANGE ON FIRST LOGIN!)
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS trading_wisdom
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE trading_wisdom;

-- ============================================
-- Quotes Table
-- ============================================
DROP TABLE IF EXISTS quotes;
CREATE TABLE quotes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(500) NOT NULL COMMENT '语录内容',
    category ENUM('RISK_MGMT', 'MINDSET', 'DISCIPLINE', 'TECHNICAL') NOT NULL COMMENT '分类',
    market_type ENUM('STOCK', 'FUTURES', 'GENERAL') NOT NULL DEFAULT 'GENERAL' COMMENT '市场类型',
    source ENUM('SYSTEM', 'USER') NOT NULL DEFAULT 'SYSTEM' COMMENT '来源',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at BIGINT NOT NULL COMMENT '创建时间戳(毫秒)',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳(毫秒)',
    INDEX idx_category (category),
    INDEX idx_market_type (market_type),
    INDEX idx_source (source),
    INDEX idx_created (created_at),
    INDEX idx_is_favorite (is_favorite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易语录表';

-- ============================================
-- Admin Config Table
-- ============================================
DROP TABLE IF EXISTS admin_config;
CREATE TABLE admin_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_path VARCHAR(100) NOT NULL UNIQUE DEFAULT 'xyz-admin' COMMENT '管理后台路径',
    admin_password_hash VARCHAR(255) NOT NULL COMMENT '管理员密码哈希(bcrypt)',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理配置表';

-- ============================================
-- Initial Data
-- ============================================

-- Initialize admin config
-- Default password: admin123
-- Bcrypt hash (cost=10): $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO admin_config (admin_password_hash, updated_at)
VALUES ('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', UNIX_TIMESTAMP() * 1000);

-- Sample quotes for testing
INSERT INTO quotes (content, category, market_type, source, created_at, updated_at) VALUES
('止损是交易的第一课，学会止损才能生存。', 'RISK_MGMT', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('计划你的交易，交易你的计划。', 'DISCIPLINE', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('不要让情绪左右你的交易决策。', 'MINDSET', 'STOCK', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('趋势是你的朋友，永远不要逆势而为。', 'TECHNICAL', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('复利是第八大奇迹，懂得复利的人赚，不懂的人被赚。', 'MINDSET', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('只在大概率盈利的时机出手，学会空仓也是一种交易。', 'DISCIPLINE', 'FUTURES', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('盈亏同源，控制风险比追求利润更重要。', 'RISK_MGMT', 'STOCK', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('市场永远是对的，错的只有你的判断。', 'MINDSET', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- ============================================
-- Verification Queries (for manual testing)
-- ============================================
-- SHOW TABLES;
-- DESCRIBE quotes;
-- DESCRIBE admin_config;
-- SELECT * FROM quotes LIMIT 5;
-- SELECT * FROM admin_config;
