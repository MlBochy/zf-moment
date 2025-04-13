package com.zumo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentVO {
    private Long id;
    private UserVO user;
    private String content;
    private List<String> images;
    private LocalDateTime createTime;
    private Long liked;
    private List<UserVO> likes;
    private Boolean islike;
    private List<CommentVO> comments;

}