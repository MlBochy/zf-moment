package com.zumo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_moment")
public class Moment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content;

    private String images;

    private Long liked;

    private LocalDateTime createTime;
    /**
     * 是否点赞过了
     */
    @TableField(exist = false)
    private boolean isLiked;

}
