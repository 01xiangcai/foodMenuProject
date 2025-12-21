-- AI对话历史表
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    wx_user_id BIGINT NOT NULL COMMENT '微信用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user(用户) 或 assistant(AI助手)',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wx_user_id (wx_user_id),
    INDEX idx_family_id (family_id),
    INDEX idx_create_time (create_time)
) COMMENT 'AI对话历史表';
