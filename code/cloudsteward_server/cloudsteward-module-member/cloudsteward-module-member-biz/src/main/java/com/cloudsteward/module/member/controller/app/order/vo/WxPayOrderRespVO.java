package com.cloudsteward.module.member.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @PACKAGE_NAME: com.cloudsteward.module.member.controller.app.order.vo
 * @DESCRIPTION:
 * @USER: MrGuan  mrguan@aliyun.com
 * @DATE: 2023/8/3 17:07
 */
@Schema(description = "miniapp - 微信支付，下单接口 Response VO")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WxPayOrderRespVO implements Serializable {

    @Schema(description = "商户 订单号")
    private String orderNo;
    @Schema(description = "应用ID")
    private String appId;
    @Schema(description = "随机字符串")
    private String nonceStr;
    @Schema(description = "订单详情扩展字符串，原名package")
    private String pkg;
    @Schema(description = "签名方式")
    private String signType;
    @Schema(description = "时间戳")
    private String timeStamp;
    @Schema(description = "签名")
    private String paySign;
    @Schema(description = "计算出来的订单总价(包含押金) 单位/分")
    private Integer price;
    @Schema(description = "实际需微信支付金额 单位/分")
    private Integer payPrice;
    @Schema(description = "订单实际开始时间")
    private Date startTime;
    @Schema(description = "订单实际结束时间")
    private Date endTime;
    @Schema(description = "包场标记")
    private Integer timeIndex;


    //易宝收款订单号
    private String uniqueOrderNo;

    //预支付标识信息
    private String prePayTn;

    //跳转小程序的路径
    private String miniProgramPath;

    //小程序原始id
    private String miniProgramOrgId;

}
