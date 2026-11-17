package com.cloudsteward.module.member.controller.app.manager.vo;

import com.cloudsteward.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppPayOrderReqVO extends PageParam {

    @Schema(description = "门店")
    @NotNull(message = "门店不能为空")
    private Long storeId;

    @Schema(description = "订单号")
    private String orderNo;


}
