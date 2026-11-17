package com.cloudsteward.module.member.controller.app.manager.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppVipDeleteReqVO {

    @Schema(description = "门店ID")
    @NotNull(message = "门店不能为空")
    private Long storeId;


    @Schema(description = "用户ID")
    @NotNull(message = "用户不能为空")
    private Long userId;

}
