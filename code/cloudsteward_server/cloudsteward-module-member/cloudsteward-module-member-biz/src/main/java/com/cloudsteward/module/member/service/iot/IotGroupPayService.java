package com.cloudsteward.module.member.service.iot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.cloudsteward.module.infra.api.config.ConfigApi;
import com.cloudsteward.module.member.forest.IotGroupPayClient;
import com.cloudsteward.module.member.service.iot.device.IspResult;
import com.cloudsteward.module.member.service.iot.groupPay.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

import static com.cloudsteward.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.cloudsteward.module.member.enums.ErrorCodeConstants.IOT_ERROR;

@Slf4j
@Component
public class IotGroupPayService {

    @Resource
    private ConfigApi configApi;

    @Resource
    private IotGroupPayClient iotGroupPayClient;

    public String clientId() {
        return configApi.getConfigValueByKey("iot.clientId");
    }

    public String secret() {
        return configApi.getConfigValueByKey("iot.secret");
    }

    public String getScopeUrl(IotGroupPayScopeUrlReqVO reqVO) {
        IspResult<String> result = iotGroupPayClient.getScopeUrl(reqVO, clientId(), secret());
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw exception(IOT_ERROR, result.getMessage());
        }
    }

    public IotGroupPayPrepareRespVO prepare(IotGroupPayPrepareReqVO reqVO) {
        reqVO.setCode(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.normalize(reqVO.getCode()));
        reqVO.setPlatform(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.detect(reqVO.getCode()));
        IspResult<Object> response = iotGroupPayClient.prepare(reqVO, clientId(), secret());
        if (response == null) {
            throw exception(IOT_ERROR, "团购平台未返回验券结果");
        }
        if (!response.isSuccess()) {
            String message = StringUtils.hasText(response.getMessage()) ? response.getMessage() : response.getMsg();
            throw exception(IOT_ERROR, StringUtils.hasText(message) ? message : "团购平台验券失败");
        }

        // 云老板的 result 可能是 JSON 对象，也可能是包含同一个对象的 JSON 字符串。
        Object result = response.getResult();
        if (!(result instanceof String) && !(result instanceof Map)) {
            throw exception(IOT_ERROR, "团购平台返回的券信息格式异常");
        }
        IotGroupPayPrepareRespVO ticket;
        try {
            String json = result instanceof String ? (String) result : JSON.toJSONString(result);
            ticket = JSON.parseObject(json, IotGroupPayPrepareRespVO.class);
        } catch (JSONException e) {
            throw exception(IOT_ERROR, "团购平台返回的券信息格式异常");
        }
        if (ticket == null || !StringUtils.hasText(ticket.getTicketName())
                || ticket.getPayAmount() == null || ticket.getPayAmount() < 0) {
            throw exception(IOT_ERROR, "团购平台返回的券信息不完整");
        }
        return ticket;
    }

    public String consume(IotGroupPayConsumeReqVO reqVO) {
        reqVO.setCode(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.normalize(reqVO.getCode()));
        reqVO.setPlatform(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.detect(reqVO.getCode()));
        IspResult<String> result = iotGroupPayClient.consume(reqVO, clientId(), secret());
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw exception(IOT_ERROR, result.getMessage());
        }
    }

    public String consumeWithResult(IotGroupPayConsumeReqVO reqVO) {
        reqVO.setCode(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.normalize(reqVO.getCode()));
        reqVO.setPlatform(com.cloudsteward.module.member.service.groupPay.GroupBuyPlatform.detect(reqVO.getCode()));
        IspResult<String> result = iotGroupPayClient.consumeWithResult(reqVO, clientId(), secret());
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw exception(IOT_ERROR, result.getMessage());
        }
    }

    public String revoke(IotGroupPayConsumeReqVO reqVO) {
        IspResult<String> result = iotGroupPayClient.revoke(reqVO, clientId(), secret());
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw exception(IOT_ERROR, result.getMessage());
        }
    }
}
