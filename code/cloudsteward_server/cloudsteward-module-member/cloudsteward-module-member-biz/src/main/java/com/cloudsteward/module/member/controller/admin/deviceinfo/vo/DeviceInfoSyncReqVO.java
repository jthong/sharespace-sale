package com.cloudsteward.module.member.controller.admin.deviceinfo.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class DeviceInfoSyncReqVO {
    @NotNull(message = "网关不能为空")
    private Long deviceId;
}
