-- 数据库恢复脚本
-- 如果数据库文件损坏，可以删除现有文件并重新创建

-- 1. 停止应用程序
-- 2. 删除 data/lostfounddb.mv.db 和 data/lostfounddb.trace.db 文件
-- 3. 重新启动应用程序，它会自动重新创建数据库

-- 或者使用以下SQL在H2控制台中执行：

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建物品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    post_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
    location VARCHAR(50) NOT NULL,
    detailed_location VARCHAR(100),
    lost_found_time TIMESTAMP NOT NULL,
    total_weight INTEGER,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- 创建匹配记录表
CREATE TABLE IF NOT EXISTS item_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id BIGINT NOT NULL,
    found_item_id BIGINT NOT NULL,
    match_weight DOUBLE NOT NULL,
    matched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lost_item_id) REFERENCES items(id),
    FOREIGN KEY (found_item_id) REFERENCES items(id)
);

-- 创建聊天线程表
CREATE TABLE IF NOT EXISTS chat_threads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (thread_id) REFERENCES chat_threads(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50) NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    related_item_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (related_item_id) REFERENCES items(id)
);

-- 插入默认管理员用户
INSERT INTO users (username, display_name, student_id, email, phone, password, role, enabled) 
VALUES ('admin', '系统管理员', '20240001', 'admin@campus.edu', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ADMIN', TRUE);

-- 插入测试用户
INSERT INTO users (username, display_name, student_id, email, phone, password, role, enabled) 
VALUES ('testuser1', '测试用户1', '20240002', 'test1@campus.edu', '13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'USER', TRUE);

INSERT INTO users (username, display_name, student_id, email, phone, password, role, enabled) 
VALUES ('testuser2', '测试用户2', '20240003', 'test2@campus.edu', '13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'USER', TRUE);

-- 插入测试物品
INSERT INTO items (title, description, category, post_type, status, location, detailed_location, lost_found_time, total_weight, approved, owner_id) 
VALUES ('丢失的校园卡', '上面有一个愚蠢的小鳄鱼卡套', 'ID_CARD', 'LOST', 'PENDING_CLAIM', 'LIBRARY', 'LIBRARY_MAIN_READING_ROOM', CURRENT_TIMESTAMP, 10, TRUE, 2);

INSERT INTO items (title, description, category, post_type, status, location, detailed_location, lost_found_time, total_weight, approved, owner_id) 
VALUES ('捡到一张校园卡', '上面有一个愚蠢的小鳄鱼卡套', 'ID_CARD', 'FOUND', 'PENDING_CLAIM', 'LIBRARY', 'LIBRARY_MAIN_READING_ROOM', CURRENT_TIMESTAMP, 10, TRUE, 3);




