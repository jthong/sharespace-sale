package com.cloudsteward.module.member.controller.app.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloudsteward.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class AppPayOrderRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付单号")
    private String payOrderNo;

    @Schema(description = "支付金额")
    private Integer price;

    @Schema(description = "退款金额")
    private Integer refundPrice;

    @Schema(description = "商品内容")
    private String goodsName;

    @JsonFormat(pattern = DateUtils.FORMAT_ORDER_TIME, timezone = TIME_ZONE_DEFAULT)
    @DateTimeFormat(pattern = DateUtils.FORMAT_ORDER_TIME)
    private Date payTime;


}
