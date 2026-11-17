package com.cloudsteward.module.member.controller.app.inventory.vo;

import com.cloudsteward.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppUserInventoryDetailReqVO {

    @Schema(description = "用户手机号")
    @NotNull(message = "用户手机号不能为空")
    @Mobile
    private String mobile;


    @Schema(description = "门店ID")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;


}
