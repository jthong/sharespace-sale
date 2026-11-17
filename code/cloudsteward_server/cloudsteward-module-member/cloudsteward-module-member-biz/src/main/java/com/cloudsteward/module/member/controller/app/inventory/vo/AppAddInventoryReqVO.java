package com.cloudsteward.module.member.controller.app.inventory.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.cloudsteward.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class AppAddInventoryReqVO {

    @Schema(description = "用户手机号")
    @NotNull(message = "用户手机号不能为空")
    @Mobile
    private String phone;

    @Schema(description = "门店ID")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "过期时间")
    @NotNull(message = "过期时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date expireTime;


    @Schema(description = "商品列表")
    @NotEmpty(message = "商品列表不能为空")
    private List<AppGoodsVO> goodsList;

}
