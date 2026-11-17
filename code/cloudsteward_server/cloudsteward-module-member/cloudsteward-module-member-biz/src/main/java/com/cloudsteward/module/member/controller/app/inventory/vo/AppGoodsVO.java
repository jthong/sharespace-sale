package com.cloudsteward.module.member.controller.app.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class AppGoodsVO {

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @Min(value = 1, message = "商品数量最小为1")
    @Max(value = 99999, message = "商品数量最大为99999")
    @NotNull(message = "商品数量不能为空")
    private Integer num;
}