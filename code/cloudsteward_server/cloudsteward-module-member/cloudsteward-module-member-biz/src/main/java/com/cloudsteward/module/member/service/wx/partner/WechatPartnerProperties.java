package com.cloudsteward.module.member.service.wx.partner;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ToString(onlyExplicitlyIncluded = true)
@Component
@ConfigurationProperties(prefix = "wx.pay.partner")
public class WechatPartnerProperties {
    private boolean enabled;
    private String spAppId;
    private String spMchId;
    private String miniAppId;
    private String serialNumber;
    private String privateKeyPath;
    private String certificatePath;
    private String apiV3Key;
    private String notifyUrl;
    private String refundNotifyUrl;
    private String payerScope = "sub";
}
