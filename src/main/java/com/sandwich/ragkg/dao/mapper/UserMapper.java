package com.sandwich.ragkg.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sandwich.ragkg.dao.entity.UserDO;
import com.sandwich.ragkg.dto.req.UserUpdateReqDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sunwenzhi
 * @description 用户持久层
 * @date 2026/1/24
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    UserDO findByUserName(String username);

    void update(UserUpdateReqDTO requestParam);

    /**
     * 更新用户密码
     *
     * @param username 用户名
     * @param encodedPassword 加密后的新密码
     * @return 影响行数
     */
    int updatePassword(String username, String encodedPassword);
}
