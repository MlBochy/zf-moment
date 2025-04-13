package com.zumo.controller;

import com.zumo.dto.Result;
import com.zumo.entity.User;
import com.zumo.service.IUserService;
import com.zumo.utils.Md5Util;
import com.zumo.utils.ThreadLocalUtil;
import com.zumo.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;
    //注册
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)) {
            return Result.error("用户名和密码不能为空");
        }
        
        if (!username.matches("^\\S{5,16}$") || !password.matches("^\\S{5,16}$")) {
            return Result.error("用户名和密码长度必须在5-16位之间");
        }
        
        User u = userService.findByUserName(username);
        if(u != null) {
            return Result.error("用户名已存在");
        } else {
            userService.register(username, password);
            return Result.success("注册成功");
        }
    }
    //登录
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, String> params) {
        return userService.login(params);
    }
    //查看基本信息
    @GetMapping("userInfo")
    public Result<UserVO> userInfo(/*@RequestHeader(name = "Authorization") String token*/){
        return userService.getUserInfo();
    }
    //更新基本信息
    @PutMapping("/update")
    public Result<User> update(@RequestBody @Validated User user){

        return userService.update(user);
    }

    @PatchMapping("/updateAvatar")
    public Result updateAvatar (@RequestParam @URL String avatarUrl){
        //userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updateBackground")
    public Result updateBackground(@RequestBody Map<String, String> params) {
        return userService.updateBackground(params);
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params){
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        if(!StringUtils.hasLength(oldPwd)||!StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd)){
            return Result.error("缺少参数");
        }

        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        if(!Md5Util.getMD5String(oldPwd).equals(user.getPassword())){
            return Result.error("原密码错误");
        }

        if(!newPwd.equals(rePwd))return Result.error("两次密码不一致");

        //userService.updatePwd(newPwd);
        return Result.success();
    }

}
