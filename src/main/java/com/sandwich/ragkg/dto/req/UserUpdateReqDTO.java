package com.sandwich.ragkg.dto.req;

import lombok.Data;

/**
 * 用户登录请求参数
 */
@Data
public class UserUpdateReqDTO {
    private String username;
    private String name;
    private String phone;
    private String email;
    private String avatar;
}
