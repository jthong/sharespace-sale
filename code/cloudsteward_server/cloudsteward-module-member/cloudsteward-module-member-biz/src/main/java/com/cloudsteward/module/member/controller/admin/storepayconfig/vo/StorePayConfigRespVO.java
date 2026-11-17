package com.cloudsteward.module.member.controller.admin.storepayconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 门店支付收款配置 Response VO")
@Data
@ToString(callSuper = true)
public class StorePayConfigRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25242")
    private Long id;

    @Schema(description = "商户号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28488")
    @NotNull(message = "商户号不能为空")
    private String mchId;

    @Schema(description = "是否分账", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否分账不能为空")
    private Boolean split;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3591")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "3591")
    @NotNull(message = "门店名称不能为空")
    private String storeName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
