package com.cloudsteward.module.member.controller.app.inventory.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InventoryGoodsSaveReqVO {

    /**
     * ID 新增不传，修改时传
     */
    private Long id;
    /**
     * 门店ID
     */
    @NotNull(message = "门店不能为空")
    private Long storeId;
    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String goodsName;
    /**
     * 单位
     */
    @NotNull(message = "单位不能为空")
    private String units;
    /**
     * 是否启用
     */
    private Boolean enable;


}
