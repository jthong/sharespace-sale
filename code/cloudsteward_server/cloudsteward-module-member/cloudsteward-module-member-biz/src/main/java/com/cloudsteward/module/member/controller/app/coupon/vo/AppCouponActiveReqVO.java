package com.cloudsteward.module.member.controller.app.coupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloudsteward.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class AppCouponActiveReqVO {

    @Schema(description = "优惠券ID")
    @NotNull(message = "优惠券不能为空")
    private Long couponId;

    @Schema(description = "活动名称")
    @NotNull(message = "活动名称不能为空")
    private String activeName;

    @Schema(description = "总数量")
    @NotNull(message = "总数量不能为空")
    @Min(value = 1,message = "最小数量1")
    private Integer num;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = DateUtils.FORMAT_ORDER_TIME, timezone = TIME_ZONE_DEFAULT)
    @DateTimeFormat(pattern = DateUtils.FORMAT_ORDER_TIME)
    @NotNull(message = "截止时间不能为空")
    private Date endTime;

}
