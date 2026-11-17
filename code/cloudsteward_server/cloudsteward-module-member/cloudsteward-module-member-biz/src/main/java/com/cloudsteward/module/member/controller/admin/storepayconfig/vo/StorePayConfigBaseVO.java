package com.cloudsteward.module.member.controller.admin.storepayconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 门店支付收款配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class StorePayConfigBaseVO {

    @Schema(description = "商户号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28488")
    @NotNull(message = "商户号不能为空")
    private String mchId;

    @Schema(description = "是否分账", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否分账不能为空")
    private Boolean split;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3591")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

}
