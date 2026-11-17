package com.cloudsteward.module.member.service.iot.device;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetSubDevicesReqVO {

    private String gatewayNo;
}
