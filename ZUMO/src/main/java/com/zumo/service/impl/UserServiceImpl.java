package com.zumo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zumo.dto.Result;
import com.zumo.entity.User;
import com.zumo.mapper.UserMapper;
import com.zumo.service.IUserService;
import com.zumo.utils.JwtUtil;
import com.zumo.utils.Md5Util;
import com.zumo.utils.ThreadLocalUtil;
import com.zumo.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User user = query().eq("username", username).one();
        return user;
    }

    /**
     * 注册
     * @param username
     * @param password
     */
    @Override
    public void register(String username, String password) {
        //加密密码
        String md5String = Md5Util.getMD5String(password);
        User u = new User();
        u.setUsername(username);
        u.setPassword(md5String);
        u.setNickName(username);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        userMapper.insert(u);
    }

    /**
     * 获取用户信息
     * @return
     */
    @Override
    public Result<UserVO> getUserInfo() {
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = findByUserName(username);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return Result.success(userVO);
    }

    /**
     * 登录
     * @param params
     * @return
     */
    @Override
    public Result<String> login(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)) {
            return Result.error("用户名和密码不能为空");
        }

        User u = findByUserName(username);
        if(u == null) return Result.error("用户名错误");

        if(Md5Util.getMD5String(password).equals(u.getPassword())){
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",u.getId());
            claims.put("username",u.getUsername());
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        } else {
            return Result.error("密码错误");
        }
    }

    @Override
    public Result<User> update(User user) {
        // 获取当前登录用户信息
        Map<String, Object> map = ThreadLocalUtil.get();
        Long currentUserId = Long.valueOf(map.get("id").toString());
        
        // 验证是否有权限修改
        if (!currentUserId.equals(user.getId())) {
            return Result.error("无权修改其他用户信息");
        }
        
        // 获取当前用户信息
        User currentUser = getById(currentUserId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        
        // 只允许修改特定字段
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setNickName(user.getNickName());
        updateUser.setBio(user.getBio());
        updateUser.setAvatarUrl(user.getAvatarUrl());
        updateUser.setBackgroundUrl(currentUser.getBackgroundUrl()); // 保留原有的背景图片
        updateUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(updateUser);
        return Result.success();
    }

    @Override
    public Result updateBackground(Map<String, String> params) {
        String backgroundUrl = params.get("backgroundUrl");
        if (!StringUtils.hasLength(backgroundUrl)) {
            return Result.error("背景图片不能为空");
        }
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            Long currentUserId = Long.valueOf(map.get("id").toString());
            
            // 获取当前用户信息
            User currentUser = getById(currentUserId);
            if (currentUser == null) {
                return Result.error("用户不存在");
            }
            
            // 更新背景图片，保留其他字段
            User updateUser = new User();
            updateUser.setId(currentUserId);
            updateUser.setBackgroundUrl(backgroundUrl);
            updateUser.setNickName(currentUser.getNickName());
            updateUser.setBio(currentUser.getBio());
            updateUser.setAvatarUrl(currentUser.getAvatarUrl());
            updateUser.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(updateUser);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

}
