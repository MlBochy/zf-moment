package com.zumo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentDTO {
    
    @NotNull(message = "动态ID不能为空")
    private Long momentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private Long parentId;  // 父评论ID，用于回复功能
} 