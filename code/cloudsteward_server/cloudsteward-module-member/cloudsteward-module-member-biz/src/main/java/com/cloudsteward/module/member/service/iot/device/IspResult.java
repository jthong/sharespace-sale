package com.cloudsteward.module.member.service.iot.device;

import lombok.Data;

@Data
public class IspResult<T> {

    private String code;

    private String message;

    private String msg;

    private String traceId;

    private T result;

    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
}
