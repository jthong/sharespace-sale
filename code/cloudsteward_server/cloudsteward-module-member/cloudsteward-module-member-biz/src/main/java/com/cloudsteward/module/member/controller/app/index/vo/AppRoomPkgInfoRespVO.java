package com.cloudsteward.module.member.controller.app.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppRoomPkgInfoRespVO {

    @Schema(description = "套餐名称")
    private String pkgName;

    @Schema(description = "售价")
    private BigDecimal price;

}
