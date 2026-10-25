package com.starlight.cms.exception;

import lombok.Getter;

/**
 * 自定义业务异常：业务规则不满足时抛出（如座位已售、余额不足）
 * 由全局异常处理器统一捕获并转成 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
