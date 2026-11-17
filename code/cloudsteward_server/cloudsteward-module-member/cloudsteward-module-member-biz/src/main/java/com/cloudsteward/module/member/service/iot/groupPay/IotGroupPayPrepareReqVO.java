package com.cloudsteward.module.member.service.iot.groupPay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class IotGroupPayPrepareReqVO {

    @Schema(description = "团购平台类型：1-美团/大众点评，2-抖音", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "团购平台类型不能为空")
    private Integer platform;

    @Schema(description = "门店ID（服务商分配的shopId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "门店ID不能为空")
    private String shopId;

    @Schema(description = "团购券码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "团购券码不能为空")
    private String code;
}
