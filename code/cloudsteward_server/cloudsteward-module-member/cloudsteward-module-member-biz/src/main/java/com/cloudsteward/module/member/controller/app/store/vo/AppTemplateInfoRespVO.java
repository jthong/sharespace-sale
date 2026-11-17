package com.cloudsteward.module.member.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AppTemplateInfoRespVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    private Long templateId;

    @Schema(description = "支持版本")
    private String eversion;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @NotNull(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "模板Key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板Key不能为空")
    private String templateKey;

    @Schema(description = "模板预览图")
    private String templateImg;
}
