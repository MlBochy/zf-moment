package com.zumo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zumo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
