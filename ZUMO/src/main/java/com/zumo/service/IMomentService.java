package com.zumo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zumo.dto.CommentDTO;
import com.zumo.dto.Result;
import com.zumo.entity.Moment;
import com.zumo.vo.MomentVO;
import com.zumo.dto.CreateMomentDTO;
import com.zumo.vo.UserVO;

import java.util.List;

public interface IMomentService extends IService<Moment> {
    /**
     * 获取动态列表
     * @return
     */
    Result<List<MomentVO>> getMoments();

    /**
     * 创建动态
     * @param dto
     * @return
     */
    Result<MomentVO> createMoment(CreateMomentDTO dto);

    /**
     * 获取动态详情
     * @param momentId
     * @return
     */
    Result<MomentVO> getMomentDetail(Long momentId);

    /**
     * 删除动态
     * @param momentId
     * @return
     */
    Result<Void> deleteMoment(Long momentId);

    /**
     * 点赞动态
     * @param momentId
     * @return
     */
    Result<Void> likeMoment(Long momentId);

    Result getMomentLikes(Long momentId);

    Result getMomentComments(Long momentId);

    Result createMomentComment(Long momentId, CommentDTO dto);

    Result<MomentVO> updateMoment(Long momentId, CreateMomentDTO dto);

    Result getUserProfile(Long userId);

    Result getUserMoments(Long userId);

    Result<Void> likeUserMoment(Long momentId);

    Result getUserMomentComments(Long momentId);

    Result createUserMomentComment(Long momentId, CommentDTO dto);

    Result deleteMomentComment(Long commentId);

}
