package com.zumo.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String nickName;
    private String avatarUrl;
    private String backgroundUrl;
    private String bio;
}
