package com.zumo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zumo.dto.CommentDTO;
import com.zumo.dto.CreateMomentDTO;
import com.zumo.dto.Result;
import com.zumo.entity.Comment;
import com.zumo.entity.Moment;
import com.zumo.entity.User;
import com.zumo.mapper.MomentMapper;
import com.zumo.mapper.UserMapper;
import com.zumo.service.ICommentService;
import com.zumo.service.IMomentService;
import com.zumo.service.IUserService;
import com.zumo.utils.ThreadLocalUtil;
import com.zumo.vo.CommentVO;
import com.zumo.vo.MomentVO;
import com.zumo.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MomentServiceImpl extends ServiceImpl<MomentMapper, Moment> implements IMomentService {

    @Autowired
    private IUserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MomentMapper momentMapper;
    @Autowired
    private ICommentService commentService;

    @Override
    public Result<List<MomentVO>> getMoments() {
        try {
            // 获取所有动态，按创建时间倒序排序
            List<Moment> moments = query()
                .orderByDesc("create_time")
                .list();
            List<MomentVO> momentVOs = new ArrayList<>();

            for (Moment moment : moments) {
                MomentVO momentVO = new MomentVO();
                BeanUtils.copyProperties(moment, momentVO);

                // 设置用户信息
                User user = userService.getById(moment.getUserId());
                if (user != null) {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    momentVO.setUser(userVO);
                }

                // 解析图片URL数组
                if (moment.getImages() != null) {
                    List<String> images = objectMapper.readValue(moment.getImages(), new TypeReference<List<String>>() {});
                    momentVO.setImages(images);
                }

                // 设置点赞状态和点赞用户信息
                // 获取当前登录用户ID
                Map<String, Object> map = ThreadLocalUtil.get();
                Long userId = Long.valueOf(map.get("id").toString());
                
                // 获取Redis中的点赞信息
                String key = "moment:like:" + momentVO.getId();
                
                // 获取点赞用户数量
                Long likedCount = stringRedisTemplate.opsForZSet().size(key);
                momentVO.setLiked(likedCount != null ? likedCount : 0);
                
                // 获取当前用户是否点赞
                Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
                momentVO.setIslike(score != null);
                
                // 获取前5个点赞用户
                Set<String> top5UserIds = stringRedisTemplate.opsForZSet().range(key, 0, 4);
                if (top5UserIds != null && !top5UserIds.isEmpty()) {
                    List<Long> ids = top5UserIds.stream().map(Long::valueOf).collect(Collectors.toList());
                    String join = StrUtil.join(",", ids);
                    List<User> users = userService.query().in("id", ids).last("ORDER BY FIELD(id,"+ join + ")").list();
                    List<UserVO> userVOS = users.stream().map(u -> {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(u, userVO);
                        return userVO;
                    }).collect(Collectors.toList());
                    momentVO.setLikes(userVOS);
                } else {
                    momentVO.setLikes(new ArrayList<>());
                }
                
                momentVOs.add(momentVO);
            }

            return Result.success(momentVOs);
        } catch (Exception e) {
            log.error("获取动态列表失败", e);
            return Result.error("获取动态列表失败");
        }
    }

    @Override
    public Result<MomentVO> createMoment(CreateMomentDTO dto) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());

            // 创建动态
            Moment moment = new Moment();
            moment.setUserId(userId);
            moment.setContent(dto.getContent());
            moment.setCreateTime(LocalDateTime.now());
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                moment.setImages(objectMapper.writeValueAsString(dto.getImages()));
            }

            // 保存动态
            save(moment);

            // 转换为VO
            MomentVO momentVO = new MomentVO();
            BeanUtils.copyProperties(moment, momentVO);
            momentVO.setImages(dto.getImages());

            // 设置用户信息
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                momentVO.setUser(userVO);
            }

            return Result.success(momentVO);
        } catch (Exception e) {
            log.error("发布动态失败", e);
            return Result.error("发布动态失败");
        }
    }

    @Override
    public Result<MomentVO> getMomentDetail(Long momentId) {
        try {
            Moment moment = getById(momentId);
            if (moment == null) {
                return Result.error("动态不存在");
            }

            MomentVO momentVO = new MomentVO();
            BeanUtils.copyProperties(moment, momentVO);

            // 设置用户信息
            User user = userService.getById(moment.getUserId());
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                momentVO.setUser(userVO);
            }

            // 解析图片URL数组
            if (moment.getImages() != null) {
                List<String> images = objectMapper.readValue(moment.getImages(), new TypeReference<List<String>>() {});
                momentVO.setImages(images);
            }

            return Result.success(momentVO);
        } catch (Exception e) {
            log.error("获取动态详情失败", e);
            return Result.error("获取动态详情失败");
        }
    }

    @Override
    public Result<Void> deleteMoment(Long momentId) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());

            // 检查动态是否存在且属于当前用户
            Moment moment = getById(momentId);
            if (moment == null) {
                return Result.error("动态不存在");
            }
            if (!moment.getUserId().equals(userId)) {
                return Result.error("无权删除此动态");
            }

            // 删除动态
            removeById(momentId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除动态失败", e);
            return Result.error("删除动态失败");
        }
    }

    @Override
    public Result<Void> likeMoment(Long momentId) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());
            
            // 获取动态信息
            Moment moment = getById(momentId);
            if (moment == null) {
                return Result.error("动态不存在");
            }
            
            // 判断当前用户是否已经点过赞
            String key = "moment:like:" + momentId;
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            
            if(score == null){
                // 如果未点赞，则点赞
                // 更新数据库中的点赞数
                moment.setLiked(moment.getLiked() == null ? 1 : moment.getLiked() + 1);
                boolean isSuccess = updateById(moment);
                if (isSuccess){
                    // 保存用户ID到Redis集合中
                    stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
                }
            } else {
                // 如果已点赞，则取消点赞
                // 更新数据库中的点赞数
                moment.setLiked(Math.max(0, moment.getLiked() == null ? 0 : moment.getLiked() - 1));
                boolean isSuccess = updateById(moment);
                if (isSuccess){
                    // 从Redis集合中移除用户ID
                    stringRedisTemplate.opsForZSet().remove(key, userId.toString());
                }
            }
            return Result.success();
        } catch (Exception e) {
            log.error("点赞动态失败", e);
            return Result.error("点赞动态失败");
        }
    }

    @Override
    public Result<List<UserVO>> getMomentLikes(Long momentId) {
        try {
            // 获取Redis中的点赞用户ID列表（最早的5个）
            String key = "moment:like:" + momentId;
            Set<String> userIds = stringRedisTemplate.opsForZSet().range(key, 0, 4);
            
            if (userIds != null && !userIds.isEmpty()) {
                // 获取用户信息
                List<Long> ids = userIds.stream().map(Long::valueOf).collect(Collectors.toList());
                String join = StrUtil.join(",", ids);
                List<User> users = userService.query().in("id", ids).last("ORDER BY FIELD(id,"+ join + ")").list();
                
                // 转换为UserVO
                List<UserVO> userVOS = users.stream().map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).collect(Collectors.toList());
                
                return Result.success(userVOS);
            }
            
            return Result.success(new ArrayList<>());
        } catch (Exception e) {
            log.error("获取动态点赞用户列表失败", e);
            return Result.error("获取动态点赞用户列表失败");
        }
    }

    @Override
    public Result getMomentComments(Long momentId) {
        try {
            List<CommentVO> momentComments = commentService.query()
                .eq("moment_id", momentId)
                .orderByDesc("create_time")
                .list()
                .stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtils.copyProperties(comment, commentVO);
                    User user = userService.getById(comment.getUserId());
                    if (user != null) {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(user, userVO);
                        commentVO.setUser(userVO);
                    }
                    return commentVO;
                }).toList();
            return Result.success(momentComments);
        } catch (Exception e) {
            log.error("获取动态评论失败", e);
            return Result.error("获取动态评论失败");
        }
    }

    @Override
    public Result createMomentComment(Long momentId, CommentDTO commentDTO) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());

            Comment comment = new Comment();
            BeanUtil.copyProperties(commentDTO, comment);
            comment.setMomentId(momentId);
            comment.setUserId(userId);
            comment.setCreateTime(LocalDateTime.now());
            
            boolean save = commentService.save(comment);
            if (save) {
                // 转换为VO并返回
                CommentVO commentVO = new CommentVO();
                BeanUtils.copyProperties(comment, commentVO);
                User user = userService.getById(userId);
                if (user != null) {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    commentVO.setUser(userVO);
                }
                return Result.success(commentVO);
            }
            return Result.error("创建动态评论失败");
        } catch (Exception e) {
            log.error("创建动态评论失败", e);
            return Result.error("创建动态评论失败");
        }
    }

    @Override
    public Result<MomentVO> updateMoment(Long momentId, CreateMomentDTO dto) {
        try {
            Moment moment = getById(momentId);
            if (moment != null) {
                moment.setContent(dto.getContent());
                moment.setImages(JSONUtil.toJsonStr(dto.getImages()));
                boolean isSuccess = updateById(moment);
                if (isSuccess) {
                    MomentVO momentVO = new MomentVO();
                    BeanUtils.copyProperties(moment, momentVO);
                    return Result.success(momentVO);
                }
                return Result.error("更新动态失败");
            }
        }catch (Exception e){
            log.error("更新动态失败", e);
            return Result.error("更新动态失败");
        }
        return null;
    }

    @Override
    public Result getUserProfile(Long userId) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 转换为VO对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 获取用户动态数量
        Long momentCount = momentMapper.selectCount(
                new LambdaQueryWrapper<Moment>()
                        .eq(Moment::getUserId, userId)
        );
        userVO.setMomentCount(momentCount);

        return Result.success(userVO);
    }

    @Override
    public Result getUserMoments(Long userId) {
        try {
            // 获取所有动态，按创建时间倒序排序
            List<Moment> moments = query()
                    .orderByDesc("create_time")
                    .eq("user_id", userId)
                    .list();
            List<MomentVO> momentVOs = new ArrayList<>();

            for (Moment moment : moments) {
                MomentVO momentVO = new MomentVO();
                BeanUtils.copyProperties(moment, momentVO);

                // 设置用户信息
                User user = userService.getById(moment.getUserId());
                if (user != null) {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    momentVO.setUser(userVO);
                }

                // 解析图片URL数组
                if (moment.getImages() != null) {
                    List<String> images = objectMapper.readValue(moment.getImages(), new TypeReference<List<String>>() {
                    });
                    momentVO.setImages(images);
                }

                // 设置点赞状态和点赞用户信息
                // 获取当前登录用户ID
                Map<String, Object> map = ThreadLocalUtil.get();
                Long thisUserId = Long.valueOf(map.get("id").toString());

                // 获取Redis中的点赞信息
                String key = "moment:like:" + momentVO.getId();

                // 获取点赞用户数量
                Long likedCount = stringRedisTemplate.opsForZSet().size(key);
                momentVO.setLiked(likedCount != null ? likedCount : 0);

                // 获取当前用户是否点赞
                Double score = stringRedisTemplate.opsForZSet().score(key, thisUserId.toString());
                momentVO.setIslike(score != null);

                // 获取前5个点赞用户
                Set<String> top5UserIds = stringRedisTemplate.opsForZSet().range(key, 0, 4);
                if (top5UserIds != null && !top5UserIds.isEmpty()) {
                    List<Long> ids = top5UserIds.stream().map(Long::valueOf).collect(Collectors.toList());
                    String join = StrUtil.join(",", ids);
                    List<User> users = userService.query().in("id", ids).last("ORDER BY FIELD(id," + join + ")").list();
                    List<UserVO> userVOS = users.stream().map(u -> {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(u, userVO);
                        return userVO;
                    }).collect(Collectors.toList());
                    momentVO.setLikes(userVOS);
                } else {
                    momentVO.setLikes(new ArrayList<>());
                }

                momentVOs.add(momentVO);
            }

            return Result.success(momentVOs);
        } catch (Exception e) {
            log.error("获取动态列表失败", e);
            return Result.error("获取动态列表失败");
        }
    }

    @Override
    public Result likeUserMoment(Long momentId) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());

            // 获取动态信息
            Moment moment = getById(momentId);
            if (moment == null) {
                return Result.error("动态不存在");
            }

            // 判断当前用户是否已经点过赞
            String key = "moment:like:" + momentId;
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());

            if(score == null){
                // 如果未点赞，则点赞
                // 更新数据库中的点赞数
                moment.setLiked(moment.getLiked() == null ? 1 : moment.getLiked() + 1);
                boolean isSuccess = updateById(moment);
                if (isSuccess){
                    // 保存用户ID到Redis集合中
                    stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
                }
            } else {
                // 如果已点赞，则取消点赞
                // 更新数据库中的点赞数
                moment.setLiked(Math.max(0, moment.getLiked() == null ? 0 : moment.getLiked() - 1));
                boolean isSuccess = updateById(moment);
                if (isSuccess){
                    // 从Redis集合中移除用户ID
                    stringRedisTemplate.opsForZSet().remove(key, userId.toString());
                }
            }
            return Result.success();
        } catch (Exception e) {
            log.error("点赞动态失败", e);
            return Result.error("点赞动态失败");
        }
    }

    @Override
    public Result getUserMomentComments(Long momentId) {
        try {
            List<CommentVO> momentComments = commentService.query()
                    .eq("moment_id", momentId)
                    .orderByDesc("create_time")
                    .list()
                    .stream()
                    .map(comment -> {
                        CommentVO commentVO = new CommentVO();
                        BeanUtils.copyProperties(comment, commentVO);
                        User user = userService.getById(comment.getUserId());
                        if (user != null) {
                            UserVO userVO = new UserVO();
                            BeanUtils.copyProperties(user, userVO);
                            commentVO.setUser(userVO);
                        }
                        return commentVO;
                    }).toList();
            return Result.success(momentComments);
        } catch (Exception e) {
            log.error("获取动态评论失败", e);
            return Result.error("获取动态评论失败");
        }
    }

    @Override
    public Result createUserMomentComment(Long momentId, CommentDTO dto) {
        try {
            // 获取当前登录用户ID
            Map<String, Object> map = ThreadLocalUtil.get();
            Long userId = Long.valueOf(map.get("id").toString());

            Comment comment = new Comment();
            BeanUtil.copyProperties(dto, comment);
            comment.setMomentId(momentId);
            comment.setUserId(userId);
            comment.setCreateTime(LocalDateTime.now());

            boolean save = commentService.save(comment);
            if (save) {
                // 转换为VO并返回
                CommentVO commentVO = new CommentVO();
                BeanUtils.copyProperties(comment, commentVO);
                User user = userService.getById(userId);
                if (user != null) {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    commentVO.setUser(userVO);
                }
                return Result.success(commentVO);
            }
            return Result.error("创建动态评论失败");
        } catch (Exception e) {
            log.error("创建动态评论失败", e);
            return Result.error("创建动态评论失败");
        }
    }

    @Override
    public Result deleteMomentComment(Long commentId) {
        try {
            boolean remove = commentService.removeById(commentId);
            if (remove) {
                return Result.success();
            }
            return Result.error("删除动态评论失败");
        } catch (Exception e) {
            log.error("删除动态评论失败", e);
            return Result.error("删除动态评论失败");
        }
    }

}
