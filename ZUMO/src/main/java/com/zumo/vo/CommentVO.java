package com.zumo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long momentId;
    private String content;
    private LocalDateTime createTime;
    private UserVO user;
} 