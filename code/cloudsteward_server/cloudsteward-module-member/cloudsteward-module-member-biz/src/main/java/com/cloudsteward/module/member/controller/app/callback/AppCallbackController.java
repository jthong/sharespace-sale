package com.cloudsteward.module.member.controller.app.callback;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.common.pojo.CommonResult;
import com.cloudsteward.framework.operatelog.core.annotations.OperateLog;
import com.cloudsteward.module.member.service.iot.IotService;
import com.cloudsteward.module.member.service.payorder.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

import static com.cloudsteward.framework.common.pojo.CommonResult.success;

@Tag(name = "miniapp - 回调")
@RestController
@RequestMapping("/callback")
@Validated
@Slf4j
public class AppCallbackController {

    @Resource
    private PayOrderService payOrderService;

    @Resource
    private IotService iotService;


    @PostMapping("/wxpay/update")
    @Operation(summary = "微信支付回调")
    @PermitAll
    @OperateLog(enable = false)
    @Parameter(name = "xmlData")
    public String updateWxOrder(@RequestBody String xmlData) {
        return payOrderService.updateWxOrder(xmlData);
    }

    @PostMapping("/yeepay")
    @Operation(summary = "易宝支付回调")
    @PermitAll
    @OperateLog(enable = false)
    public String updateYeepayOrder(@RequestBody JSONObject data) {
        return payOrderService.updateYeepayOrder(data);
    }


    @PostMapping("/wxpay/urefunded")
    @Operation(summary = "微信退款回调")
    @PermitAll
    @OperateLog(enable = false)
    public String updateWxRefunded(@RequestBody String xmlData) {
        return payOrderService.updateWxRefunded(xmlData);
    }

    @PostMapping(value = "/iotCallback")
    @Operation(summary = "物联网平台回调")
    @PermitAll
    @OperateLog(enable = false)
    public CommonResult<JSONObject> iotCallback(@RequestBody JSONObject json, HttpServletResponse response) {
        log.info("收到物联网平台回调,params:{}", json);
        return success(iotService.iotPlatform(json, response));
    }

}
