package com.sandwich.ragkg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandwich.ragkg.common.enums.ResultCodeEnum;
import com.sandwich.ragkg.common.enums.RoleEnum;
import com.sandwich.ragkg.dao.entity.UserDO;
import com.sandwich.ragkg.dao.mapper.UserMapper;
import com.sandwich.ragkg.dto.req.UserLoginRequestDTO;
import com.sandwich.ragkg.dto.req.UserRegisterReqDTO;
import com.sandwich.ragkg.common.exception.CustomException;
import com.sandwich.ragkg.dto.req.UserUpdateReqDTO;
import com.sandwich.ragkg.dto.resp.UserLoginRespDTO;
import com.sandwich.ragkg.service.UserService;
import com.sandwich.ragkg.utils.JwtUtils;
import com.sandwich.ragkg.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunwenzhi
 * @description 用户接口实现层
 * @date 2026/1/24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    JwtUtils jwtUtils;
    private static final String PRIVATE_TAG_PREFIX = "PRIVATE_";
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 检查数据库中是否已存在该用户名
        UserDO user = userMapper.findByUserName(requestParam.getUsername());
        if (ObjectUtil.isNotEmpty(user)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        requestParam.setRole(RoleEnum.USER.role);
        requestParam.setPassword(PasswordUtil.encode(requestParam.getPassword()));
        String privateTagId = PRIVATE_TAG_PREFIX + requestParam.getUsername();
        // 只分配私人组织标签
        requestParam.setOrgTags(privateTagId);
        // 设置私人组织标签为主组织标签
        requestParam.setPrimaryOrg(privateTagId);
        int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
        if (inserted < 1) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR, "插入数据库失败");
        }
//        // 缓存组织标签信息
//        orgTagCacheService.cacheUserOrgTags(username, List.of(privateTagId));
//        orgTagCacheService.cacheUserPrimaryOrg(username, privateTagId);
    }

    @Override
    public String authenticateUser(String username, String password) {
        UserDO user = userMapper.findByUserName(username);
        if (user == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        return user.getUsername();
    }

    @Override
    public UserLoginRespDTO login(UserLoginRequestDTO requestParam) {
        if (requestParam.getUsername() == null || requestParam.getUsername().isEmpty()
                || requestParam.getPassword() == null || requestParam.getPassword().isEmpty()) {
            throw new CustomException(ResultCodeEnum.PARAM_IS_NULL);
        }
        UserDO userDO = baseMapper.selectOne(Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername()));
        if (userDO == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        String username = authenticateUser(requestParam.getUsername(), requestParam.getPassword());
        String token = jwtUtils.generateToken(username);
        return UserLoginRespDTO.builder()
                .username(userDO.getUsername())
                .name(userDO.getName())
                .role(userDO.getRole())
                .phone(userDO.getPhone())
                .email(userDO.getEmail())
                .orgTags(userDO.getOrgTags())
                .primaryOrg(userDO.getPrimaryOrg())
                .token(token)
                .build();
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        userMapper.update(requestParam);
    }

}
