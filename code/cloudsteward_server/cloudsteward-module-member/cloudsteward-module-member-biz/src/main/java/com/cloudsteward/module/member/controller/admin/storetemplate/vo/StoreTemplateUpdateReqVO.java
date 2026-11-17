package com.cloudsteward.module.member.controller.admin.storetemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 门店模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoreTemplateUpdateReqVO extends StoreTemplateBaseVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5454")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

}
