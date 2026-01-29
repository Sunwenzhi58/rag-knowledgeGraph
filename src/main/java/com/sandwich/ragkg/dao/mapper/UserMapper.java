package com.sandwich.ragkg.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sandwich.ragkg.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sunwenzhi
 * @description 用户持久层
 * @date 2026/1/24
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    UserDO findByUserName(String username);
}
