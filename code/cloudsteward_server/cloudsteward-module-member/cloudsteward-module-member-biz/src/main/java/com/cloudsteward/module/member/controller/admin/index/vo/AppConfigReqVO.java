package com.cloudsteward.module.member.controller.admin.index.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppConfigReqVO {

    @NotNull(message = "接口地址不能为空，格式：api.xxxxxx.com")
    private String hostname;
    private String clientId;
    private String secret;
    private String groupPay;
    private String dyappKey;
    private String dysecret;
    private String mtappKey;
    private String mtsecret;
}
