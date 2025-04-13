DROP DATABASE IF EXISTS ncmoment;
CREATE DATABASE ncmoment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ncmoment;

-- 用户表（添加avatar_url字段）
CREATE TABLE tb_user (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password CHAR(32) NOT NULL,  -- MD5加密后固定32位
                       nick_name VARCHAR(50) NOT NULL,
                       avatar_url VARCHAR(255) DEFAULT '/default-avatar.jpg', -- 新增头像字段
                       background_url VARCHAR(255) DEFAULT '/default-background.jpg',
                       bio VARCHAR(100) DEFAULT '',
                       created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE `tb_moment` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `user_id` bigint NOT NULL COMMENT '用户ID',
                             `content` varchar(500) NOT NULL COMMENT '动态内容',
                             `images` text COMMENT '图片URL数组，JSON格式',
                             `liked` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈动态表';

-- 评论表（保持不变）
CREATE TABLE tb_comment (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          moment_id INT NOT NULL,
                          user_id INT NOT NULL,
                          content VARCHAR(500) NOT NULL,
                          created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (moment_id) REFERENCES tb_moment(moment_id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

