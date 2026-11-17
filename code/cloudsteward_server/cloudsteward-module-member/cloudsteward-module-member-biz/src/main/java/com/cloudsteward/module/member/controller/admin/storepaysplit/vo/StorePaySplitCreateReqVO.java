package com.cloudsteward.module.member.controller.admin.storepaysplit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 门店支付分账配置创建 Request VO")
@Data
@ToString(callSuper = true)
public class StorePaySplitCreateReqVO {
    @Schema(description = "门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10078")
    @NotNull(message = "门店不能为空")
    private Long storeId;

    @Schema(description = "易宝商户号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17797")
    @NotNull(message = "易宝商户号不能为空")
    private String mchId;

    @Schema(description = "分账比例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分账比例不能为空")
    @Min(value = 1, message = "分账比例最小为1%")
    @Max(value = 99, message = "分账比例最大为99%")
    private Integer rate;

    @Schema(description = "备注", example = "你猜")
    private String remark;
}
