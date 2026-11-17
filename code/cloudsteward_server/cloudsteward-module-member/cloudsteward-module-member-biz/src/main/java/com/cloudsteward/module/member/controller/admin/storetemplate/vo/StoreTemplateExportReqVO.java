package com.cloudsteward.module.member.controller.admin.storetemplate.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloudsteward.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门店模板 Excel 导出 Request VO，参数和 StoreTemplatePageReqVO 是一致的")
@Data
public class StoreTemplateExportReqVO {

    @Schema(description = "模板名称", example = "李四")
    private String templateName;

    @Schema(description = "模板Key")
    private String templateKey;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
