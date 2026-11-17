package com.cloudsteward.module.member.controller.admin.storepayconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloudsteward.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门店支付收款配置 Excel 导出 Request VO，参数和 StorePayConfigPageReqVO 是一致的")
@Data
public class StorePayConfigExportReqVO {

    @Schema(description = "商户号", example = "28488")
    private String mchId;

    @Schema(description = "是否分账")
    private Boolean split;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "门店ID", example = "3591")
    private Long storeId;

}
