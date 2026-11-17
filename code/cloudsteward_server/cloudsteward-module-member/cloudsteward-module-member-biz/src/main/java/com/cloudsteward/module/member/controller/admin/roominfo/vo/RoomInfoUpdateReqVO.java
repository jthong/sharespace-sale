package com.cloudsteward.module.member.controller.admin.roominfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 房间管理更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoomInfoUpdateReqVO extends RoomInfoBaseVO {

    @Schema(description = "房间id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25625")
    @NotNull(message = "房间id不能为空")
    private Long roomId;

    @Schema(description = "房间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "包间1")
    @NotNull(message = "房间名称不能为空")
    private String roomName;

    @Schema(description = "呼叫名称", example = "包间1")
    private String roomCallName;

    @Schema(description = "房间类型(棋牌/台球/自习室)", example = "0")
    private Integer roomClass;

    @Schema(description = "包间类型(特价包/小包/中包等)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "包间类型不能为空")
    private Integer type;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "工作日价格", example = "1")
    private BigDecimal workPrice;

    @Schema(description = "单价(时价)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "单价不能为空")
    private BigDecimal price;

    @Schema(description = "押金", example = "0")
    private BigDecimal deposit;

    @Schema(description = "通宵价格", example = "0")
    private BigDecimal tongxiaoPrice;

    @Schema(description = "房间标签(设施，逗号分隔)", example = "0,7,12")
    private String label;

    @Schema(description = "房间图片(逗号分隔URL)", example = "https://xxx/a.png")
    private String imageUrls;

    @Schema(description = "排序位置", example = "1")
    private Integer sortId;

    @Schema(description = "喇叭声音(0关1开)", example = "1")
    private Integer yunlabaSound;

    @Schema(description = "禁用开始时间", example = "00:00")
    private String banTimeStart;

    @Schema(description = "禁用结束时间", example = "06:00")
    private String banTimeEnd;

    @Schema(description = "最大提前开始时间(小时)", example = "24")
    private Integer leadHour;

    @Schema(description = "最小下单时间(小时)", example = "1")
    private Integer minHour;

    @Schema(description = "跳过清洁", example = "false")
    private Boolean jumpClear;

    @Schema(description = "是否可预定", example = "true")
    private Boolean reserve;

    @Schema(description = "预付费价格", example = "0")
    private BigDecimal prePrice;

    @Schema(description = "预付费计价分钟", example = "60")
    private Integer preUnit;

    @Schema(description = "最低消费价格", example = "0")
    private BigDecimal minCharge;

    @Schema(description = "包场-通宵价格", example = "0")
    private BigDecimal txPrice;

    @Schema(description = "包场-上午价格", example = "0")
    private BigDecimal morningPrice;

    @Schema(description = "包场-下午价格", example = "0")
    private BigDecimal afternoonPrice;

    @Schema(description = "包场-夜间价格", example = "0")
    private BigDecimal nightPrice;

    @Schema(description = "NFC URL", example = "")
    private String nfcUrl;

    @Schema(description = "是否小时下单", example = "true")
    private Boolean xiaoshiSubmit;

    @Schema(description = "状态(0禁用/1空闲/2待清洁/3使用中/4已预约)", example = "1")
    private Integer status;

}
