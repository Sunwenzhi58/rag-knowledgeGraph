package com.sandwich.ragkg.dto.req;

import lombok.Data;

/**
 * 用户修改密码请求参数
 *
 * @author sunwenzhi
 * @date 2026/2/7
 */
@Data
public class UserUpdatePasswordReqDTO {
    /**
     * 原始密码
     */
    private String password;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码（前端校验用）
     */
    private String confirmPassword;
}
