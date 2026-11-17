package com.cloudsteward.module.member.controller.app.inventory.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class AppUserInventoryDetailRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "商品id")
    private Long goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "单位")
    private String units;

    @Schema(description = "存入数量")
    private Integer saveNum;

    @Schema(description = "剩余数量")
    private Integer stockNum;

    @Schema(description = "寄存单id")
    private Long inventoryId;

    @Schema(description = "单号")
    private String orderNo;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private Date expireTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private Date createTime;





}
