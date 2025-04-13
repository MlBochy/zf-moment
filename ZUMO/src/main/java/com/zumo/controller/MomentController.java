package com.zumo.controller;

import com.zumo.dto.CommentDTO;
import com.zumo.dto.CreateMomentDTO;
import com.zumo.dto.Result;
import com.zumo.service.IMomentService;
import com.zumo.service.IUserService;
import com.zumo.vo.MomentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moments")
@Slf4j
public class MomentController {

    @Autowired
    private IMomentService momentService;

    @GetMapping
    public Result<List<MomentVO>> getMoments() {
        log.info("获取动态列表");
        return momentService.getMoments();
    }

    @GetMapping("/{momentId}")
    public Result<MomentVO> getMomentDetail(@PathVariable Long momentId) {
        log.info("获取动态详情: momentId={}", momentId);
        return momentService.getMomentDetail(momentId);
    }

    @PostMapping
    public Result<MomentVO> createMoment(@RequestBody CreateMomentDTO dto) {
        log.info("发布动态: content={}, images={}", dto.getContent(), dto.getImages());
        return momentService.createMoment(dto);
    }

    @PutMapping("/{momentId}")
    public Result<MomentVO> updateMoment(@PathVariable Long momentId, @RequestBody CreateMomentDTO dto) {
        log.info("更新动态: momentId={}, content={}, images={}", momentId, dto.getContent(), dto.getImages());
        return momentService.updateMoment(momentId, dto);
    }

    @DeleteMapping("/{momentId}")
    public Result<Void> deleteMoment(@PathVariable Long momentId) {
        log.info("删除动态: momentId={}", momentId);
        return momentService.deleteMoment(momentId);
    }

    @PostMapping("/{momentId}/like")
    public Result<Void> likeMoment(@PathVariable Long momentId) {
        log.info("点赞动态: momentId={}", momentId);
        return momentService.likeMoment(momentId);
    }

    @GetMapping("/{momentId}/likes")
    public Result getMomentLikes(@PathVariable Long momentId) {
        log.info("获取动态点赞数: momentId={}", momentId);
        return momentService.getMomentLikes(momentId);
    }

    @GetMapping("/{momentId}/comments")
    public Result getMomentComments(@PathVariable Long momentId) {
        log.info("获取动态评论: momentId={}", momentId);
        return momentService.getMomentComments(momentId);
    }

    @PostMapping("/{momentId}/comments")
    public Result createMomentComment(@PathVariable Long momentId,@RequestBody CommentDTO dto) {
        log.info("创建动态评论: momentId={}, content={}", momentId, dto.getContent());
        return momentService.createMomentComment(momentId, dto);
     }

     @DeleteMapping("/{momentId}/comments/{commentId}")
     public Result deleteMomentComment(@PathVariable Long momentId, @PathVariable Long commentId) {
         log.info("删除动态评论: momentId={}, commentId={}", momentId, commentId);
         return momentService.deleteMomentComment(commentId);
     }

    @GetMapping("/user/{userId}")
    public Result getUserProfile(@PathVariable Long userId) {
        log.info("查看用户信息: userId={}", userId);
        return momentService.getUserProfile(userId);
    }

    @GetMapping("/user/{userId}/moments")
    public Result getUserMoments(@PathVariable Long userId) {
        log.info("查看用户动态: userId={}", userId);
        return momentService.getUserMoments(userId);
    }

    @PostMapping("/user/{userId}/{momentId}/like")
    public Result likeUserMoment(@PathVariable Long userId, @PathVariable Long momentId) {
        log.info("用户点赞动态: userId={}, momentId={}", userId, momentId);
        return momentService.likeUserMoment(momentId);
    }

    @GetMapping("/user/{userId}/{momentId}/comments")
    public Result getMomentComments(@PathVariable Long userId, @PathVariable Long momentId) {
        log.info("查看用户动态评论: userId={}, momentId={}", userId, momentId);
        return momentService.getUserMomentComments(momentId);
    }

    @PostMapping("/user/{userId}/{momentId}/comments")
    public Result createMomentComment(@PathVariable Long userId, @PathVariable Long momentId, @RequestBody CommentDTO dto) {
        log.info("创建用户动态评论: userId={}, momentId={}, content={}", userId, momentId, dto.getContent());
        return momentService.createUserMomentComment(momentId, dto);
    }

    @DeleteMapping("/user/{userId}/{momentId}/comments/{commentId}")
    public Result deleteMomentComment(@PathVariable Long userId, @PathVariable Long momentId, @PathVariable Long commentId) {
        log.info("删除用户动态评论: userId={}, momentId={}, commentId={}", userId, momentId, commentId);
        return momentService.deleteMomentComment(commentId);
    }
}
