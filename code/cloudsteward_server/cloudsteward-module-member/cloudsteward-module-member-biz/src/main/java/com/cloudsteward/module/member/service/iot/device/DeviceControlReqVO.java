package com.cloudsteward.module.member.service.iot.device;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceControlReqVO {

    private String deviceNo;

    private String gatewayNo;

    private Integer type;

    private String userName;

    private String pwd;

    private String keyboardPwd;

    private Long keyboardPwdId;

    private String newKeyboardPwd;

    private Integer outlet;

    private String msgType;

    private String voiceMsg;

    private String voiceIds;

    private String orderId;

    private String mp3Url;

    private Integer volume;

    private String audioId;

    private String cmd;
}
