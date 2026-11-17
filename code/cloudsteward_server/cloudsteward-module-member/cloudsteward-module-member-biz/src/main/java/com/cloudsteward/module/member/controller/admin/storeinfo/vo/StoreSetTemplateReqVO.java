package com.cloudsteward.module.member.controller.admin.storeinfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StoreSetTemplateReqVO {

    @Schema(description = "门店ID")
    @NotNull(message = "门店不能为空")
    private Long storeId;

    @Schema(description = "门店模板  为空表示简洁模式  custom表示自定义")
    private String templateKey;


}
