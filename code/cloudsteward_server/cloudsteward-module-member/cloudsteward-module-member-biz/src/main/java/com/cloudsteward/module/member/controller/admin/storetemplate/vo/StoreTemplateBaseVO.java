package com.cloudsteward.module.member.controller.admin.storetemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 门店模板 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class StoreTemplateBaseVO {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "模板Key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板Key不能为空")
    private String templateKey;

    @Schema(description = "模板预览图")
    private String templateImg;

}
