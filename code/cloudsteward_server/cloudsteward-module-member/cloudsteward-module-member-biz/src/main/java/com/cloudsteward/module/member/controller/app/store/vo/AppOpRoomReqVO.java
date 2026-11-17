package com.cloudsteward.module.member.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppOpRoomReqVO {

    @Schema(description = "房间ID")
    @NotNull(message = "房间不能为空")
    private Long roomId;


    //1 开门开电 2关门关电  3仅开门  4仅关门  5仅开灯 6仅关灯
    @NotNull(message = "操作类型不能为空")
    private String type;

}
