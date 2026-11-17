package com.cloudsteward.module.member.forest;

import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.cloudsteward.module.member.service.iot.device.IspResult;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayConsumeReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayPrepareReqVO;
import com.cloudsteward.module.member.service.iot.groupPay.IotGroupPayScopeUrlReqVO;

public interface IotGroupPayClient {

    @Post(url = "https://openapi.yunlaoban.vip/api/isp/groupBuy/getScopeUrl",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IspResult<String> getScopeUrl(@JSONBody IotGroupPayScopeUrlReqVO reqVO,
                                   @Var("clientId") String clientId,
                                   @Var("secret") String secret);

    @Post(url = "https://openapi.yunlaoban.vip/api/isp/groupBuy/prepare",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IspResult<Object> prepare(@JSONBody IotGroupPayPrepareReqVO reqVO,
                              @Var("clientId") String clientId,
                              @Var("secret") String secret);

    @Post(url = "https://openapi.yunlaoban.vip/api/isp/groupBuy/consume",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IspResult<String> consume(@JSONBody IotGroupPayConsumeReqVO reqVO,
                               @Var("clientId") String clientId,
                               @Var("secret") String secret);

    @Post(url = "https://openapi.yunlaoban.vip/api/isp/groupBuy/consumeWithResult",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IspResult<String> consumeWithResult(@JSONBody IotGroupPayConsumeReqVO reqVO,
                                         @Var("clientId") String clientId,
                                         @Var("secret") String secret);

    @Post(url = "https://openapi.yunlaoban.vip/api/isp/groupBuy/revoke",
            headers = {
                    "Content-Type:application/json",
                    "clientId:${clientId}",
                    "secret:${secret}",
            })
    IspResult<String> revoke(@JSONBody IotGroupPayConsumeReqVO reqVO,
                              @Var("clientId") String clientId,
                              @Var("secret") String secret);
}
