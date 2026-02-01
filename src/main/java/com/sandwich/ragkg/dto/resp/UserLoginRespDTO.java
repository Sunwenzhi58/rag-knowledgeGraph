package com.sandwich.ragkg.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口返回响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRespDTO {
    private String username;
    private String role;
    private String name;
    private String phone;
    private String email;
    private String avatar;
    private String orgTags;
    private String primaryOrg;
    /**
     * 用户Token
     */
    private String token;
}
