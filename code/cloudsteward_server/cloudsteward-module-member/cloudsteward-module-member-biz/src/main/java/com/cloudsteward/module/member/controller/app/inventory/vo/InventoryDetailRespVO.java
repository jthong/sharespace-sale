package com.cloudsteward.module.member.controller.app.inventory.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.cloudsteward.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;


@Data
public class InventoryDetailRespVO {

    //记录ID
    private Long id;
    //门店ID
    private Long storeId;
    //门店名称
    private String storeName;
    //寄存单ID
    private Long inventoryId;
    //商品ID
    private Long goodsId;
    //商品名称
    private String goodsName;
    //单位
    private String units;
    //存入数量
    private Integer saveNum;
    //剩余数量、过期数量、取出数量
    private Integer stockNum;
    //创建时间
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private Date createTime;
    //过期时间
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private Date expireTime;

}
