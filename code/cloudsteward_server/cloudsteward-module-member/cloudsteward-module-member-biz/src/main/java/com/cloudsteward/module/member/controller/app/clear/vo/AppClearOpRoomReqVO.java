package com.cloudsteward.module.member.controller.app.clear.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppClearOpRoomReqVO {

    @Schema(description = "任务ID")
    @NotNull(message = "任务不能为空")
    private Long id;


    @Schema(description = "类型")
    @NotNull(message = "类型不能为空")
    private String type;

}
