package com.sandwich.ragkg.common.exception;

import com.sandwich.ragkg.common.enums.ResultCodeEnum;

/**
 * 业务自定义异常，用于携带“错误码 + 错误信息”，符合对外 HTTP 接口返回错误码的规范。
 * 命名以 Exception 结尾，符合《阿里巴巴 Java 开发手册》异常类命名要求。
 *
 * @author project
 */
public class CustomException extends RuntimeException {

    private final String code;
    private final String msg;

    /**
     * 使用枚举定义错误码和默认文案
     */
    public CustomException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.msg);
        this.code = resultCodeEnum.code;
        this.msg = resultCodeEnum.msg;
    }

    /**
     * 使用枚举 + 自定义文案（覆盖枚举默认文案）
     */
    public CustomException(ResultCodeEnum resultCodeEnum, String overrideMsg) {
        super(overrideMsg != null ? overrideMsg : resultCodeEnum.msg);
        this.code = resultCodeEnum.code;
        this.msg = overrideMsg != null ? overrideMsg : resultCodeEnum.msg;
    }

    /**
     * 直接指定错误码和文案（用于暂无枚举的场景，后续建议收敛到 ResultCodeEnum）
     */
    public CustomException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
