package com.cloudsteward.module.member.controller.admin.storepaysplit.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.cloudsteward.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static com.cloudsteward.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 门店支付分账配置 Excel 导出 Request VO，参数和 StorePaySplitPageReqVO 是一致的")
@Data
public class StorePaySplitExportReqVO {

    @Schema(description = "门店名称", example = "10078")
    private Long storeId;

    @Schema(description = "易宝商户号", example = "17797")
    private String mchId;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
