package com.sandwich.ragkg.common.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.sandwich.ragkg.common.Result;
import com.sandwich.ragkg.common.enums.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器。对 Web 层捕获异常并转为统一 Result，不再向上抛出，
 * 符合《阿里巴巴 Java 开发手册》中“开放接口层要将异常处理成错误码和错误信息方式返回”的规约。
 */
@ControllerAdvice(basePackages = "com.sandwich.ragkg.controller")
@ResponseBody
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.get();

    /**
     * 业务自定义异常：按错误码、错误信息返回，便于前端/调用方识别
     */
    @ExceptionHandler(CustomException.class)
    public Result customError(CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 其它未分类异常：记日志并返回通用系统错误，避免暴露内部细节
     */
    @ExceptionHandler(Exception.class)
    public Result error(Exception e) {
        log.error("异常信息：", e);
        return Result.error();
    }
}
