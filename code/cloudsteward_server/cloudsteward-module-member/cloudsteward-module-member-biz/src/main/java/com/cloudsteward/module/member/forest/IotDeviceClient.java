package com.cloudsteward.module.member.forest;

import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.cloudsteward.module.member.service.iot.device.DeviceControlReqVO;
import com.cloudsteward.module.member.service.iot.device.GetSubDevicesReqVO;
import com.cloudsteward.module.member.service.iot.device.IotResult;

public interface IotDeviceClient {

    @Post(url = "http://iot.yunlaoban.vip:18080/iot/device/getSubDevices",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IotResult<String> getSubDevices(@JSONBody GetSubDevicesReqVO reqVO,
                                     @Var("clientId") String clientId,
                                     @Var("secret") String secret);

    @Post(url = "http://iot.yunlaoban.vip:18080/iot/device/control",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IotResult<String> control(@JSONBody DeviceControlReqVO reqVO,
                               @Var("clientId") String clientId,
                               @Var("secret") String secret);
}
