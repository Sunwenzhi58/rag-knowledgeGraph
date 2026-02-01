package com.sandwich.ragkg.controller;

import com.sandwich.ragkg.common.Result;
import com.sandwich.ragkg.dto.req.UserLoginRequestDTO;
import com.sandwich.ragkg.dto.req.UserRegisterReqDTO;
import com.sandwich.ragkg.dto.req.UserUpdateReqDTO;
import com.sandwich.ragkg.service.UserService;
import com.sandwich.ragkg.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author sunwenzhi
 * @description 用户控制器类
 * @date 2026/1/24
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserLoginRequestDTO requestParam) {
        return Result.success(userService.login(requestParam));
    }

    @PutMapping("/update")
    public Result update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Result.success();
    }
    @GetMapping("/test1")
    public Result test(){
        return Result.success("测试成功");
    }
}
