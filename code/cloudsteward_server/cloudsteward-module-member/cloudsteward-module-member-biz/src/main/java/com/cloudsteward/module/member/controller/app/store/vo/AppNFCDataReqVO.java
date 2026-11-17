package com.cloudsteward.module.member.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppNFCDataReqVO {

    @NotNull(message = "房间ID不能为空")
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "门店ID")
    private Long storeId;

}
