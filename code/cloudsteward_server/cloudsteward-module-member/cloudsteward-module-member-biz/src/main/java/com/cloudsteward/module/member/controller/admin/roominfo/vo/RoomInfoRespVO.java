package com.cloudsteward.module.member.controller.admin.roominfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房间管理 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoomInfoRespVO extends RoomInfoBaseVO {

    @Schema(description = "房间id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25625")
    private Long roomId;

    @Schema(description = "房间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String roomName;

    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27907")
    private Long storeId;

    @Schema(description = "房间类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer type;

    @Schema(description = "呼叫名称")
    private String roomCallName;

    @Schema(description = "房间类型(棋牌/台球/自习室)", example = "0")
    private Integer roomClass;

    @Schema(description = "工作日价格")
    private BigDecimal workPrice;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "31263")
    private BigDecimal price;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "通宵价格")
    private BigDecimal tongxiaoPrice;

    @Schema(description = "房间标签")
    private String label;

    @Schema(description = "房间图片(逗号分隔URL)")
    private String imageUrls;

    @Schema(description = "排序位置", example = "32351")
    private Integer sortId;

    @Schema(description = "喇叭声音(0关1开)")
    private Integer yunlabaSound;

    @Schema(description = "禁用开始时间")
    private String banTimeStart;

    @Schema(description = "禁用结束时间")
    private String banTimeEnd;

    @Schema(description = "最大提前开始时间(小时)")
    private Integer leadHour;

    @Schema(description = "最小下单时间(小时)")
    private Integer minHour;

    @Schema(description = "跳过清洁")
    private Boolean jumpClear;

    @Schema(description = "是否可预定")
    private Boolean reserve;

    @Schema(description = "预付费价格")
    private BigDecimal prePrice;

    @Schema(description = "预付费计价分钟")
    private Integer preUnit;

    @Schema(description = "最低消费价格")
    private BigDecimal minCharge;

    @Schema(description = "包场-通宵价格")
    private BigDecimal txPrice;

    @Schema(description = "包场-上午价格")
    private BigDecimal morningPrice;

    @Schema(description = "包场-下午价格")
    private BigDecimal afternoonPrice;

    @Schema(description = "包场-夜间价格")
    private BigDecimal nightPrice;

    @Schema(description = "NFC URL")
    private String nfcUrl;

    @Schema(description = "是否小时下单")
    private Boolean xiaoshiSubmit;

    @Schema(description = "总完成订单数")
    private Integer totalOrderNum;

    @Schema(description = "总收益")
    private BigDecimal totalMoney;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
