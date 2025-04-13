package com.zumo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zumo.dto.Result;
import com.zumo.entity.User;
import com.zumo.vo.UserVO;

import java.util.Map;


public interface IUserService extends IService<User> {
    User findByUserName(String username);

    void register(String username, String password);

    Result<UserVO> getUserInfo();

    Result<String> login(Map<String, String> params);

    Result<User> update(User user);

    Result updateBackground(Map<String, String> params);

}
