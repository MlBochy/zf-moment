package com.zumo.vo;

import lombok.Data;


@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickName;
    private String avatarUrl;
    private String backgroundUrl;
    private String bio;
    private Long momentCount;
} 