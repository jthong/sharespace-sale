package com.cloudsteward.module.member.controller.app.coupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloudsteward.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class AppCouponActiveInfoRespVO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券key")
    private String couponKey;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "活动名称")
    private String activeName;

    @Schema(description = "总数量")
    private Integer num;

    @Schema(description = "剩余数量")
    private Integer balanceNum;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = DateUtils.FORMAT_ORDER_TIME, timezone = TIME_ZONE_DEFAULT)
    @DateTimeFormat(pattern = DateUtils.FORMAT_ORDER_TIME)
    private Date endTime;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = DateUtils.FORMAT_ORDER_TIME, timezone = TIME_ZONE_DEFAULT)
    @DateTimeFormat(pattern = DateUtils.FORMAT_ORDER_TIME)
    private Date expriceTime;

    @Schema(description = "适用门槛", requiredMode = Schema.RequiredMode.REQUIRED, example = "24523")
    private BigDecimal minUsePrice;

    @Schema(description = "优惠券面额", requiredMode = Schema.RequiredMode.REQUIRED, example = "14728")
    private BigDecimal price;

    @Schema(description = "适用门店id", example = "7851")
    private Long storeId;

    @Schema(description = "适用门店名称", example = "7851")
    private String storeName;

    @Schema(description = "适用房间类型 值见字典 ", example = "1")
    private Integer roomType;

    @Schema(description = "优惠券类型 值见字典", example = "1")
    private Integer type;


    @Schema(description = "是否已领取")
    private Boolean isGet;
}
