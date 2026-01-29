package com.sandwich.ragkg.common.enums;

/**
 * @author sunwenzhi
 * @description
 * @date 2026/1/24
 */
public enum RoleEnum {
    USER("user"),
    ADMIN("admin");

    public final String role;
    RoleEnum(String role) {
        this.role = role;
    }
}
