package com.zumo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long momentId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
} 