package com.cloudsteward.module.member.service.iot.device;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IotDeviceRoomInfoVO {

    private Long deviceId;

    private String deviceSn;

    private Long roomId;

    private String roomName;

    private String roomCallName;

    private Long storeId;

    private Integer type;

    private Integer minHour;

    private BigDecimal price;

    private boolean jumpClear;

    private Long tenantId;


}
