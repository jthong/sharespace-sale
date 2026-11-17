package com.cloudsteward.module.member.controller.app.callback;

import com.alibaba.fastjson.JSONObject;
import com.cloudsteward.framework.operatelog.core.annotations.OperateLog;
import com.cloudsteward.module.member.service.wx.partner.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/callback/wxpay/partner")
public class WechatPartnerCallbackController {
    @Resource private WechatPartnerClient client;
    @Resource private WechatPartnerPaymentService payments;
    @Resource private WechatPartnerProperties properties;
    private JSONObject decode(String body,HttpServletRequest request) {
        return client.decodeNotification(body,request.getHeader("Wechatpay-Timestamp"),request.getHeader("Wechatpay-Nonce"),request.getHeader("Wechatpay-Serial"),request.getHeader("Wechatpay-Signature"));
    }
    @PostMapping("/notify") @PermitAll @OperateLog(enable=false)
    public ResponseEntity<Void> payment(@RequestBody String body,HttpServletRequest request) {
        JSONObject notification;
        try { notification=decode(body,request);if(!"TRANSACTION.SUCCESS".equals(notification.getString("event_type")))return ResponseEntity.badRequest().build(); }
        catch(Exception e){return ResponseEntity.badRequest().build();}
        try {payments.settle(notification.getJSONObject("resource"));return ResponseEntity.noContent().build();}
        catch(Exception e){return ResponseEntity.status(500).build();}
    }
    @PostMapping("/refund") @PermitAll @OperateLog(enable=false)
    public ResponseEntity<Void> refund(@RequestBody String body,HttpServletRequest request) {
        JSONObject notification;
        try { notification=decode(body,request);String event=notification.getString("event_type");
            if(!java.util.Arrays.asList("REFUND.SUCCESS","REFUND.ABNORMAL","REFUND.CLOSED").contains(event))return ResponseEntity.badRequest().build();
            JSONObject resource=notification.getJSONObject("resource");
            if(resource==null||!properties.getSpMchId().equals(resource.getString("mchid")))return ResponseEntity.badRequest().build();
        } catch(Exception e){return ResponseEntity.badRequest().build();}
        try{payments.acceptRefund(notification.getJSONObject("resource"));return ResponseEntity.noContent().build();}
        catch(Exception e){return ResponseEntity.status(500).build();}
    }
}
