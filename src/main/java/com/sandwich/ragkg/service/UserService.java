package com.sandwich.ragkg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sandwich.ragkg.dao.entity.UserDO;
import com.sandwich.ragkg.dto.req.UserLoginReqDTO;
import com.sandwich.ragkg.dto.req.UserRegisterReqDTO;
import com.sandwich.ragkg.dto.req.UserUpdatePasswordReqDTO;
import com.sandwich.ragkg.dto.req.UserUpdateReqDTO;
import com.sandwich.ragkg.dto.resp.UserLoginRespDTO;

/**
 * @author sunwenzhi
 * @description 用户接口层
 * @date 2026/1/24
 */
public interface UserService extends IService<UserDO> {
    void register(UserRegisterReqDTO requestParam);

    String authenticateUser(String username, String password);

    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    void update(UserUpdateReqDTO requestParam);

    void updatePassword(UserUpdatePasswordReqDTO requestParam);
}
