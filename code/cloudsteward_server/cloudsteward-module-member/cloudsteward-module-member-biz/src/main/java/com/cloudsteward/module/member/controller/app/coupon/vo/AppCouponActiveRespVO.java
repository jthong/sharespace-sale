package com.cloudsteward.module.member.controller.app.coupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloudsteward.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class AppCouponActiveRespVO {

    @Schema(description = "优惠券ID")
    private Long couponId;

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

}
