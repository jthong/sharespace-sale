package com.cloudsteward.module.member.controller.admin.storepaysplit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 门店支付分账配置 Response VO")
@Data
@ToString(callSuper = true)
public class StorePaySplitRespVO  {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9986")
    private Long id;

    @Schema(description = "门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10078")
    @NotNull(message = "门店不能为空")
    private Long storeId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "10078")
    @NotNull(message = "门店名称不能为空")
    private String storeName;

    @Schema(description = "易宝商户号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17797")
    @NotNull(message = "易宝商户号不能为空")
    private String mchId;

    @Schema(description = "分账比例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分账比例不能为空")
    private Integer rate;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
