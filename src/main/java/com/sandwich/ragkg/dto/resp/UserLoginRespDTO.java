package com.sandwich.ragkg.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口返回响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRespDTO {
    private String username;
    private String role;
    private String orgTags;
    private String primaryOrg;
    /**
     * 用户Token
     */
    private String token;
}
