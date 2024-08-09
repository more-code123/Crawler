package com.wy.common.exceptions;

/**
 * @Author WY
 * @Date 2024/08/09 15:40
 * @Desc 客户端不存在异常
 **/
public class WorkerNotFoundException extends RuntimeException {
    public WorkerNotFoundException(String message) {
        super(message);
    }
}
