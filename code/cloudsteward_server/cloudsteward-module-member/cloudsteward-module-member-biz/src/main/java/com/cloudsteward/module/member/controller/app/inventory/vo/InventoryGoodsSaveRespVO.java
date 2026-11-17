package com.cloudsteward.module.member.controller.app.inventory.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class InventoryGoodsSaveRespVO {

    /**
     * ID 新增不传，修改时传
     */
    private Long id;
    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 单位
     */
    private String units;
    /**
     * 是否启用
     */
    private Boolean enable;

    @Schema(description = "创建人")
    private String nickname;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private Date createTime;


}
