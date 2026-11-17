package com.cloudsteward.module.member.controller.app.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class AppInventoryPageRespVO {


    @Schema(description = "门店名称")
    private String storeName;


    @Schema(description = "商品信息")
    private List<InventoryDetailRespVO> goodsList;

}
