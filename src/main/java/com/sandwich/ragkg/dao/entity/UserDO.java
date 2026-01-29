package com.sandwich.ragkg.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author sunwenzhi
 * @description 用户类
 * @date 2026/1/24
 */
@Data
@TableName(value = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    @TableId
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("role")
    private String role;

    @TableField("org_tags")
    private String orgTags; // 用户所属组织标签，多个用逗号分隔

    @TableField("primary_org")
    private String primaryOrg; // 用户主组织标签

    @TableField("create_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
