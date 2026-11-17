package com.cloudsteward.module.member.controller.app.manager.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppPayRefundReqVO {

    @Schema(description = "ID")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "退款金额 单位:分")
    @NotNull(message = "退款金额不能为空")
    private Integer price;

}
