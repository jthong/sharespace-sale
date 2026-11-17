package com.cloudsteward.module.member.controller.app.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AppInventoryPageReqVO {

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "状态 0-待取, 1-已取, 2-过期")
    private Integer status;

    @Schema(hidden = true)
    private Long userId;

}
