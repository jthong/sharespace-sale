package com.cloudsteward.module.member.controller.admin.storepayconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 门店支付收款配置创建 Request VO")
@Data
public class StorePayConfigCreateReqVO {
    @Schema(description = "商户号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28488")
    @NotNull(message = "商户号不能为空")
    private String mchId;

    @Schema(description = "是否分账", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean split;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3591")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;
}
