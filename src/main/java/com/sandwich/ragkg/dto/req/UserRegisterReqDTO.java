package com.sandwich.ragkg.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {

    private Long id;

    private String username;

    private String password;

    private String role;

    private String orgTags;

    private String primaryOrg;

    private String name;

    private String phone;

    private String email;
}
