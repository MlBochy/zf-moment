package com.zumo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zumo.entity.Comment;
import com.zumo.mapper.CommentMapper;
import com.zumo.service.ICommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
}
