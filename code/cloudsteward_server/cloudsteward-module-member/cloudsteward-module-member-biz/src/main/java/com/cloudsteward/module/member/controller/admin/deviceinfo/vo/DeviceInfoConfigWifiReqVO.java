package com.cloudsteward.module.member.controller.admin.deviceinfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeviceInfoConfigWifiReqVO {


    @Schema(description = "设备id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备id不能为空")
    private Long deviceId;


    @Schema(description = "wifi名称")
    private String ssid;


    @Schema(description = "wifi密码")
    private String passwd;

}
